package combine.list;

import combine.CombineService;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import player.Player;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class PhanRaTrangBiKichHoat {

    public class PhanRaTrangBi {
        
        private static final int[][] optionIds = {
            {128, 129, 127, 233, 245, 130, 131, 132, 233, 237, 133, 135, 134, 233, 241}};

        public static void showInfoCombine(Player player) {
            if (player.combineNew.itemsCombine.size() != 1) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần đặt đúng vật phẩm!", "Đóng");
                return;
            }

            Item item1 = player.combineNew.itemsCombine.get(0);

            if (!isValidItem(item1)) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm không đủ điều kiện để phân rã!", "Đóng");
                return;
            }

            player.combineNew.goldCombine = 2_000_000_000;
            player.combineNew.ratioCombine = 100;

            String npcSay = "|2|Tỉ lệ thành công: " + 100 + "%\n"
                    + "|2|Cần: " + Util.numberToMoney(2_000_000_000) + " vàng\n";

            if (player.inventory.gold < 2_000_000_000) {
                npcSay += "|7|Còn thiếu " + Util.powerToString(2_000_000_000 - player.inventory.gold) + " vàng\n";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Phân rã\n" + Util.numberToMoney(2_000_000_000) + " vàng", "Từ chối");
            }
        }

        public static void ThucHienPhanRa(Player player) {
            if (player.combineNew.itemsCombine.size() != 1) {
                Service.gI().sendThongBao(player, "Cần đặt đúng 1 vật phẩm!");
                return;
            }

            Item item1 = player.combineNew.itemsCombine.get(0);

            if (!isValidItem(item1)) {
                Service.gI().sendThongBao(player, "Vật phẩm không đủ điều kiện để phân rã!");
                return;
            }

            if (player.inventory.gold < 2_000_000_000) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện!");
                return;
            }

            if (item1.quantity < 1) {
                Service.gI().sendThongBao(player, "Không đủ vật phẩm để thực hiện!");
                return;
            }
            player.inventory.gold -= 2_000_000_000;
            InventoryService.gI().subQuantityItemsBag(player, item1, 1);
            if (Util.isTrue(100, 100)) {
                int itemId = 1656;
                Item existingItem = InventoryService.gI().findItemBag(player, itemId);
                if (existingItem != null) {
                    existingItem.quantity += 1;
                } else {
                    Item newItem = new Item();
                    newItem.template = ItemService.gI().getTemplate(itemId);
                    newItem.quantity = 1;
                    InventoryService.gI().addItemBag(player, newItem);
                }
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Phân rã thành công!");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Phân rã thất bại!");
            }

            InventoryService.gI().sendItemBags(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }

        private static boolean isValidItem(Item item) {
            if (item == null || item.itemOptions == null) {
                return false;
            }
            for (ItemOption option : item.itemOptions) {
                for (int[] optionsForRace : optionIds) {
                    for (int validOptionId : optionsForRace) {
                        if (option.optionTemplate.id == validOptionId) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

    }
}
