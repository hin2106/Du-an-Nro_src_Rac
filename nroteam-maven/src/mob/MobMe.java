package mob;
import map.Zone;
import player.Player;
import utils.SkillUtil;
import services.Service;
import utils.Util;
import network.Message;

public final class MobMe extends Mob {

    private Player player;
    private final long lastTimeSpawn;
    private final int timeSurvive;

    public MobMe(Player player) {
        super();
        this.player = player;
        this.id = (int) player.id;
        int level = player.playerSkill.getSkillbyId(12).point;
        this.tempId = SkillUtil.getTempMobMe(level);
        this.point.maxHp =  Math.min(SkillUtil.getHPMobMe(player.nPoint.hpMax, level), 2_000_000_000);
        this.point.dame =  Math.min(SkillUtil.getHPMobMe(player.nPoint.getDameAttack(false), level), 2_000_000_000);
        this.point.hp = this.point.maxHp;
        this.zone = player.zone;
        this.lastTimeSpawn = System.currentTimeMillis();
        this.timeSurvive = SkillUtil.getTimeSurviveMobMe(level);
        spawn();
    }

    public MobMe(Player player, int customTempId, int damagePercent) {
        super();
        this.player = player;
        this.id = (int) player.id;
        this.tempId = customTempId;
        int level = player.playerSkill.getSkillbyId(12) != null ? player.playerSkill.getSkillbyId(12).point : 1;
        this.point.maxHp = Math.min(SkillUtil.getHPMobMe(player.nPoint.hpMax, level), 2_000_000_000);
        this.point.dame = Math.min((long)(player.nPoint.getDameAttack(false) * damagePercent / 100.0), 2_000_000_000);
        this.point.hp = this.point.maxHp;
        this.zone = player.zone;
        this.lastTimeSpawn = System.currentTimeMillis();
        this.timeSurvive = 360000;
        spawn();
    }

    @Override
    public void update() {
        if (Util.canDoWithTime(lastTimeSpawn, timeSurvive) && this.player.setClothes.pikkoroDaimao != 5) {
            this.mobMeDie();
            this.dispose();
        }
    }

    public void attack(Player pl, Mob mob, boolean miss) {
        Message msg;
        try {
            if (pl != null) {
                long dame = !miss ? this.point.dame : 0;
                if ((pl.nPoint.hp > dame && pl.nPoint.hp > pl.nPoint.hpMax * 0.05) || this.player.setClothes.pikkoroDaimao == 5) {
                    double dameHit = pl.injured(this.player, dame, true, true);
                    msg = new Message(-95);
                    msg.writer().writeByte(2);
                    msg.writer().writeInt(this.id);
                    msg.writer().writeInt((int) pl.id);
                    msg.writeSmartLong(dameHit);
                    msg.writeSmartLong(pl.nPoint.hp);
                    Service.gI().sendMessAllPlayerInMap(this.player, msg);
                    msg.cleanup();
                }
            }

            if (mob != null && !mob.isDie()) {
                long actualDame = Math.min(this.point.dame, mob.point.gethp());
                long tnsm = mob.getTiemNangForPlayer(this.player, actualDame);
                msg = new Message(-95);
                msg.writer().writeByte(3);
                msg.writer().writeInt(this.id);
                msg.writer().writeInt((int) mob.id);
                mob.point.sethp(mob.point.gethp() - actualDame);
                msg.writeSmartLong(mob.point.gethp());
                msg.writeSmartLong(actualDame);
                Service.gI().sendMessAllPlayerInMap(this.player, msg);
                msg.cleanup();
                Service.gI().addSMTN(player, (byte) 2, tnsm, true);
            }
        } catch (Exception e) {
        }
    }

    public void spawn() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(0);//type
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(this.tempId);
            msg.writeSmartLong(this.point.hp);// hp mob
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void goToMap(Zone zone) {
        if (zone != null) {
            this.removeMobInMap();
            this.zone = zone;
        }
    }

    private void removeMobInMap() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(7);//type
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void mobMeDie() {
        Message msg;
        try {
            msg = new Message(-95);
            msg.writer().writeByte(6);//type
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    @Override
    public synchronized void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        Message msg;
        try {
            if (damage > point.maxHp / 20) {
                damage = point.maxHp / 20;
            }
            point.hp -= damage;
            msg = new Message(-95);
            msg.writer().writeByte(5);//type
            msg.writer().writeInt((int) plAtt.id);
            msg.writer().writeByte(plAtt.playerSkill.skillSelect.template.id); // id skill
            msg.writer().writeInt(id); //mob id
            msg.writer().writeInt((int) damage);
            msg.writeSmartLong(point.hp);
            Service.gI().sendMessAllPlayerInMap(this.player, msg);
            msg.cleanup();
            if (isDie()) {
                mobMeDie();
                dispose();
            }
        } catch (Exception e) {
        }
    }

    public void dispose() {
        player.mobMe = null;
        this.player = null;
    }
}
