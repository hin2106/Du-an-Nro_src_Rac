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
import services.ItemService;
import services.player.InventoryService;
import services.player.PlayerService;
import services.map.MapService;
import item.Item.ItemOption;
import utils.Util;

public class Npc_ThoDaiCa extends Event {

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
        Map map = MapService.gI().getMapById(ConstMap.DAO_KAME);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TRUNG_THU) {
            Npc npc = new Npc(map.mapId, 1, 337, 288, 69, 4118) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        this.createOtherMenu(
                                player,
                                ConstNpc.BASE_MENU,
                                "Hôm nay ta rảnh nên sẽ cho các ngươi 1 điều ước",
                                "Ước bằng\n 50 ngọc",
                                "Ước bằng\n 99 cà rốt",
                                "Đóng"
                        );
                    }
                }

                @Override
                public void confirmMenu(Player player, int select) {
                    if (!canOpenNpc(player)) {
                        return;
                    }

                    int menu = player.idMark.getIndexMenu();
                    boolean isBase = player.idMark.isBaseMenu();

                    if (isBase || menu == ConstNpc.BASE_MENU) {
                        if (select == 0) {
                            this.createOtherMenu(player, 999,
                                    "50 ngọc cũng được, hãy ước đi thể hiện mình là đại gia.",
                                    "Đồng ý", "Từ chối");
                        } else if (select == 1) {
                            this.createOtherMenu(player, 998,
                                    "99 cà rốt cũng được, loại thượng phẩm à, ngươi ước đi",
                                    "Đồng ý", "Từ chối");
                        }
                        return;
                    }

                    if (menu == 999) {
                        if (select == 0) {
                            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                                Service.gI().sendThongBao(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
                                return;
                            }
                            if (player.inventory.gem < 50) {
                                int need = 50 - player.inventory.gem;
                                Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu " + need + " ngọc nữa");
                                return;
                            }
                            player.inventory.gem -= 50;
                            PlayerService.gI().sendInfoHpMpMoney(player);

                            UocX50Gem(player);

                            this.createOtherMenu(player, 9999, "                  Thích nhé             ", "Đóng");
                        }
                        return;
                    }

                    if (menu == 998) {
                        if (select == 0) {
                            if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
                                Service.gI().sendThongBao(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
                                return;
                            }
                            Item carot = InventoryService.gI().findItemBag(player, 99);
                            if (carot == null || carot.quantity < 99) {
                                Service.gI().sendThongBao(player, "Bạn không đủ 99 cà rốt");
                                return;
                            }
                            InventoryService.gI().subQuantityItemsBag(player, carot, 99);
                            InventoryService.gI().sendItemBags(player);

                            UocX99CaRot(player);

                            this.createOtherMenu(player, 9999, "           Thích nhé              ", "Đóng");
                        }
                    }
                }
            };
            map.addNpc(npc);
        }
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

    private void UocX50Gem(Player pl) {
        short[] fragIds = new short[]{828, 829, 830, 831, 832, 833, 834, 835, 836, 837, 838, 839, 840, 841, 842};
        short goiDau9 = 597;
        short dau10 = 595;
        short petThoU = 1686;
        short ctGohanBu = 765;
        short[] ctVipByGender = new short[]{604, 605, 606};
        int roll = Util.nextInt(1, 100);
        Item reward;
        if (roll <= 35) {
            reward = ItemService.gI().createNewItem(fragIds[Util.nextInt(0, fragIds.length - 1)]);
            reward.itemOptions.add(new ItemOption(30, 0));
            reward.quantity = 1;
        } else if (roll <= 50) {
            int idItem = Util.nextInt(213, 219);
            pl.charms.addTimeCharms(idItem, 60);
            reward = ItemService.gI().createNewItem((short) idItem);
            reward.quantity = 1;

        } else if (roll <= 62) {
            reward = ItemService.gI().createNewItem(goiDau9);
            reward.quantity = 1;
        } else if (roll <= 75) {
            reward = ItemService.gI().createNewItem(dau10);
            reward.quantity = Util.nextInt(20, 30);
        } else if (roll <= 88) {
            reward = ItemService.gI().createNewItem(ctVipByGender[pl.gender]);
            reward.itemOptions.add(new ItemOption(50, 23));
            reward.itemOptions.add(new ItemOption(77, 20));
            reward.itemOptions.add(new ItemOption(103, 20));
            if (Util.isTrue(10, 100)) {
                reward.itemOptions.add(new ItemOption(73, 0));
            } else {
                int days = (Util.nextInt(0, 1) == 0) ? 15 : 30;
                reward.itemOptions.add(new ItemOption(93, days));
            }
        } else if (roll <= 95) {
            reward = ItemService.gI().createNewItem(ctGohanBu);
            reward.itemOptions.add(new ItemOption(50, 22));
            reward.itemOptions.add(new ItemOption(77, 19));
            reward.itemOptions.add(new ItemOption(103, 19));
            reward.itemOptions.add(new ItemOption(27, 5));
            reward.itemOptions.add(new ItemOption(28, 5));
            reward.itemOptions.add(new ItemOption(177, 0));
            if (Util.isTrue(10, 100)) {
                reward.itemOptions.add(new ItemOption(73, 0));
            } else {
                int days = (Util.nextInt(0, 1) == 0) ? 15 : 30;
                reward.itemOptions.add(new ItemOption(93, days));
            }
        } else {
            reward = ItemService.gI().createNewItem(petThoU);
            reward.itemOptions.add(new ItemOption(50, Util.nextInt(10, 15)));
            reward.itemOptions.add(new ItemOption(77, Util.nextInt(10, 15)));
            reward.itemOptions.add(new ItemOption(103, Util.nextInt(10, 15)));
            reward.itemOptions.add(new ItemOption(236, Util.nextInt(10, 20)));
            reward.itemOptions.add(new ItemOption(14, 10));
            reward.itemOptions.add(new ItemOption(30, 0));
            if (Util.isTrue(10, 100)) {
                reward.itemOptions.add(new ItemOption(73, 0));
            } else {
                int days = (Util.nextInt(0, 1) == 0) ? 15 : 30;
                reward.itemOptions.add(new ItemOption(93, days));
            }
        }
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().sendItemBags(pl);
        if (reward.quantity > 1) {
            Service.gI().sendThongBao(pl, "Bạn nhận được " + reward.quantity + " " + reward.template.name);
        } else {
            Service.gI().sendThongBao(pl, "Bạn nhận được " + reward.template.name);
        }
    }

    private void UocX99CaRot(Player pl) {
        UocX50Gem(pl);
    }
}
