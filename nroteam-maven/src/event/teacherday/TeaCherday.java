package event.teacherday;

import event.Event;
import item.Item;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import consts.ConstMap;
import map.ItemMap;
import mob.Mob;
import services.ItemService;
import services.map.MapService;
import shop.TabShopSanta;
import utils.Util;

public class TeaCherday extends Event {
    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_20_11;
    }

    private static int dropCount = 0;
    private static int lastDay = 0;

    @Override
    public void init() {
        if (server.Manager.EVENT_SEVER != eventId()) {
            cleanup();
            return;
        }
        initNpc();
    }

    @Override
    public void initNpc() {
        // new Npc_ThoChiChi().initNpc();
        new BongHong().initNpc();
    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(player.Player pl, Mob m, List<ItemMap> list, int x, int yEnd) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (today != lastDay) {
            dropCount = 0;
            lastDay = today;
        }
        if (dropCount < 1000) {
            int mapId = pl.zone.map.mapId;
            if (mapId == 6 || mapId == 10 || mapId == 68 || mapId == 69 || mapId == 70) {
                int rate = Util.nextInt(1, 100);
                if (rate <= 5) {
                    Item item = ItemService.gI().createNewItem((short) 1364);
                    ItemMap itemMap = new ItemMap(pl.zone, item.template.id, 1, x, pl.location.y, pl.id);
                    list.add(itemMap);
                    dropCount++;
                }
            }
        }
    }

    @Override
    public void cleanup() {
        try {
            int[] maps = { ConstMap.RUNG_BAMBOO, ConstMap.RUNG_DUONG_XI, ConstMap.NAM_KAME, ConstMap.DAO_BULONG,
                    ConstMap.DAO_KAME };
            for (int mapId : maps) {
                map.Map m = MapService.gI().getMapById(mapId);
                if (m != null) {
                    m.removeNpcByTempId(80);
                    m.removeNpcByTempId(81);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean useItem(player.Player p, item.Item item) {
        return false;
    }

    @Override
    public List<TabShopSanta.SantaItemConfig> getSantaShopItems() {
        List<TabShopSanta.SantaItemConfig> items = new ArrayList<>();
        // Item event 20/11 - thêm/sửa/xóa item tại đây
        // Ví dụ: items.add(new TabShopSanta.SantaItemConfig(itemId, (byte) typeSell, cost, new int[][]{{optionId, param}}, isNew));
        return items;
    }
}
