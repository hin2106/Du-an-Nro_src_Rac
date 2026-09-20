package mob;

import utils.TimeUtil;

public class BigBoss extends Mob {

    public int action = 0;

    public long lastBigBossAttackTime;

    public BigBoss(Mob mob) {
        super(mob);
    }

    @Override
    public void update() {
        if (this.zone == null || this.point == null || this.location == null) {
            return;
        }
        if (this.effectSkill == null) {
            this.effectSkill = new MobEffectSkill(this);
        }
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        effectSkill.update();
        attack();
    }
}
