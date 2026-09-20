package combine.list;

import combine.CombineService;
import combine.CombineSystem;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

public class EpSaoTrangBi {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineSystem.isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (CombineSystem.isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0;
            int starEmpty = 0;
            if (trangBi != null && daPhaLe != null
                    && trangBi.template != null && trangBi.itemOptions != null) {
                for (ItemOption io : trangBi.itemOptions) {
                    if (io != null && io.optionTemplate != null) {
                        if (io.optionTemplate.id == 102) {
                            star = io.param;
                        } else if (io.optionTemplate.id == 107) {
                            starEmpty = io.param;
                        }
                    }
                }
                if (starEmpty <= 9) {
                    if (starEmpty >= 8 && !CombineSystem.CheckSlot(trangBi, starEmpty)) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần cường hóa lỗ sao pha lê thứ " + (starEmpty == 8 ? "8" : "9") + " trước khi ép vào",
                                "Đóng");
                        return;
                    }

                    player.combineNew.gemCombine = CombineSystem.getGemEpSao(star);
                    String npcSay = trangBi.template.name + "\n|2|";
                    for (ItemOption io : trangBi.itemOptions) {
                        if (io != null && io.optionTemplate != null && io.optionTemplate.id != 102) {
                            npcSay += io.getOptionString() + "\n";
                        }
                    }

                    if (daPhaLe.template != null && daPhaLe.template.type == 30) {
                        if (daPhaLe.itemOptions != null) {
                            for (ItemOption io : daPhaLe.itemOptions) {
                                npcSay += "|7|" + io.getOptionString() + "\n";
                            }
                        }
                    } else {
                        int optionId = CombineSystem.getOptionDaPhaLe(daPhaLe);
                        int param = CombineSystem.getParamDaPhaLe(daPhaLe);
                        if (optionId != -1 && param != -1) {
                            npcSay += "|7|" + ItemService.gI().getItemOptionTemplate(optionId).name
                                    .replaceAll("#", param + "") + "\n";
                        }
                    }
                    npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.gemCombine) + " ngọc";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào, và lỗ sao tối đa là 9",
                            "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
        }
    }

    public static void epSaoTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (CombineSystem.isTrangBiPhaLeHoa(item)) {
                    trangBi = item;
                } else if (CombineSystem.isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0;
            int starEmpty = 0;
            if (trangBi != null && daPhaLe != null
                    && trangBi.template != null && trangBi.itemOptions != null) {
                ItemOption optionStar = null;
                for (ItemOption io : trangBi.itemOptions) {
                    if (io != null && io.optionTemplate != null) {
                        if (io.optionTemplate.id == 102) {
                            star = io.param;
                            optionStar = io;
                        } else if (io.optionTemplate.id == 107) {
                            starEmpty = io.param;
                        }
                    }
                }
                if (star < starEmpty) {
                    if (starEmpty >= 8 && !CombineSystem.CheckSlot(trangBi, starEmpty)) {
                        Service.gI().sendThongBao(player, "Cần cường hóa lỗ sao pha lê thứ "
                                + (starEmpty == 8 ? "8" : "9") + " trước khi ép vào");
                        return;
                    }
                    player.inventory.subGem(gem);
                    int optionId = CombineSystem.getOptionDaPhaLe(daPhaLe);
                    int param = CombineSystem.getParamDaPhaLe(daPhaLe);
                    if (optionId == -1 || param == -1) {
                        Service.gI().sendThongBao(player, "Đá pha lê không hợp lệ");
                        return;
                    }
                    ItemOption option = null;
                    for (ItemOption io : trangBi.itemOptions) {
                        if (io != null && io.optionTemplate != null && io.optionTemplate.id == optionId) {
                            option = io;
                            break;
                        }
                    }
                    if (optionStar != null && starEmpty >= 8) {
                        ItemOption newOption = new ItemOption(optionId, param);
                        trangBi.itemOptions.add(newOption);
                        if (starEmpty == 8) {
                            optionStar.param = 8;
                            Service.gI().sendThongBao(player, "Đã ép sao lên 8 thành công!");
                        } else if (starEmpty == 9) {
                            optionStar.param = 9;
                            Service.gI().sendThongBao(player, "Đã ép sao lên 9 thành công!");
                        }
                    } else {
                        if (option != null) {
                            option.param += param;
                        } else {
                            trangBi.itemOptions.add(new ItemOption(optionId, param));
                        }
                        if (optionStar != null) {
                            optionStar.param++;
                        } else {
                            trangBi.itemOptions.add(new ItemOption(102, 1));
                        }
                    }
                    InventoryService.gI().subQuantityItemsBag(player, daPhaLe, 1);
                    CombineService.gI().reOpenItemCombine(player);
                    CombineService.gI().sendEffectSuccessCombine(player);
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendMoney(player);
                }
            }
        }
    }
}
