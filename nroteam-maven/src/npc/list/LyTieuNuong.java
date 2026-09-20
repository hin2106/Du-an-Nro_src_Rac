package npc.list;

import consts.ConstMiniGame;
import consts.ConstNpc;
import minigame.DecisionMaker;
import minigame.DecisionMakerGem;
import minigame.DecisionMakerGold;
import minigame.DecisionMakerRuby;
import minigame.LuckyNumber;
import minigame.LuckyNumberService;
import minigame.RockPaperScissors;
import npc.Npc;
import player.Player;
import services.func.Input;

public class LyTieuNuong extends Npc {

    public LyTieuNuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Mini game.", "Kéo\nBúa\nBao", "Con số\nmay mắn\nvàng", "Con số\nmay mắn\nngọc xanh", "Chọn ai đây", "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 ->
                        createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO, "Hãy chọn mức cược.", "1 Tr vàng", "5 Tr vàng", "10 Tr vàng", "500 Tr vàng");
                    case 1 -> {
                        LuckyNumber.showMenu(this, player, false);
                        player.idMark.setGemCSMM(false);
                    }
                    case 2 -> {
                        LuckyNumber.showMenu(this, player, true);
                        player.idMark.setGemCSMM(true);
                    }
                    case 3 ->
                        DecisionMaker.gI().showMenu(this, player);
                    default -> {
                    }
                }
            } else {
                switch (player.idMark.getIndexMenu()) {
                    case ConstMiniGame.MENU_KEO_BUA_BAO -> RockPaperScissors.confirmMenu(this, player, select);
                    case ConstMiniGame.MENU_PLAY_KEO_BUA_BAO -> {
                        if (player.idMark.getTimePlayKeoBuaBao() - System.currentTimeMillis() > 0) {
                            RockPaperScissors.confirmPlay(this, player, select);
                        } else {
                            createOtherMenu(player, ConstMiniGame.MENU_KEO_BUA_BAO, "Hãy chọn mức cược.", "1 Tr vàng", "5 Tr vàng", "10 Tr vàng", "500 Tr vàng");
                        }
                    }
                    case ConstMiniGame.MENU_CON_SO_MAY_MAN_VANG -> {
                    }
                    case ConstMiniGame.MENU_CON_SO_MAY_MAN_NGOC -> {
                    }
                    case ConstMiniGame.MENU_CHON_AI_DAY -> {
                        switch (select) {
                            case 0 -> DecisionMaker.gI().showTutorial(this, player);
                            case 1 -> DecisionMakerGold.showMenuSelect(this, player);
                            case 2 -> DecisionMakerRuby.showMenuSelect(this, player);
                            case 3 -> DecisionMakerGem.showMenuSelect(this, player);
                        }
                    }
                    case ConstMiniGame.MENU_LUCKY_NUMBER -> {
                        if (select == 0) {
                            LuckyNumber.showMenu(this, player, player.idMark.isGemCSMM());
                        }
                    }
                    case ConstMiniGame.MENU_PLAY_LUCKY_NUMBER_GOLD, ConstMiniGame.MENU_PLAY_LUCKY_NUMBER_GEM -> {
                        switch (select) {
                            case 0 -> LuckyNumber.showMenu(this, player, player.idMark.isGemCSMM());
                            case 1 -> Input.gI().createFormSelectOneNumberLuckyNumber(player, player.idMark.isGemCSMM());
                            case 2 -> LuckyNumberService.addOneNumber(player, true);
                            case 3 -> LuckyNumberService.addOneNumber(player, false);
                            case 4 -> LuckyNumber.showMenuTutorials(this, player);
                            default -> {
                        }
                        }
                    }
                    case ConstMiniGame.MENU_PLAY_DECISION_MAKER_GOLD -> {
                        switch (select) {
                            case 0 -> DecisionMakerGold.showMenuSelect(this, player);
                            case 1 -> DecisionMakerGold.selectPlay(this, player, true);
                            case 2 -> DecisionMakerGold.selectPlay(this, player, false);
                        }
                    }
                    case ConstMiniGame.MENU_PLAY_DECISION_MAKER_GEM -> {
                        switch (select) {
                            case 0 -> DecisionMakerGem.showMenuSelect(this, player);
                            case 1 -> DecisionMakerGem.selectPlay(this, player, true);
                            case 2 -> DecisionMakerGem.selectPlay(this, player, false);
                        }
                    }
                    case ConstMiniGame.MENU_PLAY_DECISION_MAKER_RUBY -> {
                        switch (select) {
                            case 0 -> DecisionMakerRuby.showMenuSelect(this, player);
                            case 1 -> DecisionMakerRuby.selectPlay(this, player, true);
                            case 2 -> DecisionMakerRuby.selectPlay(this, player, false);
                        }
                    }
                    case ConstMiniGame.MENU_WAIT_NEW_GAME -> {
                        if (select == 0) {
                            DecisionMaker.gI().showTutorial(this, player);
                        }
                    }
                    default -> {
                    }
                }
            }
        }
    }
}