package dragonpass;

import consts.ConstMob;
import consts.ConstItem;
import dragonpass.DragonPassModels.PlayerState;
import dragonpass.DragonPassModels.Reward;
import dragonpass.DragonPassModels.Season;
import dragonpass.DragonPassModels.Snapshot;
import dragonpass.DragonPassModels.TaskState;
import item.Item;
import data.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;
import player.Inventory;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Logger;

public final class DragonPassService {
    private static final int[] EARTH_MAPS = {0, 1, 2, 3, 4, 5, 6, 24, 27, 28, 29, 30, 42, 47};
    private static final int[] NAMEC_MAPS = {7, 8, 9, 10, 11, 12, 13, 25, 31, 32, 33, 34, 43};
    private static final int[] XAYDA_MAPS = {14, 15, 16, 17, 18, 19, 20, 26, 35, 36, 37, 38, 44, 52};

    private static final DragonPassService INSTANCE = new DragonPassService();
    private final ConcurrentHashMap<Long, Integer> knownGemBalances = new ConcurrentHashMap<>();
    private ScheduledExecutorService finalizer;

    private DragonPassService() {}

    public static DragonPassService gI() {
        return INSTANCE;
    }

    public synchronized void initialize() {
        DragonPassRepository.ensureSchema();
        if (finalizer != null) return;
        finalizer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "DragonPass-Finalizer"); thread.setDaemon(true); return thread;
        });
        finalizer.scheduleWithFixedDelay(this::checkState, 5, 30, TimeUnit.SECONDS);
    }

    public Snapshot snapshot(Player player) throws Exception {
        Season season = DragonPassRepository.getCurrentEvent();
        if (season == null) {
            Season inactive = DragonPassRepository.ensureDraft();
            inactive.phase = DragonPassConstants.PHASE_INACTIVE;
            Snapshot empty = new Snapshot(); empty.season = inactive; empty.player = new PlayerState();
            empty.player.playerId = player.id; empty.player.seasonId = inactive.id;
            return empty;
        }
        PlayerState state = DragonPassRepository.getOrCreatePlayer(player.id, season);
        Snapshot snapshot = new Snapshot();
        snapshot.season = season;
        snapshot.player = state;
        snapshot.rewards = DragonPassRepository.getRewards(season.id);
        Set<Integer> normalClaims = DragonPassRepository.getClaims(player.id, season.id, DragonPassConstants.TRACK_NORMAL);
        Set<Integer> plusClaims = DragonPassRepository.getClaims(player.id, season.id, DragonPassConstants.TRACK_PLUS);
        snapshot.claimedNormal.addAll(normalClaims);
        snapshot.claimedPlus.addAll(plusClaims);
        snapshot.tasks = DragonPassRepository.getTaskStates(player.id, season);
        snapshot.hasNotification = hasNotification(snapshot, normalClaims, plusClaims);
        return snapshot;
    }

    private boolean hasNotification(Snapshot snapshot, Set<Integer> normalClaims, Set<Integer> plusClaims) {
        if (snapshot.season.phase == DragonPassConstants.PHASE_ACTIVE) {
            for (TaskState task : snapshot.tasks) {
                if (task.unlocked && !task.claimed && task.progress >= task.definition.target()) return true;
            }
        }
        Set<String> configured = new HashSet<>();
        for (Reward reward : snapshot.rewards) {
            if (reward.configured()) configured.add(reward.level + ":" + reward.track + ":" + reward.slot);
        }
        int level = snapshot.player.level();
        if (snapshot.player.passType >= DragonPassConstants.PASS_NORMAL) {
            for (int i = 1; i <= level; i++) {
                if (!normalClaims.contains(i) && configured.contains(i + ":" + DragonPassConstants.TRACK_NORMAL + ":1")) return true;
            }
        }
        if (snapshot.player.passType >= DragonPassConstants.PASS_PLUS) {
            for (int i = 1; i <= level; i++) {
                if (!plusClaims.contains(i)
                        && configured.contains(i + ":" + DragonPassConstants.TRACK_PLUS + ":1")
                        && configured.contains(i + ":" + DragonPassConstants.TRACK_PLUS + ":2")) return true;
            }
        }
        return false;
    }

    public int purchase(Player player, byte requestedType) throws Exception {
        Season season = requireSeason();
        if (season.status == DragonPassConstants.STATUS_FINALIZING) throw new IllegalStateException("Dragon Pass đang phát thưởng");
        synchronized (player) {
            int spent = DragonPassRepository.purchase(player, season, requestedType, UUID.randomUUID().toString());
            Service.gI().sendMoney(player);
            return spent;
        }
    }

    public List<Reward> claimReward(Player player, int level, byte track) throws Exception {
        if (level < 1 || level > DragonPassConstants.MAX_LEVEL) throw new IllegalStateException("Cấp quà không hợp lệ");
        if (track != DragonPassConstants.TRACK_NORMAL && track != DragonPassConstants.TRACK_PLUS)
            throw new IllegalStateException("Hàng quà không hợp lệ");
        Season season = requireSeason();
        if (season.status == DragonPassConstants.STATUS_FINALIZING) throw new IllegalStateException("Dragon Pass đang phát thưởng");
        synchronized (player) {
            PlayerState state = DragonPassRepository.getOrCreatePlayer(player.id, season);
            if (state.level() < level) throw new IllegalStateException("Bạn chưa đạt cấp " + level);
            if (track == DragonPassConstants.TRACK_NORMAL && state.passType < DragonPassConstants.PASS_NORMAL)
                throw new IllegalStateException("Bạn chưa kích hoạt Dragon Pass thường");
            if (track == DragonPassConstants.TRACK_PLUS && state.passType < DragonPassConstants.PASS_PLUS)
                throw new IllegalStateException("Bạn chưa kích hoạt Dragon Pass Plus");

            List<Reward> rewards = DragonPassRepository.getLevelRewards(season.id, level, track);
            int requiredCount = track == DragonPassConstants.TRACK_PLUS ? 2 : 1;
            if (rewards.size() != requiredCount || rewards.stream().anyMatch(r -> !r.configured()))
                throw new IllegalStateException("Phần thưởng đang cập nhật");
            if (DragonPassRepository.getClaims(player.id, season.id, track).contains(level))
                throw new IllegalStateException("Phần thưởng đã được nhận");

            long goldToAdd = 0L;
            long gemToAdd = 0L;
            List<Item> bagSimulation = InventoryService.gI().copyItemsBag(player);
            boolean inventoryChanged = false;
            for (Reward reward : rewards) {
                if (reward.itemTemplateId == ConstItem.VANG) {
                    goldToAdd += reward.quantity;
                    continue;
                }
                if (reward.itemTemplateId == ConstItem.NGOC) {
                    gemToAdd += reward.quantity;
                    continue;
                }
                Item item = createRewardItem(reward);
                if (item == null) throw new IllegalStateException("Cấu hình vật phẩm không hợp lệ");
                if (!InventoryService.gI().addItemList(bagSimulation, item))
                    throw new IllegalStateException("Hành trang không đủ chỗ trống");
                inventoryChanged = true;
            }

            if (goldToAdd > Inventory.LIMIT_GOLD - player.inventory.gold)
                throw new IllegalStateException("Vàng đã đạt giới hạn, không thể nhận thưởng");
            if (gemToAdd > Integer.MAX_VALUE - (long) player.inventory.gem)
                throw new IllegalStateException("Ngọc đã đạt giới hạn, không thể nhận thưởng");

            if (!DragonPassRepository.markClaimed(player.id, season.id, level, track))
                throw new IllegalStateException("Phần thưởng đã được nhận");

            List<Item> originalBag = player.inventory.itemsBag;
            long originalGold = player.inventory.gold;
            int originalGem = player.inventory.gem;
            try {
                if (inventoryChanged) player.inventory.itemsBag = bagSimulation;
                player.inventory.gold += goldToAdd;
                player.inventory.gem += (int) gemToAdd;
                if (inventoryChanged) InventoryService.gI().sendItemBags(player);
                if (goldToAdd > 0 || gemToAdd > 0) Service.gI().sendMoney(player);
                return rewards;
            } catch (Exception e) {
                player.inventory.itemsBag = originalBag;
                player.inventory.gold = originalGold;
                player.inventory.gem = originalGem;
                DragonPassRepository.unmarkClaimed(player.id, season.id, level, track);
                throw e;
            }
        }
    }

    private Item createRewardItem(Reward reward) {
        Item item = ItemService.gI().createNewItem((short) reward.itemTemplateId, reward.quantity);
        if (item == null) return null;
        if (reward.optionsData == null || reward.optionsData.isBlank()) return item;
        Object parsed = JSONValue.parse(reward.optionsData);
        if (parsed instanceof JSONArray options) {
            for (Object value : options) {
                if (value instanceof JSONObject option) {
                    int id = ((Number) option.get("id")).intValue();
                    int param = ((Number) option.get("param")).intValue();
                    item.itemOptions.add(new Item.ItemOption(id, param));
                }
            }
        }
        return item;
    }

    public int claimTask(Player player, int taskId) throws Exception {
        return DragonPassRepository.claimTask(player.id, requireSeason(), taskId);
    }

    public int claimAllTasks(Player player) throws Exception {
        return DragonPassRepository.claimAllTasks(player.id, requireSeason());
    }

    public void onLogin(Player player) {
        if (isRealPlayer(player)) knownGemBalances.put(player.id, player.inventory.gem);
        record(player, DragonPassTask.LOGIN, 1);
        if (player != null && player.zone != null && player.zone.map != null) onMapVisited(player, player.zone.map.mapId);
    }

    /** Called whenever the authoritative balance is sent to the client. */
    public void onMoneyChanged(Player player) {
        if (!isRealPlayer(player)) return;
        int current = player.inventory.gem;
        Integer previous = knownGemBalances.put(player.id, current);
        if (previous != null && current < previous) onGemSpent(player);
    }

    public void onMobKilled(Player player, int mobTemplateId) {
        record(player, DragonPassTask.KILL_ANY_MOB, 1);
        if (mobTemplateId == ConstMob.MOC_NHAN) record(player, DragonPassTask.KILL_WOODEN_DUMMY, 1);
    }

    public void onBossKilled(Player player) {
        if (!isRealPlayer(player)) return;
        try {
            Season season = activeSeasonOrNull();
            if (season == null) return;
            if (!DragonPassRepository.isTaskClaimed(player.id, season, DragonPassTask.KILL_BOSS_1))
                DragonPassRepository.addTaskProgress(player.id, season, DragonPassTask.KILL_BOSS_1, 1);
            else if (!DragonPassRepository.isTaskClaimed(player.id, season, DragonPassTask.KILL_BOSS_2))
                DragonPassRepository.addTaskProgress(player.id, season, DragonPassTask.KILL_BOSS_2, 1);
            else if (!DragonPassRepository.isTaskClaimed(player.id, season, DragonPassTask.KILL_BOSS_3))
                DragonPassRepository.addTaskProgress(player.id, season, DragonPassTask.KILL_BOSS_3, 1);
        } catch (Exception e) { Logger.logException(DragonPassService.class, e); }
    }

    public void onMapVisited(Player player, int mapId) {
        if (!isRealPlayer(player)) return;
        try {
            Season season = activeSeasonOrNull();
            if (season == null) return;
            if (contains(EARTH_MAPS, mapId)) DragonPassRepository.visitMap(player.id, season, DragonPassTask.VISIT_EARTH, mapId);
            if (contains(NAMEC_MAPS, mapId)) DragonPassRepository.visitMap(player.id, season, DragonPassTask.VISIT_NAMEC, mapId);
            if (contains(XAYDA_MAPS, mapId)) DragonPassRepository.visitMap(player.id, season, DragonPassTask.VISIT_XAYDA, mapId);
        } catch (Exception e) { Logger.logException(DragonPassService.class, e); }
    }

    public void onSuperRankJoined(Player player) { record(player, DragonPassTask.JOIN_SUPER_RANK, 1); }
    public void onEquipmentUpgraded(Player player) { record(player, DragonPassTask.UPGRADE_EQUIPMENT, 1); }
    public void onEquipmentCrystalized(Player player) { record(player, DragonPassTask.CRYSTALIZE_EQUIPMENT, 1); }
    public void onGemSpent(Player player) { record(player, DragonPassTask.SPEND_GEM, 1); }
    public void onDragonBallCombined(Player player) { record(player, DragonPassTask.COMBINE_DRAGON_BALL, 1); }
    public void onTreasureCompleted(Player player) { record(player, DragonPassTask.COMPLETE_TREASURE, 1); }
    public void onSnakeWayCompleted(Player player) { record(player, DragonPassTask.COMPLETE_SNAKE_WAY, 1); }
    public void onCampCompleted(Player player) { record(player, DragonPassTask.COMPLETE_CAMP, 1); }
    public void onTournamentRoundTwoWon(Player player) { record(player, DragonPassTask.WIN_TOURNAMENT_ROUND_2, 1); }

    private void record(Player player, int taskId, int amount) {
        if (!isRealPlayer(player)) return;
        try {
            Season season = activeSeasonOrNull();
            if (season == null) return;
            DragonPassRepository.addTaskProgress(player.id, season, taskId, amount);
        }
        catch (Exception e) { Logger.logException(DragonPassService.class, e); }
    }

    private Season activeSeasonOrNull() throws Exception {
        Season season = DragonPassRepository.getCurrentEvent();
        return season != null && season.status == DragonPassConstants.STATUS_ACTIVE ? season : null;
    }

    private boolean isRealPlayer(Player player) {
        return player != null && player.isPl() && !player.isPet && !player.isBoss;
    }

    private boolean contains(int[] values, int value) {
        for (int candidate : values) if (candidate == value) return true;
        return false;
    }

    private Season requireSeason() {
        try {
            Season season = DragonPassRepository.getCurrentEvent();
            if (season == null) throw new IllegalStateException("Dragon Pass chưa được kích hoạt");
            return season;
        } catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new IllegalStateException("Không tải được Dragon Pass", e); }
    }

    public void activate(Season event) throws Exception {
        validateRewards(DragonPassRepository.getRewards(event.id));
        DragonPassRepository.activate(event);
        DragonPassHandler.gI().broadcastSeasonChanged();
    }

    public void enterSummary() throws Exception {
        Season event=DragonPassRepository.getCurrentEvent();
        if(event==null||event.status!=DragonPassConstants.STATUS_ACTIVE)throw new IllegalStateException("Không có Dragon Pass đang hoạt động");
        if(!DragonPassRepository.beginSummary(event.id))throw new IllegalStateException("Không thể chuyển sang tổng kết");
        DragonPassHandler.gI().broadcastSeasonChanged();
    }

    public void finishNow() throws Exception {
        Season event=DragonPassRepository.getCurrentEvent();
        if(event==null)throw new IllegalStateException("Không có Dragon Pass đang chạy");
        finalizeEvent(event);
    }

    private void checkState() {
        try {
            Season event=DragonPassRepository.getFinalizingEvent();
            if(event!=null){finalizeEvent(event);return;}
            event=DragonPassRepository.getCurrentEvent();
            if(event!=null&&event.status==DragonPassConstants.STATUS_ACTIVE&&event.endsAt!=null
                    &&!java.time.LocalDateTime.now().isBefore(event.endsAt)){
                DragonPassRepository.beginSummary(event.id);DragonPassHandler.gI().broadcastSeasonChanged();
            }
        }catch(Exception e){Logger.logException(DragonPassService.class,e,"Kiểm tra trạng thái Dragon Pass thất bại");}
    }

    private synchronized void finalizeEvent(Season event) throws Exception {
        if(event.status==DragonPassConstants.STATUS_ACTIVE||event.status==DragonPassConstants.STATUS_SUMMARY){
            if(!DragonPassRepository.beginFinalize(event.id))return;
            event=DragonPassRepository.getEvent(event.id);DragonPassHandler.gI().broadcastSeasonChanged();
        }
        if(event.status!=DragonPassConstants.STATUS_FINALIZING)throw new IllegalStateException("Dragon Pass không thể kết thúc ở trạng thái hiện tại");
        try{
            for(long playerId:DragonPassRepository.getParticipantIds(event.id))deliverUnclaimed(event,playerId);
            DragonPassRepository.markFinished(event.id);DragonPassRepository.ensureDraft();DragonPassHandler.gI().broadcastSeasonChanged();
        }catch(Exception e){Logger.logException(DragonPassService.class,e,"Tổng kết Dragon Pass thất bại");throw e;}
    }

    private void deliverUnclaimed(Season event,long playerId)throws Exception{
        DragonPassRepository.claimCompletedTaskExp(playerId,event);
        PlayerState state=DragonPassRepository.getOrCreatePlayer(playerId,event);
        Set<Integer> normal=DragonPassRepository.getClaims(playerId,event.id,DragonPassConstants.TRACK_NORMAL);
        Set<Integer> plus=DragonPassRepository.getClaims(playerId,event.id,DragonPassConstants.TRACK_PLUS);
        List<Entitlement> pending=new ArrayList<>();
        for(int level=1;level<=state.level();level++){
            if(state.passType>=DragonPassConstants.PASS_NORMAL&&!normal.contains(level))addEntitlement(pending,event,level,DragonPassConstants.TRACK_NORMAL);
            if(state.passType>=DragonPassConstants.PASS_PLUS&&!plus.contains(level))addEntitlement(pending,event,level,DragonPassConstants.TRACK_PLUS);
        }
        List<Entitlement> batch=new ArrayList<>();int attachmentCount=0,part=1;
        for(Entitlement entitlement:pending){
            if(!batch.isEmpty()&&attachmentCount+entitlement.rewards.size()>DragonPassConstants.MAIL_REWARD_LIMIT){
                sendMailBatch(event,playerId,batch,part++);batch=new ArrayList<>();attachmentCount=0;
            }
            batch.add(entitlement);attachmentCount+=entitlement.rewards.size();
        }
        if(!batch.isEmpty())sendMailBatch(event,playerId,batch,part);
    }

    private void addEntitlement(List<Entitlement> pending,Season event,int level,byte track)throws Exception{
        List<Reward> rewards=DragonPassRepository.getLevelRewards(event.id,level,track);
        int required=track==DragonPassConstants.TRACK_PLUS?2:1;
        if(rewards.size()==required&&rewards.stream().allMatch(Reward::configured))pending.add(new Entitlement(level,track,rewards));
    }

    private void sendMailBatch(Season event,long playerId,List<Entitlement> requested,int part)throws Exception{
        try(Connection con=AlyraManager.getConnection()){
            con.setAutoCommit(false);
            try{
                List<Entitlement> accepted=new ArrayList<>();
                try(PreparedStatement ps=con.prepareStatement("INSERT IGNORE INTO dragon_pass_delivery(season_id,player_id,level_no,track,mail_id) VALUES(?,?,?,?,NULL)")){
                    for(Entitlement e:requested){ps.setLong(1,event.id);ps.setLong(2,playerId);ps.setInt(3,e.level);ps.setByte(4,e.track);if(ps.executeUpdate()>0)accepted.add(e);}
                }
                if(accepted.isEmpty()){con.rollback();return;}
                long now=System.currentTimeMillis(),mailId;
                try(PreparedStatement ps=con.prepareStatement("INSERT INTO player_mail(player_id,title,content,has_reward,is_read,status,created_at,expired_at) VALUES(?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    ps.setLong(1,playerId);ps.setString(2,"Phần thưởng Dragon Pass"+(part>1?" - Phần "+part:""));
                    ps.setString(3,"Đây là các phần thưởng Dragon Pass bạn đã đủ điều kiện nhưng chưa nhận trước khi sự kiện kết thúc.");
                    ps.setBoolean(4,true);ps.setBoolean(5,false);ps.setByte(6,(byte)1);ps.setTimestamp(7,new Timestamp(now));
                    ps.setTimestamp(8,new Timestamp(now+DragonPassConstants.MAIL_EXPIRE_DAYS*86_400_000L));ps.executeUpdate();
                    try(ResultSet rs=ps.getGeneratedKeys()){if(!rs.next())throw new IllegalStateException("Không tạo được thư Dragon Pass");mailId=rs.getLong(1);}
                }
                try(PreparedStatement reward=con.prepareStatement("INSERT INTO mail_reward(mail_id,reward_type,reward_id,amount,claimed,options_data) VALUES(?,?,?,?,0,?)");
                        PreparedStatement claim=con.prepareStatement("INSERT IGNORE INTO dragon_pass_claim(player_id,season_id,level_no,track) VALUES(?,?,?,?)");
                        PreparedStatement delivered=con.prepareStatement("UPDATE dragon_pass_delivery SET mail_id=? WHERE season_id=? AND player_id=? AND level_no=? AND track=? AND mail_id IS NULL")){
                    for(Entitlement e:accepted){
                        for(Reward r:e.rewards){byte type=r.itemTemplateId==ConstItem.VANG?(byte)2:r.itemTemplateId==ConstItem.NGOC?(byte)3:(byte)1;
                            reward.setLong(1,mailId);reward.setByte(2,type);reward.setLong(3,type==1?r.itemTemplateId:0);reward.setInt(4,r.quantity);reward.setString(5,r.optionsData);reward.addBatch();}
                        claim.setLong(1,playerId);claim.setLong(2,event.id);claim.setInt(3,e.level);claim.setByte(4,e.track);claim.addBatch();
                        delivered.setLong(1,mailId);delivered.setLong(2,event.id);delivered.setLong(3,playerId);delivered.setInt(4,e.level);delivered.setByte(5,e.track);delivered.addBatch();
                    }
                    reward.executeBatch();claim.executeBatch();delivered.executeBatch();
                }
                con.commit();
            }catch(Exception e){con.rollback();throw e;}
        }
    }

    private void validateRewards(List<Reward> rewards){
        Set<String> configured=new HashSet<>();for(Reward r:rewards)if(r.configured())configured.add(r.level+":"+r.track+":"+r.slot);
        for(int level=1;level<=DragonPassConstants.MAX_LEVEL;level++){
            if(!configured.contains(level+":"+DragonPassConstants.TRACK_NORMAL+":1")
                    ||!configured.contains(level+":"+DragonPassConstants.TRACK_PLUS+":1")
                    ||!configured.contains(level+":"+DragonPassConstants.TRACK_PLUS+":2"))
                throw new IllegalStateException("Chưa cấu hình đủ quà cấp "+level);
        }
    }

    private static final class Entitlement{
        final int level;final byte track;final List<Reward> rewards;
        Entitlement(int level,byte track,List<Reward> rewards){this.level=level;this.track=track;this.rewards=rewards;}
    }
}
