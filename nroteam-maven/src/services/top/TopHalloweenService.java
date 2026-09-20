package services.top;

import consts.ConstPlayer;
import data.AlyraManager;
import data.AlyraResultSet;
import network.Message;
import player.Player;
import services.Service;
import services.map.NpcService;
import utils.Logger;
import managers.TopRewardManager;
import services.ShopService;
import utils.TimeUtil;
import item.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TopHalloweenService {

    private static TopHalloweenService instance;

    public static TopHalloweenService gI() {
        if (instance == null) {
            instance = new TopHalloweenService();
        }
        return instance;
    }
    private static long parseTime(String s) {
        try {
            return TimeUtil.getTime(s, "dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            return 0L;
        }
    }

    public static final long EVENT_END = parseTime("31/10/2025 23:59");
    public static final long CLAIM_DEADLINE = parseTime("30/11/2025 23:59");

    public boolean isClaimOpen() {
        long now = System.currentTimeMillis();
        return now >= EVENT_END && now <= CLAIM_DEADLINE;
    }

    public void openPreviewCandy(Player p) {
        String tagName = "ITEMS_TOP_REWARD_HALLOWEEN_CANDY";
        if (p.topRewards.containsKey(tagName)) {
            ShopService.gI().opendShop(p, tagName, true);
            return;
        }
        if (!isClaimOpen()) {
            return;
        }
        int rank = getRank(1, p.eventPointType3);
        List<Item> items = TopRewardManager.gI().buildItems((byte) 3, rank);
        if (items != null && !items.isEmpty()) {
            p.topRewards.put(tagName, items);
            ShopService.gI().opendShop(p, tagName, true);
        }
    }

    public void openPreviewCard(Player p) {
        String tagName = "ITEMS_TOP_REWARD_HALLOWEEN_CARD";
        if (p.topRewards.containsKey(tagName)) {
            ShopService.gI().opendShop(p, tagName, true);
            return;
        }
        if (!isClaimOpen()) {
            return;
        }
        int rank = getRank(2, p.eventPointType4);
        List<Item> items = TopRewardManager.gI().buildItems((byte) 4, rank);
        if (items != null && !items.isEmpty()) {
            p.topRewards.put(tagName, items);
            ShopService.gI().opendShop(p, tagName, true);
        }
    }

    // Type 1: Top Hộp kẹo Ma quỷ -> dùng eventPointType3 (index 2)
    // Type 2: Top Thiệp Halloween -> dùng eventPointType4 (index 3)

    public void addCandyPoint(Player p, int delta) {
        if (p == null) return;
        p.eventPointType3 += delta;
        Service.gI().sendThongBao(p, "Bạn nhận được +" + delta + " điểm Top Hộp kẹo Ma quỷ");
    }

    public void addCardPoint(Player p, int delta) {
        if (p == null) return;
        p.eventPointType4 += delta;
        Service.gI().sendThongBao(p, "Bạn nhận được +" + delta + " điểm Top Thiệp Halloween");
    }

    public void showTop(Player viewer, int type) {
        if (viewer == null) return;
        viewer.idMark.setMenuType(1);
        Message msg = null;
        try {
            List<TopRowService> list = getTop(type, 100);
            if (list == null || list.isEmpty()) {
                NpcService.gI().createMenuConMeo(viewer, -1, -1, "Chưa có người đủ điểm vào TOP", "OK");
                return;
            }
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
                long lastVal = (r.lastTimeOnline != null ? r.lastTimeOnline : 0L);
                msg.writer().writeUTF(relativeTimeShort(lastVal));
                msg.writer().writeUTF((r.point != null ? r.point : 0) + " điểm");
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("TopHalloweenService.showTop error: " + e);
        } finally {
            if (msg != null) msg.cleanup();
        }
    }

    public void showSelfRank(Player viewer, int type, int point) {
        if (viewer == null) return;
        int min = (type == 1 ? 20 : 10);
        if (point < min) {
            NpcService.gI().createMenuConMeo(viewer, -1, -1, "Chưa đủ điểm vào TOP (ít nhất là " + min + " điểm)", "OK");
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
            int idx = (type == 1 ? 2 : 3);
            int min = (type == 1 ? 20 : 10);
            while (rs.next()) {
                int point = 0;
                try {
                    String json = rs.getString("data_event");
                    point = parsePointFromDataEvent(json, idx);
                } catch (Exception ignored) {}
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int head = rs.getInt("head");
                int gender = 0;
                try { gender = rs.getInt("gender"); } catch (Exception ignored) {}
                int body = (gender == ConstPlayer.NAMEC ? 59 : 57);
                int leg = (gender == ConstPlayer.NAMEC ? 60 : 58);
                long lastOnline = 0L;
                if (point >= min) {
                    list.add(new TopRowService(id, name, head, body, leg, 0L, null, null, null, null, point, lastOnline, 0L));
                }
            }
            try {
                java.util.List<player.Player> onlines = server.Client.gI().getPlayers();
                if (onlines != null) {
                    java.util.Map<Integer, TopRowService> map = new java.util.HashMap<>();
                    for (TopRowService r : list) map.put(r.id, r);
                    for (player.Player p : onlines) {
                        if (p == null) continue;
                        int point = (type == 1 ? p.eventPointType3 : p.eventPointType4);
                        if (point < min) continue;
                        TopRowService row = map.get((int) p.id);
                        int body = (p.gender == ConstPlayer.NAMEC ? 59 : 57);
                        int leg = (p.gender == ConstPlayer.NAMEC ? 60 : 58);
                        if (row == null) {
                            row = new TopRowService((int) p.id, p.name, p.head, body, leg, 0L, null, null, null, null, point, 0L, 0L);
                            map.put((int) p.id, row);
                        } else {
                            // TopRowService fields are final; replace the entry with a new object
                            row = new TopRowService((int) p.id, p.name, p.head, body, leg, 0L, null, null, null, null, point, 0L, 0L);
                            map.put((int) p.id, row);
                        }
                    }
                    list = new ArrayList<>(map.values());
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            Logger.error("TopHalloweenService.getTop error: " + e);
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
            int idx = (type == 1 ? 2 : 3);
            while (rs.next()) {
                int p = 0;
                try {
                    String json = rs.getString("data_event");
                    p = parsePointFromDataEvent(json, idx);
                } catch (Exception ignored) {}
                if (p > point) count++;
            }
        } catch (Exception e) {
            Logger.error("TopHalloweenService.getRank error: " + e);
        }
        return count + 1;
    }

    private int parsePointFromDataEvent(String json, int idx) {
        if (json == null) return 0;
        try {
            String s = json.trim();
            if (s.startsWith("[")) s = s.substring(1);
            if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
            String[] parts = s.split(",");
            if (idx >= 0 && idx < parts.length) {
                String val = parts[idx].trim();
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                return Integer.parseInt(val);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private String relativeTimeShort(long last) {
        if (last <= 0) return "vừa xong";
        long ms = System.currentTimeMillis() - last;
        if (ms < 0) ms = 0;
        long days = (ms / (24L * 60 * 60 * 1000));
        long hours = (ms / (60L * 60 * 1000));
        if (days > 0) return days + " ngày trước";
        if (hours > 0) return hours + " giờ trước";
        return "vừa xong";
    }
}

