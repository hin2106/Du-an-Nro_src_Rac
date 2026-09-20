//package daos;
//
///*
// * Generic Event DAO to manage event points, top queries, and claim flags
// */
//import data.AlyraManager;
//import data.AlyraResultSet;
//import item.Item;
//import java.util.ArrayList;
//import java.util.List;
//import services.ItemService;
//import services.top.TopAppearanceCache;
//import services.top.TopRowService;
//import utils.Logger;
//
//public class EventDAO {
//
//    public static void addPoints(String eventKey, String category, int playerId, int amount) {
//        try {
//            AlyraManager.executeUpdate(
//                    "INSERT INTO event_data(event_key, category, player_id, kind, points, updated_at) "
//                    + "VALUES(?, ?, ?, 0, ?, NOW()) "
//                    + "ON DUPLICATE KEY UPDATE points = points + VALUES(points), updated_at = NOW()",
//                    eventKey, category, playerId, amount);
//        } catch (Exception e) {
//            Logger.logException(EventDAO.class, e);
//        }
//    }
//
//    public static int getPoints(String eventKey, String category, int playerId) {
//        try {
//            AlyraResultSet rs = AlyraManager.executeQuery(
//                    "SELECT points FROM event_data WHERE event_key = ? AND category = ? AND player_id = ? AND kind = 0",
//                    eventKey, category, playerId);
//            if (rs.next()) {
//                return rs.getInt("points");
//            }
//        } catch (Exception e) {
//            Logger.logException(EventDAO.class, e);
//        }
//        return 0;
//    }
//
//    public static int getRank(String eventKey, String category, int point) {
//        try {
//            AlyraResultSet rs = AlyraManager.executeQuery(
//                    "SELECT COUNT(*) AS c FROM event_data WHERE event_key = ? AND category = ? AND kind = 0 AND points > ?",
//                    eventKey, category, point);
//            if (rs.next()) {
//                int c = 0;
//                try {
//                    Object val = rs.getObject("c");
//                    if (val instanceof Number n) {
//                        c = n.intValue();
//                    } else {
//                        String s = rs.getString("c");
//                        if (s != null && !s.isEmpty()) {
//                            c = Integer.parseInt(s.replaceAll("[^0-9-]", ""));
//                        }
//                    }
//                } catch (Exception ignored) {
//                }
//                return c + 1;
//            }
//        } catch (Exception e) {
//            Logger.logException(EventDAO.class, e);
//        }
//        return 0;
//    }
//
//    public static List<Item> getRewards(String eventKey, String category, int rank) {
//        List<Item> list = new ArrayList<>();
//        try {
//            AlyraResultSet rs = AlyraManager.executeQuery(
//                    "SELECT item_id, quantity FROM event_data WHERE event_key = ? AND category = ? AND kind = 2 AND ? BETWEEN min_rank AND max_rank ORDER BY min_rank ASC",
//                    eventKey, category, rank);
//            while (rs.next()) {
//                short itemId = (short) rs.getInt("item_id");
//                int quantity = Math.max(1, rs.getInt("quantity"));
//                list.add(ItemService.gI().createNewItem(itemId, quantity));
//            }
//        } catch (Exception e) {
//            Logger.logException(EventDAO.class, e);
//        }
//        return list;
//    }
//
//    public static List<TopRowService> queryTop(String eventKey, String category, int limit) {
//        List<TopRowService> list = new ArrayList<>();
//        try {
//            String sql = "SELECT p.id AS id, ed.points, p.name, p.head, p.gender "
//                    + "FROM event_data ed JOIN player p ON p.id = ed.player_id "
//                    + "WHERE ed.event_key = ? AND ed.category = ? AND ed.kind = 0 AND ed.points > 9 "
//                    + "ORDER BY ed.points DESC LIMIT ?";
//            AlyraResultSet rs = AlyraManager.executeQuery(sql, eventKey, category, limit);
//            while (rs.next()) {
//                int id = rs.getInt("id");
//                String name = rs.getString("name");
//                int head = rs.getInt("head");
//                int gender = 0;
//                try {
//                    gender = rs.getInt("gender");
//                } catch (Exception ignored) {
//                }
//                int body = (gender == 1) ? 59 : 57;
//                int leg = (gender == 1) ? 60 : 58;
//                int point = rs.getInt("points");
//                short[] parts = TopAppearanceCache.getAppearance(id, (short) head, (short) body, (short) leg);
//                list.add(new TopRowService(id, name, parts[0], parts[1], parts[2], 0L, null, null, null, null, point, 0L, null));
//            }
//        } catch (Exception e) {
//            Logger.logException(EventDAO.class, e);
//        }
//        return list;
//    }
//
//    public static boolean isClaimed(String eventKey, String category, int playerId) {
//        try {
//            AlyraResultSet rs = AlyraManager.executeQuery(
//                    "SELECT claimed FROM event_data WHERE event_key = ? AND category = ? AND player_id = ? AND kind = 1",
//                    eventKey, category, playerId);
//            if (rs.next()) {
//                return rs.getInt("claimed") == 1;
//            }
//        } catch (Exception e) {
//            Logger.logException(EventDAO.class, e);
//        }
//        return false;
//    }
//
//    public static void setClaimed(String eventKey, String category, int playerId) {
//        try {
//            AlyraManager.executeUpdate(
//                    "INSERT INTO event_data(event_key, category, player_id, kind, claimed, claimed_at) VALUES (?, ?, ?, 1, 1, NOW()) "
//                    + "ON DUPLICATE KEY UPDATE claimed = 1, claimed_at = NOW()",
//                    eventKey, category, playerId);
//        } catch (Exception e) {
//            Logger.logException(EventDAO.class, e);
//        }
//    }
//}
