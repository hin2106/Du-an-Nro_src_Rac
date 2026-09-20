package network;

import interfaces.IServerClose;
import java.net.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.io.InputStream;
import interfaces.ISession;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import interfaces.ISessionAcceptHandler;
import interfaces.INetwork;
import network.SessionFactory;
import network.SessionManager;
import network.ConnectionRateLimiter;
import network.ConnectionMonitor;
import server.ServerManager;
import server.Client;
import utils.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Network implements INetwork, Runnable {

    private static Network instance;
    private int port;
    private String bindIp;
    private ServerSocketChannel serverSocketChannel;
    private Class<? extends ISession> sessionClone;
    private boolean start;
    private IServerClose serverClose;
    private ISessionAcceptHandler acceptHandler;
    private Thread loopServer;
    private Selector selector;

    // Rate limiting cho log messages - chỉ log mỗi 5 giây cho cùng một IP
    private static final long LOG_RATE_LIMIT_MS = 5000; // 5 giây
    private final Map<String, Long> lastLogTime = new ConcurrentHashMap<>();
    private final AtomicLong nextGlobalRejectionLogMs = new AtomicLong(0);

    public static Network gI() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    private Network() {
        this.port = -1;
        this.bindIp = null;
        this.sessionClone = Session.class;
    }

    @Override
    public INetwork init() {
        try {
            this.selector = Selector.open();
        } catch (IOException ex) {
            Logger.errorln(ex.toString());
        }
        this.loopServer = new Thread(this, "Network");
        return this;
    }

    @Override
    public INetwork start(final int port) throws Exception {
        if (port < 0) {
            throw new Exception("Please initialize the server port!");
        }
        if (this.acceptHandler == null) {
            throw new Exception("AcceptHandler has not been initialized!");
        }
        if (!ISession.class.isAssignableFrom(this.sessionClone)) {
            throw new Exception("The type 'session clone' is invalid!");
        }
        try {
            this.port = port;
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(false);

            // Bind to specific IP if configured, otherwise bind to all interfaces
            InetSocketAddress bindAddress;
            if (this.bindIp != null && !this.bindIp.trim().isEmpty() && !this.bindIp.equals("0.0.0.0")) {
                bindAddress = new InetSocketAddress(this.bindIp, port);
                Logger.log("Binding server to IP: " + this.bindIp + " on port " + port + "\n");
            } else {
                bindAddress = new InetSocketAddress(port);
                Logger.log("Binding server to all interfaces on port " + port + "\n");
            }

            this.serverSocketChannel.socket().bind(bindAddress);
            this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ex) {
            Logger.error("Error initializing server at " + (this.bindIp != null ? this.bindIp + ":" : "") + port + ": "
                    + ex.getMessage() + "\n");
            System.exit(0);
        }
        this.start = true;
        this.loopServer.start();
        String bindInfo = this.bindIp != null && !this.bindIp.trim().isEmpty() && !this.bindIp.equals("0.0.0.0")
                ? this.bindIp + ":" + this.port
                : "all interfaces on port " + this.port;
        Logger.success("Server initialized and listening on " + bindInfo + "\n");
        return this;
    }

    @Override
    public INetwork close() {
        this.start = false;
        if (this.serverSocketChannel != null) {
            try {
                this.serverSocketChannel.close();
            } catch (IOException ex) {
            }
        }
        if (this.serverClose != null) {
            this.serverClose.serverClose();
        }
        return this;
    }

    @Override
    public INetwork dispose() {
        this.acceptHandler = null;
        this.loopServer = null;
        this.serverSocketChannel = null;
        return this;
    }

    @Override
    public INetwork setAcceptHandler(final ISessionAcceptHandler handler) {
        this.acceptHandler = handler;
        return this;
    }

    @Override
    public void run() {
        while (start) {
            try {
                int selected = selector.select(100); // 100ms timeout

                if (selected > 0) {
                    for (SelectionKey key : selector.selectedKeys()) {
                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            try {
                                ServerSocketChannel server = (ServerSocketChannel) key.channel();
                                java.nio.channels.SocketChannel clientChannel = server.accept();

                                if (clientChannel != null) {
                                    clientChannel.configureBlocking(true);
                                    Socket socket = clientChannel.socket();
                                    String ipAddress;
                                    if (ServerManager.USE_NETTY_PROXY) {
                                        if (!socket.getInetAddress().isLoopbackAddress()) {
                                            socket.close();
                                            continue;
                                        }
                                        ipAddress = readTrustedProxyAddress(socket);
                                        if (ipAddress == null) {
                                            socket.close();
                                            continue;
                                        }
                                    } else {
                                        ipAddress = ((java.net.InetSocketAddress) socket.getRemoteSocketAddress())
                                                .getAddress().getHostAddress();
                                    }
                                        // Stage 1a: Engine hard-block check (blocked IP / temp-ban / mode limit)
                                        if (!ServerManager.USE_NETTY_PROXY
                                                && !AntiDDoSEngine.gI().checkConnectionAllowed(ipAddress)) {
                                        try {
                                            socket.close();
                                                logRejectionIfAllowed(ipAddress, "antiddos blocked");
                                        } catch (IOException ignored) {
                                        }
                                        continue;
                                    }
                                        // Stage 1b: Per-IP connection velocity limiter
                                        if (!ServerManager.USE_NETTY_PROXY
                                                && !ConnectionRateLimiter.gI().canConnect(ipAddress)) {
                                            try {
                                                socket.close();
                                                logRejectionIfAllowed(ipAddress, "rate limit");
                                            } catch (IOException ignored) {
                                            }
                                            AntiDDoSEngine.gI().onConnectionClosed(ipAddress, false);
                                            continue;
                                        }
                                    if (!ServerManager.gI().canConnectWithIp(ipAddress)) {
                                        try {
                                            socket.close();
                                            logRejectionIfAllowed(ipAddress, "max connections per IP");
                                        } catch (IOException ignored) {
                                        }
                                        ConnectionRateLimiter.gI().onDisconnect(ipAddress);
                                        AntiDDoSEngine.gI().onConnectionClosed(ipAddress, false);
                                        continue;
                                    }
                                    ServerManager.gI().incrementConnectionCount(ipAddress);

                                    int currentSessions = SessionManager.gI().getNumSession();
                                    int maxConnections = ConnectionRateLimiter.gI().getMaxTotalConnections();
                                    if (currentSessions >= maxConnections) {
                                        try {
                                            socket.close();
                                            int onlinePlayers = Client.gI() != null
                                                    ? Client.gI().getPlayers().size()
                                                    : 0;
                                            logRejectionIfAllowed(ipAddress,
                                                    "server at max capacity (Sessions: " + currentSessions + "/"
                                                            + maxConnections +
                                                            ", Online: " + onlinePlayers + ")");
                                            if (lastLogTime.get(ipAddress) == null ||
                                                    (System.currentTimeMillis()
                                                            - lastLogTime.get(ipAddress)) >= LOG_RATE_LIMIT_MS) {
                                                ConnectionMonitor.gI().logConnectionStats();
                                            }
                                        } catch (IOException ignored) {
                                        }
                                        // Rollback cả 2 counters
                                        ConnectionRateLimiter.gI().onDisconnect(ipAddress);
                                        ServerManager.gI().disconnectByIp(ipAddress);
                                        AntiDDoSEngine.gI().onConnectionClosed(ipAddress, false);
                                        continue;
                                    }

                                    socket.setTcpNoDelay(true);
                                    socket.setKeepAlive(true);
                                    try {
                                        final ISession session = SessionFactory.gI().cloneSession(this.sessionClone,
                                                socket, ipAddress);
                                        this.acceptHandler.sessionInit(session);
                                        SessionManager.gI().putSession(session);
                                    } catch (Exception sessionError) {
                                        try { socket.close(); } catch (IOException ignored) {}
                                        ConnectionRateLimiter.gI().onDisconnect(ipAddress);
                                        ServerManager.gI().disconnectByIp(ipAddress);
                                        AntiDDoSEngine.gI().onConnectionClosed(ipAddress, false);
                                        throw sessionError;
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.error("Error accepting connection: " + ex.getMessage() + "\n");
                            }
                        }
                    }
                    selector.selectedKeys().clear();
                }
            } catch (IOException ex) {
                if (start) {
                    Logger.error("Selector error: " + ex.getMessage() + "\n");
                }
            } catch (Exception ex2) {
                Logger.errorln("Network error: " + ex2.toString());
            }
        }
    }

    @Override
    public INetwork setDoSomeThingWhenClose(final IServerClose serverClose) {
        this.serverClose = serverClose;
        return this;
    }

    @Override
    public INetwork setTypeSessionClone(final Class<? extends ISession> clazz) throws Exception {
        this.sessionClone = clazz;
        return this;
    }

    @Override
    public ISessionAcceptHandler getAcceptHandler() throws Exception {
        if (this.acceptHandler == null) {
            throw new Exception("AcceptHandler has not been initialized!");
        }
        return this.acceptHandler;
    }

    @Override
    public void stopConnect() {
        this.start = false;
    }

    /**
     * Set the IP address to bind to. If null, empty, or "0.0.0.0", binds to all
     * interfaces.
     * This is useful when using firewall or when you need to bind to a specific
     * network interface.
     * 
     * @param bindIp The IP address to bind to, or null/empty to bind to all
     *               interfaces
     * @return This Network instance for method chaining
     */
    public INetwork setBindIp(String bindIp) {
        this.bindIp = bindIp;
        return this;
    }

    /**
     * Log rejection message với rate limiting để tránh spam log
     * Chỉ log mỗi LOG_RATE_LIMIT_MS milliseconds cho cùng một IP
     */
    private void logRejectionIfAllowed(String ipAddress, String reason) {
        long now = System.currentTimeMillis();
        long next = nextGlobalRejectionLogMs.get();
        if (now < next || !nextGlobalRejectionLogMs.compareAndSet(next, now + 250L)) return;
        Long lastLog = lastLogTime.get(ipAddress);

        if (lastLog == null || (now - lastLog) >= LOG_RATE_LIMIT_MS) {
            Logger.warningln("Connection rejected - " + reason + ": " + ipAddress);
            lastLogTime.put(ipAddress, now);

            // Cleanup old entries (giữ map không quá lớn)
            if (lastLogTime.size() > 4096) {
                long cutoff = now - LOG_RATE_LIMIT_MS * 10; // Xóa entries cũ hơn 50 giây
                lastLogTime.entrySet().removeIf(entry -> entry.getValue() < cutoff);
            }
        }
    }

    /** Reads a PROXY protocol v1 header from the loopback-only backend port. */
    private String readTrustedProxyAddress(Socket socket) {
        try {
            socket.setSoTimeout(2_000);
            InputStream input = socket.getInputStream();
            StringBuilder line = new StringBuilder(108);
            while (line.length() < 108) {
                int value = input.read();
                if (value < 0) return null;
                line.append((char) value);
                int length = line.length();
                if (length >= 2 && line.charAt(length - 2) == '\r' && line.charAt(length - 1) == '\n') {
                    break;
                }
            }
            socket.setSoTimeout(0);
            String[] parts = line.toString().trim().split("\\s+");
            if (parts.length != 6 || !"PROXY".equals(parts[0])
                    || !("TCP4".equals(parts[1]) || "TCP6".equals(parts[1]))) {
                return null;
            }
            InetAddress address = InetAddress.getByName(parts[2]);
            if (!address.getHostAddress().equalsIgnoreCase(parts[2])
                    && !parts[2].contains(":")) {
                return null;
            }
            int sourcePort = Integer.parseInt(parts[4]);
            if (sourcePort < 1 || sourcePort > 65535) return null;
            return address.getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }
}
