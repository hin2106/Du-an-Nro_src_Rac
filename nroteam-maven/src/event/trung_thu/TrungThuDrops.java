package event.trung_thu;

import java.util.Calendar;
import java.util.List;
import item.Item;
import map.ItemMap;
import mob.Mob;
import player.Player;
import server.ServerManager;
import utils.Util;

public class TrungThuDrops {

    public static void apply(Player player, Mob mob, List<ItemMap> list, int x, int yEnd) {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        if (player.lastTrungThuDropDay == dayOfYear) {
            return;
        }

        if (!Util.isTrue(1, 1)) {
            return;
        }

        // Gán itemID theo thứ trong tuần
        short itemId = -1;
        switch (dayOfWeek) {
            case Calendar.SUNDAY: // Chủ nhật
                itemId = 702;
                break;
            case Calendar.MONDAY: // Thứ hai
                itemId = 703;
                break;
            case Calendar.TUESDAY: // Thứ ba
                itemId = 704;
                break;
            case Calendar.WEDNESDAY: // Thứ tư
                itemId = 705;
                break;
            case Calendar.THURSDAY: // Thứ năm
                itemId = 706;
                break;
            case Calendar.FRIDAY: // Thứ sáu
                itemId = 707;
                break;
            case Calendar.SATURDAY: // Thứ bảy
                itemId = 708;
                break;
        }

        if (itemId == -1)
            return;

        // Tạo vật phẩm rơi
        ItemMap item = new ItemMap(mob.zone, itemId, 1, x, yEnd, player.id);

        // Thêm option mặc định
        item.options.add(new Item.ItemOption(87, 0));
        item.options.add(new Item.ItemOption(174, 2025));
        item.options.add(new Item.ItemOption(30, 0));

        // Thêm vào danh sách rơi
        list.add(item);

        // Đánh dấu đã rơi hôm nay
        player.lastTrungThuDropDay = dayOfYear;
        player.lastTrungThuDropBoot = ServerManager.timeStart; // Lưu để tương thích với code cũ
    }
}
