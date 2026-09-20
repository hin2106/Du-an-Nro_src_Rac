package audit;

import data.AlyraManager;
import item.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import map.ItemMap;
import player.Player;
import utils.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/** So cai tai san dieu tra, chi giu du lieu tho trong 3 ngay. */
public final class AssetAuditService {
    private static final AssetAuditService INSTANCE = new AssetAuditService();
    private final ScheduledExecutorService maintenance = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "asset-audit-retention");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean started;
    private final ThreadLocal<Boolean> exactBalanceHandled = ThreadLocal.withInitial(() -> false);
    private final ConcurrentHashMap<Long, Balance> balanceSnapshots = new ConcurrentHashMap<>();

    public static AssetAuditService gI() { return INSTANCE; }

    public synchronized void start() {
        if (started) return;
        createSchema();
        maintenance.scheduleWithFixedDelay(this::purgeQuietly, 1, 1, TimeUnit.HOURS);
        started = true;
        Logger.log("Asset audit da khoi dong (retention 3 ngay)\n");
    }

    public synchronized void shutdown() {
        maintenance.shutdownNow();
        started = false;
    }

    private void createSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS asset_audit_event ("
                + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),"
                + "trace_id VARCHAR(64) NULL,event_type VARCHAR(32) NOT NULL,asset_type VARCHAR(16) NOT NULL,"
                + "item_template_id INT NULL,item_name VARCHAR(255) NULL,quantity BIGINT NOT NULL,"
                + "from_player_id BIGINT NULL,from_player_name VARCHAR(64) NULL,"
                + "to_player_id BIGINT NULL,to_player_name VARCHAR(64) NULL,"
                + "map_id INT NULL,zone_id INT NULL,x INT NULL,y INT NULL,context TEXT NULL,item_data LONGTEXT NULL,"
                + "INDEX idx_audit_trace_time(trace_id,created_at),"
                + "INDEX idx_audit_from_time(from_player_id,created_at),"
                + "INDEX idx_audit_to_time(to_player_id,created_at),INDEX idx_audit_time(created_at)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection c = AlyraManager.getConnection(); Statement st = c.createStatement()) {
            st.execute(sql);
            if (!hasColumn(c, "asset_audit_event", "item_data")) {
                st.execute("ALTER TABLE asset_audit_event ADD COLUMN item_data LONGTEXT NULL AFTER context");
            }
            st.execute("CREATE TABLE IF NOT EXISTS asset_restore_case ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),"
                    + "trace_id VARCHAR(64) NOT NULL,original_player_id BIGINT NOT NULL,original_player_name VARCHAR(64),"
                    + "item_template_id INT NOT NULL,item_name VARCHAR(255),quantity BIGINT NOT NULL,reason TEXT,evidence LONGTEXT,"
                    + "INDEX idx_restore_trace(trace_id),INDEX idx_restore_time(created_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        } catch (Exception e) {
            throw new IllegalStateException("Khong tao duoc bang asset_audit_event", e);
        }
    }

    public void recordItem(String type, String traceId, Item item, long quantity,
            Player from, Player to, Player locationPlayer, String context) {
        if (item == null || item.template == null) return;
        record(type, traceId, "ITEM", (int) item.template.id, item.template.name, quantity,
                from, to, locationPlayer, context, serializeItem(item, quantity));
    }

    public void recordItemMap(String type, ItemMap mapItem, Player from, Player to, String context) {
        if (mapItem == null || mapItem.itemTemplate == null) return;
        record(type, mapItem.auditTraceId, "ITEM", (int) mapItem.itemTemplate.id,
                mapItem.itemTemplate.name, mapItem.quantity, from, to, to != null ? to : from, context,
                mapItem.zone == null ? null : mapItem.zone.map.mapId,
                mapItem.zone == null ? null : mapItem.zone.zoneId, mapItem.x, mapItem.y, serializeItemMap(mapItem));
    }

    public void recordCurrency(String type, String assetType, long quantity,
            Player from, Player to, Player locationPlayer, String context) {
        record(type, null, assetType, null, assetType, quantity, from, to, locationPlayer, context);
    }

    public void markExactBalanceHandled() { exactBalanceHandled.set(true); }
    public boolean consumeExactBalanceHandled() {
        boolean value = exactBalanceHandled.get();
        exactBalanceHandled.remove();
        return value;
    }

    public void syncBalance(Player player) {
        if (player == null || player.inventory == null || !player.isPl()) return;
        balanceSnapshots.put(player.id, Balance.of(player));
    }

    /** Bat cac thay doi do timer, event server hoac tac vu khong di qua Controller. */
    public void reconcileBackgroundBalance(Player player) {
        if (player == null || player.inventory == null || !player.isPl()) return;
        long now = System.currentTimeMillis();
        Balance current = Balance.of(player);
        Balance previous = balanceSnapshots.putIfAbsent(player.id, current);
        if (previous == null || now - previous.checkedAt < 1000) return;
        if (!balanceSnapshots.replace(player.id, previous, current)) return;
        long gold=current.gold-previous.gold, gem=(long)current.gem-previous.gem, ruby=(long)current.ruby-previous.ruby;
        long bars=(long)current.goldBars-previous.goldBars;
        String context="Thay đổi nền từ sự kiện/timer hoặc tác vụ server";
        if(gold!=0)recordCurrency("BACKGROUND_BALANCE_CHANGE","GOLD",gold,gold<0?player:null,gold>0?player:null,player,context);
        if(gem!=0)recordCurrency("BACKGROUND_BALANCE_CHANGE","GEM",gem,gem<0?player:null,gem>0?player:null,player,context);
        if(ruby!=0)recordCurrency("BACKGROUND_BALANCE_CHANGE","RUBY",ruby,ruby<0?player:null,ruby>0?player:null,player,context);
        if(bars!=0)recordCurrency("BACKGROUND_GOLD_BAR_CHANGE","GOLD_BAR",bars,bars<0?player:null,bars>0?player:null,player,context);
    }

    private static int countGoldBars(Player player) {
        if(player.inventory.itemsBag==null)return 0;long total=0;
        for(Item item:player.inventory.itemsBag)if(item!=null&&item.template!=null&&item.template.id==457)total+=item.quantity;
        return (int)Math.min(Integer.MAX_VALUE,total);
    }

    private static final class Balance {
        final long gold,checkedAt; final int gem,ruby,goldBars;
        Balance(long gold,int gem,int ruby,int goldBars,long checkedAt){this.gold=gold;this.gem=gem;this.ruby=ruby;this.goldBars=goldBars;this.checkedAt=checkedAt;}
        static Balance of(Player p){return new Balance(p.inventory.gold,p.inventory.gem,p.inventory.ruby,countGoldBars(p),System.currentTimeMillis());}
    }

    private void record(String eventType, String traceId, String assetType, Integer templateId,
            String itemName, long quantity, Player from, Player to, Player location, String context) {
        record(eventType, traceId, assetType, templateId, itemName, quantity, from, to, location, context, null);
    }

    private void record(String eventType, String traceId, String assetType, Integer templateId,
            String itemName, long quantity, Player from, Player to, Player location, String context, String itemData) {
        Integer mapId = null, zoneId = null, x = null, y = null;
        if (location != null && location.zone != null && location.location != null) {
            mapId = location.zone.map.mapId; zoneId = location.zone.zoneId;
            x = location.location.x; y = location.location.y;
        }
        record(eventType, traceId, assetType, templateId, itemName, quantity, from, to,
                location, context, mapId, zoneId, x, y, itemData);
    }

    private void record(String eventType, String traceId, String assetType, Integer templateId,
            String itemName, long quantity, Player from, Player to, Player ignored, String context,
            Integer mapId, Integer zoneId, Integer x, Integer y, String itemData) {
        String sql = "INSERT INTO asset_audit_event(trace_id,event_type,asset_type,item_template_id,item_name,quantity,"
                + "from_player_id,from_player_name,to_player_id,to_player_name,map_id,zone_id,x,y,context,item_data)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = AlyraManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setString(ps, 1, traceId); ps.setString(2, eventType); ps.setString(3, assetType);
            if (templateId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, templateId);
            setString(ps, 5, itemName); ps.setLong(6, quantity);
            setPlayer(ps, 7, 8, from); setPlayer(ps, 9, 10, to);
            setInt(ps, 11, mapId); setInt(ps, 12, zoneId); setInt(ps, 13, x); setInt(ps, 14, y);
            setString(ps, 15, context); setString(ps, 16, itemData); ps.executeUpdate();
        } catch (Exception e) {
            Logger.logException(AssetAuditService.class, e);
        }
    }

    public List<AssetAuditEvent> search(String selector, int days) throws Exception {
        int safeDays = Math.max(1, Math.min(3, days));
        String value = selector == null ? "" : selector.trim();
        boolean numeric = value.matches("\\d+");
        String sql = "SELECT * FROM asset_audit_event WHERE created_at >= NOW() - INTERVAL " + safeDays + " DAY ";
        if (!value.isEmpty()) {
            sql += numeric
                    ? "AND (from_player_id=? OR to_player_id=? OR trace_id=?) "
                    : "AND (LOWER(from_player_name) LIKE ? OR LOWER(to_player_name) LIKE ? OR trace_id=?) ";
        }
        sql += "ORDER BY created_at DESC LIMIT 5000";
        List<AssetAuditEvent> result = new ArrayList<>();
        try (Connection c = AlyraManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (!value.isEmpty()) {
                if (numeric) { ps.setLong(1, Long.parseLong(value)); ps.setLong(2, Long.parseLong(value)); }
                else { ps.setString(1, "%" + value.toLowerCase() + "%"); ps.setString(2, "%" + value.toLowerCase() + "%"); }
                ps.setString(3, value);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(read(rs));
            }
        }
        return result;
    }

    public List<AssetAuditEvent> trace(String traceId) throws Exception {
        List<AssetAuditEvent> result = new ArrayList<>();
        try (Connection c = AlyraManager.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM asset_audit_event WHERE trace_id=? AND created_at>=NOW()-INTERVAL 3 DAY ORDER BY created_at,id")) {
            ps.setString(1, traceId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(read(rs)); }
        }
        return result;
    }

    private AssetAuditEvent read(ResultSet rs) throws Exception {
        AssetAuditEvent e = new AssetAuditEvent();
        e.id=rs.getLong("id"); e.createdAt=rs.getTimestamp("created_at"); e.traceId=rs.getString("trace_id");
        e.eventType=rs.getString("event_type"); e.assetType=rs.getString("asset_type");
        int temp=rs.getInt("item_template_id"); e.itemTemplateId=rs.wasNull()?null:temp;
        e.itemName=rs.getString("item_name"); e.quantity=rs.getLong("quantity");
        e.fromPlayerId=getLong(rs,"from_player_id"); e.fromPlayerName=rs.getString("from_player_name");
        e.toPlayerId=getLong(rs,"to_player_id"); e.toPlayerName=rs.getString("to_player_name");
        e.mapId=getInt(rs,"map_id"); e.zoneId=getInt(rs,"zone_id"); e.x=getInt(rs,"x"); e.y=getInt(rs,"y");
        e.context=rs.getString("context"); e.itemData=rs.getString("item_data"); return e;
    }

    private void purgeQuietly() {
        try (Connection c=AlyraManager.getConnection(); Statement st=c.createStatement()) {
            st.executeUpdate("DELETE FROM asset_audit_event WHERE created_at < NOW() - INTERVAL 3 DAY");
            st.executeUpdate("DELETE FROM asset_restore_case WHERE created_at < NOW() - INTERVAL 3 DAY");
        } catch (Exception e) { Logger.logException(AssetAuditService.class, e); }
    }

    private static void setPlayer(PreparedStatement ps,int idIndex,int nameIndex,Player p)throws Exception{
        if(p==null){ps.setNull(idIndex,java.sql.Types.BIGINT);ps.setNull(nameIndex,java.sql.Types.VARCHAR);}
        else{ps.setLong(idIndex,p.id);setString(ps,nameIndex,p.name);}
    }
    private static void setString(PreparedStatement ps,int i,String v)throws Exception{if(v==null)ps.setNull(i,java.sql.Types.VARCHAR);else ps.setString(i,v);}
    private static void setInt(PreparedStatement ps,int i,Integer v)throws Exception{if(v==null)ps.setNull(i,java.sql.Types.INTEGER);else ps.setInt(i,v);}
    private static Long getLong(ResultSet rs,String c)throws Exception{long v=rs.getLong(c);return rs.wasNull()?null:v;}
    private static Integer getInt(ResultSet rs,String c)throws Exception{int v=rs.getInt(c);return rs.wasNull()?null:v;}

    private static boolean hasColumn(Connection c, String table, String column) throws Exception {
        try (ResultSet rs = c.getMetaData().getColumns(c.getCatalog(), null, table, column)) { return rs.next(); }
    }

    @SuppressWarnings("unchecked")
    private static String serializeItem(Item item, long quantity) {
        if (item == null || item.template == null) return null;
        JSONObject root = new JSONObject(); root.put("templateId", (int)item.template.id);
        root.put("quantity", quantity); root.put("createTime", item.createTime);
        JSONArray options = new JSONArray();
        for (Item.ItemOption option : item.itemOptions) {
            if (option == null || option.optionTemplate == null) continue;
            JSONObject value = new JSONObject(); value.put("id", option.optionTemplate.id); value.put("param", option.param); options.add(value);
        }
        root.put("options", options); return root.toJSONString();
    }

    @SuppressWarnings("unchecked")
    private static String serializeItemMap(ItemMap item) {
        if (item == null || item.itemTemplate == null) return null;
        JSONObject root = new JSONObject(); root.put("templateId", (int)item.itemTemplate.id);
        root.put("quantity", (long)item.quantity); root.put("createTime", item.auditItemCreateTime);
        JSONArray options = new JSONArray();
        if (item.options != null) for (Item.ItemOption option : item.options) {
            if (option == null || option.optionTemplate == null) continue;
            JSONObject value = new JSONObject(); value.put("id", option.optionTemplate.id); value.put("param", option.param); options.add(value);
        }
        root.put("options", options); return root.toJSONString();
    }
}
