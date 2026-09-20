package managers.boss;

import boss.Boss;
import network.Message;
import player.Player;
import services.map.MapService;
import utils.Logger;

public class TrungThuEventManager extends BossManager {

    private static TrungThuEventManager instance;
    private static final int MENU_TYPE = 9;

    public static TrungThuEventManager gI() {
        if (instance == null) {
            instance = new TrungThuEventManager();
        }
        return instance;
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
            msg.writer().writeUTF("Danh sách Boss Trung Thu");
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
            Logger.logException(TrungThuEventManager.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // @Override
    // public void run() {
    // while (!Maintenance.isRunning()) {
    // try {
    // long st = System.currentTimeMillis();
    // for (int i = this.bosses.size() - 1; i >= 0; i--) {
    // if (i < this.bosses.size()) {
    // Boss boss = this.bosses.get(i);
    // try {
    // boss.update();
    // } catch (Exception e) {
    // e.printStackTrace();
    // try {
    // removeBoss(boss);
    // } catch (Exception ex) {
    // }
    // }
    // }
    // }
    // Functions.sleep(Math.max(150 - (System.currentTimeMillis() - st), 10));
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    // }
}
