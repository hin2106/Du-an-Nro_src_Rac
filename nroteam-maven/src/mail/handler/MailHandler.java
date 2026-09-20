package mail.handler;

import mail.entity.MailReward;
import mail.entity.PlayerMail;
import mail.enums.RewardType;
import mail.service.MailService;
import network.Message;
import player.Player;
import consts.Cmd_message;
import services.Service;
import services.ItemService;
import services.player.InventoryService;
import utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Handler xử lý Mail qua socket protocol
 * 
 * CMD: -48 (MAIL)
 * Sub-actions:
 *   0 = GET_MAIL_LIST (client gửi) / MAIL_LIST (server trả)
 *   1 = GET_MAIL_DETAIL (client gửi) / MAIL_DETAIL (server trả)
 *   2 = MARK_READ (client gửi)
 *   3 = CLAIM_REWARDS (client gửi) / CLAIM_RESULT (server trả)
 *   4 = DELETE_MAIL (client gửi)
 *   5 = MAIL_COUNT (server trả)
 *   6 = NEW_MAIL_NOTIFY (server push)
 */
public class MailHandler {

    public static final byte ACTION_GET_LIST = 0;
    public static final byte ACTION_GET_DETAIL = 1;
    public static final byte ACTION_MARK_READ = 2;
    public static final byte ACTION_CLAIM_REWARDS = 3;
    public static final byte ACTION_DELETE = 4;
    public static final byte ACTION_MAIL_COUNT = 5;
    public static final byte ACTION_NEW_MAIL_NOTIFY = 6;

    private static MailHandler instance;
    private MailService mailService;

    public MailHandler() {
        this.mailService = new MailService();
    }

    public MailHandler(MailService mailService) {
        this.mailService = mailService;
    }

    public static MailHandler gI() {
        if (instance == null) {
            instance = new MailHandler();
        }
        return instance;
    }

    /**
     * Xử lý message từ client
     */
    public void controller(Player player, Message msg) {
        try {
            byte action = msg.reader().readByte();
            switch (action) {
                case ACTION_GET_LIST -> {
                    int page = msg.reader().readByte();
                    int pageSize = msg.reader().readByte();
                    sendMailList(player, page, pageSize);
                }
                case ACTION_GET_DETAIL -> {
                    long mailId = msg.reader().readInt();
                    sendMailDetail(player, mailId);
                }
                case ACTION_MARK_READ -> {
                    long mailId = msg.reader().readInt();
                    markMailAsRead(player, mailId);
                }
                case ACTION_CLAIM_REWARDS -> {
                    byte count = msg.reader().readByte();
                    long[] rewardIds = new long[count];
                    for (int i = 0; i < count; i++) {
                        rewardIds[i] = msg.reader().readInt();
                    }
                    claimRewards(player, rewardIds);
                }
                case ACTION_DELETE -> {
                    long mailId = msg.reader().readInt();
                    deleteMail(player, mailId);
                }
                default -> Logger.warning("Unknown mail action: " + action + "\n");
            }
        } catch (IOException e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    /**
     * Gửi danh sách mail cho player
     */
    public void sendMailList(Player player, int page, int pageSize) {
        try {
            List<PlayerMail> mails = mailService.getPlayerMailsWithPaging(
                    player.id, page, pageSize);
            int totalCount = mailService.getMailCount(player.id);

            Message msg = new Message(Cmd_message.MAIL);
            msg.writer().writeByte(ACTION_GET_LIST);
            msg.writer().writeByte(page);
            msg.writer().writeShort(totalCount);
            msg.writer().writeByte(mails.size());

            for (PlayerMail mail : mails) {
                msg.writer().writeInt((int) mail.getId());
                msg.writer().writeUTF(mail.getTitle());
                msg.writer().writeBoolean(mail.isRead());
                msg.writer().writeBoolean(mail.isHasReward());
                msg.writer().writeLong(mail.getExpiredAt().getTime());
            }

            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    /**
     * Gửi chi tiết mail cho player
     */
    public void sendMailDetail(Player player, long mailId) {
        try {
            PlayerMail mail = mailService.getMailDetail(mailId);
            if (mail == null || mail.getPlayerId() != player.id) {
                sendError(player, "Mail không tồn tại");
                return;
            }

            List<MailReward> rewards = (mail.getRewards() != null) ? 
                    mail.getRewards() : List.of();

            Message msg = new Message(Cmd_message.MAIL);
            msg.writer().writeByte(ACTION_GET_DETAIL);
            msg.writer().writeInt((int) mail.getId());
            msg.writer().writeUTF(mail.getTitle());
            msg.writer().writeUTF(mail.getContent());
            msg.writer().writeBoolean(mail.isRead());
            msg.writer().writeLong(mail.getExpiredAt().getTime());
            msg.writer().writeByte(rewards.size());

            for (MailReward reward : rewards) {
                msg.writer().writeInt((int) reward.getId());
                msg.writer().writeByte(reward.getRewardType());
                msg.writer().writeInt((int) reward.getRewardId());
                msg.writer().writeInt(reward.getAmount());
                msg.writer().writeBoolean(reward.isClaimed());
                msg.writer().writeUTF(reward.getOptionsData() == null ? "" : reward.getOptionsData());
            }

            player.sendMessage(msg);
            msg.cleanup();

            // Auto mark as read
            if (!mail.isRead()) {
                mailService.openMail(mailId);
            }
        } catch (IOException e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    /**
     * Đánh dấu mail đã đọc
     */
    public void markMailAsRead(Player player, long mailId) {
        try {
            PlayerMail mail = mailService.getMailDetail(mailId);
            if (mail == null || mail.getPlayerId() != player.id) {
                return;
            }
            mailService.openMail(mailId);
        } catch (Exception e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    /**
     * Nhận rewards
     */
    public void claimRewards(Player player, long[] rewardIds) {
        try {
            List<MailReward> claimableRewards = new ArrayList<>();
            int requiredItemSlots = 0;

            for (long rewardId : rewardIds) {
                MailReward reward = mailService.getRewardDAO().getRewardById(rewardId);
                if (reward == null || reward.isClaimed()) {
                    continue;
                }

                PlayerMail ownerMail = mailService.getMailDAO().getMailById(reward.getMailId());
                if (ownerMail == null || ownerMail.getPlayerId() != player.id) {
                    continue;
                }

                claimableRewards.add(reward);
                if (reward.getRewardType() == RewardType.ITEM.getCode()) {
                    requiredItemSlots++;
                }
            }

            if (requiredItemSlots > 0) {
                int emptyBagSlots = Byte.toUnsignedInt(InventoryService.gI().getCountEmptyBag(player));
                if (emptyBagSlots <= requiredItemSlots) {
                    sendClaimResult(player, 0,
                            "Hành trang trống không đủ.\n Cần nhiều hơn " + requiredItemSlots + " ô để nhận thư.");
                    return;
                }
            }

            int claimedCount = 0;
            boolean inventoryChanged = false;
            boolean moneyChanged = false;

            for (MailReward reward : claimableRewards) {
                try {
                    byte type = reward.getRewardType();
                    int amount = reward.getAmount();
                    boolean canClaimReward = true;

                    if (type == RewardType.GOLD.getCode()) {
                        player.inventory.gold += amount;
                        moneyChanged = true;
                    } else if (type == RewardType.GEM.getCode()) {
                        player.inventory.gem += amount;
                        moneyChanged = true;
                    } else if (type == RewardType.ITEM.getCode()) {
                        item.Item newItem = ItemService.gI().createNewItem(
                                (short) reward.getRewardId(), amount);
                        if (newItem != null) {
                            applyOptionsData(newItem, reward.getOptionsData());
                            boolean added = InventoryService.gI().addItemBag(player, newItem);
                            if (added) {
                                inventoryChanged = true;
                            } else {
                                canClaimReward = false;
                            }
                        } else {
                            canClaimReward = false;
                        }
                    } else if (type == RewardType.STAMINA.getCode()) {
                        player.nPoint.stamina += amount;
                    } else if (type == RewardType.EXP.getCode()) {
                        player.nPoint.tiemNang += amount;
                    } else if (type == RewardType.VIP_POINT.getCode()) {
                        player.inventory.ruby += amount;
                        moneyChanged = true;
                    }

                    if (canClaimReward) {
                        mailService.claimReward(reward.getId());
                        claimedCount++;
                    }
                } catch (Exception e) {
                    Logger.error("Lỗi nhận reward " + reward.getId() + ": " + e.getMessage() + "\n");
                }
            }

            // Đồng bộ client
            if (moneyChanged) {
                Service.gI().sendMoney(player);
            }
            if (inventoryChanged) {
                InventoryService.gI().sendItemBags(player);
            }

            // Gửi kết quả
            sendClaimResult(player, claimedCount,
                    claimedCount > 0
                            ? "Đã nhận " + claimedCount + " phần thưởng"
                            : "Không có phần thưởng để nhận");
        } catch (IOException e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    private void sendClaimResult(Player player, int claimedCount, String message) throws IOException {
        Message msg = new Message(Cmd_message.MAIL);
        msg.writer().writeByte(ACTION_CLAIM_REWARDS);
        msg.writer().writeByte(claimedCount);
        msg.writer().writeUTF(message);
        player.sendMessage(msg);
        msg.cleanup();
    }

    /**
     * Xóa mail
     */
    public void deleteMail(Player player, long mailId) {
        try {
            PlayerMail mail = mailService.getMailDetail(mailId);
            if (mail == null || mail.getPlayerId() != player.id) {
                sendDeleteResult(player, false, "Mail không tồn tại");
                return;
            }

            // Kiểm tra rewards: không cho xóa nếu còn reward chưa nhận (vàng, ngọc, item)
            List<MailReward> rewards = mailService.getRewardDAO().getRewardsByMailId(mailId);
            if (rewards != null) {
                for (MailReward reward : rewards) {
                    byte type = reward.getRewardType();
                    boolean isValuable = type == RewardType.ITEM.getCode()
                            || type == RewardType.GOLD.getCode()
                            || type == RewardType.GEM.getCode();
                    if (isValuable && !reward.isClaimed()) {
                        sendDeleteResult(player, false, "Không thể xóa thư còn vàng/ngọc/vật phẩm chưa nhận");
                        return;
                    }
                }
            }

            mailService.deleteMail(mailId);
            sendDeleteResult(player, true, "Đã xóa mail");
        } catch (Exception e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    private void sendDeleteResult(Player player, boolean success, String message) throws IOException {
        Message msg = new Message(Cmd_message.MAIL);
        msg.writer().writeByte(ACTION_DELETE);
        msg.writer().writeBoolean(success);
        msg.writer().writeUTF(message);
        player.sendMessage(msg);
        msg.cleanup();
    }

    /**
     * Gửi số lượng mail (push từ server)
     */
    public void sendMailCount(Player player) {
        try {
            int totalCount = mailService.getMailCount(player.id);

            Message msg = new Message(Cmd_message.MAIL);
            msg.writer().writeByte(ACTION_MAIL_COUNT);
            msg.writer().writeShort(totalCount);

            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    /**
     * Thông báo có mail mới (push từ server)
     */
    public void notifyNewMail(Player player, String title) {
        try {
            Message msg = new Message(Cmd_message.MAIL);
            msg.writer().writeByte(ACTION_NEW_MAIL_NOTIFY);
            msg.writer().writeUTF(title);

            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(MailHandler.class, e);
        }
    }

    /**
     * Áp dụng item options từ JSON data
     * Format: [{"id":30,"param":100},{"id":31,"param":200}]
     */
    private void applyOptionsData(item.Item item, String optionsData) {
        if (optionsData == null || optionsData.isEmpty()) {
            return;
        }
        try {
            Object parsed = JSONValue.parse(optionsData);
            if (parsed instanceof JSONArray arr) {
                for (Object obj : arr) {
                    if (obj instanceof JSONObject opt) {
                        int optId = ((Number) opt.get("id")).intValue();
                        int param = ((Number) opt.get("param")).intValue();
                        item.itemOptions.add(new item.Item.ItemOption(optId, param));
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Lỗi parse optionsData: " + e.getMessage() + "\n");
        }
    }

    /**
     * Gửi thông báo lỗi
     */
    private void sendError(Player player, String errorMsg) {
        Service.gI().sendThongBao(player, errorMsg);
    }

    public MailService getMailService() {
        return mailService;
    }

    // ============================================
    // Convenience methods (dùng cho programmatic API)
    // ============================================

    /**
     * Lấy danh sách mail của player
     */
    public List<PlayerMail> getMailList(long playerId) {
        return mailService.getPlayerMails(playerId);
    }

    /**
     * Lấy danh sách mail với phân trang
     */
    public List<PlayerMail> getMailListWithPaging(long playerId, int page, int pageSize) {
        return mailService.getPlayerMailsWithPaging(playerId, page, pageSize);
    }

    /**
     * Lấy chi tiết mail (kèm rewards)
     */
    public PlayerMail getMailDetail(long mailId) {
        return mailService.getMailDetail(mailId);
    }

    /**
     * Đánh dấu mail đã đọc
     */
    public void openMail(long mailId) {
        mailService.openMail(mailId);
    }

    /**
     * Lấy reward chưa nhận của mail
     */
    public List<MailReward> getUnclaimedRewards(long mailId) {
        return mailService.getRewardDAO().getUnclaimedRewardsByMailId(mailId);
    }

    /**
     * Nhận reward
     */
    public void claimReward(long rewardId) throws Exception {
        mailService.claimReward(rewardId);
    }

    /**
     * Nhận nhiều reward
     */
    public void claimRewards(List<Long> rewardIds) throws Exception {
        mailService.claimRewards(rewardIds);
    }

    /**
     * Xóa mail (không cần Player)
     */
    public void deleteMail(long mailId) {
        mailService.deleteMail(mailId);
    }
}
