package event.newyear;

import consts.ConstItem;
import consts.ConstMap;
import consts.ConstNpc;
import item.Item;
import map.Map;
import npc.Npc;
import player.Player;
import server.Manager;
import services.ItemService;
import services.Service;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.InventoryService;
import utils.Util;

public class Npc_DuongTang {

    public void initNpc() {
        if (Manager.EVENT_SEVER != consts.ConstEvent.SU_KIEN_TET) {
            return;
        }
        addNpcToMap(ConstMap.LANG_ARU, 485, 432);
        addNpcToMap(ConstMap.NGU_HANH_SON, 1048, 408);
        addNpcToMap(ConstMap.NGU_HANH_SON_123, 141, 384);
    }

    private void addNpcToMap(int mapId, int cx, int cy) {
        Map map = MapService.gI().getMapById(mapId);
        if (map == null) {
            return;
        }
        Npc npc = new Npc(map.mapId, 1, cx, cy, ConstNpc.DUONG_TANG, 4544) {
            @Override
            public void openBaseMenu(Player player) {
                if (canOpenNpc(player)) {
                    switch (mapId) {
                        case ConstMap.LANG_ARU -> createOtherMenu(player, ConstNpc.BASE_MENU,
                                "A mi phò phò, thí chủ hãy giúp giải cứu đồ đệ của bần tăng đang bị phong ấn tại ngũ hành sơn",
                                "Đồng ý", "Từ chối");
                        case ConstMap.NGU_HANH_SON -> createOtherMenu(player, ConstNpc.BASE_MENU,
                                "A mi phò phò, thí chủ hãy thu thập bùa 'giải khai phong ấn'\n"
                                + "mỗi chữ 10 cái.\n"
                                + "|7|(Chi tiết xem tại diễn đàn, fanpage)",
                                "Giải\nPhong ấn", "Đóng");
                        case ConstMap.NGU_HANH_SON_123 -> createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Ra khỏi ngôi làng này sẽ gặp ngọn núi ngũ hành sơn",
                                "Về\nLàng Aru", "Đóng");
                        default -> super.openBaseMenu(player);
                    }
                }
            }

            @Override
            public void confirmMenu(Player player, int select) {
                if (canOpenNpc(player) && player.idMark.isBaseMenu()) {
                    switch (mapId) {
                        case ConstMap.LANG_ARU -> {
                            if (select == 0) {
                                if (player.nPoint.power >= 2_000_000_000L) {
                                    // zoneId = -1 (auto select), x = 141
                                    ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.NGU_HANH_SON_123, -1, 141);
                                } else {
                                    Service.gI().sendThongBao(player, "Bạn phải đạt đủ 2 Tỷ mới được vào");
                                }
                            }
                        }
                        case ConstMap.NGU_HANH_SON -> {
                            if (select == 0) {
                                Item chukhai = InventoryService.gI().findItemBag(player, ConstItem.CHU_KHAI);
                                Item chugiai = InventoryService.gI().findItemBag(player, ConstItem.CHU_GIAI);
                                Item chuan = InventoryService.gI().findItemBag(player, ConstItem.CHU_AN);
                                Item chuphong = InventoryService.gI().findItemBag(player, ConstItem.CHU_PHONG);

                                if (chukhai != null && chukhai.quantity >= 10 || chugiai != null && chugiai.quantity >= 10
                                        || chuan != null && chuan.quantity >= 10 || chuphong != null && chuphong.quantity >= 10) {
                                    if (chukhai != null) {
                                        InventoryService.gI().subQuantityItemsBag(player, chukhai, 10);
                                    }
                                    if (chugiai != null) {
                                        InventoryService.gI().subQuantityItemsBag(player, chugiai, 10);
                                    }
                                    if (chuan != null) {
                                        InventoryService.gI().subQuantityItemsBag(player, chuan, 10);
                                    }
                                    if (chuphong != null) {
                                        InventoryService.gI().subQuantityItemsBag(player, chuphong, 10);
                                    }
                                    Item selectedItem = null;
                                    switch (player.gender) {
                                        case 0:
                                            if (Util.isTrue(1, 10)) {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG_DE_TU);
                                                break;
                                            } else if (Util.isTrue(1, 10)) {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_BAT_GIOI_DE_TU);
                                                break;
                                            } else {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG);
                                                break;
                                            }
                                        case 1:
                                            if (Util.isTrue(1, 10)) {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG_DE_TU);
                                                break;
                                            } else if (Util.isTrue(1, 10)) {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_BAT_GIOI_DE_TU);
                                                break;
                                            } else {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG_545);
                                                break;
                                            }
                                        case 2:
                                            if (Util.isTrue(1, 10)) {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG_DE_TU);
                                                break;
                                            } else if (Util.isTrue(1, 10)) {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_BAT_GIOI_DE_TU);
                                                break;
                                            } else {
                                                selectedItem = ItemService.gI().createNewItem((short) ConstItem.CAI_TRANG_TON_NGO_KHONG_546);
                                                break;
                                            }
                                        default:
                                            return;
                                    }
                                    if (Util.isTrue(1, 50)) {
                                        selectedItem.itemOptions.add(new Item.ItemOption(50, Util.nextInt(25, 30)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(94, Util.nextInt(10, 20)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(77, Util.nextInt(70, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(103, Util.nextInt(70, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(101, Util.nextInt(50, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(114, Util.nextInt(50, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(106, 0));
                                        selectedItem.itemOptions.add(new Item.ItemOption(174, 2026));
                                    } else {
                                        selectedItem.itemOptions.add(new Item.ItemOption(50, Util.nextInt(25, 30)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(94, Util.nextInt(10, 20)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(77, Util.nextInt(70, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(103, Util.nextInt(70, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(101, Util.nextInt(50, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(114, Util.nextInt(50, 100)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(106, 0));
                                        selectedItem.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 3)));
                                        selectedItem.itemOptions.add(new Item.ItemOption(174, 2026));
                                    }
                                    InventoryService.gI().addItemBag(player, selectedItem);
                                    InventoryService.gI().sendItemBags(player);
                                    this.npcChat(player.zone,
                                            "A mi phò phò, đa tạ thí chủ tương trợ, xin hãy nhận món quà mọn này, bần tăng sẽ niệm chú giải thoát cho Ngộ Không");
                                    Service.gI().sendThongBao(player, "Bạn nhận được: " + selectedItem.template.name);
                                } else {
                                    createOtherMenu(player, 1,
                                            "A mi phò phò, thí chủ hãy thu thập bùa 'giải khai phong ấn', mỗi chứ 10 cái.",
                                            "Ok");
                                }
                            }
                        }
                        case ConstMap.NGU_HANH_SON_123 -> {
                            if (select == 0) {
                                ChangeMapService.gI().changeMapBySpaceShip(player, ConstMap.LANG_ARU, -1, 432);
                            }
                        }
                    }
                }
            }
        };
        map.addNpc(npc);
    }

}
