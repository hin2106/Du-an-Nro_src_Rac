package event.halloween;

import event.Event;
import java.util.ArrayList;
import java.util.List;
import map.ItemMap;
import mob.Mob;
import player.Player;
import managers.boss.BossManager;
import managers.boss.HalloweenEventManager;
import shop.TabShopSanta;

import consts.ConstMap;
import services.map.MapService;

public class Halloween extends Event {

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_HALLOWEEN;
    }

    @Override
    public void init() {
        if (server.Manager.EVENT_SEVER != eventId()) {
            cleanup();
            return;
        }
        initNpc();
        spawnBosses();
    }

    @Override
    public void initNpc() {
        try {
            // new event.halloween.Npc_ThoChiChi().initNpc();
        } catch (Exception ignored) {
        }
    }

    private void spawnBosses() {
        if (server.Manager.EVENT_SEVER != eventId()) {
            return;
        }
        if (HalloweenEventManager.gI().getBossCount() == 0) {
            int count = 10;
            for (int i = 0; i < count; i++) {
                BossManager.gI().createBoss(boss.BossID.BIMA);
                BossManager.gI().createBoss(boss.BossID.MATROI);
                BossManager.gI().createBoss(boss.BossID.DOI);
            }
        }
    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(Player p, Mob m, List<ItemMap> list, int x, int yEnd) {
        HalloweenDrops.apply(p, m, list, x, yEnd);
    }

    @Override
    public boolean useItem(Player p, item.Item item) {
        return false;
    }

    @Override
    public List<TabShopSanta.SantaItemConfig> getSantaShopItems() {
        List<TabShopSanta.SantaItemConfig> items = new ArrayList<>();
        // Item event Halloween - thêm/sửa/xóa item tại đây
        // Ví dụ: items.add(new TabShopSanta.SantaItemConfig(itemId, (byte) typeSell, cost, new int[][]{{optionId, param}}, isNew));
        return items;
    }

    @Override
    public void cleanup() {
        int[] HallowenbosssIds = {boss.BossID.BIMA, boss.BossID.MATROI, boss.BossID.DOI};
        for (int id : HallowenbosssIds) {
            managers.boss.BossManager.gI().removeAllByBossId(id);
        }
        try {
            int[] maps = {ConstMap.LANG_ARU, ConstMap.LANG_MORI, ConstMap.LANG_KAKAROT, ConstMap.DAO_KAME};
            for (int mapId : maps) {
                map.Map m = MapService.gI().getMapById(mapId);
                if (m != null) {
                    m.removeNpcByTempId(80);
                    m.removeNpcByTempId(113);
                }
            }
        } catch (Exception ignored) {
        }
    }

}
