package combine.list;

import consts.ConstNpc;
import item.Item;
import combine.CombineService;
import static combine.CombineService.MAX_STAR_ITEM;
import combine.CombineSystem;
import player.Player;
import server.ServerNotify;
import services.player.InventoryService;
import services.Service;
import utils.Util;

public class PhaLeHoaTrangBi {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Hãy chọn 1 vật phẩm để pha lê hóa", "Đóng");
            return;
        }

        Item item = player.combineNew.itemsCombine.get(0);
        if (!CombineSystem.isTrangBiPhaLeHoa(item)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Vật phẩm này không thể đục lỗ", "Đóng");
            return;
        }

        int star = 0;
        int epStar = -1;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 107) {
                star = io.param;
            }
            if (io.optionTemplate.id == 102) {
                epStar = io.param;
            }
        }

        if (star >= MAX_STAR_ITEM) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
            return;
        }

        if (star < 7) {
            processPhaLeHoa(player, item, star);
            return;
        }

        if (epStar == -1 || epStar != star) {
            String msg = (star == 8)
                    ? "Cần cường hóa và ép lỗ thứ 8 để tiếp tục pha lê hóa"
                    : "Chưa ép hết lỗ sao, không thể pha lê hóa";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, msg, "Đóng");
            return;
        }

        processPhaLeHoa(player, item, star);
    }

    private static void processPhaLeHoa(Player player, Item item, int star) {
        player.combineNew.goldCombine = CombineSystem.getGoldPhaLeHoa(star);
        player.combineNew.gemCombine = CombineSystem.getGemPhaLeHoa(star);
        player.combineNew.ratioCombine = CombineSystem.getRatioPhaLeHoa(star);

        StringBuilder npcSay = new StringBuilder(item.template.name + "\n|2|");
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id != 102) {
                npcSay.append(io.getOptionString()).append("\n");
            }
        }
        npcSay.append("|7|Tỉ lệ thành công: ").append(player.combineNew.ratioCombine).append("%\n");

		if (player.combineNew.goldCombine <= player.inventory.gold
				&& player.combineNew.gemCombine <= player.inventory.gem) {
            npcSay.append("|1|Cần ")
                    .append(Util.numberToMoney(player.combineNew.goldCombine))
                    .append(" vàng và ")
					.append(player.combineNew.gemCombine);
			CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
					"Nâng cấp\n " + (player.combineNew.gemCombine * 100) + " ngọc\n x100 lần",
					"Nâng cấp\n " + (player.combineNew.gemCombine * 10) + " ngọc\n x10 lần",
					"Nâng cấp\n " + player.combineNew.gemCombine + " ngọc");
        } else {
            String missing = "";
            if (player.combineNew.goldCombine > player.inventory.gold) {
                missing += "Thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng\n";
            }
            if (player.combineNew.gemCombine > player.inventory.gem) {
                missing += "Thiếu " + (player.combineNew.gemCombine - player.inventory.gem) + " ngọc";
            }
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay + missing, "Đóng");
        }
    }

    public static void phaLeHoa(Player player, int... numm) {
        if (player.idMark != null && !Util.canDoWithTime(player.idMark.getLastTimeCombine(), 500)) {
            return;
        }
        player.idMark.setLastTimeCombine(System.currentTimeMillis());

        int n = (numm.length > 0) ? numm[0] : 1;
        if (player.combineNew.itemsCombine.isEmpty()) {
            return;
        }

        Item item = player.combineNew.itemsCombine.get(0);
        if (!CombineSystem.isTrangBiPhaLeHoa(item)) {
            return;
        }

        int star = 0;
        Item.ItemOption optionStar = null;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 107) {
                star = io.param;
                optionStar = io;
                break;
            }
        }

        int num = 0;
        int fail = 0;
        boolean success = false;

        for (int i = 0; i < n; i++) {
            num = i + 1;

            long goldNeed = CombineSystem.getGoldPhaLeHoa(star);
            int gemNeed = CombineSystem.getGemPhaLeHoa(star);
            int ratioBase = (int) CombineSystem.getRatioPhaLeHoa(star);

            if (player.inventory.gold < goldNeed || player.inventory.gem < gemNeed) {
                break;
            }

            player.inventory.gold -= goldNeed;
            player.inventory.gem -= gemNeed;
            double ratioBonus = 1.0;
            if (optionStar != null) {
                switch (optionStar.param) {
                    case 4, 5, 6, 7, 8, 9 ->
                        ratioBonus = 1.2;
                }
            }

            if (Util.isTrue((int) (ratioBase * ratioBonus), 100)) {
                success = true;
                star++;
                if (optionStar == null) {
                    optionStar = new Item.ItemOption(107, star);
                    item.itemOptions.add(optionStar);
                } else {
                    optionStar.param = star;
                }
                break;
            } else {
                fail++;
            }
        }

        if (success) {
            if (star >= 7) {
                ServerNotify.gI().notify("Chúc mừng " + player.name
                        + " vừa pha lê hóa thành công " + item.template.name
                        + " lên " + star + " sao pha lê");
            }
            if (n > 1 && num > 1) {
                Service.gI().sendThongBao(player, "Thành công sau " + num + " lần thử, lên " + star + " sao!");
            } else {
                Service.gI().sendThongBao(player, "Pha lê hóa thành công, trang bị lên " + star + " sao!");
            }
            CombineService.gI().sendEffectSuccessCombine(player);
            dragonpass.DragonPassService.gI().onEquipmentCrystalized(player);
        } else {
            Service.gI().sendThongBao(player, "Pha lê hóa thất bại sau " + fail + " lần thử!");
            CombineService.gI().sendEffectFailCombine(player);
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
    }
}
