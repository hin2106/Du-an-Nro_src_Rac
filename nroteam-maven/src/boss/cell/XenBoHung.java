package boss.cell;
import consts.ConstPlayer;
import boss.Boss;
import boss.BossesData;
import boss.BossID;
import item.Item;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.ItemService;
import services.Service;
import services.TaskService;
import utils.Util;

public class XenBoHung extends Boss {


    public XenBoHung() throws Exception {
        super(BossID.XEN_BO_HUNG, BossesData.XEN_BO_HUNG_1, BossesData.XEN_BO_HUNG_2, BossesData.XEN_BO_HUNG_3);
    }

    @Override
    public void reward(Player plKill) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        int drop = 190;
        int quantity = Util.nextInt(20000, 30000);
        ItemMap itemMap = new ItemMap(this.zone, drop, quantity, x, y, plKill.id);
        Service.gI().dropItemMap(zone, itemMap);

        if (Util.isTrue(20, 100)) {
            int group = Util.nextInt(1, 100) <= 70 ? 0 : 1;

            int[][] drops = {
                    { 230, 231, 232, 234, 235, 236, 238, 239, 240, 242, 243, 244, 246, 247, 248, 250, 251, 252, 266,
                            267, 268, 270, 271, 272, 274, 275, 276 }, // Áo Quần Giày
                    { 254, 255, 256, 258, 259, 260, 262, 263, 264, 278, 279, 280 } // Găng Rada
            };

            int dropOptional = drops[group][Util.nextInt(0, drops[group].length - 1)];

            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, 1, x, y, plKill.id);
            List<Item.ItemOption> optionalOps = ItemService.gI().getListOptionItemShop((short) dropOptional);
            optionalOps.forEach(option -> option.param = (int) (option.param * Util.nextInt(100, 115) / 100.0));
            optionalItemMap.options.addAll(optionalOps);

            int rand = Util.nextInt(1, 100);
            int value = 0;
            if (rand <= 80) {
                value = Util.nextInt(1, 3);
            } else if (rand <= 97) {
                value = Util.nextInt(4, 5);
            } else {
                value = 6;
            }
            optionalItemMap.options.add(new Item.ItemOption(107, value));

            Service.gI().dropItemMap(zone, optionalItemMap);
        }

        if (Util.isTrue(80, 100)) {
            int[] dropItems = { 16, 17, 1150, 1151, 1152, 1152, 1066, 1067, 1068, 1069, 1070, 1229 };
            int dropOptional = dropItems[Util.nextInt(0, dropItems.length - 1)];
            ItemMap optionalItemMap = new ItemMap(this.zone, dropOptional, Util.nextInt(1, 3), x, y, plKill.id);
            Service.gI().dropItemMap(zone, optionalItemMap);
        }

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
            return Util.maxIntValue(damage) ;
        } else {
            return 0;
        }
    }

}
