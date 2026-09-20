package dungeon;

import utils.Functions;
import boss.Boss;
import boss.bando.TrungUyXanhLo;
import clan.Clan;
import map.TrapMap;
import map.Zone;
import mob.Mob;
import player.Player;
import services.ItemTimeService;
import services.map.MapService;
import services.Service;
import services.map.ChangeMapService;
import utils.Util;
import server.Manager;
import system.Template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import server.Maintenance;
import services.map.ItemMapService;
import utils.TimeUtil;
import task.KolTaskService;
import map.ItemMap;

@Data
public class TreasureUnderSea implements Runnable {

    public static final long POWER_CAN_GO_TO_DBKB = 2000000000;
    public static final int AVAILABLE = 50;
    public static final int TIME_BAN_DO_KHO_BAU = 1800000;

    public int id;
    public byte level;
    public final List<Zone> zones;

    public Clan clan;
    public boolean isOpened;
    private long lastTimeOpen;
    private boolean kickoutbdkb;
    private long timeKickOutBDKB;
    private Boss boss;
    private long lastTimeSendNotify;
    private boolean allCharactersDead;
    public boolean removeText = false;
    private int totalGoldDropped = 0;
    private static final int MAX_GOLD_PIECES = 15;
    private static final int MAX_LEVEL = 110;
    private static final int GOLD_PER_PIECE_MAX_LEVEL = 30000;
    private Set<Integer> mobsDroppedGold = new HashSet<>();
    private boolean bossDroppedGold = false;
    private boolean kolTaskUpdated = false; // Flag to prevent duplicate KOL task updates

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public TreasureUnderSea(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning() && isOpened) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - startTime), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_BAN_DO_KHO_BAU)
                    || (kickoutbdkb && Util.canDoWithTime(timeKickOutBDKB, 60000))) {
                finish();
                dispose();
            }

            allCharactersDead = true;
            for (Zone zone : zones) {

                if (zone.map.mapId == 135) {
                    for (Player pl : zone.getNotBosses()) {
                        if (pl != null) {
                            TrapMap trap = zone.isInTrap(pl);
                            if (trap != null) {
                                trap.doPlayer(pl);
                            }
                        }
                    }
                }

                for (Mob mob : zone.mobs) {
                    if (!mob.isDie()) {
                        allCharactersDead = false;
                    } else {
                        if ((mob.lvMob > 0 || mob.isBigBoss()) && !mobsDroppedGold.contains(mob.id)) {
                            dropGoldForMob(zone, mob);
                            mobsDroppedGold.add(mob.id);
                        }
                    }
                }

                if (allCharactersDead) {
                    for (Player cBoss : zone.getBosses()) {
                        if (!cBoss.isDie()) {
                            allCharactersDead = false;
                            break;
                        }
                    }
                }
            }
            if (!kickoutbdkb && (allCharactersDead || Util.canDoWithTime(lastTimeOpen, TIME_BAN_DO_KHO_BAU - 60000))) {
                kickoutbdkb = true;
                timeKickOutBDKB = System.currentTimeMillis();
            }

            if (kickoutbdkb && Util.canDoWithTime(lastTimeSendNotify, 10000)) {
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        if (removeText == false) {
                            ItemTimeService.gI().removeTextBanDoKhoBau(pl);
                            sendThanhTichBanDoKhoBau(pl);
                        }
                        Service.gI().sendThongBao(pl, "Cái hang này sắp sập rồi, chúng ta phải rời khỏi đây ngay "
                                + TimeUtil.getTimeLeft(timeKickOutBDKB, 60) + " nữa");
                    }
                    removeText = true;
                    lastTimeSendNotify = System.currentTimeMillis();
                }
            }

            // Kiểm tra boss chết và drop vàng
            if (boss != null && boss.isDie() && !bossDroppedGold) {
                for (Zone zone : zones) {
                    if (zone.map.mapId == 137 && boss.zone != null && boss.zone.equals(zone)) {
                        dropGoldForBoss(zone, boss);
                        bossDroppedGold = true;
                        break;
                    }
                }
            }

        }
    }

    public void sendThanhTichBanDoKhoBau(Player pl) {
        long timeDoneBDKB;
        timeDoneBDKB = System.currentTimeMillis() - pl.clan.lastTimeOpenBanDoKhoBau;
        int levelDoneBDKB;
        levelDoneBDKB = pl.clan.BanDoKhoBau.level;
        if (levelDoneBDKB > pl.clan.levelDoneBanDoKhoBau) {
            pl.clan.levelDoneBanDoKhoBau = levelDoneBDKB;
            pl.clan.thoiGianHoanThanhBDKB = timeDoneBDKB;
        } else if (levelDoneBDKB == pl.clan.levelDoneBanDoKhoBau) {
            if (timeDoneBDKB < pl.clan.thoiGianHoanThanhBDKB) {
                pl.clan.thoiGianHoanThanhBDKB = timeDoneBDKB;
            }
        }
        pl.clan.updatethanhTichBDKB(pl.clan.id);
        pl.clan.updatethanhTichBDKBForLeader();
        pl.clan.updateThongTinLeader(pl.clan.id);
    }

    public void openBanDoKhoBau(Player plOpen, Clan clan, byte level) {
        try {
            this.level = level;
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.lastTimeOpenBanDoKhoBau = this.lastTimeOpen;
            this.clan.playerOpenBanDoKhoBau = plOpen;
            this.clan.BanDoKhoBau = this;
            this.kickoutbdkb = false;
            this.isOpened = true;
            this.allCharactersDead = false;
            this.totalGoldDropped = 0;
            this.mobsDroppedGold.clear();
            this.bossDroppedGold = false;
            this.kolTaskUpdated = false; // Reset flag for new dungeon
            this.init();
            ChangeMapService.gI().goToDBKB(plOpen);
            sendTextBanDoKhoBau();
        } catch (Exception e) {
            plOpen.clan.lastTimeOpenBanDoKhoBau = 0;
            this.dispose();
        }
    }

    private void init() {
        for (Zone zone : this.zones) {
            for (TrapMap trap : zone.trapMaps) {
                trap.dame = this.level * 100000;
            }

            if (zone.map.mapId == 135 || zone.map.mapId == 136 || zone.map.mapId == 137) {
                List<Mob> mobs = zone.mobs;
                // Lấy MapTemplate để lấy HP gốc
                Template.MapTemplate mapTemplate = null;
                for (Template.MapTemplate mt : Manager.MAP_TEMPLATES) {
                    if (mt != null && mt.id == zone.map.mapId) {
                        mapTemplate = mt;
                        break;
                    }
                }
                for (int i = 0; i < mobs.size(); i++) {
                    Mob mob = mobs.get(i);
                    // Lấy HP gốc từ MapTemplate hoặc từ maxHp nếu MapTemplate không có
                    long baseHp = mob.point.maxHp;
                    if (mapTemplate != null && mapTemplate.mobHp != null && mob.id < mapTemplate.mobHp.length) {
                        baseHp = mapTemplate.mobHp[mob.id];
                    } else if (mob.point.hp <= 0 && mob.point.maxHp > 0) {
                        // Nếu không lấy được từ template, dùng maxHp hiện tại
                        baseHp = mob.point.maxHp;
                    } else if (mob.point.hp > 0) {
                        // Nếu HP hiện tại > 0, có thể đây là giá trị gốc
                        baseHp = mob.point.hp;
                    }
                    if (((i == 5 || i == 10) && zone.map.mapId == 135) || (i == 5 && zone.map.mapId == 136)
                            || (i == 5 && zone.map.mapId == 137) || (i == 2 && zone.map.mapId == 138)) {
                        mob.lvMob = 1;
                        mob.point.dame = Math.min(level * 11 * mob.point.dame * 10, 2_000_000_000);
                        mob.point.maxHp = Math.min(level * 11 * baseHp, 2_000_000_000);
                        mob.hoiSinh();
                        mob.hoiSinhMobPhoBan();
                    } else {
                        mob.lvMob = 0;
                        mob.point.dame = (int) Math.min(level * 11 * mob.point.dame, 2_000_000_000);
                        mob.point.maxHp = (int) Math.min(level * 11 * baseHp, 2_000_000_000);
                        mob.hoiSinh();
                        mob.hoiSinhMobPhoBan();
                    }
                }
            } else {
                // Lấy MapTemplate để lấy HP gốc
                Template.MapTemplate mapTemplate = null;
                for (Template.MapTemplate mt : Manager.MAP_TEMPLATES) {
                    if (mt != null && mt.id == zone.map.mapId) {
                        mapTemplate = mt;
                        break;
                    }
                }
                for (Mob mob : zone.mobs) {
                    // Lấy HP gốc từ MapTemplate hoặc từ maxHp nếu MapTemplate không có
                    long baseHp = mob.point.maxHp;
                    if (mapTemplate != null && mapTemplate.mobHp != null && mob.id < mapTemplate.mobHp.length) {
                        baseHp = mapTemplate.mobHp[mob.id];
                    } else if (mob.point.hp <= 0 && mob.point.maxHp > 0) {
                        // Nếu không lấy được từ template, dùng maxHp hiện tại
                        baseHp = mob.point.maxHp;
                    } else if (mob.point.hp > 0) {
                        // Nếu HP hiện tại > 0, có thể đây là giá trị gốc
                        baseHp = mob.point.hp;
                    }
                    mob.point.dame = Math.min(level * 11 * mob.point.dame, 2_000_000_000);
                    mob.point.maxHp = Math.min(level * 11 * baseHp, 2_000_000_000);
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            }

            if (zone.map.mapId == 137) {
                try {
                    long bossDamage = (200000 * level);
                    long bossMaxHealth = (200000000 * level);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    boss = new TrungUyXanhLo(
                            zone,
                            level,
                            (int) bossDamage,
                            (int) bossMaxHealth);
                } catch (Exception exception) {
                }
            }
        }
        new Thread(this, "Bản Đồ Kho Báu: " + this.clan.name).start();
    }

    public void finish() {
        try {
            if (this.clan != null) {
                long timeDoneBDKB = System.currentTimeMillis() - this.clan.lastTimeOpenBanDoKhoBau;
                int levelDoneBDKB = this.level;
                if (levelDoneBDKB > this.clan.levelDoneBanDoKhoBau) {
                    this.clan.levelDoneBanDoKhoBau = levelDoneBDKB;
                    this.clan.thoiGianHoanThanhBDKB = timeDoneBDKB;
                } else if (levelDoneBDKB == this.clan.levelDoneBanDoKhoBau) {
                    if (timeDoneBDKB < this.clan.thoiGianHoanThanhBDKB || this.clan.thoiGianHoanThanhBDKB <= 0) {
                        this.clan.thoiGianHoanThanhBDKB = timeDoneBDKB;
                    }
                }
                this.clan.updatethanhTichBDKB(this.clan.id);
                this.clan.updatethanhTichBDKBForLeader();
                this.clan.updateThongTinLeader(this.clan.id);
                if (levelDoneBDKB >= 20 && !kolTaskUpdated) {
                    for (Player pl : this.clan.membersInGame) {
                        if (pl != null && pl.zone != null && MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)) {
                            try {
                                KolTaskService.doneTaskKolTreasureUnderSea(pl);
                            } catch (Exception e) {
                                utils.Logger.log("Error updating KOL task for TreasureUnderSea: " + e.getMessage());
                            }
                        }
                    }
                    kolTaskUpdated = true; // Mark as updated to prevent duplicate
                }
            }
        } catch (Exception ignore) {
        }
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfBDKB(pl);
                }
            }

        }
    }

    private void kickOutOfBDKB(Player player) {
        if (MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId)) {
            if (bossDroppedGold) {
                dragonpass.DragonPassService.gI().onTreasureCompleted(player);
            }
            ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1038);
        }
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void sendTextBanDoKhoBau() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextBanDoKhoBau(pl);
        }
    }

    private void removeTextBanDoKhoBau() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextBanDoKhoBau(pl);
        }
    }

    /**
     * Drop vàng cho siêu quái hoặc bigboss khi chết
     */
    private void dropGoldForMob(Zone zone, Mob mob) {
        if (totalGoldDropped >= MAX_GOLD_PIECES) {
            return;
        }

        // Mỗi siêu quái/bigboss drop 1-3 cục vàng tùy level
        int goldPerPiece = calculateGoldPerPiece();
        if (goldPerPiece <= 0) {
            return;
        }

        // Tính số cục vàng cho mob này (1-3 cục tùy level)
        int piecesPerMob = Math.max(1, Math.min(3, (int) Math.ceil((double) level / 40)));

        // Giới hạn số cục vàng còn lại
        int remainingPieces = MAX_GOLD_PIECES - totalGoldDropped;
        int goldPieces = Math.min(piecesPerMob, remainingPieces);

        // Drop vàng trải đều xung quanh vị trí mob chết
        int mobX = mob.location.x;
        int mobY = zone.map.yPhysicInTop(mobX, mob.location.y);

        // Tính toán khoảng cách giữa các cục vàng để trải đều
        int spreadRadius = 150; // Bán kính spread
        double angleStep = (2 * Math.PI) / goldPieces;

        for (int i = 0; i < goldPieces; i++) {
            double angle = i * angleStep;
            int x = mobX + (int) (Math.cos(angle) * spreadRadius) + Util.nextInt(-30, 31);
            int y = mobY + (int) (Math.sin(angle) * spreadRadius) + Util.nextInt(-30, 31);

            // Đảm bảo tọa độ hợp lệ
            x = Math.max(50, Math.min(x, zone.map.pxw - 50));
            y = zone.map.yPhysicInTop(x, y);

            ItemMap goldItem = new ItemMap(zone, 190, goldPerPiece, x, y, -1);
            Service.gI().dropItemMap(zone, goldItem);
            totalGoldDropped++;

            if (totalGoldDropped >= MAX_GOLD_PIECES) {
                break;
            }
        }
    }

    /**
     * Drop vàng cho boss khi chết
     */
    private void dropGoldForBoss(Zone zone, Boss boss) {
        if (totalGoldDropped >= MAX_GOLD_PIECES) {
            return;
        }

        // Boss drop nhiều hơn mob (3-5 cục tùy level)
        int goldPerPiece = calculateGoldPerPiece();
        if (goldPerPiece <= 0) {
            return;
        }

        // Tính số cục vàng cho boss (3-5 cục tùy level)
        int piecesPerBoss = Math.max(3, Math.min(5, (int) Math.ceil((double) level / 25)));

        // Giới hạn số cục vàng còn lại
        int remainingPieces = MAX_GOLD_PIECES - totalGoldDropped;
        int goldPieces = Math.min(piecesPerBoss, remainingPieces);

        // Drop vàng trải đều xung quanh vị trí boss chết
        int bossX = boss.location.x;
        int bossY = zone.map.yPhysicInTop(bossX, boss.location.y);

        // Tính toán khoảng cách giữa các cục vàng để trải đều
        int spreadRadius = 200; // Bán kính spread lớn hơn cho boss
        double angleStep = (2 * Math.PI) / goldPieces;

        for (int i = 0; i < goldPieces; i++) {
            double angle = i * angleStep;
            int x = bossX + (int) (Math.cos(angle) * spreadRadius) + Util.nextInt(-40, 41);
            int y = bossY + (int) (Math.sin(angle) * spreadRadius) + Util.nextInt(-40, 41);

            // Đảm bảo tọa độ hợp lệ
            x = Math.max(50, Math.min(x, zone.map.pxw - 50));
            y = zone.map.yPhysicInTop(x, y);

            ItemMap goldItem = new ItemMap(zone, 190, goldPerPiece, x, y, -1);
            Service.gI().dropItemMap(zone, goldItem);
            totalGoldDropped++;

            if (totalGoldDropped >= MAX_GOLD_PIECES) {
                break;
            }
        }
    }

    /**
     * Tính toán số vàng mỗi cục dựa trên level
     * Level max (110) = 30000 vàng/cục, level 1 = ít hơn
     */
    private int calculateGoldPerPiece() {
        if (level <= 0) {
            return 273; // 30000 / 110 ≈ 273
        }
        // Tính tỷ lệ: level / MAX_LEVEL, tối thiểu 273 vàng/cục
        double ratio = (double) level / MAX_LEVEL;
        int goldPerPiece = (int) Math.max(273, Math.round(GOLD_PER_PIECE_MAX_LEVEL * ratio));
        return Math.min(goldPerPiece, GOLD_PER_PIECE_MAX_LEVEL);
    }

    public void dispose() {
        if (boss != null) {
            this.boss.leaveMap();
        }
        for (Zone zone : zones) {
            for (int i = zone.items.size() - 1; i >= 0; i--) {
                if (i < zone.items.size()) {
                    ItemMapService.gI().removeItemMap(zone.items.get(i));
                }
            }
        }
        this.removeTextBanDoKhoBau();
        this.allCharactersDead = false;
        this.boss = null;
        this.isOpened = false;
        this.clan.BanDoKhoBau = null;
        this.clan = null;
        this.kickoutbdkb = false;
        this.totalGoldDropped = 0;
        this.mobsDroppedGold.clear();
        this.bossDroppedGold = false;
        this.kolTaskUpdated = false; // Reset flag
    }
}
