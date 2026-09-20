package npc.list;
import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.map.NpcService;
import services.TaskService;
import services.map.ChangeMapService;
import services.ShopService;

public class Bill extends Npc {

    public Bill(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            TaskService.gI().checkDoneTaskTalkNpc(player, this);
            int phieuthucan = 0;
            Item doiphieuthucan = InventoryService.gI().findItemBag(player, (short) 1792);
            if (mapId == 154) {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "...",
                        "Về\nthánh địa\nKaio", "Từ chối");
            } else {
                if (doiphieuthucan != null) {
                    phieuthucan = doiphieuthucan.quantity;
                }
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chưa tới giờ thi đấu, xem hướng dẫn để biết thêm chi tiết",
                        "Nói\nchuyện", "Hướng\ndẫn\nthêm", "Đổi thức ăn\n Lấy phiếu ăn", "Đổi phiếu ăn\n Lấy quà\n[" + phieuthucan + "]", "Từ chối");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 48 -> {
                    switch (player.idMark.getIndexMenu()) {
                        case ConstNpc.BASE_MENU -> {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().canOpenBillShop(player)) {
                                        createOtherMenu(player, 2, "Đói bụng quá...ngươi mang cho ta 99 phần đồ ăn\nta sẽ cho một món đồ Hủy Diệt.\nNếu tâm trạng ta vui ngươi có thể nhận trang bị tăng đến 15% \n|7|LƯU Ý: khi mua đồ hủy diệt sẽ bị trừ 1 đồ thần linh tương tự\n(ví dụ mua áo HD sẽ mất áo thần)", "OK", "Từ chối");
                                    } else {
                                        createOtherMenu(player, 2, "Ngươi trang bị đủ bộ 5 món trang bị Thần\nvà mang 99 phần đồ ăn tới đây...\nrồi ta nói chuyện tiếp.", "OK");
                                    }
                                }
                                case 1 -> {
                                    NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_BILL);
                                }
                                case 2 -> {
                                    createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                            "Vào hành trang, chọn loại thức ăn muốn đổi rồi chọn sử dụng", "OK");
                                }
                                case 3 -> {
                                    Item PhieuThucAn = InventoryService.gI().findItemBag(player, 1792);
                                    if (PhieuThucAn == null || PhieuThucAn.quantity == 0) {
                                        npcChat(player, "Hãy mang phiếu thức ăn đến đây gặp ta.");
                                    } else {
                                        ShopService.gI().opendShop(player, "QDPHIEUAN", false);
                                    }
                                }
                            }
                            break;
                        }
                        case 2 -> {
                            if (select == 0 && InventoryService.gI().canOpenBillShop(player)) {
                                ShopService.gI().opendShop(player, "BILL", true);
                            }
                            break;
                        }
                    }
                }
                case 154 -> {
                    if (select == 0) {
                        ChangeMapService.gI().changeMap(player, 50, -1, 318, 336);
                    }
                }
            }
        }
    }
}