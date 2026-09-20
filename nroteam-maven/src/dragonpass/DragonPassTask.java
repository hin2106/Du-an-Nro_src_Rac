package dragonpass;

import java.util.List;

public record DragonPassTask(int id, byte type, String name, String description,
        int target, int exp, int prerequisiteId) {
    public static final byte DAILY = 0;
    public static final byte SEASON = 1;

    public static final int LOGIN = 1;
    public static final int KILL_ANY_MOB = 2;
    public static final int VISIT_EARTH = 3;
    public static final int VISIT_NAMEC = 4;
    public static final int VISIT_XAYDA = 5;
    public static final int JOIN_SUPER_RANK = 6;
    public static final int UPGRADE_EQUIPMENT = 7;
    public static final int CRYSTALIZE_EQUIPMENT = 8;
    public static final int SPEND_GEM = 9;
    public static final int COMBINE_DRAGON_BALL = 10;
    public static final int KILL_WOODEN_DUMMY = 11;

    public static final int KILL_BOSS_1 = 101;
    public static final int KILL_BOSS_2 = 102;
    public static final int KILL_BOSS_3 = 103;
    public static final int COMPLETE_TREASURE = 104;
    public static final int COMPLETE_SNAKE_WAY = 105;
    public static final int COMPLETE_CAMP = 106;
    public static final int WIN_TOURNAMENT_ROUND_2 = 107;

    public static final List<DragonPassTask> ALL = List.of(
            new DragonPassTask(LOGIN, DAILY, "Đăng nhập", "Đăng nhập vào game 1 lần.", 1, 50, 0),
            new DragonPassTask(KILL_ANY_MOB, DAILY, "Tiêu diệt quái bất kỳ", "Tiêu diệt đủ 100 quái bất kỳ.", 100, 100, 0),
            new DragonPassTask(VISIT_EARTH, DAILY, "Dạo chơi Trái Đất", "Đi qua toàn bộ map thuộc hành tinh Trái Đất.", 14, 50, 0),
            new DragonPassTask(VISIT_NAMEC, DAILY, "Dạo chơi Namec", "Đi qua toàn bộ map thuộc hành tinh Namec.", 13, 50, 0),
            new DragonPassTask(VISIT_XAYDA, DAILY, "Dạo chơi Xayda", "Đi qua toàn bộ map thuộc hành tinh Xayda.", 14, 50, 0),
            new DragonPassTask(JOIN_SUPER_RANK, DAILY, "Tham gia đấu giải siêu hạng", "Tham gia 1 lần đấu giải siêu hạng.", 1, 50, 0),
            new DragonPassTask(UPGRADE_EQUIPMENT, DAILY, "Nâng cấp trang bị thành công", "Nâng cấp thành công 1 trang bị bất kỳ.", 1, 50, 0),
            new DragonPassTask(CRYSTALIZE_EQUIPMENT, DAILY, "Pha lê hóa trang bị thành công", "Pha lê hóa thành công 1 trang bị bất kỳ.", 1, 50, 0),
            new DragonPassTask(SPEND_GEM, DAILY, "Tiêu Ngọc", "Thực hiện 1 giao dịch tiêu Ngọc bất kỳ.", 1, 50, 0),
            new DragonPassTask(COMBINE_DRAGON_BALL, DAILY, "Nhập Ngọc Rồng thành công", "Hoàn thành nhập Ngọc Rồng 1 lần bất kỳ.", 1, 50, 0),
            new DragonPassTask(KILL_WOODEN_DUMMY, DAILY, "Tiêu diệt 10 mộc nhân", "Tiêu diệt đủ 10 mộc nhân.", 10, 50, 0),
            new DragonPassTask(KILL_BOSS_1, SEASON, "Tiêu diệt boss - Mốc 1", "Chỉ người kết liễu boss được tính.", 1, 100, 0),
            new DragonPassTask(KILL_BOSS_2, SEASON, "Tiêu diệt boss - Mốc 2", "Mở sau khi nhận EXP mốc 1; chỉ người kết liễu được tính.", 2, 200, KILL_BOSS_1),
            new DragonPassTask(KILL_BOSS_3, SEASON, "Tiêu diệt boss - Mốc 3", "Mở sau khi nhận EXP mốc 2; chỉ người kết liễu được tính.", 3, 300, KILL_BOSS_2),
            new DragonPassTask(COMPLETE_TREASURE, SEASON, "Hoàn thành Động kho báu", "Cùng bang hội hạ Trung úy Xanh Lơ và được tàu đưa về.", 1, 300, 0),
            new DragonPassTask(COMPLETE_SNAKE_WAY, SEASON, "Hoàn thành Con đường rắn độc", "Cùng bang hội hạ boss Cadic và được tàu đưa về.", 1, 300, 0),
            new DragonPassTask(COMPLETE_CAMP, SEASON, "Hoàn thành Doanh trại", "Tương tác với NPC Độc Nhãn để hoàn thành.", 1, 300, 0),
            new DragonPassTask(WIN_TOURNAMENT_ROUND_2, SEASON, "Thắng vòng 2 Đại hội võ thuật", "Thắng trận 1, sau đó thắng tiếp trận 2 ở giải bất kỳ.", 1, 300, 0));

    public static DragonPassTask find(int id) {
        for (DragonPassTask task : ALL) {
            if (task.id == id) return task;
        }
        return null;
    }
}
