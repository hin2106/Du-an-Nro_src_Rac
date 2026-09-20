package top;

import data.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import top.TopBoardModels.Reward;
import utils.Logger;

public final class TopBoardRepository {
    private TopBoardRepository() {}

    public static void ensureSchema() {
        try {
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS top_reward_config ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,board VARCHAR(24) NOT NULL,rank_from INT NOT NULL,"
                    + "rank_to INT NOT NULL,reward_type TINYINT NOT NULL,item_template_id INT NOT NULL DEFAULT 0,"
                    + "amount INT NOT NULL,options_data TEXT NULL,slot_no INT NOT NULL DEFAULT 0,"
                    + "INDEX idx_top_reward_board_rank(board,rank_from,rank_to)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS top_reward_delivery ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,board VARCHAR(24) NOT NULL,period_key VARCHAR(24) NOT NULL,"
                    + "player_id BIGINT NOT NULL,rank_no INT NOT NULL,mail_id BIGINT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "UNIQUE KEY uk_top_delivery(board,period_key,player_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (Exception e) {
            Logger.logException(TopBoardRepository.class, e, "Khởi tạo dữ liệu phần thưởng Top thất bại");
        }
    }

    public static List<Reward> rewardsFor(String board, int rank) {
        List<Reward> result = new ArrayList<>();
        String sql = "SELECT * FROM top_reward_config WHERE board=? AND ? BETWEEN rank_from AND rank_to ORDER BY slot_no,id";
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, board); ps.setInt(2, rank);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(read(rs)); }
        } catch (Exception e) { Logger.logException(TopBoardRepository.class, e); }
        return result;
    }

    public static List<Reward> loadBoard(String board) throws Exception {
        List<Reward> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM top_reward_config WHERE board=? ORDER BY rank_from,slot_no,id")) {
            ps.setString(1, board);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(read(rs)); }
        }
        return result;
    }

    public static void replaceBoard(String board, List<Reward> rewards) throws Exception {
        validate(rewards);
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement del = con.prepareStatement("DELETE FROM top_reward_config WHERE board=?")) {
                del.setString(1, board); del.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO top_reward_config"
                    + "(board,rank_from,rank_to,reward_type,item_template_id,amount,options_data,slot_no) VALUES(?,?,?,?,?,?,?,?)")) {
                for (Reward r : rewards) {
                    ps.setString(1, board); ps.setInt(2, r.rankFrom); ps.setInt(3, r.rankTo);
                    ps.setByte(4, r.type); ps.setInt(5, r.itemId); ps.setInt(6, r.amount);
                    ps.setString(7, r.options == null ? "[]" : r.options); ps.setInt(8, r.slot); ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
        }
    }

    private static void validate(List<Reward> rewards) {
        for (Reward r : rewards) {
            if (r.rankFrom < 1 || r.rankTo < r.rankFrom || r.rankTo > 100)
                throw new IllegalArgumentException("Khoảng hạng chỉ được nằm trong Top 1-100");
            if (r.amount <= 0) throw new IllegalArgumentException("Số lượng quà phải lớn hơn 0");
            if (r.type == 1 && r.itemId <= 0) throw new IllegalArgumentException("Quà vật phẩm chưa chọn item_template");
        }
    }

    public static boolean reserveDelivery(Connection con, String board, String period, long playerId, int rank) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO top_reward_delivery(board,period_key,player_id,rank_no) VALUES(?,?,?,?)")) {
            ps.setString(1, board); ps.setString(2, period); ps.setLong(3, playerId); ps.setInt(4, rank);
            return ps.executeUpdate() == 1;
        }
    }

    public static void completeDelivery(Connection con, String board, String period, long playerId, long mailId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("UPDATE top_reward_delivery SET mail_id=? WHERE board=? AND period_key=? AND player_id=?")) {
            ps.setLong(1, mailId); ps.setString(2, board); ps.setString(3, period); ps.setLong(4, playerId); ps.executeUpdate();
        }
    }

    public static void cancelDelivery(String board, String period, long playerId) {
        try { AlyraManager.executeUpdate("DELETE FROM top_reward_delivery WHERE board=? AND period_key=? AND player_id=? AND mail_id IS NULL",
                board, period, playerId); } catch (Exception ignored) {}
    }

    private static Reward read(ResultSet rs) throws Exception {
        Reward r = new Reward();
        r.id = rs.getLong("id"); r.board = rs.getString("board"); r.rankFrom = rs.getInt("rank_from");
        r.rankTo = rs.getInt("rank_to"); r.type = rs.getByte("reward_type"); r.itemId = rs.getInt("item_template_id");
        r.amount = rs.getInt("amount"); r.options = rs.getString("options_data"); r.slot = rs.getInt("slot_no");
        return r;
    }
}
