package server;

import java.io.IOException;

import consts.ConstNpc;
import npc.Npc;
import services.map.NpcManager;
import network.MySession;
import player.Player;
import services.Service;
import services.func.TransactionService;

public class MenuController {

    private static MenuController instance;

    public static MenuController gI() {
        if (instance == null) {
            instance = new MenuController();
        }
        return instance;
    }

    public void openMenuNPC(MySession session, int idnpc, Player player) {
        TransactionService.gI().cancelTrade(player);
        if (player == null || player.zone == null || player.zone.map == null) {
            Service.gI().hideWaitDialog(player);
            return;
        }
        Npc npc;
        if (idnpc == ConstNpc.CALICK && player.zone.map.mapId != 102) {
            npc = player.zone.map.getNpc(player, idnpc);
            if (npc == null) {
                npc = NpcManager.getNpc(ConstNpc.CALICK);
            }
        } else if (idnpc == ConstNpc.LY_TIEU_NUONG) {
            npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
        } else {
            npc = player.zone.map.getNpc(player, idnpc);
        }
        if (npc != null) {
            try {
                if (services.events.HalloweenMenuHook.interceptOpenNpc(player, npc)) {
                    return;
                }
            } catch (Exception ignored) {
            }
            npc.openBaseMenu(player);
        } else {
            Service.gI().hideWaitDialog(player);
        }
    }

    public void doSelectMenu(Player player, int npcId, int select) throws IOException {
        TransactionService.gI().cancelTrade(player);
        if (player != null && player.idMark != null && player.idMark.getIndexMenu() == Npc.MENU_CANDY_PROMPT) {
            Npc npc = null;
            if (npcId == ConstNpc.CALICK && player.zone != null && player.zone.map != null
                    && player.zone.map.mapId != 102) {
                npc = player.zone.map.getNpc(player, npcId);
                if (npc == null) {
                    npc = services.map.NpcManager.getNpc(ConstNpc.CALICK);
                }
            } else if (npcId == ConstNpc.LY_TIEU_NUONG) {
                npc = services.map.NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
            } else if (player.zone != null && player.zone.map != null) {
                npc = player.zone.map.getNpc(player, npcId);
            }
            if (npc != null) {
                switch (select) {
                    case 0:
                        if (player.candyDeclinedNpcIds == null)
                            player.candyDeclinedNpcIds = new java.util.HashSet<>();
                        player.candyDeclinedNpcIds.add(npc.tempId);
                        npc.openBaseMenu(player);
                        return;
                    case 1:
                        try {
                            if (services.player.InventoryService.gI().getCountEmptyBag(player) <= 0) {
                                services.Service.gI().sendThongBao(player, "Hành trang đã đầy, cần ít nhất 1 ô trống");
                            } else {
                                item.Item candy = services.ItemService.gI().createNewItem((short) 901);
                                candy.quantity = 1;
                                services.player.InventoryService.gI().addItemBag(player, candy);
                                services.player.InventoryService.gI().sendItemBags(player);
                                services.Service.gI().sendThongBao(player, "Bạn nhận được Kẹo bàn tay");
                                if (player.candyDeclinedNpcIds == null)
                                    player.candyDeclinedNpcIds = new java.util.HashSet<>();
                                player.candyDeclinedNpcIds.add(npc.tempId);
                            }
                        } catch (Exception ignored) {
                        }
                        return;
                    default: // Đóng -> do nothing
                        return;
                }
            }
        }

        if (player == null || player.zone == null || player.zone.map == null) {
            Service.gI().hideWaitDialog(player);
            return;
        }

        switch (npcId) {
            case ConstNpc.RONG_THIENG, ConstNpc.CON_MEO -> {
                Npc npc = NpcManager.getNpc((byte) npcId);
                if (npc != null) {
                    npc.confirmMenu(player, select);
                }
            }
            default -> {
                Npc npc = null;
                if (npcId == ConstNpc.CALICK && player.zone.map.mapId != 102) {
                    npc = player.zone.map.getNpc(player, npcId);
                    if (npc == null) {
                        npc = NpcManager.getNpc(ConstNpc.CALICK);
                    }
                } else if (npcId == ConstNpc.LY_TIEU_NUONG) {
                    npc = NpcManager.getNpc(ConstNpc.LY_TIEU_NUONG);
                } else {
                    npc = player.zone.map.getNpc(player, npcId);
                }
                if (npc != null) {
                    npc.confirmMenu(player, select);
                } else {
                    Service.gI().hideWaitDialog(player);
                }
            }
        }
    }
}
