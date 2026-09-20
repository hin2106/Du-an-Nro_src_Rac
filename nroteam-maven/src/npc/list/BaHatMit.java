package npc.list;

import consts.ConstEvent;
import server.Manager;

import consts.ConstNpc;
import item.Item;
import managers.tournament.DeathOrAliveArenaManager;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import services.map.ChangeMapService;
import combine.CombineService;
import combine.list.CheTaoSuKien2011;
import combine.list.NangCapVatPham;
import daos.HistoryTransactionDAO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import services.ShopService;
import services.player.DailyGiftService;
import services.tournament.DeathOrAliveArenaService;
import services.top.TopVoDaiSinhTuService;
import tournament.DeathOrAliveArena;
import utils.TimeUtil;
import utils.Util;

public class BaHatMit extends Npc {

    public BaHatMit(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    private static final int[][] OPTION_IDS = {
            { 128, 129, 127, 233, 245, 130, 131, 132, 233, 237, 133, 135, 134, 233, 241 }
    };

    private boolean showPhanRaKichHoat(Player player) {
        try {
            if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
                return false;
            }

            for (Item it : player.inventory.itemsBag) {
                if (it == null || !it.isNotNullItem() || it.itemOptions == null) {
                    continue;
                }

                for (int optId : OPTION_IDS[0]) {
                    boolean has = it.itemOptions.stream()
                            .anyMatch(o -> o != null && o.optionTemplate != null && o.optionTemplate.id == optId);
                    if (has) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean showTaiTaoCapsule(Player player) {
        try {
            boolean deobiettengif = InventoryService.gI().isExistItemBag(player, 1634)
                    || InventoryService.gI().isExistItemBox(player, 1634)
                    || InventoryService.gI().isExistItemBody(player, 1634);
            boolean cut1233 = InventoryService.gI().isExistItemBag(player, 1656)
                    || InventoryService.gI().isExistItemBox(player, 1656)
                    || InventoryService.gI().isExistItemBody(player, 1656);
            return deobiettengif && cut1233;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (player.idMark != null && player.idMark.getIndexMenu() == ConstNpc.MENU_XEM_LS_GIAO_DICH) {
                if (player.lichSuGiaoDichDangXem != null) {
                    player.lichSuGiaoDichDangXem = null;
                }
            }
            switch (this.mapId) {
                case 5 -> {
                    List<String> opts = new ArrayList<>();
                    opts.add("Chức năng\npha lê"); // idx = 0
                    opts.add("Chuyển hoá\ntrang bị"); // idx = 1
                    opts.add("Võ đài\nSinh tử"); // idx = 2 // idx = 3
                    if (showTaiTaoCapsule(player)) { 
                        opts.add("Tái tạo\nCapsule\nKích hoạt");
                    }
                    if (showPhanRaKichHoat(player)) { // idx = 5 
                        opts.add("Phân rã\nTrang bị\nKích hoạt");
                    }

                    if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_HALLOWEEN) {
                        opts.add("Sự kiện\nHalloween");
                    }else if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_20_11) {
                        opts.add("Sự kiện\n20-11");
                    }
                    opts.add("Đóng");
                    createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?",
                            opts.toArray(String[]::new));
                }

                case 112 -> {
                    if (Util.isAfterMidnight(player.lastTimePKVoDaiSinhTu)) {
                        player.haveRewardVDST = false;
                        player.gemVoDaiSinhTu = 0;
                    }
                    if (player.haveRewardVDST) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Đây là phần thưởng cho con.",
                                "1 ngọc băng\nngẫu nhiên", "1 bí ngô");
                        return;
                    }
                    if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone).getPlayer().equals(player)) {
                            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                    "Ngươi muốn hủy đăng ký thi đấu võ đài?",
                                    "Top 100", "Đồng ý\n" + player.gemVoDaiSinhTu + " ngọc", "Từ chối", "Về\nđảo rùa");
                            return;
                        }
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                                "Top 100", "Bình chọn", "Đồng ý\n" + player.gemVoDaiSinhTu + " ngọc", "Từ chối",
                                "Về\nđảo rùa");
                        return;
                    }
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi muốn đăng ký thi đấu võ đài?\nnhiều phần thưởng giá trị đang đợi ngươi đó",
                            "Top 100", "Đồng ý\n" + player.gemVoDaiSinhTu + " ngọc", "Từ chối", "Về\nđảo rùa");
                }
                case 174 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi tìm ta có việc gì?",
                            "Quay về", "Từ chối");
                case 181 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi tìm ta có việc gì?",
                            "Quay về", "Từ chối");
                default -> {
                    List<String> menu = new ArrayList<>(Arrays.asList(
                    "Cửa hàng\nBùa",
                    "Nâng cấp\nVật phẩm",
                    "Làm phép\nNhập đá",
                    "Nhập\nNgọc Rồng"));
                    if (DailyGiftService.checkDailyGift(player, DailyGiftService.NHAN_BUA_MIEN_PHI)) {
                        menu.add(0, "Thưởng\nBùa 1h\nngẫu nhiên");
                    }
                    String[] menus = menu.toArray(String[]::new);
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi tìm ta có việc gì?", menus);
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            int currentMenu = player.idMark.getIndexMenu();
            if (currentMenu == ConstNpc.MENU_XEM_LS_GIAO_DICH) {
                xuLyPhanTrangLichSu(player, select);
                return;
            }
            switch (this.mapId) {
                case 5 -> {
                    if (player.idMark.isBaseMenu()) {
                        int currentIdx = 0;

                        if (select == currentIdx++) {
                            createOtherMenu(player, 3,
                                    "Ta có thể giúp gì cho ngươi ?",
                                    "Ép sao\ntrang bị", "Pha lê\nhóa\ntrang bị", "Đóng");
                            return;
                        }

                        if (select == currentIdx++) {
                            createOtherMenu(player, 6,
                                    "Ta sẽ biến trang bị mới cao cấp hơn của ngươi\nthành trang bị có cấp độ và sao pha lê của trang bị cũ",
                                    "Chuyển hóa\nDùng vàng", "Chuyển hóa\nDùng ngọc", "Đóng");
                            return;
                        }

                        if (select == currentIdx++) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 112, 200 + Util.nextInt(-100, 100),
                                    408);
                            return;
                        }

                        if (showTaiTaoCapsule(player)) {
                            if (select == currentIdx++) {
                                CombineService.gI().openTabCombine(player, CombineService.TAI_TAO_CAPSULE_KH);
                                return;
                            }
                        }

                        if (showPhanRaKichHoat(player)) {
                            if (select == currentIdx++) {
                                CombineService.gI().openTabCombine(player, CombineService.PHAN_RA_TRANG_BI_KH);
                                return;
                            }
                        }

                        if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_HALLOWEEN) {
                            if (select == currentIdx++) {
                                List<String> hwm = new ArrayList<>();
                                hwm.add("Làm Hộp Kẹo\nMa Quỷ");
                                boolean freeAvail = Util.isAfterMidnight(player.lastTimeFreeHalloweenWish);
                                if (freeAvail) {
                                    hwm.add("Nhận miễn phí");
                                }
                                hwm.add("Ước bằng\n50 ngọc");
                                hwm.add("Đóng");
                                createOtherMenu(player, 9091, "Sự kiện Halloween", hwm.toArray(String[]::new));
                                return;
                            }
                        }

                         if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_20_11) {
                            if (select == currentIdx++) {
                                if (player.combineNew != null && player.combineNew.timeDelay > 0 
                                    && (System.currentTimeMillis() - player.combineNew.startTimeDelay) < player.combineNew.timeDelay) {
                                    createOtherMenu(player, 9095, "Ta có thể giúp gì cho ngươi ?",
                                            "Làm\n Hộp trà", "Làm\n Hộp trà hoa cúc",
                                            "Làm\n Thiệp chúc thường", "Làm\n Thiệp chúc\n đặc biệt", "Từ chối");
                                } else if (player.combineNew != null && player.combineNew.timeDelay == 0 && player.combineNew.startTimeDelay > 0) {
                                    createOtherMenu(player, 9095, "Ta có thể giúp gì cho ngươi ?",
                                            "Nhận túi\ntrà khô", "Làm\n Hộp trà",
                                            "Làm\n Hộp trà hoa cúc", "Làm\n Thiệp chúc thường",
                                            "Làm\n Thiệp chúc\n đặc biệt", "Từ chối");
                                } else {
                                    createOtherMenu(player, 9095, "Ta có thể giúp gì cho ngươi ?",
                                            "Làm\n Túi trà khô", "Làm\n Hộp trà",
                                            "Làm\n Hộp trà hoa cúc", "Làm\n Thiệp chúc thường",
                                            "Làm\n Thiệp chúc\n đặc biệt", "Từ chối");
                                }
                                return;
                            }
                            
                        }
                    } // Các menu phụ:
                    else if (player.idMark.getIndexMenu() == 3) {
                        switch (select) {
                            case 0 ->
                                CombineService.gI().openTabCombine(player, CombineService.EP_SAO_TRANG_BI);
                            case 1 ->
                                createOtherMenu(player, 9, "Ta có thể giúp gì cho ngươi ?", "Bằng ngọc", "Đóng");
                            
                            }
                    } else if (player.idMark.getIndexMenu() == 6) {
                        switch (select) {
                            case 0 ->
                                CombineService.gI().openTabCombine(player,
                                        CombineService.CHUYEN_HOA_TRANG_BI_DUNG_VANG);
                            case 1 ->
                                CombineService.gI().openTabCombine(player,
                                        CombineService.CHUYEN_HOA_TRANG_BI_DUNG_NGOC);
                        }
                    } else if (player.idMark.getIndexMenu() == 9) {
                        if (select == 0) {
                            CombineService.gI().openTabCombine(player, CombineService.PHA_LE_HOA_TRANG_BI);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                        switch (player.combineNew.typeCombine) {
                            case CombineService.PHA_LE_HOA_TRANG_BI -> {
                                switch (select) {
                                    case 0 ->
                                        CombineService.gI().startCombineVip(player, 100);
                                    case 1 ->
                                        CombineService.gI().startCombineVip(player, 10);
                                    case 2 ->
                                        CombineService.gI().startCombine(player);
                                }
                            }
                            case CombineService.EP_SAO_TRANG_BI -> {
                                if (select == 0) {
                                    CombineService.gI().startCombine(player);
                                }
                            }
                        }
                    } else if (player.idMark.getIndexMenu() == 9091) {
                        boolean freeAvail = Util.isAfterMidnight(player.lastTimeFreeHalloweenWish);
                        int idx2 = 0;
                        if (select == idx2++) {
                            createOtherMenu(player, 9090, "Con muốn làm bao nhiêu Hộp Kẹo Ma Quỷ?", "Làm 1 lần",
                                    "Làm 10 lần", "Đóng");
                        } else if (freeAvail && select == idx2++) {
                            createOtherMenu(player, 9092, "Nhận miễn phí 1 lần mỗi ngày", "Đồng ý", "Từ chối");
                        } else if (select == idx2++) {
                            createOtherMenu(player, 9093, "50 ngọc cũng được, hãy ước đi thể hiện mình là đại gia.",
                                    "Đồng ý", "Từ chối");
                        }

                    } else if (player.idMark.getIndexMenu() == 9090) {
                        Item it901 = InventoryService.gI().findItemBag(player, 901); // Kẹo bàn tay
                        Item it1348 = InventoryService.gI().findItemBag(player, 1348); // Giỏ đựng kẹo
                        Item it1354 = InventoryService.gI().findItemBag(player, 1354); // Giấy trang trí

                        int have901 = it901 != null ? it901.quantity : 0;
                        int have1348 = it1348 != null ? it1348.quantity : 0;
                        int have1354 = it1354 != null ? it1354.quantity : 0;

                        int req1_901 = 99, req1_1348 = 1, req1_1354 = 3;
                        int req10_901 = req1_901 * 10, req10_1348 = req1_1348 * 10, req10_1354 = req1_1354 * 10;

                        switch (select) {
                            case 0 -> {
                                String info1 = new StringBuilder()
                                        .append("|7|Làm 1 Hộp Kẹo Ma Quỷ\n")
                                        .append("|2|Cần vật phẩm:\n")
                                        .append("- Kẹo bàn tay ").append(have901).append("/").append(req1_901)
                                        .append("\n")
                                        .append("- Giỏ đựng kẹo trái bí ").append(have1348).append("/")
                                        .append(req1_1348).append("\n")
                                        .append("- Giấy trang trí Halloween ").append(have1354).append("/")
                                        .append(req1_1354)
                                        .toString();

                                createOtherMenu(player, 9094, info1, "Xác nhận", "Hủy");
                            }
                            case 1 -> {
                                // 🧾 Thông báo nguyên liệu cho làm 10 lần
                                String info10 = new StringBuilder()
                                        .append("|7|Làm 10 Hộp Kẹo Ma Quỷ\n")
                                        .append("|2|Cần vật phẩm:\n")
                                        .append("- Kẹo bàn tay ").append(have901).append("/").append(req10_901)
                                        .append("\n")
                                        .append("- Giỏ đựng kẹo trái bí ").append(have1348).append("/")
                                        .append(req10_1348).append("\n")
                                        .append("- Giấy trang trí Halloween ").append(have1354).append("/")
                                        .append(req10_1354)
                                        .toString();

                                createOtherMenu(player, 9095, info10, "Xác nhận", "Hủy");
                            }
                        }

                    }else if (player.idMark.getIndexMenu() == 9095) {
                        if (player.combineNew != null && player.combineNew.timeDelay > 0 
                            && (System.currentTimeMillis() - player.combineNew.startTimeDelay) < player.combineNew.timeDelay) {
                            switch (select) {
                                case 0 -> CheTaoSuKien2011.showCombineHopTra(player);
                                case 1 -> CheTaoSuKien2011.showCombineHopTraHoaCuc(player);
                                case 2 -> CheTaoSuKien2011.showCombineThiepChucThuong(player);
                                case 3 -> CheTaoSuKien2011.showCombineThiepChucDacBiet(player);
                                default -> {
                                }
                            }
                        } else if (player.combineNew != null && player.combineNew.timeDelay == 0 && player.combineNew.startTimeDelay > 0) {
                            switch (select) {
                                case 0 -> {
                                    createOtherMenu(player, ConstNpc.NHAN_TUI_TRA_KHO, 
                                            "Cứ mang lá trà tươi tới ta sấy cho nhé.", "Đồng ý");
                                }
                                case 1 -> CheTaoSuKien2011.showCombineHopTra(player);
                                case 2 -> CheTaoSuKien2011.showCombineHopTraHoaCuc(player);
                                case 3 -> CheTaoSuKien2011.showCombineThiepChucThuong(player);
                                case 4 -> CheTaoSuKien2011.showCombineThiepChucDacBiet(player);
                                default -> {
                                }
                            }
                        } else {
                            switch (select) {
                                case 0 -> CheTaoSuKien2011.showCombineTuiTraKho(player);
                                case 1 -> CheTaoSuKien2011.showCombineHopTra(player);
                                case 2 -> CheTaoSuKien2011.showCombineHopTraHoaCuc(player);
                                case 3 -> CheTaoSuKien2011.showCombineThiepChucThuong(player);
                                case 4 -> CheTaoSuKien2011.showCombineThiepChucDacBiet(player);
                                default -> {
                                }
                            }
                        }
                    }else if (player.idMark.getIndexMenu() == ConstNpc.NHAN_TUI_TRA_KHO) {
                        if (select == 0) {
                            CheTaoSuKien2011.nhanTuiTraKho(player);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.CHE_TAO_TUI_TRA_KHO) {
                        if (select == 0) {
                            CheTaoSuKien2011.cheTaoTuiTraKho(player, false);
                        } else if (select == 1) {
                            CheTaoSuKien2011.cheTaoTuiTraKho(player, true);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.CHE_TAO_HOP_TRA) {
                        if (select == 0) {
                            CheTaoSuKien2011.cheTaoHopTra(player);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.CHE_TAO_HOP_TRA_HOA_CUC) {
                        if (select == 0) {
                            CheTaoSuKien2011.cheTaoHopTraHoaCuc(player);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.CHE_TAO_THIEP_CHUC) {
                        if (select == 0) {
                            CheTaoSuKien2011.cheTaoThiepChucThuong(player);
                        }
                    }else if (player.idMark.getIndexMenu() == ConstNpc.CHE_TAO_THIEP_CHUC_DAC_BIET) {
                        if (select == 0) {
                            CheTaoSuKien2011.cheTaoThiepChucDacBiet(player);
                        }
                    } else if (player.idMark.getIndexMenu() == 9094) {
                        if (select == 0) {
                            craftHalloweenBox(player, 1);
                        }

                    } else if (player.idMark.getIndexMenu() == 9095) {
                        if (select == 0) {
                            craftHalloweenBox(player, 10);
                        }

                    } else if (player.idMark.getIndexMenu() == 9092) {
                        if (select == 0) {
                            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                                Service.gI().sendThongBao(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
                                return;
                            }
                            boolean freeAvail = Util.isAfterMidnight(player.lastTimeFreeHalloweenWish);
                            if (!freeAvail) {
                                Service.gI().sendThongBao(player, "Hôm nay bạn đã nhận miễn phí rồi!");
                                return;
                            }
                            player.lastTimeFreeHalloweenWish = System.currentTimeMillis();
                            halloweenWishReward(player);
                        }

                    } else if (player.idMark.getIndexMenu() == 9093) {
                        if (select == 0) {
                            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                                Service.gI().sendThongBao(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
                                return;
                            }
                            if (player.inventory.getGem() < 50) {
                                Service.gI().sendThongBao(player, "Bạn không đủ 50 ngọc");
                                return;
                            }
                            player.inventory.subGem(50);
                            Service.gI().sendMoney(player);
                            halloweenWishReward(player);
                        }
                    }
                }

                case 112 -> {
                    if (player.idMark.isBaseMenu()) {
                        if (player.haveRewardVDST) {
                            switch (select) {
                                case 0 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                        Item item = ItemService.gI().createNewItem((short) (Util.nextInt(705, 708)));
                                        item.itemOptions.add(new Item.ItemOption(93, 30));
                                        InventoryService.gI().addItemBag(player, item);
                                        InventoryService.gI().sendItemBags(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);
                                        player.haveRewardVDST = false;
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Hành trang không còn chỗ trống, không thể nhặt thêm");
                                    }
                                }
                                case 1 -> {
                                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                                        Item item = ItemService.gI().createNewItem((short) 585);
                                        item.itemOptions.add(new Item.ItemOption(93, 30));
                                        InventoryService.gI().addItemBag(player, item);
                                        InventoryService.gI().sendItemBags(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name);
                                        player.haveRewardVDST = false;
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Hành trang không còn chỗ trống, không thể nhặt thêm");
                                    }
                                }
                            }
                            return;
                        }

                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                        if (vdst != null) {
                            if (vdst.getPlayer().equals(player)) {
                                switch (select) {
                                    case 0 ->
                                        TopVoDaiSinhTuService.gI().showTop(player);
                                    case 1 -> {
                                        vdst.endChallenge();
                                        Service.gI().sendThongBao(player, "Bạn đã hủy đăng ký thi đấu");
                                    }
                                    case 2 -> {
                                    }
                                    case 3 ->
                                        ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                                }
                                return;
                            }
                            switch (select) {
                                case 0 ->
                                    TopVoDaiSinhTuService.gI().showTop(player);
                                case 1 ->
                                    this.createOtherMenu(player, ConstNpc.DAT_CUOC_HAT_MIT,
                                            "Phí bình chọn là 1 triệu vàng\nkhi trận đấu kết thúc\n90% tổng tiền bình chọn sẽ chia đều cho phe bình chọn chính xác",
                                            "Bình chọn cho " + vdst.getPlayer().name + " (" + vdst.getCuocPlayer()
                                                    + ")",
                                            "Bình chọn cho hạt mít (" + vdst.getCuocBaHatMit() + ")");
                                case 2 -> {
                                    if (player.nPoint != null && player.nPoint.power >= 100_000
                                            && player.nPoint.power <= 150_000_000) {
                                        DeathOrAliveArenaService.gI().startChallenge(player);
                                    } else {
                                        this.npcChat(player, "Chỉ cho phép sức mạnh từ 100k tới 150 triệu");
                                    }
                                }
                                case 3 -> {
                                    // Từ chối - không làm gì
                                }
                                case 4 ->
                                    ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                            }
                            return;
                        }

                        // Menu khi không có ai đang thi đấu
                        switch (select) {
                            case 0 ->
                                TopVoDaiSinhTuService.gI().showTop(player);
                            case 1 -> {
                                if (player.nPoint != null && player.nPoint.power >= 100_000
                                        && player.nPoint.power <= 150_000_000) {
                                    DeathOrAliveArenaService.gI().startChallenge(player);
                                } else {
                                    this.npcChat(player, "Chỉ cho phép sức mạnh từ 100k tới 150 triệu");
                                }
                            }
                            case 2 -> {
                                // Từ chối - không làm gì
                            }
                            case 3 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.DAT_CUOC_HAT_MIT) {
                        if (DeathOrAliveArenaManager.gI().getVDST(player.zone) != null) {
                            switch (select) {
                                case 0 -> {
                                    if (player.inventory.gold >= 1_000_000) {
                                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                                        vdst.setCuocPlayer(vdst.getCuocPlayer() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonPlayer++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                                                + Util.numberToMoney(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                                case 1 -> {
                                    if (player.inventory.gold >= 1_000_000) {
                                        DeathOrAliveArena vdst = DeathOrAliveArenaManager.gI().getVDST(player.zone);
                                        vdst.setCuocBaHatMit(vdst.getCuocBaHatMit() + 1);
                                        vdst.addBinhChon(player);
                                        player.binhChonHatMit++;
                                        player.zoneBinhChon = player.zone;
                                        player.inventory.gold -= 1_000_000;
                                        Service.gI().sendMoney(player);
                                    } else {
                                        Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                                                + Util.numberToMoney(1_000_000 - player.inventory.gold) + " vàng nữa");
                                    }
                                }
                            }
                        }
                    }
                }
                case 174 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    }
                }
                case 181 -> {
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 ->
                                ChangeMapService.gI().changeMapBySpaceShip(player, 5, -1, 1156);
                        }
                    }
                }
                case 42, 43, 44, 84 -> {
                    if (player.idMark.isBaseMenu()) {
                        // Sử dụng index động để mapping đúng menu options
                        int currentIdx = 0;

                        // Index 0 (nếu có): Thưởng Bùa 1h ngẫu nhiên
                        boolean hasDailyGift = DailyGiftService.checkDailyGift(player,
                                DailyGiftService.NHAN_BUA_MIEN_PHI);
                        if (hasDailyGift) {
                            if (select == currentIdx++) {
                                int idItem = Util.nextInt(213, 219);
                                player.charms.addTimeCharms(idItem, 60);
                                Item bua = ItemService.gI().createNewItem((short) idItem);
                                Service.gI().sendThongBao(player, "Bạn vừa nhận thưởng " + bua.template.name);
                                DailyGiftService.updateDailyGift(player, DailyGiftService.NHAN_BUA_MIEN_PHI);
                                return;
                            }
                        }

                        // Index 1: Kiểm tra Giao dịch 1 ngọc
                        // if (select == currentIdx++) {
                        //     kiemTraVaBatDauXemLichSuGiaoDich(player);
                        //     return;
                        // }

                        // Index 3: Cửa hàng Bùa
                        // Cửa hàng Bùa
                        if (select == currentIdx++) {
                            createOtherMenu(player, ConstNpc.MENU_OPTION_SHOP_BUA,
                                    "Bùa của ta rất lợi hại, nhìn ngươi yếu đuối thế này, chắc muốn mua bùa để "
                                            + "mạnh mẽ à, mua không ta bán cho, xài rồi lại thích cho mà xem.",
                                    "Bùa\n1 giờ", "Bùa\n8 giờ", "Bùa\n1 tháng", "Đóng");
                            return;
                        }

                        // Index 4: Nâng cấp Vật phẩm
                        if (select == currentIdx++) {
                            CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_VAT_PHAM);
                            return;
                        }

                        if (select == currentIdx++) {
                            CombineService.gI().openTabCombine(player, CombineService.LAM_PHEP_NHAP_DA);
                            return;
                        }

                        if (select == currentIdx++) {
                            CombineService.gI().openTabCombine(player, CombineService.NHAP_NGOC_RONG);
                            return;
                        }
                        break;
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_SHOP_BUA) {
                        switch (select) {
                            case 0 ->
                                ShopService.gI().opendShop(player, "BUA_1H", true);
                            case 1 ->
                                ShopService.gI().opendShop(player, "BUA_8H", true);
                            case 2 ->
                                ShopService.gI().opendShop(player, "BUA_1M", true);
                        }
                    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                        switch (player.combineNew.typeCombine) {
                            case    CombineService.LAM_PHEP_NHAP_DA, CombineService.NHAP_NGOC_RONG -> {
                                if (select == 0) {
                                    CombineService.gI().startCombine(player);
                                }
                            }
                            case CombineService.NANG_CAP_VAT_PHAM -> {
                                if (select == 0) {
                                    CombineService.gI().startCombine(player);
                                } else if (select == 1) {
                                    NangCapVatPham.nangCapVatPham(player);
                                }
                            }
                        }
                    }
                }
                default -> {
                }
            }
        }
    }

    private void kiemTraVaBatDauXemLichSuGiaoDich(Player player) {
        if (player == null) {
            return;
        }
        int gem = player.inventory.getGem();
        if (gem < 1) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc, còn thiếu " + (1 - gem) + " ngọc nữa");
            return;
        }
        player.inventory.subGem(1);
        batDauXemLichSuGiaoDich(player);
    }

    private void batDauXemLichSuGiaoDich(Player player) {
        List<HistoryTransactionDAO.TransactionLog> tatCaLog = HistoryTransactionDAO.getHistoryForPlayer(player, 50);

        if (tatCaLog == null || tatCaLog.isEmpty()) {
            Service.gI().sendThongBao(player, "Bạn chưa có lịch sử giao dịch nào.");
            if (player.lichSuGiaoDichDangXem != null) {
                player.lichSuGiaoDichDangXem = null;
            }
            return;
        }

        player.lichSuGiaoDichDangXem = tatCaLog;
        player.trangThaiLichSuGd = 1;
        hienThiTrangLichSuGiaoDich(player);
    }

    private boolean appendTransactionItems(StringBuilder sb, String rawItemsString, String timestamp,
            String actionPrefix, String otherPlayerName) {
        if (rawItemsString == null || rawItemsString.trim().isEmpty()) {
            return false;
        }

        String[] items = rawItemsString.split(",");
        boolean daAppendGiDo = false;

        for (String itemEntry : items) {
            itemEntry = itemEntry.trim();
            if (itemEntry.isEmpty()) {
                continue;
            }

            if (itemEntry.startsWith("Gold:")) {
                try {
                    String goldValueStr = itemEntry.substring("Gold:".length()).trim();
                    long goldAmount = Long.parseLong(goldValueStr);
                    if (goldAmount > 0) {
                        String formattedGold = String.format("%,d", goldAmount).replace(",", ".");
                        sb.append(String.format("[%s] %s %s: %s vàng\n", timestamp, actionPrefix, otherPlayerName,
                                formattedGold));
                        daAppendGiDo = true;
                    }
                } catch (NumberFormatException e) {
                }
            } else {
                sb.append(String.format("[%s] %s %s: %s\n", timestamp, actionPrefix, otherPlayerName, itemEntry));
                daAppendGiDo = true;
            }
        }
        return daAppendGiDo;
    }

    private void hienThiTrangLichSuGiaoDich(Player player) {
        if (player.lichSuGiaoDichDangXem.isEmpty()) {
            Service.gI().sendThongBao(player, "Bạn không có lịch sử giao dịch nào.");
            return;
        }
        List<HistoryTransactionDAO.TransactionLog> tatCaLog = player.lichSuGiaoDichDangXem;
        int trangHienTai = player.trangThaiLichSuGd;
        int tongSoLog = tatCaLog.size();
        int tongSoTrang = (int) Math.ceil((double) tongSoLog / 1);

        if (trangHienTai < 1) {
            trangHienTai = 1;
        }
        if (trangHienTai > tongSoTrang && tongSoTrang > 0) {
            trangHienTai = tongSoTrang;
        }
        player.trangThaiLichSuGd = trangHienTai;

        int startIndex = (trangHienTai - 1) * 1;
        int endIndex = Math.min(startIndex + 1, tongSoLog);

        List<HistoryTransactionDAO.TransactionLog> logTrangNay = new ArrayList<>();
        if (startIndex >= 0 && startIndex < endIndex && endIndex <= tongSoLog) {
            logTrangNay = tatCaLog.subList(startIndex, endIndex);
        }

        StringBuilder noiDungTrang = new StringBuilder();
        String dinhDanhNguoiChoiHienTai = player.name + " (" + player.id + ")";

        if (!logTrangNay.isEmpty()) {
            for (HistoryTransactionDAO.TransactionLog log : logTrangNay) {
                String thoiGianFormat = TimeUtil.formatTime(log.transactionTime.getTime(), "yyyy-MM-dd HH:mm:ss");
                String tenNguoiChoiKhac;
                String vpPlayer1TrongLog = log.itemsExchangedByPlayer1;
                String vpPlayer2TrongLog = log.itemsExchangedByPlayer2;
                String vpBanDuaThucTe;
                String vpBanNhanThucTe;

                if (log.player1NameRecord.equals(dinhDanhNguoiChoiHienTai)) {
                    tenNguoiChoiKhac = log.player2NameRecord;
                    vpBanDuaThucTe = vpPlayer1TrongLog;
                    vpBanNhanThucTe = vpPlayer2TrongLog;
                } else {
                    tenNguoiChoiKhac = log.player1NameRecord;
                    vpBanDuaThucTe = vpPlayer2TrongLog;
                    vpBanNhanThucTe = vpPlayer1TrongLog;
                }

                boolean coGiDua = appendTransactionItems(noiDungTrang, vpBanDuaThucTe, thoiGianFormat, "Cho",
                        tenNguoiChoiKhac);
                boolean coGiNhan = appendTransactionItems(noiDungTrang, vpBanNhanThucTe, thoiGianFormat, "Nhận từ",
                        tenNguoiChoiKhac);

                // Thêm dòng trống giữa các giao dịch
                if (coGiDua || coGiNhan) {
                    noiDungTrang.append("\n");
                }
            }
        }

        if (noiDungTrang.length() > 0 && noiDungTrang.charAt(noiDungTrang.length() - 1) == '\n') {
            noiDungTrang.setLength(noiDungTrang.length() - 1);
        }

        String loiNpcNoi = noiDungTrang.toString().trim();
        if (loiNpcNoi.isEmpty()) {
            if (tongSoLog == 0) {
                loiNpcNoi = "Bạn không có lịch sử giao dịch nào.";
            } else {
                loiNpcNoi = "Không có thông tin giao dịch để hiển thị trên trang này.";
            }
        }

        List<String> tuyChonMenu = new ArrayList<>();
        if (tongSoTrang > 0) {
            String trangInfo = String.format("[%d/%d]", trangHienTai, tongSoTrang);
            if (trangHienTai > 1) {
                tuyChonMenu.add("Quay lại " + trangInfo);
            }
            if (trangHienTai < tongSoTrang) {
                tuyChonMenu.add("Tiếp theo " + trangInfo);
            }
        }

        tuyChonMenu.add("Đóng");

        this.createOtherMenu(player, ConstNpc.MENU_XEM_LS_GIAO_DICH, loiNpcNoi, tuyChonMenu.toArray(String[]::new));
    }

    private void xuLyPhanTrangLichSu(Player player, int luaChonSelect) {
        if (player.lichSuGiaoDichDangXem == null) {
            return;
        }
        int trangHienTai = player.trangThaiLichSuGd;
        int tongSoTrang = (int) Math.ceil((double) player.lichSuGiaoDichDangXem.size() / 1);

        List<String> cacTuyChonDaHienThi = new ArrayList<>();
        if (tongSoTrang > 0) {
            if (trangHienTai > 1) {
                cacTuyChonDaHienThi.add("Quay lại");
            }
            if (trangHienTai < tongSoTrang) {
                cacTuyChonDaHienThi.add("Tiếp theo");

            }
        }
        cacTuyChonDaHienThi.add("Đóng");

        if (luaChonSelect < 0 || luaChonSelect >= cacTuyChonDaHienThi.size()) {
            player.lichSuGiaoDichDangXem = null;
            return;
        }

        String hanhDong = cacTuyChonDaHienThi.get(luaChonSelect);

        if (hanhDong.startsWith("Quay lại")) {
            player.trangThaiLichSuGd--;
            hienThiTrangLichSuGiaoDich(player);
        } else if (hanhDong.startsWith("Tiếp theo")) {
            player.trangThaiLichSuGd++;
            hienThiTrangLichSuGiaoDich(player);
        } else if (hanhDong.equals("Đóng")) {
            player.lichSuGiaoDichDangXem = null;
        } else {
            player.lichSuGiaoDichDangXem = null;
        }
    }

    // ====== HALLOWEEN FEATURE: Craft & Wish ======
    private void craftHalloweenBox(Player player, int times) {
        int need901 = 99 * times; // Kẹo bàn tay (id 901)
        int need1348 = 1 * times; // Giỏ đựng kẹo trái bí (id 1348)
        int need1354 = 3 * times; // Giấy trang trí Halloween (id 1354)

        Item it901 = InventoryService.gI().findItemBag(player, 901);
        Item it1348 = InventoryService.gI().findItemBag(player, 1348);
        Item it1354 = InventoryService.gI().findItemBag(player, 1354);

        if (it901 == null || it901.quantity < need901) {
            Service.gI().sendThongBao(player,
                    "Thiếu Kẹo bàn tay (" + (it901 != null ? it901.quantity : 0) + "/" + need901 + ")");
            return;
        }
        if (it1348 == null || it1348.quantity < need1348) {
            Service.gI().sendThongBao(player,
                    "Thiếu Giỏ đựng kẹo trái bí (" + (it1348 != null ? it1348.quantity : 0) + "/" + need1348 + ")");
            return;
        }
        if (it1354 == null || it1354.quantity < need1354) {
            Service.gI().sendThongBao(player,
                    "Thiếu Giấy trang trí Halloween (" + (it1354 != null ? it1354.quantity : 0) + "/" + need1354 + ")");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
            return;
        }

        // Trừ nguyên liệu
        InventoryService.gI().subQuantityItemsBag(player, it901, need901);
        InventoryService.gI().subQuantityItemsBag(player, it1348, need1348);
        InventoryService.gI().subQuantityItemsBag(player, it1354, need1354);
        InventoryService.gI().sendItemBags(player);

        // Tạo Hộp Kẹo Ma Quỷ id 1356
        Item box = ItemService.gI().createNewItem((short) 1356);
        box.quantity = times;
        InventoryService.gI().addItemBag(player, box);
        InventoryService.gI().sendItemBags(player);
        // Cộng điểm TOP Hộp kẹo Ma Quỷ tương ứng số lần chế tạo
        try {
            services.top.TopHalloweenService.gI().addCandyPoint(player, times);
        } catch (Exception ignored) {
        }
        Service.gI().sendThongBao(player, "Bạn đã làm thành công " + times + " Hộp Kẹo Ma Quỷ");
    }

    private void halloweenWishReward(Player pl) {
        short[] fragIds = new short[] { 828, 829, 830, 831, 832, 833, 834, 835, 836, 837, 838, 839, 840, 841, 842 };
        short goiDau9 = 597;
        short dau10 = 595;
        short petThoU = 1686;
        short ctGohanBu = 765;
        short[] ctVipByGender = new short[] { 604, 605, 606 };
        int roll = Util.nextInt(1, 100);
        Item reward;
        if (roll <= 35) {
            reward = ItemService.gI().createNewItem(fragIds[Util.nextInt(0, fragIds.length - 1)]);
            reward.itemOptions.add(new Item.ItemOption(30, 0));
            reward.quantity = 1;
        } else if (roll <= 50) {
            int idItem = Util.nextInt(213, 219);
            pl.charms.addTimeCharms(idItem, 60);
            reward = ItemService.gI().createNewItem((short) idItem);
            reward.quantity = 1;
        } else if (roll <= 62) {
            reward = ItemService.gI().createNewItem(goiDau9);
            reward.quantity = 1;
        } else if (roll <= 75) {
            reward = ItemService.gI().createNewItem(dau10);
            reward.quantity = Util.nextInt(20, 30);
        } else if (roll <= 88) {
            reward = ItemService.gI().createNewItem(ctVipByGender[pl.gender]);
            reward.itemOptions.add(new Item.ItemOption(50, 23));
            reward.itemOptions.add(new Item.ItemOption(77, 20));
            reward.itemOptions.add(new Item.ItemOption(103, 20));
            if (Util.isTrue(10, 100)) {
                reward.itemOptions.add(new Item.ItemOption(73, 0));
            } else {
                int days = (Util.nextInt(0, 1) == 0) ? 15 : 30;
                reward.itemOptions.add(new Item.ItemOption(93, days));
            }
        } else if (roll <= 95) {
            reward = ItemService.gI().createNewItem(ctGohanBu);
            reward.itemOptions.add(new Item.ItemOption(50, 22));
            reward.itemOptions.add(new Item.ItemOption(77, 19));
            reward.itemOptions.add(new Item.ItemOption(103, 19));
            reward.itemOptions.add(new Item.ItemOption(27, 5));
            reward.itemOptions.add(new Item.ItemOption(28, 5));
            reward.itemOptions.add(new Item.ItemOption(177, 0));
            if (Util.isTrue(10, 100)) {
                reward.itemOptions.add(new Item.ItemOption(73, 0));
            } else {
                int days = (Util.nextInt(0, 1) == 0) ? 15 : 30;
                reward.itemOptions.add(new Item.ItemOption(93, days));
            }
        } else {
            reward = ItemService.gI().createNewItem(petThoU);
            reward.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 15)));
            reward.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 15)));
            reward.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 15)));
            reward.itemOptions.add(new Item.ItemOption(236, Util.nextInt(10, 20)));
            reward.itemOptions.add(new Item.ItemOption(14, 10));
            reward.itemOptions.add(new Item.ItemOption(30, 0));
            if (Util.isTrue(10, 100)) {
                reward.itemOptions.add(new Item.ItemOption(73, 0));
            } else {
                int days = (Util.nextInt(0, 1) == 0) ? 15 : 30;
                reward.itemOptions.add(new Item.ItemOption(93, days));
            }
        }
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().sendItemBags(pl);
        if (reward.quantity > 1) {
            Service.gI().sendThongBao(pl, "Bạn nhận được " + reward.quantity + " " + reward.template.name);
        } else {
            Service.gI().sendThongBao(pl, "Bạn nhận được " + reward.template.name);
        }
    }

}
