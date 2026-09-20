package npc.list;

import clan.Clan;
import consts.ConstNpc;
import consts.ConstPlayer;
import java.io.IOException;
import java.util.ArrayList;
import npc.Npc;
import player.Player;
import services.ItemService;
import services.map.NpcService;
import services.RewardService;
import services.Service;
import services.ShopService;
import services.TaskService;
import services.map.ChangeMapService;
import services.func.Input;
import services.player.PlayerService;
import skill.Skill;
import utils.Logger;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;

public class VuaVegeta extends Npc {

    public VuaVegeta(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            // Xử lý học kỹ năng xong (nếu có)
            handleBaseMenuAfterTalk(player, 0);

            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                if (player.gender != ConstPlayer.XAYDA) {
                    NpcService.gI().createTutorial(player, tempId, avartar,
                            "Con hãy về hành tinh của mình mà thể hiện");
                    return;
                }
                ArrayList<String> menu = new ArrayList<>();
                if (!player.canReward) {
                    menu.add("Nhiệm vụ");
                    menu.add("Học\nKỹ năng");
                    Clan clan = player.clan;
                    if (clan != null) {
                        menu.add("Về khu\nvực bang");
                        if (clan.isLeader(player)) {
                            menu.add("Giải tán\nBang hội");
                        }
                    }
                } else {
                    menu.add("Giao\nLân con");
                }
                String[] menus = menu.toArray(String[]::new);
                createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Chào con, ta rất vui khi gặp được con\nCon muốn làm gì nào ?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward) {
                RewardService.gI().rewardLancon(player);
                return;
            }
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0 -> NpcService.gI().createTutorial(player, tempId, avartar,
                            player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                    case 1 -> {
                        if (player.gender == ConstPlayer.XAYDA) {
                            handleMainOptions(player, 1);
                        } else {
                            Service.gI().sendThongBao(player, "Chỉ Xayda mới có thể học kỹ năng ở đây!");
                        }
                    }
                    case 2 -> {
                        Clan clan = player.clan;
                        if (clan != null) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
                        }
                    }
                    case 3 -> {
                        Clan clan = player.clan;
                        if (clan != null && clan.isLeader(player)) {
                            createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý",
                                    "Từ chối");
                        }
                    }
                }
            } else if (player.idMark.getIndexMenu() == 3) {
                Clan clan = player.clan;
                if (clan != null && clan.isLeader(player)) {
                    if (select == 0) {
                        Input.gI().createFormGiaiTanBangHoi(player);
                    }
                }
            } else if (player.idMark.getIndexMenu() == 13 && select == 0) {
                // Hủy học kỹ năng
                player.LearnSkill.Time = -1;
                player.LearnSkill.ItemTemplateSkillId = -1;
                Service.gI().sendThongBao(player, "Đã huỷ học kỹ năng.");
            } else if (player.idMark.getIndexMenu() == 12) {
                handleLearningSkillQuick(player, select);
            }
        }
    }

    private void handleBaseMenuAfterTalk(Player player, int selectFromBaseMenu) {
        if (selectFromBaseMenu == 0) {
            if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
                try {
                    int skillId = player.LearnSkill.ItemTemplateSkillId;
                    int skillTemplateId = SkillUtil.getTempSkillSkillByItemID(skillId);
                    if (!SkillUtil.validateSkillBeforeLearn(player, skillTemplateId)) {
                        Service.gI().sendThongBao(player, "Kỹ năng này không phù hợp với hành tinh của bạn!");
                        Logger.warning("Player " + player.name + " (gender: " + player.gender +
                                ") cố gắng học skill không hợp lệ: " + skillTemplateId);
                        player.LearnSkill.Time = -1;
                        player.LearnSkill.ItemTemplateSkillId = -1;
                        return;
                    }
                    Skill skill = SkillUtil.getSkillByItemID(player, skillId);
                    Skill newSkill = SkillUtil.createSkill(skillTemplateId, skill.point);
                    player.BoughtSkill.add(skillId);
                    SkillUtil.setSkill(player, newSkill);
                    var msg = Service.gI().messageSubCommand((byte) 62);
                    msg.writer().writeShort(newSkill.skillId);
                    player.sendMessage(msg);
                    msg.cleanup();
                    PlayerService.gI().sendInfoHpMpMoney(player);
                    player.LearnSkill.Time = -1;
                    player.LearnSkill.ItemTemplateSkillId = -1;
                    npcChat(player, "Chúc mừng con đã học xong kỹ năng!");
                } catch (IOException e) {
                    Logger.log("Lỗi khi cấp kỹ năng cho người chơi: " + e.getMessage());
                }
            }
        }
    }

    private void handleLearningSkillQuick(Player player, int select) {
        if (select == 1) {
            createOtherMenu(player, 13, "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok",
                    "Đóng");
        } else if (select == 0) {
            long timeLeft = player.LearnSkill.Time - System.currentTimeMillis();
            int gemCost = 5 + (int) (timeLeft / 600_000L);
            if (player.inventory.gem < gemCost) {
                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                return;
            }
            player.inventory.subGem(gemCost);
            PlayerService.gI().sendInfoHpMpMoney(player);
            // Kết thúc thời gian học ngay lập tức
            player.LearnSkill.Time = System.currentTimeMillis();
            if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
                try {
                    int skillId = player.LearnSkill.ItemTemplateSkillId;
                    int skillTemplateId = SkillUtil.getTempSkillSkillByItemID(skillId);
                    // Validate skill có thuộc gender của player không
                    if (!SkillUtil.validateSkillBeforeLearn(player, skillTemplateId)) {
                        Service.gI().sendThongBao(player, "Kỹ năng này không phù hợp với hành tinh của bạn!");
                        Logger.warning("Player " + player.name + " (gender: " + player.gender +
                                ") cố gắng học skill không hợp lệ: " + skillTemplateId);
                        player.LearnSkill.Time = -1;
                        player.LearnSkill.ItemTemplateSkillId = -1;
                        return;
                    }
                    Skill currentLearnedSkillLevel = SkillUtil.getSkillByItemID(player, skillId);
                    Skill newSkill = SkillUtil.createSkill(skillTemplateId, currentLearnedSkillLevel.point);
                    player.BoughtSkill.add(skillId);
                    SkillUtil.setSkill(player, newSkill);
                    var msg = Service.gI().messageSubCommand((byte) 62);
                    msg.writer().writeShort(newSkill.skillId);
                    player.sendMessage(msg);
                    msg.cleanup();
                    player.LearnSkill.Time = -1;
                    player.LearnSkill.ItemTemplateSkillId = -1;
                    npcChat(player, "Chúc mừng con đã học xong kỹ năng cấp tốc!");
                } catch (IOException e) {
                    Logger.log("Lỗi khi cấp kỹ năng cấp tốc cho người chơi: " + e.getMessage());
                }
            }
        }
    }

    private void handleMainOptions(Player player, int select) {
        switch (select) {
            case 0:
                if (player.playerTask != null && player.playerTask.taskMain != null &&
                        player.playerTask.taskMain.subTasks != null &&
                        player.playerTask.taskMain.index < player.playerTask.taskMain.subTasks.size()) {
                    NpcService.gI().createTutorial(player, tempId, avartar,
                            player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                } else
                    npcChat(player, "Hiện tại con không có nhiệm vụ chính nào hoặc có lỗi với nhiệm vụ.");
                break;
            case 1:
                if (player.LearnSkill.Time != -1) {
                    long time = player.LearnSkill.Time - System.currentTimeMillis();
                    int gem = 5 + (int) (Math.max(0, time) / 600_000L);
                    String skillName = "Kỹ năng không xác định";
                    byte skillLevel = 1;
                    try {
                        skillName = SkillUtil.findSkillTemplate(
                                SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name;
                        String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name
                                .split(" ");
                        if (subName.length > 0) {
                            try {
                                skillLevel = Byte.parseByte(subName[subName.length - 1]);
                            } catch (NumberFormatException e) {
                                String namePart = subName[subName.length - 1];
                                if (namePart.length() > 1) {
                                    try {
                                        skillLevel = Byte.parseByte(namePart.substring(namePart.length() - 1));
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.log("Error getting skill name/level for display: " + e.getMessage());
                    }
                    createOtherMenu(player, 12,
                            String.format("Con đang học kỹ năng\n%s cấp %d\nThời gian còn lại %s", skillName,
                                    skillLevel, TimeUtil.getTime(Math.max(0, time))),
                            "Học\nCấp tốc\n" + gem + " ngọc", "Huỷ", "Bỏ qua");
                } else
                    ShopService.gI().opendShop(player, "QUY_LAO", false);
                break;
            default:
                break;
        }
    }
}
