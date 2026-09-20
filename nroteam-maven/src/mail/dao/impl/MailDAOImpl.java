package mail.dao.impl;

import data.AlyraManager;
import mail.dao.IMailDAO;
import mail.entity.PlayerMail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của IMailDAO sử dụng JDBC + AlyraManager connection pool
 */
public class MailDAOImpl implements IMailDAO {

    @Override
    public long saveMail(PlayerMail mail) {
        String sql = "INSERT INTO player_mail (player_id, title, content, has_reward, is_read, status, created_at, expired_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, mail.getPlayerId());
            pstmt.setString(2, mail.getTitle());
            pstmt.setString(3, mail.getContent());
            pstmt.setBoolean(4, mail.isHasReward());
            pstmt.setBoolean(5, mail.isRead());
            pstmt.setByte(6, mail.getStatus());
            pstmt.setTimestamp(7, mail.getCreatedAt());
            pstmt.setTimestamp(8, mail.getExpiredAt());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public PlayerMail getMailById(long mailId) {
        String sql = "SELECT * FROM player_mail WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, mailId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMail(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<PlayerMail> getActiveMailsByPlayerId(long playerId) {
        String sql = "SELECT * FROM player_mail " +
                "WHERE player_id = ? " +
                "  AND status = 1 " +
                "  AND expired_at > NOW() " +
                "ORDER BY created_at DESC";
        
        return executeMailQuery(sql, playerId);
    }

    @Override
    public List<PlayerMail> getMailsByPlayerId(long playerId, int offset, int limit) {
        String sql = "SELECT * FROM player_mail " +
                "WHERE player_id = ? " +
                "  AND status != 4 " +
                "  AND expired_at > NOW() " +
                "ORDER BY created_at DESC " +
                "LIMIT ?, ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, playerId);
            pstmt.setInt(2, offset);
            pstmt.setInt(3, limit);
            
            List<PlayerMail> mails = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mails.add(mapResultSetToMail(rs));
                }
            }
            return mails;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public int getMailCountByPlayerId(long playerId) {
        String sql = "SELECT COUNT(*) as count FROM player_mail WHERE player_id = ? AND status != 4";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void markMailAsRead(long mailId) {
        String sql = "UPDATE player_mail SET is_read = true, status = 2 WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, mailId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateMailStatus(long mailId, byte status) {
        String sql = "UPDATE player_mail SET status = ? WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setByte(1, status);
            pstmt.setLong(2, mailId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHasReward(long mailId, boolean hasReward) {
        String sql = "UPDATE player_mail SET has_reward = ? WHERE id = ?";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setBoolean(1, hasReward);
            pstmt.setLong(2, mailId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMail(long mailId) {
        String sql = "UPDATE player_mail SET status = 4 WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, mailId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMailWithRewards(long mailId) {
        String deleteRewardsSql = "DELETE FROM mail_reward WHERE mail_id = ?";
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(deleteRewardsSql)) {
            pstmt.setLong(1, mailId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        deleteMail(mailId);
    }

    @Override
    public void deleteExpiredMails() {
        String sql = "DELETE FROM player_mail WHERE status != 4 AND expired_at < NOW()";
        
        try (Connection con = AlyraManager.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteOldMailsIfExceedsLimit(long playerId, int maxMailCount) {
        int currentCount = getMailCountByPlayerId(playerId);
        if (currentCount <= maxMailCount) {
            return;
        }
        
        String sql = "DELETE FROM player_mail " +
                "WHERE player_id = ? " +
                "  AND status != 4 " +
                "ORDER BY created_at ASC " +
                "LIMIT ?";
        
        int toDelete = currentCount - maxMailCount;
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, playerId);
            pstmt.setInt(2, toDelete);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method để execute mail queries
     */
    private List<PlayerMail> executeMailQuery(String sql, long playerId) {
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, playerId);
            
            List<PlayerMail> mails = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mails.add(mapResultSetToMail(rs));
                }
            }
            return mails;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Map ResultSet row to PlayerMail object
     */
    private PlayerMail mapResultSetToMail(ResultSet rs) throws SQLException {
        PlayerMail mail = new PlayerMail();
        mail.setId(rs.getLong("id"));
        mail.setPlayerId(rs.getLong("player_id"));
        mail.setTitle(rs.getString("title"));
        mail.setContent(rs.getString("content"));
        mail.setHasReward(rs.getBoolean("has_reward"));
        mail.setRead(rs.getBoolean("is_read"));
        mail.setStatus(rs.getByte("status"));
        mail.setCreatedAt(rs.getTimestamp("created_at"));
        mail.setExpiredAt(rs.getTimestamp("expired_at"));
        return mail;
    }
}
