package event.teacherday;

import consts.ConstNpc;
import event.Event;
import item.Item;
import item.Item.ItemOption;
import java.util.List;
import map.ItemMap;
import map.Map;
import map.Zone;
import mob.Mob;
import npc.Npc;
import player.Player;
import services.EffectSkillService;
import services.Service;
import services.func.EffectMapService;
import services.map.MapService;
import services.player.InventoryService;
import utils.Util;

public class BongHong extends Event {

    @Override
    public int eventId() {
        return consts.ConstEvent.SU_KIEN_20_11;
    }

  
    public Zone zone;
    private boolean disabled = false;
    private static Npc currentBongHongNpc = null;

    @Override
    public void init() {
        initNpc();
    }

    @Override
    public void initNpc() {
        spawnBongHongNpc(Util.nextInt(27, 30));
    }

    private void spawnBongHongNpc(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        if (map == null) {
            return;
        }
        int x = Util.nextInt(100, map.mapWidth - 100);
        int y = map.yPhysicInTop(x, 0);
        currentBongHongNpc = new Npc(mapId, 1, x, y, 81, 11811) {
            @Override
            public void openBaseMenu(Player player) {
                if (disabled) {
                    return;
                }
                if (canOpenNpc(player)) {
                    if (InventoryService.gI().findItem(player, 1387)) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Muốn tỉa mình, xem trình bạn thế nào",
                                "Đồng ý", "Từ chối");
                    } else {
                        this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Mang kéo tới rồi nói chuyện với mình", "Đóng");
                    }
                }
            }

            @Override
            public void confirmMenu(Player player, int select) {
                if (!canOpenNpc(player) || player.idMark.getIndexMenu() != ConstNpc.BASE_MENU || select != 0) {
                    return;
                }
                InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 1387), 1);
                InventoryService.gI().sendItemBags(player);
//                Service.gI().sendThongBao(player, "Đang tỉa bông hồng...");
                EffectSkillService.gI().setBlindDCTT(player, System.currentTimeMillis(), 10000);
                EffectSkillService.gI().sendEffectPlayer(player, player, EffectSkillService.TURN_ON_EFFECT, EffectSkillService.BLIND_EFFECT);
                disabled = true;
                int oldMapId = this.map.mapId;
                new Thread(() -> {
                    try {
                        Thread.sleep(10000);
                        if (!Util.isTrue(75, 100)) {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Xí hụt", "Đóng");
                            disabled = false;
                            return;
                        }
                        if (this.map != null) {
                            Service.gI().sendHideNpc(player, this.tempId, true);
                            EffectMapService.gI().sendEffectMapToAllInMap(player.zone, 241, 1, 1, this.cx, this.cy, 5);
                            this.map.npcs.removeIf(npc -> npc.equals(this));
                        }
                        currentBongHongNpc = null;
                        ItemMap itemMap = new ItemMap(player.zone, 1388, 1, player.location.x, player.location.y, player.id);
                        itemMap.options.add(new ItemOption(30, 0));
                        itemMap.options.add(new ItemOption(174, 2025));
                        Service.gI().dropItemMap(player.zone, itemMap);
                        int newMapId = Util.nextInt(27, 30);
                        if (newMapId == oldMapId) {
                            newMapId = (oldMapId == 30) ? 27 : oldMapId + 1;
                        }
                        disabled = false;
                        spawnBongHongNpc(newMapId);
                    } catch (InterruptedException e) {
                    }
                }).start();
            }
        };
        map.addNpc(currentBongHongNpc);
    }

    @Override
    public void initMap() {
    }

    @Override
    public void dropItem(Player player, Mob mob, List<ItemMap> list, int x, int yEnd) {
    }

    @Override
    public boolean useItem(Player player, Item item) {
        return false;
    }
}
