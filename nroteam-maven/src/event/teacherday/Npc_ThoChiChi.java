package event.teacherday;

import java.util.ArrayList;
import java.util.List;

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
import services.map.MapService;
import services.top.TopTeaCherdayService;
import utils.Util;
import services.*;

public class Npc_ThoChiChi extends Event {

    public Zone zone;

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_20_11;
    }

    @Override
    public void init() {
        initNpc();
    }

    @Override
    public void initNpc() {
        Map map = MapService.gI().getMapById(ConstMap.DAO_KAME);
        if (map != null && Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_20_11) {
            Npc npc;
            npc = new Npc(map.mapId, 1, 245, 288, 80, 9966) {
                @Override
                public void openBaseMenu(Player player) {
                    if (canOpenNpc(player)) {
                        this.createOtherMenu(
                                player,
                                ConstNpc.BASE_MENU,
                                "Bạn muốn hỏi chi?",
                                "Top\n Hộp quà\n20-11 VIP",
                                "Top\n Hộp trà\nhoa cúc",
                                "Top\n Hộp quà\n20-11",
                                "Cửa hàng",
                                "Hủy bỏ\n trang bị\n có hsd",
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
                                String[] menu = TopTeaCherdayService.gI().isClaimOpen()
                                        ? new String[] { "Top 100\nHộp quà\n20-11 VIP", "Xem điểm", "Nhận thưởng",
                                                "Đóng" }
                                        : new String[] { "Top 100\nHộp quà\n20-11 VIP", "Xem điểm", "Đóng" };
                                player.idMark.setEventMenuId(ConstNpc.EVENT_2011_HOPQUA_VIP);
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU1,
                                        "Sự kiện đua Top Hộp quà 20-11 VIP\n"
                                                + "Kết thúc: (" + Util.remain(TopTeaCherdayService.EVENT_END) + ")\n"
                                                + "Hạn nhận giải: (" + Util.remain(TopTeaCherdayService.CLAIM_DEADLINE)
                                                + ")",
                                        menu);
                            }
                            case 1 -> {
                                String[] menu = TopTeaCherdayService.gI().isClaimOpen()
                                        ? new String[] { "Top 100\nHộp trà\nhoa cúc", "Xem điểm", "Nhận thưởng",
                                                "Đóng" }
                                        : new String[] { "Top 100\nHộp trà\nhoa cúc", "Xem điểm", "Đóng" };
                                player.idMark.setEventMenuId(ConstNpc.EVENT_2011_HOPTRA);
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU1,
                                        "Sự kiện đua Top Hộp trà hoa cúc\n"
                                                + "Kết thúc: (" + Util.remain(TopTeaCherdayService.EVENT_END) + ")\n"
                                                + "Hạn nhận giải: (" + Util.remain(TopTeaCherdayService.CLAIM_DEADLINE)
                                                + ")",
                                        menu);
                            }
                            case 2 -> {
                                String[] menu = TopTeaCherdayService.gI().isClaimOpen()
                                        ? new String[] { "Top 100\nHộp quà\n20-11", "Xem điểm", "Nhận thưởng", "Đóng" }
                                        : new String[] { "Top 100\nHộp quà\n20-11", "Xem điểm", "Đóng" };
                                player.idMark.setEventMenuId(ConstNpc.EVENT_2011_HOPQUA);
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU1,
                                        "Sự kiện đua Top Hộp quà 20-11\n"
                                                + "Kết thúc: (" + Util.remain(TopTeaCherdayService.EVENT_END) + ")\n"
                                                + "Hạn nhận giải: (" + Util.remain(TopTeaCherdayService.CLAIM_DEADLINE)
                                                + ")",
                                        menu);
                            }
                            case 3 ->
                                ShopService.gI().opendShop(player, "20-11", false);
                            case 4 -> {
                                List<String> previewLines = new ArrayList<>();
                                if (ExpiryItemService.gI().collectExpiryItems(player, 30, previewLines) == 0) {
                                    Service.gI().sendThongBao(player, "Tìm không thấy");
                                    return;
                                }
                                StringBuilder preview = new StringBuilder();
                                preview.append("Bạn có chắc muốn hủy ")
                                        .append(ExpiryItemService.gI().collectExpiryItems(player, 30, previewLines))
                                        .append(" vật phẩm:");
                                for (String line : previewLines) {
                                    preview.append("\n").append(line);
                                }
                                this.createOtherMenu(player, ConstNpc.ORTHER_MENU2, preview.toString(),
                                        new String[] { "Xác nhận", "Đóng" });
                            }
                        }
                        return;
                    }
                    if (player.idMark.getIndexMenu() == ConstNpc.ORTHER_MENU1) {
                        int eventMenuId = player.idMark.getEventMenuId();
                        switch (eventMenuId) {
                                case ConstNpc.EVENT_2011_HOPQUA_VIP -> {
                                    if (!TopTeaCherdayService.gI().isClaimOpen()) {
                                        if (select == 0) {
                                            TopTeaCherdayService.gI().showTop(player, 1);
                                        } else if (select == 1) {
                                            TopTeaCherdayService.gI().showSelfRank(player, 1, player.eventPointType9);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopTeaCherdayService.gI().showTop(player, 1);
                                            case 1 ->
                                                TopTeaCherdayService.gI().showSelfRank(player, 1, player.eventPointType9);
                                            case 2 ->
                                                TopTeaCherdayService.gI().openPreview(player, "ITEMS_TOP_REWARD_20_11_VIP",
                                                        (byte) 41, TopTeaCherdayService.gI().getRankHopQuaVip(player));
                                            default -> {
                                            }
                                        }
                                    }
                                }
                                case ConstNpc.EVENT_2011_HOPTRA -> {
                                    if (!TopTeaCherdayService.gI().isClaimOpen()) {
                                        if (select == 0) {
                                            TopTeaCherdayService.gI().showTop(player, 2);
                                        } else if (select == 1) {
                                            TopTeaCherdayService.gI().showSelfRank(player, 2, player.eventPointType10);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopTeaCherdayService.gI().showTop(player, 2);
                                            case 1 ->
                                                TopTeaCherdayService.gI().showSelfRank(player, 2, player.eventPointType10);
                                            case 2 ->
                                                TopTeaCherdayService.gI().openPreview(player, "ITEMS_TOP_REWARD_20_11_HOPTRA",
                                                        (byte) 42, TopTeaCherdayService.gI().getRankHopTraHoaCuc(player));
                                            default -> {
                                            }
                                        }
                                    }
                                }
                                case ConstNpc.EVENT_2011_HOPQUA -> {
                                    if (!TopTeaCherdayService.gI().isClaimOpen()) {
                                        if (select == 0) {
                                            TopTeaCherdayService.gI().showTop(player, 3);
                                        } else if (select == 1) {
                                            TopTeaCherdayService.gI().showSelfRank(player, 3, player.eventPointType11);
                                        }
                                    } else {
                                        switch (select) {
                                            case 0 ->
                                                TopTeaCherdayService.gI().showTop(player, 3);
                                            case 1 ->
                                                TopTeaCherdayService.gI().showSelfRank(player, 3, player.eventPointType11);
                                            case 2 ->
                                                TopTeaCherdayService.gI().openPreview(player, "ITEMS_TOP_REWARD_20_11",
                                                        (byte) 43, TopTeaCherdayService.gI().getRankHopQua2011(player));
                                            default -> {
                                            }
                                        }
                                    }
                                }
                            }
                        return;
                    }
                    if (player.idMark.getIndexMenu() == ConstNpc.ORTHER_MENU2) {
                        if (select == 0) {
                            if (ExpiryItemService.gI().removeExpiryOptions(player, 30) > 0) {
                                Service.gI().sendThongBao(player, "Hủy thành công");
                            }
                        }
                    }
                }
            };
            // markEventNpc(npc);
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
