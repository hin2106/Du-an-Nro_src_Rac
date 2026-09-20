package npc.list;

import boss.BossID;
import consts.ConstNpc;
import services.dungeon.TrainingService;
import npc.Npc;
import player.Player;
import services.map.NpcService;
import services.OpenPowerService;
import services.Service;
import services.player.InventoryService;
import item.Item;
import utils.Util;

public class ToSuKaio extends Npc {

    public ToSuKaio(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Tập luyện với Tổ sư Kaio sẽ tăng "
                            + Util.formatNumber(TrainingService.gI().getTnsmMoiPhut(player))
                            + " sức mạnh mỗi phút, có thể tăng giảm tùy vào khả năng đánh quái của con",
                    player.dangKyTapTuDong ? "Hủy đăng\nký tập\ntự động" : "Đăng ký\ntập\ntự động",
                    "Đồng ý\nluyện tập",
                    "Không\nđồng ý",
                    "Nâng\nGiới hạn\nSức mạnh");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player))
            return;

        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 -> {
                    if (player.dangKyTapTuDong) {
                        player.dangKyTapTuDong = false;
                        NpcService.gI().createTutorial(player, tempId, avartar,
                                "Con đã hủy thành công đăng ký tập tự động\n"
                                        + "Từ giờ con muốn tập Offline hãy tự đến đây trước");
                        return;
                    }
                    this.createOtherMenu(player, 2001,
                            "Đăng ký để mỗi khi Offline quá 30 phút, con sẽ được tự động luyện tập với tốc độ "
                                    + TrainingService.gI().getTnsmMoiPhut(player) + " sức mạnh mỗi phút",
                            "Hướng\ndẫn\nthêm", "Đồng ý\n1 ngọc\nmỗi lần", "Không\nđồng ý");
                }
                case 1 -> TrainingService.gI().callBoss(player, BossID.TO_SU_KAIO, false);
                case 3 -> this.createOtherMenu(player, 2002,
                        "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                        "Bản thân", "Đệ tử", "Từ chối");
            }
        } else if (player.idMark.getIndexMenu() == 2001) {
            switch (select) {
                case 0 -> NpcService.gI().createTutorial(player, tempId, avartar, ConstNpc.TAP_TU_DONG);
                case 1 -> {
                    player.mapIdDangTapTuDong = mapId;
                    player.dangKyTapTuDong = true;
                    NpcService.gI().createTutorial(player, tempId, avartar,
                            "Từ giờ, quá 30 phút Offline con sẽ được tự động luyện tập");
                }
            }
        } else if (player.idMark.getIndexMenu() == 2002) {
            switch (select) {
                case 0 -> {
                    if (player.nPoint.limitPower < 4) {
                        this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Con cần đến Quốc Vương để nâng giới hạn sức mạnh từ mốc 0 đến mốc 4!",
                                "Đóng");
                        return;
                    }

                    boolean canOpen = true;
                    String msg = null;
                    if (player.nPoint.limitPower == 4 || player.nPoint.limitPower == 5) {
                        if (!InventoryService.gI().fullSetThan(player)) {
                            canOpen = false;
                            msg = "Con cần mặc đủ full set thần linh (5 món) để mở giới hạn sức mạnh!";
                        }
                    } else if (player.nPoint.limitPower == 6 || player.nPoint.limitPower == 7) {
                        if (!InventoryService.gI().fullSetThan(player)) {
                            canOpen = false;
                            msg = "Con cần mặc đủ full set thần linh (5 món) để mở giới hạn sức mạnh!";
                        }
                    }
                    // Mốc 8-9: cần full set thần linh
                    else if (player.nPoint.limitPower == 8 || player.nPoint.limitPower == 9) {
                        if (!InventoryService.gI().fullSetThan(player)) {
                            canOpen = false;
                            msg = "Con cần mặc đủ full set thần linh (5 món) để mở giới hạn sức mạnh!";
                        }
                    }
                    // Mốc 10: cần đồ hủy diệt
                    else if (player.nPoint.limitPower == 10) {
                        Item dothan = player.inventory.itemsBody.stream()
                                .filter(it -> it != null && it.template != null && it.template.level == 14)
                                .findFirst().orElse(null);
                        if (dothan == null) {
                            canOpen = false;
                            msg = "Con cần mặc ít nhất 1 món đồ hủy diệt để mở giới hạn sức mạnh!";
                        }
                    }
                    // Mốc 11-12: cần đồ thiên sứ
                    else if (player.nPoint.limitPower == 11 || player.nPoint.limitPower == 12) {
                        Item dothan = player.inventory.itemsBody.stream()
                                .filter(it -> it != null && it.template != null && it.template.level == 15)
                                .findFirst().orElse(null);
                        if (dothan == null) {
                            canOpen = false;
                            msg = "Con cần mặc ít nhất 1 món đồ thiên sứ để mở giới hạn sức mạnh!";
                        }
                    }

                    if (!canOpen) {
                        this.createOtherMenu(player, ConstNpc.IGNORE_MENU, msg, "Đóng");
                        return;
                    }

                    if (player.nPoint.limitPower < 14) {
                        this.createOtherMenu(player, ConstNpc.OPEN_POWER_MYSEFT,
                                "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của bản thân lên "
                                        + Util.numberToMoney(player.nPoint.getPowerNextLimit()),
                                "Nâng\ngiới hạn\nsức mạnh",
                                "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER)
                                        + " vàng",
                                "Đóng");
                    } else {
                        this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Sức mạnh của con đã đạt tới giới hạn tối đa!", "Đóng");
                    }
                }
                case 1 -> {
                    if (player.pet != null) {
                        // Chỉ xử lý từ mốc 4 trở đi
                        if (player.pet.nPoint.limitPower < 4) {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Đệ tử cần đến Quốc Vương để nâng giới hạn sức mạnh từ mốc 0 đến mốc 4!",
                                    "Đóng");
                            return;
                        }

                        if (player.pet.nPoint.limitPower >= 4 && player.pet.nPoint.limitPower < 13) {
                            this.createOtherMenu(player, ConstNpc.OPEN_POWER_PET,
                                    "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của đệ tử lên "
                                            + Util.numberToMoney(player.pet.nPoint.getPowerNextLimit()),
                                    "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER)
                                            + " ngọc",
                                    "Đóng");
                        } else {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Sức mạnh của đệ con đã đạt tới giới hạn", "Đóng");
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                case 2 -> this.openBaseMenu(player);
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.OPEN_POWER_MYSEFT) {
            switch (select) {
                case 0 -> OpenPowerService.gI().openPowerBasic(player);
                case 1 -> {
                    if (player.inventory.gem >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                        if (OpenPowerService.gI().openPowerSpeed(player)) {
                            player.inventory.gem -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                            Service.gI().sendMoney(player);
                        }
                    } else {
                        Service.gI().sendThongBao(player,
                                "Bạn không đủ ngọc để mở, còn thiếu "
                                        + Util.numberToMoney(
                                                OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gem)
                                        + " ngọc");
                    }
                }
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.OPEN_POWER_PET) {
            if (select == 0) {
                if (player.inventory.gem >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                    if (OpenPowerService.gI().openPowerSpeed(player.pet)) {
                        player.inventory.gem -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                        Service.gI().sendMoney(player);
                    }
                } else {
                    Service.gI().sendThongBao(player,
                            "Bạn không đủ ngọc để mở, còn thiếu "
                                    + Util.numberToMoney(
                                            OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gem)
                                    + " ngọc");
                }
            }
        }
    }
}
