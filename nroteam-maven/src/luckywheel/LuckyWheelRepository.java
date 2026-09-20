package luckywheel;

import data.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import luckywheel.LuckyWheelModels.Event;
import luckywheel.LuckyWheelModels.Milestone;
import luckywheel.LuckyWheelModels.PlayerState;
import luckywheel.LuckyWheelModels.RankEntry;
import luckywheel.LuckyWheelModels.RankReward;
import luckywheel.LuckyWheelModels.Reward;
import luckywheel.LuckyWheelModels.SpinResult;
import utils.Logger;

public final class LuckyWheelRepository {
    private static volatile boolean schemaReady;

    private LuckyWheelRepository() {}

    public static synchronized void ensureSchema() {
        if (schemaReady) return;
        try {
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_event ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,status TINYINT NOT NULL DEFAULT 0,config_version INT NOT NULL DEFAULT 1,"
                    + "duration_days INT NOT NULL DEFAULT 7,duration_hours INT NOT NULL DEFAULT 0,duration_minutes INT NOT NULL DEFAULT 0,"
                    + "price_one INT NOT NULL DEFAULT 200,price_ten INT NOT NULL DEFAULT 2000,pity_limit INT NOT NULL DEFAULT 1000,"
                    + "started_at DATETIME NULL,ends_at DATETIME NULL,finished_at DATETIME NULL,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                    + "KEY idx_lwe_status(status,ends_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_reward ("
                    + "event_id BIGINT NOT NULL,config_version INT NOT NULL,position_no TINYINT NOT NULL,"
                    + "item_template_id INT NOT NULL,quantity INT NOT NULL,options_data TEXT NULL,rate_units BIGINT NOT NULL,"
                    + "PRIMARY KEY(event_id,config_version,position_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_player ("
                    + "event_id BIGINT NOT NULL,player_id BIGINT NOT NULL,total_spins INT NOT NULL DEFAULT 0,"
                    + "pity_remaining INT NOT NULL,reached_at DATETIME NOT NULL,"
                    + "PRIMARY KEY(event_id,player_id),KEY idx_lwp_rank(event_id,total_spins,reached_at)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_spin ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,event_id BIGINT NOT NULL,player_id BIGINT NOT NULL,"
                    + "request_key BIGINT NOT NULL,config_version INT NOT NULL,spin_count TINYINT NOT NULL,cost INT NOT NULL,"
                    + "total_spins_after INT NOT NULL,pity_after INT NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "UNIQUE KEY uk_lws_request(event_id,player_id,request_key),KEY idx_lws_player(event_id,player_id)) "
                    + "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_spin_result ("
                    + "spin_id BIGINT NOT NULL,sequence_no TINYINT NOT NULL,position_no TINYINT NOT NULL,"
                    + "item_template_id INT NOT NULL,quantity INT NOT NULL,options_data TEXT NULL,"
                    + "PRIMARY KEY(spin_id,sequence_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_milestone ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY,event_id BIGINT NOT NULL,target_spins INT NOT NULL,sort_order INT NOT NULL DEFAULT 0,"
                    + "UNIQUE KEY uk_lwm_target(event_id,target_spins)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_milestone_reward ("
                    + "milestone_id BIGINT NOT NULL,slot_no SMALLINT NOT NULL,item_template_id INT NOT NULL,quantity INT NOT NULL,options_data TEXT NULL,"
                    + "PRIMARY KEY(milestone_id,slot_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_milestone_claim ("
                    + "event_id BIGINT NOT NULL,player_id BIGINT NOT NULL,target_spins INT NOT NULL,claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY(event_id,player_id,target_spins)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_rank_reward ("
                    + "event_id BIGINT NOT NULL,rank_no TINYINT NOT NULL,slot_no SMALLINT NOT NULL,item_template_id INT NOT NULL,"
                    + "quantity INT NOT NULL,options_data TEXT NULL,PRIMARY KEY(event_id,rank_no,slot_no)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            AlyraManager.executeUpdate("CREATE TABLE IF NOT EXISTS lucky_wheel_delivery ("
                    + "event_id BIGINT NOT NULL,player_id BIGINT NOT NULL,delivery_type VARCHAR(16) NOT NULL,reference_id BIGINT NOT NULL,"
                    + "mail_id BIGINT NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "PRIMARY KEY(event_id,player_id,delivery_type,reference_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            schemaReady = true;
            ensureDraft();
            Logger.success("Khoi tao database Vong quay may man thanh cong\n");
        } catch (Exception e) {
            Logger.logException(LuckyWheelRepository.class, e, "Khoi tao database Vong quay may man that bai");
        }
    }

    private static void ready() {
        if (!schemaReady) ensureSchema();
    }

    public static Event ensureDraft() throws Exception {
        ready();
        Event running = getRunningEventRaw();
        if (running != null) return running;
        Event draft = queryOne("SELECT * FROM lucky_wheel_event WHERE status=0 ORDER BY id DESC LIMIT 1");
        if (draft != null) return draft;
        long previousId = 0;
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT id FROM lucky_wheel_event WHERE status=3 ORDER BY id DESC LIMIT 1"); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) previousId = rs.getLong(1);
        }
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                long id;
                if (previousId > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO lucky_wheel_event(status,config_version,duration_days,duration_hours,duration_minutes,price_one,price_ten,pity_limit) "
                                    + "SELECT 0,1,duration_days,duration_hours,duration_minutes,price_one,price_ten,pity_limit FROM lucky_wheel_event WHERE id=?",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setLong(1, previousId); ps.executeUpdate(); id = generatedId(ps);
                    }
                    copyConfiguration(con, previousId, id);
                } else {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO lucky_wheel_event(status) VALUES(0)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.executeUpdate(); id = generatedId(ps);
                    }
                }
                con.commit();
                return getEvent(id);
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    private static void copyConfiguration(Connection con, long sourceId, long targetId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO lucky_wheel_reward(event_id,config_version,position_no,item_template_id,quantity,options_data,rate_units) "
                        + "SELECT ?,1,position_no,item_template_id,quantity,options_data,rate_units FROM lucky_wheel_reward r "
                        + "WHERE event_id=? AND config_version=(SELECT config_version FROM lucky_wheel_event WHERE id=?)")) {
            ps.setLong(1, targetId); ps.setLong(2, sourceId); ps.setLong(3, sourceId); ps.executeUpdate();
        }
        try (PreparedStatement select = con.prepareStatement("SELECT id,target_spins,sort_order FROM lucky_wheel_milestone WHERE event_id=? ORDER BY sort_order,id")) {
            select.setLong(1, sourceId);
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    long oldMilestone = rs.getLong(1), newMilestone;
                    try (PreparedStatement insert = con.prepareStatement(
                            "INSERT INTO lucky_wheel_milestone(event_id,target_spins,sort_order) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        insert.setLong(1, targetId); insert.setInt(2, rs.getInt(2)); insert.setInt(3, rs.getInt(3));
                        insert.executeUpdate(); newMilestone = generatedId(insert);
                    }
                    try (PreparedStatement copy = con.prepareStatement(
                            "INSERT INTO lucky_wheel_milestone_reward(milestone_id,slot_no,item_template_id,quantity,options_data) "
                                    + "SELECT ?,slot_no,item_template_id,quantity,options_data FROM lucky_wheel_milestone_reward WHERE milestone_id=?")) {
                        copy.setLong(1, newMilestone); copy.setLong(2, oldMilestone); copy.executeUpdate();
                    }
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO lucky_wheel_rank_reward(event_id,rank_no,slot_no,item_template_id,quantity,options_data) "
                        + "SELECT ?,rank_no,slot_no,item_template_id,quantity,options_data FROM lucky_wheel_rank_reward WHERE event_id=?")) {
            ps.setLong(1, targetId); ps.setLong(2, sourceId); ps.executeUpdate();
        }
    }

    public static Event getEditableEvent() throws Exception { return ensureDraft(); }

    public static Event getActiveEvent() throws Exception {
        ready();
        return getActiveEventRaw();
    }

    private static Event getActiveEventRaw() throws Exception {
        return queryOne("SELECT * FROM lucky_wheel_event WHERE status=1 ORDER BY id DESC LIMIT 1");
    }

    public static Event getFinalizingEvent() throws Exception {
        ready();
        return queryOne("SELECT * FROM lucky_wheel_event WHERE status=2 ORDER BY id DESC LIMIT 1");
    }

    private static Event getRunningEventRaw() throws Exception {
        return queryOne("SELECT * FROM lucky_wheel_event WHERE status IN(1,2) ORDER BY id DESC LIMIT 1");
    }

    public static Event getEvent(long id) throws Exception {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM lucky_wheel_event WHERE id=?")) {
            ps.setLong(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapEvent(rs) : null; }
        }
    }

    private static Event queryOne(String sql) throws Exception {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapEvent(rs) : null;
        }
    }

    private static Event mapEvent(ResultSet rs) throws Exception {
        Event event = new Event();
        event.id = rs.getLong("id"); event.status = rs.getByte("status"); event.configVersion = rs.getInt("config_version");
        event.durationDays = rs.getInt("duration_days"); event.durationHours = rs.getInt("duration_hours");
        event.durationMinutes = rs.getInt("duration_minutes"); event.priceOne = rs.getInt("price_one");
        event.priceTen = rs.getInt("price_ten"); event.pityLimit = rs.getInt("pity_limit");
        event.startedAt = rs.getTimestamp("started_at"); event.endsAt = rs.getTimestamp("ends_at"); event.finishedAt = rs.getTimestamp("finished_at");
        return event;
    }

    public static List<Reward> getRewards(long eventId, int version) throws Exception {
        List<Reward> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT position_no,item_template_id,quantity,options_data,rate_units FROM lucky_wheel_reward "
                        + "WHERE event_id=? AND config_version=? ORDER BY position_no")) {
            ps.setLong(1, eventId); ps.setInt(2, version);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reward reward = new Reward(); reward.eventId = eventId; reward.configVersion = version;
                    reward.position = rs.getInt(1); reward.itemTemplateId = rs.getInt(2); reward.quantity = rs.getInt(3);
                    reward.optionsData = rs.getString(4); reward.rateUnits = rs.getLong(5); result.add(reward);
                }
            }
        }
        return result;
    }

    public static PlayerState getOrCreatePlayer(long eventId, long playerId, int pityLimit) throws Exception {
        AlyraManager.executeUpdate("INSERT IGNORE INTO lucky_wheel_player(event_id,player_id,pity_remaining,reached_at) VALUES(?,?,?,NOW())",
                eventId, playerId, pityLimit);
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT total_spins,pity_remaining,reached_at FROM lucky_wheel_player WHERE event_id=? AND player_id=?")) {
            ps.setLong(1, eventId); ps.setLong(2, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Khong tai duoc du lieu vong quay cua nguoi choi");
                PlayerState state = new PlayerState(); state.eventId = eventId; state.playerId = playerId;
                state.totalSpins = rs.getInt(1); state.pityRemaining = rs.getInt(2); state.reachedAt = rs.getTimestamp(3); return state;
            }
        }
    }

    public static SpinResult findSpin(long eventId, long playerId, long requestKey) throws Exception {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT id,spin_count,cost,total_spins_after,pity_after FROM lucky_wheel_spin WHERE event_id=? AND player_id=? AND request_key=?")) {
            ps.setLong(1, eventId); ps.setLong(2, playerId); ps.setLong(3, requestKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SpinResult result = new SpinResult(); result.spinId = rs.getLong(1); result.requestKey = requestKey;
                result.count = rs.getInt(2); result.cost = rs.getInt(3); result.totalSpins = rs.getInt(4); result.pityRemaining = rs.getInt(5);
                result.duplicate = true; loadSpinResults(con, result); return result;
            }
        }
    }

    private static void loadSpinResults(Connection con, SpinResult result) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT position_no,item_template_id,quantity,options_data FROM lucky_wheel_spin_result WHERE spin_id=? ORDER BY sequence_no")) {
            ps.setLong(1, result.spinId); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reward reward = new Reward(); reward.position = rs.getInt(1); reward.itemTemplateId = rs.getInt(2);
                    reward.quantity = rs.getInt(3); reward.optionsData = rs.getString(4); result.rewards.add(reward);
                }
            }
        }
    }

    public static long saveSpin(Event event, long playerId, long requestKey, int count, int cost,
            int totalAfter, int pityAfter, List<Reward> rewards) throws Exception {
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                long spinId;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO lucky_wheel_spin(event_id,player_id,request_key,config_version,spin_count,cost,total_spins_after,pity_after) "
                                + "VALUES(?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, event.id); ps.setLong(2, playerId); ps.setLong(3, requestKey); ps.setInt(4, event.configVersion);
                    ps.setInt(5, count); ps.setInt(6, cost); ps.setInt(7, totalAfter); ps.setInt(8, pityAfter); ps.executeUpdate(); spinId = generatedId(ps);
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO lucky_wheel_spin_result(spin_id,sequence_no,position_no,item_template_id,quantity,options_data) VALUES(?,?,?,?,?,?)")) {
                    for (int i = 0; i < rewards.size(); i++) {
                        Reward reward = rewards.get(i); ps.setLong(1, spinId); ps.setInt(2, i + 1); ps.setInt(3, reward.position);
                        ps.setInt(4, reward.itemTemplateId); ps.setInt(5, reward.quantity); ps.setString(6, reward.optionsData); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE lucky_wheel_player SET total_spins=?,pity_remaining=?,reached_at=NOW() WHERE event_id=? AND player_id=?")) {
                    ps.setInt(1, totalAfter); ps.setInt(2, pityAfter); ps.setLong(3, event.id); ps.setLong(4, playerId); ps.executeUpdate();
                }
                con.commit(); return spinId;
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    public static List<Milestone> getMilestones(long eventId) throws Exception {
        List<Milestone> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT id,target_spins FROM lucky_wheel_milestone WHERE event_id=? ORDER BY sort_order,target_spins,id")) {
            ps.setLong(1, eventId); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Milestone milestone = new Milestone(); milestone.id = rs.getLong(1); milestone.eventId = eventId; milestone.targetSpins = rs.getInt(2);
                    try (PreparedStatement rewards = con.prepareStatement(
                            "SELECT item_template_id,quantity,options_data FROM lucky_wheel_milestone_reward WHERE milestone_id=? ORDER BY slot_no")) {
                        rewards.setLong(1, milestone.id); try (ResultSet rr = rewards.executeQuery()) {
                            while (rr.next()) { Reward r = new Reward(); r.itemTemplateId = rr.getInt(1); r.quantity = rr.getInt(2); r.optionsData = rr.getString(3); milestone.rewards.add(r); }
                        }
                    }
                    result.add(milestone);
                }
            }
        }
        return result;
    }

    public static List<Integer> getClaimedMilestones(long eventId, long playerId) throws Exception {
        List<Integer> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT target_spins FROM lucky_wheel_milestone_claim WHERE event_id=? AND player_id=?")) {
            ps.setLong(1, eventId); ps.setLong(2, playerId); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(rs.getInt(1)); }
        }
        return result;
    }

    public static boolean markMilestoneClaimed(long eventId, long playerId, int targetSpins) throws Exception {
        return AlyraManager.executeUpdate(
                "INSERT IGNORE INTO lucky_wheel_milestone_claim(event_id,player_id,target_spins) VALUES(?,?,?)",
                eventId, playerId, targetSpins) > 0;
    }

    public static void unmarkMilestoneClaimed(long eventId, long playerId, int targetSpins) {
        try { AlyraManager.executeUpdate("DELETE FROM lucky_wheel_milestone_claim WHERE event_id=? AND player_id=? AND target_spins=?",
                eventId, playerId, targetSpins); } catch (Exception ignored) {}
    }

    public static List<RankEntry> getRanking(long eventId) throws Exception {
        List<RankEntry> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT s.player_id,COALESCE(p.name,CONCAT('#',s.player_id)),s.total_spins FROM lucky_wheel_player s "
                        + "LEFT JOIN player p ON p.id=s.player_id WHERE s.event_id=? AND s.total_spins>=? "
                        + "ORDER BY s.total_spins DESC,s.reached_at ASC,s.player_id ASC LIMIT ?")) {
            ps.setLong(1, eventId); ps.setInt(2, LuckyWheelConstants.MIN_RANK_SPINS); ps.setInt(3, LuckyWheelConstants.RANK_LIMIT);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1; while (rs.next()) { RankEntry entry = new RankEntry(); entry.rank = rank++; entry.playerId = rs.getLong(1); entry.playerName = rs.getString(2); entry.totalSpins = rs.getInt(3); result.add(entry); }
            }
        }
        return result;
    }

    public static List<RankReward> getRankRewards(long eventId) throws Exception {
        List<RankReward> result = new ArrayList<>();
        for (int rank = 1; rank <= LuckyWheelConstants.RANK_LIMIT; rank++) { RankReward row = new RankReward(); row.rank = rank; result.add(row); }
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT rank_no,item_template_id,quantity,options_data FROM lucky_wheel_rank_reward WHERE event_id=? ORDER BY rank_no,slot_no")) {
            ps.setLong(1, eventId); try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { int rank = rs.getInt(1); if (rank < 1 || rank > result.size()) continue; Reward reward = new Reward(); reward.itemTemplateId = rs.getInt(2); reward.quantity = rs.getInt(3); reward.optionsData = rs.getString(4); result.get(rank - 1).rewards.add(reward); }
            }
        }
        return result;
    }

    public static void saveConfiguration(Event values, List<Reward> rewards) throws Exception {
        int nextVersion = values.configVersion + 1;
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE lucky_wheel_event SET config_version=?,duration_days=?,duration_hours=?,duration_minutes=?,price_one=?,price_ten=?,pity_limit=? WHERE id=?")) {
                    ps.setInt(1, nextVersion); ps.setInt(2, values.durationDays); ps.setInt(3, values.durationHours); ps.setInt(4, values.durationMinutes);
                    ps.setInt(5, values.priceOne); ps.setInt(6, values.priceTen); ps.setInt(7, values.pityLimit); ps.setLong(8, values.id); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO lucky_wheel_reward(event_id,config_version,position_no,item_template_id,quantity,options_data,rate_units) VALUES(?,?,?,?,?,?,?)")) {
                    for (Reward reward : rewards) { ps.setLong(1, values.id); ps.setInt(2, nextVersion); ps.setInt(3, reward.position); ps.setInt(4, reward.itemTemplateId); ps.setInt(5, reward.quantity); ps.setString(6, reward.optionsData); ps.setLong(7, reward.rateUnits); ps.addBatch(); }
                    ps.executeBatch();
                }
                con.commit(); values.configVersion = nextVersion;
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    public static void replaceMilestones(long eventId, List<Milestone> milestones) throws Exception {
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE r FROM lucky_wheel_milestone_reward r JOIN lucky_wheel_milestone m ON m.id=r.milestone_id WHERE m.event_id=?")) { ps.setLong(1, eventId); ps.executeUpdate(); }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM lucky_wheel_milestone WHERE event_id=?")) { ps.setLong(1, eventId); ps.executeUpdate(); }
                int order = 0;
                for (Milestone milestone : milestones) {
                    long id;
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO lucky_wheel_milestone(event_id,target_spins,sort_order) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setLong(1, eventId); ps.setInt(2, milestone.targetSpins); ps.setInt(3, order++); ps.executeUpdate(); id = generatedId(ps);
                    }
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO lucky_wheel_milestone_reward(milestone_id,slot_no,item_template_id,quantity,options_data) VALUES(?,?,?,?,?)")) {
                        int slot = 1; for (Reward reward : milestone.rewards) { ps.setLong(1, id); ps.setInt(2, slot++); ps.setInt(3, reward.itemTemplateId); ps.setInt(4, reward.quantity); ps.setString(5, reward.optionsData); ps.addBatch(); } ps.executeBatch();
                    }
                }
                con.commit();
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    public static void replaceRankRewards(long eventId, List<RankReward> ranks) throws Exception {
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM lucky_wheel_rank_reward WHERE event_id=?")) { ps.setLong(1, eventId); ps.executeUpdate(); }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO lucky_wheel_rank_reward(event_id,rank_no,slot_no,item_template_id,quantity,options_data) VALUES(?,?,?,?,?,?)")) {
                    for (RankReward rank : ranks) { int slot = 1; for (Reward reward : rank.rewards) { ps.setLong(1, eventId); ps.setInt(2, rank.rank); ps.setInt(3, slot++); ps.setInt(4, reward.itemTemplateId); ps.setInt(5, reward.quantity); ps.setString(6, reward.optionsData); ps.addBatch(); } } ps.executeBatch();
                }
                con.commit();
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    public static void activate(Event event) throws Exception {
        long duration = (((long) event.durationDays * 24 + event.durationHours) * 60 + event.durationMinutes) * 60_000L;
        int changed = AlyraManager.executeUpdate(
                "UPDATE lucky_wheel_event SET status=1,started_at=NOW(),ends_at=?,finished_at=NULL WHERE id=? AND status=0",
                new Timestamp(System.currentTimeMillis() + duration), event.id);
        if (changed == 0) throw new IllegalStateException("Su kien khong o trang thai co the kich hoat");
    }

    public static boolean beginFinalize(long eventId) throws Exception {
        return AlyraManager.executeUpdate("UPDATE lucky_wheel_event SET status=2 WHERE id=? AND status=1", eventId) > 0;
    }

    public static void markFinished(long eventId) throws Exception {
        AlyraManager.executeUpdate("UPDATE lucky_wheel_event SET status=3,finished_at=NOW() WHERE id=? AND status=2", eventId);
    }

    public static boolean deliveryExists(long eventId, long playerId, String type, long referenceId) throws Exception {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM lucky_wheel_delivery WHERE event_id=? AND player_id=? AND delivery_type=? AND reference_id=?")) {
            ps.setLong(1, eventId); ps.setLong(2, playerId); ps.setString(3, type); ps.setLong(4, referenceId); try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public static List<Long> getMilestoneEligiblePlayers(long eventId, int target) throws Exception {
        List<Long> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "SELECT p.player_id FROM lucky_wheel_player p LEFT JOIN lucky_wheel_milestone_claim c "
                        + "ON c.event_id=p.event_id AND c.player_id=p.player_id AND c.target_spins=? "
                        + "WHERE p.event_id=? AND p.total_spins>=? AND c.target_spins IS NULL")) {
            ps.setInt(1, target); ps.setLong(2, eventId); ps.setInt(3, target); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(rs.getLong(1)); }
        }
        return result;
    }

    public static void recordDelivery(Connection con, long eventId, long playerId, String type, long referenceId, long mailId) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO lucky_wheel_delivery(event_id,player_id,delivery_type,reference_id,mail_id) VALUES(?,?,?,?,?)")) {
            ps.setLong(1, eventId); ps.setLong(2, playerId); ps.setString(3, type); ps.setLong(4, referenceId); ps.setLong(5, mailId); ps.executeUpdate();
        }
    }

    private static long generatedId(PreparedStatement ps) throws Exception {
        try (ResultSet rs = ps.getGeneratedKeys()) { if (!rs.next()) throw new IllegalStateException("Khong tao duoc ID"); return rs.getLong(1); }
    }
}
