package boss.drop;

import boss.BossID;
import boss.drop.BossDropModels.Config;
import boss.drop.BossDropModels.LegacyMode;
import boss.drop.BossDropModels.Rule;
import boss.drop.BossDropModels.SourceType;
import boss.drop.BossDropModels.TlBossGenerator;
import boss.drop.BossDropModels.PoolGenerator;
import boss.drop.BossDropModels.WeightedItems;
import java.util.ArrayList;
import java.util.List;

/** Built-in editable profiles for legacy generator calls that cannot be represented by a fixed item. */
public final class BossDropDefaults {

    private BossDropDefaults() {
    }

    public static Config create(int bossId) {
        if (bossId != BossID.CUMBER && bossId != BossID.XEN_CON_1) {
            return null;
        }
        Config config = new Config();
        config.bossId = bossId;
        config.legacyMode = LegacyMode.REPLACE_CONFIGURED;
        Rule rule = new Rule();
        rule.id = bossId == BossID.CUMBER ? "legacy-rand-do-tl-boss" : "legacy-rand-do-tl";
        rule.sourceType = bossId == BossID.CUMBER ? SourceType.RAND_DO_TL_BOSS : SourceType.RAND_DO_TL;
        rule.rateUnits = 1_000_000;
        rule.quantityMin = 1;
        rule.quantityMax = 1;
        rule.rolls = 1;
        rule.killerOnly = true;
        rule.tlBoss = createTlGenerator(rule.sourceType);
        config.rules.add(rule);
        return config;
    }

    public static TlBossGenerator createTlGenerator(SourceType type) {
        TlBossGenerator value = new TlBossGenerator();
        if (type == SourceType.RAND_DO_TL) {
            value.specialOptionRateUnits = 210_000; // 30% then 70% in the legacy code
            value.addOption218 = false;
            value.trailingOptionId = -1;
        } else if (type == SourceType.RAND_DO_TL_COLOR) {
            value.specialOptionRateUnits = 210_000;
            value.addOption218 = true;
            value.trailingOptionId = 206;
            value.trailingOptionMin = 1;
            value.trailingOptionMax = 3;
        }
        return value;
    }

    public static PoolGenerator createPoolGenerator(SourceType type, int gender) {
        int safeGender = Math.max(0, Math.min(2, gender));
        PoolGenerator pool = new PoolGenerator();
        pool.gender = safeGender;
        if (type == SourceType.RAND_DO_SAO) {
            int[][][] items = {{{0, 33}, {1, 41}, {2, 49}}, {{6, 35}, {7, 43}, {8, 51}},
                {{27, 30}, {28, 47}, {29, 55}}, {{21, 24}, {22, 46}, {23, 53}},
                {{12, 57}, {12, 57}, {12, 57}}};
            pool.categories.add(new WeightedItems("Rada", 10, items[4][safeGender]));
            pool.categories.add(new WeightedItems("Áo", 22, items[0][safeGender]));
            pool.categories.add(new WeightedItems("Găng", 23, items[1][safeGender]));
            pool.categories.add(new WeightedItems("Quần", 22, items[2][safeGender]));
            pool.categories.add(new WeightedItems("Giày", 23, items[3][safeGender]));
        } else {
            int[][][] normal = {{{3,34,136,137,138,139},{4,42,152,153,154,155},{5,50,168,169,170,171}},
                {{37,38,144,145,146,147},{25,45,160,161,162,163},{26,54,176,177,178,179}},
                {{9,36,140,141,142,143},{10,44,156,157,158,159},{11,52,172,173,174,175}},
                {{39,40,148,149,150,151},{31,48,164,165,166,167},{32,56,180,181,182,183}},
                {{58,59,184,185,186,187},{58,59,184,185,186,187},{58,59,184,185,186,187}}};
            int[][][] active = {{{0,33},{1,41},{2,49}},{{6,35},{7,43},{8,51}},{{27,30},{28,47},{29,55}},
                {{21,24},{22,46},{23,53}},{{12,57},{12,57},{12,57}}};
            int[][][] source = type == SourceType.RAND_TEMP_ITEM_KICH_HOAT ? active : normal;
            // Exact effective weights of 10%, then chained 23%,23%,23%, else.
            pool.categories.add(new WeightedItems("Rada", 10_000_000, source[4][safeGender]));
            pool.categories.add(new WeightedItems("Giày", 20_700_000, source[3][safeGender]));
            pool.categories.add(new WeightedItems("Găng", 15_939_000, source[1][safeGender]));
            pool.categories.add(new WeightedItems("Áo", 12_273_030, source[0][safeGender]));
            pool.categories.add(new WeightedItems("Quần", 41_087_970, source[2][safeGender]));
        }
        return pool;
    }
}
