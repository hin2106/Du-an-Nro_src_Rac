package event;

import boss.BossID;
import consts.ConstEvent;
import item.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import managers.boss.BossManager;
import managers.boss.ChristmasEventManager;
import managers.boss.HalloweenEventManager;
import managers.boss.HungVuongEventManager;
import managers.boss.LunarNewYearEventManager;
import managers.boss.TrungThuEventManager;
import map.ItemMap;
import mob.Mob;
import npc.Npc;
import player.Player;
import shop.TabShopSanta;
import utils.Logger;

public abstract class Event {

    private static Event instance;

    private static final Map<Integer, String> EVENT_MAP = new HashMap<>();

    static {
        EVENT_MAP.put(ConstEvent.SU_KIEN_HALLOWEEN, "event.halloween.Halloween");
        EVENT_MAP.put(ConstEvent.SU_KIEN_20_11, "event.teacherday.TeaCherday");
        EVENT_MAP.put(ConstEvent.SU_KIEN_NOEL, "event.noel.Noel");
        EVENT_MAP.put(ConstEvent.SU_KIEN_TET, "event.newyear.NewYear");
        EVENT_MAP.put(ConstEvent.SU_KIEN_HUNG_VUONG, "event.hung_vuong.HungVuong");
        EVENT_MAP.put(ConstEvent.SU_KIEN_TRUNG_THU, "event.trung_thu.TrungThu");
        EVENT_MAP.put(ConstEvent.SU_KIEN_HE, "event.he.EventHe");
        EVENT_MAP.put(ConstEvent.SU_KIEN_VALENTINE, "event.valentine.Valentine");
        EVENT_MAP.put(ConstEvent.SU_KIEN_20_10, "event.women.Women");
    }

    public static synchronized void switchEvent(int eventId) {
        String eventClass = EVENT_MAP.get(eventId);
        switchEvent(eventClass, eventId);
    }

    public static synchronized void switchEvent(String eventClass, int eventId) {
        try {
            if (instance != null) {
                instance.cleanup();
                instance = null;
            }
            server.Manager.EVENT_SEVER = eventId;
            removeAllEventBossesGlobally();
            Logger.logln("[Event] Dọn toàn bộ boss event cũ...");
            if (eventId <= 0 || eventClass == null || eventClass.isEmpty()) {
                TabShopSanta.replaceItems(new java.util.ArrayList<>());
                Logger.successln("[Event] Không có sự kiện nào đang hoạt động");
                Logger.successln("[Event] Đã xóa item event khỏi shop Santa");
                return;
            }

            Class<?> clazz = Class.forName(eventClass);
            if (!Event.class.isAssignableFrom(clazz)) {
                Logger.error("[Event] Class " + eventClass + " không kế thừa Event!");
                return;
            }

            instance = (Event) clazz.getDeclaredConstructor().newInstance();
            instance.init();
            TabShopSanta.replaceItems(instance.getSantaShopItems());
            Logger.successln("[Event] Đã kích hoạt: " + eventClass + " (ID=" + eventId + ")");
            Logger.successln("[Event] Cập nhật shop Santa: " + instance.getSantaShopItems().size() + " item");

        } catch (Exception e) {
            Logger.logException(Event.class, e);
        }
    }

    public static Event getInstance() {
        return instance;
    }

    public static boolean isEventActive() {
        return instance != null;
    }

    private static final Map<int[], Class<?>> EVENT_BOSS_MANAGERS = new HashMap<>() {
        {
            put(new int[]{BossID.GOGETA, BossID.THO_DAI_CA, boss.BossID.OMEGA, boss.BossID.KHIDOT}, TrungThuEventManager.class);
            put(new int[]{BossID.BIMA, BossID.MATROI, boss.BossID.DOI}, HalloweenEventManager.class);
            put(new int[]{BossID.SON_TINH, BossID.THUY_TINH}, HungVuongEventManager.class);
            put(new int[]{BossID.ONG_GIA_NOEL, BossID.TUAN_LOC}, ChristmasEventManager.class);
            put(new int[]{BossID.LAN_CON}, LunarNewYearEventManager.class);
        }
    };

    private static void removeAllEventBossesGlobally() {
        try {
            for (int[] ids : EVENT_BOSS_MANAGERS.keySet()) {
                for (int id : ids) {
                    BossManager.gI().removeAllByBossId(id);
                }
            }
            for (Map.Entry<int[], Class<?>> entry : EVENT_BOSS_MANAGERS.entrySet()) {
                for (int id : entry.getKey()) {
                    try {
                        var method = entry.getValue().getMethod("gI");
                        Object manager = method.invoke(null);
                        var remove = entry.getValue().getMethod("removeAllByBossId", int.class);
                        remove.invoke(manager, id);
                    } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Event.class, e);
        }
    }

    public abstract int eventId();

    public abstract void init();

    public abstract void initNpc();

    public abstract void initMap();

    public abstract void dropItem(Player player, Mob mob, List<ItemMap> list, int x, int yEnd);

    public abstract boolean useItem(Player player, Item item);

    public void cleanup() {
    }

    /**
     * Trả về danh sách item event cho shop Santa.
     * Mỗi event override method này để định nghĩa item riêng.
     */
    public List<TabShopSanta.SantaItemConfig> getSantaShopItems() {
        return new java.util.ArrayList<>();
    }
}
