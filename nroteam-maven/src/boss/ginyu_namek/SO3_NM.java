package boss.ginyu_namek;


import boss.Boss;
import boss.BossID;
import consts.BossStatus;
import java.util.List;
import boss.BossesData;
import item.Item;
import map.ItemMap;
import player.Player;
import services.ItemService;
import services.Service;
import utils.Util;

public class SO3_NM extends Boss {

    private long st;

    public SO3_NM() throws Exception {
        super(BossID.SO_3_NM, false, true, BossesData.SO_3_NM);
    }


   @Override
    public void reward(Player plKill) {
        Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 2), this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        for (int i = 0; i < Util.nextInt(2); i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 3), this.location.x + i * Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
        for (int i = 0; i < Util.nextInt(3, 3); i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 4), this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), -1));
        }
        for (int i = 1; i < Util.nextInt(3, 4) + 1; i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 5), this.location.x - i * 10, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
        short itTemp = 430;
         short nr6s = 19;
        short nr7s = 20;
        ItemMap it = new ItemMap(zone, itTemp, 1, this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        ItemMap it1 = new ItemMap(zone, nr6s, 1, this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        ItemMap it2 = new ItemMap(zone, nr7s, 1, this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id); 
       
        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }
       Service.gI().dropItemMap(this.zone, it);
        Service.gI().dropItemMap(this.zone, it1);
        Service.gI().dropItemMap(this.zone, it2);
    }


    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    protected void notifyJoinMap() {
        // Luôn gửi thông báo khi xuất hiện, không bỏ qua
        super.notifyJoinMap();
    }

    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            return;
        }
        // SO3 die → gọi cả SO2 (index 0) và SO1 (index 1)
        Boss so2 = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][0];
        Boss so1 = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][1];

        if (so2 != null && !so2.isDie()) {
            // Đảm bảo SO2 respawn và join map trước khi active
            if (so2.zone == null) {
                so2.changeStatus(BossStatus.RESPAWN);
            } else {
                so2.changeStatus(BossStatus.ACTIVE);
            }
        }
        if (so1 != null && !so1.isDie()) {
            if (so1.zone == null) {
                so1.changeStatus(BossStatus.RESPAWN);
            } else {
                so1.changeStatus(BossStatus.ACTIVE);
            }
        }
    }

}
