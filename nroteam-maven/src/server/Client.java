package server;

import daos.PlayerDAO;
import data.AlyraManager;
import lombok.Getter;
import map.ItemMap;
import player.Player;
import network.MySession;
import services.ItemTimeService;
import services.Service;
import services.map.ChangeMapService;
import services.shenron.SummonDragon;
import services.func.TransactionService;
import services.dungeon.NgocRongNamecService;
import utils.Functions;
import utils.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import player.PlayerManager;
import player.PlayerScheduler;
import services.shenron.SummonDragonNamek;

public class Client implements Runnable {

    private static Client instance;

    private final Map<Long, Player> players_id = new HashMap<>();
    private final Map<Integer, Player> players_userId = new HashMap<>();
    private final Map<String, Player> players_name = new HashMap<>();
    @Getter
    private final List<Player> players = new ArrayList<>();

    private Client() {
        new Thread(this, "Update Client").start();
        PlayerScheduler.startScheduler();
    }

    public static Client gI() {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

    public void put(Player player) {
        if (player == null || player.id <= 0) {
            return;
        }

        MySession oldSessionToKick = null;
        synchronized (players) {
            Player existing = players_id.get(player.id);
            if (existing != null && existing != player) {
            }
            if (player.getSession() != null && player.getSession().userId > 0) {
                Player existingByUserId = players_userId.get(player.getSession().userId);
                if (existingByUserId != null && existingByUserId != player) {
                    oldSessionToKick = existingByUserId.getSession();
                }
            }

            this.players_id.put(player.id, player);
            this.players_name.put(player.name, player);
            if (player.getSession() != null) {
                this.players_userId.put(player.getSession().userId, player);
            }
            if (!players.contains(player)) {
                this.players.add(player);
            }
        }
        if (oldSessionToKick != null) {
            kickSession(oldSessionToKick);
        }
        PlayerManager.addPlayer(player);
    }

    private void remove(MySession session) {
        if (session.player != null) {
            this.remove(session.player);
            session.player.dispose();
        }
        if (session.joinedGame) {
            session.joinedGame = false;
            try {
                AlyraManager.executeUpdate("update account set last_time_logout = ? where id = ?",
                        new Timestamp(System.currentTimeMillis()), session.userId);
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }
        ServerManager.gI().disconnect(session);
    }

    private void remove(Player player) {
        synchronized (players) {
            this.players_id.remove(player.id);
            this.players_name.remove(player.name);
            this.players_userId.remove(player.getSession().userId);
            this.players.remove(player);
        }

        if (!player.beforeDispose) {
            player.beforeDispose = true;
            player.mapIdBeforeLogout = player.zone.map.mapId;

            if (player.idNRNM != -1) {
                ItemMap itemMap = new ItemMap(player.zone, player.idNRNM, 1, player.location.x, player.location.y, -1);
                Service.gI().dropItemMap(player.zone, itemMap);
                NgocRongNamecService.gI().pNrNamec[player.idNRNM - 353] = "";
                NgocRongNamecService.gI().idpNrNamec[player.idNRNM - 353] = -1;
                player.idNRNM = -1;
            }
            if (player.zone != null && player.zone.spatialGridEnabled && player.zone.spatialGrid != null
                    && player.isPl() && player.location != null) {
                try {
                    player.zone.spatialGrid.removePlayer(player);
                } catch (Exception e) {
                    Logger.logException(Client.class, e, "Lỗi remove player khỏi spatial grid khi logout");
                }
            }
            ChangeMapService.gI().exitMap(player);
            PlayerManager.removePlayer(player);

            TransactionService.gI().cancelTrade(player);
            if (player.clan != null) {
                player.clan.removeMemberOnline(null, player);
            }
            if (SummonDragon.gI().playerSummonShenron != null
                    && SummonDragon.gI().playerSummonShenron.id == player.id) {
                SummonDragon.gI().isPlayerDisconnect = true;
            } else {
                // Cleanup player from dragon star map if not currently summoning
                SummonDragon.gI().cleanupPlayer(player);
            }
            if (SummonDragonNamek.gI().playerSummonShenron != null
                    && SummonDragonNamek.gI().playerSummonShenron.id == player.id) {
                SummonDragonNamek.gI().isPlayerDisconnect = true;
            }
            if (player.shenronEvent != null) {
                player.shenronEvent.isPlayerDisconnect = true;
            }
            if (player.mobMe != null) {
                player.mobMe.mobMeDie();
            }
            if (player.pet != null) {
                if (player.pet.mobMe != null) {
                    player.pet.mobMe.mobMeDie();
                }
                ChangeMapService.gI().exitMap(player.pet);
            }
        }

        ItemTimeService.gI().returnTDLTTimeOnLogout(player);
        PlayerDAO.updatePlayer(player);
    }

    public void kickSession(MySession session) {
        if (session != null) {
            this.remove(session);
            session.disconnect();
        }
    }

    public Player getPlayer(long playerId) {
        return this.players_id.get(playerId);
    }

    public Player getPlayerByUser(int userId) {
        return this.players_userId.get(userId);
    }

    public Player getPlayer(String name) {
        return this.players_name.get(name);
    }

    public void close() {
        Logger.log("BEGIN KICK OUT SESSION " + players.size() + "\n");
        List<Player> playersToKick;
        synchronized (players) {
            playersToKick = new ArrayList<>(players);
            players.clear();
        }
        for (Player pl : playersToKick) {
            if (pl != null && pl.getSession() != null) {
                this.kickSession(pl.getSession());
            }
        }
        Logger.success("SUCCESSFUL\n");
    }

    private void update() {
        List<Player> playersSnapshot;
        synchronized (players) {
            if (players.isEmpty()) {
                return;
            }
            playersSnapshot = new ArrayList<>(players);
        }

        for (Player player : playersSnapshot) {
            if (player != null && player.getSession() != null) {
                MySession session = (MySession) player.getSession();
                if (session.timeWait > 0) {
                    session.timeWait--;
                    if (session.timeWait == 0) {
                        kickSession(session);
                    }
                }
            }
        }
    }

    public Player getPlayerByID(int playerId) {
        Player player = players_id.get((long) playerId);
        if (player != null && player.id == playerId) {
            return player;
        }
        synchronized (players) {
            for (Player p : players) {
                if (p != null && p.id == playerId) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public void run() {
        while (ServerManager.isRunning.get()) {
            long st = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - st), 10));
        }
    }

}
