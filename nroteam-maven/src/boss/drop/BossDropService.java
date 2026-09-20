package boss.drop;

import boss.Boss;
import boss.drop.BossDropModels.Config;
import boss.drop.BossDropModels.LegacyMode;
import boss.drop.BossDropModels.OptionMode;
import boss.drop.BossDropModels.OptionRule;
import boss.drop.BossDropModels.Rule;
import boss.drop.BossDropModels.SourceType;
import boss.drop.BossDropModels.TlBossGenerator;
import boss.drop.BossDropModels.WeightedItems;
import boss.drop.BossDropModels.PoolGenerator;
import boss.drop.BossDropModels.ActivationOptions;
import item.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import map.ItemMap;
import player.Player;
import services.Service;
import utils.Logger;
import utils.Util;

/** Executes editable boss drops while allowing legacy reward code to keep task side effects. */
public final class BossDropService {

    private static final BossDropService INSTANCE = new BossDropService();
    private static final int RATE_MAX = 1_000_000;
    private static final int EDGE_MARGIN = 60;
    private static final int[] SPREAD = {0, -24, 24, -48, 48, -72, 72, -96, 96};

    private final Map<Integer, Optional<Config>> cache = new ConcurrentHashMap<>();
    private final ThreadLocal<DropContext> active = new ThreadLocal<>();

    private BossDropService() {
    }

    public static BossDropService gI() {
        return INSTANCE;
    }

    public Config getConfig(int bossId) {
        return cache.computeIfAbsent(bossId, id -> {
            Config stored = BossDropRepository.load(id);
            return Optional.ofNullable(stored == null ? BossDropDefaults.create(id) : stored);
        }).orElse(null);
    }

    public void reload(int bossId) {
        cache.remove(bossId);
    }

    /**
     * Always executes legacy reward code so task, badge and event side effects stay intact.
     * Depending on legacyMode, its map drops are kept or intercepted before configured drops run.
     */
    public void executeReward(Boss boss, Player killer, Runnable legacyReward) {
        if (boss == null || killer == null || boss.zone == null) {
            legacyReward.run();
            return;
        }
        Config config = getConfig((int) boss.id);
        DropContext previous = active.get();
        DropContext context = new DropContext(boss, killer, config);
        active.set(context);
        try {
            legacyReward.run();
            if (config != null) {
                context.emittingConfigured = true;
                emitConfigured(context);
            }
        } catch (RuntimeException e) {
            Logger.logException(BossDropService.class, e, "Xử lý đồ rơi boss " + boss.id + " thất bại");
            throw e;
        } finally {
            if (previous == null) {
                active.remove();
            } else {
                active.set(previous);
            }
        }
    }

    /** Called by Service.dropItemMap before the packet is sent. */
    public boolean prepareLegacyDrop(ItemMap item) {
        DropContext context = active.get();
        if (context == null || item == null || item.zone != context.boss.zone) {
            return true;
        }
        if (context.emittingConfigured || context.config == null) {
            placeSafely(context, item);
            return true;
        }
        LegacyMode mode = context.config.legacyMode == null ? LegacyMode.PRESERVE : context.config.legacyMode;
        boolean suppress = mode == LegacyMode.REPLACE_CONFIGURED
                && context.managedItemIds.contains((int) item.itemTemplate.id);
        if (suppress) {
            item.zone.removeItemMap(item);
            return false;
        }
        placeSafely(context, item);
        return true;
    }

    private void emitConfigured(DropContext context) {
        List<Rule> independent = new ArrayList<>();
        Map<String, List<Rule>> groups = new HashMap<>();
        for (Rule rule : context.config.rules) {
            if (rule == null || !rule.enabled) {
                continue;
            }
            String group = rule.exclusiveGroup == null ? "" : rule.exclusiveGroup.trim();
            if (group.isEmpty()) {
                independent.add(rule);
            } else {
                groups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(rule);
            }
        }
        for (Rule rule : independent) {
            for (int roll = 0; roll < Math.max(1, rule.rolls); roll++) {
                if (roll(rule.rateUnits)) {
                    emitRule(context, rule);
                }
            }
        }
        for (List<Rule> candidates : groups.values()) {
            if (candidates.isEmpty()) {
                continue;
            }
            int rolls = candidates.stream().mapToInt(r -> Math.max(1, r.rolls)).max().orElse(1);
            int activationRate = candidates.stream().mapToInt(r -> r.rateUnits).max().orElse(0);
            for (int roll = 0; roll < rolls; roll++) {
                if (!roll(activationRate)) {
                    continue;
                }
                Rule selected = weighted(candidates);
                if (selected != null) {
                    emitRule(context, selected);
                }
            }
        }
    }

    private Rule weighted(List<Rule> rules) {
        int total = rules.stream().mapToInt(r -> Math.max(0, r.weight)).sum();
        if (total <= 0) {
            return null;
        }
        int value = Util.nextInt(total);
        for (Rule rule : rules) {
            value -= Math.max(0, rule.weight);
            if (value < 0) {
                return rule;
            }
        }
        return rules.get(rules.size() - 1);
    }

    private void emitRule(DropContext context, Rule rule) {
        int quantity = randomRange(rule.quantityMin, rule.quantityMax);
        long owner = rule.killerOnly ? context.killer.id : -1;
        ItemMap item;
        if (isTlSource(rule.sourceType)) {
            item = createTlBossItem(context, rule, quantity, owner);
        } else if (isPoolSource(rule.sourceType)) {
            item = createPoolItem(context, rule, quantity, owner);
        } else {
            item = new ItemMap(context.boss.zone, rule.itemTemplateId, quantity,
                    context.deathX, context.deathY, owner);
            applyOptions(item, rule.options);
        }
        if (item != null) {
            Service.gI().dropItemMap(context.boss.zone, item);
        }
    }

    private ItemMap createTlBossItem(DropContext context, Rule rule, int quantity, long owner) {
        TlBossGenerator generator = rule.tlBoss == null
                ? BossDropDefaults.createTlGenerator(rule.sourceType) : rule.tlBoss;
        WeightedItems category = weightedCategory(generator.categories);
        if (category == null || category.itemIds == null || category.itemIds.length == 0) {
            return null;
        }
        int itemId = category.itemIds[Util.nextInt(category.itemIds.length)];
        int scale = randomRange(generator.statScaleMin, generator.statScaleMax);
        ItemMap item = new ItemMap(context.boss.zone, itemId, quantity,
                context.deathX, context.deathY, owner);
        item.options.clear();
        addTlBaseOptions(item, itemId, scale);
        if (generator.specialOptionIds != null && generator.specialOptionIds.length > 0
                && roll(generator.specialOptionRateUnits)) {
            item.options.add(new Item.ItemOption(
                    generator.specialOptionIds[Util.nextInt(generator.specialOptionIds.length)], 0));
        }
        item.options.add(new Item.ItemOption(21, randomRange(generator.option21Min, generator.option21Max)));
        if (generator.addOption218) {
            item.options.add(new Item.ItemOption(218, generator.option218Value));
        }
        if (generator.trailingOptionId >= 0) {
            item.options.add(new Item.ItemOption(generator.trailingOptionId,
                    randomRange(generator.trailingOptionMin, generator.trailingOptionMax)));
        }
        applyOptions(item, rule.options);
        return item;
    }

    private ItemMap createPoolItem(DropContext context, Rule rule, int quantity, long owner) {
        PoolGenerator pool = rule.pool == null
                ? BossDropDefaults.createPoolGenerator(rule.sourceType, 0) : rule.pool;
        WeightedItems category = weightedCategory(pool.categories);
        if (category == null || category.itemIds == null || category.itemIds.length == 0) {
            return null;
        }
        int itemId = category.itemIds[Util.nextInt(category.itemIds.length)];
        ItemMap item = new ItemMap(context.boss.zone, itemId, quantity,
                context.deathX, context.deathY, owner);
        applyActivationOptions(item, pool.activation, pool.gender);
        applyOptions(item, rule.options);
        return item;
    }

    private void applyActivationOptions(ItemMap item, ActivationOptions activation, int gender) {
        if (activation == null || !activation.enabled || activation.optionIdsByGender == null
                || activation.optionIdsByGender.length == 0) {
            return;
        }
        int safeGender = Math.max(0, Math.min(activation.optionIdsByGender.length - 1, gender));
        int[] optionIds = activation.optionIdsByGender[safeGender];
        if (optionIds != null) {
            for (int optionId : optionIds) {
                if (optionId >= 0) item.options.add(new Item.ItemOption(optionId, activation.param));
            }
        }
        if (activation.addOption30) {
            item.options.add(new Item.ItemOption(30, activation.option30Value));
        }
    }

    private WeightedItems weightedCategory(List<WeightedItems> categories) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        int total = categories.stream().mapToInt(c -> Math.max(0, c.weightUnits)).sum();
        if (total <= 0) {
            return null;
        }
        int value = Util.nextInt(total);
        for (WeightedItems category : categories) {
            value -= Math.max(0, category.weightUnits);
            if (value < 0) {
                return category;
            }
        }
        return categories.get(categories.size() - 1);
    }

    private void addTlBaseOptions(ItemMap item, int itemId, int scale) {
        switch (itemId) {
            case 555 -> item.options.add(new Item.ItemOption(47, 800 * scale / 100));
            case 557 -> item.options.add(new Item.ItemOption(47, 850 * scale / 100));
            case 559 -> item.options.add(new Item.ItemOption(47, 900 * scale / 100));
            case 556 -> addDual(item, 52000 * scale / 100, 22, 27, 1000, 20);
            case 558 -> addDual(item, 50000 * scale / 100, 22, 27, 1000, 20);
            case 560 -> addDual(item, 48000 * scale / 100, 22, 27, 1000, 20);
            case 562 -> item.options.add(new Item.ItemOption(0, 4400 * scale / 100));
            case 564 -> item.options.add(new Item.ItemOption(0, 4300 * scale / 100));
            case 566 -> item.options.add(new Item.ItemOption(0, 4500 * scale / 100));
            case 563 -> addDual(item, 48000 * scale / 100, 23, 28, 1000, 20);
            case 565 -> addDual(item, 50000 * scale / 100, 23, 28, 1000, 20);
            case 567 -> {
                int base = 46000 * scale / 100;
                item.options.add(new Item.ItemOption(23, base / 1000));
                item.options.add(new Item.ItemOption(28, base * 150 / 1000));
            }
            case 561 -> item.options.add(new Item.ItemOption(14, 14 * scale / 100));
            default -> {
            }
        }
    }

    private void addDual(ItemMap item, int base, int firstId, int secondId, int firstDivisor, int secondDivisor) {
        item.options.add(new Item.ItemOption(firstId, base / firstDivisor));
        item.options.add(new Item.ItemOption(secondId, base / secondDivisor));
    }

    private static boolean isTlSource(SourceType type) {
        return type == SourceType.RAND_DO_TL || type == SourceType.RAND_DO_TL_COLOR
                || type == SourceType.RAND_DO_TL_BOSS;
    }

    private static boolean isPoolSource(SourceType type) {
        return type == SourceType.RAND_DO_SAO || type == SourceType.RAND_TEMP_ITEM_DO_SAO
                || type == SourceType.RAND_TEMP_ITEM_KICH_HOAT;
    }

    private void applyOptions(ItemMap item, List<OptionRule> options) {
        if (options == null) {
            return;
        }
        for (OptionRule option : options) {
            if (option != null && roll(option.rateUnits)) {
                int value = option.mode == OptionMode.RANDOM_RANGE
                        ? randomRange(option.valueMin, option.valueMax) : option.valueMin;
                item.options.add(new Item.ItemOption(option.optionId, value));
            }
        }
    }

    private void placeSafely(DropContext context, ItemMap item) {
        int[] position = nextPosition(context);
        item.x = position[0];
        item.y = position[1];
    }

    private int[] nextPosition(DropContext context) {
        int index = context.positionIndex++;
        int ring = index / SPREAD.length;
        int offset = SPREAD[index % SPREAD.length] + (ring == 0 ? 0 : (index % 2 == 0 ? ring * 12 : -ring * 12));
        int mapWidth = context.boss.zone.map.mapWidth;
        int margin = Math.min(EDGE_MARGIN, Math.max(0, mapWidth / 4));
        int minX = margin;
        int maxX = Math.max(minX, mapWidth - margin);
        int x = Math.max(minX, Math.min(maxX, context.deathX + offset));
        int y = findGroundY(context.boss, x, context.deathY);
        return new int[]{x, y};
    }

    private int findGroundY(Boss boss, int x, int deathY) {
        int mapHeight = boss.zone.map.mapHeight;
        int y = Math.max(0, Math.min(mapHeight - 24, deathY - 24));
        while (y > 0 && boss.zone.map.tileTypeAt(x, y, 2)) {
            y -= 24;
        }
        for (int scan = y; scan < mapHeight; scan += 24) {
            if (boss.zone.map.tileTypeAt(x, scan, 2)) {
                return scan - scan % 24;
            }
        }
        int fallback = boss.zone.map.yPhysicInTop(x, Math.max(0, y));
        return Math.max(0, Math.min(mapHeight - 24, fallback));
    }

    private boolean roll(int rateUnits) {
        return rateUnits >= RATE_MAX || rateUnits > 0 && Util.nextInt(RATE_MAX) < rateUnits;
    }

    private int randomRange(int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        return low == high ? low : Util.nextInt(low, high);
    }

    public static void validate(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("Cấu hình không được rỗng");
        }
        if (config.rules == null) {
            config.rules = new ArrayList<>();
        }
        Set<String> ids = new HashSet<>();
        Map<String, int[]> exclusiveSettings = new HashMap<>();
        Map<String, Integer> exclusiveWeights = new HashMap<>();
        for (Rule rule : config.rules) {
            if (rule == null) {
                throw new IllegalArgumentException("Có ô vật phẩm không hợp lệ");
            }
            if (rule.id == null || rule.id.isBlank()) {
                throw new IllegalArgumentException("Mỗi ô vật phẩm phải có mã nội bộ");
            }
            if (!ids.add(rule.id)) {
                throw new IllegalArgumentException("Mã ô vật phẩm bị trùng: " + rule.id);
            }
            if (rule.sourceType == SourceType.FIXED_ITEM && rule.itemTemplateId < 0) {
                throw new IllegalArgumentException("ID vật phẩm không được âm");
            }
            if (rule.quantityMin <= 0 || rule.quantityMax < rule.quantityMin) {
                throw new IllegalArgumentException("Khoảng số lượng không hợp lệ");
            }
            if (rule.rateUnits < 0 || rule.rateUnits > RATE_MAX || rule.rolls <= 0 || rule.weight < 0) {
                throw new IllegalArgumentException("Tỷ lệ, số lượt hoặc trọng số không hợp lệ");
            }
            if (rule.options != null) {
                for (OptionRule option : rule.options) {
                    if (option == null || option.optionId < 0 || option.valueMax < option.valueMin
                            || option.rateUnits < 0 || option.rateUnits > RATE_MAX) {
                        throw new IllegalArgumentException("Option trong ô " + rule.id + " không hợp lệ");
                    }
                }
            }
            String group = rule.exclusiveGroup == null ? "" : rule.exclusiveGroup.trim();
            if (!group.isEmpty() && rule.enabled) {
                int[] settings = exclusiveSettings.putIfAbsent(group, new int[]{rule.rateUnits, rule.rolls});
                if (settings != null && (settings[0] != rule.rateUnits || settings[1] != rule.rolls)) {
                    throw new IllegalArgumentException("Các ô trong nhóm '" + group
                            + "' phải có cùng tỷ lệ và số lượt quay");
                }
                exclusiveWeights.merge(group, rule.weight, Integer::sum);
            }
            if (isTlSource(rule.sourceType)) {
                TlBossGenerator generator = rule.tlBoss == null ? new TlBossGenerator() : rule.tlBoss;
                if (generator.statScaleMin < 0 || generator.statScaleMax < generator.statScaleMin
                        || generator.specialOptionRateUnits < 0 || generator.specialOptionRateUnits > RATE_MAX
                        || generator.categories == null || generator.categories.isEmpty()) {
                    throw new IllegalArgumentException("Cấu hình randDoTLBoss không hợp lệ");
                }
                for (WeightedItems category : generator.categories) {
                    if (category == null || category.weightUnits < 0 || category.itemIds == null
                            || category.itemIds.length == 0) {
                        throw new IllegalArgumentException("Nhóm item randDoTLBoss không hợp lệ");
                    }
                    for (int itemId : category.itemIds) if (itemId <= 0) {
                        throw new IllegalArgumentException("ID item randDoTLBoss phải lớn hơn 0");
                    }
                }
            } else if (isPoolSource(rule.sourceType)) {
                PoolGenerator pool = rule.pool == null
                        ? BossDropDefaults.createPoolGenerator(rule.sourceType, 0) : rule.pool;
                if (pool.gender < 0 || pool.gender > 2 || pool.categories == null || pool.categories.isEmpty()) {
                    throw new IllegalArgumentException("Cấu hình bộ sinh item theo hành tinh không hợp lệ");
                }
                for (WeightedItems category : pool.categories) {
                    if (category == null || category.weightUnits < 0 || category.itemIds == null
                            || category.itemIds.length == 0) {
                        throw new IllegalArgumentException("Nhóm item của bộ sinh không hợp lệ");
                    }
                    for (int itemId : category.itemIds) if (itemId < 0) {
                        throw new IllegalArgumentException("ID item của bộ sinh không được âm");
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> group : exclusiveWeights.entrySet()) {
            if (group.getValue() <= 0) {
                throw new IllegalArgumentException("Nhóm '" + group.getKey() + "' phải có tổng trọng số lớn hơn 0");
            }
        }
    }

    private static final class DropContext {
        final Boss boss;
        final Player killer;
        final Config config;
        final int deathX;
        final int deathY;
        final Set<Integer> managedItemIds = new HashSet<>();
        int positionIndex;
        boolean emittingConfigured;

        DropContext(Boss boss, Player killer, Config config) {
            this.boss = boss;
            this.killer = killer;
            this.config = config;
            this.deathX = boss.location.x;
            this.deathY = boss.location.y;
            if (config != null && config.rules != null) {
                for (Rule rule : config.rules) {
                    if (rule == null) {
                        continue;
                    }
                    if (isTlSource(rule.sourceType)) {
                        for (WeightedItems category : (rule.tlBoss == null
                                ? BossDropDefaults.createTlGenerator(rule.sourceType).categories : rule.tlBoss.categories)) {
                            if (category.itemIds != null) {
                                for (int itemId : category.itemIds) {
                                    managedItemIds.add(itemId);
                                }
                            }
                        }
                    } else if (isPoolSource(rule.sourceType)) {
                        PoolGenerator pool = rule.pool == null
                                ? BossDropDefaults.createPoolGenerator(rule.sourceType, 0) : rule.pool;
                        for (WeightedItems category : pool.categories) {
                            if (category.itemIds != null) for (int itemId : category.itemIds) managedItemIds.add(itemId);
                        }
                    } else {
                        managedItemIds.add(rule.itemTemplateId);
                    }
                }
            }
        }
    }
}
