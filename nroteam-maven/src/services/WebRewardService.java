package services;

import data.AlyraManager;
import data.AlyraResultSet;
import player.Player;
import server.Client;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import utils.Logger;

public class WebRewardService {

    private static WebRewardService instance;
    private ScheduledExecutorService executor;
    private static final String SERVICE_NAME = "WebRewardService";
    private volatile boolean heartbeatTableReady = false;

    private WebRewardService() {}

    public static WebRewardService gI() {
        if (instance == null) {
            instance = new WebRewardService();
        }
        return instance;
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::processRewards, 10, 10, TimeUnit.SECONDS);
        updateHeartbeat();
//        Logger.log("WebRewardService started.\n");
    }

    private void updateHeartbeat() {
        try {
            ensureHeartbeatTable();
            AlyraManager.executeUpdate(
                    "INSERT INTO game_server_status(service_name, last_heartbeat) VALUES(?, NOW()) "
                    + "ON DUPLICATE KEY UPDATE last_heartbeat = NOW()",
                    SERVICE_NAME);
        } catch (Exception e) {
            Logger.logException(WebRewardService.class, e, "Failed to update game server heartbeat");
        }
    }

    private synchronized void ensureHeartbeatTable() {
        if (heartbeatTableReady) {
            return;
        }
        try {
            AlyraManager.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS game_server_status ("
                    + "service_name VARCHAR(64) NOT NULL,"
                    + "last_heartbeat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY (service_name)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
            heartbeatTableReady = true;
        } catch (Exception e) {
            Logger.logException(WebRewardService.class, e, "Failed to ensure heartbeat table");
        }
    }

    private void processRewards() {
        updateHeartbeat();
        try {
            AlyraResultSet rs = AlyraManager.executeQuery("SELECT id, account_id, quantity, history_id FROM pending_rewards WHERE processed = 0");
            while (rs.next()) {
                int id = rs.getInt("id");
                int accountId = rs.getInt("account_id");
                int rewardAmount = rs.getInt("quantity");
                int historyId = rs.getInt("history_id");
                Player player = Client.gI().getPlayerByUser(accountId);
                if (player != null) { 
                    // Bảo vệ overflow: dùng long trước khi cast về int (giống GiftCodeService / InventoryService)
                    long newGem = (long) player.inventory.gem + (long) rewardAmount;
                    player.inventory.gem = (int) Math.min(newGem, Integer.MAX_VALUE);
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player, "Bạn nhận được " + rewardAmount + " ngọc từ trang web.");

                    AlyraManager.executeUpdate("UPDATE `account` SET `napngoc` = `napngoc` + ? WHERE `id` = ?", rewardAmount, accountId);
                    
                    AlyraManager.executeUpdate("UPDATE `pending_rewards` SET `processed` = 1 WHERE `id` = ?", id);
                    if (historyId > 0) {
                        AlyraManager.executeUpdate("UPDATE `lichsu_napngoc` SET `status` = 'successful' WHERE `id` = ?", historyId);
                    }
//                    Logger.log("Processed web reward for online player account_id: " + accountId + ", amount: " + rewardAmount);
                } else { 
                    AlyraManager.executeUpdate(
                        "UPDATE `player` " +
                        "SET data_inventory = JSON_SET(" +
                        "    data_inventory, " +
                        "    '$[1]', " +
                        "    CAST(JSON_UNQUOTE(JSON_EXTRACT(data_inventory, '$[1]')) AS UNSIGNED) + ?" +
                        ") " +
                        "WHERE `account_id` = ?",
                        rewardAmount, accountId);
                    AlyraManager.executeUpdate("UPDATE `account` SET `napngoc` = `napngoc` + ? WHERE `id` = ?", rewardAmount, accountId);
                    AlyraManager.executeUpdate("UPDATE `pending_rewards` SET `processed` = 1 WHERE `id` = ?", id);
                     if (historyId > 0) {
                        AlyraManager.executeUpdate("UPDATE `lichsu_napngoc` SET `status` = 'successful' WHERE `id` = ?", historyId);
                    }
//                    Logger.log("Processed web reward for offline player account_id: " + accountId + ", amount: " + rewardAmount);
                }
            }
        } catch (Exception e) {
            Logger.logException(WebRewardService.class, e, "Error processing web rewards");
        }
    }
}
