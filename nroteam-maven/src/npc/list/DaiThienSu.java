package npc.list;

import npc.Npc;
import player.Player;
import consts.ConstNpc;
import services.map.NpcService;
import services.top.TopNhiemVuService;
import services.top.TopSucManhService;

public class DaiThienSu extends Npc {

    public DaiThienSu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chào con, ta có thể cho con xem bảng xếp hạng sức mạnh!",
                    "Top 100\n Sức mạnh","Top 100\n Nhiệm vụ", "Không");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0:
                        TopSucManhService.gI().showTop(player);
                        break;
                    case 1:
                        TopNhiemVuService.gI().showTop(player);
                        break;
                    case 2:
                        // Từ chối
                        NpcService.gI().createTutorial(player, tempId, avartar, "Hẹn gặp lại con lần sau nhé!");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
