package dragonpass;

import dragonpass.DragonPassModels.Reward;
import dragonpass.DragonPassModels.Snapshot;
import dragonpass.DragonPassModels.TaskState;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import managers.GiftCodeManager;
import server.Client;
import network.Message;
import player.Player;
import services.GiftCodeService;
import services.GiftCodeService.GiftItemInfo;
import services.GiftCodeService.RedeemResult;
import utils.Logger;

public final class DragonPassHandler {
    private static final DragonPassHandler INSTANCE = new DragonPassHandler();

    private DragonPassHandler() {}

    public static DragonPassHandler gI() { return INSTANCE; }

    public void controller(Player player, Message message) {
        try {
            byte action = message.reader().readByte();
            switch (action) {
                case DragonPassConstants.ACTION_GET_STATE -> sendState(player);
                case DragonPassConstants.ACTION_BUY -> buy(player, message.reader().readByte());
                case DragonPassConstants.ACTION_CLAIM_REWARD ->
                    claimReward(player, message.reader().readUnsignedByte(), message.reader().readByte());
                case DragonPassConstants.ACTION_CLAIM_TASK -> claimTask(player, message.reader().readInt());
                case DragonPassConstants.ACTION_CLAIM_ALL_TASKS -> claimAllTasks(player);
                case DragonPassConstants.ACTION_REDEEM_GIFT_CODE ->
                    redeemGiftCode(player, message.reader().readUTF());
                case DragonPassConstants.ACTION_GET_TOP ->
                    top.TopBoardService.gI().sendBoard(player, message.reader().readByte());
                default -> sendSimpleResult(player, action, false, "Yêu cầu Dragon Pass không hợp lệ", 0);
            }
        } catch (Exception e) {
            Logger.logException(DragonPassHandler.class, e);
        }
    }

    public void sendState(Player player) {
        Message msg = null;
        try {
            Snapshot snapshot = DragonPassService.gI().snapshot(player);
            msg = new Message(DragonPassConstants.CMD);
            msg.writer().writeByte(DragonPassConstants.ACTION_GET_STATE);
            msg.writer().writeUTF(snapshot.season.key);
            msg.writer().writeByte(snapshot.season.phase);
            long activeUntil=snapshot.season.endsAt==null?0:snapshot.season.endsAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            msg.writer().writeLong(activeUntil);
            msg.writer().writeLong(activeUntil);
            msg.writer().writeInt(snapshot.player.exp);
            msg.writer().writeByte(snapshot.player.level());
            msg.writer().writeByte(snapshot.player.passType);
            msg.writer().writeShort(snapshot.season.normalPrice);
            msg.writer().writeShort(snapshot.season.plusPrice);
            msg.writer().writeBoolean(snapshot.hasNotification);

            Map<String, Reward> rewards = new HashMap<>();
            for (Reward reward : snapshot.rewards) rewards.put(key(reward.level, reward.track, reward.slot), reward);
            int levelCount=snapshot.season.phase==DragonPassConstants.PHASE_INACTIVE?0:DragonPassConstants.MAX_LEVEL;
            msg.writer().writeByte(levelCount);
            for (int level = 1; level <= levelCount; level++) {
                writeReward(msg, rewards.get(key(level, DragonPassConstants.TRACK_NORMAL, (byte) 1)));
                writeReward(msg, rewards.get(key(level, DragonPassConstants.TRACK_PLUS, (byte) 1)));
                writeReward(msg, rewards.get(key(level, DragonPassConstants.TRACK_PLUS, (byte) 2)));
                msg.writer().writeBoolean(snapshot.claimedNormal.contains(level));
                msg.writer().writeBoolean(snapshot.claimedPlus.contains(level));
            }

            msg.writer().writeByte(snapshot.tasks.size());
            for (TaskState task : snapshot.tasks) {
                msg.writer().writeInt(task.definition.id());
                msg.writer().writeByte(task.definition.type());
                msg.writer().writeUTF(task.definition.name());
                msg.writer().writeUTF(task.definition.description());
                msg.writer().writeShort(task.progress);
                msg.writer().writeShort(task.definition.target());
                msg.writer().writeShort(task.definition.exp());
                msg.writer().writeBoolean(task.claimed);
                msg.writer().writeBoolean(task.unlocked);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DragonPassHandler.class, e);
        } finally { if (msg != null) msg.cleanup(); }
    }

    private void writeReward(Message msg, Reward reward) throws IOException {
        boolean configured = reward != null && reward.configured();
        msg.writer().writeBoolean(configured);
        if (configured) {
            msg.writer().writeShort(reward.itemTemplateId);
            msg.writer().writeInt(reward.quantity);
            msg.writer().writeUTF(reward.optionsData == null ? "" : reward.optionsData);
        }
    }

    private String key(int level, byte track, byte slot) { return level + ":" + track + ":" + slot; }

    private void buy(Player player, byte passType) {
        try {
            int spent = DragonPassService.gI().purchase(player, passType);
            sendSimpleResult(player, DragonPassConstants.ACTION_BUY, true, "Kích hoạt Dragon Pass thành công", spent);
            sendState(player);
        } catch (Exception e) { sendSimpleResult(player, DragonPassConstants.ACTION_BUY, false, e.getMessage(), 0); }
    }

    private void claimReward(Player player, int level, byte track) {
        Message msg = null;
        try {
            List<Reward> rewards = DragonPassService.gI().claimReward(player, level, track);
            msg = new Message(DragonPassConstants.CMD);
            msg.writer().writeByte(DragonPassConstants.ACTION_CLAIM_REWARD);
            msg.writer().writeBoolean(true);
            msg.writer().writeUTF("Nhận thưởng thành công");
            msg.writer().writeByte(rewards.size());
            for (Reward reward : rewards) {
                msg.writer().writeShort(reward.itemTemplateId);
                msg.writer().writeInt(reward.quantity);
                msg.writer().writeUTF(reward.optionsData == null ? "" : reward.optionsData);
            }
            player.sendMessage(msg);
            sendState(player);
        } catch (Exception e) {
            try {
                msg = new Message(DragonPassConstants.CMD);
                msg.writer().writeByte(DragonPassConstants.ACTION_CLAIM_REWARD);
                msg.writer().writeBoolean(false);
                msg.writer().writeUTF(e.getMessage() == null ? "Không thể nhận thưởng" : e.getMessage());
                msg.writer().writeByte(0);
                player.sendMessage(msg);
            } catch (Exception ignored) {}
        } finally { if (msg != null) msg.cleanup(); }
    }

    private void claimTask(Player player, int taskId) {
        try {
            int exp = DragonPassService.gI().claimTask(player, taskId);
            sendSimpleResult(player, DragonPassConstants.ACTION_CLAIM_TASK, exp > 0,
                    exp > 0 ? "Đã nhận " + exp + " EXP Dragon Pass" : "Nhiệm vụ chưa đủ điều kiện nhận", exp);
            if (exp > 0) sendState(player);
        } catch (Exception e) { sendSimpleResult(player, DragonPassConstants.ACTION_CLAIM_TASK, false, e.getMessage(), 0); }
    }

    private void claimAllTasks(Player player) {
        try {
            int exp = DragonPassService.gI().claimAllTasks(player);
            sendSimpleResult(player, DragonPassConstants.ACTION_CLAIM_ALL_TASKS, exp > 0,
                    exp > 0 ? "Đã nhận " + exp + " EXP Dragon Pass" : "Không có điểm nhiệm vụ để nhận", exp);
            if (exp > 0) sendState(player);
        } catch (Exception e) { sendSimpleResult(player, DragonPassConstants.ACTION_CLAIM_ALL_TASKS, false, e.getMessage(), 0); }
    }

    private void redeemGiftCode(Player player, String code) {
        Message msg = null;
        try {
            GiftCodeManager.gI().reloadGiftCodeForPlayer(player);
            RedeemResult result = GiftCodeService.gI().redeemFromEvent(player, code);
            msg = new Message(DragonPassConstants.CMD);
            msg.writer().writeByte(DragonPassConstants.ACTION_REDEEM_GIFT_CODE);
            msg.writer().writeBoolean(result.success);
            msg.writer().writeUTF(result.message);
            msg.writer().writeByte(Math.min(255, result.rewards.size()));
            for (int i = 0; i < result.rewards.size() && i < 255; i++) {
                GiftItemInfo reward = result.rewards.get(i);
                msg.writer().writeByte(reward.type);
                msg.writer().writeShort(reward.idItem);
                msg.writer().writeInt(reward.quantity);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(DragonPassHandler.class, e);
            try {
                msg = new Message(DragonPassConstants.CMD);
                msg.writer().writeByte(DragonPassConstants.ACTION_REDEEM_GIFT_CODE);
                msg.writer().writeBoolean(false);
                msg.writer().writeUTF("Không thể đổi Gift Code");
                msg.writer().writeByte(0);
                player.sendMessage(msg);
            } catch (Exception ignored) {}
        } finally { if (msg != null) msg.cleanup(); }
    }

    private void sendSimpleResult(Player player, byte action, boolean success, String text, int value) {
        Message msg = null;
        try {
            msg = new Message(DragonPassConstants.CMD);
            msg.writer().writeByte(action);
            msg.writer().writeBoolean(success);
            msg.writer().writeUTF(text == null ? "Có lỗi xảy ra" : text);
            msg.writer().writeInt(value);
            player.sendMessage(msg);
        } catch (Exception e) { Logger.logException(DragonPassHandler.class, e); }
        finally { if (msg != null) msg.cleanup(); }
    }

    public void broadcastSeasonChanged() {
        for(Player player:new ArrayList<>(Client.gI().getPlayers())){
            Message msg=null;
            try{msg=new Message(DragonPassConstants.CMD);msg.writer().writeByte(DragonPassConstants.ACTION_SEASON_CHANGED);player.sendMessage(msg);}
            catch(Exception ignored){}finally{if(msg!=null)msg.cleanup();}
        }
    }
}
