package mail.dao;

import mail.entity.PlayerMail;
import java.util.List;

/**
 * Interface DAO cho PlayerMail
 */
public interface IMailDAO {
    /**
     * Lưu mail mới
     */
    long saveMail(PlayerMail mail);

    /**
     * Lấy mail theo ID
     */
    PlayerMail getMailById(long mailId);

    /**
     * Lấy tất cả mail của player (chưa xóa, chưa hết hạn)
     * Sắp xếp theo thời gian descending
     */
    List<PlayerMail> getActiveMailsByPlayerId(long playerId);

    /**
     * Lấy mail của player với phân trang
     */
    List<PlayerMail> getMailsByPlayerId(long playerId, int offset, int limit);

    /**
     * Lấy số mail của player
     */
    int getMailCountByPlayerId(long playerId);

    /**
     * Cập nhật trạng thái "đã đọc"
     */
    void markMailAsRead(long mailId);

    /**
     * Cập nhật trạng thái mail
     */
    void updateMailStatus(long mailId, byte status);

    /**
     * Cập nhật has_reward
     */
    void updateHasReward(long mailId, boolean hasReward);

    /**
     * Xóa mail
     */
    void deleteMail(long mailId);

    /**
     * Xóa mail và rewards liên quan
     */
    void deleteMailWithRewards(long mailId);

    /**
     * Dọn mail hết hạn
     */
    void deleteExpiredMails();

    /**
     * Dọn mail hết hạn của player cụ thể, giữ lại tối đa maxMailCount
     */
    void deleteOldMailsIfExceedsLimit(long playerId, int maxMailCount);
}
