package luckywheel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class LuckyWheelModels {
    private LuckyWheelModels() {}

    public static class Event {
        public long id;
        public byte status;
        public int configVersion;
        public int durationDays;
        public int durationHours;
        public int durationMinutes;
        public int priceOne;
        public int priceTen;
        public int pityLimit;
        public Timestamp startedAt;
        public Timestamp endsAt;
        public Timestamp finishedAt;

        public boolean active() {
            return status == LuckyWheelConstants.STATUS_ACTIVE
                    && endsAt != null && endsAt.getTime() > System.currentTimeMillis();
        }
    }

    public static class Reward {
        public long eventId;
        public int configVersion;
        public int position;
        public int itemTemplateId;
        public int quantity;
        public String optionsData = "[]";
        public long rateUnits;

        public boolean configured() {
            return position >= 1 && position <= LuckyWheelConstants.SLOT_COUNT
                    && itemTemplateId > 0 && quantity > 0 && rateUnits > 0;
        }

        public Reward copy() {
            Reward copy = new Reward();
            copy.eventId = eventId;
            copy.configVersion = configVersion;
            copy.position = position;
            copy.itemTemplateId = itemTemplateId;
            copy.quantity = quantity;
            copy.optionsData = optionsData;
            copy.rateUnits = rateUnits;
            return copy;
        }
    }

    public static class Milestone {
        public long id;
        public long eventId;
        public int targetSpins;
        public final List<Reward> rewards = new ArrayList<>();
    }

    public static class RankReward {
        public int rank;
        public final List<Reward> rewards = new ArrayList<>();
    }

    public static class PlayerState {
        public long playerId;
        public long eventId;
        public int totalSpins;
        public int pityRemaining;
        public Timestamp reachedAt;
    }

    public static class RankEntry {
        public int rank;
        public long playerId;
        public String playerName;
        public int totalSpins;
    }

    public static class SpinResult {
        public long spinId;
        public long requestKey;
        public int count;
        public int cost;
        public int totalSpins;
        public int pityRemaining;
        public boolean duplicate;
        public final List<Reward> rewards = new ArrayList<>();
    }

    public static class Snapshot {
        public Event event;
        public PlayerState player;
        public final List<Reward> rewards = new ArrayList<>();
        public final List<Milestone> milestones = new ArrayList<>();
        public final List<Integer> claimedMilestones = new ArrayList<>();
        public final List<RankEntry> ranking = new ArrayList<>();
        public final List<RankReward> rankRewards = new ArrayList<>();
    }
}
