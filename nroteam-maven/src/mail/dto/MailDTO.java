package mail.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO cho mail response
 */
public class MailDTO {
    private long id;
    private String title;
    private String content;
    private boolean isRead;
    private long expiredAt;
    private List<RewardDTO> rewards = new ArrayList<>();

    public MailDTO() {
    }

    public MailDTO(long id, String title, String content, boolean isRead, long expiredAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isRead = isRead;
        this.expiredAt = expiredAt;
    }

    // Getters & Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public List<RewardDTO> getRewards() {
        return rewards;
    }

    public void setRewards(List<RewardDTO> rewards) {
        this.rewards = rewards;
    }

    public void addReward(RewardDTO reward) {
        this.rewards.add(reward);
    }

    @Override
    public String toString() {
        return "MailDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", rewardCount=" + rewards.size() +
                '}';
    }
}
