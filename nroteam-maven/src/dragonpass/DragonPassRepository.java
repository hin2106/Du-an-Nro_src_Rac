package dragonpass;

import data.AlyraManager;
import dragonpass.DragonPassModels.PlayerState;
import dragonpass.DragonPassModels.Reward;
import dragonpass.DragonPassModels.Season;
import dragonpass.DragonPassModels.TaskState;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import utils.Logger;

public final class DragonPassRepository {
    private static volatile boolean schemaReady;

    private DragonPassRepository() {}

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        try {
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS dragon_pass_season ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, season_key VARCHAR(64) NOT NULL UNIQUE, "
                    + "start_date DATE NOT NULL, summary_date DATE NOT NULL, end_date DATE NOT NULL, "
                    + "status TINYINT NOT NULL DEFAULT 0,duration_days INT NOT NULL DEFAULT 30,"
                    + "duration_hours INT NOT NULL DEFAULT 0,duration_minutes INT NOT NULL DEFAULT 0,"
                    + "normal_price INT NOT NULL DEFAULT 360,plus_price INT NOT NULL DEFAULT 720,"
                    + "started_at DATETIME NULL,ends_at DATETIME NULL,summary_at DATETIME NULL,finished_at DATETIME NULL,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                    + "KEY idx_dps_status(status,ends_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("ALTER TABLE dragon_pass_season MODIFY season_key VARCHAR(64) NOT NULL");
            addColumn("dragon_pass_season", "status", "TINYINT NOT NULL DEFAULT 0");
            addColumn("dragon_pass_season", "duration_days", "INT NOT NULL DEFAULT 30");
            addColumn("dragon_pass_season", "duration_hours", "INT NOT NULL DEFAULT 0");
            addColumn("dragon_pass_season", "duration_minutes", "INT NOT NULL DEFAULT 0");
            addColumn("dragon_pass_season", "normal_price", "INT NOT NULL DEFAULT 360");
            addColumn("dragon_pass_season", "plus_price", "INT NOT NULL DEFAULT 720");
            addColumn("dragon_pass_season", "started_at", "DATETIME NULL");
            addColumn("dragon_pass_season", "ends_at", "DATETIME NULL");
            addColumn("dragon_pass_season", "summary_at", "DATETIME NULL");
            addColumn("dragon_pass_season", "finished_at", "DATETIME NULL");
            addColumn("dragon_pass_season", "updated_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS dragon_pass_player ("
                    + "player_id BIGINT NOT NULL, season_id BIGINT NOT NULL, exp INT NOT NULL DEFAULT 0, "
                    + "pass_type TINYINT NOT NULL DEFAULT 0, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY(player_id, season_id), KEY idx_dpp_season(season_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS dragon_pass_reward ("
                    + "season_id BIGINT NOT NULL, level_no SMALLINT NOT NULL, track TINYINT NOT NULL, slot_no TINYINT NOT NULL, "
                    + "item_template_id INT NULL, quantity INT NULL, options_data TEXT NULL, "
                    + "PRIMARY KEY(season_id, level_no, track, slot_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS dragon_pass_claim ("
                    + "player_id BIGINT NOT NULL, season_id BIGINT NOT NULL, level_no SMALLINT NOT NULL, track TINYINT NOT NULL, "
                    + "claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(player_id, season_id, level_no, track)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS dragon_pass_task_progress ("
                    + "player_id BIGINT NOT NULL, season_id BIGINT NOT NULL, cycle_key VARCHAR(10) NOT NULL, task_id INT NOT NULL, "
                    + "progress INT NOT NULL DEFAULT 0, claimed TINYINT NOT NULL DEFAULT 0, extra_data TEXT NULL, "
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY(player_id, season_id, cycle_key, task_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS dragon_pass_purchase ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, transaction_key VARCHAR(36) NOT NULL UNIQUE, player_id BIGINT NOT NULL, "
                    + "season_id BIGINT NOT NULL, old_pass_type TINYINT NOT NULL, new_pass_type TINYINT NOT NULL, gem_amount INT NOT NULL, "
                    + "status VARCHAR(16) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, KEY idx_dpp_player(player_id, season_id)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS dragon_pass_delivery ("
                    + "season_id BIGINT NOT NULL,player_id BIGINT NOT NULL,level_no SMALLINT NOT NULL,track TINYINT NOT NULL,"
                    + "mail_id BIGINT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY(season_id,player_id,level_no,track),KEY idx_dpd_mail(mail_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            migrateLegacySeasons();
            schemaReady = true;
            ensureDraft();
            Logger.success("Dragon Pass database initialized\n");
        } catch (Exception e) {
            Logger.logException(DragonPassRepository.class, e, "Khởi tạo dữ liệu Dragon Pass thất bại");
        }
    }

    private static void addColumn(String table, String column, String definition) throws Exception {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            ps.setString(1, table); ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) AlyraManager.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        }
    }

    private static void migrateLegacySeasons() throws Exception {
        AlyraManager.executeUpdate("UPDATE dragon_pass_season SET duration_days=GREATEST(1,DATEDIFF(end_date,start_date)+1),"
                + "started_at=TIMESTAMP(start_date),ends_at=TIMESTAMP(DATE_ADD(end_date,INTERVAL 1 DAY)),summary_at=TIMESTAMP(summary_date),"
                + "status=CASE WHEN CURRENT_DATE>end_date THEN ? WHEN CURRENT_DATE>=summary_date THEN ? ELSE ? END "
                + "WHERE season_key REGEXP '^[0-9]{4}-[0-9]{2}$' AND started_at IS NULL",
                DragonPassConstants.STATUS_FINISHED, DragonPassConstants.STATUS_SUMMARY, DragonPassConstants.STATUS_ACTIVE);
        AlyraManager.executeUpdate("UPDATE dragon_pass_season SET finished_at=COALESCE(finished_at,TIMESTAMP(DATE_ADD(end_date,INTERVAL 1 DAY))) "
                + "WHERE status=? AND finished_at IS NULL", DragonPassConstants.STATUS_FINISHED);
    }

    public static Season ensureDraft() throws Exception {
        ready();
        Season running = getCurrentEventRaw();
        if (running != null) return running;
        Season draft = queryOne("SELECT * FROM dragon_pass_season WHERE status=0 ORDER BY id DESC LIMIT 1");
        if (draft != null) return draft;
        long previousId = 0;
        try (Connection con = AlyraManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM dragon_pass_season WHERE status=4 ORDER BY id DESC LIMIT 1");
                    ResultSet rs = ps.executeQuery()) { if (rs.next()) previousId = rs.getLong(1); }
        }
        String key = "DP-" + System.currentTimeMillis();
        LocalDate today = LocalDate.now();
        long id;
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO dragon_pass_season(season_key,start_date,summary_date,end_date,status,duration_days,duration_hours,duration_minutes,normal_price,plus_price) "
                            + "VALUES(?,?,?,?,0,30,0,0,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, key); ps.setDate(2, Date.valueOf(today)); ps.setDate(3, Date.valueOf(today)); ps.setDate(4, Date.valueOf(today));
                ps.setInt(5, DragonPassConstants.DEFAULT_NORMAL_PRICE); ps.setInt(6, DragonPassConstants.DEFAULT_PLUS_PRICE);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (!rs.next()) throw new IllegalStateException("Không tạo được Dragon Pass nháp"); id = rs.getLong(1); }
            }
            if (previousId > 0) copyRewards(con, previousId, id);
            con.commit();
        }
        return getEvent(id);
    }

    public static Season ensureCurrentSeason() {
        try { return ensureDraft(); }
        catch (Exception e) { Logger.logException(DragonPassRepository.class, e); return null; }
    }

    private static void ready() {
        if (!schemaReady) ensureSchema();
    }

    public static Season getCurrentEvent() throws Exception { ready(); return getCurrentEventRaw(); }
    private static Season getCurrentEventRaw() throws Exception {
        return queryOne("SELECT * FROM dragon_pass_season WHERE status IN(1,2,3) ORDER BY id DESC LIMIT 1");
    }
    public static Season getFinalizingEvent() throws Exception {
        ready(); return queryOne("SELECT * FROM dragon_pass_season WHERE status=3 ORDER BY id DESC LIMIT 1");
    }
    public static Season getEvent(long id) throws Exception {
        ready(); return queryOne("SELECT * FROM dragon_pass_season WHERE id=" + id);
    }
    private static Season queryOne(String sql) throws Exception {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapSeason(rs) : null;
        }
    }
    private static Season mapSeason(ResultSet rs) throws Exception {
        Season s = new Season();
        s.id=rs.getLong("id");s.key=rs.getString("season_key");s.startDate=rs.getDate("start_date").toLocalDate();
        s.summaryDate=rs.getDate("summary_date").toLocalDate();s.endDate=rs.getDate("end_date").toLocalDate();s.status=rs.getByte("status");
        s.durationDays=rs.getInt("duration_days");s.durationHours=rs.getInt("duration_hours");s.durationMinutes=rs.getInt("duration_minutes");
        s.normalPrice=rs.getInt("normal_price");s.plusPrice=rs.getInt("plus_price");
        s.startedAt=local(rs.getTimestamp("started_at"));s.endsAt=local(rs.getTimestamp("ends_at"));
        s.summaryAt=local(rs.getTimestamp("summary_at"));s.finishedAt=local(rs.getTimestamp("finished_at"));
        s.phase=s.status==DragonPassConstants.STATUS_ACTIVE?DragonPassConstants.PHASE_ACTIVE:
                (s.status==DragonPassConstants.STATUS_SUMMARY||s.status==DragonPassConstants.STATUS_FINALIZING?DragonPassConstants.PHASE_SUMMARY:DragonPassConstants.PHASE_INACTIVE);
        return s;
    }
    private static LocalDateTime local(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }

    public static List<Season> listSeasons() throws Exception {
        ready(); List<Season> result=new ArrayList<>();
        try(Connection con=AlyraManager.getConnection();PreparedStatement ps=con.prepareStatement("SELECT * FROM dragon_pass_season ORDER BY id DESC");ResultSet rs=ps.executeQuery()){
            while(rs.next())result.add(mapSeason(rs));
        }
        return result;
    }

    public static PlayerState getOrCreatePlayer(long playerId, Season season) throws Exception {
        AlyraManager.executeUpdate("INSERT IGNORE INTO dragon_pass_player(player_id,season_id) VALUES(?,?)", playerId, season.id);
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT exp,pass_type FROM dragon_pass_player WHERE player_id=? AND season_id=?")) {
            ps.setLong(1, playerId);
            ps.setLong(2, season.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Không tải được Dragon Pass của người chơi");
                PlayerState state = new PlayerState();
                state.playerId = playerId;
                state.seasonId = season.id;
                state.exp = rs.getInt("exp");
                state.passType = rs.getByte("pass_type");
                return state;
            }
        }
    }

    public static List<Reward> getRewards(long seasonId) throws Exception {
        List<Reward> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT level_no,track,slot_no,item_template_id,quantity,options_data FROM dragon_pass_reward "
                        + "WHERE season_id=? ORDER BY level_no,track,slot_no")) {
            ps.setLong(1, seasonId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reward reward = new Reward();
                    reward.level = rs.getInt(1);
                    reward.track = rs.getByte(2);
                    reward.slot = rs.getByte(3);
                    reward.itemTemplateId = rs.getInt(4);
                    reward.quantity = rs.getInt(5);
                    reward.optionsData = rs.getString(6);
                    result.add(reward);
                }
            }
        }
        return result;
    }

    public static List<Reward> getLevelRewards(long seasonId, int level, byte track) throws Exception {
        List<Reward> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT level_no,track,slot_no,item_template_id,quantity,options_data FROM dragon_pass_reward "
                        + "WHERE season_id=? AND level_no=? AND track=? ORDER BY slot_no")) {
            ps.setLong(1, seasonId);
            ps.setInt(2, level);
            ps.setByte(3, track);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reward reward = new Reward();
                    reward.level = rs.getInt(1);
                    reward.track = rs.getByte(2);
                    reward.slot = rs.getByte(3);
                    reward.itemTemplateId = rs.getInt(4);
                    reward.quantity = rs.getInt(5);
                    reward.optionsData = rs.getString(6);
                    result.add(reward);
                }
            }
        }
        return result;
    }

    public static Set<Integer> getClaims(long playerId, long seasonId, byte track) throws Exception {
        Set<Integer> result = new HashSet<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT level_no FROM dragon_pass_claim WHERE player_id=? AND season_id=? AND track=?")) {
            ps.setLong(1, playerId);
            ps.setLong(2, seasonId);
            ps.setByte(3, track);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(rs.getInt(1)); }
        }
        return result;
    }

    public static boolean markClaimed(long playerId, long seasonId, int level, byte track) throws Exception {
        return AlyraManager.executeUpdate("INSERT IGNORE INTO dragon_pass_claim(player_id,season_id,level_no,track) VALUES(?,?,?,?)",
                playerId, seasonId, level, track) > 0;
    }

    public static void unmarkClaimed(long playerId, long seasonId, int level, byte track) {
        try { AlyraManager.executeUpdate("DELETE FROM dragon_pass_claim WHERE player_id=? AND season_id=? AND level_no=? AND track=?",
                playerId, seasonId, level, track); } catch (Exception ignored) {}
    }

    public static List<TaskState> getTaskStates(long playerId, Season season) throws Exception {
        List<TaskState> states = new ArrayList<>();
        Set<Integer> claimedSeasonTasks = new HashSet<>();
        for (DragonPassTask task : DragonPassTask.ALL) {
            String cycle = cycleKey(task, season);
            AlyraManager.executeUpdate("INSERT IGNORE INTO dragon_pass_task_progress(player_id,season_id,cycle_key,task_id) VALUES(?,?,?,?)",
                    playerId, season.id, cycle, task.id());
            TaskState state = new TaskState();
            state.definition = task;
            try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                    "SELECT progress,claimed FROM dragon_pass_task_progress WHERE player_id=? AND season_id=? AND cycle_key=? AND task_id=?")) {
                ps.setLong(1, playerId); ps.setLong(2, season.id); ps.setString(3, cycle); ps.setInt(4, task.id());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { state.progress = rs.getInt(1); state.claimed = rs.getBoolean(2); }
                }
            }
            if (task.type() == DragonPassTask.SEASON && state.claimed) claimedSeasonTasks.add(task.id());
            state.unlocked = task.prerequisiteId() == 0 || claimedSeasonTasks.contains(task.prerequisiteId());
            states.add(state);
        }
        return states;
    }

    private static String cycleKey(DragonPassTask task, Season season) {
        return task.type() == DragonPassTask.DAILY ? LocalDate.now().toString() : season.key;
    }

    public static void addTaskProgress(long playerId, Season season, int taskId, int amount) throws Exception {
        if (season.phase != DragonPassConstants.PHASE_ACTIVE || amount <= 0) return;
        DragonPassTask task = DragonPassTask.find(taskId);
        if (task == null) return;
        if (task.prerequisiteId() != 0 && !isTaskClaimed(playerId, season, task.prerequisiteId())) return;
        String cycle = cycleKey(task, season);
        AlyraManager.executeUpdate("INSERT INTO dragon_pass_task_progress(player_id,season_id,cycle_key,task_id,progress) VALUES(?,?,?,?,?) "
                        + "ON DUPLICATE KEY UPDATE progress=LEAST(?,progress+VALUES(progress))",
                playerId, season.id, cycle, task.id(), amount, task.target());
    }

    public static void visitMap(long playerId, Season season, int taskId, int mapId) throws Exception {
        if (season.phase != DragonPassConstants.PHASE_ACTIVE) return;
        DragonPassTask task = DragonPassTask.find(taskId);
        if (task == null) return;
        String cycle = cycleKey(task, season);
        AlyraManager.executeUpdate("INSERT IGNORE INTO dragon_pass_task_progress(player_id,season_id,cycle_key,task_id,extra_data) VALUES(?,?,?,?,?)",
                playerId, season.id, cycle, taskId, "[]");
        synchronized (("dp-map-" + playerId).intern()) {
            JSONArray visited = new JSONArray();
            try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                    "SELECT extra_data FROM dragon_pass_task_progress WHERE player_id=? AND season_id=? AND cycle_key=? AND task_id=?")) {
                ps.setLong(1, playerId); ps.setLong(2, season.id); ps.setString(3, cycle); ps.setInt(4, taskId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String extraData = rs.getString(1);
                        if (extraData != null && !extraData.isBlank()) {
                            Object parsed = JSONValue.parse(extraData);
                            if (parsed instanceof JSONArray arr) visited = arr;
                        }
                    }
                }
            }
            String value = String.valueOf(mapId);
            if (!visited.contains(value)) visited.add(value);
            AlyraManager.executeUpdate("UPDATE dragon_pass_task_progress SET progress=?,extra_data=? WHERE player_id=? AND season_id=? AND cycle_key=? AND task_id=?",
                    Math.min(task.target(), visited.size()), visited.toJSONString(), playerId, season.id, cycle, taskId);
        }
    }

    public static boolean isTaskClaimed(long playerId, Season season, int taskId) throws Exception {
        DragonPassTask task = DragonPassTask.find(taskId);
        if (task == null) return false;
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT claimed FROM dragon_pass_task_progress WHERE player_id=? AND season_id=? AND cycle_key=? AND task_id=?")) {
            ps.setLong(1, playerId); ps.setLong(2, season.id); ps.setString(3, cycleKey(task, season)); ps.setInt(4, taskId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getBoolean(1); }
        }
    }

    public static int claimTask(long playerId, Season season, int taskId) throws Exception {
        if (season.phase != DragonPassConstants.PHASE_ACTIVE) return 0;
        DragonPassTask task = DragonPassTask.find(taskId);
        if (task == null) return 0;
        String cycle = cycleKey(task, season);
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                int progress; boolean claimed;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT progress,claimed FROM dragon_pass_task_progress WHERE player_id=? AND season_id=? AND cycle_key=? AND task_id=? FOR UPDATE")) {
                    ps.setLong(1, playerId); ps.setLong(2, season.id); ps.setString(3, cycle); ps.setInt(4, taskId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { con.rollback(); return 0; }
                        progress = rs.getInt(1); claimed = rs.getBoolean(2);
                    }
                }
                if (claimed || progress < task.target()) { con.rollback(); return 0; }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dragon_pass_task_progress SET claimed=1 WHERE player_id=? AND season_id=? AND cycle_key=? AND task_id=?")) {
                    ps.setLong(1, playerId); ps.setLong(2, season.id); ps.setString(3, cycle); ps.setInt(4, taskId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE dragon_pass_player SET exp=exp+? WHERE player_id=? AND season_id=?")) {
                    ps.setInt(1, task.exp()); ps.setLong(2, playerId); ps.setLong(3, season.id); ps.executeUpdate();
                }
                con.commit();
                return task.exp();
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    public static int claimAllTasks(long playerId, Season season) throws Exception {
        int total = 0;
        for (DragonPassTask task : DragonPassTask.ALL) total += claimTask(playerId, season, task.id());
        return total;
    }

    @SuppressWarnings("unchecked")
    public static int purchase(player.Player player, Season season, byte requestedType, String transactionKey) throws Exception {
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                String inventoryJson;
                try (PreparedStatement ps = con.prepareStatement("SELECT data_inventory FROM player WHERE id=? FOR UPDATE")) {
                    ps.setLong(1, player.id);
                    try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new IllegalStateException("Không tìm thấy người chơi"); inventoryJson = rs.getString(1); }
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO dragon_pass_player(player_id,season_id) VALUES(?,?)")) {
                    ps.setLong(1, player.id); ps.setLong(2, season.id); ps.executeUpdate();
                }
                byte oldType;
                try (PreparedStatement ps = con.prepareStatement("SELECT pass_type FROM dragon_pass_player WHERE player_id=? AND season_id=? FOR UPDATE")) {
                    ps.setLong(1, player.id); ps.setLong(2, season.id);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); oldType = rs.getByte(1); }
                }
                byte newType;
                int price;
                if (season.phase == DragonPassConstants.PHASE_SUMMARY) {
                    if (oldType != DragonPassConstants.PASS_NORMAL || requestedType != DragonPassConstants.PASS_PLUS)
                        throw new IllegalStateException("Thời gian tổng kết chỉ cho phép nâng từ Pass thường lên Plus");
                    newType = DragonPassConstants.PASS_PLUS;
                    price = season.plusPrice - season.normalPrice;
                } else if (oldType == DragonPassConstants.PASS_NONE && requestedType == DragonPassConstants.PASS_NORMAL) {
                    newType = DragonPassConstants.PASS_NORMAL; price = season.normalPrice;
                } else if (oldType == DragonPassConstants.PASS_NONE && requestedType == DragonPassConstants.PASS_PLUS) {
                    newType = DragonPassConstants.PASS_PLUS; price = season.plusPrice;
                } else if (oldType == DragonPassConstants.PASS_NORMAL && requestedType == DragonPassConstants.PASS_PLUS) {
                    newType = DragonPassConstants.PASS_PLUS; price = season.plusPrice - season.normalPrice;
                } else {
                    throw new IllegalStateException("Gói Dragon Pass không hợp lệ hoặc đã được kích hoạt");
                }
                JSONArray inventory = (JSONArray) JSONValue.parse(inventoryJson);
                if (inventory == null || inventory.size() < 2) throw new IllegalStateException("Dữ liệu Ngọc không hợp lệ");
                // The online player's memory is authoritative; the regular save loop may not
                // have flushed a very recent gem change to data_inventory yet.
                int gem = player.inventory.gem;
                if (gem < price) throw new IllegalStateException("Bạn không đủ Ngọc");
                int newGem = gem - price;
                inventory.set(1, newGem);
                try (PreparedStatement ps = con.prepareStatement("UPDATE player SET data_inventory=? WHERE id=?")) {
                    ps.setString(1, inventory.toJSONString()); ps.setLong(2, player.id); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE dragon_pass_player SET pass_type=? WHERE player_id=? AND season_id=?")) {
                    ps.setByte(1, newType); ps.setLong(2, player.id); ps.setLong(3, season.id); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO dragon_pass_purchase(transaction_key,player_id,season_id,old_pass_type,new_pass_type,gem_amount,status) VALUES(?,?,?,?,?,?,'COMPLETED')")) {
                    ps.setString(1, transactionKey); ps.setLong(2, player.id); ps.setLong(3, season.id);
                    ps.setByte(4, oldType); ps.setByte(5, newType); ps.setInt(6, price); ps.executeUpdate();
                }
                con.commit();
                player.inventory.gem = newGem;
                return price;
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    public static void saveReward(long seasonId, int level, byte track, byte slot, Integer itemId, Integer quantity, String options) throws Exception {
        Season event=getEvent(seasonId);
        if(event==null||!event.editable())throw new IllegalStateException("Chỉ được sửa quà Dragon Pass ở trạng thái bản nháp");
        AlyraManager.executeUpdate("INSERT INTO dragon_pass_reward(season_id,level_no,track,slot_no,item_template_id,quantity,options_data) "
                        + "VALUES(?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE item_template_id=VALUES(item_template_id),quantity=VALUES(quantity),options_data=VALUES(options_data)",
                seasonId, level, track, slot, itemId, quantity, options);
    }

    public static void copyRewards(long sourceSeasonId, long targetSeasonId) throws Exception {
        Season target=getEvent(targetSeasonId);
        if(target==null||!target.editable())throw new IllegalStateException("Chỉ được ghi đè quà của Dragon Pass bản nháp");
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement delete = con.prepareStatement("DELETE FROM dragon_pass_reward WHERE season_id=?");
                    PreparedStatement copy = con.prepareStatement(
                            "INSERT INTO dragon_pass_reward(season_id,level_no,track,slot_no,item_template_id,quantity,options_data) "
                                    + "SELECT ?,level_no,track,slot_no,item_template_id,quantity,options_data FROM dragon_pass_reward WHERE season_id=?")) {
                delete.setLong(1, targetSeasonId);
                delete.executeUpdate();
                copy.setLong(1, targetSeasonId); copy.setLong(2, sourceSeasonId); copy.executeUpdate();
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void copyRewards(Connection con, long sourceSeasonId, long targetSeasonId) throws Exception {
        try (PreparedStatement copy = con.prepareStatement(
                "INSERT INTO dragon_pass_reward(season_id,level_no,track,slot_no,item_template_id,quantity,options_data) "
                        + "SELECT ?,level_no,track,slot_no,item_template_id,quantity,options_data FROM dragon_pass_reward WHERE season_id=?")) {
            copy.setLong(1,targetSeasonId);copy.setLong(2,sourceSeasonId);copy.executeUpdate();
        }
    }

    public static void saveEventConfiguration(Season event, List<Reward> rewards) throws Exception {
        if (event.status != DragonPassConstants.STATUS_DRAFT) throw new IllegalStateException("Chỉ được sửa Dragon Pass ở trạng thái bản nháp");
        long minutes=((long)event.durationDays*24+event.durationHours)*60+event.durationMinutes;
        if(event.durationDays<0||event.durationHours<0||event.durationHours>23||event.durationMinutes<0||event.durationMinutes>59||minutes<=0)
            throw new IllegalArgumentException("Thời lượng Dragon Pass không hợp lệ");
        if(event.normalPrice<=0||event.plusPrice<=event.normalPrice)throw new IllegalArgumentException("Giá Pass Plus phải lớn hơn giá Pass thường");
        try(Connection con=AlyraManager.getConnection()){
            con.setAutoCommit(false);
            try{
                try(PreparedStatement ps=con.prepareStatement("UPDATE dragon_pass_season SET duration_days=?,duration_hours=?,duration_minutes=?,normal_price=?,plus_price=? WHERE id=? AND status=0")){
                    ps.setInt(1,event.durationDays);ps.setInt(2,event.durationHours);ps.setInt(3,event.durationMinutes);ps.setInt(4,event.normalPrice);ps.setInt(5,event.plusPrice);ps.setLong(6,event.id);
                    if(ps.executeUpdate()==0)throw new IllegalStateException("Dragon Pass không còn ở trạng thái bản nháp");
                }
                try(PreparedStatement ps=con.prepareStatement("DELETE FROM dragon_pass_reward WHERE season_id=?")){ps.setLong(1,event.id);ps.executeUpdate();}
                try(PreparedStatement ps=con.prepareStatement("INSERT INTO dragon_pass_reward(season_id,level_no,track,slot_no,item_template_id,quantity,options_data) VALUES(?,?,?,?,?,?,?)")){
                    for(Reward reward:rewards){
                        if(!reward.configured())continue;
                        ps.setLong(1,event.id);ps.setInt(2,reward.level);ps.setByte(3,reward.track);ps.setByte(4,reward.slot);
                        ps.setInt(5,reward.itemTemplateId);ps.setInt(6,reward.quantity);ps.setString(7,reward.optionsData);ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
            }catch(Exception e){con.rollback();throw e;}
        }
    }

    public static void activate(Season event) throws Exception {
        long duration=(((long)event.durationDays*24+event.durationHours)*60+event.durationMinutes)*60_000L;
        Timestamp now=new Timestamp(System.currentTimeMillis()),ends=new Timestamp(System.currentTimeMillis()+duration);
        int changed=AlyraManager.executeUpdate("UPDATE dragon_pass_season SET status=1,started_at=?,ends_at=?,summary_at=NULL,finished_at=NULL,start_date=CURRENT_DATE,summary_date=DATE(?),end_date=DATE(?) WHERE id=? AND status=0",
                now,ends,ends,ends,event.id);
        if(changed==0)throw new IllegalStateException("Dragon Pass không thể kích hoạt ở trạng thái hiện tại");
    }

    public static boolean beginSummary(long eventId) throws Exception {
        return AlyraManager.executeUpdate("UPDATE dragon_pass_season SET status=2,summary_at=NOW() WHERE id=? AND status=1",eventId)>0;
    }

    public static boolean beginFinalize(long eventId) throws Exception {
        return AlyraManager.executeUpdate("UPDATE dragon_pass_season SET status=3,summary_at=COALESCE(summary_at,NOW()) WHERE id=? AND status IN(1,2)",eventId)>0;
    }

    public static void markFinished(long eventId) throws Exception {
        AlyraManager.executeUpdate("UPDATE dragon_pass_season SET status=4,finished_at=NOW() WHERE id=? AND status=3",eventId);
    }

    public static List<Long> getParticipantIds(long seasonId) throws Exception {
        List<Long> result=new ArrayList<>();
        try(Connection con=AlyraManager.getConnection();PreparedStatement ps=con.prepareStatement("SELECT player_id FROM dragon_pass_player WHERE season_id=? AND pass_type>0 ORDER BY player_id")){
            ps.setLong(1,seasonId);try(ResultSet rs=ps.executeQuery()){while(rs.next())result.add(rs.getLong(1));}
        }
        return result;
    }

    public static int claimCompletedTaskExp(long playerId, Season season) throws Exception {
        int total=0;
        try(Connection con=AlyraManager.getConnection()){
            con.setAutoCommit(false);
            try{
                for(DragonPassTask task:DragonPassTask.ALL){
                    int changed;
                    try(PreparedStatement ps=con.prepareStatement("UPDATE dragon_pass_task_progress SET claimed=1 WHERE player_id=? AND season_id=? AND task_id=? AND claimed=0 AND progress>=?")){
                        ps.setLong(1,playerId);ps.setLong(2,season.id);ps.setInt(3,task.id());ps.setInt(4,task.target());changed=ps.executeUpdate();
                    }
                    total+=changed*task.exp();
                }
                if(total>0)try(PreparedStatement ps=con.prepareStatement("UPDATE dragon_pass_player SET exp=exp+? WHERE player_id=? AND season_id=?")){
                    ps.setInt(1,total);ps.setLong(2,playerId);ps.setLong(3,season.id);ps.executeUpdate();
                }
                con.commit();
            }catch(Exception e){con.rollback();throw e;}
        }
        return total;
    }
}
