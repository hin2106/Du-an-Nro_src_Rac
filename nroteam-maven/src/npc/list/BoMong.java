package npc.list;
import consts.ConstNpc;
import consts.ConstTask;
import npc.Npc;
import player.Player;
import services.AchievementService;
import services.TaskService;
import services.map.NpcService;

public class BoMong extends Npc {

    public BoMong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
            return;
        }
        if (canOpenNpc(player)) {
            if (this.mapId == 47 || this.mapId == 84) {
                String dialog = "Muốn có thêm thỏi vàng, có nhiều cách, nạp thẻ cào là nhanh nhất, "
                        + "còn không thì chịu khó làm vài nhiệm vụ sẽ được thưởng thỏi vàng.";

                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        dialog, "Nạp Ngọc", "Nhận ngọc\nMiễn phí", "Nhiệm vụ\nhàng ngày");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (this.mapId != 47 && this.mapId != 84) {
            return;
        }

        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 ->
                    this.createOtherMenu(player, ConstNpc.MENU_NAP_TIEN,
                            "Ngươi có muốn có thêm ngọc thì chịu khó làm nhiệm vụ sẽ nhận được ngọc thưởng",
                            "Hướng\n dẫn\n nạp thẻ");
                case 1 ->
                    AchievementService.gI().openAchievementUI(player);
                case 2 -> {
                    if (player.playerTask.sideTask.template != null) {
                        String npcSay = "Bạn đang làm nhiệm vụ " + player.playerTask.sideTask.getName() + " ("
                                +
                                "\nHiện tại đã hoàn thành: " + player.playerTask.sideTask.count + "/"
                                + player.playerTask.sideTask.maxCount + " ("
                                + player.playerTask.sideTask.getPercentProcess() + "%)\nSố nhiệm vụ còn lại trong ngày: "
                                + player.playerTask.sideTask.leftTask + "/" + ConstTask.MAX_SIDE_TASK;
                        if (player.playerTask.sideTask.isDone()) {
                            // Đã hoàn thành: thêm nút Nhận thưởng riêng, Chi tiết chỉ xem thông tin
                            this.createOtherMenu(player, ConstNpc.MENU_OPTION_PAY_SIDE_TASK,
                                    npcSay, "Nhận thưởng", "Chi tiết\nNhiệm vụ", "Hủy\n nhiệm vụ");
                        } else {
                            // Chưa hoàn thành: chỉ có Chi tiết & Hủy
                            this.createOtherMenu(player, ConstNpc.MENU_OPTION_PAY_SIDE_TASK,
                                    npcSay, "Chi tiết\nNhiệm vụ", "Hủy\n nhiệm vụ");
                        }
                    } else {
                        this.createOtherMenu(player, ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK,
                                "Bạn còn " + player.playerTask.sideTask.leftTask + " nhiệm vụ chưa nhận\n Phần thưởng sẽ tương xứng tùy vào từng nhiệm vụ\nBạn muốn chọn nhiệm vụ khó hay dễ ?",
                                "Siêu khó", "Khó", "Dễ", "Từ chối");
                    }
                }
                default -> {
                }
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_NAP_TIEN) {
            if (select == 0) {
                NpcService.gI().createTutorial(player, tempId, this.avartar, ConstNpc.HUONG_DAN_NAP_NGOC);
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK) {
            switch (select) {
                case 0, 1, 2 ->
                    TaskService.gI().changeSideTask(player, (byte) select);
                case 3 ->
                    openBaseMenu(player);
            }
        } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_PAY_SIDE_TASK) {
            boolean isDone = player.playerTask.sideTask != null && player.playerTask.sideTask.isDone();
            if (isDone) {
                // 3 buttons when done: [0]=Nhận thưởng, [1]=Chi tiết, [2]=Hủy
                switch (select) {
                    case 0 -> TaskService.gI().paySideTask(player);
                    case 1 -> {
                        String detail = "Nhiệm vụ: " + player.playerTask.sideTask.getName()
                                + "\nTiến độ: " + player.playerTask.sideTask.count + "/" + player.playerTask.sideTask.maxCount
                                + " (" + player.playerTask.sideTask.getPercentProcess() + "%)"
                                + "\nCòn lại hôm nay: " + player.playerTask.sideTask.leftTask + "/" + ConstTask.MAX_SIDE_TASK;
                        NpcService.gI().createTutorial(player, tempId, this.avartar, detail);
                    }
                    case 2 -> TaskService.gI().removeSideTask(player);
                }
            } else {
                // 2 buttons when not done: [0]=Chi tiết, [1]=Hủy
                switch (select) {
                    case 0 -> {
                        String detail = "Nhiệm vụ: " + player.playerTask.sideTask.getName()
                                + "\nTiến độ: " + player.playerTask.sideTask.count + "/" + player.playerTask.sideTask.maxCount
                                + " (" + player.playerTask.sideTask.getPercentProcess() + "%)"
                                + "\nCòn lại hôm nay: " + player.playerTask.sideTask.leftTask + "/" + ConstTask.MAX_SIDE_TASK;
                        NpcService.gI().createTutorial(player, tempId, this.avartar, detail);
                    }
                    case 1 -> TaskService.gI().removeSideTask(player);
                }
            }
        }
    }
}
