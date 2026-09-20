package mob;

import consts.ConstMap;
import consts.ConstMob;
import consts.ConstTask;
import event.Event;
import event.EventItemDropLimitConfig;
import item.Item;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import map.ItemMap;
import map.Zone;
import network.Message;
import player.Location;
import player.Pet;
import player.Player;
import server.Maintenance;
import server.Manager;
import server.ServerManager;
import server.ServerNotify;
import services.AchievementService;
import services.ItemService;
import services.Service;
import services.TaskService;
import services.dungeon.TrainingService;
import services.map.ItemMapService;
import services.map.MapService;
import services.player.InventoryService;
import services.top.TopGhiDanhService;
import skill.Skill;
import utils.Logger;
import utils.TimeUtil;
import utils.Util;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;
    public int mobsKilledSTT;

    public List<Player> temporaryEnemies = new ArrayList<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;

    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;
    public static int TiLeXuatHienSieuQuai = 100;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;

    private long lastTimeAttackPlayer;
    private long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    private long lastPlayerSearchTime = 0;
    private Player cachedTargetPlayer = null;
    private static final long SEARCH_CACHE_TIME = 500;

    /**
     * Clear cache target khi player rời zone hoặc đổi map
     */
    public void clearTargetCache(Player player) {
        if (player != null && cachedTargetPlayer != null && cachedTargetPlayer.id == player.id) {
            cachedTargetPlayer = null;
            temporaryEnemies.remove(player);
        }
    }

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp(this.point.getHpFull());
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
    }

    public void setTiemNang() {
        this.maxTiemNang = (long) this.point.getHpFull() * (long) (this.pTiemNang + Util.nextInt(-2, 2)) / 100L;
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (!this.isDie()) {
            if (damage >= this.point.hp) {
                damage = this.point.hp;
            }
            if (!dieWhenHpFull) {
                if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                    damage = this.point.hp - 1;
                }
                if ((this.tempId == ConstMob.MOC_NHAN) && damage > this.point.maxHp / 10) {
                    damage = this.point.maxHp / 10;
                }
            }
            if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
                boolean mob76Die = true;
                for (Mob mob : this.zone.mobs) {
                    if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                        mob76Die = false;
                        break;
                    }
                }
                if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.LIEN_HOAN, Skill.ANTOMIC, Skill.MASENKO, Skill.KAMEJOKO ->
                            damage = 1;
                    }
                }
            }
            if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0
                    && plAtt != null) {
                long cap = Math.max(1L, this.point.maxHp * 100L / 100L);
                if (damage > cap) {
                    damage = cap;
                }
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2)
                    && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }

            if (damage > 2_000_000_000) {
                damage = 2_000_000_000;
            }

            this.point.hp -= damage;

            addTemporaryEnemies(plAtt);

            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();

                // Remove khỏi spatial grid khi mob chết
                if (this.zone != null && this.zone.spatialGridEnabled && this.zone.spatialGrid != null
                        && this.location != null) {
                    try {
                        this.zone.spatialGrid.removeMob(this);
                    } catch (Exception e) {
                        utils.Logger.logException(Mob.class, e, "Lỗi remove mob khỏi spatial grid khi die");
                    }
                }

                if (plAtt != null) {
                    if (MapService.gI().isMapSieuThanhThuy(this.zone.map.mapId)) {
                        boolean allDead = true;
                        for (Mob mob : this.zone.mobs) {
                            if (!mob.isDie()) {
                                allDead = false;
                                break;
                            }
                        }
                        if (allDead) {
                            if (this.zone.tayKarinWavesCleared < 2) {
                                this.zone.tayKarinWavesCleared++;
                                this.zone.tayKarinLastWaveAt = System.currentTimeMillis();
                                if (this.zone.tayKarinWavesCleared >= 2) {
                                    TaskService.gI().checkDoneTaskKillMobTayKarin(plAtt);
                                }
                            }
                        }
                    }
                    this.sendMobDieAffterAttacked(plAtt, (int) damage);
                    TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                    AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                }
                if (lvMob > 0) {
                    if (this.isBigBoss()) {
                        return;
                    }
                    try {
                        sendSieuQuai(0);
                        lvMob = 0;
                        if (zone != null) {
                            zone.lastSuperDeadId = id;
                        }
                    } catch (Exception e) {
                        Logger.logException(getClass(), e);
                    }
                }
                if (this.id == 13) {
                    this.zone.isbulon1Alive = false;
                }
                if (this.id == 14) {
                    this.zone.isbulon2Alive = false;
                }
            } else {
                this.sendMobStillAliveAffterAttacked((int) damage,
                        plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                Service.gI().addSMTN(plAtt, (byte) 2, getTiemNangForPlayer(plAtt, damage), true);
                TrainingService.gI().tangTnsmLuyenTap(plAtt, getTiemNangForPlayer(plAtt, damage));
            }
        }
    }

    public long getTiemNangForPlayer(Player pl, long dame) {
        int levelPlayer = Service.gI().getCurrLevel(pl);
        int levelMob = this.level;
        int checkLevel = Math.abs(levelPlayer - this.level);
        long tiemNang = (long) (dame + (point.getHpFull() * 0.0005));
        switch (this.tempId) {
            case 0 ->
                tiemNang = 1;
            case ConstMob.TEST_DAME -> {
                if (pl.nPoint != null && pl.nPoint.dame > 1000) {
                    if (tiemNang >= 1) {
                        tiemNang = 1;
                    } 
                } else {
                    if (tiemNang >= 1) {
                        tiemNang = 1;
                    }
                }
            }
        }
        if (checkLevel > 5 && levelPlayer > levelMob) {
            tiemNang = 1;
        } else {
            if (checkLevel < 0) {
                checkLevel = Math.abs(levelMob - levelPlayer);
            } else {
                tiemNang /= (int) (checkLevel * 0.5) + 1.25;
            }
        }
        if (tiemNang < 1) {
            tiemNang = 1;
        }
        if (pl.nPoint != null) {
            tiemNang = (int) pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }
        if (pl.zone != null && pl.zone.map != null
                && (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124)) {
            tiemNang *= 2;
        }
        return tiemNang;
    }

    public void update() {
        if (this.zone == null || this.zone.map == null || this.location == null || this.point == null) {
            return;
        }
        if (this.effectSkill == null) {
            this.effectSkill = new MobEffectSkill(this);
        }
        if (this.tempId == ConstMob.HIRUDEGARN && !(this instanceof BigBoss)) {
            if (!TimeUtil.is22H()) {
                if (!this.isDie()) {
                    this.startDie();
                }
                return;
            }
            if (TimeUtil.is22H() && this.isDie() && Util.canDoWithTime(this.lastTimeDie, 5000)) {
                this.hoiSinh();
                this.sendMobHoiSinh();
            }
        }
        if (!this.isDie() && !isBigBoss() && !hasNearbyPlayers()) {
            return;
        }

        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }

        if (this.isDie() && !Maintenance.isRunning() && !isBigBoss()) {
            if (zone.map.mapId == 165) {
                if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                    return;
                }
                if (Util.canDoWithTime(lastTimeDie, 5000)) {
                    this.hoiSinh();
                    this.sendMobHoiSinh();
                }
                if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                    lastTimePhucHoi = System.currentTimeMillis();
                    long hpMax = this.point.maxHp;
                    if (this.point.hp < hpMax) {
                        hoi_hp(hpMax / 10);
                    } else {
                        this.sendMobHoiSinh();
                    }
                }
                return;
            }

            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive
                            && Util.canDoWithTime(lastTimeDie, 10000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default:
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is22H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimeDie, 5000)) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        long hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
                    }
            }
        }
        effectSkill.update();
        attack();
    }

    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN || this.tempId == ConstMob.VUA_BACH_TUOC
                || this.tempId == ConstMob.ROBOT_BAO_VE || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA || this.tempId == ConstMob.GA_CHIN_CUA
                || this.tempId == ConstMob.NGUA_CHIN_LMAO || this.tempId == ConstMob.PIANO);
    }

    public boolean hasNearbyPlayers() {
        if (this.zone == null) {
            return false;
        }
        return this.zone.hasPlayersNearby(this.location.x, this.location.y, 1000);
    }

    public void attack() {
        Zone attackZone = this.zone;
        if (attackZone == null) {
            cachedTargetPlayer = null;
            return;
        }
        Player player = getPlayerCanAttack();
        if (!isDie() && !effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN
                && tempId != ConstMob.TEST_DAME && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss()
                && (this.lvMob < 1
                        || MapService.gI().isMapPhoBan(attackZone.map.mapId))
                && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
            // Player có thể rời/chuyển khu sau lúc được chọn làm mục tiêu.
            // Không cho mob đánh một tham chiếu đã không còn thuộc zone này.
            if (player != null && this.zone == attackZone && player.zone == attackZone
                    && attackZone.getHumanoids().contains(player)) {
                this.mobAttackPlayer(player);
            } else if (player != null) {
                clearTargetCache(player);
            }
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        long now = System.currentTimeMillis();
        int searchDistance = isBigBoss() ? 300 : 100;
        if (cachedTargetPlayer != null
                && (now - lastPlayerSearchTime) < SEARCH_CACHE_TIME
                && cachedTargetPlayer.zone == this.zone
                && !cachedTargetPlayer.isDie()) {
            if (this.zone != null) {
                List<Player> zonePlayers = this.zone.getHumanoids();
                if (zonePlayers == null || !zonePlayers.contains(cachedTargetPlayer)) {
                    cachedTargetPlayer = null;
                } else {
                    int cachedDistance = Util.getDistance(cachedTargetPlayer, this);
                    if (cachedDistance <= searchDistance) {
                        return cachedTargetPlayer;
                    } else {
                        cachedTargetPlayer = null;
                    }
                }
            } else {
                cachedTargetPlayer = null;
            }
        }

        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            cachedTargetPlayer = plAttack;
            lastPlayerSearchTime = now;
            return plAttack;
        }
        try {
            List<Player> nearbyPlayers = this.zone.getPlayersNear(
                    this.location.x,
                    this.location.y,
                    searchDistance);

            int distance = searchDistance;
            for (Player pl : nearbyPlayers) {
                if (pl != null && pl.zone == this.zone && !pl.isDie() && !pl.isBoss && !pl.isNewPet
                        && (pl.satellite == null || !pl.satellite.isDefend)
                        && (pl.effectSkin == null || !pl.effectSkin.isVoHinh)
                        && (this.tempId > 18 || (this.tempId > 9 && this.type == 4) || isBigBoss())) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {
            Logger.logException(Mob.class, e, "Lỗi getPlayerCanAttack");
        }

        cachedTargetPlayer = plAttack;
        lastPlayerSearchTime = now;
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (Player plAttt : playersMap) {
                    if (plAttt == null || plAttt.zone != this.zone || plAttt.isDie() || plAttt.isBoss
                            || (plAttt.satellite != null && plAttt.satellite.isDefend)
                            || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh)
                            || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {

        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        Zone attackZone = this.zone;
        if (player == null || attackZone == null || player.zone != attackZone
                || !attackZone.getHumanoids().contains(player)) {
            clearTargetCache(player);
            return;
        }
        long dameMob = this.point.getDameAttack();
        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (player.isPet && ((Pet) player).master.charms != null
                && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        // Siêu quái đánh theo phần trăm máu: bỏ nếu người chơi có bùa Oai Hùng
        if (this.lvMob > 0
                && !MapService.gI().isMapPhoBan(attackZone.map.mapId)
                && (player.charms == null || player.charms.tdOaiHung < System.currentTimeMillis())) {
            dameMob = (int) (player.nPoint.hpMax * (10.0 / 100));
        }
        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }
        if (player.itemTime != null && player.itemTime.isUseCMS) {
            dameMob = (int) Math.round(dameMob * 0.1);
        }
        // Kiểm tra lần cuối vì player có thể đổi khu trong lúc tính sát thương.
        if (this.zone != attackZone || player.zone != attackZone) {
            clearTargetCache(player);
            return;
        }
        double dame = player.injured(null, dameMob, false, true);

        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, double dame) {
        if (!player.isPet && !player.isNewPet && !player.isBot) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writeSmartLong(dame); // dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writeSmartLong(player.nPoint.hp);
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        if (MapService.gI().isMapPhoBan(zone.map.mapId)) {
            return;
        }
        if (shouldBecomeSuper()) {
            this.lvMob = 1;
            this.point.hp = this.point.maxHp <= 20_000_000 ? this.point.maxHp * 10L : 2_000_000_000L;
        } else {
            this.lvMob = 0;
        }
        this.setTiemNang();
        if (this.zone != null && this.zone.spatialGridEnabled && this.zone.spatialGrid != null
                && this.location != null) {
            try {
                this.zone.spatialGrid.addMob(this);
            } catch (Exception e) {
                utils.Logger.logException(Mob.class, e, "Lỗi thêm mob vào spatial grid khi hồi sinh");
            }
        }
    }

    private boolean shouldBecomeSuper() {
        try {
            if (isBigBoss() || point == null || point.maxHp <= 2500) {
                return false;
            }

            boolean hasSuper = zone.mobs.stream()
                    .anyMatch(m -> m != null && m != this && m.lvMob > 0 && !m.isDie());
            if (hasSuper) {
                return false;
            }

            if (zone.lastSuperDeadId == id) {
                return false;
            }
            return Util.isTrue(TiLeXuatHienSieuQuai, 10000);

        } catch (Exception e) {
            Logger.logException(getClass(), e);
            return false;
        }
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob);
            msg.writeSmartLong(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);

            // Gửi effect siêu quái nếu đây là siêu quái
            if (this.lvMob > 0) {
                this.sendSieuQuai(1);
            }
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoi_hp(long hp) {
        Message msg = null;
        try {
            this.point.sethp(this.point.gethp() + hp);
            long HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeSmartLong(this.point.gethp());
            msg.writeSmartLong(HP);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeSmartLong(Util.maxIntValue(this.point.gethp()));
            msg.writeSmartLong(Util.maxIntValue(this.point.gethp()));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, long dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writeSmartLong(Util.maxIntValue(dameHit));
            msg.writer().writeBoolean(plKill.nPoint.isCrit);
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isPet && !player.isNewPet) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        } else {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId, true);
                }
            }
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));

            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size());
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);
                msg.writer().writeShort(itemMap.itemTemplate.id);
                msg.writer().writeShort(itemMap.x);
                msg.writer().writeShort(itemMap.y);
                msg.writer().writeInt((int) itemMap.playerId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemReward;
    }

    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();
        if (player == null || !player.isPl() || player.isBoss || player.isBot) {
            return list;
        }
        int mapid = player.zone.map.mapId;
        // =========================================
        // Setting rơi item sự kiện cho mob mộc nhân
        // =========================================
            if (this.tempId == 0) {
                if (Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TET
                        && MapService.gI().isMap3Lang(mapid)
                        && rollDrop(player, 10, 100)) {
                    int soLanRoi = Util.nextInt(1, 5);
                    addDropWithEventLimit(list, player, 751, 1, x, yEnd);
                    if (soLanRoi == 2) {
                        int x2 = x + Util.nextInt(2, 5);
                        addDropWithEventLimit(list, player, 751, 1, x2, yEnd);
                    }
                }
                return list;
            }
        // =========================================
        // End
        // =========================================
        Player targetPlayer = player;
        if (targetPlayer != null && targetPlayer.isPet) {
            targetPlayer = ((Pet) targetPlayer).master;
        }

        try {
            if (player != null && player.isPl()) {
                Calendar cal = Calendar.getInstance();
                int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                if (player.lastGemDropDay != dayOfYear) {
                    int qty = (hour >= 4 && hour < 6) ? 2 : 1;
                    list.add(new ItemMap(zone, 77, qty, x, yEnd, player.id));
                    player.lastGemDropDay = dayOfYear;
                    player.lastGemDropBoot = ServerManager.timeStart;
                }
            }
        } catch (Exception ignored) {
        }
        if (this.tempId == ConstMob.TEST_DAME) {
            if (!Util.isTrue(0, 100)) {
                return list;
            }
        }
        //======================
        // Drop item sự kiện tết
        //======================
            // Sự kiện Tết: Thịt Heo từ quái Heo (tempId 15-20), tỉ lệ 5%, rơi 1-2 lần
            if (Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TET 
                    && this.tempId >= 15 && this.tempId <= 20) {
                if (rollDrop(player, 5, 100)) {
                    int soLanRoi = Util.nextInt(1, 2);
                    addDropWithEventLimit(list, player, 748, 1, x, yEnd);
                    if (soLanRoi == 2) {
                        int x2 = x + Util.nextInt(2, 5);
                        addDropWithEventLimit(list, player, 748, 1, x2, yEnd);
                    }
                }
            }
            
            // Sự kiện Tết: Đậu Xanh. 
            if (Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TET 
                    && MapService.gI().isMapPhoBan(mapid)) {
                if (rollDrop(player, 25, 100)) {
                    int soLanRoi = Util.nextInt(1, 5);
                    addDropWithEventLimit(list, player, 750, 1, x, yEnd);
                    if (soLanRoi == 2) {
                        int x2 = x + Util.nextInt(2, 5);
                        addDropWithEventLimit(list, player, 750, 1, x2, yEnd);
                    }
                }
            }
            // Sự kiện Tết: Bùa Giải Khai Phong Ấn. 
            if (Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TET
                    && rollDrop(player, 10, 100)) {
                int itemId = Util.nextInt(537, 540); // 537, 538, 539, 540 mỗi loại 25%
                addDropWithEventLimit(list, player, itemId, 1, x, yEnd);
            }
        //===================
        // End Drop item sự kiện Tết
        //===================
        if (this.zone.map.mapId == 155 && player.setClothes.setGod14()) {
            if (rollDrop(player, 20, 20000) || (player.isActive() && rollDrop(player, 100, 10000))) {
                list.add(new ItemMap(zone, Util.nextInt(1066, 1070), 1, x, player.location.y, player.id));
            }
        }
        if (mapid == 165 && targetPlayer != null) {
            if (!Util.isTrue(100, 100)) {
                return list;
            }
            int increment = 1;
            if (targetPlayer.itemTime != null && targetPlayer.itemTime.isUseNangLuong) {
                increment = 3;
            }

            Item bottle = InventoryService.gI().findItemBag(targetPlayer, 1852);
            if (bottle == null) {
                return list;
            }

            boolean found = false;
            if (bottle.itemOptions != null) {
                for (Item.ItemOption io : bottle.itemOptions) {
                    if (io.optionTemplate.id == 253) {
                        io.param += increment;
                        found = true;
                        break;
                    }
                }
            } else {
                bottle.itemOptions = new ArrayList<>();
            }

            if (!found) {
                bottle.itemOptions.add(new Item.ItemOption(253, increment));
            }

            InventoryService.gI().sendItemBags(targetPlayer);
        }

        if (player.itemTime.isUseMayDo
                && (rollDrop(player, 20, 100)
                        || (player.isActive() && rollDrop(player, 1, 50)))
                && this.tempId > 57 && this.tempId < 66) {
            list.add(new ItemMap(zone, 380, 1, x, yEnd, player.id));
        }

        if (player.itemTime.isUseMayDo
                && (rollDrop(player, 20, 100)
                        || (player.isActive() && rollDrop(player, 1, 50)))
                && this.tempId > 57 && this.tempId < 66) {
            list.add(new ItemMap(zone, 380, 1, x, yEnd, player.id));
        }

        // if (player.isPl() && TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
        //     if ((player.gender == 0 && this.tempId == 11)
        //             || (player.gender == 1 && this.tempId == 12)
        //             || (player.gender == 2 && this.tempId == 10)) {
        //         list.add(new ItemMap(zone, 20, 1, x, yEnd, player.id));
        //         TaskService.gI().checkDoneTaskFind7Stars(player);
        //     }
        // }

        if (player.isPl() && TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
            if  (this.tempId == 13) {
                if (rollDrop(player, 30, 100)) {
                    list.add(new ItemMap(zone, 85, 1, x, yEnd, player.id));
                    TaskService.gI().checkDoneTaskFindDoremon(player);
                }
            }
        }

        // UP Porata
        if (MapService.gI().isMapUpPorata(mapid)) {
            int tiLeDropManhVo933 = 25; // Tỉ lệ
            int tiLeDropManhVo1868 = 20; // Tỉ lệ

            if (player.itemTime != null && player.itemTime.isUseCoBonLa) {
                tiLeDropManhVo933 += 30; // Tăng thêm 30% tỉ lệ nếu có CoBonLa
                tiLeDropManhVo1868 += 30; // Tăng thêm 30% tỉ lệ nếu có CoBonLa
                if (tiLeDropManhVo933 > 100) {
                    tiLeDropManhVo933 = 100;
                }
                if (tiLeDropManhVo1868 > 100) {
                    tiLeDropManhVo1868 = 100;
                }
            }
            if (InventoryService.gI().findItemBongTai(player)
                    && rollDrop(player, tiLeDropManhVo933, 100)
                    && player.itemEvent.canDropManhVo(20150)) {
                ItemMap it = new ItemMap(zone, 933, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(31, Util.nextInt(1, 100)));
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } // Drop item 1868 (Mảnh vỡ bông tai cấp 3)
            else if (InventoryService.gI().findItemBongTaiCap2(player)
                    && rollDrop(player, tiLeDropManhVo1868, 100)
                    && player.itemEvent.canDropManhVo(20150)) {
                // Random số lượng từ 1-100
                ItemMap it = new ItemMap(zone, 1868, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(31, Util.nextInt(1, 100)));
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            } // Drop item 935
            else if (rollDrop(player, 5, 100)) {
                ItemMap it = new ItemMap(zone, 935, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(31, Util.nextInt(1, 10)));
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            }
            // Drop item 934
            if (rollDrop(player, 1, 200)) {
                ItemMap it = new ItemMap(zone, 934, Util.nextInt(1, 5), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            }
        }
        // Vàng Ngọc
        int vang;
        if (MapService.gI().isMap3Planets(mapid)
                && (rollGoldDrop(player, 1, 20) || (Manager.TEST && rollGoldDrop(player, 1, 5))
                        || (player.isActive() && rollGoldDrop(player, 1, 20)) || (player.isAdmin() && rollGoldDrop(player, 1, 20)))) {
            vang = Util.nextInt(500, 3000);
            if (vang < 1000) {
                list.add(new ItemMap(zone, 76, vang, x, yEnd, player.id));
            } else if (vang < 2000) {
                list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
            } else {
                list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
            }
        }

        if (MapService.gI().isMapNappa(mapid)
                && (rollGoldDrop(player, 1, 100) || (Manager.TEST && rollGoldDrop(player, 1, 5))
                        || (player.isActive() && rollGoldDrop(player, 1, 20)) || (player.isAdmin() && rollGoldDrop(player, 1, 10)))) {
            vang = Util.nextInt(2000, 6000);
            if (vang < 3000) {
                list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
            } else if (vang < 5000) {
                list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
            } else {
                list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
            }
        }

        // Vàng cold
        if (MapService.gI().isMapCold(mapid)
                && (rollGoldDrop(player, 1, 100) || (Manager.TEST && rollGoldDrop(player, 1, 5))
                        || (player.isActive() && rollGoldDrop(player, 1, 20)) || (player.isAdmin() && rollGoldDrop(player, 1, 10)))) {
            vang = Util.nextInt(8000, 18000);
            if (vang < 10000) {
                list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
            } else if (vang < 14000) {
                list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
            } else {
                list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
            }
        }

        // Vàng tương lai
        if (MapService.gI().isMapTuongLai(mapid)
                && (rollGoldDrop(player, 1, 100) || (Manager.TEST && rollGoldDrop(player, 1, 5))
                        || (player.isActive() && rollGoldDrop(player, 1, 20)) || (player.isAdmin() && rollGoldDrop(player, 1, 10)))) {
            vang = Util.nextInt(5000, 12000);
            if (vang < 6000) {
                list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
            } else if (vang < 10000) {
                list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
            } else {
                list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
            }
        }

        // Vàng phó bản
        if (MapService.gI().isMapPhoBan(mapid)
                && (rollGoldDrop(player, 1, 100) || (Manager.TEST && rollGoldDrop(player, 1, 5))
                        || (player.isActive() && rollGoldDrop(player, 1, 10)) || (player.isAdmin() && rollGoldDrop(player, 1, 10)))) {
            vang = Util.nextInt(8000, 20000);
            if (vang < 6000) {
                list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
            } else if (vang < 10000) {
                list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
            } else {
                list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
            }
        }

        if (rollDrop(player, 30, 1000)) {
            list.add(new ItemMap(zone, 456, Util.nextInt(1, 1), x, yEnd, player.id));
        }

        // SKH 3 mao dau
        if (MapService.gI().isMapUpSKH(mapid) && Util.isTrue(500, 500)) {
            long now = System.currentTimeMillis();

            if (player.timeUpSKH > now) {
                short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);

                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                int[] opsrand = ItemService.gI().randOptionItemKichHoatUnified((byte) player.gender);
                for (int opId : opsrand) {
                    it.options.add(new Item.ItemOption(opId, 0));
                }
                it.options.add(new Item.ItemOption(30, 0));

                list.add(it);
            }
        }

        // Đồ Thần + Thức Ăn
        if (MapService.gI().isMapCold(mapid)) {
            if (player.isPet) {
                player = ((Pet) player).master;
            }
            if (player.isPet) { // Lặp lại kiểm tra, có thể do logic riêng
                player = ((Pet) player).master;
            }
            if (rollDrop(player, 1, 2000000) || (player.isActive() && rollDrop(player, 1, 250000))
                    || (Manager.TEST && rollDrop(player, 1, 2000)) || (player.isAdmin() && rollDrop(player, 10, 500))) {
                ItemMap it = ItemService.gI().randDoTLCOler(this.zone, 1, x, yEnd, player.id);
                list.add(it);
                ServerNotify.gI().notify(player.name + " vừa nhặt được " + it.itemTemplate.name + " tại "
                        + this.zone.map.mapName + " khu " + this.zone.zoneId);
            }
            if ((rollDrop(player, 1, 20000) || (player.isActive() && rollDrop(player, 1, 700))
                    || (Manager.TEST && rollDrop(player, 1, 100)) || (player.isAdmin() && rollDrop(player, 10, 100)))
                    && InventoryService.gI().fullSetThan(player)) {
                ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                list.add(it);
            }
        }

        // Thức ăn tương lai
        if (MapService.gI().isMapTuongLai(mapid)
                && ((rollDrop(player, 1, 20000) || (player.isActive() && rollDrop(player, 1, 1000))
                        || (Manager.TEST && rollDrop(player, 1, 200)) || (player.isAdmin() && rollDrop(player, 10, 100))))
                && InventoryService.gI().fullSetThan(player)) {
            ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(30, 0));
            list.add(it);
        }

        // Sao Pha Lê Mảnh đá Vụn Đá Nâng Cấp
        if (player.nPoint.isDoSPL
                && (rollDrop(player, 1, 100) || (player.isActive() && rollDrop(player, 1, 100))
                        || (Manager.TEST && rollDrop(player, 1, 50)) || (player.isAdmin() && rollDrop(player, 10, 100)))) {
            int rand = Util.nextInt(0, 6);
            ItemMap it = new ItemMap(zone, 441 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
            list.add(it);
        }

        // Đá nâng cấp
        if (MapService.gI().isMapCold(mapid)
                && (rollDrop(player, 1, 10000) || (Manager.TEST && rollDrop(player, 1, 5))
                        || (player.isActive() && rollDrop(player, 1, 500)))) {
            int rand = Util.nextInt(0, 4);
            ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(71 - rand, 0));
            list.add(it);
        }

        // Mảnh đá vụn cho bản đồ Doanh Trại
        if (MapService.gI().isMapDoanhTrai(mapid)
                && (rollDrop(player, 1, 10000) || (Manager.TEST && rollDrop(player, 1, 5))
                        || (player.isActive() && rollDrop(player, 1, 10)))) {
            ItemMap it = new ItemMap(zone, 225, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(74, 0));
            list.add(it);
        }

        // Mảnh đá vụn cho bản đồ 3 Planets (tỷ lệ khác)
        if (MapService.gI().isMap3Planets(mapid)
                && (rollDrop(player, 1, 500) || (Manager.TEST && rollDrop(player, 1, 10))
                        || (player.isActive() && rollDrop(player, 1, 250)))) {
            ItemMap it = new ItemMap(zone, 225, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(74, 0));
            list.add(it);
        }

        // Kiểm tra nếu map nằm trong danh sách các map cần áp dụng xác suất
        if ((MapService.gI().isMap3Planets(mapid) || MapService.gI().isMapNappa(mapid)
                || MapService.gI().isMapTuongLai(mapid) || MapService.gI().isMapCold(mapid))
                && (rollDrop(player, 1, 200) || (player.isActive() && rollDrop(player, 1, 100)))) {
            int rand = Util.nextInt(0, 1);
            ItemMap it = new ItemMap(zone, 19 + rand, 1, x, yEnd, player.id);
            list.add(it);
        }

        // Xử lý khi sử dụng CoBonLa - tăng 100% may mắn
        if (player.itemTime.isUseCoBonLa) {
            // Tăng 100% may mắn rơi đồ (gấp đôi xác suất)
            if (player.nPoint.isDoSPL && (rollDrop(player, 2, 200) || (player.isActive() && rollDrop(player, 2, 100)))) {
                int rand = Util.nextInt(0, 1);
                ItemMap it = new ItemMap(zone, 19 + rand, 1, x, yEnd, player.id);
                list.add(it);
            }
            if (this.zone.map.mapId == 155 && player.setClothes.setGod14()) {
                if (rollDrop(player, 20, 20000) || (player.isActive() && rollDrop(player, 100, 10000))) {
                    list.add(new ItemMap(zone, Util.nextInt(1066, 1070), 1, x, player.location.y, player.id));
                }
            }
            // Tăng xác suất rơi đồ ở các map khác
            if ((MapService.gI().isMap3Planets(mapid) || MapService.gI().isMapNappa(mapid)
                    || MapService.gI().isMapTuongLai(mapid) || MapService.gI().isMapCold(mapid))
                    && (rollDrop(player, 2, 200) || (player.isActive() && rollDrop(player, 2, 100)))) {
                int rand = Util.nextInt(0, 1);
                ItemMap it = new ItemMap(zone, 19 + rand, 1, x, yEnd, player.id);
                list.add(it);
            }
        }

        if (Event.isEventActive()) {
            Event.getInstance().dropItem(player, this, list, x, yEnd);
        }

        return list;
    }

    /** Option 236 tăng đúng param% xác suất rơi đồ từ quái. */
    private boolean rollDrop(Player player, int chance, int total) {
        if (chance <= 0 || total <= 0) {
            return false;
        }
        int bonus = player != null && player.nPoint != null ? Math.max(0, player.nPoint.tlItemDrop) : 0;
        long adjusted = (long) chance * (100L + bonus) / 100L;
        return Util.isTrue((int) Math.min(total, adjusted), total);
    }

    /** Set Gohan đủ 5 món chỉ nhân bốn xác suất rơi item vàng. */
    private boolean rollGoldDrop(Player player, int chance, int total) {
        int multiplier = player != null && player.setClothes != null && player.setClothes.gohan >= 5 ? 4 : 1;
        return rollDrop(player, (int) Math.min(Integer.MAX_VALUE, (long) chance * multiplier), total);
    }

    private void addDropWithEventLimit(List<ItemMap> list, Player player, int itemId, int quantity, int x, int yEnd) {
        int finalQuantity = quantity;
        int maxPerDay = EventItemDropLimitConfig.getLimit(Manager.EVENT_SEVER, itemId);
        if (maxPerDay > 0 && player != null && player.itemEvent != null) {
            finalQuantity = player.itemEvent.consumeLimitedItemDrop(itemId, quantity, maxPerDay);
        }
        if (finalQuantity > 0) {
            list.add(new ItemMap(zone, itemId, finalQuantity, x, yEnd, player.id));
        }
    }

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
            case ConstMob.QUY_BAY_ME:
            case ConstMob.PHI_LONG_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 10)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                        TaskService.gI().checkDoneTaskFind7Stars(player);
                    } else {
                        Service.gI().sendThongBao(player,
                                "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(long dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeSmartLong(Util.maxIntValue(this.point.gethp()));
            msg.writeSmartLong(Util.maxIntValue(dameHit));
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); // level mob
            msg.writeSmartLong(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob);
            msg.writeSmartLong(this.point.hp);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendSieuQuai(int type) {
        Message msg;
        try {
            msg = new Message(-75);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(type);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDisable(boolean bool) {
        Message msg;
        try {
            msg = new Message(81);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDoneMove(boolean bool) {
        Message msg;
        try {
            msg = new Message(82);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendFire(boolean bool) {
        Message msg;
        try {
            msg = new Message(85);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendIce(boolean bool) {
        Message msg;
        try {
            msg = new Message(86);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendWind(boolean bool) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMobMaxHp(long maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writeSmartLong(Util.maxIntValue(maxHp));
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private void phanSatThuong(Player plTarget, double dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        long percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            long damePST = (int) (long) (dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                long hpMob = this.point.hp;
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writeSmartLong(this.point.hp);
                msg.writeSmartLong(damePST);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (IOException e) {
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
}
