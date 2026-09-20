package event;

import consts.ConstEvent;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventItemDropLimitConfig {

    private static final Map<Integer, Map<Integer, Integer>> EVENT_ITEM_DAILY_LIMITS = new ConcurrentHashMap<>();

    static {
        // Item Thịt Heo
        setLimit(ConstEvent.SU_KIEN_TET, 748, 99);
        // Item Đậu Xanh
        setLimit(ConstEvent.SU_KIEN_TET, 750, 99);
        // Item Bùa Giải Khai Phong Ấn
        setLimit(ConstEvent.SU_KIEN_TET, 537, 99);
        setLimit(ConstEvent.SU_KIEN_TET, 538, 99);
        setLimit(ConstEvent.SU_KIEN_TET, 539, 99);
        setLimit(ConstEvent.SU_KIEN_TET, 540, 99);
    }

    private EventItemDropLimitConfig() {
    }

    public static void setLimit(int eventId, int itemId, int maxPerDay) {
        if (maxPerDay <= 0) {
            removeLimit(eventId, itemId);
            return;
        }
        EVENT_ITEM_DAILY_LIMITS
                .computeIfAbsent(eventId, k -> new ConcurrentHashMap<>())
                .put(itemId, maxPerDay);
    }

    public static void removeLimit(int eventId, int itemId) {
        Map<Integer, Integer> limits = EVENT_ITEM_DAILY_LIMITS.get(eventId);
        if (limits == null) {
            return;
        }
        limits.remove(itemId);
        if (limits.isEmpty()) {
            EVENT_ITEM_DAILY_LIMITS.remove(eventId);
        }
    }

    public static int getLimit(int eventId, int itemId) {
        Map<Integer, Integer> limits = EVENT_ITEM_DAILY_LIMITS.get(eventId);
        if (limits == null) {
            return -1;
        }
        Integer value = limits.get(itemId);
        return value != null ? value : -1;
    }

    public static Map<Integer, Integer> getEventLimits(int eventId) {
        Map<Integer, Integer> limits = EVENT_ITEM_DAILY_LIMITS.get(eventId);
        if (limits == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(limits);
    }
}
