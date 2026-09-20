package npc.list;
import consts.ConstNpc;
import item.Item;
import java.util.List;
import npc.Npc;
import player.Player;
import services.Service;
import services.ShopService;
import services.player.InventoryService;

public class ChiChi extends Npc {

    private static final int MENU_HUY_TRANG_BI_HSD = 9001;

    public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Bạn muốn hỏi chi?", 
                "Cửa hàng", 
                "Hủy bỏ\ntrang bị có HSD", 
                "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            switch (player.idMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                            ShopService.gI().opendShop(player, "THO_CHI_CHI", false);
                        }
                        case 1 -> {
                            showItemsWithHSD(player);
                        }
                    }
                }
                case MENU_HUY_TRANG_BI_HSD -> {
                    if (select == 0) {
                        removeItemsWithHSD(player);
                    }
                }
            }
        }
    }

    private void showItemsWithHSD(Player player) {
        List<Item> itemsHsd = player.inventory.itemsBag.stream()
                .filter(i -> i != null && i.isNotNullItem()
                        && i.itemOptions.stream().anyMatch(io -> io.optionTemplate.id == 93))
                .limit(7)
                .toList();

        if (itemsHsd.isEmpty()) {
            Service.gI().sendThongBao(player, "Bạn không có vật phẩm nào có hạn sử dụng.");
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Bạn có chắc muốn hủy bỏ ").append(itemsHsd.size())
                .append(" vật phẩm sau:\n");

        for (Item item : itemsHsd) {
            int hsd = getHSDDays(item);
            message.append(item.template.name).append(" (HSD: ").append(hsd).append(" ngày)\n");
        }

        this.createOtherMenu(player, MENU_HUY_TRANG_BI_HSD, message.toString(), "Đồng ý", "Hủy");
    }

    private void removeItemsWithHSD(Player player) {
        List<Item> itemsHsd = player.inventory.itemsBag.stream()
                .filter(i -> i != null && i.isNotNullItem()
                        && i.itemOptions.stream().anyMatch(io -> io.optionTemplate.id == 93))
                .limit(7)
                .toList();

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

    private int getHSDDays(Item item) {
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
