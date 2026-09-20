package npc.list;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import npc.Npc;
import player.Player;
import services.player.InventoryService;
import services.ItemService;
import services.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import services.ShopService;
import services.map.ChangeMapService;
import services.player.PlayerService;

public class Vados extends Npc {

    public Vados(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "|2|Ngươi Muốn Làm Gì?",
                        "Tới Khu Vực Thiên Tử", "Đóng");
            } else if (this.mapId == 189) {
                player.iDMark.setIndexMenu((byte) 0);
                this.createOtherMenu(player, (byte) 0,
                        "Đây là khu vực thiên tử, nơi chứa trang sức của thiên tử đã thất lạc hàng nghìn năm trước, ngươi định làm gì?",
                        "Cửa hàng", "Đổi Chân Thiên tử", "Đổi Danh hiệu", "Về nhà");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        int currentMenu = player.iDMark.getIndexMenu();

        switch (this.mapId) {
            case 5:
                if (currentMenu == ConstNpc.BASE_MENU) {
                    if (select == 0) {
                        ChangeMapService.gI().changeMapInYard(player, 189, -1, 350);
                    } else if (select == 1) {
                        // Dialog closes
                    }
                }
                break;

            case 189:
                switch (currentMenu) {
                    case 0 -> handleMap189BaseMenuSelect(player, select);
                    case 50 -> handleMap189NguyenLieuChoiceSelect(player, select);
                    case 51 -> handleExchange(player, select, (short) 1978);
                    case 52 -> handleExchange(player, select, (short) 1979); // Tinh thể
                }
                break;

        }
    }

    private void handleMap189BaseMenuSelect(Player player, int select) {
        switch (select) {
            case 0:
                ShopService.gI().opendShop(player, "THIEN_TU", false);
                break;
            case 1:
                player.iDMark.setIndexMenu((byte) 50);
                this.createOtherMenu(player, (byte) 50,
                        "Ngươi sưu tầm được đủ nguyên liệu nào rồi?",
                        "Ma quái", "Tinh thể", "Đóng");
                break;
            case 2:
                this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chức năng đổi danh hiệu đang được phát triển.", "Đóng");
                break;
            case 3:
                ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, 0, -1);
                break;
            default:
                break;
        }
    }

    private void handleMap189NguyenLieuChoiceSelect(Player player, int select) {
        switch (select) {
            case 0 -> {
                player.iDMark.setIndexMenu((byte) 51);
                this.createOtherMenu(player, (byte) 51,
                        "Ngươi cần tìm đủ số lượng Ma quái để có thể đổi lấy Trang sức Chân thiên tử tân thủ với công thức tương ứng sau:\n" +
                                "Cần số lượng " + 99 + " Ma quái để đổi Trang sức 30 ngày\n" +
                                        "Cần số lượng " + 999 + " Ma quái để đổi Trang sức vĩnh viễn\n" +
                                                "Ngươi muốn đổi loại trang sức nào?",
                        "30 ngày\n(-" + (5_000_000_000L / 1_000_000_000L) + " Tỷ vàng)",
                        "Vĩnh viễn\n(-" + (9_000_000_000L / 1_000_000_000L) + " Tỷ vàng)",
                        "Đóng");
            }
            case 1 -> {
                player.iDMark.setIndexMenu((byte) 52);
                this.createOtherMenu(player, (byte) 52,
                        "Ngươi cần tìm đủ số lượng Tinh thể để có thể đổi lấy Trang sức Chân thiên tử tân thủ với công thức tương ứng sau:\n" +
                                "Cần số lượng " + 9 + " Tinh thể để đổi Trang sức 30 ngày\n" +
                                        "Cần số lượng " + 99 + " Tinh thể để đổi Trang sức vĩnh viễn\n" +
                                                "Ngươi muốn đổi loại trang sức nào?",
                        "30 ngày\n(-" + (5_000_000_000L / 1_000_000_000L) + " Tỷ vàng)",
                        "Vĩnh viễn\n(-" + (9_000_000_000L / 1_000_000_000L) + " Tỷ vàng)",
                        "Đóng");
            }
            case 2 -> {
                player.iDMark.setIndexMenu((byte) 0);
                this.createOtherMenu(player, (byte) 0,
                        "Đây là khu vực thiên tử, nơi chứa trang sức của thiên tử đã thất lạc hàng nghìn năm trước, ngươi định làm gì?",
                        "Cửa hàng", "Đổi Chân Thiên tử", "Đổi Danh hiệu", "Về nhà");
            }
            default -> {
            }
        }
    }

    private void handleExchange(Player player, int select, short materialItemId) {
        int materialNeeded;
        long goldCost;
        boolean isPermanent;
        String durationText;
        switch (select) {
            case 0 -> {
                goldCost = 5_000_000_000L;
                isPermanent = false;
                durationText = "30 ngày";
                if (materialItemId == (short) 1978) {
                    materialNeeded = 99;
                } else {
                    materialNeeded = 9;
                }
            }
            case 1 -> {
                goldCost = 9_000_000_000L;
                isPermanent = true;
                durationText = "vĩnh viễn";
                if (materialItemId == (short) 1978) {
                    materialNeeded = 999;
                } else {
                    materialNeeded = 99;
                }
            }
            case 2 -> {
                player.iDMark.setIndexMenu((byte) 50);
                this.createOtherMenu(player, (byte) 50,
                        "Ngươi sưu tầm được đủ nguyên liệu nào rồi?",
                        "Ma quái", "Tinh thể", "Đóng");
                return;
            }
            default -> {
                return;
            }
        }

        if (player.inventory.gold < goldCost) {
            Service.gI().sendThongBao(player, "Không đủ vàng!");
            return;
        }

        int currentMaterialQty = getTotalQuantityOfItemInBag(player, materialItemId);
        if (currentMaterialQty < materialNeeded) {
            String materialName = ItemService.gI().getTemplate(materialItemId).name;
            Service.gI().sendThongBao(player, "Không đủ " + materialName + ".");
            return;
        }

        player.inventory.subGold((int) goldCost);
        PlayerService.gI().sendInfoHpMpMoney(player);

        int amountLeftToSubtract = materialNeeded;
        List<Item> itemsFullyConsumed = new ArrayList<>();
        for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
            Item itemInBag = player.inventory.itemsBag.get(i);
            if (itemInBag.isNotNullItem() && itemInBag.template.id == materialItemId) {
                if (itemInBag.quantity > amountLeftToSubtract) {
                    itemInBag.quantity -= amountLeftToSubtract;
                    amountLeftToSubtract = 0;
                    break;
                } else {
                    amountLeftToSubtract -= itemInBag.quantity;
                    itemsFullyConsumed.add(itemInBag);
                }
            }
            if (amountLeftToSubtract == 0) {
                break;
            }
        }
        
        if (amountLeftToSubtract > 0) {
            Service.gI().sendThongBao(player, "Lỗi khi trừ nguyên liệu. Số lượng không khớp.");
            player.inventory.addGold((int) goldCost);
            PlayerService.gI().sendInfoHpMpMoney(player);
            return;
        }

        for (Item itemToRemove : itemsFullyConsumed) {
            InventoryService.gI().removeItemBag(player, itemToRemove);
        }
        InventoryService.gI().sendItemBags(player);

        Item accessory = ItemService.gI().createNewItem((short) 1969);
        if (accessory == null) {
            Service.gI().sendThongBao(player, "Lỗi tạo vật phẩm, vui lòng báo quản trị viên.");
            player.inventory.addGold((int) goldCost);
            PlayerService.gI().sendInfoHpMpMoney(player);
            return;
        }

        int sucDanhValue = ThreadLocalRandom.current().nextInt(1, 5 + 1);
        int phanTramHPValue = ThreadLocalRandom.current().nextInt(1, 5 + 1);
        int phanTramKIValue = ThreadLocalRandom.current().nextInt(1, 5 + 1);
        int hanSudung = ThreadLocalRandom.current().nextInt(15, 30 + 1);
        
        accessory.itemOptions.add(new ItemOption(50, sucDanhValue));
        accessory.itemOptions.add(new ItemOption(77, phanTramHPValue));
        accessory.itemOptions.add(new ItemOption(103, phanTramKIValue));

        if (!isPermanent) {
            accessory.itemOptions.add(new ItemOption(93, hanSudung));
        }

        InventoryService.gI().addItemBag(player, accessory);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn đã nhận được " + accessory.template.name + " (" + durationText + ")!");
    }

    private int getTotalQuantityOfItemInBag(Player player, short itemId) {
        int total = 0;
        if (player.inventory.itemsBag == null) {
            return 0;
        }
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == itemId) {
                total += item.quantity;
            }
        }
        return total;
    }
}