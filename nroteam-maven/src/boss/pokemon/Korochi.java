
package boss.pokemon;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import consts.ConstPlayer;
import java.util.Random;
import map.ItemMap;
import player.Player;
import services.Service;
import services.TaskService;
import services.player.PlayerService;
import utils.Util;


public class Korochi extends Boss {

    public boolean callMushashi;

    public Korochi() throws Exception {
        super(BossID.KOROCHI, BossesData.KOROCHI);
    }

    @Override
    public void reward(Player plKill) {
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, 190, Util.nextInt(20000, 30001),
                this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
        if (Util.isTrue(80, 100)) {
            int[] items = Util.isTrue(50, 100) ? new int[]{18, 19, 20} : new int[]{1066, 1067, 1068, 1069, 1070, 1229};
            int randomItem = items[new Random().nextInt(items.length)];
            Service.gI().dropItemMap(this.zone, new ItemMap(this.zone, randomItem, 1,
                    this.location.x, this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id));
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

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }
    private long st;

    @Override
    public void doneChatS() {
        if (this.bossAppearTogether != null && this.bossAppearTogether[this.currentLevel] != null) {
            for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                if (boss.id == BossID.MUSHASHI || boss.id == BossID.MEOWTH) {
                    boss.changeStatus(BossStatus.AFK);
                }
            }
        }
        this.changeStatus(BossStatus.AFK);
        if (Korochi.this.zone != null) {
            if (Korochi.this.bossAppearTogether != null && Korochi.this.bossAppearTogether[Korochi.this.currentLevel] != null) {
                for (Boss boss : Korochi.this.bossAppearTogether[Korochi.this.currentLevel]) {
                    if (boss.id == BossID.MUSHASHI || boss.id == BossID.MEOWTH) {
                        boss.changeStatus(BossStatus.ACTIVE);
                    }
                }
            }
            Korochi.this.changeStatus(BossStatus.ACTIVE);
        }
    }

    @Override
    public void doneChatE() {
        if (this.parentBoss == null) {
            return;
        }
        this.parentBoss.changeStatus(BossStatus.ACTIVE);
    }
}
