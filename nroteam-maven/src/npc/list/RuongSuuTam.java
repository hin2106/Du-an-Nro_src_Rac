package npc.list;

import consts.ConstNpc;
import npc.Npc;
import player.Player;
import services.ItemService;
import services.Service;
import services.player.InventoryService;

public class RuongSuuTam extends Npc {
    
    private static final long PRICE_GOLD = 1_500_000_000;
    private static final int MAX_SLOTS = 40;
    private static final int PRICE_GEM = 500; // ngọc xanh cho slot 31 -> 40

    public RuongSuuTam(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Vàng bạc châu báu gì cứ yên tâm giao hết cho tôi\n",
                "Mở rương",
                "Nâng cấp\nrương",
                "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {
                        InventoryService.gI().sendItemBox1(player);
                    }
                    case 1 -> {
                        int currentSlots = player.inventory.itemsBox1.size();
                        if (currentSlots >= MAX_SLOTS) {
                            String npcSay = "Rương đã mở tối đa " + MAX_SLOTS + " ô, không thể mở thêm!";
                            createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "OK");
                            return;
                        }
                        int nextSlot = currentSlots + 1;
                        String npcSay;
                        if (nextSlot >= 31) {
                            npcSay = "Bạn có chắc chắn muốn mở thêm ô " + nextSlot 
                                   + " với giá " + PRICE_GEM + " ngọc xanh?";
                        } else {
                            npcSay = "Bạn có chắc chắn muốn mở thêm ô " + nextSlot 
                                   + " với giá 1,5 tỷ vàng?";
                        }
                        createOtherMenu(player, ConstNpc.ORTHER_MENU, npcSay,
                                "Đồng ý",
                                "Từ chối");
                    }
                }
            } else {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.ORTHER_MENU -> {
                        int currentSlots = player.inventory.itemsBox1.size();
                        int nextSlot = currentSlots + 1;
                        
                        if (currentSlots >= MAX_SLOTS) {
                            String npcSay = "Rương đã mở tối đa " + MAX_SLOTS + " ô, không thể mở thêm";
                            createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "OK");
                            return;
                        }

                        if (nextSlot >= 31) {
                            if (player.inventory.gem < PRICE_GEM) {
                                Service.gI().sendThongBao(player, 
                                    "Bạn cần " + PRICE_GEM + " ngọc xanh để mở ô " + nextSlot);
                                return;
                            }
                            player.inventory.gem -= PRICE_GEM;
                            Service.gI().sendThongBao(player, "Nâng cấp\n Xong");
                        } else {
                            if (player.inventory.gold < PRICE_GOLD) {
                                Service.gI().sendThongBao(player, 
                                    "Không đủ vàng để mở rộng rương");
                                return;
                            }
                            player.inventory.gold -= PRICE_GOLD;
                        }
                        player.inventory.itemsBox1.add(ItemService.gI().createItemNull());
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player, "Nâng cấp\n Xong");
                    }
                }
            }
        }
    }
}
