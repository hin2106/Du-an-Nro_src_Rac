package mail.entity;

/**
 * Reward đính kèm với mail
 * 
 * Bảng: mail_reward
 * - id: id
 * - mail_id: mail tương ứng
 * - reward_type: loại reward (item, gold, gem, stamina, exp, vip_point)
 * - reward_id: id của reward (item id, 0 cho gold/gem/stamina)
 * - amount: số lượng
 * - claimed: đã nhận
 */
public class MailReward {
    private long id;
    private long mailId;
    private byte rewardType; // RewardType
    private long rewardId; // ID của item, nếu reward_type = item; = 0 cho các loại khác
    private int amount;
    private boolean claimed;
    private String optionsData; // JSON: [{"id":30,"param":100},{"id":31,"param":200}]

    public MailReward() {
    }

    /**
     * Constructor cho item reward
     */
    public MailReward(long mailId, byte rewardType, long rewardId, int amount) {
        this.mailId = mailId;
        this.rewardType = rewardType;
        this.rewardId = rewardId;
        this.amount = amount;
        this.claimed = false;
    }

    /**
     * Constructor cho item reward với options
     */
    public MailReward(long mailId, byte rewardType, long rewardId, int amount, String optionsData) {
        this.mailId = mailId;
        this.rewardType = rewardType;
        this.rewardId = rewardId;
        this.amount = amount;
        this.claimed = false;
        this.optionsData = optionsData;
    }

    /**
     * Constructor cho non-item reward (gold, gem, stamina, etc)
     */
    public MailReward(long mailId, byte rewardType, int amount) {
        this.mailId = mailId;
        this.rewardType = rewardType;
        this.rewardId = 0;
        this.amount = amount;
        this.claimed = false;
    }

    // Getters & Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMailId() {
        return mailId;
    }

    public void setMailId(long mailId) {
        this.mailId = mailId;
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

    public String getOptionsData() {
        return optionsData;
    }

    public void setOptionsData(String optionsData) {
        this.optionsData = optionsData;
    }

    @Override
    public String toString() {
        return "MailReward{" +
                "id=" + id +
                ", mailId=" + mailId +
                ", rewardType=" + rewardType +
                ", rewardId=" + rewardId +
                ", amount=" + amount +
                ", claimed=" + claimed +
                ", optionsData='" + optionsData + '\'' +
                '}';
    }
}
