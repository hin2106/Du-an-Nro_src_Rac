package network;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import interfaces.IKeySessionHandler;
import interfaces.IMessageHandler;
import interfaces.IMessageSendCollect;

import interfaces.ISession;
import consts.SocketType;
import interfaces.TypeSession;

public class Session implements ISession {

    private static ISession instance;
    private static int ID_INIT;
    private SocketType socketType = SocketType.SERVER;
    private byte[] KEYS;
    private boolean sentKey;
    public int id;
    private Socket socket;
    private volatile boolean connected;
    private Sender sender;
    private Collector collector;
    private QueueHandler queueHandler;
    private final Thread tSender;
    private final Thread tCollector;
    private IKeySessionHandler keyHandler;
    private String ip;
    private final AtomicBoolean disconnectStarted = new AtomicBoolean(false);
    /** Set to true by MySession when login succeeds; used by AntiDDoSEngine to route close event. */
    protected volatile boolean wasAuthenticated = false;

    public static ISession gI() throws Exception {
        if (instance == null) {
            throw new Exception("Instance has not been initialized!");
        }
        return instance;
    }

    public Session(String host, int port) throws IOException {
        this.id = 31072002;
        this.socket = new Socket(host, port);
        this.socket.setSendBufferSize(0x100000);
        this.socket.setReceiveBufferSize(0x100000);
        this.socket.setTcpNoDelay(true);
        this.socket.setKeepAlive(true);
        this.socketType = SocketType.CLIENT;
        this.connected = true;
        this.sender = this.sender != null ? this.sender.setSocket(this.socket) : new Sender(this, this.socket);
        this.collector = this.collector != null ? this.collector.setSocket(this.socket)
                : new Collector(this, this.socket);
        this.queueHandler = new QueueHandler(this);
        this.tSender = new Thread(this.sender != null ? this.sender.setSocket(this.socket)
                : (this.sender = new Sender(this, this.socket)), "Sender - IP : " + this.ip);
        this.tCollector = new Thread(this.collector != null ? this.collector.setSocket(this.socket)
                : (this.collector = new Collector(this, this.socket)), "Collector - IP : " + this.ip);
    }

    public Session(Socket socket) {
        this.KEYS = "NguyenDucVuEntertainment".getBytes();
        this.id = ID_INIT++;
        this.socket = socket;
        try {
            this.socket.setSendBufferSize(0x100000);
            this.socket.setReceiveBufferSize(0x100000);
            // Keep TCP optimizations but remove SoTimeout to avoid breaking client reads
            this.socket.setTcpNoDelay(true);
            this.socket.setKeepAlive(true);
        } catch (SocketException ignored) {
        }
        this.socketType = SocketType.SERVER;
        this.connected = true;
        this.ip = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().replace("/", "");
        this.sender = this.sender != null ? this.sender.setSocket(this.socket) : new Sender(this, this.socket);
        this.collector = this.collector != null ? this.collector.setSocket(this.socket)
                : new Collector(this, this.socket);
        this.queueHandler = new QueueHandler(this);
        this.tSender = new Thread(this.sender != null ? this.sender.setSocket(this.socket)
                : (this.sender = new Sender(this, this.socket)), "Sender - IP : " + this.ip);
        this.tCollector = new Thread(this.collector != null ? this.collector.setSocket(this.socket)
                : (this.collector = new Collector(this, this.socket)), "Collector - IP : " + this.ip);
    }

    @Override
    public void sendMessage(Message msg) {
        if (this.isConnected() && msg != null) {
            this.sender.sendMessage(msg);
        }
    }

    @Override
    public ISession setSendCollect(IMessageSendCollect collect) {
        this.sender.setSend(collect);
        this.collector.setCollect(collect);
        return this;
    }

    @Override
    public ISession setMessageHandler(IMessageHandler handler) {
        this.queueHandler.setMessageHandler(handler);
        return this;
    }

    @Override
    public ISession setKeyHandler(IKeySessionHandler handler) {
        this.keyHandler = handler;
        return this;
    }

    @Override
    public ISession startSend() {
        this.tSender.start();
        return this;
    }

    @Override
    public ISession startCollect() {
        this.tCollector.start();
        return this;
    }

    @Override
    public ISession startQueueHandler() {
        this.queueHandler.run();
        return this;
    }

    @Override
    public ISession start() {
        this.tSender.start();
        this.tCollector.start();
        this.queueHandler.run();
        return this;
    }

    @Override
    public String getIP() {
        return this.ip;
    }

    /**
     * Replaces the loopback address with the address supplied by the trusted
     * in-process TCP proxy. Never call this with data received from a client.
     */
    public void setRemoteIp(String remoteIp) {
        if (remoteIp == null || remoteIp.isBlank()) {
            return;
        }
        this.ip = remoteIp;
        onRemoteIpChanged(remoteIp);
    }

    protected void onRemoteIpChanged(String remoteIp) {
        // Hook for subclasses that cache the address separately.
    }

    @Override
    public long getID() {
        return this.id;
    }

    @Override
    public void disconnect() {
        if (!disconnectStarted.compareAndSet(false, true)) {
            return;
        }
        this.connected = false;
        this.sentKey = false;
        if (this.sender != null) {
            this.sender.close();
        }
        if (this.collector != null) {
            this.collector.close();
        }
        if (this.queueHandler != null) {
            this.queueHandler.close();
        }
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException ignored) {
            }
        }
            // Notify AntiDDoS engine so per-IP connection counter stays accurate
            if (this.ip != null && this.socketType == consts.SocketType.SERVER) {
                AntiDDoSEngine.gI().onConnectionClosed(this.ip, this.wasAuthenticated);
            }
        this.dispose();
    }

    @Override
    public void dispose() {
        if (this.sender != null) {
            this.sender.dispose();
        }
        if (this.collector != null) {
            this.collector.dispose();
        }
        if (this.queueHandler != null) {
            this.queueHandler.dispose();
        }
        this.socket = null;
        this.sender = null;
        this.collector = null;
        this.queueHandler = null;
        this.ip = null;
        SessionManager.gI().removeSession(this);
    }

    @Override
    public void sendKey() throws Exception {
        if (this.keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        this.keyHandler.sendKey(this);
    }

    @Override
    public void setKey(Message message) throws Exception {
        if (this.keyHandler == null) {
            throw new Exception("Key handler has not been initialized!");
        }
        this.keyHandler.setKey(this, message);
    }

    @Override
    public void setKey(byte[] key) {
        this.KEYS = key;
    }

    @Override
    public boolean sentKey() {
        return this.sentKey;
    }

    @Override
    public void setSentKey(boolean sent) {
        this.sentKey = sent;
    }

    @Override
    public void doSendMessage(Message msg) throws Exception {
        this.sender.doSendMessage(msg);
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public byte[] getKey() {
        return this.KEYS;
    }

    @Override
    public SocketType getSocketType() {
        return this.socketType;
    }

    @Override
    public QueueHandler getQueueHandler() {
        return this.queueHandler;
    }

    @Override
    public TypeSession getTypeSession() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
