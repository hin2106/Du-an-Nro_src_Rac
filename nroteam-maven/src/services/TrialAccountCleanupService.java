package services;

import data.AlyraManager;
import utils.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service tự động xóa trial account cũ để tránh đầy database
 * Xóa các trial account:
 * - Không có player
 * - Không login trong 7 ngày
 * - Hoặc có player nhưng không login trong 30 ngày
 */
public class TrialAccountCleanupService {

    private static TrialAccountCleanupService instance;
    private ScheduledExecutorService scheduler;
    private static final long CLEANUP_INTERVAL_HOURS = 24; // Chạy mỗi 24 giờ
    private static final long TRIAL_ACCOUNT_EXPIRE_DAYS = 7; // Trial account không có player hết hạn sau 7 ngày
    private static final long TRIAL_ACCOUNT_WITH_PLAYER_EXPIRE_DAYS = 30; // Trial account có player hết hạn sau 30 ngày

    private TrialAccountCleanupService() {
    }

    public static TrialAccountCleanupService gI() {
        if (instance == null) {
            instance = new TrialAccountCleanupService();
        }
        return instance;
    }

    /**
     * Bắt đầu service cleanup
     */
    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TrialAccountCleanup");
            t.setDaemon(true);
            return t;
        });

        // Chạy ngay lần đầu, sau đó chạy định kỳ
        scheduler.schedule(() -> {
            cleanupTrialAccounts();
        }, 60, TimeUnit.SECONDS); // Chạy sau 60 giây khi server start

        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupTrialAccounts();
            } catch (Exception e) {
                Logger.logException(TrialAccountCleanupService.class, e, "Lỗi khi cleanup trial accounts");
            }
        }, CLEANUP_INTERVAL_HOURS, CLEANUP_INTERVAL_HOURS, TimeUnit.HOURS);

        Logger.success("TrialAccountCleanupService đã khởi động - sẽ chạy mỗi " + CLEANUP_INTERVAL_HOURS + " giờ\n");
    }

    /**
     * Dừng service
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Xóa các trial account cũ
     */
    private void cleanupTrialAccounts() {
        try {
            long currentTime = System.currentTimeMillis();
            long expireTimeNoPlayer = currentTime - (TRIAL_ACCOUNT_EXPIRE_DAYS * 24 * 60 * 60 * 1000L);
            long expireTimeWithPlayer = currentTime - (TRIAL_ACCOUNT_WITH_PLAYER_EXPIRE_DAYS * 24 * 60 * 60 * 1000L);
            int deletedNoPlayer = AlyraManager.executeUpdate(
                    "DELETE a FROM account a " +
                    "LEFT JOIN player p ON a.id = p.account_id " +
                    "WHERE a.username LIKE 'trial_%' " +
                    "AND p.id IS NULL " +
                    "AND (a.last_time_login IS NULL OR a.last_time_login < ?) " +
                    "AND a.create_time < ?",
                    new java.sql.Timestamp(expireTimeNoPlayer),
                    new java.sql.Timestamp(expireTimeNoPlayer));

            int deletedWithPlayer = AlyraManager.executeUpdate(
                    "DELETE a, p, pm, sr, kp, kpb, sk, dpp, dpc, dpt, dppu FROM account a " +
                    "INNER JOIN player p ON a.id = p.account_id " +
                    "LEFT JOIN player_mail pm ON pm.player_id = p.id " +
                    "LEFT JOIN super_rank sr ON sr.player_id = p.id " +
                    "LEFT JOIN kol_task_progress kp ON kp.player_id = p.id " +
                    "LEFT JOIN kol_task_progress_backup kpb ON kpb.player_id = p.id " +
                    "LEFT JOIN shop_ky_gui sk ON sk.player_id = p.id " +
                    "LEFT JOIN dragon_pass_player dpp ON dpp.player_id = p.id " +
                    "LEFT JOIN dragon_pass_claim dpc ON dpc.player_id = p.id " +
                    "LEFT JOIN dragon_pass_task_progress dpt ON dpt.player_id = p.id " +
                    "LEFT JOIN dragon_pass_purchase dppu ON dppu.player_id = p.id " +
                    "WHERE a.username LIKE 'trial_%' " +
                    "AND (a.last_time_login IS NULL OR a.last_time_login < ?)",
                    new java.sql.Timestamp(expireTimeWithPlayer));

            int totalDeleted = deletedNoPlayer + deletedWithPlayer;
            if (totalDeleted > 0) {
                Logger.success("Đã xóa " + totalDeleted + " trial account cũ (" + deletedNoPlayer + " không có player, " + deletedWithPlayer + " có player)\n");
            }
        } catch (Exception e) {
            Logger.logException(TrialAccountCleanupService.class, e, "Lỗi khi cleanup trial accounts");
        }
    }

    /**
     * Xóa trial account ngay lập tức (dùng khi cần)
     */
    public void cleanupNow() {
        cleanupTrialAccounts();
    }
}

