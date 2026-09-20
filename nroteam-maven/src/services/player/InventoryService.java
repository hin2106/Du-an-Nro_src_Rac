package services.player;
import consts.ConstPlayer;
import item.Item;
import item.Item.ItemOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import network.Message;
import npc.MabuEgg;
import player.Inventory;
import player.Pet;
import player.Player;
import services.ItemService;
import services.Service;
import services.dungeon.BlackBallWarService;
import services.dungeon.NgocRongNamecService;
import services.map.ChangeMapService;
import services.map.ItemMapService;

public class InventoryService {

    private static InventoryService I;
    private final Map<Integer, List<Item>> itemsIndex = new HashMap<>();

    public static InventoryService gI() {
        if (InventoryService.I == null) {
            InventoryService.I = new InventoryService();
        }
        return InventoryService.I;
    }

    public Item findItem(List<Item> list, int tempId) {
        try {
            for (Item item : list) {
                if (item.isNotNullItem() && item.template.id == tempId) {
                    return item;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public Item findItemBody(Player player, int tempId) {
        if (player == null || player.inventory == null || player.inventory.itemsBody == null) {
            return null;
        }
        return this.findItem(player.inventory.itemsBody, tempId);
    }

    public Item findItemBag(Player player, int tempId) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return null;
        }
        return this.findItem(player.inventory.itemsBag, tempId);
    }

    public Item findItemBox(Player player, int tempId) {
        if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
            return null;
        }
        return this.findItem(player.inventory.itemsBox, tempId);
    }

    public boolean isExistItem(List<Item> list, int tempId) {
        try {
            return this.findItem(list, tempId) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExistItemBody(Player player, int tempId) {
        if (player == null || player.inventory == null || player.inventory.itemsBody == null) {
            return false;
        }
        return this.isExistItem(player.inventory.itemsBody, tempId);
    }

    public boolean isExistItemBag(Player player, int tempId) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return false;
        }
        return this.isExistItem(player.inventory.itemsBag, tempId);
    }

    public boolean isExistItemBox(Player player, int tempId) {
        if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
            return false;
        }
        return this.isExistItem(player.inventory.itemsBox, tempId);
    }

    public List<Item> copyList(List<Item> items) {
        List<Item> list = new ArrayList<>();
        for (Item item : items) {
            list.add(ItemService.gI().copyItem(item));
        }
        return list;
    }

    public List<Item> copyItemsBody(Player player) {
        if (player == null || player.inventory == null || player.inventory.itemsBody == null) {
            return new ArrayList<>();
        }
        return copyList(player.inventory.itemsBody);
    }

    public List<Item> copyItemsBag(Player player) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return new ArrayList<>();
        }
        return copyList(player.inventory.itemsBag);
    }

    public List<Item> copyItemsBox(Player player) {
        if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
            return new ArrayList<>();
        }
        return copyList(player.inventory.itemsBox);
    }

    public void throwItem(Player player, int where, int index) {
        if (player == null || player.inventory == null) {
            return;
        }
        Item itemThrow = null;
        if (where == 0) {
            if (player.inventory.itemsBody == null || index < 0 || index >= player.inventory.itemsBody.size()) {
                return;
            }
            itemThrow = player.inventory.itemsBody.get(index);
            removeItemBody(player, index);
            sendItemBody(player);
            Service.gI().Send_Caitrang(player);
        } else if (where == 1) {
            if (player.inventory.itemsBag == null || index < 0 || index >= player.inventory.itemsBag.size()) {
                return;
            }
            itemThrow = player.inventory.itemsBag.get(index);
            if (itemThrow == null || itemThrow.template == null) {
                return;
            }
            if (itemThrow.template.id == 570) {
                Service.gI().sendThongBao(player, "Không thể bỏ vật phẩm này.");
                return;
            }
            if (itemThrow.template != null && itemThrow.template.id != 457) {
                removeItemBag(player, index);
                sortItems(player.inventory.itemsBag);
                sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Thưa ngài");
            }
        }
        if (itemThrow != null && itemThrow.template != null) {
            int itemId = itemThrow.template.id;
            if ((itemId == 454 || itemId == 921 || itemId == 1798) && player.pet != null) {
                if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                    player.pet.unFusion();
                }
            }
        }
    }

    public void removeItem(List<Item> items, int index) {
        Item item = ItemService.gI().createItemNull();
        items.set(index, item);
    }

    public void removeItem(List<Item> items, Item item) {
        if (item == null) {
            return;
        }
        Item it = ItemService.gI().createItemNull();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(item)) {
                items.set(i, it);
                item.dispose();
                break;
            }
        }
    }

    public void removeItemBag(Player player, int index) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return;
        }
        this.removeItem(player.inventory.itemsBag, index);
    }

    public void removeItemBag(Player player, Item item) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return;
        }
        this.removeItem(player.inventory.itemsBag, item);
    }

    public void removeItemBody(Player player, int index) {
        if (player == null || player.inventory == null || player.inventory.itemsBody == null) {
            return;
        }
        this.removeItem(player.inventory.itemsBody, index);
    }

    public void removeItemPetBody(Player player, int index) {
        this.removeItemBody(player.pet, index);
    }

    public void removeItemBox(Player player, int index) {
        if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
            return;
        }
        this.removeItem(player.inventory.itemsBox, index);
    }

    public void subQuantityItemsBag(Player player, Item item, int quantity) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return;
        }
        subQuantityItem(player.inventory.itemsBag, item, quantity);
    }

    public void subQuantityItemsBody(Player player, Item item, int quantity) {
        if (player == null || player.inventory == null || player.inventory.itemsBody == null) {
            return;
        }
        subQuantityItem(player.inventory.itemsBody, item, quantity);
    }

    public void subQuantityItemsBox(Player player, Item item, int quantity) {
        if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
            return;
        }
        subQuantityItem(player.inventory.itemsBox, item, quantity);
    }

    public void subQuantityItemsBagByIndex(Player player, int index, int quantity) {
        if (player == null || player.inventory == null || player.inventory.itemsBag == null) {
            return;
        }
        if (index < 0 || index >= player.inventory.itemsBag.size()) {
            return;
        }
        subQuantityItemByIndex(player.inventory.itemsBag, index, quantity);
    }

    public void subQuantityItemsBodyByIndex(Player player, int index, int quantity) {
        if (player == null || player.inventory == null || player.inventory.itemsBody == null) {
            return;
        }
        if (index < 0 || index >= player.inventory.itemsBody.size()) {
            return;
        }
        subQuantityItemByIndex(player.inventory.itemsBody, index, quantity);
    }

    public void subQuantityItemsBoxByIndex(Player player, int index, int quantity) {
        if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
            return;
        }
        if (index < 0 || index >= player.inventory.itemsBox.size()) {
            return;
        }
        subQuantityItemByIndex(player.inventory.itemsBox, index, quantity);
    }

    public void subQuantityItemByIndex(List<Item> items, int index, int quantity) {
        if (items == null || index < 0 || index >= items.size()) {
            return;
        }
        Item item = items.get(index);
        if (item != null && item.isNotNullItem()) {
            item.quantity -= quantity;
            if (item.quantity <= 0) {
                this.removeItem(items, index);
            }
        }
    }

    public void subQuantityItem(List<Item> items, Item item, int quantity) {
        if (item != null) {
            for (Item it : items) {
                if (item.equals(it)) {
                    it.quantity -= quantity;
                    if (it.quantity <= 0) {
                        this.removeItem(items, item);
                    }
                    break;
                }
            }
        }
    }

    public void sortItems(List<Item> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (!list.get(i).isNotNullItem()) {
                int indexSwap = -1;
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(j).isNotNullItem()) {
                        indexSwap = j;
                        break;
                    }
                }
                if (indexSwap != -1) {
                    Item sItem = ItemService.gI().createItemNull();
                    list.set(i, list.get(indexSwap));
                    list.set(indexSwap, sItem);
                } else {
                    break;
                }
            }
        }
    }

    public Item finditemBongHoa(Player player, int soluong) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && (item.template.id == 589) && item.quantity >= soluong) {
                return item;
            }
        }
        return null;
    }

    public void sortItemv2(List<Item> items) {
        int index = 0;
        for (Item item : items) {
            if (item != null && item.quantity > 0) {
                items.set(index, item);
                index++;
            }
        }
        for (int i = index; i < items.size(); i++) {
            items.set(i, null);
        }
    }

    private Item putItemBag(Player player, Item item) {
        for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
            if (!player.inventory.itemsBag.get(i).isNotNullItem()) {
                player.inventory.itemsBag.set(i, item);
                Item sItem = ItemService.gI().createItemNull();
                return sItem;
            }
        }
        return item;
    }

    private Item putItemBox(Player player, Item item) {
        for (int i = 0; i < player.inventory.itemsBox.size(); i++) {
            if (!player.inventory.itemsBox.get(i).isNotNullItem()) {
                player.inventory.itemsBox.set(i, item);
                Item sItem = ItemService.gI().createItemNull();
                return sItem;
            }
        }
        return item;
    }

    private Item putItemBody(Player player, Item item) {
        Item sItem = item;
        if (!item.isNotNullItem() || player == null || player.inventory == null || player.inventory.itemsBody == null) {
            return sItem;
        }
        switch (item.template.type) {
            case 0, 1, 2, 3, 4, 5, 32, 23, 24, 11, 75, 72, 27, 35, 98, 99 -> {
            }
            default -> {
                Service.gI().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Trang bị không phù hợp!");
                return sItem;
            }
        }

        if (item.template.gender < 3 && item.template.gender != player.gender) {
            Service.gI().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Trang bị không phù hợp!");
            return sItem;
        }

        if (player.isPet && item.template.type > 6 && item.template.id != 1930) {
            Service.gI().sendThongBao(((Pet) player).master, "Trang bị không phù hợp với đệ tử!");
            return sItem;
        }

        if (item.template.id == 691 || item.template.id == 692 || item.template.id == 693) {
            List<Item> itemsBody = player.inventory.itemsBody;
            if (itemsBody.get(0).isNotNullItem() && itemsBody.get(5).isNotNullItem()) {
                Service.gI().sendThongBaoOK(player.isPet ? ((Pet) player).master : player,
                        "Vui lòng cởi áo để có thể sử dụng!");
                return sItem;
            }
        }
        long powerRequire = item.template.strRequire;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 21) {
                powerRequire = io.param * 1000000000L;
                break;
            }
        }
        if (player.nPoint.power < powerRequire) {
            Service.gI().sendThongBao(player.isPet ? ((Pet) player).master : player, "Sức mạnh không đủ yêu cầu!");
            return sItem;
        }

        handleOption210(item);
        checkOption231(item);
        handleOption261(item);

        int index = -1;
        switch (item.template.type) {
            case 0, 1, 2, 3, 4, 5 ->
                index = item.template.type;
            case 32 ->
                index = 6;
            case 23, 24 ->
                index = 7;
            case 11 -> {
                index = 8;
            }
            // case 0, 1, 2, 3, 4, 5 ->
            //     index = item.template.type;
            // case 32 -> //giap luyen tap
            //     index = 6;
            // case 27 -> //pet 
            //     index = 7;
            // case 11 -> { //deo lung
            //     if (player.isPet) {
            //         index = 6;
            //     } else {
            //         index = 8;
            //     }
            // }
            // case 23, 24 -> // van bay
            //     index = 9;
            // case 35 -> // sach tuyet ki
            //     index = 11;
            case 72, 75, 98, 99 -> {
                Service.gI().sendThongBaoOK(player.isPet ? ((Pet) player).master : player,
                        "Không thể trang bị vật phẩm này vào người!");
                return sItem;
            }
        }

        if (index < 0 || index >= player.inventory.itemsBody.size()) {
            Service.gI().sendThongBaoOK(player.isPet ? ((Pet) player).master : player,
                    "Không thể trang bị vật phẩm này!");
            return sItem;
        }

        sItem = player.inventory.itemsBody.get(index);
        if (index == 8 && sItem.isNotNullItem()) {
            Service.gI().removeEffPlayer(player, sItem.template.part);
        }

        player.inventory.itemsBody.set(index, item);
        return sItem;
    }

    public void itemBagToBody(Player player, int index) {
        if (index < 0 || player.inventory == null || player.inventory.itemsBag == null
                || index >= player.inventory.itemsBag.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = player.inventory.itemsBag.get(index);
        if (!item.isNotNullItem()) {
            return;
        }
        if (item.template.id == 1930) {
            if (!player.isPet) {
                Service.gI().sendThongBao(player, "Chỉ đệ tử mới có thể trang bị vật phẩm này!");
                return;
            }
        }
        player.inventory.itemsBag.set(index, putItemBody(player, item));
        sendItemBody(player);
        sendItemBags(player);
        Service.gI().point(player);
        Service.gI().Send_Caitrang(player);
    }

    public void itemBodyToBag(Player player, int index) {
        if (player.inventory == null || player.inventory.itemsBody == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (index < 0 || index >= player.inventory.itemsBody.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = player.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            // if (index == 7 && !player.isPet) {
            //     if (player.newPet != null) {
            //         ChangeMapService.gI().exitMap(player.newPet);
            //         player.newPet.dispose();
            //         player.newPet = null;
            //     }
            // }
            player.inventory.itemsBody.set(index, putItemBag(player, item));
            sendItemBody(player);            
            sendItemBags(player);
            Service.gI().point(player);
            Service.gI().Send_Caitrang(player);
        }
    }

    public void itemBagToPetBody(Player player, int index) {
        try {
            if (player.pet == null) {
                Service.gI().sendThongBao(player, "Không có đệ tử");
                return;
            }
            if (player.pet.nPoint.power < 1500000) {
                Service.gI().sendThongBao(player, "Đệ tử phải đạt 1tr5 sức mạnh");
                return;
            }
            if (player.inventory.itemsBag.size() < 6) {
                Service.gI().sendThongBao(player, "Cần ít nhất 6 ô hành trang");
                return;
            }
            Item item = player.inventory.itemsBag.get(index);
            if (!item.isNotNullItem()) {
                return;
            }
            boolean canEquip = false;
            if (item.template.type >= 0 && item.template.type <= 5) {
                canEquip = true;
            } else if (item.template.id == 1930) {
                canEquip = true;
            }

            if (!canEquip) {
                Service.gI().sendThongBao(player, "Vật phẩm này không thể trang bị cho đệ tử!");
                return;
            }

            Item itemSwap = putItemBody(player.pet, item);
            if (itemSwap.equals(item)) {
                Service.gI().sendThongBao(player, "Không thể trang bị!");
                return;
            }
            player.inventory.itemsBag.set(index, itemSwap);
            sendItemBody(player);
            sendItemBags(player);
            Service.gI().point(player);
            Service.gI().showInfoPet(player);
            Service.gI().Send_Caitrang(player.pet);
            Service.gI().Send_Caitrang(player);
            Service.gI().sendFlagBag(player.pet);

        } catch (Exception e) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            e.printStackTrace();
        }
    }

    public void itemPetBodyToBag(Player player, int index) {
        if (player.pet == null || player.pet.inventory == null || player.pet.inventory.itemsBody == null) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (index < 0 || index >= player.pet.inventory.itemsBody.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = player.pet.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            player.pet.inventory.itemsBody.set(index, putItemBag(player, item));
            sendItemBody(player);            
            sendItemBags(player);
            Service.gI().point(player);
            Service.gI().Send_Caitrang(player.pet);
            Service.gI().Send_Caitrang(player);
            Service.gI().showInfoPet(player);
        }
    }

    public void itemBoxToBodyOrBag(Player player, int index) {
        if (index < 0) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = player.inventory.itemsBox.get(index);
        if (item.isNotNullItem()) {
            boolean done = false;
            if (item.template.type >= 0 && item.template.type <= 5 || item.template.type == 32) {
                Item itemBody = player.inventory.itemsBody.get(item.template.type == 32 ? 6 : item.template.type);
                if (!itemBody.isNotNullItem()) {
                    if (item.template.gender == player.gender || item.template.gender == 3) {
                        long powerRequire = item.template.strRequire;
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 21) {
                                powerRequire = io.param * 1000000000L;
                                break;
                            }
                        }
                        if (powerRequire <= player.nPoint.power) {
                            player.inventory.itemsBody.set(item.template.type == 32 ? 6 : item.template.type, item);
                            player.inventory.itemsBox.set(index, itemBody);
                            done = true;

                            sendItemBody(player);
                            sendItemBags(player);
                            Service.gI().point(player);
                            Service.gI().Send_Caitrang(player);
                        }
                    }
                }
            }
            if (!done) {
                if (addItemBag(player, item)) {

                    if (item.quantity == 0) {
                        Item sItem = ItemService.gI().createItemNull();
                        player.inventory.itemsBox.set(index, sItem);
                    }
                    sendItemBags(player);
                }
            }
            sendItemBox(player);
        }
    }

    public void itemBoxToBodyOrBag1(Player player, int index) {
        if (index < 0) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = player.inventory.itemsBox1.get(index);
        if (item.isNotNullItem()) {
            boolean done = false;
            if (item.template.type >= 0 && item.template.type <= 5 || item.template.type == 32) {
                Item itemBody = player.inventory.itemsBody.get(item.template.type == 32 ? 6 : item.template.type);
                if (!itemBody.isNotNullItem()) {
                    if (item.template.gender == player.gender || item.template.gender == 3) {
                        long powerRequire = item.template.strRequire;
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 21) {
                                powerRequire = io.param * 1000000000L;
                                break;
                            }
                        }
                        if (powerRequire <= player.nPoint.power) {
                            player.inventory.itemsBody.set(item.template.type == 32 ? 6 : item.template.type, item);
                            player.inventory.itemsBox1.set(index, itemBody);
                            done = true;

                            sendItemBody(player);
                            sendItemBags(player);
                            Service.gI().point(player);
                            Service.gI().Send_Caitrang(player);
                        }
                    }
                }
            }
            if (!done) {
                if (addItemBag(player, item)) {

                    if (item.quantity == 0) {
                        Item sItem = ItemService.gI().createItemNull();
                        player.inventory.itemsBox1.set(index, sItem);
                    }
                    sendItemBags(player);
                }
            }
            sendItemBox1(player);
        }
    }

    public void itemBagToBox(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBag.size()) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = player.inventory.itemsBag.get(index);
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 457) {
                Service.gI().sendThongBao(player, "Không thể cất vàng vào rương");
                return;
            }
            if (addItemBox(player, item)) {
                if (item.quantity == 0) {
                    Item sItem = ItemService.gI().createItemNull();
                    player.inventory.itemsBag.set(index, sItem);
                }
                sortItems(player.inventory.itemsBag);
                sendItemBags(player);
                sendItemBox(player);
            }
        }
    }

    public void itemBagToBox1(Player player, int index) {
        if (index < 0) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        Item item = player.inventory.itemsBag.get(index);
        if (item != null && item.isNotNullItem()) {
            if (item.template.id == 457) {
                Service.gI().sendThongBao(player, "Không thể cất vàng vào rương");
                return;
            }
            if (addItemBox1(player, item)) {
                if (item.quantity == 0) {
                    Item sItem = ItemService.gI().createItemNull();
                    player.inventory.itemsBag.set(index, sItem);
                }
                sortItems(player.inventory.itemsBag);
                sendItemBags(player);
                sendItemBox1(player);
            }
        }
    }

    public void itemBodyToBox(Player player, int index) {
        if (index < 0 || index >= player.inventory.itemsBody.size()) {
            Item item = player.inventory.itemsBody.get(index);
            if (item.isNotNullItem()) {
                player.inventory.itemsBody.set(index, putItemBox(player, item));
                sortItems(player.inventory.itemsBag);
                sendItemBody(player);
                sendItemBox(player);
                Service.gI().point(player);
                Service.gI().Send_Caitrang(player);
            }
        }
    }

    public void sendItemBags(Player player) {
        sortItems(player.inventory.itemsBag);
        Message msg;
        try {
            msg = new Message(-36);
            msg.writer().writeByte(0);
            msg.writer().writeByte(player.inventory.itemsBag.size());
            for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                Item item = player.inventory.itemsBag.get(i);
    if (!item.isNotNullItem()) {
        msg.writer().writeShort(-1);
        continue;
    }
                msg.writer().writeShort(item.template.id);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeUTF(item.getInfo());
                msg.writer().writeUTF(item.getContent());
                msg.writer().writeByte(item.itemOptions.size()); // options
                for (int j = 0; j < item.itemOptions.size(); j++) {
                    if (item.itemOptions.get(j).optionTemplate.id == 213) {
                        int opId = 213;
                        int param = item.itemOptions.get(j).param;
                        if (param > 1_000_000) {
                            opId = 223;
                            param /= 1_000_000;
                        } else if (param > 1000) {
                            opId = 222;
                            param /= 1000;
                        }
                        msg.writer().writeShort(opId);
                        msg.writer().writeInt(param);
                    } else {
                        int opId = item.itemOptions.get(j).optionTemplate.id;
                        msg.writer().writeShort(opId);
                        int paramToSend;
                        switch (opId) {
                            case 129:
                            case 141:
                            case 127:
                            case 139:
                            case 128:
                            case 140:
                            case 131:
                            case 143:
                            case 132:
                            case 144:
                            case 130:
                            case 142:
                            case 135:
                            case 138:
                            case 133:
                            case 136:
                            case 134:
                            case 137:
                            case 237:
                            case 238:
                            case 239:
                            case 240:
                            case 241:
                            case 242:
                            case 243:
                            case 244:
                            case 245:
                            case 246:
                            case 247:
                            case 248:
                            case 254:
                            case 255:
                            case 256:
                            case 257:
                                paramToSend = 0;
                                break;
                            default:
                                paramToSend =  item.itemOptions.get(j).param;
                        }
                        msg.writer().writeInt(paramToSend);
                    }
                }
            }

            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendItemBody(Player player) {
        Message msg;
        try {
            msg = new Message(-37);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.getHead());
            msg.writer().writeByte(player.inventory.itemsBody.size());

            int cntSongoku = 0, cntThienXinHang = 0, cntKirin = 0, cntOcTieu = 0, cntPikkoroDaimao = 0, cntPicolo = 0;
            int cntNappa = 0, cntKakarot = 0, cntCadic = 0;
            int cntThanVuTruKaio = 0, cntNail = 0, cntCadicM = 0, cntchampa = 0;
            int bodySize = Math.min(5, player.inventory.itemsBody.size());
            for (int i = 0; i < bodySize; i++) {
                Item it = player.inventory.itemsBody.get(i);
                if (it == null || !it.isNotNullItem() || it.itemOptions == null) {
                    continue;
                }
                boolean counted = false;
                for (Item.ItemOption io : it.itemOptions) {
                    if (io == null || io.optionTemplate == null) {
                        continue;
                    }
                    switch (io.optionTemplate.id) {
                        case 129:
                        case 141:
                            cntSongoku++;
                            counted = true;
                            break;
                        case 127:
                        case 139:
                            cntThienXinHang++;
                            counted = true;
                            break;
                        case 128:
                        case 140:
                            cntKirin++;
                            counted = true;
                            break;
                        case 131:
                        case 143:
                            cntOcTieu++;
                            counted = true;
                            break;
                        case 132:
                        case 144:
                            cntPikkoroDaimao++;
                            counted = true;
                            break;
                        case 130:
                        case 142:
                            cntPicolo++;
                            counted = true;
                            break;
                        case 135:
                        case 138:
                            cntNappa++;
                            counted = true;
                            break;
                        case 133:
                        case 136:
                            cntKakarot++;
                            counted = true;
                            break;
                        case 134:
                        case 137:
                            cntCadic++;
                            counted = true;
                            break;
                        case 245:
                        case 246:
                        case 247:
                        case 248:
                            cntThanVuTruKaio++;
                            counted = true;
                            break;
                        case 237:
                        case 238:
                        case 239:
                        case 240:
                            cntNail++;
                            counted = true;
                            break;
                        case 241:
                        case 242:
                        case 243:
                        case 244:
                            cntCadicM++;
                            counted = true;
                            break;
                        case 254:
                        case 255:
                        case 256:
                        case 257:
                            cntchampa++;
                            counted = true;
                            break;
                    }
                    if (counted) {
                        break;
                    }
                }
            }

            for (Item item : player.inventory.itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<Item.ItemOption> itemOptions = item.itemOptions;
                    int optCount = Math.min(255, itemOptions != null ? itemOptions.size() : 0);
                    msg.writer().writeByte(optCount);
                    for (int _i = 0; _i < optCount; _i++) {
                        Item.ItemOption itemOption = itemOptions.get(_i);
                        if (itemOption.optionTemplate.id == 213) {
                            int opId = 213;
                            int param = itemOption.param;
                            if (param > 1_000_000) {
                                opId = 223;
                                param /= 1_000_000;
                            } else if (param > 1000) {
                                opId = 222;
                                param /= 1000;
                            }
                            msg.writer().writeShort(opId);
                            msg.writer().writeInt(param);
                        } else {
                            int opId = itemOption.optionTemplate.id;
                            msg.writer().writeShort(opId);
                            int paramToSend =  itemOption.param;
                            switch (opId) {

                                case 129:
                                case 141:
                                    paramToSend =  (cntSongoku >= 5 ? 1 : 0);
                                    break;
                                case 127:
                                case 139:
                                    paramToSend =  (cntThienXinHang >= 5 ? 1 : 0);
                                    break;
                                case 128:
                                case 140:
                                    paramToSend =  (cntKirin >= 5 ? 1 : 0);
                                    break;
                                case 131:
                                case 143:
                                    paramToSend =  (cntOcTieu >= 5 ? 1 : 0);
                                    break;
                                case 132:
                                case 144:
                                    paramToSend =  (cntPikkoroDaimao >= 5 ? 1 : 0);
                                    break;
                                case 130:
                                case 142:
                                    paramToSend =  (cntPicolo >= 5 ? 1 : 0);
                                    break;
                                case 135:
                                case 138:
                                    paramToSend =  (cntNappa >= 5 ? 1 : 0);
                                    break;
                                case 133:
                                case 136:
                                    paramToSend =  (cntKakarot >= 5 ? 1 : 0);
                                    break;
                                case 134:
                                case 137:
                                    paramToSend =  (cntCadic >= 5 ? 1 : 0);
                                    break;
                                case 246:
                                    paramToSend =  (cntThanVuTruKaio >= 2 ? 1 : 0);
                                    break;
                                case 247:
                                    paramToSend =  (cntThanVuTruKaio >= 4 ? 1 : 0);
                                    break;
                                case 248:
                                    paramToSend =  (cntThanVuTruKaio >= 5 ? 1 : 0);
                                    break;
                                case 238:
                                    paramToSend =  (cntNail >= 2 ? 1 : 0);
                                    break;
                                case 239:
                                    paramToSend =  (cntNail >= 4 ? 1 : 0);
                                    break;
                                case 240:
                                    paramToSend =  (cntNail >= 5 ? 1 : 0);
                                    break;
                                case 242:
                                    paramToSend =  (cntCadicM >= 2 ? 1 : 0);
                                    break;
                                case 243:
                                    paramToSend =  (cntCadicM >= 4 ? 1 : 0);
                                    break;
                                case 244:
                                    paramToSend =  (cntCadicM >= 5 ? 1 : 0);
                                    break;
                                case 255:
                                    paramToSend =  (cntchampa >= 2 ? 1 : 0);
                                    break;
                                case 256:
                                    paramToSend =  (cntchampa >= 4 ? 1 : 0);
                                    break;
                                case 257:
                                    paramToSend =  (cntchampa >= 5 ? 1 : 0);
                                    break;
                                default:

                            }
                            msg.writer().writeInt(paramToSend);
                        }
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
        Service.gI().Send_Caitrang(player);
    }

    public void sendItemBox(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(0);
            msg.writer().writeByte(player.inventory.itemsBox.size());
            for (Item it : player.inventory.itemsBox) {
                msg.writer().writeShort(it.isNotNullItem() ? it.template.id : -1);
                if (it.isNotNullItem()) {
                    msg.writer().writeInt(it.quantity);
                    msg.writer().writeUTF(it.getInfo());
                    msg.writer().writeUTF(it.getContent());
                    msg.writer().writeByte(it.itemOptions.size());
                    for (Item.ItemOption io : it.itemOptions) {
                        if (io.optionTemplate.id == 213) {
                            int opId = 213;
                            int param = io.param;
                            if (param > 1_000_000) {
                                opId = 223;
                                param /= 1_000_000;
                            } else if (param > 1000) {
                                opId = 222;
                                param /= 1000;
                            }
                            msg.writer().writeShort(opId);
                            msg.writer().writeInt(param);
                        } else {
                            msg.writer().writeShort(io.optionTemplate.id);
                            msg.writer().writeInt(io.param);
                        }
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
        this.openBox(player);
    }

    public void openBox(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendItemBox1(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(0);
            msg.writer().writeByte(player.inventory.itemsBox1.size());
            for (Item it : player.inventory.itemsBox1) {
                msg.writer().writeShort(it.isNotNullItem() ? it.template.id : -1);
                if (it.isNotNullItem()) {
                    msg.writer().writeInt(it.quantity);
                    msg.writer().writeUTF(it.getInfo());
                    msg.writer().writeUTF(it.getContent());
                    msg.writer().writeByte(it.itemOptions.size());
                    for (Item.ItemOption io : it.itemOptions) {
                        if (io.optionTemplate.id == 213) {
                            int opId = 213;
                            int param = io.param;
                            if (param > 1_000_000) {
                                opId = 223;
                                param /= 1_000_000;
                            } else if (param > 1000) {
                                opId = 222;
                                param /= 1000;
                            }
                            msg.writer().writeShort(opId);
                            msg.writer().writeInt(param);
                        } else {
                            msg.writer().writeShort(io.optionTemplate.id);
                            msg.writer().writeInt(io.param);
                        }
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
        this.openBox1(player);
    }

    public void openBox1(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private boolean addItemSpecial(Player player, Item item) {
        if (item.template.type == 13) {
            int min = 0;
            try {
                if (player.idMark == null) {
                    return false;
                }
                String tagShopBua = null;
                try {
                    java.lang.reflect.Method getTagMethod = player.idMark.getClass().getMethod("getTagNameShop");
                    tagShopBua = (String) getTagMethod.invoke(player.idMark);
                } catch (Exception e) {
                    return false;
                }
                if (tagShopBua == null) {
                    return false;
                }
                switch (tagShopBua) {
                    case "BUA_1H" ->
                        min = 60;
                    case "BUA_8H" ->
                        min = 60 * 8;
                    case "BUA_1M" ->
                        min = 60 * 24 * 30;
                    default -> {
                    }
                }
            } catch (Exception e) {
            }
            player.charms.addTimeCharms(item.template.id, min);
            return true;
        }

        switch (item.template.id) {
            case 568 -> {
                if (player.mabuEgg == null) {
                    MabuEgg.createMabuEgg(player);
                }
                return true;
            }
            case 453 -> {
                player.haveTennisSpaceShip = true;
                return true;
            }
            case 74 -> {
                player.nPoint.setFullHpMp();
                PlayerService.gI().sendInfoHpMp(player);
                return true;
            }
        }
        return false;
    }

    public boolean addItemBag(Player player, Item item) {
        if (player == null || player.inventory == null || item == null || item.template == null) {
            return false;
        }

        if (ItemMapService.gI().isBlackBall(item.template.id)) {
            return BlackBallWarService.gI().pickBlackBall(player, item);
        }
        if (ItemMapService.gI().isNamecBall(item.template.id)
                || ItemMapService.gI().isNamecBallStone(item.template.id)) {
            return NgocRongNamecService.gI().pickNamekBall(player, item);
        }
        if (addItemSpecial(player, item)) {
            return true;
        }
        switch (item.template.type) {
            case 9 -> {
                int actualGold = item.quantity;
                if (player.inventory.gold + item.quantity <= Inventory.LIMIT_GOLD) {
                    if (player.effectSkill.isChibi && player.typeChibi == 0) {
                        player.inventory.gold += item.quantity;
                    }
                    if (player.nPoint.tlGold > 0) {
                        actualGold += (item.quantity * player.nPoint.tlGold / 100); // Tính vàng cộng thêm
                    }
                    if (player.playerIntrinsic.intrinsic.id == 23) {
                        actualGold += player.nPoint.calPercent(actualGold,
                                player.playerIntrinsic.intrinsic.param1);
                    }
                    player.inventory.gold += actualGold;
                    Service.gI().sendMoney(player);
                    return true;
                } else {
                    Service.gI().sendThongBao(player, "Vàng sau khi nhặt quá giới hạn cho phép");
                    return false;
                }
            }
            case 10 -> {
                long gem = (long) player.inventory.gem + (long) item.quantity;
                if (gem > Integer.MAX_VALUE) {
                    gem = Integer.MAX_VALUE;
                }
                player.inventory.gem = (int) gem;
                Service.gI().sendMoney(player);
                return true;
            }
            case 34 -> {
                long ruby = (long) player.inventory.ruby + (long) item.quantity;
                if (ruby > Integer.MAX_VALUE) {
                    ruby = Integer.MAX_VALUE;
                }
                player.inventory.ruby = (int) ruby;
                Service.gI().sendMoney(player);
                return true;
            }
        }
        if (item.template.id == 517) {
            if (player.inventory.itemsBag.size() < Inventory.MAX_ITEMS_BAG) {
                player.inventory.itemsBag.add(ItemService.gI().createItemNull());
                Service.gI().sendThongBaoOK(player, "Hành trang của bạn đã được mở rộng thêm 1 ô");
                return true;
            } else {
                Service.gI().sendThongBaoOK(player, "Hành trang của bạn đã đạt tối đa");
                return false;
            }
        } else if (item.template.id == 518) {
            if (player.inventory.itemsBox.size() < Inventory.MAX_ITEMS_BOX) {
                player.inventory.itemsBox.add(ItemService.gI().createItemNull());
                Service.gI().sendThongBaoOK(player, "Rương đồ của bạn đã được mở rộng thêm 1 ô");
                return true;
            } else {
                Service.gI().sendThongBaoOK(player, "Rương đồ của bạn đã đạt tối đa");
                return false;
            }
        }
        return addItemList(player.inventory.itemsBag, item);
    }

    public boolean addItemBox(Player player, Item item) {
        if (player == null || player.inventory == null || player.inventory.itemsBox == null) {
            return false;
        }
        return addItemList(player.inventory.itemsBox, item);
    }

    // public boolean addItemBox1(Player player, Item item) {
    // if (player == null || player.inventory == null || player.inventory.itemsBox1
    // == null) {
    // return false;
    // }
    // return addItemList(player.inventory.itemsBox1, item);
    // }

    public boolean addItemBox1(Player player, Item item) {
        if (player == null || player.inventory == null || player.inventory.itemsBox1 == null) {
            return false;
        }

        if (item == null || item.template == null) {
            return false;
        }

       
        int[] allowedTypes = { 5, 23, 24, 27, 11 };
        if (!isAllowedType(item.template.type, allowedTypes)) {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return false;
        }

      
        List<Item> box1 = player.inventory.itemsBox1;
        for (int i = 0; i < box1.size(); i++) {
            Item slotItem = box1.get(i);
           
            if (slotItem == null || !slotItem.isNotNullItem()) {
                return addItemList(box1, item);
            }
        }

        
        Service.gI().sendThongBao(player, "Rương đã đầy");
        return false;
    }

    private boolean isAllowedType(int type, int[] allowedTypes) {
        for (int allowedType : allowedTypes) {
            if (type == allowedType) {
                return true;
            }
        }
        return false;
    }

    public boolean addItemList(List<Item> items, Item itemAdd) {
        try {
            if (itemAdd != null && itemAdd.isNotNullItem() && itemAdd.template.id == 901) {
                itemAdd.itemOptions = new java.util.ArrayList<>();
                itemAdd.itemOptions.add(new Item.ItemOption(86, 0));
                itemAdd.itemOptions.add(new Item.ItemOption(93, 35));
                itemAdd.itemOptions.add(new Item.ItemOption(174, 2025));
            }
        } catch (Exception ignored) {
        }
        if (itemAdd == null || itemAdd.itemOptions == null) {
            return false;
        }
        if (itemAdd.itemOptions.isEmpty()) {
            itemAdd.itemOptions.add(new Item.ItemOption(73, 0));
        }
        int[] idParam = isItemIncrementalOption(itemAdd);
        if (idParam[0] != -1) {
            for (Item it : items) {
                if (it.isNotNullItem() && it.template.id == itemAdd.template.id && sameAuditLot(it, itemAdd)) {
                    for (Item.ItemOption io : it.itemOptions) {
                        if (io.optionTemplate.id == idParam[0]) {
                            io.param += idParam[1];
                        }
                    }
                    itemAdd.quantity = 0;
                    return true;
                }
            }
        }
        if (itemAdd.template.isUpToUp) {
            for (Item it : items) {
                if (!it.isNotNullItem() || it.template.id != itemAdd.template.id
                        || !sameAuditLot(it, itemAdd)
                        || (!checkListsEqual(it.itemOptions, itemAdd.itemOptions) && itemAdd.template.id != 2074
                                && !itemAdd.isDaNangCap() && !itemAdd.isManhTS())
                        || it.quantity >= 100_000_000) {
                    continue;
                }
                if ((itemAdd.template.id >= 1066 && itemAdd.template.id <= 1070) || itemAdd.template.id == 457
                        || itemAdd.template.id == 610 || itemAdd.template.type == 14 || itemAdd.template.id == 2048
                        || itemAdd.template.id > 2049 && itemAdd.template.id < 2056 || itemAdd.template.id == 821
                        || itemAdd.template.id == 2075) {
                    it.quantity += itemAdd.quantity;
                    itemAdd.quantity = 0;
                    return true;
                }
                if (it.quantity < 99999) {
                    int add = 99999 - it.quantity;
                    if (itemAdd.quantity <= add) {
                        it.quantity += itemAdd.quantity;
                        itemAdd.quantity = 0;
                        return true;
                    } else {
                        it.quantity = 99999;
                        itemAdd.quantity -= add;
                    }
                }
            }
        }
        if (itemAdd.quantity > 0) {
            for (int i = 0; i < items.size(); i++) {
                if (!items.get(i).isNotNullItem()) {
                    items.set(i, ItemService.gI().copyItem(itemAdd));
                    itemAdd.quantity = 0;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sameAuditLot(Item first, Item second) {
        String a = first == null ? null : first.auditTraceId;
        String b = second == null ? null : second.auditTraceId;
        boolean emptyA = a == null || a.isBlank();
        boolean emptyB = b == null || b.isBlank();
        return emptyA && emptyB || !emptyA && !emptyB && a.equals(b);
    }

    public static boolean checkListsEqual(List<ItemOption> list1, List<ItemOption> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            if (list1.get(i).optionTemplate.id != list2.get(i).optionTemplate.id
                    || list1.get(i).param != list2.get(i).param) {
                return false;
            }
        }

        return true;
    }

    // private int[] isItemIncrementalOption(Item item) {
    // for (Item.ItemOption io : item.itemOptions) {
    // switch (io.optionTemplate.id) {
    // case 1 -> {
    // return new int[]{io.optionTemplate.id, io.param};
    // }
    // case 31 -> {
    // return new int[]{io.optionTemplate.id, io.param};
    // }
    // }
    // }
    // return new int[]{-1, -1};
    // }
    private int[] isItemIncrementalOption(Item item) {
        // Mặc định cho item 933 hoặc 1868
        if (item.id == 933 || item.id == 1868) {
            return new int[] { 31, 1 }; // hoặc param khác nếu cần
        }

        for (Item.ItemOption io : item.itemOptions) {
            switch (io.optionTemplate.id) {
                case 1 -> {
                    return new int[] { io.optionTemplate.id, io.param };
                }
                case 31 -> {
                    return new int[] { io.optionTemplate.id, io.param };
                }
            }
        }
        return new int[] { -1, -1 };
    }

    public byte getCountEmptyBag(Player player) {
        return getCountEmptyListItem(player.inventory.itemsBag);
    }

    public byte getCountEmptyListItem(List<Item> list) {
        byte count = 0;
        for (Item item : list) {
            if (!item.isNotNullItem()) {
                count++;
            }
        }
        return count;
    }

    public byte getIndexBag(Player pl, Item it) {
        for (byte i = 0; i < pl.inventory.itemsBag.size(); ++i) {
            Item item = pl.inventory.itemsBag.get(i);
            if (item != null && it.equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public boolean finditemWoodChest(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 570) {
                return false;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == 570) {
                return false;
            }
        }
        return true;
    }

    public int getParam(Player player, int idoption, int itemID) {
        for (Item it : player.inventory.itemsBag) {
            if (it != null && it.itemOptions != null && it.isNotNullItem() && it.template.id == itemID) {
                for (ItemOption iop : it.itemOptions) {
                    if (iop.optionTemplate.id == idoption) {
                        return iop.param;
                    }
                }
            }
        }
        return 0;
    }

    public void subParamItemsBag(Player player, int itemID, int idoption, int param) {
        Item itemRemove = null;
        for (Item it : player.inventory.itemsBag) {
            if (it != null && it.template.id == itemID) {
                for (ItemOption op : it.itemOptions) {
                    if (op != null && op.optionTemplate.id == idoption) {
                        op.param -= param;
                        if (op.param <= 0) {
                            itemRemove = it;
                        }
                        break;
                    }
                }
                break;
            }
        }
        if (itemRemove != null) {
            removeItem(player.inventory.itemsBag, itemRemove);
        }
    }

    public boolean findItem(Player player, int id) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == id) {
                return true;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == id) {
                return true;
            }
        }
        for (Item item : player.inventory.itemsBox1) {
            if (item.isNotNullItem() && item.template.id == id) {
                return true;
            }
        }
        return false;
    }

    public boolean findItemBongTai(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 454) {
                return true;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == 454) {
                return true;
            }
        }
        return false;
    }

    public boolean findItemBongTaiCap2(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 921) {
                return true;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == 921) {
                return true;
            }
        }
        return false;
    }

    public boolean findItemBongTaiCap3(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 1884) {
                return true;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == 1884) {
                return true;
            }
        }
        return false;
    }

    public boolean findItemSkinQuyLaoKame(Player player) {
        for (Item item : player.inventory.itemsBody) {
            if (item.isNotNullItem() && item.template.id == 710) {
                return true;
            }
        }
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 710) {
                return true;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == 710) {
                return true;
            }
        }
        return false;
    }

    public boolean findItemNTK(Player player) {
        if (player.isPl()) {
            for (Item item : player.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 992) {
                    return true;
                }
            }
            for (Item item : player.inventory.itemsBox) {
                if (item.isNotNullItem() && item.template.id == 992) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findItemTVC(Player player) {
        if (player.isPl()) {
            for (Item item : player.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 2077) {
                    return true;
                }
            }
            for (Item item : player.inventory.itemsBox) {
                if (item.isNotNullItem() && item.template.id == 2077) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean findItemTatVoGiangSinh(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 649) {
                return true;
            }
        }
        return false;
    }

    public boolean findSenzu(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                return true;
            }
        }
        return false;
    }

    public boolean fullSetThan(Player player) {
        for (int i = 0; i < 5; i++) {
            Item item = player.inventory.itemsBody.get(i);
            if (item == null || item.template == null || item.template.level != 13) {
                return false;
            }
        }
        return true;
    }

    public boolean x99ThucAn(Player player) {
        Item doAn = player.inventory.itemsBag.stream()
                .filter(it -> it != null && it.template != null
                        && (it.template.id == 663 || it.template.id == 664 || it.template.id == 665
                                || it.template.id == 666 || it.template.id == 667)
                        && it.quantity >= 99)
                .findFirst().orElse(null);
        return doAn != null;
    }

    public boolean canOpenBillShop(Player player) {
        return fullSetThan(player) && x99ThucAn(player);
    }

    public boolean optionCanUpgrade(int id) {
        return id == 0 || id == 22 || id == 23 || id == 14 || id == 27 || id == 28 || id == 47;
    }

    public int getIndexItem(Player player, List<Item> items, Item item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    public int getIndexItemBag(Player player, Item item) {
        return getIndexItem(player, player.inventory.itemsBag, item);
    }

    public int getIndexItemBody(Player player, Item item) {
        return getIndexItem(player, player.inventory.itemsBody, item);
    }

    public int getIndexItemBox(Player player, Item item) {
        return getIndexItem(player, player.inventory.itemsBox, item);
    }

    private void handleOption210(Item item) {
        for (int i = 0; i < item.itemOptions.size(); i++) {
            Item.ItemOption io = item.itemOptions.get(i);
            if (io.optionTemplate.id == 210) {
                int option210Index = i;
                item.itemOptions.remove(i);
                int numberOfOptionsToAdd = io.param;
                int[] allOptions = { 8, 14, 108, 94, 108, 16, 80, 81, 97, 100, 101, 104, 106 };
                List<Integer> selectedOptions = new ArrayList<>();
                while (selectedOptions.size() < numberOfOptionsToAdd && selectedOptions.size() < allOptions.length) {
                    int randomIndex = (int) (Math.random() * allOptions.length);
                    int selectedOption = allOptions[randomIndex];
                    if (!selectedOptions.contains(selectedOption)) {
                        selectedOptions.add(selectedOption);
                    }
                }
                List<Item.ItemOption> newOptions = new ArrayList<>();
                for (int option : selectedOptions) {
                    int newParam = 0;
                    if (option == 8 || option == 14 || option == 108 || option == 94 || option == 108) {
                        newParam = 3 + (int) (Math.random() * 3);
                    } else if (option == 16 || option == 80 || option == 81 || option == 97 || option == 100
                            || option == 101 || option == 104) {
                        newParam = 10 + (int) (Math.random() * 16);
                    } else if (option == 106) {
                        newParam = 0;
                    }
                    newOptions.add(new Item.ItemOption(option, newParam));
                }
                item.itemOptions.addAll(option210Index, newOptions);
                break;
            }
        }
    }

    // option 261 nhận 5-10% new
    private void handleOption261(Item item) {
        for (int i = 0; i < item.itemOptions.size(); i++) {
            Item.ItemOption io = item.itemOptions.get(i);
            if (io.optionTemplate.id == 261) {
                int option261Index = i;
                item.itemOptions.remove(i);
                int newParam = 5 + (int) (Math.random() * 6);
                Item.ItemOption newOption = new Item.ItemOption(10, newParam);
                item.itemOptions.add(option261Index, newOption);
                break;
            }
        }
    }

    private void checkOption231(Item item) {
        for (int i = 0; i < item.itemOptions.size(); i++) {
            Item.ItemOption io = item.itemOptions.get(i);
            if (io.optionTemplate.id == 231) {
                item.itemOptions.remove(i);
                double randomValue = Math.random();
                if (randomValue <= 0.99) {
                    int[] validParams = { 3, 7, 15, 21 };
                    int selectedParam = validParams[(int) (Math.random() * validParams.length)];
                    item.itemOptions.add(new Item.ItemOption(93, selectedParam));
                }
                break;
            }
        }
    }
}
