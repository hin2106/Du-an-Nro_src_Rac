package dungeon;
import utils.Functions;
import boss.Boss;
import boss.gas.DrLychee;
import clan.Clan;
import map.Zone;
import mob.Mob;
import player.Player;
import services.ItemTimeService;
import services.map.MapService;
import services.Service;
import services.map.ChangeMapService;
import utils.Util;
import server.Manager;
import system.Template;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import server.Maintenance;
import services.map.ItemMapService;
import utils.TimeUtil;

@Data
public class DestronGas implements Runnable {

    public static final long POWER_CAN_GO_TO_KHI_GAS_HUY_DIET = 2000000000;
    public static final int AVAILABLE = 50;
    public static final int TIME_KHI_GAS_HUY_DIET = 1800000;
    public static final int N_PLAYER_CLAN = 0;

    public int id;
    public byte level;
    public final List<Zone> zones;

    public Clan clan;
    public boolean isOpened;
    private long lastTimeOpen;
    private long lastTimeUpdateMessage;
    private boolean kickoutkghd;
    private long timeKickOutKGHD;
    public List<Boss> bosses = new ArrayList<>();
    private boolean callBoss;
    public boolean hatchiyatchDead;

    public DestronGas(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning() && isOpened) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - startTime), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (isOpened) {
            if (Util.canDoWithTime(lastTimeOpen, TIME_KHI_GAS_HUY_DIET) || (kickoutkghd && Util.canDoWithTime(timeKickOutKGHD, 60000))) {
                finish();
                dispose();
            }

            boolean allCharactersDead = true;
            for (Zone zone : zones) {
                for (Mob mob : zone.mobs) {
                    if (!mob.isDie()) {
                        allCharactersDead = false;
                        break;
                    }
                }
            }

            if (allCharactersDead && !callBoss) {
                try {
                    long bossDamage = (1000 * level);
                    long bossMaxHealth = (15000000 * level);
                    bossDamage = Math.min(bossDamage, 200000000L);
                    bossMaxHealth = Math.min(bossMaxHealth, 2000000000L);
                    bosses.add(new DrLychee(
                            getMapById(148),
                            clan,
                            level,
                            (int) bossDamage,
                            (int) bossMaxHealth
                    ));
                    callBoss = true;
                } catch (Exception exception) {
                }
            }

            if (!kickoutkghd && (hatchiyatchDead || Util.canDoWithTime(lastTimeOpen, TIME_KHI_GAS_HUY_DIET - 60000))) {
                kickoutkghd = true;
                timeKickOutKGHD = System.currentTimeMillis();
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Nơi này sắp nổ tung mau chạy đi");
                    }
                }
            }
            if (kickoutkghd && Util.canDoWithTime(lastTimeUpdateMessage, 10000)) {
                lastTimeUpdateMessage = System.currentTimeMillis();
                for (Zone zone : zones) {
                    List<Player> players = zone.getPlayers();
                    for (Player pl : players) {
                        Service.gI().sendThongBao(pl, "Về làng Aru sau " + TimeUtil.getTimeLeft(timeKickOutKGHD, 60) + " nữa");
                    }
                }
            }

        }
    }

    public void openKhiGasHuyDiet(Player plOpen, Clan clan, byte level) {
        try {
            this.level = level;
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.lastTimeOpenKhiGasHuyDiet = this.lastTimeOpen;
            this.clan.playerOpenKhiGasHuyDiet = plOpen;
            this.clan.KhiGasHuyDiet = this;
            this.callBoss = false;
            this.isOpened = true;
            this.init();
            sendTextKhiGasHuyDiet();
        } catch (Exception e) {
            plOpen.clan.lastTimeOpenKhiGasHuyDiet = 0;
            this.dispose();
        }
    }

    public void sendThanhTichKhiGaHuyDiet(Player pl) {
        long timeDoneKGHD;
        timeDoneKGHD = System.currentTimeMillis() - pl.clan.lastTimeOpenBanDoKhoBau;
        int levelDoneKGHD;
        levelDoneKGHD = pl.clan.KhiGasHuyDiet.level;
        if (levelDoneKGHD > pl.clan.levelDoneKhiGaHuyDiet) {
            pl.clan.levelDoneKhiGaHuyDiet = levelDoneKGHD;
            pl.clan.thoiGianHoanThanhKhiGaHuyDiet = levelDoneKGHD;
        } else if (levelDoneKGHD == pl.clan.levelDoneKhiGaHuyDiet) {
            if (timeDoneKGHD < pl.clan.thoiGianHoanThanhKhiGaHuyDiet) {
                pl.clan.thoiGianHoanThanhKhiGaHuyDiet = timeDoneKGHD;
            }
        }
        pl.clan.updatethanhTichKGHD(pl.clan.id);
        pl.clan.updatethanhTichKGHDForLeader();
    }

    private void init() {
        for (Zone zone : this.zones) {
            // Lấy MapTemplate để lấy HP gốc
            Template.MapTemplate mapTemplate = null;
            for (Template.MapTemplate mt : Manager.MAP_TEMPLATES) {
                if (mt != null && mt.id == zone.map.mapId) {
                    mapTemplate = mt;
                    break;
                }
            }
            List<Mob> mobs = zone.mobs;
            for (int i = 0; i < mobs.size(); i++) {
                Mob mob = mobs.get(i);
                // Lấy HP gốc từ MapTemplate hoặc từ maxHp nếu MapTemplate không có
                long baseHp = mob.point.maxHp;
                if (mapTemplate != null && mapTemplate.mobHp != null && mob.id < mapTemplate.mobHp.length) {
                    baseHp = mapTemplate.mobHp[mob.id];
                } else if (mob.point.hp <= 0 && mob.point.maxHp > 0) {
                    // Nếu không lấy được từ template, dùng maxHp hiện tại
                    baseHp = mob.point.maxHp;
                } else if (mob.point.hp > 0) {
                    // Nếu HP hiện tại > 0, có thể đây là giá trị gốc
                    baseHp = mob.point.hp;
                }
                // Kiểm tra map ID có trong danh sách cho phép
                boolean isSpecialMap = (zone.map.mapId == 147)
                        || (zone.map.mapId == 148)
                        || (zone.map.mapId == 149)
                        || (zone.map.mapId == 151)
                        || (zone.map.mapId == 152);
                if (isSpecialMap && (i == 0 || i == 1)) {
                    mob.lvMob = 1;
                    mob.point.dame = Math.min(level * 31 * 5 * mob.tempId * 10, 10_000_000_000l);
                    mob.point.maxHp = Math.min(level * 11 * baseHp, 10_000_000_000l);
                    mob.point.sethp(mob.point.maxHp);
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                } else {
                    mob.lvMob = mob.tempId == 76 ? 1 : 0;
                    mob.point.dame = Math.min(level * 31 * 5 * mob.tempId, 10_000_000_000l);
                    // Mob thường: HP x11
                    mob.point.maxHp = Math.min(level * 11 * baseHp, 10_000_000_000l);
                    mob.point.sethp(mob.point.maxHp);
                    mob.hoiSinh();
                    mob.hoiSinhMobPhoBan();
                }
            }
        }
        new Thread(this, "Khí Gas Hủy Diệt: " + this.clan.name).start();
    }

    public void finish() {
        try {
            if (this.clan != null) {
                long timeDoneKGHD = System.currentTimeMillis() - this.clan.lastTimeOpenKhiGasHuyDiet;
                int levelDoneKGHD = this.level;
                if (levelDoneKGHD > this.clan.levelDoneKhiGaHuyDiet) {
                    this.clan.levelDoneKhiGaHuyDiet = levelDoneKGHD;
                    this.clan.thoiGianHoanThanhKhiGaHuyDiet = timeDoneKGHD;
                } else if (levelDoneKGHD == this.clan.levelDoneKhiGaHuyDiet) {
                    if (timeDoneKGHD < this.clan.thoiGianHoanThanhKhiGaHuyDiet || this.clan.thoiGianHoanThanhKhiGaHuyDiet <= 0) {
                        this.clan.thoiGianHoanThanhKhiGaHuyDiet = timeDoneKGHD;
                    }
                }
                this.clan.updatethanhTichKGHD(this.clan.id);
                this.clan.updatethanhTichKGHDForLeader();
            }
        } catch (Exception ignore) {
        }
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfKGHD(pl);
                }
            }

        }
    }

    private void kickOutOfKGHD(Player player) {
        if (MapService.gI().isMapKhiGasHuyDiet(player.zone.map.mapId)) {
            ChangeMapService.gI().changeMapBySpaceShip(player, 0, -1, -1);
        }
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : this.zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    private void sendTextKhiGasHuyDiet() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextKhiGasHuyDiet(pl);
        }
    }

    private void removeTextKhiGasHuyDiet() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextKhiGasHuyDiet(pl);
            sendThanhTichKhiGaHuyDiet(pl);
        }
    }

    public void dispose() {
        for (Zone zone : zones) {
            for (int i = zone.items.size() - 1; i >= 0; i--) {
                if (i < zone.items.size()) {
                    ItemMapService.gI().removeItemMap(zone.items.get(i));
                }
            }
        }
        for (Boss boss : bosses) {
            if (!boss.isDie()) {
                boss.leaveMap();
            }
        }
        this.removeTextKhiGasHuyDiet();
        this.bosses.clear();
        this.isOpened = false;
        this.clan.KhiGasHuyDiet = null;
        this.clan = null;
        this.kickoutkghd = false;
        this.hatchiyatchDead = false;
    }
}
