package boss.baotri;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import map.ItemMap;
import player.Player;
import services.Service;
import utils.Util;
import java.util.Calendar;

public class Mai extends Boss {

    public Mai() throws Exception {
        super(BossID.MAI, BossesData.MAI);
    }

//    @Override
//    public void reward(Player plKill) {
//        short itTemp = 636;
//        ItemMap it = new ItemMap(zone, itTemp, 1, this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
//        it.options.add(new Item.ItemOption(50, Util.nextInt(10, 10)));
//        it.options.add(new Item.ItemOption(148, Util.nextInt(33, 33)));
//        it.options.add(new Item.ItemOption(101, Util.nextInt(10, 10)));
//        it.options.add(new Item.ItemOption(151, Util.nextInt(1, 1)));
//        it.options.add(new Item.ItemOption(152, Util.nextInt(1, 1)));
//        it.options.add(new Item.ItemOption(108, Util.nextInt(2, 40)));
//        it.options.add(new Item.ItemOption(93, 7));
//        it.options.add(new Item.ItemOption(30, Util.nextInt(1, 1)));
//        Service.gI().dropItemMap(this.zone, it);
//    }

    @Override
    public void autoLeaveMap() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (currentHour != 4) {
            this.leaveMapNew();
            return;
        }
        
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (currentHour == 4) {
            super.joinMap();
            st = System.currentTimeMillis();
        } else {
            this.leaveMapNew();
        }
    }
    
    private long st;

    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            damage = 1;

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
}