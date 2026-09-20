package map;

import audit.AssetAuditService;

import consts.ConstTask;
import boss.Boss;
import boss.BossID;
import boss.learn.TrainingBoss;
import services.map.NpcManager;
import consts.ConstMob;
import item.Item;
import java.lang.ref.SoftReference;
import services.map.ItemMapService;
import services.ItemService;
import services.map.MapService;
import services.player.PlayerService;
import services.Service;
import services.TaskService;
import services.player.InventoryService;
import utils.FileIO;
import utils.Logger;
import utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import mob.Mob;
// import mob.bigboss.GauTuongCuop;
import network.Message;
import npc.NonInteractiveNPC;
import npc.Npc;
import player.Player;
import player.PlayerManager;
import utils.TimeUtil;

public class Zone {

    public static final byte PLAYERS_TIEU_CHUAN_TRONG_MAP = 7;

    public int countItemAppeaerd = 0;

    public Map map;
    public int zoneId;
    public int maxPlayer;
    public int shenronType = -1;

    private final List<Player> nonInteractiveNPCs;
    private final List<Player> humanoids;
    private final List<Player> notBosses;
    private final List<Player> players;
    public final List<Player> bosses;
    private final List<Player> pets;

    public final List<Mob> mobs;
    public final List<ItemMap> items;

    public long lastTimeDropBlackBall;
    public boolean finishBlackBallWar;
    public boolean finishMapMaBu;

    public boolean isbulon1Alive = true;
    public boolean isbulon2Alive = true;
    public boolean isTUTAlive = true;
    public boolean isGoldenFriezaAlive;

    public boolean isCompeting;
    public String rankName1;
    public String rankName2;
    public int rank1;
    public int rank2;

    public List<TrapMap> trapMaps;
    public List<MaBuHold> maBuHolds;

    public int superMobId = -1;

    public int lastSuperDeadId = -1;

    public Player Npc;

    public int tayKarinWavesCleared = 0;
    public long tayKarinLastWaveAt = 0L;

    public SpatialGrid spatialGrid;
    public boolean spatialGridEnabled = true;
    private int updateCount = 0;
    private static final int CLEANUP_INTERVAL = 50; // giảm overhead
    private static final int REBUILD_INTERVAL = 35; // để giảm CPU usage

    private static final java.util.Map<Integer, SoftReference<byte[]>> BG_ITEM_CACHE = Collections
            .synchronizedMap(new java.util.LinkedHashMap<Integer, SoftReference<byte[]>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<Integer, SoftReference<byte[]>> e) {
                    return size() > 32;
                }
            });
    private static final java.util.Map<Integer, java.lang.ref.SoftReference<byte[]>> EFF_ITEM_CACHE = Collections
            .synchronizedMap(new LinkedHashMap<Integer, SoftReference<byte[]>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<Integer, SoftReference<byte[]>> e) {
                    return size() > 32;
                }
            });

    public static int clearAssetCaches() {
        int n = BG_ITEM_CACHE.size() + EFF_ITEM_CACHE.size();
        BG_ITEM_CACHE.clear();
        EFF_ITEM_CACHE.clear();
        return n;
    }

    public static void evictAssets(int mapId) {
        BG_ITEM_CACHE.remove(mapId);
        EFF_ITEM_CACHE.remove(mapId);
    }

    public boolean isFullPlayer() {
        // Không tính admin vào giới hạn số người trong khu
        return this.getNumOfPlayers() >= this.maxPlayer;
    }

    private void udMob() {
        if (this.players.isEmpty() && this.mobs.size() > 100) {
            return;
        }
        int mobCount = this.mobs.size();
        for (int i = mobCount - 1; i >= 0; i--) {
            try {
                if (i < this.mobs.size()) {
                    Mob mob = this.mobs.get(i);
                    if (mob != null) {
                        mob.update();
                    }
                }
            } catch (Exception e) {
                Logger.logException(Zone.class, e, "Lỗi update mobs");
            }
        }
    }

    private void udNonInteractiveNPC() {
        if (this.nonInteractiveNPCs.isEmpty()) {
            return;
        }
        try {
            for (int i = this.getNonInteractiveNPCs().size() - 1; i >= 0; i--) {
                Player pl = this.getNonInteractiveNPCs().get(i);
                if (pl != null && pl.zone != null) {
                    pl.update();
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Lỗi update npcs");
        }
    }

    private void udItem() {
        if (this.items.isEmpty()) {
            return;
        }
        try {
            for (int i = this.items.size() - 1; i >= 0; i--) {
                try {
                    if (i < this.items.size()) {
                        ItemMap item = this.items.get(i);
                        if (item != null && item.itemTemplate != null) {
                            item.update();
                        } else {
                            items.remove(i);
                            System.err.println("Remove item " + i);
                        }
                    }
                } catch (Exception e) {
                    Logger.logException(Zone.class, e, "Lỗi item");
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Lỗi update items");
        }
    }

    public void update() {
        updateCount++;
        if (spatialGridEnabled && spatialGrid != null) {
            try {
                if (updateCount >= REBUILD_INTERVAL) {
                    rebuildSpatialGrid();
                    updateCount = 0;
                } else if (updateCount % CLEANUP_INTERVAL == 0) {
                    spatialGrid.cleanupEmptyCells();
                }
            } catch (Exception e) {
                Logger.logException(Zone.class, e, "Lỗi update spatial grid");
            }
        }
        udMob();
        udItem();
        udNonInteractiveNPC();
        // GauTuongCuop.checkAndSpawnAuto(); 
    }

    private void rebuildSpatialGrid() {
        if (!spatialGridEnabled || spatialGrid == null) {
            return;
        }
        try {
            spatialGrid.clear();
            int playerCount = this.players.size();
            for (int i = 0; i < playerCount; i++) {
                Player p = this.players.get(i);
                if (p != null && p.zone == this && p.location != null && !p.isDie()) {
                    spatialGrid.addPlayer(p);
                }
            }
            int mobCount = this.mobs.size();
            for (int i = 0; i < mobCount; i++) {
                Mob m = this.mobs.get(i);
                if (m != null && m.location != null && !m.isDie()) {
                    spatialGrid.addMob(m);
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Lỗi rebuild spatial grid");
        }
    }

    public List<Player> getPlayersNear(int x, int y, int distance) {
        if (spatialGridEnabled && spatialGrid != null) {
            List<Player> result = spatialGrid.getPlayersNear(x, y, distance);
            if (result.isEmpty() && !this.players.isEmpty()) {
                List<Player> fallback = new ArrayList<>();
                for (Player p : getNotBosses()) {
                    if (p != null && !p.isDie() && p.location != null) {
                        int dist = Math.abs(p.location.x - x) + Math.abs(p.location.y - y);
                        if (dist <= distance) {
                            fallback.add(p);
                        }
                    }
                }
                return fallback;
            }
            return result;
        }
        // Fallback khi spatial grid disabled
        List<Player> result = new ArrayList<>();
        for (Player p : getNotBosses()) {
            if (p != null && !p.isDie() && p.location != null) {
                int dist = Math.abs(p.location.x - x) + Math.abs(p.location.y - y);
                if (dist <= distance) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    public Player getNearestPlayer(int x, int y, int maxDistance) {
        if (spatialGridEnabled && spatialGrid != null) {
            return spatialGrid.getNearestPlayer(x, y, maxDistance);
        }
        Player nearest = null;
        int minDist = maxDistance;
        for (Player p : getNotBosses()) {
            if (p != null && !p.isDie() && p.location != null) {
                int dist = Math.abs(p.location.x - x) + Math.abs(p.location.y - y);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = p;
                }
            }
        }
        return nearest;
    }

    public boolean hasPlayersNearby(int x, int y, int distance) {
        if (spatialGridEnabled && spatialGrid != null) {
            boolean result = spatialGrid.hasPlayersNearby(x, y, distance);
            // FIX: Fallback nếu spatial grid trả về false nhưng có player trong zone
            if (!result && !this.players.isEmpty()) {
                for (Player p : getNotBosses()) {
                    if (p != null && !p.isDie() && p.location != null) {
                        int dist = Math.abs(p.location.x - x) + Math.abs(p.location.y - y);
                        if (dist <= distance) {
                            return true;
                        }
                    }
                }
            }
            return result;
        }
        for (Player p : getNotBosses()) {
            if (p != null && !p.isDie() && p.location != null) {
                int dist = Math.abs(p.location.x - x) + Math.abs(p.location.y - y);
                if (dist <= distance) {
                    return true;
                }
            }
        }
        return false;
    }

    public Zone(Map map, int zoneId, int maxPlayer) {
        this.map = map;
        this.zoneId = zoneId;
        this.maxPlayer = maxPlayer;
        this.nonInteractiveNPCs = new ArrayList<>();
        this.humanoids = new ArrayList<>();
        this.notBosses = new ArrayList<>();
        this.players = new ArrayList<>();
        this.bosses = new ArrayList<>();
        this.pets = new ArrayList<>();
        this.mobs = new ArrayList<>();
        this.items = new ArrayList<>();
        this.trapMaps = new ArrayList<>();
        this.maBuHolds = new ArrayList<>();
        this.spatialGrid = new SpatialGrid(1440, 336, 150);
    }

    private void reconcileOnlinePlayers() {
        // Player tải từ database có thể đã được gán zone trước khi đi qua goToMap.
        // Đối soát với registry online để Zone luôn có đủ player thật; bot vẫn nằm
        // trong players qua goToMap nên cùng được đếm ở vòng phía dưới.
        this.players.removeIf(pl -> pl == null || pl.zone != this);
        for (Player online : PlayerManager.getPlayers()) {
            if (online != null && online.zone == this && !this.players.contains(online)) {
                this.addPlayer(online);
            }
        }
    }

    public synchronized int getNumOfPlayers() {
        reconcileOnlinePlayers();

        // Không tính admin vào giới hạn khu, giữ nguyên luật cũ của server.
        int count = 0;
        for (Player pl : this.players) {
            if (pl != null && pl.zone == this && !pl.isAdmin()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Số nhân vật hiển thị trong tab chọn khu. Khác với giới hạn khu, danh sách
     * này phải tính cả admin để khớp với danh sách người chơi mà client đang thấy.
     */
    public synchronized int getNumOfPlayersForDisplay() {
        reconcileOnlinePlayers();
        return this.players.size();
    }

    public int getNumOfBosses() {
        return this.bosses.size();
    }

    public boolean isBossCanJoin(Boss boss) {
        for (Player b : this.bosses) {
            if (b.id == boss.id) {
                return false;
            }
        }
        return true;
    }

    public synchronized void addPlayer(Player player) {
        if (player != null) {
            if (!this.humanoids.contains(player)) {
                this.humanoids.add(player);
            }

            if (player instanceof NonInteractiveNPC) {
                this.nonInteractiveNPCs.add(player);
            }

            if (!player.isBoss && !this.notBosses.contains(player) && !player.isNewPet
                    && !(player instanceof NonInteractiveNPC)) {
                this.notBosses.add(player);
            }

            if (!player.isBoss && !player.isNewPet && !player.isPet && !this.players.contains(player)
                    && !(player instanceof NonInteractiveNPC)) {
                this.players.add(player);
            }

            if (player.isBoss) {
                this.bosses.add(player);
            }
            if (player.isPet || player.isNewPet) {
                this.pets.add(player);
            }

            // Thêm vào spatial grid ngay lập tức nếu có location và spatial grid enabled
            if (spatialGridEnabled && spatialGrid != null && player.location != null
                    && player.zone == this && !player.isBoss && !player.isPet && !player.isNewPet
                    && !(player instanceof NonInteractiveNPC)) {
                try {
                    spatialGrid.addPlayer(player);
                } catch (Exception e) {
                    Logger.logException(Zone.class, e, "Lỗi thêm player vào spatial grid");
                }
            }

        }
    }

    public synchronized void removePlayer(Player player) {
        this.nonInteractiveNPCs.remove(player);
        this.humanoids.remove(player);
        this.notBosses.remove(player);
        this.players.remove(player);
        this.bosses.remove(player);
        this.pets.remove(player);

        // Clear cache target của tất cả mobs trong zone khi player rời zone
        if (player != null && this.mobs != null) {
            for (Mob mob : this.mobs) {
                if (mob != null) {
                    mob.clearTargetCache(player);
                }
            }
        }

        // Remove khỏi spatial grid ngay lập tức
        if (spatialGridEnabled && spatialGrid != null && player != null
                && !player.isBoss && !player.isPet && !player.isNewPet
                && !(player instanceof NonInteractiveNPC)) {
            try {
                spatialGrid.removePlayer(player);
            } catch (Exception e) {
                Logger.logException(Zone.class, e, "Lỗi remove player khỏi spatial grid");
            }
        }

        // Xóa item "em bé" của player khi rời map
        if (player != null && !player.isBoss && !player.isPet) {
            services.TaskService.gI().removeBabyItemForPlayer(player);
        }
    }

    public ItemMap getItemMapByItemMapId(int itemId) {
        for (ItemMap item : this.items) {
            if (item != null && item.itemMapId == itemId) {
                return item;
            }
        }
        return null;
    }

    public ItemMap getItemMapByTempId(int tempId) {
        for (ItemMap item : this.items) {
            if (item.itemTemplate.id == tempId) {
                return item;
            }
        }
        return null;
    }

    public List<ItemMap> getItemMapsForPlayer(Player player) {
        List<ItemMap> list = new ArrayList<>();
        int playerTaskId = TaskService.gI().getIdTask(player);
        for (ItemMap item : items) {
            if (item.itemTemplate.id == 78) {
                if (playerTaskId != ConstTask.TASK_3_1) {
                    continue;
                }
                if (item.playerId != player.id && item.playerId != -1) {
                    continue;
                }
            }
            if (item.itemTemplate.id == 74) {
                if (playerTaskId < ConstTask.TASK_3_0) {
                    continue;
                }
            }
            if (item.itemTemplate.id == 726 && item.playerId != player.id) {
                continue;
            }
            list.add(item);
        }
        return list;
    }

    public List<Player> getNonInteractiveNPCs() {
        return nonInteractiveNPCs;
    }

    public List<Player> getHumanoids() {
        return humanoids;
    }

    public List<Player> getNotBosses() {
        return notBosses;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getBosses() {
        return bosses;
    }

    public Player getNpc() {
        return Npc;
    }

    public void setNpc(Player npc) {
        this.Npc = npc;
    }

    public Player getPlayerInMap(long idPlayer) {
        for (Player pl : humanoids) {
            if (pl != null && pl.id == idPlayer) {
                return pl;
            }
        }
        return null;
    }

    public Player getPlayerInMapOffline(Player player, long idPlayer) {
        for (Player pl : bosses) {
            if (pl.id == idPlayer && pl instanceof TrainingBoss && ((TrainingBoss) pl).playerAtt.equals(player)) {
                return pl;
            }
        }
        return null;
    }

    public void pickItem(Player player, int itemMapId) {
        ItemMap itemMap = getItemMapByItemMapId(itemMapId);
        if (itemMap != null && !itemMap.isPickedUp) {
            synchronized (itemMap) {
                if (!itemMap.isPickedUp) {
                    if (itemMap.itemTemplate != null) {
                        if (itemMap.itemTemplate.type == 22) {
                            return;
                        }

                        if (itemMap.itemTemplate.id == 78) {
                            int playerTaskId = TaskService.gI().getIdTask(player);
                            if (playerTaskId != ConstTask.TASK_3_1) {
                                Service.gI().sendThongBao(player, "Bạn cần làm nhiệm vụ 3_1 để nhặt vật phẩm này");
                                return;
                            }
                        }
                        int playerId = Math.abs(itemMap.playerId > 100_000_000 ? 1_000_000_000 - (int) itemMap.playerId
                                : (int) itemMap.playerId);
                        boolean canPickup = false;
                        if (itemMap.itemTemplate.id == 78) {
                            canPickup = (itemMap.playerId == player.id);
                        } else {
                            canPickup = (playerId == player.id || itemMap.playerId == player.id
                                    || itemMap.playerId == -1);
                        }

                        if (canPickup) {
                            Item item = ItemService.gI().createItemFromItemMap(itemMap);
                            if (item == null || item.template == null) {
                                return;
                            }
                            boolean picked = false;

                            if (item.template.id == 648) {
                                if (!InventoryService.gI().findItemTatVoGiangSinh(player)) {
                                    Service.gI().sendThongBao(player, "Cần thêm Tất,vớ giáng sinh");
                                    return;
                                }
                            }

                            if (InventoryService.gI().addItemBag(player, item)) {
                                if (item == null || item.template == null) {
                                    Logger.logException(Zone.class,
                                            new Exception("Item or template is null after addItemBag"));
                                    return;
                                }
                                int itemType = item.template.type;
                                Message msg;
                                try {
                                    msg = new Message(-20);
                                    msg.writer().writeShort(itemMapId);
                                    switch (itemType) {
                                        case 9, 10, 34 -> {
                                            msg.writer()
                                                    .writeUTF(item.quantity > Short.MAX_VALUE
                                                            ? "Bạn vừa nhận được " + Util.formatNumber(item.quantity)
                                                                    + " " + item.template.name
                                                            : "");
                                            PlayerService.gI().sendInfoHpMpMoney(player);
                                        }
                                        default -> {
                                            // Kiểm tra lại template trước khi sử dụng
                                            if (item.template != null) {
                                                switch (item.template.id) {
                                                    case 73 ->
                                                        msg.writer().writeUTF("");
                                                    case 74 ->
                                                        msg.writer().writeUTF("Bạn mới vừa ăn " + item.template.name);
                                                    case 78 ->
                                                        msg.writer().writeUTF("Wow, một cậu bé dễ thương!");
                                                    default -> {
                                                        if (item.template.type >= 0 && item.template.type < 5) {
                                                            msg.writer()
                                                                    .writeUTF("Bạn nhận được " + item.template.name);
                                                        }
                                                        if (item.template.id == 648) {
                                                            InventoryService.gI().subQuantityItemsBag(player,
                                                                    InventoryService.gI().findItemBag(player, 649), 1);
                                                        }
                                                    }
                                                }
                                            }
                                            InventoryService.gI().sendItemBags(player);
                                        }
                                    }
                                    msg.writer().writeShort(item.quantity > Short.MAX_VALUE ? 9999 : item.quantity);
                                    player.sendMessage(msg);
                                    msg.cleanup();
                                    Service.gI().sendToAntherMePickItem(player, itemMapId);

                                    picked = true;
                                    if (itemMap.playerOriginDrop) {
                                        AssetAuditService.gI().recordItemMap("PLAYER_PICKUP", itemMap, null, player,
                                                "Nhặt vật phẩm do người chơi vứt");
                                    }
                                    if (itemMap.itemTemplate != null && itemMap.itemTemplate.id != 74) {
                                        itemMap.isPickedUp = true;
                                    }

                                    if (!(this.map.mapId >= 21 && this.map.mapId <= 23
                                            && itemMap.itemTemplate != null && itemMap.itemTemplate.id == 74
                                            || this.map.mapId >= 42 && this.map.mapId <= 44
                                                    && itemMap.itemTemplate != null && itemMap.itemTemplate.id == 78)) {
                                        removeItemMap(itemMap);
                                    }
                                } catch (Exception e) {
                                    Logger.logException(Zone.class, e);
                                }
                            } else {
                                if (item != null && item.template != null) {
                                    if (!ItemMapService.gI().isBlackBall(item.template.id)
                                            && !ItemMapService.gI().isNamecBall(item.template.id)
                                            && !ItemMapService.gI().isNamecBallStone(item.template.id)) {
                                        String text = "Hành trang không còn chỗ trống, không thể nhặt thêm";
                                        Service.gI().sendThongBao(player, text);
                                        return;
                                    }
                                }
                            }

                            if (picked && itemMap != null) {
                                TaskService.gI().checkDoneTaskPickItem(player, itemMap);
                                TaskService.gI().checkDoneSideTaskPickItem(player, itemMap);
                                TaskService.gI().checkDoneClanTaskPickItem(player, itemMap);
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Không thể nhặt vật phẩm của người khác");
                            return;
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                }
            }
        }
    }

    public void addItem(ItemMap itemMap) {
        if (itemMap != null && !items.contains(itemMap)) {
            items.add(0, itemMap);
        }
    }

    public void removeItemMap(ItemMap itemMap) {
        this.items.remove(itemMap);
    }

    public Player getRandomPlayerInMap() {
        // Fix: Duyệt 1 lần thay vì 2 lần - collect eligible players trước
        List<Player> eligible = new ArrayList<>();
        for (Player pl : this.notBosses) {
            if (pl != null && (pl.effectSkin == null || !pl.effectSkin.isVoHinh)
                    && pl.maBuHold == null && !pl.isMabuHold) {
                eligible.add(pl);
            }
        }

        if (eligible.isEmpty()) {
            return null;
        }

        return eligible.get(Util.nextInt(0, eligible.size() - 1));
    }

    // View distance cho player loading (1440 pixels ~ 2 screen widths)
    private static final int PLAYER_VIEW_DISTANCE = 1440;

    public void load_Me_To_Another(Player player) {
        try {
            if (player == null || player.zone == null || player.location == null) {
                return;
            }
            List<Player> targetPlayers;
            boolean isOfflineMap = MapService.gI().isMapOffline(this.map.mapId);
            if (isOfflineMap) {
                if (player instanceof TrainingBoss || player instanceof NonInteractiveNPC) {
                    targetPlayers = new ArrayList<>();
                    for (int i = players.size() - 1; i >= 0; i--) {
                        Player pl = players.get(i);
                        if (pl != null && !player.equals(pl)) {
                            if (player instanceof NonInteractiveNPC
                                    || ((TrainingBoss) player).playerAtt.equals(pl)) {
                                targetPlayers.add(pl);
                            }
                        }
                    }
                } else {
                    return;
                }
            } else {
                // Gửi đến TẤT CẢ player trong zone (không filter khoảng cách)
                // giống behavior của sendPlayerMove (CMD -7) để đảm bảo tất cả thấy nhau
                List<Player> humanoidsSnapshot = new ArrayList<>(this.humanoids);
                targetPlayers = new ArrayList<>();
                for (Player pl : humanoidsSnapshot) {
                    if (pl != null && !player.equals(pl) && pl.zone == this) {
                        targetPlayers.add(pl);
                    }
                }
            }
            if (!targetPlayers.isEmpty()) {
                if (targetPlayers.size() > 5) {
                    targetPlayers.parallelStream()
                            .filter(pl -> pl != null && pl.getSession() != null)
                            .forEach(pl -> {
                                try {
                                    infoPlayer(pl, player);
                                    // Gửi effect set cho người khác nếu player có effect
                                    if (player.setClothes != null && player.setClothes.hasSetEffect()) {
                                        player.setClothes.sendSetEffectToPlayer(pl);
                                    }
                                } catch (Exception e) {
                                    Logger.logException(Zone.class, e,
                                            "Lỗi infoPlayer trong parallel stream");
                                }
                            });
                } else {
                    for (Player pl : targetPlayers) {
                        if (pl != null && pl.getSession() != null) {
                            infoPlayer(pl, player);
                            // Gửi effect set cho người khác nếu player có effect
                            if (player.setClothes != null && player.setClothes.hasSetEffect()) {
                                player.setClothes.sendSetEffectToPlayer(pl);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Lỗi load_Me_To_Another");
        }
    }

    public void load_Another_To_Me(Player player) {
        try {
            if (player == null || player.location == null || player.getSession() == null) {
                return;
            }

            List<Player> sourcePlayers;
            boolean isOfflineMap = MapService.gI().isMapOffline(this.map.mapId);

            if (isOfflineMap) {
                sourcePlayers = new ArrayList<>();
                for (int i = this.humanoids.size() - 1; i >= 0; i--) {
                    Player pl = this.humanoids.get(i);
                    if (pl != null && pl.nPoint != null) {
                        if (pl instanceof NonInteractiveNPC
                                || (pl instanceof TrainingBoss
                                        && ((TrainingBoss) pl).playerAtt.equals(player))) {
                            sourcePlayers.add(pl);
                        }
                    }
                }
            } else {
                // Gửi info TẤT CẢ player trong zone cho player mới join
                // không filter khoảng cách - giống behavior CMD -7 movement packet
                List<Player> humanoidsSnapshot = new ArrayList<>(this.humanoids);
                sourcePlayers = new ArrayList<>();
                for (Player pl : humanoidsSnapshot) {
                    if (pl != null && !player.equals(pl) && pl.nPoint != null && pl.zone == this) {
                        sourcePlayers.add(pl);
                    }
                }
            }
            if (!sourcePlayers.isEmpty()) {
                if (sourcePlayers.size() > 64) {
                    sourcePlayers.parallelStream()
                            .filter(pl -> pl != null && pl.nPoint != null)
                            .forEach(pl -> {
                                try {
                                    infoPlayer(player, pl);
                                    // Gửi effect set của người khác cho player nếu họ có effect
                                    if (pl.setClothes != null && pl.setClothes.hasSetEffect()) {
                                        pl.setClothes.sendSetEffectToPlayer(player);
                                    }
                                } catch (Exception e) {
                                    Logger.logException(Zone.class, e,
                                            "Lỗi infoPlayer trong parallel stream");
                                }
                            });
                } else {
                    for (Player pl : sourcePlayers) {
                        if (pl != null && pl.nPoint != null) {
                            infoPlayer(player, pl);
                            // Gửi effect set của người khác cho player nếu họ có effect
                            if (pl.setClothes != null && pl.setClothes.hasSetEffect()) {
                                pl.setClothes.sendSetEffectToPlayer(player);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Lỗi load_Another_To_Me");
        }
    }

    public void loadBoss(Boss boss) {
        try {
            if (MapService.gI().isMapOffline(this.map.mapId)) {
                for (Player pl : this.bosses) {
                    if (!boss.equals(pl) && !pl.isPl() && !pl.isPet && !pl.isNewPet) {
                        infoPlayer(boss, pl);
                        infoPlayer(pl, boss);
                    }
                }
            } else {
                for (Player pl : this.bosses) {
                    if (!boss.equals(pl)) {
                        infoPlayer(boss, pl);
                        infoPlayer(pl, boss);
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(MapService.class, e);
        }
    }

    private void infoPlayer(Player plReceive, Player plInfo) {
        Message msg;
        try {
            if (plInfo == null || plInfo.nPoint == null) {
                Logger.warning("Player or nPoint is null in infoPlayer: " + (plInfo != null ? plInfo.name : "null"));
                return;
            }

            msg = new Message(-5);
            msg.writer().writeInt((int) plInfo.id);
            if (plInfo.clan != null) {
                msg.writer().writeInt(plInfo.clan.id);
            } else if (plInfo.isBoss && (plInfo.id == BossID.MABU || plInfo.id == BossID.SUPERBU)) {
                msg.writer().writeInt(-100);
            } else if (plInfo.isCopy) {
                msg.writer().writeInt(-2);
            } else {
                msg.writer().writeInt(-1);
            }
            msg.writer().writeByte(Service.gI().getCurrLevel(plInfo));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(plInfo.typePk);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeShort(plInfo.getHead());
            msg.writer().writeUTF(Service.gI().name(plInfo));
            msg.writeSmartLong(Util.maxIntValue(plInfo.nPoint.hp));
            msg.writeSmartLong(Util.maxIntValue(plInfo.nPoint.hpMax));
            msg.writer().writeShort(plInfo.getBody());
            msg.writer().writeShort(plInfo.getLeg());
            int flagbag = plInfo.getFlagBag();
            if (plReceive.isPl() && plReceive.getSession() != null && plReceive.getSession().version >= 228) {
                switch (flagbag) {
                    case 83:
                        flagbag = 205;
                        break;
                }
            }
            msg.writer().writeShort(flagbag); // bag - client reads as readShort()
            msg.writer().writeByte(-1);        // weapon slot - client reads as readByte() 'b'
            msg.writer().writeShort(plInfo.location.x);
            msg.writer().writeShort(plInfo.location.y);
            msg.writer().writeShort(0); // effbuffhp
            msg.writer().writeShort(0); // effbuffmp

            msg.writer().writeByte(0); // num eff

            msg.writer().writeByte(plInfo.idMark.getIdSpaceShip());

            msg.writer().writeByte(plInfo.effectSkill != null && plInfo.effectSkill.isMonkey ? 1 : 0);
            msg.writer().writeShort(plInfo.getMount());
            msg.writer().writeByte(plInfo.cFlag);

            msg.writer().writeByte(0);
            msg.writer().writeShort(plInfo.getAura()); // idauraeff
            msg.writer().writeByte(plInfo.getEffFront()); // seteff
            msg.writer().writeShort(plInfo.getHat()); // id hat

            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Service.gI()
                .sendFlagPlayerToMe(plReceive, plInfo);
        try {
            if (plInfo.isPl()) {
                if (plInfo.effectSkill != null && plInfo.effectSkill.isChibi && plInfo.typeChibi != -1) {
                    Service.gI().sendChibiFollowToMe(plReceive, plInfo);
                }
            }
        } catch (Exception e) {
        }

        try {
            if (plInfo.isDie()) {
                msg = new Message(-8);
                msg.writer().writeInt((int) plInfo.id);
                msg.writer().writeByte(0);
                msg.writer().writeShort(plInfo.location.x);
                msg.writer().writeShort(plInfo.location.y);
                plReceive.sendMessage(msg);
                msg.cleanup();
            }
        } catch (Exception e) {

        }
    }

    public void mapInfo(Player pl) {
        Message msg;
        try {
            msg = new Message(-24);
            msg.writer().writeByte(this.map.mapId);
            msg.writer().writeByte(this.map.planetId);
            msg.writer().writeByte(this.map.tileId);
            msg.writer().writeByte(this.map.bgId);
            msg.writer().writeByte(this.map.type);
            msg.writer().writeUTF(this.map.mapName);
            msg.writer().writeByte(this.zoneId);

            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);

            try {
                List<WayPoint> wayPoints = this.map.wayPoints;
                msg.writer().writeByte(wayPoints.size());
                for (WayPoint wp : wayPoints) {
                    msg.writer().writeShort(wp.minX);
                    msg.writer().writeShort(wp.minY);
                    msg.writer().writeShort(wp.maxX);
                    msg.writer().writeShort(wp.maxY);
                    msg.writer().writeBoolean(wp.isEnter);
                    msg.writer().writeBoolean(wp.isOffline);
                    msg.writer().writeUTF(wp.name);
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            try {
                // Fix: Duyệt 1 lần thay vì 2 lần - collect valid mobs trước
                List<Mob> validMobs = new ArrayList<>();
                for (Mob mob : this.mobs) {
                    if (!(mob.isBigBoss() && mob.tempId != 70 && mob.isDie())) {
                        validMobs.add(mob);
                    }
                }

                msg.writer().writeByte(validMobs.size());
                for (Mob mob : validMobs) {
                    msg.writer().writeBoolean(false); // is disable
                    msg.writer().writeBoolean(mob.tempId == ConstMob.TEST_DAME);
                    msg.writer().writeBoolean(false); // is fire
                    msg.writer().writeBoolean(false); // is ice
                    msg.writer().writeBoolean(false); // is wind
                    msg.writer().writeShort(mob.tempId);
                    msg.writer().writeByte(0); // sys
                    msg.writeSmartLong(mob.point.gethp());
                    msg.writer().writeByte(mob.level);
                    msg.writeSmartLong((mob.point.getHpFull()));
                    msg.writer().writeShort(mob.location.x);
                    msg.writer().writeShort(mob.location.y);
                    msg.writer().writeByte(mob.status);
                    msg.writer().writeByte(mob.lvMob);
                    msg.writer()
                            .writeBoolean(mob.tempId == ConstMob.GAU_TUONG_CUOP || (mob.tempId >= ConstMob.VOI_CHIN_NGA
                                    && mob.tempId <= ConstMob.PIANO));
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            msg.writer().writeByte(0);

            try {
                List<Npc> npcs = NpcManager.getNpcsByMapPlayer(pl);
                msg.writer().writeByte(npcs.size());
                for (Npc npc : npcs) {
                    msg.writer().writeByte(npc.status);
                    msg.writer().writeShort(npc.cx);
                    msg.writer().writeShort(npc.cy);
                    msg.writer().writeByte(npc.tempId);
                    msg.writer().writeShort(npc.avartar);
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            try {
                int playerTaskId = TaskService.gI().getIdTask(pl);
                List<ItemMap> validItems = new ArrayList<>();
                for (ItemMap item : this.items) {
                    if (item.itemTemplate.id == 78) {
                        if (playerTaskId != ConstTask.TASK_3_1) {
                            continue;
                        }
                        if (item.playerId != pl.id && item.playerId != -1) {
                            continue;
                        }
                    }
                    if (item.itemTemplate.id == 74) {
                        if (playerTaskId < ConstTask.TASK_3_0) {
                            continue;
                        }
                    }
                    if (item.itemTemplate.id == 726 && item.playerId != pl.id) {
                        continue;
                    }
                    validItems.add(item);
                }

                msg.writer().writeByte(validItems.size());
                for (ItemMap it : validItems) {
                    msg.writer().writeShort(it.itemMapId);
                    msg.writer().writeShort(it.itemTemplate.id);
                    msg.writer().writeShort(it.x);
                    msg.writer().writeShort(it.y);
                    msg.writer().writeInt((int) it.playerId);
                }
            } catch (Exception e) {
                msg.writer().writeByte(0);
            }

            try {
                byte[] bgItem = null;
                java.lang.ref.SoftReference<byte[]> refBg = BG_ITEM_CACHE.get(this.map.mapId);
                if (refBg != null) {
                    bgItem = refBg.get();
                }
                if (bgItem == null) {
                    bgItem = FileIO.readFile("data/map/item_bg_map_data/" + this.map.mapId);
                    if (bgItem != null) {
                        BG_ITEM_CACHE.put(this.map.mapId, new java.lang.ref.SoftReference<>(bgItem));
                    }
                }
                if (bgItem != null) {
                    msg.writer().write(bgItem);
                } else {
                    msg.writer().writeShort(0);
                }
            } catch (Exception e) {
                msg.writer().writeShort(0);
            }

            try {
                byte[] effItem = null;
                java.lang.ref.SoftReference<byte[]> refEff = EFF_ITEM_CACHE.get(this.map.mapId);
                if (refEff != null) {
                    effItem = refEff.get();
                }
                if (effItem == null) {
                    effItem = FileIO.readFile("data/map/eff_map/" + this.map.mapId);
                    if (effItem != null) {
                        EFF_ITEM_CACHE.put(this.map.mapId, new java.lang.ref.SoftReference<>(effItem));
                    }
                }
                
                //beff, 1 la roi
                if (effItem != null) {
                    msg.writer().write(effItem);
                } else {
                    msg.writer().writeShort(0);
                }
            } catch (Exception e) {
                msg.writer().writeShort(0);
            }

            msg.writer().writeByte(this.map.bgType);
            msg.writer().writeByte(pl.idMark.getIdSpaceShip());
            msg.writer().writeByte(this.map.mapId == 148 ? 1 : 0);
            pl.sendMessage(msg);

            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public TrapMap isInTrap(Player player) {
        for (TrapMap trap : this.trapMaps) {
            if (player.location.x >= trap.x && player.location.x <= trap.x + trap.w
                    && player.location.y >= trap.y && player.location.y <= trap.y + trap.h) {
                return trap;
            }
        }
        return null;
    }

    public void sendBigBoss(Player player) {
        for (Mob mob : this.mobs) {
            if (mob.tempId == ConstMob.HIRUDEGARN && !mob.isDie()) {
                if (mob.lvMob >= 2) {
                    Service.gI().sendBigBoss2(player, 5, mob);
                } else if (mob.lvMob >= 1) {
                    Service.gI().sendBigBoss2(player, 6, mob);
                }
                break;
            }
        }
    }

    public MaBuHold getMaBuHold() {
        for (MaBuHold hold : MapService.gI().getMapById(128).zones.get(this.zoneId).maBuHolds) {
            if (hold.player == null) {
                return hold;
            }
        }
        return null;
    }

    public void setMaBuHold(int slot, int zoneId, Player player) {
        MapService.gI().getMapById(128).zones.get(zoneId).maBuHolds.set(slot, new MaBuHold(slot, player));
    }

    public boolean isKhongCoTrongTaiTrongKhu() {
        boolean kovao = true;
        for (Player pl : players) {
            if (!pl.isPl()) {
                kovao = false;
                break;
            }
            if ((pl.zone.map.mapId >= 21
                    && pl.zone.map.mapId <= 23)
                    || pl.zone.map.mapId == 170
                    || pl.zone.map.mapId == 153
                    || pl.zone.map.mapId == 52
                    || pl.zone.map.mapId == 113
                    || pl.zone.map.mapId == 129
                    || MapService.gI().isMapCL(pl.zone.map.mapId)
                    || MapService.gI().isMapDoanhTrai(pl.zone.map.mapId)
                    || MapService.gI().isMapBlackBallWar(pl.zone.map.mapId)
                    || MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)
                    || MapService.gI().isMapPhoBan(pl.zone.map.mapId)
                    || MapService.gI().isMapMaBu(pl.zone.map.mapId)
                    || MapService.gI().isMapKhiGasHuyDiet(pl.zone.map.mapId)
                    || MapService.gI().isMapConDuongRanDoc(pl.zone.map.mapId)
                    || MapService.gI().isMapOffline(pl.zone.map.mapId)) {
                kovao = false;
            }
        }
        return kovao;
    }
}
