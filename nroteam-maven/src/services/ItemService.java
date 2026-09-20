package services;

import combine.CombineService;
import item.Item;
import item.Item.ItemOption;
import java.util.*;
import map.ItemMap;
import map.Zone;
import player.Player;
import server.Manager;
import services.player.InventoryService;
import shop.ItemShop;
import system.Template;
import system.Template.ItemOptionTemplate;
import system.Template.ItemTemplate;
import utils.Util;

public class ItemService {

    private static ItemService i;

    public static ItemService gI() {
        if (i == null) {
            i = new ItemService();
        }
        return i;
    }

    public short getItemIdByIcon(short IconID) {
        for (int i = 0; i < Manager.ITEM_TEMPLATES.size(); i++) {
            if (Manager.ITEM_TEMPLATES.get(i).iconID == IconID) {
                return Manager.ITEM_TEMPLATES.get(i).id;
            }
        }
        return -1;
    }

    public Item createItemNull() {
        Item item = new Item();
        return item;
    }

    public Item createItemFromItemShop(ItemShop itemShop) {
        if (itemShop == null || itemShop.temp == null) {
            return null;
        }
        Item item = new Item();
        item.template = itemShop.temp;
        item.quantity = 1;
        if (item.itemOptions == null) {
            item.itemOptions = new ArrayList<>();
        }
        if (itemShop.options != null) {
            for (Item.ItemOption io : itemShop.options) {
                item.itemOptions.add(new Item.ItemOption(io));
            }
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item copyItem(Item item) {
        Item it = new Item();
        it.itemOptions = new ArrayList<>();
        it.template = item.template;
        it.info = item.info;
        it.content = item.content;
        it.quantity = item.quantity;
        it.createTime = item.createTime;
        it.auditTraceId = item.auditTraceId;
        for (Item.ItemOption io : item.itemOptions) {
            it.itemOptions.add(new Item.ItemOption(io));
        }
        return it;
    }

    public Item createNewItem(short templateId) {
        return createNewItem(templateId, 1);
    }

    public Item createNewItem(short templateId, int quantity) {
        if (quantity <= 0) {
            return null;
        }
        ItemTemplate template = getTemplate(templateId);
        if (template == null) {
            return null;
        }
        Item newItem = new Item();
        newItem.template = template;
        newItem.quantity = quantity;
        if (newItem.itemOptions == null) {
            newItem.itemOptions = new ArrayList<>();
        } else {
            newItem.itemOptions.clear();
        }
        return newItem;
    }

    public Item otpts(short tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.createTime = System.currentTimeMillis();
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(47, Util.nextInt(2000, 2500)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(22, Util.nextInt(150, 200)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(0, Util.nextInt(18000, 20000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(23, Util.nextInt(150, 200)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(21, 80));
            item.itemOptions.add(new ItemOption(14, Util.nextInt(20, 25)));
        }
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemSetKichHoat(int tempId, int quantity) {
        Item item = new Item();
        item.template = getTemplate(tempId);
        item.quantity = quantity;
        item.itemOptions = createItemNull().itemOptions;
        item.createTime = System.currentTimeMillis();
        item.content = item.getContent();
        item.info = item.getInfo();
        return item;
    }

    public Item createItemFromItemMap(ItemMap itemMap) {
        if (itemMap == null || itemMap.itemTemplate == null) {
            return null;
        }
        Item item = createNewItem(itemMap.itemTemplate.id, itemMap.quantity);
        if (item == null) {
            return null;
        }
        if (itemMap.options != null) {
            item.itemOptions = itemMap.options;
        }
        item.auditTraceId = itemMap.auditTraceId;
        return item;
    }

    public ItemOptionTemplate getItemOptionTemplate(int id) {
        return Manager.ITEM_OPTION_TEMPLATES.get(id);
    }

    public Template.ItemTemplate getTemplate(int id) {
        for (Template.ItemTemplate template : Manager.ITEM_TEMPLATES) {
            if (template != null && template.id == id) {
                return template;
            }
        }
        return null;
    }

    public int getPercentTrainArmor(Item item) {
        if (item != null) {
            return switch (item.template.id) {
                case 529, 534 ->
                    10;
                case 530, 535 ->
                    20;
                case 531, 536 ->
                    30;
                case 1716 ->
                    40;
                default ->
                    0;
            };
        } else {
            return 0;
        }
    }

    public boolean isTrainArmor(Item item) {
        if (item != null) {
            switch (item.template.id) {
                case 529:
                case 534:
                case 530:
                case 535:
                case 531:
                case 536:
                case 1716:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Kiểm tra item có hết hạn không (option 93)
     * Tính toán chính xác dựa trên timestamp thay vì số ngày đã qua
     */
    public boolean isOutOfDateTime(Item item) {
        if (item == null) {
            return false;
        }

        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 93) {
                long expiryTimestamp = item.createTime + ((long) io.param * 24L * 60L * 60L * 1000L);
                long currentTime = System.currentTimeMillis();
                if (currentTime >= expiryTimestamp) {
                    return true; // Hết hạn
                }
                long remainingMillis = expiryTimestamp - currentTime;
                int remainingDays = (int) Math.ceil(remainingMillis / (24.0 * 60.0 * 60.0 * 1000.0));
                if (remainingDays != io.param) {
                    io.param = remainingDays;
                    item.createTime = currentTime - ((long) remainingDays * 24L * 60L * 60L * 1000L) + remainingMillis;
                }
            }
        }
        return false;
    }

    public void OpenItem736(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 50;
            int ruby = 20;
            int dbv = 10;
            int vb = 10;
            int bh = 5;
            int ct = 5;
            Item item = randomRac();
            if (rd <= rac) {
                item = randomRac();
            } else if (rd <= rac + ruby) {
                item = createItemSetKichHoat(77, 1);
            } else if (rd <= rac + ruby + dbv) {
                item = daBaoVe();
            } else if (rd <= rac + ruby + dbv + vb) {
                item = vanBay2011(true);
            } else if (rd <= rac + ruby + dbv + vb + bh) {
                item = phuKien2011(true);
            } else if (rd <= rac + ruby + dbv + vb + bh + ct) {
                item = caitrang2011(true);
            }
            if (item.template.id == 77) {
                item.quantity = Util.nextInt(1, 2);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            player.inventory.event++;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenItem648(Player player, Item itemUse) {
        try {
            if (InventoryService.gI().getCountEmptyBag(player) <= 1) {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            int rd = Util.nextInt(1, 100);
            int rac = 50;
            int ruby = 20;
            int dbv = 10;
            int vb = 10;
            int bh = 5;
            int ct = 5;
            Item item = randomRac();
            if (rd <= rac) {
                item = randomRac2();
            } else if (rd <= rac + ruby) {
                item = createItemSetKichHoat(77, 1);
            } else if (rd <= rac + ruby + dbv) {
                item = vatphamsk(true);
            } else if (rd <= rac + ruby + dbv + vb) {
                item = vanBayChrimas(true);
            } else if (rd <= rac + ruby + dbv + vb + bh) {
                item = phuKienChristmas(true);
            } else if (rd <= rac + ruby + dbv + vb + bh + ct) {
                item = caitrangChristmas(true);
            }
            if (item.template.id == 77) {
                item.quantity = Util.nextInt(1, 2);
            }
            icon[0] = itemUse.template.iconID;
            icon[1] = item.template.iconID;
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            player.inventory.event++;
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineService.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Item caitrang2011(boolean rating) {
        Item item = createItemSetKichHoat(680, 1);
        item.itemOptions.add(new Item.ItemOption(76, 1));
        item.itemOptions.add(new Item.ItemOption(77, 28));
        item.itemOptions.add(new Item.ItemOption(103, 25));
        item.itemOptions.add(new Item.ItemOption(147, 24));
        if (Util.isTrue(995, 1000) && rating) {
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));
        }
        return item;
    }

    public Item caitrangChristmas(boolean rating) {
        Item item = createItemSetKichHoat(Util.nextInt(386, 394), 1);
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 51)));
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 51)));
        item.itemOptions.add(new Item.ItemOption(147, Util.nextInt(15, 20)));
        item.itemOptions.add(new Item.ItemOption(95, Util.nextInt(15, 51)));
        item.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 30)));
        item.itemOptions.add(new Item.ItemOption(106, 0));
        if (Util.isTrue(995, 1000) && rating) {
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));
        }
        return item;
    }

    public Item phuKien2011(boolean rating) {
        Item item = createItemSetKichHoat(954, 1);
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(5) + 5));
        item.itemOptions.add(new Item.ItemOption(147, new Random().nextInt(5) + 5));
        if (Util.isTrue(1, 100)) {
            item.itemOptions.get(Util.nextInt(item.itemOptions.size() - 1)).param = 10;
        }
        item.itemOptions.add(new Item.ItemOption(30, 1));
        if (Util.isTrue(995, 1000) && rating) {
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));
        }
        return item;
    }

    public Item phuKienChristmas(boolean rating) {
        Item item = createItemSetKichHoat(745, 1);
        item.itemOptions.add(new Item.ItemOption(77, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(103, new Random().nextInt(25) + 5));
        item.itemOptions.add(new Item.ItemOption(147, new Random().nextInt(25) + 5));
        if (Util.isTrue(1, 100)) {
            item.itemOptions.get(Util.nextInt(item.itemOptions.size() - 1)).param = 10;
        }
        item.itemOptions.add(new Item.ItemOption(30, 1));
        if (Util.isTrue(995, 1000) && rating) {
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));
        }
        return item;
    }

    public Item vanBay2011(boolean rating) {
        Item item = createItemSetKichHoat(795, 1);
        item.itemOptions.add(new Item.ItemOption(89, 1));
        item.itemOptions.add(new Item.ItemOption(30, 1));
        if (Util.isTrue(950, 1000) && rating) {
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));
        }
        return item;
    }

    public Item daBaoVe() {
        Item item = createItemSetKichHoat(987, 1);
        item.itemOptions.add(new Item.ItemOption(30, 1));
        return item;
    }

    public Item randomRac() {
        short[] racs = { 20, 19, 18, 17 };
        Item item = createItemSetKichHoat(racs[Util.nextInt(racs.length - 1)], 1);
        if (optionRac(item.template.id) != 0) {
            item.itemOptions.add(new Item.ItemOption(optionRac(item.template.id), 1));
        }
        return item;
    }

    public Item randomRac2() {
        short[] racs = { 585, 704, 2048, 379, 384, 385, 381, 828, 829, 830, 831, 832, 833, 834, 835, 836, 837, 838, 839,
                840, 841, 842, 934, 935 };
        int idItem = racs[Util.nextInt(racs.length - 1)];
        if (Util.isTrue(1, 100)) {
            idItem = 956;
        }
        Item item = createItemSetKichHoat(idItem, 1);
        if (optionRac(item.template.id) != 0) {
            item.itemOptions.add(new Item.ItemOption(optionRac(item.template.id), 1));
        }
        return item;
    }

    public Item vanBayChrimas(boolean rating) {
        Item item = createItemSetKichHoat(746, 1);
        item.itemOptions.add(new Item.ItemOption(89, 1));
        item.itemOptions.add(new Item.ItemOption(30, 1));
        if (Util.isTrue(950, 1000) && rating) {
            item.itemOptions.add(new Item.ItemOption(93, new Random().nextInt(3) + 1));
        }
        return item;
    }

    public byte optionRac(short itemId) {
        switch (itemId) {
            case 220:
                return 71;
            case 221:
                return 70;
            case 222:
                return 69;
            case 224:
                return 67;
            case 223:
                return 68;
            default:
                return 0;
        }
    }

    public Item vatphamsk(boolean hsd) {
        int[] itemId = { 2025, 2026, 2036, 2037, 2038, 2039, 2040, 2019, 2020, 2021, 2022, 2023, 2024, 954, 955, 952,
                953, 924, 860, 742 };
        byte[] option = { 77, 80, 81, 103, 50, 94, 5 };
        byte[] option_v2 = { 14, 16, 17, 19, 27, 28, 47, 87 };
        byte optionid = 0;
        byte optionid_v2 = 0;
        byte param = 0;
        Item lt = ItemService.gI().createNewItem((short) itemId[Util.nextInt(itemId.length)]);
        lt.itemOptions.clear();
        optionid = option[Util.nextInt(0, 6)];
        param = (byte) Util.nextInt(5, 15);
        lt.itemOptions.add(new Item.ItemOption(optionid, param));
        if (Util.isTrue(1, 100)) {
            optionid_v2 = option_v2[Util.nextInt(option_v2.length)];
            lt.itemOptions.add(new Item.ItemOption(optionid_v2, param));
        }
        if (Util.isTrue(999, 1000) && hsd) {
            lt.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 7)));
        }
        lt.itemOptions.add(new Item.ItemOption(30, 0));
        return lt;
    }

    public List<Item.ItemOption> getListOptionItemShop(short id) {
        List<Item.ItemOption> list = new ArrayList<>();
        Manager.SHOPS.forEach(shop -> shop.tabShops.forEach(tabShop -> tabShop.itemShops.forEach(itemShop -> {
            if (itemShop.temp.id == id && list.isEmpty()) {
                list.addAll(itemShop.options);
            }
        })));
        return list;
    }

    public int randTempItemDoSao(int gender) {
        int[][] ao = { { 3, 34, 136, 137, 138, 139 }, { 4, 42, 152, 153, 154, 155 }, { 5, 50, 168, 169, 170, 171 } };
        int[][] quan = { { 9, 36, 140, 141, 142, 143 }, { 10, 44, 156, 157, 158, 159 },
                { 11, 52, 172, 173, 174, 175 } };
        int[][] gang = { { 37, 38, 144, 145, 146, 147 }, { 25, 45, 160, 161, 162, 163 },
                { 26, 54, 176, 177, 178, 179 } };
        int[][] giay = { { 39, 40, 148, 149, 150, 151 }, { 31, 48, 164, 165, 166, 167 },
                { 32, 56, 180, 181, 182, 183 } };
        int[][] rada = { { 58, 59, 184, 185, 186, 187 }, { 58, 59, 184, 185, 186, 187 },
                { 58, 59, 184, 185, 186, 187 } };
        int[][][] item = { ao, gang, quan, giay, rada };

        Random random = new Random();

        int type;
        if (Util.isTrue(10, 100)) {
            type = 4;
        } else if (Util.isTrue(23, 100)) {
            type = 3;
        } else if (Util.isTrue(23, 100)) {
            type = 1;
        } else if (Util.isTrue(23, 100)) {
            type = 0;
        } else {
            type = 2;
        }

        int index = random.nextInt(6);

        return item[type][gender][index];
    }

    public int randTempItemKichHoat(int gender) {
        int[][][] items = { { { 0, 33 }, { 1, 41 }, { 2, 49 } }, { { 6, 35 }, { 7, 43 }, { 8, 51 } },
                { { 27, 30 }, { 28, 47 }, { 29, 55 } }, { { 21, 24 }, { 22, 46 }, { 23, 53 } },
                { { 12, 57 }, { 12, 57 }, { 12, 57 } } };

        int type;
        if (Util.isTrue(10, 100)) {
            type = 4;
        } else if (Util.isTrue(23, 100)) {
            type = 3;
        } else if (Util.isTrue(23, 100)) {
            type = 1;
        } else if (Util.isTrue(23, 100)) {
            type = 0;
        } else {
            type = 2;
        }

        return items[type][gender][Util.nextInt(2)];
    }

    public int randDoSao(int gender) {
        int[][][] items = {
                { { 0, 33 }, { 1, 41 }, { 2, 49 } },
                { { 6, 35 }, { 7, 43 }, { 8, 51 } },
                { { 27, 30 }, { 28, 47 }, { 29, 55 } },
                { { 21, 24 }, { 22, 46 }, { 23, 53 } },
                { { 12, 57 }, { 12, 57 }, { 12, 57 } }
        };
        int rand = Util.nextInt(100);

        int type;
        if (rand < 10) {
            type = 4;
        } else if (rand < 32) {
            type = 0;
        } else if (rand < 55) {
            type = 1;
        } else if (rand < 77) {
            type = 2;
        } else {
            type = 3;
        }
        return items[type][gender][Util.nextInt(2)];
    }

    public int[] randOptionItemKichHoat(int gender) {
        int op1;
        int op2;
        switch (gender) {
            case 0 -> {
                if (Util.isTrue(100, 100)) {
                    op1 = 128;
                    op2 = 140;
                } else if (Util.isTrue(100, 100)) {
                    op1 = 127;
                    op2 = 139;
                } else {
                    op1 = 129;
                    op2 = 141;
                }
            }
            case 1 -> {
                if (Util.isTrue(100, 100)) {
                    op1 = 130;
                    op2 = 142;
                } else if (Util.isTrue(100, 100)) {
                    op1 = 131;
                    op2 = 143;
                } else {
                    op1 = 132;
                    op2 = 144;
                }
            }
            default -> {
                if (Util.isTrue(100, 100)) {
                    op1 = 134;
                    op2 = 137;
                } else if (Util.isTrue(100, 100)) {
                    op1 = 135;
                    op2 = 138;
                } else {
                    op1 = 133;
                    op2 = 136;
                }
            }
        }
        return new int[] { op1, op2 };
    }

    public ItemMap randDoTL(Zone zone, int quantity, int x, int y, long id) {
        short idTempTL;
        short[] ao = { 555, 557, 559 };
        short[] quan = { 556, 558, 560 };
        short[] gang = { 562, 564, 566 };
        short[] giay = { 563, 565, 567 };
        short[] nhan = { 561 };
        short[] options = { 86, 87 };
        if (Util.isTrue(10, 100)) {
            idTempTL = nhan[0];
        } else if (Util.isTrue(25, 100)) {
            idTempTL = gang[Util.nextInt(3)];
        } else if (Util.isTrue(45, 100)) {
            idTempTL = quan[Util.nextInt(3)];
        } else if (Util.isTrue(75, 100)) {
            idTempTL = ao[Util.nextInt(3)];
        } else {
            idTempTL = giay[Util.nextInt(3)];
        }

        int tiLe = Util.nextInt(100, 115);
        List<ItemOption> itemoptions = new ArrayList<>();

        switch (idTempTL) {
            case 555:
                itemoptions.add(new ItemOption(47, 800 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 557:
                itemoptions.add(new ItemOption(47, 850 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 559:
                itemoptions.add(new ItemOption(47, 900 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 556:
                int chiso = 52000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 558:
                chiso = 50000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 560:
                chiso = 48000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 562:
                itemoptions.add(new ItemOption(0, 4400 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 564:
                itemoptions.add(new ItemOption(0, 4300 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 566:
                itemoptions.add(new ItemOption(0, 4500 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 563:
                chiso = 48000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 565:
                chiso = 50000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 567:
                chiso = 46000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso * 150 / 1000));
                if (tiLe > 100) {

                }
                break;
            case 561:
                itemoptions.add(new ItemOption(14, 14 * tiLe / 100));
                break;
            default:
                break;
        }

        if (Util.isTrue(30, 100)) {
            if (Util.isTrue(70, 100)) {
                itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
            }
        }

        itemoptions.add(new ItemOption(21, Util.nextInt(15, 17)));

        ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
        it.options.clear();
        it.options.addAll(itemoptions);
        return it;
    }

    public ItemMap randDoTLCOler(Zone zone, int quantity, int x, int y, long id) {
        short idTempTL;
        short[] ao = { 555, 557, 559 };
        short[] quan = { 556, 558, 560 };
        short[] gang = { 562, 564, 566 };
        short[] giay = { 563, 565, 567 };
        short[] nhan = { 561 };
        short[] options = { 86, 87 };
        if (Util.isTrue(10, 100)) {
            idTempTL = nhan[0];
        } else if (Util.isTrue(25, 100)) {
            idTempTL = gang[Util.nextInt(3)];
        } else if (Util.isTrue(45, 100)) {
            idTempTL = quan[Util.nextInt(3)];
        } else if (Util.isTrue(75, 100)) {
            idTempTL = ao[Util.nextInt(3)];
        } else {
            idTempTL = giay[Util.nextInt(3)];
        }

        int tiLe = Util.nextInt(100, 115);
        List<ItemOption> itemoptions = new ArrayList<>();

        switch (idTempTL) {
            case 555:
                itemoptions.add(new ItemOption(47, 800 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 557:
                itemoptions.add(new ItemOption(47, 850 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 559:
                itemoptions.add(new ItemOption(47, 900 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 556:
                int chiso = 52000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 558:
                chiso = 50000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 560:
                chiso = 48000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 562:
                itemoptions.add(new ItemOption(0, 4400 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 564:
                itemoptions.add(new ItemOption(0, 4300 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 566:
                itemoptions.add(new ItemOption(0, 4500 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 563:
                chiso = 48000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 565:
                chiso = 50000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 567:
                chiso = 46000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso * 150 / 1000));
                if (tiLe > 100) {

                }
                break;
            case 561:
                itemoptions.add(new ItemOption(14, 14 * tiLe / 100));
                break;
            default:
                break;
        }

        if (Util.isTrue(30, 100)) {
            if (Util.isTrue(70, 100)) {
                itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
            }
        }

        itemoptions.add(new ItemOption(21, Util.nextInt(15, 17)));
        itemoptions.add(new ItemOption(218, 1));
        itemoptions.add(new ItemOption(206, Util.nextInt(1, 3)));

        ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
        it.options.clear();
        it.options.addAll(itemoptions);
        return it;
    }

    public ItemMap randDoTLBoss(Zone zone, int quantity, int x, int y, long id) {
        short idTempTL;
        short[] ao = { 555, 557, 559 };
        short[] quan = { 556, 558, 560 };
        short[] gang = { 562, 564, 566 };
        short[] giay = { 563, 565, 567 };
        short[] nhan = { 561 };
        short[] options = { 86, 87 };
        if (Util.isTrue(10, 100)) {
            idTempTL = nhan[0];
        } else if (Util.isTrue(25, 100)) {
            idTempTL = gang[Util.nextInt(3)];
        } else if (Util.isTrue(45, 100)) {
            idTempTL = quan[Util.nextInt(3)];
        } else if (Util.isTrue(75, 100)) {
            idTempTL = ao[Util.nextInt(3)];
        } else {
            idTempTL = giay[Util.nextInt(3)];
        }

        int tiLe = Util.nextInt(100, 115);
        List<ItemOption> itemoptions = new ArrayList<>();

        switch (idTempTL) {
            case 555:
                itemoptions.add(new ItemOption(47, 800 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 557:
                itemoptions.add(new ItemOption(47, 850 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 559:
                itemoptions.add(new ItemOption(47, 900 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 556:
                int chiso = 52000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 558:
                chiso = 50000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 560:
                chiso = 48000 * tiLe / 100;
                itemoptions.add(new ItemOption(22, chiso / 1000));
                itemoptions.add(new ItemOption(27, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 562:
                itemoptions.add(new ItemOption(0, 4400 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 564:
                itemoptions.add(new ItemOption(0, 4300 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 566:
                itemoptions.add(new ItemOption(0, 4500 * tiLe / 100));
                if (tiLe > 100) {

                }
                break;
            case 563:
                chiso = 48000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 565:
                chiso = 50000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso / 20));
                if (tiLe > 100) {

                }
                break;
            case 567:
                chiso = 46000 * tiLe / 100;
                itemoptions.add(new ItemOption(23, chiso / 1000));
                itemoptions.add(new ItemOption(28, chiso * 150 / 1000));
                if (tiLe > 100) {

                }
                break;
            case 561:
                itemoptions.add(new ItemOption(14, 14 * tiLe / 100));
                break;
            default:
                break;
        }

        if (Util.isTrue(30, 100)) {
            itemoptions.add(new ItemOption(options[Util.nextInt(options.length)], 0));
        }

        itemoptions.add(new ItemOption(21, Util.nextInt(15, 17)));
        itemoptions.add(new ItemOption(218, 1));
        itemoptions.add(new ItemOption(207, Util.nextInt(4, 6)));

        ItemMap it = new ItemMap(zone, idTempTL, quantity, x, y, id);
        it.options.clear();
        it.options.addAll(itemoptions);
        return it;
    }

    public void OpenSKHTuChon(Player player, int itemUseId, int select) throws Exception {
        if (select < 0 || select > 4) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống trong hành trang!");
            return;
        }
        Item itemUse = InventoryService.gI().findItem(player.inventory.itemsBag, itemUseId);
        if (itemUse == null) {
            return;
        }
        int gender = player.gender;
        if (gender > 2) {
            gender = 2;
        }
        int[][][] itemIds = {
                {
                        { 0, 3, 33, 34, 136, 137, 138, 139, 230, 231, 232, 233 },
                        { 6, 9, 35, 36, 140, 141, 142, 143, 242, 243, 244, 245 },
                        { 21, 24, 37, 38, 144, 145, 146, 147, 254, 256, 257 },
                        { 27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269 },
                        { 12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281 }
                },
                {
                        { 1, 4, 41, 42, 152, 153, 154, 155, 235, 236, 237 },
                        { 7, 10, 43, 44, 156, 157, 158, 159, 246, 247, 248, 249 },
                        { 22, 25, 45, 46, 160, 161, 162, 163, 259, 260, 261 },
                        { 28, 31, 47, 48, 164, 165, 166, 167, 270, 271, 272, 273 },
                        { 12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281 }
                },
                {
                        { 2, 5, 49, 50, 168, 169, 170, 171, 238, 239, 240, 241 },
                        { 8, 11, 51, 52, 172, 173, 174, 174, 250, 251, 252, 253 },
                        { 23, 26, 53, 54, 176, 177, 178, 179, 262, 263, 264, 265 },
                        { 29, 32, 55, 56, 180, 181, 182, 183, 274, 275, 276, 277 },
                        { 12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281 }
                }
        };

        int[][] optionIds = {
                { 128, 129, 127, 233, 245 },
                { 130, 131, 132, 233, 237 },
                { 133, 135, 134, 233, 241 }
        };

        int[] selectedItemIds = itemIds[gender][select];
        int itemId = selectedItemIds[Util.nextInt(selectedItemIds.length)];
        int[] possibleOptions = optionIds[gender];
        int optionRandom = possibleOptions[Util.nextInt(possibleOptions.length)];

        Item item = createItemSKH(itemId, optionRandom);
        if (item != null) {
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().sendItemBags(player);
        }
    }

    public void OpenSKH(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống trong hành trang!");
            return;
        }

        Item itemUse = InventoryService.gI().findItem(player.inventory.itemsBag, 1559);
        if (itemUse == null) {
            return;
        }

        int gender = player.gender;
        if (gender > 2) {
            gender = 2;
        }

        int[][][] itemIds = {
                { // Trái đất
                        { 0, 3, 33, 34, 136, 137, 138, 139, 230, 231, 232, 233 }, // Áo
                        { 6, 9, 35, 36, 140, 141, 142, 143, 242, 243, 244, 245 }, // Quần
                        { 21, 24, 37, 38, 144, 145, 146, 147, 254, 256, 257 }, // Găng
                        { 27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269 }, // Giày
                        { 12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281 } // Rada
                },
                { // Namek
                        { 1, 4, 41, 42, 152, 153, 154, 155, 235, 236, 237 }, // Áo
                        { 7, 10, 43, 44, 156, 157, 158, 159, 246, 247, 248, 249 }, // Quần
                        { 22, 25, 45, 46, 160, 161, 162, 163, 259, 260, 261 }, // Găng
                        { 28, 31, 47, 48, 164, 165, 166, 167, 270, 271, 272, 273 }, // Giày
                        { 12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281 } // Rada
                },
                { // Xayda
                        { 2, 5, 49, 50, 168, 169, 170, 171, 238, 239, 240, 241 }, // Áo
                        { 8, 11, 51, 52, 172, 173, 174, 175, 250, 251, 252, 253 }, // Quần
                        { 23, 26, 53, 54, 176, 177, 178, 179, 262, 263, 264, 265 }, // Găng
                        { 29, 32, 55, 56, 180, 181, 182, 183, 274, 275, 276, 277 }, // Giày
                        { 12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281 } // Rada
                }
        };

        int[][] optionIds = {
                { 128, 129, 127, 233, 245 }, // Trái đất
                { 130, 131, 132, 233, 237 }, // Namek
                { 133, 135, 134, 233, 241 } // Xayda
        };

        // Random loại trang bị (0=áo,1=quần,2=găng,3=giày,4=rada)
        int randomType = Util.nextInt(5);
        int[] selectedItemIds = itemIds[gender][randomType];
        int itemId = selectedItemIds[Util.nextInt(selectedItemIds.length)];

        int[] possibleOptions = optionIds[gender];
        int optionRandom = possibleOptions[Util.nextInt(possibleOptions.length)];

        Item item = createItemSKH(itemId, optionRandom);
        if (item != null) {
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            InventoryService.gI().subQuantityItemsBag(player, itemUse, 1);
            InventoryService.gI().sendItemBags(player);
        }
    }

    public Item createItemSKH(int itemId, int skhId) {
        Item item = createItemSetKichHoat(itemId, 1);
        if (item != null) {
            item.itemOptions.addAll(ItemService.gI().getListOptionItemShop((short) itemId));
            item.itemOptions.add(new Item.ItemOption(skhId, 0));
            for (int subId : getOptionIdsBySKH(skhId)) {
                item.itemOptions.add(new Item.ItemOption(subId, 0));
            }
            item.itemOptions.add(new Item.ItemOption(30, 1));
        }
        return item;
    }

    public int[] getOptionIdsBySKH(int skhId) {
        return switch (skhId) {
            case 127 ->
                new int[] { 139 };
            case 128 ->
                new int[] { 140 };
            case 129 ->
                new int[] { 141 };
            case 130 ->
                new int[] { 142 };
            case 131 ->
                new int[] { 143 };
            case 132 ->
                new int[] { 144 };
            case 133 ->
                new int[] { 136 };
            case 134 ->
                new int[] { 137 };
            case 135 ->
                new int[] { 138 };
            case 233 ->
                new int[] { 234 };
            case 237 ->
                new int[] { 238, 239, 240 };
            case 241 ->
                new int[] { 242, 243, 244 };
            case 245 ->
                new int[] { 246, 247, 248 };
            case 254 ->
                new int[] { 255, 256, 257 };
            default ->
                new int[] {};
        };
    }

    public Item doThienSu(int itemId, int gender) {
        Item item = createItemSetKichHoat(itemId, 1);
        List<Integer> ao = Arrays.asList(1048, 1049, 1050); // Áo -> Giáp
        List<Integer> quan = Arrays.asList(1051, 1052, 1053); // Quần -> HP
        List<Integer> gang = Arrays.asList(1054, 1055, 1056); // Găng -> Sức đánh
        List<Integer> giay = Arrays.asList(1057, 1058, 1059); // Giày -> KI
        List<Integer> nhan = Arrays.asList(1060, 1061, 1062); // Nhẫn -> Chí mạng

        int basePercent;
        int roll = Util.nextInt(0, 100);

        if (roll < 25) {
            basePercent = Util.nextInt(20, 23);
        } else if (roll < 70) {
            basePercent = Util.nextInt(24, 26);
        } else if (roll < 80) {
            basePercent = Util.nextInt(27, 29);
        } else if (roll < 95) {
            basePercent = Util.nextInt(30, 32);
        } else {
            basePercent = Util.nextInt(33, 35);
        }
        int totalPercent = basePercent;
        int baseAo = 0, baseQuan = 0, baseGang = 0, baseGiay = 0;
        double baseNhan = 0;
        switch (gender) {
            case 0 -> { // Trái đất
                baseAo = 1600;
                baseQuan = 104;
                baseGang = 8_800;
                baseGiay = 96;
                baseNhan = 16;
            }
            case 1 -> { // Namek
                baseAo = 1700;
                baseQuan = 100;
                baseGang = 8_600;
                baseGiay = 100;
                baseNhan = 16;
            }
            case 2 -> { // Xayda
                baseAo = 1800;
                baseQuan = 96;
                baseGang = 9_000;
                baseGiay = 92;
                baseNhan = 16;
            }
        }

        //---từng loại---
        if (ao.contains(itemId)) {
            int value = (int) Math.round(baseAo * (1 + totalPercent / 100.0));
            item.itemOptions.add(new ItemOption(47, value)); // Giáp +#
        } else if (quan.contains(itemId)) {
            int value = (int) Math.round(baseQuan * (1 + totalPercent / 100.0));
            item.itemOptions.add(new ItemOption(22, value)); // HP +#k
        } else if (gang.contains(itemId)) {
            int value = (int) Math.round(baseGang * (1 + totalPercent / 100.0));
            item.itemOptions.add(new ItemOption(0, value)); // Sức đánh +#
        } else if (giay.contains(itemId)) {
            int value = (int) Math.round(baseGiay * (1 + totalPercent / 100.0));
            item.itemOptions.add(new ItemOption(23, value)); // KI +#k
        } else if (nhan.contains(itemId)) {
            double value = baseNhan * (1 + totalPercent / 100.0);
            item.itemOptions.add(new ItemOption(14, (int) Math.round(value))); // Chí mạng +#%
        }
        item.itemOptions.add(new ItemOption(21, 120)); // Sức mạnh yêu cầu # tỉ
        item.itemOptions.add(new ItemOption(30, 1)); // Không giao dịch
        return item;
    }

    public void Gwen_AddOption(ItemMap item, int skhId) {
        Gwen_Option_All(item.options, skhId);
    }

    public void Gwen_Option(Item item, int skhId) {
        Gwen_Option_All(item.itemOptions, skhId);
    }

    private void Gwen_Option_All(List<ItemOption> item, int skhId) {
        item.add(new ItemOption(skhId, 0));
        item.add(new ItemOption(Gwen_ID(skhId), 0));
        item.add(new ItemOption(30, 1));
    }

    public void addSetGohanOptions(Item item, int depositLeft, int luckPercent) {
        if (item.itemOptions == null) {
            item.itemOptions = new ArrayList<>();
        }
        item.itemOptions.add(new Item.ItemOption(233, 1));
        item.itemOptions.add(new Item.ItemOption(234, 1));
        item.itemOptions.add(new Item.ItemOption(235, depositLeft));
        item.itemOptions.add(new Item.ItemOption(236, luckPercent));
    }

    public void addSetOptionsByGender(Item item, byte gender) {
        if (item.itemOptions == null) {
            item.itemOptions = new ArrayList<>();
        }
        int[] ops = randOptionItemKichHoatNew(gender);
        for (int opId : ops) {
            item.itemOptions.add(new Item.ItemOption(opId, 0));
        }
    }

    public int Gwen_ID(int skhId) {
        switch (skhId) {
            case 127:
                return 139;
            case 128:
                return 140;
            case 129:
                return 141;
            case 130:
                return 142;
            case 131:
                return 143;
            case 132:
                return 144;
            case 133:
                return 136;
            case 134:
                return 137;
            case 135:
                return 138;
        }
        return 0;
    }

    public int[] randOptionItemKichHoatNew(byte gender) {
        int op1, op2, op3, op4;
        switch (gender) {
            case 0 -> {
                {
                    op1 = 245;
                    op2 = 246;
                    op3 = 247;
                    op4 = 248;
                }
            }
            case 1 -> {
                {
                    op1 = 237;
                    op2 = 238;
                    op3 = 239;
                    op4 = 240;
                }
            }
            default -> {
                {
                    op1 = 241;
                    op2 = 242;
                    op3 = 243;
                    op4 = 244;
                }
            }
        }
        return new int[] { op1, op2, op3, op4 };
    }

    public int[] randOptionItemKichHoatUnified(byte gender) {
        if (utils.Util.isTrue(100, 100)) {
            return randOptionItemKichHoatNew(gender);
        } else {
            return randOptionItemKichHoat(gender);
        }
    }

}
