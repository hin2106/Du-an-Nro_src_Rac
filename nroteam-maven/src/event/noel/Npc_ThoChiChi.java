package event.noel;

import consts.ConstMap;
import consts.ConstNpc;
import event.Event;
import event.EventNpcUtils;
import map.Map;
import map.Zone;
import npc.Npc;
import npc.NpcFactory;
import player.Player;
import server.Manager;
import services.ShopService;
import services.map.MapService;
import services.top.TopHalloweenService;
import utils.Util;

public class Npc_ThoChiChi extends Event {

    public Zone zone;

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_NOEL;
    }

    @Override
    public void init() {
        initNpc();
    }

    @Override
    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.DAO_KAME);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_NOEL) {
            Npc npc;
            npc = new Npc(map.mapId, 1, 245, 288, 80, 9966) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        this.createOtherMenu(
                                player,
                                ConstNpc.BASE_MENU,
                                "Bạn muốn hỏi Chi Chi?",
                                "Top\n Dùng hộp\n Diêm",
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
                            case 0 -> {
                                String[] menu = TopHalloweenService.gI().isClaimOpen()
                                        ? new String[]{"Top 100\n Hộp kẹo\n Ma quỷ", "Xem điểm", "Nhận thưởng", "Đóng"}
                                        : new String[]{"Top 100\n Hộp kẹo\n Ma quỷ", "Xem điểm", "Đóng"};
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU1,
                                        "Sự kiện đua Top Hộp quà ma quỷ nhận quà khủng\n"
                                        + "Kết thúc và trao giải sau: (" + Util.remain(TopHalloweenService.EVENT_END) + ")\n"
                                        + "Hạn chót nhận giải: (" + Util.remain(TopHalloweenService.CLAIM_DEADLINE) + ")\n"
                                        + "Đến gặp Chi Chi để nhận giải nhé\n" + "Chi tiết xem tại diễn đàn, fanpage.",
                                        menu, "CANDY");
                            }
                            case 1 -> {
                                ShopService.gI().opendShop(player, "HALLOWEEN", true);
                            }
                            case 2 -> {
                                EventNpcUtils.showItemsWithHSD(player, this);
                            }
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
                            switch (s) {
                                case "CANDY" -> {
                                    if (!TopHalloweenService.gI().isClaimOpen()) {
                                        switch (select) {
                                            case 0 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 2 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                        }
                                    }
                                }
                                case "CARD" -> {
                                    if (!TopHalloweenService.gI().isClaimOpen()) {
                                        switch (select) {
                                            case 0 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 2 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                        }
                                    }
                                }
                                case "TUI_MU" -> {
                                    if (!TopHalloweenService.gI().isClaimOpen()) {
                                        switch (select) {
                                            case 0 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopHalloweenService.gI().showTop(player, 3);
                                            case 1 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                            case 2 ->
                                                TopHalloweenService.gI().showTop(player, 2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };
//            markEventNpc(npc);
            map.addNpc(npc);
        }
    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(player.Player player, mob.Mob mob, java.util.List<map.ItemMap> list, int x, int yEnd) {
    }

    @Override
    public boolean useItem(Player player, item.Item item) {
        return false;
    }
}
