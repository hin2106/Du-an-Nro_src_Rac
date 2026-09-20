package minigame;

import consts.ConstMiniGame;
import consts.ConstNpc;
import daos.NDVSqlFetcher;
import npc.Npc;
import player.Player;
import server.Maintenance;
import utils.Functions;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class DecisionMaker implements Runnable {

    private static DecisionMaker instance;

    public static DecisionMaker gI() {
        if (instance == null) {
            instance = new DecisionMaker();
        }
        return instance;
    }

    public static boolean spinGame;
    public static boolean delayNewGame;

    static {
        DecisionMakerData.timeGame = DecisionMakerData.timeGameDefalue;
        spinGame = true;
        delayNewGame = false;
    }

    public List<DecisionMakerData> listPlayer = new ArrayList<>();
    public List<DecisionMakerData.resulPlayer> listResulPlayer = new ArrayList<>();

    @Override
    public void run() {
        while (!Maintenance.isRunning()) {
            try {
                if (DecisionMakerData.timeGame > 0) {
                    DecisionMakerData.timeGame--;
                }
                if (DecisionMakerData.timeGame == 0 && spinGame) {
                    spinGame();
                }
                if (DecisionMakerData.timeDelay > 0) {
                    DecisionMakerData.timeDelay--;
                    if (DecisionMakerData.timeDelay == 0) {
                        resetNewGame();
                    }
                }
                Functions.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void spinGame() {
        playerWin(ConstMiniGame.VANG, true);
        playerWin(ConstMiniGame.VANG, false);

        playerWin(ConstMiniGame.NGOC_XANH, true);
        playerWin(ConstMiniGame.NGOC_XANH, false);

        playerWin(ConstMiniGame.HONG_NGOC, true);
        playerWin(ConstMiniGame.HONG_NGOC, false);

        spinGame = false;
        delayNewGame = true;
        DecisionMakerData.timeDelay = 60;
    }

    public void resetNewGame() {
        DecisionMakerData.timeGame = DecisionMakerData.timeGameDefalue;
        spinGame = true;
        delayNewGame = false;
        listPlayer.clear();
        listResulPlayer.clear();
    }

    public void playerWin(byte TYPE, boolean isNormal) {
        List<DecisionMakerData> listPl = new ArrayList<>();
        if (!listPlayer.isEmpty()) {
            for (DecisionMakerData player : listPlayer) {
                if (player.type == TYPE && player.isNormal == isNormal) {
                    listPl.add(player);
                }
            }
        }
        if (!listPl.isEmpty()) {
            int index = Util.nextInt(0, listPl.size() - 1);
            long playerId = listPl.get(index).id;
            Player player = NDVSqlFetcher.loadById(playerId);
            DecisionMakerService.newDataResul(player, TYPE, listPl.get(index).money);
        }
    }

    public void showMenuWaitNewGame(Npc npc, Player player) {
        StringBuilder npcSay = new StringBuilder("Chúc mừng các bạn may mắn được chọn lần trước là:");
        for (DecisionMakerData.resulPlayer pl : listResulPlayer) {
            String currency;
            currency = switch (pl.type) {
                case ConstMiniGame.VANG -> " vàng";
                case ConstMiniGame.HONG_NGOC, ConstMiniGame.NGOC_XANH -> " hồng ngọc";
                default -> "";
            };
            npcSay.append("\n").append(pl.name).append(" +").append(Util.numberToMoney(pl.money)).append(currency);
        }
        npcSay.append("\nTrò chơi sẽ bắt đầu sau: ").append(DecisionMakerData.timeDelay).append(" giây nữa.");
        npc.createOtherMenu(player, ConstMiniGame.MENU_WAIT_NEW_GAME, npcSay.toString(), "Thể lệ", "OK");
    }

    public void showMenu(Npc npc, Player player) {
        if (DecisionMaker.delayNewGame && DecisionMakerData.timeDelay > 0) {
            DecisionMaker.gI().showMenuWaitNewGame(npc, player);
            return;
        }
        npc.createOtherMenu(player, ConstMiniGame.MENU_CHON_AI_DAY,
                "Trò chơi Chọn Ai Đây đang được diễn ra, nếu bạn tin tưởng mình đang tràn đầy may mắn thì có thể tham gia thử.",
                "Thể lệ",
                "Chọn bằng\nVàng",
                "Chọn bằng\nhồng ngọc",
                "Chọn bằng\nngọc xanh");
    }

    public void showTutorial(Npc npc, Player player) {
        npc.createOtherMenu(player, ConstNpc.IGNORE_MENU, """
              M\u1ed7i l\u01b0\u1ee3t ch\u01a1i c\u00f3 6 gi\u1ea3i th\u01b0\u1edfng
              \u0110\u01b0\u1ee3c ch\u1ecdn t\u1ed1i \u0111a 10 l\u1ea7n m\u1ed7i gi\u1ea3i
              Th\u1eddi gian 1 l\u01b0\u1ee3t ch\u1ecdn l\u00e0 5 ph\u00fat
              Khi h\u1ebft gi\u1edd, h\u1ec7 th\u1ed1ng s\u1ebd ng\u1eabu nhi\u00ean ch\u1ecdn ra 1 ng\u01b0\u1eddi may m\u1eafn
              c\u1ee7a t\u1eebng gi\u1ea3i v\u00e0 trao th\u01b0\u1edfng.
              L\u01b0u \u00fd: N\u1ebfu tham gia tr\u00f2 ch\u01a1i b\u1eb1ng Ng\u1ecdc xanh ho\u1eb7c H\u1ed3ng ng\u1ecdc
              th\u00ec ng\u01b0\u1eddi th\u1eafng s\u1ebd nh\u1eadn \u0111\u01b0\u1ee3c l\u00e0 h\u1ed3ng ng\u1ecdc.""",
                "OK");
    }
}