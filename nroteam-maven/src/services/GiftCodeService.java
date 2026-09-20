package services;
import item.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import managers.GiftCodeManager;
import player.Player;
import services.player.InventoryService;
import system.GiftCode;
import data.AlyraManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class GiftCodeService {

    private static GiftCodeService instance;
    private static final int MAX_LINES = 6;

    public static GiftCodeService gI() {
        if (instance == null) {
            instance = new GiftCodeService();
        }
        return instance;
    }

    public void giftCode(Player player, String code) {
        RedeemResult result = redeem(player, code, true);
        if (!result.success) Service.gI().sendThongBao(player, result.message);
    }

    public RedeemResult redeemFromEvent(Player player, String code) {
        return redeem(player, code, false);
    }

    private RedeemResult redeem(Player player, String code, boolean legacyNotifications) {
        String normalizedCode = code == null ? "" : code.trim();
        if (normalizedCode.isEmpty()) return RedeemResult.failure("Vui lòng nhập Gift Code");

        GiftCode giftcode = GiftCodeManager.gI().checkUseGiftCode(player, normalizedCode, legacyNotifications);
        if (giftcode == null) return RedeemResult.failure("Gift Code không chính xác, đã hết lượt hoặc đã được sử dụng");
        if (giftcode.timeCode()) return RedeemResult.failure("Gift Code đã hết hạn");

        Set<Integer> keySet = giftcode.detail.keySet();
        List<GiftItemInfo> receivedItems = new ArrayList<>();
        List<GiftItemInfo> pendingItems = new ArrayList<>();

        for (Integer key : keySet) {
            int idItem = key;
            int quantity = giftcode.detail.get(key);
            switch (idItem) {
                case -1 -> {
                    player.inventory.gold = Math.min(player.inventory.gold + (long) quantity, 2000000000L);
                    receivedItems.add(new GiftItemInfo(-1, quantity, "vàng", 2));
                }
                case -2 -> {
                    player.inventory.gem = Math.min(player.inventory.gem + quantity, 200000000);
                    receivedItems.add(new GiftItemInfo(-2, quantity, "ngọc", 3));
                }
                case -3 -> {
                    player.inventory.ruby = Math.min(player.inventory.ruby + quantity, 200000000);
                    receivedItems.add(new GiftItemInfo(-3, quantity, "ngọc khóa", 4));
                }
                default -> {
                    Item itemGiftTemplate = ItemService.gI().createNewItem((short) idItem);
                    if (itemGiftTemplate == null) continue;
                    Item itemGift = new Item((short) idItem);
                    if (itemGift.template.id == 457) {
                        itemGift.itemOptions.add(new Item.ItemOption(30, 0));
                    } else {
                        List<Item.ItemOption> configuredOptions = giftcode.option.get(key);
                        itemGift.itemOptions = configuredOptions == null
                                ? new ArrayList<>() : new ArrayList<>(configuredOptions);
                    }
                    int remainingQuantity = quantity;
                    int addedQuantity = 0;
                    while (remainingQuantity > 0) {
                        if (InventoryService.gI().getCountEmptyBag(player) <= 0) break;
                        Item itemToAdd = new Item((short) idItem);
                        itemToAdd.itemOptions = new ArrayList<>(itemGift.itemOptions);
                        itemToAdd.quantity = 1;
                        if (!InventoryService.gI().addItemBag(player, itemToAdd)) break;
                        addedQuantity++;
                        remainingQuantity--;
                    }
                    if (addedQuantity > 0) {
                        receivedItems.add(new GiftItemInfo(idItem, addedQuantity, itemGift.template.name, 1,
                                itemGift.itemOptions));
                    }
                    if (remainingQuantity > 0) {
                        pendingItems.add(new GiftItemInfo(idItem, remainingQuantity, itemGift.template.name, 1,
                                itemGift.itemOptions));
                    }
                }
            }
        }

        if (!pendingItems.isEmpty()) {
            savePendingItems(player, pendingItems);
            if (legacyNotifications)
                Service.gI().sendThongBao(player, "Hành trang của bạn đã đầy, vui lòng đăng nhập lại");
        }
        if (legacyNotifications) sendGiftNotifications(player, receivedItems);
        InventoryService.gI().sendItemBags(player);
        String message = pendingItems.isEmpty() ? "Đổi Gift Code thành công"
                : "Đổi thành công. Một số vật phẩm đang chờ vì hành trang đầy";
        return RedeemResult.success(message, receivedItems);
    }

    private void sendGiftNotifications(Player player, List<GiftItemInfo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<String> queue = new ArrayList<>();
        queue.add("|0|Bạn vừa nhận được:");

        for (GiftItemInfo item : items) {
            String line = switch (item.type) {
                case 1 ->
                    "|1|x" + item.quantity + " " + item.name;
                case 2 ->
                    "|2|x" + item.quantity + " " + item.name;
                case 3 ->
                    "|3|x" + item.quantity + " " + item.name;
                case 4 ->
                    "|4|x" + item.quantity + " " + item.name;
                default ->
                    "|0|x" + item.quantity + " " + item.name;
            };
            queue.add(line);
            // Giới hạn 5 dòng cuối
            if (queue.size() > MAX_LINES) {
                queue.remove(1);
            }
            StringBuilder builder = new StringBuilder();
            for (String s : queue) {
                builder.append(s).append("\b");
            }

            Service.gI().sendThongBao(player, builder.toString());

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Lưu pending items vào database
     */
    @SuppressWarnings("unchecked")
    private void savePendingItems(Player player, List<GiftItemInfo> pendingItems) {
        try {
            // Đảm bảo cột tồn tại
            try {
                AlyraManager.executeQuery("SELECT pending_gift_items FROM player WHERE id = ? LIMIT 1", player.id);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Unknown column")) {
                    createPendingGiftItemsColumn();
                }
            }
            JSONArray dataArray = new JSONArray();
            for (GiftItemInfo item : pendingItems) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("id", item.idItem);
                itemObj.put("quantity", item.quantity);
                itemObj.put("name", item.name);

                // Lưu options nếu có
                if (item.options != null && !item.options.isEmpty()) {
                    JSONArray optionsArray = new JSONArray();
                    for (Item.ItemOption opt : item.options) {
                        JSONObject optObj = new JSONObject();
                        optObj.put("id", opt.optionTemplate.id);
                        optObj.put("param", opt.param);
                        optionsArray.add(optObj);
                    }
                    itemObj.put("options", optionsArray);
                }

                dataArray.add(itemObj);
            }

            String jsonData = dataArray.toJSONString();
            AlyraManager.executeUpdate("UPDATE player SET pending_gift_items = ? WHERE id = ?", jsonData, player.id);
        } catch (Exception e) {
        }
    }

    /**
     * Load pending items từ database
     */
    private List<GiftItemInfo> loadPendingItems(Player player) {
        List<GiftItemInfo> pendingItems = new ArrayList<>();
        try {
            String jsonData = null;
            var rs = AlyraManager.executeQuery("SELECT pending_gift_items FROM player WHERE id = ?", player.id);
            if (rs != null && rs.next()) {
                jsonData = rs.getString("pending_gift_items");
            }

            if (jsonData == null || jsonData.isBlank() || "null".equalsIgnoreCase(jsonData.trim())) {
                return pendingItems;
            }

            JSONArray dataArray = (JSONArray) JSONValue.parse(jsonData);
            if (dataArray == null) {
                return pendingItems;
            }

            for (Object obj : dataArray) {
                JSONObject itemObj = (JSONObject) obj;
                int idItem = Integer.parseInt(itemObj.get("id").toString());
                int quantity = Integer.parseInt(itemObj.get("quantity").toString());
                String name = itemObj.get("name") != null ? itemObj.get("name").toString() : "";

                List<Item.ItemOption> options = new ArrayList<>();
                JSONArray optionsArray = (JSONArray) itemObj.get("options");
                if (optionsArray != null) {
                    for (Object optObj : optionsArray) {
                        JSONObject opt = (JSONObject) optObj;
                        int optId = Integer.parseInt(opt.get("id").toString());
                        int param = Integer.parseInt(opt.get("param").toString());
                        options.add(new Item.ItemOption(optId, param));
                    }
                }

                pendingItems.add(new GiftItemInfo(idItem, quantity, name, 1, options));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pendingItems;
    }

    private void createPendingGiftItemsColumn() {
        try {
            AlyraManager.executeUpdate("ALTER TABLE player ADD COLUMN pending_gift_items TEXT NULL");
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unchecked")
    public void checkAndGivePendingItems(Player player) {
        try {
            List<GiftItemInfo> pendingItems = loadPendingItems(player);
            if (pendingItems.isEmpty()) {
                return;
            }
            List<GiftItemInfo> receivedItems = new ArrayList<>();
            List<GiftItemInfo> stillPending = new ArrayList<>();

            for (GiftItemInfo item : pendingItems) {
                int emptySlots = InventoryService.gI().getCountEmptyBag(player);
                if (emptySlots <= 0) {
                    stillPending.add(item);
                    continue;
                }

                int itemsToAdd = Math.min(item.quantity, emptySlots);
                Item itemToAdd = new Item((short) item.idItem);
                itemToAdd.itemOptions = item.options != null ? new ArrayList<>(item.options) : new ArrayList<>();
                itemToAdd.quantity = itemsToAdd;

                if (InventoryService.gI().addItemBag(player, itemToAdd)) {
                    receivedItems.add(new GiftItemInfo(item.idItem, itemsToAdd, item.name, 1, item.options));

                    if (itemsToAdd < item.quantity) {
                        stillPending.add(
                                new GiftItemInfo(item.idItem, item.quantity - itemsToAdd, item.name, 1, item.options));
                    }
                } else {
                    stillPending.add(item);
                }
            }
            if (stillPending.isEmpty()) {
                AlyraManager.executeUpdate("UPDATE player SET pending_gift_items = NULL WHERE id = ?", player.id);
            } else {
                List<GiftItemInfo> allPending = loadPendingItems(player);
                allPending.removeIf(item -> {
                    for (GiftItemInfo received : receivedItems) {
                        if (received.idItem == item.idItem && received.quantity >= item.quantity) {
                            return true;
                        }
                    }
                    return false;
                });
                allPending.addAll(stillPending);

                JSONArray dataArray = new JSONArray();
                for (GiftItemInfo item : allPending) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("id", item.idItem);
                    itemObj.put("quantity", item.quantity);
                    itemObj.put("name", item.name);

                    if (item.options != null && !item.options.isEmpty()) {
                        JSONArray optionsArray = new JSONArray();
                        for (Item.ItemOption opt : item.options) {
                            JSONObject optObj = new JSONObject();
                            optObj.put("id", opt.optionTemplate.id);
                            optObj.put("param", opt.param);
                            optionsArray.add(optObj);
                        }
                        itemObj.put("options", optionsArray);
                    }

                    dataArray.add(itemObj);
                }

                String jsonData = dataArray.toJSONString();
                AlyraManager.executeUpdate("UPDATE player SET pending_gift_items = ? WHERE id = ?", jsonData,
                        player.id);
            }
            if (!receivedItems.isEmpty()) {
                InventoryService.gI().sendItemBags(player);
            }
        } catch (Exception e) {
        }
    }

    public static final class RedeemResult {
        public final boolean success;
        public final String message;
        public final List<GiftItemInfo> rewards;

        private RedeemResult(boolean success, String message, List<GiftItemInfo> rewards) {
            this.success = success;
            this.message = message;
            this.rewards = rewards == null ? new ArrayList<>() : rewards;
        }

        public static RedeemResult success(String message, List<GiftItemInfo> rewards) {
            return new RedeemResult(true, message, rewards);
        }

        public static RedeemResult failure(String message) {
            return new RedeemResult(false, message, new ArrayList<>());
        }
    }

    public static class GiftItemInfo {

        public int idItem;
        public int quantity;
        public String name;
        public int type; // 1: item, 2: gold, 3: gem, 4: ruby
        public List<Item.ItemOption> options;

        GiftItemInfo(int idItem, int quantity, String name, int type) {
            this(idItem, quantity, name, type, null);
        }

        GiftItemInfo(int idItem, int quantity, String name, int type, List<Item.ItemOption> options) {
            this.idItem = idItem;
            this.quantity = quantity;
            this.name = name;
            this.type = type;
            this.options = options;
        }
    }

}
