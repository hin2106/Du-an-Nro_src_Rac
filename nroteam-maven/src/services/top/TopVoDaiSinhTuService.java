package services.top;
import data.AlyraManager;
import data.AlyraResultSet;
import network.Message;
import player.Player;
import utils.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TopVoDaiSinhTuService {

    private static TopVoDaiSinhTuService instance;

    public static TopVoDaiSinhTuService gI() {
        if (instance == null) {
            instance = new TopVoDaiSinhTuService();
        }
        return instance;
    }

    public List<TopRowService> queryTop(int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            String sql = "SELECT id, name, head, gender, "
                    + "CAST(JSON_EXTRACT(vodaisinhtu, '$[2]') AS UNSIGNED) AS lastTimePK "
                    + "FROM player "
                    + "WHERE vodaisinhtu IS NOT NULL "
                    + "AND JSON_EXTRACT(vodaisinhtu, '$[1]') > 0 "
                    + "ORDER BY CAST(JSON_EXTRACT(vodaisinhtu, '$[1]') AS UNSIGNED) DESC "
                    + "LIMIT " + limit;

            AlyraResultSet rs = AlyraManager.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int head = rs.getInt("head");
                int gender = 0;
                try {
                    gender = rs.getInt("gender");
                } catch (Exception ignored) {
                }

                int body = (gender == 1) ? 59 : 57;
                int leg = (gender == 1) ? 60 : 58;
                long lastTimePK = 0;

                try {
                    Object v = rs.getObject("lastTimePK");
                    if (v instanceof Number n) {
                        lastTimePK = n.longValue();
                    }
                } catch (Exception ignored) {
                }

                short[] parts = TopAppearanceCache.getAppearance(id, (short) head, (short) body, (short) leg);
                list.add(new TopRowService(id, name, parts[0], parts[1], parts[2], lastTimePK, 0L, null, null, null));
            }
        } catch (Exception e) {
            Logger.error("TopVoDaiSinhTuService.queryTop error: " + e);
        }
        return list;
    }

    public void showTop(Player viewer) {
        if (viewer == null) {
            return;
        }
        Message msg = null;
        try {
            List<TopRowService> list = queryTop(100);
            if (list == null) {
                list = new ArrayList<>();
            }
            if (list.isEmpty()) {
                list.add(new TopRowService(
                        (int) viewer.id,
                        viewer.name,
                        viewer.getHead(),
                        viewer.getBody(),
                        viewer.getLeg(),
                        viewer.lastTimePKVoDaiSinhTu,
                        0L,
                        null, null, null
                ));
            }

            viewer.idMark.setMenuType(1);
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100");
            msg.writer().writeByte(list.size());

            int rank = 1;
            for (TopRowService r : list) {
                msg.writer().writeInt(rank);
                msg.writer().writeInt(r.id);
                msg.writer().writeShort((short) r.head);
                if (viewer.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort((short) r.body);
                msg.writer().writeShort((short) r.leg);
                msg.writer().writeUTF(r.name);
                msg.writer().writeUTF(relativeTimeShort(r.lastTime));
                msg.writer().writeUTF("...");
                rank++;
            }

            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("TopVoDaiSinhTuService.showTop error: " + e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private String relativeTimeShort(long last) {
        if (last <= 0) {
            return "vừa xong";
        }
        long ms = System.currentTimeMillis() - last;
        if (ms < 0) {
            ms = 0;
        }
        long minutes = ms / 60_000L;
        long hours = ms / 3_600_000L;
        long days = ms / 86_400_000L;

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
