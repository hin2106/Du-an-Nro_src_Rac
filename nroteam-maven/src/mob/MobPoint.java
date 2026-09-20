package mob;import utils.Util;

public class MobPoint {

    public final Mob mob;
    public long hp;
    public long maxHp;
    public long dame;

    public MobPoint(Mob mob) {
        this.mob = mob;
    }

    public long getHpFull() {
        return maxHp;
    }

    public void setHpFull(long hp) {
        maxHp = Math.max(0, hp);
    }

    public long gethp() {
        return hp;
    }

    public void sethp(long hp) {
        this.hp = Math.max(0, hp);
    }

    public long getDame() {
        return dame;
    }

    public void setDame(long dame) {
        this.dame = Math.max(0, dame);
    }


    public long getHpAsInt() {
        return Util.maxIntValue(hp);
    }

    public long getHpFullAsInt() {
        return Util.maxIntValue(maxHp);
    }


    public long getDameAsInt() {
        return Util.maxIntValue(dame);
    }

    public long getDameAttack() {
        if (this.dame > 0) {
            long randDame = this.dame + Util.nextInt(-(int) (this.dame / 100), (int) (this.dame / 100));
            return Util.maxIntValue(randDame);
        } else {
            long base = (this.getHpFull() * Util.nextInt(mob.pDame - 1, mob.pDame + 1) / 100)
                    + Util.nextInt(-(mob.level * 10), mob.level * 10);
            return Util.maxIntValue(base);
        }
    }
}
