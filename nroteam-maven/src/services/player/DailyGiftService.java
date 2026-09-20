package services.player;
import daos.DailyGiftDAO;
import player.Player;

public class DailyGiftService {

    public static final byte NHAN_NGOC_MIEN_PHI = 0;
    public static final byte NHAN_BUA_MIEN_PHI = 1;

    public static boolean checkDailyGift(Player player, byte id) {
        for (DailyGiftDAO data : player.dailyGiftDao) {
            if (data.id == id && !data.daNhan) {
                return true;
            }
        }
        return false;
    }

    public static void updateDailyGift(Player player, byte id) {
        for (DailyGiftDAO data : player.dailyGiftDao) {
            if (data.id == id && !data.daNhan) {
                data.daNhan = true;
                break;
            }
        }
    }

    public static void addAndReset(Player player) {
        if (player.dailyGiftDao != null) {
            player.dailyGiftDao.clear();
        }
        for (byte i = 0; i < 2; i++) {
            DailyGiftDAO data = new DailyGiftDAO();
            data.id = i;
            data.daNhan = false;
            player.dailyGiftDao.add(data);
        }
    }
}
