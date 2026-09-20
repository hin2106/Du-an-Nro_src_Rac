package daos;

import data.AlyraManager;
import data.AlyraResultSet;
import item.Item;
import item.ItemTime;
import mail.entity.MailReward;
import mail.enums.RewardType;
import mail.service.MailService;
import player.Friend;
import player.Fusion;
import player.Inventory;
import player.Player;
import skill.Skill;
import services.map.MapService;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import network.MySession;
import system.Template;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.Manager;
import services.map.ChangeMapService;
import utils.TimeUtil;

import java.util.List;
import java.util.Map;
import task.KolTaskService;

@SuppressWarnings("unchecked")
public class PlayerDAO {

    private static final String WELCOME_MAIL_TITLE = "Welcome to the server!";
    private static final String WELCOME_MAIL_CONTENT = "Chào mừng bạn đến với máy chủ!\n"
            + "Nhận quà tân thủ để bắt đầu hành trình trở thành người chơi mạnh nhất nào.";
    private static final int WELCOME_MAIL_EXPIRE_DAYS = 30;

    public static boolean createNewPlayer(int userId, String name, byte gender, int hair) {
        try {
            JSONArray dataArray = new JSONArray();
            int greenGem = (Manager.TEST) ? 1000000000 : 1000;
            dataArray.add(2000); // vàng
            dataArray.add(greenGem); // ngọc xanh
            dataArray.add(0); // hồng ngọc
            dataArray.add(0); // point
            dataArray.add(0); // event
            String inventory = dataArray.toJSONString();
            dataArray.clear();
            dataArray.add(39 + gender); // map
            dataArray.add(100); // x
            dataArray.add(384); // y
            String location = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); // giới hạn sức mạnh
            dataArray.add(2000); // sức mạnh
            dataArray.add(2000); // tiềm năng
            dataArray.add(1000); // thể lực
            dataArray.add(1000); // thể lực đầy
            dataArray.add(gender == 0 ? 200 : 100); // hp gốc
            dataArray.add(gender == 1 ? 200 : 100); // ki gốc
            dataArray.add(gender == 2 ? 15 : 10); // sức đánh gốc
            dataArray.add(0); // giáp gốc
            dataArray.add(0); // chí mạng gốc
            dataArray.add(0); // chí mạng dragon
            dataArray.add(0); // năng động
            dataArray.add(gender == 0 ? 200 : 100); // hp hiện tại
            dataArray.add(gender == 1 ? 200 : 100); // ki hiện tại
            String point = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(1); // level
            dataArray.add(5); // curent pea
            dataArray.add(0); // is upgrade
            dataArray.add(new Date().getTime()); // last time harvest
            dataArray.add(new Date().getTime()); // last time upgrade
            String magicTree = dataArray.toJSONString();
            dataArray.clear();
            /**
             *
             * [
             * {"temp_id":"1","option":[[5,7],[7,3]],"create_time":"49238749283748957""},
             * {"temp_id":"1","option":[[5,7],[7,3]],"create_time":"49238749283748957""},
             * {"temp_id":"-1","option":[],"create_time":"0""}, ... ]
             */

            int idAo = gender == 0 ? 0 : gender == 1 ? 1 : 2;
            int idQuan = gender == 0 ? 6 : gender == 1 ? 7 : 8;
            int def = gender == 2 ? 3 : 2;
            int hp = gender == 0 ? 30 : 20;

            JSONArray item = new JSONArray();
            JSONArray options = new JSONArray();
            JSONArray opt = new JSONArray();
            for (int i = 0; i < 9; i++) {
                switch (i) {
                    case 0 -> {
                        // áo
                        opt.add(47); // id option
                        opt.add(def); // param option
                        item.add(idAo); // id item
                        item.add(1); // số lượng
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    case 1 -> {
                        // quần
                        opt.add(6); // id option
                        opt.add(hp); // param option
                        item.add(idQuan); // id item
                        item.add(1); // số lượng
                        options.add(opt.toJSONString());
                        opt.clear();
                    }
                    default -> {
                        item.add(-1); // id item
                        item.add(0); // số lượng
                    }
                }
                item.add(options.toJSONString()); // full option item
                item.add(System.currentTimeMillis()); // thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBody = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 20; i++) {
                if (i == 0) { // thỏi vàng
                    opt.add(2); // id option
                    opt.add(8); // param option
                    item.add(63); // id item
                    item.add(10); // số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); // id item
                    item.add(0); // số lượng
                }
                item.add(options.toJSONString()); // full option item
                item.add(System.currentTimeMillis()); // thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBag = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 20; i++) {
                if (i == 0) { // rada
                    opt.add(14); // id option
                    opt.add(1); // param option
                    item.add(12); // id item
                    item.add(1); // số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); // id item
                    item.add(0); // số lượng
                }
                item.add(options.toJSONString()); // full option item
                item.add(System.currentTimeMillis()); // thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBox = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 20; i++) {
                if (i == 0) { // rada
                    opt.add(-1); // id option
                    opt.add(0); // param option
                    item.add(-1); // id item
                    item.add(0); // số lượng
                    options.add(opt.toJSONString());
                    opt.clear();
                } else {
                    item.add(-1); // id item
                    item.add(0); // số lượng
                }
                item.add(options.toJSONString()); // full option item
                item.add(System.currentTimeMillis()); // thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBox1 = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 110; i++) {
                item.add(-1); // id item
                item.add(0); // số lượng
                item.add(options.toJSONString()); // full option item
                item.add(System.currentTimeMillis()); // thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsBoxLuckyRound = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 110; i++) {
                item.add(-1); // id item
                item.add(0); // số lượng
                item.add(options.toJSONString()); // full option item
                item.add(System.currentTimeMillis()); // thời gian item được tạo
                dataArray.add(item.toJSONString());
                options.clear();
                item.clear();
            }
            String itemsDaBan = dataArray.toJSONString();
            dataArray.clear();

            for (int i = 0; i < 110; i++) {
                JSONArray linhdz = new JSONArray();
                linhdz.add(-1);
                linhdz.add(0);
                linhdz.add(new JSONArray().toJSONString());
                linhdz.add(System.currentTimeMillis());
                dataArray.add(linhdz.toJSONString());
            }

            String itemMailBox = dataArray.toJSONString();
            dataArray.clear();

            String friends = dataArray.toJSONString();
            String enemies = dataArray.toJSONString();

            dataArray.add(0); // id nội tại
            dataArray.add(0); // chỉ số 1
            dataArray.add(0); // chỉ số 2
            dataArray.add(0); // số lần mở
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            dataArray.add(0); //
            String intrinsic = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(0); // 0: bổ huyết
            dataArray.add(0); // 1: bổ huyết 2
            dataArray.add(0); // 2: bổ khí
            dataArray.add(0); // 3: bổ khí 2
            dataArray.add(0); // 4: giáp xên
            dataArray.add(0); // 5: giáp xên 2
            dataArray.add(0); // 6: cuồng nộ
            dataArray.add(0); // 7: cuồng nộ 2
            dataArray.add(0); // 8: ẩn danh
            dataArray.add(0); // 9: ẩn danh 2
            dataArray.add(0); // 10: mở giới hạn sức mạnh
            dataArray.add(0); // 11: máy dò
            dataArray.add(0); // 12: kho báu x2
            dataArray.add(0); // 13: reserved
            dataArray.add(0); // 14: thức ăn cold
            dataArray.add(0); // 15: icon thức ăn cold
            dataArray.add(0); // 16: TDLT
            dataArray.add(0); // 17: CMS
            dataArray.add(0); // 18: GTPT
            dataArray.add(0); // 19: DK
            dataArray.add(0); // 20: RX
            dataArray.add(0); // 21: thức ăn cold 2
            dataArray.add(0); // 22: icon thức ăn cold 2
            dataArray.add(0); // 23: NCD
            dataArray.add(0); // 24: bùa Santa
            dataArray.add(0); // 25: năng lượng
            dataArray.add(0); // 26: cỏ bốn lá
            dataArray.add(0); // 27: khẩu trang
            dataArray.add(0); // 28: carrot
            dataArray.add(0); // 29: bánh trung thu
            dataArray.add(0); // 30: bánh trung thu đặc biệt
            dataArray.add(0); // 31: bánh trung thu 2 trứng
            dataArray.add(0); // 32: bánh trung thu 1 trứng
            dataArray.add(0); // 33: bánh dẻo thỏ xanh
            dataArray.add(0); // 34: bánh dẻo thỏ trắng
            dataArray.add(0); // 35: bánh dẻo thỏ hồng
            dataArray.add(0); // 36: halloween
            dataArray.add(0); // 37: outfit halloween
            dataArray.add(0); // 38: shenron pet x3
            dataArray.add(0); // 39: shenron porata hp
            dataArray.add(0); // 40: shenron porata ki
            dataArray.add(0); // 41: shenron porata dame
            dataArray.add(0); // 42: shenron player x3
            dataArray.add(0); // 43: Bánh chưng 10%
            dataArray.add(0); // 44: Bánh tét 5%
            String itemTime = dataArray.toJSONString();
            dataArray.clear();
            int taskIndex = (Manager.TEST) ? 28 : 0;
            dataArray.add(taskIndex); // id nhiệm vụ
            dataArray.add(0); // index nhiệm vụ con
            dataArray.add(0); // số lượng đã làm
            String task = dataArray.toJSONString();
            dataArray.clear();

            String kolTask = "[-1,0,0,0]";
            String kolTaskVip = "[-1,0,0,0]";

            String mabuEgg = dataArray.toJSONString();

            dataArray.add(System.currentTimeMillis()); // bùa trí tuệ
            dataArray.add(System.currentTimeMillis()); // bùa mạnh mẽ
            dataArray.add(System.currentTimeMillis()); // bùa da trâu
            dataArray.add(System.currentTimeMillis()); // bùa oai hùng
            dataArray.add(System.currentTimeMillis()); // bùa bất tử
            dataArray.add(System.currentTimeMillis()); // bùa dẻo dai
            dataArray.add(System.currentTimeMillis()); // bùa thu hút
            dataArray.add(System.currentTimeMillis()); // bùa đệ tử
            dataArray.add(System.currentTimeMillis()); // bùa trí tuệ x3
            dataArray.add(System.currentTimeMillis()); // bùa trí tuệ x4
            dataArray.add(System.currentTimeMillis()); // bình hút năng lượng
            String charms = dataArray.toJSONString();
            dataArray.clear();

            int[] skillsArr = gender == 0 ? new int[] { 0, 1, 6, 9, 10, 20, 22, 19}
                    : gender == 1 ? new int[] { 2, 3, 7, 11, 12, 17, 18, 19}
                            : new int[] { 4, 5, 8, 13, 14, 21, 23, 19};
            // [{"temp_id":"4","point":0,"last_time_use":0},]

            JSONArray skill = new JSONArray();
            for (int i = 0; i < skillsArr.length; i++) {
                skill.add(skillsArr[i]); // id skill
                if (i == 0) {
                    skill.add(1); // level skill
                } else {
                    skill.add(0); // level skill
                }
                skill.add(0); // thời gian sử dụng trước đó
                dataArray.add(skill.toString());
                skill.clear();
            }
            String skills = dataArray.toJSONString();
            dataArray.clear();

            dataArray.add(gender == 0 ? 0 : gender == 1 ? 2 : 4);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            dataArray.add(-1);
            String skillsShortcut = dataArray.toJSONString();
            dataArray.clear();

            String petData = dataArray.toJSONString();

            JSONArray blackBall = new JSONArray();
            for (int i = 1; i <= 7; i++) {
                blackBall.add(0);
                blackBall.add(0);
                blackBall.add(0);
                dataArray.add(blackBall.toJSONString());
                blackBall.clear();
            }
            String dataBlackBall = dataArray.toString();
            dataArray.clear();

            dataArray.add(-1); // id side task
            dataArray.add(0); // thời gian nhận
            dataArray.add(0); // số lượng đã làm
            dataArray.add(0); // số lượng cần làm
            dataArray.add(20); // số nhiệm vụ còn lại có thể nhận
            dataArray.add(0); // mức độ nhiệm vụ
            String dataSideTask = dataArray.toJSONString();
            dataArray.clear();
            dataArray.clear();
            dataArray.add(gender == 0 ? 0 : gender == 1 ? 2 : 4);

            String dataBoughtSkill = dataArray.toJSONString();
            dataArray.clear();

            // Data Luyện Tập
            Player player = new Player();
            dataArray.add(player.levelLuyenTap);
            dataArray.add(player.dangKyTapTuDong);
            dataArray.add(player.mapIdDangTapTuDong);
            dataArray.add(player.tnsmLuyenTap);
            if (player.isOffline) {
                dataArray.add(player.lastTimeOffline);
            } else {
                dataArray.add(System.currentTimeMillis());
            }
            dataArray.add(player.traning.getTop());
            dataArray.add(player.traning.getTime());
            dataArray.add(player.traning.getLastTime());
            dataArray.add(player.traning.getLastTop());
            dataArray.add(player.traning.getLastRewardTime());
            String dataLuyenTap = dataArray.toJSONString();
            dataArray.clear();

            // data achievement
            if (player.achievement != null) {
                for (Template.AchievementQuest aq : player.achievement.getAchievementList()) {
                    JSONArray a = new JSONArray();
                    a.add(aq.completed);
                    a.add(aq.isRecieve);
                    dataArray.add(a.toJSONString());
                    a.clear();
                }
            }
            String achievement = dataArray.toJSONString();
            dataArray.clear();

            // gift code
            for (String code : player.giftCode.rewards) {
                dataArray.add(code);
            }
            String giftCode = dataArray.toJSONString();
            dataArray.clear();
            String dailyGift = "[]";
            int insertedPlayer = AlyraManager.executeUpdate("insert into player"
                    + "(account_id, name, head, gender, have_tennis_space_ship, clan_id, "
                    + "data_inventory, data_location, data_point, data_magic_tree, items_body, "
                    + "items_bag, items_box, items_box1, items_box_lucky_round, items_daban ,item_mails_box, friends, enemies, data_intrinsic, data_item_time,"
                    + "data_task, data_mabu_egg, data_charm, skills, skills_shortcut, pet,"
                    + "data_black_ball, data_side_task, BoughtSkill,dailyGift, masterDoesNotAttack, data_luyentap, data_achievement, giftcode, ghi_danh_point) "
                    + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    userId, name, hair, gender, 0, -1, inventory, location, point, magicTree,
                    itemsBody, itemsBag, itemsBox, itemsBox1, itemsBoxLuckyRound, itemsDaBan, itemMailBox, friends,
                    enemies, intrinsic,
                    itemTime, task, mabuEgg, charms, skills, skillsShortcut, petData, dataBlackBall,
                    dataSideTask, dataBoughtSkill,
                    dailyGift, 0, dataLuyenTap, achievement, giftCode, 0);

            if (insertedPlayer != 1) {
                Logger.error("Không thể tạo player mới cho account " + userId + "\n");
                return false;
            }

            long createdPlayerId = getCreatedPlayerId(userId, name);
            if (createdPlayerId > 0) {
                sendWelcomeMailForNewPlayer(createdPlayerId);
            }

            Logger.success("Tạo player mới thành công!\n");
            return true;
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi tạo player mới");
            return false;
        }
    }

    private static long getCreatedPlayerId(int userId, String name) {
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery(
                    "select id from player where account_id = ? and name = ? order by id desc limit 1",
                    userId, name);
            if (rs.first()) {
                return rs.getLong("id");
            }
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Khong the lay id player moi tao");
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return -1;
    }

    private static void sendWelcomeMailForNewPlayer(long playerId) {
        try {
            MailService mailService = new MailService();
            long mailId = mailService.sendMail(
                    playerId,
                    WELCOME_MAIL_TITLE,
                    WELCOME_MAIL_CONTENT,
                    WELCOME_MAIL_EXPIRE_DAYS);

            if (mailId <= 0) {
                return;
            }

            List<MailReward> rewards = new java.util.ArrayList<>();
            rewards.add(new MailReward(mailId, RewardType.GOLD.getCode(), 1000000000));
            rewards.add(new MailReward(mailId, RewardType.GEM.getCode(), 10000));
            rewards.add(new MailReward(mailId, RewardType.ITEM.getCode(), 457, 50, "[{\"id\":30,\"param\":0}]"));

            mailService.getRewardDAO().saveRewards(rewards);
            mailService.getMailDAO().updateHasReward(mailId, true);
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Gui mail chao mung that bai");
        }
    }

    public static void updatePlayer(Player player) {
        if (player != null && player.idMark.isLoadedAllDataPlayer()) {
            long st = System.currentTimeMillis();
            try {
                JSONArray dataArray = new JSONArray();

                // data kim lượng
                dataArray.add(player.inventory.gold > Inventory.LIMIT_GOLD
                        ? Inventory.LIMIT_GOLD
                        : player.inventory.gold);
                dataArray.add(player.inventory.gem);
                dataArray.add(player.inventory.ruby);
                dataArray.add(player.inventory.coupon);
                dataArray.add(player.inventory.event);
                String inventory = dataArray.toJSONString();
                dataArray.clear();

                int mapId = player.mapIdBeforeLogout;
                int x = player.location.x;
                int y = player.location.y;
                long hp = player.nPoint.hp;
                long mp = player.nPoint.mp;
                if (player.isDie()) {
                    mapId = player.gender + 21;
                    x = 300;
                    y = 336;
                    hp = 1;
                    mp = 1;
                } else {
                    if (MapService.gI().isMapDoanhTrai(mapId) || MapService.gI().isMapBlackBallWar(mapId)
                            || ChangeMapService.gI().checkMapCanJoin(player,
                                    MapService.gI().getMapCanJoin(player, mapId, 0)) == null) {
                        mapId = player.gender + 21;
                        x = 300;
                        y = 336;
                    }
                }

                // data vị trí
                dataArray.add(mapId);
                dataArray.add(x);
                dataArray.add(y);
                String location = dataArray.toJSONString();
                dataArray.clear();

                // data chỉ số
                dataArray.add(player.nPoint.limitPower);
                dataArray.add(player.nPoint.power);
                dataArray.add(player.nPoint.tiemNang);
                dataArray.add(player.nPoint.stamina);
                dataArray.add(player.nPoint.maxStamina);
                dataArray.add(player.nPoint.hpg);
                dataArray.add(player.nPoint.mpg);
                dataArray.add(player.nPoint.dameg);
                dataArray.add(player.nPoint.defg);
                dataArray.add(player.nPoint.critg);
                dataArray.add(player.nPoint.critdragon);
                dataArray.add(0);
                dataArray.add(hp);
                dataArray.add(mp);
                String point = dataArray.toJSONString();
                dataArray.clear();

                // data đậu thần
                dataArray.add(player.magicTree.level);
                dataArray.add(player.magicTree.currPeas);
                dataArray.add(player.magicTree.isUpgrade ? 1 : 0);
                dataArray.add(player.magicTree.lastTimeHarvest);
                dataArray.add(player.magicTree.lastTimeUpgrade);
                String magicTree = dataArray.toJSONString();
                dataArray.clear();

                // data body
                JSONArray dataItem = new JSONArray();
                for (Item item : player.inventory.itemsBody) {
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
                String itemsBody = dataArray.toJSONString();
                dataArray.clear();

                // data bag
                for (Item item : player.inventory.itemsBag) {
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
                String itemsBag = dataArray.toJSONString();
                dataArray.clear();

                // data box
                for (Item item : player.inventory.itemsBox) {
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

                // data box 1
                for (Item item : player.inventory.itemsBox1) {
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
                String itemsBox1 = dataArray.toJSONString();
                dataArray.clear();

                // data box crack ball
                for (Item item : player.inventory.itemsBoxCrackBall) {
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
                String itemsBoxLuckyRound = dataArray.toJSONString();
                dataArray.clear();

                // data item da ban
                for (Item item : player.inventory.itemsDaBan) {
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
                String itemsDaBan = dataArray.toJSONString();
                dataArray.clear();

                // data item mail box
                for (int i = 0; i < 110; i++) {
                    JSONArray hieudz = new JSONArray();
                    hieudz.add(-1);
                    hieudz.add(0);
                    hieudz.add(new JSONArray().toJSONString());
                    hieudz.add(System.currentTimeMillis());
                    dataArray.add(hieudz.toJSONString());
                }

                String itemMailBox = dataArray.toJSONString();
                dataArray.clear();

                // data bạn bè
                JSONArray dataFE = new JSONArray();
                for (Friend f : player.friends) {
                    dataFE.add(f.id);
                    dataFE.add(f.name);
                    dataFE.add(f.head);
                    dataFE.add(f.body);
                    dataFE.add(f.leg);
                    dataFE.add(f.bag);
                    dataFE.add(f.power);
                    dataArray.add(dataFE.toJSONString());
                    dataFE.clear();
                }
                String friend = dataArray.toJSONString();
                dataArray.clear();

                // data kẻ thù
                for (Friend e : player.enemies) {
                    dataFE.add(e.id);
                    dataFE.add(e.name);
                    dataFE.add(e.head);
                    dataFE.add(e.body);
                    dataFE.add(e.leg);
                    dataFE.add(e.bag);
                    dataFE.add(e.power);
                    dataArray.add(dataFE.toJSONString());
                    dataFE.clear();
                }
                String enemy = dataArray.toJSONString();
                dataArray.clear();

                // data nội tại
                dataArray.add(player.playerIntrinsic.intrinsic.id);
                dataArray.add(player.playerIntrinsic.intrinsic.param1);
                dataArray.add(player.playerIntrinsic.intrinsic.param2);
                dataArray.add(player.playerIntrinsic.countOpen);
                dataArray.add(player.effectSkill.isIntrinsic);
                dataArray.add(player.effectSkill.skillID);
                dataArray.add(player.effectSkill.cooldown);
                dataArray.add(player.effectSkill.lastTimeUseSkill);
                String intrinsic = dataArray.toJSONString();
                dataArray.clear();

                // data item time
                dataArray.add((player.itemTime.isUseBoHuyet
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet))
                        : 0));
                dataArray.add((player.itemTime.isUseBoHuyet2
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet2))
                        : 0));
                dataArray.add((player.itemTime.isUseBoKhi
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi))
                        : 0));
                dataArray.add((player.itemTime.isUseBoKhi2
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi2))
                        : 0));
                dataArray.add((player.itemTime.isUseGiapXen
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen))
                        : 0));
                dataArray.add((player.itemTime.isUseGiapXen2
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen2))
                        : 0));
                dataArray.add((player.itemTime.isUseCuongNo
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo))
                        : 0));
                dataArray.add((player.itemTime.isUseCuongNo2
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo2))
                        : 0));
                dataArray.add((player.itemTime.isUseAnDanh
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh))
                        : 0));
                dataArray.add((player.itemTime.isUseAnDanh2
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh2))
                        : 0));
                dataArray.add((player.itemTime.isOpenPower
                        ? (ItemTime.TIME_OPEN_POWER - (System.currentTimeMillis() - player.itemTime.lastTimeOpenPower))
                        : 0));
                dataArray.add((player.itemTime.isUseMayDo
                        ? (ItemTime.TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo))
                        : 0));
                dataArray.add((player.itemTime.isUseKhoBauX2
                        ? (ItemTime.TIME_MAY_DO2 - (System.currentTimeMillis() - player.itemTime.lastTimeUseKhoBauX2))
                        : 0));
                dataArray.add(0);
                dataArray.add((player.itemTime.isEatMeal
                        ? (ItemTime.TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal))
                        : 0));
                dataArray.add(player.itemTime.iconMeal);
                dataArray.add((player.itemTime.isUseTDLT
                        ? ((player.itemTime.timeTDLT - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT))
                                / 60 / 1000)
                        : 0));
                dataArray.add((player.itemTime.isUseCMS
                        ? (ItemTime.TIME_CMS - (System.currentTimeMillis() - player.itemTime.lastTimeUseCMS))
                        : 0));
                dataArray.add((player.itemTime.isUseGTPT
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeUseGTPT))
                        : 0));
                dataArray.add((player.itemTime.isUseDK
                        ? (ItemTime.TIME_DK - (System.currentTimeMillis() - player.itemTime.lastTimeUseDK))
                        : 0));
                dataArray.add((player.itemTime.isUseRX
                        ? ((player.itemTime.timeRX - (System.currentTimeMillis() - player.itemTime.lastTimeUseRX)) / 60
                                / 1000)
                        : 0));
                dataArray.add((player.itemTime.isEatMeal2
                        ? (ItemTime.TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal2))
                        : 0));
                dataArray.add(player.itemTime.iconMeal2);
                dataArray.add((player.itemTime.isUseNCD
                        ? (ItemTime.TIME_NCD - (System.currentTimeMillis() - player.itemTime.lastTimeUseNCD))
                        : 0));
                dataArray.add((player.itemTime.isUseBuaSanta
                        ? (ItemTime.TIME_BUA_SANTA - (System.currentTimeMillis() - player.itemTime.lastTimeBuaSanta))
                        : 0));
                dataArray.add((player.itemTime.isUseNangLuong
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeNangLuong))
                        : 0));
                dataArray.add((player.itemTime.isUseCoBonLa
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCoBonLa))
                        : 0));
                dataArray.add((player.itemTime.isUseKhauTrang
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeKhauTrang))
                        : 0));
                dataArray.add((player.itemTime.isUseCarrot
                        ? (player.itemTime.timeCarrot - (System.currentTimeMillis() - player.itemTime.lastTimeCarrot))
                        : 0));
                dataArray
                        .add((player.itemTime.isUseBanhTrungThu
                                ? (ItemTime.TIME_TRUNG_THU
                                        - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThu))
                                : 0));
                dataArray
                        .add((player.itemTime.isUseBanhTrungThuDacBiet
                                ? (ItemTime.TIME_ITEM
                                        - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThuDacBiet))
                                : 0));
                dataArray
                        .add((player.itemTime.isUseBanhTrungThu2Trung
                                ? (ItemTime.TIME_ITEM
                                        - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThu2Trung))
                                : 0));
                dataArray
                        .add((player.itemTime.isUseBanhTrungThu1Trung
                                ? (ItemTime.TIME_ITEM
                                        - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThu1Trung))
                                : 0));
                dataArray.add((player.itemTime.isUseBanhDeoThoXanh
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhDeoThoXanh))
                        : 0));
                dataArray.add((player.itemTime.isUseBanhDeoThoTrang
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhDeoThoTrang))
                        : 0));
                dataArray.add((player.itemTime.isUseBanhDeoThoHong
                        ? (ItemTime.TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhDeoThoHong))
                        : 0));
                dataArray
                        .add((player.itemTime.isHallowen
                                ? (player.itemTime.timeHallowen
                                        - (System.currentTimeMillis() - player.itemTime.lastTimeHallowen))
                                : 0));
                dataArray.add(player.itemTime.idOutfitHalloween);
                dataArray.add((player.itemEvent != null && player.itemEvent.isShenronPetX3
                    ? (ItemTime.TIME_SHENRON_EVENT
                        - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPetX3))
                    : 0));
                dataArray.add((player.itemEvent != null && player.itemEvent.isShenronPorataHp
                    ? (ItemTime.TIME_SHENRON_EVENT
                        - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPorataHp))
                    : 0));
                dataArray.add((player.itemEvent != null && player.itemEvent.isShenronPorataKi
                    ? (ItemTime.TIME_SHENRON_EVENT
                        - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPorataKi))
                    : 0));
                dataArray.add((player.itemEvent != null && player.itemEvent.isShenronPorataDame
                    ? (ItemTime.TIME_SHENRON_EVENT
                        - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPorataDame))
                    : 0));
                dataArray.add((player.itemEvent != null && player.itemEvent.isShenronPlayerX3
                    ? (ItemTime.TIME_SHENRON_EVENT
                        - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPlayerX3))
                    : 0));
                dataArray.add((player.itemTime.isUseBanhChung
                    ? (ItemTime.TIME_BANH_TET - (System.currentTimeMillis() - player.itemTime.lastTimeBanhChung))
                    : 0));
                dataArray.add((player.itemTime.isUseBanhTet
                    ? (ItemTime.TIME_BANH_TET - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTet))
                    : 0));
                String itemTime = dataArray.toJSONString();
                dataArray.clear();

                // data nhiệm vụ
                dataArray.add(player.playerTask.taskMain.id);
                dataArray.add(player.playerTask.taskMain.index);
                dataArray.add(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
                dataArray.add(player.playerTask.taskMain.lastTime);
                String task = dataArray.toJSONString();
                dataArray.clear();

                KolTaskService.saveTaskToDB(player);

                // data nhiệm vụ hàng ngày
                dataArray
                        .add(player.playerTask.sideTask.template != null ? player.playerTask.sideTask.template.id : -1);
                dataArray.add(player.playerTask.sideTask.receivedTime);
                dataArray.add(player.playerTask.sideTask.count);
                dataArray.add(player.playerTask.sideTask.maxCount);
                dataArray.add(player.playerTask.sideTask.leftTask);
                dataArray.add(player.playerTask.sideTask.level);
                String sideTask = dataArray.toJSONString();
                dataArray.clear();

                // data trứng bư
                if (player.mabuEgg != null) {
                    dataArray.add(player.mabuEgg.lastTimeCreate);
                    dataArray.add(player.mabuEgg.timeDone);
                }
                String mabuEgg = dataArray.toJSONString();
                dataArray.clear();

                // data bùa
                dataArray.add(player.charms.tdTriTue);
                dataArray.add(player.charms.tdManhMe);
                dataArray.add(player.charms.tdDaTrau);
                dataArray.add(player.charms.tdOaiHung);
                dataArray.add(player.charms.tdBatTu);
                dataArray.add(player.charms.tdDeoDai);
                dataArray.add(player.charms.tdThuHut);
                dataArray.add(player.charms.tdDeTu);
                dataArray.add(player.charms.tdTriTue3);
                dataArray.add(player.charms.tdTriTue4);
                String charm = dataArray.toJSONString();
                dataArray.clear();

                // data skill - chỉ save skills hợp lệ với gender
                JSONArray dataSkill = new JSONArray();
                int savedCount = 0;
                int filteredCount = 0;
                for (Skill skill : player.playerSkill.skills) {
                    if (skill == null || skill.template == null) {
                        continue;
                    }

                    int skillId = skill.template.id;
                    // Validate skill có thuộc gender không trước khi save
                    if (!utils.SkillUtil.isSkillValidForGender(skillId, player.gender) && skillId != -1) {
                        filteredCount++;
                        Logger.warning("Player " + player.name + " có skill không hợp lệ khi save: " + skillId
                                + " - sẽ bỏ qua");
                        continue;
                    }

                    dataSkill.add(skillId);
                    dataSkill.add(skill.point);
                    dataSkill.add(skill.lastTimeUseThisSkill);
                    dataSkill.add(skill.currLevel);
                    dataArray.add(dataSkill.toJSONString());
                    dataSkill.clear();
                    savedCount++;
                }

                if (filteredCount > 0) {
                    Logger.warning("Đã lọc " + filteredCount + " skill không hợp lệ khi save player " + player.name);
                }

                String skills = dataArray.toJSONString();
                dataArray.clear();
                dataArray.clear();

                // data skill shortcut - chỉ save shortcuts hợp lệ với gender
                for (int skillId : player.playerSkill.skillShortCut) {
                    // Validate shortcut skill có thuộc gender không
                    if (skillId == -1 || utils.SkillUtil.isSkillValidForGender(skillId, player.gender)) {
                        dataArray.add(skillId);
                    } else {
                        // Loại bỏ shortcut không hợp lệ
                        Logger.warning("Player " + player.name + " có skill shortcut không hợp lệ: " + skillId
                                + " - sẽ set thành -1");
                        dataArray.add(-1);
                    }
                }
                String skillShortcut = dataArray.toJSONString();
                dataArray.clear();

                String pet = dataArray.toJSONString();
                String petInfo;
                String petPoint;
                String petBody;
                String petSkill;

                // data pet
                if (player.pet != null) {
                    dataArray.add(player.pet.typePet);
                    dataArray.add(player.pet.gender);
                    dataArray.add(player.pet.name);
                    dataArray.add(player.fusion.typeFusion);
                    int timeLeftFusion = (int) (Fusion.TIME_FUSION
                            - (System.currentTimeMillis() - player.fusion.lastTimeFusion));
                    dataArray.add(timeLeftFusion < 0 ? 0 : timeLeftFusion);
                    dataArray.add(player.pet.status);
                    petInfo = dataArray.toJSONString();
                    dataArray.clear();

                    dataArray.add(player.pet.nPoint.limitPower);
                    dataArray.add(player.pet.nPoint.power);
                    dataArray.add(player.pet.nPoint.tiemNang);
                    dataArray.add(player.pet.nPoint.stamina);
                    dataArray.add(player.pet.nPoint.maxStamina);
                    dataArray.add(player.pet.nPoint.hpg);
                    dataArray.add(player.pet.nPoint.mpg);
                    dataArray.add(player.pet.nPoint.dameg);
                    dataArray.add(player.pet.nPoint.defg);
                    dataArray.add(player.pet.nPoint.critg);
                    dataArray.add(player.pet.nPoint.hp);
                    dataArray.add(player.pet.nPoint.mp);
                    petPoint = dataArray.toJSONString();
                    dataArray.clear();

                    JSONArray items = new JSONArray();
                    JSONArray options = new JSONArray();
                    JSONArray opt = new JSONArray();
                    for (Item item : player.pet.inventory.itemsBody) {
                        if (item.isNotNullItem()) {
                            dataItem.add(item.template.id);
                            dataItem.add(item.quantity);
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
                            dataItem.add(options.toJSONString());
                        }

                        dataItem.add(item.createTime);
                        dataItem.add(item.auditTraceId == null ? "" : item.auditTraceId);

                        items.add(dataItem.toJSONString());
                        dataItem.clear();
                        options.clear();
                    }
                    petBody = items.toJSONString();

                    JSONArray petSkills = new JSONArray();
                    for (Skill s : player.pet.playerSkill.skills) {
                        JSONArray pskill = new JSONArray();
                        if (s.skillId != -1) {
                            pskill.add(s.template.id);
                            pskill.add(s.point);
                            pskill.add(s.lastTimeUseThisSkill);
                            pskill.add(s.currLevel);
                        } else {
                            pskill.add(-1);
                            pskill.add(0);
                            pskill.add(0);
                            pskill.add(0);
                        }
                        petSkills.add(pskill.toJSONString());
                    }
                    petSkill = petSkills.toJSONString();

                    dataArray.add(petInfo);
                    dataArray.add(petPoint);
                    dataArray.add(petBody);
                    dataArray.add(petSkill);

                    pet = dataArray.toJSONString();
                }
                dataArray.clear();

                // data thưởng ngọc rồng đen
                for (int i = 0; i < player.rewardBlackBall.timeOutOfDateReward.length; i++) {
                    JSONArray dataBlackBall = new JSONArray();
                    dataBlackBall.add(player.rewardBlackBall.timeOutOfDateReward[i]);
                    dataBlackBall.add(player.rewardBlackBall.lastTimeGetReward[i]);
                    dataBlackBall.add(player.rewardBlackBall.quantilyBlackBall[i]);
                    dataArray.add(dataBlackBall.toJSONString());
                    dataBlackBall.clear();
                }
                String dataBlackBall = dataArray.toJSONString();
                dataArray.clear();

                // Ma Bao Ve
                dataArray.add(player.mbv);
                dataArray.add(player.baovetaikhoan);
                dataArray.add(player.mbvtime);
                String dataBVTK = dataArray.toJSONString();
                dataArray.clear();

                // Card
                String dataCard = JSONValue.toJSONString(player.Cards);

                // BDKB
                dataArray.add(player.timesPerDayBDKB);
                dataArray.add(player.lastTimeJoinBDKB);
                String dataBDKB = dataArray.toJSONString();
                dataArray.clear();

                // CDRD
                dataArray.add(player.joinCDRD);
                dataArray.add(player.lastTimeJoinCDRD);
                dataArray.add(player.talkToThuongDe);
                dataArray.add(player.talkToThanMeo);
                String dataCDRD = dataArray.toJSONString();
                dataArray.clear();

                // Nhận Thỏi Vàng
                dataArray.add(player.danhanthoivang);
                dataArray.add(player.lastRewardGoldBarTime);
                String dataNhanThoiVang = dataArray.toJSONString();
                dataArray.clear();

                // Rương Gỗ
                dataArray.add(player.levelWoodChest);
                dataArray.add(player.goldChallenge);
                dataArray.add(player.rubyChallenge);
                dataArray.add(player.lastTimeRewardWoodChest);
                dataArray.add(player.lastTimePKDHVT23);
                String dataRuongGo = dataArray.toJSONString();
                dataArray.clear();

                // Siêu thần thủy
                dataArray.add(player.winSTT);
                dataArray.add(player.lastTimeWinSTT);
                dataArray.add(player.callBossPocolo);
                String dataSieuThanThuy = dataArray.toJSONString();
                dataArray.clear();

                // Võ đài sinh tử
                dataArray.add(player.haveRewardVDST);
                dataArray.add(player.gemVoDaiSinhTu);
                dataArray.add(player.lastTimePKVoDaiSinhTu);
                dataArray.add(player.timePKVDST);
                String dataVoDaiSinhTu = dataArray.toJSONString();
                dataArray.clear();

                // Data item event
                dataArray.add(player.itemEvent.remainingTVGSCount);
                dataArray.add(player.itemEvent.lastTVGSTime);
                dataArray.add(player.itemEvent.remainingHHCount);
                dataArray.add(player.itemEvent.lastHHTime);
                dataArray.add(player.itemEvent.remainingBNCount);
                dataArray.add(player.itemEvent.lastBNTime);
                dataArray.add(player.itemEvent.lastLimitedItemDropTime);
                dataArray.add(player.itemEvent.toLimitedItemDropJson());
                String dataItemEvent = dataArray.toJSONString();
                dataArray.clear();

                // Data Luyện Tập
                dataArray.add(player.levelLuyenTap);
                dataArray.add(player.dangKyTapTuDong);
                dataArray.add(player.mapIdDangTapTuDong);
                dataArray.add(player.tnsmLuyenTap);
                if (player.isOffline) {
                    dataArray.add(player.lastTimeOffline);
                } else {
                    dataArray.add(System.currentTimeMillis());
                }
                dataArray.add(player.traning.getTop());
                dataArray.add(player.traning.getTime());
                dataArray.add(player.traning.getLastTime());
                dataArray.add(player.traning.getLastTop());
                dataArray.add(player.traning.getLastRewardTime());
                String dataLuyenTap = dataArray.toJSONString();
                dataArray.clear();

                // data nhiệm vụ bang hàng ngày
                dataArray
                        .add(player.playerTask.clanTask.template != null ? player.playerTask.clanTask.template.id : -1);
                dataArray.add(player.playerTask.clanTask.receivedTime);
                dataArray.add(player.playerTask.clanTask.count);
                dataArray.add(player.playerTask.clanTask.maxCount);
                dataArray.add(player.playerTask.clanTask.leftTask);
                dataArray.add(player.playerTask.clanTask.level);
                String clanTask = dataArray.toJSONString();
                dataArray.clear();

                // data vip
                dataArray.add(player.timesPerDayCuuSat);
                dataArray.add(player.lastTimeCuuSat);
                dataArray.add(player.nhanDeTuNangVIP);
                dataArray.add(player.nhanVangNangVIP);
                dataArray.add(player.nhanSKHVIP);
                dataArray.add(player.vip);
                dataArray.add(player.timevip);
                String dataVip = dataArray.toJSONString();
                dataArray.clear();

                dataArray.add(player.LearnSkill.Time);
                dataArray.add(player.LearnSkill.ItemTemplateSkillId);
                dataArray.add(player.LearnSkill.Potential);

                String LearnSkill = dataArray.toJSONString();
                dataArray.clear();
                // data achievement
                if (player.achievement != null) {
                    for (Template.AchievementQuest aq : player.achievement.getAchievementList()) {
                        JSONArray a = new JSONArray();
                        a.add(aq.completed);
                        a.add(aq.isRecieve);
                        dataArray.add(a.toJSONString());
                        a.clear();
                    }
                }
                String achievement = dataArray.toJSONString();
                dataArray.clear();

                // gift code
                for (String code : player.giftCode.rewards) {
                    dataArray.add(code);
                }
                String giftCode = dataArray.toJSONString();
                dataArray.clear();

                for (int idSkill : player.BoughtSkill) {
                    dataArray.add(idSkill);
                }
                String BoughtSkill = dataArray.toJSONString();
                dataArray.clear();

                // data event
                dataArray.add(player.eventPointType1);
                dataArray.add(player.eventPointType2);
                dataArray.add(player.eventPointType3);
                dataArray.add(player.eventPointType4);
                dataArray.add(player.eventPointType5);
                dataArray.add(player.eventPointType6);
                dataArray.add(player.checkDailyReward);
                dataArray.add(player.checkTopReward1);
                dataArray.add(player.checkTopReward2);
                dataArray.add(player.checkTopReward3);
                dataArray.add(player.eventTimeType1);
                dataArray.add(player.eventTimeType2);
                dataArray.add(player.lastTrungThuDropDay);
                dataArray.add(player.lastTrungThuDropBoot);
                dataArray.add(player.lastHalloweenDropDay);
                dataArray.add(player.lastHalloweenDropBoot);
                dataArray.add(player.lastGemDropDay);
                dataArray.add(player.lastGemDropBoot);
                dataArray.add(player.lastCandyResetDate != null ? player.lastCandyResetDate : "");
                org.json.simple.JSONArray candyArr = new org.json.simple.JSONArray();
                if (player.candyDeclinedNpcIds != null) {
                    for (Integer id : player.candyDeclinedNpcIds) {
                        candyArr.add(id);
                    }
                }
                dataArray.add(candyArr.toJSONString());
                String dataEvent = dataArray.toJSONString();
                dataArray.clear();

                String dataBadges = JSONValue.toJSONString(player.dataBadges);

                String dataTaskBadges = JSONValue.toJSONString(player.dataTaskBadges);

                String dataDailyGift = JSONValue.toJSONString(player.dailyGiftDao);

                JSONObject topRewardsJson = new JSONObject();
                for (Map.Entry<String, List<Item>> entry : player.topRewards.entrySet()) {
                    JSONArray itemsArray = new JSONArray();
                    for (Item item : entry.getValue()) {
                        JSONArray itemInfo = new JSONArray();
                        if (item.isNotNullItem()) {
                            itemInfo.add(item.template.id);
                            itemInfo.add(item.quantity);
                            JSONArray options = new JSONArray();
                            for (Item.ItemOption io : item.itemOptions) {
                                JSONArray opt = new JSONArray();
                                opt.add(io.optionTemplate.id);
                                opt.add(io.param);
                                options.add(opt.toJSONString());
                            }
                            itemInfo.add(options.toJSONString());
                        } else {
                            itemInfo.add(-1);
                            itemInfo.add(0);
                            itemInfo.add(new JSONArray().toJSONString());
                        }
                        itemInfo.add(item.createTime);
                        itemsArray.add(itemInfo.toJSONString());
                    }
                    topRewardsJson.put(entry.getKey(), itemsArray.toJSONString());
                }
                String dataTopReward = topRewardsJson.toJSONString();

                String query = "update player set head = ?, have_tennis_space_ship = ?, "
                        + "clan_id = ?, data_inventory = ?, data_location = ?, data_point = ?, data_magic_tree = ?, "
                        + "items_body = ?, items_bag = ?, items_box = ?,items_box1 = ?, items_box_lucky_round = ?, items_daban = ?, item_mails_box = ?, friends = ?, "
                        + "enemies = ?, data_intrinsic = ?, data_item_time = ?, data_task = ?, data_mabu_egg = ?, pet = ?, "
                        + "data_black_ball = ?, data_side_task = ?, data_charm = ?, skills = ?, skills_shortcut = ?, notify = ?, "
                        + "baovetaikhoan = ?, data_card = ?, lasttimepkcommeson = ?, bandokhobau = ?, doanhtrai = ?, conduongrandoc = ?, masterDoesNotAttack = ?, "
                        + "nhanthoivang = ?, ruonggo = ?, sieuthanthuy = ?, vodaisinhtu = ?, rongxuong = ?, data_item_event = ?, data_luyentap = ?, data_clan_task = ?, data_vip = ?, "
                        + "rank = ?, data_achievement = ?, giftcode = ?, event_point = ?, data_event = ?, dataBadges = ?, dataTaskBadges = ?, BoughtSkill = ?, LearnSkill = ?, dailyGift = ?, ghi_danh_point = ?, data_top = ? where id = ?";

                AlyraManager.executeUpdate(query,
                        player.head,
                        player.haveTennisSpaceShip,
                        (player.clan != null ? player.clan.id : -1),
                        inventory,
                        location,
                        point,
                        magicTree,
                        itemsBody,
                        itemsBag,
                        itemsBox,
                        itemsBox1,
                        itemsBoxLuckyRound,
                        itemsDaBan,
                        itemMailBox,
                        friend,
                        enemy,
                        intrinsic,
                        itemTime,
                        task,
                        mabuEgg,
                        pet,
                        dataBlackBall,
                        sideTask,
                        charm,
                        skills,
                        skillShortcut,
                        player.notify,
                        dataBVTK,
                        dataCard,
                        player.lastPkCommesonTime,
                        dataBDKB,
                        player.lastTimeJoinDT,
                        dataCDRD,
                        player.doesNotAttack,
                        dataNhanThoiVang,
                        dataRuongGo,
                        dataSieuThanThuy,
                        dataVoDaiSinhTu,
                        player.lastTimeShenronAppeared,
                        dataItemEvent,
                        dataLuyenTap,
                        clanTask,
                        dataVip,
                        player.superRank.rank,
                        achievement,
                        giftCode,
                        player.event.getEventPoint(),
                        dataEvent,
                        dataBadges,
                        dataTaskBadges,
                        BoughtSkill,
                        LearnSkill,
                        dataDailyGift,
                        player.ghiDanhPoint,
                        dataTopReward,
                        player.id);
                SuperRankDAO.updateData(player);
                if (player.isOffline) {
                    Logger.log(TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Player "
                            + player.name + " updated successfully! " + (System.currentTimeMillis() - st) + "ms\n");
                    player.dispose();
                } else {
                    Logger.rainbow(TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Player " + player.name
                            + " saved successfully! " + (System.currentTimeMillis() - st) + "ms");
                }
            } catch (Exception e) {
                Logger.logException(PlayerDAO.class, e, "Lỗi save player " + player.name);
            }

        }
    }

    public static boolean checkLogout(Connection con, Player player) {
        long lastTimeLogout = 0;
        long lastTimeLogin = 0;
        try {
            PreparedStatement ps = con.prepareStatement("select * from account where id = ? limit 1");
            ps.setInt(1, player.getSession().userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lastTimeLogout = rs.getTimestamp("last_time_logout").getTime();
                lastTimeLogin = rs.getTimestamp("last_time_login").getTime();
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
            return false;
        }
        return lastTimeLogout > lastTimeLogin;
    }

    public static boolean subcash(Player player, int num) {
        PreparedStatement ps = null;
        try (Connection con = AlyraManager.getConnection();) {
            if (player.getSession().cash >= num) {
            } else {
                return false;
            }
            ps = con.prepareStatement("update account set cash = cash - ? where id = ?");
            ps.setInt(1, num);
            ps.setInt(2, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().cash -= num;

        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi update vip " + player.name);
            return false;
        }
        return true;
    }

    public static boolean addvip(Player player, int num) {
        PreparedStatement ps = null;
        try (Connection con = AlyraManager.getConnection();) {
            if (player.getSession().vip < num) {
                return false;
            }
            ps = con.prepareStatement("update account set vip = vip - ? where id = ?");
            ps.setInt(1, num);
            ps.setInt(2, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().vip -= num;
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi update vip " + player.name);
            return false;
        }
        return true;
    }

    public static boolean ExistUsername(String username) {
        AlyraResultSet rs = null;
        try {
            rs = AlyraManager.executeQuery("SELECT * FROM `account` WHERE `username` = ?", username);
            return rs.first();
        } catch (Exception e) {
            Logger.error("Error checking username existence: " + e.getMessage() + "\n" + e.getStackTrace());
            return false;
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }

    public static boolean subvip(Player player, int num) {
        PreparedStatement ps = null;
        try (Connection con = AlyraManager.getConnection();) {
            if (player.getSession().vip < num) {
                return false;
            }

            // Cập nhật cơ sở dữ liệu sau khi kiểm tra
            ps = con.prepareStatement("update account set vip = vip - ? where id = ?");
            ps.setInt(1, num);
            ps.setInt(2, player.getSession().userId);
            ps.executeUpdate();

            // Cập nhật số lượng VIP trong session của người chơi
            player.getSession().vip -= num;

        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi update vip " + player.name);
            return false;
        }
        return true;
    }

    public static boolean subvnd(Player player, int num) {
        PreparedStatement ps = null;
        try (Connection con = AlyraManager.getConnection();) {
            if (player.getSession().vnd < num) {
                return false;
            }
            ps = con.prepareStatement("update account set vnd = vnd - ? where id = ?");
            ps.setInt(1, num);
            ps.setInt(2, player.getSession().userId);
            ps.executeUpdate();
            player.getSession().vnd -= num;

        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e, "Lỗi update vnd " + player.name);
            return false;
        }
        return true;
    }

    public static int addVnd(String characterName, int amount) {
        if (amount <= 0 || characterName == null || characterName.trim().isEmpty()) {
            return -1;
        }

        Connection con = null;
        PreparedStatement psSelect = null;
        PreparedStatement psUpdate = null;
        ResultSet rs = null;
        int accountId = -1;

        try {
            con = AlyraManager.getConnection();
            con.setAutoCommit(false);

            psSelect = con.prepareStatement("SELECT account_id FROM player WHERE LOWER(name) = LOWER(?)");
            psSelect.setString(1, characterName);
            rs = psSelect.executeQuery();

            if (rs.next()) {
                accountId = rs.getInt("account_id");
            } else {
                con.rollback();
                return -1;
            }

            if (accountId != -1) {
                psUpdate = con.prepareStatement("UPDATE account SET vnd = vnd + ?, danap = danap + ? WHERE id = ?");
                psUpdate.setInt(1, amount);
                psUpdate.setInt(2, amount);
                psUpdate.setInt(3, accountId);
                int rowsAffected = psUpdate.executeUpdate();

                if (rowsAffected > 0) {
                    con.commit();
                    return accountId;
                } else {
                    con.rollback();
                    return -1;
                }
            } else {
                con.rollback();
                return -1;
            }

        } catch (SQLException | RuntimeException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ignore) {
            }
            return -1;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (psSelect != null) {
                    psSelect.close();
                }
                if (psUpdate != null) {
                    psUpdate.close();
                }
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException ignore) {
            }
        }
    }

    public static void banAccount(MySession session, Player player) {
        PreparedStatement ps = null;
        try (Connection con = AlyraManager.getConnection();) {
            ps = con.prepareStatement("update account set ban = 1 where id = ? and username = ?");
            ps.setInt(1, player.getSession().userId);
            ps.setString(2, player.getSession().uu);
            ps.executeUpdate();
        } catch (Exception e) {
            Logger.logException(PlayerDAO.class, e);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
            }
        }
    }
}
