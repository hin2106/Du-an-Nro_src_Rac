package boss.event.noel;

import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import boss.Boss;
import static consts.BossType.CHRISTMAS_EVENT;
import player.Player;
import server.Client;
import services.Service;
import services.map.ChangeMapService;
import services.player.PlayerService;
import utils.Logger;
import utils.Util;

public class TuanLoc extends Boss {

    private static final int ESCORT_TIMEOUT = 900_000;
    private static final int MAX_DISTANCE_FOLLOW = 600;
    private static final int WARN_DISTANCE = 450;
    private static final int TELEPORT_DISTANCE = 400;
    private static final int FOLLOW_DISTANCE = 85;
    private static final int MAP_CHANGE_COOLDOWN = 10_000;
    private static final int CONSECUTIVE_MAP_CHANGE_WINDOW = 30_000;
    private static final int MAX_CONSECUTIVE_MAP_CHANGES = 2;
    private static final int ESCORT_START_DELAY = 3_000;
    private static final int MOVE_COOLDOWN = 500;
    private static final int WARN_COOLDOWN = 5000;
    private static final int MAX_MOVE_SPEED = 200; // Khoảng cách tối đa có thể di chuyển trong 1 tick (200ms)
    private static final int TELEPORT_DETECTION_DISTANCE = 300; // Khoảng cách tối thiểu để coi là teleport
    private static final long POSITION_CHECK_INTERVAL = 200; // Kiểm tra vị trí mỗi 200ms

    private long st;
    private int timeLeave;
    private long lastTimeMove;
    private int moveDirection;
    public long playerId;
    private long lastWarnTime;
    private long lastFollowTeleport;
    private int lastFollowMapId = -1;
    private long lastTimeNearPlayer = 0;
    private int consecutiveMapChanges = 0;
    private long firstMapChangeTime = 0;
    private long escortStartTime = 0;

    private boolean isRestingPhase = false;
    private long phaseStartTime = 0;
    private long lastChatTime = 0;
    private int lastPlayerX = -1;
    private int lastPlayerY = -1;
    private int lastPlayerMapId = -1;
    private long lastPositionCheckTime = 0;

    public TuanLoc() throws Exception {
        super(CHRISTMAS_EVENT, BossID.TUAN_LOC, BossesData.TUAN_LOC);
    }

    @Override
    public void joinMap() {
        if (zoneFinal != null) {
            joinMapByZone(zoneFinal);
            this.notifyJoinMap();
            this.changeStatus(BossStatus.CHAT_S);
            return;
        }
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }
        if (this.zone != null) {
            try {
                int zoneid = Util.nextInt(0, this.zone.map.zones.size() - 1);
                this.zone = this.zone.map.zones.get(zoneid);
                int spawnX = Util.nextInt(100, 500);
                int spawnY = this.zone.map.yPhysicInTop(spawnX, 0);
                ChangeMapService.gI().changeMap(this, this.zone, spawnX, spawnY);
                this.changeStatus(BossStatus.ACTIVE);
                this.changeToTypeNonPK();
                st = System.currentTimeMillis();
                timeLeave = Util.nextInt(1_000_000, 3_000_000);
                moveDirection = Util.isTrue(1, 2) ? 1 : -1;
            } catch (Exception e) {
                Logger.error(this.data[0].name + ": Lỗi đang tiến hành REST\n");
                this.changeStatus(BossStatus.REST);
            }
        } else {
            Logger.error(this.data[0].name + ": Lỗi map đang tiến hành RESPAWN\n");
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void chatM() {
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, timeLeave)) {
            this.leaveMapNew();
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

    private boolean canMoveNow() {
        long now = System.currentTimeMillis();
        if (phaseStartTime == 0) {
            phaseStartTime = now;
            isRestingPhase = false;
            return true;
        }
        long dt = now - phaseStartTime;
        if (isRestingPhase) {
            if (dt < 2000) {
                return false;
            }
            isRestingPhase = false;
            phaseStartTime = now;
            return true;
        } else {
            if (dt < 5000) {
                return true;
            }
            isRestingPhase = true;
            phaseStartTime = now;
            return false;
        }
    }

    @Override
    public void active() {
        this.attack();
        if (Util.canDoWithTime(st, ESCORT_TIMEOUT)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    @Override
    public void afk() {
        if (!Util.canDoWithTime(this.lastTimeMove, MOVE_COOLDOWN)) {
            return;
        }
        this.lastTimeMove = System.currentTimeMillis();

        Player pl = Client.gI().getPlayer(playerId);
        if (pl == null || pl.zone == null || pl.zone.map == null) {
            this.leaveMap();
            return;
        }

        if (this.escortStartTime == 0) {
            this.escortStartTime = System.currentTimeMillis();
            updatePlayerPosition(pl);
        }

        if (this.zone == null || this.zone.map == null) {
            this.leaveMap();
            return;
        }

        if (this.zone.map.planetId != pl.zone.map.planetId) {
            abandonPlayer(pl, "Tuần lộc đã bỏ bạn vì bạn đi quá xa!");
            return;
        }

        if (pl.changeMapVIP) {
            pl.changeMapVIP = false;
            abandonPlayer(pl, "Tuần lộc đã bỏ bạn vì bạn dùng dịch chuyển nhanh!");
            return;
        }

        if (detectTeleport(pl)) {
            abandonPlayer(pl, "Tuần lộc đã bỏ bạn vì bạn dịch chuyển quá nhanh!");
            return;
        }

        int dis = Util.getDistance(this, pl);

        if (dis <= FOLLOW_DISTANCE) {
            this.lastTimeNearPlayer = System.currentTimeMillis();
        }

        if (!this.zone.equals(pl.zone)) {
            if (!canFollowPlayerToNewMap(pl)) {
                return;
            }

            if (!services.map.MapService.gI().isMapOffline(pl.zone.map.mapId)
                    && !services.map.MapService.gI().isMapMaBu(pl.zone.map.mapId)
                    && !services.map.MapService.gI().isMapDoanhTrai(pl.zone.map.mapId)) {
                ChangeMapService.gI().changeMap(this, pl.zone, pl.location.x + Util.nextInt(-20, 20), pl.location.y);
                trackMapChange();
                updatePlayerPosition(pl);
            }
            return;
        }
        if (dis > MAX_DISTANCE_FOLLOW) {
            abandonPlayer(pl, "Tuần lộc đã bỏ bạn vì bạn đi quá xa!");
            return;
        }
        if (dis > WARN_DISTANCE) {
            if (Util.canDoWithTime(this.lastWarnTime, WARN_COOLDOWN)) {
                this.lastWarnTime = System.currentTimeMillis();
                Service.gI().sendThongBao(pl, "Bạn đã quá xa Tuần lộc!");
            }
        }
        if (dis > TELEPORT_DISTANCE) {
            int newX = this.location.x + (Util.isTrue(1, 2) ? 1 : -1) * Util.nextInt(5, 10);
            int groundY = this.zone.map.yPhysicInTop(newX, pl.location.y > 0 ? pl.location.y - 24 : 0);
            PlayerService.gI().playerMove(this, newX, groundY);
            return;
        }
        if (dis > FOLLOW_DISTANCE) {
            int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
            int dx = Util.nextInt(20, 30);
            int newX = this.location.x + (dir == 1 ? dx : -dx);
            int groundY = this.zone.map.yPhysicInTop(newX, pl.location.y > 0 ? pl.location.y - 24 : 0);
            PlayerService.gI().playerMove(this, newX, groundY);
        }

        updatePlayerPosition(pl);
        this.changeToTypeNonPK();
    }

    @Override
    public void attack() {
        if (!Util.canDoWithTime(this.lastTimeMove, 200)) {
            return;
        }
        this.lastTimeMove = System.currentTimeMillis();

        if (Util.canDoWithTime(this.lastChatTime, Util.nextInt(5000, 10000))) {
            this.lastChatTime = System.currentTimeMillis();
            this.chat("Éc éc!");
        }

        if (!canMoveNow()) {
            return;
        }

        try {
            moveToSafe();
        } catch (Exception ignored) {
        }
    }

    private void moveToSafe() {
        if (this.location == null || this.zone == null || this.zone.map == null) {
            return;
        }

        if (this.location.x <= 100) {
            moveDirection = 1;
        } else if (this.location.x >= this.zone.map.mapWidth - 100) {
            moveDirection = -1;
        }

        if (moveDirection == 0) {
            moveDirection = Util.isTrue(1, 2) ? 1 : -1;
        }

        int moveDistance = Util.nextInt(20, 30);
        int newX = this.location.x + (moveDirection * moveDistance);

        if (newX < 50) {
            newX = 50;
            moveDirection = 1;
        }
        if (newX > this.zone.map.mapWidth - 50) {
            newX = this.zone.map.mapWidth - 50;
            moveDirection = -1;
        }

        int groundY = this.zone.map.yPhysicInTop(newX, 0);
        PlayerService.gI().playerMove(this, newX, groundY);
    }

    private boolean canFollowPlayerToNewMap(Player pl) {
        long now = System.currentTimeMillis();

        if (this.escortStartTime > 0 && !Util.canDoWithTime(this.escortStartTime, ESCORT_START_DELAY)) {
            Service.gI().sendThongBao(pl, "Vui lòng đợi 3 giây sau khi nhận Tuần lộc!");
            return false;
        }

        if (!Util.canDoWithTime(this.lastFollowTeleport, MAP_CHANGE_COOLDOWN)) {
            abandonPlayer(pl, "Tuần lộc đã bỏ bạn vì bạn chuyển map quá nhanh! Chạy cc gì nhanh vlin");
            return false;
        }

        if (this.firstMapChangeTime > 0) {
            long timeSinceFirst = now - this.firstMapChangeTime;
            if (timeSinceFirst < CONSECUTIVE_MAP_CHANGE_WINDOW) {
                if (this.consecutiveMapChanges >= MAX_CONSECUTIVE_MAP_CHANGES) {
                    abandonPlayer(pl, "Tuần lộc đã bỏ bạn vì bạn chuyển map quá nhiều lần!");
                    return false;
                }
            } else {
                this.firstMapChangeTime = now;
                this.consecutiveMapChanges = 0;
            }
        }

        return true;
    }

    private void trackMapChange() {
        long now = System.currentTimeMillis();
        this.lastFollowTeleport = now;

        if (this.firstMapChangeTime == 0) {
            this.firstMapChangeTime = now;
            this.consecutiveMapChanges = 1;
        } else {
            long timeSinceFirst = now - this.firstMapChangeTime;
            if (timeSinceFirst < CONSECUTIVE_MAP_CHANGE_WINDOW) {
                this.consecutiveMapChanges++;
            } else {
                this.firstMapChangeTime = now;
                this.consecutiveMapChanges = 1;
            }
        }

        this.lastFollowMapId = this.zone != null && this.zone.map != null ? this.zone.map.mapId : -1;
    }

    private boolean detectTeleport(Player pl) {
        if (pl == null || pl.location == null || pl.zone == null || pl.zone.map == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (lastPlayerX == -1 || lastPlayerY == -1 || lastPlayerMapId == -1) {
            updatePlayerPosition(pl);
            return false;
        }

        if (!Util.canDoWithTime(lastPositionCheckTime, POSITION_CHECK_INTERVAL)) {
            return false;
        }

        if (lastPlayerMapId != pl.zone.map.mapId) {
            updatePlayerPosition(pl);
            return false;
        }
        int distanceMoved = Util.getDistance(lastPlayerX, lastPlayerY, pl.location.x, pl.location.y);
        long timeElapsed = now - lastPositionCheckTime;

        if (distanceMoved > TELEPORT_DETECTION_DISTANCE) {
            double speed = (double) distanceMoved / Math.max(timeElapsed, 1);
            if (speed > (MAX_MOVE_SPEED / POSITION_CHECK_INTERVAL)) {
                return true;
            }
        }

        updatePlayerPosition(pl);
        return false;
    }

    private void updatePlayerPosition(Player pl) {
        if (pl != null && pl.location != null && pl.zone != null && pl.zone.map != null) {
            this.lastPlayerX = pl.location.x;
            this.lastPlayerY = pl.location.y;
            this.lastPlayerMapId = pl.zone.map.mapId;
            this.lastPositionCheckTime = System.currentTimeMillis();
        }
    }

    private void abandonPlayer(Player pl, String message) {
        this.playerId = 0;
        this.changeStatus(BossStatus.ACTIVE);
        this.changeToTypeNonPK();
        this.consecutiveMapChanges = 0;
        this.firstMapChangeTime = 0;
        this.lastFollowTeleport = 0;
        this.lastFollowMapId = -1;
        this.lastTimeNearPlayer = 0;
        this.escortStartTime = 0;

        this.lastPlayerX = -1;
        this.lastPlayerY = -1;
        this.lastPlayerMapId = -1;
        this.lastPositionCheckTime = 0;

        if (pl != null) {
            pl.changeMapVIP = false;
            Service.gI().sendThongBao(pl, message);
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        return 0;
    }

    @Override
    public void die(Player plKill) {
    }

    @Override
    public void reward(Player plKill) {
    }
}
