package event.trung_thu;
import consts.ConstMap;
import consts.ConstNpc;
import event.Event;
import item.Item;
import java.util.List;
import map.ItemMap;
import map.Map;
import map.Zone;
import mob.Mob;
import npc.Npc;
import player.Player;
import server.Manager;
import services.Service;
import services.ShopService;
import services.map.MapService;

public class Npc_TrungThu extends Event {
    
    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_TRUNG_THU;
    }

    public Zone zone;

    @Override
    public void init() {
        initNpc();
    }

    @Override
    public void initNpc() {
        Map map0 = MapService.gI().getMapById(ConstMap.LANG_ARU);
        Map map7 = MapService.gI().getMapById(ConstMap.LANG_MORI);
        Map map14 = MapService.gI().getMapById(ConstMap.LANG_KAKAROT);
        if (Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TRUNG_THU) {
            if (map0 != null) {
                map0.addNpc(createTrungThuNpc(map0));
            }
            if (map7 != null) {
                map7.addNpc(createTrungThuNpc(map7));
            }
            if (map14 != null) {
                map14.addNpc(createTrungThuNpc(map14));
            }
        }
    }

    private Npc createTrungThuNpc(Map map) {
        int[] pos = getNpcPos(map);
        return new Npc(map.mapId, 1, pos[0], pos[1], 95, 4118) {
            @Override
            public void openBaseMenu(Player player) {
                if (canOpenNpc(player)) {
                    this.createOtherMenu(
                            player,
                            ConstNpc.BASE_MENU,
                            "Chúc các bạn trung thu vui vẻ",
                            "Cửa hàng",
                            "Đổi\nThỏ cưng",
                            "Đổi 99\nCarot\n lấy quà",
                            "Đổi 99\nĐuôi khỉ\n lấy quà"
                    );
                }
            }

            @Override
            public void confirmMenu(Player player, int select) {
                if (!canOpenNpc(player)) {
                    return;
                }
                int index = player.idMark.getIndexMenu();
                switch (index) {
                    case ConstNpc.BASE_MENU -> {
                        switch (select) {
                            case 0 -> {
                                ShopService.gI().opendShop(player, "TRUNG_THU", false);
                            }
                            case 1 -> {
                                this.createOtherMenu(player, 999,
                                        "Đổi 20 Bánh trung thu Gà quay lấy Thỏ xám\n"
                                        + "Đổi 10 Bánh trung thu thập cẩm lấy Thỏ trắng\n"
                                        + "Đổi 10 Bánh trung thu Hạt sen lấy Pet mèo đen đuôi vàng\n"
                                        + "Hạn sử dụng ngẫu nhiên",
                                        "Đổi\n Thỏ xám", "Đổi\n Thỏ trắng", "Đổi\n Mèo đen\n đuôi vàng", "Đóng");
                            }
                            case 2 -> {
                                ShopService.gI().opendShop(player, "THO_CHI_CHI", false);
                            }
                            case 3 -> {
                                ShopService.gI().opendShop(player, "THO_CHI_CHI", false);
                            }
                        }
                    }
                    case 999 -> {
                        switch (select) {
                            case 0 ->
                                this.createOtherMenu(player, 998,
                                        "Bạn có chắc muốn đổi\n20 Bánh trung thu Gà quay lấy Thỏ xám ?", "Đồng ý", "Từ chối");
                            case 1 ->
                                this.createOtherMenu(player, 997,
                                        "Bạn có chắc muốn đổi\n10 Bánh trung thu thập cẩm lấy Thỏ trắng ?", "Đồng ý", "Từ chối");
                            case 2 ->
                                this.createOtherMenu(player, 996,
                                        "Bạn có chắc muốn đổi\n10 Bánh trung thu hạt sen lấy Pet mèo đen đuôi vàng ?", "Đồng ý", "Từ chối");
                        }
                    }
                    case 998 -> {
                        if (select == 0) {
                            // code logic đổi thỏ xám
                            Service.gI().sendThongBao(player, "Bạn đã nhận Thỏ xám");
                        }
                    }
                    case 997 -> {
                        if (select == 0) {
                            // code logic đổi thỏ trắng
                            Service.gI().sendThongBao(player, "Bạn đã nhận Thỏ trắng");
                        }
                    }
                    case 996 -> {
                        if (select == 0) {
                            // code logic đổi mèo đen
                            Service.gI().sendThongBao(player, "Bạn đã nhận Mèo đen đuôi vàng");
                        }
                    }
                }
            }
        };
    }

    private int[] getNpcPos(Map map) {
        int x;
        x = switch (map.mapId) {
            case ConstMap.LANG_ARU ->
                1072;
            case ConstMap.LANG_MORI ->
                1085;
            case ConstMap.LANG_KAKAROT ->
                1098;
            default ->
                500;
        };
        x = Math.max(60, Math.min(x, map.mapWidth - 60));
        int y = map.yPhysicInTop(x, 100);
        return new int[]{x, y};
    }

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
