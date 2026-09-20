package network;

import java.net.Socket;

import lombok.Setter;
import java.io.DataInputStream;
import java.io.IOException;
import interfaces.IMessageSendCollect;
import interfaces.ISession;
import consts.Cmd_message;
import consts.SocketType;

public final class Collector implements Runnable {

    private ISession session;
    private DataInputStream dis;
    @Setter
    private IMessageSendCollect collect;

    public Collector(ISession session, Socket socket) {
        this.session = session;
        this.setSocket(socket);
    }

    public Collector setSocket(Socket socket) {
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException ignored) {
        }
        return this;
    }

    @Override
    public void run() {
        try {
            while (session != null && session.isConnected()) {
                final Message msg = this.collect.readMessage(this.session, this.dis);
                if (msg.command == Cmd_message.GET_SESSION_ID) {
                    if (this.session instanceof MySession) {
                        ((MySession) this.session).onKeyExchangeCompleted();
                    }
                    if (session.getSocketType() == SocketType.SERVER) {
                        this.session.sendKey();
                    } else {
                        this.session.setKey(msg);
                    }
                    msg.cleanup();
                } else {
                        if (this.session instanceof MySession) {
                            ((MySession) this.session).onPreAuthCommand(msg.command);
                        }
                        // Stage 2: Packet rate + size guard (early drop before game logic)
                        String ip = this.session.getIP();
                        if (ip != null && !AntiDDoSEngine.gI().checkPacketAllowed(
                                ip, msg.command & 0xFF, msg.getDataSize())) {
                            msg.cleanup(); // drop – do not enqueue
                        } else {
                            this.session.getQueueHandler().addMessage(msg);
                        }
                }
            }
        } catch (Exception ignored) {
        }
        try {
            Network.gI().getAcceptHandler().sessionDisconnect(session);
        } catch (Exception ignored) {
        }
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    public void close() {
        if (dis != null) {
            try {
                dis.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void dispose() {
        session = null;
        dis = null;
        collect = null;
    }
}
