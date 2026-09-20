package services.top;

import consts.ConstPlayer;
import data.AlyraManager;
import data.AlyraResultSet;
import item.Item;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import managers.TopRewardManager;
import network.Message;
import player.Player;
import services.Service;
import services.ShopService;
import services.map.NpcService;
import utils.Logger;
import utils.TimeUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

@SuppressWarnings("unchecked")
public class TopWomenService {

    private static TopWomenService instance;

    public static TopWomenService gI() {
        if (instance == null) {
            instance = new TopWomenService();
        }
        return instance;
    }

    public int getRankCapsule(Player p) {
        return getRank(1, p.eventPointType3);
    }

    public int getRankThiep(Player p) {
        return getRank(2, p.eventPointType4);
    }

    public int getRankHopQua(Player p) {
        return getRank(3, p.eventPointType5);
    }

    private static long parseTime(String s) {
        try {
            return TimeUtil.getTime(s, "dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            return 0L;
        }
    }

    public static final long EVENT_END = parseTime("27/10/2025 23:59");
    public static final long CLAIM_DEADLINE = parseTime("10/11/2025 23:59");

    public boolean isClaimOpen() {
        long now = System.currentTimeMillis();
        return now >= EVENT_END && now <= CLAIM_DEADLINE;
    }

    public void addCapsulePoint(Player p, int delta) {
        if (p == null || delta <= 0) {
            return;
        }
        p.eventPointType3 += delta;
        Service.gI().sendThongBao(p, "Bạn nhận được +" + delta + " điểm Top Capsule Trang Sức VIP");
        savePlayerEventData(p);
    }

    public void addThiepPoint(Player p, int delta) {
        if (p == null || delta <= 0) {
            return;
        }
        p.eventPointType4 += delta;
        Service.gI().sendThongBao(p, "Bạn nhận được +" + delta + " điểm Top Thiệp chúc VIP");

        savePlayerEventData(p);
    }

    public void addHopQuaPoint(Player p, int delta) {
        if (p == null || delta <= 0) {
            return;
        }
        p.eventPointType5 += delta;
        Service.gI().sendThongBao(p, "Bạn nhận được +" + delta + " điểm Top Hộp quà 20/10");
        savePlayerEventData(p);
    }

    private void savePlayerEventData(Player p) {
        try {
            // Build event data JSON array
            org.json.simple.JSONArray dataArray = new org.json.simple.JSONArray();
            dataArray.add(p.eventPointType1);
            dataArray.add(p.eventPointType2);
            dataArray.add(p.eventPointType3);
            dataArray.add(p.eventPointType4);
            dataArray.add(p.eventPointType5);
            dataArray.add(p.eventPointType6);
            dataArray.add(p.checkDailyReward);
            dataArray.add(p.checkTopReward1);
            dataArray.add(p.checkTopReward2);
            dataArray.add(p.checkTopReward3);
            dataArray.add(p.eventTimeType1);
            dataArray.add(p.eventTimeType2);
            dataArray.add(p.lastTrungThuDropDay);
            dataArray.add(p.lastTrungThuDropBoot);
            dataArray.add(p.lastHalloweenDropDay);
            dataArray.add(p.lastHalloweenDropBoot);
            dataArray.add(p.lastGemDropDay);
            dataArray.add(p.lastGemDropBoot);

            String dataEvent = dataArray.toJSONString();
            AlyraManager.executeUpdate(
                    "UPDATE player SET data_event = ? WHERE id = ?",
                    dataEvent,
                    p.id);
        } catch (Exception e) {
            Logger.error("TopWomenService.savePlayerEventData error: " + e);
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
                msg.writer().writeUTF(relativeTimeShort(lastVal));
                msg.writer().writeUTF((r.point != null ? r.point : 0) + " điểm");
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("TopWomenService.showTop error: " + e);
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
            NpcService.gI().createMenuConMeo(viewer, -1, -1, "Chưa đủ điểm vào TOP (ít nhất là " + min + " điểm)",
                    "Đóng");
            return;
        }
        int rank = getRank(type, point);
        NpcService.gI().createMenuConMeo(viewer, -1, -1, "TOP " + rank + " với " + point + " điểm", "OK");
    }

    private List<TopRowService> getTop(int type, int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            String sql = "SELECT p.id, p.name, p.head, p.gender, p.data_event FROM player p";
            AlyraResultSet rs = AlyraManager.executeQuery(sql);
            int idx = indexForType(type);
            int min = minForType(type);
            while (rs.next()) {
                int point = 0;
                long lastPointTime = 0L;
                try {
                    String json = rs.getString("data_event");
                    point = parsePointFromDataEvent(json, idx);
                    lastPointTime = parseTimeFromDataEvent(json, idx);
                } catch (Exception ignored) {
                }
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
                list.add(new TopRowService(id, name, head, body, leg, 0L, null, null, null, null, point, 0L,
                        lastPointTime));
            }
        } catch (Exception e) {
            Logger.error("TopWomenService.getTop error: " + e);
        }
        list.sort(Comparator.comparingInt((TopRowService r) -> r.point != null ? r.point : 0).reversed()
                .thenComparingInt(r -> r.id));
        if (list.size() > limit) {
            return new ArrayList<>(list.subList(0, limit));
        }
        return list;
    }

    private int getRank(int type, int point) {
        int count = 0;
        try {
            AlyraResultSet rs = AlyraManager.executeQuery("SELECT data_event FROM player");
            int idx = indexForType(type);
            while (rs.next()) {
                int p = 0;
                try {
                    String json = rs.getString("data_event");
                    p = parsePointFromDataEvent(json, idx);
                } catch (Exception ignored) {
                }
                if (p > point) {
                    count++;
                }
            }
        } catch (Exception e) {
            Logger.error("TopWomenService.getRank error: " + e);
        }
        return count + 1;
    }

    private int indexForType(int type) {
        return switch (type) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            default -> 2;
        };
    }

    private int minForType(int type) {
        return switch (type) {
            case 1 ->
                20;
            case 2 ->
                10;
            case 3 ->
                10;
            default ->
                10;
        };
    }

    private int parsePointFromDataEvent(String json, int idx) {
        if (json == null) {
            return 0;
        }
        try {
            JSONArray arr = (JSONArray) JSONValue.parse(json);
            if (arr == null) {
                return 0;
            }
            if (idx >= 0 && idx < arr.size()) {
                Object v = arr.get(idx);
                if (v instanceof Number number) {
                    return number.intValue();
                }
                if (v != null) {
                    return Integer.parseInt(v.toString());
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }

    private long parseTimeFromDataEvent(String json, int idx) {
        if (json == null) {
            return 0L;
        }
        try {
            JSONArray arr = (JSONArray) JSONValue.parse(json);
            if (arr == null) {
                return 0L;
            }
            int timeIdx = (idx == 0) ? 10 : (idx == 1) ? 11 : -1;
            if (timeIdx >= 0 && timeIdx < arr.size()) {
                Object v = arr.get(timeIdx);
                if (v instanceof Number number) {
                    return number.longValue();
                }
                if (v != null) {
                    return Long.parseLong(v.toString());
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return 0L;
    }

    private String relativeTimeShort(long last) {
        if (last <= 0) {
            return "vừa xong";
        }
        long ms = System.currentTimeMillis() - last;
        if (ms < 0) {
            ms = 0;
        }
        long months = ms / (30L * 24 * 60 * 60 * 1000);
        long days = ms / (24L * 60 * 60 * 1000);
        long hours = ms / (60L * 60 * 1000);
        if (months > 0) {
            return months + " tháng trước";
        }
        if (days > 0) {
            return days + " ngày trước";
        }
        if (hours > 0) {
            return hours + " giờ trước";
        }
        return "vừa xong";
    }
}
