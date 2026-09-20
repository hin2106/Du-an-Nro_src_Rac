package npc.list;
import consts.ConstNpc;
import consts.ConstTask;
import npc.Npc;
import player.Player;
import services.map.NpcService;
import services.Service;
import services.TaskService;
import services.map.ChangeMapService;
import utils.Logger;

public class Calick extends Npc {

    public Calick(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (player == null || player.playerTask == null || player.playerTask.taskMain == null) {
            Service.gI().hideWaitDialog(player);
            return;
        }

        if (TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
            Service.gI().hideWaitDialog(player);
            Service.gI().sendThongBao(player, "Không thể thực hiện");
            return;
        }

        if (player.zone == null || player.zone.map == null) {
            Service.gI().hideWaitDialog(player);
            return;
        }

        int currentTaskId = TaskService.gI().getIdTask(player);
        int currentMapId = player.zone.map.mapId;

        if (currentTaskId == ConstTask.TASK_24_1) {
            if (currentMapId == 102) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào chú, thực ra cháu không phải là người của thời đại này mà là người của...\nTương lai 20 năm sắp tới\n Tên cháu là Ca lích! người Xayda\nCháu đến đây bằng 'Cổ máy thời gian'\nBố mẹ cháu vốn là bạn thân của chú\n Họ chính là Ca Đíc và Bunma!\nĐây là thuốc trợ tim dành cho chú Sôngôku\n nhờ chú đưa cho Quy Lão giùm cháu nhé, cám ơn chú",
                        "Nghe tiếp");
            } else {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào chú, thực ra cháu không phải là người của thời đại này mà là người của...\nTương lai 20 năm sắp tới\n Tên cháu là Ca lích! người Xayda\nCháu đến đây bằng 'Cổ máy thời gian'\nBố mẹ cháu vốn là bạn thân của chú\n Họ chính là Ca Đíc và Bunma!\nĐây là thuốc trợ tim dành cho chú Sôngôku\n nhờ chú đưa cho Quy Lão giùm cháu nhé, cám ơn chú",
                        "Nghe tiếp");
            }
            return;
        }

        if (currentMapId == 102) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chào chú, cháu có thể giúp gì?",
                    "Kể\nChuyện", "Quay về\nQuá khứ");
        } else {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Chào chú, cháu có thể giúp gì?",
                        "Kể\nChuyện", "Đi đến\nTương lai", "Từ chối");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (player == null || player.idMark == null || player.zone == null || player.zone.map == null) {
            Service.gI().hideWaitDialog(player);
            return;
        }

        try {
            int currentMapId = player.zone.map.mapId;
            int currentTaskId = TaskService.gI().getIdTask(player);
            boolean isBaseMenu = player.idMark.isBaseMenu();
            if (currentTaskId == ConstTask.TASK_24_1) {
                if (select == 0) {
                    TaskService.gI().doneTask(player, ConstTask.TASK_24_1);
                    NpcService.gI().createTutorial(player, this.avartar, ConstNpc.CALICK_KE_CHUYEN);
                } else {
                    Service.gI().sendThongBao(player, "Hãy nói chuyện với Calick");
                    if (currentMapId == 102) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chào chú, thực ra cháu không phải là người của thời đại này mà là người của...\nTương lai 20 năm sắp tới\n Tên cháu là Ca lích! người Xayda\nCháu đến đây bằng 'Cổ máy thời gian'\nBố mẹ cháu vốn là bạn thân của chú\n Họ chính là Ca Đíc và Bunma!\nĐây là thuốc trợ tim dành cho chú Sôngôku\n nhờ chú đưa cho Quy Lão giùm cháu nhé, cám ơn chú",
                                "Nghe tiếp");
                    } else {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                                "Chào chú, thực ra cháu không phải là người của thời đại này mà là người của...\nTương lai 20 năm sắp tới\n Tên cháu là Ca lích! người Xayda\nCháu đến đây bằng 'Cổ máy thời gian'\nBố mẹ cháu vốn là bạn thân của chú\n Họ chính là Ca Đíc và Bunma!\nĐây là thuốc trợ tim dành cho chú Sôngôku\n nhờ chú đưa cho Quy Lão giùm cháu nhé, cám ơn chú",
                                "Nghe tiếp");
                    }
                }
                return;
            }
            if (currentMapId == 102) {
                if (isBaseMenu) {
                    if (select == 0) {
                        NpcService.gI().createTutorial(player, this.avartar, ConstNpc.CALICK_KE_CHUYEN);
                    } else if (select == 1) {
                        ChangeMapService.gI().goToQuaKhu(player);
                    } else {
                        Service.gI().hideWaitDialog(player);
                    }
                } else {
                    Service.gI().hideWaitDialog(player);
                }
            } else {
                if (isBaseMenu || select >= 0) {
                    switch (select) {
                        case 0:
                            NpcService.gI().createTutorial(player, this.avartar, ConstNpc.CALICK_KE_CHUYEN);
                            break;
                        case 1:
                            if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_23_0) {
                                ChangeMapService.gI().goToTuongLai(player);
                            } else {
                                Service.gI().sendThongBao(player, "Bạn chưa đủ điều kiện để thực hiện");
                                Service.gI().hideWaitDialog(player);
                            }
                            break;
                        case 2:
                            Service.gI().hideWaitDialog(player);
                            break;
                        default:
                            Service.gI().hideWaitDialog(player);
                            break;
                    }
                } else {
                    Service.gI().hideWaitDialog(player);
                }
            }
        } catch (Exception e) {
            Logger.logException(Calick.class, e, "Lỗi trong confirmMenu của Calick");
            Service.gI().hideWaitDialog(player);
            Service.gI().sendThongBao(player, "Có lỗi xảy ra, vui lòng thử lại");
        }
    }
}
