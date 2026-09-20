package item;
import player.NPoint;
import player.Player;
import services.ItemTimeService;
import services.Service;
import utils.Util;

public class ItemTime {

    public static final byte DOANH_TRAI = 0;
    public static final byte BAN_DO_KHO_BAU = 1;
    public static final byte CON_DUONG_RAN_DOC = 2;
    public static final byte KHI_GAS_HUY_DIET = 3;
    public static final byte TEXT_NHIEM_VU_HANG_NGAY = 4;
    public static final byte TEXT_NHAN_BUA_MIEN_PHI = 5;
    public static final byte KEO_BUA_BAO = 6;
    public static final byte BOSS_CHALLENGE = 7;

    public static final int TIME_ITEM = 600000;
    public static final int TIME_OPEN_POWER = 8640000;
    public static final int TIME_MAY_DO = 1800000;
    public static final int TIME_MAY_DO2 = 1800000;

    public static final int TIME_BUA_SANTA = 1800000;
    public static final int TIME_CO_4 = 1800000;
    public static final int TIME_EAT_MEAL = 600000;
    public static final int TIME_CMS = 3600000;
    public static final int TIME_DK = 1800000;
    public static final int TIME_NCD = 1800000;
    public static final int TIME_BANH_TET = 60 * 60 * 1000;
    public static final int TIME_TRUNG_THU = 150 * 60 * 1000;
    public static final int TIME_SHENRON_EVENT = 30 * 60 * 1000;

    public static final int TIEM_NANG_LUONG = 600_000_000;
    private Player player;

    public boolean isUseBoHuyet;
    public boolean isUseBoKhi;
    public boolean isUseGiapXen;
    public boolean isUseCuongNo;
    public boolean isUseBanhChung;
    public boolean isUseBanhTet;
    public boolean isUseAnDanh;
    public boolean isUseBoHuyet2;
    public boolean isUseBoKhi2;
    public boolean isUseGiapXen2;
    public boolean isUseCuongNo2;
    public boolean isUseAnDanh2;

    public long lastTimeBoHuyet;
    public long lastTimeBoKhi;
    public long lastTimeGiapXen;
    public long lastTimeCuongNo;
    public long lastTimeBanhChung;
    public long lastTimeBanhTet;
    public long lastTimeAnDanh;

    public long lastTimeBoHuyet2;
    public long lastTimeBoKhi2;
    public long lastTimeGiapXen2;
    public long lastTimeCuongNo2;
    public long lastTimeAnDanh2;

    public boolean isUseMayDo;
    public long lastTimeUseMayDo;
    public boolean isUseKhoBauX2;
    public long lastTimeUseKhoBauX2;
    public boolean isUseBuaSanta;
    public long lastTimeBuaSanta;

    public boolean isOpenPower;
    public long lastTimeOpenPower;

    public boolean isUseTDLT;
    public long lastTimeUseTDLT;
    public int timeTDLT;

    public boolean isUseRX;
    public long lastTimeUseRX;
    public int timeRX;

    public boolean isUseCMS;
    public long lastTimeUseCMS;

    public boolean isUseNCD;
    public long lastTimeUseNCD;

    public boolean isUseGTPT;
    public long lastTimeUseGTPT;

    public boolean isUseDK;
    public long lastTimeUseDK;

    public boolean isEatMeal;
    public long lastTimeEatMeal;
    public int iconMeal;

    public boolean isEatMeal2;
    public long lastTimeEatMeal2;
    public int iconMeal2;
    public long lastTimeKhauTrang;
    public boolean isUseKhauTrang;

    public boolean isUseNangLuong;
    public long lastTimeNangLuong;

    public boolean isUseCoBonLa;
    public long lastTimeCoBonLa;

    public boolean isUseCarrot;
    public long lastTimeCarrot;
    public int timeCarrot;

    public boolean isHallowen;
    public long lastTimeHallowen;
    public long timeHallowen;
    public int idOutfitHalloween;

    public boolean isUseBanhTrungThu;
    public boolean isUseBanhTrungThuDacBiet;
    public boolean isUseBanhTrungThu2Trung;
    public boolean isUseBanhTrungThu1Trung;
    public boolean isUseBanhDeoThoXanh;
    public boolean isUseBanhDeoThoTrang;
    public boolean isUseBanhDeoThoHong;

    public long lastTimeBanhTrungThu;
    public long lastTimeBanhTrungThuDacBiet;
    public long lastTimeBanhTrungThu2Trung;
    public long lastTimeBanhTrungThu1Trung;
    public long lastTimeBanhDeoThoXanh;
    public long lastTimeBanhDeoThoTrang;
    public long lastTimeBanhDeoThoHong;

    public ItemTime(Player player) {
        this.player = player;
    }

    public void update() {
        if (isEatMeal) {
            if (Util.canDoWithTime(lastTimeEatMeal, TIME_EAT_MEAL)) {
                isEatMeal = false;
                Service.gI().point(player);
            }
        }
        if (isEatMeal2) {
            if (Util.canDoWithTime(lastTimeEatMeal2, TIME_EAT_MEAL)) {
                isEatMeal2 = false;
                Service.gI().point(player);
            }
        }
        if (isUseBoHuyet) {
            if (Util.canDoWithTime(lastTimeBoHuyet, TIME_ITEM)) {
                isUseBoHuyet = false;
                Service.gI().point(player);
            }
        }

        if (isUseBoKhi) {
            if (Util.canDoWithTime(lastTimeBoKhi, TIME_ITEM)) {
                isUseBoKhi = false;
                Service.gI().point(player);
            }
        }

        if (isUseGiapXen) {
            if (Util.canDoWithTime(lastTimeGiapXen, TIME_ITEM)) {
                isUseGiapXen = false;
            }
        }
        if (isUseCuongNo) {
            if (Util.canDoWithTime(lastTimeCuongNo, TIME_ITEM)) {
                isUseCuongNo = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhChung) {
            if (Util.canDoWithTime(lastTimeBanhChung, TIME_BANH_TET)) {
                isUseBanhChung = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhTet) {
            if (Util.canDoWithTime(lastTimeBanhTet, TIME_BANH_TET)) {
                isUseBanhTet = false;
                Service.gI().point(player);
            }
        }
        if (isUseAnDanh) {
            if (Util.canDoWithTime(lastTimeAnDanh, TIME_ITEM)) {
                isUseAnDanh = false;
            }
        }

        if (isUseBoHuyet2) {
            if (Util.canDoWithTime(lastTimeBoHuyet2, TIME_ITEM)) {
                isUseBoHuyet2 = false;
                Service.gI().point(player);
            }
        }

        if (isUseBoKhi2) {
            if (Util.canDoWithTime(lastTimeBoKhi2, TIME_ITEM)) {
                isUseBoKhi2 = false;
                Service.gI().point(player);
            }
        }
        if (isUseGiapXen2) {
            if (Util.canDoWithTime(lastTimeGiapXen2, TIME_ITEM)) {
                isUseGiapXen2 = false;
            }
        }
        if (isUseCuongNo2) {
            if (Util.canDoWithTime(lastTimeCuongNo2, TIME_ITEM)) {
                isUseCuongNo2 = false;
                Service.gI().point(player);
            }
        }
        if (isUseAnDanh2) {
            if (Util.canDoWithTime(lastTimeAnDanh2, TIME_ITEM)) {
                isUseAnDanh2 = false;
            }
        }
        if (isUseCMS) {
            if (Util.canDoWithTime(lastTimeUseCMS, TIME_CMS)) {
                isUseCMS = false;
            }
        }
        if (isUseGTPT) {
            if (Util.canDoWithTime(lastTimeUseGTPT, TIME_ITEM)) {
                isUseGTPT = false;
            }
        }
        if (isUseDK) {
            if (Util.canDoWithTime(lastTimeUseDK, TIME_DK)) {
                isUseDK = false;
            }
        }
        if (isOpenPower) {
            if (Util.canDoWithTime(lastTimeOpenPower, TIME_OPEN_POWER)) {
                player.nPoint.limitPower++;
                if (player.nPoint.limitPower > NPoint.MAX_LIMIT) {
                    player.nPoint.limitPower = NPoint.MAX_LIMIT;
                }
                Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được tăng lên "
                        + Util.numberToMoney(player.nPoint.getPowerNextLimit()));
                isOpenPower = false;
            }
        }
        if (isUseMayDo) {
            if (Util.canDoWithTime(lastTimeUseMayDo, TIME_MAY_DO)) {
                isUseMayDo = false;
            }
        }
        if (isUseBuaSanta) {
            if (Util.canDoWithTime(lastTimeBuaSanta, TIME_BUA_SANTA)) {
                isUseBuaSanta = false;
            }
        }
        if (isUseKhoBauX2) {
            if (Util.canDoWithTime(lastTimeUseKhoBauX2, TIME_MAY_DO2)) {
                isUseKhoBauX2 = false;
            }
        }
        if (isUseTDLT) {
            if (Util.canDoWithTime(lastTimeUseTDLT, timeTDLT)) {
                this.isUseTDLT = false;
                ItemTimeService.gI().sendCanAutoPlay(this.player);
            }
        }
        if (isUseRX) {
            if (Util.canDoWithTime(lastTimeUseRX, timeRX)) {
                isUseRX = false;
            }
        }
        if (isUseNangLuong) {
            if (Util.canDoWithTime(lastTimeNangLuong, TIME_ITEM)) {
                isUseNangLuong = false;
                Service.gI().point(player);
            }
        }
        if (isUseCoBonLa) {
            if (Util.canDoWithTime(lastTimeCoBonLa, TIME_CO_4)) {
                isUseCoBonLa = false;
            }
        }
        if (isUseKhauTrang) {
            if (Util.canDoWithTime(lastTimeKhauTrang, TIME_ITEM)) {
                isUseKhauTrang = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhTrungThu) {
            if (Util.canDoWithTime(lastTimeBanhTrungThu, TIME_TRUNG_THU)) {
                isUseBanhTrungThu = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhTrungThuDacBiet) {
            if (Util.canDoWithTime(lastTimeBanhTrungThuDacBiet, TIME_ITEM)) {
                isUseBanhTrungThuDacBiet = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhTrungThu2Trung) {
            if (Util.canDoWithTime(lastTimeBanhTrungThu2Trung, TIME_ITEM)) {
                isUseBanhTrungThu2Trung = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhTrungThu1Trung) {
            if (Util.canDoWithTime(lastTimeBanhTrungThu1Trung, TIME_ITEM)) {
                isUseBanhTrungThu1Trung = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhDeoThoXanh) {
            if (Util.canDoWithTime(lastTimeBanhDeoThoXanh, TIME_ITEM)) {
                isUseBanhDeoThoXanh = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhDeoThoTrang) {
            if (Util.canDoWithTime(lastTimeBanhDeoThoTrang, TIME_ITEM)) {
                isUseBanhDeoThoTrang = false;
                Service.gI().point(player);
            }
        }
        if (isUseBanhDeoThoHong) {
            if (Util.canDoWithTime(lastTimeBanhDeoThoHong, TIME_ITEM)) {
                isUseBanhDeoThoHong = false;
                Service.gI().point(player);
            }
        }
    }

    public void dispose() {
        this.player = null;
    }
}
