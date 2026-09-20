package boss.challenge;

import utils.Functions;
import boss.Boss;
import boss.challenge.FideChallenge;
import boss.challenge.MabuChallenge;
import boss.challenge.XenBoHungChallenge;
import clan.Clan;
import map.Zone;
import player.Player;
import services.ItemTimeService;
import services.map.ChangeMapService;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import server.Maintenance;
import services.map.ItemMapService;
import services.Service;
import services.map.MapService;
import utils.Logger;

public class BossChallenge implements Runnable {

    public static final int TIME_BOSS_CHALLENGE = 1800000;
    public static final int AVAILABLE = 5;
    public static final int TIME_LEAVE_MAP = 30000;

    public int id;
    public final List<Zone> zones;

    public Clan clan;
    public boolean isOpened;
    private long lastTimeOpen;
    private long lastTimeUpdateMessage;
    private boolean timeToLeave;
    private long timeStartLeave;
    public List<Boss> bosses = new ArrayList<>();
    public int currentRound = 1;
    public boolean roundCompleted = false;
    public int maxRounds = 3;

    public BossChallenge(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning() && isOpened ) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - startTime), 10));
            } catch (Exception e) {
                Logger.logException(BossChallenge.class, e);
            }
        }
    }

    public void update() {
        try {
            if (!isOpened) {
                return;
            }
            boolean expired = Util.canDoWithTime(lastTimeOpen, TIME_BOSS_CHALLENGE)
                    || (timeToLeave && Util.canDoWithTime(timeStartLeave, TIME_LEAVE_MAP));
            if (expired) {
                finish();
                return;
            }
            boolean allBossesDead = bosses.stream().allMatch(Boss::isDie);
            if (allBossesDead && !roundCompleted && !bosses.isEmpty()) {
                roundCompleted = true;
                removeTextBossChallenge();
                timeToLeave = true;
                timeStartLeave = System.currentTimeMillis();
                bosses.clear();
                thongbaoall("Về khu vực bang sau 30 giây");
            }
            if (bosses.isEmpty() && !roundCompleted) {
                spawnBossForRound(currentRound);
            }
            if (timeToLeave && Util.canDoWithTime(lastTimeUpdateMessage, 1000)) {
                lastTimeUpdateMessage = System.currentTimeMillis();
                long timeLeft = TIME_LEAVE_MAP - (System.currentTimeMillis() - timeStartLeave);
                if (timeLeft > 0) {
                    int secondsLeft = (int) (timeLeft / 1000);
                    thongbaoall("Về khu vực bang sau " + secondsLeft + " giây");
                }
            }
        } catch (Exception e) {
            Logger.logException(getClass(), e);
        }
    }

    public List<Zone> getZones() {
        return this.zones;
    }

    private void spawnBossForRound(int round) {
        try {
            Zone bossZone = getMapById(164);
            if (bossZone == null) {
                return;
            }
            Boss boss = null;
            switch (round) {
                case 1 -> {
                    boss = new FideChallenge();
                }
                case 2 -> {
                    boss = new XenBoHungChallenge();
                }
                case 3 -> {
                    boss = new MabuChallenge();
                }
            }
            if (boss.currentLevel < 0 || boss.currentLevel >= boss.data.length) {
                boss.currentLevel = 0;
            }
            boss.initBase();
            boss.zone = bossZone;
            boss.location.x = Util.nextInt(200, 400);
            boss.location.y = bossZone.map.yPhysicInTop(boss.location.x, 100);
            boss.joinMap();
            bosses.add(boss);
        } catch (Exception e) {
            Logger.logException(BossChallenge.class, e);
        }
    }

    public void openBossChallenge(Player plOpen, Clan clan) {
        try {
            this.lastTimeOpen = System.currentTimeMillis();
            this.clan = clan;
            this.clan.lastTimeBossChallenge = this.lastTimeOpen;
            this.clan.playerOpenBossChallenge = plOpen;
            this.clan.bossChallenge = this;
            this.isOpened = true;
            this.currentRound = Math.min(clan.bossChallengeToday, 3);
            this.roundCompleted = false;
            this.timeToLeave = false;
            this.bosses.clear();
            sendTextBossChallenge();
            for (Player member : clan.membersInGame) {
                if (member != null) {
                    ChangeMapService.gI().changeMapNonSpaceship(member, 164, 420, 408);
                }
            }
            new Thread(this, "Boss Challenge: " + this.clan.name).start();
        } catch (Exception e) {
            plOpen.clan.lastTimeBossChallenge = 0;
            this.dispose();
        }
    }

    public void startNextRound(Player player) {
        if (!isOpened || player.clan != this.clan) {
            return;
        }
        if (currentRound >= maxRounds || !roundCompleted) {
            return;
        }
        currentRound++;
        roundCompleted = false;
        for (Player member : clan.membersInGame) {
            if (member != null) {
                ChangeMapService.gI().changeMapNonSpaceship(member, 164, 420, 408);
            }
        }
        spawnBossForRound(currentRound);
        sendTextBossChallenge();
    }

    public void finish() {
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    Player pl = zone.getPlayers().get(i);
                    kickOutOfBossChallenge(pl);
                    ChangeMapService.gI().changeMapNonSpaceship(pl, 153, 420, 408);
                }
            }
        }
        dispose();
    }

    private void thongbaoall(String message) {
        for (Zone zone : zones) {
            for (Player pl : zone.getPlayers()) {
                Service.gI().sendThongBao(pl, message);
            }
        }
    }

    private void kickOutOfBossChallenge(Player player) {
         if (MapService.gI().isMapCL(player.zone.map.mapId)) {
        ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
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

    private void sendTextBossChallenge() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextClanBoss(pl, currentRound,
                    (int) ((TIME_BOSS_CHALLENGE - (System.currentTimeMillis() - lastTimeOpen)) / 1000));
        }
    }

    private void removeTextBossChallenge() {
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextClanBoss(pl);
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
        this.removeTextBossChallenge();
        this.bosses.clear();
        this.isOpened = false;
        if (this.clan != null) {
            this.clan.bossChallenge = null;
        }
        this.clan = null;
        this.timeToLeave = false;
    }
}
