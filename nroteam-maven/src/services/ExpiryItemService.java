package services;

import item.Item;
import java.util.List;
import player.Player;
import services.player.InventoryService;

public class ExpiryItemService {

    private static final ExpiryItemService INSTANCE = new ExpiryItemService();

    public static ExpiryItemService gI() {
        return INSTANCE;
    }

    public int collectExpiryItems(Player player, int maxDays, List<String> infoLines) {
        if (player == null || player.inventory == null) {
            return 0;
        }
        return processItems(player.inventory.itemsBody, maxDays, infoLines, false, player, false)
                + processItems(player.inventory.itemsBag, maxDays, infoLines, false, player, false);
    }

    public int removeExpiryOptions(Player player, int maxDays) {
        if (player == null || player.inventory == null) {
            return 0;
        }
        int removed = processItems(player.inventory.itemsBody, maxDays, null, true, player, true)
                + processItems(player.inventory.itemsBag, maxDays, null, true, player, false);
        if (removed > 0) {
            InventoryService.gI().sendItemBody(player);
            InventoryService.gI().sendItemBags(player);
        }
        return removed;
    }

    private int processItems(List<Item> items, int maxDays, List<String> infoLines, boolean remove, Player player, boolean isBody) {
        if (items == null) {
            return 0;
        }
        int count = 0;
        int start = remove && !isBody ? items.size() - 1 : 0;
        int end = remove && !isBody ? -1 : items.size();
        int step = remove && !isBody ? -1 : 1;

        for (int i = start; remove && !isBody ? i > end : i < end; i += step) {
            Item item = items.get(i);
            Item.ItemOption expiry = getExpiryOption(item, maxDays);
            if (expiry != null) {
                if (remove) {
                    if (isBody) {
                        InventoryService.gI().removeItemBody(player, i);
                    } else {
                        InventoryService.gI().removeItemBag(player, i);
                    }
                } else if (infoLines != null) {
                    infoLines.add(item.template.name + " (hsd:" + expiry.param + ")");
                }
                count++;
            }
        }
        return count;
    }

    private Item.ItemOption getExpiryOption(Item item, int maxDays) {
        if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
            return null;
        }
        for (Item.ItemOption opt : item.itemOptions) {
            if (opt != null && opt.optionTemplate != null
                    && (opt.optionTemplate.id == 93 || opt.optionTemplate.id == 188)
                    && opt.param < maxDays) {
                return opt;
            }
        }
        return null;
    }
}
