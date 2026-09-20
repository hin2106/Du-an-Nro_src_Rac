package event.women;

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
import services.top.TopWomenService;

public class Npc_ThoChiChi {

    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.DAO_KAME);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_20_10) {
            Npc npc;
            npc = new Npc(map.mapId, 1, 245, 288, 80, 9966) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        this.createOtherMenu(
                                player,
                                ConstNpc.BASE_MENU,
                                "Bạn muốn hỏi chi ?",
                                "Top\nCapsule\nTrang Sức VIP",
                                "Top\nThiệp chúc\nVIP",
                                "Top\nHộp quà\n20/10",
                                "Cửa hàng",
                                "Hủy bỏ\ntrang bị có HSD",
                                "Đóng");
                    }
                }

                @Override
                public void confirmMenu(Player player, int select) {
                    if (!canOpenNpc(player)) {
                        return;
                    }
                    if (player.idMark.isBaseMenu()) {
                        switch (select) {
                            case 0 -> openCapsuleTopMenu(player);
                            case 1 -> openThiepTopMenu(player);
                            case 2 -> openHopQuaTopMenu(player);
                            case 3 -> ShopService.gI().opendShop(player, "20-10", false);
                            case 4 -> EventNpcUtils.showItemsWithHSD(player, this);
                        }
                        return;
                    }
                    if (player.idMark.getIndexMenu() == EventNpcUtils.MENU_HUY_TRANG_BI_HSD) {
                        if (select == 0) {
                            EventNpcUtils.removeItemsWithHSD(player);
                        }
                        return;
                    }
                    if (player.idMark.getIndexMenu() == ConstNpc.ORTHER_MENU1) {
                        Object key = NpcFactory.PLAYERID_OBJECT.get(player.id);
                        if (key instanceof String s) {
                            handleTopMenuSelection(player, select, s);
                        }
                    }
                }

                private void openCapsuleTopMenu(Player player) {
                    String[] menu = EventUtils.buildTopMenuOptions(TopWomenService.gI().isClaimOpen());
                    String message = EventUtils.formatTopMenu("Women's Day", "Capsule Trang Sức VIP",
                            TopWomenService.EVENT_END, TopWomenService.CLAIM_DEADLINE);
                    this.createOtherMenu(player, ConstNpc.ORTHER_MENU1, message, menu, "CAPSULE");
                }

                private void openThiepTopMenu(Player player) {
                    String[] menu = EventUtils.buildTopMenuOptions(TopWomenService.gI().isClaimOpen());
                    String message = EventUtils.formatTopMenu("Women's Day", "Thiệp chúc VIP",
                            TopWomenService.EVENT_END, TopWomenService.CLAIM_DEADLINE);
                    this.createOtherMenu(player, ConstNpc.ORTHER_MENU1, message, menu, "THIEP");
                }

                private void openHopQuaTopMenu(Player player) {
                    String[] menu = EventUtils.buildTopMenuOptions(TopWomenService.gI().isClaimOpen());
                    String message = EventUtils.formatTopMenu("Women's Day", "Hộp quà 20/10",
                            TopWomenService.EVENT_END, TopWomenService.CLAIM_DEADLINE);
                    this.createOtherMenu(player, ConstNpc.ORTHER_MENU1, message, menu, "HOPQUA");
                }

                private void handleTopMenuSelection(Player player, int select, String topType) {
                    switch (topType) {
                        case "CAPSULE" -> handleTopSelection(player, select, 1, player.eventPointType3,
                                "ITEMS_TOP_REWARD_CAPSULE_VIP", (byte) 21);
                        case "THIEP" -> handleTopSelection(player, select, 2, player.eventPointType4,
                                "ITEMS_TOP_REWARD_THIEP_VIP", (byte) 22);
                        case "HOPQUA" -> handleTopSelection(player, select, 3, player.eventPointType5,
                                "ITEMS_TOP_REWARD_HOPQUA_2010", (byte) 23);
                    }
                }

                private void handleTopSelection(Player player, int select, int topType, int points,
                        String rewardTag, byte board) {
                    TopWomenService service = TopWomenService.gI();
                    switch (select) {
                        case 0 -> service.showTop(player, topType);
                        case 1 -> service.showSelfRank(player, topType, points);
                        case 2 -> {
                            if (service.isClaimOpen()) {
                                int rank = switch (topType) {
                                    case 1 -> service.getRankCapsule(player);
                                    case 2 -> service.getRankThiep(player);
                                    case 3 -> service.getRankHopQua(player);
                                    default -> 0;
                                };
                                service.openPreview(player, rewardTag, board, rank);
                            }
                        }
                    }
                }
            };
            map.addNpc(npc);
        }
    }
}
