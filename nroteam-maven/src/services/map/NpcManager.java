package services.map;

import consts.ConstNpc;
import consts.ConstTask;
import npc.Npc;
import player.Player;
import server.Manager;
import services.TaskService;
import java.util.ArrayList;
import java.util.List;
import utils.Util;

public class NpcManager {

    public static Npc getByIdAndMap(int id, int mapId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == id && npc.mapId == mapId) {
                return npc;
            }
        }
        return null;
    }

    public static Npc getNpc(byte tempId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == tempId) {
                return npc;
            }
        }
        return null;
    }

    public static List<Npc> getNpcsByMapPlayer(Player player) {
        List<Npc> list = new ArrayList<>();
        if (player.zone != null) {
            for (Npc npc : player.zone.map.npcs) {
                if (npc.tempId == ConstNpc.QUA_TRUNG && player.mabuEgg == null
                        && player.zone.map.mapId == (21 + player.gender)) {
                    continue;
                } else if (npc.tempId == ConstNpc.CALICK) {
                    if (TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
                        continue;
                    }
                    if (player.zone.map.mapId != 102) {
                        if (Util.isTrue(30, 100)) {
                            int newX = Util.nextInt(20, player.zone.map.mapWidth - 20);
                            int newY = player.zone.map.yPhysicInTop(newX, 0);
                            npc.cx = newX;
                            npc.cy = newY;
                        }
                    }
                } else if (npc.tempId == ConstNpc.DR_MYUU && player.zone.map.mapId != 166) {
                    if (Util.isTrue(30, 100)) {
                        int newX = Util.nextInt(20, player.zone.map.mapWidth - 20);
                        int newY = player.zone.map.yPhysicInTop(newX, 0);
                        npc.cx = newX;
                        npc.cy = newY;
                    }
                } else if (npc.tempId == ConstNpc.QUOC_VUONG && player.nPoint.power < 17000000000L) {
                    continue;
                }

                list.add(npc);
            }
        }
        return list;
    }

}
