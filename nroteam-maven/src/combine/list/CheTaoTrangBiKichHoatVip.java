
package combine.list;

import combine.CombineService;
import combine.CombineSystem;
import consts.ConstItem;
import consts.ConstNpc;
import item.Item;
import player.Player;
import services.ItemService;
import services.RewardService;
import services.Service;
import services.player.InventoryService;
import utils.Util;



public class CheTaoTrangBiKichHoatVip {

    private static final int COST_DAP_DO_KICH_HOAT = 500_000_000;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            Item it = player.combineNew.itemsCombine.get(0);
            Item it2 = player.combineNew.itemsCombine.get(1);
            Item it3 = player.combineNew.itemsCombine.get(2);

            if (it == null || it2 == null || it3 == null ||
                !CombineSystem.isDestroyClothes(it.template.id) ||
                !CombineSystem.isDotl(it2) ||
                !CombineSystem.isDaKichHoat(it3) ||
                it3.quantity < 10) {

                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 món đồ Hủy Diệt, 1 món đồ Thần Linh và 10 đá kích hoạt", "Đóng");
                return;
            }

            String npcSay = "|1|" + it.template.name + "\n" + it2.template.name + "\nCần 10 đá " + it3.template.name + "\n";
            npcSay += "Ngươi có muốn\nchế tạo đồ hủy diệt với đồ thần linh thành\n";
            npcSay += "|7|" + CombineSystem.getTypeTrangBi(it.template.type)
                    + " cấp bậc ngẫu nhiên (set kích hoạt ngẫu nhiên)\n|2|Cần "
                    + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng";

            if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Chế tạo");
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay,
                        "Thiếu " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT - player.inventory.gold) + " vàng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần đủ 3 món: 1 Hủy Diệt, 1 Thần Linh, 10 đá kích hoạt", "Đóng");
        }
    }

    public static void chetaokhvip(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            Item it = player.combineNew.itemsCombine.get(0);
            Item it2 = player.combineNew.itemsCombine.get(1);
            Item it3 = player.combineNew.itemsCombine.get(2);

            if (it == null || it2 == null || it3 == null ||
                !CombineSystem.isDestroyClothes(it.template.id) ||
                !CombineSystem.isDotl(it2) ||
                !CombineSystem.isDaKichHoat(it3) ||
                it3.quantity < 10) {

                Service.gI().sendThongBao(player, "Nguyên liệu không hợp lệ hoặc chưa đủ số lượng!");
                return;
            }

            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống.");
                return;
            }

            if (player.inventory.gold < COST_DAP_DO_KICH_HOAT) {
                Service.gI().sendThongBao(player, "Không đủ vàng.");
                return;
            }

            player.inventory.subGold(COST_DAP_DO_KICH_HOAT);

            int soluongitem = ConstItem.LIST_ITEM_CLOTHES[0][0].length;
            int id;

            if (player.combineNew.countDap >= 100) {
                id = soluongitem - 1;
            } else {
                if (Util.isTrue(199, 200)) {
                    if (Util.isTrue(1, 200)) {
                        id = Util.nextInt(0, soluongitem - 7); // từ bậc 1 đến bậc 6
                    } else {
                        id = Util.nextInt(5, soluongitem - 2); // từ bậc 6 đến 12
                    }
                } else {
                    id = soluongitem - 2; // bậc cao
                }
            }

            CombineService.gI().sendEffectSuccessCombine(player);

            int gender = it.template.gender == 3 ? 0 : it.template.gender;

            Item newItem = ItemService.gI().createNewItem((short) ConstItem.LIST_ITEM_CLOTHES[gender][it.template.type][id]);
            RewardService.SetClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);
            RewardService.gI().initActivationOption(
                    newItem.template.gender < 3 ? newItem.template.gender : player.gender,
                    newItem.template.type,
                    newItem.itemOptions);

            InventoryService.gI().addItemBag(player, newItem);
            InventoryService.gI().subQuantityItemsBag(player, it, 1);
            InventoryService.gI().subQuantityItemsBag(player, it2, 1);
            InventoryService.gI().subQuantityItemsBag(player, it3, 10);
            InventoryService.gI().sendItemBags(player);

            player.combineNew.countDap++;
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}