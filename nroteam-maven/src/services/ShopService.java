package services;

import consts.ConstAchievement;
import consts.ConstEvent;
import data.AlyraManager;
import item.Item;
import item.Item.ItemOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import network.Message;
import npc.MagicTree;
import player.Inventory;
import player.Player;
import player.badges.BadgesData;
import player.badges.BadgesService;
import player.badges.BagesTemplate;
import services.func.BuyBackService;
import services.func.Input;
import services.map.NpcService;
import services.player.InventoryService;
import shop.ItemShop;
import shop.Shop;
import shop.TabShop;
import skill.Skill;
import task.BadgesTask;
import utils.Logger;
import utils.SkillUtil;
import utils.TimeUtil;
import server.Manager;
import services.func.UseItem;
import services.top.TopTrungThuService;
import services.top.TopWomenService;
import utils.Util;

public class ShopService {

    private static final byte COST_GOLD = 0;
    private static final byte COST_GEM = 1;
    private static final byte COST_RUBY = 3;
    private static final byte COST_COUPON = 4;

    private static final byte NORMAL_SHOP = 0;
    private static final byte SPEC_SHOP = 3;
    private static final byte KINANG_SHOP = 1;

    private static ShopService instance;

    public static ShopService gI() {
        if (ShopService.instance == null) {
            ShopService.instance = new ShopService();
        }
        return ShopService.instance;
    }

    public void opendShop(Player player, String tagName, boolean allGender) {

        switch (tagName) {
            case "ITEMS_LUCKY_ROUND" -> {
                openShopType4(player, tagName, player.inventory.itemsBoxCrackBall);
                return;
            }
            case "ITEMS_DABAN" -> {
                openShopType8(player, tagName, player.inventory.itemsDaBan);
                return;
            }
            case "ITEMS_MAIL_BOX" -> {
                return;
            }
            case "ITEMS_TOP_REWARD_VIP", "ITEMS_TOP_REWARD_LONGDEN" -> {
                openTopReward(player, tagName);
                return;
            }
            default -> {
            }
        }
        try {
            Shop shop = this.getShop(tagName);
            for (TabShop tabShop : shop.tabShops) {
                for (ItemShop item : tabShop.itemShops) {
                    if (item.temp.id == 1627) {
                        if (player.inventory.itemsBag.size() >= 35) {
                            item.cost = ((player.inventory.itemsBag.size() - 35) + 1) * 2;
                        } else {
                            item.cost = 1;
                        }
                    }
                }
            }
            shop = this.resolveShop(player, shop, allGender);
            switch (shop.typeShop) {
                case KINANG_SHOP ->
                    openShopType1(player, shop);
                case NORMAL_SHOP ->
                    openShopType0(player, shop);
                case SPEC_SHOP ->
                    openShopType3(player, shop);
            }
        } catch (Exception ex) {
            Service.gI().sendThongBao(player, ex.getMessage());
        }
    }

    private Shop getShop(String tagName) throws Exception {
        for (Shop s : Manager.SHOPS) {
            if (s.tagName != null && s.tagName.equals(tagName)) {
                return s;
            }
        }
        throw new Exception("Shop " + tagName + " không tồn tại!");
    }

    private Shop resolveShop(Player player, Shop shop, boolean allGender) {
        if (shop.tagName != null
                && (shop.tagName.equals("BUA_1H") || shop.tagName.equals("BUA_8H") || shop.tagName.equals("BUA_1M"))) {
            return this.resolveShopBua(player, new Shop(shop));
        }

        Shop filteredShop = allGender ? new Shop(shop) : new Shop(shop, player);

        if (InventoryService.gI().findItemBag(player, (short) 454) != null) {
            for (TabShop tabShop : filteredShop.tabShops) {
                tabShop.itemShops.removeIf(itemShop -> itemShop.temp.id == 454);
            }
        }
        if (player.haveTennisSpaceShip) {
            for (TabShop tabShop : filteredShop.tabShops) {
                tabShop.itemShops.removeIf(itemShop -> itemShop.temp.id == 453);
            }
        }

        long now = System.currentTimeMillis();
        boolean qua30Ngay = Util.isTimeDifferenceGreaterThanNDays(player.createTime, 30);
        boolean hetHanSKH = player.timeUpSKH <= now;
        boolean daMuaSKH = player.daMuaGiaHanSKH;

        if (qua30Ngay || daMuaSKH || hetHanSKH) {
            for (TabShop tabShop : filteredShop.tabShops) {
                tabShop.itemShops.removeIf(itemShop -> itemShop.temp.id == 1703 || itemShop.temp.id == 1717);
            }
        }

        if (InventoryService.gI().findItemBag(player, (short) 795) != null) {
            for (TabShop tabShop : filteredShop.tabShops) {
                tabShop.itemShops.removeIf(itemShop -> itemShop.temp.id == 795);
            }
        }

        boolean isSpecialPet = player.pet != null
                && (player.pet.typePet == 4 || player.pet.typePet == 5 || player.pet.typePet == 6);

        for (TabShop tabShop : filteredShop.tabShops) {
            if (tabShop == null || tabShop.itemShops == null) {
                continue;
            }
            tabShop.itemShops.removeIf(itemShop -> {
                if (itemShop == null || itemShop.temp == null) {
                    return false;
                }
                int id = itemShop.temp.id;
                if (id == 401) {
                    return player.pet == null || isSpecialPet;
                }
                if (id == 1890) {
                    return player.pet == null || !isSpecialPet;
                }
                return false; // không đụng các item khác
            });
        }
        return filteredShop;

    }

    private Shop resolveShopBua(Player player, Shop s) {
        for (TabShop tabShop : s.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                long min = 0;
                switch (item.temp.id) {
                    case 213 ->
                        min = (player.charms.tdTriTue - System.currentTimeMillis()) / 60000;
                    case 214 ->
                        min = (player.charms.tdManhMe - System.currentTimeMillis()) / 60000;
                    case 215 ->
                        min = (player.charms.tdDaTrau - System.currentTimeMillis()) / 60000;
                    case 216 ->
                        min = (player.charms.tdOaiHung - System.currentTimeMillis()) / 60000;
                    case 217 ->
                        min = (player.charms.tdBatTu - System.currentTimeMillis()) / 60000;
                    case 218 ->
                        min = (player.charms.tdDeoDai - System.currentTimeMillis()) / 60000;
                    case 219 ->
                        min = (player.charms.tdThuHut - System.currentTimeMillis()) / 60000;
                    case 522 ->
                        min = (player.charms.tdDeTu - System.currentTimeMillis()) / 60000;
                    case 671 ->
                        min = (player.charms.tdTriTue3 - System.currentTimeMillis()) / 60000;
                    case 672 ->
                        min = (player.charms.tdTriTue4 - System.currentTimeMillis()) / 60000;
                }
                if (min > 0) {
                    item.options.clear();
                    if (min >= 1440) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new Item.ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new Item.ItemOption(65, (int) min));
                    }
                }
            }
        }
        return s;
    }

    private void openShopType0(Player player, Shop shop) {
        if (shop == null || player == null || player.idMark == null) {
            // Logger.warning("openShopType0: shop or player is null");
            return;
        }
        player.idMark.setShopOpen(shop);
        player.idMark.setTagNameShop(shop.tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(NORMAL_SHOP);
            msg.writer().writeByte(shop.tabShops.size());
            for (TabShop tab : shop.tabShops) {
                msg.writer().writeUTF(tab.name);
                List<ItemShop> validItems = new ArrayList<>();
                for (ItemShop itemShop : tab.itemShops) {
                    if (itemShop != null && itemShop.temp != null && itemShop.temp.id > 0) {
                        system.Template.ItemTemplate template = ItemService.gI().getTemplate(itemShop.temp.id);
                        if (template != null) {
                            validItems.add(itemShop);
                        } 
                    }
                }
                msg.writer().writeByte(validItems.size());
                for (ItemShop itemShop : validItems) {
                    // Logger.log("openShopType0: Sending item templateId=" + itemShop.temp.id);
                    msg.writer().writeShort(itemShop.temp.id);
                    switch (itemShop.typeSell) {
                        case COST_GOLD -> {
                            msg.writer().writeInt(itemShop.cost);
                            msg.writer().writeInt(0);
                        }
                        case COST_GEM, COST_RUBY, COST_COUPON -> {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        }
                    }
                    msg.writer().writeByte(itemShop.options.size());
                    for (Item.ItemOption option : itemShop.options) {
                        msg.writer().writeShort(option.optionTemplate.id);
                        msg.writer().writeInt(option.param);
                    }
                    msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                    if (itemShop.temp.type == 5) {
                        msg.writer().writeByte(1);
                        msg.writer().writeShort(itemShop.temp.head);
                        msg.writer().writeShort(itemShop.temp.body);
                        msg.writer().writeShort(itemShop.temp.leg);
                        msg.writer().writeShort(-1);
                    } else {
                        msg.writer().writeByte(0);
                    }
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(ShopService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openShopType1(Player player, Shop shop) {
        if (shop == null || player == null || player.idMark == null) {
            return;
        }
        player.idMark.setShopOpen(shop);
        player.idMark.setTagNameShop(shop.tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(KINANG_SHOP);
            msg.writer().writeByte(shop.tabShops.size());
            // Logger.log("openShopType1: Shop type=1, tabs=" + shop.tabShops.size());
            for (TabShop tab : shop.tabShops) {
                msg.writer().writeUTF(tab.name);
                List<ItemShop> validItems = new ArrayList<>();
                for (ItemShop itemShop : tab.itemShops) {
                    if (itemShop != null && itemShop.temp != null && itemShop.temp.id > 0) {
                        system.Template.ItemTemplate template = ItemService.gI().getTemplate(itemShop.temp.id);
                        if (template != null) {
                            validItems.add(itemShop);
                        } 
                    }
                }
                msg.writer().writeByte(validItems.size());
                for (ItemShop itemShop : validItems) {
                    msg.writer().writeShort(itemShop.temp.id);
                    String[] subName = itemShop.temp.name.split("");
                    byte level = Byte.parseByte(subName[subName.length - 1]);
                    var skillTemplateId = SkillUtil.getTempSkillSkillByItemID(itemShop.temp.id);
                    var costPotential = SkillUtil.findSkillTemplate(skillTemplateId).skillss.stream()
                            .filter(s -> s.point == level)
                            .findFirst()
                            .map(s -> (int) s.powRequire)
                            .orElse(0);
                    msg.writer().writeLong(costPotential);
                    msg.writer().writeByte(itemShop.options.size());
                    for (Item.ItemOption option : itemShop.options) {
                        msg.writer().writeShort(option.optionTemplate.id);
                        msg.writer().writeInt(option.param);
                    }
                    msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(ShopService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openShopType3(Player player, Shop shop) {
        if (player == null || player.idMark == null || shop == null) {
            return;
        }
        player.idMark.setShopOpen(shop);
        player.idMark.setTagNameShop(shop.tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(SPEC_SHOP);
            msg.writer().writeByte(shop.tabShops.size());
            for (TabShop tab : shop.tabShops) {
                msg.writer().writeUTF(tab.name);
                List<ItemShop> validItems = new ArrayList<>();
                for (ItemShop itemShop : tab.itemShops) {
                    if (itemShop != null && itemShop.temp != null && itemShop.temp.id > 0) {
                        system.Template.ItemTemplate template = ItemService.gI().getTemplate(itemShop.temp.id);
                        if (template != null) {
                            validItems.add(itemShop);
                        } 
                    }
                }
                msg.writer().writeByte(validItems.size());
                for (ItemShop itemShop : validItems) {
                    // Logger.log("openShopType3: Sending item templateId=" + itemShop.temp.id + ", iconSpec=" + itemShop.iconSpec);
                    msg.writer().writeShort(itemShop.temp.id);
                    msg.writer().writeShort(itemShop.iconSpec);
                    msg.writer().writeInt(itemShop.cost);
                    msg.writer().writeByte(itemShop.options.size());
                    for (Item.ItemOption option : itemShop.options) {
                        msg.writer().writeShort(option.optionTemplate.id);
                        msg.writer().writeInt(option.param);
                    }
                    msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                    if (itemShop.temp.type == 5) {
                        msg.writer().writeByte(1);
                        msg.writer().writeShort(itemShop.temp.head);
                        msg.writer().writeShort(itemShop.temp.body);
                        msg.writer().writeShort(itemShop.temp.leg);
                        msg.writer().writeShort(-1);
                    } else {
                        msg.writer().writeByte(0);
                    }
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(ShopService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openShopType4(Player player, String tagName, List<Item> items) {
        if (items == null || player == null || player.idMark == null) {
            return;
        }
        player.idMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Phần\nthưởng");
            List<Item> validItems = new ArrayList<>();
            for (Item item : items) {
                if (item != null && item.template != null && item.template.id > 0) {
                    system.Template.ItemTemplate template = ItemService.gI().getTemplate(item.template.id);
                    if (template != null) {
                        validItems.add(item);
                    } 
                }
            }
            msg.writer().writeByte(validItems.size());
            for (Item item : validItems) {
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("LUCKY REWARD");
                msg.writer().writeByte(item.itemOptions.size() + 1);
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeShort(io.optionTemplate.id);
                    msg.writer().writeInt(io.param);
                }
                msg.writer().writeShort(31);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeByte(1);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(ShopService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openShopType8(Player player, String tagName, List<Item> items) {
        if (items == null || player == null || player.idMark == null) {
            return;
        }
        player.idMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(8);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Mua lại");
            List<Item> validItems = new ArrayList<>();
            for (Item item : items) {
                if (item != null && item.template != null && item.template.id > 0) {
                    system.Template.ItemTemplate template = ItemService.gI().getTemplate(item.template.id);
                    if (template != null) {
                        validItems.add(item);
                    } 
                }
            }
            msg.writer().writeByte(validItems.size());
            for (Item item : validItems) {
                int gemCost = item.template.gem / 2;
                int goldCost = gemCost == 0 ? Math.max(item.template.gold / 2, item.quantity * 100) : 0;
                msg.writer().writeShort(item.template.id);
                msg.writer().writeInt(goldCost);
                msg.writer().writeInt(gemCost);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeByte(item.itemOptions.size());
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeShort(io.optionTemplate.id);
                    msg.writer().writeInt(io.param);
                }
                msg.writer().writeByte(0);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(ShopService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void openTopReward(Player player, String tagName) {
        if (player == null || player.idMark == null) {
            return;
        }
        List<Item> items = player.topRewards.get(tagName);
        if (items == null) {
            return;
        }
        player.idMark.setTagNameShop(tagName);
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            List<Item> validItems = new ArrayList<>();
            for (Item it : items) {
                if (it != null && it.template != null && it.template.id > 0) {
                    system.Template.ItemTemplate template = ItemService.gI().getTemplate(it.template.id);
                    if (template != null) {
                        validItems.add(it);
                    } 
                }
            }
            String tabTitle = validItems.size() + " Vật\nphẩm";
            msg.writer().writeUTF(tabTitle);
            msg.writer().writeByte(validItems.size());
            int rank = 0;
            if ("ITEMS_TOP_REWARD_VIP".equals(tagName)) {
                rank = TopTrungThuService.gI().getRankVip(player);
            } else if ("ITEMS_TOP_REWARD_LONGDEN".equals(tagName)) {
                rank = TopTrungThuService.gI().getRankLongDen(player);
            }
            for (Item item : validItems) {
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("TOP #" + rank + " EVENT TRUNG THU (x" + item.quantity + ")");
                msg.writer().writeByte(item.itemOptions.size() + 1);
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeShort(io.optionTemplate.id);
                    msg.writer().writeInt(io.param);
                }
                msg.writer().writeShort(31);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeByte(1);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(ShopService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void takeItem(Player player, byte type, int tempId) throws Exception {
        if (player == null || player.idMark == null) {
            return;
        }
        String tagName = player.idMark.getTagNameShop();
        if (tagName == null || tagName.isEmpty()) {
            return;
        }
        switch (tagName) {
            case "ITEMS_LUCKY_ROUND" -> {
                getItemSideBoxLuckyRound(player, player.inventory.itemsBoxCrackBall, type, tempId);
                return;
            }
            case "ITEMS_DABAN" -> {
                buyItemDaBan(player, player.inventory.itemsDaBan, tempId);
                return;
            }
            case "BILL" -> {
                buyItemHD(player, tempId);
                return;
            }
            case "ITEMS_MAIL_BOX" -> {
                return;

            }
            case "ITEMS_TOP_REWARD_VIP", "ITEMS_TOP_REWARD_LONGDEN" -> {
                getItemTopReward(player, type, tempId);
                return;
            }
        }

        if (player.idMark.getShopOpen() == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        switch (tagName) {
            case "BUA_1H", "BUA_8H", "BUA_1M" ->
                buyItemBua(player, tempId);
            case "SANTA_HEAD" ->
                Service.gI().Send_Caitrang(player);
            default ->
                buyItem(player, tempId);
        }
        Service.gI().sendMoney(player);
    }

    private boolean subMoneyByItemShop(Player player, ItemShop is) {
        int gold = 0, gem = 0, ruby = 0, coupon = 0;

        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;
            default -> {
                Service.gI().sendThongBao(player, "Loại tiền không hợp lệ");
                return false;
            }
        }

        if (player.inventory.gold < gold) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng");
            return false;
        }

        if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBao(player, "Bạn không có đủ điểm");
            return false;
        }

        int needGemRuby = gem + ruby;
        if (needGemRuby > 0) {
            if (player.inventory.gem + player.inventory.ruby < needGemRuby) {
                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                return false;
            }
            int rubyToUse = Math.min(needGemRuby, player.inventory.ruby);
            int gemToUse = needGemRuby - rubyToUse;
            player.inventory.ruby -= rubyToUse;
            player.inventory.gem -= gemToUse;
        }

        player.inventory.gold -= gold;
        player.inventory.coupon -= coupon;
        return true;
    }

    private boolean subMoneyByItemShopV2(Player player, ItemShop is) {
        int gold = 0, gem = 0, ruby = 0, coupon = 0;
        switch (is.typeSell) {
            case COST_GOLD ->
                gold = is.cost;
            case COST_GEM ->
                gem = is.cost;
            case COST_RUBY ->
                ruby = is.cost;
            case COST_COUPON ->
                coupon = is.cost;
        }
        if (player.inventory.gold < gold) {
            Service.gI().sendThongBaoOK(player,
                    "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold - player.inventory.gold));
            return false;
        }
        if (player.inventory.gem < gem) {
            Service.gI().sendThongBaoOK(player,
                    "Bạn không đủ ngọc, còn thiếu " + Util.numberToMoney(gem - player.inventory.gem));
            return false;
        }
        if (player.inventory.ruby < ruby) {
            Service.gI().sendThongBaoOK(player,
                    "Bạn không đủ ngọc, còn thiếu " + Util.numberToMoney(ruby - player.inventory.ruby));
            return false;
        }
        if (player.inventory.coupon < coupon) {
            Service.gI().sendThongBaoOK(player,
                    "Bạn không đủ điểm, còn thiếu " + Util.numberToMoney(coupon - player.inventory.coupon));
            return false;
        }
        player.inventory.gold -= gold;
        player.inventory.gem -= gem;
        player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        Service.gI().sendMoney(player);
        return true;
    }

    private void buyItemBua(Player player, int itemTempId) {
        if (player == null || player.idMark == null) {
            return;
        }
        Shop shop = player.idMark.getShopOpen();
        if (shop == null) {
            return;
        }
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (subMoneyByItemShop(player, is)) {
            Item item = ItemService.gI().createItemFromItemShop(is);
            if (item != null) {
                InventoryService.gI().addItemBag(player, item);
                InventoryService.gI().sendItemBags(player);
                opendShop(player, shop.tagName, true);
            }
        }
    }

    private void learnKyNang(Player pl, ItemShop is) {
        if (pl.nPoint.power < is.temp.strRequire) {
            Service.gI().sendThongBao(pl,
                    "Bạn còn thiếu " + Util.numberToMoney(is.temp.strRequire - pl.nPoint.power) + " sức mạnh");
            return;
        }
        if (pl.LearnSkill != null && pl.LearnSkill.Time != -1) {
            Service.gI().sendThongBao(pl, "Bạn đang học 1 kỹ năng khác, hãy hoàn tất hoặc hủy trước.");
            return;
        }

        int skillTemplateId = SkillUtil.getTempSkillSkillByItemID(is.temp.id);
        if (skillTemplateId == -1) {
            Service.gI().sendThongBao(pl, "Không xác định được kỹ năng tương ứng.");
            return;
        }

        // Validate skill có thuộc gender của player không
        if (!SkillUtil.validateSkillBeforeLearn(pl, skillTemplateId)) {
            Service.gI().sendThongBao(pl, "Kỹ năng này không phù hợp với hành tinh của bạn!");
            Logger.warning("Player " + pl.name + " (gender: " + pl.gender +
                    ") cố gắng mua skill không hợp lệ: " + skillTemplateId);
            return;
        }

        Skill skillPlayer = pl.playerSkill.getSkillbyId(skillTemplateId);
        byte level = 1;
        try {
            String name = is.temp.name;
            for (int i = name.length() - 1; i >= 0; i--) {
                if (Character.isDigit(name.charAt(i))) {
                    level = Byte.parseByte(String.valueOf(name.charAt(i)));
                    break;
                }
            }
        } catch (NumberFormatException ignored) {
        }

        if (level < 1 || level > 7) {
            Service.gI().sendThongBao(pl, "Cấp kỹ năng không hợp lệ.");
            return;
        }
        if (skillPlayer != null && skillPlayer.point >= level) {
            Service.gI().sendThongBao(pl, "Bạn đã học kỹ năng này rồi.");
            return;
        }
        if ((skillPlayer == null || skillPlayer.point == 0) && level != 1) {
            Service.gI().sendThongBao(pl, "Vui lòng học cấp 1 trước!");
            return;
        }
        if (skillPlayer != null && level - skillPlayer.point != 1) {
            Service.gI().sendThongBao(pl, "Bạn chưa thể học kỹ năng này.");
            return;
        }

        long[] time = { 900_000L, 1_800_000L, 3_600_000L, 86_400_000L, 259_200_000L, 604_800_000L, 1_296_000_000L };
        long timeLong = time[level - 1];
        String timeStudy = switch (level) {
            case 1, 2 ->
                TimeUtil.convertMillisecondToMinute(timeLong);
            case 3 ->
                TimeUtil.convertMillisecondToHour(timeLong);
            default ->
                TimeUtil.convertMillisecondToDay(timeLong);
        };

        var template = SkillUtil.findSkillTemplate(skillTemplateId);
        if (template == null) {
            Service.gI().sendThongBao(pl, "Không tìm thấy dữ liệu kỹ năng.");
            return;
        }
        int potential = template.skillss.stream().findFirst().map(s -> (int) s.powRequire).orElse(0);
        if (pl.nPoint.tiemNang < potential) {
            Service.gI().sendThongBao(pl, "Bạn không đủ tiềm năng để học chiêu thức này.");
            return;
        }

        String text = "Con có muốn học kỹ năng " + template.name + " cấp " + level + "\nCần " + potential
                + " điểm tiềm năng và thời gian học là " + timeStudy;
        pl.LearnSkill.ItemTemplateSkillId = is.temp.id;
        pl.LearnSkill.Time = -1;
        pl.LearnSkill.Potential = potential;
        NpcService.gI().createMenuConMeo(pl, 671, NpcService.gI().getAvatar(13 + pl.gender), text, "Đồng ý", "Từ chối");
    }

    public void buyItem(Player player, int itemTempId) throws Exception {
        if (player == null || player.idMark == null) {
            return;
        }
        Shop shop = player.idMark.getShopOpen();
        if (shop == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy cửa hàng.");
            return;
        }
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện giao dịch.");
            return;
        }
        if (player.nPoint.power < is.temp.strRequire) {
            Service.gI().sendThongBao(player,
                    "Bạn còn thiếu " + Util.numberToMoney(is.temp.strRequire - player.nPoint.power) + " sức mạnh");
            return;
        }

        if (itemTempId == 1703 || itemTempId == 1717) {
            if (player.daMuaGiaHanSKH) {
                Service.gI().sendThongBao(player, "Bạn đã gia hạn set kích hoạt rồi, không thể mua thêm.");
                return;
            }
        }

        if (is.tabShop.id == 30) {
            Item pGG = InventoryService.gI().findItem(player.inventory.itemsBag, 459);
            if (pGG != null) {
                Item item = ItemService.gI().createItemFromItemShop(is);
                if (item != null && is.temp != null) {
                    InventoryService.gI().subQuantityItemsBag(player, pGG, 1);
                    InventoryService.gI().addItemBag(player, item);
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendThongBao(player, "Đổi thành công " + is.temp.name);
                }
            } else {
                Service.gI().sendThongBao(player, "Bạn không có phiếu giảm giá!");
            }
            return;
        }
        if (is.tabShop.id == 44) {
            buyDanhHieu(player, is);
            return;
        }
        if (is.tabShop.id == 45) {
            changeDanhHieu(player, is);
            return;
        }
        if (shop.typeShop == ShopService.KINANG_SHOP) {
            learnKyNang(player, is);
            return;
        }
        // if (is.tabShop.id == 49) {
        // if (player.event.getEventPoint() < is.cost) {
        // Service.gI().sendThongBao(player, "Không đủ điểm sự kiện!");
        // return;
        // }
        // player.event.subEventPoint(is.cost);
        // Item item = ItemService.gI().createNewItem((short) is.temp.id, 1);
        // InventoryService.gI().addItemBag(player, item);
        // InventoryService.gI().sendItemBags(player);
        // Service.gI().sendThongBao(player, "Đã đổi " + item.template.name + " bằng " +
        // is.cost + " điểm sự kiện.");
        // return;
        // }

        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }
        if (itemTempId == 711 && !InventoryService.gI().findItemSkinQuyLaoKame(player)) {
            Service.gI().sendThongBao(player, "Bạn phải có cải trang Quy Lão Kame mới có thể đổi.");
            return;
        }
        if ((itemTempId == 1524 || itemTempId == 1523 || itemTempId == 521)
                && !checkAutoTrainPurchase(player, itemTempId)) {
            return;
        }

        // Kiểm tra phiếu giảm giá cho shop SANTA_GIAM_GIA
        String tagName = player.idMark.getTagNameShop();
        if ("SANTA_GIAM_GIA".equals(tagName)) {
            Item pGG = InventoryService.gI().findItemBag(player, (short) 459);
            if (pGG == null || pGG.quantity < 1) {
                Service.gI().sendThongBao(player, "Bạn không có phiếu giảm giá!");
                return;
            }
        }

        if (shop.typeShop == ShopService.NORMAL_SHOP) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }
        } else if (shop.typeShop == ShopService.SPEC_SHOP) {
            if (!this.subIemByItemShop(player, is)) {
                return;
            }
        }

        Item item = ItemService.gI().createItemFromItemShop(is);
        if (item == null) {
            Service.gI().sendThongBao(player, "Không thể tạo vật phẩm.");
            return;
        }

        if (is.tabShop.id == 49) {
            boolean hasOpt231 = false;
            for (ItemOption o : item.itemOptions) {
                if (o.optionTemplate.id == 231) {
                    hasOpt231 = true;
                    break;
                }
            }
            if (hasOpt231) {
                for (int i = item.itemOptions.size() - 1; i >= 0; i--) {
                    if (item.itemOptions.get(i).optionTemplate.id == 231) {
                        item.itemOptions.remove(i);
                    }
                }
                boolean isPermanent = Util.isTrue(5, 100);
                ItemOption opt73 = null, opt93 = null;
                for (ItemOption o : item.itemOptions) {
                    if (o.optionTemplate.id == 73) {
                        opt73 = o;
                    }
                    if (o.optionTemplate.id == 93) {
                        opt93 = o;
                    }
                }
                if (isPermanent) {
                    if (opt93 != null) {
                        item.itemOptions.remove(opt93);
                    }
                    if (opt73 == null) {
                        item.itemOptions.add(new ItemOption(73, 0));
                    }
                } else {
                    int roll = Util.nextInt(1, 3);
                    int days = (roll == 1) ? 7 : (roll == 2 ? 15 : 30);
                    if (opt73 != null) {
                        item.itemOptions.remove(opt73);
                    }
                    if (opt93 != null) {
                        opt93.param = days;
                    } else {
                        item.itemOptions.add(new ItemOption(93, days));
                    }
                }
            }
        }

        int[][] listDauThan = { { 13, 293 }, { 60, 294 }, { 61, 295 }, { 62, 296 }, { 63, 297 }, { 64, 298 },
                { 65, 299 }, { 352, 596 }, { 523, 597 } };
        item = buyMagicPean(player, listDauThan, item);
        if (item.template.id == 1523 || item.template.id == 1524) {
            item = ItemService.gI().createNewItem((short) 521);
            item.itemOptions.addAll(is.options);
        }

        if (itemTempId == 1703 || itemTempId == 1717) {
            if (player.daMuaGiaHanSKH) {
                Service.gI().sendThongBao(player, "Bạn đã gia hạn set kích hoạt rồi, không thể mua thêm.");
                return;
            }
            int addDays = (itemTempId == 1703 ? 7 : 15);
            UseItem.gI().giaHanKichHoat(player, addDays);
            player.daMuaGiaHanSKH = true;
            AlyraManager.executeUpdate("UPDATE player SET da_mua_gia_han_skh = 1 WHERE id = " + player.id);
            return;
        }

        // Xử lý shop giảm giá SANTA_GIAM_GIA: thêm option 154 trước khi thêm vào hành
        // trang
        if ("SANTA_GIAM_GIA".equals(tagName)) {
            // Thêm option 154 để không thể bán lại
            if (!item.isHaveOption(154)) {
                item.itemOptions.add(new Item.ItemOption(154, 0));
            }
        }

        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);

        if (is.tabShop.id == 49 && item.template != null && item.template.id == 1910) {
            TopTrungThuService.gI().addVipPoint(player, 1);
        }
        // Halloween: cộng điểm Top Thiệp Halloween khi mua item id 1117 tại tab shop id
        // 54
        if (is.tabShop.id == 54 && item.template != null && item.template.id == 1117) {
            services.top.TopHalloweenService.gI().addCardPoint(player, 1);

        }
        if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_20_10) {
            if (is.tabShop.id == 56 && item.template != null && item.template.id == 1718) {
                TopWomenService.gI().addThiepPoint(player, 1);
            }
            if (is.tabShop.id == 56 && item.template != null && item.template.id == 1719) {
                TopWomenService.gI().addHopQuaPoint(player, 1);
            }
            if (is.tabShop.id == 49 && item.template != null && item.template.id == 1928) {
                TopWomenService.gI().addCapsulePoint(player, 1);
            }
        }

        if (itemTempId == 1523 || itemTempId == 1524 || itemTempId == 521) {
            updateAutoTrainPurchase(player, itemTempId);
        }

        // Xử lý shop giảm giá SANTA_GIAM_GIA: trừ phiếu giảm giá sau khi mua thành công
        if ("SANTA_GIAM_GIA".equals(tagName)) {
            // Trừ phiếu giảm giá (đã kiểm tra ở trên)
            Item pGG = InventoryService.gI().findItemBag(player, (short) 459);
            if (pGG != null && pGG.quantity >= 1) {
                InventoryService.gI().subQuantityItemsBag(player, pGG, 1);
                InventoryService.gI().sendItemBags(player);
            }
        }

    }

    private boolean checkAutoTrainPurchase(Player player, int itemTempId) {
        int state = player.autoTrainState;
        if (itemTempId == 1524 && state != 2) {
            Service.gI().sendThongBao(player, "Bạn cần mua Tự động luyện tập 2 trước!");
            return false;
        }
        if (itemTempId == 1523 && state != 1) {
            Service.gI().sendThongBao(player, "Bạn cần mua Tự động luyện tập 1 trước!");
            return false;
        }
        return true;
    }

    private void updateAutoTrainPurchase(Player player, int itemTempId) {
        switch (itemTempId) {
            case 1524 ->
                player.autoTrainState = 0;
            case 521 -> {
                if (player.autoTrainState != 2) {
                    player.autoTrainState = 1;
                }
            }
            case 1523 ->
                player.autoTrainState = 2;
        }
    }

    private void buyDanhHieu(Player pl, ItemShop is) {
        int idBadgesCanBuy = BagesTemplate.fineIdEffectbyIdItem(is.temp.id);

        // Kiểm tra đã sở hữu badge chưa
        if (pl.dataBadges.stream().anyMatch(bg -> bg.idBadGes == idBadgesCanBuy)) {
            Service.gI().sendThongBao(pl, "Bạn đã sở hữu danh hiệu này rồi.");
            return;
        }

        // Tìm task tương ứng với badge này
        BadgesTask task = pl.dataTaskBadges.stream()
                .filter(data -> data.idBadgesReward == idBadgesCanBuy)
                .findFirst().orElse(null);

        if (task != null) {
            // Kiểm tra task đã hoàn thành CHÍNH XÁC chưa
            if (task.isExactlyDone() && !task.badgeClaimed) {
                // Lấy số ngày từ option 93 trong database
                // Nếu không có option 93 → vĩnh viễn (-1)
                int days = BagesTemplate.getDaysFromBadgeOptions(idBadgesCanBuy);
                new BadgesData(pl, idBadgesCanBuy, days);

                // Đánh dấu đã claim và reset count
                task.badgeClaimed = true;
                task.count = 0;

                // Cập nhật stats ngay lập tức (vì BadgesData constructor tự động set
                // isUse=true)
                pl.nPoint.calPoint();

                Service.gI().sendThongBao(pl, "Chúc mừng bạn đã nhận được danh hiệu: " + is.temp.name);
            } else if (task.badgeClaimed) {
                Service.gI().sendThongBao(pl, "Bạn đã nhận danh hiệu này rồi.");
            } else {
                String taskName = Manager.TASKS_BADGES_TEMPLATE.stream()
                        .filter(btt -> btt.idbadgesReward == idBadgesCanBuy)
                        .findFirst().map(btt -> btt.name).orElse("N/A");
                String thongBao = "Bạn chưa hoàn thành yêu cầu.\n" + "Nhiệm vụ: " + taskName + "\n" + "Tiến độ: "
                        + task.count + " / " + task.countMax;
                NpcService.gI().createTutorial(pl, 2993, thongBao);
            }
        } else {
            Service.gI().sendThongBao(pl, "Không tìm thấy nhiệm vụ cho danh hiệu này.");
        }
    }

    private void changeDanhHieu(Player pl, ItemShop is) {
        long remainingTime = pl.lastTimeChangeBadges - System.currentTimeMillis();
        if (remainingTime > 0) {
            Service.gI().sendThongBao(pl, "Vui lòng đợi " + remainingTime / 1000 + " giây nữa");
            return;
        }

        int idBadgesEffect = BagesTemplate.fineIdEffectbyIdItem(is.temp.id);

        // Tìm danh hiệu trong danh sách
        BadgesData targetBadge = null;
        for (BadgesData bg : pl.dataBadges) {
            if (bg.idBadGes == idBadgesEffect) {
                targetBadge = bg;
                break;
            }
        }

        // Kiểm tra tồn tại
        if (targetBadge == null) {
            Service.gI().sendThongBao(pl, "Bạn không sở hữu danh hiệu này.");
            return;
        }

        // Kiểm tra hết hạn
        if (targetBadge.timeofUseBadges < System.currentTimeMillis()) {
            Service.gI().sendThongBao(pl, "Danh hiệu này đã hết hạn sử dụng.");
            pl.lastTimeChangeBadges = System.currentTimeMillis() + 30000;
            return;
        }

        // Toggle bật/tắt danh hiệu
        boolean isActivated = BadgesService.toggleBadges(pl, idBadgesEffect);

        // Cập nhật lại stats ngay lập tức
        pl.nPoint.calPoint();

        // Thông báo cho player
        if (isActivated) {
            Service.gI().sendThongBao(pl, "Đã bật danh hiệu " + is.temp.name);
        } else {
            Service.gI().sendThongBao(pl, "Đã tắt danh hiệu " + is.temp.name);
        }

        pl.lastTimeChangeBadges = System.currentTimeMillis() + 30000;
    }

    private boolean subIemByItemShop(Player pl, ItemShop itemShop) {
        short itSpecId = ItemService.gI().getItemIdByIcon((short) itemShop.iconSpec);
        int cost = itemShop.cost;
        Item itSpecTemplate = ItemService.gI().createNewItem(itSpecId);
        switch (itSpecTemplate.template.id) {
            case 76, 188, 189, 190 -> {
                if (pl.inventory.gold >= cost) {
                    pl.inventory.gold -= cost;
                    return true;
                }
                Service.gI().sendThongBao(pl, "Bạn không đủ vàng.");
                return false;
            }
            case 861 -> {
                if (pl.inventory.gem >= cost) {
                    pl.inventory.gem -= cost;
                    return true;
                }
                Service.gI().sendThongBao(pl, "Bạn không đủ ngọc.");
                return false;
            }
            default -> {
                Item itemInBag = InventoryService.gI().findItemBag(pl, itSpecId);
                if (itemInBag == null || !itemInBag.isNotNullItem()) {
                    Service.gI().sendThongBao(pl, "Không tìm thấy " + itSpecTemplate.template.name);
                    return false;
                }
                if (itemInBag.quantity < cost) {
                    Service.gI().sendThongBao(pl, "Bạn không có đủ " + cost + " " + itSpecTemplate.template.name);
                    return false;
                }
                InventoryService.gI().subQuantityItemsBag(pl, itemInBag, cost);
                return true;
            }
        }
    }

    public void showConfirmSellItem(Player pl, int where, int index) {
        if (index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        Item item;
        if (where == 0) {
            item = pl.inventory.itemsBody.get(index);
        } else {
            if (pl.getSession().version < 220) {
                index -= (pl.inventory.itemsBody.size() - 7);
            }
            item = pl.inventory.itemsBag.get(index);
        }

        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 570) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            if (item.isHaveOption(154)) {
                Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
                return;
            }
            int quantity = item.quantity;
            if (item.template.id == 457) {
                Input.gI().createFormBanSLL(pl);
                return;
            }
            int cost = Math.max(1, item.template.gold / 4) * quantity;
            String text = "Bạn có muốn bán\nx" + quantity + " " + item.template.name + "\nvới giá là "
                    + Util.numberToMoney(cost) + " vàng?";
            Message msg = null;
            try {
                msg = new Message(7);
                msg.writer().writeByte(where);
                msg.writer().writeShort(index);
                msg.writer().writeUTF(text);
                pl.sendMessage(msg);
            } catch (IOException e) {
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void sellItem(Player pl, int where, int index) {
        if (pl == null || pl.idMark == null || pl.idMark.getShopOpen() == null || pl.idMark.getTagNameShop() == null
                || index < 0) {
            if (pl != null) {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
            return;
        }

        // Xử lý version cũ nếu cần (tương tự showConfirmSellItem)
        int actualIndex = index;
        if (where == 1 && pl.getSession().version < 220) {
            actualIndex = index - (pl.inventory.itemsBody.size() - 7);
        }

        // Kiểm tra index hợp lệ
        List<Item> targetList = (where == 0) ? pl.inventory.itemsBody : pl.inventory.itemsBag;
        if (actualIndex < 0 || actualIndex >= targetList.size()) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }

        Item item = targetList.get(actualIndex);

        if (item == null || !item.isNotNullItem()) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }

        if (item.template.id == 570 || InventoryService.gI().getParam(pl, 93, item.template.id) > 0) {
            Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
            return;
        }
        if (item.isHaveOption(154)) {
            Service.gI().sendThongBao(pl, "Bạn không thể bán vật phẩm này");
            return;
        }
        int quantity = item.quantity;
        int cost = item.template.gold;
        if (item.template.id == 457) {
            quantity = 1;
        } else {
            cost /= 4;
        }
        cost = Math.max(1, cost) * quantity;

        if (pl.inventory.gold > Inventory.LIMIT_GOLD - cost) {
            Service.gI().sendThongBao(pl, "Vàng sau khi bán vượt quá giới hạn");
            return;
        }

        pl.inventory.gold += cost;
        Service.gI().sendMoney(pl);
        Service.gI().sendThongBao(pl,
                "Đã bán " + item.template.name + " thu được " + Util.numberToMoney(cost) + " vàng");

        if (item.template.id != 457) {
            BuyBackService.gI().addItem(pl, item);
        }

        // Sử dụng phương thức mới với index để đảm bảo chính xác vị trí item
        if (where == 0) {
            InventoryService.gI().subQuantityItemsBodyByIndex(pl, actualIndex, quantity);
            InventoryService.gI().sendItemBody(pl);
            Service.gI().Send_Caitrang(pl);
        } else {
            InventoryService.gI().subQuantityItemsBagByIndex(pl, actualIndex, quantity);
            InventoryService.gI().sendItemBags(pl);
        }

        if (pl.idMark != null) {
            String tagName = pl.idMark.getTagNameShop();
            if ("BUNMA".equals(tagName) || "DENDE".equals(tagName) || "APPULE".equals(tagName)) {
                AchievementService.gI().checkDoneTask(pl, ConstAchievement.TRUM_NHAT_VE_CHAI);
            }
        }
    }

    private void getItemSideBoxLuckyRound(Player player, List<Item> items, byte type, int index) {
        if (player == null || player.idMark == null || items == null || index < 0 || index >= items.size()) {
            if (player != null) {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
            return;
        }

        switch (type) {
            case 0 -> {
                Item item = items.get(index);
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        InventoryService.gI().addItemBag(player, item);
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được "
                                        + (item.template.id == 189 ? Util.numberToMoney(item.quantity) + " vàng"
                                                : item.template.name));
                        InventoryService.gI().sendItemBags(player);
                        items.remove(index);
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đã đầy");
                    }
                }
            }
            case 1 -> {
                items.remove(index);
                Service.gI().sendThongBao(player, "Xóa vật phẩm thành công");
            }
            case 2 -> {
                for (int i = items.size() - 1; i >= 0; i--) {
                    Item item = items.get(i);
                    if (InventoryService.gI().addItemBag(player, item)) {
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được "
                                        + (item.template.id == 189 ? Util.numberToMoney(item.quantity) + " vàng"
                                                : item.template.name));
                        items.remove(i);
                    }
                }
                InventoryService.gI().sendItemBags(player);
            }
        }
        if (player.idMark.getTagNameShop() != null) {
            openShopType4(player, player.idMark.getTagNameShop(), items);
        }
    }

    private void getItemTopReward(Player player, byte type, int index) {
        if (player == null || player.idMark == null) {
            return;
        }
        String tagName = player.idMark.getTagNameShop();
        if (tagName == null) {
            return;
        }
        List<Item> items = player.topRewards.get(tagName);
        if (items == null || index < 0 || index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }

        switch (type) {
            case 0 -> {
                Item item = items.get(index);
                if (item.isNotNullItem()) {
                    if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                        InventoryService.gI().addItemBag(player, item);
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được "
                                        + (item.template.id == 189 ? Util.numberToMoney(item.quantity) + " vàng"
                                                : item.template.name));
                        InventoryService.gI().sendItemBags(player);
                        items.remove(index);
                    } else {
                        Service.gI().sendThongBao(player, "Hành trang đã đầy");
                    }
                }
            }
            case 1 -> {
                items.remove(index);
                Service.gI().sendThongBao(player, "Xóa vật phẩm thành công");
            }
            case 2 -> {
                for (int i = items.size() - 1; i >= 0; i--) {
                    Item item = items.get(i);
                    if (InventoryService.gI().addItemBag(player, item)) {
                        Service.gI().sendThongBao(player,
                                "Bạn nhận được "
                                        + (item.template.id == 189 ? Util.numberToMoney(item.quantity) + " vàng"
                                                : item.template.name));
                        items.remove(i);
                    }
                }
                InventoryService.gI().sendItemBags(player);
            }
        }
        if (player.idMark.getTagNameShop() != null) {
            openTopReward(player, player.idMark.getTagNameShop());
        }
    }

    private void buyItemDaBan(Player player, List<Item> items, int index) {
        if (player == null || player.idMark == null || items == null || index >= items.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = items.get(index);
        int gemCost = item.template.gem / 2;
        int goldCost = gemCost == 0 ? Math.max(item.template.gold / 2, item.quantity * 100) : 0;

        if (player.inventory.gold < goldCost) {
            Service.gI().sendThongBao(player, "Bạn không có đủ vàng!");
            return;
        }
        if (player.inventory.gem < gemCost) {
            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc xanh!");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy");
            return;
        }
        player.inventory.gem -= gemCost;
        player.inventory.gold -= goldCost;
        Service.gI().sendMoney(player);
        InventoryService.gI().addItemBag(player, item);
        Service.gI().sendThongBao(player, "Bạn đã mua lại " + item.template.name);
        InventoryService.gI().sendItemBags(player);
        items.remove(index);

        if (player.idMark.getTagNameShop() != null) {
            openShopType8(player, player.idMark.getTagNameShop(), items);
        }
    }

    // private void buyItemHD(Player player, int itemTempId) {
    // Shop shop = player.idMark.getShopOpen();
    // ItemShop is = shop.getItemShop(itemTempId);
    // if (is == null) {
    // Service.gI().sendThongBao(player, "Không thể thực hiện");
    // return;
    // }
    // if (InventoryService.gI().getCountEmptyBag(player) < 1) {
    // Service.gI().sendThongBao(player, "Hành trang đã đầy.");
    // return;
    // }
    // if (!subMoneyByItemShopV2(player, is)) {
    // return;
    // }

    // Item item = ItemService.gI().createItemFromItemShop(is);
    // if (item.template.level == 14) {
    // Item doAn = player.inventory.itemsBag
    // .stream().filter(it -> it != null && it.template != null
    // && (it.template.id >= 663 && it.template.id <= 667) && it.quantity >= 99)
    // .findFirst().orElse(null);
    // if (doAn != null) {
    // InventoryService.gI().subQuantityItemsBag(player, doAn, 99);
    // } else {
    // Service.gI().sendThongBao(player, "Không có đủ thức ăn");
    // return;
    // }
    // }
    // if (player.inventory.itemsBody.stream().anyMatch(it -> it != null &&
    // it.isNotNullItem())) {
    // if (player.inventory.itemsBody.stream()
    // .noneMatch(it -> it != null && it.template != null && it.template.level ==
    // 13)) {
    // Service.gI().sendThongBao(player, "Không có đủ set thần");
    // return;
    // }
    // }
    // int param = 0;
    // if (item.template.level == 14) {
    // int random = Util.nextInt(1, 100);
    // if (random <= 1) {
    // param = 15;
    // } else if (random <= 15) {
    // param = Util.nextInt(11, 14);
    // } else if (random <= 35) {
    // param = Util.nextInt(7, 10);
    // } else if (random <= 60) {
    // param = Util.nextInt(4, 6);
    // } else {
    // param = Util.nextInt(0, 3);
    // }
    // }

    // List<ItemOption> itemoptions = new ArrayList<>();
    // if (!item.itemOptions.isEmpty()) {
    // for (ItemOption ios : item.itemOptions) {
    // if (item.template.level == 14 &&
    // InventoryService.gI().optionCanUpgrade(ios.optionTemplate.id)
    // && param > 0) {
    // itemoptions.add(new ItemOption(ios.optionTemplate.id, ios.param + (ios.param
    // * param) / 100));
    // } else if (ios.optionTemplate.id != 164) {
    // itemoptions.add(new ItemOption(ios.optionTemplate.id, ios.param));
    // }
    // }
    // } else {
    // itemoptions.add(new ItemOption(73, (short) 0));
    // }
    // itemoptions.add(new ItemOption(30, (short) 0));
    // item.itemOptions.clear();
    // item.itemOptions.addAll(itemoptions);
    // InventoryService.gI().addItemBag(player, item);
    // InventoryService.gI().sendItemBags(player);
    // Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
    // }

    private void buyItemHD(Player player, int itemTempId) {
        if (player == null || player.idMark == null) {
            return;
        }
        Shop shop = player.idMark.getShopOpen();
        if (shop == null) {
            return;
        }
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }

        Item item = ItemService.gI().createItemFromItemShop(is);
        if (item == null || item.template == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (item.template.level == 14) {

            // ===== Kiểm tra đủ 5 món Thần Linh =====
            int[][] setThanIds = {
                    { 555, 556, 562, 563, 561 },
                    { 557, 558, 564, 565, 561 },
                    { 559, 560, 566, 567, 561 }
            };
            int[] requiredIds = setThanIds[player.gender];

            boolean hasFullSet = true;
            for (int id : requiredIds) {
                Item bodyItem = InventoryService.gI().findItemBody(player, id);
                Item bagItem = InventoryService.gI().findItemBag(player, id);
                if (bodyItem == null && bagItem == null) {
                    hasFullSet = false;
                    break;
                }
            }

            if (!hasFullSet) {
                Service.gI().sendThongBao(player,
                        "Con cần mang đủ 5 món Thần Linh (trên người hoặc trong hành trang).");
                return;
            }
            // ===== Kiểm tra thức ăn =====
            Item doAn = player.inventory.itemsBag.stream()
                    .filter(it -> it != null && it.template != null
                            && (it.template.id >= 663 && it.template.id <= 667)
                            && it.quantity >= 99)
                    .findFirst().orElse(null);

            if (doAn == null) {
                Service.gI().sendThongBao(player, "Không có đủ thức ăn");
                return;
            }
            if (!subMoneyByItemShopV2(player, is)) {
                return;
            }

            // ===== Trừ thức ăn =====
            InventoryService.gI().subQuantityItemsBag(player, doAn, 99);

            // ===== Trừ món Thần Linh tương ứng =====
            int[][][] items = {
                    { { 555 }, { 556 }, { 562 }, { 563 }, { 561 } },
                    { { 557 }, { 558 }, { 564 }, { 565 }, { 561 } },
                    { { 559 }, { 560 }, { 566 }, { 567 }, { 561 } }
            };

            int idThan = items[player.gender][is.temp.type][0];
            Item thanBody = InventoryService.gI().findItemBody(player, idThan);
            Item thanBag = InventoryService.gI().findItemBag(player, idThan);

            if (thanBody != null) {
                InventoryService.gI().subQuantityItemsBody(player, thanBody, 1);
                InventoryService.gI().sendItemBody(player);
            } else if (thanBag != null) {
                InventoryService.gI().subQuantityItemsBag(player, thanBag, 1);
                InventoryService.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Không có món Thần tương ứng để đổi đồ Hủy Diệt");
                return;
            }
        } else {

            if (!subMoneyByItemShopV2(player, is)) {
                return;
            }
        }
        int param = 0;
        if (item.template.level == 14) {
            int random = Util.nextInt(1, 100);
            if (random <= 1) {
                param = 15;
            } else if (random <= 15) {
                param = Util.nextInt(11, 14);
            } else if (random <= 35) {
                param = Util.nextInt(7, 10);
            } else if (random <= 60) {
                param = Util.nextInt(4, 6);
            } else {
                param = Util.nextInt(0, 3);
            }
        }
        List<ItemOption> itemoptions = new ArrayList<>();
        if (!item.itemOptions.isEmpty()) {
            for (ItemOption ios : item.itemOptions) {
                if (item.template.level == 14 && InventoryService.gI().optionCanUpgrade(ios.optionTemplate.id)
                        && param > 0) {
                    itemoptions.add(new ItemOption(ios.optionTemplate.id,
                            ios.param + (ios.param * param) / 100));
                } else if (ios.optionTemplate.id != 164) {
                    itemoptions.add(new ItemOption(ios.optionTemplate.id, ios.param));
                }
            }
        } else {
            itemoptions.add(new ItemOption(73, (short) 0));
        }

        itemoptions.add(new ItemOption(30, (short) 0));
        item.itemOptions.clear();
        item.itemOptions.addAll(itemoptions);

        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);

        Service.gI().sendThongBao(player, "Mua thành công " + is.temp.name);
    }

    private Item buyMagicPean(Player player, int[][] listDauThan, Item item) {
        for (int[] dauThan : listDauThan) {
            if (item.template.id == dauThan[1]) {
                Item newItem = ItemService.gI().createNewItem((short) dauThan[0]);
                newItem.itemOptions.add(new Item.ItemOption(player.magicTree.level > 2 ? 2 : 48,
                        MagicTree.PEA_PARAM[player.magicTree.level - 1]));
                newItem.quantity = 30;
                return newItem;
            }
        }
        return item;
    }
}
