package shop;

import item.Item;
import player.Player;
import task.BadgesTaskService;
import java.util.ArrayList;
import player.badges.BagesTemplate;

public class TabShopDanhHieu extends TabShop {

    public TabShopDanhHieu(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;
        this.name = tabShop.name;

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == player.gender || itemShop.temp.gender > 2) {
                boolean shouldAdd = true;
                for (Integer i : BagesTemplate.listEffect(player)) {
                    if (itemShop.temp.id == i) {
                        shouldAdd = false;
                        break;
                    }
                }
                if (shouldAdd) {
                    ItemShop itemShopCopy = new ItemShop(itemShop);
                    
                    int percent = BadgesTaskService.sendPercenBadgesTask(player, BagesTemplate.fineIdEffectbyIdItem(itemShopCopy.temp.id));
                    if (percent != 0) {
                        boolean optionExists = false;
                        for (Item.ItemOption option : itemShopCopy.options) {
                            if (option.optionTemplate.id == 220) {
                                optionExists = true;
                                option.param = percent;
                                break;
                            }
                        }
                        if (!optionExists) {
                            itemShopCopy.options.add(0, new Item.ItemOption(220, percent));
                        }
                    }
                    
                    this.itemShops.add(itemShopCopy);
                }
            }
        }
    }
}
