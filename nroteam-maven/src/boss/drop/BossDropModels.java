package boss.drop;

import java.util.ArrayList;
import java.util.List;

/** Data objects persisted as JSON for the boss drop editor. */
public final class BossDropModels {

    private BossDropModels() {
    }

    public enum LegacyMode {
        PRESERVE("Giữ toàn bộ setup code gốc"),
        REPLACE_CONFIGURED("Thay các item đã cấu hình");

        private final String label;

        LegacyMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum SourceType {
        FIXED_ITEM("Vật phẩm cố định"),
        RAND_DO_TL("Bộ sinh randDoTL"),
        RAND_DO_TL_COLOR("Bộ sinh randDoTLCOler"),
        RAND_DO_TL_BOSS("Bộ sinh randDoTLBoss"),
        RAND_DO_SAO("Bộ sinh randDoSao"),
        RAND_TEMP_ITEM_DO_SAO("Bộ sinh randTempItemDoSao"),
        RAND_TEMP_ITEM_KICH_HOAT("Bộ sinh randTempItemKichHoat");

        private final String label;

        SourceType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum OptionMode {
        FIXED,
        RANDOM_RANGE
    }

    public static final class Config {
        public int bossId;
        public LegacyMode legacyMode = LegacyMode.PRESERVE;
        public List<Rule> rules = new ArrayList<>();
    }

    public static final class Rule {
        public String id;
        public boolean enabled = true;
        public SourceType sourceType = SourceType.FIXED_ITEM;
        public int itemTemplateId;
        public int quantityMin = 1;
        public int quantityMax = 1;
        /** 1,000,000 units = 100%. */
        public int rateUnits = 1_000_000;
        public int rolls = 1;
        public String exclusiveGroup = "";
        public int weight = 1;
        public boolean killerOnly = true;
        public List<OptionRule> options = new ArrayList<>();
        public TlBossGenerator tlBoss;
        public PoolGenerator pool;
    }

    public static final class OptionRule {
        public int optionId;
        public OptionMode mode = OptionMode.FIXED;
        public int valueMin;
        public int valueMax;
        public int rateUnits = 1_000_000;
    }

    /** Editable equivalent of ItemService.randDoTLBoss. */
    public static final class TlBossGenerator {
        public List<WeightedItems> categories = defaultCategories();
        public int statScaleMin = 100;
        public int statScaleMax = 115;
        public int specialOptionRateUnits = 300_000;
        public int[] specialOptionIds = {86, 87};
        public int option21Min = 15;
        public int option21Max = 17;
        public int option218Value = 1;
        public boolean addOption218 = true;
        public int trailingOptionId = 207;
        public int trailingOptionMin = 4;
        public int trailingOptionMax = 6;

        public static List<WeightedItems> defaultCategories() {
            List<WeightedItems> result = new ArrayList<>();
            // Exact effective probabilities of the legacy if/else-if chain, scaled by 2,000,000.
            result.add(new WeightedItems("Nhẫn", 200_000, new int[]{561}));
            result.add(new WeightedItems("Găng", 450_000, new int[]{562, 564, 566}));
            result.add(new WeightedItems("Quần", 607_500, new int[]{556, 558, 560}));
            result.add(new WeightedItems("Áo", 556_875, new int[]{555, 557, 559}));
            result.add(new WeightedItems("Giày", 185_625, new int[]{563, 565, 567}));
            return result;
        }
    }

    public static final class PoolGenerator {
        public int gender;
        public List<WeightedItems> categories = new ArrayList<>();
        public ActivationOptions activation = new ActivationOptions();
    }

    public static final class ActivationOptions {
        public boolean enabled;
        public int[][] optionIdsByGender = {{128, 140}, {130, 142}, {134, 137}};
        public int param = 1;
        public boolean addOption30;
        public int option30Value = 7;
    }

    public static final class WeightedItems {
        public String name;
        public int weightUnits;
        public int[] itemIds;

        public WeightedItems() {
        }

        public WeightedItems(String name, int weightUnits, int[] itemIds) {
            this.name = name;
            this.weightUnits = weightUnits;
            this.itemIds = itemIds;
        }
    }
}
