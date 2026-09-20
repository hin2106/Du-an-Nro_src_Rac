package player;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import utils.Logger;

public class PlayerUpdateOptimizer {

    private static final int THREAD_COUNT = Math.max(2,
            Math.min(8, Runtime.getRuntime().availableProcessors()));
    private static final ExecutorService workerPool = Executors.newFixedThreadPool(THREAD_COUNT, r -> {
        Thread t = new Thread(r, "PlayerUpdate-Worker");
        t.setDaemon(true);
        return t;
    });

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "PlayerUpdate-Scheduler");
        t.setDaemon(true);
        return t;
    });

    // Intervals
    private static final int FAST_TICK_INTERVAL = 200; // ms - Critical updates
    private static final int SLOW_TICK_INTERVAL = 1000; // ms - Non-critical updates

    private static final AtomicInteger fastTickCount = new AtomicInteger(0);
    private static final AtomicInteger slowTickCount = new AtomicInteger(0);
    private static long lastFastTickDuration = 0;
    private static long lastSlowTickDuration = 0;

    private static final int BATCH_SIZE = 32;
    private static final int DIRECT_UPDATE_THRESHOLD = 5; // Với <= 5 players, update trực tiếp không cần batching

    private static volatile boolean started = false;
    private static volatile boolean running = false;

    // Reusable collections để giảm allocations
    private static final ThreadLocal<List<Player>> playerListCache = ThreadLocal.withInitial(() -> new ArrayList<>(16));
    private static final ThreadLocal<List<CompletableFuture<Void>>> futuresCache = ThreadLocal
            .withInitial(() -> new ArrayList<>(4));

    public static synchronized void start() {
        if (started) {
            System.err.println("[PlayerUpdateOptimizer] Already started!");
            return;
        }
        started = true;
        running = true;

        // Fast tick - Critical updates (200ms)
        scheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                return;
            }
            try {
                long st = System.currentTimeMillis();
                processFastTick();
                lastFastTickDuration = System.currentTimeMillis() - st;
                fastTickCount.incrementAndGet();

                if (lastFastTickDuration > 150) {
                    System.err.println("[WARN] Fast tick slow: " + lastFastTickDuration + "ms");
                }
            } catch (Exception e) {
                Logger.logException(PlayerUpdateOptimizer.class, e, "Error in fast tick");
            }
        }, 0, FAST_TICK_INTERVAL, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                return;
            }
            try {
                long st = System.currentTimeMillis();
                processSlowTick();
                lastSlowTickDuration = System.currentTimeMillis() - st;
                slowTickCount.incrementAndGet();

                if (lastSlowTickDuration > 800) {
                    System.err.println("[WARN] Slow tick slow: " + lastSlowTickDuration + "ms");
                }
            } catch (Exception e) {
                Logger.logException(PlayerUpdateOptimizer.class, e, "Error in slow tick");
            }
        }, 500, SLOW_TICK_INTERVAL, TimeUnit.MILLISECONDS);
        Logger.rainbow("[PlayerUpdateOptimizer] Started successfully");
    }

    private static void processFastTick() {
        Collection<Player> allPlayers = PlayerManager.getPlayers();
        if (allPlayers.isEmpty()) {
            return;
        }

        int totalPlayers = allPlayers.size();

        // Fix: Với ít players, update trực tiếp không cần batching/threading overhead
        if (totalPlayers <= DIRECT_UPDATE_THRESHOLD) {
            // Direct update - không tạo objects không cần thiết
            for (Player p : allPlayers) {
                if (p == null || !p.isPl() || p.beforeDispose) {
                    continue;
                }
                updatePlayerFast(p);
            }
            return;
        }

        List<Player> playerList = playerListCache.get();
        playerList.clear();
        playerList.addAll(allPlayers);

        int actualSize = playerList.size();
        if (actualSize == 0) {
            return;
        }

        List<CompletableFuture<Void>> futures = futuresCache.get();
        futures.clear();

        for (int i = 0; i < actualSize; i += BATCH_SIZE) {
            final int fromIdx = i;
            final int toIdx = Math.min(i + BATCH_SIZE, actualSize);
            final List<Player> batch = new ArrayList<>(playerList.subList(fromIdx, toIdx));

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (Player p : batch) {
                    if (p == null || !p.isPl() || p.beforeDispose) {
                        continue;
                    }
                    updatePlayerFast(p);
                }
            }, workerPool);

            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                    .get(FAST_TICK_INTERVAL - 20, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.err.println("[WARN] Fast tick timeout - some batches incomplete");
        } catch (InterruptedException | ExecutionException e) {
            Logger.logException(PlayerUpdateOptimizer.class, e, "Error waiting for fast tick");
        }
    }

    private static void processSlowTick() {
        Collection<Player> allPlayers = PlayerManager.getPlayers();
        if (allPlayers.isEmpty()) {
            return;
        }

        int totalPlayers = allPlayers.size();
        if (totalPlayers <= DIRECT_UPDATE_THRESHOLD) {
            for (Player p : allPlayers) {
                if (p == null || !p.isPl() || p.beforeDispose) {
                    continue;
                }
                updatePlayerSlow(p);
            }
            return;
        }
        List<Player> playerList = playerListCache.get();
        playerList.clear();
        playerList.addAll(allPlayers);

        // Fix: Dùng size thực tế sau khi addAll để tránh IndexOutOfBoundsException
        int actualSize = playerList.size();
        if (actualSize == 0) {
            return;
        }

        List<CompletableFuture<Void>> futures = futuresCache.get();
        futures.clear();

        for (int i = 0; i < actualSize; i += BATCH_SIZE) {
            final int fromIdx = i;
            final int toIdx = Math.min(i + BATCH_SIZE, actualSize);
            final List<Player> batch = new ArrayList<>(playerList.subList(fromIdx, toIdx));

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (Player p : batch) {
                    if (p == null || !p.isPl() || p.beforeDispose) {
                        continue;
                    }
                    updatePlayerSlow(p);
                }
            }, workerPool);

            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                    .get(SLOW_TICK_INTERVAL - 50, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.err.println("[WARN] Slow tick timeout - some batches incomplete");
        } catch (InterruptedException | ExecutionException e) {
            Logger.logException(PlayerUpdateOptimizer.class, e, "Error waiting for slow tick");
        }
    }

    public static synchronized void stop() {
        if (!started) {
            return;
        }
        System.out.println("[PlayerUpdateOptimizer] Stopping...");
        running = false;
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }

            workerPool.shutdown();
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }

            started = false;
            System.out.println("[PlayerUpdateOptimizer] Stopped successfully");
        } catch (InterruptedException e) {
            Logger.logException(PlayerUpdateOptimizer.class, e, "Error stopping optimizer");
        }
    }

    private static void updatePlayerFast(Player player) {
        if (!player.updateInProgress.compareAndSet(false, true)) {
            return;
        }
        try {
            player.isUpdating = true;
            player.fastUpdate();
        } catch (Exception e) {
            Logger.logException(PlayerUpdateOptimizer.class, e,
                    "Error in fast update: " + player.name);
        } finally {
            player.isUpdating = false;
            player.updateInProgress.set(false);
        }
    }

    private static void updatePlayerSlow(Player player) {
        if (!player.updateInProgress.compareAndSet(false, true)) {
            return;
        }
        try {
            player.slowUpdate();
        } catch (Exception e) {
            Logger.logException(PlayerUpdateOptimizer.class, e,
                    "Error in slow update: " + player.name);
        } finally {
            player.updateInProgress.set(false);
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public static String getPerformanceInfo() {
        return String.format(
                "Fast: %dms | Slow: %dms | Players: %d | Workers: %d",
                lastFastTickDuration,
                lastSlowTickDuration,
                PlayerManager.getPlayers().size(),
                THREAD_COUNT);
    }
}
