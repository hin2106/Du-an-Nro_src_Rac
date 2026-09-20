package services.tournament;

import consts.ConstSuperRank;
import daos.NDVSqlFetcher;
import daos.SuperRankDAO;
import java.util.List;
import managers.SuperRankManager;
import map.Map;
import map.Zone;
import network.Message;
import player.Player;
import server.Client;
import services.map.MapService;
import services.Service;
import tournament.SuperRank;
import tournament.SuperRankBuilder;
import utils.Logger;
import utils.TimeUtil;

public class SuperRankService {

    private static SuperRankService instance;

    public static SuperRankService gI() {
        if (instance == null) {
            instance = new SuperRankService();
        }
        return instance;
    }

    public void competing(Player player, long id) {
        if (player.zone == null || player.zone.map == null || player.zone.map.mapId != 113 || id == -1) {
            return;
        }
        int menuType = player.idMark.getMenuType();
        Player pl = loadPlayer(id);
        if (pl == null) {
            return;
        }
        if (SuperRankManager.gI().currentlyCompeting(player.id)) {
            Service.gI().sendThongBao(player, ConstSuperRank.TEXT_DANG_THI_DAU);
            return;
        } else if (SuperRankManager.gI().currentlyCompeting(pl.id)) {
            Service.gI().sendThongBao(player, ConstSuperRank.TEXT_DOI_THU_DANG_THI_DAU);
            return;
        } else if (SuperRankManager.gI().awaitingCompetition(player.id)) {
            Service.gI().sendThongBao(player, ConstSuperRank.TEXT_DANG_CHO);
            return;
        } else if (SuperRankManager.gI().awaitingCompetition(pl.id)) {
            Service.gI().sendThongBao(player, ConstSuperRank.TEXT_DOI_THU_CHO_THI_DAU);
            return;
        } else if (player.superRank.rank < pl.superRank.rank) {
            Service.gI().sendThongBao(player, ConstSuperRank.TEXT_DUOI_HANG);
            return;
        } else if (player.superRank.rank == pl.superRank.rank) {
            Service.gI().sendThongBao(player, ConstSuperRank.TEXT_CHINH_MINH);
            return;
        } else if (pl.superRank.rank < 10 && player.superRank.rank - pl.superRank.rank > 2) {
            Service.gI().sendThongBao(player, ConstSuperRank.TEXT_KHONG_THE_THI_DAU_TREN_2_HANG);
            return;
        } else if (player.superRank.ticket <= 0 && player.inventory.getGemAndRuby() < 1) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu 1 ngọc nữa");
            return;
        }
        switch (menuType) {
            case 0 -> {
                Service.gI().sendThongBao(player, ConstSuperRank.TEXT_TOP_100);
            }
            case 1 -> {
                if (SuperRankManager.gI().SPRCheck(player.zone)) {
                    Service.gI().sendThongBao(player, ConstSuperRank.TEXT_CHO_IT_PHUT);
                    SuperRankManager.gI().addWSPR(player.id, pl.id);
                } else {
                    SuperRankManager.gI().addSPR(new SuperRank(player, id, player.zone));
                }
                dragonpass.DragonPassService.gI().onSuperRankJoined(player);
            }
            case 2 -> {
                SuperRankManager.gI().addSPR(new SuperRank(player, id, getZone(113)));
                dragonpass.DragonPassService.gI().onSuperRankJoined(player);
            }
        }
    }

    public void topList(Player player, int type) {
        if (player == null) {
            return;
        }
        long st = System.currentTimeMillis();
        player.idMark.setMenuType(type);
        Message msg = null;
        try {
            List<SuperRankBuilder> list = (type == 0)
                    ? SuperRankDAO.getPlayerListInRank(player.superRank.rank, 100)
                    : SuperRankDAO.getPlayerListInRankRange(player.superRank.rank, 11);

            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Top 100 Cao Thủ");
            msg.writer().writeByte(list.size());
            for (SuperRankBuilder sb : list) {
                msg.writer().writeInt(sb.getRank());
                msg.writer().writeInt(sb.getId());
                msg.writer().writeShort(sb.getHead());
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(sb.getBody());
                msg.writer().writeShort(sb.getLeg());
                msg.writer().writeUTF(sb.getName());
                msg.writer().writeUTF(textStatus(sb));
                msg.writer().writeUTF(sb.getInfo());
            }
            player.sendMessage(msg);
            for (SuperRankBuilder sb : list) {
                sb.dispose();
            }
        } catch (Exception e) {
            Logger.error("Error in topList: ");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
            Logger.primaryln("Processing time: " + (System.currentTimeMillis() - st) + " ms");
        }
    }

    public Player loadPlayer(long id) {
        try {
            Player pl = NDVSqlFetcher.loadById(id);
            if (pl != null) {
                pl.setClothes.setup();
                if (pl.pet != null) {
                    pl.pet.setClothes.setup();
                }
                pl.nPoint.calPoint();
            }
            return pl;
        } catch (Exception e) {
            Logger.error("Error loading player: ");
            return null;
        }
    }

    public Player getPlayer(long id) {
        return Client.gI().getPlayer(id);
    }

    public String textInfo(Player pl) {
        if (pl == null || pl.nPoint == null) {
            return "Không xác định!";
        }
        pl.setClothes.setup();
        if (pl.pet != null) {
            pl.pet.setClothes.setup();
        }
        pl.nPoint.calPoint();
        return String.format("HP %.2f\nSức đánh %.2f\nGiáp %.2f\n%d:%d",
                pl.nPoint.hpMax,
                pl.nPoint.dame,
                pl.nPoint.def,
                pl.superRank.win,
                pl.superRank.lose);
    }

    public String textInfoNew(Player pl) {
        if (pl == null || pl.nPoint == null) {
            return "Không xác định!";
        }
        pl.setClothes.setup();
        if (pl.pet != null) {
            pl.pet.setClothes.setup();
        }
        pl.nPoint.calPoint();
        StringBuilder text = new StringBuilder();
        text.append(String.format("HP: %.2f\n", pl.nPoint.hpMax));
        text.append(String.format("Sức đánh: %.2f\n", pl.nPoint.dame));
        text.append(String.format("Giáp: %.2f\n", pl.nPoint.def));
        text.append("Thắng/Thua: ").append(pl.superRank.win).append("/").append(pl.superRank.lose);

        for (int i = 0; i < pl.superRank.history.size(); i++) {
            String history = pl.superRank.history.get(i);
            long lastTime = pl.superRank.lastTime.get(i);
            text.append("\n").append(history).append(" ").append(TimeUtil.getTimeLeft(lastTime));
        }
        return text.toString();
    }

    public String textStatus(SuperRankBuilder srb) {
        if (SuperRankManager.gI().awaitingCompetition(srb.getId())) {
            return ConstSuperRank.TEXT_DANG_CHO;
        } else if (SuperRankManager.gI().currentlyCompeting(srb.getId())) {
            return SuperRankManager.gI().getCompeting(srb.getId());
        }
        return textReward(srb.getRank());
    }

    public String textReward(int rank) {
        return switch (rank) {
            case 1 ->
                "+100 ngọc/ ngày";
            case 2, 3, 4, 5, 6, 7, 8, 9, 10 ->
                "+20 ngọc/ ngày";
            default ->
                rank <= 100 ? "+5 ngọc/ ngày" : rank <= 1000 ? "+1 ngọc/ ngày" : "";
        };
    }

    public int reward(int rank) {
        return switch (rank) {
            case 1 ->
                100;
            case 2, 3, 4, 5, 6, 7, 8, 9, 10 ->
                20;
            default ->
                rank <= 100 ? 5 : rank <= 1000 ? 1 : 0;
        };
    }

    public Zone getZone(int mapId) {
        try {
            Map map = MapService.gI().getMapById(mapId);
            if (map != null) {
                for (Zone zone : map.zones) {
                    if (!SuperRankManager.gI().SPRCheck(zone)) {
                        return zone;
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Error getting zone: ");
        }
        return null;
    }
}
