package boss.drop;

import boss.drop.BossDropModels.Config;
import com.google.gson.Gson;
import data.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import utils.Logger;

public final class BossDropRepository {

    private static final Gson GSON = new Gson();
    private static volatile boolean schemaReady;

    private BossDropRepository() {
    }

    public static synchronized void ensureSchema() {
        if (schemaReady) {
            return;
        }
        try {
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS boss_drop_config ("
                    + "boss_id INT NOT NULL PRIMARY KEY, config_json LONGTEXT NOT NULL, "
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
            schemaReady = true;
        } catch (Exception e) {
            Logger.logException(BossDropRepository.class, e, "Khởi tạo cấu hình đồ rơi boss thất bại");
        }
    }

    public static Config load(int bossId) {
        ensureSchema();
        if (!schemaReady) {
            return null;
        }
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "SELECT config_json FROM boss_drop_config WHERE boss_id=?")) {
            ps.setInt(1, bossId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Config config = GSON.fromJson(rs.getString(1), Config.class);
                if (config != null) {
                    config.bossId = bossId;
                }
                return config;
            }
        } catch (Exception e) {
            Logger.logException(BossDropRepository.class, e, "Không tải được cấu hình boss " + bossId);
            return null;
        }
    }

    public static void save(Config config) throws Exception {
        ensureSchema();
        if (!schemaReady) {
            throw new IllegalStateException("Database cấu hình đồ rơi chưa sẵn sàng");
        }
        BossDropService.validate(config);
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO boss_drop_config(boss_id,config_json) VALUES(?,?) "
                        + "ON DUPLICATE KEY UPDATE config_json=VALUES(config_json)")) {
            ps.setInt(1, config.bossId);
            ps.setString(2, GSON.toJson(config));
            ps.executeUpdate();
        }
    }

    public static void delete(int bossId) throws Exception {
        ensureSchema();
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM boss_drop_config WHERE boss_id=?")) {
            ps.setInt(1, bossId);
            ps.executeUpdate();
        }
    }
}
