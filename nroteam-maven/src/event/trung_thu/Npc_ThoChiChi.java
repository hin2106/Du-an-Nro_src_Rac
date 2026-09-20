package event.trung_thu;
import consts.ConstMap;
import consts.ConstNpc;
import event.Event;
import event.EventNpcUtils;
import item.Item;
import java.util.List;
import map.ItemMap;
import map.Map;
import map.Zone;
import mob.Mob;
import npc.Npc;
import npc.NpcFactory;
import player.Player;
import server.Manager;
import services.map.MapService;
import services.top.TopTrungThuService;

public class Npc_ThoChiChi extends Event {

    public Zone zone;

    @Override
    public void init() {
        initNpc();
    }

    @Override
    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.DAO_KAME);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TRUNG_THU) {
            Npc npc;
            npc = new Npc(map.mapId, 1, 245, 288, 80, 9966) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        this.createOtherMenu(
                                player,
                                ConstNpc.BASE_MENU,
                                "Bạn muốn hỏi chi ?",
                                "Top\nHộp quà\nTrung Thu\nVIP",
                                "Top\nLồng đèn\ntreo",
                                "Hủy bỏ\ntrang bị \ncó HSD",
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
                            case 2 -> EventNpcUtils.showItemsWithHSD(player, this);
                            case 0 -> {
                                String[] menu = TopTrungThuService.gI().isClaimOpen()
                                        ? new String[] { "Top 100\n Hộp quà\n Trung Thu\n VIP", "Xem điểm",
                                                "Nhận thưởng", "Đóng" }
                                        : new String[] { "Top 100\n Hộp quà\n Trung Thu\n VIP", "Xem điểm", "Đóng" };
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU1,
                                        "Sự kiện đua Top Lồng đèn treo nhận quà khủng\n"
                                                + "Kết thúc và trao giải sau: (" + remain(TopTrungThuService.EVENT_END)
                                                + ")\n"
                                                + "Hạn chót nhận giải: (" + remain(TopTrungThuService.CLAIM_DEADLINE)
                                                + ")\n"
                                                + "Đến gặp Chi Chi để nhận giải nhé\n"
                                                + "Chi tiết xem tại diễn đàn, fanpage.",
                                        menu, "VIP");
                            }
                            case 1 -> {
                                String[] menu = TopTrungThuService.gI().isClaimOpen()
                                        ? new String[] { "Top 100\n Lồng đèn\n treo", "Xem điểm", "Nhận thưởng",
                                                "Đóng" }
                                        : new String[] { "Top 100\n Lồng đèn\n treo", "Xem điểm", "Đóng" };
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU1,
                                        "Sự kiện đua Top Hộp quà Trung Thu VIP nhận quà khủng\n"
                                                + "Kết thúc và trao giải sau: (" + remain(TopTrungThuService.EVENT_END)
                                                + ")\n"
                                                + "Hạn chót nhận giải: (" + remain(TopTrungThuService.CLAIM_DEADLINE)
                                                + ")\n"
                                                + "Đến gặp Chi Chi để nhận giải nhé\n"
                                                + "Chi tiết xem tại diễn đàn, fanpage.",
                                        menu, "LONGDEN");
                            }
                        }
                        return;
                    }
                    if (player.idMark.getIndexMenu() == ConstNpc.ORTHER_MENU1) {
                        Object key = NpcFactory.PLAYERID_OBJECT.get(player.id);
                        if (key instanceof String s) {
                            switch (s) {
                                case "VIP" -> {
                                    if (!TopTrungThuService.gI().isClaimOpen()) {
                                        switch (select) {
                                            case 0 ->
                                                TopTrungThuService.gI().showTop(player, 1);
                                            case 1 ->
                                                TopTrungThuService.gI().showSelfRank(player, 1, player.eventPointType1);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopTrungThuService.gI().showTop(player, 1);
                                            case 1 ->
                                                TopTrungThuService.gI().showSelfRank(player, 1, player.eventPointType1);
                                            case 2 ->
                                                TopTrungThuService.gI().openPreviewVip(player);
                                        }
                                    }
                                }
                                case "LONGDEN" -> {
                                    if (!TopTrungThuService.gI().isClaimOpen()) {
                                        switch (select) {
                                            case 0 ->
                                                TopTrungThuService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopTrungThuService.gI().showSelfRank(player, 2, player.eventPointType2);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopTrungThuService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopTrungThuService.gI().showSelfRank(player, 2, player.eventPointType2);
                                            case 2 ->
                                                TopTrungThuService.gI().openPreviewLongDen(player);
                                        }
                                    }
                                }

                            }
                        }
                    }
                    if (player.idMark.getIndexMenu() == EventNpcUtils.MENU_HUY_TRANG_BI_HSD) {
                        if (select == 0) {
                            EventNpcUtils.removeItemsWithHSD(player);
                        }
                    }
                }
            };
            map.addNpc(npc);
        }
    }

    private String remain(long end) {
        long ms = end - System.currentTimeMillis();
        if (ms <= 0) {
            return "đã kết thúc";
        }
        long days = ms / 86_400_000L;
        long hours = (ms % 86_400_000L) / 3_600_000L;
        if (days > 0) {
            return days + " ngày nữa";
        }
        if (hours > 0) {
            return hours + " giờ nữa";
        }
        long minutes = (ms % 3_600_000L) / 60_000L;
        return Math.max(1, minutes) + " phút nữa";
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

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_TRUNG_THU;
    }
}
