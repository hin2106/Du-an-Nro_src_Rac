package boss.ginyu_nappa;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import java.util.Random;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import utils.Util;

public class SO3 extends Boss {

    private long st;

    public SO3() throws Exception {
        super(BossID.SO_3, false, true, BossesData.SO_3);
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) {
            return;
        }
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // Rơi vàng (ID 190)
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, Util.nextInt(20000, 30001),
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));

        // 50% xác suất rơi thêm vật phẩm
        if (Util.isTrue(80, 100)) {
            int[] items = Util.isTrue(50, 100) ? new int[] { 18, 19, 20 }
                    : new int[] { 1066, 1067, 1068, 1069, 1070, 1229 };
            int randomItem = items[new Random().nextInt(items.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, randomItem, 1,
                    this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
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
            // Đảm bảo SO1 respawn và join map trước khi active
            if (so1.zone == null) {
                so1.changeStatus(BossStatus.RESPAWN);
            } else {
                so1.changeStatus(BossStatus.ACTIVE);
            }
        }
    }

}
