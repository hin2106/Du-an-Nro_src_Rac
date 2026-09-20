package recharge;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class RechargeModels {
    private RechargeModels() {}

    public static final class Settings {
        public String bankName = "MBBank";
        public String accountNumber = "8386888999888";
        public String accountHolder = "VY NGOC DINH";
        public String transferPattern = "{player_name}";
        public String warning = "Chuyển khoản đúng nội dung, chuyển khoản không đúng nội dung -> admin không giải quyết.";
        public long rateVnd = 1_000;
        public int rateGem = 1;

        public int diamondsFor(long cashAmount) {
            if (cashAmount <= 0 || rateVnd <= 0 || rateGem <= 0) return 0;
            java.math.BigInteger result = java.math.BigInteger.valueOf(cashAmount)
                    .multiply(java.math.BigInteger.valueOf(rateGem))
                    .divide(java.math.BigInteger.valueOf(rateVnd));
            return result.min(java.math.BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        }

        public String transferContent(String playerName) {
            String pattern = transferPattern == null || transferPattern.isBlank()
                    ? "{player_name}" : transferPattern;
            return pattern.replace("{player_name}", playerName == null ? "" : playerName);
        }
    }

    public static final class PlayerChoice {
        public final long id;
        public final String name;

        public PlayerChoice(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override public String toString() { return name + " (ID: " + id + ")"; }
    }

    public static final class RechargePackage {
        public long id;
        public String name;
        public boolean active = true;
        public List<PackageReward> rewards = new ArrayList<>();

        @Override public String toString() { return name == null ? "Gói mới" : name; }
    }

    public static final class PackageReward {
        public long id;
        public long packageId;
        public String type = "ITEM";
        public int itemId;
        public int amount = 1;
        public String optionsData;

        public String describe(String itemName) {
            return amount + " x " + ("ITEM".equals(type) ? itemName + " [" + itemId + "]"
                    : "GEM".equals(type) ? "Kim cương" : "Vàng");
        }
    }

    public static final class History {
        public long id;
        public long playerId;
        public String playerName;
        public String type;
        public long cashAmount;
        public int diamondBase;
        public int diamondTotal;
        public long packageId;
        public String packageName;
        public int eventPoints;
        public long mailId;
        public String rewardSnapshot;
        public String status;
        public Timestamp createdAt;
    }
}
