package npc.list;

import consts.ConstNpc;
import npc.Npc;
import player.Player;
import services.Service;
import services.map.ChangeMapService;


public class DrMyuu extends Npc {

    public DrMyuu(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 20||this.mapId == 19||this.mapId == 18) {
                createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Năm 740, ta tìm thấy các kí sinh trùng của King Tuffle,\nsau đó ta đã nghiên cứu và chế tạo kí sinh trùng Baby.\nBaby có khả năng bám vào cơ thể người khác,\nkiểm soát sức mạnh của họ và làm việc theo ý của ta.\nTuy nhiên ta đã mất kiểm soát nó hoàn toàn...\n Người có thể giúp ta chế ngự nó không ?",
                        "Đồng ý",
                        "Từ chối");
            } else if (this.mapId == 166) {
                createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngươi muốn về hả ?", "Quay Về", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 20||this.mapId == 19||this.mapId == 18) {
                switch (select) {
                    case 0 ->
                        // ChangeMapService.gI().changeMapBySpaceShip(player, 166, -1, 255);
                         Service.gI().sendThongBao(player, "đang phát triển");
                }
            } else if (this.mapId == 166) {
                switch (select) {
                    case 0 ->
                        ChangeMapService.gI().changeMapBySpaceShip(player, 20, -1, 1093);
                }
            }
        }
    }
}
