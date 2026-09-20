package server;

import boss.earth.BojackManager;
import bot.BotManager;
import daos.HistoryTransactionDAO;
import dungeon.RedRibbonHQ;
import interfaces.ISession;
import interfaces.ISessionAcceptHandler;
import interfaces.INetwork;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.Map;
import managers.ConsignShopManager;
import managers.SuperRankManager;
import managers.boss.*;
import managers.tournament.DeathOrAliveArenaManager;
import managers.tournament.The23rdMartialArtCongressManager;
import managers.tournament.WorldMartialArtsTournamentManager;
import minigame.DecisionMaker;
import minigame.LuckyNumber;
import network.*;
import network.proxy.NettyDDoSProxy;
import player.Player;
import player.PlayerScheduler;
import services.WebRewardService;
import backup.BackupScheduler;
import audit.AssetAuditService;
import services.TrialAccountCleanupService;
import services.map.MapUpdateOptimizer;
import services.player.ClanService;
import utils.Logger;
import utils.TimeUtil;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import utils.ConsoleLogFormatter;

public class ServerManager {
    

    public static String timeStart;
    public static String DOMAIN = "https://ngocrongluna.site";
    public static String NAME = "NRO Luna";
    public static String IP = "157.10.53.65";
    public static String BIND_IP = null;
    public static int PORT = 14445;
    public static boolean USE_NETTY_PROXY = false; 
    public static int BACKEND_PORT = 14446;

    private static ServerManager instance;
    public static final AtomicBoolean isRunning = new AtomicBoolean(false);
    public static Network nettyServer;
    private NettyDDoSProxy ddosProxy;
    public static final Map<Object, Object> CLIENTS = new ConcurrentHashMap<>();

    private ExecutorService managerThreadPool;
    private ScheduledExecutorService scheduledExecutor;
    private static final long STARTUP_DELAY_MS = 50;
    private static final long GRACEFUL_SHUTDOWN_TIMEOUT_SECONDS = 15;
    private static final long FORCE_SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final Object CMD_LOG_LOCK = new Object();
    private static final LineState CMD_LOG_LINE_STATE = new LineState();
    private static final DateTimeFormatter CMD_LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static volatile boolean cmdLogRedirected = false;

    private final AtomicInteger activeManagerThreads = new AtomicInteger(0);

    public static ServerManager gI() {
        if (instance == null) {
            synchronized (ServerManager.class) {
                if (instance == null) {
                    instance = new ServerManager();
                    instance.init();
                }
            }
        }
        return instance;
    }


    public void init() {
        Manager.gI();
        HistoryTransactionDAO.deleteHistory();
    }

    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }
        try {
            if (timeStart == null) {
                timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
            }

            activeServerSocket();
            SessionTimeoutChecker.gI().start();
            dragonpass.DragonPassRepository.ensureSchema();
            top.TopBoardService.gI().initialize();
            luckywheel.LuckyWheelService.gI().initialize();
            boss.drop.BossDropRepository.ensureSchema();
            services.TrialAccountCleanupService.gI().start();
            Manager.MAPS.forEach(map.Map::initBoss);
            startAllManagers();

            // Schedule trao quà top Whis tuần vào 00:01 thứ 2
            if (scheduledExecutor == null) {
                scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            }
            scheduledExecutor.scheduleAtFixedRate(() -> {
                try {
                    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                    if (now.getDayOfWeek() == DayOfWeek.MONDAY && now.getHour() == 0 && now.getMinute() == 1) {
                        top.TopBoardService.gI().rewardWhisWeekly();
                        Logger.successln("Đã trao quà top Whis tuần lúc 00:01 thứ 2!");
                    }
                } catch (Exception e) {
                    Logger.error("Lỗi khi trao quà top Whis tuần: " + e.getMessage());
                }
            }, 0, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            Logger.error("Lỗi khi khởi động server: " + e.getMessage() + "\n");
            e.printStackTrace();
            isRunning.set(false);
        }
    }

    private void startAllManagers() {
        int index = 0;
        startManagerThread(SuperRankManager.gI(), "SuperRankManager", index++);
        startManagerThread(The23rdMartialArtCongressManager.gI(), "DHVT23Manager", index++);
        startManagerThread(DeathOrAliveArenaManager.gI(), "VoĐaiSinhTuManager", index++);
        startManagerThread(WorldMartialArtsTournamentManager.gI(), "WMATManager", index++);
        startManagerThread(AutoMaintenance.gI(), "AutoMaintenanceManager", index++);
        startManagerThread(BossManager.gI(), "BossManager", index++);
        startManagerThread(SkillSummonedManager.gI(), "SkillSummonedManager", index++);
        startManagerThread(BrolyManager.gI(), "BrolyManager", index++);
        startManagerThread(AnTromManager.gI(), "AnTromManager", index++);
        startManagerThread(OtherBossManager.gI(), "OtherBossManager", index++);
        startManagerThread(FinalBossManager.gI(), "FinalBossManager", index++);
        startManagerThread(RedRibbonHQManager.gI(), "RedRibbonHQManager", index++);
        startManagerThread(TreasureUnderSeaManager.gI(), "TreasureUnderSeaManager", index++);
        startManagerThread(SnakeWayManager.gI(), "SnakeWayManager", index++);
        startManagerThread(GasDestroyManager.gI(), "GasDestroyManager", index++);
        startManagerThread(BotManager.gI(), "BotManager", index++);
        startManagerThread(TrungThuEventManager.gI(), "TrungThuEventManager", index++);
        startManagerThread(HalloweenEventManager.gI(), "HalloweenEventManager", index++);
        startManagerThread(ChristmasEventManager.gI(), "ChristmasEventManager", index++);
        startManagerThread(HungVuongEventManager.gI(), "HungVuongEventManager", index++);
        startManagerThread(YardartManager.gI(), "YardartManager", index++);
        startManagerThread(LunarNewYearEventManager.gI(), "LunarNewYearEventManager", index++);
        startManagerThread(LuckyNumber.gI(), "LuckyNumberManager", index++);
        startManagerThread(DecisionMaker.gI(), "DecisionMakerManager", index++);
        startManagerThread(BojackManager.gI(), "BojackManager", index++);
        startManagerThread(Boss12hManager.gI(), "Boss12hManager", index++);
        startManagerThread(Boss14hManager.gI(), "Boss14hManager", index++);
        Logger.successln("Load success " + index + " managers.");

    }

    private void startManagerThread(Runnable manager, String threadName, int delayIndex) {
        long delay = delayIndex * STARTUP_DELAY_MS;
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                manager.run();
            } catch (Exception e) {
                Logger.error("[ERROR] Manager " + threadName + " crashed: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }, threadName).start();
    }

    private void activeServerSocket() {
        try {
            Network networkInstance = Network.gI();
            INetwork network = networkInstance.init();

            // When proxy is enabled, backend binds to localhost only
            if (USE_NETTY_PROXY) {
                networkInstance.setBindIp("127.0.0.1");
            } else if (BIND_IP != null && !BIND_IP.trim().isEmpty()) {
                networkInstance.setBindIp(BIND_IP);
            }

            network.setAcceptHandler(new ISessionAcceptHandler() {
                @Override
                public void sessionInit(ISession is) {
                    if (is instanceof MySession) {
                        MySession mySession = (MySession) is;
                        mySession.sessionCreateTime = System.currentTimeMillis();
                    }

                    is.setMessageHandler(Controller.gI())
                            .setSendCollect(new MessageSendCollect())
                            .setKeyHandler(new MyKeyHandler())
                            .startCollect()
                            .startQueueHandler();
                }

                @Override
                public void sessionDisconnect(ISession session) {
                    Client.gI().kickSession((MySession) session);
                }
            }).setTypeSessionClone(MySession.class)
                    .setDoSomeThingWhenClose(() -> {
                        Logger.error("SERVER CLOSE\n");
                        System.exit(0);
                    });

            // Start backend on BACKEND_PORT (or PORT if proxy disabled)
            if (USE_NETTY_PROXY && (BACKEND_PORT < 1 || BACKEND_PORT > 65535 || BACKEND_PORT == PORT)) {
                throw new IllegalStateException("Invalid backend port: " + BACKEND_PORT);
            }
            int backendPort = USE_NETTY_PROXY ? BACKEND_PORT : PORT;
            network.start(backendPort);
            Logger.log("Network socket started on port " + backendPort + "\n");

            // Start Netty proxy if enabled
            if (USE_NETTY_PROXY) {
                ddosProxy = new NettyDDoSProxy(PORT, "127.0.0.1", BACKEND_PORT);
                ddosProxy.startAsync();
                if (!ddosProxy.awaitStarted(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("DDoS proxy did not start on port " + PORT);
                }
                Logger.log("DDoS proxy started on port " + PORT
                        + " → 127.0.0.1:" + BACKEND_PORT + "\n");
            }
        } catch (Exception e) {
            Logger.error("Không thể khởi động server socket: " + e.getMessage() + "\n");
            throw new RuntimeException("Failed to start server socket", e);
        }
    }

    public boolean canConnectWithIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return false;
        }

        if (Manager.BEHIND_FIREWALL) {
            return true;
        }

        Object o = CLIENTS.get(ipAddress);
        if (o == null) {
            return true;
        }

        try {
            int currentConnections = Integer.parseInt(String.valueOf(o));
            return currentConnections < Manager.MAX_PER_IP;
        } catch (NumberFormatException e) {
            Logger.warning("Invalid connection count for IP " + ipAddress + ": " + o + "\n");
            CLIENTS.remove(ipAddress);
            return true;
        }
    }

    public void incrementConnectionCount(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || Manager.BEHIND_FIREWALL) {
            return;
        }

        CLIENTS.compute(ipAddress, (key, value) -> {
            if (value == null) {
                return 1;
            }
            try {
                int count = Integer.parseInt(String.valueOf(value));
                return count + 1;
            } catch (NumberFormatException e) {
                Logger.warning("Reset connection count for IP " + ipAddress + " due to invalid value\n");
                return 1;
            }
        });
    }

    public void disconnect(MySession session) {
        if (session != null && session.getIP() != null) {
            disconnectByIp(session.getIP());
        }
    }

    public void disconnectByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || Manager.BEHIND_FIREWALL) {
            return;
        }

        CLIENTS.compute(ipAddress, (key, value) -> {
            if (value == null) {
                return null;
            }
            try {
                int count = Integer.parseInt(String.valueOf(value));
                count--;
                return count <= 0 ? null : count;
            } catch (NumberFormatException e) {
                Logger.warning("Remove invalid entry for IP " + ipAddress + "\n");
                return null;
            }
        });
    }

    public void close() {
        if (!isRunning.compareAndSet(true, false)) {
            Logger.warning("Server đã dừng hoặc đang trong quá trình dừng\n");
            return;
        }
        Logger.log("========== SHUTDOWN SERVER ==========\n");
        long shutdownStartTime = System.currentTimeMillis();

        // Shutdown DDoS proxy first (stop accepting new connections)
        if (ddosProxy != null) {
            ddosProxy.shutdown();
            Logger.log("DDoS proxy stopped\n");
        }

        shutdownScheduledExecutor();
        shutdownManagerThreadPool();
        shutdownServices();
        saveAllPlayers();
        closeClientConnections();

        long shutdownDuration = System.currentTimeMillis() - shutdownStartTime;
        Logger.log("========== SHUTDOWN HOÀN TẤT (Thời gian: " + shutdownDuration + "ms) ==========\n");
        ServerManagerUI.thuTucTuDongKhoiDongLai();
        System.exit(0);
    }

    private void shutdownScheduledExecutor() {
        if (scheduledExecutor == null || scheduledExecutor.isShutdown()) {
            return;
        }
        Logger.log("Đang dừng Scheduled Executor...\n");
        scheduledExecutor.shutdown();

        try {
            if (!scheduledExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                Logger.warning("Scheduled Executor không dừng trong 2 giây, force shutdown...\n");
                scheduledExecutor.shutdownNow();
                if (!scheduledExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    Logger.error("Không thể force shutdown Scheduled Executor\n");
                }
            } else {
                Logger.log("Scheduled Executor đã dừng thành công\n");
            }
        } catch (InterruptedException e) {
            Logger.error("Interrupted khi shutdown Scheduled Executor: " + e.getMessage() + "\n");
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownManagerThreadPool() {
        if (managerThreadPool == null || managerThreadPool.isShutdown()) {
            return;
        }

        Logger.log("Đang dừng Manager Thread Pool (Active threads: "
                + activeManagerThreads.get() + ")...\n");

        managerThreadPool.shutdown();

        try {
            if (!managerThreadPool.awaitTermination(GRACEFUL_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                Logger.warning("Một số manager threads chưa dừng sau "
                        + GRACEFUL_SHUTDOWN_TIMEOUT_SECONDS + " giây, force shutdown...\n");

                List<Runnable> pendingTasks = managerThreadPool.shutdownNow();
                Logger.warning("Force shutdown với " + pendingTasks.size() + " pending tasks\n");

                if (!managerThreadPool.awaitTermination(FORCE_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    Logger.error("Không thể force shutdown một số manager threads (Active: "
                            + activeManagerThreads.get() + ")\n");
                }
            } else {
                Logger.log("Manager Thread Pool đã dừng thành công\n");
            }
        } catch (InterruptedException e) {
            Logger.error("Interrupted khi shutdown Manager Thread Pool: " + e.getMessage() + "\n");
            managerThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownServices() {
        Logger.log("Đang shutdown các services...\n");
        safeShutdown("BackupScheduler", () -> BackupScheduler.gI().shutdown());
        safeShutdown("AssetAuditService", () -> AssetAuditService.gI().shutdown());
        safeShutdown("ClanService", () -> ClanService.gI().close());
        safeShutdown("MapUpdateOptimizer", () -> MapUpdateOptimizer.gI().shutdown());
        safeShutdown("ConsignShopManager", () -> ConsignShopManager.gI().save());
        safeShutdown("SessionTimeoutChecker", () -> SessionTimeoutChecker.gI().stop());
        safeShutdown("TrialAccountCleanupService", () -> TrialAccountCleanupService.gI().stop());
        safeShutdown("ConnectionRateLimiter", () -> ConnectionRateLimiter.gI().shutdown());
        Logger.log("Các services đã shutdown\n");
    }

    private void safeShutdown(String serviceName, Runnable shutdownAction) {
        try {
            shutdownAction.run();
            Logger.log(serviceName + " đã shutdown thành công\n");
        } catch (Exception e) {
            Logger.error("Lỗi khi shutdown " + serviceName + ": " + e.getMessage() + "\n");
        }
    }

    private void saveAllPlayers() {
        try {
            Logger.log("Đang validate và save tất cả players...\n");

            if (Client.gI() == null) {
                Logger.warning("Client instance is null, skip player save\n");
                return;
            }

            List<Player> allPlayers = Client.gI().getPlayers();
            if (allPlayers == null || allPlayers.isEmpty()) {
                Logger.log("Không có players để save\n");
                return;
            }

            int totalPlayers = allPlayers.size();
            int validatedCount = 0;
            int errorCount = 0;

            for (Player player : allPlayers) {
                try {
                    if (player != null && player.playerSkill != null) {
                        int removedCount = utils.SkillUtil.validateAndFilterSkillsByGender(player);
                        if (removedCount > 0) {
                            Logger.warning("Player " + player.name + " có " + removedCount
                                    + " skill không hợp lệ đã được loại bỏ\n");
                        }
                        daos.PlayerDAO.updatePlayer(player);
                        validatedCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    Logger.error("Lỗi khi save player "
                            + (player != null ? player.name : "null") + ": " + e.getMessage() + "\n");
                }
            }

            Logger.log("Đã save " + validatedCount + "/" + totalPlayers
                    + " players (Errors: " + errorCount + ")\n");

        } catch (Exception e) {
            Logger.error("Lỗi trong quá trình save players: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void closeClientConnections() {
        try {
            if (Client.gI() != null) {
                Logger.log("Đang đóng tất cả client connections...\n");
                Client.gI().close();
                Logger.log("Client connections đã đóng\n");
            }

            PlayerScheduler.stopScheduler();
            Logger.log("PlayerScheduler đã dừng\n");

        } catch (Exception e) {
            Logger.error("Lỗi khi đóng client connections: " + e.getMessage() + "\n");
        }
    }

    public boolean isServerRunning() {
        return isRunning.get();
    }

    public int getActiveManagerThreadCount() {
        return activeManagerThreads.get();
    }

    public static void main(String[] args) {
        setupCmdLogging();
        System.setProperty("java.awt.headless", "false");
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("user.timezone", "Asia/Ho_Chi_Minh");
        ServerManager.timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
        ServerManager.gI().run();
        try {
            data.AlyraManager.gI().initialize();
            Logger.log("Database initialized successfully\n");
            AssetAuditService.gI().start();
        } catch (Exception e) {
            Logger.error("Lỗi khi khởi tạo database: " + e.getMessage() + "\n");
            e.printStackTrace();
            System.exit(1);
        }
        loadServerConfig();
        ServerManager.gI();
        SwingUtilities.invokeLater(() -> {
            try {
                ServerManagerUI ui = new ServerManagerUI();
                ui.setVisible(true);
            } catch (Exception e) {
                Logger.error("Lỗi khi khởi động UI: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        });
        try {
            WebRewardService.gI().start();
        } catch (Exception e) {
            Logger.error("Lỗi khi khởi động WebRewardService: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        BackupScheduler.gI().start();
    }

    private static void setupCmdLogging() {
        if (cmdLogRedirected) {
            return;
        }

        synchronized (CMD_LOG_LOCK) {
            if (cmdLogRedirected) {
                return;
            }

            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;

            try {
                OutputStream fileOutput = new FileOutputStream("log.txt", true);
                PrintStream teeOut = new PrintStream(
                    new TeeOutputStream(originalOut, fileOutput, CMD_LOG_LOCK, CMD_LOG_LINE_STATE),
                        true,
                        StandardCharsets.UTF_8.name()
                );
                PrintStream teeErr = new PrintStream(
                    new TeeOutputStream(originalErr, fileOutput, CMD_LOG_LOCK, CMD_LOG_LINE_STATE),
                        true,
                        StandardCharsets.UTF_8.name()
                );

                System.setOut(teeOut);
                System.setErr(teeErr);
                cmdLogRedirected = true;
                System.out.println("[ServerManager] Dang ghi log CMD vao log.txt");
            } catch (Exception e) {
                originalErr.println("[ServerManager] Khong the mo log.txt de ghi log: " + e.getMessage());
            }
        }
    }

    private static class TeeOutputStream extends OutputStream {

        private final OutputStream first;
        private final OutputStream second;
        private final Object lock;
        private final LineState lineState;
        private final ByteArrayOutputStream pending = new ByteArrayOutputStream(256);

        TeeOutputStream(OutputStream first, OutputStream second, Object lock, LineState lineState) {
            this.first = first;
            this.second = second;
            this.lock = lock;
            this.lineState = lineState;
        }

        @Override
        public void write(int b) throws IOException {
            synchronized (lock) {
                pending.write(b);
                if (b == '\n' || b == '\r') flushPending();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            synchronized (lock) {
                for (int i = off; i < off + len; i++) {
                    pending.write(b[i]);
                    if (b[i] == '\n' || b[i] == '\r') flushPending();
                }
            }
        }

        private void flushPending() throws IOException {
            if (pending.size() == 0) return;
            String raw = pending.toString(StandardCharsets.UTF_8);
            pending.reset();
            byte[] normalized = ConsoleLogFormatter.normalize(raw).getBytes(StandardCharsets.UTF_8);
            first.write(normalized);
            for (byte b : normalized) writeWithTimestampToFile(b);
        }

        private void writeWithTimestampToFile(int b) throws IOException {
            if (lineState.atLineStart && b != '\n' && b != '\r') {
                String prefix = "[" + LocalDateTime.now().format(CMD_LOG_TIME_FORMAT) + "] ";
                second.write(prefix.getBytes(StandardCharsets.UTF_8));
                lineState.atLineStart = false;
            }

            second.write(b);
            if (b == '\n') {
                lineState.atLineStart = true;
            }
        }

        @Override
        public void flush() throws IOException {
            synchronized (lock) {
                flushPending();
                first.flush();
                second.flush();
            }
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }

    private static class LineState {
        private boolean atLineStart = true;
    }

    private static void loadServerConfig() {
        try {
            java.util.Properties properties = new java.util.Properties();
            properties.load(new java.io.FileInputStream("data/config/sever.properties"));

            Object value;
            if ((value = properties.get("server.name")) != null) {
                ServerManager.NAME = String.valueOf(value);
            }
            if ((value = properties.get("server.ip")) != null) {
                ServerManager.IP = String.valueOf(value);
            }
            if ((value = properties.get("server.port")) != null) {
                ServerManager.PORT = Integer.parseInt(String.valueOf(value));
            }
            if ((value = properties.get("server.bind.ip")) != null) {
                String bindIp = String.valueOf(value);
                if (bindIp != null && !bindIp.trim().isEmpty()) {
                    ServerManager.BIND_IP = bindIp;
                }
            }
            Logger.log("Server configuration loaded successfully\n");
        } catch (Exception e) {
            Logger.warningln("Không thể load server config từ properties file, sử dụng giá trị mặc định\n");
        }
    }

}
