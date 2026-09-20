// package combine.list;

// import combine.CombineService;
// import consts.ConstNpc;
// import item.Item;
// import item.Item.ItemOption;
// import java.util.ArrayList;
// import java.util.List;
// import player.Player;
// import server.ServerNotify;
// import services.ItemService;
// import services.Service;
// import services.player.InventoryService;
// import utils.Util;

// public class CheTaoTrangBiThienSu {

//     private static final long GOLD_CHE_TAO = 200_000_000L; // đúng ảnh minh hoạ

//     // Đá Nâng Cấp ID: 1074-1078 (Lv1-Lv5)
//     private static final int[] DA_NANG_CAP_IDS = { 1074, 1075, 1076, 1077, 1078 };

//     // Đá May Mắn ID: 1079-1083 (Lv1-Lv5)
//     private static final int[] DA_MAY_MAN_IDS = { 1079, 1080, 1081, 1082, 1083 };

//     // Công Thức Thường ID: 1071-1073 (Trái Đất, Namec, Xayda)
//     private static final int[] CONG_THUC_THUONG_IDS = { 1071, 1072, 1073 };

//     // Công Thức VIP ID: 1084-1086 (Trái Đất, Namec, Xayda)
//     private static final int[] CONG_THUC_VIP_IDS = { 1084, 1085, 1086 };

//     // Mảnh Thiên Sứ ID: 1066-1070 (áo, quần, giày, nhẫn, găng)
//     private static final int[] MANH_THIEN_SU_IDS = { 1066, 1067, 1068, 1069, 1070 };
//     private static final String[] MANH_LABELS = { "Áo", "Quần", "Giày", "Nhẫn", "Găng" };

//     private static final short[][] TRANG_BI_THIEN_SU_IDS = {
//             { 1048, 1051, 1054, 1057, 1060 }, // Trái Đất
//             { 1049, 1052, 1055, 1058, 1061 }, // Namec
//             { 1050, 1053, 1056, 1059, 1062 } // Xayda
//     };

//     private static final int[] OPTION_BONUS_IDS = { 50, 77, 103, 94, 5 };

//     private static final int MANH_THIEN_SU_NEED = 999;
//     private static final int MANH_THIEN_SU_FAIL = 99;

//     private static int levelOf(int id, int[] pool) {
//         for (int i = 0; i < pool.length; i++)
//             if (pool[i] == id)
//                 return i + 1; // 1..5
//         return 0;
//     }

//     private static boolean in(int id, int[] pool) {
//         for (int x : pool)
//             if (x == id)
//                 return true;
//         return false;
//     }

//     private static String goldStr(long g) {
//         return Util.powerToString(g) + " vàng";
//     }

//     private static String planetName(int gender) {
//         return switch (gender) {
//             case 0 -> "Trái Đất";
//             case 1 -> "Namec";
//             case 2 -> "Xayda";
//             default -> "??? lỗi mẹ rồi";
//         };
//     }

//     private static int typeIndexFromManh(Item manh) {
//         for (int i = 0; i < MANH_THIEN_SU_IDS.length; i++) {
//             if (manh.template.id == MANH_THIEN_SU_IDS[i])
//                 return i;
//         }
//         return -1;
//     }

//     public static void showInfoCombine(Player p) {
//         try {
//             Item ctThuong = null, ctVip = null, manh = null, daNC = null, daMM = null;

//             for (Item it : p.combineNew.itemsCombine) {
//                 if (it == null || it.template == null)
//                     continue;
//                 int id = it.template.id;
//                 if (in(id, CONG_THUC_THUONG_IDS))
//                     ctThuong = it;
//                 else if (in(id, CONG_THUC_VIP_IDS))
//                     ctVip = it;
//                 else if (in(id, MANH_THIEN_SU_IDS))
//                     manh = it;
//                 else if (in(id, DA_NANG_CAP_IDS))
//                     daNC = it;
//                 else if (in(id, DA_MAY_MAN_IDS))
//                     daMM = it;
//             }

//             boolean isVip = ctVip != null;
//             Item congThuc = isVip ? ctVip : ctThuong;

//             int lvNC = daNC == null ? 0 : levelOf(daNC.template.id, DA_NANG_CAP_IDS);
//             int lvMM = daMM == null ? 0 : levelOf(daMM.template.id, DA_MAY_MAN_IDS);

//             int bonusNC = lvNC * 10; // +10% .. +50%
//             int bonusMM = lvMM * 10; // +10% .. +50%
//             int base = isVip ? 50 : 35;
//             int success = Math.min(85, base + bonusNC); // cap 85%

//             String planet = "???";
//             String typeName = "Trang Bị";
//             if (congThuc != null) {
//                 int genderCt = congThuc.template.gender > 2 ? p.gender : congThuc.template.gender;
//                 planet = planetName(genderCt);
//             }
//             if (manh != null) {
//                 int typeIdx = typeIndexFromManh(manh);
//                 if (typeIdx >= 0 && typeIdx < MANH_LABELS.length)
//                     typeName = MANH_LABELS[typeIdx];
//             }

//             StringBuilder sb = new StringBuilder();
//             sb.append("|1|Chế tạo ").append(typeName).append(" Thiên Sứ ").append(planet).append("\n");
//             int have = manh == null ? 0 : manh.quantity;
//             sb.append("|2|Mảnh ghép ").append(have).append("/").append(MANH_THIEN_SU_NEED).append("\n");

//             if (lvNC > 0) {
//                 sb.append("|2|Đá nâng cấp cấp ").append(lvNC)
//                         .append(" (+").append(bonusNC).append("% tỉ lệ thành công)\n");
//             }
//             if (lvNC < 1) {
//                 sb.append("|3|Bạn còn thiếu đá nâng cấp\n");
//             }
//             if (lvMM > 0) {
//                 sb.append("|2|Đá may mắn cấp ").append(lvMM)
//                         .append(" (+").append(bonusMM).append("% tỉ lệ tối đa các chỉ số)\n");
//             }

//             sb.append("|2|Tỉ lệ thành công: ").append(success).append("%\n");
//             sb.append("|2|Phí nâng cấp: ").append(Util.powerToString(GOLD_CHE_TAO)).append("\n");

//             boolean missing = false;
//             if (congThuc == null)
//                 missing = true;
//             if (manh == null || have < MANH_THIEN_SU_NEED)
//                 missing = true;
//             if (daNC == null)
//                 missing = true;
//             if (InventoryService.gI().getCountEmptyBag(p) < 1)
//                 missing = true;
//             if (p.inventory.gold < GOLD_CHE_TAO)
//                 missing = true;

//             if (missing) {
//                 CombineService.gI().whis.createOtherMenu(p, ConstNpc.IGNORE_MENU, sb.toString(), "Đóng");
//             } else {
//                 CombineService.gI().whis.createOtherMenu(
//                         p, ConstNpc.MENU_START_COMBINE, sb.toString(),
//                         "Nâng cấp", "Từ chối");
//             }

//         } catch (Exception e) {
//             e.printStackTrace();
//             Service.gI().sendThongBao(p, "Lỗi hiển thị: " + e.getMessage());
//         }
//     }

//     public static void CheTaoTS(Player p) {
//         try {
//             // gom item
//             Item ctThuong = null, ctVip = null, manh = null, daNC = null, daMM = null;
//             for (Item it : p.combineNew.itemsCombine) {
//                 if (it == null || it.template == null)
//                     continue;
//                 int id = it.template.id;
//                 if (in(id, CONG_THUC_THUONG_IDS))
//                     ctThuong = it;
//                 else if (in(id, CONG_THUC_VIP_IDS))
//                     ctVip = it;
//                 else if (in(id, MANH_THIEN_SU_IDS))
//                     manh = it;
//                 else if (in(id, DA_NANG_CAP_IDS))
//                     daNC = it;
//                 else if (in(id, DA_MAY_MAN_IDS))
//                     daMM = it;
//             }

//             boolean isVip = ctVip != null;
//             Item congThuc = isVip ? ctVip : ctThuong;
//             if (congThuc == null || manh == null || daNC == null) {
//                 Service.gI().sendThongBao(p, "Thiếu vật phẩm bắt buộc!");
//                 CombineService.gI().reOpenItemCombine(p);
//                 return;
//             }
//             if (manh.quantity < MANH_THIEN_SU_NEED) {
//                 Service.gI().sendThongBao(p, "Thiếu mảnh: cần " + MANH_THIEN_SU_NEED);
//                 CombineService.gI().reOpenItemCombine(p);
//                 return;
//             }
//             if (InventoryService.gI().getCountEmptyBag(p) < 1) {
//                 Service.gI().sendThongBao(p, "Cần ít nhất 1 ô trống hành trang");
//                 return;
//             }
//             if (p.inventory.gold < GOLD_CHE_TAO) {
//                 Service.gI().sendThongBao(p, "Thiếu " + goldStr(GOLD_CHE_TAO - p.inventory.gold));
//                 return;
//             }

//             // trừ vàng trước
//             p.inventory.gold -= GOLD_CHE_TAO;
//             Service.gI().sendMoney(p);

//             // tỉ lệ
//             int lvNC = levelOf(daNC.template.id, DA_NANG_CAP_IDS); // 1..5
//             int lvMM = daMM == null ? 0 : levelOf(daMM.template.id, DA_MAY_MAN_IDS);
//             int base = isVip ? 50 : 35;
//             int success = Math.min(85, base + lvNC * 10);
//             boolean thanhCong = Util.isTrue(success, 100);

//             if (thanhCong) {
//                 CombineService.gI().sendEffectSuccessCombine(p);

//                 // gender nguồn từ CT; nếu CT gender>2 thì lấy theo player
//                 int genderCt = congThuc.template.gender > 2 ? p.gender : congThuc.template.gender;

//                 // xác định loại trang bị từ mảnh
//                 int typeIdx = typeIndexFromManh(manh);
//                 if (genderCt < 0 || genderCt > 2 || typeIdx < 0 || typeIdx > 4) {
//                     Service.gI().sendThongBao(p, "Hành tinh/loại mảnh không hợp lệ");
//                     return;
//                 }

//                 short equipId = switch (typeIdx) {
//                     case 0 -> TRANG_BI_THIEN_SU_IDS[genderCt][0]; // Áo
//                     case 1 -> TRANG_BI_THIEN_SU_IDS[genderCt][1]; // Quần
//                     case 4 -> TRANG_BI_THIEN_SU_IDS[genderCt][2]; // Găng
//                     case 2 -> TRANG_BI_THIEN_SU_IDS[genderCt][3]; // Giày
//                     case 3 -> TRANG_BI_THIEN_SU_IDS[genderCt][4]; // Nhẫn
//                     default -> -1;
//                 };
//                 if (equipId == -1) {
//                     Service.gI().sendThongBao(p, "Không xác định được trang bị");
//                     return;
//                 }
//                 Item itemTS = ItemService.gI().doThienSu(equipId, genderCt);
//                 if (itemTS == null) {
//                     Service.gI().sendThongBao(p, "Không thể tạo trang bị");
//                     return;
//                 }

//                 int bonusChance = lvMM * 10; // 0..50
//                 int soDong = 0; // Khởi tạo biến đếm số dòng option
//                 if (Util.isTrue(bonusChance, 100)) {
//                     soDong = Util.nextInt(1, 3); // 1-3 dòng bonus
//                     itemTS.itemOptions.add(new ItemOption(15, soDong));
//                     List<Integer> pool = new ArrayList<>();
//                     for (int idOpt : OPTION_BONUS_IDS)
//                         pool.add(idOpt);

//                     for (int i = 0; i < soDong && !pool.isEmpty(); i++) {
//                         int pick = Util.nextInt(0, pool.size() - 1);
//                         int optId = pool.remove(pick);
//                         int param = Util.nextInt(1, 3); // 1-3%
//                         itemTS.itemOptions.add(new ItemOption(optId, param));
//                     }
//                 }

//                 // add vào túi & trừ vật phẩm tiêu hao
//                 InventoryService.gI().addItemBag(p, itemTS);
//                 Service.gI().sendThongBao(p, "Chế tạo thành công! Nhận " + itemTS.template.name);

//                 // Gửi thông báo toàn server
//                 String notifyMessage = "Người chơi " + p.name + " đã chế tạo thành công " + itemTS.template.name;
//                 if (soDong > 0) {
//                     notifyMessage += " với " + soDong + " dòng chỉ số thưởng,mọi người đều kinh ngạc.";
//                 }
//                 ServerNotify.gI().notify(notifyMessage);

//                 InventoryService.gI().subQuantityItemsBag(p, manh, MANH_THIEN_SU_NEED);
//             } else {
//                 CombineService.gI().sendEffectFailCombine(p);
//                 Service.gI().sendThongBao(p, "Chế tạo thất bại!");
//                 if (manh != null)
//                     InventoryService.gI().subQuantityItemsBag(p, manh, MANH_THIEN_SU_FAIL);
//             }

//             // trừ các nguyên liệu 1 cái
//             if (congThuc != null)
//                 InventoryService.gI().subQuantityItemsBag(p, congThuc, 1);
//             if (daNC != null)
//                 InventoryService.gI().subQuantityItemsBag(p, daNC, 1);
//             if (daMM != null)
//                 InventoryService.gI().subQuantityItemsBag(p, daMM, 1);

//             InventoryService.gI().sendItemBags(p);
//             Service.gI().sendMoney(p);
//             CombineService.gI().reOpenItemCombine(p);

//         } catch (Exception e) {
//             e.printStackTrace();
//             Service.gI().sendThongBao(p, "Lỗi chế tạo: " + e.getMessage());
//         }
//     }

// }


package combine.list;

import combine.CombineService;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import java.util.ArrayList;
import java.util.List;
import player.Player;
import server.ServerNotify;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class CheTaoTrangBiThienSu {

    private static final long GOLD_CHE_TAO = 200_000_000L; // đúng ảnh minh hoạ

    // Đá Nâng Cấp ID: 1074-1078 (Lv1-Lv5)
    private static final int[] DA_NANG_CAP_IDS = { 1074, 1075, 1076, 1077, 1078 };

    // Đá May Mắn ID: 1079-1083 (Lv1-Lv5)
    private static final int[] DA_MAY_MAN_IDS = { 1079, 1080, 1081, 1082, 1083 };

    // Công Thức Thường ID: 1071-1073 (Trái Đất, Namec, Xayda)
    private static final int[] CONG_THUC_THUONG_IDS = { 1071, 1072, 1073 };

    // Công Thức VIP ID: 1084-1086 (Trái Đất, Namec, Xayda)
    private static final int[] CONG_THUC_VIP_IDS = { 1084, 1085, 1086 };

    // Mảnh Thiên Sứ ID: 1066-1070 (áo, quần, giày, nhẫn, găng)
    private static final int[] MANH_THIEN_SU_IDS = { 1066, 1067, 1068, 1069, 1070 };
    private static final String[] MANH_LABELS = { "Áo", "Quần", "Giày", "Nhẫn", "Găng" };

    private static final short[][] TRANG_BI_THIEN_SU_IDS = {
            { 1048, 1051, 1054, 1057, 1060 }, // Trái Đất
            { 1049, 1052, 1055, 1058, 1061 }, // Namec
            { 1050, 1053, 1056, 1059, 1062 } // Xayda
    };

    private static final int[] OPTION_BONUS_IDS = { 50, 77, 103, 94, 5 };

    private static final int MANH_THIEN_SU_NEED = 999;
    private static final int MANH_THIEN_SU_FAIL = 99;

    // Tỉ lệ thành công công thức thường: 35-40-45-50-55 (Lv1-Lv5)
    private static final int[] TI_LE_THUONG = { 0, 35, 40, 45, 50, 55 };

    // Tỉ lệ thành công công thức VIP: 50-55-60-65-70 (Lv1-Lv5)
    private static final int[] TI_LE_VIP = { 0, 50, 55, 60, 65, 70 };

    private static int levelOf(int id, int[] pool) {
        for (int i = 0; i < pool.length; i++)
            if (pool[i] == id)
                return i + 1; // 1..5
        return 0;
    }

    private static boolean in(int id, int[] pool) {
        for (int x : pool)
            if (x == id)
                return true;
        return false;
    }

    private static String goldStr(long g) {
        return Util.powerToString(g) + " vàng";
    }

    private static String planetName(int gender) {
        return switch (gender) {
            case 0 -> "Trái Đất";
            case 1 -> "Namec";
            case 2 -> "Xayda";
            default -> "??? lỗi mẹ rồi";
        };
    }

    private static int typeIndexFromManh(Item manh) {
        for (int i = 0; i < MANH_THIEN_SU_IDS.length; i++) {
            if (manh.template.id == MANH_THIEN_SU_IDS[i])
                return i;
        }
        return -1;
    }

    public static void showInfoCombine(Player p) {
        try {
            Item ctThuong = null, ctVip = null, manh = null, daNC = null, daMM = null;

            for (Item it : p.combineNew.itemsCombine) {
                if (it == null || it.template == null)
                    continue;
                int id = it.template.id;
                if (in(id, CONG_THUC_THUONG_IDS))
                    ctThuong = it;
                else if (in(id, CONG_THUC_VIP_IDS))
                    ctVip = it;
                else if (in(id, MANH_THIEN_SU_IDS))
                    manh = it;
                else if (in(id, DA_NANG_CAP_IDS))
                    daNC = it;
                else if (in(id, DA_MAY_MAN_IDS))
                    daMM = it;
            }

            boolean isVip = ctVip != null;
            Item congThuc = isVip ? ctVip : ctThuong;

            int lvNC = daNC == null ? 0 : levelOf(daNC.template.id, DA_NANG_CAP_IDS);
            int lvMM = daMM == null ? 0 : levelOf(daMM.template.id, DA_MAY_MAN_IDS);

            int bonusNC = lvNC > 0 ? (isVip ? TI_LE_VIP[lvNC] : TI_LE_THUONG[lvNC]) : 0;
            int bonusMM = lvMM * 10; // +10% .. +50%

            String planet = "???";
            String typeName = "Trang Bị";
            if (congThuc != null) {
                int genderCt = congThuc.template.gender > 2 ? p.gender : congThuc.template.gender;
                planet = planetName(genderCt);
            }
            if (manh != null) {
                int typeIdx = typeIndexFromManh(manh);
                if (typeIdx >= 0 && typeIdx < MANH_LABELS.length)
                    typeName = MANH_LABELS[typeIdx];
            }

            StringBuilder sb = new StringBuilder();
            sb.append("|1|Chế tạo ").append(typeName).append(" Thiên Sứ ").append(planet).append("\n");
            int have = manh == null ? 0 : manh.quantity;
            sb.append("|2|Mảnh ghép ").append(have).append("/").append(MANH_THIEN_SU_NEED).append("\n");

            if (lvNC > 0) {
                sb.append("|2|Đá nâng cấp cấp ").append(lvNC)
                        .append(" (Tỉ lệ ").append(bonusNC).append("%)\n");
            }
            if (lvNC < 1) {
                sb.append("|3|Bạn còn thiếu đá nâng cấp\n");
            }
            if (lvMM > 0) {
                sb.append("|2|Đá may mắn cấp ").append(lvMM)
                        .append(" (+").append(bonusMM).append("% tỉ lệ tối đa các chỉ số)\n");
            }

            sb.append("|2|Tỉ lệ thành công: ").append(bonusNC).append("%\n");
            sb.append("|2|Phí nâng cấp: ").append(Util.powerToString(GOLD_CHE_TAO)).append("\n");

            boolean missing = false;
            if (congThuc == null)
                missing = true;
            if (manh == null || have < MANH_THIEN_SU_NEED)
                missing = true;
            if (daNC == null)
                missing = true;
            if (InventoryService.gI().getCountEmptyBag(p) < 1)
                missing = true;
            if (p.inventory.gold < GOLD_CHE_TAO)
                missing = true;

            if (missing) {
                CombineService.gI().whis.createOtherMenu(p, ConstNpc.IGNORE_MENU, sb.toString(), "Đóng");
            } else {
                CombineService.gI().whis.createOtherMenu(
                        p, ConstNpc.MENU_START_COMBINE, sb.toString(),
                        "Nâng cấp", "Từ chối");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(p, "Lỗi hiển thị: " + e.getMessage());
        }
    }

    public static void CheTaoTS(Player p) {
        try {
            // gom item
            Item ctThuong = null, ctVip = null, manh = null, daNC = null, daMM = null;
            for (Item it : p.combineNew.itemsCombine) {
                if (it == null || it.template == null)
                    continue;
                int id = it.template.id;
                if (in(id, CONG_THUC_THUONG_IDS))
                    ctThuong = it;
                else if (in(id, CONG_THUC_VIP_IDS))
                    ctVip = it;
                else if (in(id, MANH_THIEN_SU_IDS))
                    manh = it;
                else if (in(id, DA_NANG_CAP_IDS))
                    daNC = it;
                else if (in(id, DA_MAY_MAN_IDS))
                    daMM = it;
            }

            boolean isVip = ctVip != null;
            Item congThuc = isVip ? ctVip : ctThuong;
            if (congThuc == null || manh == null || daNC == null) {
                Service.gI().sendThongBao(p, "Thiếu vật phẩm bắt buộc!");
                CombineService.gI().reOpenItemCombine(p);
                return;
            }
            if (manh.quantity < MANH_THIEN_SU_NEED) {
                Service.gI().sendThongBao(p, "Thiếu mảnh: cần " + MANH_THIEN_SU_NEED);
                CombineService.gI().reOpenItemCombine(p);
                return;
            }
            if (InventoryService.gI().getCountEmptyBag(p) < 1) {
                Service.gI().sendThongBao(p, "Cần ít nhất 1 ô trống hành trang");
                return;
            }
            if (p.inventory.gold < GOLD_CHE_TAO) {
                Service.gI().sendThongBao(p, "Thiếu " + goldStr(GOLD_CHE_TAO - p.inventory.gold));
                return;
            }

            // trừ vàng trước
            p.inventory.gold -= GOLD_CHE_TAO;
            Service.gI().sendMoney(p);

            // tỉ lệ
            int lvNC = levelOf(daNC.template.id, DA_NANG_CAP_IDS); // 1..5
            int lvMM = daMM == null ? 0 : levelOf(daMM.template.id, DA_MAY_MAN_IDS);
            int success = isVip ? TI_LE_VIP[lvNC] : TI_LE_THUONG[lvNC];
            boolean thanhCong = Util.isTrue(success, 100);

            if (thanhCong) {
                CombineService.gI().sendEffectSuccessCombine(p);

                // gender nguồn từ CT; nếu CT gender>2 thì lấy theo player
                int genderCt = congThuc.template.gender > 2 ? p.gender : congThuc.template.gender;

                // xác định loại trang bị từ mảnh
                int typeIdx = typeIndexFromManh(manh);
                if (genderCt < 0 || genderCt > 2 || typeIdx < 0 || typeIdx > 4) {
                    Service.gI().sendThongBao(p, "Hành tinh/loại mảnh không hợp lệ");
                    return;
                }

                short equipId = switch (typeIdx) {
                    case 0 -> TRANG_BI_THIEN_SU_IDS[genderCt][0]; // Áo
                    case 1 -> TRANG_BI_THIEN_SU_IDS[genderCt][1]; // Quần
                    case 4 -> TRANG_BI_THIEN_SU_IDS[genderCt][2]; // Găng
                    case 2 -> TRANG_BI_THIEN_SU_IDS[genderCt][3]; // Giày
                    case 3 -> TRANG_BI_THIEN_SU_IDS[genderCt][4]; // Nhẫn
                    default -> -1;
                };
                if (equipId == -1) {
                    Service.gI().sendThongBao(p, "Không xác định được trang bị");
                    return;
                }
                Item itemTS = ItemService.gI().doThienSu(equipId, genderCt);
                if (itemTS == null) {
                    Service.gI().sendThongBao(p, "Không thể tạo trang bị");
                    return;
                }

                int bonusChance = lvMM * 10; // 0..50
                int soDong = 0; // Khởi tạo biến đếm số dòng option
                if (Util.isTrue(bonusChance, 100)) {
                    soDong = Util.nextInt(1, 3); // 1-3 dòng bonus
                    itemTS.itemOptions.add(new ItemOption(15, soDong));
                    List<Integer> pool = new ArrayList<>();
                    for (int idOpt : OPTION_BONUS_IDS)
                        pool.add(idOpt);

                    for (int i = 0; i < soDong && !pool.isEmpty(); i++) {
                        int pick = Util.nextInt(0, pool.size() - 1);
                        int optId = pool.remove(pick);
                        int param = Util.nextInt(1, 3); // 1-3%
                        itemTS.itemOptions.add(new ItemOption(optId, param));
                    }
                }

                // add vào túi & trừ vật phẩm tiêu hao
                InventoryService.gI().addItemBag(p, itemTS);
                Service.gI().sendThongBao(p, "Chế tạo thành công! Nhận " + itemTS.template.name);

                // Gửi thông báo toàn server
                String notifyMessage = "Người chơi " + p.name + " đã chế tạo thành công " + itemTS.template.name;
                if (soDong > 0) {
                    notifyMessage += " với " + soDong + " dòng chỉ số thưởng, mọi người đều kinh ngạc.";
                }
                ServerNotify.gI().notify(notifyMessage);

                InventoryService.gI().subQuantityItemsBag(p, manh, MANH_THIEN_SU_NEED);
            } else {
                CombineService.gI().sendEffectFailCombine(p);
                Service.gI().sendThongBao(p, "Chế tạo thất bại!");
                if (manh != null)
                    InventoryService.gI().subQuantityItemsBag(p, manh, MANH_THIEN_SU_FAIL);
            }

            // trừ các nguyên liệu 1 cái
            if (congThuc != null)
                InventoryService.gI().subQuantityItemsBag(p, congThuc, 1);
            if (daNC != null)
                InventoryService.gI().subQuantityItemsBag(p, daNC, 1);
            if (daMM != null)
                InventoryService.gI().subQuantityItemsBag(p, daMM, 1);

            InventoryService.gI().sendItemBags(p);
            Service.gI().sendMoney(p);
            CombineService.gI().reOpenItemCombine(p);

        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(p, "Lỗi chế tạo: " + e.getMessage());
        }
    }

}
