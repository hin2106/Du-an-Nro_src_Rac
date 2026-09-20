package services.func;
import bot.Bot;
import daos.PlayerDAO;
import data.AlyraManager;
import player.Player;
import network.Message;
import server.Client;
import server.Maintenance;
import services.Service;
import utils.Functions;
import utils.Logger;
import utils.Util;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TransactionService implements Runnable {

    private static final int TIME_DELAY_TRADE = 10000;

    static final Map<Player, Trade> PLAYER_TRADE = new HashMap<>();

    private static final byte SEND_INVITE_TRADE = 0;
    private static final byte ACCEPT_TRADE = 1;
    private static final byte ADD_ITEM_TRADE = 2;
    private static final byte CANCEL_TRADE = 3;
    private static final byte LOCK_TRADE = 5;
    private static final byte ACCEPT = 7;

    private static TransactionService i;

    private TransactionService() {
    }

    public static TransactionService gI() {
        if (i == null) {
            i = new TransactionService();
            new Thread(i).start();
        }
        return i;
    }

    public void controller(Player pl, Message msg) {
        try {
            byte action = msg.reader().readByte();
            int playerId = -1;
            Player plMap = null;
            Trade trade = PLAYER_TRADE.get(pl);
            if (pl.baovetaikhoan) {
                Service.gI().sendThongBao(pl, "Chức năng bảo vệ đã được bật. Bạn vui lòng kiểm tra lại");
                return;
            }
            if (action == SEND_INVITE_TRADE) {
                pl.idMark.setTransactionWP(false);
                pl.idMark.setTransactionWVP(false);
            }
            switch (action) {
                case SEND_INVITE_TRADE, ACCEPT_TRADE -> {
                    playerId = msg.reader().readInt();
                    plMap = pl.zone.getPlayerInMap(playerId);
                    if (plMap != null && plMap.isPl()) {
                        if (pl.nPoint.power < 20_000_000 || plMap.nPoint.power < 20_000_000) {
                            Service.gI().sendThongBao(pl, "Ngươi phải đạt ít nhất 20 triệu sức mạnh mới có thể giao dịch.");
                            return;
                        }
                        if (plMap.tradeWVP) {
                            return;
                        }
                        trade = PLAYER_TRADE.get(pl);
                        if (trade == null) {
                            trade = PLAYER_TRADE.get(plMap);
                        }
                        if (trade == null) {
                            if (action == SEND_INVITE_TRADE) {
                                if (Util.canDoWithTime(pl.idMark.getLastTimeTrade(), TIME_DELAY_TRADE)
                                        && Util.canDoWithTime(plMap.idMark.getLastTimeTrade(), TIME_DELAY_TRADE)) {
                                    boolean checkLogout1 = false;
                                    boolean checkLogout2 = false;
                                    try (Connection con = AlyraManager.getConnection()) {
                                        checkLogout1 = PlayerDAO.checkLogout(con, pl);
                                        checkLogout2 = PlayerDAO.checkLogout(con, plMap);
                                    } catch (Exception e) {
                                    }
                                    if (checkLogout1) {
                                        Client.gI().kickSession(pl.getSession());
                                        break;
                                    }
                                    if (checkLogout2) {
                                        Client.gI().kickSession(plMap.getSession());
                                        break;
                                    }
                                    pl.idMark.setLastTimeTrade(System.currentTimeMillis());
                                    pl.idMark.setPlayerTradeId((int) plMap.id);
                                    sendInviteTrade(pl, plMap);
                                } else {
                                    final Player target = plMap;
                                    long timeLeft = TIME_DELAY_TRADE - (System.currentTimeMillis() - Math.max(pl.idMark.getLastTimeTrade(), target.idMark.getLastTimeTrade()));
                                    int secondsLeft = (int) Math.ceil(timeLeft / 1000.0);
                                    Service.gI().sendThongBao(pl, "Vui lòng chờ " + secondsLeft + " giây để có thể giao dịch lại.");
                                }
                            } else {
                                if (plMap.idMark.getPlayerTradeId() == pl.id) {
                                    trade = new Trade(pl, plMap);
                                    trade.openTabTrade();
                                }
                            }
                        } else {
                            Service.gI().sendThongBao(pl, "Không thể thực hiện");
                        }
                    }
                }
                case ADD_ITEM_TRADE -> {
                    if (trade != null) {
                        byte index = msg.reader().readByte();
                        int quantity = msg.reader().readInt();
                        if (quantity < 0) {
                            Service.gI().sendThongBao(pl, "Không thể thực hiện");
                            trade.cancelTrade();
                            break;
                        }
                        if (quantity == 0) {//do
                            quantity = 1;
                        }
                        if (index != -1 && quantity > Trade.QUANLITY_MAX) {
                            Service.gI().sendThongBao(pl, "Đã quá giới hạn giao dịch...");
                            trade.cancelTrade();
                            break;
                        }
                        trade.addItemTrade(pl, index, quantity);
                    }
                }
                case CANCEL_TRADE -> {
                    if (trade != null) {
                        trade.cancelTrade();
                    }
                }
                case LOCK_TRADE -> {
                    if (Maintenance.isRunning()) {
                        trade.cancelTrade();
                        break;
                    }
                    if (trade != null) {
                        trade.lockTran(pl);
                    }
                }
                case ACCEPT -> {
                    if (Maintenance.isRunning()) {
                        trade.cancelTrade();
                        break;
                    }
                    if (trade != null) {
                        trade.acceptTrade();
                        if (trade.accept == 1) {
                            Service.gI().sendThongBao(pl, "Xin chờ đối phương đồng ý");
                        } 
                        // else if (trade.accept == 2) {
                        //     trade.dispose();
                        // }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(this.getClass(), e);
        }
    }

    /**
     * Mời giao dịch
     */
    private void sendInviteTrade(Player plInvite, Player plReceive) {
        Message msg = null;
        try {
            msg = new Message(-86);
            msg.writer().writeByte(0);
            msg.writer().writeInt((int) plInvite.id);
            plReceive.sendMessage(msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    /**
     * Hủy giao dịch
     *
     * @param player
     */
    public void cancelTrade(Player player) {
        Trade trade = PLAYER_TRADE.get(player);
        if (trade != null) {
            trade.cancelTrade();
        }
    }

    public boolean check(Player player) {
        return PLAYER_TRADE.get(player) != null;
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning()) {
            try {
                long st = System.currentTimeMillis();
                Set<Map.Entry<Player, Trade>> entrySet = PLAYER_TRADE.entrySet();
                for (Map.Entry entry : entrySet) {
                    ((Trade) entry.getValue()).update();
                }
                Functions.sleep(Math.max(300 - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
            }
        }
    }
}
