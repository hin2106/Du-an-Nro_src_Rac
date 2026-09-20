package server;

import radar.OptionCard;
import services.RadarService;
import radar.RadarCard;
import system.ConsignItem;
import managers.ConsignShopManager;
import data.AlyraManager;
import consts.ConstPlayer;
import consts.ConstMap;
import data.DataGame;
import system.Template.*;
import clan.Clan;
import clan.ClanMember;
import daos.ShopDAO;
import static data.DataGame.MAP_MOUNT_NUM;
import event.ConfigEvent;
import event.Event;
import system.GiftCode;
import managers.GiftCodeManager;
import intrinsic.Intrinsic;
import item.Item;
import item.Item.ItemOption;
import map.WayPoint;
import npc.Npc;
import npc.NpcFactory;
import shop.Shop;
import skill.NClass;
import skill.Skill;
import task.SideTaskTemplate;
import task.SubTaskMain;
import task.TaskMain;
import services.map.MapService;
import utils.Logger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import managers.boss.BossManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import npc.NonInteractiveNPC;
import player.Player;
import player.badges.BagesTemplate;
import task.BadgesTaskTemplate;
import task.ClanTaskTemplate;
import task.KolTaskTemplate;
import managers.TopRewardManager;
import services.map.EffectEventManager;
import services.map.MapUpdateOptimizer;
import services.map.RandomNpcSpawner;

public final class Manager {

    private static Manager instance;

    public static byte SERVER = 1;
    public static byte SECOND_WAIT_LOGIN = 5;
    public static int MAX_PER_IP = 10;
    public static int MAX_PLAYER = 2000;
    public static int RATE_EXP_SERVER = 1;
    public static boolean BEHIND_FIREWALL = false; // Khi true, bypass max per IP check vì tất cả IP đều giống nhau

    public static int EVENT_SEVER = 3; // doi id event o đây

    public static boolean LOCAL = false;
    public static boolean TEST = false;
    public static boolean DAO_AUTO_UPDATER = false;
    public static final String BOOT_TOKEN = Long.toString(System.currentTimeMillis());

    public static String TIME_VIP_START = "15/02/2025";
    public static String TIME_VIP_END = "15/03/2025";
    public static String START_DATE_EVENT = "01/12/2024";
    public static int HOUR_CHECK_END_EVENT = 23;
    public static int MINUTE_CHECK_END_EVENT = 59;
    public static String END_DATE_EVENT = "23/12/2025";
    public static int EVENT_TYPE;
    public static int AUTO_MAINTENANCE = 1;
    public static int AUTO_MAINTENANCE_HOUR = 1;
    public static int AUTO_MAINTENANCE_MINUTE = 1;
    public static boolean DUA_TOP = false;
    public static MapTemplate[] MAP_TEMPLATES;
    public static final List<map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final List<ArrHead2Frames> ARR_HEAD_2_FRAMES = new ArrayList<>();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<ClanTaskTemplate> CLAN_TASKS_TEMPLATE = new ArrayList<>();
    public static final Map<Integer, KolTaskTemplate> KOL_TASKS_TEMPLATE = new HashMap<>();
    public static final List<AchievementTemplate> ACHIEVEMENT_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<BgItem> BG_ITEMS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static List<Shop> SHOPS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static final List<BadgesTaskTemplate> TASKS_BADGES_TEMPLATE = new ArrayList<>();

    public static final List<BagesTemplate> BAGES_TEMPLATES = new ArrayList<>();

    public static Manager gI() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public ConfigEvent gameConfig;

    private Manager() {
        try {
            loadProperties();
            gameConfig = new ConfigEvent();
        } catch (IOException ex) {
            Logger.logException(Manager.class, ex, "Lỗi load properites");
            System.exit(0);
        }

        this.loadDatabase();
        NpcFactory.createNpcConMeo();
        NpcFactory.createNpcRongThieng();
        Event.switchEvent(EVENT_SEVER);
        if (Event.isEventActive()) {
            Event.getInstance().init();
        }
        this.initMap();
    }

    private void initMap() {
        int[][] tileTyleTop = readTileIndexTileType(ConstMap.TILE_TOP);

        for (MapTemplate mapTemp : MAP_TEMPLATES) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            map.Map map = new map.Map(
                    mapTemp.id,
                    mapTemp.name,
                    mapTemp.planetId,
                    mapTemp.tileId,
                    mapTemp.bgId,
                    mapTemp.bgType,
                    mapTemp.type,
                    tileMap,
                    tileTop,
                    mapTemp.zones,
                    mapTemp.maxPlayerPerZone,
                    mapTemp.wayPoints);
            MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
            // new Thread(map, "Update map " + map.mapName).start();
        }
        MapUpdateOptimizer.gI().start(1000); // Update mỗi 1000ms
        new NonInteractiveNPC().initNonInteractiveNPC();
        if (Event.isEventActive()) {
            Event.getInstance().initNpc();
            Logger.rainbow("[Event] Init Npc Event successfully");
        } else {
        }
        initDynamicNpcSpawner();
        Logger.rainbow("Initialize map successfully!");
    }

    private void initDynamicNpcSpawner() {
        try {
            // RandomNpcSpawner drMyuuSpawner = new RandomNpcSpawner(
            //         consts.ConstNpc.DR_MYUU,
            //         java.util.Arrays.asList(20, 19, 18), // các map spawn
            //         java.time.Duration.ofMinutes(10), // tồn tại
            //         java.time.Duration.ofMinutes(0) // nghỉ
            // );
            // drMyuuSpawner.start();

            RandomNpcSpawner calickSpawner = new RandomNpcSpawner(
                    consts.ConstNpc.CALICK,
                    java.util.Arrays.asList(27, 28, 29),
                    java.time.Duration.ofMinutes(5),
                    java.time.Duration.ofMinutes(0));
            calickSpawner.start();
            Logger.rainbow("Dynamic NPC spawners started successfully.");
        } catch (Exception e) {
            Logger.logException(getClass(), e);
        }
    }

    void loadDatabase() {
        long st = System.currentTimeMillis();
        JSONArray dataArray;
        JSONObject dataObject = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = AlyraManager.getConnection(); Connection conn = AlyraManager.getConnection_Data()) {
            ps = conn.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();

            while (rs.next()) {
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");

                dataArray = (JSONArray) JSONValue.parse(rs.getString("part").replaceAll("\\\"", ""));
                for (int j = 0; j < dataArray.size(); j++) {
                    JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    part.partDetails.add(new PartDetail(
                            Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                }
                parts.add(part);
            }
            File dir = new File("data/update_data");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Ghi file nhị phân
            File outputFile = new File(dir, "part");
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
                dos.writeShort(parts.size());
                for (Part part : parts) {
                    dos.writeByte(part.type);
                    for (PartDetail partDetail : part.partDetails) {
                        dos.writeShort(partDetail.iconId);
                        dos.writeByte(partDetail.dx);
                        dos.writeByte(partDetail.dy);
                    }
                }
                dos.flush();
                Logger.rainbow("Successfully loaded part (" + parts.size() + ")");
            } catch (Exception e) {
            }
            // load bg item template
            ps = conn.prepareStatement("select * from bg_item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BgItem bgItem = new BgItem();
                bgItem.id = rs.getInt("id");
                bgItem.layer = rs.getByte("layer");
                bgItem.dx = rs.getShort("dx");
                bgItem.dy = rs.getShort("dy");
                bgItem.idImage = rs.getShort("image_id");
                BG_ITEMS.add(bgItem);
            }
            Logger.rainbow("Successfully loaded bg item template (" + BG_ITEMS.size() + ")");

            // load array head 2 frames
            ps = conn.prepareStatement("select * from array_head_2_frames");
            rs = ps.executeQuery();
            while (rs.next()) {
                ArrHead2Frames arrHead2Frames = new ArrHead2Frames();
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data"));
                for (int i = 0; i < dataArray.size(); i++) {
                    arrHead2Frames.frames.add(Integer.valueOf(dataArray.get(i).toString()));
                }
                ARR_HEAD_2_FRAMES.add(arrHead2Frames);
            }
            Logger.rainbow("Successfully loaded arr head 2 frames (" + ARR_HEAD_2_FRAMES.size() + ")");

            // load clan
            ps = con.prepareStatement("select * from clan");
            rs = ps.executeQuery();
            while (rs.next()) {
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.name2 = rs.getString("name_2");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                if (clan.level < 1) {
                    clan.level = 1;
                }
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("members"));
                for (int i = 0; i < dataArray.size(); i++) {
                    dataObject = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(i)));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (NumberFormatException e) {
                    }
                    clan.addClanMember(cm);
                }
                dataArray.clear();
                CLANS.add(clan);
            }
            ps = con.prepareStatement("select id from clan order by id desc limit 1", 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
            if (rs.first()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }
            Logger.rainbow("Successfully loaded clan (" + CLANS.size() + "), clan next id: " + Clan.NEXT_ID);

            // load skill
            ps = conn.prepareStatement("select * from skill_template order by nclass_id, slot");
            rs = ps.executeQuery();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{"));

                for (int j = 0; j < dataArray.size(); j++) {
                    JSONObject dts = (JSONObject) JSONValue.parse(String.valueOf(dataArray.get(j)));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
                skillTemplate.skills = skillTemplate.skillss.toArray(new Skill[0]);
            }
            Logger.rainbow("Successfully loaded skill (" + NCLASS.size() + ")");

            // load head avatar
            ps = conn.prepareStatement("select * from head_avatar");
            rs = ps.executeQuery();
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                HEAD_AVATARS.add(headAvatar);
            }
            Logger.rainbow("Successfully loaded head avatar (" + HEAD_AVATARS.size() + ")");

            // load flag bag
            ps = conn.prepareStatement("select * from flag_bag");
            rs = ps.executeQuery();
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            Logger.rainbow("Successfully loaded flag bag (" + FLAGS_BAGS.size() + ")");

            // load intrinsic
            ps = conn.prepareStatement("select * from intrinsic");
            rs = ps.executeQuery();
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT ->
                        INTRINSIC_TD.add(intrinsic);
                    case ConstPlayer.NAMEC ->
                        INTRINSIC_NM.add(intrinsic);
                    case ConstPlayer.XAYDA ->
                        INTRINSIC_XD.add(intrinsic);
                    default -> {
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                    }
                }
                INTRINSICS.add(intrinsic);
            }
            Logger.rainbow("Successfully loaded intrinsic (" + INTRINSICS.size() + ")");

            // load task
            ps = conn.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id");
            rs = ps.executeQuery();
            int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail");
                    TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = rs.getString("sub_name");
                subTask.maxCount = rs.getShort("max_count");
                subTask.notify = rs.getString("notify");
                subTask.npcId = rs.getByte("npc_id");
                subTask.mapId = rs.getShort("map");
                task.subTasks.add(subTask);
            }
            Logger.rainbow("Successfully loaded task (" + TASKS.size() + ")");

            // load side task
            ps = conn.prepareStatement("select * from side_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            Logger.rainbow("Successfully loaded side task (" + SIDE_TASKS_TEMPLATE.size() + ")");

            // load kol task reward
            ps = conn.prepareStatement("SELECT * FROM kol_task_template WHERE active = 1");
            rs = ps.executeQuery();
            java.sql.ResultSetMetaData md = rs.getMetaData();
            boolean hasMin = false, hasMax = false, hasRewardVip = false, hasIsvip = false, hasIs_vip = false;
            for (int i = 1; i <= md.getColumnCount(); i++) {
                if ("max_count_min".equalsIgnoreCase(md.getColumnLabel(i))) {
                    hasMin = true;
                }
                if ("max_count_max".equalsIgnoreCase(md.getColumnLabel(i))) {
                    hasMax = true;
                }
                if ("reward_data_vip".equalsIgnoreCase(md.getColumnLabel(i))) {
                    hasRewardVip = true;
                }
                if ("isvip".equalsIgnoreCase(md.getColumnLabel(i))) {
                    hasIsvip = true;
                }
                if ("is_vip".equalsIgnoreCase(md.getColumnLabel(i))) {
                    hasIs_vip = true;
                }
            }
            while (rs.next()) {
                KolTaskTemplate temp = new KolTaskTemplate();
                temp.id = rs.getInt("id");
                temp.name = rs.getString("name");
                temp.description = rs.getString("description");
                temp.maxCount = rs.getInt("max_count");
                if (hasMin) {
                    temp.maxCountMin = rs.getInt("max_count_min");
                }
                if (hasMax) {
                    temp.maxCountMax = rs.getInt("max_count_max");
                }
                temp.rewardData = rs.getString("reward_data");
                temp.rewardDataVip = hasRewardVip ? rs.getString("reward_data_vip") : null;
                temp.active = rs.getBoolean("active");
                temp.isVip = hasIsvip ? rs.getBoolean("isvip") : (hasIs_vip && rs.getBoolean("is_vip"));
                Manager.KOL_TASKS_TEMPLATE.put(temp.id, temp);
            }

            // load task badges
            ps = con.prepareStatement("select * from task_badges_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                BadgesTaskTemplate badgesTaskTemplate = new BadgesTaskTemplate();
                badgesTaskTemplate.id = rs.getInt("id");
                badgesTaskTemplate.name = rs.getString("NAME");
                badgesTaskTemplate.count = rs.getInt("maxCount");
                badgesTaskTemplate.idbadgesReward = rs.getInt("idbadgesReward");
                TASKS_BADGES_TEMPLATE.add(badgesTaskTemplate);
            }
            Logger.rainbow("Successfully loaded task badges (" + TASKS_BADGES_TEMPLATE.size() + ")");

            // load clan task
            ps = conn.prepareStatement("select * from clan_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ClanTaskTemplate clanTask = new ClanTaskTemplate();
                clanTask.id = rs.getInt("id");
                clanTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                clanTask.count[0][0] = Integer.parseInt(mc1[0]);
                clanTask.count[0][1] = Integer.parseInt(mc1[1]);
                clanTask.count[1][0] = Integer.parseInt(mc2[0]);
                clanTask.count[1][1] = Integer.parseInt(mc2[1]);
                clanTask.count[2][0] = Integer.parseInt(mc3[0]);
                clanTask.count[2][1] = Integer.parseInt(mc3[1]);
                clanTask.count[3][0] = Integer.parseInt(mc4[0]);
                clanTask.count[3][1] = Integer.parseInt(mc4[1]);
                clanTask.count[4][0] = Integer.parseInt(mc5[0]);
                clanTask.count[4][1] = Integer.parseInt(mc5[1]);
                CLAN_TASKS_TEMPLATE.add(clanTask);
            }
            Logger.rainbow("Successfully loaded clan task (" + CLAN_TASKS_TEMPLATE.size() + ")");

            ps = conn.prepareStatement("select * from achievement_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ACHIEVEMENT_TEMPLATE.add(new AchievementTemplate(rs.getString("info1"), rs.getString("info2"),
                        rs.getInt("money"), rs.getLong("max_count")));
            }
            Logger.rainbow("Successfully loaded achievement (" + ACHIEVEMENT_TEMPLATE.size() + ")");
            int batchSize = 750;
            int offset = 0;
            try {
                while (true) {
                    ps = conn.prepareStatement("SELECT * FROM item_template LIMIT ? OFFSET ?");
                    ps.setInt(1, batchSize);
                    ps.setInt(2, offset);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        break;
                    }

                    do {
                        ItemTemplate itemTemp = new ItemTemplate();
                        itemTemp.id = rs.getShort("id");
                        itemTemp.type = rs.getByte("type");
                        itemTemp.gender = rs.getByte("gender");
                        itemTemp.name = rs.getString("name");
                        itemTemp.description = rs.getString("description");
                        itemTemp.level = rs.getByte("level");
                        itemTemp.iconID = rs.getShort("icon_id");
                        itemTemp.part = rs.getShort("part");
                        itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                        itemTemp.strRequire = rs.getInt("power_require");
                        itemTemp.gold = rs.getInt("gold");
                        itemTemp.gem = rs.getInt("gem");
                        itemTemp.head = rs.getInt("head");
                        itemTemp.body = rs.getInt("body");
                        itemTemp.leg = rs.getInt("leg");

                        ITEM_TEMPLATES.add(itemTemp);
                    } while (rs.next());
                    offset += batchSize;
                }

                Logger.rainbow("Successfully loaded map item template (" + ITEM_TEMPLATES.size() + " items)");

            } catch (SQLException e) {
                Logger.error("Error loading item templates: " + e.getMessage());
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException e) {
                    Logger.error("Error closing resources: " + e.getMessage());
                }
            }

            ps = conn.prepareStatement("select id, name from item_option_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            Logger.rainbow("Successfully loaded map item option template (" + ITEM_OPTION_TEMPLATES.size() + ")");

            // load shop
            SHOPS = ShopDAO.getShops(conn);
            Logger.rainbow("Successfully loaded shop (" + SHOPS.size() + ")");

            // load notify
            ps = conn.prepareStatement("select * from notify order by id desc");
            rs = ps.executeQuery();
            while (rs.next()) {
                NOTIFY.add(rs.getString("name") + "<>" + rs.getString("text"));
            }
            Logger.rainbow("Successfully loaded notify (" + NOTIFY.size() + ")");

            // load image by name
            ps = conn.prepareStatement("select name, n_frame from img_by_name");
            rs = ps.executeQuery();
            while (rs.next()) {
                IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            Logger.rainbow("Successfully loaded images by name (" + IMAGES_BY_NAME.size() + ")");

            // Load mount
            for (ItemTemplate item : ITEM_TEMPLATES) {
                if (item.type == 24 && getNFrameImageByName("mount_" + item.part + "_0") != 0) {
                    MAP_MOUNT_NUM.put(item.id, (short) (item.part + 30000));
                }
            }
            Logger.rainbow("Successfully loaded mount (" + MAP_MOUNT_NUM.size() + ")");

            // Load item ki gui
            ps = con.prepareStatement("SELECT * FROM shop_ky_gui");
            rs = ps.executeQuery();
            int skippedItems = 0;
            while (rs.next()) {
                try {
                    int i = rs.getInt("id");
                    int idPl = rs.getInt("player_id");
                    String playerName = rs.getString("player_name");
                    byte tab = rs.getByte("tab");
                    short itemId = rs.getShort("item_id");
                    int gold = rs.getInt("gold");
                    int gem = rs.getInt("gem");
                    int quantity = rs.getInt("quantity");
                    long isTime = rs.getLong("lastTime");
                    boolean isBuy = rs.getByte("isBuy") == 1;
                    if (gold < 0)
                        gold = -1;
                    if (gem < 0)
                        gem = -1;
                    if (quantity < 0)
                        quantity = 1;

                    if (gold > 0 && gem > 0) {
                        Logger.logln("WARNING: ConsignItem ID=" + i + " has both gold (" + gold
                                + ") and gem (" + gem + "). Keeping GEM only.");
                        gold = -1;
                    }
                    // Parse item options
                    List<Item.ItemOption> op = new ArrayList<>();
                    JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                    if (jsa2 != null) {
                        for (int j = 0; j < jsa2.size(); ++j) {
                            JSONObject jso2 = (JSONObject) jsa2.get(j);
                            int idOptions = Integer.parseInt(jso2.get("id").toString());
                            int param = Integer.parseInt(jso2.get("param").toString());
                            op.add(new Item.ItemOption(idOptions, param));
                        }
                    }
                    ConsignShopManager.gI().listItem
                            .add(new ConsignItem(i, itemId, idPl, playerName, tab, gold, gem, quantity, isTime, op, isBuy));

                } catch (Exception e) {
                    skippedItems++;
                    Logger.logException(Manager.class, e, "Error loading ConsignItem ID=" + rs.getInt("id"));
                }
            }
            Logger.rainbow("Successfully loaded Consign Item (" + ConsignShopManager.gI().listItem.size() + ")"
                    + (skippedItems > 0 ? " - Skipped " + skippedItems + " invalid items" : ""));

            // load mob template
            ps = conn.prepareStatement("select * from mob_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getByte("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                MOB_TEMPLATES.add(mobTemp);
            }
            Logger.rainbow("Successfully loaded mob template (" + MOB_TEMPLATES.size() + ")");

            // load npc template
            ps = conn.prepareStatement("select * from npc_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                NPC_TEMPLATES.add(npcTemp);
            }
            Logger.rainbow("Successfully loaded npc template (" + NPC_TEMPLATES.size() + ")");
            ps = conn.prepareStatement("select * from data_badges");
            rs = ps.executeQuery();
            while (rs.next()) {
                BagesTemplate template = new BagesTemplate();
                template.id = rs.getInt("id");
                template.idEffect = rs.getInt("idEffect");
                template.idItem = rs.getInt("idItem");
                template.NAME = rs.getString("NAME");

                JSONArray option = (JSONArray) JSONValue.parse(rs.getString("Options"));
                ;
                if (option != null) {
                    for (int u = 0; u < option.size(); u++) {
                        JSONObject jsonobject = (JSONObject) option.get(u);
                        int optionId = Integer.parseInt(jsonobject.get("id").toString());
                        int param = Integer.parseInt(jsonobject.get("param").toString());
                        template.options.add(new Item.ItemOption(optionId, param));
                    }
                }
                BAGES_TEMPLATES.add(template);
            }
            Logger.rainbow("Successfully loaded badges template (" + BAGES_TEMPLATES.size() + ")");
            // load map template
            ps = conn.prepareStatement("select count(id) from map_template", 
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ps.executeQuery();
            if (rs.first()) {
                int countRow = rs.getShort(1);
                MAP_TEMPLATES = new MapTemplate[countRow];
                ps = conn.prepareStatement("select * from map_template");
                rs = ps.executeQuery();
                short i = 0;
                while (rs.next()) {
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = rs.getInt("id");
                    String mapName = rs.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    mapTemplate.type = rs.getByte("type");
                    mapTemplate.planetId = rs.getByte("planet_id");
                    mapTemplate.bgType = rs.getByte("bg_type");
                    mapTemplate.tileId = rs.getByte("tile_id");
                    mapTemplate.bgId = rs.getByte("bg_id");
                    mapTemplate.zones = rs.getByte("zones");
                    mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
                    // load waypoints
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ","));
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    // load mobs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("mobs").replaceAll("\\\"", ""));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new long[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        mapTemplate.mobHp[j] = Long.parseLong(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    // load npcs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("npcs").replaceAll("\\\"", ""));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        dtn.clear();
                    }
                    dataArray.clear();
                    ;
                    MAP_TEMPLATES[i++] = mapTemplate;
                }
                Logger.success("Successfully loaded map template (" + MAP_TEMPLATES.length + ")\n");
            }

            ps = conn.prepareStatement("select * from radar");
            rs = ps.executeQuery();
            while (rs.next()) {
                RadarCard rd = new RadarCard();
                rd.Id = rs.getShort("id");
                rd.IconId = rs.getShort("iconId");
                rd.Rank = rs.getByte("rank");
                rd.Max = rs.getByte("max");
                rd.Type = rs.getByte("type");
                rd.Template = rs.getShort("template");
                rd.Name = rs.getString("name");
                rd.Info = rs.getString("info");
                JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("body"));
                rd.PartsByLevel.clear();
                rd.AuraByLevel.clear();
                rd.IconByLevel.clear();
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        short h = Short.parseShort(ob.get("head").toString());
                        short b = Short.parseShort(ob.get("body").toString());
                        short l = Short.parseShort(ob.get("leg").toString());
                        short g = Short.parseShort(ob.get("bag").toString());
                        rd.PartsByLevel.add(new short[] { h, b, l, g });
                        Object auraVal = ob.get("aura");
                        rd.AuraByLevel.add(auraVal != null ? Short.parseShort(auraVal.toString()) : rd.AuraId);
                        Object iconVal = ob.get("icon");
                        rd.IconByLevel.add(iconVal != null ? Short.parseShort(iconVal.toString()) : rd.IconId);
                    }
                }
                if (!rd.PartsByLevel.isEmpty()) {
                    rd.Head = rd.PartsByLevel.get(0)[0];
                    rd.Body = rd.PartsByLevel.get(0)[1];
                    rd.Leg = rd.PartsByLevel.get(0)[2];
                    rd.Bag = rd.PartsByLevel.get(0)[3];
                }
                rd.Options.clear();
                arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject ob = (JSONObject) arr.get(i);
                    if (ob != null) {
                        rd.Options.add(new OptionCard(Integer.parseInt(ob.get("id").toString()),
                                Short.parseShort(ob.get("param").toString()),
                                Byte.parseByte(ob.get("activeCard").toString())));
                    }
                }
                rd.Require = rs.getShort("require");
                rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
            Logger.rainbow("Successfully loaded radar template (" + RadarService.gI().RADAR_TEMPLATE.size() + ")");

            // Load giftcode
            ps = con.prepareStatement("SELECT * FROM giftcode");
            rs = ps.executeQuery();
            while (rs.next()) {
                GiftCode giftcode = new GiftCode();
                giftcode.code = rs.getString("code");
                giftcode.id = rs.getInt("id");
                giftcode.countLeft = rs.getInt("count_left");
                if (giftcode.countLeft == -1) {
                    giftcode.countLeft = 999999999;
                }
                giftcode.datecreate = rs.getTimestamp("datecreate");
                giftcode.dateexpired = rs.getTimestamp("expired");
                JSONArray jar = (JSONArray) JSONValue.parse(rs.getString("detail"));
                if (jar != null) {
                    for (int i = 0; i < jar.size(); ++i) {
                        JSONObject jsonObj = (JSONObject) jar.get(i);

                        int id = Integer.parseInt(jsonObj.get("id").toString());
                        int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                        JSONArray option = (JSONArray) jsonObj.get("options");
                        ArrayList<ItemOption> optionList = new ArrayList<>();

                        if (option != null) {
                            for (int u = 0; u < option.size(); u++) {
                                JSONObject jsonobject = (JSONObject) option.get(u);
                                int optionId = Integer.parseInt(jsonobject.get("id").toString());
                                int param = Integer.parseInt(jsonobject.get("param").toString());
                                optionList.add(new Item.ItemOption(optionId, param));
                            }
                        }

                        giftcode.option.put(id, optionList);
                        giftcode.detail.put(id, quantity);
                    }
                }

                GiftCodeManager.gI().listGiftCode.add(giftcode);
            }
            Logger.rainbow("Successfully loaded giftcode (" + GiftCodeManager.gI().listGiftCode.size() + ")");

            TopRewardManager.gI().reload();
            Logger.rainbow("Successfully loaded gift_top (" + TopRewardManager.gI().countCfg() + ")");

            File directory = new File("data/icon/x4");
            if (directory.isDirectory()) {
                Optional<File> maxFile = Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                        .filter(File::isFile)
                        .filter(file -> file.getName().endsWith(".png"))
                        .max(Comparator.comparingInt(file -> {
                            String name = file.getName();
                            return Integer.parseInt(name.substring(0, name.length() - 4));
                        }));
                if (maxFile.isPresent()) {
                    String fileName = maxFile.get().getName();
                    short maxVersion = Short.parseShort(fileName.substring(0, fileName.length() - 4));
                    DataGame.maxSmallVersion = (short) (maxVersion + 1);
                    Logger.rainbow("Successfully loaded max small version (" + DataGame.maxSmallVersion + ")");
                }
            }

            DataGame.warmAssetVersionCaches();

            Player pl = null;
            BossManager bossManager = BossManager.gI();
            bossManager.loadBoss(pl);
            Logger.rainbow("Successfully loaded bosses (" + bossManager.getBossCount() + ")");
            EffectEventManager.gI().load();
        } catch (Exception e) {
            Logger.logException(Manager.class,
                    e, "Database loading error");
            System.exit(0);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                AlyraManager.close_data();
            } catch (SQLException e) {
            }
        }

        dragonpass.DragonPassService.gI().initialize();
        Logger.log("Total database loading time: " + (System.currentTimeMillis() - st) + " (ms)\n");

    }

    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("data/config/sever.properties"));
        Object value;
        if ((value = properties.get("server.sv")) != null) {
            SERVER = Byte.parseByte(String.valueOf(value));
        }
        String serverName = properties.getProperty("server.name");
        if (serverName != null) {
            ServerManager.NAME = serverName;
        }
        if ((value = properties.get("server.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        String bindIp = properties.getProperty("server.bind.ip");
        if (bindIp != null && !bindIp.trim().isEmpty()) {
            ServerManager.BIND_IP = bindIp.trim();
        } else {
            ServerManager.BIND_IP = null; // Bind to all interfaces
        }
        String linkServer = "";
        value = properties.get("server.ip");
        if (value != null) {
            ServerManager.IP = String.valueOf(value);
            linkServer = ServerManager.NAME + ":" + ServerManager.IP + ":" + ServerManager.PORT + ":0";
        } else {
            for (int i = 1; i <= 10; i++) {
                value = properties.get("server.sv" + i);
                if (value != null) {
                    linkServer += String.valueOf(value) + ":0,";
                }
            }
        }
        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.waitlogin")) != null) {
            SECOND_WAIT_LOGIN = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.maxperip")) != null) {
            MAX_PER_IP = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.maxplayer")) != null) {
            MAX_PLAYER = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.expserver")) != null) {
            RATE_EXP_SERVER = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.behindfirewall")) != null) {
            BEHIND_FIREWALL = String.valueOf(value).toLowerCase().equals("true");
            if (BEHIND_FIREWALL) {
                Logger.log("Server running behind firewall - max per IP check will be bypassed\n");
            }
        }
        if ((value = properties.get("server.usenettyproxy")) != null) {
            ServerManager.USE_NETTY_PROXY = String.valueOf(value).toLowerCase().equals("true");
            Logger.log("Netty DDoS Proxy: " + (ServerManager.USE_NETTY_PROXY ? "ENABLED" : "DISABLED") + "\n");
        }
        if ((value = properties.get("server.backendport")) != null) {
            ServerManager.BACKEND_PORT = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.local")) != null) {
            LOCAL = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.test")) != null) {
            TEST = String.valueOf(value).toLowerCase().equals("true");
        }
        if ((value = properties.get("server.daoautoupdater")) != null) {
            DAO_AUTO_UPDATER = String.valueOf(value).equalsIgnoreCase("true");
        }
        if ((value = properties.get("auto.maintenance")) != null) {
            AUTO_MAINTENANCE = Integer.parseInt(String.valueOf(value).trim());
        }
        if ((value = properties.get("auto.maintenance.hour")) != null) {
            AUTO_MAINTENANCE_HOUR = Integer.parseInt(String.valueOf(value).trim());
        }
        if ((value = properties.get("auto.maintenance.minute")) != null) {
            AUTO_MAINTENANCE_MINUTE = Integer.parseInt(String.valueOf(value).trim());
        }
    }

    private int[][] readTileIndexTileType(int tileTypeFocus) {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == tileTypeFocus) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == tileTypeFocus) {
                            tileIndexTileType[i][k] = typeIndex;

                        }
                    }
                }
            }
        } catch (IOException e) {
            Logger.logException(MapService.class,
                    e);
        }
        return tileIndexTileType;
    }

    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            try (DataInputStream dis = new DataInputStream(new FileInputStream("data/map/tile_map_data/" + mapId))) {
                int w = dis.readByte();
                int h = dis.readByte();
                tileMap = new int[h][w];
                for (int[] tm : tileMap) {
                    for (int j = 0; j < tm.length; j++) {
                        tm[j] = dis.readByte();
                    }
                }
            }
        } catch (IOException e) {
        }
        return tileMap;
    }

    public static Clan getClanById(int id) throws Exception {
        for (Clan clan : CLANS) {
            if (clan.id == id) {
                return clan;
            }
        }
        throw new Exception("Không tìm thấy clan id: " + id);
    }

    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    public static int getNumClan() {
        return CLANS.size();

    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }

}
