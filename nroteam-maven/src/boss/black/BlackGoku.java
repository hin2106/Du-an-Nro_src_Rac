package boss.black;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import consts.ConstPlayer;
import consts.ConstTask;
import consts.ConstTaskBadges;
import java.util.Random;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.RewardService;
import services.Service;
import services.SkillService;
import services.TaskService;
import task.BadgesTaskService;
import utils.Util;

public class BlackGoku extends Boss {

    private long st;
    private int timeLeaveMap;
    private static final Random random = new Random();

    public BlackGoku() throws Exception {
        super(BossID.BLACK_GOKU, false, true, BossesData.BLACK_GOKU, BossesData.SUPER_BLACK_GOKU);
    }

    @Override
    public void reward(Player plKill) {
        int[] itemTime = { 15, 16, 17, 18, 19, 20 };

        for (int i = 0; i < 2; i++) {
            if (Util.isTrue(20, 100)) {
                int itemId = itemTime[random.nextInt(itemTime.length)];
                Service.gI().dropItemMap(this.zone, new ItemMap(zone, itemId, 1, this.location.x,
                        zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
                
            } else if (Util.isTrue(20, 10000)) {
                int[] set1 = { 562, 564, 566, 561 };
                int id = set1[Util.nextInt(0, set1.length - 1)];
                dropEquipment(id, plKill);
            } // Rơi set 2
            else if (Util.isTrue(30, 100)) {
                int[] set2 = { 555, 556, 563, 557, 558, 565, 559, 567, 560 };
                int id = set2[Util.nextInt(0, set2.length - 1)];
                dropEquipment(id, plKill);
            }
        }

        if (TaskService.gI().getIdTask(plKill) == ConstTask.TASK_32_0) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 992, 1, this.location.x, this.location.y, plKill.id));
        }

        // Rơi set 1

        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);
    }

    private void dropEquipment(int itemId, Player plKill) {
        ItemMap item = new ItemMap(this.zone, itemId, 1, this.location.x,
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        RewardService.SetClothes(item.itemTemplate.id, item.itemTemplate.type, item.options);
        RewardService.gI().initStarOption(item, new RewardService.RatioStar[] {
                new RewardService.RatioStar((byte) 1, 55, 65),
                new RewardService.RatioStar((byte) 2, 35, 45),
                new RewardService.RatioStar((byte) 3, 20, 25),
                new RewardService.RatioStar((byte) 4, 10, 20),
                new RewardService.RatioStar((byte) 5, 5, 10),
                new RewardService.RatioStar((byte) 6, 3, 7),
                new RewardService.RatioStar((byte) 7, 1, 5)
        });
        Service.gI().dropItemMap(zone, item);
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (this.isDie()) {
            return 0;
        }

        if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
            this.chat("Xí hụt");
            return 0;
        }

        if (this.currentLevel != 0) {
            damage /= 2;
        }

        if (this.nPoint == null) {
            return 0;
        }

        long reduction = Util.nextInt(1, 100000);
        damage = Math.max(0, damage - reduction);
        damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);

        if (!piercing && effectSkill != null && effectSkill.isShielding) {
            if (damage > nPoint.hpMax) {
                EffectSkillService.gI().breakShield(this);
            }
            damage = 1;
        }

        this.nPoint.subHP(damage);
        if (isDie()) {
            this.setDie(plAtt);
            die(resolveKiller(plAtt));
        }

        return (int) damage;
    }

    // @Override
    // public void autoLeaveMap() {
    // if (Util.canDoWithTime(st, timeLeaveMap)) {
    // if (Util.isTrue(1, 2)) {
    // this.leaveMap();
    // } else {
    // this.leaveMapNew();
    // }
    // }
    // if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
    // st = System.currentTimeMillis();
    // timeLeaveMap = Util.nextInt(300000, 900000);
    // } else if (this.zone == null) {
    // this.changeToTypeNonPK();
    // }
    // }
    // @Override
    // public void joinMap() {
    // if (this.data == null || this.currentLevel < 0 || this.currentLevel >=
    // this.data.length || this.data[this.currentLevel] == null) {
    // System.err.println("Error: BossData not correctly initialized for BlackGoku
    // at level " + this.currentLevel);
    // return;
    // }
    // this.name = this.data[this.currentLevel].getName() + " " + Util.nextInt(1,
    // 100);
    // super.joinMap();
    // st = System.currentTimeMillis();
    // timeLeaveMap = Util.nextInt(600000, 900000);
    // }
    @Override
    public void active() {
        super.active(); // To change body of generated methods, choose Tools | Templates.
        if (Util.canDoWithTime(st, 1900000)) {
            restByTimeout = true;
            this.currentLevel = this.data.length - 1;
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

    @Override
    public void joinMap() {
        super.joinMap(); // To change body of generated methods, choose Tools |Templates.
        st = System.currentTimeMillis();
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }
                this.playerSkill.skillSelect = this.playerSkill.skills
                        .get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
                int dis = Util.getDistance(this, pl);
                if (dis > 450) {
                    move(pl.location.x - 24, pl.location.y);
                } else if (dis > 100) {
                    int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                    int move = Util.nextInt(50, 100);
                    move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
                } else {
                    if (Util.isTrue(30, 100)) {
                        int move = Util.nextInt(50);
                        move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                    }
                    SkillService.gI().useSkill(this, pl, null, -1, null);
                    checkPlayerDie(pl);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
