
package combine.list;
/*
 * author: Đỗ Hoàng Hiếu 
 * zalo: 0988171315
 */
import combine.CombineService;
import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import item.Item.ItemOption;
import player.Player;
import services.Service;
import services.player.InventoryService;
import utils.Util;

public class ChuyenHoaDungVang {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            notice(player, "Hãy đặt vào đúng 2 trang bị để chuyển hoá");
            return;
        }

        Item item1 = player.combineNew.itemsCombine.get(0);
        Item item2 = player.combineNew.itemsCombine.get(1);
        if (item1 == null || item2 == null) return;

        // Đảm bảo item1 là Lưỡng Long (level 12)
        if (item1.template.level != 12) {
            Item temp = item1; item1 = item2; item2 = temp;
        }

        int level1 = item1.template.level, level2 = item2.template.level;
        if ((level1 != 12 && level1 != 13) || (level2 != 12 && level2 != 13)) {
            notice(player, "Cần trang bị có level phù hợp");
            return;
        }

        for (ItemOption io : item2.itemOptions) {
            int id = io.optionTemplate.id;
            if ((id == 72 && io.param != 0) || ((id == 107 || id == 102) && io.param > 0)) {
                notice(player, "Trang bị thần phải có cấp độ 0 và không có sao pha lê");
                return;
            }
        }

        if (item1.template.type != item2.template.type || item1.template.gender != item2.template.gender) {
            notice(player, "Cần 2 trang bị cùng loại và cùng hành tinh");
            return;
        }

        int levelUpgrade = item1.itemOptions.stream()
            .filter(io -> io.optionTemplate.id == 72)
            .mapToInt(io -> io.param)
            .findFirst().orElse(0);

        if (levelUpgrade < 4) {
            notice(player, "Trang bị gốc phải có cấp độ từ +4 trở lên");
            return;
        }

        // Hiển thị thông tin
        StringBuilder info = new StringBuilder(ConstFont.BOLD_BLUE + "Hiện tại " + item2.template.name);
        item2.itemOptions.stream()
            .filter(io -> io.optionTemplate.id != 72 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107)
            .forEach(io -> info.append("\n").append(ConstFont.BOLD_DARK).append(
                (io.optionTemplate.id == 22 || io.optionTemplate.id == 23)
                    ? io.optionTemplate.name.replace("#", String.valueOf(io.param))
                    : io.getOptionString()
            ));

        info.append("\n").append(ConstFont.BOLD_BLUE).append("Sau khi nâng cấp (+" + levelUpgrade + ")");
        item2.itemOptions.stream()
            .filter(io -> io.optionTemplate.id != 72 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107)
            .forEach(io -> {
                float param = io.param;
                for (int i = 1; i < levelUpgrade; i++) param += param * 0.1f;
                int newParam = Math.round(param);
                info.append("\n").append(ConstFont.BOLD_GREEN)
                    .append(io.optionTemplate.name.replace("#", String.valueOf(newParam)));
            });

        int baseOptionCount = (int) item1.itemOptions.stream()
            .filter(io -> io.optionTemplate.id != 72 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107)
            .count();

        long cost = (baseOptionCount * 50L + (levelUpgrade - 1) * 250L + 100L) * 100_000L;
        info.append("\n").append(ConstFont.BOLD_GREEN).append("Chuyển qua tất cả sao pha lê");
        info.append("\n").append(player.inventory.gold < cost ? ConstFont.BOLD_RED : ConstFont.BOLD_BLUE)
            .append("Cần ").append(Util.numberToMoney(cost)).append(" vàng");

        if (player.inventory.gold < cost) {
            notice(player, info.toString(), "Còn thiếu\n" + Util.numberToMoney(cost - player.inventory.gold));
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                info.toString(), "Chuyển hoá\n" + Util.numberToMoney(cost) + " vàng", "Từ chối");
        }

        player.combineNew.goldCombine = (int) cost;
        player.combineNew.levelCombine = levelUpgrade - 1;
    }

    public static void ThucHienChuyenHoaVang(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) return;

        Item item1 = player.combineNew.itemsCombine.get(0);
        Item item2 = player.combineNew.itemsCombine.get(1);
        if (item1 == null || item2 == null) return;

        if (item1.template.level != 12) {
            Item temp = item1; item1 = item2; item2 = temp;
        }

        int level = item1.itemOptions.stream()
            .filter(io -> io.optionTemplate.id == 72)
            .mapToInt(io -> io.param)
            .findFirst().orElse(0);

        long cost = player.combineNew.goldCombine;
        if (player.inventory.gold < cost) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }

        player.inventory.gold -= cost;
        item2.itemOptions.removeIf(io -> io.optionTemplate.id == 6 || io.optionTemplate.id == 7);

        item2.itemOptions.stream()
            .filter(io -> io.optionTemplate.id != 72 && io.optionTemplate.id != 102 && io.optionTemplate.id != 107)
            .forEach(io -> {
                float param = io.param;
                for (int i = 1; i < level; i++) param += param * 0.1f;
                io.param = Math.round(param);
            });

        for (ItemOption io1 : item1.itemOptions) {
            if (io1.optionTemplate.id == 107) {
                item2.itemOptions.removeIf(io2 -> io2.optionTemplate.id == 107);
                item2.itemOptions.add(new ItemOption(107, io1.param));
            } else if (io1.optionTemplate.id != 72 && io1.optionTemplate.id != 102 && io1.optionTemplate.id != 6 && io1.optionTemplate.id != 7) {
                boolean exists = item2.itemOptions.stream().anyMatch(io2 -> io2.optionTemplate.id == io1.optionTemplate.id);
                if (!exists) {
                    float param = io1.param;
                    for (int i = 1; i < level; i++) param += param * 0.1f;
                    item2.itemOptions.add(new ItemOption(io1.optionTemplate.id, Math.round(param)));
                }
            }
        }

        item2.itemOptions.removeIf(io -> io.optionTemplate.id == 72);
        item2.itemOptions.add(new ItemOption(72, level - 1));

        InventoryService.gI().subQuantityItemsBag(player, item1, 1);
        CombineService.gI().sendEffectSuccessCombine(player);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static void notice(Player player, String message) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, message, "Đóng");
    }

    private static void notice(Player player, String message, String subOption) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, message, subOption);
    }
}
