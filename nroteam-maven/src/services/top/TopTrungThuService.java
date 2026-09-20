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
import daos.PlayerDAO;
import services.Service;
import services.ShopService;
import services.map.NpcService;
import utils.Logger;
import utils.TimeUtil;

public class TopTrungThuService {

    private static TopTrungThuService instance;

    public static TopTrungThuService gI() {
        if (instance == null) {
            instance = new TopTrungThuService();
        }
        return instance;
    }

    public int getRankVip(player.Player p) {
        return getRank(1, p.eventPointType1);
    }

    public int getRankLongDen(player.Player p) {
        return getRank(2, p.eventPointType2);
    }

    private static long parseTime(String s) {
        try {
            return TimeUtil.getTime(s, "dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            return 0L;
        }
    }

    public static final long EVENT_END = parseTime("28/09/2025 23:59");
    public static final long CLAIM_DEADLINE = parseTime("12/1/2026 23:59");

    public boolean isClaimOpen() {
        long now = System.currentTimeMillis();
        return now >= EVENT_END && now <= CLAIM_DEADLINE;
    }

    public void addVipPoint(Player p, int delta) {
        if (p == null) {
            return;
        }
        p.eventPointType1 += delta;
        p.eventTimeType1 = System.currentTimeMillis();
        Service.gI().sendThongBao(p, "Bạn nhận được +" + delta + " điểm Top Hộp quà Trung Thu VIP");
    }

    public void addLongDenPoint(Player p, int delta) {
        if (p == null) {
            return;
        }
        p.eventPointType2 += delta;
        p.eventTimeType2 = System.currentTimeMillis();
        Service.gI().sendThongBao(p, "Bạn nhận được +" + delta + " điểm Top Lồng đèn treo");
    }

    public void openPreviewVip(Player p) {
        String tagName = "ITEMS_TOP_REWARD_VIP";
        if (p.topRewards.containsKey(tagName)) {
            ShopService.gI().opendShop(p, tagName, true);
            return;
        }
        if (!isClaimOpen()) {
            return;
        }
        int rankVip = getRankVip(p);
        List<Item> items = TopRewardManager.gI().buildItems((byte) 1, rankVip);
        if (items != null && !items.isEmpty()) {
            p.topRewards.put(tagName, items);
            ShopService.gI().opendShop(p, tagName, true);
        }
    }

    public void openPreviewLongDen(Player p) {
        String tagName = "ITEMS_TOP_REWARD_LONGDEN";
        if (p.topRewards.containsKey(tagName)) {
            ShopService.gI().opendShop(p, tagName, true);
            return;
        }
        if (!isClaimOpen()) {
            return;
        }
        int rankLongDen = getRankLongDen(p);
        List<Item> items = TopRewardManager.gI().buildItems((byte) 2, rankLongDen);
        if (items != null && !items.isEmpty()) {
            p.topRewards.put(tagName, items);
            ShopService.gI().opendShop(p, tagName, true);
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
            Logger.error("TopTrungThuService.showTop error: " + e);
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
        int min = (type == 1 ? 20 : 10);
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
            int idx = (type == 1 ? 0 : 1);
            int min = (type == 1 ? 20 : 10);
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
                list.add(new TopRowService(id, name, head, body, leg, 0L, null, null, null,
                        null, point, 0L, lastPointTime));
            }
        } catch (Exception e) {
            Logger.error("TopTrungThuService.getTop error: " + e);
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
            int idx = (type == 1 ? 0 : 1);
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
            Logger.error("TopTrungThuService.getRank error: " + e);
        }
        return count + 1;
    }

    private int parsePointFromDataEvent(String json, int idx) {
        if (json == null) {
            return 0;
        }
        try {
            String s = json.trim();
            if (s.startsWith("[")) {
                s = s.substring(1);
            }
            if (s.endsWith("]")) {
                s = s.substring(0, s.length() - 1);
            }
            String[] parts = s.split(",");
            if (idx >= 0 && idx < parts.length) {
                String val = parts[idx].trim();
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                return Integer.parseInt(val);
            }
        } catch (Exception ignored) {
        }
        return 0;
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
        long days = (ms / (24L * 60 * 60 * 1000));
        long hours = (ms / (60L * 60 * 1000));
        if (months > 0) {
            return months + " tháng trước";
        } else if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else {
            return "vừa xong";
        }
    }

    private long parseTimeFromDataEvent(String json, int idx) {
        if (json == null) {
            return 0L;
        }
        try {
            String s = json.trim();
            if (s.startsWith("[")) {
                s = s.substring(1);
            }
            if (s.endsWith("]")) {
                s = s.substring(0, s.length() - 1);
            }
            String[] parts = s.split(",");
            int timeIdx = (idx == 1 ? 10 : 11);
            if (idx == 0) {
                timeIdx = 10;
            } else if (idx == 1) {
                timeIdx = 11;
            }
            if (timeIdx >= 0 && timeIdx < parts.length) {
                String val = parts[timeIdx].trim();
                if (val.startsWith("\"") && val.endsWith("\"")) {
                    val = val.substring(1, val.length() - 1);
                }
                return Long.parseLong(val);
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }
}
