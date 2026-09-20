package minigame;

import consts.ConstFont;
import consts.ConstMiniGame;
import npc.Npc;
import player.Player;
import services.Service;
import utils.Util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class RockPaperScissorsService {

    private static final int HISTORY_SIZE_FOR_AI = 5;
    private static final Random random = new Random();

    public static void loseKeoBuaBao(Npc npc, Player player) {
        String ketQuaPlayer = convertNumberToString(player.idMark.getKeoBuaBaoPlayer());
        String ketQuaServer = convertNumberToString(player.idMark.getKeoBuaBaoServer());
        String money = Util.numberToMoney(player.idMark.getMoneyKeoBuaBao());
        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                ConstFont.BOLD_RED + "Bạn ra cái <" + ketQuaPlayer + ">\n"
                        + "Tôi ra cái <" + ketQuaServer + ">\n"
                        + ConstFont.BOLD_DARK + "Tôi thắng nhé hihi\n"
                        + ConstFont.BOLD_RED + "Bạn bị trừ " + money + " vàng",
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");
        player.inventory.gold -= player.idMark.getMoneyKeoBuaBao();
        Service.gI().sendMoney(player);
        player.idMark.addPlayerChoiceHistory(player.idMark.getKeoBuaBaoPlayer());
    }

    public static void winKeoBuaBao(Npc npc, Player player) {
        String ketQuaPlayer = convertNumberToString(player.idMark.getKeoBuaBaoPlayer());
        String ketQuaServer = convertNumberToString(player.idMark.getKeoBuaBaoServer());
        String money = Util.numberToMoney(player.idMark.getMoneyKeoBuaBao());
        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                ConstFont.BOLD_GREEN + "Bạn ra cái <" + ketQuaPlayer + ">\n"
                        + "Tôi ra cái <" + ketQuaServer + ">\n"
                        + ConstFont.BOLD_DARK + "Bạn thắng rồi huhu\n"
                        + ConstFont.BOLD_GREEN + "Bạn nhận được " + money + " vàng",
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");
        player.inventory.gold += player.idMark.getMoneyKeoBuaBao();
        Service.gI().sendMoney(player);
        player.idMark.addPlayerChoiceHistory(player.idMark.getKeoBuaBaoPlayer());
    }

    public static void hoaKeoBuaBao(Npc npc, Player player) {
        String ketQuaPlayer = convertNumberToString(player.idMark.getKeoBuaBaoPlayer());
        String ketQuaServer = convertNumberToString(player.idMark.getKeoBuaBaoServer());
        npc.createOtherMenu(player, ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                ConstFont.BOLD_BLUE + "Bạn ra cái <" + ketQuaPlayer + ">\n"
                        + "Tôi ra cái <" + ketQuaServer + ">\n"
                        + ConstFont.BOLD_YELLOW + "Hoà nhau nhé haha",
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");
        player.idMark.addPlayerChoiceHistory(player.idMark.getKeoBuaBaoPlayer());
    }

    public static String convertNumberToString(int i) {
        switch (i) {
            case RockPaperScissors.KEO -> {
                return "Kéo";
            }
            case RockPaperScissors.BUA -> {
                return "Búa";
            }
            case RockPaperScissors.BAO -> {
                return "Bao";
            }
        }
        return "";
    }

    public static int checkWinLose(Player player) {
        byte playerChoice = player.idMark.getKeoBuaBaoPlayer();
        byte serverChoice = player.idMark.getKeoBuaBaoServer();

        if (playerChoice == serverChoice) {
            return 3;
        }

        if ((playerChoice == RockPaperScissors.KEO && serverChoice == RockPaperScissors.BAO) ||
            (playerChoice == RockPaperScissors.BUA && serverChoice == RockPaperScissors.KEO) ||
            (playerChoice == RockPaperScissors.BAO && serverChoice == RockPaperScissors.BUA)) {
            return 1; 
        } else {
            return 2; 
        }
    }

    public static byte generateServerChoiceAI(Player player) {
        List<Byte> history = player.idMark.getRecentPlayerChoices(HISTORY_SIZE_FOR_AI);

        if (history == null || history.size() < 3) {
            return (byte) Util.nextInt(3);
        }

        Map<Byte, Long> counts = history.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        Byte mostFrequentChoice = Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
        long maxCount = counts.get(mostFrequentChoice);

        boolean clearPreference = counts.values().stream().filter(count -> count == maxCount).count() == 1;

        if (!clearPreference) {
            return (byte) Util.nextInt(3);
        }

        byte counterMove;
        switch (mostFrequentChoice) {
            case RockPaperScissors.KEO -> counterMove = RockPaperScissors.BUA;
            case RockPaperScissors.BUA -> counterMove = RockPaperScissors.BAO;
            case RockPaperScissors.BAO -> counterMove = RockPaperScissors.KEO;
            default -> {
                return (byte) Util.nextInt(3);
            }
        }
        int chance = random.nextInt(100);
        if (chance < 50) { 
            return counterMove;
        } else if (chance < 75) {
            return (byte) ((counterMove + 1) % 3);
        } else { 
            return (byte) ((counterMove + 2) % 3);
        }
    }
}