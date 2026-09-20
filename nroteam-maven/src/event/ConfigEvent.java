package event;

import consts.ConstEvent;
import lombok.Getter;
import lombok.Setter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import server.Manager;
import utils.Logger;

@Setter
@Getter
public class ConfigEvent {

    private static final String EVENT_PROPERTIES_PATH = "data/config/event.properties";

    private static final Map<String, Integer> EVENT_CLASS_TO_ID = new HashMap<>();

    static {
        EVENT_CLASS_TO_ID.put("event.halloween.Halloween", ConstEvent.SU_KIEN_HALLOWEEN);
        EVENT_CLASS_TO_ID.put("event.teacherday.TeaCherday", ConstEvent.SU_KIEN_20_11);
        EVENT_CLASS_TO_ID.put("event.noel.Noel", ConstEvent.SU_KIEN_NOEL);
        EVENT_CLASS_TO_ID.put("event.newyear.NewYear", ConstEvent.SU_KIEN_TET);
        EVENT_CLASS_TO_ID.put("event.hung_vuong.HungVuong", ConstEvent.SU_KIEN_HUNG_VUONG);
        EVENT_CLASS_TO_ID.put("event.trung_thu.TrungThu", ConstEvent.SU_KIEN_TRUNG_THU);
        EVENT_CLASS_TO_ID.put("event.valentine.Valentine", ConstEvent.SU_KIEN_VALENTINE);
        EVENT_CLASS_TO_ID.put("event.women.Women", ConstEvent.SU_KIEN_20_10);
    }

    public ConfigEvent() {
        load();
    }

    public void load() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(EVENT_PROPERTIES_PATH)) {
            properties.load(fis);
            int configuredEventId = resolveConfiguredEventId(properties);
            Manager.EVENT_SEVER = configuredEventId;
            Logger.successln("[EventConfig] Loaded event id=" + configuredEventId + " from " + EVENT_PROPERTIES_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int resolveConfiguredEventId(Properties properties) {
        String eventIdRaw = trimToNull(properties.getProperty("event.id"));
        if (eventIdRaw != null) {
            Integer parsed = parseEventId(eventIdRaw);
            if (parsed != null) {
                return parsed;
            }
            Logger.warning("[EventConfig] Invalid event.id='" + eventIdRaw + "', fallback to key 'event'");
        }

        String eventRaw = trimToNull(properties.getProperty("event"));
        if (eventRaw == null) {
            Logger.warning("[EventConfig] Missing key 'event' or 'event.id', keep default id=" + Manager.EVENT_SEVER);
            return Manager.EVENT_SEVER;
        }

        Integer parsedAsId = parseEventId(eventRaw);
        if (parsedAsId != null) {
            return parsedAsId;
        }

        Integer mapped = EVENT_CLASS_TO_ID.get(eventRaw);
        if (mapped != null) {
            return mapped;
        }

        Logger.warning("[EventConfig] Unknown event='" + eventRaw + "', keep default id=" + Manager.EVENT_SEVER);
        return Manager.EVENT_SEVER;
    }

    private Integer parseEventId(String raw) {
        try {
            int id = Integer.parseInt(raw.trim());
            if (id >= ConstEvent.KHONG_CO_SU_KIEN && id <= ConstEvent.SU_KIEN_20_10) {
                return id;
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
