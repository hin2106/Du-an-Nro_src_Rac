package services;

import services.map.MapService;
import services.map.NpcService;
import services.player.PlayerService;
import utils.Functions;
import data.AlyraManager;
import consts.ConstNpc;
import consts.ConstPlayer;
import consts.BossStatus;
import utils.FileIO;
import data.DataGame;
import boss.BossData;
import boss.nhanban.NhanBan;
import boss.learn.TrainingBoss;
import consts.ConstAchievement;
import data.AlyraResultSet;
import interfaces.ISession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import item.Item;
import map.ItemMap;
import mob.Mob;
import player.Pet;
import item.Item.ItemOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

import map.Zone;
import managers.boss.BossManager;
import player.Player;
import network.MySession;
import skill.Skill;
import network.Message;
import server.Client;
import services.map.ChangeMapService;
import utils.Logger;
import utils.TimeUtil;
import utils.Util;

import map.MaBuHold;
import npc.NonInteractiveNPC;
import npc.Npc;
import server.Manager;
import shop.ItemShop;
import shop.Shop;

public class Service {

    private static final long REGISTER_COOLDOWN_MS = 5_000L;
    private static final ConcurrentHashMap<String, Long> LAST_REGISTER_BY_IP = new ConcurrentHashMap<>();

    public static final int[] flagTempId = { 363, 364, 365, 366, 367, 368, 369, 370, 371, 519, 520, 747 };
    public static final int[] flagIconId = { 2761, 2330, 2323, 2327, 2326, 2324, 2329, 2328, 2331, 4386, 4385, 2325 };
    short[][] con_đĩ = { { 281, 361, 351 }, { 512, 513, 536 }, { 514, 515, 537 } };

    private static Service instance;

    public static Service gI() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public void sendBadgesPlayer(Player player, int sec, int idImg) {
        Message msg;
        try {
            msg = new Message(24);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(sec);
            msg.writer().writeShort(idImg);
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (IOException e) {
        }
    }

    public void sendHaveChibiFollowToAllMap(Player pl) {
        if (pl.zone != null) {
            for (Player plMap : pl.zone.getPlayers()) {
                if (plMap != null && plMap.isPl()) {
                    sendChibiFollowToMe(plMap, pl);
                }
            }
        }
    }

    public void sendChibiFollowToMe(Player me, Player pl) {
        try {
            if (pl.typeChibi == -1 || !pl.effectSkill.isChibi) {
                Message msg = new Message(31);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeByte(0);
                me.sendMessage(msg);
                msg.cleanup();
                return;
            }

            short smallId = (short) (pl.typeChibi + 5000);
            Message msg = new Message(31);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(1);
            msg.writer().writeShort(smallId);
            msg.writer().writeByte(1);
            int[] fr = { 0, 1, 2 };
            msg.writer().writeByte(fr.length);
            for (int frame : fr) {
                msg.writer().writeByte(frame);
            }
            msg.writer().writeShort(32);
            msg.writer().writeShort(32);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendChibi(Player player) {
        try {
            if (player.typeChibi == -1 || !player.effectSkill.isChibi) {
                Message msg = new Message(31);
                msg.writer().writeInt((int) player.id);
                msg.writer().writeByte(0);
                sendMessAllPlayerInMap(player, msg);
                msg.cleanup();
                return;
            }

            short smallId = (short) (player.typeChibi + 5000);
            Message msg = new Message(31);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(1);
            msg.writer().writeShort(smallId);
            msg.writer().writeByte(1);

            int[] fr = { 0, 1, 2 };
            msg.writer().writeByte(fr.length);
            for (int frame : fr) {
                msg.writer().writeByte(frame);
            }

            msg.writer().writeShort(32);
            msg.writer().writeShort(32);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        }
    }

    public void ClosePanel(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(7);
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendMessAllPlayer(Message msg) {
        PlayerService.gI().sendMessageAllPlayer(msg);
    }

    public void sendMessAllPlayerIgnoreMe(Player player, Message msg) {
        PlayerService.gI().sendMessageIgnore(player, msg);
    }

    public void sendMessAllPlayerInMap(Zone zone, Message msg) throws IOException {
        if (zone == null || msg == null) {
            return;
        }
        List<Player> playersSnapshot;
        synchronized (zone) {
            playersSnapshot = new ArrayList<>(zone.getPlayers());
        }

        if (playersSnapshot.isEmpty()) {
            msg.dispose();
            return;
        }

        byte[] data = msg.getData();
        for (Player pl : playersSnapshot) {
            if (pl != null && pl.session != null) {
                try {
                    Message clone = new Message(msg.command);
                    clone.writer().write(data);
                    pl.sendMessage(clone);
                    clone.cleanup();
                } catch (Exception e) {
                    Logger.logException(Service.class, e);
                }
            }
        }
        msg.cleanup();
    }

    public void sendMessAllPlayerInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.dispose();
            return;
        }
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            if (player instanceof TrainingBoss || player instanceof NonInteractiveNPC) {
                List<Player> playersSnapshot;
                synchronized (player.zone) {
                    playersSnapshot = new ArrayList<>(player.zone.getPlayers());
                }
                if (playersSnapshot.isEmpty()) {
                    msg.dispose();
                    return;
                }
                for (Player pl : playersSnapshot) {
                    if (pl != null
                            && (player instanceof NonInteractiveNPC || ((TrainingBoss) player).playerAtt.equals(pl))) {
                        pl.sendMessage(msg);
                    }
                }
            } else {
                player.sendMessage(msg);
            }
        } else {
            List<Player> playersSnapshot;
            synchronized (player.zone) {
                playersSnapshot = new ArrayList<>(player.zone.getPlayers());
            }
            if (playersSnapshot.isEmpty()) {
                msg.dispose();
                return;
            }
            for (Player pl : playersSnapshot) {
                if (pl != null && pl.getSession() != null && pl.isPl()) {
                    pl.sendMessage(msg);
                }
            }
        }
        msg.cleanup();
    }

    public void sendMessAnotherNotMeInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.cleanup();
            return;
        }
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            if (player instanceof TrainingBoss || player instanceof NonInteractiveNPC) {
                List<Player> playersSnapshot;
                synchronized (player.zone) {
                    playersSnapshot = new ArrayList<>(player.zone.getPlayers());
                }
                if (playersSnapshot.isEmpty()) {
                    msg.dispose();
                    return;
                }
                for (Player pl : playersSnapshot) {
                    if (pl != null && !pl.equals(player)
                            && (player instanceof NonInteractiveNPC || ((TrainingBoss) player).playerAtt.equals(pl))) {
                        pl.sendMessage(msg);
                    }
                }
            }
        } else {
            List<Player> playersSnapshot;
            synchronized (player.zone) {
                playersSnapshot = new ArrayList<>(player.zone.getPlayers());
            }
            if (playersSnapshot.isEmpty()) {
                msg.dispose();
                return;
            }
            for (Player pl : playersSnapshot) {
                if (pl != null && pl.getSession() != null && !pl.equals(player) && pl.isPl()) {
                    pl.sendMessage(msg);
                }
            }
        }
        msg.cleanup();
    }

    public void Send_Info_NV(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);// Cập nhật máu
            msg.writer().writeInt((int) pl.id);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hp));
            msg.writer().writeByte(0);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (IOException e) {

        }
    }

    public void Send_Info_NV_do_Injure(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);// Cập nhật máu
            msg.writer().writeInt((int) pl.id);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hp));
            msg.writer().writeByte(2);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (IOException e) {

        }
    }

    public void sendInfoPlayerEatPea(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);
            msg.writer().writeInt((int) pl.id);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hp));
            msg.writer().writeByte(1);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void resetPoint(Player player, int x, int y) {
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            msg = new Message(46);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            player.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
        }
    }

    public void clearMap(Player player) {
        Message msg;
        try {
            msg = new Message(-22);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void chat(Player player, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeUTF(text);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        }

    }

    public void chatJustForMe(Player me, Player plChat, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeUTF(text);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public Npc getNpc(Player player) {
        Npc closestNpc = null;
        double closestDistance = Double.MAX_VALUE;
        for (Npc npc : player.zone.map.npcs) {
            double distance = Util.getDistance(player, npc);
            if (distance <= 150 && distance < closestDistance) {
                closestDistance = distance;
                closestNpc = npc;
            }
        }
        return closestNpc;
    }

    public void Transport(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(pl.type);
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void Transport(Player pl, int type) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(type);
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void point(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        player.nPoint.calPoint();
        Send_Info_NV(player);
        if (!player.isPet && !player.isBoss && !player.isNewPet) {
            Message msg = null;
            try {
                msg = new Message(-42);
                msg.writer().writeInt((int)player.nPoint.hpg);
                msg.writer().writeInt((int)player.nPoint.mpg);
                msg.writer().writeInt((int)player.nPoint.dameg);
                msg.writeSmartLong(Util.maxIntValue(player.nPoint.hpMax));
                msg.writeSmartLong(Util.maxIntValue(player.nPoint.mpMax));
                msg.writeSmartLong(Util.maxIntValue(player.nPoint.hp));
                msg.writeSmartLong(Util.maxIntValue(player.nPoint.mp));
                msg.writer().writeByte(player.nPoint.speed);
                msg.writer().writeByte(20);
                msg.writer().writeByte(20);
                msg.writer().writeByte(1);
                msg.writeSmartLong(Util.maxIntValue(player.nPoint.dame));
                msg.writer().writeLong(player.nPoint.def);
                msg.writer().writeByte(player.nPoint.crit);
                msg.writer().writeLong(player.nPoint.tiemNang);
                msg.writer().writeShort(100);
                msg.writer().writeInt(player.nPoint.defg);
                msg.writer().writeByte(player.nPoint.critg);
               // msg.writer().writeByte(player.nPoint.tlDef);
             //   msg.writer().writeShort(player.nPoint.tlSDCM);
                player.sendMessage(msg);
            } catch (IOException e) {
                Logger.logException(Service.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public String name(Player player) {
        if (player.isPl() && player.clan != null) {
            try {
                if (!player.clan.name2.isEmpty()) {
                    return "[" + player.clan.name2 + "] " + player.name;
                } else if (player.clan.name.length() > 3) {
                    return "[" + player.clan.name.substring(0, 3) + "] " + player.name;
                } else {
                    return "[" + player.clan.name + "] " + player.name;
                }
            } catch (Exception e) {
            }
        } else if (player.name == null) {
            return "";
        }
        return player.name;
    }

    public void player(Player pl) {
        if (pl == null) {
            return;
        }
        if (pl.playerTask == null || pl.playerTask.taskMain == null) {
            return;
        }
        Message msg;
        try {
            msg = messageSubCommand((byte) 0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.playerTask.taskMain.id);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(pl.head);
            msg.writer().writeUTF(pl.name);
            msg.writer().writeByte(0); // cPK
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeLong(pl.nPoint.power);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(pl.gender);
            ArrayList<Skill> skills = (ArrayList<Skill>) pl.playerSkill.skills;
            msg.writer().writeByte(pl.playerSkill.getSizeSkill());
            for (Skill skill : skills) {
                if (skill.skillId != -1) {
                    msg.writer().writeShort(skill.skillId);
                }
            }
            if (pl.getSession().version >= 214) {
                msg.writer().writeLong(pl.inventory.gold);
            } else {
                msg.writer().writeInt((int) pl.inventory.gold);
            }
            msg.writer().writeInt(pl.inventory.ruby);
            msg.writer().writeInt(pl.inventory.gem);
            ArrayList<Item> itemsBody = (ArrayList<Item>) pl.inventory.itemsBody;
            msg.writer().writeByte(itemsBody.size());
            for (Item item : itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeShort(itemOption.optionTemplate.id);
                        msg.writer().writeInt(itemOption.param);
                    }
                }

            }
            ArrayList<Item> itemsBag = (ArrayList<Item>) pl.inventory.itemsBag;
            msg.writer().writeByte(itemsBag.size());
            for (int i = 0; i < itemsBag.size(); i++) {
                Item item = itemsBag.get(i);
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeShort(itemOption.optionTemplate.id);
                        msg.writer().writeInt(itemOption.param);
                    }
                }

            }
            ArrayList<Item> itemsBox = (ArrayList<Item>) pl.inventory.itemsBox;
            msg.writer().writeByte(itemsBox.size());
            for (int i = 0; i < itemsBox.size(); i++) {
                Item item = itemsBox.get(i);
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeShort(itemOption.optionTemplate.id);
                        msg.writer().writeInt(itemOption.param);
                    }
                }
            }
            DataGame.sendHeadAvatar(msg);
            // Nơi con đĩ xuất hiện
            msg.writer().writeShort(con_đĩ[pl.gender][0]);
            msg.writer().writeShort(con_đĩ[pl.gender][1]);
            msg.writer().writeShort(con_đĩ[pl.gender][2]);

            msg.writer().writeByte(pl.fusion.typeFusion != ConstPlayer.NON_FUSION ? 1 : 0);
            msg.writer().writeInt(pl.deltaTime);

            msg.writer().writeByte(pl.isNewMember ? 1 : 0);

            msg.writer().writeShort(pl.getAura());
            msg.writer().writeByte(pl.getEffFront());
            msg.writer().writeShort(pl.getHat());
            pl.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public Message messageNotLogin(byte command) throws IOException {
        Message ms = new Message(-29);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageNotMap(byte command) throws IOException {
        Message ms = new Message(-28);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageSubCommand(byte command) throws IOException {
        Message ms = new Message(-30);
        ms.writer().writeByte(command);
        return ms;
    }
        public Message playGuest(String username, String password) throws IOException {
        Message msg = new Message(-101);
        msg.writer().writeUTF(username);
        msg.writer().writeUTF(password);
        return msg;
    }

    public void addSMTN(Player player, byte type, long param, boolean isOri) {
        if (player.isPet && player.nPoint != null) {
            player.nPoint.powerUp(param);
            player.nPoint.tiemNangUp(param);
            Player master = ((Pet) player).master;
            param = (long) (param * 0.5);
            param = master.nPoint.calSubTNSM(param);
            master.nPoint.powerUp(param);
            master.nPoint.tiemNangUp(param);

            addSMTN(master, type, param, true);
        } else if (player.isBot) {
            player.nPoint.power += param;
            player.nPoint.tiemNang += param;
        } else {
            if (player.nPoint == null || player.nPoint.power > player.nPoint.getPowerLimit()) {
                return;
            }
            if (player.masterId > 0) {
                Player master = Client.gI().getPlayer(player.masterId);
                if (master != null) {
                    long bonus = (long) (param * 0.2);
                    master.nPoint.powerUp(bonus);
                    master.nPoint.tiemNangUp(bonus);
                    PlayerService.gI().sendTNSM(master, type, (int)bonus);
                }
            }
            if (player.discipleId > 0) {
                Player disciple = Client.gI().getPlayer(player.discipleId);
                if (disciple != null) {
                    long bonus = (long) (param * 0.2);
                    disciple.nPoint.powerUp(bonus);
                    disciple.nPoint.tiemNangUp(bonus);
                    PlayerService.gI().sendTNSM(disciple, type, (int)bonus);
                }
            }
            switch (type) {
                case 1:
                    player.nPoint.tiemNangUp(param);
                    break;
                case 2:
                    player.nPoint.powerUp(param);
                    player.nPoint.tiemNangUp(param);
                    break;
                default:
                    player.nPoint.powerUp(param);
                    break;
            }
            PlayerService.gI().sendTNSM(player, type, (int) param);
            if (isOri) {
                if (player.clan != null) {
                    player.clan.addSMTNClan(player, param);
                }
            }
            try {
                if (player.nPoint.power >= 40_000_000_000L) {
                    player.masterId = -1;
                    player.discipleId = -1;
                }
            } catch (Exception ignored) {
            }
        }
    }

    public String get_HanhTinh(int hanhtinh) {
        switch (hanhtinh) {
            case 0:
                return "Trái Đất";
            case 1:
                return "Namếc";
            case 2:
                return "Xayda";
            default:
                return "";
        }
    }

    public List<String> ListCaption(int gender) {
        List<String> Captions = new ArrayList<>();
        Captions.add("Tân thủ");
        Captions.add("Tập sự sơ cấp");
        Captions.add("Tập sự trung cấp");
        Captions.add("Tập sự cao cấp");
        Captions.add("Tân binh");
        Captions.add("Chiến binh");
        Captions.add("Chiến binh cao cấp");
        Captions.add("Vệ binh");
        Captions.add("Vệ binh hoàng gia");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 1");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 2");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 3");
        Captions.add("Siêu " + (gender == 0 ? "nhân" : get_HanhTinh(gender)) + " cấp 4");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 1");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 2");
        Captions.add("Thần " + get_HanhTinh(gender) + " cấp 3");
        Captions.add("Giới Vương Thần cấp 1");
        Captions.add("Giới Vương Thần cấp 2");
        Captions.add("Giới Vương Thần cấp 3");
        Captions.add("Thần hủy diệt cấp 1");
        Captions.add("Thần hủy diệt cấp 2");
        Captions.add("Thần hủy diệt cấp 3");
        return Captions;
    }

    public String getCurrStrLevel(Player pl) {
        return ListCaption(pl.gender).get(getCurrLevel(pl));
    }

    public int getCurrLevel(Player pl) {
        if (pl.nPoint == null) {
            return 0;
        }
        long sucmanh = pl.nPoint.power;
        if (sucmanh < 3000) {
            return 0;
        } else if (sucmanh < 15000) {
            return 1;
        } else if (sucmanh < 40000) {
            return 2;
        } else if (sucmanh < 90000) {
            return 3;
        } else if (sucmanh < 170000) {
            return 4;
        } else if (sucmanh < 340000) {
            return 5;
        } else if (sucmanh < 700000) {
            return 6;
        } else if (sucmanh < 1500000) {
            return 7;
        } else if (sucmanh < 15000000) {
            return 8;
        } else if (sucmanh < 150000000) {
            return 9;
        } else if (sucmanh < 1500000000) {
            return 10;
        } else if (sucmanh < 5000000000L) {
            return 11;
        } else if (sucmanh < 10000000000L) {
            return 12;
        } else if (sucmanh < 40000000000L) {
            return 13;
        } else if (sucmanh < 50010000000L) {
            return 14;
        } else if (sucmanh < 60010000000L) {
            return 15;
        } else if (sucmanh < 70010000000L) {
            return 16;
        } else if (sucmanh < 80010000000L) {
            return 17;
        } else if (sucmanh < 100010000000L) {
            return 18;
        } else if (sucmanh < 11100010000000L) {
            return 19;
        }
        return 20;
    }

    public void hsChar(Player pl, long hp, long mp) {
        try {
            if (pl.isPl() && pl.effectSkill != null && pl.effectSkill.isBodyChangeTechnique) {
                PlayerService.gI().changeAndSendTypePK(pl, 5);
            }
            pl.setJustRevivaled();
            pl.nPoint.setHp(hp);
            pl.nPoint.setMp(mp);

            if (pl.isPl()) {
                Message msg = new Message(-16);
                pl.sendMessage(msg);
                msg.cleanup();
                PlayerService.gI().sendInfoHpMpMoney(pl);
            }
            Message msg = messageSubCommand((byte) 15);
            msg.writer().writeInt((int) pl.id);
            msg.writeSmartLong(Util.maxIntValue(hp));
            msg.writeSmartLong(Util.maxIntValue(mp));
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            Send_Info_NV(pl);
            PlayerService.gI().sendInfoHpMp(pl);

            AchievementService.gI().checkDoneTask(pl, ConstAchievement.THANH_HOI_SINH);

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void charDie(Player pl) {
        if (pl == null || pl.location == null) {
            return;
        }
        Message msg;
        try {
            if (!pl.isPet && !pl.isNewPet && pl.isPl()) {
                msg = new Message(-17);
                msg.writer().writeByte((int) pl.id);
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                pl.sendMessage(msg);
                msg.cleanup();
            } else if (pl.isPet) {
                ((Pet) pl).lastTimeDie = System.currentTimeMillis();
            }
            msg = new Message(-8);
            msg.writer().writeShort((int) pl.id);
            int cPk = 0;
            msg.writer().writeByte(cPk); // cpk
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();

            Send_Info_NV(pl);

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void attackMob(Player pl, int mobId, boolean isMobMe, int masterId) {
        if (pl != null && pl.zone != null) {
            if (!isMobMe) {
                for (Mob mob : pl.zone.mobs) {
                    if (mob.id == mobId) {
                        SkillService.gI().useSkill(pl, null, mob, -1, null);
                        break;
                    }
                }
            } else {
                Player plAtt = pl.zone.getPlayerInMap(masterId);
                if (plAtt != null && SkillService.gI().canAttackPlayer(pl, plAtt)) {
                    Mob mob = plAtt.mobMe;
                    if (mob != null) {
                        mob.injured(pl, pl.nPoint.getDameAttack(false), true);
                    }
                }
            }
        }
    }

    public void Send_Caitrang(Player player) {
        if (player != null) {
            Message msg;
            try {
                msg = new Message(-90);
                msg.writer().writeByte(1);// check type
                msg.writer().writeInt((int) player.id); // id player
                short head = player.getHead();
                short body = player.getBody();
                short leg = player.getLeg();

                msg.writer().writeShort(head);// set head
                msg.writer().writeShort(body);// setbody
                msg.writer().writeShort(leg);// set leg
                msg.writer().writeByte(player.effectSkill.isMonkey ? 1 : 0);// set khỉ
                sendMessAllPlayerInMap(player, msg);
                msg.cleanup();
            } catch (IOException e) {
            }
        }
    }

    public void setNotMonkey(Player player) {
        Message msg;
        try {
            msg = new Message(-90);
            msg.writer().writeByte(-1);
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void sendFlagBag(Player pl) {
        Message msg;
        try {
            msg = new Message(-64);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(pl.getFlagBag());
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendThongBaoOK(Player pl, String text) {
        if (pl.isPet || pl.isNewPet) {
            return;
        }
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void sendThongBaoOK(MySession session, String text) {
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendThongBaoAllPlayer(String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            this.sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendBigMessage(Player player, int iconId, String text) {
        try {
            Message msg;
            msg = new Message(-70);
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {

        }
    }

    public void sendThongBaoFromAdmin(Player player, String text) {
        sendBigMessage(player, 1139, text);
    }

    public void sendThongBao(Player pl, String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            pl.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
        }
    }

    public void sendThongBao(List<Player> pl, String thongBao) {
        for (int i = 0; i < pl.size(); i++) {
            Player ply = pl.get(i);
            if (ply != null) {
                this.sendThongBao(ply, thongBao);
            }
        }
    }

    public void sendThongBaoToAnotherNotMe(Player me, String text) {
        for (int i = 0; i < Client.gI().getPlayers().size(); i++) {
            Player pl = Client.gI().getPlayers().get(i);
            if (pl != null && !pl.equals(me)) {
                this.sendThongBao(pl, text);
            }
        }
    }

    public void sendMoney(Player pl) {
        Message msg;
        try {
            dragonpass.DragonPassService.gI().onMoneyChanged(pl);
            msg = new Message(6);
            if (pl.getSession().version >= 214) {
                msg.writer().writeLong(pl.inventory.gold);
            } else {
                msg.writer().writeInt((int) pl.inventory.gold);
            }
            msg.writer().writeInt(pl.inventory.gem);
            msg.writer().writeInt(pl.inventory.ruby);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendToAntherMePickItem(Player player, int itemMapId) {
        Message msg;
        try {
            msg = new Message(-19);
            msg.writer().writeShort(itemMapId);
            msg.writer().writeInt((int) player.id);
            sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {

        }
    }

    public void reload_HP_NV(Player pl) {
        Message msg = null;
        try {
            msg = messageSubCommand((byte) 9);
            msg.writer().writeInt((int) pl.id);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hp));
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hpMax));
            sendMessAllPlayer(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void openFlagUI(Player pl) {
        Message msg = null;
        try {
            msg = new Message((byte) -103);
            msg.writer().writeByte(0); // action 0 = danh sách flag
            msg.writer().writeByte((byte) flagTempId.length); // số lượng flag

            for (int i = 0; i < flagTempId.length; i++) {
                short itemId = (short) flagTempId[i];
                msg.writer().writeShort(itemId); // ID template item

                // Item options cho mỗi flag
                msg.writer().writeByte(1); // 1 option
                msg.writer().writeShort(i == 0 ? 73 : 88); // option ID (ví dụ)
                msg.writer().writeInt(i == 0 ? 0 : 5); // param value
            }

            pl.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void changeFlag(Player pl, int index) {
        Message msg;
        try {
            pl.cFlag = (byte) index;
            msg = new Message(-103);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(index);
            Service.gI().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(index);
            msg.writer().writeShort(flagIconId[index]);
            Service.gI().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            if (pl.pet != null) {
                pl.pet.cFlag = (byte) index;
                msg = new Message(-103);
                msg.writer().writeByte(1);
                msg.writer().writeInt((int) pl.pet.id);
                msg.writer().writeByte(index);
                Service.gI().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();

                msg = new Message(-103);
                msg.writer().writeByte(2);
                msg.writer().writeByte(index);
                msg.writer().writeShort(index > -1 ? flagIconId[index] : index);
                Service.gI().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();
            }
            pl.idMark.setLastTimeChangeFlag(System.currentTimeMillis());

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void sendFlagPlayerToMe(Player me, Player pl) {
    Message msg = null;
    try {
        msg = new Message((byte) -103);
        msg.writer().writeByte(2); // action 2
        msg.writer().writeByte(pl.cFlag); // flag value
        msg.writer().writeShort((short) flagIconId[pl.cFlag]); // image ID
        me.sendMessage(msg);
    } catch (IOException e) {
        Logger.logException(Service.class, e);
    } finally {
        if (msg != null) {
            msg.cleanup();
        }
    }
}
    // public void chooseFlag(Player pl, byte flagIndex) {
    //     if (flagIndex < 0 || flagIndex >= flagTempId.length) {
    //         return;
    //     }

    //     // Kiểm tra cooldown
    //     if (!Util.canDoWithTime(pl.idMark.getLastTimeChangeFlag(), 60000)) {
    //         Service.gI().sendThongBao(pl,
    //                 "Chờ " + TimeUtil.getTimeLeft(pl.idMark.getLastTimeChangeFlag(), 60) + " nữa");
    //         return;
    //     }

    //     // Kiểm tra map không được phép đổi flag
    //     if (MapService.gI().isMapBlackBallWar(pl.zone.map.mapId) ||
    //             MapService.gI().isMapMaBu(pl.zone.map.mapId)) {
    //         Service.gI().sendThongBao(pl, "Không có thể thay đổi flag ở đây");
    //         return;
    //     }

    //     try {
    //         pl.cFlag = flagIndex; // Lưu flag cá nhân
    //         pl.idMark.setLastTimeChangeFlag(System.currentTimeMillis());

    //         // Message 1: Cập nhật flag cho nhân vật
    //         Message msg1 = new Message((byte) -103);
    //         msg1.writer().writeByte(1); // action 1
    //         msg1.writer().writeInt((int) pl.id); // character ID
    //         msg1.writer().writeByte(flagIndex); // flag value
    //         Service.gI().sendMessAllPlayerInMap(pl, msg1);
    //         msg1.cleanup();

    //         // Message 2: Cập nhật ảnh flag
    //         Message msg2 = new Message((byte) -103);
    //         msg2.writer().writeByte(2); // action 2
    //         msg2.writer().writeByte(flagIndex); // flag value
    //         msg2.writer().writeShort((short) flagIconId[flagIndex]); // image ID
    //         Service.gI().sendMessAllPlayerInMap(pl, msg2);
    //         msg2.cleanup();

    //         // Cập nhật flag pet nếu có
    //         if (pl.pet != null) {
    //             pl.pet.cFlag = flagIndex;

    //             Message msgPet1 = new Message((byte) -103);
    //             msgPet1.writer().writeByte(1);
    //             msgPet1.writer().writeInt((int) pl.pet.id);
    //             msgPet1.writer().writeByte(flagIndex);
    //             Service.gI().sendMessAllPlayerInMap(pl.pet, msgPet1);
    //             msgPet1.cleanup();

    //             Message msgPet2 = new Message((byte) -103);
    //             msgPet2.writer().writeByte(2);
    //             msgPet2.writer().writeByte(flagIndex);
    //             msgPet2.writer()
    //                     .writeShort((short) (flagIndex == -1 ? flagIconId[flagIndex] : flagIconId[flagIndex - 1]));
    //             Service.gI().sendMessAllPlayerInMap(pl.pet, msgPet2);
    //             msgPet2.cleanup();
    //         }

    //     } catch (IOException e) {
    //         Logger.logException(Service.class, e);
    //     }
    // }


    public void chooseFlag(Player pl, byte flagIndex) {
    if (flagIndex < 0 || flagIndex >= flagTempId.length) {
        return;
    }

    // Kiểm tra cooldown
    if (!Util.canDoWithTime(pl.idMark.getLastTimeChangeFlag(), 60000)) {
       Service.gI().sendThongBao(pl,
                   "Chờ " + TimeUtil.getTimeLeft(pl.idMark.getLastTimeChangeFlag(), 60) + " nữa");
        return;
    }

    try {
        pl.cFlag = flagIndex;
        pl.idMark.setLastTimeChangeFlag(System.currentTimeMillis());

        // Message 1: Cập nhật flag nhân vật
        Message msg1 = new Message((byte) -103);
        msg1.writer().writeByte(1);
        msg1.writer().writeInt((int) pl.id);
        msg1.writer().writeByte(flagIndex);
        Service.gI().sendMessAllPlayerInMap(pl, msg1);
        msg1.cleanup();

        // Message 2: Cập nhật ảnh flag
        Message msg2 = new Message((byte) -103);
        msg2.writer().writeByte(2);
        msg2.writer().writeByte(flagIndex);
        msg2.writer().writeShort((short) flagIconId[flagIndex]);
        Service.gI().sendMessAllPlayerInMap(pl, msg2);
        msg2.cleanup();

    } catch (IOException e) {
        Logger.logException(Service.class, e);
    }
}
    public void attackPlayer(Player pl, int idPlAnPem) {
        Player player;
        if (MapService.gI().isMapOffline(pl.zone.map.mapId)) {
            player = pl.zone.getPlayerInMapOffline(pl, idPlAnPem);
        } else {
            player = pl.zone.getPlayerInMap(idPlAnPem);
        }
        SkillService.gI().useSkill(pl, player, null, -1, null);
    }

    public void releaseCooldownSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                skill.lastTimeUseThisSkill = System.currentTimeMillis() - skill.coolDown;
                int leftTime = 0;
                msg.writer().writeInt(leftTime);
            }
            pl.sendMessage(msg);
            pl.nPoint.setMp(pl.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();

        } catch (IOException e) {
        }
    }

    public void sendTimeSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                msg.writer().writeInt(timeLeft);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendTimeSkill(Player pl, Skill skill) {
        Message msg;
        try {
            msg = new Message(-94);
            msg.writer().writeShort(skill.skillId);
            int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
            if (timeLeft < 0) {
                timeLeft = 0;
            }
            msg.writer().writeInt(timeLeft);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void releaseCooldownSkill(Player pl, Skill skill) {
        Message msg;
        try {
            msg = new Message(-94);
            msg.writer().writeShort(skill.skillId);
            skill.lastTimeUseThisSkill = System.currentTimeMillis() - skill.coolDown;
            int leftTime = 0;
            msg.writer().writeInt(leftTime);
            pl.sendMessage(msg);
            pl.nPoint.setMp(pl.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void dropItemMap(map.Zone zone, map.ItemMap item) {
        if (!boss.drop.BossDropService.gI().prepareLegacyDrop(item)) {
            return;
        }
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id); 
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y); 
            msg.writer().writeInt(3);
            sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (IOException e) {
            e.printStackTrace(); // Nên thêm dòng này để dễ debug nếu có lỗi
        }
    }

    public void dropItemMapForMe(Player player, ItemMap item) {
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt(3);//
            player.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void showInfoPet(Player pl) {
        if (pl != null && pl.pet != null) {
            Message msg;
            try {
                msg = new Message(-107);
                msg.writer().writeByte(2);
                msg.writer().writeShort(pl.pet.getAvatar());
                msg.writer().writeByte(pl.pet.inventory.itemsBody.size());

                for (Item item : pl.pet.inventory.itemsBody) {
                    if (!item.isNotNullItem()) {
                        msg.writer().writeShort(-1);
                    } else {
                        msg.writer().writeShort(item.template.id);
                        msg.writer().writeInt(item.quantity);
                        msg.writer().writeUTF(item.getInfo());
                        msg.writer().writeUTF(item.getContent());

                        int countOption = item.itemOptions.size();
                        msg.writer().writeByte(countOption);
                        for (ItemOption iop : item.itemOptions) {
                            msg.writer().writeShort(iop.optionTemplate.id);
                            msg.writer().writeInt(iop.param);
                        }
                    }
                }

                msg.writeSmartLong(pl.pet.nPoint.hp); // hp
                msg.writeSmartLong(pl.pet.nPoint.hpMax); // hpfull
                msg.writeSmartLong(pl.pet.nPoint.mp); // mp
                msg.writeSmartLong(pl.pet.nPoint.mpMax); // mpfull
                msg.writeSmartLong(pl.pet.nPoint.dame); // damefull
                msg.writer().writeUTF(pl.pet.name); // name
                msg.writer().writeUTF(Service.gI().getCurrStrLevel(pl.pet));
                msg.writer().writeLong(pl.pet.nPoint.power); // power
                msg.writer().writeLong(pl.pet.nPoint.tiemNang); // tiềm năng
                msg.writer().writeByte(pl.pet.status); // status
                msg.writer().writeShort(pl.pet.nPoint.stamina); // stamina
                msg.writer().writeShort(pl.pet.nPoint.maxStamina); // stamina full
                msg.writer().writeByte(pl.pet.nPoint.crit); // crit
                msg.writer().writeLong(pl.pet.nPoint.def); // def
               //  msg.writer().writeShort(pl.pet.nPoint.defg);
              // msg.writer().writeByte(pl.pet.nPoint.critg);
                int sizeSkill = pl.pet.playerSkill.skills.size();
                if (pl.pet.typePet == 4 || pl.pet.typePet == 5 || pl.pet.typePet == 6) {
                    msg.writer().writeByte(5);
                } else {
                    msg.writer().writeByte(4);
                }
                for (int i = 0; i < sizeSkill; i++) {
                    if (pl.pet.playerSkill.skills.get(i).skillId != -1) {
                        msg.writer().writeShort(pl.pet.playerSkill.skills.get(i).skillId);
                    } else {
                        switch (i) {
                            case 1 -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 150tr để mở");
                            }
                            case 2 -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 1tỷ5 để mở");
                            }
                            case 3 -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 20tỷ để mở");
                            }
                            case 4 -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 60tỷ để mở");
                            }
                            default -> {
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 60tỷ để mở");
                            }
                        }
                    }
                }

                pl.sendMessage(msg);
                msg.cleanup();
            } catch (IOException e) {
                Logger.logException(Service.class,
                        e);
            }
        }
    }

    public void sendSpeedPlayer(Player pl, int speed) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 8);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(speed != -1 ? speed : pl.nPoint.speed);
            pl.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void setPos(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void setPos2(Player player, int x, int y) {
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void setPos0(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(0);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void getPlayerMenu(Player player, int playerId) {
        Message msg = null;
        try {
            if (player == null || player.zone == null) {
                return;
            }

            msg = new Message(-79);
            Player pl = player.zone.getPlayerInMap(playerId);

            // Gửi thông tin player (power, level)
            if (pl != null && pl.nPoint != null) {
                msg.writer().writeInt(playerId);
                msg.writer().writeLong(pl.nPoint.power);
                msg.writer().writeUTF(Service.gI().getCurrStrLevel(pl));
                player.sendMessage(msg);
            }
            msg.cleanup();
            msg = null;

            // Kiểm tra nếu đang accept trade thì return
            if (player.idMark.isAcpTrade()) {
                player.idMark.setAcpTrade(false);
                return;
            }

            // Kiểm tra và hiển thị menu mua cải trang
            if (pl != null && pl.inventory != null && pl.inventory.itemsBody != null
                    && pl.inventory.itemsBody.size() > 5) {
                item.Item disguise = pl.inventory.itemsBody.get(5);

                // Kiểm tra disguise có hợp lệ không
                if (disguise == null || !disguise.isNotNullItem()) {
                    return;
                }

                // Kiểm tra template
                if (disguise.template == null) {
                    return;
                }

                try {
                    // Chặn cải trang có hạn sử dụng (option 93)
                    if (disguise.isHaveOption(93)) {
                        return;
                    }
                    // Chặn cải trang bị khóa (option 30)
                    if (disguise.isHaveOption(30)) {
                        return;
                    }
                    // Chặn cải trang không thể bán lại (option 154)
                    if (disguise.isHaveOption(154)) {
                        return;
                    }
                } catch (Exception e) {
                    Logger.logException(Service.class, e);
                    return;
                }

                // Tính giá cải trang
                int storeGem = 0;
                ItemShop is = null;

                try {
                    for (Shop shop : Manager.SHOPS) {
                        if (shop == null) {
                            continue;
                        }
                        ItemShop found = shop.getItemShop(disguise.template.id);
                        if (found != null) {
                            is = found;
                            break;
                        }
                    }
                } catch (Exception e) {
                    Logger.logException(Service.class, e);
                }

                // Chặn cải trang không phải bán bằng GEM
                if (is != null) {
                    if (is.typeSell != 1) { // 1 = COST_GEM
                        return;
                    }
                    storeGem = Math.max(is.cost, disguise.template.gem);
                } else if (disguise.template.gem > 0) {
                    storeGem = disguise.template.gem;
                } else {
                    return; // Không có giá gem
                }

                if (storeGem > 0) {
                    int localGem = (storeGem * 95) / 100;
                    SubMenuService.SubMenu buyDisguise = new SubMenuService.SubMenu(
                            SubMenuService.BUY_DISGUISE,
                            "Cải trang",
                            Util.numberToMoney(localGem) + " ngọc xanh");
                    SubMenuService.gI().showSubMenu(player, buyDisguise);
                }
            }
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hideWaitDialog(Player pl) {
        Message msg;
        try {
            msg = new Message(-99);
            msg.writer().writeByte(-1);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void chatPrivate(Player plChat, Player plReceive, String text) {
        if (Functions.isSpam(plChat, text)) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(92);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            if (plChat.getSession().version > 214) {
                msg.writer().writeShort(-1);
            }
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag());
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plChat.sendMessage(msg);
            // Receive
            msg = new Message(92);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            if (plReceive.getSession().version > 214) {
                msg.writer().writeShort(-1);
            }
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag());
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plReceive.sendMessage(msg);
        } catch (IOException e) {

        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void changePassword(Player player, String oldPass, String newPass, String rePass) throws Exception {
        if (player.getSession().pp.equals(oldPass)) {
            if (newPass.length() >= 6) {
                if (newPass.equals(rePass)) {
                    player.getSession().pp = newPass;
                    try {
                        AlyraManager.executeUpdate("update account set password = ? where id = ? and username = ?",
                                rePass, player.getSession().userId, player.getSession().uu);
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thành công!");
                    } catch (IOException ex) {
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thất bại!");
                        Logger
                                .logException(Service.class,
                                        ex);
                    }
                } else {
                    Service.gI().sendThongBao(player, "Mật khẩu nhập lại không đúng!");
                }
            } else {
                Service.gI().sendThongBao(player, "Mật khẩu ít nhất 6 ký tự!");
            }
        } else {
            Service.gI().sendThongBao(player, "Mật khẩu cũ không đúng!");
        }
    }

    public void switchToCreateChar(MySession session) {
        Message msg;
        try {
            msg = new Message(2);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendCaption(MySession session, byte gender) {
        Message msg;
        try {
            msg = new Message(-41);
            List<String> captions = ListCaption(gender);
            msg.writer().writeByte(captions.size());
            for (String caption : captions) {
                msg.writer().writeUTF(caption);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendHavePet(Player player) {
        Message msg;
        try {
            msg = new Message(-107);
            msg.writer().writeByte(player.pet == null ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendWaitToLogin(MySession session, int secondsWait) {
        Message msg;
        try {
            msg = new Message(122);
            msg.writer().writeShort(secondsWait);
            session.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void sendMessage(MySession session, int cmd, String path) {
        Message msg;
        try {
            msg = new Message(cmd);
            msg.writer().write(FileIO.readFile(path));
            session.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendNangDong(Player player) {
        Message msg;
        try {
            msg = new Message(-97);
            msg.writer().writeInt(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void setClientType(MySession session, Message msg) {
        try {
            session.typeClient = (msg.reader().readByte());// client_type
            msg.reader().readByte();// zoom_level requested by client
            session.zoomLevel = 4;// server only serves the x4 asset set
            msg.reader().readBoolean();// is_gprs
            msg.reader().readInt();// width
            msg.reader().readInt();// height
            msg.reader().readBoolean();// is_qwerty
            msg.reader().readBoolean();// is_touch
            String platform = msg.reader().readUTF();
            String[] arrPlatform = platform.split("\\|");
            session.version = Integer.parseInt(arrPlatform[1].replaceAll("\\.", ""));
        } catch (IOException e) {
        } finally {
            msg.cleanup();
        }
        DataGame.sendLinkIP(session);
    }

    public void dropSatellite(Player pl, Item item, Zone map, int x, int y) {
        ItemMap itemMap = new ItemMap(map, item.template, item.quantity, x, y, pl.id);
        itemMap.options = item.itemOptions;
        if (pl.clan != null) {
            itemMap.clanId = pl.clan.id;
        }
        map.addItem(itemMap);
        Message msg = null;
        try {
            msg = new Message(68);
            msg.writer().writeShort(itemMap.itemMapId);
            msg.writer().writeShort(itemMap.itemTemplate.id);
            msg.writer().writeShort(itemMap.x);
            msg.writer().writeShort(itemMap.y);
            msg.writer().writeInt(-2);
            msg.writer().writeShort(200);
            sendMessAllPlayerInMap(map, msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void mabaove(Player player, int mbv) {
        if (Integer.toString(mbv).length() != 6) {
            Service.gI().sendThongBaoOK(player, "Mã bảo vệ phải có độ dài là 6 số.");
        } else if (player.mbv == 0) {
            player.idMark.setMbv(mbv);
            NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1,
                    "Bạn chưa từng kích hoạt chức năng mã bảo vệ để kích hoạt bạn cần có 30K vàng, mật khẩu của bạn là: "
                            + mbv,
                    "Đồng ý", "Từ chối");
        } else if (player.mbv != mbv) {
            Service.gI().sendThongBao(player, "Mật khẩu không đúng. Vui lòng kiểm tra lại");
        } else {
            if (player.baovetaikhoan) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1,
                        "Tài khoản đang được bảo vệ\nBạn có muốn tắt bảo vệ không?", "Đồng ý", "Từ chối");
            } else {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MA_BAO_VE, -1,
                        "Tài khoản không được bảo vệ\nBạn muốn bật chứ năng bảo vệ tài khoản?", "Đồng ý", "Từ chối");
            }
        }
    }

    public void sendEffPlayer(Player pl, Player plReceive, int idEff, int layer, int loop, int loopCount) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopCount);
            msg.writer().writeByte(0);
            msg.writer().flush();
            sendMessAllPlayerInMap(pl.zone, msg);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffAllPlayer(Player pl, int idEff, int layer, int loop, int loopCount) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopCount);
            msg.writer().writeByte(0);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void removeEffAllPlayer(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) pl.id);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeEffPlayer(Player pl, int idEff) {
        Message msg = null;
        try {
            msg = new Message(-128);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(idEff);
            sendMessAllPlayerInMap(pl.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void Send_Body_Mob(Mob mob, int type, int idIcon) {
        Message msg = null;
        try {
            msg = new Message(-112);
            msg.writer().writeByte(type);
            msg.writer().writeByte(mob.id);
            if (type == 1) {
                msg.writer().writeShort(idIcon);
            }
            sendMessAllPlayerInMap(mob.zone, msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendPlayerVS(Player pVS1, Player pVS2, byte type) {
        Message msg = null;
        try {

            if (pVS1 == null) {
                return;
            }

            pVS1.typePk = type;

            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS1.id);
            msg.writer().writeByte(type);

            pVS1.sendMessage(msg);

            if (pVS2 != null && pVS2.isPl()) {
                pVS2.sendMessage(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendPVB(Player pVS1, Player pVS2, byte type) {
        Message msg = null;
        try {
            pVS1.typePk = type;
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS1.id);
            msg.writer().writeByte(type);
            pVS1.sendMessage(msg);
            msg = new Message(-30);
            msg.writer().writeByte((byte) 35);
            msg.writer().writeInt((int) pVS2.id);
            msg.writer().writeByte(type);
            pVS1.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendPVP(Player p1, Player p2) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) p2.id);
            msg.writer().writeByte(3);
            p1.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void exitMap(Player player, long playerExitMapId) {
        Message msg;
        try {
            msg = new Message(-6);
            msg.writer().writeInt((int) playerExitMapId);
            player.sendMessage(msg);
            msg.cleanup();

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        }
    }

    public void SendPowerInfo(Player player) {
        Message msg = null;
        try {
            msg = new Message(-115);
            msg.writer().writeUTF("TL");
            msg.writer().writeShort(player.fightMabu.pointMabu);
            msg.writer().writeShort(player.fightMabu.POINT_MAX);
            msg.writer().writeShort(3);
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void SendPercentPowerInfo(Player player) {
        Message msg = null;
        try {
            msg = new Message(-115);
            msg.writer().writeUTF("%");
            msg.writer().writeShort(player.fightMabu.pointPercent);
            msg.writer().writeShort(player.fightMabu.POINT_MAX * 2);
            msg.writer().writeShort(3);
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void SendMabu(Zone zone, int percent) {
        Message msg = null;
        try {
            msg = new Message(-117);
            msg.writer().writeByte(percent);
            sendMessAllPlayerInMap(zone, msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void callNhanBan(Player player) throws Exception {
        if (player == null || player.zone == null || player.zone.map == null) {
            return;
        }
        if (BossManager.gI().findBossClone(player) != null) {
            sendThongBao(player, "Nhân bản của bạn đang giao chiến, hãy chờ kết quả");
            return;
        }
        if (player.playerSkill == null || player.playerSkill.skills == null) {
            sendThongBao(player, "Không thể tạo nhân bản lúc này");
            return;
        }
        List<Skill> skillList = new ArrayList<>();
        for (byte i = 0; i < player.playerSkill.skills.size(); i++) {
            Skill skill = player.playerSkill.skills.get(i);
            if (skill.point > 0 && skill.template.id != Skill.TU_SAT && skill.template.id != Skill.TROI) {
                skillList.add(skill);
            }
        }
        if (skillList.isEmpty()) {
            sendThongBao(player, "Bạn cần ít nhất 1 kỹ năng tấn công để gọi nhân bản");
            return;
        }
        int[][] skillTemp = new int[skillList.size()][3];
        for (byte i = 0; i < skillList.size(); i++) {
            Skill skill = skillList.get(i);
            if (skill.point > 0 && skill.template.id != Skill.TU_SAT && skill.template.id != Skill.TROI) {
                skillTemp[i][0] = skill.template.id;
                skillTemp[i][1] = skill.point;
                skillTemp[i][2] = skill.coolDown;
            }
        }
            BossData bossData = new BossData(
                player.name,
                player.gender,
                new short[] { player.getHead(), player.getBody(), player.getLeg(), player.getFlagBag(),
                    player.getAura(), player.getEffFront() },
                Math.max(1L, player.nPoint.dame),
                new long[] { Math.max(1L, player.nPoint.hpMax) },
                new int[] { player.zone.map.mapId },
                skillTemp,
                new String[0],
                new String[0],
                new String[0],
                0);
            NhanBan nhanBan = new NhanBan(player, bossData);
            nhanBan.respawn();
            nhanBan.changeStatus(BossStatus.JOIN_MAP);
            EffectSkillService.gI().setPKCommeson(player, 300000);
    }

    public void sendBigBoss(Zone zone, int action, int size, int id, double dame) {
        Message msg = null;
        try {
            msg = new Message(102);
            msg.writer().writeByte(action);
            if (action != 6 && action != 7) {
                msg.writer().writeByte(size);
                msg.writer().writeInt(id);
                msg.writeSmartLong(dame);
            }
            sendMessAllPlayerInMap(zone, msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendBigBoss2(Zone zone, int action, Mob bigboss) {
        Message msg = null;
        try {
            msg = new Message(101);
            msg.writer().writeByte(action);
            msg.writer().writeShort(bigboss.location.x);
            msg.writer().writeShort(bigboss.location.y);
            sendMessAllPlayerInMap(zone, msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendBigBoss2(Player player, int action, Mob bigboss) {
        Message msg = null;
        try {
            msg = new Message(101);
            msg.writer().writeByte(action);
            msg.writer().writeShort(bigboss.location.x);
            msg.writer().writeShort(bigboss.location.y);
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendMabuHold(Player player, int action, short x, short y) {
        if (player == null || player.location == null || player.zone == null) {
            return;
        }
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            if (action == 0) {
                setPos(player, x, y);
            }
            msg = new Message(52);
            msg.writer().writeByte(action); // 0 false, 1 true
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMabuHoldToMe(Player player, Player plReceive, int action, short x, short y) {
        if (player == null || plReceive == null || plReceive.getSession() == null) {
            return;
        }
        Message msg;
        try {
            msg = new Message(52);
            msg.writer().writeByte(action); // 0 false, 1 true
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendEffMabuHoldAllPlayerMapToMe(Player pl) {
        if (pl == null || pl.zone == null) {
            return;
        }
        for (Player plM : pl.zone.getPlayers()) {
            if (plM != null && plM.isPl()) {
                if (plM.maBuHold != null) {
                    sendMabuHoldToMe(plM, pl, 1, (short) plM.maBuHold.x, (short) plM.maBuHold.y);
                }
            }
        }
    }

    public void sendEffMabuEat(Player player, Player plTarget) {
        if (player == null || plTarget == null || player.zone == null) {
            return;
        }
        Message msg;
        try {
            msg = new Message(52);
            msg.writer().writeByte(2);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeInt((int) plTarget.id);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMabuEat(Player player, Player plTarget) {
        if (player == null || plTarget == null || player.zone == null) {
            return;
        }
        if (plTarget.isPl() && plTarget.maBuHold == null) {
            MaBuHold mabuHold = player.zone.getMaBuHold();
            if (mabuHold != null) {
                new Thread(() -> {
                    int zoneId = player.zone.zoneId;
                    player.zone.setMaBuHold(mabuHold.slot, zoneId, plTarget);
                    sendEffMabuEat(player, plTarget);
                    Functions.sleep(3000);
                    if (player.zone == null || player.zone.map == null || player.zone.map.mapId != 127) {
                        return;
                    }
                    map.Map map128 = MapService.gI().getMapById(128);
                    if (map128 == null || map128.zones == null || zoneId < 0 || zoneId >= map128.zones.size()) {
                        return;
                    }
                    Zone zone = map128.zones.get(zoneId);
                    if (zone == null) {
                        return;
                    }
                    ChangeMapService.gI().changeMap(plTarget, zone, -1, 336);
                    Functions.sleep(500);
                    plTarget.isMabuHold = false;
                    if (plTarget.effectSkill != null && !plTarget.effectSkill.isShielding) {
                        EffectSkillService.gI().setMabuHold(plTarget, mabuHold);
                        Functions.sleep(1500);
                        if (plTarget.fusion != null && plTarget.pet != null
                                && plTarget.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                            plTarget.pet.unFusion();
                        }
                    }
                }).start();
            }
        }
    }

    public void sendMabuAttackSkill(Player player) {
        if (player == null || player.location == null || player.zone == null || player.nPoint == null) {
            return;
        }
        Message msg;
        try {
            int skillId[] = { 0, 1, 3 };
            int skill = skillId[Util.nextInt(3)];
            if (Util.isTrue(1, 10)) {
                skill = 2;
            }
            msg = new Message(51);
            msg.writer().writeInt((int) player.id); // charid
            msg.writer().writeShort(skill); // skill id 0 1 2 3
            msg.writer().writeShort(player.location.x); // x
            msg.writer().writeShort(player.location.y); // y
            List<Player> notBosses = player.zone.getNotBosses();
            if (notBosses == null) {
                msg.cleanup();
                return;
            }
            msg.writer().writeByte(notBosses.size()); // số player
            for (Player plM : notBosses) {
                if (plM != null && plM.nPoint != null) {
                    msg.writer().writeInt((int) plM.id);
                    double damage = plM.injured(player, player.nPoint.dame + plM.nPoint.hp / 10, true, false);
                    msg.writeSmartLong(Util.maxIntValue(damage));
                }
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    // ========================READ OPT========================
    public Message messageReadOpt(byte command) throws IOException {
        Message ms = new Message(24);
        ms.writer().writeByte(command);
        return ms;
    }

    public void sendMessageServer(String data) {
        Message msg;
        try {
            msg = messageReadOpt((byte) 4);
            msg.writer().writeUTF(data);
            sendMessAllPlayer(msg);
        } catch (IOException e) {
        }
    }

    public void sendHideNpc(Player player, int npcId, boolean isHide) {
        Message msg;
        try {
            msg = new Message(-73);
            msg.writer().writeByte(npcId);
            msg.writer().writeByte(isHide ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendTopRank(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-119);
            msg.writer().writeInt(pl.superRank.rank);
            pl.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void moveFast(Player pl, int x, int y) {
        Message msg;
        try {
            msg = new Message(58);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeInt((int) pl.id);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void dropAndPickItemDNC(Player pl, int itemId) {
        ItemMap item = new ItemMap(pl.zone, itemId, 1, pl.location.x, pl.location.y, pl.id);
        item.options.add(new ItemOption(71 - (itemId - 220), 0));
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void dropAndPickItem(Player pl, int itemId, int quantity) {
        ItemMap item = new ItemMap(pl.zone, itemId, quantity, pl.location.x, pl.location.y, pl.id);
        Service.gI().dropItemMap(pl.zone, item);
        pl.zone.pickItem(pl, item.itemMapId);
    }

    public void sendTypePK(Player player, Player boss) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) boss.id);
            msg.writer().writeByte(3);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void playerInfoUpdate(Player pl, Player plR, String plName, int plHead, int plBody, int plLeg) {
        if (pl == null) {
            return;
        }
        if (pl.location == null) {
            Logger.error("[Service] location là null cho player: " + pl.name + " trong playerInfoUpdate");
            return;
        }
        Message msg = null;
        try {
            msg = messageSubCommand((byte) 7);
            msg.writer().writeInt((int) pl.id);
            if (pl.clan != null) {
                msg.writer().writeInt(pl.clan.id);
            } else if (pl.isCopy) {
                msg.writer().writeInt(-2);
            } else {
                msg.writer().writeInt(-1);
            }
            msg.writer().writeByte(Service.gI().getCurrLevel(pl));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(plHead);
            msg.writer().writeUTF(plName);
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hp));
            msg.writeSmartLong(Util.maxIntValue(pl.nPoint.hpMax));
            msg.writer().writeShort(plBody);
            msg.writer().writeShort(plLeg);
            int flagbag = pl.getFlagBag();
            if (pl.isPl() && plR != null && plR.getSession() != null && plR.getSession().version >= 228) {
                switch (flagbag) {
                    case 83 ->
                        flagbag = 205;
                }
            }
            msg.writer().writeByte(flagbag); // bag
            msg.writer().writeByte(-1);
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(0);
            msg.writer().writeShort(pl.getAura()); // idauraeff
            msg.writer().writeByte(pl.getEffFront()); // seteff
            msg.writer().writeShort(pl.getHat()); // id hat
            plR.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }

    }

    public void sendLoginFail(MySession session, boolean isLoggingIn) throws Exception {
        Message msg;
        try {
            msg = new Message(-102);
            msg.writer().writeByte(isLoggingIn ? 1 : 0);
            session.doSendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void stealMoney(Player pl, int stealMoney) {
        Message msg;
        try {
            msg = new Message(95);
            msg.writer().writeInt(stealMoney);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {

        }
    }

    public void LogicEffect(Player pl, int id, int layer, int loop, int loopcount, int stand, int times) {
        try {
            Message msg = new Message(-128);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(id);
            msg.writer().writeByte(layer);
            msg.writer().writeByte(loop);
            msg.writer().writeShort(loopcount);
            msg.writer().writeByte(stand);

            sendMessAllPlayerInMap(pl.zone, msg);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> removeEffectChar(pl, id), times, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
        }
    }

    private void removeEffectChar(Player pl, int id) {
        try {
            Message msg = new Message(-128);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeShort(id);

            sendMessAllPlayerInMap(pl.zone, msg);
        } catch (IOException e) {
        }
    }

    public void sendServerMessage(Player player, String text) {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendDialogMessage(Player pl, String text) {
        Message msg = null;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendDialogMessage(MySession session, String text) {
        Message msg = null;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            session.sendMessage(msg);

        } catch (IOException e) {
            Logger.logException(Service.class,
                    e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendRequestUpdate(Player p) {
        Message msg;
        try {
            msg = new Message(-126);
            p.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void showYourNumber(Player player, String Number, String result, String finish, int type) {
        Message msg = null;
        try {
            msg = new Message(-126);
            msg.writer().writeByte(type);
            if (type == 0) {
                msg.writer().writeUTF(Number);
            } else if (type == 1) {
                msg.writer().writeByte(type);
                msg.writer().writeUTF(result);
                msg.writer().writeUTF(finish);
            }
            player.sendMessage(msg);
        } catch (IOException e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendAura(Player pl) {
        try {
            Message message = new Message(127);
            message.writer().writeByte(4);
            message.writer().writeInt((int) pl.id);
            message.writer().writeShort(pl.getAura());
            message.writer().writeByte(-1);
            message.writer().flush();
            Service.gI().sendMessAllPlayerInMap(pl.zone, message);
            message.cleanup();
        } catch (IOException ex) {
        }
    }

    public void sendCaptcha0(Player pl, String keyCaptcha, byte[] image) {
        Message msg = null;
        try {
            msg = new Message(-85);
            msg.writer().writeByte(0);
            msg.writer().writeShort(image.length);
            msg.writer().write(image);
            msg.writer().writeUTF(keyCaptcha);
            pl.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendCaptcha1(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-85);
            msg.writer().writeByte(1);
            pl.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void removeNpc(Player player, npc.Npc npc) {
        if (player == null || player.session == null || npc == null) {
            return;
        }
        try {
            Message msg = new Message(-73);
            msg.writer().writeByte(0);
            msg.writer().writeShort(npc.tempId);
            player.sendMessage(msg);
            msg.cleanup();

            utils.Logger.logln("[Service] Gửi gói removeNpc(-73) tới " + player.name + " - tempId: " + npc.tempId);
        } catch (IOException e) {
            utils.Logger.error("[Service] Lỗi khi gửi removeNpc: " + e.getMessage());
        }
    }

    public void sendCaptcha2(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-85);
            msg.writer().writeByte(2);
            pl.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void switchToRegisterScr(ISession session) {
        try {
            Message message;
            try {
                message = new Message(42);
                message.writer().writeByte(0);
                session.sendMessage(message);
                message.cleanup();
            } catch (IOException e) {
            }
        } catch (Exception e) {
        }
    }

    public synchronized void regisAccount(MySession session, Message _msg) {
        try {
            if (session == null || _msg == null) {
                return;
            }

            if (session.player != null || session.userId > 0) {
                sendThongBaoOK(session, "Vui lòng đăng xuất trước khi tạo tài khoản mới.");
                return;
            }

            String registerIp = session.getIP();
            long now = System.currentTimeMillis();
            Long lastRegister = LAST_REGISTER_BY_IP.put(registerIp, now);
            if (LAST_REGISTER_BY_IP.size() > 10_000) {
                LAST_REGISTER_BY_IP.entrySet().removeIf(entry -> now - entry.getValue() > 60 * 60_000L);
            }
            if (lastRegister != null && now - lastRegister < REGISTER_COOLDOWN_MS) {
                sendThongBaoOK(session, "Bạn thao tác quá nhanh, vui lòng thử lại sau.");
                return;
            }

            _msg.readUTF(); // f1
            _msg.readUTF(); // f2
            _msg.readUTF(); // f3
            _msg.readUTF(); // f4
            _msg.readUTF(); // f5

            String user = Util.sanitizeInput(_msg.readUTF()).trim();
            String pass = Util.sanitizeInput(_msg.readUTF()).trim();

            if (user.isEmpty()) {
                sendThongBaoOK(session, "Vui lòng nhập tài khoản");
                return;
            }

            if (pass.isEmpty()) {
                sendThongBaoOK(session, "Vui lòng nhập mật khẩu");
                return;
            }

            if (user.length() < 5 || user.length() > 20) {
                sendThongBaoOK(session, "Tài khoản phải có độ dài 5 đến 20 ký tự");
                return;
            }

            if (pass.length() < 3 || pass.length() > 18) {
                sendThongBaoOK(session, "Mật khẩu phải có độ dài 3-18 ký tự");
                return;
            }

            if (!user.matches("^[a-zA-Z0-9]+$")) {
                sendThongBaoOK(session, "Tài khoản chỉ được chứa chữ và số");
                return;
            }

            if (!pass.matches("^[a-zA-Z0-9]+$")) {
                sendThongBaoOK(session, "Mật khẩu chỉ được chứa chữ và số");
                return;
            }

            AlyraResultSet rs = AlyraManager.executeQuery(
                    "SELECT 1 FROM account WHERE username = ?", user);

            if (rs.next()) {
                sendThongBaoOK(session, "Tài khoản đã tồn tại");
                rs.dispose();
                return;
            }
            rs.dispose();

            AlyraResultSet weekly = AlyraManager.executeQuery(
                    "SELECT COUNT(*) AS count FROM account WHERE ip_address = ? "
                            + "AND username NOT LIKE 'trial_%' AND create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)",
                    registerIp);
            int weeklyCount = weekly.first() ? weekly.getInt("count") : 0;
            weekly.dispose();
            if (weeklyCount >= 7) {
                Logger.warning("IP " + registerIp + " reached weekly account registration limit\n");
                sendThongBaoOK(session, "Bạn đã đạt giới hạn tạo tài khoản. Vui lòng thử lại sau.");
                return;
            }

            int inserted = AlyraManager.executeUpdate(
                    "INSERT INTO account(username,password,create_time,update_time,ban,is_admin,active,activated,ip_address) "
                            + "VALUES(?,?,NOW(),NOW(),0,0,1,1,?)",
                    user, pass, registerIp);
            if (inserted > 0) {
                Message result = new Message(42);
                result.writer().writeByte(1);
                session.sendMessage(result);
                result.cleanup();
                sendThongBaoOK(session, "Tạo tài khoản thành công. Hãy đăng nhập để tiếp tục.");
            } else {
                sendThongBaoOK(session, "Không thể tạo tài khoản lúc này. Vui lòng thử lại sau.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendThongBaoOK(session, "Đã xảy ra lỗi bất ngờ, vui lòng thử lại sau!");
        }
    }

}
