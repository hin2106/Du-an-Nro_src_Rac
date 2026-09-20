package luckywheel;

import consts.ConstItem;
import data.AlyraManager;
import item.Item;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import luckywheel.LuckyWheelModels.Event;
import luckywheel.LuckyWheelModels.Milestone;
import luckywheel.LuckyWheelModels.PlayerState;
import luckywheel.LuckyWheelModels.RankEntry;
import luckywheel.LuckyWheelModels.RankReward;
import luckywheel.LuckyWheelModels.Reward;
import luckywheel.LuckyWheelModels.Snapshot;
import luckywheel.LuckyWheelModels.SpinResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Inventory;
import player.Player;
import server.Client;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import utils.Logger;

public final class LuckyWheelService {
    private static final LuckyWheelService INSTANCE = new LuckyWheelService();
    private final SecureRandom random = new SecureRandom();
    private ScheduledExecutorService scheduler;

    private LuckyWheelService() {}

    public static LuckyWheelService gI() { return INSTANCE; }

    public synchronized void initialize() {
        LuckyWheelRepository.ensureSchema();
        if (scheduler != null) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "LuckyWheel-Finalizer"); thread.setDaemon(true); return thread;
        });
        scheduler.scheduleAtFixedRate(this::checkExpiration, 5, 5, TimeUnit.SECONDS);
    }

    public Snapshot snapshot(Player player) throws Exception {
        Event event = requireActive();
        Snapshot snapshot = new Snapshot(); snapshot.event = event;
        snapshot.player = LuckyWheelRepository.getOrCreatePlayer(event.id, player.id, event.pityLimit);
        snapshot.rewards.addAll(LuckyWheelRepository.getRewards(event.id, event.configVersion));
        snapshot.milestones.addAll(LuckyWheelRepository.getMilestones(event.id));
        snapshot.claimedMilestones.addAll(LuckyWheelRepository.getClaimedMilestones(event.id, player.id));
        snapshot.ranking.addAll(LuckyWheelRepository.getRanking(event.id));
        snapshot.rankRewards.addAll(LuckyWheelRepository.getRankRewards(event.id));
        return snapshot;
    }

    public SpinResult spin(Player player, int count, long requestKey) throws Exception {
        if (count != 1 && count != 10) throw new IllegalArgumentException("So luot quay khong hop le");
        Event event = requireActive();
        synchronized (player) {
            SpinResult old = LuckyWheelRepository.findSpin(event.id, player.id, requestKey);
            if (old != null) return old;
            int requiredEmpty = count == 1 ? 3 : 12;
            if (countEmptySlots(player) < requiredEmpty)
                throw new IllegalStateException("Can it nhat " + requiredEmpty + " o trong hanh trang de quay");
            int cost = count == 1 ? event.priceOne : event.priceTen;
            if (cost <= 0 || player.inventory.gem < cost) throw new IllegalStateException("Khong du Ngoc de quay");

            List<Reward> pool = LuckyWheelRepository.getRewards(event.id, event.configVersion);
            validateRewards(pool);
            Map<Integer, Reward> byPosition = new HashMap<>();
            for (Reward reward : pool) byPosition.put(reward.position, reward);
            PlayerState state = LuckyWheelRepository.getOrCreatePlayer(event.id, player.id, event.pityLimit);
            int pity = Math.max(1, state.pityRemaining);
            List<Reward> results = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int position = pity <= 1 ? LuckyWheelConstants.PITY_SLOT : choosePosition(pool);
                Reward selected = byPosition.get(position);
                if (selected == null) throw new IllegalStateException("Cau hinh o qua khong hop le");
                results.add(selected.copy());
                pity = position == LuckyWheelConstants.PITY_SLOT ? event.pityLimit : pity - 1;
            }

            InventoryMutation mutation = simulateRewards(player, results, cost);
            int totalAfter = state.totalSpins + count;
            long spinId = LuckyWheelRepository.saveSpin(event, player.id, requestKey, count, cost, totalAfter, pity, results);
            applyMutation(player, mutation);

            SpinResult result = new SpinResult(); result.spinId = spinId; result.requestKey = requestKey;
            result.count = count; result.cost = cost; result.totalSpins = totalAfter; result.pityRemaining = pity; result.rewards.addAll(results);
            return result;
        }
    }

    public List<Reward> claimMilestone(Player player, long milestoneId) throws Exception {
        Event event = requireActive();
        synchronized (player) {
            PlayerState state = LuckyWheelRepository.getOrCreatePlayer(event.id, player.id, event.pityLimit);
            Milestone selected = null;
            for (Milestone milestone : LuckyWheelRepository.getMilestones(event.id)) if (milestone.id == milestoneId) selected = milestone;
            if (selected == null) throw new IllegalArgumentException("Moc thuong khong ton tai");
            if (state.totalSpins < selected.targetSpins) throw new IllegalStateException("Chua du luot quay de nhan moc nay");
            if (selected.rewards.isEmpty()) throw new IllegalStateException("Qua moc dang cap nhat");
            InventoryMutation mutation = simulateRewards(player, selected.rewards, 0);
            if (!LuckyWheelRepository.markMilestoneClaimed(event.id, player.id, selected.targetSpins))
                throw new IllegalStateException("Qua moc da duoc nhan");
            try { applyMutation(player, mutation); }
            catch (Exception e) { LuckyWheelRepository.unmarkMilestoneClaimed(event.id, player.id, selected.targetSpins); throw e; }
            return selected.rewards;
        }
    }

    private int choosePosition(List<Reward> rewards) {
        long value = random.nextLong(LuckyWheelConstants.RATE_TOTAL);
        long current = 0;
        for (Reward reward : rewards) {
            current += reward.rateUnits;
            if (value < current) return reward.position;
        }
        return LuckyWheelConstants.SLOT_COUNT;
    }

    private int countEmptySlots(Player player) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) if (item == null || !item.isNotNullItem()) count++;
        return count;
    }

    private InventoryMutation simulateRewards(Player player, List<Reward> rewards, int gemCost) {
        InventoryMutation result = new InventoryMutation();
        result.bag = InventoryService.gI().copyItemsBag(player);
        result.gold = player.inventory.gold;
        result.gem = (long) player.inventory.gem - gemCost;
        if (result.gem < 0) throw new IllegalStateException("Khong du Ngoc de quay");
        for (Reward reward : rewards) {
            if (reward.itemTemplateId == ConstItem.VANG) { result.gold += reward.quantity; continue; }
            if (reward.itemTemplateId == ConstItem.NGOC) { result.gem += reward.quantity; continue; }
            Item item = createRewardItem(reward);
            if (item == null || !InventoryService.gI().addItemList(result.bag, item))
                throw new IllegalStateException("Hanh trang khong du cho trong");
            result.bagChanged = true;
        }
        if (result.gold > Inventory.LIMIT_GOLD) throw new IllegalStateException("Vang da dat gioi han");
        if (result.gem > Integer.MAX_VALUE) throw new IllegalStateException("Ngoc da dat gioi han");
        return result;
    }

    private void applyMutation(Player player, InventoryMutation mutation) {
        player.inventory.itemsBag = mutation.bag;
        player.inventory.gold = mutation.gold;
        player.inventory.gem = (int) mutation.gem;
        if (mutation.bagChanged) InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
    }

    private Item createRewardItem(Reward reward) {
        Item item = ItemService.gI().createNewItem((short) reward.itemTemplateId, reward.quantity);
        if (item == null) return null;
        Object parsed = JSONValue.parse(reward.optionsData == null ? "[]" : reward.optionsData);
        if (parsed instanceof JSONArray array) {
            for (Object value : array) {
                if (!(value instanceof JSONObject option)) continue;
                Object id = option.get("id"), param = option.get("param");
                if (id instanceof Number && param instanceof Number)
                    item.itemOptions.add(new Item.ItemOption(((Number) id).intValue(), ((Number) param).intValue()));
            }
        }
        return item;
    }

    public void saveConfiguration(Event event, List<Reward> rewards) throws Exception {
        validateGeneral(event); validateRewards(rewards); LuckyWheelRepository.saveConfiguration(event, rewards);
    }

    public void activate(Event event) throws Exception {
        validateGeneral(event); validateRewards(LuckyWheelRepository.getRewards(event.id, event.configVersion));
        if (LuckyWheelRepository.getMilestones(event.id).isEmpty()) throw new IllegalStateException("Can cau hinh it nhat mot moc thuong");
        for (RankReward rank : LuckyWheelRepository.getRankRewards(event.id))
            if (rank.rewards.isEmpty()) throw new IllegalStateException("Chua cau hinh qua Top " + rank.rank);
        LuckyWheelRepository.activate(event);
        LuckyWheelHandler.gI().broadcastConfigChanged();
    }

    public void finishNow() throws Exception {
        Event event = LuckyWheelRepository.getActiveEvent();
        if (event == null) throw new IllegalStateException("Khong co su kien dang hoat dong");
        finalizeEvent(event);
    }

    private Event requireActive() throws Exception {
        Event event = LuckyWheelRepository.getActiveEvent();
        if (event == null) throw new IllegalStateException("Su kien Vong quay may man chua bat dau");
        if (!event.active()) {
            finalizeEvent(event);
            throw new IllegalStateException("Su kien Vong quay may man da ket thuc");
        }
        return event;
    }

    private void checkExpiration() {
        try {
            Event finalizing = LuckyWheelRepository.getFinalizingEvent();
            if (finalizing != null) { finalizeEvent(finalizing); return; }
            Event event = LuckyWheelRepository.getActiveEvent();
            if (event != null && !event.active()) finalizeEvent(event);
        } catch (Exception e) { Logger.logException(LuckyWheelService.class, e, "Loi tong ket Vong quay may man"); }
    }

    private synchronized void finalizeEvent(Event event) throws Exception {
        boolean justStarted = false;
        if (event.status == LuckyWheelConstants.STATUS_ACTIVE) {
            if (!LuckyWheelRepository.beginFinalize(event.id)) return;
            justStarted = true;
        }
        event.status = LuckyWheelConstants.STATUS_FINALIZING;
        if (justStarted) LuckyWheelHandler.gI().broadcastEventEnded();
        try {
            List<RankEntry> ranking = LuckyWheelRepository.getRanking(event.id);
            List<RankReward> rankRewards = LuckyWheelRepository.getRankRewards(event.id);
            for (RankEntry entry : ranking) {
                List<Reward> rewards = rankRewards.get(entry.rank - 1).rewards;
                sendRewardMail(event.id, entry.playerId, "RANK", entry.rank,
                        "Phan thuong Top " + entry.rank + " Vong quay may man",
                        "Chuc mung ban dat Top " + entry.rank + " voi " + entry.totalSpins + " luot quay trong su kien.", rewards);
            }
            for (Milestone milestone : LuckyWheelRepository.getMilestones(event.id)) {
                for (long playerId : LuckyWheelRepository.getMilestoneEligiblePlayers(event.id, milestone.targetSpins)) {
                    sendRewardMail(event.id, playerId, "MILESTONE", milestone.id,
                            "Qua moc Vong quay may man",
                            "Day la qua moc " + milestone.targetSpins + " luot quay ban da du dieu kien nhung chua nhan.", milestone.rewards);
                }
            }
            LuckyWheelRepository.markFinished(event.id);
            LuckyWheelRepository.ensureDraft();
        } catch (Exception e) {
            Logger.logException(LuckyWheelService.class, e, "Tong ket Vong quay may man that bai");
            throw e;
        }
    }

    private void sendRewardMail(long eventId, long playerId, String type, long referenceId,
            String title, String content, List<Reward> rewards) throws Exception {
        if (rewards == null || rewards.isEmpty() || LuckyWheelRepository.deliveryExists(eventId, playerId, type, referenceId)) return;
        try (Connection con = AlyraManager.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement exists = con.prepareStatement(
                        "SELECT 1 FROM lucky_wheel_delivery WHERE event_id=? AND player_id=? AND delivery_type=? AND reference_id=? FOR UPDATE")) {
                    exists.setLong(1, eventId); exists.setLong(2, playerId); exists.setString(3, type); exists.setLong(4, referenceId);
                    try (ResultSet rs = exists.executeQuery()) { if (rs.next()) { con.rollback(); return; } }
                }
                long now = System.currentTimeMillis(), mailId;
                try (PreparedStatement mail = con.prepareStatement(
                        "INSERT INTO player_mail(player_id,title,content,has_reward,is_read,status,created_at,expired_at) VALUES(?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    mail.setLong(1, playerId); mail.setString(2, title); mail.setString(3, content); mail.setBoolean(4, true);
                    mail.setBoolean(5, false); mail.setByte(6, (byte) 1); mail.setTimestamp(7, new Timestamp(now));
                    mail.setTimestamp(8, new Timestamp(now + LuckyWheelConstants.MAIL_EXPIRE_DAYS * 86_400_000L)); mail.executeUpdate();
                    try (ResultSet rs = mail.getGeneratedKeys()) { if (!rs.next()) throw new IllegalStateException("Khong tao duoc thu thuong"); mailId = rs.getLong(1); }
                }
                try (PreparedStatement rewardInsert = con.prepareStatement(
                        "INSERT INTO mail_reward(mail_id,reward_type,reward_id,amount,claimed,options_data) VALUES(?,?,?,?,0,?)")) {
                    for (Reward reward : rewards) {
                        byte rewardType = reward.itemTemplateId == ConstItem.VANG ? (byte) 2
                                : reward.itemTemplateId == ConstItem.NGOC ? (byte) 3 : (byte) 1;
                        rewardInsert.setLong(1, mailId); rewardInsert.setByte(2, rewardType);
                        rewardInsert.setLong(3, rewardType == 1 ? reward.itemTemplateId : 0);
                        rewardInsert.setInt(4, reward.quantity); rewardInsert.setString(5, reward.optionsData); rewardInsert.addBatch();
                    }
                    rewardInsert.executeBatch();
                }
                LuckyWheelRepository.recordDelivery(con, eventId, playerId, type, referenceId, mailId);
                con.commit();
            } catch (Exception e) { con.rollback(); throw e; }
        }
    }

    public static void validateGeneral(Event event) {
        long minutes = ((long) event.durationDays * 24 + event.durationHours) * 60 + event.durationMinutes;
        if (event.durationDays < 0 || event.durationHours < 0 || event.durationHours > 23
                || event.durationMinutes < 0 || event.durationMinutes > 59 || minutes <= 0)
            throw new IllegalArgumentException("Thoi luong su kien khong hop le");
        if (event.priceOne <= 0 || event.priceTen <= 0) throw new IllegalArgumentException("Gia quay phai lon hon 0");
        if (event.pityLimit <= 0) throw new IllegalArgumentException("So luot bao hiem phai lon hon 0");
    }

    public static void validateRewards(List<Reward> rewards) {
        if (rewards == null || rewards.size() != LuckyWheelConstants.SLOT_COUNT)
            throw new IllegalArgumentException("Phai cau hinh du 19 o qua");
        rewards.sort(Comparator.comparingInt(r -> r.position));
        Set<Integer> positions = new HashSet<>(); long total = 0;
        for (Reward reward : rewards) {
            if (!reward.configured() || !positions.add(reward.position)) throw new IllegalArgumentException("Cau hinh o qua khong hop le");
            total += reward.rateUnits;
        }
        if (total != LuckyWheelConstants.RATE_TOTAL) throw new IllegalArgumentException("Tong ti le phai bang 100%");
        if (rewards.get(rewards.size() - 1).position != LuckyWheelConstants.SLOT_COUNT
                || rewards.get(rewards.size() - 1).rateUnits < LuckyWheelConstants.LAST_SLOT_MIN_RATE)
            throw new IllegalArgumentException("O cuoi phai con it nhat 0.01% ti le");
    }

    private static final class InventoryMutation {
        List<Item> bag;
        long gold;
        long gem;
        boolean bagChanged;
    }
}
