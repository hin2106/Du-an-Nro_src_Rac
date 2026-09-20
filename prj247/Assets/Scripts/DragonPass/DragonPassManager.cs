using System;
using System.Collections.Generic;

namespace DragonPass
{
    public sealed class RewardData
    {
        public bool configured;
        public byte rewardType = 1;
        public short itemId;
        public int quantity;
        public string options;
    }

    public sealed class LevelData
    {
        public int level;
        public RewardData normal;
        public RewardData plus1;
        public RewardData plus2;
        public bool claimedNormal;
        public bool claimedPlus;
    }

    public sealed class TaskData
    {
        public int id;
        public byte type;
        public string name;
        public string description;
        public int progress;
        public int target;
        public int exp;
        public bool claimed;
        public bool unlocked;
    }

    public sealed class State
    {
        public string seasonKey = "";
        public byte phase;
        public long activeUntil;
        public long seasonUntil;
        public int exp;
        public int level;
        public byte passType;
        public int normalPrice;
        public int plusPrice;
        public bool hasNotification;
        public readonly List<LevelData> levels = new List<LevelData>();
        public readonly List<TaskData> tasks = new List<TaskData>();
    }

    public static class Network
    {
        public const sbyte CMD = -58;
        public const byte GET_STATE = 0;
        public const byte BUY = 1;
        public const byte CLAIM_REWARD = 2;
        public const byte CLAIM_TASK = 3;
        public const byte CLAIM_ALL_TASKS = 4;
        public const byte REDEEM_GIFT_CODE = 7;
        public const byte GET_TOP = 20;

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
        public static void Buy(byte passType) { Send(BUY, m => m.writer().writeByte(passType)); }
        public static void ClaimReward(int level, byte track)
        {
            Send(CLAIM_REWARD, m => { m.writer().writeByte(level); m.writer().writeByte(track); });
        }
        public static void ClaimTask(int taskId) { Send(CLAIM_TASK, m => m.writer().writeInt(taskId)); }
        public static void ClaimAllTasks() { Send(CLAIM_ALL_TASKS, null); }
        public static void RedeemGiftCode(string code)
        {
            Send(REDEEM_GIFT_CODE, m => m.writer().writeUTF(code ?? string.Empty));
        }
        public static void GetTop(byte board) { Send(GET_TOP, m => m.writer().writeByte(board)); }
    }

    public sealed class Manager
    {
        private static readonly Manager instance = new Manager();
        public static Manager Instance { get { return instance; } }
        public State Current = new State();
        public string ResultText;
        public float ResultSeconds;
        public List<RewardData> ClaimedRewards = new List<RewardData>();
        public float RewardPopupSeconds;
        public string RewardPopupText;
        public bool RewardPopupSuccess = true;
        public bool GiftCodeSubmitting;
        public bool SeasonChanged;

        public void HandleServerMessage(Message msg)
        {
            try
            {
                byte action = (byte)msg.reader().readByte();
                if (action == Network.GET_STATE) ReadState(msg);
                else if (action == Network.CLAIM_REWARD) ReadRewardResult(msg);
                else if (action == Network.BUY || action == Network.CLAIM_TASK || action == Network.CLAIM_ALL_TASKS)
                    ReadSimpleResult(msg, action);
                else if (action == Network.REDEEM_GIFT_CODE) ReadGiftCodeResult(msg);
                else if (action == Network.GET_TOP) TopBoardManager.Read(msg);
                else if (action == 6) Network.GetState();
            }
            catch (Exception e)
            {
                ResultText = "Không đọc được dữ liệu Dragon Pass: " + e.Message;
                ResultSeconds = 2f;
            }
        }

        private void ReadState(Message msg)
        {
            State state = new State();
            state.seasonKey = msg.reader().readUTF();
            state.phase = (byte)msg.reader().readByte();
            state.activeUntil = msg.reader().readLong();
            state.seasonUntil = msg.reader().readLong();
            state.exp = msg.reader().readInt();
            state.level = msg.reader().readUnsignedByte();
            state.passType = (byte)msg.reader().readByte();
            state.normalPrice = msg.reader().readShort();
            state.plusPrice = msg.reader().readShort();
            state.hasNotification = msg.reader().readBoolean();
            int levelCount = msg.reader().readUnsignedByte();
            for (int i = 1; i <= levelCount; i++)
            {
                LevelData level = new LevelData();
                level.level = i;
                level.normal = ReadReward(msg);
                level.plus1 = ReadReward(msg);
                level.plus2 = ReadReward(msg);
                level.claimedNormal = msg.reader().readBoolean();
                level.claimedPlus = msg.reader().readBoolean();
                state.levels.Add(level);
            }
            int taskCount = msg.reader().readUnsignedByte();
            for (int i = 0; i < taskCount; i++)
            {
                TaskData task = new TaskData();
                task.id = msg.reader().readInt();
                task.type = (byte)msg.reader().readByte();
                task.name = msg.reader().readUTF();
                task.description = msg.reader().readUTF();
                task.progress = msg.reader().readShort();
                task.target = msg.reader().readShort();
                task.exp = msg.reader().readShort();
                task.claimed = msg.reader().readBoolean();
                task.unlocked = msg.reader().readBoolean();
                state.tasks.Add(task);
            }

            // Ưu tiên nhiệm vụ đã đủ điều kiện nhưng chưa nhận, giữ nhiệm vụ
            // đang làm ở giữa và đẩy nhiệm vụ đã nhận xuống cuối danh sách.
            List<TaskData> readyTasks = new List<TaskData>();
            List<TaskData> pendingTasks = new List<TaskData>();
            List<TaskData> claimedTasks = new List<TaskData>();
            for (int i = 0; i < state.tasks.Count; i++)
            {
                TaskData task = state.tasks[i];
                bool completed = task.target > 0 && task.progress >= task.target;
                if (task.claimed) claimedTasks.Add(task);
                else if (completed) readyTasks.Add(task);
                else pendingTasks.Add(task);
            }
            state.tasks.Clear();
            state.tasks.AddRange(readyTasks);
            state.tasks.AddRange(pendingTasks);
            state.tasks.AddRange(claimedTasks);
            if (!string.IsNullOrEmpty(Current.seasonKey) && Current.seasonKey != state.seasonKey) SeasonChanged = true;
            Current = state;
            DragonPassUI.OnStateReloaded();
        }

        private RewardData ReadReward(Message msg)
        {
            RewardData reward = new RewardData();
            reward.configured = msg.reader().readBoolean();
            if (reward.configured)
            {
                reward.itemId = msg.reader().readShort();
                reward.quantity = msg.reader().readInt();
                reward.options = msg.reader().readUTF();
            }
            return reward;
        }

        private void ReadRewardResult(Message msg)
        {
            bool success = msg.reader().readBoolean();
            string text = msg.reader().readUTF();
            int count = msg.reader().readUnsignedByte();
            ClaimedRewards.Clear();
            for (int i = 0; i < count; i++)
            {
                RewardData reward = new RewardData();
                reward.configured = true;
                reward.itemId = msg.reader().readShort();
                reward.rewardType = reward.itemId == 76 ? (byte)2
                    : (reward.itemId == 77 ? (byte)3 : (byte)1);
                reward.quantity = msg.reader().readInt();
                reward.options = msg.reader().readUTF();
                ClaimedRewards.Add(reward);
            }
            if (success)
            {
                RewardPopupText = null;
                RewardPopupSuccess = true;
                RewardPopupSeconds = 5f;
            }
            else { ResultText = text; ResultSeconds = 2f; }
        }

        private void ReadSimpleResult(Message msg, byte action)
        {
            bool success = msg.reader().readBoolean();
            string text = msg.reader().readUTF();
            msg.reader().readInt();
            if (action == Network.BUY && success)
            {
                ClaimedRewards.Clear();
                RewardPopupText = text;
                RewardPopupSeconds = 5f;
                ResultText = null;
                ResultSeconds = 0f;
                return;
            }
            ResultText = text;
            ResultSeconds = 2f;
            if (!success && string.IsNullOrEmpty(ResultText)) ResultText = "Không thể thực hiện.";
        }

        private void ReadGiftCodeResult(Message msg)
        {
            bool success = msg.reader().readBoolean();
            string text = msg.reader().readUTF();
            int count = msg.reader().readUnsignedByte();
            ClaimedRewards.Clear();
            for (int i = 0; i < count; i++)
            {
                RewardData reward = new RewardData();
                reward.configured = true;
                reward.rewardType = (byte)msg.reader().readByte();
                reward.itemId = msg.reader().readShort();
                reward.quantity = msg.reader().readInt();
                ClaimedRewards.Add(reward);
            }
            GiftCodeSubmitting = false;
            RewardPopupText = text;
            RewardPopupSuccess = success;
            RewardPopupSeconds = 5f;
            ResultText = null;
            ResultSeconds = 0f;
            DragonPassUI.OnGiftCodeResult(success);
        }
    }
}
