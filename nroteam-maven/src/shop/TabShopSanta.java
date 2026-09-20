package shop;

import player.Player;
import java.util.ArrayList;
import java.util.List;

import item.Item;
import services.ItemService;
import services.player.InventoryService;

public class TabShopSanta extends TabShop {

    private final int[] listDauThan = {293, 294, 295, 296, 297, 298, 299, 596, 597, 598};
    
    // DANH SÁCH VẬT PHẨM ĐƯỢC THÊM TỪ BÊN NGOÀI
    private static final List<SantaItemConfig> additionalItems = new ArrayList<>();

    // CLASS LƯU CẤU HÌNH VẬT PHẨM
    public static class SantaItemConfig {
        public int itemId;
        public byte typeSell;
        public int cost;
        public int[][] options;
        public boolean isNew;

        public SantaItemConfig(int itemId, byte typeSell, int cost, int[][] options, boolean isNew) {
            this.itemId = itemId;
            this.typeSell = typeSell;
            this.cost = cost;
            this.options = options;
            this.isNew = isNew;
        }
    }

    public TabShopSanta(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        int dauCanBuyId = idDauCanBuy(player);
        boolean hasBongTai = InventoryService.gI().findItemBongTai(player);

        // THÊM VẬT PHẨM TỪ DANH SÁCH ADDITIONAL ITEMS TRƯỚC (LỌC THEO GENDER)
        addAdditionalSantaItems(player);

        // SAU ĐÓ THÊM CÁC VẬT PHẨM KHÁC
        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == player.gender || itemShop.temp.gender == 3) {
                boolean isInListDauThan = false;
                for (int id : listDauThan) {
                    if (itemShop.temp.id == id) {
                        isInListDauThan = true;
                        break;
                    }
                }
                if (!isInListDauThan || itemShop.temp.id == dauCanBuyId) {
                    itemShop.tabShop = this;
                    this.itemShops.add(new ItemShop(itemShop));
                }
            }
        }
    }

    /**
     * PHƯƠNG THỨC QUAN TRỌNG: Class khác gọi để thêm vật phẩm vào shop Santa
     */
    public static void addSantaItem(int itemId, byte typeSell, int cost, int[][] options, boolean isNew) {
        additionalItems.add(new SantaItemConfig(itemId, typeSell, cost, options, isNew));
    }

    /**
     * Thêm vật phẩm từ danh sách additionalItems, lọc theo gender của player
     */
    private void addAdditionalSantaItems(Player player) {
        for (SantaItemConfig config : additionalItems) {
            if (!containsItem(config.itemId)) {
                ItemShop itemShop = createSantaItem(config.itemId, config.typeSell, config.cost, config.options, config.isNew);
                // Lọc gender: chỉ hiển thị item cùng gender hoặc gender 3 (all)
                if (itemShop.temp.gender == player.gender || itemShop.temp.gender == 3) {
                    this.itemShops.add(0, itemShop);
                }
            }
        }
    }

    /**
     * Tạo vật phẩm cho shop Santa
     */
    private ItemShop createSantaItem(int itemId, byte typeSell, int cost, int[][] options, boolean isNew) {
        ItemShop itemShop = new ItemShop();
        itemShop.temp = ItemService.gI().getTemplate((short) itemId);
        itemShop.tabShop = this;
        itemShop.typeSell = typeSell;
        itemShop.cost = cost;
        itemShop.options = new ArrayList<>();
        itemShop.isNew = isNew;
        
        for (int[] option : options) {
            itemShop.options.add(new Item.ItemOption(option[0], option[1]));
        }
        
        return itemShop;
    }

    /**
     * Kiểm tra xem vật phẩm đã tồn tại trong tab chưa
     */
    private boolean containsItem(int itemId) {
        for (ItemShop itemShop : this.itemShops) {
            if (itemShop.temp.id == itemId) {
                return true;
            }
        }
        return false;
    }

    public int idDauCanBuy(Player player) {
        int level = player.magicTree.level;
        if (level == 10) {
            return listDauThan[9];
        } else if (level >= 1 && level <= 9) {
            return listDauThan[level];
        }
        throw new IllegalArgumentException("Invalid magic tree level: " + level);
    }

    public static void replaceItems(List<SantaItemConfig> items) {
        additionalItems.clear();
        additionalItems.addAll(items);
    }
}