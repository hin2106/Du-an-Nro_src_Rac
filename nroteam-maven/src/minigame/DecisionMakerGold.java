/*
 * Copyright by SOULMATE
 */

package minigame;

import consts.ConstMiniGame;
import npc.Npc;
import player.Player;
import services.Service;
import utils.Util;

public class DecisionMakerGold {

    public static void showMenuSelect(Npc npc, Player player) {
        long totalNormal = DecisionMakerService.getTotalMoney(ConstMiniGame.VANG, true);
        long totalVIP = DecisionMakerService.getTotalMoney(ConstMiniGame.VANG, false);
        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_DECISION_MAKER_GOLD,
                "Tổng giải thưởng: " + Util.number(totalNormal) + " vàng, cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, ConstMiniGame.VANG, true) + "%\n"
                        + "Tổng giải VIP: " + Util.number(totalVIP) + " vàng, cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, ConstMiniGame.VANG, false) + "%\n"
                        + "Thời gian còn lại: " + DecisionMakerData.timeGame + " giây.",
                "Cập nhật",
                "Thường\n1 triệu\nvàng",
                "VIP\n10 triệu\nvàng",
                "Đóng"
        );
    }

    public static void selectPlay(Npc npc, Player player, boolean isNormal) {
        int money = isNormal ? ConstMiniGame.COST_GOLD_NORMAL : ConstMiniGame.COST_GOLD_VIP;
        if (player.inventory.gold < money) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + (money - player.inventory.gold) + " vàng nữa");
            return;
        }
        player.inventory.gold -= money;
        Service.gI().sendMoney(player);
        DecisionMakerService.newData(player, money, ConstMiniGame.VANG, isNormal);
        showMenuSelect(npc, player);
    }

}
