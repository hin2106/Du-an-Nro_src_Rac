package mail.service;

import mail.dao.IMailDAO;
import mail.dao.IMailRewardDAO;
import mail.dao.impl.MailDAOImpl;
import mail.dao.impl.MailRewardDAOImpl;
import mail.entity.MailReward;
import mail.entity.PlayerMail;
import java.util.List;

/**
 * Service xử lý logic mail
 */
public class MailService {
    private IMailDAO mailDAO;
    private IMailRewardDAO rewardDAO;

    private static final int MAX_MAIL_PER_PLAYER = 99;

    public MailService() {
        this.mailDAO = new MailDAOImpl();
        this.rewardDAO = new MailRewardDAOImpl();
    }

    public MailService(IMailDAO mailDAO, IMailRewardDAO rewardDAO) {
        this.mailDAO = mailDAO;
        this.rewardDAO = rewardDAO;
    }

    /**
     * Gửi mail cho player
     */
    public long sendMail(long playerId, String title, String content, int expireDayCount) {
        PlayerMail mail = new PlayerMail(playerId, title, content, expireDayCount);
        long mailId = mailDAO.saveMail(mail);
        
        // Nếu vượt quá giới hạn, xóa mail cũ
        mailDAO.deleteOldMailsIfExceedsLimit(playerId, MAX_MAIL_PER_PLAYER);
        
        return mailId;
    }

    /**
     * Lấy danh sách mail của player (chưa xóa, chưa hết hạn)
     */
    public List<PlayerMail> getPlayerMails(long playerId) {
        return mailDAO.getActiveMailsByPlayerId(playerId);
    }

    /**
     * Lấy danh sách mail với phân trang
     */
    public List<PlayerMail> getPlayerMailsWithPaging(long playerId, int page, int pageSize) {
        int offset = page * pageSize;
        return mailDAO.getMailsByPlayerId(playerId, offset, pageSize);
    }

    /**
     * Mở mail (đánh dấu là đã đọc)
     */
    public void openMail(long mailId) {
        mailDAO.markMailAsRead(mailId);
    }

    /**
     * Lấy thông tin chi tiết mail
     */
    public PlayerMail getMailDetail(long mailId) {
        PlayerMail mail = mailDAO.getMailById(mailId);
        if (mail != null) {
            List<MailReward> rewards = rewardDAO.getRewardsByMailId(mailId);
            mail.setRewards(rewards);
        }
        return mail;
    }

    /**
     * Nhận reward từ mail
     * 
     * Flow:
     * 1. Load reward list
     * 2. Validate not claimed
     * 3. Add reward to inventory
     * 4. Mark reward claimed
     */
    public void claimReward(long rewardId) throws Exception {
        // 1. Load reward
        MailReward reward = rewardDAO.getRewardById(rewardId);
        if (reward == null) {
            throw new Exception("Reward không tồn tại");
        }

        // 2. Validate còn chưa nhận
        if (reward.isClaimed()) {
            throw new Exception("Reward đã được nhận");
        }

        // 3. Add reward to inventory (Call InventoryService)
        // inventoryService.addReward(reward.getPlayerId(), reward.getRewardType(), 
        //                              reward.getRewardId(), reward.getAmount());

        // 4. Mark reward as claimed
        rewardDAO.markRewardAsClaimed(rewardId);
    }

    /**
     * Nhận nhiều reward cùng lúc
     */
    public void claimRewards(List<Long> rewardIds) throws Exception {
        for (Long rewardId : rewardIds) {
            claimReward(rewardId);
        }
    }

    /**
     * Xóa mail
     */
    public void deleteMail(long mailId) {
        mailDAO.deleteMailWithRewards(mailId);
    }

    /**
     * Dọn mail hết hạn (Cron Job)
     */
    public void cleanupExpiredMails() {
        mailDAO.deleteExpiredMails();
    }

    /**
     * Lấy số mail của player
     */
    public int getMailCount(long playerId) {
        return mailDAO.getMailCountByPlayerId(playerId);
    }

    /**
     * Kiểm tra mail có reward chưa nhận
     */
    public boolean hasUnclaimedRewards(long mailId) {
        List<MailReward> rewards = rewardDAO.getUnclaimedRewardsByMailId(mailId);
        return !rewards.isEmpty();
    }

    /**
     * Lấy danh sách reward chưa nhận của mail
     */
    public List<MailReward> getUnclaimedRewards(long mailId) {
        return rewardDAO.getUnclaimedRewardsByMailId(mailId);
    }

    // Getters
    public IMailDAO getMailDAO() {
        return mailDAO;
    }

    public IMailRewardDAO getRewardDAO() {
        return rewardDAO;
    }
}
