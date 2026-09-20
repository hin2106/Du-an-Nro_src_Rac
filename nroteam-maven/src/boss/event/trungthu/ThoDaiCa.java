package boss.event.trungthu;
import boss.BossID;
import boss.BossesData;
import boss.Boss;
import consts.ConstPlayer;
import static consts.BossType.TRUNGTHU_EVENT;
import item.Item;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.Service;
import services.SkillService;
import utils.Util;

public class ThoDaiCa extends Boss {

    private long lastTimeMove;
    private int timeMove;
    private static final int CARROT_RATE = 100;

    public ThoDaiCa() throws Exception {
        super(TRUNGTHU_EVENT, BossID.THO_DAI_CA, true, true, BossesData.THO_DAI_CA);
    }

    @Override
    public void reward(Player plKill) {
        int dropCount = Util.nextInt(3, 7);
        for (int i = 0; i < dropCount; i++) {
            int x = this.location.x + Util.nextInt(-30, 30);
            int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
            ItemMap it = new ItemMap(this.zone, 670, 1, x, y, plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        int goldPiles = Util.nextInt(2, 4);
        for (int i = 0; i < goldPiles; i++) {
            int x = this.location.x + Util.nextInt(-40, 40);
            int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
            int[] goldItemIds = new int[]{188, 189, 190};
            int goldItemId = goldItemIds[Util.nextInt(0, goldItemIds.length - 1)];
            int qty = Util.nextInt(5, 50);
            ItemMap gold = new ItemMap(this.zone, goldItemId, qty, x, y, plKill.id);
            Service.gI().dropItemMap(this.zone, gold);
        }
    }

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon, 1000)) {
                this.chat("Xí hụt");
                return 0;
            }
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = damage / 1;
            }
            if (!piercing && damage > 1000) {
                damage = 1000;
            }
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(resolveKiller(plAtt));
            }
            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        List<Player> playersMap = this.zone.getNotBosses();
        for (Player pl : playersMap) {
            if (pl == null || pl.isDie() || pl.effectSkill == null) {
                continue;
            }
            if (Util.getDistance(this, pl) <= 35) {
                if (!bienThanhCarot(pl)) {
                    if (!pl.effectSkill.isCarrot && Util.isTrue(CARROT_RATE, 100)) {
                        EffectSkillService.gI().setCarrot(pl, 300000);
                        this.chat("Héhé, " + pl.name + " thành cà rốt rồi!");
                    }
                }
            }
        }
        this.attack();
    }

    @Override
    public void attack() {
        if (this.effectSkill.isCharging) {
            return;
        }
        if (Util.canDoWithTime(this.lastTimeAttack, 500)) {
            this.lastTimeAttack = System.currentTimeMillis();
            Player pl = getPlayerAttack();
            if (pl == null || pl.isDie()) {
                if (Util.canDoWithTime(lastTimeMove, timeMove)) {
                    Player plRand = super.getPlayerAttack();
                    if (plRand != null) {
                        this.moveToPlayer(plRand);
                        this.lastTimeMove = System.currentTimeMillis();
                        this.timeMove = Util.nextInt(3000, 12000);
                    }
                }
                return;
            }
            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            int dis = Util.getDistance(this, pl);
            if (dis > 300) {
                move(pl.location.x - 24, pl.location.y);
            } else if (dis > 80) {
                int dir = (this.location.x - pl.location.x < 0 ? 1 : -1);
                int move = Util.nextInt(40, 80);
                move(this.location.x + (dir == 1 ? move : -move), pl.location.y);
            } else {
                if (Util.isTrue(30, 100)) {
                    int move = Util.nextInt(30);
                    move(pl.location.x + (Util.nextInt(0, 1) == 1 ? move : -move), this.location.y);
                }
                SkillService.gI().useSkill(this, pl, null, -1, null);
                checkPlayerDie(pl);
            }
        }
    }

    private boolean bienThanhCarot(Player pl) {
        int[] rabbitIds = new int[]{406, 407, 408};
        if (pl.inventory != null && pl.inventory.itemsBody != null) {
            for (int i = 0; i < pl.inventory.itemsBody.size(); i++) {
                Item it = pl.inventory.itemsBody.get(i);
                if (it != null && it.isNotNullItem() && it.template != null) {
                    if (it.template.id == 463) {
                        return true;
                    }
                    for (int id : rabbitIds) {
                        if (it.template.id == id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    private long st;

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
    }
}
