package services.player;

import clan.Clan;
import clan.ClanMember;
import clan.ClanMessage;
import consts.ConstAchievement;
import consts.ConstNpc;
import consts.ConstTask;
import daos.NDVSqlFetcher;
import daos.PlayerDAO;
import data.AlyraManager;
import item.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import network.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import player.Player;
import server.Client;
import server.Manager;
import services.AchievementService;
import services.FlagBagService;
import services.ItemService;
import services.ItemTimeService;
import services.Service;
import services.TaskService;
import services.map.NpcService;
import system.Template.FlagBag;
import utils.Logger;
import utils.TimeUtil;
import utils.Util;

public class ClanService {

    private static final byte REQUEST_FLAGS_CHOOSE_CREATE_CLAN = 1;
    private static final byte ACCEPT_CREATE_CLAN = 2;
    private static final byte REQUEST_FLAGS_CHOOSE_CHANGE_CLAN = 3;
    private static final byte ACCEPT_CHANGE_INFO_CLAN = 4;

    private static final byte CHAT = 0;
    private static final byte ASK_FOR_PEA = 1;
    private static final byte ASK_FOR_JOIN_CLAN = 2;

    private static final byte ACCEPT_ASK_JOIN_CLAN = 0;
    private static final byte CANCEL_ASK_JOIN_CLAN = 1;

    private static final byte KICK_OUT = -1;
    private static final byte CAT_CHUC = 2;
    private static final byte PHONG_PHO = 1;
    private static final byte PHONG_PC = 0;

    private static final byte SEND_INVITE_CLAN = 0;
    private static final byte ACCEPT_JOIN_CLAN = 1;

    private static ClanService instance;

    private ClanService() {
    }

    public static ClanService gI() {
        if (instance == null) {
            instance = new ClanService();
        }
        return instance;
    }

    public Clan getClanById(int id) throws Exception {
        return getClanById(0, Manager.getNumClan(), id);
    }

    private Clan getClanById(int l, int r, int id) throws Exception {
        if (l <= r) {
            int m = (l + r) / 2;
            Clan clan = null;
            try {
                clan = Manager.CLANS.get(m);
            } catch (Exception e) {
                throw new Exception("Không tìm thấy clan id: " + id);
            }
            if (clan.id == id) {
                return clan;
            } else if (clan.id > id) {
                return getClanById(l, m - 1, id);
            } else {
                return getClanById(m + 1, r, id);
            }
        } else {
            throw new Exception("Không tìm thấy clan id: " + id);
        }
    }

    public List<Clan> getClans(String name) {
        List<Clan> listClan = new ArrayList<>();
        for (Clan clan : Manager.CLANS) {
            if (clan.name.toLowerCase().contains(name.toLowerCase())) {
                listClan.add(clan);
            }
            if (listClan.size() >= 20) {
                break;
            }
        }
        return listClan;
    }

    public void getClan(Player player, Message msg) {
        try {
            byte action = msg.reader().readByte();
            switch (action) {
                case REQUEST_FLAGS_CHOOSE_CREATE_CLAN, REQUEST_FLAGS_CHOOSE_CHANGE_CLAN ->
                    FlagBagService.gI().sendListFlagClan(player);
                case ACCEPT_CREATE_CLAN -> {
                    short imgId = msg.reader().readShort();
                    String name = msg.reader().readUTF();
                    createClan(player, imgId, name);
                }
                case ACCEPT_CHANGE_INFO_CLAN -> {
                    short imgId = msg.reader().readShort();
                    String slogan = msg.reader().readUTF();
                    changeInfoClan(player, imgId, slogan);
                }
            }
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }
//hnamwrite
    public void clanMessage(Player player, Message msg) {
        try {
            byte type = msg.reader().readByte();
            switch (type) {
                case CHAT ->
                    chat(player, msg.reader().readUTF());
                case ASK_FOR_PEA ->
                    askForPea(player);
                case ASK_FOR_JOIN_CLAN ->
                    askForJoinClan(player, msg.reader().readInt());
            }
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    public synchronized void clanDonate(Player plGive, Message msg) {
        // Validate player state
        if (plGive == null || plGive.clan == null || plGive.inventory == null) {
            return;
        }

        try {
            int messageId = msg.reader().readInt();

            // Validate message ID
            ClanMessage cmg = plGive.clan.getClanMessage(messageId);
            if (cmg == null) {
                Service.gI().sendThongBao(plGive, "Yêu cầu không tồn tại");
                return;
            }
            if (cmg.type != 1) {
                Service.gI().sendThongBao(plGive, "Không thể thực hiện");
                return;
            }
            synchronized (cmg) {
                if (cmg.receiveDonate >= cmg.maxDonate) {
                    Service.gI().sendThongBao(plGive, "Đã đủ số lượng hỗ trợ");
                    return;
                }
                if (cmg.maxDonate < 0 || cmg.maxDonate > 100) {
                    Logger.warning("Invalid maxDonate detected: " + cmg.maxDonate + " from player: " + plGive.name);
                    Service.gI().sendThongBao(plGive, "Lỗi dữ liệu");
                    return;
                }
                if (plGive.id == cmg.playerId) {
                    Service.gI().sendThongBao(plGive, "Không thể tự cho đậu cho chính mình");
                    return;
                }
                Player plReceive = plGive.clan.getPlayerOnline(cmg.playerId);
                if (plReceive == null) {
                    Service.gI().sendThongBao(plGive, "Người chơi hiện không online");
                    return;
                }

                if (plReceive.inventory == null || plReceive.inventory.itemsBag == null) {
                    Service.gI().sendThongBao(plGive, "Không thể thực hiện lúc này");
                    return;
                }
                if (plReceive.inventory.itemsBag.stream().filter(Item::isNotNullItem).count() >= plReceive.inventory.itemsBag.size()) {
                    Service.gI().sendThongBao(plGive, plReceive.name + " đã đầy hành trang");
                    return;
                }
                Item pea = plGive.inventory.itemsBox.stream()
                        .filter(item -> item != null && item.isNotNullItem()
                        && item.template != null && item.template.type == 6
                        && item.quantity > 0)
                        .findFirst().orElse(null);

                if (pea == null || pea.template == null) {
                    Service.gI().sendThongBao(plGive, "Không tìm thấy đậu trong rương");
                    return;
                }

                if (pea.quantity < 1) {
                    Service.gI().sendThongBao(plGive, "Số lượng đậu không hợp lệ");
                    return;
                }
                Short peaTemplateId = pea.template.id;
                InventoryService.gI().subQuantityItem(plGive.inventory.itemsBox, pea, 1);

                try {
                    Item peaCopy = ItemService.gI().createNewItem(peaTemplateId);
                    if (peaCopy == null || peaCopy.template == null) {
                        Item rollbackPea = ItemService.gI().createNewItem(peaTemplateId);
                        if (rollbackPea != null) {
                            rollbackPea.itemOptions = pea.itemOptions != null ? new java.util.ArrayList<>(pea.itemOptions) : new java.util.ArrayList<>();
                            rollbackPea.quantity = 1;
                            InventoryService.gI().addItemList(plGive.inventory.itemsBox, rollbackPea);
                        }
                        Service.gI().sendThongBao(plGive, "Không thể tạo item");
                        Logger.error("Failed to create pea copy for player: " + plGive.name);
                        return;
                    }

                    peaCopy.itemOptions = pea.itemOptions != null ? new java.util.ArrayList<>(pea.itemOptions) : new java.util.ArrayList<>();
                    peaCopy.quantity = 1;
                    boolean added = InventoryService.gI().addItemBag(plReceive, peaCopy);
                    if (!added) {
                        Item rollbackPea = ItemService.gI().createNewItem(peaTemplateId);
                        if (rollbackPea != null) {
                            rollbackPea.itemOptions = pea.itemOptions != null ? new java.util.ArrayList<>(pea.itemOptions) : new java.util.ArrayList<>();
                            rollbackPea.quantity = 1;
                            InventoryService.gI().addItemList(plGive.inventory.itemsBox, rollbackPea);
                        }
                        Service.gI().sendThongBao(plGive, plReceive.name + " không thể nhận");
                        return;
                    }
                    cmg.receiveDonate++;
                    InventoryService.gI().sendItemBags(plReceive);
                    Service.gI().sendThongBao(plReceive, plGive.name + " đã cho bạn " + peaCopy.template.name);
                    Service.gI().sendThongBao(plGive, "Đã cho " + plReceive.name + " " + peaCopy.template.name);
                    plGive.clan.sendMessageClan(cmg);
                    AchievementService.gI().checkDoneTask(plGive, ConstAchievement.HO_TRO_DONG_DOI);
                    if (plReceive.isOffline) {
                        plReceive.notify = plGive.name + " đã cho bạn " + peaCopy.template.name;
                        PlayerDAO.updatePlayer(plReceive);
                    }
                } catch (Exception ex) {
                    try {
                        InventoryService.gI().addItemList(plGive.inventory.itemsBox, ItemService.gI().createNewItem(pea.template.id));
                    } catch (Exception rollbackEx) {
                        Logger.error("CRITICAL: Failed to rollback pea to " + plGive.name);
                    }
                    Logger.logException(ClanService.class, ex);
                    Service.gI().sendThongBao(plGive, "Đã xảy ra lỗi, vui lòng thử lại");
                }
            }
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
            Service.gI().sendThongBao(plGive, "Đã xảy ra lỗi");
        }
    }

    public void joinClan(Player player, Message msg) {
        try {
            int clanMessageId = msg.reader().readInt();
            byte action = msg.reader().readByte();
            switch (action) {
                case ACCEPT_ASK_JOIN_CLAN ->
                    acceptAskJoinClan(player, clanMessageId);
                case CANCEL_ASK_JOIN_CLAN ->
                    cancelAskJoinClan(player, clanMessageId);
            }
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    public void clanRemote(Player player, Message msg) {
        try {
            int playerId = msg.reader().readInt();
            byte role = msg.reader().readByte();
            switch (role) {
                case CAT_CHUC ->
                    catChuc(player, playerId);
                case KICK_OUT ->
                    kickOut(player, playerId);
                case PHONG_PHO ->
                    phongPho(player, playerId);
                case PHONG_PC ->
                    showMenuNhuongPc(player, playerId);
            }
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    public void clanInvite(Player player, Message msg) {
        try {
            byte action = msg.reader().readByte();
            switch (action) {
                case SEND_INVITE_CLAN ->
                    sendInviteClan(player, msg.reader().readInt());
                case ACCEPT_JOIN_CLAN ->
                    acceptJoinClan(player, msg.reader().readInt());
            }
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    private void sendInviteClan(Player player, int playerId) {
        Player pl = Client.gI().getPlayer(playerId);
        if (pl != null && player.clan != null && (player.clan.isLeader(player) || player.clan.isDeputy(player))) {
            if (player.clan.getCurrMembers() >= player.clan.maxMember) {
                Service.gI().sendThongBao(player, "Bang đã đủ thành viên, không thể mời thêm.");
                return;
            }
            if (TaskService.gI().getIdTask(pl) < ConstTask.TASK_10_0) {
                Service.gI().sendThongBao(player, pl.name + " chưa thể vào bang lúc này");
                return;
            }
            if (pl.clan != null) {
                Service.gI().sendThongBao(player, pl.name + " đang ở trong bang khác.");
                return;
            }
            if (pl.idMark.isHoldBlackBall()) {
                Service.gI().sendThongBao(player, pl.name + " đang giữ ngọc rồng sao đen.");
                return;
            }

            Service.gI().sendThongBao(player, "Đã gửi lời mời đến " + pl.name);
            try {
                Message msg = new Message(-57);
                msg.writer().writeUTF(player.name + " mời bạn vào bang " + player.clan.name);
                msg.writer().writeInt(player.clan.id);
                msg.writer().writeInt(758435);
                pl.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(ClanService.class, e);
            }
        }
    }

    private void acceptJoinClan(Player player, int clanId) {
        try {
            if (TaskService.gI().getIdTask(player) < ConstTask.TASK_8_0) {
                Service.gI().sendThongBao(player, "Bạn chưa thể vào bang lúc này");
                return;
            }
            if (player.idMark.isHoldBlackBall()) {
                Service.gI().sendThongBao(player, "Bạn đang giữ ngọc rồng sao đen.");
                return;
            }
            if (player.clan != null) {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
                return;
            }
            Clan clan = getClanById(clanId);
            if (clan != null && clan.getCurrMembers() < clan.maxMember) {
                clan.addClanMember(player, Clan.MEMBER);
                clan.addMemberOnline(player);
                player.clan = clan;

                clan.sendMyClanForAllMember();
                this.sendClanId(player);
                Service.gI().sendFlagBag(player);
                Service.gI().sendThongBao(player, "Bạn đã gia nhập bang: " + clan.name);
                ItemTimeService.gI().sendTextDoanhTrai(player);
                checkDoneTaskJoinClan(clan);
                clan.update();
            } else {
                Service.gI().sendThongBao(player, "Bang đã đủ thành viên.");
            }
        } catch (Exception ex) {
            Service.gI().sendThongBao(player, ex.getMessage());
        }
    }

    private void acceptAskJoinClan(Player player, int clanMessageId) throws Exception {
        Clan clan = player.clan;
        if (clan == null || !clan.isLeader(player)) {
            return;
        }

        ClanMessage cmg = clan.getClanMessage(clanMessageId);
        if (cmg == null || clan.members.stream().anyMatch(cm -> cm.id == cmg.playerId)) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }

        int newMemberId = cmg.playerId;
        Player newMember = Client.gI().getPlayer(newMemberId);
        if (newMember == null) {
            newMember = NDVSqlFetcher.loadById(newMemberId);
        }

        cmg.type = 0;
        cmg.role = Clan.LEADER;
        cmg.playerId = (int) player.id;
        cmg.playerName = player.name;
        cmg.isNewMessage = 0;
        cmg.color = ClanMessage.RED;

        if (newMember != null) {
            if (newMember.idMark.isHoldBlackBall()) {
                Service.gI().sendThongBao(player, newMember.name + " đang giữ ngọc rồng sao đen.");
                return;
            }
            if (newMember.clan == null) {
                if (clan.getCurrMembers() < clan.maxMember) {
                    clan.addClanMember(newMember, Clan.MEMBER);
                    clan.addMemberOnline(newMember);
                    newMember.clan = player.clan;

                    cmg.text = "Chấp nhận " + newMember.name + " vào bang.";
                    this.sendClanId(newMember);
                    Service.gI().sendFlagBag(newMember);
                    ItemTimeService.gI().sendTextDoanhTrai(newMember);
                    Service.gI().sendThongBao(newMember, "Bạn đã gia nhập bang: " + clan.name);
                    checkDoneTaskJoinClan(clan);
                    clan.update();

                    if (newMember.isOffline) {
                        newMember.notify = "Bạn đã gia nhập bang: " + clan.name;
                        PlayerDAO.updatePlayer(newMember);
                    }
                } else {
                    Service.gI().sendThongBao(player, "Bang đã đủ thành viên.");
                }
            } else {
                cmg.text = newMember.name + " đã vào bang khác.";
            }
        } else {
            cmg.text = "Người chơi đang offline.";
        }
        clan.sendMyClanForAllMember();
    }

    private void cancelAskJoinClan(Player player, int clanMessageId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            ClanMessage cmg = clan.getClanMessage(clanMessageId);
            if (cmg != null) {
                Player newMember = Client.gI().getPlayer(cmg.playerId);
                cmg.type = 0;
                cmg.role = Clan.LEADER;
                cmg.playerId = (int) player.id;
                cmg.playerName = player.name;
                cmg.isNewMessage = 0;
                cmg.color = ClanMessage.RED;
                cmg.text = "Từ chối " + cmg.playerName + " vào bang.";
                if (newMember != null) {
                    if (newMember.clan != null) {
                        cmg.text = newMember.name + " đã vào bang khác.";
                    } else {
                        Service.gI().sendThongBao(newMember, "Bạn đã bị từ chối vào bang: " + clan.name);
                    }
                }
                clan.sendMyClanForAllMember();
            }
        }
    }

    private synchronized void askForPea(Player player) {
        if (player == null || player.clan == null || player.inventory == null) {
            return;
        }
        ClanMember cm = player.clan.getClanMember((int) player.id);
        if (cm == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            Logger.warning("Player " + player.name + " not found in clan members");
            return;
        }
        // Check cooldown (5 phút)
        long cooldownTime = 1000 * 60 * 5; // 5 minutes
        long timeSinceLastAsk = System.currentTimeMillis() - cm.timeAskPea;

        if (timeSinceLastAsk < cooldownTime) {
            Service.gI().sendThongBao(player, "Vui lòng chờ " + TimeUtil.getTimeLeft(cm.timeAskPea, 300) + " nữa.");
            return;
        }
        synchronized (cm) {
            long pendingRequests = player.clan.getCurrClanMessages().stream()
                    .filter(msg -> msg.type == 1 && msg.playerId == player.id)
                    .count();

            if (pendingRequests >= 3) {
                Service.gI().sendThongBao(player, "Bạn đã có quá nhiều yêu cầu đang chờ xử lý");
                return;
            }
            ClanMessage cmg = new ClanMessage(player.clan);
            cmg.type = 1;
            cmg.playerId = cm.id;
            cmg.playerName = cm.name;
            cmg.role = cm.role;
            cmg.receiveDonate = 0;
            cmg.maxDonate = 5; // byte type, max 127
            cmg.time = (int) (System.currentTimeMillis() / 1000);
            cm.timeAskPea = System.currentTimeMillis();
            player.clan.addClanMessage(cmg);
            player.clan.sendMessageClan(cmg);
            Service.gI().sendThongBao(player, "Đã gửi yêu cầu xin đậu thành công");
        }
    }

    private void askForJoinClan(Player player, int clanId) {
        if (player.clan != null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        try {
            Clan clan = getClanById(clanId);
            if (clan != null) {
                boolean alreadyInClan = clan.members.stream().anyMatch(cm -> cm.id == player.id);
                boolean alreadyAsked = clan.getCurrClanMessages().stream()
                        .anyMatch(c -> c.type == 2 && c.playerId == player.id && c.role == -1);

                if (!alreadyInClan && !alreadyAsked) {
                    ClanMessage cmg = new ClanMessage(clan);
                    cmg.type = 2;
                    cmg.playerId = (int) player.id;
                    cmg.playerName = player.name;
                    cmg.playerPower = player.nPoint.power;
                    cmg.role = -1;
                    clan.addClanMessage(cmg);
                    clan.sendMessageClan(cmg);
                    Service.gI().sendThongBao(player, "Đã gửi yêu cầu đến bang hội.");
                } else {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                }
            }
        } catch (Exception ex) {
            Service.gI().sendThongBao(player, ex.getMessage());
        }
    }

    private void changeInfoClan(Player player, short imgId, String slogan) {
        if (slogan != null && !slogan.isEmpty()) {
            changeSlogan(player, slogan);
        } else {
            changeFlag(player, imgId);
        }
    }

    private void createClan(Player player, short imgId, String name) {
        if (player.clan != null) {
            return;
        }
        if (name.length() > 30) {
            Service.gI().sendThongBao(player, "Tên bang hội không được quá 30 ký tự");
            return;
        }
        FlagBag flagBag = FlagBagService.gI().getFlagBag(imgId);
        if (flagBag != null) {
            if (player.inventory.gold < flagBag.gold) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng.");
                return;
            }
            if (player.inventory.gem < flagBag.gem) {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc.");
                return;
            }
            player.inventory.gold -= flagBag.gold;
            player.inventory.gem -= flagBag.gem;
            PlayerService.gI().sendInfoHpMpMoney(player);

            Clan clan = new Clan();
            clan.imgId = imgId;
            clan.name = name;
            Manager.addClan(clan);

            player.clan = clan;
            clan.addClanMember(player, Clan.LEADER);
            clan.addMemberOnline(player);
            clan.insert();

            Service.gI().sendFlagBag(player);
            sendMyClan(player);
            Service.gI().sendThongBao(player, "Chúc mừng bạn đã tạo bang thành công.");
        }
    }

    public void sendListClan(Player player, String name) {
        try {
            List<Clan> clans = getClans(name);
            Message msg = new Message(-47);
            msg.writer().writeByte(clans.size());
            for (Clan clan : clans) {
                msg.writer().writeInt(clan.id);
                msg.writer().writeUTF(clan.name);
                msg.writer().writeUTF(clan.slogan);
                msg.writer().writeShort(clan.imgId);
                msg.writer().writeUTF(String.valueOf(clan.powerPoint));
                msg.writer().writeUTF(clan.getLeader().name);
                msg.writer().writeByte(clan.getCurrMembers());
                msg.writer().writeByte(clan.maxMember);
                msg.writer().writeInt(clan.createTime);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    public void sendListMemberClan(Player player, int clanId) {
        try {
            Clan clan = getClanById(clanId);
            if (clan != null) {
                clan.reloadClanMember();
                Message msg = new Message(-50);
                msg.writer().writeByte(clan.getCurrMembers());
                for (ClanMember cm : clan.getMembers()) {
                    msg.writer().writeInt((int) cm.id);
                    msg.writer().writeShort(cm.head);
                    msg.writer().writeShort(-1);
                    msg.writer().writeShort(cm.leg);
                    msg.writer().writeShort(cm.body);
                    msg.writer().writeUTF(cm.name);
                    msg.writer().writeByte(cm.role);
                    msg.writer().writeUTF(Util.numberToMoney(cm.powerPoint));
                    msg.writer().writeInt(cm.donate);
                    msg.writer().writeInt(cm.receiveDonate);
                    msg.writer().writeInt(cm.clanPoint);
                    msg.writer().writeInt(cm.joinTime);
                }
                player.sendMessage(msg);
                msg.cleanup();
            }
        } catch (Exception ex) {
            Service.gI().sendThongBao(player, ex.getMessage());
        }
    }

    public void sendMyClan(Player player) {
        if (player == null) {
            return;
        }
        try {
            Message msg = new Message(-53);
            if (player.clan == null) {
                msg.writer().writeInt(-1);
            } else {
                Clan clan = player.clan;
                msg.writer().writeInt(clan.id);
                msg.writer().writeUTF(clan.name);
                msg.writer().writeUTF(clan.slogan);
                msg.writer().writeShort(clan.imgId);
                msg.writer().writeUTF(String.valueOf(clan.powerPoint));
                msg.writer().writeUTF(clan.getLeader().name);
                msg.writer().writeByte(clan.getCurrMembers());
                msg.writer().writeByte(clan.maxMember);
                msg.writer().writeByte(clan.getRole(player));
                msg.writer().writeInt((int) clan.capsuleClan);
                msg.writer().writeByte(clan.level);
                for (ClanMember cm : clan.getMembers()) {
                    Player pl = Client.gI().getPlayer(cm.id);
                    if (pl != null) {
                        cm.powerPoint = pl.nPoint.power;
                    }
                    msg.writer().writeInt(cm.id);
                    msg.writer().writeShort(cm.head);
                    msg.writer().writeShort(-1);
                    msg.writer().writeShort(cm.leg);
                    msg.writer().writeShort(cm.body);
                    msg.writer().writeUTF(cm.name);
                    msg.writer().writeByte(cm.role);
                    msg.writer().writeUTF(Util.numberToMoney(cm.powerPoint));
                    msg.writer().writeInt(cm.donate);
                    msg.writer().writeInt(cm.receiveDonate);
                    msg.writer().writeInt(cm.clanPoint);
                    msg.writer().writeInt(cm.memberPoint);
                    msg.writer().writeInt(cm.joinTime);
                }
                List<ClanMessage> clanMessages = clan.getCurrClanMessages();
                msg.writer().writeByte(clanMessages.size());
                for (ClanMessage cmg : clanMessages) {
                    msg.writer().writeByte(cmg.type);
                    msg.writer().writeInt(cmg.id);
                    msg.writer().writeInt(cmg.playerId);
                    msg.writer()
                            .writeUTF(cmg.type == 2 ? cmg.playerName + " (" + Util.numberToMoney(cmg.playerPower) + ")"
                                    : cmg.playerName);
                    msg.writer().writeByte(cmg.role);
                    msg.writer().writeInt(cmg.time);
                    if (cmg.type == 0) {
                        msg.writer().writeUTF(cmg.text == null ? "" : cmg.text);
                        msg.writer().writeByte(cmg.color);
                    } else if (cmg.type == 1) {
                        msg.writer().writeByte(cmg.receiveDonate);
                        msg.writer().writeByte(cmg.maxDonate);
                        msg.writer().writeByte(cmg.isNewMessage);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    public void sendClanId(Player player) {
        try {
            Message msg = new Message(-61);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt(player.clan == null ? -1 : player.clan.id);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    public void showMenuLeaveClan(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_LEAVE_CLAN, -1,
                "Bạn có chắc chắn rời bang hội không?", "OK", "Từ chối");
    }

    public void showMenuNhuongPc(Player player, int playerId) {
        if (player.clan != null && player.clan.isLeader(player)) {
            ClanMember cm = player.clan.getClanMember(playerId);
            if (cm != null) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.CONFIRM_NHUONG_PC, -1,
                        "Bạn có đồng ý nhường chức bang chủ cho " + cm.name + " ?", new String[]{
                            "Đồng ý", "Từ chối"
                        }, playerId);
            }
        }
    }

    public void changeSlogan(Player player, String slogan) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            clan.slogan = slogan.length() > 250 ? slogan.substring(0, 250) : slogan;
            clan.sendMyClanForAllMember();
            clan.update();
        }
    }

    public void changeFlag(Player player, int imgId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player) && imgId != clan.imgId) {
            FlagBag flagBag = FlagBagService.gI().getFlagBag(imgId);
            if (flagBag != null) {
                if (player.inventory.gold < flagBag.gold) {
                    Service.gI().sendThongBao(player, "Bạn không đủ vàng.");
                    return;
                }
                if (player.inventory.gem < flagBag.gem) {
                    Service.gI().sendThongBao(player, "Bạn không đủ ngọc.");
                    return;
                }
                player.inventory.gold -= flagBag.gold;
                player.inventory.gem -= flagBag.gem;
                PlayerService.gI().sendInfoHpMpMoney(player);
                player.clan.imgId = imgId;
                clan.sendFlagBagForAllMember();
                clan.update();
            }
        }
    }

    public void leaveClan(Player player) {
        Clan clan = player.clan;
        if (clan != null) {
            ClanMember cm = clan.getClanMember((int) player.id);
            if (cm != null) {
                if (clan.isLeader(player)) {
                    Service.gI().sendThongBao(player, "Phải nhường chức bang chủ trước khi rời.");
                    return;
                }
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.role = clan.getRole(player);
                cmg.playerId = (int) player.id;
                cmg.playerName = player.name;
                cmg.text = player.name + " đã rời bang.";
                cmg.color = ClanMessage.RED;

                clan.removeClanMember(cm);
                clan.removeMemberOnline(cm, player);
                player.clan = null;
                player.clanMember = null;
                ClanService.gI().sendMyClan(player);
                ClanService.gI().sendClanId(player);
                Service.gI().sendFlagBag(player);
                ItemTimeService.gI().removeTextDoanhTrai(player);

                clan.sendMyClanForAllMember();
                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);
                clan.update();
            }
        }
    }

    public void catChuc(Player player, int memberId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            ClanMember cm = clan.getClanMember(memberId);
            if (cm != null) {
                ClanMember leader = clan.getLeader();
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.role = leader.role;
                cmg.playerId = leader.id;
                cmg.playerName = leader.name;
                cmg.text = "Cắt chức phó bang của " + cm.name;
                cmg.color = ClanMessage.RED;

                cm.role = Clan.MEMBER;
                clan.sendMyClanForAllMember();
                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);
                clan.update();
            }
        }
    }

    public void kickOut(Player player, int memberId) throws Exception {
        Clan clan = player.clan;
        ClanMember cm = clan.getClanMember(memberId);
        if (clan != null && cm != null
                && (clan.isLeader(player) || (clan.isDeputy(player) && cm.role == Clan.MEMBER))) {

            Player plKicked = clan.getPlayerOnline(memberId);
            ClanMember cmKicker = clan.getClanMember((int) player.id);
            ClanMessage cmg = new ClanMessage(clan);
            cmg.type = 0;
            cmg.role = cmKicker.role;
            cmg.playerId = cmKicker.id;
            cmg.playerName = cmKicker.name;
            cmg.text = "Đuổi " + cm.name + " ra khỏi bang.";
            cmg.color = ClanMessage.RED;

            clan.removeClanMember(cm);
            clan.removeMemberOnline(cm, plKicked);

            if (plKicked != null) {
                plKicked.clan = null;
                plKicked.clanMember = null;
                ClanService.gI().sendMyClan(plKicked);
                ClanService.gI().sendClanId(plKicked);
                Service.gI().sendFlagBag(plKicked);
                ItemTimeService.gI().removeTextDoanhTrai(plKicked);
            } else {
                removeClanPlayer(memberId);
            }
            clan.sendMyClanForAllMember();
            clan.addClanMessage(cmg);
            clan.sendMessageClan(cmg);
            clan.update();
        }
    }

    private void removeClanPlayer(int plId) {
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE player SET clan_id = -1 WHERE id = ?")) {
            ps.setInt(1, plId);
            ps.executeUpdate();
        } catch (Exception ex) {
            Logger.logException(ClanService.class, ex);
        }
    }

    public void phongPho(Player player, int memberId) {
        Clan clan = player.clan;
        if (clan != null && (clan.isLeader(player) || clan.isDeputy(player))) {
            ClanMember cm = clan.getClanMember(memberId);
            if (cm != null && cm.role == Clan.MEMBER) {
                ClanMember promoter = clan.getClanMember((int) player.id);
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.role = promoter.role;
                cmg.playerId = promoter.id;
                cmg.playerName = promoter.name;
                cmg.text = "Phong phó bang cho " + cm.name;
                cmg.color = ClanMessage.RED;

                cm.role = Clan.DEPUTY;
                clan.sendMyClanForAllMember();
                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);
                clan.update();
            } else {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        }
    }

    public void phongPc(Player player, int memberId) {
        Clan clan = player.clan;
        if (clan != null && clan.isLeader(player)) {
            ClanMember leader = clan.getLeader();
            ClanMember newLeader = clan.getClanMember(memberId);
            if (newLeader != null && newLeader.role == Clan.DEPUTY) {
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.role = leader.role;
                cmg.playerId = leader.id;
                cmg.playerName = leader.name;
                cmg.text = "Nhường chức bang chủ cho " + newLeader.name;
                cmg.color = ClanMessage.RED;

                leader.role = Clan.MEMBER;
                newLeader.role = Clan.LEADER;
                clan.update();
                clan.sendMyClanForAllMember();
                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);
            }
        }
    }

    public void chat(Player player, String text) {
        Clan clan = player.clan;
        if (clan != null) {
            ClanMember cm = clan.getClanMember((int) player.id);
            if (cm != null) {
                ClanMessage cmg = new ClanMessage(clan);
                cmg.type = 0;
                cmg.playerId = cm.id;
                cmg.playerName = cm.name;
                cmg.role = cm.role;
                cmg.text = text;
                cmg.color = 0;
                clan.addClanMessage(cmg);
                clan.sendMessageClan(cmg);
            }
        }
    }

    public void rollCallCapsuleClan(Player player) {
        if (player == null || player.clan == null) {
            return;
        }
        Clan clan = player.clan;
        ClanMember cm = clan.getClanMember((int) player.id);
        if (cm == null) {
            return;
        }
        if (utils.TimeUtil.isSameDay(cm.attendanceTime, System.currentTimeMillis())) {
            Service.gI().sendThongBao(player, "Bạn đã điểm danh hôm nay rồi!");
            return;
        }
        cm.attendanceTime = System.currentTimeMillis();
        int reward = 1;
        clan.capsuleClan += reward;
        clan.update();
        ClanService.gI().sendMyClan(player);
        Service.gI().sendThongBao(player, "Điểm danh thành công! +" + reward + " Capsule Bang");
    }

    private void checkDoneTaskJoinClan(Clan clan) {
        if (clan.getMembers().size() >= 2) {
            for (Player player : clan.membersInGame) {
                TaskService.gI().checkDoneTaskJoinClan(player);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void close() {
        String sql = "UPDATE clan SET slogan = ?, img_id = ?, power_point = ?, max_member = ?, clan_point = ?, "
                + "level = ?, members = ?, name_2 = ?, thanhtichbdkb = ?, thanhtichkghd = ?, thanhtichcdrd = ?, thongtin = ?, "
                + "boss_challenge = ? WHERE id = ? LIMIT 1";
        try (Connection con = AlyraManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (Clan clan : Manager.CLANS) {
                JSONArray dataArray = new JSONArray();
                for (ClanMember cm : clan.members) {
                    JSONObject dataObject = new JSONObject();
                    dataObject.put("id", cm.id);
                    dataObject.put("name", cm.name);
                    dataObject.put("head", cm.head);
                    dataObject.put("body", cm.body);
                    dataObject.put("leg", cm.leg);
                    dataObject.put("role", cm.role);
                    dataObject.put("donate", cm.donate);
                    dataObject.put("receive_donate", cm.receiveDonate);
                    dataObject.put("member_point", cm.memberPoint);
                    dataObject.put("clan_point", cm.clanPoint);
                    dataObject.put("join_time", cm.joinTime);
                    dataObject.put("ask_pea_time", cm.timeAskPea);
                    dataObject.put("power", cm.powerPoint);
                    dataArray.add(dataObject.toJSONString());
                }
                String member = dataArray.toJSONString();

                String topBanDoKhoBau = "[" + clan.levelDoneBanDoKhoBau + "," + clan.thoiGianHoanThanhBDKB + ","
                        + System.currentTimeMillis() + "]";
                String topKhiGaHuyDiet = "[" + clan.levelDoneKhiGaHuyDiet + "," + clan.thoiGianHoanThanhKhiGaHuyDiet
                        + "," + System.currentTimeMillis() + "]";
                String topConDuongRanDoc = "[" + clan.levelDoneConDuongRanDoc + ","
                        + clan.thoiGianHoanThanhConDuongRanDoc + "," + System.currentTimeMillis() + "]";
                String thongTinLeader = "[" + clan.getLeader().id + "," + clan.getLeader().name + ","
                        + clan.getLeader().head + "," + clan.getLeader().body + "," + clan.getLeader().leg + "]";

                ps.setString(1, clan.slogan);
                ps.setInt(2, clan.imgId);
                ps.setLong(3, clan.powerPoint);
                ps.setByte(4, clan.maxMember);
                ps.setInt(5, clan.capsuleClan);
                ps.setInt(6, clan.level);
                ps.setString(7, member);
                ps.setString(8, clan.name2);
                ps.setString(9, topBanDoKhoBau);
                ps.setString(10, topKhiGaHuyDiet);
                ps.setString(11, topConDuongRanDoc);
                ps.setString(12, thongTinLeader);
                ps.setString(13, "[" + clan.bossChallengeToday + "," + clan.lastBossChallenge + "]");
                ps.setInt(14, clan.id);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            Logger.logException(ClanService.class, e);
        }
    }

    public int capsule(Clan clan) {
        if (clan != null) {
            switch (clan.level) {
                case 1 -> {
                    return 100;
                }
                case 2 -> {
                    return 300;
                }
                case 3 -> {
                    return 500;
                }
                case 4 -> {
                    return 700;
                }
                case 5 -> {
                    return 900;
                }
                case 6 -> {
                    return 1100;
                }
                case 7 -> {
                    return 1300;
                }
                case 8 -> {
                    return 1500;
                }
                case 9 -> {
                    return 1700;
                }
                case 10 -> {
                    return 1900;
                }
                case 11 -> {
                    return 2100;
                }
            }
        }
        return 999999;
    }
}
