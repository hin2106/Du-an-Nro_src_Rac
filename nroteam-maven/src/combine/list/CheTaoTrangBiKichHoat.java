
package combine.list;

import combine.CombineService;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import player.Player;
import services.ItemService;
import services.RewardService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class CheTaoTrangBiKichHoat {

    private static final int COST_DAP_DO_KICH_HOAT = 500_000_000;

    public static void showInfoCombine(Player player) {
        int size = player.combineNew.itemsCombine.size();
        if (size >= 2 && size <= 3) {
            Item dhd = null, dtl = null, dakichhoat = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (item != null && item.isNotNullItem()) {
                    if (item.template.id == 1772) {
                        dakichhoat = item;
                    } else if (item.template.id >= 650 && item.template.id <= 662) {
                        dhd = item;
                    } else if (item.template.id >= 555 && item.template.id <= 567) {
                        dtl = item;
                    }
                }
            }

            if (dhd != null && dakichhoat != null) {
                StringBuilder npcSay = new StringBuilder("|6|").append(dhd.template.name).append("\n");
                for (ItemOption io : dhd.itemOptions) {
                    npcSay.append("|2|").append(io.getOptionString()).append("\n");
                }

                if (dtl != null) {
                    npcSay.append("|6|").append(dtl.template.name).append("\n");
                    for (ItemOption io : dtl.itemOptions) {
                        npcSay.append("|2|").append(io.getOptionString()).append("\n");
                    }
                }

                npcSay.append("Ngươi có muốn chuyển hóa thành\n")
                      .append("|1|").append(combine.CombineSystem.getNameItemC0(dhd.template.gender, dhd.template.type))
                      .append(" (ngẫu nhiên kích hoạt)\n|7|Tỉ lệ thành công ")
                      .append(dtl != null ? "100%" : "40%")
                      .append("\n|2|Cần ").append(Util.numberToMoney(COST_DAP_DO_KICH_HOAT)).append(" vàng");

                if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
                            "Xác nhận");
                } else {
                    long shortfall = COST_DAP_DO_KICH_HOAT - player.inventory.gold;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(),
                            "Còn thiếu\n" + Util.numberToMoney(shortfall) + " vàng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 món Huỷ Diệt, 1 Đá nâng cấp kích hoạt và (tùy chọn) 1 món Thần Linh để tăng tỷ lệ", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 món Huỷ Diệt, 1 Đá nâng cấp kích hoạt và (tùy chọn) 1 món Thần Linh", "Đóng");
        }
    }

    public static void chetaokh(Player player) {
        int size = player.combineNew.itemsCombine.size();
        if (size >= 2 && size <= 3) {
            Item dhd = null, dtl = null, dakichhoat = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (item != null && item.isNotNullItem()) {
                    if (item.template.id == 1772) {
                        dakichhoat = item;
                    } else if (item.template.id >= 650 && item.template.id <= 662) {
                        dhd = item;
                    } else if (item.template.id >= 555 && item.template.id <= 567) {
                        dtl = item;
                    }
                }
            }

            if (dhd != null && dakichhoat != null
                    && InventoryService.gI().getCountEmptyBag(player) > 0
                    && player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {

                player.inventory.gold -= COST_DAP_DO_KICH_HOAT;

                int tiLe = dtl != null ? 100 : 40;
                boolean isSuccess = Util.isTrue(tiLe, 100);

                if (isSuccess) {
                    CombineService.gI().sendEffectSuccessCombine(player);
                    Item item = ItemService.gI().createNewItem(
                            (short) combine.CombineSystem.getTempIdItemC3(dhd.template.gender, dhd.template.type));
                    RewardService.SetClothes(item.template.id, item.template.type, item.itemOptions);
                    RewardService.gI().initActivationOption(
                            item.template.gender < 3 ? item.template.gender : player.gender,
                            item.template.type, item.itemOptions);
                    InventoryService.gI().addItemBag(player, item);
                } else {
                    CombineService.gI().sendEffectFailCombine(player);
                }

                InventoryService.gI().subQuantityItemsBag(player, dhd, 1);
                InventoryService.gI().subQuantityItemsBag(player, dakichhoat, 1);
                if (dtl != null) {
                    InventoryService.gI().subQuantityItemsBag(player, dtl, 1);
                }

                InventoryService.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                CombineService.gI().reOpenItemCombine(player);
            } else {
                Service.gI().sendThongBao(player, "Không đủ điều kiện để chế tạo hoặc hành trang đã đầy.");
            }
        }
    }
}