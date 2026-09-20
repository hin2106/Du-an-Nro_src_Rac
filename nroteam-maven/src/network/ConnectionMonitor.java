package network;

import server.Client;
import utils.Logger;
import java.util.List;

/**
 * Monitor và log thông tin về connections để debug vấn đề giới hạn kết nối
 */
public class ConnectionMonitor {

    private static ConnectionMonitor instance;

    public static ConnectionMonitor gI() {
        if (instance == null) {
            synchronized (ConnectionMonitor.class) {
                if (instance == null) {
                    instance = new ConnectionMonitor();
                }
            }
        }
        return instance;
    }

    /**
     * Log thông tin chi tiết về connections
     */
    public void logConnectionStats() {
        try {
            int totalSessions = SessionManager.gI().getNumSession();
            int onlinePlayers = 0;
            try {
                if (Client.gI() != null) {
                    onlinePlayers = Client.gI().getPlayers().size();
                }
            } catch (Exception e) {
                // Ignore
            }
            int maxConnections = ConnectionRateLimiter.gI().getMaxTotalConnections();
            int maxPlayers = server.Manager.MAX_PLAYER;

            // Đếm số sessions chưa login
            List<interfaces.ISession> sessions = SessionManager.gI().getSessions();
            int notLoggedInSessions = 0;
            int loggedInSessions = 0;

            for (interfaces.ISession session : sessions) {
                if (session instanceof MySession) {
                    MySession mySession = (MySession) session;
                    if (mySession.player != null || mySession.joinedGame) {
                        loggedInSessions++;
                    } else {
                        notLoggedInSessions++;
                    }
                }
            }

            Logger.warningln("=== Connection Stats ===");
            Logger.warningln("Total Sessions: " + totalSessions + "/" + maxConnections);
            Logger.warningln("  - Logged in: " + loggedInSessions);
            Logger.warningln("  - Not logged in: " + notLoggedInSessions);
            Logger.warningln("Online Players: " + onlinePlayers + "/" + maxPlayers);
            Logger.warningln("Available connection slots: " + (maxConnections - totalSessions));
            Logger.warningln("=========================");
        } catch (Exception e) {
            Logger.logException(ConnectionMonitor.class, e, "Error logging connection stats");
        }
    }
}
