package bot;

import boss.Boss;
import consts.BossStatus;
import consts.ConstPlayer;
import java.util.Random;
import java.util.Set;
import managers.boss.BossManager;
import services.Service;
import services.SkillService;
import services.map.ChangeMapService;
import services.map.MapService;
import services.player.PlayerService;
import skill.Skill;

public class Sanb {

    public Bot bot;
    public Boss bossAttack;
    public long lastTimeSkill1;
    public long lastSearchBoss;

    private long nextBossSearchAt;
    private long nextMoveAt;
    private long nextAttackAt;
    private long nextSpecialSkillAt;
    private long nextWanderAt;
    private final Random random;

    public Sanb(Bot b) {
        this.bot = b;
        this.random = new Random();
        this.lastSearchBoss = 0;
        this.lastTimeSkill1 = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        this.nextBossSearchAt = now + between(1_000, 4_000);
        this.nextMoveAt = now + between(250, 900);
        this.nextAttackAt = now + between(500, 1_500);
        this.nextSpecialSkillAt = now + between(35_000, 70_000);
        this.nextWanderAt = now + between(2_500, 7_000);
    }

    public void update() {
        try {
            this.SanBot();
        } catch (Exception e) {
            System.err.println("[Sanb] Lỗi update: " + bot.name + " - " + e.getMessage());
        }
    }

    public boolean isMap(int mapId) {
        return (MapService.gI().isMapDoanhTrai(mapId) || MapService.gI().isMapBlackBallWar(mapId)
                || MapService.gI().isMapBanDoKhoBau(mapId) || MapService.gI().isMapMaBu(mapId)
                || MapService.gI().isMapConDuongRanDoc(mapId));
    }

    public void GetBoss(int status) {
        long now = System.currentTimeMillis();
        if (now < nextBossSearchAt) {
            return;
        }
        lastSearchBoss = now;
        nextBossSearchAt = now + between(2_500, 6_000);

        if (this.bossAttack != null && !this.bossAttack.isDie() && this.bossAttack.zone != null) {
            return;
        }

        try {
            Boss candidate = null;
            Set<Integer> ids = BossManager.gI().getExistingBossIDs();
            if (ids == null || ids.isEmpty()) {
                this.bossAttack = null;
                return;
            }

            int pick = random.nextInt(ids.size());
            int i = 0;
            int chosenId = -1;
            for (int id : ids) {
                if (i++ == pick) {
                    chosenId = id;
                    break;
                }
            }

            if (chosenId != -1) {
                candidate = BossManager.gI().findBossByBossID(chosenId);
            }

            if (candidate == null || candidate.isDie()) {
                this.bossAttack = null;
                return;
            }

            map.Zone bossZone = candidate.zone;
            if (bossZone == null || bossZone.map == null) {
                this.bossAttack = null;
                return;
            }

            boolean canJoin = (!this.isMap(bossZone.map.mapId)
                    && !bossZone.isFullPlayer()
                    && bossZone.mobs != null
                    && bossZone.mobs.size() >= 1);

            if (!canJoin) {
                this.bossAttack = null;
                return;
            }

            this.bossAttack = candidate;
            this.nextAttackAt = now + between(600, 1_600);
            this.nextMoveAt = now + between(200, 700);

            if (this.bossAttack.nPoint != null && this.bot.nPoint != null) {
                this.bot.nPoint.hpg = Math.max(this.bossAttack.nPoint.hpMax / 2, 100000);
                this.bot.nPoint.mpg = Math.max(this.bossAttack.nPoint.mpMax, 100000);
                this.bot.nPoint.dameg = Math.max(this.bossAttack.nPoint.hpMax / 20, 10000);
                this.bot.nPoint.calPoint();
                this.bot.nPoint.setFullHpMp();
                Service.gI().point(this.bot);
            }

            ChangeMapService.gI().goToMap(this.bot, bossZone);
            if (this.bot.zone != null) {
                this.bot.zone.load_Me_To_Another(this.bot);
            }
        } catch (Exception e) {
            System.err.println("[Sanb] Lỗi tìm boss: " + bot.name);
            this.bossAttack = null;
        }
    }

    public void GetSkil() {
        try {
            if (this.bot.playerSkill != null && this.bot.playerSkill.skills != null
                    && !this.bot.playerSkill.skills.isEmpty()) {
                Skill selected = null;
                int candidates = 0;
                for (Skill skill : this.bot.playerSkill.skills) {
                    if (skill != null && skill.template != null
                            && (skill.template.type == 1 || skill.template.type == 2)
                            && random.nextInt(++candidates) == 0) {
                        selected = skill;
                    }
                }
                this.bot.playerSkill.skillSelect = selected != null
                        ? selected : this.bot.playerSkill.skills.get(0);
            }

            long now = System.currentTimeMillis();
            if (now >= nextSpecialSkillAt) {
                switch (this.bot.gender) {
                    case ConstPlayer.XAYDA -> this.bot.useSkill(Skill.BIEN_KHI);
                    case ConstPlayer.TRAI_DAT -> this.bot.useSkill(Skill.QUA_CAU_KENH_KHI);
                    case ConstPlayer.NAMEC -> this.bot.useSkill(Skill.MAKANKOSAPPO);
                }
                this.lastTimeSkill1 = now;
                this.nextSpecialSkillAt = now + between(40_000, 80_000);
            }
        } catch (Exception e) {
            System.err.println("[Sanb] Lỗi get skill: " + bot.name);
        }
    }

    public void SanBot() {
        this.GetBoss(0);
        this.GetSkil();

        if (this.bossAttack != null && !this.bossAttack.isDie() && this.bossAttack.bossStatus == BossStatus.ACTIVE) {
            if (this.bot.zone != this.bossAttack.zone) {
                this.bossAttack = null;
                return;
            }

            this.bot.typePk = ConstPlayer.PK_ALL;
            long now = System.currentTimeMillis();

            if (this.bot.location != null && this.bossAttack.location != null) {
                int dx = Math.abs(this.bot.location.x - this.bossAttack.location.x);
                int dy = Math.abs(this.bot.location.y - this.bossAttack.location.y);

                if (dx > 75 || dy > 50) {
                    if (now >= this.nextMoveAt) {
                        try {
                            int approachX = clamp(this.bossAttack.location.x + between(-45, 45), 24,
                                    Math.max(24, this.bot.zone.map.mapWidth - 24));
                            int approachY = clamp(this.bossAttack.location.y + between(-12, 12), 0,
                                    Math.max(0, this.bot.zone.map.mapHeight - 24));
                            PlayerService.gI().playerMove(this.bot, approachX, approachY);
                            this.nextMoveAt = now + between(450, 1_100);
                        } catch (Exception e) {
                            System.err.println("[Sanb] Lỗi di chuyển: " + bot.name);
                        }
                    }
                } else {
                    if (now >= this.nextAttackAt) {
                        try {
                            Skill skill = this.bot.playerSkill.skillSelect;
                            if (skill == null) {
                                this.nextAttackAt = now + between(300, 700);
                                return;
                            }
                            if (now - skill.lastTimeUseThisSkill < skill.coolDown) {
                                this.nextAttackAt = now + between(120, 350);
                                return;
                            }
                            SkillService.gI().useSkill(this.bot, this.bossAttack, null, -1, null);
                            skill.lastTimeUseThisSkill = now;
                            this.nextAttackAt = now + between(250, random.nextInt(9) == 0 ? 1_800 : 750);
                            if (random.nextInt(5) == 0) {
                                this.nextMoveAt = now;
                            }
                        } catch (Exception e) {
                            System.err.println("[Sanb] Lỗi tấn công: " + bot.name);
                        }
                    }
                }
            }
        } else {
            randomWander();
        }
    }

    private void randomWander() {
        long now = System.currentTimeMillis();
        if (now >= nextWanderAt) {
            try {
                if (bot.zone != null && bot.zone.map != null) {
                    int mapWidth = bot.zone.map.mapWidth;
                    int mapHeight = bot.zone.map.mapHeight;

                    if (mapWidth > 0 && mapHeight > 0) {
                        int targetX = random.nextInt(Math.max(100, mapWidth - 100)) + 50;
                        int targetY = random.nextInt(Math.max(50, mapHeight / 2));
                        if (random.nextInt(4) != 0) {
                            PlayerService.gI().playerMove(bot, (short) targetX, (short) targetY);
                        }
                        nextWanderAt = now + between(3_000, 8_000);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Sanb] Lỗi wander: " + bot.name);
            }
        }
    }

    private int between(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
