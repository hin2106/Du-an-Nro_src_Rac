package services.dungeon;


import dungeon.BlackBallWar;
import static dungeon.BlackBallWar.COST_X3;
import static dungeon.BlackBallWar.COST_X5;
import static dungeon.BlackBallWar.COST_X7;
import static dungeon.BlackBallWar.X3;
import static dungeon.BlackBallWar.X5;
import static dungeon.BlackBallWar.X7;
import item.Item;
import java.util.ArrayList;
import java.util.List;
import map.ItemMap;
import map.Zone;

import player.Player;
import services.player.PlayerService;
import services.Service;
import services.map.ChangeMapService;
import utils.TimeUtil;
import utils.Util;

public class BlackBallWarService {

    private static BlackBallWarService instance;
    private final List<BlackBallWar> blackBallWars;

    public static BlackBallWarService gI() {
        if (instance == null) {
            instance = new BlackBallWarService();
        }
        return instance;
    }

    private BlackBallWarService() {
        this.blackBallWars = new ArrayList<>();
    }

    public void addMapBlackBallWar(int id, Zone zone) {
        if (zone == null) return;
        this.blackBallWars.add(new BlackBallWar(zone));
    }

    // ====================== THẢ NGỌC RỒNG ======================
    public void dropBlackBall(Player player) {
        if (player == null || player.zone == null || player.idMark == null) return;

        try {
            if (!player.idMark.isHoldBlackBall()) return;

            player.idMark.setHoldBlackBall(false);
            int itemId = player.idMark.getTempIdBlackBallHold();
            player.idMark.setTempIdBlackBallHold(-1);

            ItemMap itemMap = new ItemMap(
                    player.zone,
                    itemId,
                    1,
                    player.location.x,
                    player.zone.map.yPhysicInTop(player.location.x, player.location.y - 24),
                    -1
            );

            Service.gI().dropItemMap(player.zone, itemMap);
            player.zone.lastTimeDropBlackBall = System.currentTimeMillis();
            Service.gI().sendFlagBag(player);

            resetZoneFlagsAfterDrop(player);

        } catch (Exception e) {
            Service.gI().sendThongBao(player, "Có lỗi khi thả ngọc rồng!");
            e.printStackTrace();
        }
    }

    private void resetZoneFlagsAfterDrop(Player player) {
        try {
            if (player == null || player.zone == null) return;
            List<Player> players = player.zone.getPlayers();
            for (Player pl : players) {
                if (pl == null) continue;
                if (pl.clan != null && player.clan != null && pl.clan.equals(player.clan)) {
                    Service.gI().changeFlag(pl, Util.nextInt(1, 7));
                } else {
                    Service.gI().changeFlag(pl, Util.nextInt(1, 7));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== VÀO MAP ======================
    public void joinMapBlackBallWar(Player player) {
        if (player == null || player.zone == null) return;

        try {
            boolean flagSet = false;

            if (player.clan != null) {
                for (Player pl : player.zone.getPlayers()) {
                    if (pl == null) continue;
                    if (!player.equals(pl) && pl.clan != null && player.clan.equals(pl.clan) && !player.isBoss) {
                        Service.gI().changeFlag(player, pl.cFlag);
                        flagSet = true;
                        break;
                    }
                }
            }

            if (!flagSet && !player.isBoss) {
                Service.gI().changeFlag(player, Util.nextInt(1, 7));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== NHẶT NGỌC RỒNG ======================
    public boolean pickBlackBall(Player player, Item item) {
        if (player == null || player.zone == null || player.idMark == null || item == null) return false;

        try {
            if (!TimeUtil.isBlackBallWarCanPick()) {
                Service.gI().sendThongBao(player, "Chưa thể nhặt lúc này, hãy đợi "
                        + TimeUtil.getSecondsUntilCanPick() + " giây nữa!");
                return false;
            }

            if (player.zone.finishBlackBallWar) {
                Service.gI().sendThongBao(player, "Sự kiện đã kết thúc, quay lại vào 20h ngày mai!");
                return false;
            }

            if (!Util.canDoWithTime(player.zone.lastTimeDropBlackBall, BlackBallWar.TIME_CAN_PICK_BLACK_BALL_AFTER_DROP)) {
                String wait = TimeUtil.getTimeLeft(player.zone.lastTimeDropBlackBall,
                        BlackBallWar.TIME_CAN_PICK_BLACK_BALL_AFTER_DROP / 1000);
                Service.gI().sendThongBao(player, "Chưa thể nhặt lúc này, hãy đợi " + wait + " nữa!");
                return false;
            }

            player.idMark.setHoldBlackBall(true);
            player.idMark.setTempIdBlackBallHold(item.template.id);
            player.idMark.setLastTimeHoldBlackBall(System.currentTimeMillis());
            Service.gI().sendFlagBag(player);

            setClanFlagWhenPick(player);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi khi nhặt ngọc rồng!");
            return false;
        }
    }

    private void setClanFlagWhenPick(Player player) {
        try {
            if (player == null || player.zone == null) return;
            List<Player> players = player.zone.getPlayers();
            for (Player pl : players) {
                if (pl == null) continue;
                if (player.clan != null && pl.clan != null && player.clan.equals(pl.clan)) {
                    Service.gI().changeFlag(pl, 8); // flag cầm ngọc
                } else if (pl.equals(player)) {
                    Service.gI().changeFlag(pl, 8);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================== PHÙ HỘ HP ======================
    public void xHPKI(Player player, byte x) {
        if (player == null || player.effectSkin == null || player.inventory == null || player.nPoint == null) {
            return;
        }

        int cost = switch (x) {
            case X3 -> COST_X3;
            case X5 -> COST_X5;
            case X7 -> COST_X7;
            default -> 0;
        };

        if (cost <= 0) {
            Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ!");
            return;
        }

        if (player.inventory.gold < cost) {
            Service.gI().sendThongBao(player, "Không đủ vàng! Thiếu "
                    + Util.numberToMoney(cost - player.inventory.gold) + " vàng");
            return;
        }

        // Không chồng buff
        if (player.effectSkin.xHPKI > 1 || player.effectSkin.xDame > 1) {
            Service.gI().sendThongBao(player, "Bạn đã được phù hộ rồi!");
            return;
        }

        try {
            player.inventory.gold -= cost;
            Service.gI().sendMoney(player);

            player.effectSkin.xHPKI = x;
            player.effectSkin.lastTimeXHPKI = System.currentTimeMillis();

            player.nPoint.calPoint();
            player.nPoint.setHp(player.nPoint.hp * x);
            player.nPoint.setMp(player.nPoint.mp * x);

            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().point(player);

            Service.gI().sendThongBao(player, "Phù hộ thành công! HP/MP đã tăng x" + x);
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi khi phù hộ HP!");
        }
    }

    // ====================== PHÙ HỘ SỨC ĐÁNH ======================
    public void xDame(Player player, byte x) {
        if (player == null || player.effectSkin == null || player.inventory == null || player.nPoint == null) {
            return;
        }

        int cost = switch (x) {
            case X3 -> COST_X3;
            case X5 -> COST_X5;
            case X7 -> COST_X7;
            default -> 0;
        };

        if (cost <= 0) {
            Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ!");
            return;
        }

        if (player.inventory.gold < cost) {
            Service.gI().sendThongBao(player, "Không đủ vàng! Thiếu "
                    + Util.numberToMoney(cost - player.inventory.gold) + " vàng");
            return;
        }

        // Không chồng buff
        if (player.effectSkin.xHPKI > 1 || player.effectSkin.xDame > 1) {
            Service.gI().sendThongBao(player, "Bạn đã được phù hộ rồi!");
            return;
        }

        try {
            player.inventory.gold -= cost;
            Service.gI().sendMoney(player);

            player.effectSkin.xDame = x;
            player.effectSkin.lastTimeXDame = System.currentTimeMillis();

            player.nPoint.calPoint();
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().point(player);

            Service.gI().sendThongBao(player, "Phù hộ thành công! Sức đánh đã tăng x" + x);
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi khi phù hộ sức đánh!");
        }
    }

    // ====================== CHUYỂN MAP ======================
    public void changeMap(Player player, byte index) {
        if (player == null || player.mapBlackBall == null || index < 0 || index >= player.mapBlackBall.size()) {
            Service.gI().sendThongBao(player, "Không thể di chuyển tới khu vực này!");
            return;
        }

        try {
            if (!TimeUtil.isBlackBallWarOpen()) {
                Service.gI().sendThongBao(player, "Trò chơi tìm ngọc hôm nay đã kết thúc, hẹn gặp lại vào 20h ngày mai!");
                Service.gI().hideWaitDialog(player);
                return;
            }

            ChangeMapService.gI().changeMap(
                    player,
                    player.mapBlackBall.get(index).map.mapId,
                    -1, 50, 50
            );

        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Không thể chuyển bản đồ!");
        }
    }
}
