package services.map;
import consts.ConstNpc;
import npc.Npc;
import npc.NpcFactory;
import player.Player;
import server.Manager;
import network.Message;
import services.Service;
import utils.Logger;

public class NpcService {

    private static NpcService i;

    public static NpcService gI() {
        if (i == null) {
            i = new NpcService();
        }
        return i;
    }

    public void createMenuRongThieng(Player player, int indexMenu, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.RONG_THIENG, 0, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.CON_MEO, avatar, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String[] menuSelect,
            Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        createMenuConMeo(player, indexMenu, avatar, npcSay, menuSelect);
    }

    private void createMenu(Player player, int indexMenu, byte npcTempId, int avatar, String npcSay,
            String... menuSelect) {
        if (player == null || !player.isPl()) {
            return;
        }
        Message msg;
        try {
            if (player.idMark != null) {
                player.idMark.setIndexMenu(indexMenu);
            }
            msg = new Message(32);
            msg.writer().writeShort(npcTempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                if (menu == null) {
                    menu = "";
                }
                msg.writer().writeUTF(menu);
            }
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(NpcService.class, e);
        }
    }

    public boolean SummonDragonWhis_1_1(Player playerSummonShenron, int select) {
        switch (select) {
            case 0 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 1 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 2 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 3 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 4 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
        }
        return true;
    }

    public boolean SummonDragonWhis_1_2(Player playerSummonShenron, int select) {
        switch (select) {
            case 0 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 1 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 2 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
        }
        return true;
    }

    public boolean SummonDragonWhis_2_1(Player playerSummonShenron, int select) {
        switch (select) {
            case 0 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 1 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 2 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
        }
        return true;
    }

    public boolean SummonDragonWhis_3_1(Player playerSummonShenron, int select) {
        switch (select) {
            case 0 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 1 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 2 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
        }
        return true;
    }

    public boolean SummonDragonBlack_1(Player playerSummonShenron, int select) {
        switch (select) {
            case 0 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 1 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 2 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 3 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 4 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
        }
        return true;
    }

    public boolean SummonDragonICE_1(Player playerSummonShenron, int select) {
        switch (select) {
            case 0 -> {
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            }
            case 1 -> {
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            }
            case 2 ->
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            case 3 -> {
                Service.gI().sendThongBao(playerSummonShenron, "chua lam:v luoi qua huhu");
            }
        }
        return true;
    }

    public void createTutorial(Player player, int avatar, String npcSay) {
        Message msg;
        try {
            msg = new Message(38);
            msg.writer().writeShort(ConstNpc.CON_MEO);
            msg.writer().writeUTF(npcSay);
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createTutorial(Player player, int tempId, int avatar, String npcSay) {
        Message msg;
        try {
            msg = new Message(38);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            if (avatar != -1) {
                msg.writer().writeShort(avatar);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public int getAvatar(int npcId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == npcId) {
                return npc.avartar;
            }
        }
        if (Manager.NPC_TEMPLATES != null) {
            for (system.Template.NpcTemplate template : Manager.NPC_TEMPLATES) {
                if (template != null && template.id == npcId) {
                    return template.avatar;
                }
            }
        }
        return 1139;
    }

    public void createNpcAppear(Npc npc, map.Zone zone) {
        if (npc == null || zone == null || zone.map == null) {
            return;
        }
        try {
            network.Message msg = new network.Message(-30);
            msg.writer().writeByte(0); // type = 0 => add NPC
            msg.writer().writeShort(npc.tempId);
            msg.writer().writeShort(npc.cx);
            msg.writer().writeShort(npc.cy);
            msg.writer().writeByte(npc.status);
            msg.writer().writeShort(npc.avartar);
            services.Service.gI().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.logException(NpcService.class, e);
        }
    }

    public void removeNpc(Npc npc, map.Zone zone) {
        if (npc == null || zone == null || zone.map == null) {
            return;
        }
        try {
            network.Message msg = new network.Message(-30);
            msg.writer().writeByte(1);
            msg.writer().writeShort(npc.tempId);
            services.Service.gI().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            utils.Logger.logException(NpcService.class, e);
        }
    }
}
