package npc.list;

import boss.BossID;
import combine.CombineService;
import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.ItemService;
import services.Service;
import services.dungeon.TrainingService;
import services.map.ChangeMapService;
import services.player.InventoryService;
import services.top.TopWhisService;

public class Whis extends Npc {

    private static final int COST_HD = 50000000;

    public Whis(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player))
            return;
        switch (this.mapId) {
            case 154 -> {
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Thử đánh với ta xem nào.\nNgươi còn 1 lượt nữa cơ mà.",
                        "Nói chuyện", "Top 100",
                        "[LV:" + (player.traning.getTop() + 1) + "]");
            }
            case 48 -> createOtherMenu(player, ConstNpc.BASE_MENU, "Coming Soon", "OK");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player))
            return;
        switch (this.mapId) {
            case 154 -> {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> createOtherMenu(player, 5, "Ta sẽ giúp ngươi chế tạo trang bị thiên sứ",
                                "Chế tạo", "Đánh Thức Bill", "Từ chối");
                        case 1 -> TopWhisService.gI().showTop(player);
                        case 2 -> TrainingService.gI().callBoss(player, BossID.WHIS, false);
                    }
                } else if (player.idMark.getIndexMenu() == 5) {
                    switch (select) {
                        case 0 -> {
                            if (!player.setClothes.checkSetDes()) {
                                createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Ngươi hãy trang bị đủ 5 món trang bị Hủy Diệt rồi nói chuyện tiếp.", "OK");
                                return;
                            }
                            player.iDMark.setNpcChose(this);
                            CombineService.gI().openTabCombine(player, CombineService.NANG_CAP_DO_TS);
                        }
                        case 1 -> {
                            Item chuongDong = InventoryService.gI().findItemBag(player, 1165);
                            Item caTuyet = InventoryService.gI().findItemBag(player, 1166);
                            Item banhQuy = InventoryService.gI().findItemBag(player, 1167);
                            Item keoDuong = InventoryService.gI().findItemBag(player, 1168);
                            Item keoNguoiTuyet = InventoryService.gI().findItemBag(player, 1169);

                            if (chuongDong == null || caTuyet == null || banhQuy == null || keoDuong == null
                                    || keoNguoiTuyet == null
                                    || chuongDong.quantity < 30 || caTuyet.quantity < 30 || banhQuy.quantity < 30
                                    || keoDuong.quantity < 30 || keoNguoiTuyet.quantity < 1) {
                                npcChat(player, "Ngươi không đủ nguyên liệu.");
                                return;
                            }

                            if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                npcChat(player, "Hành trang của bạn không đủ chỗ trống");
                                return;
                            }

                            InventoryService.gI().subQuantityItemsBag(player, chuongDong, 30);
                            InventoryService.gI().subQuantityItemsBag(player, caTuyet, 30);
                            InventoryService.gI().subQuantityItemsBag(player, banhQuy, 30);
                            InventoryService.gI().subQuantityItemsBag(player, keoDuong, 30);
                            InventoryService.gI().subQuantityItemsBag(player, keoNguoiTuyet, 1);

                            Item goiQua = ItemService.gI().createNewItem((short) 1170);
                            InventoryService.gI().addItemBag(player, goiQua);
                            InventoryService.gI().sendItemBags(player);
                            npcChat(player, "Bạn Nhận Được 1 Gói Quà !");
                        }
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_START_COMBINE) {
                    if (player.combineNew != null && player.combineNew.typeCombine == CombineService.NANG_CAP_DO_TS) {
                        CombineService.gI().startCombine(player);
                    }
                }
            }
            
        }
    }

    
}