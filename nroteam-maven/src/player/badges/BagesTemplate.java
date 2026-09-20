package player.badges;

import item.Item;
import player.Player;
import server.Manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BagesTemplate {

    public int id;
    public int idEffect;
    public int idItem;
    public String NAME;
    public List<Item.ItemOption> options = new ArrayList<>();

    public static int findIdItemByIdIdEffect(int idEffect) {
        for (BagesTemplate data : Manager.BAGES_TEMPLATES) {
            if (data.idEffect == idEffect) {
                return data.idItem;
            }
        }
        return -1;
    }

    public static int fineIdEffectbyIdItem(int idItem) {
        for (BagesTemplate data : Manager.BAGES_TEMPLATES) {
            if (data.idItem == idItem) {
                return data.idEffect;
            }
        }
        return -1;
    }

    public static BagesTemplate fineBadgesbyIdItem(int idItem) {
        for (BagesTemplate data : Manager.BAGES_TEMPLATES) {
            if (data.idItem == idItem) {
                return data;
            }
        }
        return null;
    }

    public static List<Integer> listEffect(Player player) {
        if (player == null || player.dataBadges == null || player.dataBadges.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> setIdItem = new HashSet<>();
        long currentTime = System.currentTimeMillis();

        for (BadgesData data : player.dataBadges) {
            if (data.timeofUseBadges == Long.MAX_VALUE || data.timeofUseBadges >= currentTime) {
                for (BagesTemplate temp : Manager.BAGES_TEMPLATES) {
                    if (temp.idEffect == data.idBadGes) {
                        setIdItem.add(temp.idItem);
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(setIdItem);
    }

    public static int getDaysFromBadgeOptions(int idEffect) {
        for (BagesTemplate temp : Manager.BAGES_TEMPLATES) {
            if (temp.idEffect == idEffect) {
                for (Item.ItemOption option : temp.options) {
                    if (option.optionTemplate.id == 93) {
                        return option.param; // Trả về số ngày
                    }
                }
                return -1;
            }
        }
        return 30;
    }

    public static List<Item.ItemOption> sendListItemOption(Player player) {
        List<Item.ItemOption> listOptions = new ArrayList<>();
        BadgesData activeBadge = null;
        for (BadgesData data : player.dataBadges) {
            if (data.isUse) {
                activeBadge = data;
                break;
            }
        }

        if (activeBadge == null) {
            return listOptions;
        }
        for (BagesTemplate temp : Manager.BAGES_TEMPLATES) {
            if (temp.idEffect == activeBadge.idBadGes) {
                for (Item.ItemOption option : temp.options) {
                    if (option.optionTemplate.id == 93 || option.optionTemplate.id == 220) {
                        continue;
                    }
                    listOptions.add(option);
                }
                break;
            }
        }

        return listOptions;
    }

}
