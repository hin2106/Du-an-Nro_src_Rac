package managers.boss;

import boss.Boss;
import boss.BossID;
import boss.broly.SuperBroly;
import consts.BossStatus;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import map.Map;
import map.Zone;
import network.Message;
import player.Player;
import server.Maintenance;
import server.ServerNotify;
import services.map.MapService;
import utils.Functions;
import utils.Logger;
import utils.Util;

public class BrolyManager extends BossManager {

    private static BrolyManager instance;
    private static long lastSuperBrolySpawnCheck = 0;
    private static final long SUPER_BROLY_CHECK_INTERVAL = 300000; // 5 phút
    private static final int MENU_TYPE = 5;

    public static BrolyManager gI() {
        if (instance == null) {
            instance = new BrolyManager();
        }
        return instance;
    }

    @Override
    public Boss findBossByBossID(int bossID) {
        for (Boss boss : this.bosses) {
            if (boss == null) {
                continue;
            }
            if (boss.id == bossID) {
                if (boss.zone != null && boss.zone.map != null) {
                    int mapId = boss.zone.map.mapId;
                    int zoneId = boss.zone.zoneId;

                    if (mapId >= 0 && zoneId >= 0) {
                        return boss;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void showListBoss(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(MENU_TYPE);

        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Danh sách Boss Broly");
            var visibleBosses = bosses.stream()
                    .filter(b -> b != null && b.data != null && b.data[0] != null
                            && !MapService.gI().isMapBlackBallWar(b.data[0].getMapJoin()[0]))
                    .toList();

            msg.writer().writeByte(visibleBosses.size());

            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = this.bosses.get(i);
                int uniqueId;
                if (boss.zone != null && boss.zone.map != null) {
                    uniqueId = ((int) boss.id << 16) | ((boss.zone.map.mapId & 0xFF) << 8) | (boss.zone.zoneId & 0xFF);
                } else {
                    uniqueId = (int) boss.id;
                }
                msg.writer().writeInt(uniqueId);
                msg.writer().writeInt(uniqueId);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName()
                        + " [] HP: " + Util.number(boss.nPoint.hpMax));
                msg.writer().writeUTF(boss.bossStatus.toString());
                msg.writer().writeUTF(
                        boss.zone != null
                                ? boss.zone.map.mapName + " (" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId
                                : "Không ở map nào");
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(AnTromManager.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning()) {
            try {
                long start = System.currentTimeMillis();
                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    if (i < this.bosses.size()) {
                        Boss boss = this.bosses.get(i);
                        try {
                            boss.update();
                        } catch (Exception e) {
                            Logger.logException(BrolyManager.class, e);
                            removeBoss(boss);
                        }
                    }
                }
                checkAndSpawnSuperBrolyAuto();

                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - start), 10));
            } catch (Exception e) {
                Logger.logException(BrolyManager.class, e);
            }
        }
    }

    private void checkAndSpawnSuperBrolyAuto() {
        long now = System.currentTimeMillis();
        if (now - lastSuperBrolySpawnCheck < SUPER_BROLY_CHECK_INTERVAL) {
            return;
        }
        lastSuperBrolySpawnCheck = now;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean isPrimeTime = (hour >= 18 || hour < 3);

        if (!isPrimeTime) {
            return;
        }
        int superBrolyCount = 0;
        for (Boss boss : this.bosses) {
            if (boss.id == BossID.SUPER_BROLY) {
                superBrolyCount++;
            }
        }
        int targetCount = Util.nextInt(1, 5); // 1-4 boss
        if (superBrolyCount < targetCount && Util.isTrue(20, 100)) { // 20%
            spawnRandomSuperBroly();
        }
    }

    private void spawnRandomSuperBroly() {
        try {
            int[] brolyMaps = new int[] { 5, 13, 20, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38 };
            int randomMapId = brolyMaps[Util.nextInt(brolyMaps.length)];
            Map map = MapService.gI().getMapById(randomMapId);

            if (map == null || map.zones == null || map.zones.isEmpty()) {
                return;
            }

            synchronized (this) {
                List<Zone> availableZones = new ArrayList<>();
                for (Zone zone : map.zones) {
                    if (zone == null) {
                        continue;
                    }

                    if (zone.zoneId < 2) {
                        continue;
                    }

                    if (zone.getBosses() != null) {
                        boolean hasSuperBroly = false;
                        boolean hasBroly = false;
                        for (Player boss : zone.getBosses()) {
                            if (boss != null && !boss.isDie()) {
                                if (boss.id == BossID.SUPER_BROLY) {
                                    hasSuperBroly = true;
                                }
                                if (boss.id == BossID.BROLY) {
                                    hasBroly = true;
                                }
                            }
                        }
                        if (hasSuperBroly || hasBroly) {
                            continue;
                        }
                    }

                    if (this.checkBosses(zone, BossID.SUPER_BROLY)) {
                        continue;
                    }

                    if (this.checkBosses(zone, BossID.BROLY)) {
                        continue;
                    }

                    availableZones.add(zone);
                }

                if (availableZones.isEmpty()) {
                    return;
                }

                Zone randomZone = availableZones.get(Util.nextInt(0, availableZones.size() - 1));

                boolean hasSuperBrolyInSelectedZone = this.checkBosses(randomZone, BossID.SUPER_BROLY);
                if (hasSuperBrolyInSelectedZone) {
                    return;
                }

                boolean hasBrolyInSelectedZone = this.checkBosses(randomZone, BossID.BROLY);
                if (hasBrolyInSelectedZone) {
                    return;
                }

                if (randomZone.getBosses() != null) {
                    boolean hasSuperBroly = false;
                    boolean hasBroly = false;
                    for (Player boss : randomZone.getBosses()) {
                        if (boss != null && !boss.isDie()) {
                            if (boss.id == BossID.SUPER_BROLY) {
                                hasSuperBroly = true;
                            }
                            if (boss.id == BossID.BROLY) {
                                hasBroly = true;
                            }
                        }
                    }
                    if (hasSuperBroly || hasBroly) {
                        return;
                    }
                }

                int x = randomZone.map.mapWidth > 100 ? Util.nextInt(100, randomZone.map.mapWidth - 100)
                        : Util.nextInt(100);
                int y = randomZone.map.yPhysicInTop(x, 100);

                SuperBroly superBroly = new SuperBroly(randomZone, x, y);
                superBroly.joinMap();
                superBroly.changeStatus(BossStatus.ACTIVE);
                ServerNotify.gI().notify("BOSS Super Broly vừa xuất hiện tại " + randomZone.map.mapName);
            }
        } catch (Exception ex) {
            Logger.error("Lỗi spawn SuperBroly tự động: " + ex.getMessage() + "\n");
        }
    }

    public int countSuperBrolyInZone(Zone zone) {
        int count = 0;
        for (Boss boss : this.bosses) {
            if (boss != null && boss.id == BossID.SUPER_BROLY && boss.zone != null
                    && boss.zone.equals(zone) && !boss.isDie()) {
                count++;
            }
        }
        return count;
    }

    public Boss getFirstSuperBrolyInZone(Zone zone) {
        for (Boss boss : this.bosses) {
            if (boss != null && boss.id == BossID.SUPER_BROLY && boss.zone != null
                    && boss.zone.equals(zone) && !boss.isDie()) {
                return boss;
            }
        }
        return null;
    }

}
