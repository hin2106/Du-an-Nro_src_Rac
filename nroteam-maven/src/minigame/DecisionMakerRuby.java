/*
 * Copyright by SOULMATE
 */

package minigame;

import consts.ConstMiniGame;
import npc.Npc;
import player.Player;
import services.Service;
import utils.Util;

public class DecisionMakerRuby {
    public static void showMenuSelect(Npc npc, Player player) {
        long totalNormal = DecisionMakerService.getTotalMoney(ConstMiniGame.HONG_NGOC, true);
        long totalVIP = DecisionMakerService.getTotalMoney(ConstMiniGame.HONG_NGOC, false);
        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_DECISION_MAKER_RUBY,
                "Tổng giải thưởng: " + Util.number(totalNormal) + " hồng ngọc, cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, ConstMiniGame.HONG_NGOC, true) + "%\n"
                        + "Tổng giải VIP: " + Util.number(totalVIP) + " hồng ngọc, cơ hội trúng của bạn là: " + DecisionMakerService.getPercent(player, ConstMiniGame.HONG_NGOC, false) + "%\n"
                        + "Thời gian còn lại: " + DecisionMakerData.timeGame + " giây.",
                "Cập nhật",
                "Thường\n10 ngọc\nxanh",
                "VIP\n100 ngọc\nxanh",
                "Đóng"
        );
    }

    public static void selectPlay(Npc npc, Player player, boolean isNormal) {
        int money = isNormal ? ConstMiniGame.COST_RUBY_NORMAL : ConstMiniGame.COST_RUBY_VIP;
        if (player.inventory.ruby < money) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu " + (money - player.inventory.ruby) + " ngọc nữa");
            return;
        }
        player.inventory.ruby -= money;
        Service.gI().sendMoney(player);
        DecisionMakerService.newData(player, money, ConstMiniGame.HONG_NGOC, isNormal);
        showMenuSelect(npc, player);
    }
}
