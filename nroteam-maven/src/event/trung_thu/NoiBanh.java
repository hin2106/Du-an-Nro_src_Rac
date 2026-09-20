package event.trung_thu;

import consts.ConstFont;
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
import services.map.MapService;
import services.player.InventoryService;

public class NoiBanh extends Event {

    public Zone zone;

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_TRUNG_THU;
    }

    private static final String KEY_GA_QUAY = "GA_QUAY";
    private static final String KEY_GA_QUAY_HH = "GA_QUAY_HH";
    private static final String KEY_HAT_SEN = "HAT_SEN";

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
            if (map0 != null)   map0.addNpc(createNoiBanhNpc(map0));
            if (map7 != null)   map7.addNpc(createNoiBanhNpc(map7));
            if (map14 != null)  map14.addNpc(createNoiBanhNpc(map14));
        }
    }

    private Npc createNoiBanhNpc(Map map) {
        int[] pos = getNpcPos(map);
        return new Npc(map.mapId, 1, pos[0], pos[1], 66, 7084) {
            @Override
            public void openBaseMenu(Player player) {
                if (canOpenNpc(player)) {
                    this.createOtherMenu(
                            player,
                            ConstNpc.BASE_MENU,
                            "Xin chào, mình là nồi bánh, bạn muốn nấu bánh gì?",
                            "Bánh trung\n thu Gà quay",
                            "Bánh trung\n thu Gà quay\n Hảo hạng",
                            "Bánh trung\n thu Hạt sen",
                            "Đóng"
                    );
                }
            }

            @Override
            public void confirmMenu(Player player, int select) {
                if (!canOpenNpc(player)) {
                    return;
                }
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> {
                            Item botMi = InventoryService.gI().findItemBag(player, 888);
                            Item dauXanh = InventoryService.gI().findItemBag(player, 750);
                            Item trungVitMuoi = InventoryService.gI().findItemBag(player, 886);
                            Item gaQuayNguyenCon = InventoryService.gI().findItemBag(player, 887);
                            long qBotMi = botMi != null ? botMi.quantity : 0;
                            long qDauXanh = dauXanh != null ? dauXanh.quantity : 0;
                            long qTrung = trungVitMuoi != null ? trungVitMuoi.quantity : 0;
                            long qGa = gaQuayNguyenCon != null ? gaQuayNguyenCon.quantity : 0;

                            StringBuilder text = new StringBuilder();
                            text.append(ConstFont.BOLD_GREEN).append("Bánh trung thu Gà quay\n")
                                    .append(formatRequirement("Bột mì", qBotMi, 10))
                                    .append(formatRequirement("Đậu xanh", qDauXanh, 10))
                                    .append(formatRequirement("Trứng vịt muối", qTrung, 2))
                                    .append(formatRequirement("Gà quay nguyên con", qGa, 1))
                                    .append(ConstFont.BOLD_RED).append("Giá vàng: ").append(String.format("%,d", 20_000_000)).append("\n");
                            boolean ok = qBotMi >= 10 && qDauXanh >= 10 && qTrung >= 2 && qGa >= 1;
                            if (!ok) {
                                createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Từ chối");
                            } else {
                                createOtherMenu(player, ConstNpc.ORTHER_MENU1, text.toString(), new String[]{"Đồng ý", "Từ chối"}, KEY_GA_QUAY);
                            }
                        }
                        case 1 -> {
                            Item botMi = InventoryService.gI().findItemBag(player, 888);
                            Item dauXanh = InventoryService.gI().findItemBag(player, 750);
                            Item trungVitMuoi = InventoryService.gI().findItemBag(player, 886);
                            Item gaQuayNguyenCon = InventoryService.gI().findItemBag(player, 887);
                            long qBotMi = botMi != null ? botMi.quantity : 0;
                            long qDauXanh = dauXanh != null ? dauXanh.quantity : 0;
                            long qTrung = trungVitMuoi != null ? trungVitMuoi.quantity : 0;
                            long qGa = gaQuayNguyenCon != null ? gaQuayNguyenCon.quantity : 0;

                            StringBuilder text = new StringBuilder();
                            text.append(ConstFont.BOLD_GREEN).append("Bánh trung thu Gà quay\n")
                                    .append("30% Cơ hội nhận thêm Bánh trung thu thập cẩm\n")
                                    .append(formatRequirement("Bột mì", qBotMi, 10))
                                    .append(formatRequirement("Đậu xanh", qDauXanh, 10))
                                    .append(formatRequirement("Trứng vịt muối", qTrung, 2))
                                    .append(formatRequirement("Gà quay nguyên con", qGa, 1))
                                    .append(ConstFont.BOLD_RED).append("Giá ngọc: ").append(20).append("\n");
                            boolean ok = qBotMi >= 10 && qDauXanh >= 10 && qTrung >= 2 && qGa >= 1;
                            if (!ok) {
                                createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Từ chối");
                            } else {
                                createOtherMenu(player, ConstNpc.ORTHER_MENU1, text.toString(), new String[]{"Đồng ý", "Từ chối"}, KEY_GA_QUAY_HH);
                            }
                        }
                        case 2 -> {
                            Item botMi = InventoryService.gI().findItemBag(player, 888);
                            Item dauXanh = InventoryService.gI().findItemBag(player, 750);
                            Item trungVitMuoi = InventoryService.gI().findItemBag(player, 886);
                            Item hatSen = InventoryService.gI().findItemBag(player, 1312);
                            long qBotMi = botMi != null ? botMi.quantity : 0;
                            long qDauXanh = dauXanh != null ? dauXanh.quantity : 0;
                            long qTrung = trungVitMuoi != null ? trungVitMuoi.quantity : 0;
                            long qHatSen = hatSen != null ? hatSen.quantity : 0;

                            StringBuilder text = new StringBuilder();
                            text.append(ConstFont.BOLD_GREEN).append("Bánh trung thu Hạt sen\n")
                                    .append(formatRequirement("Bột mì", qBotMi, 10))
                                    .append(formatRequirement("Đậu xanh", qDauXanh, 10))
                                    .append(formatRequirement("Trứng vịt muối", qTrung, 2))
                                    .append(formatRequirement("Hạt sen", qHatSen, 1))
                                    .append(ConstFont.BOLD_RED).append("Giá ngọc: ").append(20).append("\n");
                            boolean ok = qBotMi >= 10 && qDauXanh >= 10 && qTrung >= 2 && qHatSen >= 1;
                            if (!ok) {
                                createOtherMenu(player, ConstNpc.IGNORE_MENU, text.toString(), "Từ chối");
                            } else {
                                createOtherMenu(player, ConstNpc.ORTHER_MENU1, text.toString(), new String[]{"Đồng ý", "Từ chối"}, KEY_HAT_SEN);
                            }
                        }
                    }
                    return;
                }
                if (player.idMark.getIndexMenu() == ConstNpc.ORTHER_MENU1) {
                    if (select == 0) {
                        Object key = npc.NpcFactory.PLAYERID_OBJECT.get(player.id);
                        if (KEY_GA_QUAY.equals(key)) {
                            craftGaQuay(player);
                        } else if (KEY_GA_QUAY_HH.equals(key)) {
                            craftGaQuayHaoHang(player);
                        } else if (KEY_HAT_SEN.equals(key)) {
                            craftHatSen(player);
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
                576;
            case ConstMap.LANG_MORI ->
                514;
            case ConstMap.LANG_KAKAROT ->
                193;
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

    private static String formatRequirement(String itemName, long currentQuantity, long requiredQuantity) {
        return (currentQuantity >= requiredQuantity ? ConstFont.BOLD_BLUE : ConstFont.BOLD_RED)
                + itemName + " " + currentQuantity + "/" + requiredQuantity + "\n";
    }


    private void craftGaQuay(Player player) {
        Item botMi = InventoryService.gI().findItemBag(player, 888);
        Item dauXanh = InventoryService.gI().findItemBag(player, 750);
        Item trungVitMuoi = InventoryService.gI().findItemBag(player, 886);
        Item ga = InventoryService.gI().findItemBag(player, 887);
        if (botMi == null || botMi.quantity < 10 || dauXanh == null || dauXanh.quantity < 10
                || trungVitMuoi == null || trungVitMuoi.quantity < 2 || ga == null || ga.quantity < 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.inventory.gold < 20_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(player, botMi, 10);
        InventoryService.gI().subQuantityItemsBag(player, dauXanh, 10);
        InventoryService.gI().subQuantityItemsBag(player, trungVitMuoi, 2);
        InventoryService.gI().subQuantityItemsBag(player, ga, 1);
        player.inventory.subGold(20_000_000);
        Service.gI().sendMoney(player);
        Item out = services.ItemService.gI().createNewItem((short) 890, 1);
        InventoryService.gI().addItemBag(player, out);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Nấu bánh thành công!");
    }

    private void craftGaQuayHaoHang(Player player) {
        Item botMi = InventoryService.gI().findItemBag(player, 888);
        Item dauXanh = InventoryService.gI().findItemBag(player, 750);
        Item trungVitMuoi = InventoryService.gI().findItemBag(player, 886);
        Item ga = InventoryService.gI().findItemBag(player, 887);
        if (botMi == null || botMi.quantity < 10 || dauXanh == null || dauXanh.quantity < 10
                || trungVitMuoi == null || trungVitMuoi.quantity < 2 || ga == null || ga.quantity < 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.inventory.gem < 20) {
            Service.gI().sendThongBao(player, "Không đủ ngọc");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(player, botMi, 10);
        InventoryService.gI().subQuantityItemsBag(player, dauXanh, 10);
        InventoryService.gI().subQuantityItemsBag(player, trungVitMuoi, 2);
        InventoryService.gI().subQuantityItemsBag(player, ga, 1);
        player.inventory.subGem(20);
        Service.gI().sendMoney(player);
        Item out = services.ItemService.gI().createNewItem((short) 890, 1);
        InventoryService.gI().addItemBag(player, out);
        if (utils.Util.isTrue(30, 100)) {
            Item extra = services.ItemService.gI().createNewItem((short) 891, 1);
            InventoryService.gI().addItemBag(player, extra);
        }
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Nấu bánh thành công!");
    }

    private void craftHatSen(Player player) {
        Item botMi = InventoryService.gI().findItemBag(player, 888);
        Item dauXanh = InventoryService.gI().findItemBag(player, 750);
        Item trungVitMuoi = InventoryService.gI().findItemBag(player, 886);
        Item hatSen = InventoryService.gI().findItemBag(player, 1312);
        if (botMi == null || botMi.quantity < 10 || dauXanh == null || dauXanh.quantity < 10
                || trungVitMuoi == null || trungVitMuoi.quantity < 2 || hatSen == null || hatSen.quantity < 1) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.inventory.gem < 20) {
            Service.gI().sendThongBao(player, "Không đủ ngọc");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(player, botMi, 10);
        InventoryService.gI().subQuantityItemsBag(player, dauXanh, 10);
        InventoryService.gI().subQuantityItemsBag(player, trungVitMuoi, 2);
        InventoryService.gI().subQuantityItemsBag(player, hatSen, 1);
        player.inventory.subGem(20);
        Service.gI().sendMoney(player);
        Item out = services.ItemService.gI().createNewItem((short) 1313, 1);
        InventoryService.gI().addItemBag(player, out);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Nấu bánh thành công!");
    }
}
