package event.halloween;

import item.Item;
import player.Player;
import services.ItemService;
import services.Service;
import utils.Util;

public class BoMongHalloween {

    private static final short[] PUMPKIN_IDS = new short[]{702, 703, 704, 705, 706, 707, 708};

    private static final int[] RATES_EASY = new int[]{5, 6, 8, 10, 20, 21, 30};
    private static final int[] RATES_HARD = new int[]{8, 9, 10, 13, 20, 20, 20};
    private static final int[] RATES_SUPER_HARD = new int[]{12, 13, 15, 15, 15, 15, 15};

    public static void reward(Player player, byte level) {
        if (player == null) return;
        int[] rates;
        switch (level) {
            case 0: // VERY_HARD (Siêu khó)
                rates = RATES_SUPER_HARD;
                break;
            case 1: // HARD (Khó)
                rates = RATES_HARD;
                break;
            case 2: // EASY (Dễ)
            default:
                rates = RATES_EASY;
                break;
        }
        short itemId = rollItemByRates(rates);
        if (itemId <= 0) return;

        Item item = ItemService.gI().createNewItem(itemId, 1);
        if (item != null) {
            if (item.itemOptions != null) item.itemOptions.clear();
            else item.itemOptions = new java.util.ArrayList<>();
            item.itemOptions.add(new Item.ItemOption(87, 0));
            item.itemOptions.add(new Item.ItemOption(174, 2025));
            item.itemOptions.add(new Item.ItemOption(30, 0));
            services.player.InventoryService.gI().addItemBag(player, item);
            services.player.InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn nhận được " + item.getName());
        }
    }

    private static short rollItemByRates(int[] rates) {
        int roll = Util.nextInt(1, 100);
        int acc = 0;
        for (int i = 0; i < rates.length && i < PUMPKIN_IDS.length; i++) {
            acc += rates[i];
            if (roll <= acc) {
                return PUMPKIN_IDS[i];
            }
        }
        // Fallback last item if rounding issues
        return PUMPKIN_IDS[PUMPKIN_IDS.length - 1];
    }
}

