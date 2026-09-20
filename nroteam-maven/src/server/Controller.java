package server;

import audit.AssetAuditService;

import boss.Boss;
import managers.boss.BossManager;
import managers.boss.BrolyManager;
import managers.boss.AnTromManager;
import managers.boss.Boss12hManager;
import managers.boss.Boss14hManager;
import managers.boss.TrungThuEventManager;
import managers.boss.HalloweenEventManager;
import consts.ConstAchievement;
import services.ConsignShopService;
import services.player.ClanService;
import services.ChatGlobalService;
import services.SubMenuService;
import services.Service;
import services.player.IntrinsicService;
import services.FlagBagService;
import services.ItemTimeService;
import services.SkillService;
import services.map.NpcService;
import services.TaskService;
import services.map.ItemMapService;
import services.player.PlayerService;
import services.player.FriendAndEnemyService;
import data.AlyraManager;
import data.AlyraResultSet;
import consts.ConstIgnoreName;
import consts.ConstMap;
import utils.Util;
import data.DataGame;
import network.MySession;
import java.io.IOException;
import services.map.ChangeMapService;
import services.func.UseItem;
import services.func.Input;
import consts.ConstNpc;
import consts.ConstTask;
import data.ItemData;
import radar.Card;
import services.RadarService;
import services.map.NpcManager;
import player.Player;
import player.badges.BadgesService;
import matches.PVPService;
import services.AchievementService;
import services.ShopService;
import interfaces.IMessageHandler;
import network.Message;
import interfaces.ISession;
import services.dungeon.BlackBallWarService;
import services.dungeon.TrainingService;
import services.map.MapService;
import map.Zone;
import combine.CombineService;
import daos.PlayerDAO;
import daos.SuperRankDAO;
import managers.boss.ChristmasEventManager;
import network.SessionManager;
import player.PlayerManager;
import services.func.EffectMapService;
import services.func.LuckyRound;
import services.func.TransactionService;
import services.player.DailyGiftService;
import services.tournament.SuperRankService;
import skill.Skill;
import utils.Logger;

public class Controller implements IMessageHandler {

    private int errors;

    private static Controller instance;

    public static Controller gI() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }

    @Override
    public void onMessage(ISession s, Message _msg) {
        long st = System.currentTimeMillis();
        MySession _session = (MySession) s;
        Player player = null;
        long auditGoldBefore = Long.MIN_VALUE;
        int auditGemBefore = Integer.MIN_VALUE;
        int auditRubyBefore = Integer.MIN_VALUE;
        int auditGoldBarBefore = Integer.MIN_VALUE;
        byte auditCommand = _msg.command;
        try {
            player = _session.player;
            if (player != null && player.inventory != null) {
                auditGoldBefore = player.inventory.gold;
                auditGemBefore = player.inventory.gem;
                auditRubyBefore = player.inventory.ruby;
                auditGoldBarBefore = countItem(player, 457);
            }
            byte cmd = _msg.command;
            switch (cmd) {
                case 75 -> {
                    if (player != null) {
                        long token = _msg.reader().readLong();
                        Message response = new Message(75);
                        try {
                            response.writer().writeLong(token);
                            _session.sendMessage(response);
                        } finally {
                            response.cleanup();
                        }
                    }
                }
                case -100 -> {
                    if (player == null) {
                        return;
                    }
                    if (TransactionService.gI().check(player)) {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                        return;
                    }
                    if (player.baovetaikhoan) {
                        Service.gI().sendThongBao(player, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                        return;
                    }
                    byte action = _msg.reader().readByte();
                    switch (action) {
                        case 0 -> {
                            short idItem = _msg.reader().readShort();
                            byte moneyType = _msg.reader().readByte();
                            int money = _msg.reader().readInt();
                            int quantity;
                            if (player.getSession().version >= 222) {
                                quantity = _msg.reader().readInt();
                            } else {
                                quantity = _msg.reader().readByte();
                            }
                            if (quantity > 0) {
                                ConsignShopService.gI().KiGui(player, idItem, money, moneyType, quantity);
                            }
                        }
                        case 1, 2 -> {
                            short idItem = _msg.reader().readShort();
                            ConsignShopService.gI().claimOrDel(player, action, idItem);
                        }
                        case 3 -> {
                            short idItem = _msg.reader().readShort();
                            _msg.reader().readByte();
                            _msg.reader().readInt();
                            ConsignShopService.gI().buyItem(player, idItem);
                        }
                        case 4 -> {
                            byte moneyType = _msg.reader().readByte();
                            int money = _msg.reader().readByte();
                            ConsignShopService.gI().openShopKyGui(player, moneyType, money);
                        }
                        case 5 -> {
                            short idItem = _msg.reader().readShort();
                            ConsignShopService.gI().upItemToTop(player, idItem);
                        }
                        default ->
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                }

                case 127 -> {
                    if (player != null) {
                        byte actionRadar = _msg.reader().readByte();
                        switch (actionRadar) {
                            case 0 ->
                                RadarService.gI().sendRadar(player, player.Cards);
                            case 1 -> {
                                short idC = _msg.reader().readShort();
                                Card card = player.Cards.stream().filter(r -> r != null && r.Id == idC).findFirst()
                                        .orElse(null);
                                if (card != null) {
                                    if (card.Level == 0) {
                                        return;
                                    }

                                    if (card.Used == 0) {
                                        if (player.Cards.stream().anyMatch(c -> c != null && c.Used == 1)) {
                                            Service.gI().sendThongBaoFromAdmin(player, "Số thẻ sử dụng đã đạt tối đa");
                                            return;
                                        }
                                        card.Used = 1;
                                    } else {
                                        card.Used = 0;
                                    }

                                    RadarService.gI().Radar1(player, idC, card.Used);
                                    Service.gI().point(player);
                                    player.updateAura();
                                }
                            }

                        }
                    }
                }
                case -105 -> {
                    if (player != null) {
                        if (player.type == 0 && player.maxTime == 30) {
                            ChangeMapService.gI().changeMapBySpaceShip(player, 102, -1, Util.nextInt(60, 200));
                            player.idMark.setGotoFuture(false);
                        } else if (player.type == 1 && player.maxTime == 5) {
                            if (player.idMark != null && player.idMark.isGoToBDKB()) {
                                ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 135, -1),
                                        35, 35);
                                player.idMark.setGoToBDKB(false);
                            }
                        } else if (player.type == 2 && player.maxTime == 5) {
                            if (MapService.gI().isMapHanhTinhThucVat(player.zone.map.mapId)) {
                                ChangeMapService.gI().changeMap(player, 80, -1, -1, 5);
                            } else {
                                ChangeMapService.gI().changeMap(player, 160, -1, -1, 5);
                            }
                        } else if (player.type == 3 && player.maxTime == 5) {
                            ChangeMapService.gI().changeMap(player, player.idMark.getZoneKhiGasHuyDiet(),
                                    player.idMark.getXMapKhiGasHuyDiet(), player.idMark.getYMapKhiGasHuyDiet());
                            player.idMark.setZoneKhiGasHuyDiet(null);
                        } else if (player.type == 4 && player.maxTime == 5) {
                            if (player.idMark != null && player.idMark.isGoToKGHD()) {
                                ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 149, -1),
                                        100 + (Util.nextInt(-10, 10)), 336);
                                player.idMark.setGoToKGHD(false);
                            }
                        } else if (player.type == 5 && player.maxTime == 5) {
                            ChangeMapService.gI().changeMap(player, MapService.gI().getMapCanJoin(player, 156, -1),
                                    100 + (Util.nextInt(-10, 10)), 336);
                        }
                    }
                }
                case -127 -> {
                    if (player != null) {
                        LuckyRound.gI().readOpenBall(player, _msg);
                    }
                }
                case -125 -> {
                    if (player != null) {
                        Input.gI().doInput(player, _msg);
                    }
                }
                case 112 -> {
                    if (player != null) {
                        IntrinsicService.gI().showMenu(player);
                    }
                }
                case -34 -> {
                    if (player != null) {
                        switch (_msg.reader().readByte()) {
                            case 1 ->
                                player.magicTree.openMenuTree();
                            case 2 ->
                                player.magicTree.loadMagicTree();
                        }
                    }
                }
                case 42 ->
                    Service.gI().regisAccount(_session, _msg);
                case 3 -> {
                }
                case -99 -> {
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerEnemy(player, _msg);
                    }
                }
                case 18 -> {
                    if (player != null) {
                        player.changeMapVIP = true;
                        FriendAndEnemyService.gI().goToPlayerWithYardrat(player, _msg);
                    }
                }
                case -72 -> {
                    if (player != null) {
                        FriendAndEnemyService.gI().chatPrivate(player, _msg);
                    }
                }
                case -80 -> {
                    if (player != null) {
                        FriendAndEnemyService.gI().controllerFriend(player, _msg);
                    }
                }
                case -59 -> {
                    if (player != null) {
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player,
                                    "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        PVPService.gI().controllerThachDau(player, _msg);
                    }
                }
                case -86 -> {
                    if (player != null) {
                        TransactionService.gI().controller(player, _msg);
                    }
                }
                case -107 -> {
                    if (player != null) {
                        Service.gI().showInfoPet(player);
                    }
                }
                case -108 -> {
                    if (player != null && player.pet != null) {
                        player.pet.changeStatus(_msg.reader().readByte());
                    }
                }
                case 6 -> {
                    // buy item
                    if (player != null && !Maintenance.isRunning()) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player,
                                    "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        byte typeBuy = _msg.reader().readByte();
                        int tempId = _msg.reader().readShort();
                        ShopService.gI().takeItem(player, typeBuy, tempId);
                    }
                }
                case 7 -> {
                    byte action;
                    // sell item
                    if (player != null && !Maintenance.isRunning()) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player,
                                    "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        action = _msg.reader().readByte();
                        if (action == 0) {
                            ShopService.gI().showConfirmSellItem(player, _msg.reader().readByte(),
                                    _msg.reader().readShort());
                        } else {
                            ShopService.gI().sellItem(player, _msg.reader().readByte(),
                                    _msg.reader().readShort());
                        }
                    }
                }
                case 29 -> {
                    if (player != null) {
                        ChangeMapService.gI().openZoneUI(player);
                    }
                }
                case 21 -> {
                    if (player != null) {
                        int zoneId = _msg.reader().readByte();
                        ChangeMapService.gI().changeZone(player, zoneId);
                    }
                }
                case -71 -> {
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        ChatGlobalService.gI().chat(player, _msg.reader().readUTF());
                    }
                }
                case -79 -> {
                    if (player != null) {
                        Service.gI().getPlayerMenu(player, _msg.reader().readInt());
                    }
                }
                case -113 -> {
                    if (player != null && player.playerSkill != null) {
                        for (int i = 0; i < 10; i++) {
                            try {
                                player.playerSkill.skillShortCut[i] = _msg.reader().readByte();
                            } catch (IOException e) {
                                player.playerSkill.skillShortCut[i] = -1;
                            }
                        }
                        player.playerSkill.sendSkillShortCut();
                    }
                }
                case -103 -> {
                    if (player != null) {
                        byte act = _msg.reader().readByte();
                        switch (act) {
                            case 0 -> {
                                Service.gI().openFlagUI(player);
                            }
                            case 1 -> {
                                byte flagType = _msg.reader().readByte();
                                Service.gI().chooseFlag(player, flagType);
                            }
                        }
                    }
                }
                case -101 -> {
                    if (player == null) {
                        login2(_session, _msg);
                    }
                }

                case -7 -> {
                    if (player != null) {
                        if (player.isDie()) {
                            Service.gI().charDie(player);
                            return;
                        }
                        if (player.effectSkill.isHaveEffectSkill()) {
                            return;
                        }
                        int toX = player.location.x;
                        int toY = player.location.y;
                        try {
                            byte b = _msg.reader().readByte();
                            toX = _msg.reader().readShort();
                            try {
                                toY = _msg.reader().readShort();
                            } catch (IOException ex) {
                            }
                            if (player.zone != null && MapService.gI().isMapBlackBallWar(player.zone.map.mapId)
                                    && Util.getDistance(player.location.x, player.location.y, toX, toY) > 500) {
                                return;
                            }
                            if (b == 1) {
                                AchievementService.gI().checkDoneTaskFly(player, player.location.x - toX);
                            }
                        } catch (IOException e) {
                        }
                        PlayerService.gI().playerMove(player, toX, toY);
                    }
                }
                case -74 -> {
                    String ip = _session.ipAddress;
                    Logger.warning("ip " + ip + " đang tải dữ liệu\n");
                    if (SessionManager.firewallDownDataGame.containsKey(ip)) {
                        int soLanConnect = SessionManager.firewallDownDataGame.get(ip);
                        if (soLanConnect > 999) {
                            Service.gI().sendThongBaoOK(_session,
                                    "Bạn đã tải dữ liệu nhiều lần, đợi bảo trì rồi quay lại");
                            return;
                        } else {
                            SessionManager.firewallDownDataGame.put(ip, soLanConnect + 1);
                        }
                    } else {
                        SessionManager.firewallDownDataGame.put(ip, 1);
                    }
                    byte type = _msg.reader().readByte();
                    if (type == 1) {
                        DataGame.sendSizeRes(_session);
                    } else if (type == 2) {
                        DataGame.sendRes(_session);
                    }
                }
                case -81 -> {
                    if (player != null) {
                        try {
                            _msg.reader().readByte();
                            int[] indexItem = new int[_msg.reader().readByte()];
                            for (int i = 0; i < indexItem.length; i++) {
                                indexItem[i] = _msg.reader().readByte();
                            }
                            CombineService.gI().showInfoCombine(player, indexItem);
                        } catch (IOException e) {
                        }
                    }
                }
                case -87 ->
                    DataGame.updateData(_session);
                case -67 -> {
                    int id = _msg.reader().readInt();
                    DataGame.sendIcon(_session, id);
                }
                case 66 ->
                    DataGame.sendImageByName(_session, _msg.reader().readUTF());
                case -85 -> {
                    if (player != null) {
                        byte action = _msg.reader().readByte();
                        switch (action) {
                            case 0 -> {
                                char ch = (char) _msg.reader().readByte();
                                services.CaptchaService.gI().receiveCaptcha(player, ch);
                            }
                            case 1 -> {
                                services.CaptchaService.gI().sendCaptcha(player);
                            }
                        }
                    }
                }
                case -66 -> {
                    if (player != null) {
                        int effId = _msg.reader().readShort();
                        int idT = effId;
                        if (player.zone == null) {
                            break;
                        }
                        int shenronType = player.zone.shenronType;
                        if (idT == 25 && shenronType != -1 && player.zone.map.mapId != 0 && player.zone.map.mapId != 7
                                && player.zone.map.mapId != 14) {
                            idT = shenronType == 1 ? 51 : shenronType == 0 ? 51 : 60;
                        }
                        DataGame.sendEffectTemplate(_session, effId, idT);
                    }
                    break;
                }
                case -62 -> {
                    if (player != null) {
                        FlagBagService.gI().sendIconFlagChoose(player, _msg.reader().readByte());
                    }
                }
                case -63 -> {
                    if (player != null) {
                        short fbid = _msg.reader().readShort();
                        int fbidz = fbid & 0xFF; // Chuyển sang byte không dấu
                        FlagBagService.gI().sendIconEffectFlag(player, fbidz);
                    }
                }
                case -32 -> {
                    int bgId = _msg.reader().readShort();
                    DataGame.sendItemBGTemplate(_session, bgId);
                }
                case 22 -> {
                    if (player != null) {
                        _msg.reader().readByte();
                        NpcManager.getNpc(ConstNpc.DAU_THAN).confirmMenu(player, _msg.reader().readByte());
                    }
                }
                case -33, -23 -> {
                    if (player != null) {
                        ChangeMapService.gI().changeMapWaypoint(player);
                        Service.gI().hideWaitDialog(player);
                    }
                }
                case -45 -> {
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        byte status = _msg.reader().readByte();
                        SkillService.gI().useSkill(player, null, null, status, _msg);
                    }
                }
                case -46 -> {
                    if (player != null) {
                        ClanService.gI().getClan(player, _msg);
                    }
                }
                case -51 -> {
                    if (player != null) {
                        ClanService.gI().clanMessage(player, _msg);
                    }
                }
                case -54 -> {
                    if (player != null) {
                        ClanService.gI().clanDonate(player, _msg);
                    }
                }
                case -49 -> {
                    if (player != null) {
                        ClanService.gI().joinClan(player, _msg);
                    }
                }
                case -50 -> {
                    if (player != null) {
                        ClanService.gI().sendListMemberClan(player, _msg.reader().readInt());
                    }
                }
                case -56 -> {
                    if (player != null) {
                        ClanService.gI().clanRemote(player, _msg);
                    }
                }
                case -47 -> {
                    if (player != null) {
                        ClanService.gI().sendListClan(player, _msg.reader().readUTF());
                    }
                }
                case -55 -> {
                    if (player != null) {
                        ClanService.gI().showMenuLeaveClan(player);
                    }
                }
                case -57 -> {
                    if (player != null) {
                        ClanService.gI().clanInvite(player, _msg);
                    }
                }
                case -40 -> {
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        UseItem.gI().getItem(_session, _msg);
                    }
                }
                case -41 ->
                    Service.gI().sendCaption(_session, _msg.reader().readByte());
                case -43 -> {
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        if (player.baovetaikhoan) {
                            Service.gI().sendThongBao(player,
                                    "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                            return;
                        }
                        UseItem.gI().doItem(player, _msg);
                    }
                }
                case -91 -> {
                    if (player != null) {
                        switch (player.idMark.getTypeChangeMap()) {
                            case ConstMap.CHANGE_CAPSULE -> {
                                UseItem.gI().choseMapCapsule(player, _msg.reader().readByte());
                            }
                            case ConstMap.CHANGE_BLACK_BALL -> {
                                BlackBallWarService.gI().changeMap(player, _msg.reader().readByte());
                            }
                        }
                    }
                }
                case -39 -> {
                    if (player != null && player.loadmap) {
                        player.loadmap = false;
                        ChangeMapService.gI().finishLoadMap(player);
                        EffectMapService.gI().sendEffEvent(player);
                    }
                }
                case 11 -> {
                    int modId = _msg.reader().readShort();
                    DataGame.requestMobTemplate(_session, modId);
                }
                case 44 -> {
                    if (player != null) {
                        if (TransactionService.gI().check(player)) {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                            return;
                        }
                        String chat = _msg.reader().readUTF();
                        Command.gI().chat(player, chat);
                    }
                }
                case 32 -> {
                    if (player != null) {
                        int npcId = _msg.reader().readShort();
                        int select = _msg.reader().readByte();
                        MenuController.gI().doSelectMenu(player, npcId, select);
                    }
                }
                case 33 -> {
                    if (player != null) {
                        int npcId = _msg.reader().readShort();
                        if (npcId == ConstNpc.LY_TIEU_NUONG) {
                            MenuController.gI().openMenuNPC(_session, npcId, player);
                        } else {
                            MenuController.gI().openMenuNPC(_session, npcId, player);
                        }
                    }
                }
                case 34 -> {
                    if (player != null) {
                        int selectSkill = _msg.reader().readShort();
                        SkillService.gI().selectSkill(player, selectSkill);
                    }
                }
                case 54 -> {
                    if (player != null) {
                        int mobId = _msg.reader().readByte();
                        int masterId = -1;
                        boolean isMobMe = mobId == -1;
                        if (isMobMe) {
                            masterId = _msg.reader().readInt();
                        }
                        // _msg.reader().readByte();
                        Service.gI().attackMob(player, mobId, isMobMe, masterId);
                    }
                }
                case -60 -> {
                    if (player != null) {
                        int playerId = _msg.reader().readInt();
                        Service.gI().attackPlayer(player, playerId);
                    }
                }
                case -27 -> {
                    if (!_session.sentKey()) {
                        _session.sendKey();
                        DataGame.sendVersionRes(_session);
                    }
                }
                case -111 ->
                    DataGame.sendDataImageVersion(_session);
                case -20 -> {
                    if (player != null && !player.isDie()) {
                        int itemMapId = _msg.reader().readShort();
                        ItemMapService.gI().pickItem(player, itemMapId, false);
                    }
                }
                case -28 ->
                    messageNotMap(_session, _msg);
                case -29 ->
                    messageNotLogin(_session, _msg);
                case -30 ->
                    messageSubCommand(_session, _msg);
                case -15 -> {
                    if (player != null) {
                        PlayerService.gI().Dievenha(player);
                    }
                }

                case -16 -> {
                    // hồi sinh
                    if (player != null && !player.isPKDHVT) {
                        PlayerService.gI().hoiSinh(player);
                    }
                }
                case -104 -> {
                    if (player != null) {
                        Service.gI().mabaove(player, _msg.reader().readInt());
                    }
                }
                case -118 -> {
                    if (player != null) {
                        int _id = _msg.reader().readInt();
                        int menuType = player.idMark.getMenuType();
                        switch (menuType) {
                            case 0, 1, 2 -> {
                                SuperRankService.gI().competing(player, _id);
                            }
                            case 3, 4 -> {
                                if (player.isAdmin()) {
                                    Boss boss = BossManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = BossManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            case 5 -> {
                                if (player.isAdmin()) {
                                    Boss boss = BrolyManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = BrolyManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Không tìm thấy boss hoặc boss chưa có vị trí");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            case 6 -> {
                                if (player.isAdmin()) {
                                    Boss boss = AnTromManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = AnTromManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            case 7 -> {
                                if (player.isAdmin()) {
                                    Boss boss = Boss12hManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = Boss12hManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            case 8 -> {
                                if (player.isAdmin()) {
                                    Boss boss = Boss14hManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = Boss14hManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            case 9 -> {
                                if (player.isAdmin()) {
                                    Boss boss = TrungThuEventManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = TrungThuEventManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            case 10 -> {
                                if (player.isAdmin()) {
                                    Boss boss = HalloweenEventManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = HalloweenEventManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Không tìm thấy boss hoặc boss chưa có vị trí");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            case 11 -> {
                                if (player.isAdmin()) {
                                    Boss boss = ChristmasEventManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = ChristmasEventManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    } else {
                                        Service.gI().sendThongBao(player,
                                                "Không tìm thấy boss hoặc boss chưa có vị trí");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                            default -> {
                                if (player.isAdmin()) {
                                    Boss boss = BossManager.gI().findBossByUniqueId(_id);
                                    if (boss == null) {
                                        boss = BossManager.gI().findBossByBossID(_id);
                                    }
                                    if (boss != null && boss.zone != null && boss.zone.map != null) {
                                        ChangeMapService.gI().changeMapBoss(player, boss.zone.map.mapId,
                                                boss.zone.zoneId, boss.location.x, boss.location.y);
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                                }
                            }
                        }
                    }
                }
                case -38 -> {
                    if (player != null) {
                        finishUpdate(player);
                    }
                }
                case 126 -> {
                }
                case -78 ->
                    _msg.reader().readInt();
                case -114 -> {
                }
                case 27 -> {
                }
                case -76 ->
                    AchievementService.gI().confirmAchievement(player, _msg.reader().readByte());
                case -48 -> {
                    if (player != null) {
                        mail.handler.MailHandler.gI().controller(player, _msg);
                    }
                }
                case -58 -> {
                    if (player != null) {
                        dragonpass.DragonPassHandler.gI().controller(player, _msg);
                    }
                }
                case 67 -> {
                    if (player != null) {
                        luckywheel.LuckyWheelHandler.gI().controller(player, _msg);
                    }
                }
                default -> {
                    Logger.log("CMD: " + cmd + "\n");
                }
            }
        } catch (Exception e) {
            if (errors < 5) {
                errors++;
                Logger.logException(Controller.class, e);
                if (player != null) {
                    Logger.warning("Player: " + player.name + "\n");
                }
                Logger.warning("Lỗi function: 'onMessage'\n");
                Logger.warning("Lỗi controller message command: " + _msg.command + "\n");
            }
        } finally {
            if (player != null && player.inventory != null && auditGoldBefore != Long.MIN_VALUE) {
                String context = describeAuditCommand(auditCommand);
                long goldDelta = player.inventory.gold - auditGoldBefore;
                long gemDelta = (long) player.inventory.gem - auditGemBefore;
                long rubyDelta = (long) player.inventory.ruby - auditRubyBefore;
                long barDelta = (long) countItem(player, 457) - auditGoldBarBefore;
                boolean exactHandled = AssetAuditService.gI().consumeExactBalanceHandled();
                if (!exactHandled && goldDelta != 0) AssetAuditService.gI().recordCurrency("BALANCE_CHANGE", "GOLD", goldDelta,
                        goldDelta < 0 ? player : null, goldDelta > 0 ? player : null, player, context);
                if (!exactHandled && gemDelta != 0) AssetAuditService.gI().recordCurrency("BALANCE_CHANGE", "GEM", gemDelta,
                        gemDelta < 0 ? player : null, gemDelta > 0 ? player : null, player, context);
                if (!exactHandled && rubyDelta != 0) AssetAuditService.gI().recordCurrency("BALANCE_CHANGE", "RUBY", rubyDelta,
                        rubyDelta < 0 ? player : null, rubyDelta > 0 ? player : null, player, context);
                if (!exactHandled && barDelta != 0) AssetAuditService.gI().recordCurrency("GOLD_BAR_CHANGE", "GOLD_BAR", barDelta,
                        barDelta < 0 ? player : null, barDelta > 0 ? player : null, player, context);
                AssetAuditService.gI().syncBalance(player);
            }
            _msg.cleanup();
            _msg.dispose();
            long timeDo = System.currentTimeMillis() - st;
            if (timeDo > 10000) {
                Logger.warning(_msg.command + " - TimeOut: " + timeDo + " ms\n");
            }
        }
    }

    public void messageNotLogin(MySession session, Message msg) {
        if (msg != null) {
            try {
                byte cmd = msg.reader().readByte();
                switch (cmd) {
                    case 0, 113 -> {
                        String username = Util.sanitizeInput(msg.reader().readUTF());
                        String password = Util.sanitizeInput(msg.reader().readUTF());
                        if (username == null) {
                            username = "";
                        }
                        if (password == null) {
                            password = "";
                        }
                        username = username.trim();
                        password = password.trim();
                        
                        if (username.isEmpty() && password.isEmpty()) {
                            Service.gI().switchToRegisterScr(session);
                        } else {
                            session.login(username, password);
                        }
                    }
                    case 1 -> {
                        msg.reader().readUTF(); // 1
                        msg.reader().readUTF(); // 2
                        msg.reader().readUTF(); // 3
                        msg.reader().readUTF(); // 4
                        msg.reader().readUTF(); // 5
                        msg.reader().readUTF(); // 6

                        String maGioiThieu = Util.sanitizeInput(msg.reader().readUTF()).trim();
                        String username = Util.sanitizeInput(msg.reader().readUTF()).trim();
                        String password = Util.sanitizeInput(msg.reader().readUTF());

                        if (username == null || username.isEmpty()) {
                            Service.gI().sendThongBaoOK(session, "Tài khoản không được để trống!");
                            return;
                        }

                        if (username.length() < 5 || username.length() > 20){
                            Service.gI().sendThongBaoOK(session,"Tài khoản phải có từ 5 đến 20 ký tự");
                            return;
                        }

                        if (password == null || password.trim().isEmpty()) {
                            Service.gI().sendThongBaoOK(session, "Mật khẩu không được để trống!");
                            return;
                        }

                        if (password.length() < 3 || password.length() > 18) {
                            Service.gI().sendThongBaoOK(session, "Mật khẩu phải có độ dài từ 3 đến 18 ký tự!");
                            return;
                        }

                        if (PlayerDAO.ExistUsername(username)) {
                            Service.gI().switchToRegisterScr(session);
                            Service.gI().sendThongBaoOK(session,
                                    "Tài khoản đã tồn tại! Vui lòng thử lại.");
                            return;
                        }

                        Service.gI().switchToRegisterScr(session);
                        Service.gI().sendThongBaoOK(session,
                                "Vui lòng sử dụng màn hình đăng ký tài khoản mới.");
                    }
                    case 2 ->
                        Service.gI().setClientType(session, msg);
                    default -> {
                    }
                }
            } catch (IOException e) {
                Logger.logException(Controller.class, e);
            }
        }
    }

    public void login2(MySession session, Message msg) throws IOException {
        Service.gI().switchToRegisterScr(session);
    }

    private static int countItem(Player player, int templateId) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) return 0;
        long total = 0;
        for (item.Item item : player.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id == templateId) total += item.quantity;
        }
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    private static String describeAuditCommand(byte command) {
        return switch (command) {
            case -100 -> "Ký gửi/mua bán tại chợ ký gửi (lệnh -100)";
            case -86 -> "Giao dịch trực tiếp giữa người chơi (lệnh -86)";
            case 6 -> "Mua vật phẩm tại cửa hàng (lệnh 6)";
            case 7 -> "Bán vật phẩm tại cửa hàng (lệnh 7)";
            case 22, 32 -> "Tương tác menu NPC (lệnh " + command + ")";
            case -48 -> "Nhận/gửi phần thưởng qua mail (lệnh -48)";
            case -58 -> "Dragon Pass (lệnh -58)";
            case 67 -> "Vòng quay may mắn (lệnh 67)";
            case -34 -> "Nâng cấp/thu hoạch cây đậu (lệnh -34)";
            default -> "Thao tác client, mã lệnh " + command;
        };
    }

    public void messageNotMap(MySession _session, Message _msg) {
        if (_msg != null) {
            Player player = null;
            try {
                player = _session.player;
                byte cmd = _msg.reader().readByte();
                switch (cmd) {
                    case 2 ->
                        createChar(_session, _msg);
                    case 6 ->
                        DataGame.updateMap(_session);
                    case 7 ->
                        DataGame.updateSkill(_session);
                    case 8 ->
                        ItemData.updateItem(_session);
                    case 10 ->
                        DataGame.sendMapTemp(_session, _msg.reader().readUnsignedByte());
                    case 13 -> {
                        // client ok
                        if (player != null && player.isPl()) {
                            Service.gI().player(player);
                            Service.gI().Send_Caitrang(player);
                            // -64 my flag bag
                            Service.gI().sendFlagBag(player);

                            // -113 skill shortcut
                            if (player.playerSkill != null) {
                                player.playerSkill.sendSkillShortCut();
                            }
                            // item time
                            ItemTimeService.gI().sendAllItemTime(player);

                            // send current task
                            TaskService.gI().sendInfoCurrentTask(player);

                            if (TaskService.gI().getIdTask(player) == ConstTask.TASK_0_0) {
                                NpcService.gI().createTutorial(player, -1,
                                        "Chào Mừng " + player.name + " Đến Với: " + ServerManager.NAME + "\n"
                                                + "Nhiệm vụ đầu tiên của bạn là di chuyển\n"
                                                + "Bạn hãy di chuyển nhân vật theo mũi tên chỉ hướng");
                            } else {
                                // -70 thông báo bigmessage
                                // sendThongBaoServer(player);
                            }

                            if (player.inventory != null && player.inventory.itemsBody != null
                                    && player.inventory.itemsBody.size() > 10
                                    && player.inventory.itemsBody.get(10).isNotNullItem()) {
                                Service.gI().sendChibi(player);
                            }

                            if (player.zone == null) {
                                Zone defaultZone = MapService.gI().getZone(21 + player.gender);
                                if (defaultZone != null) {
                                    player.zone = defaultZone;
                                    player.location.x = 300;
                                    player.location.y = 336;
                                    player.zone.addPlayer(player);
                                } else {
                                    Logger.error("Cannot set default zone for player: " + player.name);
                                    return;
                                }
                            }
                            player.zone.mapInfo(player);

                            if (player.getSession().version >= 231) {
                                for (Skill skill : player.playerSkill.skills) {
                                    if (skill.currLevel <= 0 || skill.template.type != 4) {
                                        continue;
                                    }
                                    SkillService.gI().sendCurrLevelSpecial(player, skill);
                                }
                            }
                            Service.gI().sendTimeSkill(player);
                            TrainingService.gI().tnsmLuyenTapUp(player);
                            // player.sendNewPet();
                            // if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_32_0 && !player.isAdmin()
                            //         && player.getSession().eventPoint >= 0) {
                            //     ChatGlobalService.gI().chatVip(player,
                            //             "Trùm server " + player.name + " vừa mới nhậm chức, chúng mày nằm xuống!");
                            //     if (player.getSession().version < 237) {
                            //         Service.gI().sendThongBaoAllPlayer(
                            //                 "Trùm server " + player.name + " vừa mới nhậm chức, chúng mày nằm xuống!");
                            //     }
                            //     ServerNotify.gI().notify(
                            //             "Trùm server " + player.name + " vừa mới nhậm chức, chúng mày nằm xuống!");
                            // }
                            // if (player.isAdmin()) {
                            // Service.gI().sendMessageServer("Admin đã xuất hiện, chúng mày nằm xuống!");
                            // }
                            if (player.getSession() != null && player.getSession().danap > 0) {
                                AchievementService.gI().checkDoneTask(player, ConstAchievement.LAN_DAU_NAP_NGOC);
                            }
                            if (DailyGiftService.checkDailyGift(player, DailyGiftService.NHAN_NGOC_MIEN_PHI)) {
                                Service.gI().sendThongBao(player,
                                        "Hôm nay bạn sẽ nhận được từ 1 đến 2 viên ngọc khi tiêu diệt 1 con quái");
                            }
                        }
                    }
                    default -> {
                    }
                }
            } catch (IOException e) {
                Logger.logException(Controller.class, e);
            }
        }
    }

    public void messageSubCommand(MySession _session, Message _msg) {
        if (_msg != null) {
            Player player = null;
            try {
                player = _session.player;
                byte command = _msg.reader().readByte();
                switch (command) {
                    case 16 -> {
                        byte type = _msg.reader().readByte();
                        short point = _msg.reader().readShort();
                        if (player != null && player.nPoint != null) {
                            player.nPoint.increasePoint(type, point);
                        }
                    }
                    case 64 -> {
                        int playerId = _msg.reader().readInt();
                        int menuId = _msg.reader().readShort();
                        SubMenuService.gI().controller(player, playerId, menuId);
                    }
                    default -> {
                    }
                }
            } catch (IOException e) {
                Logger.logException(Controller.class, e);
            }
        }
    }

    public void createChar(MySession session, Message msg) {
        if (!Maintenance.isRunning()) {
            AlyraResultSet rs = null;
            boolean created = false;
            try {
                String name = msg.reader().readUTF().trim().toLowerCase();
                int gender = msg.reader().readByte();
                int hair = msg.reader().readByte();
                boolean validAppearance = (gender == 0 && (hair == 64 || hair == 30 || hair == 31))
                        || (gender == 1 && (hair == 9 || hair == 29 || hair == 32))
                        || (gender == 2 && (hair == 6 || hair == 27 || hair == 28));
                if (!validAppearance) {
                    Service.gI().sendThongBaoOK(session, "Hành tinh hoặc kiểu tóc không hợp lệ");
                    return;
                }
                if (name.length() >= 5 && name.length() <= 10) {
                    rs = AlyraManager.executeQuery("select * from player where name = ?", name);
                    if (rs.first()) {
                        Service.gI().sendThongBaoOK(session, "Tên nhân vật đã tồn tại");
                    } else {
                        if (Util.haveSpecialCharacter(name)) {
                            Service.gI().sendThongBaoOK(session, "Tên nhân vật không được chứa ký tự đặc biệt");
                        } else {
                            boolean isNotIgnoreName = true;
                            for (String n : ConstIgnoreName.IGNORE_NAME) {
                                if (name.equals(n)) {
                                    Service.gI().sendThongBaoOK(session, "Tên nhân vật đã tồn tại");
                                    isNotIgnoreName = false;
                                    break;
                                }
                            }
                            if (isNotIgnoreName) {
                                if (session.userId <= 0 || session.uu == null || session.uu.startsWith("trial_")) {
                                    Service.gI().sendThongBaoOK(session,
                                            "Vui lòng đăng nhập tài khoản trước khi tạo nhân vật.");
                                    return;
                                }
                                created = PlayerDAO.createNewPlayer(session.userId, name.toLowerCase(), (byte) gender,
                                        hair);
                            }
                        }
                    }
                } else {
                    Service.gI().sendThongBaoOK(session,
                            "Tên nhân vật chỉ đồng ý các ký tự a-z, 0-9 và chiều dài từ 5 đến 10 ký tự");
                }
            } catch (Exception e) {
                Logger.logException(Controller.class, e);
            } finally {
                if (rs != null) {
                    rs.dispose();
                }
            }
            if (created) {
                session.login(session.uu, session.pp);
            }
        }
    }

    public void sendInfo(MySession session) {
        try {
            Player player = session.player;
            DataGame.sendTileSetInfo(session);
            IntrinsicService.gI().sendInfoIntrinsic(player);
            Service.gI().point(player);
            TaskService.gI().sendTaskMain(player);
            Service.gI().clearMap(player);
            ClanService.gI().sendMyClan(player);
            PlayerService.gI().sendMaxStamina(player);
            PlayerService.gI().sendCurrentStamina(player);
            Service.gI().sendNangDong(player);
            Service.gI().sendHavePet(player);
            if (player.superRank != null && player.superRank.rank < 1) { 
                Service.gI().sendTopRank(player);

                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        player.superRank.rank = SuperRankDAO.getRank((int) player.id);
                        player.superRank.lastRewardTime = System.currentTimeMillis();
                        SuperRankDAO.insertData(player);
                        Service.gI().sendTopRank(player);
                    } catch (Exception e) {
                        utils.Logger.logException(Controller.class, e, "Error loading SuperRank");
                    }
                });
            } else {
                Service.gI().sendTopRank(player);
            }
            ServerNotify.gI().sendNotifyTab(player);
            player.setClothes.setup();
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            BadgesService.removeExpiredBadges(player);

            if (PlayerManager.getPlayer(player.id) != player) {
                PlayerManager.addPlayer(player);
            }
            ItemTimeService.gI().sendCanAutoPlay(player);
        } catch (Exception e) {
        }
    }

    public void finishUpdate(Player player) {
        if (player.getSession() != null) {
            player.getSession().finishUpdate = true;
        }
    }

}
