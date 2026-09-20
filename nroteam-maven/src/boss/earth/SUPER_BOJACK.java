package boss.earth;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import item.Item;
import java.util.List;
import map.ItemMap;
import services.map.MapService;
import map.Zone;
import player.Player;
import services.ItemService;
import services.Service;
import utils.TimeUtil;
import utils.Util;
import managers.boss.BossManager;

public class SUPER_BOJACK extends Boss {

    private long st;

    public SUPER_BOJACK() throws Exception {
        super(BossID.SUPER_BOJACK, false, true, BossesData.SUPER_BOJACK_2);
    }

    @Override
    public void moveTo(int x, int y) {
        if (this.currentLevel == 1) return;
        super.moveTo(x, y);
    }

    @Override
    public void reward(Player plKill) {
        // Drop cơ bản
        Service.gI().dropItemMap(this.zone,
                new ItemMap(zone, 190, Util.nextInt(1, 10),
                        this.location.x + Util.nextInt(-50, 50),
                        this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                        plKill.id));

        for (int i = 0; i < Util.nextInt(2); i++) {
            Service.gI().dropItemMap(this.zone,
                    new ItemMap(zone, 821, Util.nextInt(1, 3),
                            this.location.x + i * Util.nextInt(-50, 50),
                            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                            plKill.id));
        }

        // Drop tiền lớn
        for (int i = 0; i < Util.nextInt(3, 15); i++) {
            Service.gI().dropItemMap(this.zone,
                    new ItemMap(zone, 190, Util.nextInt(1_000_000, 20_000_000),
                            this.location.x + i * 10,
                            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                            plKill.id));
        }
        for (int i = 1; i < Util.nextInt(3, 15) + 1; i++) {
            Service.gI().dropItemMap(this.zone,
                    new ItemMap(zone, 190, Util.nextInt(1_000_000, 20_000_000),
                            this.location.x - i * 10,
                            this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                            plKill.id));
        }

        // Drop item đặc biệt
        short itTemp = 428;
        ItemMap it = new ItemMap(zone, itTemp, 1,
                this.location.x + Util.nextInt(-50, 50),
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24),
                plKill.id);

        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) it.options = ops;

        Service.gI().dropItemMap(this.zone, it);
    }
    

    @Override
    protected void notifyJoinMap() {
        if (this.currentLevel == 1) return;
        super.notifyJoinMap();
    }

    @Override
    public void joinMap() {
        if (!TimeUtil.isBojackOpen()) {
           // System.out.println("❌ Ngoài khung giờ, Bojack không thể xuất hiện!");
            this.leaveMapNew();
            return;
        }
        super.joinMap();
        st = System.currentTimeMillis();
        //System.out.println("🔥 [SUPER_BOJACK] Bojack đã xuất hiện trong khung giờ hợp lệ!");
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapNew();
           // System.out.println("🕒 [SUPER_BOJACK] Bojack tự rời map sau 15 phút.");
        }

        if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
            st = System.currentTimeMillis();
        }

        if (!TimeUtil.isBojackOpen()) {
            //System.out.println("🕒 [SUPER_BOJACK] Hết khung giờ xuất hiện — Bojack sẽ biến mất!");
            this.leaveMapNew();
        }
    }

    @Override
    public void die(Player plKill) {
        super.die(plKill);
       // System.out.println("💀 [SUPER_BOJACK] đã bị hạ. Chuẩn bị spawn BOJACK thường...");

        try {
            // Lấy hoặc tạo BOJACK thường
            BossManager bossManager = BossManager.gI();
            Boss bojack = bossManager.getBossByIdIncludingDead(BossID.BOJACK);
            if (bojack != null && bojack.isDie()) {
                bossManager.removeAllByBossId(BossID.BOJACK);
                bojack = null;
            }
            if (bojack == null) {
                bojack = bossManager.createBoss(BossID.BOJACK);
            }

            // Spawn an toàn
            if (bojack != null && bojack.zone == null && bojack.data != null && bojack.data.length > 0) {
                int[] mapJoin = bojack.data[0].getMapJoin();
                if (mapJoin != null && mapJoin.length > 0) {
                    int randomMapId = mapJoin[Util.nextInt(0, mapJoin.length - 1)];
                    Zone spawnZone = MapService.gI().getMapWithRandZone(randomMapId);
                    if (spawnZone != null) {
                        bojack.zoneFinal = spawnZone;
                        bojack.changeStatus(BossStatus.RESPAWN);
                        //System.out.println("🔥 BOJACK thường đã xuất hiện tại zone " );
                    } else {
                       // System.out.println("⚠️ BOJACK chưa tìm thấy zone hợp lệ, không thể spawn.");
                    }
                } else {
                   // System.out.println("⚠️ BOJACK chưa có map hợp lệ, không thể spawn.");
                }
            } else {
               // System.out.println("⚠️ BOJACK đang tồn tại hoặc đang trên map.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
