package services;

import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import player.Player;
import network.Message;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import managers.ConsignShopManager;
import services.player.InventoryService;
import system.ConsignItem;
import utils.Logger;

public class ConsignShopService {

    private static ConsignShopService instance;
    private static final int GOLD_BAR_ID = 457; // Thỏi vàng
    private static final int LISTING_FEE_GEM = 5; // Phí đăng bán
    private static final int UP_TOP_FEE = 5; // thỏi vàng để up top
    private static final double TAX_RATE = 0.05; // 5% VAT
    private static final int MIN_PRICE = 10; // Giá tối thiểu

    public static ConsignShopService gI() {
        if (instance == null) {
            instance = new ConsignShopService();
        }
        return instance;
    }

    private List<ConsignItem> getItemKyGui(byte tab, int... max) {
        List<ConsignItem> its = new ArrayList<>();
        List<ConsignItem> listSort = new ArrayList<>();
        List<ConsignItem> listSort2 = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && it.tab == tab && !it.isBuy))
                .forEachOrdered((it) -> {
                    its.add(it);
                });
        its.stream().filter(i -> i != null).sorted(Comparator.comparing(i -> i.lasttime, Comparator.reverseOrder()))
                .forEach(i -> listSort.add(i));
        if (max.length == 2) {
            if (listSort.size() > max[1]) {
                for (int i = max[0]; i < max[1]; i++) {
                    if (listSort.get(i) != null) {
                        listSort2.add(listSort.get(i));
                    }
                }
            } else {
                for (int i = max[0]; i < listSort.size(); i++) {
                    if (listSort.get(i) != null) {
                        listSort2.add(listSort.get(i));
                    }
                }
            }
            return listSort2;
        }
        if (max.length == 1 && listSort.size() > max[0]) {
            for (int i = 0; i < max[0]; i++) {
                if (listSort.get(i) != null) {
                    listSort2.add(listSort.get(i));
                }
            }
            return listSort2;
        }
        return listSort;
    }

    private List<ConsignItem> getItemKyGui() {
        List<ConsignItem> its = new ArrayList<>();
        List<ConsignItem> listSort = new ArrayList<>();
        ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null && !it.isBuy)).forEachOrdered((it) -> {
            its.add(it);
        });
        its.stream().filter(i -> i != null).sorted(Comparator.comparing(i -> i.lasttime, Comparator.reverseOrder()))
                .forEach(i -> listSort.add(i));
        return listSort;
    }

    private synchronized boolean subGoldBar(Player pl, int quantity) {
        if (pl == null || quantity <= 0) {
            return false;
        }

        int totalQuantity = 0;
        List<Item> goldBars = new ArrayList<>();
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == GOLD_BAR_ID) {
                if (!item.isHaveOption(30)) {
                    goldBars.add(item);
                    totalQuantity += item.quantity;
                }
            }
        }
        if (totalQuantity < quantity) {
            Service.gI().sendThongBao(pl,
                    "Không đủ thỏi vàng có thể giao dịch (cần " + quantity + ", có " + totalQuantity + ")");
            return false;
        }
        int remaining = quantity;
        for (Item item : goldBars) {
            if (remaining <= 0) {
                break;
            }
            int toSub = Math.min(item.quantity, remaining);
            InventoryService.gI().subQuantityItemsBag(pl, item, toSub);
            remaining -= toSub;
        }

        return remaining == 0;
    }

    private synchronized boolean addGoldBar(Player pl, int quantity) {
        if (pl == null || quantity <= 0) {
            return false;
        }
        if (quantity > Integer.MAX_VALUE - 1000) {
            Logger.logException(ConsignShopService.class,
                    new Exception("Potential overflow detected in addGoldBar: " + quantity));
            return false;
        }

        try {
            Item goldBar = ItemService.gI().createNewItem((short) GOLD_BAR_ID);
            goldBar.quantity = quantity;
            boolean added = InventoryService.gI().addItemBag(pl, goldBar);
            if (!added) {
                Service.gI().sendThongBao(pl, "Hành trang đầy, không thể nhận thỏi vàng");
            }
            return added;
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error in addGoldBar");
            return false;
        }
    }

    public synchronized void buyItem(Player pl, int id) {
        try {
            if (pl == null || !pl.getSession().actived) {
                Service.gI().sendThongBao(pl, "Yêu cầu kích hoạt tài khoản");
                openShopKyGui(pl); // Fix: refresh UI to unlock client
                return;
            }

            if (pl.nPoint.power < 17000000000L) {
                Service.gI().sendThongBao(pl, "Yêu cầu sức mạnh lớn hơn 17 tỷ");
                this.openShopKyGui(pl);
                return;
            }
            ConsignItem it = getItemBuy(id);
            if (it == null) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại");
                openShopKyGui(pl);
                return;
            }

            if (it.isBuy) {
                Service.gI().sendThongBao(pl, "Vật phẩm đã được bán");
                openShopKyGui(pl);
                return;
            }
            if (it.player_sell == pl.id) {
                Service.gI().sendThongBao(pl, "Không thể mua vật phẩm của chính mình");
                openShopKyGui(pl);
                return;
            }
            if (it.goldSell <= 0 && it.gemSell <= 0) {
                Service.gI().sendThongBao(pl, "Giá không hợp lệ");
                Logger.logException(ConsignShopService.class,
                        new Exception("Invalid price: both prices are zero or negative. goldSell=" + it.goldSell
                                + ", gemSell=" + it.gemSell));
                openShopKyGui(pl);
                return;
            }
            if (it.goldSell < -1 || it.gemSell < -1) {
                Service.gI().sendThongBao(pl, "Giá không hợp lệ");
                Logger.logException(ConsignShopService.class,
                        new Exception(
                                "Invalid price: less than -1. goldSell=" + it.goldSell + ", gemSell=" + it.gemSell));
                openShopKyGui(pl);
                return;
            }
            if (it.goldSell > 0 && it.gemSell > 0) {
                Service.gI().sendThongBao(pl, "Chỉ được chọn 1 loại tiền tệ");
                Logger.logException(ConsignShopService.class,
                        new Exception("Both prices are positive: goldSell=" + it.goldSell + ", gemSell=" + it.gemSell));
                openShopKyGui(pl);
                return;
            }
            if (it.goldSell > 0 && it.goldSell < MIN_PRICE) {
                Service.gI().sendThongBao(pl,
                        "Vật phẩm này có giá không hợp lệ (tối thiểu " + MIN_PRICE + " thỏi vàng)");
                Logger.logException(ConsignShopService.class,
                        new Exception(
                                "Item with invalid price: goldSell=" + it.goldSell + " (minimum: " + MIN_PRICE + ")"));
                openShopKyGui(pl);
                return;
            }
            if (it.gemSell > 0 && it.gemSell < MIN_PRICE) {
                Service.gI().sendThongBao(pl,
                        "Vật phẩm này có giá không hợp lệ (tối thiểu " + MIN_PRICE + " ngọc xanh)");
                Logger.logException(ConsignShopService.class,
                        new Exception(
                                "Item with invalid price: gemSell=" + it.gemSell + " (minimum: " + MIN_PRICE + ")"));
                openShopKyGui(pl);
                return;
            }

            boolean success = false;
            if (it.goldSell > 0) {
                if (it.goldSell > Integer.MAX_VALUE - 1000) {
                    Service.gI().sendThongBao(pl, "Giá quá cao");
                    openShopKyGui(pl);
                    return;
                }
                if (!subGoldBar(pl, it.goldSell)) {
                    Service.gI().sendThongBao(pl, "Bạn không đủ thỏi vàng để mua vật phẩm này");
                    openShopKyGui(pl);
                    return;
                }
                success = true;
            } else if (it.gemSell > 0) {
                if (it.gemSell > Integer.MAX_VALUE / 2 || pl.inventory.gem > Integer.MAX_VALUE - it.gemSell) {
                    Service.gI().sendThongBao(pl, "Bán cắt cổ vậy fen ?");
                    openShopKyGui(pl);
                    return;
                }
                if (pl.inventory.gem < it.gemSell) {
                    Service.gI().sendThongBao(pl, "Bạn không đủ ngọc xanh để mua vật phẩm này");
                    openShopKyGui(pl);
                    return;
                }
                pl.inventory.gem -= it.gemSell;
                success = true;
            } else {
                Service.gI().sendThongBao(pl, "Vật phẩm không có giá");
                openShopKyGui(pl);
                return;
            }

            if (success) {
                if (!it.markAsSold()) {
                    if (it.goldSell > 0) {
                        addGoldBar(pl, it.goldSell);
                    } else if (it.gemSell > 0) {
                        pl.inventory.gem += it.gemSell;
                    }
                    Service.gI().sendThongBao(pl, "Vật phẩm vừa được người khác mua");
                    Service.gI().sendMoney(pl);
                    openShopKyGui(pl);
                    return;
                }
                Item item = ItemService.gI().createNewItem(it.itemId);
                item.quantity = it.quantity;
                item.itemOptions.clear();
                item.itemOptions.addAll(it.options);
                if (!InventoryService.gI().addItemBag(pl, item)) {
                    if (it.goldSell > 0) {
                        addGoldBar(pl, it.goldSell);
                    } else if (it.gemSell > 0) {
                        pl.inventory.gem += it.gemSell;
                    }
                    synchronized (it) {
                        it.isBuy = false;
                    }
                    Service.gI().sendThongBao(pl, "Hành trang đầy, không thể nhận vật phẩm");
                    Service.gI().sendMoney(pl);
                    openShopKyGui(pl);
                    return;
                }

                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendMoney(pl);
                Service.gI().sendThongBao(pl, "Mua thành công " + item.template.name);
                openShopKyGui(pl);

                // FIX: Cập nhật UI cho người bán để họ thấy vật phẩm đã bán
                try {
                    Player seller = player.PlayerManager.getPlayer(it.player_sell);
                    if (seller != null && seller.getSession() != null && seller.getSession().actived) {
                        Logger.log("CONSIGN_SHOP: Notifying seller (ID: " + it.player_sell + ") that item " + it.id + " was sold");
                        openShopKyGui(seller);
                    }
                } catch (Exception e) {
                    Logger.logException(ConsignShopService.class, e, "Error notifying seller about sold item");
                }
            }
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error in buyItem");
            Service.gI().sendThongBao(pl, "Có lỗi xảy ra, vui lòng thử lại");
            if (pl != null) {
                openShopKyGui(pl);
            }
        }
    }

    public ConsignItem getItemBuy(int id) {
        // Search directly in the main list to include sold items
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.id == id) {
                return it;
            }
        }
        return null;
    }

    public ConsignItem getItemBuy(Player pl, int id) {
        for (ConsignItem it : ConsignShopManager.gI().listItem) {
            if (it != null && it.id == id && it.player_sell == pl.id) {
                return it;
            }
        }
        return null;
    }

    public ConsignItem getItemBuy(Player pl, ConsignItem itk) {
        // Optimization: If the item has a valid ID, find it directly.
        if (itk != null && itk.id > 0) {
            for (ConsignItem it : ConsignShopManager.gI().listItem) {
                if (it != null && it.id == itk.id && it.player_sell == pl.id) {
                    return it;
                }
            }
        }
        // This part is for items that are not yet in the consign list (from player's bag)
        // and thus have a temporary ID (like bag index). They don't have a 'real' state yet.
        return null;
    }

    public void openShopKyGui(Player pl, byte index, int page) {
        if (page > getItemKyGui(index).size()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-100);
            msg.writer().writeByte(index);
            List<ConsignItem> items = getItemKyGui(index);
            List<ConsignItem> itemsSend = getItemKyGui(index, (int) (page * 20), (int) (page * 20 + 20));
            int cTab = (int) Math.ceil((double) items.size() / 20);
            byte tab = (byte) (cTab > 0 ? cTab : 1);
            msg.writer().writeByte(tab);
            msg.writer().writeByte(page);
            msg.writer().writeByte(itemsSend.size());
            for (int j = 0; j < itemsSend.size(); j++) {
                ConsignItem itk = itemsSend.get(j);
                Item it = ItemService.gI().createNewItem(itk.itemId);
                it.itemOptions.clear();
                if (itk.options.isEmpty()) {
                    it.itemOptions.add(new ItemOption(73, 0));
                } else {
                    it.itemOptions.addAll(itk.options);
                }
                msg.writer().writeShort(it.template.id);
                msg.writer().writeShort(itk.id);
                msg.writer().writeInt(itk.goldSell);
                msg.writer().writeInt(itk.gemSell);
                msg.writer().writeByte(0);
                if (pl.getSession().version >= 222) {
                    msg.writer().writeInt(itk.quantity);
                } else {
                    msg.writer().writeByte(itk.quantity);
                }
                msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0);
                msg.writer().writeShort(it.itemOptions.size());
                for (int a = 0; a < it.itemOptions.size(); a++) {
                    msg.writer().writeShort(it.itemOptions.get(a).optionTemplate.id);
                    msg.writer().writeInt(it.itemOptions.get(a).param);
                }
                msg.writer().writeByte(0);
                msg.writer().writeByte(0);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public synchronized void upItemToTop(Player pl, int id) {
        try {
            ConsignItem it = getItemBuy(id);
            if (it == null || it.isBuy) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                return;
            }
            if (it.player_sell != pl.id) {
                Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                openShopKyGui(pl);
                return;
            }

            pl.iDMark.setIdItemUpTop(id);
            NpcService.gI().createMenuConMeo(pl, ConstNpc.UP_TOP_ITEM, -1,
                    "Bạn có muốn đưa vật phẩm '" + ItemService.gI().createNewItem(it.itemId).template.name
                            + "' lên trang đầu?\nYêu cầu " + UP_TOP_FEE + " thỏi vàng.",
                    "Đồng ý", "Từ Chối");
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error in upItemToTop");
        }
    }

    public synchronized void confirmUpTop(Player pl, int id) {
        try {
            ConsignItem it = getItemBuy(id);
            if (it == null || it.isBuy) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                openShopKyGui(pl);
                return;
            }
            if (it.player_sell != pl.id) {
                Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                openShopKyGui(pl);
                return;
            }

            if (!subGoldBar(pl, UP_TOP_FEE)) {
                Service.gI().sendThongBao(pl, "Bạn không đủ thỏi vàng (cần " + UP_TOP_FEE + ")");
                openShopKyGui(pl);
                return;
            }

            it.updateLastTime();
            Service.gI().sendThongBao(pl, "Đã đưa vật phẩm lên trang đầu");
            openShopKyGui(pl);
            Logger.log("UP_TOP: Player " + pl.id + " up item " + it.id);
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error in confirmUpTop");
            if (pl != null) {
                openShopKyGui(pl);
            }
        }
    }

    public synchronized void claimOrDel(Player pl, byte action, int id) {
        try {
            ConsignItem it = getItemBuy(pl, id);

            switch (action) {
                case 1: // Hủy bán - trả lại item
                    if (it == null) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại");
                        openShopKyGui(pl);
                        return;
                    }
                    if (it.isBuy) {
                        Service.gI().sendThongBao(pl, "Vật phẩm đã được bán, không thể hủy");
                        openShopKyGui(pl);
                        return;
                    }

                    if (it.player_sell != pl.id) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                        openShopKyGui(pl);
                        return;
                    }

                    Item item = ItemService.gI().createNewItem(it.itemId);
                    item.quantity = it.quantity;
                    item.itemOptions.clear();
                    item.itemOptions.addAll(it.options);

                    if (ConsignShopManager.gI().removeItem(it)) {
                        if (!InventoryService.gI().addItemBag(pl, item)) {
                            ConsignShopManager.gI().addItem(it);
                            Service.gI().sendThongBao(pl, "Hành trang đầy");
                            openShopKyGui(pl);
                            return;
                        }
                        InventoryService.gI().sendItemBags(pl);
                        Service.gI().sendMoney(pl);
                        Service.gI().sendThongBao(pl, "Hủy bán vật phẩm thành công");
                        openShopKyGui(pl);
                    }
                    break;

                case 2:
                    if (it == null) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại");
                        openShopKyGui(pl);
                        return;
                    }
                    if (!it.isBuy) {
                        Service.gI().sendThongBao(pl, "Vật phẩm chưa được bán");
                        openShopKyGui(pl);
                        return;
                    }

                    if (it.player_sell != pl.id) {
                        Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                        openShopKyGui(pl);
                        return;
                    }

                    boolean claimSuccess = false;

                    if (it.goldSell > 0) {
                        // Tính VAT: làm tròn lên để đảm bảo người chơi luôn nhận được ít nhất 1 thỏi
                        // vàng
                        // Ví dụ: 1 thỏi -> sau VAT = 0.95 -> làm tròn lên = 1
                        // Ví dụ: 10 thỏi -> sau VAT = 9.5 -> làm tròn lên = 10
                        double taxAmount = it.goldSell * TAX_RATE;
                        double amountAfterTax = it.goldSell - taxAmount;
                        // Làm tròn lên để đảm bảo người chơi không bị mất tiền do làm tròn xuống
                        int finalAmount = (int) Math.ceil(amountAfterTax);

                        // Đảm bảo số tiền sau thuế >= 1
                        if (finalAmount < 1) {
                            finalAmount = 1;
                        }

                        if (finalAmount < 0 || finalAmount > Integer.MAX_VALUE - 1000) {
                            Service.gI().sendThongBao(pl, "Số tiền không hợp lệ");
                            Logger.logException(ConsignShopService.class,
                                    new Exception("Invalid amount after tax: " + finalAmount + " (original: "
                                            + it.goldSell + ")"));
                            openShopKyGui(pl);
                            return;
                        }
                        if (addGoldBar(pl, finalAmount)) {
                            claimSuccess = true;
                        } else {
                            Service.gI().sendThongBao(pl, "Không thể nhận thỏi vàng, hành trang đầy");
                            openShopKyGui(pl);
                            return;
                        }
                    } else if (it.gemSell > 0) {
                        // Tính VAT: làm tròn lên để đảm bảo người chơi luôn nhận được ít nhất 1 ngọc
                        double taxAmount = it.gemSell * TAX_RATE;
                        double amountAfterTax = it.gemSell - taxAmount;
                        // Làm tròn lên để đảm bảo người chơi không bị mất tiền do làm tròn xuống
                        int finalAmount = (int) Math.ceil(amountAfterTax);

                        // Đảm bảo số tiền sau thuế >= 1
                        if (finalAmount < 1) {
                            finalAmount = 1;
                        }

                        if (finalAmount < 0 || pl.inventory.gem > Integer.MAX_VALUE - finalAmount) {
                            Service.gI().sendThongBao(pl, "Số tiền không hợp lệ");
                            Logger.logException(ConsignShopService.class,
                                    new Exception("Gem overflow detected: " + finalAmount + " (original: " + it.gemSell
                                            + ")"));
                            return;
                        }
                        pl.inventory.gem += finalAmount;
                        claimSuccess = true;
                    }

                    if (claimSuccess && ConsignShopManager.gI().removeItem(it)) {
                        Service.gI().sendMoney(pl);
                        InventoryService.gI().sendItemBags(pl);
                        Service.gI().sendThongBao(pl, "Nhận tiền thành công");
                        Logger.log("CONSIGN_SHOP: Player " + pl.id + " claimed money for item " + it.id +
                                   (it.goldSell > 0 ? " (gold: " + it.goldSell + ")" : " (gem: " + it.gemSell + ")"));
                        openShopKyGui(pl);
                    } else if (!claimSuccess) {
                        Service.gI().sendThongBao(pl, "Không thể nhận tiền, vui lòng thử lại");
                        Logger.log("CONSIGN_SHOP: Failed to claim money for item " + it.id + " - claimSuccess was false");
                        openShopKyGui(pl);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể xóa vật phẩm khỏi danh sách, vui lòng thử lại");
                        Logger.log("CONSIGN_SHOP: Failed to remove item " + it.id + " from list");
                        openShopKyGui(pl);
                    }
                    break;

                default:
                    Service.gI().sendThongBao(pl, "Hành động không hợp lệ");
                    break;
            }
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error in claimOrDel");
            Service.gI().sendThongBao(pl, "Có lỗi xảy ra, vui lòng thử lại");
            if (pl != null) {
                openShopKyGui(pl);
            }
        }
    }

    public List<ConsignItem> getItemCanKiGui(Player pl) {
        List<ConsignItem> finalItems = new ArrayList<>();

        // 1. Get all items the player has actually consigned (sold or not sold).
        List<ConsignItem> consignedItems = ConsignShopManager.gI().listItem.stream()
                .filter(it -> it != null && it.player_sell == pl.id)
                .toList();
        finalItems.addAll(consignedItems);

        // 2. Get items from the bag that are eligible AND not already in the consigned list.
        pl.inventory.itemsBag.stream()
                .filter(this::itemCanConsign)
                .filter(bagItem -> consignedItems.stream().noneMatch(ci -> ci.itemId == bagItem.template.id && !ci.isBuy))
                .forEach(bagItem -> {
                    finalItems.add(new ConsignItem(
                            InventoryService.gI().getIndexBag(pl, bagItem), // Temporary ID is the bag index
                            bagItem.template.id,
                            (int) pl.id,
                            pl.name,
                            (byte) 4, // Tab for player's own items
                            -1, -1, // No price yet
                            bagItem.quantity,
                            -1, // No timestamp yet
                            bagItem.itemOptions,
                            false // Not sold, not even consigned yet
                    ));
                });

        return finalItems;
    }

    public boolean itemCanConsign(Item it) {
        if (it == null || it.template == null) {
            return false;
        }

        // Cấm thỏi vàng
        if (it.template.id == GOLD_BAR_ID) {
            return false;
        }

        // Cấm item có option 30
        if (it.isHaveOption(30)) {
            return false;
        }
        // Cấm item có option 154 (
        if (it.isHaveOption(154)) {
            return false;
        }
        // Cấm item có option 93
        if (it.isHaveOption(93)) {
            return false;
        }
        // Chỉ cho phép ký gửi các loại item hợp lệ
        if (it.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 86)
                || it.itemOptions.stream().anyMatch(op -> op.optionTemplate.id == 87)
                || it.template.type == 14 // Đá nâng cấp
                || it.template.type == 15 // Vật phẩm sự kiện
                || it.template.type == 6 // Thức ăn
                || (it.template.id >= 14 && it.template.id <= 20)) { // Ngọc Rồng
            return true;
        }

        return false;
    }

    public int getMaxId() {
        try {
            List<Integer> id = new ArrayList<>();
            ConsignShopManager.gI().listItem.stream().filter((it) -> (it != null)).forEachOrdered((it) -> {
                id.add(it.id);
            });
            return Collections.max(id);
        } catch (Exception e) {
            return 0;
        }
    }

    public byte getTabKiGui(Item it) {
        if (it.template.type >= 0 && it.template.type <= 2) {
            return 0;
        } else if ((it.template.type >= 3 && it.template.type <= 4)) {
            return 1;
        } else if (it.template.type == 29) {
            return 2;
        } else {
            return 3;
        }
    }

    public synchronized void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        try {
            if (pl == null || id < 0 || money <= 0 || quantity <= 0) {
                Service.gI().sendThongBao(pl, "Thông tin không hợp lệ");
                if (pl != null) {
                    openShopKyGui(pl);
                }
                return;
            }
            if (money > Integer.MAX_VALUE / 2) {
                Service.gI().sendThongBao(pl, "Giá quá cao");
                openShopKyGui(pl);
                return;
            }
            if (quantity > 99) {
                Service.gI().sendThongBao(pl, "Ký gửi tối đa x99");
                openShopKyGui(pl);
                return;
            }
            if (id >= pl.inventory.itemsBag.size()) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại");
                openShopKyGui(pl);
                return;
            }
            Item originalItem = pl.inventory.itemsBag.get(id);
            if (originalItem == null || originalItem.template == null) {
                Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại");
                openShopKyGui(pl);
                return;
            }
            if (originalItem.template.id == GOLD_BAR_ID) {
                Service.gI().sendThongBao(pl, "Thỏi vàng không thể ký gửi");
                openShopKyGui(pl);
                return;
            }
            if (originalItem.isHaveOption(30)) {
                Service.gI().sendThongBao(pl, "Vật phẩm không thể giao dịch, không thể ký gửi");
                openShopKyGui(pl);
                return;
            }
            if (originalItem.isHaveOption(154)) {
                Service.gI().sendThongBao(pl, "Vật phẩm không thể bán lại, không thể ký gửi");
                openShopKyGui(pl);
                return;
            }
            if (originalItem.isHaveOption(93)) {
                Service.gI().sendThongBao(pl, "Vật phẩm có hạn sử dụng, không thể ký gửi");
                openShopKyGui(pl);
                return;
            }
            if (!ConsignShopService.gI().itemCanConsign(originalItem)) {
                Service.gI().sendThongBao(pl, "Vật phẩm này không thể ký gửi");
                openShopKyGui(pl);
                return;
            }
            if (quantity > originalItem.quantity) {
                Service.gI().sendThongBao(pl, "Số lượng vật phẩm không đủ");
                openShopKyGui(pl);
                return;
            }
            if (pl.inventory.gem < LISTING_FEE_GEM) {
                Service.gI().sendThongBao(pl,
                        "Bạn cần có ít nhất " + LISTING_FEE_GEM + " ngọc xanh để làm phí đăng bán");
                openShopKyGui(pl);
                return;
            }

            // Kiểm tra giá tối thiểu: phải >= 10 thỏi vàng hoặc 10 ngọc xanh
            if (moneyType == 0) { // Thỏi vàng
                if (money < MIN_PRICE) {
                    Service.gI().sendThongBao(pl, "Giá bán tối thiểu phải là " + MIN_PRICE + " thỏi vàng");
                    openShopKyGui(pl);
                    return;
                }
            } else if (moneyType == 1) { // Ngọc xanh
                if (money < MIN_PRICE) {
                    Service.gI().sendThongBao(pl, "Giá bán tối thiểu phải là " + MIN_PRICE + " ngọc xanh");
                    openShopKyGui(pl);
                    return;
                }
            }

            Item it = ItemService.gI().copyItem(originalItem);
            if (moneyType != 0 && moneyType != 1) {
                Service.gI().sendThongBao(pl, "Loại tiền không hợp lệ (chỉ chấp nhận Thỏi vàng hoặc Ngọc)");
                openShopKyGui(pl);
                return;
            }
            int originalGem = pl.inventory.gem;
            pl.inventory.gem -= LISTING_FEE_GEM;
            InventoryService.gI().subQuantityItemsBag(pl, originalItem, quantity);

            ConsignItem consignItem;
            try {
                if (moneyType == 0) { // Thỏi vàng
                    consignItem = new ConsignItem(getMaxId() + 1, it.template.id, (int) pl.id, pl.name,
                            getTabKiGui(it), money, -1, quantity, System.currentTimeMillis(), it.itemOptions, false);
                } else { // Gem
                    consignItem = new ConsignItem(getMaxId() + 1, it.template.id, (int) pl.id, pl.name,
                            getTabKiGui(it), -1, money, quantity, System.currentTimeMillis(), it.itemOptions, false);
                }
            } catch (Exception e) {
                // Rollback: trả lại item và phí
                Item rollbackItem = ItemService.gI().createNewItem(it.template.id);
                rollbackItem.quantity = quantity;
                rollbackItem.itemOptions.clear();
                rollbackItem.itemOptions.addAll(it.itemOptions);
                InventoryService.gI().addItemBag(pl, rollbackItem);
                pl.inventory.gem = originalGem;

                Service.gI().sendThongBao(pl, "Có lỗi khi tạo vật phẩm ký gửi, vui lòng thử lại");
                Logger.logException(ConsignShopService.class, e, "Error creating ConsignItem for player " + pl.id);
                openShopKyGui(pl);
                return;
            }

            if (!ConsignShopManager.gI().addItem(consignItem)) {
                // Rollback: trả lại item và phí
                Item rollbackItem = ItemService.gI().createNewItem(it.template.id);
                rollbackItem.quantity = quantity;
                rollbackItem.itemOptions.clear();
                rollbackItem.itemOptions.addAll(it.itemOptions);
                InventoryService.gI().addItemBag(pl, rollbackItem);
                pl.inventory.gem = originalGem;

                Service.gI().sendThongBao(pl, "Có lỗi khi đăng bán, vui lòng thử lại");
                Logger.logException(ConsignShopService.class,
                        new Exception(
                                "Failed to add consign item for player " + pl.id + ", item ID: " + consignItem.id));
                openShopKyGui(pl);
                return;
            }

            InventoryService.gI().sendItemBags(pl);
            openShopKyGui(pl);
            Service.gI().sendMoney(pl);
            Service.gI().sendThongBao(pl, "Đăng bán thành công");

        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Lỗi ký gửi");
            Service.gI().sendThongBao(pl, "Có lỗi xảy ra, vui lòng thử lại");
            if (pl != null) {
                openShopKyGui(pl);
            }
        }
    }

    private String getPlayerName(int playerId) {
        // Try to get from online players first
        Player onlinePlayer = player.PlayerManager.getPlayer(playerId);
        if (onlinePlayer != null) {
            return onlinePlayer.name;
        }
        // If not online, query from database
        try {
            data.AlyraResultSet rs = data.AlyraManager.executeQuery("SELECT name FROM player WHERE id = ? LIMIT 1", playerId);
            if (rs.first()) {
                String name = rs.getString("name");
                rs.dispose();
                return name != null ? name : "Unknown";
            }
            if (rs != null) {
                rs.dispose();
            }
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error getting player name for id: " + playerId);
        }
        return "Unknown";
    }

    public void openShopKyGui(Player pl) {
        Message msg = null;
        try {
            Logger.log("openShopKyGui: Opening consign shop for player " + (pl != null ? pl.name : "null"));
            msg = new Message(-44);
            msg.writer().writeByte(2);
            msg.writer().writeByte(5);
            Logger.log("openShopKyGui: Shop type=2, tabs=5");
            for (byte i = 0; i < 5; i++) {
                List<ConsignItem> items;
                List<ConsignItem> itemsSend;

                if (i == 4) {
                    // Handle Tab 4: Player's personal consignment view
                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);

                    // 1. Get items player has actually consigned (sold or for sale)
                    List<ConsignItem> consignedItems = ConsignShopManager.gI().listItem.stream()
                            .filter(it -> it != null && it.player_sell == pl.id)
                            .toList();

                    // 2. Get items from player's bag that can be consigned
                    List<Item> bagItemsForConsignment = pl.inventory.itemsBag.stream()
                            .filter(this::itemCanConsign)
                            .toList();

                    int totalItems = consignedItems.size() + bagItemsForConsignment.size();
                    byte maxPage = (byte) Math.max(1, (int) Math.ceil((double) totalItems / 20.0));
                    msg.writer().writeByte(maxPage);
                    msg.writer().writeByte(totalItems);

                    // Send consigned items first
                    for (ConsignItem itk : consignedItems) {
                        sendItemInfo(msg, pl, itk, itk.isBuy ? (byte) 2 : (byte) 1);
                    }

                    // Send bag items next
                    for (Item bagItem : bagItemsForConsignment) {
                        ConsignItem tempItem = new ConsignItem(
                            InventoryService.gI().getIndexBag(pl, bagItem),
                            bagItem.template.id, (int)pl.id, pl.name, (byte)4, -1, -1,
                            bagItem.quantity, -1, bagItem.itemOptions, false);
                        sendItemInfo(msg, pl, tempItem, (byte) 0); // isMe = 0 for bag items
                    }
                } else {
                    // Handle regular shop tabs
                    items = getItemKyGui(i);
                    itemsSend = getItemKyGui(i, 20);

                    msg.writer().writeUTF(ConsignShopManager.gI().tabName[i]);
                    byte maxPageShop = (byte) Math.max(1, (int) Math.ceil((double) items.size() / 20.0));
                    msg.writer().writeByte(maxPageShop);
                    msg.writer().writeByte(itemsSend.size());

                    for (ConsignItem itk : itemsSend) {
                        sendItemInfo(msg, pl, itk, (itk.player_sell == pl.id) ? (byte) 1 : (byte) 0);
                    }
                }
            }
            Logger.log("openShopKyGui: Message sent successfully for player " + (pl != null ? pl.name : "null"));
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(ConsignShopService.class, e, "Error in openShopKyGui for player: " + (pl != null ? pl.name : "null"));
            Logger.warning("openShopKyGui: Exception details - " + e.getMessage());
            if (pl != null) {
                Service.gI().sendThongBao(pl, "Có lỗi khi mở shop ký gửi");
            }
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }


    private void sendItemInfo(Message msg, Player pl, ConsignItem itk, byte isMe) throws IOException {
        Item it = ItemService.gI().createNewItem(itk.itemId);
        if (it == null) return;

        it.itemOptions.clear();
        if (itk.options != null && !itk.options.isEmpty()) {
            it.itemOptions.addAll(itk.options);
        } else {
            it.itemOptions.add(new ItemOption(73, 0));
        }

        msg.writer().writeShort(it.template.id);
        msg.writer().writeShort(itk.id);
        msg.writer().writeInt(itk.goldSell);
        msg.writer().writeInt(itk.gemSell);

        // Determine buyType correctly:
        // 0 = item from bag (not consigned yet), 1 = listing (selling), 2 = sold (awaiting claim)
        byte buyType;
        if ((itk.goldSell == -1 && itk.gemSell == -1) || itk.lasttime == -1) {
            buyType = 0; // from bag
        } else if (itk.isBuy) {
            buyType = 2; // sold
        } else {
            buyType = 1; // selling
        }
        msg.writer().writeByte(buyType);

        if (pl.getSession().version >= 222) {
            msg.writer().writeInt(itk.quantity);
        } else {
            msg.writer().writeByte(itk.quantity);
        }

        // isMe: 1 if owner, 0 otherwise. Keep compatibility with existing callers.
        byte ownerFlag = (byte) ((itk.player_sell == pl.id) ? 1 : 0);
        // If caller provided isMe as 2 (for sold), normalize to ownerFlag to avoid confusion on client
        msg.writer().writeByte(ownerFlag);

        msg.writer().writeByte(it.itemOptions.size());
        for (ItemOption option : it.itemOptions) {
            msg.writer().writeShort(option.optionTemplate.id);
            msg.writer().writeInt(option.param);
        }

        msg.writer().writeByte(0); // newItem

        if (it.template.type == 5) {
            msg.writer().writeByte(1);
            msg.writer().writeShort(it.template.head);
            msg.writer().writeShort(it.template.body);
            msg.writer().writeShort(it.template.leg);
            msg.writer().writeShort((short) -1);
        } else {
            msg.writer().writeByte(0);
        }

        if (pl.getSession().version >= 237) {
            String playerName = (itk.playerName != null && !itk.playerName.isEmpty()) ? itk.playerName : "Unknown";
            msg.writer().writeUTF(playerName);
        }
    }
}
