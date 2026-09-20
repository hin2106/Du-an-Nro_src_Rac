package daos;

import data.AlyraManager;
import utils.Logger;
import task.KolTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KolTaskDAO {

    private static KolTaskDAO instance;

    public static KolTaskDAO gI() {
        if (instance == null) {
            instance = new KolTaskDAO();
        }
        return instance;
    }

    public KolTask loadProgress(long playerId, int taskId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = AlyraManager.getConnection();
            ps = con.prepareStatement(
                "SELECT task_id, count, claimed, claimed_vip FROM kol_task_progress WHERE player_id = ? AND task_id = ?"
            );
            ps.setLong(1, playerId);
            ps.setInt(2, taskId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                KolTask task = new KolTask();
                task.missionId = rs.getInt("task_id");
                task.count = rs.getInt("count");
                task.claimed = rs.getBoolean("claimed");
                task.claimedVip = rs.getBoolean("claimed_vip");
                return task;
            }
        } catch (SQLException e) {
            Logger.errorln("Error loading KOL task progress for player " + playerId + ", task " + taskId + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
        return null;
    }

    public List<KolTask> loadAllProgress(long playerId) {
        List<KolTask> tasks = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = AlyraManager.getConnection();
            ps = con.prepareStatement(
                "SELECT task_id, count, claimed, claimed_vip FROM kol_task_progress WHERE player_id = ? ORDER BY task_id ASC"
            );
            ps.setLong(1, playerId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                KolTask task = new KolTask();
                task.missionId = rs.getInt("task_id");
                task.count = rs.getInt("count");
                task.claimed = rs.getBoolean("claimed");
                task.claimedVip = rs.getBoolean("claimed_vip");
                tasks.add(task);
            }
        } catch (SQLException e) {
            Logger.errorln("Error loading all KOL task progress for player " + playerId + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
        return tasks;
    }

    public int getHighestCompletedTask(long playerId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = AlyraManager.getConnection();
            ps = con.prepareStatement(
                "SELECT MAX(task_id) as max_task FROM kol_task_progress WHERE player_id = ? AND claimed = 1"
            );
            ps.setLong(1, playerId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("max_task");
            }
        } catch (SQLException e) {
            Logger.errorln("Error getting highest completed task for player " + playerId + ": " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
        return 0;
    }

    public void saveProgress(long playerId, KolTask task) {
        if (task == null || task.missionId <= 0) {
            return;
        }
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = AlyraManager.getConnection();
            con.setAutoCommit(true);  // Ensure auto-commit is enabled
            
            ps = con.prepareStatement(
                "INSERT INTO kol_task_progress (player_id, task_id, count, claimed, claimed_vip, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW()) " +
                "ON DUPLICATE KEY UPDATE count = ?, claimed = ?, updated_at = NOW()"
            );
            ps.setLong(1, playerId);
            ps.setInt(2, task.missionId);
            ps.setInt(3, task.count);
            ps.setBoolean(4, task.claimed);
            ps.setBoolean(5, task.claimedVip);
            ps.setInt(6, task.count);
            ps.setBoolean(7, task.claimed);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.errorln("Error saving KOL task progress for player " + playerId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void deleteProgress(long playerId) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = AlyraManager.getConnection();
            ps = con.prepareStatement("DELETE FROM kol_task_progress WHERE player_id = ?");
            ps.setLong(1, playerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.errorln("Error deleting KOL task progress for player " + playerId + ": " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void increaseCount(long playerId, int taskId) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = AlyraManager.getConnection();
            con.setAutoCommit(true);  // Ensure auto-commit is enabled
            
            ps = con.prepareStatement(
                "UPDATE kol_task_progress SET count = count + 1, updated_at = NOW() " +
                "WHERE player_id = ? AND task_id = ?"
            );
            ps.setLong(1, playerId);
            ps.setInt(2, taskId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                ps.close();
                ps = con.prepareStatement(
                    "INSERT INTO kol_task_progress (player_id, task_id, count, claimed, claimed_vip, updated_at) " +
                    "VALUES (?, ?, 1, 0, 0, NOW())"
                );
                ps.setLong(1, playerId);
                ps.setInt(2, taskId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.errorln("Error increasing count for player " + playerId + ", task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void claimReward(long playerId, int taskId, boolean isVip) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = AlyraManager.getConnection();
            con.setAutoCommit(true);  // Ensure auto-commit is enabled
            
            String column = isVip ? "claimed_vip" : "claimed";
            ps = con.prepareStatement(
                "UPDATE kol_task_progress SET " + column + " = 1, updated_at = NOW() " +
                "WHERE player_id = ? AND task_id = ?"
            );
            ps.setLong(1, playerId);
            ps.setInt(2, taskId);
            int updated = ps.executeUpdate();
            
            if (updated == 0) {
                Logger.errorln("KolTaskDAO: Failed to claim reward - no record found for player " + playerId + ", task " + taskId);
            }
        } catch (SQLException e) {
            Logger.errorln("Error claiming reward for player " + playerId + ", task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
    }

    public List<KolTask> getUnclaimedVipTasks(long playerId) {
        List<KolTask> tasks = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = AlyraManager.getConnection();
            ps = con.prepareStatement(
                "SELECT task_id, count, claimed, claimed_vip FROM kol_task_progress " +
                "WHERE player_id = ? AND claimed_vip = 0 ORDER BY task_id ASC"
            );
            ps.setLong(1, playerId);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                KolTask task = new KolTask();
                task.missionId = rs.getInt("task_id");
                task.count = rs.getInt("count");
                task.claimed = rs.getBoolean("claimed");
                task.claimedVip = rs.getBoolean("claimed_vip");
                task.maxCount = task.count;
                tasks.add(task);
            }
        } catch (SQLException e) {
            Logger.errorln("Error getting unclaimed VIP tasks for player " + playerId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                Logger.errorln("Error closing resources: " + e.getMessage());
            }
        }
        return tasks;
    }
}
