package services.top;
import data.AlyraManager;
import data.AlyraResultSet;
import java.io.IOException;
import network.Message;
import player.Player;
import utils.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TopWhisService {
    /**
     * Trao quà top Whis qua mail và reset top (gọi vào thứ 2 hàng tuần)
     */
    public void rewardTopWeekly() {
        // Số lượng ngọc thưởng cho từng hạng
        final int REWARD_TOP1 = 1000;
        final int REWARD_TOP2_5 = 500;
        final int REWARD_TOP6_10 = 200;
        final int GOLD_TOP1 = 500000000;
        final int GOLD_TOP2_5 = 200000000;
        final int GOLD_TOP6_10 = 100000000;
        final int MAIL_EXPIRE_DAYS = 7;
        try {
            List<TopRowService> topList = queryTop(10);
            for (int i = 0; i < topList.size(); i++) {
                TopRowService row = topList.get(i);
                int gem = 0;
                int gold = 0;
                if (i == 0) {
                    gem = REWARD_TOP1;
                    gold = GOLD_TOP1;
                } else if (i < 5) {
                    gem = REWARD_TOP2_5;
                    gold = GOLD_TOP2_5;
                } else {
                    gem = REWARD_TOP6_10;
                    gold = GOLD_TOP6_10;
                }
                String title = "Phần thưởng TOP Whis tuần";
                String content = "Chúc mừng bạn đạt TOP " + (i+1) + " luyện tập Whis tuần này! Đây là phần thưởng của bạn:";
                try {
                    mail.service.MailService mailService = new mail.service.MailService();
                    long mailId = mailService.sendMail(row.id, title, content, MAIL_EXPIRE_DAYS);
                    if (mailId > 0) {
                        java.util.List<mail.entity.MailReward> rewards = new java.util.ArrayList<>();
                        rewards.add(new mail.entity.MailReward(mailId, mail.enums.RewardType.GEM.getCode(), gem));
                        rewards.add(new mail.entity.MailReward(mailId, mail.enums.RewardType.GOLD.getCode(), gold));
                        String optionsData = "[{\"id\":30,\"param\":0}]";
                        rewards.add(new mail.entity.MailReward(mailId, mail.enums.RewardType.ITEM.getCode(), 457L, 1, optionsData));
                        mailService.getRewardDAO().saveRewards(rewards);
                        mailService.getMailDAO().updateHasReward(mailId, true);
                    }
                } catch (Exception ex) {
                    utils.Logger.logException(TopWhisService.class, ex, "Gửi mail phần thưởng top Whis thất bại");
                }
            }
            // Reset data_luyentap cho các player trong top
            for (TopRowService row : topList) {
                try {
                    String sql = "UPDATE player SET data_luyentap='[0,0,0,0,0,0,0,0]' WHERE id=" + row.id;
                    data.AlyraManager.executeUpdate(sql);
                } catch (Exception ex) {
                    utils.Logger.error("Reset data_luyentap for player id=" + row.id + ": " + ex);
                }
            }
        } catch (Exception e) {
            utils.Logger.error("TopWhisService.rewardTopWeekly error: " + e);
        }
    }

    private static TopWhisService instance;

    public static TopWhisService gI() {
        if (instance == null) {
            instance = new TopWhisService();
        }
        return instance;
    }

    public List<TopRowService> queryTop(int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            String sql = "SELECT id, name, head, gender, data_luyentap FROM player WHERE data_luyentap IS NOT NULL";
            AlyraResultSet rs = AlyraManager.executeQuery(sql);
            while (rs.next()) {
                String json = rs.getString("data_luyentap");
                if (json == null || json.isEmpty()) {
                    continue;
                }
                String[] parts = toArray(json);
                int levelIdx0 = (parts.length > 0) ? parseInt(parts[0]) : 0;
                int levelIdx5 = (parts.length > 5) ? parseInt(parts[5]) : 0;
                int level = Math.max(levelIdx0, levelIdx5);
                if (level <= 1) {
                    continue;
                }
                int time = (parts.length > 6) ? parseInt(parts[6]) : Integer.MAX_VALUE;
                long lastTime = (parts.length > 7) ? parseLong(parts[7]) : 0L;
                int gender = 0;
                try {
                    gender = rs.getInt("gender");
                } catch (Exception ignored) {
                }
                int body = (gender == 1) ? 59 : 57;
                int leg = (gender == 1) ? 60 : 58;
                list.add(new TopRowService(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("head"),
                        body,
                        leg,
                        lastTime,
                        null,
                        level,
                        (long) time,
                        null
                ));
            }
        } catch (Exception e) {
            Logger.error("TopWhisService.queryTop error: " + e);
        }

        list.sort(Comparator
                .comparingInt((TopRowService r) -> r.level != null ? r.level : 0).reversed()
                .thenComparingLong(r -> r.time != null ? r.time : Long.MAX_VALUE)
                .thenComparingLong((TopRowService r) -> r.lastTime));
        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
    }

    private String[] toArray(String jsonArr) {
        String s = jsonArr.trim();
        if (s.startsWith("[")) {
            s = s.substring(1);
        }
        if (s.endsWith("]")) {
            s = s.substring(0, s.length() - 1);
        }
        String[] tokens = s.split(",");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }

    private int parseInt(String v) {
        try {
            return Integer.parseInt(v.replaceAll("[^0-9-]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseLong(String v) {
        try {
            return Long.parseLong(v.replaceAll("[^0-9-]", ""));
        } catch (Exception e) {
            return 0L;
        }
    }

    public void showTop(Player viewer) {
        if (viewer == null) {
            return;
        }
        viewer.idMark.setMenuType(1);
        Message msg = null;
        try {
            List<TopRowService> list = queryTop(100);
            if (list.isEmpty()) {
                list = new ArrayList<>();
                list.add(new TopRowService(
                        (int) viewer.id,
                        viewer.name,
                        viewer.getHead(),
                        viewer.getBody(),
                        viewer.getLeg(),
                        viewer.traning != null ? viewer.traning.getLastTime() : System.currentTimeMillis(),
                        null,
                        viewer.traning != null ? viewer.traning.getTop() : 0,
                        viewer.traning != null ? (long) viewer.traning.getTime() : 0L,
                        null
                ));
            }
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100 Whis");
            msg.writer().writeByte(list.size());

            int rank = 1;
            for (TopRowService r : list) {
                msg.writer().writeInt(rank++);
                msg.writer().writeInt(r.id);
                short headShow = (short) r.head;
                short bodyShow = (short) r.body;
                short legShow = (short) r.leg;
                short[] parts = TopAppearanceCache.getAppearance(r.id, headShow, bodyShow, legShow);
                headShow = parts[0];
                bodyShow = parts[1];
                legShow = parts[2];
                msg.writer().writeShort(headShow);
                if (viewer.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(bodyShow);
                msg.writer().writeShort(legShow);
                msg.writer().writeUTF(r.name);
                String seconds = formatSeconds(r.time != null ? r.time.intValue() : 0);
                msg.writer().writeUTF("LV:" + (r.level != null ? r.level : 0) + " với " + seconds + " giây");
                msg.writer().writeUTF(relativeTime(r.lastTime));
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("TopWhisService.showTop error: " + e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private String formatSeconds(int ms) {
        String str = String.format(java.util.Locale.US, "%.1f", ms / 1000.0);
        return str.replace('.', ',');
    }

    private String relativeTime(long last) {
        if (last <= 0) {
            return "vừa xong";
        }
        long ms = System.currentTimeMillis() - last;
        if (ms < 0) {
            ms = 0;
        }
        long minutes = ms / (60_000L);
        long hours = ms / (3_600_000L);
        long days = ms / (86_400_000L);
        if (days > 0) {
            return days + " ngày trước";
        }
        if (hours > 0) {
            return hours + " giờ trước";
        }
        if (minutes > 0) {
            return minutes + " phút trước";
        }
        return "vừa xong";
    }
}
