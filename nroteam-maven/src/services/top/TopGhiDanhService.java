package services.top;
import data.AlyraManager;
import data.AlyraResultSet;
import consts.ConstPlayer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import network.Message;
import player.Player;
import utils.Logger;
import services.map.NpcService;

public class TopGhiDanhService {

    private static TopGhiDanhService instance;

    public static TopGhiDanhService gI() {
        if (instance == null) {
            instance = new TopGhiDanhService();
        }
        return instance;
    }

    private List<TopRowService> getTopByGender(byte gender, int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            String sql = "SELECT p.id, p.name, p.head, p.gender, p.ghi_danh_point, p.ghi_danh_point_time "
                    + "FROM player p WHERE p.gender = ? AND p.ghi_danh_point > 9 ORDER BY p.ghi_danh_point DESC LIMIT ?";
            AlyraResultSet rs = AlyraManager.executeQuery(sql, gender, limit);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int head = rs.getInt("head");
                int body = (gender == ConstPlayer.NAMEC ? 59 : 57);
                int leg = (gender == ConstPlayer.NAMEC ? 60 : 58);
                int point = 0;
                try {
                    point = rs.getInt("ghi_danh_point");
                } catch (Exception ignored) {
                }
                long lastPointTime = 0L;
                try {
                    String ts = rs.getString("ghi_danh_point_time");
                    if (ts != null && !ts.isEmpty()) {
                        Object obj = rs.getObject("ghi_danh_point_time");
                        if (obj instanceof java.sql.Timestamp t) {
                            lastPointTime = t.getTime();
                        }
                    }
                } catch (Exception ignored) {
                }
                list.add(new TopRowService(id, name, head, body, leg, 0L, null, null, null, null, point, 0L, lastPointTime));
            }
        } catch (Exception e) {
            Logger.error("TopGhiDanhService.getTopByGender error: " + e);
        }
        return list;
    }

    private int getRankByGender(byte gender, int point) {
        try {
            String sql = "SELECT COUNT(*) AS c FROM player WHERE gender = ? AND ghi_danh_point > ?";
            AlyraResultSet rs = AlyraManager.executeQuery(sql, gender, point);
            if (rs.next()) {
                int c = 0;
                try {
                    Object val = rs.getObject("c");
                    if (val instanceof Number n) {
                        c = n.intValue();
                    } else {
                        String s = rs.getString("c");
                        if (s != null && !s.isEmpty()) {
                            c = Integer.parseInt(s.replaceAll("[^0-9-]", ""));
                        }
                    }
                } catch (Exception ignored) {
                }
                return c + 1;
            }
        } catch (Exception e) {
            Logger.error("TopGhiDanhService.getRankByGender error: " + e);
        }
        return 0;
    }

    public void showSelfRank(Player viewer) {
        if (viewer == null) {
            return;
        }
        try {
            AlyraResultSet rs = AlyraManager.executeQuery("SELECT gender, ghi_danh_point FROM player WHERE id = ?", (int) viewer.id);
            if (rs.next()) {
                byte gender = (byte) rs.getInt("gender");
                int point = 0;
                try {
                    point = rs.getInt("ghi_danh_point");
                } catch (Exception ignored) {
                }
                if (point < 10) {
                    NpcService.gI().createMenuConMeo(viewer, -1, -1, "Chưa đủ điểm vào TOP (ít nhất là 10 điểm)", "OK");
                } else {
                    int rank = getRankByGender(gender, point);
                    NpcService.gI().createMenuConMeo(viewer, -1, -1, "TOP " + rank + " với " + point + " điểm", "OK");
                }
            }
        } catch (Exception e) {
            Logger.error("TopGhiDanhService.showSelfRank error: " + e);
        }
    }

    public void showTopByGender(Player viewer, byte gender) {
        if (viewer == null) {
            return;
        }
        viewer.idMark.setMenuType(1);
        Message msg = null;
        try {
            List<TopRowService> list = getTopByGender(gender, 100);

            msg = new Message(-96);
            msg.writer().writeByte(0);
            String title;
            switch (gender) {
                case ConstPlayer.TRAI_DAT ->
                    title = "Top 100 Trái đất";
                case ConstPlayer.NAMEC ->
                    title = "Top 100 Namếc";
                default ->
                    title = "Top 100 Xayda";
            }
            msg.writer().writeUTF(title);
            msg.writer().writeByte(list.size());

            int rank = 1;
            for (var p : list) {
                int r = rank++;
                msg.writer().writeInt(r);
                msg.writer().writeInt(p.id);
                short headShow = (short) p.head;
                short bodyShow = (short) p.body;
                short legShow = (short) p.leg;
                short[] parts = TopAppearanceCache.getAppearance(p.id, headShow, bodyShow, legShow);
                headShow = parts[0];
                bodyShow = parts[1];
                legShow = parts[2];
                msg.writer().writeShort(headShow);
                if (viewer.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(bodyShow);
                msg.writer().writeShort(legShow);
                msg.writer().writeUTF(p.name);
                String timeStr = "vừa xong";
                Long last = (p.lastPointTime != null && p.lastPointTime > 0) ? p.lastPointTime : p.lastTimeOnline;
                if (last != null && last > 0) {
                    long ms = System.currentTimeMillis() - last;
                    if (ms < 0) {
                        ms = 0;
                    }
                    long months = ms / (30L * 24 * 60 * 60 * 1000);
                    long days = (ms / (24L * 60 * 60 * 1000));
                    long hours = (ms / (60L * 60 * 1000));
                    if (months > 0) {
                        timeStr = months + " tháng trước";
                    } else if (days > 0) {
                        timeStr = days + " ngày trước";
                    } else if (hours > 0) {
                        timeStr = hours + " giờ trước";
                    } else {
                        timeStr = "vừa xong";
                    }
                }
                msg.writer().writeUTF(timeStr);
                msg.writer().writeUTF((p.point != null ? p.point : 0) + " điểm");
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("Error showTopByGender GhiDanh: ");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public static void updatePointWithTime(int playerId, int point) {
        try {
            AlyraManager.executeUpdate("UPDATE player SET ghi_danh_point = ?, ghi_danh_point_time = NOW() WHERE id = ?", point, playerId);
        } catch (Exception e) {
            Logger.logException(TopGhiDanhService.class, e);
        }
    }
}
