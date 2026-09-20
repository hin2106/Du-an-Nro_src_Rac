package network;

import daos.NDVSqlFetcher;
import java.net.Socket;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import managers.GiftCodeManager;
import player.Player;
import server.Client;
import server.Controller;
import server.Maintenance;
import server.Manager;
import system.AntiLogin;
import data.DataGame;
import item.Item;
import services.GiftCodeService;
import services.Service;
import consts.Cmd_message;
import utils.Logger;
import utils.TimeUtil;

public class MySession extends Session {

    public enum PreAuthStage {
        CONNECTED,
        KEY_EXCHANGED,
        CLIENT_IDENTIFIED,
        LOGIN_INTENT,
        REGISTER_INTENT,
        AUTHENTICATED
    }

    // Thread-safe login attempt tracker per IP; cleaned every 10 minutes.
    // Prevents unbounded memory growth when attackers rotate through thousands of IPs.
    private static final ConcurrentHashMap<String, AntiLogin> ANTILOGIN = new ConcurrentHashMap<>();

    // Global semaphore caps concurrent heavy DB login operations.
    // Each login/loginTrial does 5-6 queries + full player deserialisation.
    // Without this, 3000 simultaneous connections can exhaust the DB pool.
    private static final int SEMAPHORE_PERMITS = 20;
    private static final Semaphore LOGIN_SEMAPHORE = new Semaphore(SEMAPHORE_PERMITS, true);

    static {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AntiLogin-Cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(() -> {
            try {
                ANTILOGIN.entrySet().removeIf(e -> e.getValue().isExpired());
            } catch (Exception ignored) {}
        }, 10, 10, TimeUnit.MINUTES);
    }

    // ---- fields ----
    public static final byte[] KEYS = { 0 };
    public byte curR, curW;
    public byte timeWait = 100;
    public boolean sentKey;
    public String ipAddress;
    public boolean isAdmin;
    public boolean actived;
    public Player player;
    public int userId;
    public String uu;
    public String pp;
    public int typeClient;
    public byte zoomLevel;
    public long lastTimeLogout;
    public boolean joinedGame;
    public long sessionCreateTime;
    public int goldBar;
    public long gold;
    public int eventPoint;
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;
    public double bdPlayer;
    public int version;
    public int cash;
    public int vnd;
    public int danap;
    public int vip;
    public int luotquay;
    public boolean finishUpdate;
    private volatile PreAuthStage preAuthStage = PreAuthStage.CONNECTED;
    private volatile long lastPreAuthActivityTime = System.currentTimeMillis();
    private volatile long loginIntentTime = 0L;

    // ---- constructor ----
    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    protected void onRemoteIpChanged(String remoteIp) {
        this.ipAddress = remoteIp;
    }

    // ---- login semaphore helpers ----

    /**
     * Attempt to acquire a login permit with a mode-aware timeout.
     * Callers MUST call {@link #releaseLoginPermit()} in a finally block on success.
     */
    private boolean acquireLoginPermit() {
        AntiDDoSEngine.DefenseMode mode = AntiDDoSEngine.gI().getMode();
        int maxWaitMs;
        if (mode == AntiDDoSEngine.DefenseMode.EMERGENCY) {
            maxWaitMs = 300;
        } else if (mode == AntiDDoSEngine.DefenseMode.UNDER_ATTACK) {
            maxWaitMs = 800;
        } else {
            maxWaitMs = 3_000;
        }
        try {
            return LOGIN_SEMAPHORE.tryAcquire(maxWaitMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void releaseLoginPermit() {
        LOGIN_SEMAPHORE.release();
    }

    private void advancePreAuthStage(PreAuthStage nextStage) {
        if (nextStage.ordinal() > this.preAuthStage.ordinal()) {
            this.preAuthStage = nextStage;
        }
    }

    public void onKeyExchangeCompleted() {
        this.lastPreAuthActivityTime = System.currentTimeMillis();
        advancePreAuthStage(PreAuthStage.KEY_EXCHANGED);
    }

    public void onPreAuthCommand(byte cmd) {
        if (this.wasAuthenticated || this.joinedGame) {
            this.preAuthStage = PreAuthStage.AUTHENTICATED;
            return;
        }
        long now = System.currentTimeMillis();
        this.lastPreAuthActivityTime = now;
        switch (cmd) {
            case Cmd_message.CLIENT_INFO, Cmd_message.SET_CLIENTTYPE ->
                advancePreAuthStage(PreAuthStage.CLIENT_IDENTIFIED);
            case Cmd_message.LOGIN, Cmd_message.LOGIN_DE -> {
                this.loginIntentTime = now;
                advancePreAuthStage(PreAuthStage.LOGIN_INTENT);
            }
            case Cmd_message.REGISTER -> {
                this.loginIntentTime = now;
                advancePreAuthStage(PreAuthStage.REGISTER_INTENT);
            }
            case Cmd_message.NOT_LOGIN, Cmd_message.LOGIN2 -> {
                this.loginIntentTime = now;
                advancePreAuthStage(PreAuthStage.LOGIN_INTENT);
            }
            case 42 -> {
                this.loginIntentTime = now;
                advancePreAuthStage(PreAuthStage.REGISTER_INTENT);
            }
            default -> {
            }
        }
    }

    public PreAuthStage getPreAuthStage() {
        return preAuthStage;
    }

    public long getLastPreAuthActivityTime() {
        return lastPreAuthActivityTime;
    }

    public boolean isLoginFlowStarted() {
        return preAuthStage.ordinal() >= PreAuthStage.CLIENT_IDENTIFIED.ordinal();
    }

    public boolean hasLoginIntent() {
        return preAuthStage == PreAuthStage.LOGIN_INTENT || preAuthStage == PreAuthStage.REGISTER_INTENT;
    }

    // ---- key exchange ----

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (IOException e) {
        }
    }

    // ---- login with account credentials ----

    public void login(String username, String password) {
        if (username == null) {
            username = "";
        }
        if (password == null) {
            password = "";
        }
        username = username.trim();
        password = password.trim();

        if (username.isEmpty() && password.isEmpty()) {
            Service.gI().switchToRegisterScr(this);
            return;
        }

        if (username.startsWith("trial_")) {
            Service.gI().switchToRegisterScr(this);
            return;
        }

        AntiLogin al = ANTILOGIN.computeIfAbsent(this.ipAddress, k -> new AntiLogin());

        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }

        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }

        if (Maintenance.isRunning()) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }

        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }

        if (this.player == null) {
            Player pl = null;
            boolean permitAcquired = false;
            try {
                if (!acquireLoginPermit()) {
                    Service.gI().sendThongBaoOK(this, "Máy chủ đang bận, vui lòng thử lại sau");
                    return;
                }
                permitAcquired = true;
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;

                pl = NDVSqlFetcher.login(this, al);
                long databaseLoadMs = System.currentTimeMillis() - st;
                if (pl != null) {
                    long assetStartedAt = System.currentTimeMillis();
                    DataGame.sendSmallVersion(this);
                    DataGame.sendBgItemVersion(this);
                    long assetVersionMs = System.currentTimeMillis() - assetStartedAt;
                    this.timeWait = 0;
                    this.joinedGame = true;
                    this.preAuthStage = PreAuthStage.AUTHENTICATED;
                    this.wasAuthenticated = true;                    // move from pre-auth → authenticated pool
                    AntiDDoSEngine.gI().onPlayerAuthenticated(this.getIP());
                    pl.nPoint.calPoint();
                    pl.nPoint.setHp(pl.nPoint.hp);
                    pl.nPoint.setMp(pl.nPoint.mp);
                    pl.zone.addPlayer(pl);
                    if (pl.pet != null) {
                        pl.pet.nPoint.calPoint();
                        pl.pet.nPoint.setHp(pl.pet.nPoint.hp);
                        pl.pet.nPoint.setMp(pl.pet.nPoint.mp);
                    }
                    pl.setSession(this);
                    Client.gI().put(pl);
                    this.player = pl;
                    DataGame.sendVersionGame(this);
                    DataGame.sendDataItemBG(this);
                    Controller.gI().sendInfo(this);
                    dragonpass.DragonPassService.gI().onLogin(this.player);
                    long totalLoginMs = System.currentTimeMillis() - st;
                    Logger.warningln(
                            TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Successful login for player "
                                    + this.player.name + ": " + totalLoginMs + " ms");
                    if (totalLoginMs >= 1_000) {
                        Logger.warningln("Slow login detail - database/player: " + databaseLoadMs
                                + " ms, asset versions: " + assetVersionMs
                                + " ms, initialize/send: " + (totalLoginMs - databaseLoadMs - assetVersionMs)
                                + " ms");
                    }
                    if (this.player.notify != null && !this.player.notify.equals("null")
                            && !this.player.notify.isEmpty() && this.player.notify.length() > 0) {
                        Service.gI().sendThongBao(this.player, this.player.notify);
                        this.player.notify = null;
                    }
                    GiftCodeService.gI().checkAndGivePendingItems(this.player);
                    GiftCodeManager.gI().clearPlayerReloadFlag(pl.id);

                    // Notify VIP expiry asynchronously — use thread-pool, not a raw new Thread()
                    final Player finalPl = pl;
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(500);

                            Service.gI().sendThongBaoOK(finalPl,
                                    "Chào mừng " + finalPl.name + " đã quay trở lại máy chủ!\n"
                                    + "Chúc bạn có những giờ phút chơi game vui vẻ.");
                        } catch (Exception ignored) {
                        }
                    });
                }
            } catch (Exception e) {
                if (pl != null) {
                    pl.dispose();
                }
            } finally {
                if (permitAcquired) {
                    releaseLoginPermit();
                }
            }
        }
    }

    // ---- guest/trial login (high-cost: 5-6 DB queries + full player load) ----

    public void loginTrial() {
        Service.gI().switchToRegisterScr(this);
    }

    private void loginTrialLegacyDisabled() {
        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }

        if (Maintenance.isRunning()) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }

        // Block trial logins entirely during EMERGENCY — each one does 5-6 DB queries.
        AntiDDoSEngine.DefenseMode curMode = AntiDDoSEngine.gI().getMode();
        if (curMode == AntiDDoSEngine.DefenseMode.EMERGENCY) {
            Service.gI().sendThongBaoOK(this, "Máy chủ đang quá tải, vui lòng tạo tài khoản hoặc thử lại sau");
            this.disconnect();
            return;
        }

        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }

        if (this.player == null) {
            data.AlyraResultSet rs = null;
            boolean permitAcquired = false;
            try {
                if (!acquireLoginPermit()) {
                    Service.gI().sendThongBaoOK(this, "Máy chủ đang bận, vui lòng thử lại sau");
                    return;
                }
                permitAcquired = true;
                int trialAccountId = this.userId > 0 && this.uu != null && this.uu.startsWith("trial_")
                        ? this.userId : -1;

                if (trialAccountId <= 0) {
                    trialAccountId = NDVSqlFetcher.createTrialAccount(this);
                    if (trialAccountId <= 0) {
                        Service.gI().sendThongBaoOK(this,
                                "Không thể tạo tài khoản trial. Bạn đã đạt giới hạn hoặc vui lòng thử lại sau");
                        return;
                    }
                }

                this.userId = trialAccountId;
                rs = data.AlyraManager.executeQuery(
                        "select username, password from account where id = ? limit 1", trialAccountId);
                if (rs.first()) {
                    this.uu = rs.getString("username");
                    this.pp = rs.getString("password");
                    try {
                        this.sendMessage(Service.gI().playGuest(this.uu, this.pp));
                    } catch (Exception e) {
                        Logger.logException(MySession.class, e, "Lỗi khi gửi playGuest");
                    }
                } else {
                    rs.dispose();
                    Service.gI().sendThongBaoOK(this, "Không tìm thấy tài khoản trial");
                    return;
                }
                rs.dispose();
                rs = null;

                this.isAdmin = false;
                this.actived = true;
                this.goldBar = 0;
                this.gold = 0;
                this.cash = 0;
                this.vnd = 0;
                this.danap = 0;
                this.vip = 0;

                rs = data.AlyraManager.executeQuery(
                        "select * from player where account_id = ? limit 1", trialAccountId);
                if (!rs.first()) {
                    rs.dispose();
                    DataGame.sendVersionGame(this);
                    DataGame.sendDataItemBG(this);
                    Service.gI().switchToCreateChar(this);
                } else {
                    rs.dispose();
                    Player pl = NDVSqlFetcher.login(this, null);
                    if (pl != null) {
                        DataGame.sendSmallVersion(this);
                        DataGame.sendBgItemVersion(this);
                        this.timeWait = 0;
                        this.joinedGame = true;
                        this.preAuthStage = PreAuthStage.AUTHENTICATED;
                        this.wasAuthenticated = true;                    // move from pre-auth → authenticated pool
                        AntiDDoSEngine.gI().onPlayerAuthenticated(this.getIP());
                        pl.nPoint.calPoint();
                        pl.nPoint.setHp(pl.nPoint.hp);
                        pl.nPoint.setMp(pl.nPoint.mp);
                        pl.zone.addPlayer(pl);
                        if (pl.pet != null) {
                            pl.pet.nPoint.calPoint();
                            pl.pet.nPoint.setHp(pl.pet.nPoint.hp);
                            pl.pet.nPoint.setMp(pl.pet.nPoint.mp);
                        }
                        pl.setSession(this);
                        Client.gI().put(pl);
                        this.player = pl;
                        DataGame.sendVersionGame(this);
                        DataGame.sendDataItemBG(this);
                        Controller.gI().sendInfo(this);
                        dragonpass.DragonPassService.gI().onLogin(this.player);
                        Logger.warningln(
                                TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin()
                                        + "m: Successful trial login for player "
                                        + this.player.name);
                        if (this.player.notify != null && !this.player.notify.equals("null")
                                && !this.player.notify.isEmpty() && this.player.notify.length() > 0) {
                            Service.gI().sendThongBao(this.player, this.player.notify);
                            this.player.notify = null;
                        }
                        GiftCodeService.gI().checkAndGivePendingItems(this.player);
                        GiftCodeManager.gI().clearPlayerReloadFlag(pl.id);
                    }
                }
            } catch (Exception e) {
                Logger.logException(MySession.class, e, "Lỗi khi login trial");
                Service.gI().sendThongBaoOK(this, "Đã xảy ra lỗi, vui lòng thử lại sau");
            } finally {
                if (rs != null) {
                    try {
                        rs.dispose();
                    } catch (Exception ignored) {
                    }
                }
                if (permitAcquired) {
                    releaseLoginPermit();
                }
            }
        }
    }
}
