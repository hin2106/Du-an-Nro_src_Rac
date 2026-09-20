package network;

import java.net.Socket;
import interfaces.ISession;

public class SessionFactory {

    private static SessionFactory instance;

    public static SessionFactory gI() {
        if (instance == null) {
            instance = new SessionFactory();
        }
        return instance;
    }

    public <T extends ISession> T cloneSession(Class<T> clazz, Socket socket) throws Exception {
        return clazz.getConstructor(Socket.class).newInstance(socket);
    }

    public <T extends ISession> T cloneSession(Class<T> clazz, Socket socket, String remoteIp) throws Exception {
        T session = cloneSession(clazz, socket);
        if (session instanceof Session) {
            ((Session) session).setRemoteIp(remoteIp);
        }
        return session;
    }
}
