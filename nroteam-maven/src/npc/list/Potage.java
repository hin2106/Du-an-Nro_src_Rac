package npc.list;
import managers.boss.BossManager;
import consts.ConstNpc;
import java.util.logging.Level;
import java.util.logging.Logger;
import npc.Npc;
import player.Player;
import services.map.NpcService;
import services.Service;
import utils.Util;

public class Potage extends Npc {

    public Potage(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 140) {
                Player BossClone = BossManager.gI().findBossClone(player);
                if (BossClone != null) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Đang có 1 nhân bản của " + BossClone.name + " hãy chờ kết quả trận đấu",
                            "OK");
                } else {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Hãy giúp ta đánh bại bản sao\nNgươi chỉ có 5 phút để hạ hắn\nPhần thưởng cho ngươi là 1 bình Commeson",
                            "Hướng\ndẫn\nthêm", "OK", "Từ chối");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 140) {
                if (player.idMark.isBaseMenu()) {
                    Player BossClone = BossManager.gI().findBossClone(player);
                    if (BossClone == null) {
                        switch (select) {
                            case 0 ->
                                NpcService.gI().createTutorial(player, tempId, this.avartar,
                                        "Thứ bị phong ấn tại đây là vũ khí có tên Commeson...");
                            case 1 -> {
                                // Check cooldown 5 giây để chống spam click
                                if (System.currentTimeMillis() - player.lastPkCommesonTime < 5000) {
                                    return;
                                }
                                if (!Util.isAfterMidnight(player.lastPkCommesonTime)) {
                                    Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                } else {
                                    // Ghi timestamp TRƯỚC khi bắt đầu trận để đảm bảo chỉ đánh 1 lần/ngày
                                    player.lastPkCommesonTime = System.currentTimeMillis();
                                    try {
                                        Service.gI().callNhanBan(player);
                                    } catch (Exception ex) {
                                        Logger.getLogger(Potage.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
