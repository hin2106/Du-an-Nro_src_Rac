using System.Collections.Generic;

namespace DragonPass
{
    public sealed class TopRewardView
    {
        public byte type;
        public short itemId;
        public int amount;
    }

    public sealed class TopEntryView
    {
        public int rank;
        public int playerId;
        public short head;
        public short body;
        public short leg;
        public string name = "";
        public string guild = "";
        public int primaryValue;
        public long timeMs;
        public long achievedAt;
        public readonly List<TopRewardView> rewards = new List<TopRewardView>();
    }

    public sealed class TopBoardState
    {
        public byte board;
        public string title = "Top";
        public readonly List<TopEntryView> entries = new List<TopEntryView>();
        public TopEntryView self;
    }

    public static class TopBoardManager
    {
        public static readonly TopBoardState[] Boards = { new TopBoardState(), new TopBoardState() };

        public static void Read(Message msg)
        {
            byte board = (byte)msg.reader().readByte();
            if (board > 1) return;
            TopBoardState state = new TopBoardState(); state.board = board; state.title = msg.reader().readUTF();
            int count = msg.reader().readUnsignedByte();
            for (int i = 0; i < count; i++) state.entries.Add(ReadEntry(msg));
            if (msg.reader().readBoolean()) state.self = ReadEntry(msg);
            Boards[board] = state;
            TopBoardUI.OnReloaded(board);
        }

        private static TopEntryView ReadEntry(Message msg)
        {
            TopEntryView e = new TopEntryView();
            e.rank = msg.reader().readInt(); e.playerId = msg.reader().readInt();
            e.head = msg.reader().readShort(); e.body = msg.reader().readShort(); e.leg = msg.reader().readShort();
            e.name = msg.reader().readUTF(); e.guild = msg.reader().readUTF();
            e.primaryValue = msg.reader().readInt(); e.timeMs = msg.reader().readLong(); e.achievedAt = msg.reader().readLong();
            int rewardCount = msg.reader().readUnsignedByte();
            for (int i = 0; i < rewardCount; i++) e.rewards.Add(new TopRewardView
            {
                type = (byte)msg.reader().readByte(), itemId = msg.reader().readShort(), amount = msg.reader().readInt()
            });
            return e;
        }
    }
}
