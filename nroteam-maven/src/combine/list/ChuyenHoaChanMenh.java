
package combine.list;



import combine.CombineService;
import consts.ConstNpc; // Still used for IGNORE_MENU
import item.Item;
import item.Item.ItemOption;
import player.Player;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

import combine.CombineService;
import consts.ConstNpc;
import item.Item;
import player.Player;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class ChuyenHoaChanMenh {


    public static void showInfoHoanChuyen(Player player) {
        if (player.combineNew == null || player.combineNew.itemsCombine.isEmpty()) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Vui lòng đặt Chân thiên tử và Đá hoàn chuyển vào ô.", "Đóng");
            return;
        }

        Item chanThienTu = null;
        Item daHoanChuyen = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item == null || !item.isNotNullItem()) continue;

            if (item.template.id >= (short)1300 && item.template.id <= (short)1308) { // MIN_CHAN_THIEN_TU_ID, MAX_CHAN_THIEN_TU_ID_FOR_UPGRADE
                chanThienTu = item;
            } else if (item.template.id == (short)1202) { // ITEM_ID_DA_HOAN_CHUYEN
                daHoanChuyen = item;
            }
        }

        if (chanThienTu == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Thiếu Chân thiên tử để hoán chuyển.", "Đóng", "Hướng dẫn\nHoán chuyển");
            return;
        }
         if (daHoanChuyen == null) {
             CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Thiếu Đá hoàn chuyển.", "Đóng", "Hướng dẫn\nHoán chuyển");
            return;
        }

        player.combineNew.DaNangcap = 10; // DA_HOAN_CHUYEN_NEEDED
        player.combineNew.TileNangcap = 70; // HOAN_CHUYEN_SUCCESS_RATE

        StringBuilder npcSay = new StringBuilder();
        npcSay.append("Hoán chuyển ").append(chanThienTu.template.name).append("\n");
        npcSay.append("thành Thánh thiên tử cấp 1\n|2|");
        for (Item.ItemOption io : chanThienTu.itemOptions) {
            npcSay.append(io.getOptionString()).append("\n");
        }
        npcSay.append("|7|Tỉ lệ thành công: ").append(player.combineNew.TileNangcap).append("%\n");
        npcSay.append("|1|Cần ").append(player.combineNew.DaNangcap).append(" Đá hoàn chuyển\n");
        npcSay.append("|1|Phí hoán chuyển: Miễn Phí\n");

        CombineService.gI().baHatMit.createOtherMenu(player, (byte)103, npcSay.toString(), // MENU_XAC_NHAN_HOAN_CHUYEN_CHAN_THIEN_TU
                "Hoán chuyển", "Hướng dẫn\nHoán chuyển", "Đóng");
    }

    public static void doHoanChuyenChanThienTu(Player player) {
        if (player.combineNew == null || player.combineNew.itemsCombine.isEmpty()) {
            Service.gI().sendThongBao(player, "Có lỗi xảy ra, vui lòng thử lại.");
            return;
        }
        
        Item chanThienTu = null;
        Item daHoanChuyen = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item == null || !item.isNotNullItem()) continue;
            if (item.template.id >= (short)1300 && item.template.id <= (short)1308) { // MIN_CHAN_THIEN_TU_ID, MAX_CHAN_THIEN_TU_ID_FOR_UPGRADE
                chanThienTu = item;
            } else if (item.template.id == (short)1202) { // ITEM_ID_DA_HOAN_CHUYEN
                daHoanChuyen = item;
            }
        }

        if (chanThienTu == null || daHoanChuyen == null) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm cần thiết.");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }
        
        int daCan = 10; // DA_HOAN_CHUYEN_NEEDED
        int tileThanhCong = 70; // HOAN_CHUYEN_SUCCESS_RATE

        if (daHoanChuyen.quantity < daCan) {
            Service.gI().sendThongBao(player, "Không đủ Đá hoàn chuyển. Cần " + daCan + ".");
            CombineService.gI().reOpenItemCombine(player);
            return;
        }

        InventoryService.gI().subQuantityItemsBag(player, daHoanChuyen, daCan);
        InventoryService.gI().subQuantityItemsBag(player, chanThienTu, 1);

        boolean success = Util.isTrue(tileThanhCong, 100);

        if (success) {
            CombineService.gI().sendEffectSuccessCombine(player);
            
            Item thanhThienTu = ItemService.gI().createNewItem((short)1309); // ITEM_ID_THANH_THIEN_TU_CAP_1
            thanhThienTu.itemOptions.add(new Item.ItemOption(50, 10)); 
            thanhThienTu.itemOptions.add(new Item.ItemOption(77, 30000)); 
            thanhThienTu.itemOptions.add(new Item.ItemOption(103, 10)); 

            InventoryService.gI().addItemBag(player, thanhThienTu);
            Service.gI().sendThongBao(player, "Hoán chuyển thành công! Nhận được " + thanhThienTu.template.name + "!");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Hoán chuyển thất bại! Chân thiên tử đã bị tiêu hao.");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }


    public static String getHuongDanHoanChuyen() {
        StringBuilder hd = new StringBuilder();
        hd.append("= Hoán Chuyển Chân Thiên Tử =\n\n");
        hd.append("1. Vật phẩm cần:\n");
        hd.append("   - Chân Thiên Tử (Bất kỳ cấp nào từ 1-9).\n");
        hd.append("   - ").append(10).append(" Đá hoàn chuyển.\n\n"); // DA_HOAN_CHUYEN_NEEDED
        hd.append("2. Cách thực hiện:\n");
        hd.append("   - Đặt Chân Thiên Tử và Đá hoàn chuyển vào ô.\n");
        hd.append("   - Nhấn \"Hoán chuyển\" để thử.\n\n");
        hd.append("3. Kết quả:\n");
        hd.append("   - Thành công: Nhận Thánh thiên tử cấp 1.\n");
        hd.append("   - Thất bại: Mất Chân thiên tử và Đá hoàn chuyển.\n");
        hd.append("   - Tỉ lệ thành công: ").append(70).append("%.\n"); // HOAN_CHUYEN_SUCCESS_RATE
        return hd.toString();
    }
}
