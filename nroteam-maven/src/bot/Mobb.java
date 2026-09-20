package bot;

import java.util.Random;
import mob.Mob;
import services.SkillService;
import services.player.PlayerService;
import skill.Skill;

public class Mobb {

    private Mob mAttack;
    public long lastTimeChanM;
    public Bot bot;

    private long lastSearchMob;
    private long nextMoveAt;
    private long nextAttackAt;
    private long nextWanderAt;
    private long nextMapChangeAt;
    private long targetSince;
    private final Random rand = new Random();

    public Mobb(Bot b) {
        this.bot = b;
        this.lastSearchMob = 0;
        this.lastTimeChanM = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        this.nextMoveAt = now + between(250, 900);
        this.nextAttackAt = now + between(450, 1_400);
        this.nextWanderAt = now + between(2_000, 6_000);
        this.nextMapChangeAt = now + between(150_000, 300_000);
    }

    public void update() {
        try {
            this.Attack();
        } catch (Exception e) {
            System.err.println("[Mobb] Lỗi update: " + bot.name + " - " + e.getMessage());
        }
    }

    private void GetMobAttack() {
        long now = System.currentTimeMillis();
        if (now - lastSearchMob < between(650, 1_100)) {
            return;
        }
        lastSearchMob = now;

        if (bot.zone == null || bot.zone.mobs == null || bot.zone.mobs.isEmpty()) {
            mAttack = null;
            return;
        }

        if (mAttack != null && !mAttack.isDie() && mAttack.zone == bot.zone
                && now - targetSince < between(8_000, 18_000)) {
            return;
        }

        try {
            Mob selectedMob = null;
            int selectedScore = Integer.MAX_VALUE;

            for (Mob mob : bot.zone.mobs) {
                if (mob != null && !mob.isDie() && mob.location != null && bot.location != null) {
                    int dx = Math.abs(bot.location.x - mob.location.x);
                    int dy = Math.abs(bot.location.y - mob.location.y);
                    // Một chút ngẫu nhiên để cả đàn bot không cùng khóa đúng một mob gần nhất.
                    int distance = dx + dy + rand.nextInt(121);

                    if (distance < selectedScore) {
                        selectedScore = distance;
                        selectedMob = mob;
                    }
                }
            }

            if (selectedMob != null) {
                mAttack = selectedMob;
                targetSince = now;
                nextAttackAt = Math.max(nextAttackAt, now + between(250, 900));
            } else {
                mAttack = null;
            }
        } catch (Exception e) {
            System.err.println("[Mobb] Lỗi tìm mob: " + bot.name);
            mAttack = null;
        }
    }

    private Skill getAttackSkill() {
        if (bot.playerSkill == null || bot.playerSkill.skills.isEmpty()) {
            return null;
        }
        Skill selected = null;
        int candidates = 0;
        for (Skill s : bot.playerSkill.skills) {
            if (s != null && s.template != null && (s.template.type == 1 || s.template.type == 2)) {
                // Reservoir sampling: chọn kỹ năng hợp lệ ngẫu nhiên mà không cần tạo list phụ.
                if (rand.nextInt(++candidates) == 0) {
                    selected = s;
                }
            }
        }
        return selected != null ? selected : bot.playerSkill.skills.get(0);
    }

    public void Attack() {
        GetMobAttack();
        if (mAttack == null || mAttack.isDie()) {
            randomWander();
            return;
        }
        if (bot.location == null || mAttack.location == null) {
            return;
        }

        Skill skill = getAttackSkill();
        if (skill == null) {
            System.err.println("[Mobb] Bot " + bot.name + " không có skill");
            return;
        }

        long now = System.currentTimeMillis();
        int dx = Math.abs(bot.location.x - mAttack.location.x);
        int dy = Math.abs(bot.location.y - mAttack.location.y);

        if (dx > 75 || dy > 50) {
            if (now >= nextMoveAt) {
                try {
                    int approachX = clamp(mAttack.location.x + between(-38, 38), 24,
                            Math.max(24, bot.zone.map.mapWidth - 24));
                    int approachY = clamp(mAttack.location.y + between(-12, 12), 0,
                            Math.max(0, bot.zone.map.mapHeight - 24));
                    PlayerService.gI().playerMove(bot, approachX, approachY);
                    nextMoveAt = now + between(550, 1_250);
                } catch (Exception e) {
                    System.err.println("[Mobb] Lỗi di chuyển: " + bot.name);
                }
            }
            return;
        }

        if (now >= nextAttackAt) {
            if (now - skill.lastTimeUseThisSkill < skill.coolDown) {
                nextAttackAt = now + between(120, 350);
                return;
            }
            bot.playerSkill.skillSelect = skill;
            try {
                SkillService.gI().useSkill(bot, null, mAttack, -1, null);
                skill.lastTimeUseThisSkill = now;
                // Thi thoảng ngập ngừng hoặc đổi vị trí như người chơi thật.
                nextAttackAt = now + between(280, rand.nextInt(10) == 0 ? 1_900 : 850);
                if (rand.nextInt(6) == 0) {
                    nextMoveAt = now;
                }
            } catch (Exception ex) {
                System.err.println("[Mobb] Lỗi tấn công: " + bot.name);
            }
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
                        int targetX = rand.nextInt(Math.max(100, mapWidth - 100)) + 50;
                        int targetY = rand.nextInt(Math.max(50, mapHeight / 2));
                        if (rand.nextInt(4) != 0) {
                            PlayerService.gI().playerMove(bot, (short) targetX, (short) targetY);
                        }
                        nextWanderAt = now + between(2_500, 7_500);
                    }
                }
            } catch (Exception e) {
                System.err.println("[Mobb] Lỗi wander: " + bot.name);
            }
        }
    }

    public void chanGeMap() {
        long now = System.currentTimeMillis();
        if (now >= nextMapChangeAt) {
            bot.joinMap();
            lastTimeChanM = now;
            nextMapChangeAt = now + between(150_000, 300_000);
            mAttack = null;
            if (bot.zone != null) {
                scaleStatsToZone(bot.zone);
            }
        }
    }

    private int between(int min, int max) {
        return min + rand.nextInt(max - min + 1);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public void scaleStatsToZone(map.Zone zone) {
        if (zone == null || zone.mobs == null || zone.mobs.isEmpty()) {
            return;
        }
        try {
            long totalHp = 0;
            int count = 0;
            for (Mob mob : zone.mobs) {
                if (mob != null && mob.point != null) {
                    totalHp += mob.point.getHpFull();
                    count++;
                }
            }
            if (count > 0) {
                long avgHp = totalHp / count;
                bot.nPoint.hpg = (int) Math.min(2_000_000_000, avgHp * 2);
                bot.nPoint.mpg = (int) Math.min(2_000_000_000, avgHp * 5);
                bot.nPoint.dameg = (int) Math.max(100, avgHp / 25);
                bot.nPoint.calPoint();
                bot.nPoint.setFullHpMp();
            }
        } catch (Exception e) {
            System.err.println("[Mobb] Lỗi scale stats: " + bot.name);
        }
    }
}
