package services.top;
import data.AlyraManager;
import data.AlyraResultSet;
import network.Message;
import player.Player;
import utils.Logger;
import utils.TimeUtil;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TopSucManhService {

    private static TopSucManhService instance;

    private static final long MIN_POWER_FOR_TOP = 5_000_000L;
    private static final long CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(5); // Cache 5 phút

    // Cache cho top ranking
    private List<TopRowService> cachedTopList = null;
    private long lastCacheUpdate = 0;
    private final Object cacheLock = new Object();

    public static TopSucManhService gI() {
        if (instance == null) {
            instance = new TopSucManhService();
        }
        return instance;
    }

    public List<TopRowService> queryTop(int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            String powerExpr = "CAST(JSON_UNQUOTE(JSON_EXTRACT(data_point, '$[1]')) AS UNSIGNED)";
            String sql = "SELECT id, name, head, gender, " + powerExpr + " AS power "
                    + "FROM player "
                    + "WHERE " + powerExpr + " >= " + MIN_POWER_FOR_TOP + " "
                    + "ORDER BY " + powerExpr + " DESC LIMIT " + limit;
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
                long power = 0L;
                try {
                    Object v = rs.getObject("power");
                    if (v instanceof Number n) {
                        power = n.longValue();
                    }
                } catch (Exception ignored) {
                }
                short[] parts = TopAppearanceCache.getAppearance(id, (short) head, (short) body, (short) leg);
                list.add(new TopRowService(id, name, parts[0], parts[1], parts[2], 0L, power, null, null, null, null,
                        null, null));
            }
            rs.dispose();
        } catch (Exception e) {
            Logger.error("TopSucManhService.queryTop error: " + e);
        }
        return list;
    }

    private List<TopRowService> getTopWithCache(int limit) {
        long now = System.currentTimeMillis();
        synchronized (cacheLock) {
            // Kiểm tra cache còn hợp lệ không
            if (cachedTopList != null && (now - lastCacheUpdate) < CACHE_DURATION_MS) {
                return new ArrayList<>(cachedTopList); // Return copy để tránh modification
            }

            // Cache hết hạn hoặc chưa có, query từ database
            List<TopRowService> newList = queryTop(limit);
            cachedTopList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
            lastCacheUpdate = now;
            return new ArrayList<>(cachedTopList);
        }
    }

    /**
     * Cập nhật cache ngay lập tức (gọi khi có thay đổi về sức mạnh)
     */
    public void invalidateCache() {
        synchronized (cacheLock) {
            cachedTopList = null;
            lastCacheUpdate = 0;
        }
    }

    public void showTop(Player viewer) {
        if (viewer == null) {
            return;
        }
        Message msg = null;
        try {
            // Sử dụng cache thay vì query trực tiếp
            List<TopRowService> list = getTopWithCache(100);
            if (list == null) {
                list = new ArrayList<>();
            }
            if (list.isEmpty()) {
                long viewerPower = viewer.nPoint != null ? viewer.nPoint.power : 0L;
                if (viewerPower >= MIN_POWER_FOR_TOP) {
                    list.add(new TopRowService(
                            (int) viewer.id,
                            viewer.name,
                            viewer.getHead(),
                            viewer.getBody(),
                            viewer.getLeg(),
                            0L,
                            viewerPower,
                            null, null, null, null, null, null));
                }
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
                // Sử dụng phương thức format thời gian mới từ TimeUtil
                msg.writer().writeUTF(TimeUtil.formatRelativeTimeShort(r.lastTime));
                msg.writer().writeUTF("Sức mạnh: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(r.power != null ? r.power : 0L));
                rank++;
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("TopSucManhService.showTop error: " + e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }
}
