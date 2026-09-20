package services.tournament;

import managers.tournament.DeathOrAliveArenaManager;
import map.Zone;
import player.Player;
import network.Message;
import consts.ConstNpc;
import java.io.IOException;
import map.Map;
import npc.Npc;
import services.map.NpcManager;
import services.player.InventoryService;
import services.map.MapService;
import services.Service;
import services.map.ChangeMapService;
import tournament.DeathOrAliveArena;

public class DeathOrAliveArenaService {

    private static DeathOrAliveArenaService i;

    public static DeathOrAliveArenaService gI() {
        if (i == null) {
            i = new DeathOrAliveArenaService();
        }
        return i;
    }

    public void startChallenge(Player player) {
        Zone zone = getMapChallenge(112);
        if (zone != null) {
            if (player.inventory.gem >= player.gemVoDaiSinhTu) {
                player.inventory.gem -= player.gemVoDaiSinhTu;
                InventoryService.gI().sendItemBags(player);
                player.gemVoDaiSinhTu += 10;
                player.lastTimePKVoDaiSinhTu = System.currentTimeMillis();
            } else {
                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc !");
                return;
            }
            if (!zone.equals(player.zone)) {
                ChangeMapService.gI().changeMap(player, zone, player.location.x, 408);
            }
            Npc baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
            DeathOrAliveArena vdst = new DeathOrAliveArena();
            vdst.setPlayer(player);
            vdst.setNpc(baHatMit);
            vdst.setRound(0);
            vdst.toTheNextRound();
            vdst.setZone(zone);
            vdst.setTimeTotal(0);
            DeathOrAliveArenaManager.gI().add(vdst);
            baHatMit.npcChat(player, "Số thứ tự của ngươi là 1 chuẩn bị thi đấu nhé.");
            Service.gI().releaseCooldownSkill(player);
            player.isPKDHVT = true;
            player.lastTimePKDHVT23 = System.currentTimeMillis();
            vdst.endChallenge = false;
        } else {
        }
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

    public Zone getMapChallenge(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        Zone zone = null;
        try {
            if (map != null) {
                int zoneId = 0;
                while (zoneId < map.zones.size()) {
                    Zone zonez = map.zones.get(zoneId);
                    if (DeathOrAliveArenaManager.gI().getVDST(zonez) == null) {
                        zone = zonez;
                        break;
                    }
                    zoneId++;
                }
            }
        } catch (Exception e) {
        }
        return zone;
    }
}
