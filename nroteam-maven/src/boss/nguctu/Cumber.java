package boss.nguctu;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.ConstPlayer;
import consts.ConstTaskBadges;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.ItemService;
import services.Service;
import task.BadgesTaskService;
import utils.Util;

public class Cumber extends Boss {

    public Cumber() throws Exception {
        super(BossID.CUMBER, BossesData.CUMBER_1, BossesData.CUMBER_2);
    }

    @Override
    public void reward(Player plKill) {
       


         if (Util.isTrue(100, 100)) {
            ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
            if (it != null) {
                Service.gI().dropItemMap(zone, it);
            }

        BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.TRUM_SAN_BOSS, 1);}
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
                damage = damage / 6;
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
