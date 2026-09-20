package services.events;

import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.ItemService;
import services.Service;
import services.map.NpcManager;
import server.Manager;
import consts.ConstEvent;

public final class HalloweenMenuHook {

    private HalloweenMenuHook() {
    }

    private static final int MENU_INDEX = Npc.MENU_CANDY_PROMPT;

    private static final java.util.Map<Long, String> CLEARED_BOOT = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean interceptOpenNpc(Player player, Npc npc) {
        try {
            if (player == null || npc == null) {
                return false;
            }
            if (Manager.EVENT_SEVER != ConstEvent.SU_KIEN_HALLOWEEN) {
                return false;
            }
            try {
                String boot = Manager.BOOT_TOKEN;
                String marked = CLEARED_BOOT.get(player.id);
                if (marked == null || !marked.equals(boot)) {
                    if (player.candyDeclinedNpcIds != null) {
                        player.candyDeclinedNpcIds.clear();
                    }
                    CLEARED_BOOT.put(player.id, boot);
                }
            } catch (Exception ignored) {
            }
            try {
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String today = df.format(new java.util.Date());
                if (player.lastCandyResetDate == null || !today.equals(player.lastCandyResetDate)) {
                    if (player.candyDeclinedNpcIds != null) {
                        player.candyDeclinedNpcIds.clear();
                    }
                    player.lastCandyResetDate = today;
                }
            } catch (Exception ignored) {
            }
            if (player.nPoint == null || player.nPoint.power < 200_000_000L) {
                return false;
            }

            boolean wearing = false;
            if (player.inventory != null && player.inventory.itemsBody != null) {
                for (item.Item it : player.inventory.itemsBody) {
                    if (it != null && it.isNotNullItem()) {
                        int tid = (it.template != null) ? it.template.id : -1;
                        if (tid == 739 || tid == 742 || tid == 1709) { // các đồ Halloween
                            wearing = true;
                            break;
                        }
                    }
                }
            }

            if (npc.tempId == ConstNpc.DAU_THAN
                    || npc.tempId == ConstNpc.RUONG_DO
                    || npc.tempId == ConstNpc.RUONG_GO
                    || npc.tempId == ConstNpc.QUA_TRUNG) {
                return false;
            }

            if (!wearing) {
                return false;
            }
            if (player.candyDeclinedNpcIds == null) {
                player.candyDeclinedNpcIds = new java.util.HashSet<>();
            }
            if (!player.candyDeclinedNpcIds.contains(npc.tempId)) {
                npc.createOtherMenu(player, MENU_INDEX,
                        "Ổ được rồi, kẹo đây, tha cho ta hahaha",
                        "Từ chối nhận kẹo", "Cho kẹo hay bị ghẹo?", "Đóng");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean handleSelect(Player player, int npcId, int select) {
        try {
            if (player == null || player.idMark == null) {
                return false;
            }
            if (player.idMark.getIndexMenu() != MENU_INDEX) {
                return false;
            }
            Npc npc;
            if (npcId == ConstNpc.CALICK && player.zone.map.mapId != 102) {
                npc = NpcManager.getNpc(ConstNpc.CALICK);
            } else if (npcId == ConstNpc.LY_TIEU_NUONG) {
                npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
            } else {
                npc = player.zone.map.getNpc(player, npcId);
            }
            if (npc == null) {
                return true;
            }
            switch (select) {
                case 0:
                    if (player.candyDeclinedNpcIds == null) {
                        player.candyDeclinedNpcIds = new java.util.HashSet<>();
                    }
                    player.candyDeclinedNpcIds.add(npc.tempId);
                    npc.openBaseMenu(player);
                    return true;
                case 1:
                    try {
                    if (services.player.InventoryService.gI().getCountEmptyBag(player) <= 0) {
                        Service.gI().sendThongBao(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
                    } else {
                        Item candy = ItemService.gI().createNewItem((short) 901);
                        candy.quantity = 1;
                        if (candy.itemOptions != null) {
                            candy.itemOptions.clear();
                        } else {
                            candy.itemOptions = new java.util.ArrayList<>();
                        }
                        candy.itemOptions.add(new item.Item.ItemOption(86, 0));
                        candy.itemOptions.add(new item.Item.ItemOption(93, 35));
                        candy.itemOptions.add(new item.Item.ItemOption(174, 2025));

                        services.player.InventoryService.gI().addItemBag(player, candy);
                        // Safety: enforce options on the instance in bag (in case addItem copies the item)
                        try {
                            for (int idx = player.inventory.itemsBag.size() - 1; idx >= 0; idx--) {
                                item.Item it = player.inventory.itemsBag.get(idx);
                                if (it != null && it.isNotNullItem() && it.template.id == 901) {
                                    // If not exactly our 3 options, reset to required
                                    it.itemOptions = new java.util.ArrayList<>();
                                    it.itemOptions.add(new item.Item.ItemOption(86, 0));
                                    it.itemOptions.add(new item.Item.ItemOption(93, 35));
                                    it.itemOptions.add(new item.Item.ItemOption(174, 2025));
                                    break;
                                }
                            }
                        } catch (Exception ignored2) {
                        }
                        services.player.InventoryService.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được Kẹo bàn tay");
                        if (player.candyDeclinedNpcIds == null) {
                            player.candyDeclinedNpcIds = new java.util.HashSet<>();
                        }
                        player.candyDeclinedNpcIds.add(npc.tempId);
                    }
                } catch (Exception ignored) {
                }
                return true;
                default: // Đóng
                    return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
