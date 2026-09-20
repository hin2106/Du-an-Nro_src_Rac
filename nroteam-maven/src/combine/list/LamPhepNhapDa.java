package combine.list;

import combine.CombineService;
import consts.ConstNpc;
import item.Item;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import system.Template;
import utils.Util;

public class LamPhepNhapDa {

    private static final int GOLD_TAO_DA = 10_000_000;  // gold
    private static final int RATIO_TAO_DA = 80;  // ratio

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item item1 = player.combineNew.itemsCombine.get(0);
            Item item2 = player.combineNew.itemsCombine.get(1);

            if ((item1.template.id == 225 && item2.template.id == 226) || (item1.template.id == 226 && item2.template.id == 225)) {
                // check item pl
                player.combineNew.goldCombine = GOLD_TAO_DA;
                player.combineNew.ratioCombine = RATIO_TAO_DA;

                String npcSay = "....";
                npcSay += "....";
                npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n";
                npcSay += "|2|Cần: " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng\n";
                npcSay += "...";

                // check bag
                if (player.inventory.gold < player.combineNew.goldCombine) {
                    npcSay += "|7|Còn thiếu " + Util.powerToString(player.combineNew.goldCombine - player.inventory.gold) + " vàng\n";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\n" + Util.numberToMoney(player.combineNew.goldCombine) + " vàng\n", "Từ chối");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "...", "Đóng"); // báo cần item gì ấy
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "...", "Đóng"); // như trên
        }
    }

    public static void lamphepnhapda(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            long gold = player.combineNew.goldCombine;

            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }

            Item item1 = player.combineNew.itemsCombine.get(0);
            Item item2 = player.combineNew.itemsCombine.get(1);

            // kiem tra so luong item
            if ((item1.template.id == 225 && item2.template.id == 226) || (item1.template.id == 226 && item2.template.id == 225)) {
                // sub item
                player.inventory.gold -= gold;
                InventoryService.gI().subQuantityItemsBag(player, item1, 10);
                InventoryService.gI().subQuantityItemsBag(player, item2, 1);

                // Ngẫu nhiên nhận item id 220 -> 224
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    int randomId = Util.nextInt(220, 224);
                    Template.ItemTemplate newItemTemplate = ItemService.gI().getTemplate(randomId);
                    Item newItem = new Item();
                    newItem.template = newItemTemplate;
                    newItem.quantity = 1;
                    InventoryService.gI().addItemBag(player, newItem);
                    CombineService.gI().sendEffectSuccessCombine(player);
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                }

                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            } else {
                Service.gI().sendThongBao(player, "..."); // thông báo ko đủ nguyên lịu
            }
        }
    }
}
