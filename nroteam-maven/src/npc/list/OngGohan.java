package npc.list;

import consts.ConstNpc;
import consts.ConstTask;
import npc.Npc;
import player.Player;
import services.Service;
import services.TaskService;

public class OngGohan extends Npc {

    private boolean daNhanNgocXanh = false;

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        if (canOpenNpc(player)) {
            if (player.getSession() != null && player.getSession().uu != null 
                    && player.getSession().uu.startsWith("trial_")) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Bạn đang dùng tài khoản dùng thử.\nHãy đăng ký tài khoản để lưu tiến trình chơi vĩnh viễn!",
                        "Đăng ký tài khoản", "Hỗ trợ nhiệm vụ");
            } else {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Ta có thể hỗ trợ một số nhiệm vụ cho con.",
                        "Hỗ trợ nhiệm vụ");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.idMark.isBaseMenu()) {
            // Kiểm tra nếu là tài khoản trial
            boolean isTrialAccount = player.getSession() != null 
                    && player.getSession().uu != null 
                    && player.getSession().uu.startsWith("trial_");
            
            if (isTrialAccount) {
                // Menu cho trial account: "Đăng ký tài khoản", "Hỗ trợ nhiệm vụ"
                switch (select) {
                    case 0 -> {
                        // Hiển thị xác nhận đăng ký
                        this.createOtherMenu(player, ConstNpc.MENU_TRIAL_REGISTER,
                                "Bạn đang dùng tài khoản dùng thử.\n"
                                + "Đăng ký tài khoản thật để:\n"
                                + "- Lưu tiến trình vĩnh viễn\n"
                                + "- Đăng nhập từ bất kỳ đâu\n"
                                + "- Bảo mật tài khoản\n"
                                + "Bạn có muốn đăng ký không?",
                                "Đồng ý", "Từ chối");
                    }
                    case 1 -> {
                        int taskId = TaskService.gI().getIdTask(player);
                        boolean supported = false;

                        if (taskId >= ConstTask.TASK_9_0 && taskId < ConstTask.TASK_11_0) {
                            player.playerTask.taskMain.id = 10;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        } else if (taskId >= ConstTask.TASK_18_0 && taskId < ConstTask.TASK_20_0) {
                            player.playerTask.taskMain.id = 19;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        } else if (taskId == ConstTask.TASK_31_5) {
                            player.playerTask.taskMain.id = 32;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        } else if (taskId >= ConstTask.TASK_12_0 && taskId < ConstTask.TASK_18_0) {
                            player.playerTask.taskMain.id = 19;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        }
                        if (supported) {
                            TaskService.gI().sendNextTaskMain(player);
                            Service.gI().sendThongBao(player, "Bạn đã được hỗ trợ nhiệm vụ thành công.");
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Chỉ hỗ trợ nhiệm vụ Tàu Pảy Pảy, DHVT, Trung úy trắng và nhiệm vụ tìm Berry.");
                        }
                    }

                    default -> {
                    }
                }
            } else {
                // Menu cho tài khoản thường: "Hỗ trợ nhiệm vụ"
                switch (select) {
                    case 0 -> {
                        int taskId = TaskService.gI().getIdTask(player);
                        boolean supported = false;

                        if (taskId >= ConstTask.TASK_9_0 && taskId < ConstTask.TASK_11_0) {
                            player.playerTask.taskMain.id = 10;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        } else if (taskId >= ConstTask.TASK_18_0 && taskId < ConstTask.TASK_20_0) {
                            player.playerTask.taskMain.id = 19;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        } else if (taskId == ConstTask.TASK_31_5) {
                            player.playerTask.taskMain.id = 32;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        } else if (taskId >= ConstTask.TASK_12_0 && taskId < ConstTask.TASK_18_0) {
                            player.playerTask.taskMain.id = 19;
                            player.playerTask.taskMain.index = 0;
                            supported = true;
                        }
                        if (supported) {
                            TaskService.gI().sendNextTaskMain(player);
                            Service.gI().sendThongBao(player, "Bạn đã được hỗ trợ nhiệm vụ thành công.");
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Chỉ hỗ trợ nhiệm vụ Tàu Pảy Pảy, DHVT, Trung úy trắng và nhiệm vụ tìm Berry.");
                        }
                    }

                    default -> {
                    }
                }
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_TRIAL_REGISTER) {
            // Xử lý menu xác nhận đăng ký
            switch (select) {
                case 0 -> {
                    // Người chơi đồng ý đăng ký -> Mở màn hình đăng ký
                    Service.gI().switchToRegisterScr(player.getSession());
                }
                case 1 -> {
                    // Từ chối -> Không làm gì
                    Service.gI().sendThongBao(player, "Bạn có thể đăng ký tài khoản bất cứ lúc nào!");
                }
            }
        }
    }
}
