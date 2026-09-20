package mail.dto;

/**
 * DTO cho reward response
 */
public class RewardDTO {
    private long id;
    private byte rewardType;
    private long rewardId;
    private int amount;
    private boolean claimed;

    public RewardDTO() {
    }

    public RewardDTO(long id, byte rewardType, long rewardId, int amount, boolean claimed) {
        this.id = id;
        this.rewardType = rewardType;
        this.rewardId = rewardId;
        this.amount = amount;
        this.claimed = claimed;
    }

    // Getters & Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte getRewardType() {
        return rewardType;
    }

    public void setRewardType(byte rewardType) {
        this.rewardType = rewardType;
    }

    public long getRewardId() {
        return rewardId;
    }

    public void setRewardId(long rewardId) {
        this.rewardId = rewardId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    @Override
    public String toString() {
        return "RewardDTO{" +
                "id=" + id +
                ", rewardType=" + rewardType +
                ", rewardId=" + rewardId +
                ", amount=" + amount +
                ", claimed=" + claimed +
                '}';
    }
}
