package event.halloween;

import item.Item;
import java.util.Calendar;
import java.util.List;
import map.ItemMap;
import mob.Mob;
import player.Player;
import server.ServerManager;
import utils.Util;

public class HalloweenDrops {

    public static void apply(Player player, Mob mob, List<ItemMap> list, int x, int yEnd) {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

        if (player.lastHalloweenDropDay == dayOfYear) {
            return;
        }
        if (!Util.isTrue(1, 1)) {
            return;
        }

        short itemId = getDailyPumpkinId(dayOfWeek);
        if (itemId == -1) {
            return;
        }

        ItemMap item = createHalloweenItem(mob.zone, itemId, x, yEnd, player.id);
        list.add(item);

        // Đánh dấu đã rơi hôm nay
        player.lastHalloweenDropDay = dayOfYear;
        player.lastHalloweenDropBoot = ServerManager.timeStart; // Lưu để tương thích với code cũ
    }

    private static short getDailyPumpkinId(int dayOfWeek) {
        return switch (dayOfWeek) {
            case Calendar.SUNDAY ->
                702;
            case Calendar.MONDAY ->
                703;
            case Calendar.TUESDAY ->
                704;
            case Calendar.WEDNESDAY ->
                705;
            case Calendar.THURSDAY ->
                706;
            case Calendar.FRIDAY ->
                707;
            case Calendar.SATURDAY ->
                708;
            default ->
                -1;
        };
    }

    private static ItemMap createHalloweenItem(map.Zone zone, short itemId, int x, int yEnd, long playerId) {
        ItemMap item = new ItemMap(zone, itemId, 1, x, yEnd, playerId);
        item.options.add(new Item.ItemOption(87, 0));
        item.options.add(new Item.ItemOption(174, 2025));
        item.options.add(new Item.ItemOption(30, 0));
        return item;
    }
}
