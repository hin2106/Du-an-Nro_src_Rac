package luckywheel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import luckywheel.LuckyWheelModels.Milestone;
import luckywheel.LuckyWheelModels.RankEntry;
import luckywheel.LuckyWheelModels.RankReward;
import luckywheel.LuckyWheelModels.Reward;
import luckywheel.LuckyWheelModels.Snapshot;
import luckywheel.LuckyWheelModels.SpinResult;
import network.Message;
import player.Player;
import server.Client;
import utils.Logger;

public final class LuckyWheelHandler {
    private static final LuckyWheelHandler INSTANCE = new LuckyWheelHandler();

    private LuckyWheelHandler() {}

    public static LuckyWheelHandler gI() { return INSTANCE; }

    public void controller(Player player, Message message) {
        try {
            byte action = message.reader().readByte();
            switch (action) {
                case LuckyWheelConstants.ACTION_GET_STATE -> sendState(player);
                case LuckyWheelConstants.ACTION_SPIN -> spin(player, message.reader().readUnsignedByte(), message.reader().readInt());
                case LuckyWheelConstants.ACTION_CLAIM_MILESTONE -> claimMilestone(player, message.reader().readInt());
                default -> sendError(player, action, "Yeu cau Vong quay may man khong hop le");
            }
        } catch (Exception e) {
            Logger.logException(LuckyWheelHandler.class, e);
        }
    }

    public void sendState(Player player) {
        Message msg = null;
        try {
            msg = new Message(LuckyWheelConstants.CMD);
            msg.writer().writeByte(LuckyWheelConstants.ACTION_GET_STATE);
            Snapshot snapshot;
            try { snapshot = LuckyWheelService.gI().snapshot(player); }
            catch (Exception inactive) { msg.writer().writeBoolean(false); player.sendMessage(msg); return; }
            msg.writer().writeBoolean(true);
            msg.writer().writeLong(snapshot.event.id);
            msg.writer().writeInt(snapshot.event.configVersion);
            msg.writer().writeLong(snapshot.event.endsAt.getTime());
            msg.writer().writeInt(snapshot.event.priceOne);
            msg.writer().writeInt(snapshot.event.priceTen);
            msg.writer().writeInt(snapshot.event.pityLimit);
            msg.writer().writeInt(snapshot.player.totalSpins);
            msg.writer().writeInt(snapshot.player.pityRemaining);

            msg.writer().writeByte(snapshot.rewards.size());
            for (Reward reward : snapshot.rewards) writeReward(msg, reward, true);

            msg.writer().writeShort(snapshot.milestones.size());
            for (Milestone milestone : snapshot.milestones) {
                msg.writer().writeLong(milestone.id);
                msg.writer().writeInt(milestone.targetSpins);
                msg.writer().writeBoolean(snapshot.claimedMilestones.contains(milestone.targetSpins));
                msg.writer().writeShort(milestone.rewards.size());
                for (Reward reward : milestone.rewards) writeReward(msg, reward, false);
            }

            msg.writer().writeByte(snapshot.ranking.size());
            for (RankEntry entry : snapshot.ranking) {
                msg.writer().writeByte(entry.rank); msg.writer().writeLong(entry.playerId);
                msg.writer().writeUTF(entry.playerName == null ? "" : entry.playerName); msg.writer().writeInt(entry.totalSpins);
            }

            msg.writer().writeByte(snapshot.rankRewards.size());
            for (RankReward rank : snapshot.rankRewards) {
                msg.writer().writeByte(rank.rank); msg.writer().writeShort(rank.rewards.size());
                for (Reward reward : rank.rewards) writeReward(msg, reward, false);
            }
            player.sendMessage(msg);
        } catch (Exception e) { Logger.logException(LuckyWheelHandler.class, e); }
        finally { if (msg != null) msg.cleanup(); }
    }

    private void spin(Player player, int count, long requestKey) {
        Message msg = null;
        try {
            SpinResult result = LuckyWheelService.gI().spin(player, count, requestKey);
            msg = new Message(LuckyWheelConstants.CMD);
            msg.writer().writeByte(LuckyWheelConstants.ACTION_SPIN);
            msg.writer().writeBoolean(true); msg.writer().writeUTF("");
            msg.writer().writeLong(result.requestKey); msg.writer().writeInt(result.cost);
            msg.writer().writeInt(result.totalSpins); msg.writer().writeInt(result.pityRemaining);
            msg.writer().writeByte(result.rewards.size());
            for (Reward reward : result.rewards) writeReward(msg, reward, true);
            player.sendMessage(msg);
        } catch (Exception e) { sendError(player, LuckyWheelConstants.ACTION_SPIN, safeMessage(e, "Khong the thuc hien luot quay")); }
        finally { if (msg != null) msg.cleanup(); }
    }

    private void claimMilestone(Player player, long milestoneId) {
        Message msg = null;
        try {
            List<Reward> rewards = LuckyWheelService.gI().claimMilestone(player, milestoneId);
            msg = new Message(LuckyWheelConstants.CMD);
            msg.writer().writeByte(LuckyWheelConstants.ACTION_CLAIM_MILESTONE);
            msg.writer().writeBoolean(true); msg.writer().writeUTF("Nhan qua moc thanh cong"); msg.writer().writeLong(milestoneId);
            msg.writer().writeShort(rewards.size()); for (Reward reward : rewards) writeReward(msg, reward, false);
            player.sendMessage(msg); sendState(player);
        } catch (Exception e) { sendError(player, LuckyWheelConstants.ACTION_CLAIM_MILESTONE, safeMessage(e, "Khong the nhan qua moc")); }
        finally { if (msg != null) msg.cleanup(); }
    }

    private void writeReward(Message msg, Reward reward, boolean includePosition) throws IOException {
        if (includePosition) msg.writer().writeByte(reward.position);
        msg.writer().writeShort(reward.itemTemplateId); msg.writer().writeInt(reward.quantity);
        msg.writer().writeUTF(reward.optionsData == null ? "[]" : reward.optionsData);
        if (includePosition) msg.writer().writeLong(reward.rateUnits);
    }

    private void sendError(Player player, byte action, String text) {
        Message msg = null;
        try {
            msg = new Message(LuckyWheelConstants.CMD); msg.writer().writeByte(action);
            msg.writer().writeBoolean(false); msg.writer().writeUTF(text == null ? "Co loi xay ra" : text); player.sendMessage(msg);
        } catch (Exception e) { Logger.logException(LuckyWheelHandler.class, e); }
        finally { if (msg != null) msg.cleanup(); }
    }

    public void broadcastEventEnded() {
        broadcastSimple(LuckyWheelConstants.ACTION_EVENT_ENDED);
    }

    public void broadcastConfigChanged() {
        broadcastSimple(LuckyWheelConstants.ACTION_CONFIG_CHANGED);
    }

    private void broadcastSimple(byte action) {
        List<Player> players = new ArrayList<>(Client.gI().getPlayers());
        for (Player player : players) {
            Message msg = null;
            try { msg = new Message(LuckyWheelConstants.CMD); msg.writer().writeByte(action); player.sendMessage(msg); }
            catch (Exception ignored) {} finally { if (msg != null) msg.cleanup(); }
        }
    }

    private String safeMessage(Exception e, String fallback) {
        return e.getMessage() == null || e.getMessage().isBlank() ? fallback : e.getMessage();
    }
}
