package map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import mob.Mob;
import player.Player;

public class SpatialGrid {

    private static final int DEFAULT_CELL_SIZE = 200;
    private static final int MAX_ENTITIES_PER_QUERY = 1000;

    private final int cellSize;
    private final int mapWidth;
    private final int mapHeight;
    private final int gridWidth;
    private final int gridHeight;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Player>> playerGrid;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Mob>> mobGrid;

    public SpatialGrid(int mapWidth, int mapHeight) {
        this(mapWidth, mapHeight, DEFAULT_CELL_SIZE);
    }

    public SpatialGrid(int mapWidth, int mapHeight, int cellSize) {
        if (mapWidth <= 0 || mapHeight <= 0 || cellSize <= 0) {
            throw new IllegalArgumentException("Kich thuoc map va cell phai lon hon 0");
        }

        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.cellSize = cellSize;
        this.gridWidth = (mapWidth + cellSize - 1) / cellSize;
        this.gridHeight = (mapHeight + cellSize - 1) / cellSize;

        this.playerGrid = new ConcurrentHashMap<>(gridWidth * gridHeight);
        this.mobGrid = new ConcurrentHashMap<>(gridWidth * gridHeight);
    }

    private String getCellKey(int gridX, int gridY) {
        return gridX + ":" + gridY;
    }

    private int getGridX(int worldX) {
        if (worldX < 0) {
            return 0;
        }
        if (worldX >= mapWidth) {
            return gridWidth - 1;
        }
        return worldX / cellSize;
    }

    private int getGridY(int worldY) {
        if (worldY < 0) {
            return 0;
        }
        if (worldY >= mapHeight) {
            return gridHeight - 1;
        }
        return worldY / cellSize;
    }

    private boolean isValidLocation(int x, int y) {
        return x >= 0 && x < mapWidth && y >= 0 && y < mapHeight;
    }

    public void clear() {
        playerGrid.clear();
        mobGrid.clear();
    }

    public void addPlayer(Player player) {
        if (player == null || player.location == null || player.isDie()) {
            return;
        }

        if (!isValidLocation(player.location.x, player.location.y)) {
            return;
        }

        int gx = getGridX(player.location.x);
        int gy = getGridY(player.location.y);
        String key = getCellKey(gx, gy);

        CopyOnWriteArrayList<Player> list = playerGrid.get(key);
        if (list == null) {
            list = playerGrid.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        }
        // Tránh duplicate để giảm memory
        if (!list.contains(player)) {
            list.add(player);
        }
    }

    public void addMob(Mob mob) {
        if (mob == null || mob.location == null || mob.isDie()) {
            return;
        }

        if (!isValidLocation(mob.location.x, mob.location.y)) {
            return;
        }

        int gx = getGridX(mob.location.x);
        int gy = getGridY(mob.location.y);
        String key = getCellKey(gx, gy);

        CopyOnWriteArrayList<Mob> list = mobGrid.get(key);
        if (list == null) {
            // Tối ưu: Chỉ tạo list mới khi cần, giảm allocations
            list = mobGrid.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        }
        // Tránh duplicate để giảm memory
        if (!list.contains(mob)) {
            list.add(mob);
        }
    }

    public void removePlayer(Player player) {
        if (player == null || player.location == null) {
            return;
        }

        int gx = getGridX(player.location.x);
        int gy = getGridY(player.location.y);
        String key = getCellKey(gx, gy);

        CopyOnWriteArrayList<Player> list = playerGrid.get(key);
        if (list != null) {
            list.remove(player);
            if (list.isEmpty()) {
                playerGrid.remove(key);
            }
        }
    }

    public void removeMob(Mob mob) {
        if (mob == null || mob.location == null) {
            return;
        }

        int gx = getGridX(mob.location.x);
        int gy = getGridY(mob.location.y);
        String key = getCellKey(gx, gy);

        CopyOnWriteArrayList<Mob> list = mobGrid.get(key);
        if (list != null) {
            list.remove(mob);
            if (list.isEmpty()) {
                mobGrid.remove(key);
            }
        }
    }

    public void updatePlayerPosition(Player player, int oldX, int oldY) {
        if (player == null || player.location == null) {
            return;
        }

        int oldGx = getGridX(oldX);
        int oldGy = getGridY(oldY);
        int newGx = getGridX(player.location.x);
        int newGy = getGridY(player.location.y);

        if (oldGx != newGx || oldGy != newGy) {
            String oldKey = getCellKey(oldGx, oldGy);
            String newKey = getCellKey(newGx, newGy);

            CopyOnWriteArrayList<Player> oldList = playerGrid.get(oldKey);
            if (oldList != null) {
                oldList.remove(player);
                if (oldList.isEmpty()) {
                    playerGrid.remove(oldKey);
                }
            }

            playerGrid.computeIfAbsent(newKey, k -> new CopyOnWriteArrayList<>()).add(player);
        }
    }

    public void updateMobPosition(Mob mob, int oldX, int oldY) {
        if (mob == null || mob.location == null) {
            return;
        }

        int oldGx = getGridX(oldX);
        int oldGy = getGridY(oldY);
        int newGx = getGridX(mob.location.x);
        int newGy = getGridY(mob.location.y);

        if (oldGx != newGx || oldGy != newGy) {
            String oldKey = getCellKey(oldGx, oldGy);
            String newKey = getCellKey(newGx, newGy);

            CopyOnWriteArrayList<Mob> oldList = mobGrid.get(oldKey);
            if (oldList != null) {
                oldList.remove(mob);
                if (oldList.isEmpty()) {
                    mobGrid.remove(oldKey);
                }
            }

            mobGrid.computeIfAbsent(newKey, k -> new CopyOnWriteArrayList<>()).add(mob);
        }
    }

    private int getDistanceSquared(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    public List<Player> getPlayersNear(int x, int y, int distance) {
        if (!isValidLocation(x, y) || distance <= 0) {
            return new ArrayList<>();
        }

        List<Player> result = new ArrayList<>();
        int distanceSquared = distance * distance;

        int cellRange = (distance / cellSize) + 1;
        int centerX = getGridX(x);
        int centerY = getGridY(y);

        int minGx = Math.max(0, centerX - cellRange);
        int maxGx = Math.min(gridWidth - 1, centerX + cellRange);
        int minGy = Math.max(0, centerY - cellRange);
        int maxGy = Math.min(gridHeight - 1, centerY + cellRange);

        for (int gx = minGx; gx <= maxGx; gx++) {
            for (int gy = minGy; gy <= maxGy; gy++) {
                String key = getCellKey(gx, gy);
                CopyOnWriteArrayList<Player> list = playerGrid.get(key);

                if (list != null) {
                    for (Player p : list) {
                        if (p == null || p.location == null || p.isDie()) {
                            continue;
                        }

                        int distSq = getDistanceSquared(p.location.x, p.location.y, x, y);
                        if (distSq <= distanceSquared) {
                            result.add(p);
                            if (result.size() >= MAX_ENTITIES_PER_QUERY) {
                                return result;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public List<Mob> getMobsNear(int x, int y, int distance) {
        if (!isValidLocation(x, y) || distance <= 0) {
            return new ArrayList<>();
        }

        List<Mob> result = new ArrayList<>();
        int distanceSquared = distance * distance;

        int cellRange = (distance / cellSize) + 1;
        int centerX = getGridX(x);
        int centerY = getGridY(y);

        int minGx = Math.max(0, centerX - cellRange);
        int maxGx = Math.min(gridWidth - 1, centerX + cellRange);
        int minGy = Math.max(0, centerY - cellRange);
        int maxGy = Math.min(gridHeight - 1, centerY + cellRange);

        for (int gx = minGx; gx <= maxGx; gx++) {
            for (int gy = minGy; gy <= maxGy; gy++) {
                String key = getCellKey(gx, gy);
                CopyOnWriteArrayList<Mob> list = mobGrid.get(key);

                if (list != null) {
                    for (Mob m : list) {
                        if (m == null || m.location == null || m.isDie()) {
                            continue;
                        }

                        int distSq = getDistanceSquared(m.location.x, m.location.y, x, y);
                        if (distSq <= distanceSquared) {
                            result.add(m);
                            if (result.size() >= MAX_ENTITIES_PER_QUERY) {
                                return result;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public Player getNearestPlayer(int x, int y, int maxDistance) {
        if (!isValidLocation(x, y) || maxDistance <= 0) {
            return null;
        }

        Player nearest = null;
        int minDistSq = maxDistance * maxDistance;

        int cellRange = (maxDistance / cellSize) + 1;
        int centerX = getGridX(x);
        int centerY = getGridY(y);

        int minGx = Math.max(0, centerX - cellRange);
        int maxGx = Math.min(gridWidth - 1, centerX + cellRange);
        int minGy = Math.max(0, centerY - cellRange);
        int maxGy = Math.min(gridHeight - 1, centerY + cellRange);

        for (int gx = minGx; gx <= maxGx; gx++) {
            for (int gy = minGy; gy <= maxGy; gy++) {
                String key = getCellKey(gx, gy);
                CopyOnWriteArrayList<Player> list = playerGrid.get(key);

                if (list != null) {
                    for (Player p : list) {
                        if (p == null || p.location == null || p.isDie()) {
                            continue;
                        }

                        int distSq = getDistanceSquared(p.location.x, p.location.y, x, y);
                        if (distSq < minDistSq) {
                            minDistSq = distSq;
                            nearest = p;
                        }
                    }
                }
            }
        }

        return nearest;
    }

    public Mob getNearestMob(int x, int y, int maxDistance) {
        if (!isValidLocation(x, y) || maxDistance <= 0) {
            return null;
        }

        Mob nearest = null;
        int minDistSq = maxDistance * maxDistance;

        int cellRange = (maxDistance / cellSize) + 1;
        int centerX = getGridX(x);
        int centerY = getGridY(y);

        int minGx = Math.max(0, centerX - cellRange);
        int maxGx = Math.min(gridWidth - 1, centerX + cellRange);
        int minGy = Math.max(0, centerY - cellRange);
        int maxGy = Math.min(gridHeight - 1, centerY + cellRange);

        for (int gx = minGx; gx <= maxGx; gx++) {
            for (int gy = minGy; gy <= maxGy; gy++) {
                String key = getCellKey(gx, gy);
                CopyOnWriteArrayList<Mob> list = mobGrid.get(key);

                if (list != null) {
                    for (Mob m : list) {
                        if (m == null || m.location == null || m.isDie()) {
                            continue;
                        }

                        int distSq = getDistanceSquared(m.location.x, m.location.y, x, y);
                        if (distSq < minDistSq) {
                            minDistSq = distSq;
                            nearest = m;
                        }
                    }
                }
            }
        }

        return nearest;
    }

    public boolean hasPlayersNearby(int x, int y, int distance) {
        if (!isValidLocation(x, y) || distance <= 0) {
            return false;
        }

        int distanceSquared = distance * distance;
        int cellRange = (distance / cellSize) + 1;
        int centerX = getGridX(x);
        int centerY = getGridY(y);

        int minGx = Math.max(0, centerX - cellRange);
        int maxGx = Math.min(gridWidth - 1, centerX + cellRange);
        int minGy = Math.max(0, centerY - cellRange);
        int maxGy = Math.min(gridHeight - 1, centerY + cellRange);

        for (int gx = minGx; gx <= maxGx; gx++) {
            for (int gy = minGy; gy <= maxGy; gy++) {
                String key = getCellKey(gx, gy);
                CopyOnWriteArrayList<Player> list = playerGrid.get(key);

                if (list != null) {
                    for (Player p : list) {
                        if (p != null && p.location != null && !p.isDie()) {
                            int distSq = getDistanceSquared(p.location.x, p.location.y, x, y);
                            if (distSq <= distanceSquared) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public int getPlayerCount() {
        int count = 0;
        for (CopyOnWriteArrayList<Player> list : playerGrid.values()) {
            count += list.size();
        }
        return count;
    }

    public int getMobCount() {
        int count = 0;
        for (CopyOnWriteArrayList<Mob> list : mobGrid.values()) {
            count += list.size();
        }
        return count;
    }

    public int getActiveCellCount() {
        return playerGrid.size() + mobGrid.size();
    }

    public void cleanupEmptyCells() {
        playerGrid.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        mobGrid.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
