package minigame;

import consts.ConstFont;
import consts.ConstMiniGame;
import npc.Npc;
import player.Player;
import services.ItemTimeService;
import services.Service;
import utils.Util;

public class RockPaperScissors {

    public static final byte KEO = 0;
    public static final byte BUA = 1;
    public static final byte BAO = 2;
    
    public static long timePlay = 15;
    public static int COST_0 = 1_000_000;
    public static int COST_1 = 5_000_000;
    public static int COST_2 = 10_000_000;
    public static int COST_3 = 500_000_000; 

    public static void confirmMenu(Npc npc, Player player, int select) {
        int tiendatcuoc;
        switch (select) {
            case 0 -> tiendatcuoc = COST_0;
            case 1 -> tiendatcuoc = COST_1;
            case 2 -> tiendatcuoc = COST_2;
            case 3 -> tiendatcuoc = COST_3;
            default -> {
                return;
            } 
        }

        String money = Util.numberToMoney(tiendatcuoc);
        player.idMark.setMoneyKeoBuaBao(tiendatcuoc);
        player.idMark.setTimePlayKeoBuaBao(System.currentTimeMillis() + (timePlay * 1000));
        ItemTimeService.gI().sendTextTimeKeoBuaBao(player, (int) timePlay);

        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                ConstFont.BOLD_GREEN + "Mức vàng cược: " + money + "\n"
                        + ConstFont.BOLD_DARK + "Hãy chọn Kéo, Búa hoặc Bao\n"
                        + ConstFont.BOLD_RED + "Thời gian " + timePlay + " giây bắt đầu",
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");
    }

    public static void confirmPlay(Npc npc, Player player, int select) {
        switch (select) {
            case 0, 1, 2 -> {
                if (player.inventory.gold < player.idMark.getMoneyKeoBuaBao()) {
                    long soVangConThieu = player.idMark.getMoneyKeoBuaBao() - player.inventory.gold;
                    Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(soVangConThieu) + " vàng nữa");
                    return;
                }

                byte playerChoice = (byte) select;
                player.idMark.setKeoBuaBaoPlayer(playerChoice);
                byte serverChoice = RockPaperScissorsService.generateServerChoiceAI(player);
                player.idMark.setKeoBuaBaoServer(serverChoice);

                int result = RockPaperScissorsService.checkWinLose(player);

                switch (result) {
                    case 1 -> RockPaperScissorsService.winKeoBuaBao(npc, player);
                    case 2 -> RockPaperScissorsService.loseKeoBuaBao(npc, player);
                    default -> RockPaperScissorsService.hoaKeoBuaBao(npc, player);
                }
            }
            case 3 -> npc.createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO,
                    "Hãy chọn mức cược.",
                    "1 Tr vàng",
                    "5 Tr vàng",
                    "10 Tr vàng",
                    "500 Tr vàng");
            default -> {
            }
        }
    }
}