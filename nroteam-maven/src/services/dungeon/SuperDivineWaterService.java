package services.dungeon;
import boss.pocolo.Pocolo;
import consts.ConstNpc;
import java.util.ArrayList;
import java.util.List;
import map.Zone;
import mob.Mob;
import player.Player;
import services.EffectSkillService;
import services.map.MapService;
import services.map.NpcService;
import services.player.PlayerService;
import services.Service;
import services.TaskService;
import services.map.ChangeMapService;
import utils.Util;

public class SuperDivineWaterService {

    private static SuperDivineWaterService instance;
    private final List<Zone> zones = new ArrayList<>();

    public static SuperDivineWaterService gI() {
        if (instance == null) {
            instance = new SuperDivineWaterService();
        }
        return instance;
    }

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public void joinMapThanhThuy(Player player) {
        if (player.winSTT && !Util.isAfterMidnight(player.lastTimeWinSTT)) {
            Service.gI().sendThongBao(player, "Hãy gặp thần mèo Karin để sử dụng");
            return;
        } else if (player.winSTT && Util.isAfterMidnight(player.lastTimeWinSTT)) {
            player.winSTT = false;
            player.callBossPocolo = false;
            player.zoneSieuThanhThuy = null;
        }
        Zone zoneJoin = null;
        for (Zone zone : this.zones) {
            if (zone.getNumOfPlayers() + zone.getNumOfBosses() == 0) {
                zoneJoin = zone;
                break;
            }
        }
        if (player.zoneSieuThanhThuy != null) {
            zoneJoin = player.zoneSieuThanhThuy;
        }
        if (zoneJoin != null) {
            init(zoneJoin, player);
            EffectSkillService.gI().setPKSTT(player, 900000);
            ChangeMapService.gI().changeMap(player, zoneJoin, 70, 336);
            TaskService.gI().checkDoneTaskJoinMapTayKarin(player);
        } else {
            Service.gI().sendThongBao(player, "Vui lòng thử lại sau ít phút!");
        }
    }

    public void init(Zone zone, Player player) {
        Player mc = zone.getNpc();
        Service.gI().setPos(mc, 100, 336);
        player.zoneSieuThanhThuy = zone;

        long totalDamage = player.nPoint.dame / 2L;
        long totalHp = player.nPoint.hpMax / 2L;

        for (Mob mob : zone.mobs) {
            long dameVal;
            long hpVal;

            switch (mob.tempId) {
                case 26 -> {
                    dameVal = Math.min(totalHp * 150 / 100, 2_000_000_000L);
                    hpVal = Math.min(totalDamage * 150 / 100, 2_000_000_000L);
                }
                case 25 -> {
                    dameVal = Math.min(totalHp * 2, 2_000_000_000L);
                    hpVal = Math.min(totalDamage * 2, 2_000_000_000L);
                }
                default -> {
                    dameVal = Math.min(totalHp, 2_000_000_000L);
                    hpVal = Math.min(totalDamage, 2_000_000_000L);
                }
            }
            mob.point.dame = Math.min(dameVal, Integer.MAX_VALUE);
            mob.point.maxHp = Math.min(hpVal, Integer.MAX_VALUE);

            mob.lvMob = 0;
            mob.hoiSinh();
            mob.hoiSinhMobTayKarin();
        }
    }

    public void update(Player player) {
        try {
            if (player.isPl() && MapService.gI().isMapSieuThanhThuy(player.zone.map.mapId) && player.zone == player.zoneSieuThanhThuy) {
                if (!player.callBossPocolo) {
                    boolean allCharactersDead = true;
                    for (Mob mob : player.zone.mobs) {
                        if (!mob.isDie()) {
                            allCharactersDead = false;
                            break;
                        }
                    }
                    if (allCharactersDead) {
                        try {
                            long bossDamage = Math.min(player.nPoint.dame, 200_000_000L);
                            long bossMaxHealth = Math.min(player.nPoint.hpMax * 5L, 2_000_000_000L);
                            new Pocolo(
                                    player.zone,
                                    player,
                                    bossDamage,
                                    bossMaxHealth
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        player.callBossPocolo = true;
                    }
                }

                if (player.winSTT && Util.canDoWithTime(player.lastTimeWinSTT, 3500)) {
                    if (Util.canDoWithTime(player.lastTimeUpdateSTT, 10000)) {
                        Player mc = player.zone.getNpc();
                        int x = player.location.x + Util.nextInt(-50, 50);
                        int y = 336;
                        PlayerService.gI().playerMove(mc, x, y);
                        if (player.isDie()) {
                            Service.gI().hsChar(player,
                                    (int) Math.min(player.nPoint.hpMax, Integer.MAX_VALUE),
                                    (int) Math.min(player.nPoint.hpMax, Integer.MAX_VALUE));
                        }
                        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_SIEU_THAN_THUY, 2119, "Để tôi đưa cậu về", "Đồng ý", "Từ chối");
                        player.lastTimeUpdateSTT = System.currentTimeMillis();
                    }
                }
                if (!player.effectSkill.isPKSTT) {
                    ChangeMapService.gI().changeMap(player, player.gender + 21, -1, -1, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
