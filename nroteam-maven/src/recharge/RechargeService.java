package recharge;

import data.AlyraManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import player.Player;
import recharge.RechargeModels.PackageReward;
import recharge.RechargeModels.PlayerChoice;
import recharge.RechargeModels.RechargePackage;
import recharge.RechargeModels.Settings;
import server.Client;
import mail.handler.MailHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

public final class RechargeService {
    private static final int MAIL_EXPIRE_DAYS = 30;
    private static final RechargeService INSTANCE = new RechargeService();

    private RechargeService() {}
    public static RechargeService gI() { return INSTANCE; }

    public long topUpMoney(PlayerChoice player, long cashAmount) throws Exception {
        if (player == null) throw new IllegalArgumentException("Chưa chọn người chơi");
        if (cashAmount <= 0) throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        Settings settings = RechargeRepository.getSettings();
        int diamonds = settings.diamondsFor(cashAmount);
        if (diamonds <= 0) throw new IllegalArgumentException("Số tiền quá nhỏ so với tỷ giá hiện tại");
        String content = "Nạp tiền thành công.\nSố tiền: " + formatMoney(cashAmount)
                + " VNĐ.\n";
        long mailId = deliver(player, "MONEY", cashAmount, diamonds, diamonds, null,
                "Nạp thành công", content, null);
        notifyOnline(player, "Nạp thành công");
        return mailId;
    }

    public long topUpPackage(PlayerChoice player, RechargePackage pack) throws Exception {
        if (player == null) throw new IllegalArgumentException("Chưa chọn người chơi");
        if (pack == null || pack.id <= 0) throw new IllegalArgumentException("Chưa chọn gói nạp");
        RechargePackage current = null;
        for (RechargePackage candidate : RechargeRepository.listPackages(true)) if (candidate.id == pack.id) current = candidate;
        if (current == null) throw new IllegalArgumentException("Gói không tồn tại hoặc đã tắt");
        int diamonds = current.rewards.stream().filter(r -> "GEM".equals(r.type)).mapToInt(r -> r.amount).sum();
        String content = "Bạn đã nhận gói: " + current.name + ".\nPhần thưởng được đính kèm trong thư.\nGói nạp không được tính điểm sự kiện.";
        long mailId = deliver(player, "PACKAGE", 0, 0, diamonds, current,
                "Nhận gói nạp " + current.name, content, snapshot(current));
        notifyOnline(player, "Bạn đã nhận gói nạp " + current.name);
        return mailId;
    }

    private long deliver(PlayerChoice player, String type, long cashAmount, int diamondBase, int diamondTotal,
                         RechargePackage pack, String title, String content, String snapshot) throws Exception {
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                long now = System.currentTimeMillis();
                long mailId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO player_mail(player_id,title,content,has_reward,is_read,status,created_at,expired_at) VALUES(?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, player.id); ps.setString(2, title); ps.setString(3, content); ps.setBoolean(4, true);
                    ps.setBoolean(5, false); ps.setByte(6, (byte) 1); ps.setTimestamp(7, new Timestamp(now));
                    ps.setTimestamp(8, new Timestamp(now + MAIL_EXPIRE_DAYS * 86_400_000L)); ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new IllegalStateException("Không tạo được thư nạp");
                        mailId = rs.getLong(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO mail_reward(mail_id,reward_type,reward_id,amount,claimed,options_data) VALUES(?,?,?,?,0,?)")) {
                    if ("MONEY".equals(type)) {
                        addReward(ps, mailId, (byte) 3, 0, diamondTotal, null);
                    } else {
                        for (PackageReward reward : pack.rewards) {
                            byte rewardType = "ITEM".equals(reward.type) ? (byte) 1 : "GEM".equals(reward.type) ? (byte) 3 : (byte) 2;
                            addReward(ps, mailId, rewardType, rewardType == 1 ? reward.itemId : 0, reward.amount, reward.optionsData);
                        }
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO recharge_history(player_id,player_name,recharge_type,cash_amount,diamond_base,diamond_total,"
                                + "package_id,package_name,event_points,mail_id,reward_snapshot,status) VALUES(?,?,?,?,?,?,?,?,?,?,?,'SUCCESS')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, player.id); ps.setString(2, player.name); ps.setString(3, type); ps.setLong(4, cashAmount);
                    ps.setInt(5, diamondBase); ps.setInt(6, diamondTotal);
                    if (pack == null) { ps.setNull(7, java.sql.Types.BIGINT); ps.setNull(8, java.sql.Types.VARCHAR); }
                    else { ps.setLong(7, pack.id); ps.setString(8, pack.name); }
                    ps.setInt(9, "MONEY".equals(type) ? diamondBase : 0); ps.setLong(10, mailId); ps.setString(11, snapshot);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new IllegalStateException("Không ghi được lịch sử nạp");
                    }
                }
                con.commit();
                return mailId;
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    private void addReward(PreparedStatement ps, long mailId, byte type, long rewardId, int amount, String options) throws Exception {
        if (amount <= 0) throw new IllegalArgumentException("Số lượng phần thưởng không hợp lệ");
        ps.setLong(1, mailId); ps.setByte(2, type); ps.setLong(3, rewardId); ps.setInt(4, amount);
        ps.setString(5, options == null || options.isBlank() ? null : options.trim()); ps.addBatch();
    }

    @SuppressWarnings("unchecked")
    private String snapshot(RechargePackage pack) {
        JSONArray result = new JSONArray();
        for (PackageReward r : pack.rewards) {
            JSONObject row = new JSONObject();
            row.put("type", r.type); row.put("item_id", r.itemId); row.put("amount", r.amount); row.put("options", r.optionsData);
            result.add(row);
        }
        return result.toJSONString();
    }

    private void notifyOnline(PlayerChoice player, String title) {
        Player online = Client.gI().getPlayer(player.id);
        if (online != null) MailHandler.gI().notifyNewMail(online, title);
    }

    public static String formatMoney(long value) { return String.format(java.util.Locale.US, "%,d", value).replace(',', '.'); }
}
