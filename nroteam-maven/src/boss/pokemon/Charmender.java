package boss.pokemon;

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import consts.BossStatus;
import item.Item;
import player.Player;
import services.Service;
import services.ItemService;
import services.FlagBagService;
import services.player.InventoryService;
import utils.Util;

public class Charmender extends Boss {

    private long st;

    public Charmender() throws Exception {
        super(BossID.CHARMENDER, BossesData.CHARMENDER);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (Util.canDoWithTime(st, 900000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }
    
    @Override
    public synchronized double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            if (damage >= 50000) {
                damage = 50000;
            }
            this.nPoint.subHP(damage);
            return (int) damage;
        } else {
            return 0;
        }
    }
    
    @Override
    public void die(player.Player plKill) {
        try {
            if (plKill != null && plKill.isPl()) {
                Item pokeBall = InventoryService.gI().findItemBag(plKill, 1813);
                if (pokeBall == null || !pokeBall.isNotNullItem() || pokeBall.quantity <= 0) {
                    Service.gI().sendThongBao(plKill, "Thiếu Pokéball để thu phục!");
                } else {
                    InventoryService.gI().subQuantityItemsBag(plKill, pokeBall, 1);
                    InventoryService.gI().sendItemBags(plKill);
                    boolean success = Util.isTrue(100, 100);
                    if (!success) {
                        Service.gI().sendThongBao(plKill, "Thu phục thất bại");
                    } else {
                        if (services.FlagBagService.gI().getFlagBag(149) != null) {
                            FlagBagService.gI().sendIconEffectFlag(plKill, 149);
                        }
                        Service.gI().sendBigMessage(plKill, 1139, "Thu phục đang thực hiện...");
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                                if (InventoryService.gI().getCountEmptyBag(plKill) > 0) {
                                    Item reward = ItemService.gI().createNewItem((short) 1803);
                                    reward.quantity = 1;
                                    try {
                                        reward.itemOptions.add(new Item.ItemOption(0, Util.nextInt(2000, 5000)));
                                        reward.itemOptions.add(new Item.ItemOption(22, Util.nextInt(2000, 5000))); 
                                        reward.itemOptions.add(new Item.ItemOption(23, Util.nextInt(2000, 5000)));
                                        reward.itemOptions.add(new Item.ItemOption(30, 0));
                                        reward.itemOptions.add(new Item.ItemOption(93, Util.nextInt(0, 30)));
                                    } catch (Exception ignored) {}
                                    InventoryService.gI().addItemBag(plKill, reward);
                                    InventoryService.gI().sendItemBags(plKill);
                                    Service.gI().sendThongBao(plKill, "Bạn đã thu phục được Charmender!");
                                } else {
                                    Service.gI().sendThongBao(plKill, "Hành trang đầy, không thể nhận vật phẩm.");
                                }
                            } catch (InterruptedException ignored) {}
                        }).start();
                    }
                }
            }
        } catch (Exception ignored) {}
        super.die(plKill);
    }
}
