package daos;

import item.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import services.ItemService;
import shop.ItemShop;
import shop.Shop;
import shop.TabShop;
import utils.Logger;

public class ShopDAO {

    public static List<Shop> getShops(Connection con) {
        List<Shop> list = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("select * from shop order by npc_id asc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Shop shop = new Shop();
                shop.id = rs.getInt("id");
                shop.npcId = rs.getByte("npc_id");
                shop.tagName = rs.getString("tag_name");
                shop.typeShop = rs.getByte("type_shop");
                loadShopTab(con, shop);
                list.add(shop);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return list;
    }

    private static void loadShopTab(Connection con, Shop shop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from tab_shop where shop_id = ? order by id");
            ps.setInt(1, shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TabShop tab = new TabShop();
                tab.shop = shop;
                tab.id = rs.getInt("id");
                tab.name = rs.getString("name").replaceAll("<>", "\n");
                loadItemShop(con, tab);
                shop.tabShops.add(tab);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    private static void loadItemShop(Connection con, TabShop tabShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from item_shop where is_sell = 1 and tab_id = ? "
                    + "order by create_time desc");
            int id = tabShop.id;
            if (id >= 41 && id <= 43) {// 10,11,12
                id -= 31;
            }
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ItemShop itemShop = new ItemShop();
                itemShop.tabShop = tabShop;
                itemShop.id = rs.getInt("id");
                itemShop.temp = ItemService.gI().getTemplate(rs.getShort("temp_id"));
                itemShop.isNew = rs.getBoolean("is_new");
                itemShop.cost = rs.getInt("cost");
                itemShop.iconSpec = rs.getInt("icon_spec");
                itemShop.typeSell = rs.getByte("type_sell");
                loadItemShopOption(con, itemShop);
                tabShop.itemShops.add(itemShop);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    private static void loadItemShopOption(Connection con, ItemShop itemShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from item_shop_option where item_shop_id = ?");
            ps.setInt(1, itemShop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemShop.options.add(new Item.ItemOption(rs.getInt("option_id"), rs.getInt("param")));
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    // ==================== INSERT METHODS ====================

    /**
     * Thêm shop mới vào database
     * 
     * @param con      Connection
     * @param npcId    NPC ID
     * @param tagName  Tag name của shop
     * @param typeShop Loại shop
     * @return ID của shop vừa tạo, -1 nếu lỗi
     */
    public static int insertShop(Connection con, byte npcId, String tagName, byte typeShop) {
        try {
            String sql = "INSERT INTO shop (npc_id, tag_name, type_shop) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setByte(1, npcId);
            ps.setString(2, tagName);
            ps.setByte(3, typeShop);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    rs.close();
                    ps.close();
                    Logger.success("Đã thêm shop mới với ID: " + newId);
                    return newId;
                }
            }
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi thêm shop");
        }
        return -1;
    }

    /**
     * Thêm tab shop mới vào database
     * 
     * @param con    Connection
     * @param shopId ID của shop
     * @param name   Tên tab (có thể dùng <> để xuống dòng)
     * @return ID của tab vừa tạo, -1 nếu lỗi
     */
    public static int insertTabShop(Connection con, int shopId, String name) {
        try {
            String sql = "INSERT INTO tab_shop (shop_id, name) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, shopId);
            ps.setString(2, name);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    rs.close();
                    ps.close();
                    Logger.success("Đã thêm tab shop mới với ID: " + newId);
                    return newId;
                }
            }
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi thêm tab shop");
        }
        return -1;
    }

    /**
     * Thêm item vào shop
     * 
     * @param con      Connection
     * @param tabId    ID của tab
     * @param tempId   ID của item template
     * @param cost     Giá bán
     * @param isNew    Có phải item mới không
     * @param iconSpec Icon spec
     * @param typeSell Loại bán (0: vàng, 1: gem, ...)
     * @return ID của item shop vừa tạo, -1 nếu lỗi
     */
    public static int insertItemShop(Connection con, int tabId, short tempId, int cost, boolean isNew, int iconSpec,
            byte typeSell) {
        try {
            String sql = "INSERT INTO item_shop (tab_id, temp_id, cost, is_new, icon_spec, type_sell, is_sell, create_time) VALUES (?, ?, ?, ?, ?, ?, 1, NOW())";
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, tabId);
            ps.setShort(2, tempId);
            ps.setInt(3, cost);
            ps.setBoolean(4, isNew);
            ps.setInt(5, iconSpec);
            ps.setByte(6, typeSell);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    rs.close();
                    ps.close();
                    Logger.success("Đã thêm item shop mới với ID: " + newId);
                    return newId;
                }
            }
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi thêm item shop");
        }
        return -1;
    }

    /**
     * Thêm option cho item shop
     * 
     * @param con        Connection
     * @param itemShopId ID của item shop
     * @param optionId   ID của option
     * @param param      Tham số của option
     * @return true nếu thành công, false nếu lỗi
     */
    public static boolean insertItemShopOption(Connection con, int itemShopId, int optionId, int param) {
        try {
            String sql = "INSERT INTO item_shop_option (item_shop_id, option_id, param) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, itemShopId);
            ps.setInt(2, optionId);
            ps.setInt(3, param);

            int affectedRows = ps.executeUpdate();
            ps.close();

            if (affectedRows > 0) {
                Logger.success("Đã thêm option cho item shop ID: " + itemShopId);
                return true;
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi thêm item shop option");
        }
        return false;
    }

    /**
     * Lấy danh sách tất cả shops
     * 
     * @param con Connection
     * @return Danh sách shop IDs và names
     */
    public static List<String[]> getAllShops(Connection con) {
        List<String[]> shops = new ArrayList<>();
        try {
            PreparedStatement ps = con
                    .prepareStatement("SELECT id, npc_id, tag_name, type_shop FROM shop ORDER BY npc_id ASC");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                shops.add(new String[] {
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getByte("npc_id")),
                        rs.getString("tag_name"),
                        String.valueOf(rs.getByte("type_shop"))
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return shops;
    }

    /**
     * Lấy danh sách tabs của một shop
     * 
     * @param con    Connection
     * @param shopId ID của shop
     * @return Danh sách tab IDs và names
     */
    public static List<String[]> getTabsByShopId(Connection con, int shopId) {
        List<String[]> tabs = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id, name FROM tab_shop WHERE shop_id = ? ORDER BY id");
            ps.setInt(1, shopId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tabs.add(new String[] {
                        String.valueOf(rs.getInt("id")),
                        rs.getString("name")
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return tabs;
    }

    public static List<String[]> getAllItemOptions(Connection con) {
        List<String[]> options = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id, name FROM item_option_template ORDER BY id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                options.add(new String[] {
                        String.valueOf(rs.getInt("id")),
                        rs.getString("name")
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return options;
    }

    /**
     * Lấy danh sách options của một item shop
     * 
     * @param con        Connection
     * @param itemShopId ID của item shop
     * @return Danh sách option IDs, names và params
     */
    public static List<String[]> getItemShopOptions(Connection con, int itemShopId) {
        List<String[]> options = new ArrayList<>();
        try {
            String sql = "SELECT iso.option_id, iso.param, iot.name " +
                    "FROM item_shop_option iso " +
                    "LEFT JOIN item_option_template iot ON iot.id = iso.option_id " +
                    "WHERE iso.item_shop_id = ? ORDER BY iso.option_id";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, itemShopId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                options.add(new String[] {
                        String.valueOf(rs.getInt("option_id")),
                        rs.getString("name") != null ? rs.getString("name") : "Unknown",
                        String.valueOf(rs.getInt("param"))
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return options;
    }

    /**
     * Cập nhật param của một option
     * 
     * @param con        Connection
     * @param itemShopId ID của item shop
     * @param optionId   ID của option
     * @param param      Param mới
     * @return true nếu thành công
     */
    public static boolean updateItemShopOption(Connection con, int itemShopId, int optionId, int param) {
        try {
            String sql = "UPDATE item_shop_option SET param = ? WHERE item_shop_id = ? AND option_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, param);
            ps.setInt(2, itemShopId);
            ps.setInt(3, optionId);

            int affectedRows = ps.executeUpdate();
            ps.close();

            if (affectedRows > 0) {
                Logger.success("Đã cập nhật option cho item shop ID: " + itemShopId);
                return true;
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi cập nhật item shop option");
        }
        return false;
    }

    /**
     * Xóa một option khỏi item shop
     * 
     * @param con        Connection
     * @param itemShopId ID của item shop
     * @param optionId   ID của option
     * @return true nếu thành công
     */
    public static boolean deleteItemShopOption(Connection con, int itemShopId, int optionId) {
        try {
            String sql = "DELETE FROM item_shop_option WHERE item_shop_id = ? AND option_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, itemShopId);
            ps.setInt(2, optionId);

            int affectedRows = ps.executeUpdate();
            ps.close();

            if (affectedRows > 0) {
                Logger.success("Đã xóa option khỏi item shop ID: " + itemShopId);
                return true;
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi xóa item shop option");
        }
        return false;
    }

    /**
     * Lấy danh sách item shops theo tab_id
     * 
     * @param con   Connection
     * @param tabId ID của tab shop
     * @return Danh sách item shop IDs, temp_ids, names, costs, etc.
     */
    public static List<String[]> getItemShopsByTabId(Connection con, int tabId) {
        List<String[]> itemShops = new ArrayList<>();
        try {
            // Xử lý logic đặc biệt cho tab_id 41-43
            int queryTabId = tabId;
            if (tabId >= 41 && tabId <= 43) {
                queryTabId = tabId - 31;
            }

            String sql = "SELECT its.id, its.temp_id, its.cost, its.icon_spec, its.type_sell, its.is_new, " +
                    "it.name as item_name " +
                    "FROM item_shop its " +
                    "LEFT JOIN item_template it ON it.id = its.temp_id " +
                    "WHERE its.is_sell = 1 AND its.tab_id = ? " +
                    "ORDER BY its.create_time DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, queryTabId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemShops.add(new String[] {
                        String.valueOf(rs.getInt("id")),
                        String.valueOf(rs.getShort("temp_id")),
                        rs.getString("item_name") != null ? rs.getString("item_name") : "Unknown",
                        String.valueOf(rs.getInt("cost")),
                        String.valueOf(rs.getInt("icon_spec")),
                        String.valueOf(rs.getByte("type_sell")),
                        rs.getBoolean("is_new") ? "Có" : "Không"
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return itemShops;
    }

    /**
     * Cập nhật thông tin item shop
     * 
     * @param con        Connection
     * @param itemShopId ID của item shop
     * @param cost       Giá bán mới
     * @param iconSpec   Icon spec mới
     * @param typeSell   Loại bán mới (0: vàng, 1: gem)
     * @param isNew      Có phải item mới không
     * @return true nếu thành công
     */
    public static boolean updateItemShop(Connection con, int itemShopId, int cost, int iconSpec, byte typeSell,
            boolean isNew) {
        try {
            String sql = "UPDATE item_shop SET cost = ?, icon_spec = ?, type_sell = ?, is_new = ? WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, cost);
            ps.setInt(2, iconSpec);
            ps.setByte(3, typeSell);
            ps.setBoolean(4, isNew);
            ps.setInt(5, itemShopId);

            int affectedRows = ps.executeUpdate();
            ps.close();

            if (affectedRows > 0) {
                Logger.success("Đã cập nhật item shop ID: " + itemShopId);
                return true;
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi cập nhật item shop");
        }
        return false;
    }

    /**
     * Xóa item shop và tất cả options liên quan
     * 
     * @param con        Connection
     * @param itemShopId ID của item shop cần xóa
     * @return true nếu thành công
     */
    public static boolean deleteItemShop(Connection con, int itemShopId) {
        try {
            // Xóa tất cả options của item shop trước
            String deleteOptionsSql = "DELETE FROM item_shop_option WHERE item_shop_id = ?";
            PreparedStatement psOptions = con.prepareStatement(deleteOptionsSql);
            psOptions.setInt(1, itemShopId);
            int deletedOptions = psOptions.executeUpdate();
            psOptions.close();

            // Xóa item shop
            String deleteItemShopSql = "DELETE FROM item_shop WHERE id = ?";
            PreparedStatement psItemShop = con.prepareStatement(deleteItemShopSql);
            psItemShop.setInt(1, itemShopId);
            int affectedRows = psItemShop.executeUpdate();
            psItemShop.close();

            if (affectedRows > 0) {
                Logger.success("Đã xóa item shop ID: " + itemShopId + " và " + deletedOptions + " options liên quan");
                return true;
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e, "Lỗi khi xóa item shop");
        }
        return false;
    }
}
