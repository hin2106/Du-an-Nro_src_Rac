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

public class Boss12hManager extends BossManager {

    private static Boss12hManager instance;

    public static Boss12hManager gI() {
        if (instance == null) {
            instance = new Boss12hManager();
        }
        return instance;
    }

    private static final int[] ALL_BOSS = {
            BossID.DRABURA, BossID.BUI_BUI, BossID.BUI_BUI_2, BossID.YA_CON,
            BossID.DRABURA_2, BossID.DRABURA_3, BossID.MABU_12H,
            BossID.GOKU, BossID.CADIC_12H, BossID.SUPERBU
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
                Logger.logException(Boss12hManager.class, e);
            }
        }
    }

    private void updateBosses() {
        List<Boss> safeCopy = new CopyOnWriteArrayList<>(bosses);
        for (Boss boss : safeCopy) {
            try {
                boss.update();
            } catch (Exception e) {
                Logger.logException(Boss12hManager.class, e);
                removeBoss12H(boss);
            }
        }
    }

    private void manageSpawnCycle() {
        boolean mabuOpen = TimeUtil.isMabuOpen() || TimeUtil.TEST_OPEN_MABU;

        if (mabuOpen) {
            if (getBossCount() == 0) {
                Logger.logln("[Boss12hManager] Bắt đầu spawn Boss Mabu 12h...");
                spawnBosses();
            }
        } else {
            removeAllBosses();
        }
    }

    private void removeBoss12H(Boss boss) {
        try {
            removeBoss(boss);
        } catch (Exception ex) {
            Logger.logException(Boss12hManager.class, ex);
        }
    }

    private void spawnBosses() {
        try {
            spawnAllZoneOfMap(BossID.DRABURA, 114);
            spawnAllZoneOfMap(BossID.BUI_BUI, 115);
            spawnAllZoneOfMap(BossID.BUI_BUI_2, 117);
            spawnAllZoneOfMap(BossID.YA_CON, 118);
            spawnAllZoneOfMap(BossID.DRABURA_2, 119);
            spawnAllZoneOfMap(BossID.MABU_12H, 120);
        } catch (Exception e) {
            Logger.logException(Boss12hManager.class, e);
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
            Logger.errorln("[Boss12hManager] Lỗi spawn bossId=" + bossId + ", mapId=" + mapId + ": " + e.getMessage());
        }
    }

    private void removeAllBosses() {
        try {
            for (int id : ALL_BOSS) {
                removeAllByBossId(id);
            }
        } catch (Exception e) {
            Logger.logException(Boss12hManager.class, e);
        }
    }

    private static final int MENU_TYPE = 7;

    public void showListBoss12h(Player player) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(MENU_TYPE);

        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Danh sách Boss 12h");
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
            Logger.logException(Boss12hManager.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
