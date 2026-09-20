package mob.bigboss;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import map.Map;
import map.Zone;
import mob.BigBoss;
import mob.Mob;
import network.Message;
import player.Player;
import services.Service;
import services.map.MapService;
import task.KolTaskService;
import utils.Util;

public class GauTuongCuop extends BigBoss {

    private static long lastCheckTime;
    private static boolean isSpawned = false;

    public GauTuongCuop(Mob mob) {
        super(mob);
    }

    public static void checkAndSpawnAuto() {
        if (System.currentTimeMillis() - lastCheckTime < 60000) {
            return;
        }
        lastCheckTime = System.currentTimeMillis();

        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
        boolean isSpawnTime = (hour >= 19 && hour < 23) || (hour == 23 && minute == 0);

        if (isWeekend && isSpawnTime && !isSpawned) {
            spawnGauTuongCuop();
            isSpawned = true;
        } else if (!isSpawnTime && isSpawned) {
            isSpawned = false;
        }
    }

    private static void spawnGauTuongCuop() {
        try {
            Map map27 = MapService.gI().getMapById(27);
            Map map28 = MapService.gI().getMapById(28);

            if (map27 != null) {
                spawnGauTuongCuopInMap(map27, "Map 27");
            }
            if (map28 != null) {
                spawnGauTuongCuopInMap(map28, "Map 28");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi spawn Gấu Tướng Cướp: " + e.getMessage());
        }
    }

    private static void spawnGauTuongCuopInMap(Map map, String mapName) {
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return;
        }
        for (map.Zone zone : map.zones) {
            try {
                Mob baseMob = new Mob();
                baseMob.id = zone.mobs.size();
                baseMob.tempId = 77;
                baseMob.level = 1;
                baseMob.point.setHpFull(2000000000);
                baseMob.location.x = 400;
                baseMob.location.y = 300;
                baseMob.point.sethp(baseMob.point.getHpFull());
                baseMob.pDame = 33;
                baseMob.pTiemNang = 2;
                baseMob.type = 43;
                baseMob.setTiemNang();
                GauTuongCuop gauTuongCuop = new GauTuongCuop(baseMob);
                gauTuongCuop.zone = zone;
                zone.mobs.add(gauTuongCuop);

                // Thêm vào spatial grid khi GauTuongCuop spawn
                if (zone.spatialGridEnabled && zone.spatialGrid != null
                        && gauTuongCuop.location != null && !gauTuongCuop.isDie()) {
                    try {
                        zone.spatialGrid.addMob(gauTuongCuop);
                    } catch (Exception e) {
                        utils.Logger.logException(GauTuongCuop.class, e, "Lỗi thêm GauTuongCuop vào spatial grid");
                    }
                }
                for (Player pl : zone.getPlayers()) {
                    if (pl != null) {
                        Service.gI().sendThongBao(pl, "Gấu Tướng Cướp đã xuất hiện tại " + mapName + "!");
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi spawn Gấu Tướng Cướp tại " + mapName + ": " + e.getMessage());
            }
        }
    }

    public static void removeGauTuongCuop() {
        try {
            Map map27 = MapService.gI().getMapById(27);
            Map map28 = MapService.gI().getMapById(28);

            if (map27 != null) {
                removeGauTuongCuopFromMap(map27, "Map 27");
            }
            if (map28 != null) {
                removeGauTuongCuopFromMap(map28, "Map 28");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi remove Gấu Tướng Cướp: " + e.getMessage());
        }
    }

    private static void removeGauTuongCuopFromMap(Map map, String mapName) {
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return;
        }
        for (Zone zone : map.zones) {
            try {
                zone.mobs.removeIf(mob -> mob.tempId == 77);
            } catch (Exception e) {
                System.err.println("Lỗi khi remove Gấu Tướng Cướp khỏi " + mapName + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void attack() {
        if (!isDie() && !effectSkill.isHaveEffectSkill() && Util.canDoWithTime(lastBigBossAttackTime, 3000)) {
            if (this.zone.getNotBosses().isEmpty()) {
                return;
            }
            List<Player> players = new ArrayList<>();

            action = Util.nextInt(11, 15);

            switch (action) {
                case 11:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 50) {
                            players.add(pl);
                            break;
                        }
                    }
                    break;
                case 12:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 100) {
                            players.add(pl);
                            break;
                        }
                    }
                    break;
                case 13:
                case 14:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 150) {
                            players.add(pl);
                        }
                    }
                    break;
                case 15:
                    for (Player pl : this.zone.getNotBosses()) {
                        if (Util.getDistance(pl, this) < 200) {
                            players.add(pl);
                        }
                    }
                    break;
            }

            if (players.isEmpty()) {
                int index = Util.nextInt(0, this.zone.getNotBosses().size() - 1);
                players.add(this.zone.getNotBosses().get(index));
                action = 10;
            }

            Message msg = null;
            try {
                msg = new Message(102);
                msg.writer().writeByte(action);
                msg.writer().writeByte(this.id);
                switch (action) {
                    case 10:
                    case 21:
                        for (Player player : players) {
                            this.location.x = player.location.x + Util.nextInt(-10, 10);
                            this.location.y = player.location.y;
                        }
                        msg.writer().writeShort(this.location.x);
                        msg.writer().writeShort(this.location.y);
                        break;
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        msg.writer().writeByte(players.size());
                        int dir = 0;
                        for (Player pl : players) {
                            double dame = pl.injured(null, this.point.getDameAttack(), false, true);
                            msg.writer().writeInt((int) pl.id);
                            msg.writeSmartLong(dame);
                            dir = pl.location.x < this.location.x ? -1 : 1;
                        }
                        msg.writer().writeByte(dir);
                        break;
                    case 22:
                        break;
                    case 23:
                        break;
                    default:
                        break;
                }
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                lastBigBossAttackTime = System.currentTimeMillis();
            } catch (Exception e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                    msg = null;
                }
            }
        }
        if (this.isDie()) {
            try {
                for (Player pl : this.zone.getNotBosses()) {
                    if (pl != null && Util.getDistance(pl, this) <= 200) {
                        KolTaskService.doneTaskKolGauTuongCuop(pl);
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật nhiệm vụ KOL: " + e.getMessage());
            }
        }
    }

}
