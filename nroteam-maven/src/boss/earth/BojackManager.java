package boss.earth;

import boss.Boss;
import boss.BossID;
import consts.BossStatus;
import managers.boss.BossManager;
import utils.TimeUtil;

public class BojackManager implements Runnable {

    private static final BojackManager instance = new BojackManager();
    private boolean wasOpen = false; 

    public static BojackManager gI() {
        return instance;
    }

    private BojackManager() {}

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000); // kiểm tra mỗi 10 giây
                boolean isOpen = TimeUtil.isBojackOpen();
                if (isOpen) {
                    if (!wasOpen) {
                        wasOpen = true;

                        BossManager bossManager = BossManager.gI();
                        Boss boss = bossManager.getBossByIdIncludingDead(BossID.SUPER_BOJACK);
                        if (boss != null && boss.isDie()) {
                            bossManager.removeAllByBossId(BossID.SUPER_BOJACK);
                            boss = null;
                        }
                        if (boss == null) {
                            boss = bossManager.createBoss(BossID.SUPER_BOJACK);
                        }
                        if (boss != null && boss.zone == null) {
                            boss.changeStatus(BossStatus.RESPAWN);
                        }
                    }

                } else {
                    if (wasOpen) {
                        wasOpen = false;
                    }
                    BossManager.gI().removeAllByBossId(BossID.SUPER_BOJACK);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
