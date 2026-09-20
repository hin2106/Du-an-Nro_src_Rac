package mail.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Mail instance của từng player
 * 
 * Bảng: player_mail
 * - id: mail id
 * - player_id: người nhận
 * - title: tiêu đề
 * - content: nội dung
 * - has_reward: có reward
 * - is_read: đã đọc
 * - status: active / deleted / expired
 * - created_at: thời gian gửi
 * - expired_at: hết hạn
 */
public class PlayerMail {
    private long id;
    private long playerId;
    private String title;
    private String content;
    private boolean hasReward;
    private boolean isRead;
    private byte status; // MailStatus
    private Timestamp createdAt;
    private Timestamp expiredAt;
    private List<MailReward> rewards = new ArrayList<>();

    public PlayerMail() {
    }

    public PlayerMail(long playerId, String title, String content, int expireDayCount) {
        this.playerId = playerId;
        this.title = title;
        this.content = content;
        this.hasReward = false;
        this.isRead = false;
        this.status = 1; // ACTIVE
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.expiredAt = new Timestamp(System.currentTimeMillis() + expireDayCount * 24 * 60 * 60 * 1000L);
    }

    // Getters & Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isHasReward() {
        return hasReward;
    }

    public void setHasReward(boolean hasReward) {
        this.hasReward = hasReward;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Timestamp expiredAt) {
        this.expiredAt = expiredAt;
    }

    public List<MailReward> getRewards() {
        return rewards;
    }

    public void setRewards(List<MailReward> rewards) {
        this.rewards = rewards;
        this.hasReward = !rewards.isEmpty();
    }

    public void addReward(MailReward reward) {
        if (this.rewards == null) {
            this.rewards = new ArrayList<>();
        }
        this.rewards.add(reward);
        this.hasReward = true;
    }

    /**
     * Kiểm tra mail đã hết hạn
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiredAt.getTime();
    }

    /**
     * Kiểm tra mail còn hoạt động
     */
    public boolean isActive() {
        return status == 1 && !isExpired();
    }

    @Override
    public String toString() {
        return "PlayerMail{" +
                "id=" + id +
                ", playerId=" + playerId +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", expiredAt=" + expiredAt +
                ", rewardCount=" + rewards.size() +
                '}';
    }
}
