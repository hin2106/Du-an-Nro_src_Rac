package managers.boss;

import boss.Boss;
import boss.BossID;
import network.Message;
import player.Player;
import server.Maintenance;
import services.map.MapService;
import utils.Functions;
import utils.Logger;
import utils.TimeUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Boss14hManager extends BossManager {

    private static Boss14hManager instance;

    public static Boss14hManager gI() {
        if (instance == null) {
            instance = new Boss14hManager();
        }
        return instance;
    }

    private static final int[] ALL_BOSS = {
            BossID.MABU, BossID.SUPERBU
    };

    @Override
    public void run() {
        while (!Maintenance.isRunning()) {
            try {
                long start = System.currentTimeMillis();
                updateBosses();
                manageSpawnCycle();
                Functions.sleep(Math.max(300 - (System.currentTimeMillis() - start), 20));
            } catch (Exception e) {
                Logger.logException(Boss14hManager.class, e);
            }
        }
    }

    private void updateBosses() {
        List<Boss> safeCopy = new CopyOnWriteArrayList<>(bosses);
        for (Boss boss : safeCopy) {
            try {
                boss.update();
            } catch (Exception e) {
                Logger.logException(Boss14hManager.class, e);
                removeBoss14H(boss);
            }
        }
    }

    private void manageSpawnCycle() {
        boolean mabu14HOpen = TimeUtil.isMabu14HOpen() || TimeUtil.TEST_OPEN_MABU14H;

        if (mabu14HOpen) {
            if (getBossCount() == 0) {
                spawnBosses();
            }
        } else {
            removeAllBosses();
        }
    }

    private void removeBoss14H(Boss boss) {
        try {
            removeBoss(boss);
        } catch (Exception ex) {
            Logger.logException(Boss14hManager.class, ex);
        }
    }

    private void spawnBosses() {
        try {
            spawnAllZoneOfMap(BossID.MABU, 127);
            spawnAllZoneOfMap(BossID.SUPERBU, 128);
        } catch (Exception e) {
            Logger.logException(Boss14hManager.class, e);
        }
    }

    private void spawnAllZoneOfMap(int bossId, int mapId) {
        try {
            map.Map map = MapService.gI().getMapById(mapId);
            if (map == null || map.zones == null || map.zones.isEmpty()) {
                return;
            }

            for (map.Zone zone : map.zones) {
                if (zone == null) {
                    continue;
                }

                Boss boss = BossManager.gI().createBoss(bossId);
                if (boss == null) {
                    continue;
                }
                boss.zoneFinal = zone;
                boss.joinMapByZone(zone);
            }
        } catch (Exception e) {
            Logger.errorln("[Boss14hManager] Lỗi spawn bossId=" + bossId + ", mapId=" + mapId + ": " + e.getMessage());
        }
    }

    private void removeAllBosses() {
        try {
            for (int id : ALL_BOSS) {
                removeAllByBossId(id);
            }
        } catch (Exception e) {
            Logger.logException(Boss14hManager.class, e);
        }
    }

    private static final int MENU_TYPE = 8;

    public void showListBoss14h(Player player) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(MENU_TYPE);

        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Danh sách Boss 14h");
            var visibleBosses = bosses.stream()
                    .filter(b -> b != null && b.data != null && b.data[0] != null
                            && !MapService.gI().isMapBlackBallWar(b.data[0].getMapJoin()[0]))
                    .toList();

            msg.writer().writeByte(visibleBosses.size());

            for (Boss boss : visibleBosses) {
                var data = boss.data[0];
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeInt((int) boss.id);
                msg.writer().writeShort(data.getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(data.getOutfit()[1]);
                msg.writer().writeShort(data.getOutfit()[2]);
                msg.writer().writeUTF(data.getName());
                msg.writer().writeUTF(boss.bossStatus.toString());
                msg.writer().writeUTF(
                        boss.zone != null
                                ? boss.zone.map.mapName + " (" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId
                                : "Không ở map nào");
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Boss14hManager.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
