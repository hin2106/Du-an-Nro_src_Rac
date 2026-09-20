package mail.dao.impl;

import data.AlyraManager;
import mail.dao.IMailRewardDAO;
import mail.entity.MailReward;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của IMailRewardDAO sử dụng JDBC + AlyraManager connection pool
 */
public class MailRewardDAOImpl implements IMailRewardDAO {

    @Override
    public long saveReward(MailReward reward) {
        String sql = "INSERT INTO mail_reward (mail_id, reward_type, reward_id, amount, claimed, options_data) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, reward.getMailId());
            pstmt.setByte(2, reward.getRewardType());
            pstmt.setLong(3, reward.getRewardId());
            pstmt.setInt(4, reward.getAmount());
            pstmt.setBoolean(5, reward.isClaimed());
            pstmt.setString(6, reward.getOptionsData());
            
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
    public void saveRewards(List<MailReward> rewards) {
        String sql = "INSERT INTO mail_reward (mail_id, reward_type, reward_id, amount, claimed, options_data) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            int batchCount = 0;
            for (MailReward reward : rewards) {
                pstmt.setLong(1, reward.getMailId());
                pstmt.setByte(2, reward.getRewardType());
                pstmt.setLong(3, reward.getRewardId());
                pstmt.setInt(4, reward.getAmount());
                pstmt.setBoolean(5, reward.isClaimed());
                pstmt.setString(6, reward.getOptionsData());
                
                pstmt.addBatch();
                batchCount++;
                
                if (batchCount % 1000 == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                    batchCount = 0;
                }
            }
            
            if (batchCount > 0) {
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MailReward getRewardById(long rewardId) {
        String sql = "SELECT * FROM mail_reward WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, rewardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReward(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MailReward> getRewardsByMailId(long mailId) {
        String sql = "SELECT * FROM mail_reward WHERE mail_id = ? ORDER BY created_at";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, mailId);
            
            List<MailReward> rewards = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rewards.add(mapResultSetToReward(rs));
                }
            }
            return rewards;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void markRewardAsClaimed(long rewardId) {
        String sql = "UPDATE mail_reward SET claimed = true, claimed_at = NOW() WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, rewardId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markRewardsAsClaimed(List<Long> rewardIds) {
        String sql = "UPDATE mail_reward SET claimed = true, claimed_at = NOW() WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (Long rewardId : rewardIds) {
                pstmt.setLong(1, rewardId);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteReward(long rewardId) {
        String sql = "DELETE FROM mail_reward WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, rewardId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteRewardsByMailId(long mailId) {
        String sql = "DELETE FROM mail_reward WHERE mail_id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, mailId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRewardClaimed(long rewardId) {
        String sql = "SELECT claimed FROM mail_reward WHERE id = ?";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, rewardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("claimed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<MailReward> getUnclaimedRewardsByMailId(long mailId) {
        String sql = "SELECT * FROM mail_reward WHERE mail_id = ? AND claimed = false ORDER BY created_at";
        
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setLong(1, mailId);
            
            List<MailReward> rewards = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rewards.add(mapResultSetToReward(rs));
                }
            }
            return rewards;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Map ResultSet row to MailReward object
     */
    private MailReward mapResultSetToReward(ResultSet rs) throws SQLException {
        MailReward reward = new MailReward();
        reward.setId(rs.getLong("id"));
        reward.setMailId(rs.getLong("mail_id"));
        reward.setRewardType(rs.getByte("reward_type"));
        reward.setRewardId(rs.getLong("reward_id"));
        reward.setAmount(rs.getInt("amount"));
        reward.setClaimed(rs.getBoolean("claimed"));
        try {
            reward.setOptionsData(rs.getString("options_data"));
        } catch (SQLException ignored) {
        }
        return reward;
    }
}
