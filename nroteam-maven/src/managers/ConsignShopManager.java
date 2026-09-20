package managers;

import data.AlyraManager;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import system.ConsignItem;
import org.json.simple.JSONValue;
import utils.Logger;

public class ConsignShopManager {

    private static ConsignShopManager instance;

    public static ConsignShopManager gI() {
        if (instance == null) {
            synchronized (ConsignShopManager.class) {
                if (instance == null) {
                    instance = new ConsignShopManager();
                }
            }
        }
        return instance;
    }

    public String[] tabName = {"Trang bị", "Phụ kiện", "Hỗ trợ", "Linh tinh", ""};
    public List<ConsignItem> listItem = new CopyOnWriteArrayList<>();

    public synchronized boolean addItem(ConsignItem item) {
        if (item == null) {
            return false;
        }
        for (ConsignItem existing : listItem) {
            if (existing.id == item.id) {
                Logger.logException(ConsignShopManager.class,
                        new Exception("Duplicate ConsignItem ID: " + item.id));
                return false;
            }
        }
        return listItem.add(item);
    }
    public synchronized boolean removeItem(ConsignItem item) {
        return listItem.remove(item);
    }
    public synchronized void save() {
        Connection con = null;
        Statement s = null;
        long startTime = System.currentTimeMillis();
        int savedCount = 0;
        
        try {
            Logger.logln("ConsignShopManager: Starting to save ConsignShop data (" + listItem.size() + " items)...");
            con = AlyraManager.getConnection();
            con.setAutoCommit(false);
            s = con.createStatement();
            s.execute("TRUNCATE shop_ky_gui");
            for (ConsignItem it : this.listItem) {
                if (it != null) {
                    String options = JSONValue.toJSONString(it.options);
                    options = options.equals("null") ? "[]" : options.replace("'", "''");
                    String playerName = it.playerName != null ? it.playerName.replace("'", "''") : "Unknown";
                    String sql = String.format(
                            "INSERT INTO `shop_ky_gui`(`id`, `player_id`, `player_name`, `tab`, `item_id`, `gold`, `gem`, `quantity`, `itemOption`, `lastTime`, `isBuy`) "
                            + "VALUES (%d, %d, '%s', %d, %d, %d, %d, %d, '%s', %d, %d)",
                            it.id, it.player_sell, playerName, it.tab, it.itemId, it.goldSell, it.gemSell,
                            it.quantity, options, it.lasttime, it.isBuy ? 1 : 0
                    );
                    s.execute(sql);
                    savedCount++;
                }
            }

            con.commit();
            
            long duration = System.currentTimeMillis() - startTime;
            Logger.logln("ConsignShopManager: Saved successfully: " + savedCount + " items in " + duration + "ms");
        } catch (Exception e) {
            Logger.logException(ConsignShopManager.class, e, "Failed to save ConsignShop data! Saved " + savedCount + "/" + listItem.size() + " items before error.");
            try {
                if (con != null) {
                    con.rollback();
                    Logger.logln("ConsignShopManager: Transaction rolled back");
                }
            } catch (Exception ex) {
                Logger.logException(ConsignShopManager.class, ex, "ConsignShopManager: Failed to rollback transaction");
            }
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception ex) {
                Logger.logException(ConsignShopManager.class, ex, "ConsignShopManager: Failed to close database connection");
            }
        }
    }
}
