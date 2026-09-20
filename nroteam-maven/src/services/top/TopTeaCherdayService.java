package services.top;

import consts.ConstPlayer;
import data.AlyraManager;
import data.AlyraResultSet;
import item.Item;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import managers.TopRewardManager;
import network.Message;
import player.Player;
import services.Service;
import services.ShopService;
import services.map.NpcService;
import utils.Functions;
import utils.Logger;
import utils.Util;

public class TopTeaCherdayService {

    private static TopTeaCherdayService instance;
    private final Map<Long, int[]> pendingPoints = new ConcurrentHashMap<>();
    private final Map<Long, long[]> lastUpdateTime = new ConcurrentHashMap<>();

    public static TopTeaCherdayService gI() {
        if (instance == null) {
            instance = new TopTeaCherdayService();
        }
        return instance;
    }

    public int getRankHopQuaVip(Player p) {
        return getRank(1, p.eventPointType9);
    }

    public int getRankHopTraHoaCuc(Player p) {
        return getRank(2, p.eventPointType10);
    }

    public int getRankHopQua2011(Player p) {
        return getRank(3, p.eventPointType11);
    }

    public static final long EVENT_END = Util.parseTime("30/11/2025 23:59");
    public static final long CLAIM_DEADLINE = Util.parseTime("14/12/2025 23:59");

    public boolean isClaimOpen() {
        long now = System.currentTimeMillis();
        return now >= EVENT_END && now <= CLAIM_DEADLINE;
    }

    public void addHopQuaVipPoint(Player p, int delta) {
        if (p == null) {
            return;
        }
        p.eventPointType9 += delta;
        p.eventTimeType9 = System.currentTimeMillis();
        long playerId = p.id;
        pendingPoints.computeIfAbsent(playerId, k -> new int[3])[0] += delta;
        lastUpdateTime.computeIfAbsent(playerId, k -> new long[3])[0] = System.currentTimeMillis();
        new Thread(() -> {
            Functions.sleep(2000);
            long[] times = lastUpdateTime.get(playerId);
            if (times != null && System.currentTimeMillis() - times[0] >= 2000) {
                int[] points = pendingPoints.get(playerId);
                if (points != null && points[0] > 0) {
                    Service.gI().sendThongBao(p, "Bạn nhận được +" + points[0] + " điểm Top Hộp quà 20-11 VIP");
                    points[0] = 0;
                }
            }
        }).start();
        try {
            AlyraManager.executeUpdate(
                    "UPDATE player SET data_event = JSON_SET("
                    + "IF(JSON_VALID(data_event), data_event, JSON_ARRAY()), "
                    + "'$[8]', CAST(COALESCE(JSON_UNQUOTE(JSON_EXTRACT(data_event, '$[8]')), '0') AS UNSIGNED) "
                    + "+ ?, " + "'$[23]', ?" + ") WHERE id = ?",
                    delta, p.eventTimeType9, p.id);
        } catch (Exception e) {
            Logger.error("Top20_11Service.addHopQuaVipPoint persist error: " + e);
        }
    }

    public void addHopTraHoaCucPoint(Player p, int delta) {
        if (p == null) {
            return;
        }
        p.eventPointType10 += delta;
        p.eventTimeType10 = System.currentTimeMillis();
        long playerId = p.id;
        pendingPoints.computeIfAbsent(playerId, k -> new int[3])[1] += delta;
        lastUpdateTime.computeIfAbsent(playerId, k -> new long[3])[1] = System.currentTimeMillis();
        new Thread(() -> {
            Functions.sleep(2000);
            long[] times = lastUpdateTime.get(playerId);
            if (times != null && System.currentTimeMillis() - times[1] >= 2000) {
                int[] points = pendingPoints.get(playerId);
                if (points != null && points[1] > 0) {
                    Service.gI().sendThongBao(p, "Bạn nhận được +" + points[1] + " điểm Top Hộp trà hoa cúc");
                    points[1] = 0;
                }
            }
        }).start();
        try {
            AlyraManager.executeUpdate(
                    "UPDATE player SET data_event = JSON_SET("
                    + "IF(JSON_VALID(data_event), data_event, JSON_ARRAY()), "
                    + "'$[9]', CAST(COALESCE(JSON_UNQUOTE(JSON_EXTRACT(data_event, '$[9]')), '0') AS UNSIGNED) "
                    + "+ ?, " + "'$[24]', ?" + ") WHERE id = ?",
                    delta, p.eventTimeType10, p.id);
        } catch (Exception e) {
            Logger.error("Top20_11Service.addHopTraHoaCucPoint persist error: " + e);
        }
    }

    public void addHopQua2011Point(Player p, int delta) {
        if (p == null) {
            return;
        }
        p.eventPointType11 += delta;
        p.eventTimeType11 = System.currentTimeMillis();
        long playerId = p.id;
        pendingPoints.computeIfAbsent(playerId, k -> new int[3])[2] += delta;
        lastUpdateTime.computeIfAbsent(playerId, k -> new long[3])[2] = System.currentTimeMillis();
        new Thread(() -> {
            Functions.sleep(2000);
            long[] times = lastUpdateTime.get(playerId);
            if (times != null && System.currentTimeMillis() - times[2] >= 2000) {
                int[] points = pendingPoints.get(playerId);
                if (points != null && points[2] > 0) {
                    Service.gI().sendThongBao(p, "Bạn nhận được +" + points[2] + " điểm Top Hộp quà 20-11");
                    points[2] = 0;
                }
            }
        }).start();
        try {
            AlyraManager.executeUpdate(
                    "UPDATE player SET data_event = JSON_SET("
                    + "IF(JSON_VALID(data_event), data_event, JSON_ARRAY()), "
                    + "'$[10]', CAST(COALESCE(JSON_UNQUOTE(JSON_EXTRACT(data_event, '$[10]')), '0') AS UNSIGNED) "
                    + "+ ?, " + "'$[25]', ?" + ") WHERE id = ?",
                    delta, p.eventTimeType11, p.id);
        } catch (Exception e) {
            Logger.error("Top20_11Service.addHopQua2011Point persist error: " + e);
        }
    }

    public void openPreview(Player player, String tagName, byte board, int rank) {
        if (player == null) {
            return;
        }
        if (player.topRewards.containsKey(tagName)) {
            ShopService.gI().opendShop(player, tagName, true);
            return;
        }
        if (!isClaimOpen()) {
            return;
        }
        List<Item> items = TopRewardManager.gI().buildItems(board, rank);
        if (items != null && !items.isEmpty()) {
            player.topRewards.put(tagName, items);
            ShopService.gI().opendShop(player, tagName, true);
        }
    }

    public void showTop(Player viewer, int type) {
        if (viewer == null) {
            return;
        }
        viewer.idMark.setMenuType(1);
        Message msg = null;
        try {
            List<TopRowService> list = getTop(type, 100);
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100");
            msg.writer().writeByte(list.size());

            int rank = 1;
            for (var r : list) {
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
                Long last = (r.lastPointTime != null && r.lastPointTime > 0) ? r.lastPointTime : r.lastTimeOnline;
                long lastVal = (last != null) ? last : 0L;
                msg.writer().writeUTF(Util.relativeTimeShort(lastVal));
                msg.writer().writeUTF((r.point != null ? r.point : 0) + " điểm");
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("Top20_11Service.showTop error: " + e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void showSelfRank(Player viewer, int type, int point) {
        if (viewer == null) {
            return;
        }
        int min = minForType(type);
        if (point < min) {
            NpcService.gI().createMenuConMeo(viewer, -1, -1, "Chưa đủ điểm vào TOP (ít nhất là " + min + " điểm)", "Đóng");
            return;
        }
        int rank = getRank(type, point);
        NpcService.gI().createMenuConMeo(viewer, -1, -1, "TOP " + rank + " với " + point + " điểm", "OK");
    }

    private List<TopRowService> getTop(int type, int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            int idx = indexForType(type);
            int min = minForType(type);
            int timeIdx = timeIndexForType(type);
            String sql = "SELECT p.id, p.name, p.head, p.gender, "
                    + "COALESCE(CAST(JSON_UNQUOTE(JSON_EXTRACT(p.data_event, '$[" + idx + "]')) AS UNSIGNED), 0) AS point, "
                    + "COALESCE(CAST(JSON_UNQUOTE(JSON_EXTRACT(p.data_event, '$[" + timeIdx + "]')) AS UNSIGNED), 0) AS lastPointTime "
                    + "FROM player p";
            AlyraResultSet rs = AlyraManager.executeQuery(sql);
            while (rs.next()) {
                int point = rs.getInt("point");
                long lastPointTime = rs.getLong("lastPointTime");
                if (point < min) {
                    continue;
                }

                int id = rs.getInt("id");
                String name = rs.getString("name");
                int head = rs.getInt("head");
                int gender = 0;
                try {
                    gender = rs.getInt("gender");
                } catch (Exception ignored) {
                }
                int body = (gender == ConstPlayer.NAMEC ? 59 : 57);
                int leg = (gender == ConstPlayer.NAMEC ? 60 : 58);
                list.add(new TopRowService(id, name, head, body, leg, 0L, null, null, null, null, point, 0L, lastPointTime));
            }
        } catch (Exception e) {
            Logger.error("Top20_11Service.getTop error: " + e);
        }
        list.sort(Comparator.comparingInt((TopRowService r) -> r.point != null ? r.point : 0).reversed().thenComparingInt(r -> r.id));
        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
    }

    private int getRank(int type, int point) {
        int count = 0;
        try {
            int idx = indexForType(type);
            String sql = "SELECT COUNT(*) AS c FROM player WHERE CAST(JSON_UNQUOTE(JSON_EXTRACT(data_event, '$[" + idx + "]')) AS UNSIGNED) > " + point;
            AlyraResultSet rs = AlyraManager.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt("c");
            }
        } catch (Exception e) {
            Logger.error("Top20_11Service.getRank error: " + e);
        }
        return count + 1;
    }

    private int indexForType(int type) {
        return switch (type) {
            case 1 -> 8;
            case 2 -> 9;
            case 3 -> 10;
            default -> 8;
        };
    }

    private int timeIndexForType(int type) {
        return switch (type) {
            case 1 -> 23;
            case 2 -> 24;
            case 3 -> 25;
            default -> 23;
        };
    }

    private int minForType(int type) {
        return switch (type) {
            case 1 ->
                10;
            case 2 ->
                10;
            case 3 ->
                10;
            default ->
                10;
        };
    }
}
