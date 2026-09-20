package event.noel;

import boss.BossID;
import event.Event;
import java.util.ArrayList;
import java.util.List;
import managers.boss.BossManager;
import managers.boss.ChristmasEventManager;
import map.ItemMap;
import mob.Mob;
import server.Manager;
import shop.TabShopSanta;
import utils.Util;

public class Noel extends Event {

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_NOEL;
    }

    @Override
    public void init() {
        initNpc();
        spawnBosses();
    }

    private void spawnBosses() {
        if (Manager.EVENT_SEVER != eventId()) {
            return;
        }
        if (ChristmasEventManager.gI().getBossCount() == 0) {
            int count = 10; 
            for (int i = 0; i < count; i++) {
                BossManager.gI().createBoss(BossID.ONG_GIA_NOEL);
                BossManager.gI().createBoss(BossID.TUAN_LOC);
            }
        }
    }

    @Override
    public void initNpc() {
        try {
            // new event.noel.Npc_ThoChiChi().initNpc();
            new event.noel.Npc_CayThong().initNpc();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(player.Player p, Mob m, List<ItemMap> list, int x, int yEnd) {
    }

    @Override
    public boolean useItem(player.Player p, item.Item item) {
        return false;
    }

    @Override
    public List<TabShopSanta.SantaItemConfig> getSantaShopItems() {
        List<TabShopSanta.SantaItemConfig> items = new ArrayList<>();
        // Item event Noel - thêm/sửa/xóa item tại đây
        items.add(new TabShopSanta.SantaItemConfig(533, (byte) 1, 50, new int[][]{{30, 0}}, true));
        items.add(new TabShopSanta.SantaItemConfig(649, (byte) 1, 50, new int[][]{{30, 0}}, true));
        items.add(new TabShopSanta.SantaItemConfig(386, (byte) 1, 50, new int[][]{{50, Util.nextInt(5, 10)}, {106, 0}, {93, 5}}, true));
        items.add(new TabShopSanta.SantaItemConfig(389, (byte) 1, 50, new int[][]{{50, Util.nextInt(5, 10)}, {106, 0}, {93, 5}}, true));
        items.add(new TabShopSanta.SantaItemConfig(392, (byte) 1, 50, new int[][]{{50, Util.nextInt(5, 10)}, {106, 0}, {93, 5}}, true));
        return items;
    }

    
}
