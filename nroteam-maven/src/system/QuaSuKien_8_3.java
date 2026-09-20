
package system;

import item.Item;
import item.Item.ItemOption;
import services.ItemService;
import player.Player;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class QuaSuKien_8_3 {

    public static void HopQuaChinhChu(Player player) {
        Item hopqua_1 = InventoryService.gI().findItemBag(player, 1510);
        Item ItemCap_1 = ItemService.gI().createNewItem((short) Util.nextInt(381, 384), 1);
        Item CaiTrang = ItemService.gI().createNewItem((short) Util.nextInt(1503, 1504), 1);
        Item ManhSuuTam = ItemService.gI().createNewItem((short) Util.nextInt(828, 836), 1);

        if (hopqua_1 == null || hopqua_1.quantity < 1) {
            Service.gI().sendThongBaoOK(player, "Hộp Quà Đâu");
return;
        }
        // Option ItemCap_1
        ItemCap_1.itemOptions.add(new ItemOption(73, 1));

        // Option CaiTrang
        CaiTrang.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
        CaiTrang.itemOptions.add(new ItemOption(77, Util.nextInt(19, 25)));
        CaiTrang.itemOptions.add(new ItemOption(103, Util.nextInt(19, 25)));
        CaiTrang.itemOptions.add(new ItemOption(47, Util.nextInt(2, 10)));
        CaiTrang.itemOptions.add(new ItemOption(97, Util.nextInt(1, 37)));

        // Option ManhSuuTam
        ManhSuuTam.itemOptions.add(new ItemOption(30, 1));

        // Cập Nhật & Gửi Item Đến Balo Player
        
        InventoryService.gI().subQuantityItemsBag(player, hopqua_1, 1);
        InventoryService.gI().addItemBag(player, ItemCap_1);
        InventoryService.gI().addItemBag(player, CaiTrang);
        InventoryService.gI().addItemBag(player, ManhSuuTam);
        InventoryService.gI().sendItemBags(player);

    }

    public static void HopQuaNheNhang(Player player) {
        
        Item hopqua_1 = InventoryService.gI().findItemBag(player, 1510);
        Item ItemCap_1 = ItemService.gI().createNewItem((short) Util.nextInt(1150, 1154), 1);
        Item CaiTrang = ItemService.gI().createNewItem((short) Util.nextInt(1503, 1504), 1);
        Item ManhSuuTam = ItemService.gI().createNewItem((short) Util.nextInt(837, 842), 1);
     if (hopqua_1 == null || hopqua_1.quantity < 1) {
            Service.gI().sendThongBaoOK(player, "Hộp Quà Đâu");
return;
        }
        // Option ItemCap_1
        ItemCap_1.itemOptions.add(new ItemOption(73, 1));

        // Option CaiTrang
        CaiTrang.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
        CaiTrang.itemOptions.add(new ItemOption(77, Util.nextInt(19, 25)));
        CaiTrang.itemOptions.add(new ItemOption(103, Util.nextInt(19, 25)));
        CaiTrang.itemOptions.add(new ItemOption(47, Util.nextInt(2, 10)));
        CaiTrang.itemOptions.add(new ItemOption(97, Util.nextInt(1, 37)));

        // Option ManhSuuTam
        ManhSuuTam.itemOptions.add(new ItemOption(30, 1));

        // Cập Nhật & Gửi Item Đến Balo Player
        
        InventoryService.gI().subQuantityItemsBag(player, hopqua_1, 1);
        InventoryService.gI().addItemBag(player, ItemCap_1);
        InventoryService.gI().addItemBag(player, CaiTrang);
        InventoryService.gI().addItemBag(player, ManhSuuTam);
        InventoryService.gI().sendItemBags(player);

    }

}
