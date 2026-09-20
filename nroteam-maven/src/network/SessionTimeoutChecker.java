package network;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import server.ServerManager;
import utils.Logger;

public class SessionTimeoutChecker {

    private static SessionTimeoutChecker instance;

    // Pre-auth timeout: max time a TCP session can exist without completing login.
    // MUST be long enough for a real player to see the login screen and type credentials.
    // Bots either send nothing, or send garbage within milliseconds — they don't need 20s.
    // Real players typically take 5–15s; we give generous headroom even under attack.
    private static final long TIMEOUT_EMERGENCY    = 20_000;  // 20s
    private static final long TIMEOUT_UNDER_ATTACK = 30_000;  // 30s
    private static final long TIMEOUT_NORMAL       = 60_000;  // 60s
    private static final long CHECK_INTERVAL_MS    =  3_000;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "SessionTimeoutChecker");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean running = false;

    private SessionTimeoutChecker() {
    }

    public static SessionTimeoutChecker gI() {
        if (instance == null) {
            synchronized (SessionTimeoutChecker.class) {
                if (instance == null) {
                    instance = new SessionTimeoutChecker();
                }
            }
        }
        return instance;
    }

    /**
     * Bắt đầu kiểm tra timeout sessions
     */
    public void start() {
        if (running) {
            return;
        }
        running = true;
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkTimeoutSessions();
            } catch (Exception e) {
                Logger.logException(SessionTimeoutChecker.class, e, "Error checking session timeouts");
            }
        }, CHECK_INTERVAL_MS, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);

        Logger.success("SessionTimeoutChecker started (NORMAL=" + (TIMEOUT_NORMAL / 1000)
                + "s UNDER_ATTACK=" + (TIMEOUT_UNDER_ATTACK / 1000)
                + "s EMERGENCY=" + (TIMEOUT_EMERGENCY / 1000) + "s)\n");
    }

    /**
     * Dừng kiểm tra
     */
    public void stop() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Lấy số lượng người chơi online
     */
    private int getOnlinePlayerCount() {
        try {
            return player.PlayerManager.size();
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }

    /**
     * Kiểm tra và disconnect các session timeout
     */
    private void checkTimeoutSessions() {
        if (!running) return;
        try {
            java.util.List<interfaces.ISession> sessions = SessionManager.gI().getSessions();
            if (sessions == null || sessions.isEmpty()) return;

            long now = System.currentTimeMillis();
            int kicked = 0;

            for (interfaces.ISession is : sessions) {
                if (!(is instanceof network.MySession)) continue;
                network.MySession session = (network.MySession) is;
                if (session == null || !session.isConnected()) continue;
                if (session.sessionCreateTime == 0) continue;
                if (session.joinedGame || session.player != null) continue; // already authed

                long referenceTime = getReferenceTime(session);
                long timeoutMs = getLoginTimeoutMs(session);
                long age = now - referenceTime;
                if (age <= timeoutMs) continue;

                try {
                    String ip = session.getIP();
                    session.disconnect();
                    kicked++;
                    ConnectionRateLimiter.gI().onDisconnect(ip);
                    ServerManager.gI().disconnectByIp(ip);
                    // penalise IP for hoarding unauthenticated sessions
                    AntiDDoSEngine.gI().addReputationPublic(ip, 20,
                            "Pre-auth session held " + (age / 1000) + "s");
                } catch (Exception e) {
                    Logger.logException(SessionTimeoutChecker.class, e,
                            "Error disconnecting timeout session");
                }
            }
            if (kicked > 0) {
                Logger.warningln("[SessionTimeout] Huỷ " + kicked
                    + " phiên pre-auth quá hạn. "
                        + "Tổng: " + SessionManager.gI().getNumSession()
                        + " Online: " + getOnlinePlayerCount());
            }
        } catch (Exception e) {
            Logger.logException(SessionTimeoutChecker.class, e, "Error in checkTimeoutSessions");
        }
    }

    private long getLoginTimeoutMs(network.MySession session) {
        switch (session.getPreAuthStage()) {
            case LOGIN_INTENT:
            case REGISTER_INTENT:
                return getStageTimeout(30_000L, 45_000L, 60_000L);
            case CLIENT_IDENTIFIED:
            case KEY_EXCHANGED:
                return getStageTimeout(15_000L, 25_000L, 40_000L);
            default:
                return getStageTimeout(8_000L, 12_000L, 20_000L);
        }
    }

    private long getReferenceTime(network.MySession session) {
        if (session.getPreAuthStage().ordinal() >= network.MySession.PreAuthStage.KEY_EXCHANGED.ordinal()) {
            return session.getLastPreAuthActivityTime();
        }
        return session.sessionCreateTime;
    }

    private long getStageTimeout(long emergencyMs, long underAttackMs, long normalMs) {
        switch (AntiDDoSEngine.gI().getMode()) {
            case EMERGENCY:
                return emergencyMs;
            case UNDER_ATTACK:
                return underAttackMs;
            default:
                return normalMs;
        }
    }
}
