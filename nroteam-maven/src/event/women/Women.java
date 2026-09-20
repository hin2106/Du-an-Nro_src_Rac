package event.women;

import consts.ConstEvent;
import event.Event;
import java.util.ArrayList;
import java.util.List;
import map.ItemMap;
import mob.Mob;
import player.Player;
import server.Manager;
import shop.TabShopSanta;

public class Women extends Event {

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_20_10;
    }

    @Override
    public void init() {
        if (Manager.EVENT_SEVER != ConstEvent.SU_KIEN_20_10) {
            return;
        }
        initNpc();
    }

    @Override
    public void initNpc() {
        if (Manager.EVENT_SEVER != ConstEvent.SU_KIEN_20_10) {
            return;
        }
        // new Npc_ThoChiChi().initNpc();
    }

    @Override
    public void cleanup() {
        try {
            int[] maps = {consts.ConstMap.DAO_KAME};
            int[] npcTempIds = {80};
            for (int mapId : maps) {
                map.Map m = services.map.MapService.gI().getMapById(mapId);
                if (m != null) {
                    for (int tempId : npcTempIds) {
                        m.removeNpcByTempId(tempId);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(Player p, Mob m, List<ItemMap> list, int x, int yEnd) {
    }

    @Override
    public boolean useItem(Player p, item.Item item) {
        return false;
    }

    @Override
    public List<TabShopSanta.SantaItemConfig> getSantaShopItems() {
        List<TabShopSanta.SantaItemConfig> items = new ArrayList<>();
        // Item event 20/10 - thêm/sửa/xóa item tại đây
        // Ví dụ: items.add(new TabShopSanta.SantaItemConfig(itemId, (byte) typeSell, cost, new int[][]{{optionId, param}}, isNew));
        return items;
    }
}
