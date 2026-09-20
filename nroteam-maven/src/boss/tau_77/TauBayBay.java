package boss.tau_77;


import boss.Boss;
import boss.BossesData;
import static consts.BossType.FINAL;
import player.Player;
import services.EffectSkillService;
import services.Service;
import utils.Util;

public class TauBayBay extends Boss {

    public TauBayBay() throws Exception {
        super(FINAL, Util.randomBossId(), BossesData.TAU_PAY_PAY_DONG_NAM_KARIN);
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1)) {
                this.chat("Xí hụt");
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage, plAtt);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (damage >= 100) {
                damage = 100;
            }
            this.nPoint.dame = (int) damage / Util.nextInt(500, 1000);
            this.nPoint.subHP(damage);
            long tnSm = (int) (damage * Util.nextInt(20, 50));
            if (tnSm > 10000000) {
                tnSm = 10000000 - Util.nextInt(1000000);
            }
            if (plAtt.nPoint.power >= 1_500_000) {
                tnSm = Util.nextInt(1);
            }
            Service.gI().addSMTN(plAtt, (byte) 2, tnSm, true);
            if (isDie()) {
                this.setDie(plAtt);
                die(resolveKiller(plAtt));
            }
            return (int) damage;
        } else {
            return 0;
        }
    }
}
