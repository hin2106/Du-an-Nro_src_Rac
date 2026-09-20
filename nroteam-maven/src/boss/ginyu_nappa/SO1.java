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

public class SO1 extends Boss {

    private long st;

    public SO1() throws Exception {
        super(BossID.SO_1, false, true, BossesData.SO_1);
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
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
        }
        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null || this.parentBoss.bossAppearTogether == null
                || this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel] == null) {
            this.changeStatus(BossStatus.LEAVE_MAP);
            return;
        }
        Boss so2 = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][0];

        if (so2 != null && so2.isDie()) {
            if (so2.bossStatus == BossStatus.LEAVE_MAP || so2.zone == null) {
                this.changeStatus(BossStatus.LEAVE_MAP);
            } else if (so2.bossStatus == BossStatus.CHAT_E || so2.bossStatus == BossStatus.AFK) {
                this.changeStatus(BossStatus.LEAVE_MAP);
                if (so2.bossStatus == BossStatus.AFK || so2.bossStatus == BossStatus.CHAT_E) {
                    so2.changeStatus(BossStatus.LEAVE_MAP);
                }
            } else {
                this.changeStatus(BossStatus.AFK);
            }
        } else {
            this.changeStatus(BossStatus.AFK);
        }
    }


}
