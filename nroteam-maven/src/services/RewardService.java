package services;

import item.Item;
import item.Item.ItemOption;
import java.util.ArrayList;
import java.util.List;
import map.ItemMap;
import player.Player;
import services.player.InventoryService;
import utils.Util;

public class RewardService {

    private static RewardService I;

    private RewardService() {
    }

    public static RewardService gI() {
        if (RewardService.I == null) {
            RewardService.I = new RewardService();
        }
        return RewardService.I;
    }

    public List<Item> getListItemLuckyRound(Player player, int num, boolean vip) {
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Item it = ItemService.gI().createNewItem((short) 189);
            it.quantity = Util.nextInt(5, 50) * 1000;
            boolean success = Util.isTrue(1, 2);

            if (vip) {
                if (Util.isTrue(1, 30)) {
                    it = ItemService.gI().createNewItem((short) 1208);
                    it.itemOptions.clear();
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 25)));
                    it.itemOptions.add(new Item.ItemOption(154, 0));
                    if (!Util.isTrue(1, 300)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(3, 15)));
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(1, 30)) {
                    it = ItemService.gI().createNewItem((short) 1209);
                    it.itemOptions.clear();
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 25)));
                    it.itemOptions.add(new Item.ItemOption(154, 0));
                    if (!Util.isTrue(1, 300)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(3, 15)));
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(1, 30)) {
                    it = ItemService.gI().createNewItem((short) 1210);
                    it.itemOptions.clear();
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 25)));
                    it.itemOptions.add(new Item.ItemOption(154, 0));
                    if (!Util.isTrue(1, 300)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(3, 15)));
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(1, 25)) {
                    it = ItemService.gI().createNewItem((short) 954);
                    it.itemOptions.clear();
                    it.itemOptions.add(new Item.ItemOption(50, 22));
                    it.itemOptions.add(new Item.ItemOption(77, 15));
                    it.itemOptions.add(new Item.ItemOption(103, 15));
                    if (!Util.isTrue(1, 200)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(3, 15)));
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(1, 25)) {
                    it = ItemService.gI().createNewItem((short) 955);
                    it.itemOptions.clear();
                    it.itemOptions.add(new Item.ItemOption(50, 24));
                    it.itemOptions.add(new Item.ItemOption(77, 17));
                    it.itemOptions.add(new Item.ItemOption(103, 18));
                    if (!Util.isTrue(1, 200)) {
                        it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(3, 15)));
                    }
                    it.quantity = 1;
                } else if (Util.isTrue(1, 100)) {
                    it = ItemService.gI().createNewItem((short) 956);
                    it.itemOptions.clear();
                    it.quantity = 1;
                }
            } else {
                if (Util.isTrue(1, 2)) {
                    int[] itemId = {467, 468, 469, 470, 471, 741, 745, 800, 801, 803, 804, 1000};
                    int itemid = itemId[Util.nextInt(itemId.length)];
                    if (Util.isTrue(20, 100)) {
                        int[] itemId2 = {467, 468, 469, 470, 471, 741, 745, 800, 801, 803, 804, 999, 1000, 1001};
                        itemid = itemId2[Util.nextInt(itemId2.length)];
                    }
                    byte[] option = {77, 80, 81, 103, 50, 94, 5};
                    Item vpdl = ItemService.gI().createNewItem((short) itemid);
                    vpdl.itemOptions.clear();
                    byte optionid = option[Util.nextInt(0, 6)];
                    byte param = (byte) Util.nextInt(5, 10);
                    vpdl.itemOptions.add(new Item.ItemOption(optionid, param));
                    vpdl.itemOptions.add(new Item.ItemOption(30, 0));
                    vpdl.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
                    it = vpdl;
                    it.quantity = 1;
                } else if (Util.isTrue(1, 100)) {
                    it = ItemService.gI().createNewItem((short) Util.nextInt(18, 20));
                    it.quantity = Util.nextInt(1, 5);
                } else if (Util.isTrue(1, 30)) {
                    it = ItemService.gI().createNewItem((short) Util.nextInt(220, 224));
                    it.quantity = Util.nextInt(1, 5);
                } else if (Util.isTrue(1, 100)) {
                    it = ItemService.gI().createNewItem((short) Util.nextInt(828, 842));
                    it.quantity = Util.nextInt(1, 5);
                }
                it = itemRand(it, success);
            }
            list.add(it);
        }
        return list;
    }

    public Item itemRand(Item item, boolean success) {
        if (!success) {
            item = ItemService.gI().createNewItem((short) 189, Util.nextInt(5, 12) * 1000);
        }
        return item;
    }

    public void rewardLancon(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            player.canReward = false;
            player.haveReward = true;
            int[] items = {734, 920, 849, 743, 733};
            Item item = ItemService.gI().createNewItem((short) items[Util.nextInt(items.length)]);
            byte[] option = {77, 80, 81, 103, 50, 94, 5};
            byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
            if (Util.isTrue(5, 100)) {
                item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
            } else {
                item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 10)));
                if (Util.isTrue(1, 10)) {
                    item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 10)));
                }
                item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
            }
            item.itemOptions.add(new Item.ItemOption(89, 0));
            item.itemOptions.add(new Item.ItemOption(30, 0));
            InventoryService.gI().addItemBag(player, item);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn vừa nhận được " + item.template.name);
        } else {
            Service.gI().sendThongBao(player, "Cần 1 ô hành trang trống");
        }
    }

    public void initChiSoItem(Item item) {
        SetClothes(item.template.id, item.template.type, item.itemOptions);
    }

    public static void SetClothes(int tempId, int type, List<ItemOption> list) {
        int[][] option_param = {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}};
        switch (type) {
            case 0:
                option_param[0][0] = 47;
                switch (tempId) {
                    case 0, 1 -> option_param[0][1] = 2;
                    case 33, 41 -> option_param[0][1] = 4;
                    case 3, 4 -> option_param[0][1] = 8;
                    case 34, 42 -> option_param[0][1] = 16;
                    case 136, 152 -> option_param[0][1] = 24;
                    case 137, 153 -> option_param[0][1] = 40;
                    case 138, 154 -> option_param[0][1] = 60;
                    case 139, 155 -> option_param[0][1] = 90;
                    case 230, 234 -> option_param[0][1] = 200;
                    case 231, 235 -> option_param[0][1] = 250;
                    case 232, 236 -> option_param[0][1] = 300;
                    case 233, 237 -> option_param[0][1] = 400;
                    case 2 -> option_param[0][1] = 3;
                    case 49 -> option_param[0][1] = 5;
                    case 5 -> option_param[0][1] = 10;
                    case 50 -> option_param[0][1] = 20;
                    case 168 -> option_param[0][1] = 30;
                    case 169 -> option_param[0][1] = 50;
                    case 170 -> option_param[0][1] = 70;
                    case 171 -> option_param[0][1] = 100;
                    case 238 -> option_param[0][1] = 230;
                    case 239 -> option_param[0][1] = 280;
                    case 240 -> option_param[0][1] = 330;
                    case 241 -> option_param[0][1] = 450;
                    case 555 -> {
                        option_param[2][0] = 21;
                        option_param[0][1] = 700;
                        option_param[2][1] = 15;
                    }
                    case 557 -> {
                        option_param[2][0] = 21;
                        option_param[0][1] = 600;
                        option_param[2][1] = 15;
                    }
                    case 559 -> {
                        option_param[2][0] = 21;
                        option_param[0][1] = 800;
                        option_param[2][1] = 15;
                    }
                    case 650 -> {
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 1100;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 652 -> {
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 1000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 654 -> {
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 1300;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                }
                break;
            case 1:
                option_param[0][0] = 6;
                option_param[1][0] = 27;
                switch (tempId) {
                    case 6, 7, 8 -> option_param[0][1] = 20;
                    case 35 -> {
                        option_param[0][1] = 150;
                        option_param[1][1] = 12;
                    }
                    case 9 -> {
                        option_param[0][1] = 300;
                        option_param[1][1] = 40;
                    }
                    case 36 -> {
                        option_param[0][1] = 600;
                        option_param[1][1] = 120;
                    }
                    case 140 -> {
                        option_param[0][1] = 1400;
                        option_param[1][1] = 280;
                    }
                    case 141 -> {
                        option_param[0][1] = 3000;
                        option_param[1][1] = 600;
                    }
                    case 142 -> {
                        option_param[0][1] = 6000;
                        option_param[1][1] = 1200;
                    }
                    case 143 -> {
                        option_param[0][1] = 10000;
                        option_param[1][1] = 2000;
                    }
                    case 242 -> {
                        option_param[0][1] = 14000;
                        option_param[1][1] = 2500;
                    }
                    case 243 -> {
                        option_param[0][1] = 18000;
                        option_param[1][1] = 3000;
                    }
                    case 244 -> {
                        option_param[0][1] = 22000;
                        option_param[1][1] = 3500;
                    }
                    case 245 -> {
                        option_param[0][1] = 26000;
                        option_param[1][1] = 4000;
                    }
                    case 43 -> {
                        option_param[0][1] = 25;
                        option_param[1][1] = 10;
                    }
                    case 10 -> {
                        option_param[0][1] = 120;
                        option_param[1][1] = 28;
                    }
                    case 44 -> {
                        option_param[0][1] = 250;
                        option_param[1][1] = 100;
                    }
                    case 156 -> {
                        option_param[0][1] = 600;
                        option_param[1][1] = 240;
                    }
                    case 157 -> {
                        option_param[0][1] = 1200;
                        option_param[1][1] = 480;
                    }
                    case 158 -> {
                        option_param[0][1] = 2400;
                        option_param[1][1] = 960;
                    }
                    case 159 -> {
                        option_param[0][1] = 4800;
                        option_param[1][1] = 1800;
                    }
                    case 246 -> {
                        option_param[0][1] = 13000;
                        option_param[1][1] = 2200;
                    }
                    case 247 -> {
                        option_param[0][1] = 17000;
                        option_param[1][1] = 2700;
                    }
                    case 248 -> {
                        option_param[0][1] = 21000;
                        option_param[1][1] = 3200;
                    }
                    case 249 -> {
                        option_param[0][1] = 25000;
                        option_param[1][1] = 3700;
                    }
                    case 51 -> {
                        option_param[0][1] = 20;
                        option_param[1][1] = 8;
                    }
                    case 11 -> {
                        option_param[0][1] = 100;
                        option_param[1][1] = 20;
                    }
                    case 52 -> {
                        option_param[0][1] = 200;
                        option_param[1][1] = 80;
                    }
                    case 172 -> {
                        option_param[0][1] = 500;
                        option_param[1][1] = 200;
                    }
                    case 173 -> {
                        option_param[0][1] = 1000;
                        option_param[1][1] = 400;
                    }
                    case 174 -> {
                        option_param[0][1] = 2000;
                        option_param[1][1] = 800;
                    }
                    case 175 -> {
                        option_param[0][1] = 4000;
                        option_param[1][1] = 1600;
                    }
                    case 250 -> {
                        option_param[0][1] = 12000;
                        option_param[1][1] = 2100;
                    }
                    case 251 -> {
                        option_param[0][1] = 16000;
                        option_param[1][1] = 2600;
                    }
                    case 252 -> {
                        option_param[0][1] = 20000;
                        option_param[1][1] = 3100;
                    }
                    case 253 -> {
                        option_param[0][1] = 24000;
                        option_param[1][1] = 3600;
                    }
                    case 556 -> {
                        option_param[0][0] = 22;
                        option_param[2][0] = 21;
                        option_param[0][1] = 65;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                    }
                    case 558 -> {
                        option_param[0][0] = 22;
                        option_param[2][0] = 21;
                        option_param[0][1] = 55;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                    }
                    case 560 -> {
                        option_param[0][0] = 22;
                        option_param[2][0] = 21;
                        option_param[0][1] = 60;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 15;
                    }
                    case 651 -> {
                        option_param[0][0] = 22;
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 100;
                        option_param[1][1] = 25000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 653 -> {
                        option_param[0][0] = 22;
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 90;
                        option_param[1][1] = 22000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 655 -> {
                        option_param[0][0] = 22;
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 80;
                        option_param[1][1] = 24000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                }
                break;
            case 2:
                option_param[0][0] = 0;
                switch (tempId) {
                    case 21 -> option_param[0][1] = 4;
                    case 24 -> option_param[0][1] = 7;
                    case 37 -> option_param[0][1] = 14;
                    case 38 -> option_param[0][1] = 28;
                    case 144 -> option_param[0][1] = 55;
                    case 145 -> option_param[0][1] = 110;
                    case 146 -> option_param[0][1] = 220;
                    case 147 -> option_param[0][1] = 530;
                    case 254 -> option_param[0][1] = 680;
                    case 255 -> option_param[0][1] = 1000;
                    case 256 -> option_param[0][1] = 1500;
                    case 257 -> option_param[0][1] = 2200;
                    case 22 -> option_param[0][1] = 3;
                    case 46 -> option_param[0][1] = 6;
                    case 25 -> option_param[0][1] = 12;
                    case 45 -> option_param[0][1] = 24;
                    case 160 -> option_param[0][1] = 50;
                    case 161 -> option_param[0][1] = 100;
                    case 162 -> option_param[0][1] = 200;
                    case 163 -> option_param[0][1] = 500;
                    case 258 -> option_param[0][1] = 630;
                    case 259 -> option_param[0][1] = 950;
                    case 260 -> option_param[0][1] = 1450;
                    case 261 -> option_param[0][1] = 2150;
                    case 23 -> option_param[0][1] = 5;
                    case 53 -> option_param[0][1] = 8;
                    case 26 -> option_param[0][1] = 16;
                    case 54 -> option_param[0][1] = 32;
                    case 176 -> option_param[0][1] = 60;
                    case 177 -> option_param[0][1] = 120;
                    case 178 -> option_param[0][1] = 240;
                    case 179 -> option_param[0][1] = 560;
                    case 262 -> option_param[0][1] = 700;
                    case 263 -> option_param[0][1] = 1050;
                    case 264 -> option_param[0][1] = 1550;
                    case 265 -> option_param[0][1] = 2250;
                    case 562 -> {
                        option_param[2][0] = 21;
                        option_param[0][1] = 3200;
                        option_param[2][1] = 17;
                    }
                    case 564 -> {
                        option_param[2][0] = 21;
                        option_param[0][1] = 3000;
                        option_param[2][1] = 17;
                    }
                    case 566 -> {
                        option_param[2][0] = 21;
                        option_param[0][1] = 3500;
                        option_param[2][1] = 17;
                    }
                    case 657 -> {
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 7000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 659 -> {
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 6500;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 661 -> {
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 7500;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                }
                break;
            case 3:
                option_param[0][0] = 7;
                option_param[1][0] = 28;
                switch (tempId) {
                    case 27, 29 -> option_param[0][1] = 10;
                    case 30 -> {
                        option_param[0][1] = 25;
                        option_param[1][1] = 5;
                    }
                    case 39 -> {
                        option_param[0][1] = 120;
                        option_param[1][1] = 24;
                    }
                    case 40 -> {
                        option_param[0][1] = 250;
                        option_param[1][1] = 50;
                    }
                    case 148 -> {
                        option_param[0][1] = 500;
                        option_param[1][1] = 100;
                    }
                    case 149 -> {
                        option_param[0][1] = 1200;
                        option_param[1][1] = 240;
                    }
                    case 150 -> {
                        option_param[0][1] = 2400;
                        option_param[1][1] = 480;
                    }
                    case 151 -> {
                        option_param[0][1] = 5000;
                        option_param[1][1] = 1000;
                    }
                    case 266 -> {
                        option_param[0][1] = 9000;
                        option_param[1][1] = 1500;
                    }
                    case 267 -> {
                        option_param[0][1] = 14000;
                        option_param[1][1] = 2000;
                    }
                    case 268 -> {
                        option_param[0][1] = 19000;
                        option_param[1][1] = 2500;
                    }
                    case 269 -> {
                        option_param[0][1] = 24000;
                        option_param[1][1] = 3000;
                    }
                    case 28 -> option_param[0][1] = 15;
                    case 47 -> {
                        option_param[0][1] = 30;
                        option_param[1][1] = 6;
                    }
                    case 31 -> {
                        option_param[0][1] = 150;
                        option_param[1][1] = 30;
                    }
                    case 48 -> {
                        option_param[0][1] = 300;
                        option_param[1][1] = 60;
                    }
                    case 164 -> {
                        option_param[0][1] = 600;
                        option_param[1][1] = 120;
                    }
                    case 165 -> {
                        option_param[0][1] = 1500;
                        option_param[1][1] = 300;
                    }
                    case 166 -> {
                        option_param[0][1] = 3000;
                        option_param[1][1] = 600;
                    }
                    case 167 -> {
                        option_param[0][1] = 6000;
                        option_param[1][1] = 1200;
                    }
                    case 270 -> {
                        option_param[0][1] = 10000;
                        option_param[1][1] = 1700;
                    }
                    case 271 -> {
                        option_param[0][1] = 15000;
                        option_param[1][1] = 2200;
                    }
                    case 272 -> {
                        option_param[0][1] = 20000;
                        option_param[1][1] = 2700;
                    }
                    case 273 -> {
                        option_param[0][1] = 25000;
                        option_param[1][1] = 3200;
                    }
                    case 55 -> {
                        option_param[0][1] = 20;
                        option_param[1][1] = 4;
                    }
                    case 32 -> {
                        option_param[0][1] = 100;
                        option_param[1][1] = 20;
                    }
                    case 56 -> {
                        option_param[0][1] = 200;
                        option_param[1][1] = 40;
                    }
                    case 180 -> {
                        option_param[0][1] = 400;
                        option_param[1][1] = 80;
                    }
                    case 181 -> {
                        option_param[0][1] = 1000;
                        option_param[1][1] = 200;
                    }
                    case 182 -> {
                        option_param[0][1] = 2000;
                        option_param[1][1] = 400;
                    }
                    case 183 -> {
                        option_param[0][1] = 4000;
                        option_param[1][1] = 800;
                    }
                    case 274 -> {
                        option_param[0][1] = 8000;
                        option_param[1][1] = 1300;
                    }
                    case 275 -> {
                        option_param[0][1] = 13000;
                        option_param[1][1] = 1800;
                    }
                    case 276 -> {
                        option_param[0][1] = 18000;
                        option_param[1][1] = 2300;
                    }
                    case 277 -> {
                        option_param[0][1] = 23000;
                        option_param[1][1] = 2800;
                    }
                    case 563 -> {
                        option_param[0][0] = 23;
                        option_param[2][0] = 21;
                        option_param[0][1] = 40;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                    }
                    case 565 -> {
                        option_param[0][0] = 23;
                        option_param[2][0] = 21;
                        option_param[0][1] = 60;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                    }
                    case 567 -> {
                        option_param[0][0] = 23;
                        option_param[2][0] = 21;
                        option_param[0][1] = 50;
                        option_param[1][1] = 10000;
                        option_param[2][1] = 14;
                    }
                    case 658 -> {
                        option_param[0][0] = 23;
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 80;
                        option_param[1][1] = 22000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 660 -> {
                        option_param[0][0] = 23;
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 90;
                        option_param[1][1] = 25000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                    case 662 -> {
                        option_param[0][0] = 23;
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 85;
                        option_param[1][1] = 20000;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                }
                break;
            case 4:
                option_param[0][0] = 14;
                switch (tempId) {
                    case 12 -> option_param[0][1] = 1;
                    case 57 -> option_param[0][1] = 2;
                    case 58 -> option_param[0][1] = 3;
                    case 59 -> option_param[0][1] = 4;
                    case 184 -> option_param[0][1] = 5;
                    case 185 -> option_param[0][1] = 6;
                    case 186 -> option_param[0][1] = 7;
                    case 187 -> option_param[0][1] = 8;
                    case 278 -> option_param[0][1] = 9;
                    case 279 -> option_param[0][1] = 10;
                    case 280 -> option_param[0][1] = 11;
                    case 281 -> option_param[0][1] = 12;
                    case 561 -> {
                        option_param[2][0] = 21;
                        option_param[0][1] = 16;
                        option_param[2][1] = 18;
                    }
                    case 656 -> {
                        option_param[2][0] = 21;
                        option_param[3][0] = 30;
                        option_param[0][1] = 18;
                        option_param[2][1] = 5;
                        option_param[3][1] = 1;
                    }
                }
                break;
        }

        for (int[] param : option_param) {
            if (param[0] != -1 && param[1] != -1) {
                if (param[0] == 21) {
                    list.add(new ItemOption(param[0], param[1]));
                } else {
                    list.add(new ItemOption(param[0], (param[1] + Util.nextInt(-(param[1] * 10 / 100), param[1] * 10 / 100))));
                }
            }
        }
    }

    public void initActivationOption(int gender, int type, List<ItemOption> list) {
        if (type <= 4) {
            int[] ids = services.ItemService.gI().randOptionItemKichHoat(gender);
            for (int id : ids) {
                list.add(new ItemOption(id, 1));
            }
            list.add(new ItemOption(30, 7));
        }
    }

    public Item rewardCapsuleTet(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
            if (Util.isTrue(40, 100)) {
                int[] items = {734, 920, 849, 743, 733};
                Item item = ItemService.gI().createNewItem((short) items[Util.nextInt(items.length)]);
                byte[] option = {77, 80, 81, 103, 50, 94, 5};
                byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
                if (Util.isTrue(5, 100)) {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                    item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
                } else {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 10)));
                    if (Util.isTrue(1, 10)) {
                        item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 10)));
                    }
                    item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
                }
                item.itemOptions.add(new Item.ItemOption(89, 0));
                item.itemOptions.add(new Item.ItemOption(30, 0));
                return item;
            } else if (Util.isTrue(50, 100)) {
                int[] items = {942, 943, 944};
                Item item = ItemService.gI().createNewItem((short) items[Util.nextInt(items.length)]);
                byte[] option = {77, 80, 81, 103, 50, 94, 5};
                byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
                if (Util.isTrue(5, 100)) {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                    item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
                } else {
                    item.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 10)));
                    if (Util.isTrue(1, 10)) {
                        item.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 10)));
                    }
                    item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 30)));
                }
                item.itemOptions.add(new Item.ItemOption(30, 0));
                return item;
            } else {
                Item it = ItemService.gI().createNewItem((short) Util.nextInt(2148, 2152));
                it.quantity = 1;
                if (Util.isTrue(5, 100)) {
                    it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 30)));
                    it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 30)));
                    it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 30)));
                    it.itemOptions.add(new Item.ItemOption(94, Util.nextInt(20, 30)));
                    it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(2, 35)));
                    it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(2, 40)));
                    if (Util.isTrue(5, 30)) {
                        it.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 14)));
                    }
                    it.itemOptions.add(new Item.ItemOption(154, 0));
                } else {
                    it.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 50)));
                    it.itemOptions.add(new Item.ItemOption(103, Util.nextInt(20, 50)));
                    if (Util.isTrue(5, 30)) {
                        it.itemOptions.add(new Item.ItemOption(5, Util.nextInt(1, 35)));
                    }
                    it.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 50)));
                    it.itemOptions.add(new Item.ItemOption(94, Util.nextInt(20, 50)));
                    it.itemOptions.add(new Item.ItemOption(14, Util.nextInt(2, 50)));
                    it.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 15)));
                }
                byte[] option = {80, 81, 103, 50, 94, 5};
                byte[] option_v2 = {14, 16, 17, 19, 27, 28, 47, 87};
                if (Util.isTrue(20, 100)) {
                    it.itemOptions.add(new Item.ItemOption(option[Util.nextInt(option.length)], Util.nextInt(1, 5)));
                }
                if (Util.isTrue(20, 100)) {
                    it.itemOptions.add(new Item.ItemOption(108, Util.nextInt(1, 40)));
                }
                if (Util.isTrue(20, 100)) {
                    it.itemOptions.add(new Item.ItemOption(option_v2[Util.nextInt(option_v2.length)], Util.nextInt(1, 2)));
                }
                return it;
            }
        } else {
            Service.gI().sendThongBao(player, "Cần 1 ô hành trang trống");
            return null;
        }
    }

    public void initStarOption(ItemMap item, RatioStar[] ratioStars) {
        RatioStar ratioStar = ratioStars[Util.nextInt(0, ratioStars.length - 1)];
        if (Util.isTrue(ratioStar.ratio, ratioStar.typeRatio)) {
            item.options.add(new ItemOption(107, ratioStar.numStar));
        }
    }

    public void initStarOption(Item item, RatioStar[] ratioStars) {
        RatioStar ratioStar = ratioStars[Util.nextInt(0, ratioStars.length - 1)];
        if (Util.isTrue(ratioStar.ratio, ratioStar.typeRatio)) {
            item.itemOptions.add(new ItemOption(107, ratioStar.numStar));
        }
    }

    public void initBaseOptionSaoPhaLe(Item item) {
        int optionId = -1;
        int param = 5;
        switch (item.template.id) {
            case 441 -> optionId = 95;
            case 442 -> optionId = 96;
            case 443 -> optionId = 97;
            case 444 -> {
                param = 3;
                optionId = 98;
            }
            case 445 -> {
                param = 3;
                optionId = 99;
            }
            case 446 -> optionId = 100;
            case 447 -> optionId = 101;
        }
        if (optionId != -1) {
            item.itemOptions.add(new ItemOption(optionId, param));
        }
    }

    public static class RatioStar {

        public byte numStar;
        public int ratio;
        public int typeRatio;

        public RatioStar(byte numStar, int ratio, int typeRatio) {
            this.numStar = numStar;
            this.ratio = ratio;
            this.typeRatio = typeRatio;
        }
    }
}