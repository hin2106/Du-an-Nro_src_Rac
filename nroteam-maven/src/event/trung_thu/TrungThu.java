package event.trung_thu;

import event.Event;
import java.util.ArrayList;
import java.util.List;
import managers.boss.BossManager;
import managers.boss.TrungThuEventManager;
import map.ItemMap;
import mob.Mob;
import shop.TabShopSanta;

public class TrungThu extends Event {

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_TRUNG_THU;
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
        if (server.Manager.EVENT_SEVER != eventId()) {
            return;
        }
        new Npc_ThoDaiCa().initNpc();
        new Npc_TrungThu().initNpc();
        new ThapSangLongDen().initNpc();
        new NoiBanh().initNpc();
        // new Npc_ThoChiChi().initNpc();
    }

    private void spawnBosses() {
        if (server.Manager.EVENT_SEVER != eventId()) {
            return;
        }
        if (TrungThuEventManager.gI().getBossCount() == 0) {
            int count = 10;
            for (int i = 0; i < count; i++) {
                BossManager.gI().createBoss(boss.BossID.GOGETA);
                BossManager.gI().createBoss(boss.BossID.THO_DAI_CA);
                BossManager.gI().createBoss(boss.BossID.OMEGA);
            }
        }
    }

    @Override
    public void cleanup() {
        int[] trungThuBossIds = {boss.BossID.GOGETA, boss.BossID.THO_DAI_CA, boss.BossID.OMEGA};
        for (int id : trungThuBossIds) {
            managers.boss.BossManager.gI().removeAllByBossId(id);
        }
        try {
            int[] maps = {consts.ConstMap.LANG_ARU, consts.ConstMap.LANG_MORI, consts.ConstMap.LANG_KAKAROT, consts.ConstMap.DAO_KAME};
            int[] npcTempIds = {95, 79, 69, 80, 66}; // TrungThu, ThapSangLongDen, ThoDaiCa, ThoChiChi, NoiBanh
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
    public void dropItem(player.Player p, Mob m, List<ItemMap> list, int x, int yEnd) {
        // Trung Thu: bỏ cơ chế rơi bí ngô, logic đã chuyển sang Halloween
    }

    @Override
    public boolean useItem(player.Player p, item.Item item) {
        return false;
    }

    @Override
    public List<TabShopSanta.SantaItemConfig> getSantaShopItems() {
        List<TabShopSanta.SantaItemConfig> items = new ArrayList<>();
        // Item event Trung Thu - thêm/sửa/xóa item tại đây
        // Ví dụ: items.add(new TabShopSanta.SantaItemConfig(itemId, (byte) typeSell, cost, new int[][]{{optionId, param}}, isNew));
        return items;
    }
}
