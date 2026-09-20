package daos;
import radar.OptionCard;
import radar.Card;
import data.AlyraManager;
import data.AlyraResultSet;
import consts.ConstPlayer;
import data.DataGame;
import clan.Clan;
import clan.ClanMember;
import item.Item;
import item.ItemTime;
import npc.MabuEgg;
import npc.MagicTree;
import player.Enemy;
import player.Friend;
import player.Fusion;
import player.Pet;
import player.Player;
import skill.Skill;
import task.TaskMain;
import server.Client;
import server.Manager;
import network.MySession;
import system.AntiLogin;
import services.player.ClanService;
import services.player.IntrinsicService;
import services.ItemService;
import services.map.MapService;
import services.Service;
import services.TaskService;
import utils.Logger;
import utils.SkillUtil;
import utils.TimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import system.Template.AchievementQuest;

import utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.badges.BadgesData;
import services.player.DailyGiftService;
import task.BadgesTask;
import task.BadgesTaskService;
import task.KolTaskService;

@SuppressWarnings("unchecked")
public class NDVSqlFetcher {

    private static void loadAuditTrace(Item item, JSONArray dataItem) {
        if (item == null || dataItem == null || dataItem.size() <= 4) return;
        String value = String.valueOf(dataItem.get(4));
        if (!value.isBlank() && !"null".equalsIgnoreCase(value)) item.auditTraceId = value;
    }

    public static Player login(MySession session, AntiLogin al) {
        Player player = null;
        AlyraResultSet rs = null;
        Player plInGame;
        try {
            if (session.userId > 0) {
                rs = AlyraManager.executeQuery("select * from account where id = ?", session.userId);
                if (!rs.first()) {
                    if (rs != null)
                        rs.dispose();
                    return null;
                }
                session.uu = rs.getString("username");
                session.pp = rs.getString("password");
                session.isAdmin = rs.getBoolean("is_admin");
                session.lastTimeLogout = rs.getTimestamp("last_time_logout") != null
                        ? rs.getTimestamp("last_time_logout").getTime()
                        : 0;
                session.actived = rs.getBoolean("active");
                session.goldBar = rs.getInt("account.thoi_vang");
                session.gold = rs.getLong("account.vang");
                session.bdPlayer = rs.getDouble("account.bd_player");
                session.cash = rs.getInt("cash");
                session.vnd = rs.getInt("vnd");
                session.danap = rs.getInt("danap");
                session.vip = rs.getInt("vip");

                long lastTimeLogin = rs.getTimestamp("last_time_login").getTime();
                long lastTimeLogout = rs.getTimestamp("last_time_logout") != null
                        ? rs.getTimestamp("last_time_logout").getTime()
                        : 0;
                long createTime = rs.getTimestamp("create_time").getTime();
                int deltaTime = (int) ((System.currentTimeMillis() - createTime) / 1000);

                if (rs.getBoolean("ban")) {
                    Service.gI().sendThongBaoOK(session,
                            "Tài khoản này đang bị khóa. Liên hệ Admin để biết thêm thông tin");
                    rs.dispose();
                    return null;
                }

                rs.dispose();
                rs = AlyraManager.executeQuery("select * from player where account_id = ? limit 1", session.userId);
                if (!rs.first()) {
                    rs.dispose();
                    DataGame.sendVersionGame(session);
                    DataGame.sendDataItemBG(session);
                    Service.gI().switchToCreateChar(session);
                    return null;
                }

                plInGame = Client.gI().getPlayerByUser(session.userId);
                if (plInGame != null) {
                    utils.Logger.warning("Double check: Kicking old session for userId " + session.userId
                            + " (player: " + plInGame.name + ")");
                    Client.gI().kickSession(plInGame.getSession());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                player = loadPlayer(rs, false);
                if (player == null) {
                    Service.gI().sendThongBaoOK(session, "Không thể tải dữ liệu nhân vật, vui lòng đăng nhập lại sau.");
                    rs.dispose();
                    return null;
                }

                player.isPlayer = true;
                player.deltaTime = deltaTime;
                player.createTime = createTime;
                player.daMuaGiaHanSKH = rs.getBoolean("da_mua_gia_han_skh");
                Timestamp tsTimeUpSKH = rs.getTimestamp("time_up_skh");
                if (tsTimeUpSKH != null) {
                    player.timeUpSKH = tsTimeUpSKH.getTime();
                } else {
                    player.timeUpSKH = player.createTime + (30L * 24 * 60 * 60 * 1000);
                    AlyraManager.executeUpdate("UPDATE player SET time_up_skh = '"
                            + new java.sql.Timestamp(player.timeUpSKH)
                            + "' WHERE id = " + player.id);
                }

                long now = System.currentTimeMillis();
                player.isNewMember = (player.timeUpSKH > now);

                AlyraManager.executeUpdate(
                        "UPDATE account SET last_time_login = '" + new Timestamp(System.currentTimeMillis())
                                + "', ip_address = '" + session.ipAddress
                                + "' WHERE id = " + session.userId);

                if (al != null) {
                    al.reset();
                }

                rs.dispose();
                return player;
            }

            if (session.uu == null || session.pp == null) {
                return null;
            }

            String username = session.uu.trim();
            String password = session.pp.trim();
            if (username.isEmpty() || password.isEmpty()) {
                return null;
            }

            rs = AlyraManager.executeQuery("select * from account where username = ? and password = ?", username,
                    password);
            if (rs.first()) {
                session.userId = rs.getInt("account.id");
                session.isAdmin = rs.getBoolean("is_admin");
                session.lastTimeLogout = rs.getTimestamp("last_time_logout") != null
                        ? rs.getTimestamp("last_time_logout").getTime()
                        : 0;
                session.actived = rs.getBoolean("active");
                session.goldBar = rs.getInt("account.thoi_vang");
                session.gold = rs.getLong("account.vang");
                session.bdPlayer = rs.getDouble("account.bd_player");
                session.cash = rs.getInt("cash");
                session.vnd = rs.getInt("vnd");
                session.danap = rs.getInt("danap");
                session.vip = rs.getInt("vip");

                long lastTimeLogin = rs.getTimestamp("last_time_login").getTime();
                int secondsPass1 = (int) ((System.currentTimeMillis() - lastTimeLogin) / 1000);
                long lastTimeLogout = rs.getTimestamp("last_time_logout").getTime();
                int secondsPass = (int) ((System.currentTimeMillis() - lastTimeLogout) / 1000);
                long createTime = rs.getTimestamp("create_time").getTime();
                int deltaTime = (int) ((System.currentTimeMillis() - createTime) / 1000);

                if (rs.getBoolean("ban")) {
                    Service.gI().sendThongBaoOK(session,
                            "Tài khoản này đang bị khóa. Liên hệ Admin để biết thêm thông tin");
                    return null;
                }

                if (secondsPass1 < Manager.SECOND_WAIT_LOGIN) {
                    if (secondsPass < secondsPass1) {
                        Service.gI().sendWaitToLogin(session, Manager.SECOND_WAIT_LOGIN - secondsPass);
                        return null;
                    }
                    Service.gI().sendWaitToLogin(session, Manager.SECOND_WAIT_LOGIN - secondsPass1);
                    return null;
                }
                plInGame = Client.gI().getPlayerByUser(session.userId);
                if (plInGame != null) {
                    if (lastTimeLogin > session.lastTimeLogout) {
                        Client.gI().kickSession(plInGame.getSession());
                    } else {
                        Service.gI().sendThongBaoOK(session, "Tài khoản đang được sử dụng ở nơi khác");
                        return null;
                    }
                }

                if (secondsPass < Manager.SECOND_WAIT_LOGIN) {
                    Service.gI().sendWaitToLogin(session, Manager.SECOND_WAIT_LOGIN - secondsPass);
                    return null;
                }

                rs = AlyraManager.executeQuery("select * from player where account_id = ? limit 1", session.userId);
                if (!rs.first()) {
                    // -28 -4 version data game
                    DataGame.sendVersionGame(session);
                    // -31 data item background
                    DataGame.sendDataItemBG(session);
                    Service.gI().switchToCreateChar(session);
                    return null;
                }

                plInGame = Client.gI().getPlayerByUser(session.userId);
                if (plInGame != null) {
                    utils.Logger.warning("Double check: Kicking old session for userId " + session.userId
                            + " (player: " + plInGame.name + ")");
                    Client.gI().kickSession(plInGame.getSession());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                player = loadPlayer(rs, false);
                if (player == null) {
                    Service.gI().sendThongBaoOK(session, "Không thể tải dữ liệu nhân vật, vui lòng đăng nhập lại sau.");
                    return null;
                }

                player.isPlayer = true;
                player.deltaTime = deltaTime;
                player.createTime = createTime;
                player.daMuaGiaHanSKH = rs.getBoolean("da_mua_gia_han_skh");
                Timestamp tsTimeUpSKH = rs.getTimestamp("time_up_skh");
                if (tsTimeUpSKH != null) {
                    player.timeUpSKH = tsTimeUpSKH.getTime();
                } else {
                    player.timeUpSKH = player.createTime + (30L * 24 * 60 * 60 * 1000);
                    AlyraManager.executeUpdate("UPDATE player SET time_up_skh = '"
                            + new java.sql.Timestamp(player.timeUpSKH)
                            + "' WHERE id = " + player.id);
                }

                long now = System.currentTimeMillis();
                player.isNewMember = (player.timeUpSKH > now);

                AlyraManager.executeUpdate(
                        "UPDATE account SET last_time_login = '" + new Timestamp(System.currentTimeMillis())
                                + "', ip_address = '" + session.ipAddress
                                + "' WHERE id = " + session.userId);

                if (al != null) {
                    al.reset();
                }

            } else {
                Service.gI().sendThongBaoOK(session, "Thông tin tài khoản hoặc mật khẩu đéo đúng");
                Service.gI().sendLoginFail(session, false);
                if (al != null) {
                    al.wrong();
                }
            }

        } catch (Exception e) {
            Logger.error(session.uu);
            if (player != null) {
                player.dispose();
            }
            player = null;
            Logger.logException(NDVSqlFetcher.class, e);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return player;
    }

    public static int createTrialAccount(MySession session) {
        try {
            AlyraResultSet recent = AlyraManager.executeQuery(
                    "SELECT COUNT(*) AS count FROM account WHERE username LIKE 'trial_%' "
                            + "AND ip_address = ? AND create_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR)",
                    session.ipAddress);
            if (recent.first() && recent.getInt("count") >= 5) {
                recent.dispose();
                Logger.warning("IP " + session.ipAddress + " đã đạt giới hạn tạo trial theo giờ\n");
                return -1;
            }
            recent.dispose();

            AlyraResultSet rs = AlyraManager.executeQuery(
                    "SELECT COUNT(*) as count FROM account a " +
                            "LEFT JOIN player p ON a.id = p.account_id " +
                            "WHERE a.username LIKE 'trial_%' AND a.ip_address = ? AND p.id IS NULL",
                    session.ipAddress);

            int existingCount = 0;
            if (rs.first()) {
                existingCount = rs.getInt("count");
            }
            rs.dispose();
            if (existingCount >= 10) {
                Logger.warning("IP " + session.ipAddress + " đã đạt giới hạn create account\n");
                return -1;
            }
            String randomId = java.util.UUID.randomUUID().toString().replace("-", "");
            String trialUsername = "trial_" + randomId.substring(0, 13);
            String trialPassword = randomId;
            String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            int inserted = AlyraManager.executeUpdate(
                    "INSERT INTO account (username, password, create_time, update_time, ban, is_admin, active, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    trialUsername, trialPassword, timeNow, timeNow, 0, 0, 0, session.ipAddress);
            if (inserted > 0) {
                rs = AlyraManager.executeQuery(
                        "SELECT id FROM account WHERE username = ? LIMIT 1", trialUsername);
                if (rs.first()) {
                    int accountId = rs.getInt("id");
                    rs.dispose();
                    Logger.success("Tạo trial account thành công: " + trialUsername + " (ID: " + accountId + ")");
                    return accountId;
                }
                rs.dispose();
            }
            return -1;
        } catch (Exception e) {
            Logger.logException(NDVSqlFetcher.class, e, "Lỗi khi tạo trial account");
            return -1;
        }
    }

    public static boolean convertTrialToAccount(MySession session, String username, String password) {
        try {
            if (session.userId <= 0 || session.uu == null || !session.uu.startsWith("trial_")) {
                return false;
            }
            AlyraResultSet rs = AlyraManager.executeQuery("SELECT 1 FROM account WHERE username = ?", username);
            if (rs.first()) {
                rs.dispose();
                return false;
            }
            rs.dispose();
            String timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            int updated = AlyraManager.executeUpdate(
                    "UPDATE account SET username = ?, password = ?, update_time = ?, active = 1, activated = 1 "
                            + "WHERE id = ? AND username LIKE 'trial_%'",
                    username, password, timeNow, session.userId);
            if (updated > 0) {
                session.uu = username;
                session.pp = password;
                // Logger.successln("Chuyển trial account sang account thật thành công: " +
                // username + " (ID: " + session.userId + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            Logger.logException(NDVSqlFetcher.class, e, "Lỗi khi chuyển trial account");
            return false;
        }
    }

    public static List<Item> getMailBox(Player player) {
        try {
            List<Item> mailBoxs = new ArrayList<>();
            JSONArray dataArray = null;
            player.inventory.itemsMailBox.clear();
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection con = AlyraManager.getConnection();
            ps = con.prepareStatement("select `item_mails_box` from player where id = ?");
            ps.setLong(1, player.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                // data box hòm thư
                dataArray = (JSONArray) JSONValue.parse(rs.getString("item_mails_box"));
                for (int i = 0; i < dataArray.size(); i++) {
                    Item item = null;
                    JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        JSONArray options = (JSONArray) JSONValue
                                .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                        for (int j = 0; j < options.size(); j++) {
                            JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                            item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                        }
                        player.inventory.itemsMailBox.add(item);
                    }
                    mailBoxs.add(item);
                }
                dataArray.clear();
            }
            rs.close();
            ps.close();
            con.close();
            return mailBoxs;
        } catch (NumberFormatException | SQLException e) {
            return null;
        }
    }

    public static boolean updateMailBox(Player player) {
        try {
            JSONArray dataArray = new JSONArray();
            JSONArray dataItem = new JSONArray();
            for (Item item : player.inventory.itemsMailBox) {
                JSONArray opt = new JSONArray();
                if (item.isNotNullItem()) {
                    dataItem.add(item.template.id);
                    dataItem.add(item.quantity);
                    JSONArray options = new JSONArray();
                    for (Item.ItemOption io : item.itemOptions) {
                        opt.add(io.optionTemplate.id);
                        opt.add(io.param);
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    dataItem.add(options.toJSONString());
                } else {
                    dataItem.add(-1);
                    dataItem.add(0);
                    dataItem.add(opt.toJSONString());
                }
                dataItem.add(item.createTime);
                dataItem.add(item.auditTraceId == null ? "" : item.auditTraceId);
                dataArray.add(dataItem.toJSONString());
                dataItem.clear();
            }
            String itemsBox = dataArray.toJSONString();
            dataArray.clear();
            PreparedStatement ps = null;
            Connection con = AlyraManager.getConnection();
            ps = con.prepareStatement("update `player` set item_mails_box = ? where id = ?");
            ps.setString(1, itemsBox);
            ps.setLong(2, player.id);
            ps.executeUpdate();
            ps.close();
            con.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Player loadById(long id) {
        Player player = null;
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("select * from player where id = ? limit 1", id);
            if (rs.first() && (player = loadPlayer(rs, true)) != null) {
                player.isOffline = true;
                player.idMark.setLoadedAllDataPlayer(true);

                // Lấy dữ liệu JSON từ DB
                String jsonItems = rs.getString("item_mails_box");
                if (jsonItems != null && !jsonItems.isEmpty()) {
                    JSONArray dataArray = (JSONArray) JSONValue.parse(jsonItems);
                    if (dataArray != null) {
                        for (int i = 0; i < dataArray.size(); i++) {
                            Item item = null;
                            JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                            short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                            if (tempId != -1) {
                                item = ItemService.gI().createNewItem(tempId,
                                        Integer.parseInt(String.valueOf(dataItem.get(1))));
                                JSONArray options = (JSONArray) JSONValue
                                        .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                                if (options != null) {
                                    for (int j = 0; j < options.size(); j++) {
                                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                                        if (opt != null && opt.size() >= 2) {
                                            item.itemOptions.add(new Item.ItemOption(
                                                    Integer.parseInt(String.valueOf(opt.get(0))),
                                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                                        }
                                    }
                                }
                                item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                                loadAuditTrace(item, dataItem);
                                if (ItemService.gI().isOutOfDateTime(item)) {
                                    item = ItemService.gI().createItemNull();
                                }
                            } else {
                                item = ItemService.gI().createItemNull();
                            }
                            player.inventory.itemsMailBox.add(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (player != null) {
                player.dispose();
                player = null;
            }
            Logger.logException(NDVSqlFetcher.class, e);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return player;
    }

    private static Player loadPlayer(AlyraResultSet rs, boolean isOffline) throws Exception {
        Player player = null;
        try {
            long plHp;
            long plMp;
            JSONArray dataArray = null;

            player = new Player();

            // base info
            player.id = rs.getInt("id");
            player.name = rs.getString("name");
            player.head = rs.getShort("head");
            player.gender = rs.getByte("gender");
            if (player.head == -1 || player.head == 0) {
                switch (player.gender) {
                    case 0 ->
                        player.head = 64;
                    case 1 ->
                        player.head = 9;
                    case 2 ->
                        player.head = 6;
                }
            }
            player.haveTennisSpaceShip = rs.getBoolean("have_tennis_space_ship");
            try {
                player.ghiDanhPoint = rs.getInt("ghi_danh_point");
            } catch (Exception ignored) {
                player.ghiDanhPoint = 0;
            }
            try {
                Timestamp pt = rs.getTimestamp("ghi_danh_point_time");
                player.ghiDanhPointTime = pt != null ? pt.getTime() : 0;
            } catch (Exception ignored) {
                player.ghiDanhPointTime = 0;
            }

            // data box hòm thư
            String jsonItems = rs.getString("item_mails_box");
            if (jsonItems != null && !jsonItems.isEmpty()) {
                dataArray = (JSONArray) JSONValue.parse(jsonItems);
            }

            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    Item item = null;
                    JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        JSONArray options = (JSONArray) JSONValue
                                .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                        for (int j = 0; j < options.size(); j++) {
                            JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                            item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                        }
                        item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                        loadAuditTrace(item, dataItem);
                        if (ItemService.gI().isOutOfDateTime(item)) {
                            item = ItemService.gI().createItemNull();
                        }
                    } else {
                        item = ItemService.gI().createItemNull();
                    }
                    player.inventory.itemsMailBox.add(item);
                }
                dataArray.clear();
            }

            int clanId = rs.getInt("clan_id");
            if (clanId != -1) {
                try {
                    Clan clan = ClanService.gI().getClanById(clanId);
                    for (ClanMember cm : clan.getMembers()) {
                        if (cm.id == player.id) {
                            if (!isOffline) {
                                clan.addMemberOnline(player);
                            }
                            player.clan = clan;
                            player.clanMember = cm;
                            break;
                        }
                    }
                } catch (Exception e) {
                    player.clan = null;
                }
            }

            // data kim lượng
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_inventory"));
            player.inventory.gold = Long.parseLong(String.valueOf(dataArray.get(0)));
            player.inventory.gem = Integer.parseInt(String.valueOf(dataArray.get(1)));
            player.inventory.ruby = Integer.parseInt(String.valueOf(dataArray.get(2)));
            player.inventory.coupon = Integer.parseInt(String.valueOf(dataArray.get(3)));
            if (dataArray.size() >= 4) {
                player.inventory.coupon = Integer.parseInt(String.valueOf(dataArray.get(3)));
            } else {
                player.inventory.coupon = 0;
            }
            if (dataArray.size() >= 5 && false) {
                player.inventory.event = Integer.parseInt(String.valueOf(dataArray.get(4)));
            } else {
                player.inventory.event = 0;
            }
            dataArray.clear();

            // data tọa độ
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_location"));
                int mapId = Integer.parseInt(String.valueOf(dataArray.get(0)));
                player.location.x = Integer.parseInt(String.valueOf(dataArray.get(1)));
                player.location.y = Integer.parseInt(String.valueOf(dataArray.get(2)));
                player.location.lastTimeplayerMove = System.currentTimeMillis();
                if (mapId == 51 || MapService.gI().isMapDoanhTrai(mapId) || MapService.gI().isMapBlackBallWar(mapId)
                        || MapService.gI().isMapSieuThanhThuy(mapId) || MapService.gI().isMapMabu2H(mapId)) {
                    mapId = player.gender + 21;
                    player.location.x = 300;
                    player.location.y = 336;
                }
                if (MapService.gI().isMapMaBu(mapId)) {
                    if (!TimeUtil.isMabuOpen()) {
                        mapId = player.gender + 21;
                        player.location.x = 300;
                        player.location.y = 336;
                    }
                }
                if (mapId == 112) {
                    player.location.y = 408;
                } else if (mapId == 129 || mapId == 113) {
                    player.location.y = 360;
                }
                if (mapId == 49) {
                    mapId = 45;
                    player.location.x = 359;
                    player.location.y = 408;
                }

                player.zone = MapService.gI().getMapCanJoin(player, mapId, -1);
            } catch (Exception e) {
                Logger.error(e + "\n");
            }
            dataArray.clear();

            // data chỉ số
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_point"));
            player.nPoint.limitPower = Byte.parseByte(String.valueOf(dataArray.get(0)));
            player.nPoint.power = Long.parseLong(String.valueOf(dataArray.get(1)));
            player.nPoint.tiemNang = Long.parseLong(String.valueOf(dataArray.get(2)));
            player.nPoint.stamina = Short.parseShort(String.valueOf(dataArray.get(3)));
            player.nPoint.maxStamina = Short.parseShort(String.valueOf(dataArray.get(4)));
            player.nPoint.hpg = Long.parseLong(String.valueOf(dataArray.get(5)));
            player.nPoint.mpg = Long.parseLong(String.valueOf(dataArray.get(6)));
            player.nPoint.dameg = Long.parseLong(String.valueOf(dataArray.get(7)));
            player.nPoint.defg = Integer.parseInt(String.valueOf(dataArray.get(8)));
            player.nPoint.critg = Byte.parseByte(String.valueOf(dataArray.get(9)));
            player.nPoint.critdragon = Byte.parseByte(String.valueOf(dataArray.get(10)));
            dataArray.get(11);
            plHp = Long.parseLong(String.valueOf(dataArray.get(12)));
            plMp = Long.parseLong(String.valueOf(dataArray.get(13)));
            dataArray.clear();

            // data đậu thần
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_magic_tree"));
            byte level = Byte.parseByte(String.valueOf(dataArray.get(0)));
            byte currPea = Byte.parseByte(String.valueOf(dataArray.get(1)));
            boolean isUpgrade = Byte.parseByte(String.valueOf(dataArray.get(2))) == 1;
            long lastTimeHarvest = Long.parseLong(String.valueOf(dataArray.get(3)));
            long lastTimeUpgrade = Long.parseLong(String.valueOf(dataArray.get(4)));
            player.magicTree = new MagicTree(player, level, currPea, lastTimeHarvest, isUpgrade, lastTimeUpgrade);
            dataArray.clear();

            // data phần thưởng sao đen
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_black_ball"));
            JSONArray dataBlackBall;
            for (int i = 0; i < dataArray.size(); i++) {
                dataBlackBall = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                player.rewardBlackBall.timeOutOfDateReward[i] = Long.parseLong(String.valueOf(dataBlackBall.get(0)));
                player.rewardBlackBall.lastTimeGetReward[i] = Long.parseLong(String.valueOf(dataBlackBall.get(1)));
                try {
                    player.rewardBlackBall.quantilyBlackBall[i] = dataBlackBall.get(2) != null
                            ? Integer.parseInt(String.valueOf(dataBlackBall.get(2)))
                            : 0;
                } catch (NumberFormatException e) {
                    player.rewardBlackBall.quantilyBlackBall[i] = player.rewardBlackBall.timeOutOfDateReward[i] != 0 ? 1
                            : 0;
                }
                dataBlackBall.clear();
            }
            dataArray.clear();

            // data body
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_body"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue
                            .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    loadAuditTrace(item, dataItem);
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBody.add(item);
            }
            dataArray.clear();

            // data bag
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_bag"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue
                            .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    loadAuditTrace(item, dataItem);
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBag.add(item);
            }
            dataArray.clear();

            // data box
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_box"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue
                            .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    loadAuditTrace(item, dataItem);
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBox.add(item);
            }
            dataArray.clear();

            // data box
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_box1"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue
                            .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    loadAuditTrace(item, dataItem);
                    if (item.template.id == 2132) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                        try {
                            Date currentDate = new Date(item.createTime);
                            Date startDate = formatter.parse("15/03/2024");
                            Date endDate = formatter.parse("28/03/2024");
                            if (currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
                                System.out.println("Thu hồi cải trang rồng lộn bug.");
                                item = ItemService.gI().createItemNull();
                            }
                        } catch (ParseException e) {
                        }
                    }
                    if (ItemService.gI().isOutOfDateTime(item)) {
                        item = ItemService.gI().createItemNull();
                    }
                } else {
                    item = ItemService.gI().createItemNull();
                }
                player.inventory.itemsBox1.add(item);
            }
            dataArray.clear();

            // data box lucky round
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_box_lucky_round"));
            for (int i = 0; i < dataArray.size(); i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue
                            .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    player.inventory.itemsBoxCrackBall.add(item);
                }
            }
            dataArray.clear();

            // data item da ban
            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_daban"));
            for (int i = 0; i < dataArray.size() && i < 20; i++) {
                Item item;
                JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                if (tempId != -1) {
                    item = ItemService.gI().createNewItem(tempId, Integer.parseInt(String.valueOf(dataItem.get(1))));
                    JSONArray options = (JSONArray) JSONValue
                            .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                    for (int j = 0; j < options.size(); j++) {
                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                        item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                Integer.parseInt(String.valueOf(opt.get(1)))));
                    }
                    // 26/06/2023 - Giảm Ngày Trong Shop
                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                    loadAuditTrace(item, dataItem);
                    if (item.template.id == 2132) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                        try {
                            Date currentDate = new Date(item.createTime);
                            Date startDate = formatter.parse("15/03/2024");
                            Date endDate = formatter.parse("28/03/2024");
                            if (currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
                                System.out.println("Thu hồi cải trang rồng lộn bug.");
                                item = ItemService.gI().createItemNull();
                            }
                        } catch (ParseException e) {
                        }
                    }
                    if (!ItemService.gI().isOutOfDateTime(item)) {
                        player.inventory.itemsDaBan.add(item);
                    }
                    // player.inventory.itemsDaBan.add(item);
                }
            }
            dataArray.clear();

            dataArray = (JSONArray) JSONValue.parse(rs.getString("items_daban"));
            String itemMailsBoxStr = rs.getString("item_mails_box");
            if (itemMailsBoxStr != null && !itemMailsBoxStr.isEmpty()) {
                dataArray = (JSONArray) JSONValue.parse(itemMailsBoxStr);
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        Item item = null;
                        JSONArray dataItem = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                        short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                        if (tempId != -1) {
                            item = ItemService.gI().createNewItem(tempId,
                                    Integer.parseInt(String.valueOf(dataItem.get(1))));
                            JSONArray options = (JSONArray) JSONValue
                                    .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                            if (options != null) {
                                for (int j = 0; j < options.size(); j++) {
                                    JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                                    item.itemOptions.add(new Item.ItemOption(
                                            Integer.parseInt(String.valueOf(opt.get(0))),
                                            Integer.parseInt(String.valueOf(opt.get(1)))));
                                }
                            }
                            player.inventory.itemsMailBox.add(item);
                        } else {
                            item = ItemService.gI().createItemNull();
                            player.inventory.itemsMailBox.add(item);
                        }
                    }
                    dataArray.clear();
                }
            }

            // data friends
            dataArray = (JSONArray) JSONValue.parse(rs.getString("friends"));
            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray dataFE = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    Friend friend = new Friend();
                    friend.id = Integer.parseInt(String.valueOf(dataFE.get(0)));
                    friend.name = String.valueOf(dataFE.get(1));
                    friend.head = Short.parseShort(String.valueOf(dataFE.get(2)));
                    friend.body = Short.parseShort(String.valueOf(dataFE.get(3)));
                    friend.leg = Short.parseShort(String.valueOf(dataFE.get(4)));
                    friend.bag = Byte.parseByte(String.valueOf(dataFE.get(5)));
                    friend.power = Long.parseLong(String.valueOf(dataFE.get(6)));
                    player.friends.add(friend);
                    dataFE.clear();
                }
                dataArray.clear();
            }

            // data enemies
            dataArray = (JSONArray) JSONValue.parse(rs.getString("enemies"));
            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray dataFE = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    Enemy enemy = new Enemy();
                    enemy.id = Integer.parseInt(String.valueOf(dataFE.get(0)));
                    enemy.name = String.valueOf(dataFE.get(1));
                    enemy.head = Short.parseShort(String.valueOf(dataFE.get(2)));
                    enemy.body = Short.parseShort(String.valueOf(dataFE.get(3)));
                    enemy.leg = Short.parseShort(String.valueOf(dataFE.get(4)));
                    enemy.bag = Byte.parseByte(String.valueOf(dataFE.get(5)));
                    enemy.power = Long.parseLong(String.valueOf(dataFE.get(6)));
                    player.enemies.add(enemy);
                    dataFE.clear();
                }
                dataArray.clear();
            }

            // data nội tại
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_intrinsic"));
            byte intrinsicId = Byte.parseByte(String.valueOf(dataArray.get(0)));
            player.playerIntrinsic.intrinsic = IntrinsicService.gI().getIntrinsicById(intrinsicId);
            player.playerIntrinsic.intrinsic.param1 = Short.parseShort(String.valueOf(dataArray.get(1)));
            player.playerIntrinsic.intrinsic.param2 = Short.parseShort(String.valueOf(dataArray.get(2)));
            player.playerIntrinsic.countOpen = Byte.parseByte(String.valueOf(dataArray.get(3)));
            if (dataArray.size() > 4) {
                try {
                    player.effectSkill.isIntrinsic = Boolean.parseBoolean(String.valueOf(dataArray.get(4)));
                    player.effectSkill.skillID = Integer.parseInt(String.valueOf(dataArray.get(5)));
                    player.effectSkill.cooldown = Integer.parseInt(String.valueOf(dataArray.get(6)));
                    player.effectSkill.lastTimeUseSkill = Long.parseLong(String.valueOf(dataArray.get(7)));
                } catch (NumberFormatException e) {
                }
            }
            dataArray.clear();

            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_item_time"));
            int timeUseTDLT = 0;
            int timeOpenPower = 0;
            int timeMayDo = 0;
            int timeKhoBauX2 = 0;
            int timeBuaSanta = 0;
            int timeMeal = 0;
            int iconMeal = 0;
            int timeUseCMS = 0;
            int timeUseGTPT = 0;
            int timeUseDK = 0;
            int timeUseRX = 0;
            int timeMeal2 = 0;
            int iconMeal2 = 0;
            int timeUseNCD = 0;
            int timeNangLuong = 0;
            int timeCoBonLa = 0;
            int timeKhauTrang = 0;
            int timeCarrot = 0;
            long timeBanhTrungThu = 0;
            int timeBanhTrungThuDacBiet = 0;
            int timeBanhTrungThu2Trung = 0;
            int timeBanhTrungThu1Trung = 0;
            int timeBanhDeoThoXanh = 0;
            int timeBanhDeoThoTrang = 0;
            int timeBanhDeoThoHong = 0;
            long timehallowen = 0;
            int idOutfitHalloween = 0;
            long timeShenronPetX3 = 0;
            long timeShenronPorataHp = 0;
            long timeShenronPorataKi = 0;
            long timeShenronPorataDame = 0;
            long timeShenronPlayerX3 = 0;
            int timeBanhChung = 0;
            int timeBanhTet = 0;
            // Lưu ý: Các trường Shenron được lưu ở index 38-42 trong data_item_time
            // (sau idOutfitHalloween ở index 37)

            int timeBoHuyet = (dataArray.size() > 0 && dataArray.get(0) != null
                    && String.valueOf(dataArray.get(0)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(0))), Integer.MAX_VALUE)
                            : 0;
            int timeBoHuyet2 = (dataArray.size() > 1 && dataArray.get(1) != null
                    && String.valueOf(dataArray.get(1)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(1))), Integer.MAX_VALUE)
                            : 0;
            int timeBoKhi = (dataArray.size() > 2 && dataArray.get(2) != null
                    && String.valueOf(dataArray.get(2)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(2))), Integer.MAX_VALUE)
                            : 0;
            int timeBoKhi2 = (dataArray.size() > 3 && dataArray.get(3) != null
                    && String.valueOf(dataArray.get(3)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(3))), Integer.MAX_VALUE)
                            : 0;
            int timeGiapXen = (dataArray.size() > 4 && dataArray.get(4) != null
                    && String.valueOf(dataArray.get(4)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(4))), Integer.MAX_VALUE)
                            : 0;
            int timeGiapXen2 = (dataArray.size() > 5 && dataArray.get(5) != null
                    && String.valueOf(dataArray.get(5)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(5))), Integer.MAX_VALUE)
                            : 0;
            int timeCuongNo = (dataArray.size() > 6 && dataArray.get(6) != null
                    && String.valueOf(dataArray.get(6)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(6))), Integer.MAX_VALUE)
                            : 0;
            int timeCuongNo2 = (dataArray.size() > 7 && dataArray.get(7) != null
                    && String.valueOf(dataArray.get(7)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(7))), Integer.MAX_VALUE)
                            : 0;
            int timeAnDanh = (dataArray.size() > 8 && dataArray.get(8) != null
                    && String.valueOf(dataArray.get(8)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(8))), Integer.MAX_VALUE)
                            : 0;
            int timeAnDanh2 = (dataArray.size() > 9 && dataArray.get(9) != null
                    && String.valueOf(dataArray.get(9)).matches("-?\\d+"))
                            ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(9))), Integer.MAX_VALUE)
                            : 0;

            if (dataArray.size() > 10) {
                timeOpenPower = (String.valueOf(dataArray.get(10)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(10))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 11) {
                timeMayDo = (String.valueOf(dataArray.get(11)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(11))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 12) {
                timeKhoBauX2 = (String.valueOf(dataArray.get(12)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(12))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 14) {
                timeMeal = (String.valueOf(dataArray.get(14)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(14))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 15) {
                iconMeal = (String.valueOf(dataArray.get(15)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(15))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 16) {
                timeUseTDLT = (String.valueOf(dataArray.get(16)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(16))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 17) {
                timeUseCMS = (String.valueOf(dataArray.get(17)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(17))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 18) {
                timeUseGTPT = (String.valueOf(dataArray.get(18)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(18))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 19) {
                timeUseDK = (String.valueOf(dataArray.get(19)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(19))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 20) {
                timeUseRX = (String.valueOf(dataArray.get(20)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(20))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 21) {
                timeMeal2 = (String.valueOf(dataArray.get(21)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(21))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 22) {
                iconMeal2 = (String.valueOf(dataArray.get(22)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(22))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 23) {
                timeUseNCD = (String.valueOf(dataArray.get(23)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(23))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 24) {
                timeBuaSanta = (String.valueOf(dataArray.get(24)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(24))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 25) {
                timeNangLuong = (String.valueOf(dataArray.get(25)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(25))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 26) {
                timeCoBonLa = (String.valueOf(dataArray.get(26)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(26))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 27) {
                timeKhauTrang = (String.valueOf(dataArray.get(27)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(27))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 28) {
                timeCarrot = (String.valueOf(dataArray.get(28)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(28))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 29) {
                timeBanhTrungThu = (String.valueOf(dataArray.get(29)).matches("-?\\d+"))
                        ? Long.parseLong(String.valueOf(dataArray.get(29)))
                        : 0L;
            }
            if (dataArray.size() > 30) {
                timeBanhTrungThuDacBiet = (String.valueOf(dataArray.get(30)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(30))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 31) {
                timeBanhTrungThu2Trung = (String.valueOf(dataArray.get(31)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(31))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 32) {
                timeBanhTrungThu1Trung = (String.valueOf(dataArray.get(32)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(32))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 33) {
                timeBanhDeoThoXanh = (String.valueOf(dataArray.get(33)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(33))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 34) {
                timeBanhDeoThoTrang = (String.valueOf(dataArray.get(34)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(34))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 35) {
                timeBanhDeoThoHong = (String.valueOf(dataArray.get(35)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(35))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 36) {
                timehallowen = (String.valueOf(dataArray.get(36)).matches("-?\\d+"))
                        ? Long.parseLong(String.valueOf(dataArray.get(36)))
                        : 0L;
            }
            if (dataArray.size() > 37) {
                idOutfitHalloween = (String.valueOf(dataArray.get(37)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(37))), Integer.MAX_VALUE)
                        : 0;
            }
            // Đọc thời gian còn lại của các điều ước Shenron từ index 38-42
            if (dataArray.size() > 38) {
                try {
                    timeShenronPetX3 = (String.valueOf(dataArray.get(38)).matches("-?\\d+"))
                            ? Long.parseLong(String.valueOf(dataArray.get(38)))
                            : 0L;
                } catch (Exception e) {
                    timeShenronPetX3 = 0L;
                }
            }
            if (dataArray.size() > 39) {
                try {
                    timeShenronPorataHp = (String.valueOf(dataArray.get(39)).matches("-?\\d+"))
                            ? Long.parseLong(String.valueOf(dataArray.get(39)))
                            : 0L;
                } catch (Exception e) {
                    timeShenronPorataHp = 0L;
                }
            }
            if (dataArray.size() > 40) {
                try {
                    timeShenronPorataKi = (String.valueOf(dataArray.get(40)).matches("-?\\d+"))
                            ? Long.parseLong(String.valueOf(dataArray.get(40)))
                            : 0L;
                } catch (Exception e) {
                    timeShenronPorataKi = 0L;
                }
            }
            if (dataArray.size() > 41) {
                try {
                    timeShenronPorataDame = (String.valueOf(dataArray.get(41)).matches("-?\\d+"))
                            ? Long.parseLong(String.valueOf(dataArray.get(41)))
                            : 0L;
                } catch (Exception e) {
                    timeShenronPorataDame = 0L;
                }
            }
            if (dataArray.size() > 42) {
                try {
                    timeShenronPlayerX3 = (String.valueOf(dataArray.get(42)).matches("-?\\d+"))
                            ? Long.parseLong(String.valueOf(dataArray.get(42)))
                            : 0L;
                } catch (Exception e) {
                    timeShenronPlayerX3 = 0L;
                }
            }
            if (dataArray.size() > 43) {
                timeBanhChung = (String.valueOf(dataArray.get(43)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(43))), Integer.MAX_VALUE)
                        : 0;
            }
            if (dataArray.size() > 44) {
                timeBanhTet = (String.valueOf(dataArray.get(44)).matches("-?\\d+"))
                        ? (int) Math.min(Long.parseLong(String.valueOf(dataArray.get(44))), Integer.MAX_VALUE)
                        : 0;
            }

            player.itemTime.lastTimeBoHuyet = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoHuyet);
            player.itemTime.lastTimeBoKhi = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoKhi);
            player.itemTime.lastTimeGiapXen = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeGiapXen);
            player.itemTime.lastTimeCuongNo = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeCuongNo);
            player.itemTime.lastTimeAnDanh = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeAnDanh);
            player.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoHuyet2);
            player.itemTime.lastTimeBoKhi2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBoKhi2);
            player.itemTime.lastTimeGiapXen2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeGiapXen2);
            player.itemTime.lastTimeCuongNo2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeCuongNo2);
            player.itemTime.lastTimeBanhChung = System.currentTimeMillis() - (ItemTime.TIME_BANH_TET - timeBanhChung);
            player.itemTime.lastTimeBanhTet = System.currentTimeMillis() - (ItemTime.TIME_BANH_TET - timeBanhTet);
            player.itemTime.lastTimeAnDanh2 = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeAnDanh2);
            player.itemTime.lastTimeOpenPower = System.currentTimeMillis() - (ItemTime.TIME_OPEN_POWER - timeOpenPower);
            player.itemTime.lastTimeUseMayDo = System.currentTimeMillis() - (ItemTime.TIME_MAY_DO - timeMayDo);
            player.itemTime.lastTimeUseKhoBauX2 = System.currentTimeMillis() - (ItemTime.TIME_MAY_DO2 - timeKhoBauX2);
            player.itemTime.lastTimeBuaSanta = System.currentTimeMillis() - (ItemTime.TIME_BUA_SANTA - timeBuaSanta);
            player.itemTime.lastTimeEatMeal = System.currentTimeMillis() - (ItemTime.TIME_EAT_MEAL - timeMeal);
            player.itemTime.timeTDLT = timeUseTDLT * 60 * 1000;
            player.itemTime.lastTimeUseTDLT = System.currentTimeMillis();
            player.itemTime.lastTimeUseCMS = System.currentTimeMillis() - (ItemTime.TIME_CMS - timeUseCMS);
            player.itemTime.lastTimeUseGTPT = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeUseGTPT);
            player.itemTime.lastTimeUseDK = System.currentTimeMillis() - (ItemTime.TIME_DK - timeUseDK);
            player.itemTime.timeRX = timeUseRX * 60 * 1000;
            player.itemTime.lastTimeUseRX = System.currentTimeMillis();
            player.itemTime.lastTimeEatMeal2 = System.currentTimeMillis() - (ItemTime.TIME_EAT_MEAL - timeMeal2);
            player.itemTime.lastTimeUseNCD = System.currentTimeMillis() - (ItemTime.TIME_NCD - timeUseNCD);
            player.itemTime.lastTimeNangLuong = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeNangLuong);
            player.itemTime.lastTimeCoBonLa = System.currentTimeMillis() - (ItemTime.TIME_CO_4 - timeCoBonLa);
            player.itemTime.lastTimeKhauTrang = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeKhauTrang);
            player.itemTime.lastTimeBanhTrungThu = System.currentTimeMillis() - (ItemTime.TIME_ITEM - timeBanhTrungThu);
            player.itemTime.lastTimeBanhTrungThuDacBiet = System.currentTimeMillis()
                    - (ItemTime.TIME_ITEM - timeBanhTrungThuDacBiet);
            player.itemTime.lastTimeBanhTrungThu2Trung = System.currentTimeMillis()
                    - (ItemTime.TIME_ITEM - timeBanhTrungThu2Trung);
            player.itemTime.lastTimeBanhTrungThu1Trung = System.currentTimeMillis()
                    - (ItemTime.TIME_ITEM - timeBanhTrungThu1Trung);
            player.itemTime.lastTimeBanhDeoThoXanh = System.currentTimeMillis()
                    - (ItemTime.TIME_ITEM - timeBanhDeoThoXanh);
            player.itemTime.lastTimeBanhDeoThoTrang = System.currentTimeMillis()
                    - (ItemTime.TIME_ITEM - timeBanhDeoThoTrang);
            player.itemTime.lastTimeBanhDeoThoHong = System.currentTimeMillis()
                    - (ItemTime.TIME_ITEM - timeBanhDeoThoHong);
            if (timeShenronPetX3 > 0) {
                player.itemEvent.isShenronPetX3 = true;
                player.itemEvent.lastTimeShenronPetX3 = System.currentTimeMillis()
                        - (ItemTime.TIME_SHENRON_EVENT - timeShenronPetX3);
            } else {
                player.itemEvent.isShenronPetX3 = false;
                player.itemEvent.lastTimeShenronPetX3 = 0;
            }
            if (timeShenronPorataHp > 0) {
                player.itemEvent.isShenronPorataHp = true;
                player.itemEvent.lastTimeShenronPorataHp = System.currentTimeMillis()
                        - (ItemTime.TIME_SHENRON_EVENT - timeShenronPorataHp);
            } else {
                player.itemEvent.isShenronPorataHp = false;
                player.itemEvent.lastTimeShenronPorataHp = 0;
            }
            if (timeShenronPorataKi > 0) {
                player.itemEvent.isShenronPorataKi = true;
                player.itemEvent.lastTimeShenronPorataKi = System.currentTimeMillis()
                        - (ItemTime.TIME_SHENRON_EVENT - timeShenronPorataKi);
            } else {
                player.itemEvent.isShenronPorataKi = false;
                player.itemEvent.lastTimeShenronPorataKi = 0;
            }
            if (timeShenronPorataDame > 0) {
                player.itemEvent.isShenronPorataDame = true;
                player.itemEvent.lastTimeShenronPorataDame = System.currentTimeMillis()
                        - (ItemTime.TIME_SHENRON_EVENT - timeShenronPorataDame);
            } else {
                player.itemEvent.isShenronPorataDame = false;
                player.itemEvent.lastTimeShenronPorataDame = 0;
            }
            if (timeShenronPlayerX3 > 0) {
                player.itemEvent.isShenronPlayerX3 = true;
                player.itemEvent.lastTimeShenronPlayerX3 = System.currentTimeMillis()
                        - (ItemTime.TIME_SHENRON_EVENT - timeShenronPlayerX3);
            } else {
                player.itemEvent.isShenronPlayerX3 = false;
                player.itemEvent.lastTimeShenronPlayerX3 = 0;
            }

            player.itemTime.iconMeal = iconMeal;
            player.itemTime.isEatMeal = timeMeal != 0;
            player.itemTime.isUseBoHuyet = timeBoHuyet != 0;
            player.itemTime.isUseBoKhi = timeBoKhi != 0;
            player.itemTime.isUseGiapXen = timeGiapXen != 0;
            player.itemTime.isUseCuongNo = timeCuongNo != 0;
            player.itemTime.isUseAnDanh = timeAnDanh != 0;
            player.itemTime.isUseBoHuyet2 = timeBoHuyet2 != 0;
            player.itemTime.isUseBoKhi2 = timeBoKhi2 != 0;
            player.itemTime.isUseGiapXen2 = timeGiapXen2 != 0;
            player.itemTime.isUseCuongNo2 = timeCuongNo2 != 0;
            player.itemTime.isUseBanhChung = timeBanhChung != 0;
            player.itemTime.isUseBanhTet = timeBanhTet != 0;
            player.itemTime.isUseAnDanh2 = timeAnDanh2 != 0;
            player.itemTime.isOpenPower = timeOpenPower != 0;
            player.itemTime.isUseMayDo = timeMayDo != 0;
            player.itemTime.isUseKhoBauX2 = timeKhoBauX2 != 0;
            player.itemTime.isUseBuaSanta = timeBuaSanta != 0;
            player.itemTime.isUseTDLT = timeUseTDLT != 0;
            player.itemTime.isUseCMS = timeUseCMS != 0;
            player.itemTime.isUseGTPT = timeUseGTPT != 0;
            player.itemTime.isUseDK = timeUseDK != 0;
            player.itemTime.isUseRX = timeUseRX != 0;
            player.itemTime.iconMeal2 = iconMeal2;
            player.itemTime.isEatMeal2 = timeMeal2 != 0;
            player.itemTime.isUseNCD = timeUseNCD != 0;
            player.itemTime.isUseNangLuong = timeNangLuong != 0;
            player.itemTime.isUseCoBonLa = timeCoBonLa != 0;
            player.itemTime.isUseKhauTrang = timeKhauTrang != 0;
            player.itemTime.isUseCarrot = timeCarrot != 0;
            player.itemTime.timeCarrot = timeCarrot;
            player.itemTime.isUseBanhTrungThu = timeBanhTrungThu != 0;
            player.itemTime.isUseBanhTrungThuDacBiet = timeBanhTrungThuDacBiet != 0;
            player.itemTime.isUseBanhTrungThu2Trung = timeBanhTrungThu2Trung != 0;
            player.itemTime.isUseBanhTrungThu1Trung = timeBanhTrungThu1Trung != 0;
            player.itemTime.isUseBanhDeoThoXanh = timeBanhDeoThoXanh != 0;
            player.itemTime.isUseBanhDeoThoTrang = timeBanhDeoThoTrang != 0;
            player.itemTime.isUseBanhDeoThoHong = timeBanhDeoThoHong != 0;

            player.itemTime.isHallowen = timehallowen != 0;
            player.itemTime.timeHallowen = timehallowen;

            player.itemTime.lastTimeCarrot = System.currentTimeMillis();
            player.itemTime.lastTimeHallowen = System.currentTimeMillis();
            player.itemTime.idOutfitHalloween = idOutfitHalloween;

            dataArray.clear();

            // data nhiệm vụ
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_task"));
            TaskMain taskMain = TaskService.gI().getTaskMainById(player,
                    Byte.parseByte(String.valueOf(dataArray.get(0))));
            taskMain.index = Byte.parseByte(String.valueOf(dataArray.get(1)));
            taskMain.subTasks.get(taskMain.index).count = Short.parseShort(String.valueOf(dataArray.get(2)));
            if (dataArray.size() > 3) {
                taskMain.lastTime = Long.parseLong(String.valueOf(dataArray.get(3)));
            } else {
                taskMain.lastTime = System.currentTimeMillis();
            }
            player.playerTask.taskMain = taskMain;
            dataArray.clear();

            KolTaskService.loadTaskFromDB(player);

            // data nhiệm vụ hàng ngày
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_side_task"));
            String format = "dd-MM-yyyy";
            long receivedTime = Long.parseLong(String.valueOf(dataArray.get(1)));
            Date date = new Date(receivedTime);
            if (TimeUtil.formatTime(date, format).equals(TimeUtil.formatTime(new Date(), format))) {
                player.playerTask.sideTask.template = TaskService.gI()
                        .getSideTaskTemplateById(Integer.parseInt(String.valueOf(dataArray.get(0))));
                player.playerTask.sideTask.count = Integer.parseInt(String.valueOf(dataArray.get(2)));
                player.playerTask.sideTask.maxCount = Integer.parseInt(String.valueOf(dataArray.get(3)));
                player.playerTask.sideTask.leftTask = Integer.parseInt(String.valueOf(dataArray.get(4)));
                player.playerTask.sideTask.level = Integer.parseInt(String.valueOf(dataArray.get(5)));
                player.playerTask.sideTask.receivedTime = receivedTime;
            }

            // data trứng bư
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_mabu_egg"));
            if (!dataArray.isEmpty()) {
                player.mabuEgg = new MabuEgg(player, Long.parseLong(String.valueOf(dataArray.get(0))),
                        Long.parseLong(String.valueOf(dataArray.get(1))));
            }
            dataArray.clear();

            // data bùa
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_charm"));
            player.charms.tdTriTue = Long.parseLong(String.valueOf(dataArray.get(0)));
            player.charms.tdManhMe = Long.parseLong(String.valueOf(dataArray.get(1)));
            player.charms.tdDaTrau = Long.parseLong(String.valueOf(dataArray.get(2)));
            player.charms.tdOaiHung = Long.parseLong(String.valueOf(dataArray.get(3)));
            player.charms.tdBatTu = Long.parseLong(String.valueOf(dataArray.get(4)));
            player.charms.tdDeoDai = Long.parseLong(String.valueOf(dataArray.get(5)));
            player.charms.tdThuHut = Long.parseLong(String.valueOf(dataArray.get(6)));
            player.charms.tdDeTu = Long.parseLong(String.valueOf(dataArray.get(7)));
            player.charms.tdTriTue3 = Long.parseLong(String.valueOf(dataArray.get(8)));
            player.charms.tdTriTue4 = Long.parseLong(String.valueOf(dataArray.get(9)));
            dataArray.clear();

            // data skill
            dataArray = (JSONArray) JSONValue.parse(rs.getString("skills"));
            for (int i = 0; i < dataArray.size(); i++) {
                JSONArray dataSkill = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                int tempId = Integer.parseInt(String.valueOf(dataSkill.get(0)));

                // Validate skill có thuộc gender của player không
                if (!SkillUtil.isSkillValidForGender(tempId, player.gender) && tempId != -1) {
                    Logger.warning("Player " + player.name + " (gender: " + player.gender +
                            ") có skill không hợp lệ trong database: " + tempId + " - sẽ bỏ qua");
                    continue;
                }

                byte point = Byte.parseByte(String.valueOf(dataSkill.get(1)));
                Skill skill;
                if (point != 0) {
                    skill = SkillUtil.createSkill(tempId, point);
                } else {
                    skill = SkillUtil.createSkillLevel0(tempId);
                }
                if (skill != null) {
                    skill.lastTimeUseThisSkill = Long.parseLong(String.valueOf(dataSkill.get(2)));
                    if (dataSkill.size() > 3) {
                        skill.currLevel = Short.parseShort(String.valueOf(dataSkill.get(3)));
                    }
                    player.playerSkill.skills.add(skill);
                }
            }
            dataArray.clear();

            // Validate và filter skills sau khi load - đảm bảo không có skill lệch gender
            int removedCount = SkillUtil.validateAndFilterSkillsByGender(player);
            if (removedCount > 0) {
                Logger.warning(
                        "Đã loại bỏ " + removedCount + " skill không hợp lệ với gender của player " + player.name);
            }

            // List<Item> itemsBody = player.inventory.itemsBody;
            // if (itemsBody.get(10).isNotNullItem()) {
            // PetFollow pet = PetFollowManager.gI().findByID(itemsBody.get(10).getId());
            // player.setPetFollow(pet);
            // PlayerService.gI().sendPetFollow(player);
            // Service.gI().sendPetFollow(player, (short)
            // (player.inventory.itemsBody.get(10).template.iconID - 1));
            // }
            // data skill shortcut
            dataArray = (JSONArray) JSONValue.parse(rs.getString("skills_shortcut"));
            for (int i = 0; i < dataArray.size(); i++) {
                player.playerSkill.skillShortCut[i] = Byte.parseByte(String.valueOf(dataArray.get(i)));
            }

            // Set skillSelect - chỉ chọn skill hợp lệ với gender
            for (int i : player.playerSkill.skillShortCut) {
                if (i != -1 && SkillUtil.isSkillValidForGender(i, player.gender)) {
                    Skill skill = player.playerSkill.getSkillbyId(i);
                    if (skill != null && skill.damage > 0) {
                        player.playerSkill.skillSelect = skill;
                        break;
                    }
                }
            }
            // Nếu không có skill hợp lệ trong shortcut, chọn skill cơ bản của gender
            if (player.playerSkill.skillSelect == null ||
                    player.playerSkill.skillSelect.template == null ||
                    !SkillUtil.isSkillValidForGender(player.playerSkill.skillSelect.template.id, player.gender)) {
                int basicSkillId = player.gender == ConstPlayer.TRAI_DAT
                        ? Skill.DRAGON
                        : (player.gender == ConstPlayer.NAMEC ? Skill.DEMON : Skill.GALICK);
                Skill basicSkill = player.playerSkill.getSkillbyId(basicSkillId);
                if (basicSkill != null && basicSkill.point > 0) {
                    player.playerSkill.skillSelect = basicSkill;
                } else {
                    // Nếu không có skill nào, tạo skill cơ bản
                    Skill newSkill = SkillUtil.createSkill(basicSkillId, 1);
                    if (newSkill != null) {
                        player.playerSkill.skills.add(newSkill);
                        player.playerSkill.skillSelect = newSkill;
                    }
                }
            }
            dataArray.clear();

            // notify
            player.notify = rs.getString("notify");

            // data pet
            JSONArray petData = (JSONArray) JSONValue.parse(rs.getString("pet"));
            if (!petData.isEmpty()) {
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(0)));
                Pet pet = new Pet(player);
                pet.id = -player.id;
                pet.typePet = Byte.parseByte(String.valueOf(dataArray.get(0)));
                pet.gender = Byte.parseByte(String.valueOf(dataArray.get(1)));
                pet.name = String.valueOf(dataArray.get(2));
                player.fusion.typeFusion = Byte.parseByte(String.valueOf(dataArray.get(3)));
                player.fusion.lastTimeFusion = System.currentTimeMillis()
                        - (Fusion.TIME_FUSION - Integer.parseInt(String.valueOf(dataArray.get(4))));
                pet.status = Byte.parseByte(String.valueOf(dataArray.get(5)));

                // data chỉ số
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(1)));
                pet.nPoint.limitPower = Byte.parseByte(String.valueOf(dataArray.get(0)));
                pet.nPoint.power = Long.parseLong(String.valueOf(dataArray.get(1)));
                pet.nPoint.tiemNang = Long.parseLong(String.valueOf(dataArray.get(2)));
                pet.nPoint.stamina = Short.parseShort(String.valueOf(dataArray.get(3)));
                pet.nPoint.maxStamina = Short.parseShort(String.valueOf(dataArray.get(4)));
                pet.nPoint.hpg = Long.parseLong(String.valueOf(dataArray.get(5)));
                pet.nPoint.mpg = Long.parseLong(String.valueOf(dataArray.get(6)));
                pet.nPoint.dameg = Long.parseLong(String.valueOf(dataArray.get(7)));
                pet.nPoint.defg = Integer.parseInt(String.valueOf(dataArray.get(8)));
                pet.nPoint.critg = Integer.parseInt(String.valueOf(dataArray.get(9)));
                long hp = Long.parseLong(String.valueOf(dataArray.get(10)));
                long mp = Long.parseLong(String.valueOf(dataArray.get(11)));

                // data body
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(2)));
                for (int i = 0; i < dataArray.size(); i++) {
                    Item item;
                    JSONArray dataItem = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                    if (tempId != -1) {
                        item = ItemService.gI().createNewItem(tempId,
                                Integer.parseInt(String.valueOf(dataItem.get(1))));
                        JSONArray options = (JSONArray) JSONValue
                                .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                        for (int j = 0; j < options.size(); j++) {
                            JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                            item.itemOptions.add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                    Integer.parseInt(String.valueOf(opt.get(1)))));
                        }
                        item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                        loadAuditTrace(item, dataItem);
                        if (item.template.id == 2132) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                Date currentDate = new Date(item.createTime);
                                Date startDate = formatter.parse("15/03/2024");
                                Date endDate = formatter.parse("28/03/2024");
                                if (currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
                                    System.out.println("Thu hồi cải trang rồng lộn bug.");
                                    item = ItemService.gI().createItemNull();
                                }
                            } catch (ParseException e) {
                            }
                        }
                        if (ItemService.gI().isOutOfDateTime(item)) {
                            item = ItemService.gI().createItemNull();
                        }
                    } else {
                        item = ItemService.gI().createItemNull();
                    }
                    pet.inventory.itemsBody.add(item);
                }

                // data skills
                dataArray = (JSONArray) JSONValue.parse(String.valueOf(petData.get(3)));
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray skillTemp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    int tempId = Integer.parseInt(String.valueOf(skillTemp.get(0)));
                    byte point = Byte.parseByte(String.valueOf(skillTemp.get(1)));
                    Skill skill;
                    if (point != 0) {
                        skill = SkillUtil.createSkill(tempId, point);
                    } else {
                        skill = SkillUtil.createSkillLevel0(tempId);
                    }
                    if (skillTemp.size() > 3) {
                        skill.lastTimeUseThisSkill = Long.parseLong(String.valueOf(skillTemp.get(2)));
                    }
                    if (skillTemp.size() > 3) {
                        skill.currLevel = Short.parseShort(String.valueOf(skillTemp.get(3)));
                    }
                    switch (skill.template.id) {
                        case Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC ->
                            skill.coolDown = 1000;
                    }
                    pet.playerSkill.skills.add(skill);
                }
                while (pet.playerSkill.skills.size() < 8) {
                    pet.playerSkill.skills.add(SkillUtil.createSkillLevel0(-1));
                }

                if (pet.playerSkill.skills.size() >= 6) {
                    pet.playerSkill.skills.set(5, SkillUtil.createSkillLevel0(-1));
                }
                if (pet.playerSkill.skills.size() >= 7) {
                    pet.playerSkill.skills.set(6, SkillUtil.createSkillLevel0(-1));
                }
                if (pet.playerSkill.skills.size() >= 8) {
                    pet.playerSkill.skills.set(7, SkillUtil.createSkillLevel0(-1));
                }
                pet.nPoint.hp = hp;
                pet.nPoint.mp = mp;
                player.pet = pet;
            }

            // Data bảo vệ tài khoản
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("baovetaikhoan"));
                player.mbv = Integer.parseInt(dataArray.get(0).toString());
                player.baovetaikhoan = Boolean.parseBoolean(dataArray.get(1).toString());
                player.mbvtime = Long.parseLong(dataArray.get(2).toString());
            } catch (Exception e) {
                player.mbv = 0;
                player.baovetaikhoan = false;
                player.mbvtime = System.currentTimeMillis();
            }

            // data rada card
            dataArray = (JSONArray) JSONValue.parse(rs.getString("data_card"));
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject obj = (JSONObject) dataArray.get(i);
                player.Cards.add(new Card(Short.parseShort(obj.get("id").toString()),
                        Byte.parseByte(obj.get("amount").toString()), Byte.parseByte(obj.get("max").toString()),
                        Byte.parseByte(obj.get("level").toString()),
                        loadOptionCard((JSONArray) JSONValue.parse(obj.get("option").toString())),
                        Byte.parseByte(obj.get("used").toString())));
            }
            dataArray.clear();

            // data PK Commeson
            player.lastPkCommesonTime = rs.getLong("lasttimepkcommeson");

            // Data BDKB
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("bandokhobau"));
                player.timesPerDayBDKB = Integer.parseInt(dataArray.get(0).toString());
                player.lastTimeJoinBDKB = Long.parseLong(dataArray.get(1).toString());
            } catch (Exception e) {
                player.timesPerDayBDKB = 0;
                player.lastTimeJoinBDKB = System.currentTimeMillis();
            }

            // Data doanh trại
            player.lastTimeJoinDT = rs.getLong("doanhtrai");

            // Data CDRD
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("conduongrandoc"));
                player.joinCDRD = Boolean.parseBoolean(dataArray.get(0).toString());
                player.lastTimeJoinCDRD = Long.parseLong(dataArray.get(1).toString());
                player.talkToThuongDe = Boolean.parseBoolean(dataArray.get(2).toString());
                player.talkToThanMeo = Boolean.parseBoolean(dataArray.get(2).toString());
                if (player.clan.ConDuongRanDoc == null
                        || player.lastTimeJoinCDRD != player.clan.lastTimeOpenConDuongRanDoc) {
                    player.joinCDRD = false;
                    player.talkToThuongDe = false;
                    player.talkToThanMeo = false;
                }
            } catch (Exception e) {
                player.joinCDRD = false;
                player.lastTimeJoinCDRD = 0;
                player.talkToThuongDe = false;
                player.talkToThanMeo = false;
            }

            // Sư phụ không tấn công
            try {
                player.doesNotAttack = rs.getBoolean("masterDoesAttack");
                player.lastTimePlayerNotAttack = System.currentTimeMillis();
            } catch (Exception e) {
                player.doesNotAttack = false;
                player.lastTimePlayerNotAttack = System.currentTimeMillis();
            }

            // data Nhận Thỏi Vàng
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("nhanthoivang"));
                player.danhanthoivang = Boolean.parseBoolean(dataArray.get(0).toString());
                player.lastRewardGoldBarTime = Long.parseLong(dataArray.get(1).toString());
            } catch (Exception e) {
                player.danhanthoivang = false;
                player.lastRewardGoldBarTime = 0;
            }

            // data Rương gỗ
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("ruonggo"));
                player.levelWoodChest = Integer.parseInt(dataArray.get(0).toString());
                player.goldChallenge = Long.parseLong(dataArray.get(1).toString());
                player.rubyChallenge = Long.parseLong(dataArray.get(2).toString());
                player.lastTimeRewardWoodChest = Long.parseLong(dataArray.get(3).toString());
                player.lastTimePKDHVT23 = Long.parseLong(dataArray.get(4).toString());
            } catch (Exception e) {
                player.levelWoodChest = 0;
                player.goldChallenge = 50000000;
                player.rubyChallenge = 100;
                player.lastTimeRewardWoodChest = System.currentTimeMillis();
                player.lastTimePKDHVT23 = 0;
            }

            // data Siêu Thần Thủy
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("sieuthanthuy"));
                player.winSTT = Boolean.parseBoolean(dataArray.get(0).toString());
                player.lastTimeWinSTT = Long.parseLong(dataArray.get(1).toString());
                player.callBossPocolo = Boolean.parseBoolean(dataArray.get(2).toString());
            } catch (Exception e) {
            }

            // data Võ đài sinh tử
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("vodaisinhtu"));
                player.haveRewardVDST = Boolean.parseBoolean(dataArray.get(0).toString());
                player.gemVoDaiSinhTu = Integer.parseInt(dataArray.get(1).toString());
                player.lastTimePKVoDaiSinhTu = Long.parseLong(dataArray.get(2).toString());
                player.timePKVDST = Long.parseLong(dataArray.get(3).toString());
            } catch (Exception e) {
            }

            // Thời gian gọi rồng
            player.lastTimeShenronAppeared = rs.getLong("rongxuong");

            // data item event
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_item_event"));
                player.itemEvent.remainingTVGSCount = Integer.parseInt(dataArray.get(0).toString());
                player.itemEvent.lastTVGSTime = Long.parseLong(dataArray.get(1).toString());
                player.itemEvent.remainingHHCount = Integer.parseInt(dataArray.get(2).toString());
                player.itemEvent.lastHHTime = Long.parseLong(dataArray.get(3).toString());
                player.itemEvent.remainingBNCount = Integer.parseInt(dataArray.get(4).toString());
                player.itemEvent.lastBNTime = Long.parseLong(dataArray.get(5).toString());
                player.itemEvent.remainingBanhQuyCount = Integer.parseInt(dataArray.get(6).toString());
                player.itemEvent.lastItemBanhQuy = Long.parseLong(dataArray.get(7).toString());
                player.itemEvent.remainingKeoNguoiTuyetCount = Integer.parseInt(dataArray.get(8).toString());
                player.itemEvent.lastItemKeoNguoiTuyet = Long.parseLong(dataArray.get(9).toString());
                player.itemEvent.remainingCaTuyetCount = Integer.parseInt(dataArray.get(10).toString());
                player.itemEvent.lastItemCaTuyet = Long.parseLong(dataArray.get(11).toString());
                player.itemEvent.remainingChuongDongCount = Integer.parseInt(dataArray.get(12).toString());
                player.itemEvent.lastItemChuongDong = Long.parseLong(dataArray.get(13).toString());
                player.itemEvent.remainingKeoDuongCount = Integer.parseInt(dataArray.get(14).toString());
                player.itemEvent.lastItemKeoDuong = Long.parseLong(dataArray.get(15).toString());
                if (dataArray.size() > 16) {
                    try {
                        player.itemEvent.lastLimitedItemDropTime = Long.parseLong(dataArray.get(16).toString());
                    } catch (Exception ignored) {
                        player.itemEvent.lastLimitedItemDropTime = 0;
                    }
                } else {
                    player.itemEvent.lastLimitedItemDropTime = 0;
                }
                if (dataArray.size() > 17) {
                    player.itemEvent.loadLimitedItemDropJson(String.valueOf(dataArray.get(17)));
                } else {
                    player.itemEvent.loadLimitedItemDropJson("{}");
                }
            } catch (Exception e) {
                player.itemEvent.remainingTVGSCount = 0;
                player.itemEvent.lastTVGSTime = 0;
                player.itemEvent.remainingHHCount = 0;
                player.itemEvent.lastHHTime = 0;
                player.itemEvent.remainingBNCount = 0;
                player.itemEvent.lastBNTime = 0;
                player.itemEvent.remainingBanhQuyCount = 0;
                player.itemEvent.lastItemBanhQuy = 0;
                player.itemEvent.remainingCaTuyetCount = 0;
                player.itemEvent.lastItemCaTuyet = 0;
                player.itemEvent.remainingChuongDongCount = 0;
                player.itemEvent.lastItemChuongDong = 0;
                player.itemEvent.remainingKeoDuongCount = 0;
                player.itemEvent.lastItemKeoDuong = 0;
                player.itemEvent.remainingKeoNguoiTuyetCount = 0;
                player.itemEvent.lastItemKeoNguoiTuyet = 0;
                player.itemEvent.lastLimitedItemDropTime = 0;
                player.itemEvent.loadLimitedItemDropJson("{}");

            }
            // data luyện tập
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_luyentap"));
                player.levelLuyenTap = Integer.parseInt(dataArray.get(0).toString());
                player.dangKyTapTuDong = Boolean.parseBoolean(dataArray.get(1).toString());
                player.mapIdDangTapTuDong = Integer.parseInt(dataArray.get(2).toString());
                player.tnsmLuyenTap = Integer.parseInt(dataArray.get(3).toString());
                player.lastTimeOffline = Long.parseLong(dataArray.get(4).toString());
                if (dataArray.size() > 5) {
                    player.traning.setTop(Integer.parseInt(dataArray.get(5).toString()));
                    player.traning.setTime(Integer.parseInt(dataArray.get(6).toString()));
                    player.traning.setLastTime(Long.parseLong(dataArray.get(7).toString()));
                    player.traning.setLastTop(Integer.parseInt(dataArray.get(8).toString()));
                    player.traning.setLastRewardTime(Long.parseLong(dataArray.get(9).toString()));
                }
            } catch (Exception e) {
                player.levelLuyenTap = 0;
                player.dangKyTapTuDong = false;
                player.mapIdDangTapTuDong = -1;
                player.tnsmLuyenTap = 0;
                player.lastTimeOffline = System.currentTimeMillis();
            }

            // data nhiệm vụ bang hàng ngày
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_clan_task"));
                format = "dd-MM-yyyy";
                receivedTime = Long.parseLong(String.valueOf(dataArray.get(1)));
                date = new Date(receivedTime);
                if (TimeUtil.formatTime(date, format).equals(TimeUtil.formatTime(new Date(), format))) {
                    player.playerTask.clanTask.template = TaskService.gI()
                            .getClanTaskTemplateById(Integer.parseInt(String.valueOf(dataArray.get(0))));
                    player.playerTask.clanTask.count = Integer.parseInt(String.valueOf(dataArray.get(2)));
                    player.playerTask.clanTask.maxCount = Integer.parseInt(String.valueOf(dataArray.get(3)));
                    player.playerTask.clanTask.leftTask = Integer.parseInt(String.valueOf(dataArray.get(4)));
                    player.playerTask.clanTask.level = Integer.parseInt(String.valueOf(dataArray.get(5)));
                    player.playerTask.clanTask.receivedTime = receivedTime;
                }
            } catch (Exception e) {
            }

            // data vip
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_vip"));
                player.timesPerDayCuuSat = Integer.parseInt(String.valueOf(dataArray.get(0)));
                player.lastTimeCuuSat = Long.parseLong(String.valueOf(dataArray.get(1)));
                player.nhanDeTuNangVIP = Boolean.parseBoolean(String.valueOf(dataArray.get(2)));
                player.nhanVangNangVIP = Boolean.parseBoolean(String.valueOf(dataArray.get(3)));
                if (dataArray.size() > 6) {
                    player.nhanSKHVIP = Boolean.parseBoolean(String.valueOf(dataArray.get(4)));
                    player.vip = Byte.parseByte(dataArray.get(5).toString());
                    player.timevip = Long.parseLong(dataArray.get(6).toString());
                }

            } catch (Exception e) {
            }
            try {
                String eventData = rs.getString("data_event");
                if (eventData == null || eventData.isEmpty() || eventData.equalsIgnoreCase("null")) {
                    eventData = "[]";
                }
                JSONArray dataArrayevent = (JSONArray) JSONValue.parse(eventData);

                if (dataArrayevent != null && dataArrayevent.size() >= 10) {
                    player.eventPointType1 = Integer.parseInt(String.valueOf(dataArrayevent.get(0)));
                    player.eventPointType2 = Integer.parseInt(String.valueOf(dataArrayevent.get(1)));
                    player.eventPointType3 = Integer.parseInt(String.valueOf(dataArrayevent.get(2)));
                    player.eventPointType4 = Integer.parseInt(String.valueOf(dataArrayevent.get(3)));
                    player.eventPointType5 = Integer.parseInt(String.valueOf(dataArrayevent.get(4)));
                    player.eventPointType6 = Integer.parseInt(String.valueOf(dataArrayevent.get(5)));

                    player.checkDailyReward = Boolean.parseBoolean(String.valueOf(dataArrayevent.get(6)));
                    player.checkTopReward1 = Boolean.parseBoolean(String.valueOf(dataArrayevent.get(7)));
                    player.checkTopReward2 = Boolean.parseBoolean(String.valueOf(dataArrayevent.get(8)));
                    player.checkTopReward3 = Boolean.parseBoolean(String.valueOf(dataArrayevent.get(9)));

                    if (dataArrayevent.size() > 11) {
                        try {
                            player.eventTimeType1 = Long.parseLong(String.valueOf(dataArrayevent.get(10)));
                            player.eventTimeType2 = Long.parseLong(String.valueOf(dataArrayevent.get(11)));
                        } catch (Exception ignored) {
                            player.eventTimeType1 = 0L;
                            player.eventTimeType2 = 0L;
                        }
                    } else {
                        player.eventTimeType1 = 0L;
                        player.eventTimeType2 = 0L;
                    }
                    if (dataArrayevent.size() > 12) {
                        try {
                            player.lastTrungThuDropDay = Integer.parseInt(String.valueOf(dataArrayevent.get(12)));
                        } catch (Exception ignored) {
                            player.lastTrungThuDropDay = -1;
                        }
                    }
                    if (dataArrayevent.size() > 13) {
                        try {
                            player.lastTrungThuDropBoot = String.valueOf(dataArrayevent.get(13));
                        } catch (Exception ignored) {
                            player.lastTrungThuDropBoot = null;
                        }
                    }
                    if (dataArrayevent.size() > 14) {
                        try {
                            player.lastHalloweenDropDay = Integer.parseInt(String.valueOf(dataArrayevent.get(14)));
                        } catch (Exception ignored) {
                            player.lastHalloweenDropDay = -1;
                        }
                    }
                    if (dataArrayevent.size() > 15) {
                        try {
                            player.lastHalloweenDropBoot = String.valueOf(dataArrayevent.get(15));
                        } catch (Exception ignored) {
                            player.lastHalloweenDropBoot = null;
                        }
                    }
                    if (dataArrayevent.size() > 16) {
                        try {
                            player.lastGemDropDay = Integer.parseInt(String.valueOf(dataArrayevent.get(16)));
                        } catch (Exception ignored) {
                            player.lastGemDropDay = -1;
                        }
                    }
                    if (dataArrayevent.size() > 17) {
                        try {
                            player.lastGemDropBoot = String.valueOf(dataArrayevent.get(17));
                        } catch (Exception ignored) {
                            player.lastGemDropBoot = null;
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                org.json.simple.JSONArray ev = (org.json.simple.JSONArray) org.json.simple.JSONValue
                        .parse(rs.getString("data_event"));
                if (ev != null) {
                    if (ev.size() > 16) {
                        try {
                            player.lastCandyResetDate = String.valueOf(ev.get(16));
                        } catch (Exception ignored) {
                            player.lastCandyResetDate = null;
                        }
                    }
                    if (ev.size() > 17) {
                        try {
                            String candyJson = String.valueOf(ev.get(17));
                            org.json.simple.JSONArray arr = (org.json.simple.JSONArray) org.json.simple.JSONValue
                                    .parse(candyJson);
                            if (player.candyDeclinedNpcIds == null) {
                                player.candyDeclinedNpcIds = new java.util.HashSet<>();
                            } else {
                                player.candyDeclinedNpcIds.clear();
                            }
                            if (arr != null) {
                                for (Object o : arr) {
                                    try {
                                        player.candyDeclinedNpcIds.add(Integer.parseInt(String.valueOf(o)));
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    try {
                        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        String today = df.format(new java.util.Date());
                        if (player.lastCandyResetDate == null || !today.equals(player.lastCandyResetDate)) {
                            if (player.candyDeclinedNpcIds != null) {
                                player.candyDeclinedNpcIds.clear();
                            }
                            player.lastCandyResetDate = today;
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("LearnSkill"));
                player.LearnSkill.Time = Long.parseLong(String.valueOf(dataArray.get(0)));
                player.LearnSkill.ItemTemplateSkillId = Short.parseShort(String.valueOf(dataArray.get(1)));
                player.LearnSkill.Potential = Integer.parseInt(String.valueOf(dataArray.get(2)));

            } catch (Exception e) {
            }
            // data super rank
            SuperRankDAO.loadData(player);

            if (Util.isAfterMidnight(player.superRank.lastPKTime)) {
                if (player.superRank.ticket < 3) {
                    player.superRank.ticket++;
                }
                player.superRank.lastPKTime = System.currentTimeMillis();
            }
            // dataArray = (JSONArray) JSONValue.parse(rs.getString("diemdanh"));
            // player.CheckDayOnl = Byte.parseByte(String.valueOf(dataArray.get(0)));
            // player.diemdanh = Byte.parseByte(String.valueOf(dataArray.get(1)));
            // dataArray.clear();
            // try {
            // Calendar ngayvps = Calendar.getInstance();
            // int ngayhomnay = ngayvps.get(Calendar.DAY_OF_MONTH);
            // if (player.CheckDayOnl == 0) {
            // player.CheckDayOnl = (byte) (ngayhomnay - 1);
            // }
            // if (ngayhomnay > player.CheckDayOnl) {
            // player.CheckDayOnl = (byte) ngayhomnay;
            // player.diemdanh = 0;
            // }
            // } catch (Exception e) {
            // }
            // data achievement
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data_achievement"));
                for (int i = 0; i < Manager.ACHIEVEMENT_TEMPLATE.size(); i++) {
                    AchievementQuest aq;
                    if (i < dataArray.size()) {
                        JSONArray data = (JSONArray) JSONValue.parse(dataArray.get(i).toString());
                        aq = new AchievementQuest(Long.parseLong(data.get(0).toString()),
                                Boolean.parseBoolean(data.get(1).toString()));
                    } else {
                        aq = new AchievementQuest(0, false);
                    }
                    player.achievement.add(aq);
                }
                dataArray.clear();
            } catch (Exception e) {
            }

            // Giftcode
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("giftcode"));
                for (Object code : dataArray) {
                    player.giftCode.add((String) code);
                }
                dataArray.clear();
            } catch (Exception e) {
            }
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("BoughtSkill"));
                for (Object idSkill : dataArray) {
                    player.BoughtSkill.add(((Long) idSkill).intValue());
                }
                dataArray.clear();
            } catch (Exception e) {
                Logger.log(e.toString());
            }
            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("dataBadges"));

                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject obj = (JSONObject) dataArray.get(i);

                    int idBadges = Integer.parseInt(obj.get("idBadGes").toString());
                    long timeOfUseBadges = Long.parseLong(obj.get("timeofUseBadges").toString());
                    boolean isUse = Boolean.parseBoolean(String.valueOf(obj.get("isUse")));

                    player.dataBadges.add(new BadgesData(idBadges, timeOfUseBadges, isUse));
                }
                dataArray.clear();
            } catch (Exception ex) {
            }

            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("dailyGift"));
                if (dataArray == null || dataArray.size() < 2) {
                    DailyGiftService.addAndReset(player);
                } else {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject obj = (JSONObject) dataArray.get(i);
                        DailyGiftDAO data = new DailyGiftDAO();
                        data.id = Byte.parseByte(obj.get("id").toString());
                        data.daNhan = Boolean.parseBoolean(obj.get("daNhan").toString());
                        player.dailyGiftDao.add(data);
                    }
                }
                if (dataArray != null) {
                    dataArray.clear();
                }
            } catch (Exception ex) {
                DailyGiftService.addAndReset(player);
            }

            try {
                dataArray = (JSONArray) JSONValue.parse(rs.getString("dataTaskBadges"));

                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject obj = (JSONObject) dataArray.get(i);

                    BadgesTask data = new BadgesTask();
                    data.id = Integer.parseInt(obj.get("id").toString());
                    data.count = Integer.parseInt(obj.get("count").toString());
                    data.countMax = Integer.parseInt(obj.get("countMax").toString());
                    data.idBadgesReward = Integer.parseInt(obj.get("idBadgesReward").toString());

                    // Load badgeClaimed flag nếu có, nếu không có thì mặc định false
                    // Nếu task đã hoàn thành và đã có badge trong dataBadges thì đánh dấu đã claim
                    if (obj.containsKey("badgeClaimed")) {
                        data.badgeClaimed = Boolean.parseBoolean(obj.get("badgeClaimed").toString());
                    } else {
                        // Kiểm tra backward compatibility: nếu đã có badge thì đánh dấu đã claim
                        boolean hasBadge = player.dataBadges.stream()
                                .anyMatch(bg -> bg.idBadGes == data.idBadgesReward);
                        data.badgeClaimed = hasBadge || (data.count >= data.countMax && data.countMax > 0);
                    }

                    player.dataTaskBadges.add(data);
                }
                dataArray.clear();
            } catch (Exception ex) {
                BadgesTaskService.createAndResetTask(player);
            }

            try {
                String dataTopRewardStr = rs.getString("data_top");
                if (dataTopRewardStr != null && !dataTopRewardStr.isEmpty()) {
                    Object parsed = JSONValue.parse(dataTopRewardStr);
                    if (parsed instanceof JSONObject) {
                        JSONObject dataTopReward = (JSONObject) parsed;
                        for (Object key : dataTopReward.keySet()) {
                            String tagName = (String) key;
                            JSONArray itemsArray = (JSONArray) JSONValue.parse((String) dataTopReward.get(tagName));
                            List<Item> items = new ArrayList<>();
                            for (Object itemDataObj : itemsArray) {
                                JSONArray dataItem = (JSONArray) JSONValue.parse(itemDataObj.toString());
                                short tempId = Short.parseShort(String.valueOf(dataItem.get(0)));
                                if (tempId != -1) {
                                    Item item = ItemService.gI().createNewItem(tempId,
                                            Integer.parseInt(String.valueOf(dataItem.get(1))));
                                    JSONArray options = (JSONArray) JSONValue
                                            .parse(String.valueOf(dataItem.get(2)).replaceAll("\"", ""));
                                    for (int j = 0; j < options.size(); j++) {
                                        JSONArray opt = (JSONArray) JSONValue.parse(String.valueOf(options.get(j)));
                                        item.itemOptions
                                                .add(new Item.ItemOption(Integer.parseInt(String.valueOf(opt.get(0))),
                                                        Integer.parseInt(String.valueOf(opt.get(1)))));
                                    }
                                    item.createTime = Long.parseLong(String.valueOf(dataItem.get(3)));
                                    loadAuditTrace(item, dataItem);
                                    if (!ItemService.gI().isOutOfDateTime(item)) {
                                        items.add(item);
                                    }
                                }
                            }
                            player.topRewards.put(tagName, items);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("Error loading top rewards for player " + player.name + "\n");
                Logger.logException(NDVSqlFetcher.class, e, "Failed to parse top rewards for " + player.name);
            }

            player.nPoint.hp = plHp;
            player.nPoint.mp = plMp;
            player.idMark.setLoadedAllDataPlayer(true);
        } catch (Exception e) {
            if (player != null) {
                player.dispose();
                player = null;
            }
            throw e;
        }
        return player;
    }

    public static List<OptionCard> loadOptionCard(JSONArray json) {
        List<OptionCard> ops = new ArrayList<>();
        try {
            for (Object o : json) {
                JSONObject ob = (JSONObject) o;
                if (ob != null) {
                    ops.add(new OptionCard(Integer.parseInt(ob.get("id").toString()),
                            Integer.parseInt(ob.get("param").toString()), Byte.parseByte(ob.get("active").toString())));
                }
            }
        } catch (NumberFormatException e) {
        }
        return ops;
    }
}
