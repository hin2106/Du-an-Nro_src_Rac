package services.map;

import map.Zone;
import player.Player;
import utils.Logger;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Parallel processing - gửi song song khi nhiều players Limit players - giới
 * hạn số players load cùng lúc Delay non-critical - delay effects và animations
 */
public class MapLoadOptimizer {

    private static volatile MapLoadOptimizer instance;
    private final ExecutorService loadExecutor = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "MapLoadOptimizer-Worker");
                t.setDaemon(true);
                return t;
            });

    // Giới hạn số players load cùng lúc
    private static final int MAX_PLAYERS_LOAD_PER_BATCH = 100;
    private static final int MAX_PLAYERS_FOR_PARALLEL = 12;

    private MapLoadOptimizer() {
    }

    public static MapLoadOptimizer gI() {
        if (instance == null) {
            synchronized (MapLoadOptimizer.class) {
                if (instance == null) {
                    instance = new MapLoadOptimizer();
                }
            }
        }
        return instance;
    }

    /**
     * Tối ưu load players to me - batch và parallel nếu nhiều
     */
    public void optimizeLoadPlayersToMe(Player targetPlayer, List<Player> sourcePlayers) {
        if (targetPlayer == null || targetPlayer.getSession() == null
                || sourcePlayers == null || sourcePlayers.isEmpty()) {
            return;
        }

        // Giới hạn số players load
        List<Player> playersToLoad = sourcePlayers;
        if (sourcePlayers.size() > MAX_PLAYERS_LOAD_PER_BATCH) {
            playersToLoad = sourcePlayers.subList(0, MAX_PLAYERS_LOAD_PER_BATCH);
        }

        // Parallel nếu nhiều players
        if (playersToLoad.size() > MAX_PLAYERS_FOR_PARALLEL) {
            playersToLoad.parallelStream()
                    .filter(pl -> pl != null && pl.nPoint != null)
                    .forEach(pl -> {
                        try {
                            // Sử dụng Zone method trực tiếp
                            if (targetPlayer.zone != null) {
                                targetPlayer.zone.load_Another_To_Me(targetPlayer);
                            }
                        } catch (Exception e) {
                            Logger.logException(MapLoadOptimizer.class, e,
                                    "Error optimizing load players to me");
                        }
                    });
        } else {
            // Ít players, dùng method Zone có sẵn
            if (targetPlayer.zone != null) {
                targetPlayer.zone.load_Another_To_Me(targetPlayer);
            }
        }
    }

    /**
     * Tối ưu load me to others - batch và parallel
     */
    public void optimizeLoadMeToOthers(Player player, List<Player> targetPlayers) {
        if (player == null || player.zone == null
                || targetPlayers == null || targetPlayers.isEmpty()) {
            return;
        }
        // Giới hạn số players
        List<Player> playersToLoad = targetPlayers;
        if (targetPlayers.size() > MAX_PLAYERS_LOAD_PER_BATCH) {
            playersToLoad = targetPlayers.subList(0, MAX_PLAYERS_LOAD_PER_BATCH);
        }
        // Parallel nếu nhiều
        if (playersToLoad.size() > MAX_PLAYERS_FOR_PARALLEL) {
            playersToLoad.parallelStream()
                    .filter(pl -> pl != null && pl.getSession() != null && pl.zone != null)
                    .forEach(pl -> {
                        try {
                            pl.zone.load_Me_To_Another(pl);
                        } catch (Exception e) {
                            Logger.logException(MapLoadOptimizer.class, e,
                                    "Error optimizing load me to others");
                        }
                    });
        } else {
            // Ít players, dùng method Zone có sẵn
            if (player.zone != null) {
                player.zone.load_Me_To_Another(player);
            }
        }
    }


    public void delayNonCriticalUpdates(Player player, Runnable updates) {
        if (player == null || updates == null) {
            return;
        }
        // Delay 50-100ms để ưu tiên essentials
        loadExecutor.submit(() -> {
            try {
                Thread.sleep(50);
                updates.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Logger.logException(MapLoadOptimizer.class, e,
                        "Error in delayed non-critical updates");
            }
        });
    }

    /**
     * Lấy players trong view distance với limit
     */
    public List<Player> getPlayersInViewDistance(Zone zone, Player player, int maxCount) {
        if (zone == null || player == null || player.location == null) {
            return List.of();
        }

        boolean isOfflineMap = MapService.gI().isMapOffline(zone.map.mapId);
        if (isOfflineMap) {
            return List.of();
        }

        // Sử dụng public method từ Zone
        List<Player> nearbyPlayers = zone.getPlayersNear(
                player.location.x,
                player.location.y,
                1000); // VIEW_DISTANCE

        // Filter và giới hạn
        nearbyPlayers.removeIf(pl -> pl == null || pl.equals(player) || pl.nPoint == null);

        if (nearbyPlayers.size() > maxCount) {
            // Sort by distance và lấy gần nhất
            nearbyPlayers.sort((p1, p2) -> {
                int d1 = Math.abs(p1.location.x - player.location.x)
                        + Math.abs(p1.location.y - player.location.y);
                int d2 = Math.abs(p2.location.x - player.location.x)
                        + Math.abs(p2.location.y - player.location.y);
                return Integer.compare(d1, d2);
            });
            return nearbyPlayers.subList(0, maxCount);
        }

        return nearbyPlayers;
    }

    /**
     * Shutdown executor
     */
    public void shutdown() {
        if (loadExecutor != null) {
            loadExecutor.shutdown();
            try {
                if (!loadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    loadExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                loadExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
