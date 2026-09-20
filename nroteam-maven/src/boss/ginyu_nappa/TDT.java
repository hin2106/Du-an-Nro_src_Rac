package boss.ginyu_nappa;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import java.util.Random;
import map.ItemMap;
import player.Player;
import services.EffectSkillService;
import services.Service;
import services.TaskService;
import utils.Logger;
import utils.Util;

public class TDT extends Boss {

    private long st;

    private long lastBodyChangeTime;

    public TDT() throws Exception {
        super(BossID.TIEU_DOI_TRUONG, false, true, BossesData.TIEU_DOI_TRUONG);
    }

    private void bodyChangePlayerInMap() {
        if (this.zone != null) {
            for (Player pl : this.zone.getPlayers()) {
                if (Util.isTrue(1, 3) && pl.effectSkill != null && !pl.effectSkill.isBodyChangeTechnique) {
                    EffectSkillService.gI().setIsBodyChangeTechnique(pl);
                }
            }
        }
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, Util.nextInt(20000, 30001),
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));

        if (Util.isTrue(80, 100)) {
            int[] items = Util.isTrue(50, 100) ? new int[] { 18, 19, 20 }
                    : new int[] { 1066, 1067, 1068, 1069, 1070, 1229 };
            int randomItem = items[new Random().nextInt(items.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, randomItem, 1,
                    this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
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
    public void attack() {
        if (Util.canDoWithTime(lastBodyChangeTime, 10000)) {
            bodyChangePlayerInMap();
            this.chat("Úm ba la xì bùa");
            this.lastBodyChangeTime = System.currentTimeMillis();
        }
        super.attack();
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
