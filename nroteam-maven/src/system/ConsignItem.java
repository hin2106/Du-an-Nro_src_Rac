package system;

import item.Item.ItemOption;
import java.util.ArrayList;
import java.util.List;

public class ConsignItem {

    public final int id;
    public final short itemId;
    public final int player_sell;
    public final String playerName;
    public final byte tab;
    public final int goldSell;
    public final int gemSell;
    public final int quantity;
    public volatile long lasttime;
    public final List<ItemOption> options;
    public volatile boolean isBuy;

    public ConsignItem(int id, short itemId, int player_sell, String playerName, byte tab,
            int goldSell, int gemSell, int quantity, long lasttime,
            List<ItemOption> options, boolean isBuy) {
        if (id < 0 || player_sell < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid ConsignItem parameters");
        }
        if (goldSell < -1 || gemSell < -1) {
            throw new IllegalArgumentException("Price cannot be less than -1");
        }
        if (goldSell > 0 && gemSell > 0) {
            throw new IllegalArgumentException("Cannot have both gold and gem price");
        }

        this.id = id;
        this.itemId = itemId;
        this.player_sell = player_sell;
        this.playerName = playerName;
        this.tab = tab;
        this.goldSell = goldSell;
        this.gemSell = gemSell;
        this.quantity = quantity;
        this.lasttime = lasttime;
        this.options = (options != null ? new ArrayList<>(options) : new ArrayList<>());
        this.isBuy = isBuy;
    }

    public synchronized boolean markAsSold() {
        if (isBuy) {
            return false;
        }
        isBuy = true;
        return true;
    }

    public synchronized void updateLastTime() {
        this.lasttime = System.currentTimeMillis();
    }
}
