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
import services.ItemService;
import services.Service;
import services.map.MapService;
import services.player.InventoryService;
import services.top.TopTrungThuService;
import utils.Util;

public class ThapSangLongDen extends Event {

    public Zone zone;

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_TRUNG_THU;
    }

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
                map0.addNpc(createLongDenNpc(map0));
            }
            if (map7 != null) {
                map7.addNpc(createLongDenNpc(map7));
            }
            if (map14 != null) {
                map14.addNpc(createLongDenNpc(map14));
            }
        }
    }

    private Npc createLongDenNpc(Map map) {
        int[] pos = getNpcPos(map);
        return new Npc(map.mapId, 1, pos[0], pos[1], 79, 4118) {
            @Override
            public void openBaseMenu(Player player) {
                if (canOpenNpc(player)) {
                    this.createOtherMenu(
                            player,
                            ConstNpc.BASE_MENU,
                            "Mỗi lần treo đèn bạn sẽ được tặng 1 món quà xịn xò nhất.",
                            "Treo đèn",
                            "Đóng"
                    );
                }
            }

            @Override
            public void confirmMenu(Player player, int select) {
                if (!canOpenNpc(player) || !player.idMark.isBaseMenu()) {
                    return;
                }
                if (select == 0) {
                    Item lantern = InventoryService.gI().findItemBag(player, 1311);
                    if (lantern == null || lantern.quantity < 1) {
                        return;
                    }
                    if (InventoryService.gI().getCountEmptyBag(player) < 1) {
                        Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống");
                        return;
                    }
                    int rand = Util.nextInt(100);
                    if (rand < 50) {
                        int goldAmount = Util.nextInt(5, 1_000_000);
                        player.inventory.gold += goldAmount;
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được " + Util.numberToMoney(goldAmount) + " vàng");
                        return;
                    }
                    int rewardType = Util.nextInt(100);
                    Item it = null;
                    if (rewardType < 15) {
                        int[] ids = {1150, 1151, 1152, 1153, 1154};
                        short id = (short) ids[Util.nextInt(ids.length)];
                        it = ItemService.gI().createNewItem(id);
                        it.itemOptions.add(new Item.ItemOption(30, 0));

                    } else if (rewardType < 30) {
                        it = ItemService.gI().createNewItem((short) 1213);
                        int p = Util.nextInt(10, 15);
                        int a = Util.nextInt(0, 30);
                        int c = Util.nextInt(5, 10);
                        it.itemOptions.add(new Item.ItemOption(50, p));
                        it.itemOptions.add(new Item.ItemOption(77, p));
                        it.itemOptions.add(new Item.ItemOption(103, p));
                        it.itemOptions.add(new Item.ItemOption(5, c));
                        it.itemOptions.add(new Item.ItemOption(30, a));
                        it.itemOptions.add(new Item.ItemOption(93, a));

                    } else if (rewardType < 45) {
                        it = ItemService.gI().createNewItem((short) 1205);
                        int p = Util.nextInt(20, 24);
                        it.itemOptions.add(new Item.ItemOption(50, p));
                        it.itemOptions.add(new Item.ItemOption(77, p));
                        it.itemOptions.add(new Item.ItemOption(103, p));
                        it.itemOptions.add(new Item.ItemOption(106, p));
                        it.itemOptions.add(new Item.ItemOption(154, p));

                    } else if (rewardType < 60) {
                        it = ItemService.gI().createNewItem((short) 1684);
                        it.itemOptions.add(new Item.ItemOption(50, 25));
                        it.itemOptions.add(new Item.ItemOption(101, 70));
                        it.itemOptions.add(new Item.ItemOption(95, 15));
                        it.itemOptions.add(new Item.ItemOption(30, 0));

                    } else if (rewardType < 75) {
                        it = ItemService.gI().createNewItem((short) 1755);
                        int randomValue = Util.nextInt(15, 18);
                        it.itemOptions.add(new Item.ItemOption(50, randomValue));
                        it.itemOptions.add(new Item.ItemOption(101, randomValue));
                        it.itemOptions.add(new Item.ItemOption(95, randomValue));
                        it.itemOptions.add(new Item.ItemOption(236, 10));
                        it.itemOptions.add(new Item.ItemOption(97, 10));
                        it.itemOptions.add(new Item.ItemOption(30, 0));

                    } else {
                        short id = (short) Util.nextInt(213, 219);
                        player.charms.addTimeCharms(id, 60);
                        it = ItemService.gI().createNewItem(id);
                    }
                    Service.gI().sendThongBao(player, "Bạn nhận được " + it.template.name);
                    InventoryService.gI().addItemBag(player, it);
                    InventoryService.gI().sendItemBags(player);
                    InventoryService.gI().subQuantityItemsBag(player, lantern, 1);
                    TopTrungThuService.gI().addLongDenPoint(player, 1);

                }

            }
        };
    }

    private int[] getNpcPos(Map map) {
        int x;
        x = switch (map.mapId) {
            case ConstMap.LANG_ARU ->
                952;
            case ConstMap.LANG_MORI ->
                950;
            case ConstMap.LANG_KAKAROT ->
                944;
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
