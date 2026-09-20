package boss.chill;

import boss.BossID;
import boss.BossesData;
import consts.ConstPlayer;
import player.Player;
import services.EffectSkillService;
import services.TaskService;
import utils.Util;

import boss.Boss;

public class Chill extends Boss {

    public Chill() throws Exception {
        super(BossID.CHILL, BossesData.CHILL_1, BossesData.CHILL_2);
    }

    @Override
    public void reward(Player plKill) {

        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage / 2, plAtt);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 4;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(resolveKiller(plAtt));
            }
            return Util.maxIntValue(damage);
        } else {
            return 0;
        }
    }

}
