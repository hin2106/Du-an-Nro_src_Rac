package network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import server.Manager;
import utils.Logger;

public class ConnectionRateLimiter {

    private static ConnectionRateLimiter instance;
    private final Map<String, ConnectionInfo> connectionHistory = new ConcurrentHashMap<>();

    private static final int MAX_CONNECTIONS_PER_IP = 5; 
    private static final long TIME_WINDOW_MS = 10000; // 10 giây
    private static final long CLEANUP_INTERVAL_MS = 60000;
    private static final int MAX_TOTAL_CONNECTIONS = 5000; 

    private final ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "ConnectionRateLimiter-Cleanup");
        t.setDaemon(true);
        return t;
    });

    private ConnectionRateLimiter() {
        // Bắt đầu cleanup scheduler
        cleanupScheduler.scheduleAtFixedRate(this::cleanupOldEntries,
                CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public static ConnectionRateLimiter gI() {
        if (instance == null) {
            synchronized (ConnectionRateLimiter.class) {
                if (instance == null) {
                    instance = new ConnectionRateLimiter();
                }
            }
        }
        return instance;
    }

    public boolean canConnect(String ipAddress) {
        if (Manager.BEHIND_FIREWALL) {
            return true; // per-IP rate skipped; global rate handled by AntiDDoSEngine
        }

        long now = System.currentTimeMillis();

        // Tighten per-IP limits in elevated defense modes
        AntiDDoSEngine.DefenseMode curMode = AntiDDoSEngine.gI().getMode();
        int  maxPerIp;
        long windowMs;
        switch (curMode) {
            case EMERGENCY:
                maxPerIp = 1;
                windowMs = 30_000; // 1 new connection per 30 s per IP
                break;
            case UNDER_ATTACK:
                maxPerIp = 3;
                windowMs = TIME_WINDOW_MS;
                break;
            default:
                maxPerIp = MAX_CONNECTIONS_PER_IP;
                windowMs = TIME_WINDOW_MS;
        }

        ConnectionInfo info = connectionHistory.computeIfAbsent(ipAddress, k -> new ConnectionInfo());
        info.cleanupOldConnections(now - windowMs);

        if (info.getConnectionCount() >= maxPerIp) {
            Logger.warningln("Connection rate limit [" + curMode.name() + "] exceeded for IP: " + ipAddress
                    + " (" + info.getConnectionCount() + "/" + maxPerIp + " in " + (windowMs / 1000) + "s)");
            return false;
        }
        info.addConnection(now);
        return true;
    }

    public void onDisconnect(String ipAddress) {
        if (ipAddress == null) {
            return;
        }
        if (Manager.BEHIND_FIREWALL) {
            return;
        }

        ConnectionInfo info = connectionHistory.get(ipAddress);
        if (info != null) {
            long now = System.currentTimeMillis();
            info.rollbackLastConnection(now);
            info.cleanupOldConnections(now - TIME_WINDOW_MS);
        }
    }

    /**
     * Cleanup các entries cũ không còn hoạt động
     */
    private void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        long cutoffTime = now - TIME_WINDOW_MS * 2; // Xóa entries cũ hơn 2 time windows

        connectionHistory.entrySet().removeIf(entry -> {
            ConnectionInfo info = entry.getValue();
            info.cleanupOldConnections(cutoffTime);
            return info.getConnectionCount() == 0;
        });
    }

    private static class ConnectionInfo {
        private final java.util.List<Long> connectionTimestamps = new java.util.ArrayList<>();

        public synchronized void addConnection(long timestamp) {
            connectionTimestamps.add(timestamp);
        }

        public synchronized void cleanupOldConnections(long cutoffTime) {
            connectionTimestamps.removeIf(timestamp -> timestamp < cutoffTime);
        }

        public synchronized int getConnectionCount() {
            return connectionTimestamps.size();
        }

        public synchronized void rollbackLastConnection(long currentTime) {
            if (!connectionTimestamps.isEmpty()) {
                int lastIndex = connectionTimestamps.size() - 1;
                if (lastIndex >= 0) {
                    long lastTimestamp = connectionTimestamps.get(lastIndex);
                    if (currentTime - lastTimestamp < 1000) {
                        connectionTimestamps.remove(lastIndex);
                    }
                }
            }
        }
    }

    public void shutdown() {
        if (cleanupScheduler != null && !cleanupScheduler.isShutdown()) {
            cleanupScheduler.shutdown();
            try {
                if (!cleanupScheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    cleanupScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getConnectionCount(String ipAddress) {
        ConnectionInfo info = connectionHistory.get(ipAddress);
        if (info == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        info.cleanupOldConnections(now - TIME_WINDOW_MS);
        return info.getConnectionCount();
    }

    public int getMaxTotalConnections() {
        switch (AntiDDoSEngine.gI().getMode()) {
            case EMERGENCY:    return AntiDDoSEngine.DefenseMode.EMERGENCY.maxTotalSessions;
            case UNDER_ATTACK: return AntiDDoSEngine.DefenseMode.UNDER_ATTACK.maxTotalSessions;
            default:           return AntiDDoSEngine.DefenseMode.NORMAL.maxTotalSessions;
        }
    }
}
