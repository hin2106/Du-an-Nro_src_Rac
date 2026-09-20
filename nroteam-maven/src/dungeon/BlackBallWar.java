package dungeon;

import consts.ConstTaskBadges;
import daos.NDVSqlFetcher;
import daos.PlayerDAO;
import lombok.Data;
import map.Zone;
import player.Player;

import server.Client;
import server.Maintenance;
import services.Service;
import services.map.ChangeMapService;
import services.map.MapService;
import task.BadgesTaskService;
import utils.Functions;
import utils.TimeUtil;
import utils.Util;

@Data
public class BlackBallWar implements Runnable {

    public static final int TIME_CAN_PICK_BLACK_BALL_AFTER_DROP = 5000;

    public static final byte X3 = 3;
    public static final byte X5 = 5;
    public static final byte X7 = 7;

    public static final int COST_X3 = 100_000_000;
    public static final int COST_X5 = 300_000_000;
    public static final int COST_X7 = 500_000_000;

    public static final byte HOUR_OPEN = 20;
    public static final byte MIN_OPEN = 0;
    public static final byte SECOND_OPEN = 0;

    public static final byte HOUR_CAN_PICK_DB = 20;
    public static final byte MIN_CAN_PICK_DB = 30;
    public static final byte SECOND_CAN_PICK_DB = 0;

    public static final byte HOUR_CLOSE = 21;
    public static final byte MIN_CLOSE = 0;
    public static final byte SECOND_CLOSE = 0;

    public static final int AVAILABLE = 1;
    private static final int TIME_WIN = 300_000;

    private Zone zone;
    private volatile boolean running = true;

    public BlackBallWar(Zone zone) {
        this.zone = zone;
        start();
    }

    private void start() {
        if (zone == null) {
            return;
        }
        Thread t = new Thread(this, "Update Black Ball War Map " + zone.map.mapName + " Zone " + zone.zoneId);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        while (running && !Maintenance.isRunning()) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                long duration = System.currentTimeMillis() - startTime;
                Functions.sleep(Math.max(1000 - duration, 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (zone == null) {
            return;
        }

        try {
            if (!TimeUtil.isBlackBallWarOpen()) {
                zone.finishBlackBallWar = false;
            }

            for (int i = zone.getNumOfPlayers() - 1; i >= 0; i--) {
                Player p = null;
                try {
                    p = zone.getPlayers().get(i);
                    if (p != null) {
                        updatePlayer(p);
                    }
                } catch (Exception e) {
                    if (p != null) {
                        Service.gI().sendThongBao(p, "Có lỗi trong cập nhật người chơi Ngọc Rồng!");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePlayer(Player player) {
        if (player == null || player.zone == null) {
            return;
        }

        try {
            if (!MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
                return;
            }

            if (!TimeUtil.isBlackBallWarOpen()) {
                kickOutOfMap(player);
                return;
            }

            if (player.idMark != null && player.idMark.isHoldBlackBall()) {

                if (Util.canDoWithTime(player.idMark.getLastTimeHoldBlackBall(), TIME_WIN)) {
                    win(player);
                    return;
                }

                if (Util.canDoWithTime(player.idMark.getLastTimeNotifyTimeHoldBlackBall(), 10_000)) {
                    long secondLeft = TimeUtil.getSecondLeft(player.idMark.getLastTimeHoldBlackBall(), TIME_WIN / 1000);
                    Service.gI().sendThongBao(player, "Cố giữ ngọc thêm " + secondLeft + " giây nữa sẽ chiến thắng!");
                    player.idMark.setLastTimeNotifyTimeHoldBlackBall(System.currentTimeMillis());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void win(Player player) {
        if (player == null || player.zone == null || player.idMark == null) {
            return;
        }
        try {
            player.zone.finishBlackBallWar = true;
            int star = Math.max(1, player.idMark.getTempIdBlackBallHold() - 371);
            player.rewardBlackBall.reward((byte) star);
            Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã giành được Ngọc Rồng " + star + " Sao Đen cho bang hội!");
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.CHIEN_BINH_SAO_DEN, 1);
            if (player.clan != null && player.clan.members != null) {
                player.clan.members.forEach(m -> {
                    try {
                        Player p = Client.gI().getPlayer(m.id);
                        if (p != null) {
                            p.rewardBlackBall.reward((byte) star);
                        } else {
                            p = NDVSqlFetcher.loadById(m.id);
                            if (p != null) {
                                p.rewardBlackBall.reward((byte) star);
                                PlayerDAO.updatePlayer(p);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("[BlackBallWar] Lỗi thưởng cho thành viên bang: " + ex.getMessage());
                    }
                });
            }

            // Kick toàn bộ người chơi ra khỏi map sau khi thắng
            kickAllPlayersOutOfMap(player.zone);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void kickOutOfMap(Player player) {
        if (player == null) {
            return;
        }

        try {
            // Đổi flag cầm ngọc về flag thường
            if (player.cFlag == 8) {
                Service.gI().changeFlag(player, Util.nextInt(1, 7));
            }

            Service.gI().sendThongBao(player, "Trò chơi tìm Ngọc Rồng Đen hôm nay đã kết thúc, hẹn gặp lại vào 20h ngày mai!");
            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 24, -1, 250);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void kickAllPlayersOutOfMap(Zone zone) {
        if (zone == null) {
            return;
        }

        try {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfMap(pl);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}
