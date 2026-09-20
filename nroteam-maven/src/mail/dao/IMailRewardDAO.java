package mail.dao;

import mail.entity.MailReward;
import java.util.List;

/**
 * Interface DAO cho MailReward
 */
public interface IMailRewardDAO {
    /**
     * Lưu reward mới
     */
    long saveReward(MailReward reward);

    /**
     * Lưu nhiều reward cùng lúc
     */
    void saveRewards(List<MailReward> rewards);

    /**
     * Lấy reward theo ID
     */
    MailReward getRewardById(long rewardId);

    /**
     * Lấy tất cả reward của mail
     */
    List<MailReward> getRewardsByMailId(long mailId);

    /**
     * Cập nhật trạng thái "đã nhận"
     */
    void markRewardAsClaimed(long rewardId);

    /**
     * Cập nhật trạng thái claimed của các reward
     */
    void markRewardsAsClaimed(List<Long> rewardIds);

    /**
     * Xóa reward
     */
    void deleteReward(long rewardId);

    /**
     * Xóa tất cả reward của mail
     */
    void deleteRewardsByMailId(long mailId);

    /**
     * Kiểm tra reward đã được nhận chưa
     */
    boolean isRewardClaimed(long rewardId);

    /**
     * Lấy tất cả reward chưa nhận của mail
     */
    List<MailReward> getUnclaimedRewardsByMailId(long mailId);
}
