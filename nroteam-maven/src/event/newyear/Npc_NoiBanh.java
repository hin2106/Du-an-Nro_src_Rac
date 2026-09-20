package event.newyear;

import consts.ConstMap;
import item.Item;
import item.Item.ItemOption;
import map.Map;
import npc.Npc;
import player.Player;
import server.Manager;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import services.map.MapService;
import utils.Util;

public class Npc_NoiBanh {

    // Item IDs
    private static final int ITEM_THIT_HEO = 748;
    private static final int ITEM_THUNG_NEP = 749;
    private static final int ITEM_DAU_XANH = 750;
    private static final int ITEM_LA_DONG = 751;
    private static final int ITEM_BANH_TET = 752;
    private static final int ITEM_BANH_CHUNG = 753;

    // Cooking costs
    private static final int GOLD_COST_BANH_TET = 5_000_000;
    private static final int GOLD_COST_BANH_CHUNG = 10_000_000;
    private static final int GEM_COST_BANH_CHUNG = 10;

    private static final int THIT_HEO_BANH_TET = 10;
    private static final int NEP_BANH_TET = 10;
    private static final int DAU_BANH_TET = 10;
    private static final int LA_DONG_BANH_TET = 5;

    private static final int THIT_HEO_BANH_CHUNG = 15;
    private static final int NEP_BANH_CHUNG = 10;
    private static final int DAU_BANH_CHUNG = 10;
    private static final int LA_DONG_BANH_CHUNG = 10;
    
   
    
    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.LANG_ARU);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TET) {
            Npc npc;
            npc = new Npc(map.mapId, 1, 545, 432, 66, 7084) {   
                @Override
                public void openBaseMenu(Player player) {
                    createOtherMenu(player, 0, "Xin chào " + player.name + "\nTôi là nồi nấu bánh.\nBạn cần gì?",
                            "Tự nấu\nbánh",
                            "Từ chối");
                }

                @Override
                public void confirmMenu(Player pl, int select) {
                    if (canOpenNpc(pl)) {
                        switch (pl.idMark.getIndexMenu()) {
                            case 0 -> {
                                switch (select) {
                                    case 0 ->
                                        createOtherMenu(pl, 1, "Chọn loại bánh muốn nấu:\n\nNguyên liệu cần thiết:\n" +
                                                "- Bánh Tét: Thịt heo x10, Nếp x10, Đậu xanh x10, Lá dong x5 + 5 triệu vàng\n" +
                                                "- Bánh Chưng: Thịt heo x15, Nếp x10, Đậu xanh x10, Lá dong x10 + 10 triệu vàng + 10 Ngọc Xanh\n\n" +
                                                "Phần thưởng đặc biệt có cải trang vĩnh viễn!",
                                                "Nấu\nBánh Tét",
                                                "Nấu\nBánh Chưng",
                                                "Từ chối");
                                    default -> {
                                    }
                                }
                            }
                            case 1 -> {
                                switch (select) {
                                    case 0 -> // banh tet
                                        cookBanhTet(pl);
                                    case 1 -> // banh chung
                                        cookBanhChung(pl);
                                }
                            }
                        }
                    }
                }
            };
            map.addNpc(npc);
        }
    }

    private void cookBanhTet(Player pl) {
        Item thitheo = InventoryService.gI().findItemBag(pl, ITEM_THIT_HEO);
        Item thungnep = InventoryService.gI().findItemBag(pl, ITEM_THUNG_NEP);
        Item thungdauxanh = InventoryService.gI().findItemBag(pl, ITEM_DAU_XANH);
        Item ladong = InventoryService.gI().findItemBag(pl, ITEM_LA_DONG);
        
        StringBuilder missingItems = new StringBuilder();
        
        // Check gold
        if (pl.inventory.gold < GOLD_COST_BANH_TET) {
            long missing = GOLD_COST_BANH_TET - pl.inventory.gold;
            missingItems.append("Vàng x").append(Util.formatNumber(missing));
        }
        
        if (thitheo == null || thitheo.quantity < THIT_HEO_BANH_TET) {
            int missing = THIT_HEO_BANH_TET - (thitheo == null ? 0 : thitheo.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Thịt heo x").append(missing);
        }
        if (thungnep == null || thungnep.quantity < NEP_BANH_TET) {
            int missing = NEP_BANH_TET - (thungnep == null ? 0 : thungnep.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Thúng nếp x").append(missing);
        }
        if (thungdauxanh == null || thungdauxanh.quantity < DAU_BANH_TET) {
            int missing = DAU_BANH_TET - (thungdauxanh == null ? 0 : thungdauxanh.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Thúng đậu xanh x").append(missing);
        }
        if (ladong == null || ladong.quantity < LA_DONG_BANH_TET) {
            int missing = LA_DONG_BANH_TET - (ladong == null ? 0 : ladong.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Lá dong x").append(missing);
        }
        
        if (missingItems.length() == 0) {
            // Deduct gold
            pl.inventory.gold -= GOLD_COST_BANH_TET;
            Service.gI().sendMoney(pl);
            
            // Deduct ingredients
            InventoryService.gI().subQuantityItemsBag(pl, thitheo, THIT_HEO_BANH_TET);
            InventoryService.gI().subQuantityItemsBag(pl, thungnep, NEP_BANH_TET);
            InventoryService.gI().subQuantityItemsBag(pl, thungdauxanh, DAU_BANH_TET);
            InventoryService.gI().subQuantityItemsBag(pl, ladong, LA_DONG_BANH_TET);

            // Give Banh Tet
            Item banh_tet = ItemService.gI().createNewItem((short) ITEM_BANH_TET);
            banh_tet.itemOptions.add(new ItemOption(93, 30));
            InventoryService.gI().addItemBag(pl, banh_tet);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được Bánh Tét!");
            
            
        } else {
            Service.gI().sendThongBaoOK(pl, "Không đủ nguyên liệu, bạn còn thiếu: " + missingItems.toString());
        }
    }

    private void cookBanhChung(Player pl) {
        Item thitheo = InventoryService.gI().findItemBag(pl, ITEM_THIT_HEO);
        Item thungnep = InventoryService.gI().findItemBag(pl, ITEM_THUNG_NEP);
        Item thungdauxanh = InventoryService.gI().findItemBag(pl, ITEM_DAU_XANH);
        Item ladong = InventoryService.gI().findItemBag(pl, ITEM_LA_DONG);
        
        StringBuilder missingItems = new StringBuilder();
        
        // Check gold
        if (pl.inventory.gold < GOLD_COST_BANH_CHUNG) {
            long missing = GOLD_COST_BANH_CHUNG - pl.inventory.gold;
            missingItems.append("Vàng x").append(Util.formatNumber(missing));
        }

        if (pl.inventory.gem < GEM_COST_BANH_CHUNG) {
            int missingGem = GEM_COST_BANH_CHUNG - pl.inventory.gem;
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Ngọc xanh x").append(missingGem);
        }
        
        if (thitheo == null || thitheo.quantity < THIT_HEO_BANH_CHUNG) {
            int missing = THIT_HEO_BANH_CHUNG - (thitheo == null ? 0 : thitheo.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Thịt heo x").append(missing);
        }
        if (thungnep == null || thungnep.quantity < NEP_BANH_CHUNG) {
            int missing = NEP_BANH_CHUNG - (thungnep == null ? 0 : thungnep.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Thúng nếp x").append(missing);
        }
        if (thungdauxanh == null || thungdauxanh.quantity < DAU_BANH_CHUNG) {
            int missing = DAU_BANH_CHUNG - (thungdauxanh == null ? 0 : thungdauxanh.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Thúng đậu xanh x").append(missing);
        }
        if (ladong == null || ladong.quantity < LA_DONG_BANH_CHUNG) {
            int missing = LA_DONG_BANH_CHUNG - (ladong == null ? 0 : ladong.quantity);
            if (missingItems.length() > 0) missingItems.append(", ");
            missingItems.append("Lá dong x").append(missing);
        }
        
        
        if (missingItems.length() == 0) {
            // Deduct gold
            pl.inventory.gold -= GOLD_COST_BANH_CHUNG;
            pl.inventory.gem -= GEM_COST_BANH_CHUNG;
            Service.gI().sendMoney(pl);
            
            // Deduct ingredients
            InventoryService.gI().subQuantityItemsBag(pl, thitheo, THIT_HEO_BANH_CHUNG);
            InventoryService.gI().subQuantityItemsBag(pl, thungnep, NEP_BANH_CHUNG);
            InventoryService.gI().subQuantityItemsBag(pl, thungdauxanh, DAU_BANH_CHUNG);
            InventoryService.gI().subQuantityItemsBag(pl, ladong, LA_DONG_BANH_CHUNG);

            // Give Banh Chung
            Item banh_chung = ItemService.gI().createNewItem((short) ITEM_BANH_CHUNG);
            banh_chung.itemOptions.add(new ItemOption(93, 30));
            InventoryService.gI().addItemBag(pl, banh_chung);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được Bánh Chưng!");
            
           
        } else {
            Service.gI().sendThongBaoOK(pl, "Không đủ nguyên liệu, bạn còn thiếu: " + missingItems.toString());
        }
    }
    
    
}