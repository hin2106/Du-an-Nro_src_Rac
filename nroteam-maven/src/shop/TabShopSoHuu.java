package shop;

import item.Item;
import player.Player;
import player.badges.BagesTemplate;
import task.BadgesTaskService;

import java.util.ArrayList;
import java.util.List;

public class TabShopSoHuu extends TabShop {

    public TabShopSoHuu(TabShop tabShop, Player player) {
        this.itemShops = new ArrayList<>();
        this.shop = tabShop.shop;
        this.id = tabShop.id;

        // Lấy danh sách badges player sở hữu (chỉ badges còn hạn)
        List<Integer> ownedBadgeItems = BagesTemplate.listEffect(player);
        this.name = tabShop.name + ownedBadgeItems.size();

        if (player == null || player.dataBadges == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (ItemShop itemShop : tabShop.itemShops) {
            if (itemShop.temp.gender == player.gender || itemShop.temp.gender > 2) {
                // Kiểm tra xem player có sở hữu badge này không
                boolean shouldAdd = false;
                int idEffect = BagesTemplate.fineIdEffectbyIdItem(itemShop.temp.id);

                if (idEffect > 0) {
                    // Kiểm tra trực tiếp trong dataBadges của player
                    for (player.badges.BadgesData badgeData : player.dataBadges) {
                        if (badgeData.idBadGes == idEffect) {
                            // Kiểm tra badge còn hạn sử dụng
                            if (badgeData.timeofUseBadges == Long.MAX_VALUE ||
                                    badgeData.timeofUseBadges >= currentTime) {
                                shouldAdd = true;
                                break;
                            }
                        }
                    }
                }

                if (shouldAdd) {
                    int daysRemaining = BadgesTaskService.sendDay(player, idEffect);
                    if (daysRemaining > 0 || daysRemaining == -1) {
                        ItemShop itemShopCopy = new ItemShop(itemShop);
                        int displayDays = (daysRemaining == -1) ? 9999 : daysRemaining;
                        BagesTemplate badgeTemplate = BagesTemplate.fineBadgesbyIdItem(itemShop.temp.id);
                        if (badgeTemplate != null) {
                            itemShopCopy.options.clear();
                            for (Item.ItemOption badgeOption : badgeTemplate.options) {
                                if (badgeOption.optionTemplate.id != 93 && badgeOption.optionTemplate.id != 220) {
                                    itemShopCopy.options.add(new Item.ItemOption(
                                            badgeOption.optionTemplate.id,
                                            badgeOption.param));
                                }
                            }
                        }
                        itemShopCopy.options.add(new Item.ItemOption(93, displayDays));
                        this.itemShops.add(itemShopCopy);
                    }
                }
            }
        }
    }
}
