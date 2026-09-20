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

public class SO4_NM extends Boss {

    private long st;

    public SO4_NM() throws Exception {
        super(BossID.SO_4_NM, false, true, BossesData.SO_4_NM);
    }


@Override
public void reward(Player plKill) {
    int x = this.location.x;
    int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

    for (int i = 0; i < Util.nextInt(1, 2); i++) {
        Service.gI().dropItemMap(zone, new ItemMap(zone, 77, Util.nextInt(1, 3), x + Util.nextInt(-50, 50), y, -1));
    }
    for (int i = 0; i < Util.nextInt(3, 4); i++) {
        Service.gI().dropItemMap(zone, new ItemMap(zone, 77, Util.nextInt(1, 4), x + i * 10, y, -1));
    }
    for (int i = 1; i < Util.nextInt(3, 3) + 1; i++) {
        Service.gI().dropItemMap(zone, new ItemMap(zone, 77, Util.nextInt(1, 5), x - i * 10, y, -1));
    }

    short itTemp = 429;
    short nr6s = 19;
    short nr7s = 20;

    ItemMap it = new ItemMap(zone, itTemp, 1, x + Util.nextInt(-50, 50), y, -1);
    ItemMap it1 = new ItemMap(zone, nr6s, 1, x + Util.nextInt(-50, 50), y, -1);
    ItemMap it2 = new ItemMap(zone, nr7s, 1, x + Util.nextInt(-50, 50), y, -1);

    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
    if (!ops.isEmpty()) {
        it.options = ops;
    }

    Service.gI().dropItemMap(zone, it);
    Service.gI().dropItemMap(zone, it1);
    Service.gI().dropItemMap(zone, it2);
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
        Boss so3 = this.parentBoss.bossAppearTogether[this.parentBoss.currentLevel][2];
        if (so3 != null && !so3.isDie()) {
            if (so3.zone == null) {
                so3.changeStatus(BossStatus.RESPAWN);
            } else {
                so3.changeStatus(BossStatus.ACTIVE);
            }
        }
    }
}
