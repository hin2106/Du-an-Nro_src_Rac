package player;

import consts.ConstTask;
import item.Item;
import map.ItemMap;
import map.Zone;
import services.Service;
import services.TaskService;
import services.map.ItemMapService;
import services.player.InventoryService;
import utils.Util;

public class DropItem {

    private Player player;

    public DropItem(Player player) {
        this.player = player;
    }

    public void update() {
        Zone zone = player.zone;
        if (player.isPl() && zone != null) {
            if (zone.map.mapId == 52
                    && TaskService.gI().getIdTask(player) == ConstTask.TASK_199_6
                    && InventoryService.gI().getCountEmptyBag(player) > 0
                    && !InventoryService.gI().isExistItemBag(player, 726)
                    && !ItemMapService.gI().findItemMapByPlayer(player, 726)) {
                int x = Util.nextInt(100, zone.map.mapWidth - 100);
                int y = zone.map.yPhysicInTop(x, 100);
                ItemMap it = new ItemMap(zone, 726, 1, x, y, player.id);
                it.options.add(new Item.ItemOption(30, 0));
                it.options.add(new Item.ItemOption(93, 1));
                Service.gI().dropItemMapForMe(player, it);
            }
            if ((zone.map.mapId == 42 || zone.map.mapId == 43 || zone.map.mapId == 44)
                    && TaskService.gI().getIdTask(player) == ConstTask.TASK_3_1
                    && InventoryService.gI().getCountEmptyBag(player) > 0
                    && !InventoryService.gI().isExistItemBag(player, 78)
                    && !ItemMapService.gI().findItemMapByPlayer(player, 78)) {
                int x = 70;
                int y = (zone.map.mapId == 43) ? 264 : 288; // Map 43 has different y position
                ItemMap it = new ItemMap(zone, 78, 1, x, y, player.id);
                Service.gI().dropItemMapForMe(player, it);
            }
        }
    }

    public void dispose() {
        player = null;
    }
}
