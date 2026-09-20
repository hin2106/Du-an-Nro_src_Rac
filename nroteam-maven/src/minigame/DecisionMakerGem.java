/*
 * Copyright by SOULMATE
 */

package minigame;

import consts.ConstMiniGame;
import npc.Npc;
import player.Player;
import services.Service;
import utils.Util;

public class DecisionMakerGem {
    public static void showMenuSelect(Npc npc, Player player) {
        long totalNormal = DecisionMakerService.getTotalMoney(ConstMiniGame.NGOC_XANH, true);
        long totalVIP = DecisionMakerService.getTotalMoney(ConstMiniGame.NGOC_XANH, false);
        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_DECISION_MAKER_GEM,
                "Tổng giải thưởng: " + Util.number(totalNormal) + " hồng ngọc, cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, ConstMiniGame.NGOC_XANH, true) + "%\n"
                        + "Tổng giải VIP: " + Util.number(totalVIP) + " hồng ngọc, cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, ConstMiniGame.NGOC_XANH, false) + "%\n"
                        + "Thời gian còn lại: " + DecisionMakerData.timeGame + " giây.",
                "Cập nhật",
                "Thường\n10 ngọc\nxanh",
                "VIP\n100 ngọc\nxanh",
                "Đóng"
        );
    }

    public static void selectPlay(Npc npc, Player player, boolean isNormal) {
        int money = isNormal ? ConstMiniGame.COST_GEM_NORMAL : ConstMiniGame.COST_GEM_VIP;
        if (player.inventory.gem < money) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu " + (money - player.inventory.gem) + " ngọc nữa");
            return;
        }
        player.inventory.gem -= money;
        Service.gI().sendMoney(player);
        DecisionMakerService.newData(player, money, ConstMiniGame.NGOC_XANH, isNormal);
        showMenuSelect(npc, player);
    }
}
