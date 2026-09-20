package event.halloween;

import consts.ConstMap;
import consts.ConstNpc;
import event.EventNpcUtils;
import event.EventUtils;
import map.Map;
import npc.Npc;
import npc.NpcFactory;
import player.Player;
import server.Manager;
import services.ShopService;
import services.map.MapService;
import services.top.TopHalloweenService;

public class Npc_ThoChiChi {

    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.DAO_KAME);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_HALLOWEEN) {
            Npc npc;
            npc = new Npc(map.mapId, 1, 245, 288, 80, 9966) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Bạn muốn hỏi Chi Chi?",
                                "Top\nHộp kẹo\nMa quỷ",
                                "Top\nThiệp\nHalloween",
                                "Cửa hàng\nHALLOWEEN",
                                "Hủy bỏ\ntrang bị \ncó HSD",
                                "Đóng");
                    }
                }

                @Override
                public void confirmMenu(Player player, int select) {
                    if (!canOpenNpc(player))
                        return;
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 -> openCandyTopMenu(player);
                            case 1 -> openCardTopMenu(player);
                            case 2 -> ShopService.gI().opendShop(player, "HALLOWEEN", true);
                            case 3 -> EventNpcUtils.showItemsWithHSD(player, this);
                        }
                        return;
                    }
                    if (player.idMark.getIndexMenu() == ConstNpc.ORTHER_MENU1) {
                        Object key = NpcFactory.PLAYERID_OBJECT.get(player.id);
                        if (key instanceof String s) {
                            handleTopMenuSelection(player, select, s);
                        }
                    }
                    if (player.idMark.getIndexMenu() == EventNpcUtils.MENU_HUY_TRANG_BI_HSD) {
                        if (select == 0) {
                            EventNpcUtils.removeItemsWithHSD(player);
                        }
                    }
                }

                private void openCandyTopMenu(Player player) {
                    String[] menu = EventUtils.buildTopMenuOptions(TopHalloweenService.gI().isClaimOpen());
                    String message = EventUtils.formatTopMenu("Halloween", "Hộp kẹo Ma quỷ",
                            TopHalloweenService.EVENT_END, TopHalloweenService.CLAIM_DEADLINE);
                    this.createOtherMenu(player, ConstNpc.ORTHER_MENU1, message, menu, "CANDY");
                }

                private void openCardTopMenu(Player player) {
                    String[] menu = EventUtils.buildTopMenuOptions(TopHalloweenService.gI().isClaimOpen());
                    String message = EventUtils.formatTopMenu("Halloween", "Thiệp Halloween",
                            TopHalloweenService.EVENT_END, TopHalloweenService.CLAIM_DEADLINE);
                    this.createOtherMenu(player, ConstNpc.ORTHER_MENU1, message, menu, "CARD");
                }

                private void handleTopMenuSelection(Player player, int select, String topType) {
                    TopHalloweenService service = TopHalloweenService.gI();
                    switch (topType) {
                        case "CANDY" -> handleSelection(player, select, 1, player.eventPointType3,
                                () -> service.openPreviewCandy(player));
                        case "CARD" -> handleSelection(player, select, 2, player.eventPointType4,
                                () -> service.openPreviewCard(player));
                    }
                }

                private void handleSelection(Player player, int select, int topType, int points,
                        Runnable previewAction) {
                    TopHalloweenService service = TopHalloweenService.gI();
                    switch (select) {
                        case 0 -> service.showTop(player, topType);
                        case 1 -> service.showSelfRank(player, topType, points);
                        case 2 -> previewAction.run();
                    }
                }
            };
            map.addNpc(npc);
        }
    }
}
