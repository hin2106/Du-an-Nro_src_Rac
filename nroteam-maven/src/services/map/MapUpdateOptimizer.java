package services.map;

import map.Map;
import map.Zone;
import server.Manager;
import player.PlayerManager;
import utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tối ưu hóa việc update maps bằng cách:
 * 1. Parallel processing - xử lý nhiều maps song song
 * 2. Skip inactive maps - bỏ qua maps không có player/activity
 * 3. Dynamic thread pool - điều chỉnh số thread theo tải
 * 4. Batch processing - nhóm maps để xử lý hiệu quả hơn
 */
public class MapUpdateOptimizer {

    private static volatile MapUpdateOptimizer instance;

    // Thread pool với số lượng thread tối ưu (CPU cores * 2)
    private final int THREAD_POOL_SIZE = Math.max(2,
            Math.min(8, Runtime.getRuntime().availableProcessors()));
    private ScheduledExecutorService scheduler;
    private final ExecutorService mapUpdateExecutor;

    // Track active maps để optimize
    private final ConcurrentHashMap<Integer, Long> activeMaps = new ConcurrentHashMap<>();
    private static final long MAP_INACTIVE_TIMEOUT = 300000L; // 5 phút
    private static final long MAP_CHECK_INTERVAL = 10000L; // 10 giây kiểm tra lại

    // Statistics
    private final AtomicInteger totalUpdates = new AtomicInteger(0);
    private final AtomicInteger skippedUpdates = new AtomicInteger(0);

    // Reusable collections để giảm allocations
    private final ThreadLocal<List<Map>> mapsCache = ThreadLocal.withInitial(() -> new ArrayList<>(128));
    private final ThreadLocal<List<Future<Void>>> futuresCache = ThreadLocal.withInitial(() -> new ArrayList<>(128));

    private MapUpdateOptimizer() {
        this.mapUpdateExecutor = Executors.newFixedThreadPool(
                THREAD_POOL_SIZE,
                r -> {
                    Thread t = new Thread(r, "MapUpdateWorker");
                    t.setDaemon(true);
                    return t;
                });
    }

    public static MapUpdateOptimizer gI() {
        if (instance == null) {
            synchronized (MapUpdateOptimizer.class) {
                if (instance == null) {
                    instance = new MapUpdateOptimizer();
                }
            }
        }
        return instance;
    }

    /**
     * Khởi động scheduled task để update maps
     */
    public void start(int updateIntervalMs) {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "MapUpdateScheduler");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateAllMaps();
            } catch (Exception e) {
                Logger.logException(MapUpdateOptimizer.class, e, "Lỗi update maps");
            }
        }, 0, updateIntervalMs, TimeUnit.MILLISECONDS);

        Logger.rainbow("MapUpdateOptimizer started with " + THREAD_POOL_SIZE + " threads");
    }

    /**
     * Update tất cả maps với parallel processing
     */
    private void updateAllMaps() {
        int playerCount = PlayerManager.size();
        if (playerCount == 0) {
            cleanupInactiveMaps();
            return;
        }
        long startTime = System.currentTimeMillis();
        List<Map> mapsToUpdate = mapsCache.get();
        mapsToUpdate.clear();
        long now = System.currentTimeMillis();
        for (Map map : Manager.MAPS) {
            if (shouldScheduleMap(map, now)) {
                mapsToUpdate.add(map);
            }
        }

        if (mapsToUpdate.isEmpty()) {
            return;
        }
        List<Future<Void>> futures = futuresCache.get();
        futures.clear();

        for (Map map : mapsToUpdate) {
            futures.add(mapUpdateExecutor.submit(() -> {
                updateMap(map);
                return null;
            }));
        }
        int completed = 0;
        for (Future<Void> future : futures) {
            try {
                future.get(2000, TimeUnit.MILLISECONDS); // Bound one slow map update
                completed++;
            } catch (TimeoutException e) {
                Logger.logError("Map update timeout");
                future.cancel(true);
            } catch (ExecutionException | InterruptedException e) {
                Logger.logException(MapUpdateOptimizer.class, e, "Lỗi update map");
            }
        }
        long duration = System.currentTimeMillis() - startTime;
        totalUpdates.addAndGet(mapsToUpdate.size());
        if (duration > 1000) {
            Logger.logError(String.format(
                    "Map update took %dms (target: 1000ms). Completed: %d/%d maps",
                    duration, completed, mapsToUpdate.size()));
        }
    }
    /** Filters inactive maps before allocating and submitting worker tasks. */
    private boolean shouldScheduleMap(Map map, long now) {
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return false;
        }

        Long lastActiveTime = activeMaps.get(map.mapId);
        if (lastActiveTime != null && now - lastActiveTime < MAP_CHECK_INTERVAL) {
            return true;
        }

        for (Zone zone : map.zones) {
            if (zone != null && (!zone.getHumanoids().isEmpty() || !zone.items.isEmpty())) {
                activeMaps.put(map.mapId, now);
                return true;
            }
        }

        if (lastActiveTime != null && now - lastActiveTime > MAP_INACTIVE_TIMEOUT) {
            Zone.evictAssets(map.mapId);
            activeMaps.remove(map.mapId, lastActiveTime);
        }
        skippedUpdates.incrementAndGet();
        return false;
    }

    /** Cleanup inactive maps when the server has no online players. */
    private void cleanupInactiveMaps() {
        long now = System.currentTimeMillis();
        activeMaps.entrySet().removeIf(entry -> {
            long inactiveTime = now - entry.getValue();
            if (inactiveTime > MAP_INACTIVE_TIMEOUT) {
                Zone.evictAssets(entry.getKey());
                skippedUpdates.incrementAndGet();
                return true;
            }
            return false;
        });
    }
    /**
     * Update một map cụ thể với optimization
     */
    private void updateMap(Map map) {
        try {
            // Skip maps không có zone hoặc không active
            if (map.zones == null || map.zones.isEmpty()) {
                return;
            }
            boolean isActive = false;
            long now = System.currentTimeMillis();
            // Kiểm tra xem map có active không
            Long lastActiveTime = activeMaps.get(map.mapId);
            if (lastActiveTime != null && (now - lastActiveTime) < MAP_CHECK_INTERVAL) {
                // Map vừa được check gần đây và active, update ngay
                isActive = true;
            } else {
                // Kiểm tra lại activity
                for (Zone zone : map.zones) {
                    if (zone != null && (!zone.getHumanoids().isEmpty() || !zone.items.isEmpty())) {
                        isActive = true;
                        activeMaps.put(map.mapId, now);
                        break;
                    }
                }
            }
            // Chỉ update maps active hoặc maps cần cleanup (inactive > 5 phút)
            boolean needsUpdate = isActive;
            if (!isActive && lastActiveTime != null) {
                long inactiveTime = now - lastActiveTime;
                if (inactiveTime > MAP_INACTIVE_TIMEOUT) {
                    // Cleanup inactive map
                    needsUpdate = true;
                } else {
                    skippedUpdates.incrementAndGet();
                    return; // Skip inactive maps
                }
            }
            if (!needsUpdate && lastActiveTime == null) {
                // Map chưa từng active, skip
                skippedUpdates.incrementAndGet();
                return;
            }

            // Update map với optimization
            updateMapOptimized(map, isActive);

        } catch (Exception e) {
            Logger.logException(MapUpdateOptimizer.class, e,
                    "Lỗi update map " + map.mapName);
        }
    }

    /**
     * Update map với tối ưu cho zones
     */
    private void updateMapOptimized(Map map, boolean isActive) {
        try {
            boolean hasActiveZones = false;

            for (Zone zone : map.zones) {
                if (zone == null) {
                    continue;
                }
                // Skip zones không có activity
                if (zone.getHumanoids().isEmpty() && zone.items.isEmpty()) {
                    continue;
                }
                hasActiveZones = true;
                zone.update();
            }
            long now = System.currentTimeMillis();
            if (hasActiveZones) {
                activeMaps.put(map.mapId, now);
            } else if (isActive) {
                // Map không còn active zones
                Long lastActiveTime = activeMaps.get(map.mapId);
                if (lastActiveTime != null && (now - lastActiveTime) > MAP_INACTIVE_TIMEOUT) {
                    // Cleanup
                    Zone.evictAssets(map.mapId);
                    activeMaps.remove(map.mapId);
                }
            }

        } catch (Exception e) {
            Logger.logException(MapUpdateOptimizer.class, e,
                    "Lỗi update map optimized " + map.mapName);
        }
    }

    /**
     * Đánh dấu map là active (khi có player join)
     */
    public void markMapActive(int mapId) {
        activeMaps.put(mapId, System.currentTimeMillis());
    }

    /**
     * Lấy statistics
     */
    public String getStats() {
        int total = totalUpdates.get();
        int skipped = skippedUpdates.get();
        int active = activeMaps.size();
        double skipRatio = total > 0 ? (skipped * 100.0 / total) : 0;

        return String.format(
                "Map Updates - Total: %d, Skipped: %d (%.1f%%), Active Maps: %d/%d",
                total, skipped, skipRatio, active, Manager.MAPS.size());
    }

    /**
     * Shutdown optimizer
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (mapUpdateExecutor != null) {
            mapUpdateExecutor.shutdown();
            try {
                if (!mapUpdateExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    mapUpdateExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                mapUpdateExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        Logger.logln("MapUpdateOptimizer shutdown completed");
    }
}
