package combine.list;
import consts.ConstNpc;
import item.Item;
import combine.CombineService;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import utils.Util;

public class NhapNgocRong {

    public static void showInfoCombine(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 1) {
                Item item = player.combineNew.itemsCombine.get(0);

                if (item != null && item.isNotNullItem() && item.quantity >= 7) {
                    if (item.template.id == 14) {
                        Item loNuocPhep = InventoryService.gI().findItemBag(player, 1029);
                        Item daBaoVe = InventoryService.gI().findItemBag(player, 1143);

                        boolean gold = player.inventory.gold >= 150_000_000L;
                        boolean checkLoNuocPhep = loNuocPhep != null && loNuocPhep.isNotNullItem() && loNuocPhep.quantity >= 1;
                        boolean checkDaBaoVe = daBaoVe != null && daBaoVe.isNotNullItem() && daBaoVe.quantity >= 1;

                        String npcSay = "|2|Con có muốn biến 7 " + item.template.name + " thành\n"
                                + "1 viên Ngọc rồng Siêu Cấp\n"
                                + "|7|Cần 7 " + item.template.name + "\n"
                                + "|7|Cần " + Util.numberToMoney(150_000_000L) + " vàng\n"
                                + "|7|Cần 1 Lọ nước phép\n"
                                + "|2|Tỉ lệ thành công: 50%\n"
                                + "|1|Nếu dùng đá bảo vệ sẽ không bị mất 1 viên ngọc rồng 1 sao khi thất bại.";

                        if (gold && checkLoNuocPhep) {
                            if (checkDaBaoVe) {
                                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                        "Làm phép", "Nâng cấp dùng đá bảo vệ", "Thoát");
                            } else {
                                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                        "Làm phép", "Thoát");
                            }
                        } else {
                            String missingItems = "";
                            if (!gold) {
                                missingItems += "150.000.000 vàng\n";
                            }
                            if (!checkLoNuocPhep) {
                                missingItems += "1 Lọ nước phép\n";
                            }
                            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Còn thiếu:\n" + missingItems, "Đóng");
                        }
                    } else if (item.template.id > 14 && item.template.id <= 20) {
                        String npcSay = "|2|Con có muốn biến 7 " + item.template.name + " thành\n"
                                + "1 viên " + ItemService.gI().getTemplate((short) (item.template.id - 1)).name + "\n"
                                + "|7|Cần 7 " + item.template.name;
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
                    } else {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 7 viên ngọc rồng 1 sao trở lên", "Đóng");
                    }
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 7 viên ngọc rồng 1 sao trở lên", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 7 viên ngọc rồng 1 sao trở lên", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public static void nhapNgocRong(Player player, boolean useDBV) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);

                if (item != null && item.isNotNullItem() && item.quantity >= 7) {
                    if (item.template.id > 14 && item.template.id <= 20&& !useDBV) {
                        CombineService.gI().sendEffectCombineDB(player, item.template.iconID);
                        Item nr = ItemService.gI().createNewItem((short) (item.template.id - 1));
                        InventoryService.gI().addItemBag(player, nr);
                        InventoryService.gI().subQuantityItemsBag(player, item, 7);
                        InventoryService.gI().sendItemBags(player);
                        CombineService.gI().reOpenItemCombine(player);
                        CombineService.gI().sendEffectSuccessCombine(player);
                        dragonpass.DragonPassService.gI().onDragonBallCombined(player);
                    } else if (item.template.id == 14) {
                        Item loNuocPhep = InventoryService.gI().findItemBag(player, 1029);
                        Item daBaoVe = InventoryService.gI().findItemBag(player, 1143);
                        boolean gold = player.inventory.gold >= 150_000_000L;
                        boolean checkLoNuocPhep = loNuocPhep != null && loNuocPhep.isNotNullItem() && loNuocPhep.quantity >= 1;
                        boolean checkDaBaoVe = daBaoVe != null && daBaoVe.isNotNullItem() && daBaoVe.quantity >= 1;
                        
                        if (!gold) {
                            Service.gI().sendThongBao(player, "Bạn không có đủ vàng");
                            return;
                        }
                        if (!checkLoNuocPhep) {
                            Service.gI().sendThongBao(player, "Bạn không có Lọ nước phép");
                            return;
                        }
                        
                        player.inventory.gold -= 150_000_000L;
                        InventoryService.gI().subQuantityItemsBag(player, loNuocPhep, 1);
                        InventoryService.gI().subQuantityItemsBag(player, item, 7);
                        
                        if (Util.isTrue(50, 100)) {
                            Item superDragonBall = ItemService.gI().createNewItem((short) 1015);
                            InventoryService.gI().addItemBag(player, superDragonBall);
                            CombineService.gI().sendEffectSuccessCombine(player);
                            dragonpass.DragonPassService.gI().onDragonBallCombined(player);
                        } else {
                            CombineService.gI().sendEffectFailCombine(player);
                            if (checkDaBaoVe) {
                                InventoryService.gI().subQuantityItemsBag(player, daBaoVe, 1);
                                Item returnedDragonBall = ItemService.gI().createNewItem((short) 14);
                                returnedDragonBall.quantity = 1;
                                InventoryService.gI().addItemBag(player, returnedDragonBall);
                            }
                        }
                        
                        InventoryService.gI().sendItemBags(player);
                        Service.gI().sendMoney(player);
                        CombineService.gI().reOpenItemCombine(player);
                    }
                }
            }
        }
    }

}
