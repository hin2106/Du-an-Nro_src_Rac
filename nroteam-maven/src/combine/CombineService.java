package combine;

import combine.list.CheTaoTrangBiKichHoat;
import combine.list.CheTaoTrangBiKichHoatVip;
import combine.list.CheTaoTrangBiThienSu;
import combine.list.ChuyenHoaDungNgoc;
import combine.list.ChuyenHoaDungVang;
import consts.ConstNpc;
import item.Item;
import combine.list.EpSaoTrangBi;
import combine.list.LamPhepNhapDa;
import combine.list.NangCapVatPham;
import combine.list.NhapNgocRong;
import combine.list.PhaLeHoaTrangBi;
import combine.list.PhanRaTrangBiKichHoat.PhanRaTrangBi;
import combine.list.TaiTaoCapsuleKichHoat;
import java.io.IOException;
import player.Player;
import network.Message;
import npc.Npc;
import services.map.NpcManager;
import services.player.InventoryService;

public class CombineService {

    public static final byte MAX_STAR_ITEM = 8;
    public static final byte MAX_LEVEL_ITEM = 8;

    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte COMBINE_SUCCESS = 2;
    private static final byte COMBINE_FAIL = 3;
    private static final byte COMBINE_DRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;

    public static final int NANG_CAP_VAT_PHAM = 0;
    public static final int LAM_PHEP_NHAP_DA = 1;
    public static final int NHAP_NGOC_RONG = 2;

    public static final int EP_SAO_TRANG_BI = 3;
    public static final int PHA_LE_HOA_TRANG_BI = 4;
    public static final int NANG_CAP_SAO_PHA_LE = 6;
    public static final int DANH_BONG_SAO_PHA_LE = 7;
    public static final int CUONG_HOA_LO_SAO = 8;
    public static final int TAO_DA_HEMATITE = 9;

    public static final int NANG_CAP_BONG_TAI = 10;
    public static final int NANG_CHI_SO_BONG_TAI = 11;
    public static final int NANG_CAP_BONG_TAI_CAP_3 = 29;
    public static final int NANG_CHI_SO_BONG_TAI_CAP_3 = 30;

    public static final int CHE_TAO_TRANG_BI_KICH_HOAT = 12;
    public static final int CHE_TAO_TRANG_BI_KICH_HOAT_VIP = 13;
    public static final int PHAN_RA_TRANG_BI_KH = 996;
    public static final int TAI_TAO_CAPSULE_KH = 997;
    public static final int NANG_CAP_DO_TS = 14;
    public static final int PHAN_RA_DO_THAN_LINH = 15;

    public static final int GIAM_DINH_SACH = 18;
    public static final int TAY_SACH = 19;
    public static final int NANG_CAP_SACH_TUYET_KY = 20;
    public static final int HOI_PHUC_SACH = 21;
    public static final int PHAN_RA_SACH = 22;

    public static final int CHUYEN_HOA_TRANG_BI_DUNG_VANG = 24;
    public static final int CHUYEN_HOA_TRANG_BI_DUNG_NGOC = 25;

    private static CombineService instance;
    public final Npc baHatMit;
    public final Npc whis;

    private CombineService() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.whis = NpcManager.getNpc(ConstNpc.WHIS);
    }

    public static CombineService gI() {
        if (instance == null) {
            instance = new CombineService();
        }
        return instance;
    }

    public void showInfoCombine(Player player, int[] index) {
        if (player.combineNew == null) {
            return;
        }
        player.combineNew.clearItemCombine();
        if (index.length > 0) {
            for (int i = 0; i < index.length; i++) {
                player.combineNew.itemsCombine.add(player.inventory.itemsBag.get(index[i]));
            }
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI ->
                EpSaoTrangBi.showInfoCombine(player);
            case PHA_LE_HOA_TRANG_BI ->
                PhaLeHoaTrangBi.showInfoCombine(player);
            case CHUYEN_HOA_TRANG_BI_DUNG_VANG ->
                ChuyenHoaDungVang.showInfoCombine(player);
            case CHUYEN_HOA_TRANG_BI_DUNG_NGOC ->
                ChuyenHoaDungNgoc.showInfoCombine(player);
            case NHAP_NGOC_RONG ->
                NhapNgocRong.showInfoCombine(player);
            case NANG_CAP_VAT_PHAM ->
                NangCapVatPham.showInfoCombine(player);
            case NANG_CAP_DO_TS ->
                CheTaoTrangBiThienSu.showInfoCombine(player);
            case PHAN_RA_TRANG_BI_KH ->
                PhanRaTrangBi.showInfoCombine(player);
            case TAI_TAO_CAPSULE_KH ->
                TaiTaoCapsuleKichHoat.showInfoCombine(player);
            case LAM_PHEP_NHAP_DA ->
                LamPhepNhapDa.showInfoCombine(player);
            case CHE_TAO_TRANG_BI_KICH_HOAT ->
                CheTaoTrangBiKichHoat.showInfoCombine(player);
            case CHE_TAO_TRANG_BI_KICH_HOAT_VIP ->
                CheTaoTrangBiKichHoatVip.showInfoCombine(player);
        }
    }

    public void startCombine(Player player, int... n) {
        int num = 0;
        if (n.length > 0) {
            num = n[0];
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI ->
                EpSaoTrangBi.epSaoTrangBi(player);
            case CHUYEN_HOA_TRANG_BI_DUNG_VANG ->
                ChuyenHoaDungVang.ThucHienChuyenHoaVang(player);
            case CHUYEN_HOA_TRANG_BI_DUNG_NGOC ->
                ChuyenHoaDungNgoc.ThucHienChuyenHoaNgoc(player);
            case NHAP_NGOC_RONG ->
                NhapNgocRong.nhapNgocRong(player, num == 1);
            case NANG_CAP_VAT_PHAM ->
                NangCapVatPham.nangCapVatPham(player);
            case NANG_CAP_DO_TS ->
                CheTaoTrangBiThienSu.CheTaoTS(player);
            case PHA_LE_HOA_TRANG_BI ->
                PhaLeHoaTrangBi.phaLeHoa(player, num);
            case LAM_PHEP_NHAP_DA ->
                LamPhepNhapDa.lamphepnhapda(player);
            case CHE_TAO_TRANG_BI_KICH_HOAT ->
                CheTaoTrangBiKichHoat.chetaokh(player);
            case CHE_TAO_TRANG_BI_KICH_HOAT_VIP ->
                CheTaoTrangBiKichHoatVip.chetaokhvip(player);
            case PHAN_RA_TRANG_BI_KH ->
                PhanRaTrangBi.ThucHienPhanRa(player);
            case TAI_TAO_CAPSULE_KH ->
                TaiTaoCapsuleKichHoat.thucHienTaiTao(player);
        }

        player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();

    }

    public void startCombineVip(Player player, int n) {
        switch (player.combineNew.typeCombine) {
            case PHA_LE_HOA_TRANG_BI ->
                PhaLeHoaTrangBi.phaLeHoa(player, n);
        }
        player.idMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        int keepTypeVip = player.combineNew.getTypeCombine();
        player.combineNew.clearParamCombine();
        player.combineNew.setTypeCombine(keepTypeVip);
        player.combineNew.lastTimeCombine = System.currentTimeMillis();

    }

    /**
     * Mở tab đập đồ
     *
     * @param player
     * @param type   kiểu đập đồ
     */
    public void openTabCombine(Player player, int type) {
        player.combineNew.setTypeCombine(type);
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            if (player.iDMark.getNpcChose() != null) {
                msg.writer().writeShort(player.iDMark.getNpcChose().tempId);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng mở item
     *
     * @param player
     * @param icon1
     * @param icon2
     */
    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendEffectCombineItem(Player player, byte type, short icon1, short icon2) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(type);
            switch (type) {
                case 0:
                    msg.writer().writeUTF("");
                    msg.writer().writeUTF("");
                    break;
                case 1:
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(-1);
                    break;
                case 2: // success 0 eff 0
                case 3: // success 1 eff 0
                    break;
                case 4: // success 0 eff 1
                    msg.writer().writeShort(icon1);
                    break;
                case 5: // success 0 eff 2
                    msg.writer().writeShort(icon1);
                    break;
                case 6: // success 0 eff 3
                    msg.writer().writeShort(icon1);
                    msg.writer().writeShort(icon2);
                    break;
                case 7: // success 0 eff 4
                    msg.writer().writeShort(icon1);
                    break;
                case 8: // success 1 eff 4
                    break;
            }
            msg.writer().writeShort(-1); // id npc
            // msg.writer().writeShort(-1); // x
            // msg.writer().writeShort(-1); // y
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng đập đồ thành công
     *
     * @param player
     */
    public void sendEffectSuccessCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_SUCCESS);
            player.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng đập đồ thất bại
     *
     * @param player
     */
    public void sendEffectFailCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_FAIL);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Gửi lại danh sách đồ trong tab combine
     *
     * @param player
     */
    public void reOpenItemCombine(Player player) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combineNew.itemsCombine.size());
            for (Item it : player.combineNew.itemsCombine) {
                for (int j = 0; j < player.inventory.itemsBag.size(); j++) {
                    if (it == player.inventory.itemsBag.get(j)) {
                        msg.writer().writeByte(j);
                    }
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    /**
     * Hiệu ứng ghép ngọc rồng
     *
     * @param player
     * @param icon
     */
    public void sendEffectCombineDB(Player player, short icon) {
        Message msg = null;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendAddItemCombine(Player player, int npcId, Item... items) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Linh DZ");
            msg.writer().writeUTF("Vip Pro Max");
            msg.writer().writeShort(npcId);
            player.sendMessage(msg);
            msg.cleanup();
            msg = new Message(-81);
            msg.writer().writeByte(1);
            msg.writer().writeByte(items.length);
            for (Item item : items) {
                msg.writer().writeByte(InventoryService.gI().getIndexItemBag(player, item));
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffSuccessVip(Player player, int iconID) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(7);
            msg.writer().writeShort(iconID);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffFailVip(Player player) {
        try {
            Message msg;
            msg = new Message(-81);
            msg.writer().writeByte(8);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    /**
     * Đóng UI Combine khi không đủ điều kiện hoặc hủy thao tác
     *
     * @param player Người chơi cần đóng combine
     */
    public void closeCombineUI(Player player) {
        if (player == null || player.combineNew == null) {
            return;
        }

        try {
            Message msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            player.combineNew.clearItemCombine();
            player.combineNew.clearParamCombine();
            player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("[CombineService] Closed combine UI for player: " + player.name);
    }

    private String getTextTopTabCombine(int type) {
        return switch (type) {
            case EP_SAO_TRANG_BI ->
                "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI ->
                "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case CHUYEN_HOA_TRANG_BI_DUNG_VANG, CHUYEN_HOA_TRANG_BI_DUNG_NGOC ->
                "Lưu ý trang bị mới\nphải hơn trang bị gốc\n1 bậc";
            case PHAN_RA_TRANG_BI_KH ->
                "Ta sẽ phù phép\nphân rã thành\nkhoáng tái chế cho ngươi";
            case TAI_TAO_CAPSULE_KH ->
                "Ta sẽ phù phép\ntái tạo thành 1 viên\nCapsule kích hoạt tự chọn";
            case NHAP_NGOC_RONG ->
                "Ta sẽ phù phép\ncho 7 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case NANG_CAP_VAT_PHAM ->
                "Ta sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case PHAN_RA_DO_THAN_LINH ->
                "Ta sẽ phân rã \n  trang bị của người thành điểm!";
            case NANG_CAP_DO_TS ->
                "Ta sẽ nâng cấp \n  trang bị của người thành\n đồ thiên sứ!";
            case NANG_CAP_BONG_TAI ->
                "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành cấp 2";
            case NANG_CHI_SO_BONG_TAI ->
                "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case NANG_CAP_BONG_TAI_CAP_3 ->
                "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\nthành cấp 3";
            case NANG_CHI_SO_BONG_TAI_CAP_3 ->
                "Ta sẽ phù phép\ncho bông tai Porata cấp 3 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case NANG_CAP_SAO_PHA_LE ->
                "Ta sẽ phù phép\nnâng cấp Sao Pha Lê\nthành cấp 2";
            case DANH_BONG_SAO_PHA_LE ->
                "Đánh bóng\nSao pha lê cấp 2";
            case CUONG_HOA_LO_SAO ->
                "Cường hóa\nÔ Sao Pha lê";
            case TAO_DA_HEMATITE ->
                "Ta sẽ phù phép\n tạo đá Hematite";
            case LAM_PHEP_NHAP_DA ->
                "Ta sẽ phù phép\n tạo đá nâng cấp";
            case CHE_TAO_TRANG_BI_KICH_HOAT ->
                "Ta sẽ phù phép\ncho trang bị của ngươi trở thành\ntrang bị kích hoạt";
            case CHE_TAO_TRANG_BI_KICH_HOAT_VIP ->
                "Ta sẽ phù phép\ncho trang bị của ngươi trở thành\ntrang bị kích hoạt VIP";
            case GIAM_DINH_SACH ->
                "Ta sẽ phù phép\ngiám định sách đó cho ngươi";
            case TAY_SACH ->
                "Ta sẽ phù phép\ntẩy sách đó cho ngươi";
            case NANG_CAP_SACH_TUYET_KY ->
                "Ta sẽ phù phép\nnâng cấp Sách Tuyệt Kỹ cho ngươi";
            case HOI_PHUC_SACH ->
                "Ta sẽ phù phép\nphục hồi sách cho ngươi";
            case PHAN_RA_SACH ->
                "Ta sẽ phù phép\nphân rã sách đó cho ngươi";
            default ->
                "";
        };
    }

    private String getTextInfoTabCombine(int type) {
        return switch (type) {
            case EP_SAO_TRANG_BI ->
                "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\nChọn loại sao pha lê\n Sau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI ->
                "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case CHUYEN_HOA_TRANG_BI_DUNG_VANG, CHUYEN_HOA_TRANG_BI_DUNG_NGOC ->
                "Vào hành trang\nChọn trang bị gốc\n(Áo,quần,găng,giày hoặc rađa)\ntừ cấp [+4] trở lên\nChọn tiếp trang bị mới\nchưa nâng cấp cần nhập thể\nsau đó chọn 'Nâng cấp'";
            case NHAP_NGOC_RONG ->
                "Vào hành trang\nChọn 7 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case PHAN_RA_TRANG_BI_KH ->
                "Vào hành trang\nChọn hay nhiều\nTrang bị kích hoạt cần rã\nSau đó chọn 'Phân rã'";
            case TAI_TAO_CAPSULE_KH ->
                "Vào hành trang\nChọn 3 khoáng tái chế\nChọn 1 Capsule vỡ\nSau đó chọn 'Tái tạo'";
            case NANG_CAP_VAT_PHAM ->
                "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để nâng cấp\n Sau đó chọn 'Nâng cấp'";
            case PHAN_RA_DO_THAN_LINH ->
                "vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để phân rã\n Sau đó chọn 'Phân Rã'";
            case NANG_CAP_DO_TS ->
                "vào hành trang\nChọn 1 công thức or công thức Vip\nkèm 1 đá nâng, 1 đá may mắn\n và 999 mảnh thiên sứ\n Ta sẽ cho ra đồ thiên sứ từ 0-15% chỉ số\nSau đó chọn 'Nâng Cấp'";
            case NANG_CAP_BONG_TAI ->
                "Vào hành trang\nChọn bông tai Porata\nChọn mảnh bông tai để nâng cấp, Số lượng 9999 cái \nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI ->
                "Vào hành trang\nChọn bông tai Porata\nChọn mảnh hồn porata số lượng 99\ncái và đá xanh lam để nâng cấp.\nSau đó chọn 'Nâng cấp chỉ số'";
            case NANG_CAP_BONG_TAI_CAP_3 ->
                "Vào hành trang\nChọn bông tai Porata cấp 2\nChọn mảnh bông tai cấp 3 để nâng\ncấp, số lượng 20.000 cái\nSau đó chọn 'Nâng cấp'";
            case NANG_CHI_SO_BONG_TAI_CAP_3 ->
                "Vào hành trang\nChọn bông tai Porata cấp 3\nChọn mảnh hồn porata số lượng 99\ncái và đá xanh lam để nâng cấp.\nSau đó chọn 'Mở chỉ số'";
            case NANG_CAP_SAO_PHA_LE ->
                "Vào hành trang\nChọn đá Hematite\n Chọn loại sao pha lê (cấp 1)\nSau đó chọn 'Nâng cấp'";
            case DANH_BONG_SAO_PHA_LE ->
                "Vào hành trang\nChọn loại sao pha lê cấp 2 có từ 2 viên trở\nlên\nChọn 1 loại đá mài\nSau đó chọn 'Đánh bóng'";
            case CUONG_HOA_LO_SAO ->
                "Vào hành trang\n Chọn trang bị có Ô sao thứ 8 trở lên chưa\n cường hóa\n Chọn đá Hematite\n Chọn dùi đục\n Sau đó chọn 'Cường hóa'";
            case TAO_DA_HEMATITE ->
                "Vào hành trang\n Chọn 5 sao pha lê cấp 2 cùng màu\nChọn 'Tạo đá Hematite'";
            case LAM_PHEP_NHAP_DA ->
                "Vào hành trang\nChọn 10 mảnh đá vụn và 1 bình nước phép\nChọn Nâng Cấp";
            case CHE_TAO_TRANG_BI_KICH_HOAT ->
                "Chọn trang bị hủy diệt tương ứng với hành tinh \nChọn trang bị thần linh ngẫu nhiên kèm\n1 Đá kích hoạt\nSau đó chọn 'Chế tạo'";
            case CHE_TAO_TRANG_BI_KICH_HOAT_VIP ->
                "Chọn trang bị hủy diệt tương ứng với hành tinh \nChọn trang bị thần linh ngẫu nhiên kèm\n10 Đá kích hoạt\nSau đó chọn 'Chế tạo'";
            case GIAM_DINH_SACH ->
                "Vào hành trang chọn\n1 sách cần giám định và bùa giám định";
            case TAY_SACH ->
                "Vào hành trang chọn\n1 sách cần tẩy";
            case NANG_CAP_SACH_TUYET_KY ->
                "Vào hành trang chọn\nSách Tuyệt Kỹ 1 cần nâng cấp và 10 Kìm bấm giấy";
            case HOI_PHUC_SACH ->
                "Vào hành trang chọn\nCác Sách Tuyệt Kỹ cần phục hồi";
            case PHAN_RA_SACH ->
                "Vào hành trang chọn\n1 sách cần phân rã";
            default ->
                "";
        };
    }
}
