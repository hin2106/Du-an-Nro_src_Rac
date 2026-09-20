package services.func;

import consts.Cmd_message;
import map.Zone;
import network.Message;
import player.Player;
import server.Manager;
import services.Service;
import services.map.EffectEventManager;
import services.map.EffectEventTemplate;

public class EffectMapService {

    private static EffectMapService i;

    private EffectMapService() {

    }

    public static EffectMapService gI() {
        if (i == null) {
            i = new EffectMapService();
        }
        return i;
    }

    public void sendEffEvent(Player pl) {
       if(pl.zone==null) {
       return;}
        int plmapid = pl.zone.map.mapId;
        for (EffectEventTemplate i : EffectEventManager.gI().getList()) {
            if (Manager.EVENT_SEVER == i.getEventId()) {
                if (plmapid == i.getMapId()) {
                    EffectMapService.gI().sendEffectMapToPlayer(pl, i.getEffId(),
                            i.getLayer(), i.getLoop(), i.getX(), i.getY(), i.getDelay());
                }
            }
        }
    }

    public void sendEffectMapToPlayer(Player player, int id, int layer, int loop, int x, int y, int delay) {
        Message msg;
        try {
            msg = new Message(113);
            msg.writer().writeByte(loop);
            msg.writer().writeByte(layer);
            msg.writer().writeShort(id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeShort(delay);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendCharEffect(Player player, byte type, short id, byte layer, byte loop, short delay, boolean isStand) {
        Message msg;
        try {
            msg = new Message(Cmd_message.CHAR_EFFECT);
            msg.writer().writeByte(type);
            msg.writer().writeInt((int) player.id);
            if (type == 0) {
                msg.writer().writeShort(id);
                msg.writer().writeByte(layer);
                msg.writer().writeByte(loop);
                msg.writer().writeShort(delay);
                msg.writer().writeBoolean(isStand);
            } else if (type == 1) {
                msg.writer().writeShort(id);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void sendEffectMapToAllInMap(Zone zone, int id, int layer, int loop, int x, int y, int delay) {
        Message msg;
        try {
            msg = new Message(113);
            msg.writer().writeByte(loop);
            msg.writer().writeByte(layer);
            msg.writer().writeShort(id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeShort(delay);
            Service.gI().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

}
