package npc.list;
import consts.ConstNpc;
import item.Item;
import npc.Npc;
import player.Player;
import services.OpenPowerService;
import services.Service;
import utils.Util;

public class QuocVuong extends Npc {

    public QuocVuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                "Bản thân", "Đệ tử", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> {
                        if (player.nPoint.limitPower < 4) {
                            if (player.nPoint.limitPower == 3) {
                                Item dothan = player.inventory.itemsBody.stream()
                                        .filter(it -> it != null && it.template != null && it.template.level == 13)
                                        .findFirst()
                                        .orElse(null);

                                if (dothan == null) {
                                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                            "Con cần mặc ít nhất 1 món đồ thần linh để mở giới hạn sức mạnh!",
                                            "Đóng");
                                    return;
                                }
                            }
                            this.createOtherMenu(player, ConstNpc.OPEN_POWER_MYSEFT,
                                    "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của bản thân lên "
                                            + Util.numberToMoney(player.nPoint.getPowerNextLimit()),
                                    "Nâng\ngiới hạn\nsức mạnh",
                                    "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER)
                                            + " vàng",
                                    "Đóng");
                        } else if (player.nPoint.limitPower >= 4) {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Con đã đạt mốc 4, hãy đến Tổ sư Kaio để tiếp tục nâng giới hạn sức mạnh!",
                                    "Đóng");
                        }
                    }

                    case 1 -> {
                        if (player.pet != null) {
                            if (player.pet.nPoint.limitPower < 4) {
                                this.createOtherMenu(player, ConstNpc.OPEN_POWER_PET,
                                        "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của đệ tử lên "
                                                + Util.numberToMoney(player.pet.nPoint.getPowerNextLimit()),
                                        "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER)
                                                + " ngọc",
                                        "Đóng");
                            } else {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Đệ tử đã đạt mốc 4, hãy đến Tổ sư Kaio để tiếp tục nâng giới hạn sức mạnh!",
                                        "Đóng");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                        }

                    }
                }
            } else if (player.idMark.getIndexMenu() == ConstNpc.OPEN_POWER_MYSEFT) {
                switch (select) {
                    case 0 ->
                        OpenPowerService.gI().openPowerBasic(player);
                    case 1 -> {
                        if (player.inventory.gem >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                            if (OpenPowerService.gI().openPowerSpeed(player)) {
                                player.inventory.gem -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                                Service.gI().sendMoney(player);
                            }
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Bạn không đủ ngọc để mở, còn thiếu "
                                            + Util.numberToMoney((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER
                                                    - player.inventory.gem))
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
                                                (OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gem))
                                        + " ngọc");
                    }
                }
            }
        }
    }
}
