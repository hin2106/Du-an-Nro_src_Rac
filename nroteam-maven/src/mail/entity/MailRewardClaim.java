package mail.entity;

/**
 * Trạng thái nhận reward từ mail
 * 
 * Bảng: mail_reward_claim
 * - id: id
 * - reward_id: reward id
 * - player_id: player
 * - claimed: đã nhận
 * - claimed_at: thời gian nhận
 * 
 * LƯU Ý: Có thể gộp vào mail_reward nếu mail chỉ có 1 player
 * Trong implementation này, chúng ta merge vào MailReward.claimed
 */
public class MailRewardClaim {
    private long id;
    private long rewardId;
    private long playerId;
    private boolean claimed;
    private long claimedAt; // Unix timestamp

    public MailRewardClaim() {
    }

    public MailRewardClaim(long rewardId, long playerId) {
        this.rewardId = rewardId;
        this.playerId = playerId;
        this.claimed = false;
        this.claimedAt = 0;
    }

    // Getters & Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRewardId() {
        return rewardId;
    }

    public void setRewardId(long rewardId) {
        this.rewardId = rewardId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
        if (claimed && this.claimedAt == 0) {
            this.claimedAt = System.currentTimeMillis();
        }
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(long claimedAt) {
        this.claimedAt = claimedAt;
    }

    @Override
    public String toString() {
        return "MailRewardClaim{" +
                "id=" + id +
                ", rewardId=" + rewardId +
                ", playerId=" + playerId +
                ", claimed=" + claimed +
                '}';
    }
}
