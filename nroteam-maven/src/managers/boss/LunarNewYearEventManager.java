package managers.boss;

import boss.Boss;
import java.util.List;
public class LunarNewYearEventManager extends BossManager {

    private static LunarNewYearEventManager instance;

    public static LunarNewYearEventManager gI() {
        if (instance == null) {
            instance = new LunarNewYearEventManager();
        }
        return instance;
    }

    public void removeAll() {
        int count = 0;
        synchronized (this.bosses) {
            for (int i = this.bosses.size() - 1; i >= 0; i--) {
                Boss b = this.bosses.get(i);
                if (b == null) {
                    continue;
                }
                try {
                    if (b.zone != null) {
                        try {
                            b.changeStatus(consts.BossStatus.REST);
                            services.map.ChangeMapService.gI().exitMap(b);
                        } catch (Exception ignored) {
                        }
                    }
                    this.bosses.remove(i);
                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
