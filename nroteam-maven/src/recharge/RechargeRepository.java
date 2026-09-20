package recharge;

import data.AlyraManager;
import recharge.RechargeModels.History;
import recharge.RechargeModels.PackageReward;
import recharge.RechargeModels.PlayerChoice;
import recharge.RechargeModels.RechargePackage;
import recharge.RechargeModels.Settings;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class RechargeRepository {
    private static volatile boolean schemaReady;

    private RechargeRepository() {}

    public static synchronized void ensureSchema() throws Exception {
        if (schemaReady) return;
        AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS recharge_settings ("
                + "id TINYINT PRIMARY KEY,bank_name VARCHAR(100) NOT NULL,account_number VARCHAR(50) NOT NULL,"
                + "account_holder VARCHAR(100) NOT NULL,transfer_pattern VARCHAR(150) NOT NULL,warning_text TEXT NOT NULL,"
                + "rate_vnd BIGINT NOT NULL,rate_gem INT NOT NULL,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        AlyraManager.executeUpdate("INSERT IGNORE INTO recharge_settings"
                + "(id,bank_name,account_number,account_holder,transfer_pattern,warning_text,rate_vnd,rate_gem) VALUES"
                + "(1,'MBBank','8386888999888','VY NGOC DINH','{player_name}',"
                + "'Chuyển khoản đúng nội dung, chuyển khoản không đúng nội dung -> admin không giải quyết.',1000,1)");
        AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS recharge_package ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(150) NOT NULL,active TINYINT(1) NOT NULL DEFAULT 1,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS recharge_package_reward ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,package_id BIGINT NOT NULL,reward_type VARCHAR(10) NOT NULL,"
                + "item_template_id INT NOT NULL DEFAULT 0,amount INT NOT NULL,options_data TEXT NULL,sort_order INT NOT NULL DEFAULT 0,"
                + "KEY idx_rpr_package(package_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS recharge_history ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY,player_id BIGINT NOT NULL,player_name VARCHAR(100) NOT NULL,"
                + "recharge_type VARCHAR(10) NOT NULL,cash_amount BIGINT NOT NULL DEFAULT 0,diamond_base INT NOT NULL DEFAULT 0,"
                + "diamond_total INT NOT NULL DEFAULT 0,package_id BIGINT NULL,package_name VARCHAR(150) NULL,"
                + "event_points INT NOT NULL DEFAULT 0,mail_id BIGINT NOT NULL,reward_snapshot LONGTEXT NULL,"
                + "status VARCHAR(12) NOT NULL DEFAULT 'SUCCESS',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_rh_player(player_id,created_at),KEY idx_rh_event(recharge_type,status,created_at)) "
                + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        schemaReady = true;
        Logger.success("Khoi tao database nap thu cong thanh cong\n");
    }

    private static void ready() throws Exception { if (!schemaReady) ensureSchema(); }

    public static Settings getSettings() throws Exception {
        ready();
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM recharge_settings WHERE id=1");
             ResultSet rs = ps.executeQuery()) {
            Settings s = new Settings();
            if (rs.next()) {
                s.bankName = rs.getString("bank_name");
                s.accountNumber = rs.getString("account_number");
                s.accountHolder = rs.getString("account_holder");
                s.transferPattern = rs.getString("transfer_pattern");
                s.warning = rs.getString("warning_text");
                s.rateVnd = rs.getLong("rate_vnd");
                s.rateGem = rs.getInt("rate_gem");
            }
            return s;
        }
    }

    public static void saveSettings(Settings s) throws Exception {
        ready();
        if (s.rateVnd <= 0 || s.rateGem <= 0) throw new IllegalArgumentException("Tỷ giá phải lớn hơn 0");
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "UPDATE recharge_settings SET bank_name=?,account_number=?,account_holder=?,transfer_pattern=?,warning_text=?,rate_vnd=?,rate_gem=? WHERE id=1")) {
            ps.setString(1, s.bankName); ps.setString(2, s.accountNumber); ps.setString(3, s.accountHolder);
            ps.setString(4, s.transferPattern); ps.setString(5, s.warning); ps.setLong(6, s.rateVnd); ps.setInt(7, s.rateGem);
            ps.executeUpdate();
        }
    }

    public static List<PlayerChoice> searchPlayers(String keyword) throws Exception {
        ready();
        String value = keyword == null ? "" : keyword.trim();
        List<PlayerChoice> result = new ArrayList<>();
        String sql = "SELECT id,name FROM player WHERE name LIKE ? OR CAST(id AS CHAR)=? ORDER BY name LIMIT 30";
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + value + "%"); ps.setString(2, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new PlayerChoice(rs.getLong(1), rs.getString(2)));
            }
        }
        return result;
    }

    public static List<RechargePackage> listPackages(boolean activeOnly) throws Exception {
        ready();
        List<RechargePackage> result = new ArrayList<>();
        String sql = "SELECT id,name,active FROM recharge_package" + (activeOnly ? " WHERE active=1" : "") + " ORDER BY id";
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RechargePackage p = new RechargePackage();
                p.id = rs.getLong(1); p.name = rs.getString(2); p.active = rs.getBoolean(3);
                p.rewards = getPackageRewards(con, p.id);
                result.add(p);
            }
        }
        return result;
    }

    private static List<PackageReward> getPackageRewards(Connection con, long packageId) throws Exception {
        List<PackageReward> result = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id,reward_type,item_template_id,amount,options_data FROM recharge_package_reward WHERE package_id=? ORDER BY sort_order,id")) {
            ps.setLong(1, packageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PackageReward r = new PackageReward();
                    r.id = rs.getLong(1); r.packageId = packageId; r.type = rs.getString(2);
                    r.itemId = rs.getInt(3); r.amount = rs.getInt(4); r.optionsData = rs.getString(5);
                    result.add(r);
                }
            }
        }
        return result;
    }

    public static long savePackage(RechargePackage pack) throws Exception {
        ready();
        if (pack.name == null || pack.name.isBlank()) throw new IllegalArgumentException("Tên gói không được để trống");
        validateRewards(pack.rewards);
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (pack.id <= 0) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO recharge_package(name,active) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, pack.name.trim()); ps.setBoolean(2, pack.active); ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) { if (!rs.next()) throw new IllegalStateException("Không tạo được gói"); pack.id = rs.getLong(1); }
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE recharge_package SET name=?,active=? WHERE id=?")) {
                        ps.setString(1, pack.name.trim()); ps.setBoolean(2, pack.active); ps.setLong(3, pack.id); ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM recharge_package_reward WHERE package_id=?")) {
                        ps.setLong(1, pack.id); ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO recharge_package_reward(package_id,reward_type,item_template_id,amount,options_data,sort_order) VALUES(?,?,?,?,?,?)")) {
                    int order = 0;
                    for (PackageReward r : pack.rewards) {
                        ps.setLong(1, pack.id); ps.setString(2, r.type); ps.setInt(3, r.itemId);
                        ps.setInt(4, r.amount); ps.setString(5, emptyToNull(r.optionsData)); ps.setInt(6, order++); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                return pack.id;
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    private static void validateRewards(List<PackageReward> rewards) {
        if (rewards == null || rewards.isEmpty()) throw new IllegalArgumentException("Gói phải có ít nhất một phần thưởng");
        for (PackageReward r : rewards) {
            if (!("ITEM".equals(r.type) || "GEM".equals(r.type) || "GOLD".equals(r.type)))
                throw new IllegalArgumentException("Loại phần thưởng không hợp lệ");
            if (r.amount <= 0) throw new IllegalArgumentException("Số lượng phần thưởng phải lớn hơn 0");
            if ("ITEM".equals(r.type) && r.itemId <= 0) throw new IllegalArgumentException("Vật phẩm phải có Item ID lớn hơn 0");
        }
    }

    public static List<History> listHistory(int limit) throws Exception {
        ready();
        List<History> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM recharge_history ORDER BY id DESC LIMIT ?")) {
            ps.setInt(1, Math.max(1, Math.min(500, limit)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    History h = new History();
                    h.id = rs.getLong("id"); h.playerId = rs.getLong("player_id"); h.playerName = rs.getString("player_name");
                    h.type = rs.getString("recharge_type"); h.cashAmount = rs.getLong("cash_amount");
                    h.diamondBase = rs.getInt("diamond_base"); h.diamondTotal = rs.getInt("diamond_total");
                    h.packageId = rs.getLong("package_id"); h.packageName = rs.getString("package_name");
                    h.eventPoints = rs.getInt("event_points"); h.mailId = rs.getLong("mail_id");
                    h.rewardSnapshot = rs.getString("reward_snapshot"); h.status = rs.getString("status"); h.createdAt = rs.getTimestamp("created_at");
                    result.add(h);
                }
            }
        }
        return result;
    }

    public static String itemName(int itemId) {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT name FROM item_template WHERE id=?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString(1); }
        } catch (Exception ignored) {}
        return "Vật phẩm";
    }

    private static String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
