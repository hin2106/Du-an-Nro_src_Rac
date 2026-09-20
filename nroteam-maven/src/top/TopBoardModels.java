package top;

import java.util.ArrayList;
import java.util.List;

public final class TopBoardModels {
    private TopBoardModels() {}

    public static final String SUPER_RANK = "SUPER_RANK";
    public static final String WHIS = "WHIS";
    public static final byte CLIENT_SUPER_RANK = 0;
    public static final byte CLIENT_WHIS = 1;

    public static final class Reward {
        public long id;
        public String board;
        public int rankFrom;
        public int rankTo;
        public byte type;
        public int itemId;
        public int amount;
        public String options = "[]";
        public int slot;
    }

    public static final class Entry {
        public int playerId;
        public int rank;
        public short head;
        public short body;
        public short leg;
        public String name = "";
        public String guild = "";
        public int primaryValue;
        public long timeMs;
        public long achievedAt;
        public final List<Reward> rewards = new ArrayList<>();
    }
}
