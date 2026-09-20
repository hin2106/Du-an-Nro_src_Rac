package combine.list;

import combine.CombineService;
import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import player.Player;
import services.ItemService;
import services.ItemTimeService;
import services.Service;
import services.player.InventoryService;

public class CheTaoSuKien2011 {

    // ==================== LÀM Túi Trà Khô ====================
    public static void showCombineTuiTraKho(Player pl) {
        Item laTra = InventoryService.gI().findItemBag(pl, 1364);
        int sl = laTra != null ? laTra.quantity : 0;
        String txt = ConstFont.BOLD_GREEN + "Để làm ra 1 Túi trà khô cần\n"
                + (sl >= 99 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Lá trà: " + sl + "/99\n"
                + "Với công thức gia truyển nhà ta\n bảo đảm thơm ngon và ... \n giòn hơn cả sâu.";
        if (sl >= 99) {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.CHE_TAO_TUI_TRA_KHO,
                    txt, "90 giây\ntới nhận\nhàng", "Sấy\nlấy liền\n5tr vàng", "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.IGNORE_MENU, txt, "Đóng");
        }
    }

    public static void cheTaoTuiTraKho(Player pl, boolean nhanh) {
        Item laTra = InventoryService.gI().findItemBag(pl, 1364);
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            if (nhanh) {
                if (pl.inventory.gold >= 5000000) {
                    CombineService.gI().sendAddItemCombine(pl, ConstNpc.BA_HAT_MIT, laTra);
                    InventoryService.gI().subQuantityItemsBag(pl, laTra, 99);
                    pl.inventory.gold -= 5000000;
                    Item item = ItemService.gI().createNewItem((short) 1368);
                    item.itemOptions.add(new Item.ItemOption(87, 0));
                    InventoryService.gI().addItemBag(pl, item);
                    CombineService.gI().sendEffSuccessVip(pl, item.template.iconID);
                    InventoryService.gI().sendItemBags(pl);
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "Bạn nhận được " + item.template.name);
                } else {
                    Service.gI().sendThongBao(pl, "Không đủ 5tr vàng");
                }
            } else {
                InventoryService.gI().subQuantityItemsBag(pl, laTra, 99);
                InventoryService.gI().sendItemBags(pl);
                pl.combineNew.timeDelay = 90000;
                pl.combineNew.startTimeDelay = System.currentTimeMillis();
                ItemTimeService.gI().sendAllItemTime(pl);
                CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.IGNORE_MENU, "Nhớ ghé lấy đừng bom hàng ta", "Đóng");
            }
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
        }
    }

    public static void nhanTuiTraKho(Player pl) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            Item item = ItemService.gI().createNewItem((short) 1368);
            item.itemOptions.add(new Item.ItemOption(30, 0));
            item.itemOptions.add(new Item.ItemOption(87, 0));
            InventoryService.gI().addItemBag(pl, item);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Bạn nhận được " + item.template.name);
            pl.combineNew.timeDelay = 0;
            pl.combineNew.startTimeDelay = 0;
            ItemTimeService.gI().sendAllItemTime(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
        }
    }

    // ==================== LÀM HỘP TRÀ ====================
    public static void showCombineHopTra(Player pl) {
        Item tuiTra = InventoryService.gI().findItemBag(pl, 1368);
        Item queTre = InventoryService.gI().findItemBag(pl, 1366);
        Item niaTre = InventoryService.gI().findItemBag(pl, 1365);

        int sl1 = tuiTra != null ? tuiTra.quantity : 0;
        int sl2 = queTre != null ? queTre.quantity : 0;
        int sl3 = niaTre != null ? niaTre.quantity : 0;
        long vang = pl.inventory.gold;

        String txt = ConstFont.BOLD_GREEN + "Làm 1 Hộp Trà cần:\n"
                + (sl1 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Túi trà khô: " + sl1 + "/1\n"
                + (sl2 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Que tre: " + sl2 + "/1\n"
                + (sl3 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Nĩa tre: " + sl3 + "/1\n"
                + (vang >= 30000000 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Giá vàng: 30.000.000";

        if (sl1 >= 1 && sl2 >= 1 && sl3 >= 1 && vang >= 30000000) {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.CHE_TAO_HOP_TRA, txt, "Đồng ý", "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.IGNORE_MENU, txt, "Đóng");
        }
    }

    public static void cheTaoHopTra(Player pl) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            if (pl.inventory.gold >= 30000000) {
                Item tuiTra = InventoryService.gI().findItemBag(pl, 1368);
                Item queTre = InventoryService.gI().findItemBag(pl, 1366);
                Item niaTre = InventoryService.gI().findItemBag(pl, 1365);
                CombineService.gI().sendAddItemCombine(pl, ConstNpc.BA_HAT_MIT, tuiTra, queTre, niaTre);
                InventoryService.gI().subQuantityItemsBag(pl, tuiTra, 1);
                InventoryService.gI().subQuantityItemsBag(pl, queTre, 1);
                InventoryService.gI().subQuantityItemsBag(pl, niaTre, 1);
                pl.inventory.gold -= 30000000;
                Item item = ItemService.gI().createNewItem((short) 1369);
                item.itemOptions.add(new Item.ItemOption(86, 0));
                item.itemOptions.add(new Item.ItemOption(174, 2025));
                item.itemOptions.add(new Item.ItemOption(93, 35));
                InventoryService.gI().addItemBag(pl, item);
                CombineService.gI().sendEffSuccessVip(pl, item.template.iconID);
                Service.gI().sendMoney(pl);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Nhận được " + item.template.name);
            } else {
                Service.gI().sendThongBao(pl, "Không đủ vàng");
            }
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
        }
    }

    // ==================== LÀM HỘP TRÀ HOA CÚC ====================
    public static void showCombineHopTraHoaCuc(Player pl) {
        Item hopTra = InventoryService.gI().findItemBag(pl, 1369);
        Item tuiTra = InventoryService.gI().findItemBag(pl, 1368);
        Item queTre = InventoryService.gI().findItemBag(pl, 1366);
        Item niaTre = InventoryService.gI().findItemBag(pl, 1365);
        Item hoaCuc = InventoryService.gI().findItemBag(pl, 1367);

        int sl1 = hopTra != null ? hopTra.quantity : 0;
        int sl2 = tuiTra != null ? tuiTra.quantity : 0;
        int sl3 = queTre != null ? queTre.quantity : 0;
        int sl4 = niaTre != null ? niaTre.quantity : 0;
        int sl5 = hoaCuc != null ? hoaCuc.quantity : 0;
        int ngoc = pl.inventory.gem;

        String txt = ConstFont.BOLD_GREEN + "Làm 1 Hộp Trà Hoa Cúc cần:\n"
                + (sl1 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Hộp trà: " + sl1 + "/1\n"
                + (sl2 >= 2 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Túi trà khô: " + sl2 + "/2\n"
                + (sl3 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Que tre: " + sl3 + "/1\n"
                + (sl4 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Nĩa tre: " + sl4 + "/1\n"
                + (sl5 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Hoa cúc: " + sl5 + "/1\n"
                + (ngoc >= 5 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Giá ngọc: 5";

        if (sl1 >= 1 && sl2 >= 2 && sl3 >= 1 && sl4 >= 1 && sl5 >= 1 && ngoc >= 5) {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.CHE_TAO_HOP_TRA_HOA_CUC, txt, "Đồng ý", "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.IGNORE_MENU, txt, "Đóng");
        }
    }

    public static void cheTaoHopTraHoaCuc(Player pl) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            if (pl.inventory.gem >= 5) {
                Item hopTra = InventoryService.gI().findItemBag(pl, 1369);
                Item tuiTra = InventoryService.gI().findItemBag(pl, 1368);
                Item queTre = InventoryService.gI().findItemBag(pl, 1366);
                Item niaTre = InventoryService.gI().findItemBag(pl, 1365);
                Item hoaCuc = InventoryService.gI().findItemBag(pl, 1367);
                CombineService.gI().sendAddItemCombine(pl, ConstNpc.BA_HAT_MIT, hopTra, tuiTra, queTre, niaTre, hoaCuc);
                InventoryService.gI().subQuantityItemsBag(pl, hopTra, 1);
                InventoryService.gI().subQuantityItemsBag(pl, tuiTra, 2);
                InventoryService.gI().subQuantityItemsBag(pl, queTre, 1);
                InventoryService.gI().subQuantityItemsBag(pl, niaTre, 1);
                InventoryService.gI().subQuantityItemsBag(pl, hoaCuc, 1);
                pl.inventory.gem -= 5;
                Item item = ItemService.gI().createNewItem((short) 1370);
                item.itemOptions.add(new Item.ItemOption(86, 0));
                item.itemOptions.add(new Item.ItemOption(174, 2025));
                item.itemOptions.add(new Item.ItemOption(93, 35));
                InventoryService.gI().addItemBag(pl, item);
                CombineService.gI().sendEffSuccessVip(pl, item.template.iconID);
                Service.gI().sendMoney(pl);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Nhận được " + item.template.name);
            } else {
                Service.gI().sendThongBao(pl, "Không đủ ngọc");
            }
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
        }
    }

    // ==================== LÀM THIỆP CHÚC THƯỜNG ====================
    public static void showCombineThiepChucThuong(Player pl) {
        Item manhGiay = InventoryService.gI().findItemBag(pl, 1373);
        Item baoBi = InventoryService.gI().findItemBag(pl, 1372);
        Item keo = InventoryService.gI().findItemBag(pl, 1374);

        int sl1 = manhGiay != null ? manhGiay.quantity : 0;
        int sl2 = baoBi != null ? baoBi.quantity : 0;
        int sl3 = keo != null ? keo.quantity : 0;
        int ngoc = pl.inventory.gem;

        String txt = ConstFont.BOLD_GREEN + "Làm 1 Thiệp Chúc Thường cần:\n"
                + (sl1 >= 99 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Mảnh giấy: " + sl1 + "/99\n"
                + (sl2 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Bao bì thiệp: " + sl2 + "/1\n"
                + (sl3 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Kéo gián: " + sl3 + "/1\n"
                + (ngoc >= 15 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Giá ngọc: 15";

        if (sl1 >= 99 && sl2 >= 1 && sl3 >= 1 && ngoc >= 15) {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.CHE_TAO_THIEP_CHUC, txt, "Đồng ý", "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.IGNORE_MENU, txt, "Đóng");
        }
    }

    public static void cheTaoThiepChucThuong(Player pl) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            if (pl.inventory.gem >= 15) {
                Item manhGiay = InventoryService.gI().findItemBag(pl, 1373);
                Item baoBi = InventoryService.gI().findItemBag(pl, 1372);
                Item keo = InventoryService.gI().findItemBag(pl, 1374);
                CombineService.gI().sendAddItemCombine(pl, ConstNpc.BA_HAT_MIT, manhGiay, baoBi, keo);
                InventoryService.gI().subQuantityItemsBag(pl, manhGiay, 99);
                InventoryService.gI().subQuantityItemsBag(pl, baoBi, 1);
                InventoryService.gI().subQuantityItemsBag(pl, keo, 1);
                pl.inventory.gem -= 15;
                Item item = ItemService.gI().createNewItem((short) 1376);
                item.itemOptions.add(new Item.ItemOption(30, 0));
                item.itemOptions.add(new Item.ItemOption(87, 0));
                item.itemOptions.add(new Item.ItemOption(174, 2025));
                item.itemOptions.add(new Item.ItemOption(93, 35));
                InventoryService.gI().addItemBag(pl, item);
                CombineService.gI().sendEffSuccessVip(pl, item.template.iconID);
                Service.gI().sendMoney(pl);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Nhận được " + item.template.name);
            } else {
                Service.gI().sendThongBao(pl, "Không đủ ngọc");
            }
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
        }
    }

    // ==================== LÀM THIỆP CHÚC ĐẶC BIỆT ====================
    public static void showCombineThiepChucDacBiet(Player pl) {
        Item manhGiay = InventoryService.gI().findItemBag(pl, 1373);
        Item baoBi = InventoryService.gI().findItemBag(pl, 1372);
        Item keo = InventoryService.gI().findItemBag(pl, 1374);
        Item loiChuc = InventoryService.gI().findItemBag(pl, 1375);

        int sl1 = manhGiay != null ? manhGiay.quantity : 0;
        int sl2 = baoBi != null ? baoBi.quantity : 0;
        int sl3 = keo != null ? keo.quantity : 0;
        int sl4 = loiChuc != null ? loiChuc.quantity : 0;
        int ngoc = pl.inventory.gem;

        String txt = ConstFont.BOLD_GREEN + "Làm 1 Thiệp Chúc Đặc Biệt cần:\n"
                + (sl1 >= 99 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Mảnh giấy: " + sl1 + "/99\n"
                + (sl2 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Bao bì thiệp: " + sl2 + "/1\n"
                + (sl3 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Keo gián: " + sl3 + "/1\n"
                + (sl4 >= 1 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Lời chúc: " + sl4 + "/1\n"
                + (ngoc >= 15 ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED) + "Giá ngọc: 15";

        if (sl1 >= 99 && sl2 >= 1 && sl3 >= 1 && sl4 >= 1 && ngoc >= 15) {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.CHE_TAO_THIEP_CHUC_DAC_BIET, txt, "Đồng ý", "Từ chối");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(pl, ConstNpc.IGNORE_MENU, txt, "Đóng");
        }
    }

    public static void cheTaoThiepChucDacBiet(Player pl) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            if (pl.inventory.gem >= 15) {
                Item manhGiay = InventoryService.gI().findItemBag(pl, 1373);
                Item baoBi = InventoryService.gI().findItemBag(pl, 1372);
                Item keo = InventoryService.gI().findItemBag(pl, 1374);
                Item loiChuc = InventoryService.gI().findItemBag(pl, 1375);
                CombineService.gI().sendAddItemCombine(pl, ConstNpc.BA_HAT_MIT, manhGiay, baoBi, keo, loiChuc);
                InventoryService.gI().subQuantityItemsBag(pl, manhGiay, 99);
                InventoryService.gI().subQuantityItemsBag(pl, baoBi, 1);
                InventoryService.gI().subQuantityItemsBag(pl, keo, 1);
                InventoryService.gI().subQuantityItemsBag(pl, loiChuc, 1);
                pl.inventory.gem -= 15;
                Item item = ItemService.gI().createNewItem((short) 1377);
                item.itemOptions.add(new Item.ItemOption(30, 0));
                item.itemOptions.add(new Item.ItemOption(87, 0));
                item.itemOptions.add(new Item.ItemOption(174, 2025));
                item.itemOptions.add(new Item.ItemOption(93, 35));
                InventoryService.gI().addItemBag(pl, item);
                CombineService.gI().sendEffSuccessVip(pl, item.template.iconID);
                Service.gI().sendMoney(pl);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Nhận được " + item.template.name);
            } else {
                Service.gI().sendThongBao(pl, "Không đủ ngọc");
            }
        } else {
            Service.gI().sendThongBao(pl, "Hành trang đầy");
        }
    }
}
