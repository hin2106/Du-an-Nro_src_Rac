package services;

import consts.ConstPlayer;
import item.Item;
import static item.ItemTime.*;
import player.Fusion;
import player.Player;
import network.Message;
import java.io.IOException;
import dungeon.DestronGas;
import dungeon.RedRibbonHQ;
import dungeon.SnakeWay;
import dungeon.TreasureUnderSea;
import services.player.InventoryService;
import utils.Logger;

public class ItemTimeService {

    private static ItemTimeService i;

    public static ItemTimeService gI() {
        if (i == null) {
            i = new ItemTimeService();
        }
        return i;
    }

    public void sendAllItemTime(Player player) {
        if (player.effectSkill != null && player.effectSkill.isThoiMien) {
            long elapsed = System.currentTimeMillis() - player.effectSkill.lastTimeThoiMien;
            long remainingMs = player.effectSkill.timeThoiMien - elapsed;
            int remainingSeconds = (int) (remainingMs / 1000);

            if (remainingMs <= 0) {
                System.out.println("[ItemTimeService] ThoiMien expired but not removed, forcing remove");
                services.EffectSkillService.gI().removeThoiMien(player);
            } else {
                sendItemTime(player, 3782, remainingSeconds);
            }
        }

        ItemTimeService.gI().sendTextBanDoKhoBau(player);
        ItemTimeService.gI().sendTextDoanhTrai(player);
        ItemTimeService.gI().sendTextConDuongRanDoc(player);
        ItemTimeService.gI().sendTextKhiGasHuyDiet(player);
        ItemTimeService.gI().sendTextTimePickDoanhTrai(player);
        if (player.fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
            sendItemTime(player, player.gender == ConstPlayer.NAMEC ? 3901 : 3790,
                    (int) ((Fusion.TIME_FUSION - (System.currentTimeMillis() - player.fusion.lastTimeFusion)) / 1000));
        }

        if (player.itemTime.isUseBoHuyet) {
            sendItemTime(player, 2755,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet)) / 1000));
        }
        if (player.itemTime.isUseBoKhi) {
            sendItemTime(player, 2756,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi)) / 1000));
        }
        if (player.itemTime.isUseGiapXen) {
            sendItemTime(player, 2757,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen)) / 1000));
        }
        if (player.itemTime.isUseCuongNo) {
            sendItemTime(player, 2754,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo)) / 1000));
        }

        if (player.itemTime.isUseAnDanh) {
            sendItemTime(player, 2760,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh)) / 1000));
        }
        if (player.itemTime.isUseBoHuyet2) {
            sendItemTime(player, 10714,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoHuyet2)) / 1000));
        }
        if (player.itemTime.isUseBoKhi2) {
            sendItemTime(player, 10715,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBoKhi2)) / 1000));
        }
        if (player.itemTime.isUseGiapXen2) {
            sendItemTime(player, 10712,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeGiapXen2)) / 1000));
        }
        if (player.itemTime.isUseCuongNo2) {
            sendItemTime(player, 10716,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeCuongNo2)) / 1000));
        }
        if (player.itemTime.isUseBanhChung) {
            sendItemTime(player, 7079,
                (int) ((TIME_BANH_TET - (System.currentTimeMillis() - player.itemTime.lastTimeBanhChung)) / 1000));
        }
        if (player.itemTime.isUseBanhTet) {
            sendItemTime(player, 7080,
                (int) ((TIME_BANH_TET - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTet)) / 1000));
        }

        if (player.itemTime.isUseAnDanh2) {
            sendItemTime(player, 10717,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeAnDanh2)) / 1000));
        }
        if (player.itemTime.isUseCMS) {
            sendItemTime(player, 5829,
                    (int) ((TIME_CMS - (System.currentTimeMillis() - player.itemTime.lastTimeUseCMS)) / 1000));
        }
        if (player.itemTime.isUseNCD) {
            sendItemTime(player, 11173,
                    (int) ((TIME_NCD - (System.currentTimeMillis() - player.itemTime.lastTimeUseNCD)) / 1000));
        }
        if (player.itemTime.isUseGTPT) {
            sendItemTime(player, 3778,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeUseGTPT)) / 1000));
        }
        if (player.itemTime.isUseDK) {
            sendItemTime(player, 5072,
                    (int) ((TIME_DK - (System.currentTimeMillis() - player.itemTime.lastTimeUseDK)) / 1000));
        }
        if (player.itemTime.isOpenPower) {
            sendItemTime(player, 3783,
                    (int) ((TIME_OPEN_POWER - (System.currentTimeMillis() - player.itemTime.lastTimeOpenPower))
                            / 1000));
        }
        if (player.itemTime.isUseMayDo) {
            sendItemTime(player, 2758,
                    (int) ((TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseMayDo)) / 1000));
        }
        if (player.itemTime.isUseKhoBauX2) {
            sendItemTime(player, 12834,
                    (int) ((TIME_MAY_DO - (System.currentTimeMillis() - player.itemTime.lastTimeUseKhoBauX2)) / 1000));
        }
        if (player.itemTime.isUseBuaSanta) {
            sendItemTime(player, 13540,
                    (int) ((TIME_BUA_SANTA - (System.currentTimeMillis() - player.itemTime.lastTimeBuaSanta)) / 1000));
        }
        if (player.itemTime.isEatMeal) {
            sendItemTime(player, player.itemTime.iconMeal,
                    (int) ((TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal)) / 1000));
        }
        if (player.itemTime.isEatMeal2) {
            sendItemTime(player, player.itemTime.iconMeal2,
                    (int) ((TIME_EAT_MEAL - (System.currentTimeMillis() - player.itemTime.lastTimeEatMeal2)) / 1000));
        }
        if (player.itemTime.isUseTDLT) {
            long remainMsTDLT = (long) player.itemTime.timeTDLT
                    - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT);
            sendItemTime(player, 4387, (int) Math.max(0L, remainMsTDLT / 1000));
        }
        if (player.itemTime.isUseRX) {
            sendItemTime(player, 8579, player.itemTime.timeRX / 1000);
        }
        if (player.itemTime.isUseCoBonLa) {
            sendItemTime(player, 13618,
                    (int) ((TIME_CO_4 - (System.currentTimeMillis() - player.itemTime.lastTimeCoBonLa)) / 1000));
        }
        if (player.itemTime.isUseKhauTrang) {
            sendItemTime(player, 7149,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeKhauTrang)) / 1000));
        }
        if (player.itemTime.isUseBanhTrungThu) {
            sendItemTime(player, 4126,
                    (int) ((TIME_TRUNG_THU - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThu))
                            / 1000));
        }
        if (player.itemTime.isUseBanhTrungThuDacBiet) {
            sendItemTime(player, 11717,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThuDacBiet))
                            / 1000));
        }
        if (player.itemTime.isUseBanhTrungThu2Trung) {
            sendItemTime(player, 4043,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThu2Trung))
                            / 1000));
        }
        if (player.itemTime.isUseBanhTrungThu1Trung) {
            sendItemTime(player, 4042,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhTrungThu1Trung))
                            / 1000));
        }
        if (player.itemTime.isUseBanhDeoThoTrang) {
            sendItemTime(player, 11675,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhDeoThoTrang))
                            / 1000));
        }
        if (player.itemTime.isUseBanhDeoThoXanh) {
            sendItemTime(player, 11676,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhDeoThoXanh)) / 1000));
        }
        if (player.itemTime.isUseBanhDeoThoHong) {
            sendItemTime(player, 11677,
                    (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeBanhDeoThoHong)) / 1000));
        }

        if (player.itemTime.isUseNangLuong) {
            int seconds = (int) ((TIME_ITEM - (System.currentTimeMillis() - player.itemTime.lastTimeNangLuong)) / 1000);
            if (seconds < 0) {
                seconds = 0;
            }
            if (seconds > Short.MAX_VALUE) {
                seconds = Short.MAX_VALUE;
            }
            sendItemTime(player, 15359, seconds);
        }

        if (player.itemTime.isUseCarrot) {
            int seconds = (int) ((player.itemTime.timeCarrot
                    - (System.currentTimeMillis() - player.itemTime.lastTimeCarrot)) / 1000);
            if (seconds > 0) {
                sendItemTime(player, 4083, seconds);
                if (!player.effectSkill.isCarrot) {
                    player.effectSkill.isCarrot = true;
                    player.effectSkill.timeCarrot = seconds * 1000;
                    player.effectSkill.lastTimeCarrot = System.currentTimeMillis();
                    Service.gI().Send_Caitrang(player);
                    Service.gI().point(player);
                    Service.gI().Send_Info_NV(player);
                }
            } else {
                player.itemTime.isUseCarrot = false;
            }
        }
        if (player.itemTime.isHallowen) {
            int seconds = (int) ((player.itemTime.timeHallowen
                    - (System.currentTimeMillis() - player.itemTime.lastTimeHallowen)) / 1000);
            if (seconds > 0) {
                sendItemTime(player, 5101, seconds);
                if (!player.effectSkill.isHalloween) {
                    player.effectSkill.isHalloween = true;
                    player.effectSkill.timeHalloween = seconds * 1000;
                    player.effectSkill.lastTimeHalloween = System.currentTimeMillis();
                    player.effectSkill.idOutfitHalloween = player.itemTime.idOutfitHalloween;
                    Service.gI().Send_Caitrang(player);
                }
            } else {
                player.itemTime.isHallowen = false;
            }
        }

        if (player.combineNew != null && player.combineNew.timeDelay > 0) {
            long elapsed = System.currentTimeMillis() - player.combineNew.startTimeDelay;
            long remaining = player.combineNew.timeDelay - elapsed;
            if (remaining > 0) {
                sendItemTime(player, 11797, (int) (remaining / 1000));
            }
        }

        // Kiểm tra và cập nhật thời gian hiệu lực điều ước Shenron
        if (player.itemEvent.isShenronPetX3) {
            long timeRemaining = TIME_SHENRON_EVENT
                    - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPetX3);
            if (timeRemaining > 0) {
                sendItemTime(player, 6579, (int) (timeRemaining / 1000));
            } else {
                // Thời gian đã hết, tắt flag
                player.itemEvent.isShenronPetX3 = false;
                player.itemEvent.lastTimeShenronPetX3 = 0;
            }
        }
        if (player.itemEvent.isShenronPorataHp) {
            long timeRemaining = TIME_SHENRON_EVENT
                    - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPorataHp);
            if (timeRemaining > 0) {
                sendItemTime(player, 6579, (int) (timeRemaining / 1000));
            } else {
                // Thời gian đã hết, tắt flag
                player.itemEvent.isShenronPorataHp = false;
                player.itemEvent.lastTimeShenronPorataHp = 0;
            }
        }
        if (player.itemEvent.isShenronPorataKi) {
            long timeRemaining = TIME_SHENRON_EVENT
                    - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPorataKi);
            if (timeRemaining > 0) {
                sendItemTime(player, 6579, (int) (timeRemaining / 1000));
            } else {
                // Thời gian đã hết, tắt flag
                player.itemEvent.isShenronPorataKi = false;
                player.itemEvent.lastTimeShenronPorataKi = 0;
            }
        }
        if (player.itemEvent.isShenronPorataDame) {
            long timeRemaining = TIME_SHENRON_EVENT
                    - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPorataDame);
            if (timeRemaining > 0) {
                sendItemTime(player, 6579, (int) (timeRemaining / 1000));
            } else {
                // Thời gian đã hết, tắt flag
                player.itemEvent.isShenronPorataDame = false;
                player.itemEvent.lastTimeShenronPorataDame = 0;
            }
        }
        if (player.itemEvent.isShenronPlayerX3) {
            long timeRemaining = TIME_SHENRON_EVENT
                    - (System.currentTimeMillis() - player.itemEvent.lastTimeShenronPlayerX3);
            if (timeRemaining > 0) {
                sendItemTime(player, 6579, (int) (timeRemaining / 1000));
            } else {
                // Thời gian đã hết, tắt flag
                player.itemEvent.isShenronPlayerX3 = false;
                player.itemEvent.lastTimeShenronPlayerX3 = 0;
            }
        }

    }

    public void turnOnTDLT(Player player, Item item) {
        int min = 0;
        int minleft = 0;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                min = io.param;
                minleft = Math.max((min * 60 - 30000) / 60, 0);
                io.param = minleft > 0 ? minleft : 0;
                break;
            }
        }

        player.itemTime.isUseTDLT = true;

        int timeTDLTInMinutes = min - minleft;
        int timeTDLTInSeconds = timeTDLTInMinutes * 60;
        int maxTimeInSeconds = 30000;
        int timeTDLTInSecondsLimited = Math.min(timeTDLTInSeconds, maxTimeInSeconds);

        player.itemTime.timeTDLT = timeTDLTInSecondsLimited * 1000;

        player.itemTime.lastTimeUseTDLT = System.currentTimeMillis();
        sendCanAutoPlay(player);
        sendItemTime(player, 4387, timeTDLTInSecondsLimited);
        InventoryService.gI().sendItemBags(player);
    }

    public void turnOffTDLT(Player player, Item item) {
        player.itemTime.isUseTDLT = false;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                io.param += (short) ((player.itemTime.timeTDLT
                        - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT)) / 60 / 1000);
                break;
            }
        }
        sendCanAutoPlay(player);
        removeItemTime(player, 4387);
        InventoryService.gI().sendItemBags(player);
    }

    /**
     * Trả lại thời gian TDLT còn lại về option của item khi người chơi logout.
     * Tương đương turnOffTDLT nhưng không gửi packet (dùng khi mất kết nối).
     */
    public void returnTDLTTimeOnLogout(Player player) {
        if (player == null || player.itemTime == null || !player.itemTime.isUseTDLT) return;
        if (player.inventory == null || player.inventory.itemsBag == null) return;
        long remaining = (long) player.itemTime.timeTDLT
                - (System.currentTimeMillis() - player.itemTime.lastTimeUseTDLT);
        if (remaining > 0) {
            for (Item bagItem : player.inventory.itemsBag) {
                if (!bagItem.isNotNullItem() || bagItem.template.id != 521) continue;
                for (Item.ItemOption io : bagItem.itemOptions) {
                    if (io.optionTemplate.id == 1) {
                        io.param += (int) (remaining / 60 / 1000);
                        break;
                    }
                }
                break;
            }
        }
        player.itemTime.isUseTDLT = false;
        player.itemTime.timeTDLT = 0;
    }

    public void sendCanAutoPlay(Player player) {
        Message msg;
        try {
            msg = new Message(-116);
            msg.writer().writeByte(player.itemTime.isUseTDLT ? 1 : 0);
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(ItemTimeService.class, e);
        }
    }

    public void sendTextDoanhTrai(Player player) {
        if (player.clan != null && !player.clan.haveGoneDoanhTrai
                && player.clan.lastTimeOpenDoanhTrai != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenDoanhTrai) / 1000);
            int secondsLeft = (RedRibbonHQ.TIME_DOANH_TRAI / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, DOANH_TRAI, "Trại độc nhãn:", secondsLeft);
        }
    }

    public void sendTextTimePickDoanhTrai(Player player) {
        if (player.clan != null && player.clan.doanhTrai != null && player.clan.doanhTrai.isTimePicking) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.doanhTrai.lastTimePick) / 1000);
            int secondsLeft = (RedRibbonHQ.TIME_PICK_DOANH_TRAI / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, DOANH_TRAI, "Trại độc nhãn:", secondsLeft);
        }
    }

    public void sendTextBanDoKhoBau(Player player) {
        if (player.clan != null
                && player.clan.lastTimeOpenBanDoKhoBau != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenBanDoKhoBau) / 1000);
            int secondsLeft = (TreasureUnderSea.TIME_BAN_DO_KHO_BAU / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, BAN_DO_KHO_BAU, "Hang kho báu:", secondsLeft);
        }
    }

    public void sendTextXinbato(Player player) {
        sendTextTime(player, BAN_DO_KHO_BAU, "Tìm nước cho Xinbatô ở đảo Kame hoặc đảo Guru", 30);
    }

    public void sendTextConDuongRanDoc(Player player) {
        if (player.clan != null
                && player.clan.lastTimeOpenConDuongRanDoc != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenConDuongRanDoc) / 1000);
            int secondsLeft = (SnakeWay.TIME_CON_DUONG_RAN_DOC / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, CON_DUONG_RAN_DOC, "Con đường rắn độc:", secondsLeft);
        }
    }

    public void sendTextKhiGasHuyDiet(Player player) {
        if (player.clan != null
                && player.clan.lastTimeOpenKhiGasHuyDiet != 0) {
            int secondPassed = (int) ((System.currentTimeMillis() - player.clan.lastTimeOpenKhiGasHuyDiet) / 1000);
            int secondsLeft = (DestronGas.TIME_KHI_GAS_HUY_DIET / 1000) - secondPassed;
            if (secondsLeft < 0 || secondsLeft > 1800) {
                return;
            }
            sendTextTime(player, KHI_GAS_HUY_DIET, "Khí gas hủy diệt:", secondsLeft);
        }
    }

    public void removeTextDoanhTrai(Player player) {
        removeTextTime(player, DOANH_TRAI);
    }

    public void removeTextBanDoKhoBau(Player player) {
        removeTextTime(player, BAN_DO_KHO_BAU);
    }

    public void removeTextConDuongRanDoc(Player player) {
        removeTextTime(player, CON_DUONG_RAN_DOC);
    }

    public void removeTextKhiGasHuyDiet(Player player) {
        removeTextTime(player, KHI_GAS_HUY_DIET);
    }

    public void removeTextTime(Player player, byte id) {
        sendTextTime(player, id, null, 0);
    }

    public void sendTextTime(Player player, byte id, String text, int seconds) {
        Message msg;
        try {
            msg = new Message(65);
            msg.writer().writeByte(id);
            msg.writer().writeUTF(text == null ? "" : text);
            msg.writer().writeShort(seconds);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendItemTime(Player player, int itemId, int time) {
        Message msg;
        try {
            msg = new Message(-106);
            msg.writer().writeShort(itemId);
            msg.writer().writeShort(time);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void removeItemTime(Player player, int itemTime) {
        sendItemTime(player, itemTime, 0);
    }

    public void sendTextTimeKeoBuaBao(Player player, int time) {
        sendTextTime(player, KEO_BUA_BAO, "Kéo Búa Bao:", time);
    }

    public void sendTextClanBoss(Player player, int round, int seconds) {
        sendTextTime(player, BOSS_CHALLENGE, "Hạ Boss Bang Hội (lần thứ " + round + ")", seconds);
    }

    public void removeTextClanBoss(Player player) {
        removeTextTime(player, BOSS_CHALLENGE);
    }
}
