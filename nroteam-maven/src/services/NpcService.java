package services;

import consts.ConstNpc;
import npc.Npc;
import npc.NpcFactory;
import player.Player;
import server.Manager;
import network.Message;
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
        createMenu(player, indexMenu, ConstNpc.RONG_THIENG, -1, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String... menuSelect) {
        createMenu(player, indexMenu, ConstNpc.CON_MEO, avatar, npcSay, menuSelect);
    }

    public void createMenuConMeo(Player player, int indexMenu, int avatar, String npcSay, String[] menuSelect, Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        createMenuConMeo(player, indexMenu, avatar, npcSay, menuSelect);
    }

    private void createMenu(Player player, int indexMenu, byte npcTempId, int avatar, String npcSay, String... menuSelect) {
        if (player == null || !player.isPl() || player.idMark == null) return;
        Message msg = null;
        try {
            player.idMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(npcTempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            if (avatar != -1) msg.writer().writeShort(avatar);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(NpcService.class, e);
        } finally {
            if (msg != null) msg.cleanup();
        }
    }

    public void createTutorial(Player player, int avatar, String npcSay) {
        createTutorial(player, ConstNpc.CON_MEO, avatar, npcSay);
    }

    public void createTutorial(Player player, int tempId, int avatar, String npcSay) {
        Message msg = null;
        try {
            msg = new Message(38);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            if (avatar != -1) msg.writer().writeShort(avatar);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(NpcService.class, e);
        } finally {
            if (msg != null) msg.cleanup();
        }
    }

    public int getAvatar(int npcId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == npcId) return npc.avartar;
        }
        return 1139;
    }

    public void createBigMessage(Player player, int avatar, String npcSay, byte type, String select, String confirm) {
        Message msg = null;
        try {
            msg = new Message(-70);
            msg.writer().writeShort(avatar);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(type);
            if (type == 1) {
                msg.writer().writeUTF(confirm);
                msg.writer().writeUTF(select);
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(NpcService.class, e);
        } finally {
            if (msg != null) msg.cleanup();
        }
    }
}
