package managers.tournament;


import daos.PlayerDAO;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import map.Zone;
import data.AlyraManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import server.Maintenance;
import tournament.DeathOrAliveArena;
import utils.Functions;
import utils.Logger;
import utils.Util;

public class DeathOrAliveArenaManager implements Runnable {

    private List<String> cachedTopPlayers = null;
    private long lastTopUpdate = 0;
    private final Object topLock = new Object(); 
    private static final int TOP_COUNT = 100;
    private static final long CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(10);
    private static DeathOrAliveArenaManager instance;
    private volatile long lastUpdate;
    private static final List<DeathOrAliveArena> list = new ArrayList<>();

    public static DeathOrAliveArenaManager gI() {
        if (instance == null) {
            instance = new DeathOrAliveArenaManager();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning()) {
            try {
                long start = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - start), 10));
            } catch (Exception ex) {
            }
        }
    }

    public void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            for (int i = list.size() - 1; i >= 0; i--) {
                if (i < list.size()) {
                    list.get(i).update();
                }
            }
        }
    }

    public void add(DeathOrAliveArena vdst) {
        list.add(vdst);
    }

    public void remove(DeathOrAliveArena vdst) {
        list.remove(vdst);
    }

    public DeathOrAliveArena getVDST(@NonNull Zone zone) {
        for (DeathOrAliveArena vdst : list) {
            if (vdst.getZone().equals(zone)) {
                return vdst;
            }
        }
        return null;
    }
    public List<String> getTopPlayers() {
        long now = System.currentTimeMillis();
        synchronized (topLock) {
            if (cachedTopPlayers != null && (now - lastTopUpdate) < CACHE_DURATION_MS) {
                return cachedTopPlayers;
            }
        }
        Logger.log("Fetching top players from database...");
        List<String> topPlayersList = new ArrayList<>();
        String query = "SELECT p.name, p.data_point " +
                       "FROM player p JOIN account a ON p.account_id = a.id " +
                       "WHERE a.is_admin = 0 AND a.ban = 0 " + 
                       "ORDER BY CAST(JSON_EXTRACT(p.data_point, '$[1]') AS UNSIGNED) DESC " +
                       "LIMIT ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = AlyraManager.getConnection(); 
            if (con == null) {
                 return Collections.emptyList(); 
            }
            ps = con.prepareStatement(query);
            ps.setInt(1, TOP_COUNT); 
            rs = ps.executeQuery();

            int rank = 1;
            while (rs.next()) {
                String name = rs.getString("name");
                String dataPointJson = rs.getString("data_point");
                long power = 0;

                try {
                    Object obj = JSONValue.parseWithException(dataPointJson);
                    if (obj instanceof JSONArray) {
                        JSONArray dataPointArr = (JSONArray) obj;
                        if (dataPointArr.size() > 1) { 
                            power = Long.parseLong(String.valueOf(dataPointArr.get(1)));
                        }
                    }
                } catch (Exception e) {
                    Logger.logException(PlayerDAO.class, e, "Error parsing data_point for player: " + name);
                }
                topPlayersList.add(String.format("Top %d: %s - %s SM",
                        rank++, name, Util.numberToMoney(power)));
            }
             synchronized (topLock) {
                 this.cachedTopPlayers = topPlayersList;
                 this.lastTopUpdate = System.currentTimeMillis();
                 Logger.log("Successfully updated top players cache. Count: " + topPlayersList.size());
             }
             return topPlayersList;

        } catch (SQLException e) {
            Logger.logException(PlayerDAO.class, e, "Database error fetching top players");
            return Collections.emptyList();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException ex) {
                Logger.logException(PlayerDAO.class, ex, "Error closing resources after fetching top players");
            }
        }
    }
}
