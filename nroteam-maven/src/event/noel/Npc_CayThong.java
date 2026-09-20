package event.noel;

import consts.ConstEvent;
import consts.ConstMap;
import consts.ConstNpc;
import event.Event;
import item.Item;
import java.util.List;
import map.ItemMap;
import map.Map;
import map.Zone;
import mob.Mob;
import player.Player;
import server.Manager;
import services.map.MapService;

public class Npc_CayThong extends Event {

    public Zone zone;

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_NOEL;
    }

    @Override
    public void init() {
        initNpc();
    }
//
    @Override
    public void initNpc() {
        Map map0 = MapService.gI().getMapById(ConstMap.LANG_ARU);
        Map map7 = MapService.gI().getMapById(ConstMap.LANG_MORI);
        Map map14 = MapService.gI().getMapById(ConstMap.LANG_KAKAROT);
        if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_NOEL) {
            if (map0 != null) {
//                map0.addNpc(createcaythongNpc(map0));
            }
            if (map7 != null) {
//                map7.addNpc(createcaythongNpc(map7));
            }
            if (map14 != null) {
//                map14.addNpc(createcaythongNpc(map14));
            }
        }
    }
//
//    private Npc createcaythongNpc(Map map) {
//        int[] pos = getNpcPos(map);
////        byte tempId = Manager.ensureNpcTemplate(null, "",
////                1653, 1654, 1655, 14515);
//        Npc npc = new Npc(map.mapId, 1, pos[0], pos[1], tempId, 14515) {
//            @Override
//            public void openBaseMenu(Player player) {
//                if (canOpenNpc(player)) {
//                    this.createOtherMenu(
//                            player,
//                            ConstNpc.BASE_MENU,
//                            "Đang có 1 lượt trang trí\n "
//                            + "Trang trí 2000 lượt sẽ tặng: x2 exp toàn máy chủ 12 giờ\n"
//                            + "Trang trí 5000 lượt sẽ tặng: x3 exp toàn máy chủ 24 giờ\n"
//                            + "Trang trí 10000 lượt sẽ tặng: x3 exp toàn máy chủ 72 giờ",
//                            "Trang trí\n Quả chấu",
//                            "Trang trí\n Ngọc rồng\n 1 sao",
//                            "Vùng đất\n băng giá\n Sự kiện\n Noel",
//                            "Đóng");
//                }
//            }
//
//            @Override
//            public void confirmMenu(Player player, int select) {
//                if (!canOpenNpc(player) || !player.idMark.isBaseMenu()) {
//                    return;
//                }
//                if (select == 0) {
//                }
//            }
//        };
//        markEventNpc(npc);
//        return npc;
//    }
//
//    private int[] getNpcPos(Map map) {
//        int x;
//        x = switch (map.mapId) {
//            case ConstMap.LANG_ARU ->
//                1072;
//            case ConstMap.LANG_MORI ->
//                1085;
//            case ConstMap.LANG_KAKAROT ->
//                1098;
//            default ->
//                500;
//        };
//        x = Math.max(60, Math.min(x, map.mapWidth - 60));
//        int y = map.yPhysicInTop(x, 100);
//        return new int[]{x, y};
//    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(Player player, Mob mob, List<ItemMap> list, int x, int yEnd) {
    }

    @Override
    public boolean useItem(Player player, Item item) {
        return false;
    }
}
