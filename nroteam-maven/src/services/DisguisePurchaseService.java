package services;

import item.Item;
import player.Player;
import server.Manager;
import services.player.InventoryService;
import shop.ItemShop;
import shop.Shop;
import utils.Logger;
import utils.Util;

public class DisguisePurchaseService {

    private static final byte COST_GEM = 1;

    public static class DisguisePurchaseInfo {
        public final Player seller;
        public final short templateId;
        public final String disguiseName;
        public final byte currencyType;
        public final int storeCost;
        public final int localCost;
        public final int commission;

        public DisguisePurchaseInfo(Player seller, short templateId, String disguiseName,
                byte currencyType, int storeCost, int localCost, int commission) {
            this.seller = seller;
            this.templateId = templateId;
            this.disguiseName = disguiseName;
            this.currencyType = currencyType;
            this.storeCost = storeCost;
            this.localCost = localCost;
            this.commission = commission;
        }
    }

    private static DisguisePurchaseService instance;

    private DisguisePurchaseService() {
    }

    public static DisguisePurchaseService gI() {
        if (instance == null) {
            instance = new DisguisePurchaseService();
        }
        return instance;
    }

    public DisguisePurchaseInfo createPurchaseInfo(Player seller, Item disguise) {
        if (seller == null || disguise == null || !disguise.isNotNullItem() || disguise.template == null) {
            return null;
        }

        ItemShop itemShop = findItemShop(disguise.template.id);
        int storeCost = 0;

        if (itemShop != null) {
            if (itemShop.typeSell != COST_GEM) {
                return null;
            }
            storeCost = itemShop.cost;

            if (storeCost == 0) {
                storeCost = disguise.template.gem;
            }
        } else {
            if (disguise.template.gem > 0) {
                storeCost = disguise.template.gem;
            } else {
                return null;
            }
        }

        if (storeCost <= 0) {
            return null;
        }

        int localCost = (storeCost * 95) / 100;
        int commission = (storeCost * 20) / 100;

        return new DisguisePurchaseInfo(
                seller,
                disguise.template.id,
                disguise.template.name,
                COST_GEM,
                storeCost,
                localCost,
                commission);
    }

    public ItemShop findItemShop(short templateId) {
        try {
            for (Shop shop : Manager.SHOPS) {
                if (shop == null) {
                    continue;
                }
                ItemShop is = shop.getItemShop(templateId);
                if (is != null) {
                    return is;
                }
            }
        } catch (Exception e) {
            Logger.logException(DisguisePurchaseService.class, e);
        }
        return null;
    }

    public String validateDisguise(Item disguise, Player seller) {
        if (disguise == null || !disguise.isNotNullItem()) {
            return seller != null ? seller.name + " hiện không mặc cải trang." : "Không tìm thấy cải trang.";
        }

        if (disguise.template == null) {
            return "Cải trang không hợp lệ.";
        }

        try {
            if (disguise.isHaveOption(93)) {
                return "Cải trang này có hạn sử dụng, không thể mua từ người chơi.";
            }
            if (disguise.isHaveOption(30)) {
                return "Cải trang này đã bị khóa, không thể mua từ người chơi.";
            }
            if (disguise.isHaveOption(154)) {
                return "Cải trang này không thể mua lại từ người chơi.";
            }
        } catch (Exception e) {
            Logger.logException(DisguisePurchaseService.class, e);
            return "Đã xảy ra lỗi khi kiểm tra cải trang.";
        }

        return null;
    }

    public boolean processPurchase(Player buyer, DisguisePurchaseInfo info) {
        if (buyer == null || buyer.inventory == null || info == null) {
            return false;
        }

        Player seller = info.seller;
        if (seller == null || seller.inventory == null || seller.inventory.itemsBody == null
                || seller.inventory.itemsBody.size() <= 5) {
            Service.gI().sendThongBao(buyer, "Không thể truy cập trang bị của người bán.");
            return false;
        }

        Item disguiseRef = seller.inventory.itemsBody.get(5);
        if (disguiseRef == null || !disguiseRef.isNotNullItem() || disguiseRef.template == null) {
            Service.gI().sendThongBao(buyer, "Cải trang của người bán đã thay đổi hoặc không còn.");
            return false;
        }

        if (disguiseRef.template.id != info.templateId) {
            Service.gI().sendThongBao(buyer, "Cải trang của người bán đã thay đổi.");
            return false;
        }

        String validationError = validateDisguise(disguiseRef, seller);
        if (validationError != null) {
            Service.gI().sendThongBao(buyer, validationError);
            return false;
        }

        if (InventoryService.gI().getCountEmptyBag(buyer) <= 0) {
            Service.gI().sendThongBao(buyer, "Hành trang không đủ chỗ trống.");
            return false;
        }

        if (buyer.inventory.gem < info.localCost) {
            Service.gI().sendThongBao(buyer,
                    "Bạn không đủ ngọc. Cần " + Util.numberToMoney(info.localCost) + " ngọc xanh.");
            return false;
        }

        buyer.inventory.gem -= info.localCost;
        Service.gI().sendMoney(buyer);

        Item newItem = createDisguiseItem(info.templateId);
        if (newItem == null) {
            buyer.inventory.gem += info.localCost;
            Service.gI().sendMoney(buyer);
            Service.gI().sendThongBao(buyer, "Không thể tạo cải trang. Đã hoàn lại ngọc.");
            return false;
        }

        addNonTradableOption(newItem);

        if (!InventoryService.gI().addItemBag(buyer, newItem)) {
            buyer.inventory.gem += info.localCost;
            Service.gI().sendMoney(buyer);
            Service.gI().sendThongBao(buyer, "Không thể thêm vào hành trang. Đã hoàn lại ngọc.");
            return false;
        }

        InventoryService.gI().sendItemBags(buyer);

        if (info.commission > 0 && seller.inventory != null) {
            try {
                seller.inventory.gem += info.commission;
                Service.gI().sendMoney(seller);
                Service.gI().sendThongBao(seller,
                        buyer.name + " đã mua cải trang từ bạn, bạn nhận "
                                + Util.numberToMoney(info.commission) + " ngọc xanh.");
            } catch (Exception e) {
                Logger.logException(DisguisePurchaseService.class, e);
            }
        }

        String displayName = extractDisguiseName(disguiseRef);
        Service.gI().sendThongBao(buyer,
                "Đã mua cải trang '" + displayName + "' với giá "
                        + Util.numberToMoney(info.localCost) + " ngọc xanh.");

        return true;
    }

    private Item createDisguiseItem(short templateId) {
        try {
            ItemShop itemShop = findItemShop(templateId);
            if (itemShop != null && itemShop.typeSell == 1) {
                return ItemService.gI().createItemFromItemShop(itemShop);
            } else {
                return ItemService.gI().createNewItem(templateId);
            }
        } catch (Exception e) {
            Logger.logException(DisguisePurchaseService.class, e);
            return null;
        }
    }

    private void addNonTradableOption(Item item) {
        if (item == null) {
            return;
        }

        if (item.itemOptions == null) {
            item.itemOptions = new java.util.ArrayList<>();
        }

        boolean hasOption154 = false;
        for (Item.ItemOption opt : item.itemOptions) {
            if (opt != null && opt.optionTemplate != null && opt.optionTemplate.id == 154) {
                hasOption154 = true;
                break;
            }
        }

        if (!hasOption154) {
            item.itemOptions.add(new Item.ItemOption(154, 0));
        }
    }

    public String extractDisguiseName(Item disguise) {
        if (disguise == null || disguise.template == null) {
            return "";
        }

        String name = disguise.template.name;
        String description = disguise.template.description;
        String extractedName = null;

        if (description != null && !description.isEmpty()) {
            try {
                String lowerDesc = description.toLowerCase();
                int idx = lowerDesc.indexOf("cải trang thành ");
                if (idx >= 0) {
                    int start = idx + "cải trang thành ".length();
                    int end = description.length();

                    int comma = description.indexOf(',', start);
                    int paren = description.indexOf('(', start);
                    int dot = description.indexOf('.', start);
                    int newline = description.indexOf('\n', start);

                    if (comma > 0) {
                        end = Math.min(end, comma);
                    }
                    if (paren > 0) {
                        end = Math.min(end, paren);
                    }
                    if (dot > 0) {
                        end = Math.min(end, dot);
                    }
                    if (newline > 0) {
                        end = Math.min(end, newline);
                    }

                    extractedName = description.substring(start, end).trim();
                }
            } catch (Exception e) {
                Logger.logException(DisguisePurchaseService.class, e);
            }
        }

        if (extractedName == null || extractedName.isEmpty()) {
            if (name != null && !name.isEmpty()) {
                try {
                    if (name.toLowerCase().startsWith("cải trang")) {
                        extractedName = name.substring("cải trang".length()).trim();
                        if (extractedName.isEmpty()) {
                            extractedName = name;
                        }
                    } else {
                        extractedName = name;
                    }
                } catch (Exception e) {
                    extractedName = name;
                }
            } else {
                extractedName = "";
            }
        }

        return extractedName != null ? extractedName : "";
    }
}
