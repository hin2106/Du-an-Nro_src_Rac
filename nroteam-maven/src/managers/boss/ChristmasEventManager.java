package managers.boss;

import boss.Boss;
import network.Message;
import player.Player;
import services.map.MapService;
import utils.Logger;
import utils.Util;

public class ChristmasEventManager extends BossManager {

    private static ChristmasEventManager instance;
    private static final int MENU_TYPE = 11;

    public static ChristmasEventManager gI() {
        if (instance == null) {
            instance = new ChristmasEventManager();
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
            msg.writer().writeUTF("Danh sách Boss NOEL");
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
}
