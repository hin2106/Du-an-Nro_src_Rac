package event;

import item.Item;
import java.util.List;
import npc.Npc;
import player.Player;
import services.Service;
import services.player.InventoryService;


public class EventNpcUtils {

    public static final int MENU_HUY_TRANG_BI_HSD = 9001;


    public static void showItemsWithHSD(Player player, Npc npc) {
        List<Item> itemsHsd = getItemsWithHSD(player);

        if (itemsHsd.isEmpty()) {
            Service.gI().sendThongBao(player, "Bạn không có vật phẩm nào có hạn sử dụng.");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Bạn có chắc muốn hủy bỏ ").append(itemsHsd.size())
                .append(" vật phẩm sau:\n");

        for (Item item : itemsHsd) {
            int hsd = getHSDDays(item);
            message.append(item.template.name).append(" (hsd: ").append(hsd).append(" ngày)\n");
        }

        npc.createOtherMenu(player, MENU_HUY_TRANG_BI_HSD, message.toString(), "Đồng ý", "Hủy");
    }


    public static void removeItemsWithHSD(Player player) {
        List<Item> itemsHsd = getItemsWithHSD(player);

        if (itemsHsd.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có vật phẩm nào để hủy.");
            return;
        }

        int count = 0;
        for (Item item : itemsHsd) {
            InventoryService.gI().subQuantityItemsBag(player, item, item.quantity);
            count++;
        }
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Đã hủy bỏ " + count + " vật phẩm có hạn sử dụng!");
    }


    private static List<Item> getItemsWithHSD(Player player) {
        return player.inventory.itemsBag.stream()
                .filter(i -> i != null && i.isNotNullItem()
                        && i.itemOptions.stream().anyMatch(io -> io.optionTemplate.id == 93))
                .limit(7)
                .toList();
    }


    private static int getHSDDays(Item item) {
        if (item == null || item.itemOptions == null) {
            return 0;
        }

        for (Item.ItemOption option : item.itemOptions) {
            if (option.optionTemplate != null && option.optionTemplate.id == 93) {
                return option.param;
            }
        }
        return 0;
    }
}
