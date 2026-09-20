
package event.newyear;

import java.util.Random;
import consts.ConstItem;
import consts.ConstMap;
import consts.ConstNpc;
import item.Item;
import map.Map;
import npc.Npc;
import player.Player;
import server.Manager;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import services.map.MapService;

public class Npc_NgoKhong {

    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.NGU_HANH_SON);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TET) {
            Npc npc;
            npc = new Npc(map.mapId, 1, 979, 418, 48, 4520) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        switch (mapId) {
                            case 122 -> {
                                createOtherMenu(player, ConstNpc.BASE_MENU, "|0|Chu mi nga\n",
                                        "Tặng quả\nhồng đào", "Tặng quả\nhồng đào\nchín");
                            }
                            default ->
                                super.openBaseMenu(player);
                        }
                    }
                }

                @Override
                public void confirmMenu(Player player, int select) {
                    if (canOpenNpc(player)) {
                        if (player.idMark.isBaseMenu()) {
                            switch (select) {
                                case 0: {
                                    Item daohong = InventoryService.gI().findItemBag(player, ConstItem.QUA_HONG_DAO);

                                    if (daohong != null && daohong.quantity >= 1) {
                                        InventoryService.gI().subQuantityItemsBag(player, daohong, 1);
                                        Item[] chuItems = new Item[]{
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_KHAI),
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_GIAI),
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_AN),
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_PHONG)
                                        };
                                        Random rand = new Random();
                                        Item selectedItem = chuItems[rand.nextInt(chuItems.length)];
                                        InventoryService.gI().addItemBag(player, selectedItem);
                                        InventoryService.gI().sendItemBags(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được: " + selectedItem.template.name);
                                    } else {
                                        Service.gI().sendThongBao(player, "cần 1 quả hồng đào!");
                                    }
                                    break;
                                }
                                case 1: {
                                    Item daohong = InventoryService.gI().findItemBag(player, ConstItem.QUA_HONG_DAO_CHIN);
                                    if (daohong != null && daohong.quantity >= 1) {
                                        InventoryService.gI().subQuantityItemsBag(player, daohong, 1);
                                        Item[] chuItems = new Item[]{
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_KHAI),
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_GIAI),
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_AN),
                                            ItemService.gI().createNewItem((short) ConstItem.CHU_PHONG)
                                        };
                                        Random rand = new Random();
                                        Item selectedItem = chuItems[rand.nextInt(chuItems.length)];
                                        InventoryService.gI().addItemBag(player, selectedItem);
                                        InventoryService.gI().sendItemBags(player);
                                        Service.gI().sendThongBao(player, "Bạn nhận được: " + selectedItem.template.name);
                                    } else {
                                        Service.gI().sendThongBao(player, "cần 1 quả hồng đào chín!");
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            };
            map.addNpc(npc);
        }
    }

}