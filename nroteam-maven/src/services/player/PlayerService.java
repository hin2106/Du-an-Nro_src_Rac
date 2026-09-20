package services.player;

import daos.PlayerDAO;
import item.Item;
import player.Player;
import network.Message;
import server.Client;
import services.EffectSkillService;
import services.Service;
import services.TaskService;
import services.map.ChangeMapService;
import services.map.MapService;
import utils.Logger;
import utils.Util;

public class PlayerService {

    private static PlayerService i;

    public PlayerService() {
    }

    public static PlayerService gI() {
        if (i == null) {
            i = new PlayerService();
        }
        return i;
    }

    public void sendTNSM(Player player, byte type, int param) {
        if (param == 0 || player == null) {
            return;
        }
        try {
            Message msg = new Message(-3);
            msg.writer().writeByte(type);
            msg.writer().writeInt(param);//hnamwrite
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void sendTNSM_Sub(Player player, byte type, long amountToSubtract) {
        if (amountToSubtract <= 0 || player == null) {
            return;
        }

        try {
            long negativeParam = -amountToSubtract;
            if (negativeParam < Integer.MIN_VALUE) {
                Logger.logException(Service.class, new Exception("Giá trị mất quá lớn để dùng writeInt."));
                return;
            }
            Message msg = new Message(-3);
            msg.writer().writeByte(type);
            msg.writeSmartLong(Util.maxIntValue(negativeParam));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendPowerInfo(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        if (!player.isPet && !player.isBoss && !player.isNewPet) {
            Message msg = null;
            try {
                msg = new Message(-42);
                msg.writer().writeLong(player.nPoint.power);
                player.sendMessage(msg);
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            } finally {
                if (msg != null) {
                    msg.cleanup();
                }
            }
        }
    }

    public void sendMessageAllPlayer(Message msg) {
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();
    }

    public void sendMessageIgnore(Player plIgnore, Message msg) {
        for (Player pl : Client.gI().getPlayers()) {
            if (pl != null && !pl.equals(plIgnore)) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();
    }

    public void sendInfoHp(Player player) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 5);
            msg.writeSmartLong(Util.maxIntValue(player.nPoint.hp));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void sendInfoMp(Player player) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 6);
            msg.writeSmartLong(Util.maxIntValue(player.nPoint.mp));
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void sendInfoHpMp(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        sendInfoHp(player);
        sendInfoMp(player);
    }

    public void hoiPhuc(Player player, long hp, long mp) {
        if (!player.isDie()) {
            player.nPoint.addHp(hp);
            player.nPoint.addMp(mp);
            Service.gI().Send_Info_NV(player);
            if (!player.isPet && !player.isNewPet) {
                PlayerService.gI().sendInfoHpMp(player);
            }
        }
    }

    public void sendInfoHpMpMoney(Player player) {
        if (player == null || !player.isPl()) {
            return;
        }
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 4);
            try {
                if (player.getSession().version >= 214) {
                    msg.writer().writeLong(player.inventory.gold);
                } else {
                    msg.writer().writeInt((int) player.inventory.gold);
                }
            } catch (Exception e) {
                msg.writer().writeInt((int) player.inventory.gold);
            }
            msg.writer().writeInt(player.inventory.gem);
            if (Util.readInt) {
                msg.writer().writeInt((int) Util.maxIntValue(player.nPoint.hp));
                msg.writer().writeInt((int) Util.maxIntValue(player.nPoint.mp));
            } else {
                msg.writer().writeLong(player.nPoint.hp);
                msg.writer().writeLong(player.nPoint.mp);
            }

            msg.writer().writeInt(player.inventory.ruby);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }



    public void playerMove(Player player, int x, int y) {
        if (player.zone == null) {
            return;
        }
        if (!player.isDie()) {
            if (player.effectSkill.isCharging) {
                EffectSkillService.gI().stopCharge(player);
            }
            if (player.effectSkill.useTroi) {
                EffectSkillService.gI().removeUseTroi(player);
            }
            player.location.x = x;
            player.location.y = y;
            player.location.lastTimeplayerMove = System.currentTimeMillis();
            switch (player.zone.map.mapId) {
                case 85:
                case 86:
                case 87:
                case 88:
                case 89:
                case 90:
                case 91:
                    if (!player.isBoss && !player.isPet) {
                        if (x < 24 || x > player.zone.map.mapWidth - 24 || y < 0 || y > player.zone.map.mapHeight - 24) {
                            if (MapService.gI().getWaypointPlayerIn(player) == null) {
                                ChangeMapService.gI().changeMap(player, 21 + player.gender, 0, 200, 336);
                                return;
                            }
                        }
                        int yTop = player.zone.map.yPhysicInTop(player.location.x, player.location.y);
                        if (yTop >= player.zone.map.mapHeight - 24) {
                            ChangeMapService.gI().changeMap(player, 21 + player.gender, 0, 200, 336);
                            return;
                        }
                    }
                    break;
            }
            if (player.pet != null) {
                player.pet.followMaster();
            }
            if (player.newPet != null) {
                player.newPet.followMaster();
            }
            if (player.isPl()) {
                try {
                    int type = player.zone.map.tileMap[player.location.y / 24][player.location.x / 24];
                    player.isFly = type == 0;
                } catch (Exception e) {
                }
                if (player.isFly ) {
                    Item mountItem = player.inventory.itemsBody.size() > 7 ? player.inventory.itemsBody.get(7) : null;
                    boolean hasMount = mountItem != null && mountItem.isNotNullItem()
                            && (mountItem.template.type == 23 || mountItem.template.type == 24);
                    if (hasMount) {
                        boolean hasOpt89 = mountItem.getOptionParam(89) >= 0 && mountItem.itemOptions.stream().anyMatch(o -> o.optionTemplate.id == 89);
                        boolean hasOpt85 = mountItem.itemOptions.stream().anyMatch(o -> o.optionTemplate.id == 85);
                        if (hasOpt89) {
                            // Option 89: phục hồi 0.5% tổng HP + KI khi bay (min 1, max 1000)
                            long hpRestore = Math.max(1, Math.min(1000, player.nPoint.hpMax / 200));
                            long mpRestore = Math.max(1, Math.min(1000, player.nPoint.mpMax / 200));
                            hoiPhuc(player, hpRestore, mpRestore);
                        } else if (hasOpt85) {
                            // Option 85: phục hồi 0.5% tổng KI khi bay (min 1, max 1000)
                            long mpRestore = Math.max(1, Math.min(1000, player.nPoint.mpMax / 200));
                            hoiPhuc(player, 0, mpRestore);
                        }
                        // Có mount nhưng không có option 85/89 → không trừ KI
                    } else if (player.getMount() == -1) {
                        // Không có mount type 23/24 → trừ KI khi bay
                        long mp = player.nPoint.mpg / (100 * (player.effectSkill.isMonkey ? 2 : 1));
                        hoiPhuc(player, 0, -mp);
                    }
                }
            }
            MapService.gI().sendPlayerMove(player);
            TaskService.gI().checkDoneTaskGoToMap(player, player.zone);
        }
    }

   

    public void sendCurrentStamina(Player player) {
        Message msg;
        try {
            msg = new Message(-68);
            msg.writer().writeShort(player.nPoint.stamina);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void sendMaxStamina(Player player) {
        Message msg;
        try {
            msg = new Message(-69);
            msg.writer().writeShort(player.nPoint.maxStamina);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    public void changeAndSendTypePK(Player player, int type) {
        changeTypePK(player, type);
        sendTypePk(player);
    }

    public void changeTypePK(Player player, int type) {
        player.typePk = (byte) type;
    }

    public void sendTypePk(Player player) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 35);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeByte(player.typePk);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void banPlayer(Player playerBaned) {
        PlayerDAO.banAccount(playerBaned.getSession(), playerBaned);
        Service.gI().sendThongBao(playerBaned,
                "Tài khoản của bạn đã bị khóa\nGame sẽ mất kết nối sau 5 giây...");
        playerBaned.idMark.setLastTimeBan(System.currentTimeMillis());
        playerBaned.idMark.setBan(true);
    }

    private static final int COST_GOLD_HOI_SINH = 20_000;
    private static final int COST_GEM_HOI_SINH = 1;
    private static final int COST_GOLD_HOI_SINH_NRSD = 50_000;

    public void hoiSinh(Player player) {
        if (player.isDie() && player.zone != null && player.zone.map.mapId != 51) {
            if (Util.canDoWithTime(player.lastTimeRevived, 1500)) {
                boolean canHs;
                if (MapService.gI().isMapBlackBallWar(player.zone.map.mapId)) {
                    if (player.inventory.gold >= COST_GOLD_HOI_SINH_NRSD) {
                        player.inventory.gold -= COST_GOLD_HOI_SINH_NRSD;
                        canHs = true;
                    } else {
                        Service.gI().sendThongBao(player,
                                "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GOLD_HOI_SINH_NRSD
                                        - player.inventory.gold) + " vàng");
                        return;
                    }
                } else {
                    if (player.inventory.gem >= COST_GEM_HOI_SINH) {
                        player.inventory.gem -= COST_GEM_HOI_SINH;
                        canHs = true;
                    } else if (player.inventory.ruby >= COST_GEM_HOI_SINH) {
                        player.inventory.ruby -= COST_GEM_HOI_SINH;
                        canHs = true;
                    } else {
                        Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                        return;
                    }
                }
                if (canHs) {
                    Service.gI().sendMoney(player);
                    Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
                }
            }
        }
    }

    public void Dievenha(Player player) {
        if (player == null || player.nPoint == null) {
            return;
        }
        try {
            // Kiểm tra zone và map null
            if (player.zone == null || player.zone.map == null) {
                return;
            }
            int mapId = MapService.gI().isMapMaBu(player.zone.map.mapId)
                    ? 114
                    : player.gender + 21;
            ChangeMapService.gI().changeMapBySpaceShip(player, mapId, 0, -1);

            long powerBefore = player.nPoint.power;

            if (powerBefore < 10_000_000L) {
                return;
            }
            long lose = powerBefore / 10;

            if (lose <= 0) {
                return; // Miễn trừ
            }
            if (lose < 1_000) {
                lose = 1_000;
            }

            long maxLose = Math.min(5_000_000L, powerBefore / 5);
            if (lose > maxLose) {
                lose = maxLose;
            }
            long newPower = powerBefore - lose;

            if (newPower < 2_000_000L) {
                newPower = 2_000_000L;
            }
            if (newPower > powerBefore) {
                newPower = powerBefore;
            }
            if (newPower < 0) {
                newPower = 2_000_000L;
            }
            player.nPoint.power = newPower;
            long actualLose = powerBefore - newPower;
            if (actualLose > 0) {
                sendTNSM_Sub(player, (byte) 2, actualLose);
            }
        } catch (Exception e) {
            Logger.logException(PlayerService.class, e);
        }
    }

    private long tilegiamsucmanhdie(long power) {
        if (power > 1_000_000L) {
            return 0L; // miễn trừ
        }
        if (power > 100_000_000L) {
            return 500_000L;
        }
        if (power > 1_000_000_000L) {
            return 2_000_000L;
        }
        if (power > 60_000_000_000L) {
            return 3_000_000L;
        }
        return 10_000_000L;
    }

    public void hoiSinhMaBu(Player player) {
        if (player.isDie()) {
            boolean canHs = false;
            if (MapService.gI().isMapMaBu(player.zone.map.mapId)) {
                if (player.inventory.gold >= COST_GOLD_HOI_SINH_NRSD) {
                    player.inventory.gold -= COST_GOLD_HOI_SINH_NRSD;
                    canHs = true;
                } else {
                    Service.gI().sendThongBao(player,
                            "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GOLD_HOI_SINH_NRSD
                                    - player.inventory.gold) + " vàng");
                    return;
                }
            } else {
                if (player.inventory.gold >= COST_GOLD_HOI_SINH) {
                    player.inventory.gold -= COST_GOLD_HOI_SINH;
                    canHs = true;
                } else {
                    Service.gI().sendThongBao(player,
                            "Không đủ vàng để thực hiện, còn thiếu " + Util.numberToMoney(COST_GOLD_HOI_SINH
                                    - player.inventory.gold) + " vàng");
                    return;
                }
            }
            if (canHs) {
                Service.gI().sendMoney(player);
                Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
            }
        }
    }

}
