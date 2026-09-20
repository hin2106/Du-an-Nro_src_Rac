package services.top;
import data.AlyraManager;
import data.AlyraResultSet;
import network.Message;
import player.Player;
import server.Manager;
import task.TaskMain;
import utils.Logger;
import utils.TimeUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TopNhiemVuService {

    private static TopNhiemVuService instance;

    private static final long CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(5); // Cache 5 phút

    // Cache cho top ranking
    private List<TopRowService> cachedTopList = null;
    private long lastCacheUpdate = 0;
    private final Object cacheLock = new Object();

    public static TopNhiemVuService gI() {
        if (instance == null) {
            instance = new TopNhiemVuService();
        }
        return instance;
    }

    public List<TopRowService> queryTop(int limit) {
        List<TopRowService> list = new ArrayList<>();
        try {
            // data_task structure: [0]=id, [1]=index, [2]=count, [3]=lastTime
            String sql = "SELECT id, name, head, gender, "
                    + "CAST(JSON_UNQUOTE(JSON_EXTRACT(data_task, '$[0]')) AS UNSIGNED) AS task_id, "
                    + "CAST(JSON_UNQUOTE(JSON_EXTRACT(data_task, '$[1]')) AS UNSIGNED) AS task_index, "
                    + "CAST(JSON_UNQUOTE(JSON_EXTRACT(data_task, '$[3]')) AS UNSIGNED) AS last_time FROM player "
                    + "ORDER BY task_id DESC, task_index DESC LIMIT "
                    + limit;
            AlyraResultSet rs = AlyraManager.executeQuery(sql);
            while (rs.next()) {
                int pid = rs.getInt("id");
                long dbLast = 0L;
                try {
                    Object v = rs.getObject("last_time");
                    if (v != null) {
                        if (v instanceof Number n) {
                            dbLast = n.longValue();
                        } else {
                            String s = rs.getString("last_time");
                            if (s != null && !s.isEmpty() && !s.equalsIgnoreCase("null")) {
                                s = s.replaceAll("[^0-9-]", "");
                                if (!s.isEmpty()) {
                                    dbLast = Long.parseLong(s);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Nếu không lấy được lastTime (player cũ), set về 0 để hiển thị "vừa xong"
                    dbLast = 0L;
                }

                // Convert từ seconds sang milliseconds nếu cần
                // Timestamp hiện tại (2024) là khoảng 1.7 tỷ milliseconds
                if (dbLast > 0 && dbLast < 10_000_000_000L) {
                    dbLast *= 1000L;
                }
                String name = rs.getString("name");
                int head = rs.getInt("head");
                int gender = 0;
                try {
                    gender = rs.getInt("gender");
                } catch (Exception ignored) {
                }
                int body = (gender == 1) ? 59 : 57;
                int leg = (gender == 1) ? 60 : 58;
                int taskId = 0;
                int taskIndex = 0;
                try {
                    taskId = rs.getInt("task_id");
                    taskIndex = rs.getInt("task_index");
                } catch (Exception ignored) {
                }
                int taskNum = normalizeTaskId(taskId);
                String taskName = getTaskNameById(taskNum);
                short[] parts = TopAppearanceCache.getAppearance(pid, (short) head, (short) body, (short) leg);
                list.add(new TopRowService(pid, name, parts[0], parts[1], parts[2], dbLast, null, taskNum, null, taskName, taskIndex, null, null));
            }
            rs.dispose();
        } catch (Exception e) {
            Logger.error("TopNhiemVuService.queryTop error: " + e);
        }
        return list;
    }

    /**
     * Lấy top ranking từ cache hoặc query database nếu cache hết hạn
     */
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
     * Cập nhật cache ngay lập tức (gọi khi có thay đổi về nhiệm vụ)
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
            if (list.isEmpty()) {
                long lt = (viewer.playerTask != null && viewer.playerTask.taskMain != null)
                        ? viewer.playerTask.taskMain.lastTime
                        : 0L;
                if (lt > 0 && lt < 10_000_000_000L) {
                    lt *= 1000L;
                }
                list.add(new TopRowService((int) viewer.id, viewer.name, viewer.getHead(), viewer.getBody(),
                        viewer.getLeg(), lt));
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
                int nv = r.level != null ? r.level : 0;
                int buoc = r.point != null ? r.point : 0;
                String tn = r.leaderName != null ? r.leaderName : "NV " + nv;
                msg.writer().writeUTF(tn + " " + nv + " - " + buoc);
                rank++;
            }
            viewer.sendMessage(msg);
        } catch (IOException e) {
            Logger.error("TopNhiemVuService.showTop error: " + e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private String getTaskNameById(int taskId) {
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == taskId) {
                return tm.name;
            }
        }
        return null;
    }

    /**
     * Chuẩn hóa task id để tương thích dữ liệu cũ/mới:
     * - Dữ liệu mới: data_task[0] lưu trực tiếp taskMain.id
     * - Dữ liệu cũ: có thể lưu idTaskCustom = ((taskId << 10) + index) << 1
     */
    private int normalizeTaskId(int rawTaskId) {
        if (rawTaskId <= 0) {
            return 0;
        }
        if (rawTaskId >= 2048) {
            return rawTaskId >> 11;
        }
        return rawTaskId;
    }
}
