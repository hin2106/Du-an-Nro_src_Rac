package network;

import interfaces.ISession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    public static HashMap<String, Integer> firewall = new HashMap<>();
    public static HashMap<String, Integer> firewallDownDataGame = new HashMap<>();

    private static SessionManager instance;
    private final ConcurrentHashMap<Long, ISession> sessionsById;
    private final List<ISession> sessions;

    public static SessionManager gI() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public SessionManager() {
        this.sessionsById = new ConcurrentHashMap<>();
        this.sessions = new ArrayList<>();                                
    }

    public void putSession(ISession session) {
        synchronized (this.sessions) {
            this.sessions.add(session);
        }
        this.sessionsById.put(session.getID(), session);
    }

    public void removeSession(ISession session) {
        synchronized (this.sessions) {
            this.sessions.remove(session);
        }
        this.sessionsById.remove(session.getID());
    }

    public List<ISession> getSessions() {
        // Return a copy to avoid concurrent modification issues
        synchronized (this.sessions) {
            return new ArrayList<>(this.sessions);
        }
    }

    /**
     * Optimized O(1) lookup instead of O(n) linear search
     */
    public ISession findByID(long id) throws Exception {
        ISession session = this.sessionsById.get(id);
        if (session == null) {
            throw new Exception("Session " + id + " does not exist");
        }
        return session;
    }

    public int getNumSession() {
        return this.sessionsById.size();
    }

}
