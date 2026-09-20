package services.top;
import data.AlyraManager;
import data.AlyraResultSet;
import network.Message;
import utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import player.Player;

public class TopCDRDService {

    private static TopCDRDService instance;

    private TopCDRDService() {
    }

    public static TopCDRDService gI() {
        if (instance == null) {
            instance = new TopCDRDService();
        }
        return instance;
    }

    public List<TopRowService> queryTop(int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            String sql = "SELECT id, name, thanhtichcdrd, thongtin FROM clan WHERE thanhtichcdrd IS NOT NULL";
            AlyraResultSet rs = AlyraManager.executeQuery(sql);
            while (rs.next()) {
                String tt = rs.getString("thanhtichcdrd");
                if (tt == null || tt.isEmpty()) {
                    continue;
                }

                long[] parsed = parseThanhtichStrict(tt);
                if (parsed == null) {
                    continue;
                }

                int level = (int) parsed[0];
                long time = parsed[1];
                long lastEpoch = parsed.length > 2 ? parsed[2] : 0L;

                if (lastEpoch > 0 && lastEpoch < 10_000_000_000L) {
                    lastEpoch *= 1000L;
                }

                if (level <= 0 || time <= 0) {
                    continue;
                }

                int head = 57, body = 57, leg = 58;
                String thongTinLeader = rs.getString("thongtin");
                int leaderId = -1;
                String leaderName = "Bang chủ";
                long lastTime = (lastEpoch > 0L) ? lastEpoch : System.currentTimeMillis();

                if (thongTinLeader != null && !thongTinLeader.isEmpty()) {
                    String[] info = toArray(thongTinLeader);
                    if (info.length >= 5) {
                        leaderId = parseInt(info[0]);
                        leaderName = info[1].replace("[", "").replace("]", "").trim();
                        head = parseInt(info[2]);
                        body = parseInt(info[3]);
                        leg = parseInt(info[4]);
                    }
                }

                short[] parts = TopAppearanceCache.getAppearance(leaderId, (short) head, (short) body, (short) leg);
                list.add(new TopRowService(
                        rs.getInt("id"),
                        rs.getString("name"),
                        parts[0],
                        parts[1],
                        parts[2],
                        lastTime,
                        null,
                        level,
                        time,
                        leaderName
                ));
            }
        } catch (Exception e) {
            Logger.error("TopConDuongRanDocService.queryTop error: " + e);
        }
        list.sort(Comparator.comparingInt((TopRowService r) -> r.level != null ? r.level : 0).reversed().thenComparingLong(r -> r.time != null ? r.time : 0L));
        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
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
                String clanName = (viewer.clan != null) ? viewer.clan.name : "Bang hội";
                list.add(new TopRowService(0, clanName, viewer.getHead(), viewer.getBody(), viewer.getLeg(), System.currentTimeMillis(), null, 0, 0L, viewer.name));
            }
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100 Con Đường Rắn Độc");
            msg.writer().writeByte(list.size());
            int rank = 1;
            for (TopRowService r : list) {
                msg.writer().writeInt(rank++);
                msg.writer().writeInt(r.id);
                short headShow = (short) r.head;
                short bodyShow = (short) r.body;
                short legShow = (short) r.leg;
                msg.writer().writeShort(headShow);
                if (viewer.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(bodyShow);
                msg.writer().writeShort(legShow);
                msg.writer().writeUTF(r.name);
                msg.writer().writeUTF("Lv: " + (r.level != null ? r.level : 0) + " (" + relativeTimeShort(r.lastTime) + ")");
                msg.writer().writeUTF("Bang chủ " + (r.leaderName != null ? r.leaderName : "") + "\n[" + formatMillisPretty(r.time != null ? r.time : 0L) + "]");
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("TopConDuongRanDocService.showTop error: " + e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
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
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseLong(String v) {
        try {
            return Long.parseLong(v.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private long[] parseThanhtichStrict(String value) {
        try {
            String s = value == null ? "" : value.replaceAll("\\s+", "");
            if ("[0,0]".equals(s) || "[0]".equals(s) || "[]".equals(s)) {
                return null;
            }

            String[] tokens = toArray(value);
            if (tokens.length < 2) {
                return null;
            }

            int level = parseInt(tokens[0]);
            long time = parseLong(tokens[1]);
            long last = 0L;

            if (tokens.length > 2) {
                last = parseLong(tokens[2]);
                if (last > 0 && last < 10_000_000_000L) {
                    last *= 1000L;
                }
            }

            if (level <= 0 && time <= 0) {
                return null;
            }

            return (tokens.length > 2) ? new long[]{level, time, last} : new long[]{level, time};
        } catch (Exception ignored) {
        }
        return null;
    }

    private String formatMillisPretty(long ms) {
        if (ms <= 0) {
            return "—";
        }
        long seconds = ms / 1000L;
        long m = seconds / 60L;
        long s = seconds % 60L;
        return m + " p " + s + " s";
    }

    private String relativeTimeShort(long last) {
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

        if (days >= 30) {
            long months = days / 30;
            return months + " tháng trước";
        }
        if (days > 0) {
            return days + "n trước";
        }
        if (hours > 0) {
            return hours + "g trước";
        }
        if (minutes > 0) {
            return minutes + "p trước";
        }
        return "vừa xong";
    }
}
