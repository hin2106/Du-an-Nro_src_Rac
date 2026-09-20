using System;
using System.Collections.Generic;

namespace LuckyWheel
{
    public sealed class RewardData
    {
        public int position;
        public short itemId;
        public int quantity;
        public string options = "[]";
        public long rateUnits;
        public byte rewardType { get { return itemId == 76 ? (byte)2 : (itemId == 77 ? (byte)3 : (byte)1); } }
    }

    public sealed class MilestoneData
    {
        public long id;
        public int target;
        public bool claimed;
        public readonly List<RewardData> rewards = new List<RewardData>();
    }

    public sealed class RankEntry
    {
        public int rank;
        public long playerId;
        public string name;
        public int spins;
    }

    public sealed class RankRewardData
    {
        public int rank;
        public readonly List<RewardData> rewards = new List<RewardData>();
    }

    public sealed class State
    {
        public bool active;
        public long eventId;
        public int configVersion;
        public long endsAt;
        public int priceOne;
        public int priceTen;
        public int pityLimit;
        public int totalSpins;
        public int pityRemaining;
        public readonly List<RewardData> rewards = new List<RewardData>();
        public readonly List<MilestoneData> milestones = new List<MilestoneData>();
        public readonly List<RankEntry> ranking = new List<RankEntry>();
        public readonly List<RankRewardData> rankRewards = new List<RankRewardData>();
    }

    public static class Network
    {
        public const sbyte CMD = 67;
        public const byte GET_STATE = 0;
        public const byte SPIN = 1;
        public const byte CLAIM_MILESTONE = 2;
        private static int sequence;

        private static void Send(byte action, Action<Message> write)
        {
            Message message = null;
            try
            {
                message = new Message(CMD);
                message.writer().writeByte(action);
                if (write != null) write(message);
                Session_ME.gI().sendMessage(message);
            }
            catch (Exception) { }
            finally { if (message != null) message.cleanup(); }
        }

        public static void GetState() { Send(GET_STATE, null); }
        public static void Spin(int count)
        {
            int key = unchecked((int)mSystem.currentTimeMillis()) ^ (++sequence & 1023);
            Manager.Instance.PendingRequestKey = key;
            Send(SPIN, m => { m.writer().writeByte(count); m.writer().writeInt(key); });
        }
        public static void ClaimMilestone(long id) { Send(CLAIM_MILESTONE, m => m.writer().writeInt((int)id)); }
    }

    public sealed class Manager
    {
        private static readonly Manager instance = new Manager();
        public static Manager Instance { get { return instance; } }
        public State Current = new State();
        public int PendingRequestKey;
        public bool Submitting;
        public long SubmitStartedAt;
        public string ResultText;
        public float ResultSeconds;
        public readonly List<RewardData> LastRewards = new List<RewardData>();
        public int LastCost;

        public void HandleServerMessage(Message msg)
        {
            try
            {
                byte action = (byte)msg.reader().readByte();
                if (action == Network.GET_STATE) ReadState(msg);
                else if (action == Network.SPIN) ReadSpin(msg);
                else if (action == Network.CLAIM_MILESTONE) ReadMilestone(msg);
                else if (action == 3) DragonPass.DragonPassUI.OnLuckyWheelEnded();
                else if (action == 4) LuckyWheelUI.OnConfigChanged();
            }
            catch (Exception e)
            {
                Submitting = false;
                ShowResult("Không đọc được dữ liệu Vòng quay: " + e.Message);
            }
        }

        private void ReadState(Message msg)
        {
            State state = new State();
            state.active = msg.reader().readBoolean();
            if (!state.active)
            {
                Current = state;
                Submitting = false;
                DragonPass.DragonPassUI.OnLuckyWheelUnavailable();
                return;
            }
            state.eventId = msg.reader().readLong();
            state.configVersion = msg.reader().readInt();
            state.endsAt = msg.reader().readLong();
            state.priceOne = msg.reader().readInt();
            state.priceTen = msg.reader().readInt();
            state.pityLimit = msg.reader().readInt();
            state.totalSpins = msg.reader().readInt();
            state.pityRemaining = msg.reader().readInt();
            int rewardCount = msg.reader().readUnsignedByte();
            for (int i = 0; i < rewardCount; i++) state.rewards.Add(ReadReward(msg, true));
            int milestoneCount = msg.reader().readShort();
            for (int i = 0; i < milestoneCount; i++)
            {
                MilestoneData milestone = new MilestoneData();
                milestone.id = msg.reader().readLong(); milestone.target = msg.reader().readInt(); milestone.claimed = msg.reader().readBoolean();
                int count = msg.reader().readShort(); for (int j = 0; j < count; j++) milestone.rewards.Add(ReadReward(msg, false));
                state.milestones.Add(milestone);
            }
            int rankCount = msg.reader().readUnsignedByte();
            for (int i = 0; i < rankCount; i++)
            {
                RankEntry rank = new RankEntry(); rank.rank = msg.reader().readUnsignedByte(); rank.playerId = msg.reader().readLong();
                rank.name = msg.reader().readUTF(); rank.spins = msg.reader().readInt(); state.ranking.Add(rank);
            }
            int rankRewardCount = msg.reader().readUnsignedByte();
            for (int i = 0; i < rankRewardCount; i++)
            {
                RankRewardData rank = new RankRewardData(); rank.rank = msg.reader().readUnsignedByte();
                int count = msg.reader().readShort(); for (int j = 0; j < count; j++) rank.rewards.Add(ReadReward(msg, false)); state.rankRewards.Add(rank);
            }
            bool changed = Current.active && Current.configVersion != state.configVersion;
            Current = state;
            if (changed) LuckyWheelUI.OnStateReloaded();
        }

        private void ReadSpin(Message msg)
        {
            bool success = msg.reader().readBoolean(); string text = msg.reader().readUTF();
            if (!success) { Submitting = false; ShowResult(text); return; }
            long request = msg.reader().readLong(); LastCost = msg.reader().readInt();
            if ((int)request != PendingRequestKey) return;
            Submitting = false;
            int total = msg.reader().readInt(), pity = msg.reader().readInt();
            int count = msg.reader().readUnsignedByte(); LastRewards.Clear();
            for (int i = 0; i < count; i++) LastRewards.Add(ReadReward(msg, true));
            Current.totalSpins = total; Current.pityRemaining = pity;
            LuckyWheelUI.StartSpin(LastRewards);
        }

        private void ReadMilestone(Message msg)
        {
            bool success = msg.reader().readBoolean(); string text = msg.reader().readUTF();
            if (!success) { ShowResult(text); return; }
            long id = msg.reader().readLong(); int count = msg.reader().readShort(); LastRewards.Clear();
            for (int i = 0; i < count; i++) LastRewards.Add(ReadReward(msg, false));
            LuckyWheelUI.ShowRewards(LastRewards, "Nhận quà thành công");
        }

        private RewardData ReadReward(Message msg, bool full)
        {
            RewardData reward = new RewardData();
            if (full) reward.position = msg.reader().readUnsignedByte();
            reward.itemId = msg.reader().readShort(); reward.quantity = msg.reader().readInt(); reward.options = msg.reader().readUTF();
            if (full) reward.rateUnits = msg.reader().readLong(); return reward;
        }

        public void ShowResult(string text) { ResultText = text; ResultSeconds = 2.5f; }
    }
}
