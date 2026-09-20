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
import utils.Logger;
import utils.Util;

public class TDT_NM extends Boss {

    private long st;

    public TDT_NM() throws Exception {
        super(BossID.TIEU_DOI_TRUONG_NM, false, true, BossesData.TIEU_DOI_TRUONG_NM);
    }

    @Override
    public void reward(Player plKill) {
        Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 2), this.location.x + Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        for (int i = 0; i < Util.nextInt(2); i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 3), this.location.x + i * Util.nextInt(-50, 50), this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
        for (int i = 0; i < Util.nextInt(3, 4); i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 4), this.location.x + i * 10, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), -1));
        }
        for (int i = 1; i < Util.nextInt(3, 3) + 1; i++) {
            Service.gI().dropItemMap(this.zone, new ItemMap(zone, 77, Util.nextInt(1, 5), this.location.x - i * 10, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        }
        short itTemp = 433;
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
        if (this.currentLevel == 1) {
            return;
        }
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
        if (this.zone != null && this.zone.map != null) {
            super.notifyJoinMap();
        }
    }

    @Override
    public void joinMap() {
        try {
            super.joinMap();
            st = System.currentTimeMillis();
        } catch (Exception e) {
            this.error++;
            if (this.error < 5) {
                Logger.error("TDT joinMap error: " + e.getMessage() + "\n");
            }
            if (this.error >= 5) {
                this.changeStatus(BossStatus.REST);
            } else {
                this.changeStatus(BossStatus.RESPAWN);
            }
        }
    }

    @Override
    public void die(Player plKill) {
        try {
            if (plKill != null) {
            executeReward(plKill);
            }
            this.changeStatus(BossStatus.DIE);
        } catch (Exception e) {
            Logger.error("TDT die error: " + e.getMessage() + "\n");
            this.changeStatus(BossStatus.DIE);
        }
    }

    @Override
    public void leaveMap() {
        try {
            super.leaveMap();
        } catch (Exception e) {
            Logger.error("TDT leaveMap error: " + e.getMessage() + "\n");
            if (this.zone != null) {
                try {
                    services.map.ChangeMapService.gI().exitMap(this);
                } catch (Exception ex) {
                }
            }
            this.lastZone = null;
            this.lastTimeRest = System.currentTimeMillis();
            this.changeStatus(BossStatus.REST);
        }
    }


    @Override
    public void doneChatS() {
        this.changeStatus(BossStatus.AFK);
    }

    @Override
    public void afk() {
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        Boss so1 = this.bossAppearTogether[this.currentLevel][1];
        Boss so2 = this.bossAppearTogether[this.currentLevel][0];

        if (so1 != null && so2 != null && so1.isDie() && so2.isDie()) {
            boolean so1LeftMap = (so1.bossStatus == BossStatus.LEAVE_MAP || so1.zone == null);
            boolean so2LeftMap = (so2.bossStatus == BossStatus.LEAVE_MAP || so2.zone == null);

            if (so1LeftMap && so2LeftMap) {
                this.changeStatus(BossStatus.ACTIVE);
            }
        }
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
}
