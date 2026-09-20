package dragonpass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DragonPassModels {
    private DragonPassModels() {}

    public static class Season {
        public long id;
        public String key;
        public LocalDate startDate;
        public LocalDate summaryDate;
        public LocalDate endDate;
        public byte phase;
        public byte status;
        public int durationDays;
        public int durationHours;
        public int durationMinutes;
        public int normalPrice;
        public int plusPrice;
        public LocalDateTime startedAt;
        public LocalDateTime endsAt;
        public LocalDateTime summaryAt;
        public LocalDateTime finishedAt;

        public boolean editable() { return status == DragonPassConstants.STATUS_DRAFT; }
        public boolean running() {
            return status == DragonPassConstants.STATUS_ACTIVE
                    || status == DragonPassConstants.STATUS_SUMMARY
                    || status == DragonPassConstants.STATUS_FINALIZING;
        }
    }

    public static class PlayerState {
        public long playerId;
        public long seasonId;
        public int exp;
        public byte passType;

        public int level() {
            return Math.min(DragonPassConstants.MAX_LEVEL, exp / DragonPassConstants.EXP_PER_LEVEL);
        }
    }

    public static class Reward {
        public int level;
        public byte track;
        public byte slot;
        public int itemTemplateId;
        public int quantity;
        public String optionsData;

        public boolean configured() {
            return itemTemplateId > 0 && quantity > 0;
        }
    }

    public static class TaskState {
        public DragonPassTask definition;
        public int progress;
        public boolean claimed;
        public boolean unlocked;
    }

    public static class Snapshot {
        public Season season;
        public PlayerState player;
        public List<Reward> rewards = new ArrayList<>();
        public List<Integer> claimedNormal = new ArrayList<>();
        public List<Integer> claimedPlus = new ArrayList<>();
        public List<TaskState> tasks = new ArrayList<>();
        public boolean hasNotification;
    }
}
