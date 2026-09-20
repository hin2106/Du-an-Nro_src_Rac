package npc.list;

import clan.Clan;
import consts.ConstNpc;
import consts.ConstEvent;

import item.Item;
import java.util.ArrayList;
import java.util.List;

import dungeon.TreasureUnderSea;

import services.dungeon.TreasureUnderSeaService;
import npc.Npc;
import static npc.NpcFactory.PLAYERID_OBJECT;
import player.Player;
import server.Manager;
import services.ItemService;
import services.player.InventoryService;
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
import services.top.TopBDKBService;

public class QuyLaoKame extends Npc {

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        int countOfEventItem1999 = 0;
        Item eventItem = InventoryService.gI().findItemBag(player, (short) 1999);
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
            if (!player.canReward) {
                menu.add("Nói\nchuyện");
                if (ruacon != null && ruacon.quantity >= 1) {
                    menu.add("Giao\nRùa con");
                }
            } else {
                menu.add("Giao\nLân con");
            }

            if (eventItem != null) {
                countOfEventItem1999 = eventItem.quantity;
            }
            boolean showExchange = Manager.EVENT_SEVER == ConstEvent.SU_KIEN_HALLOWEEN
                    || Manager.EVENT_SEVER == ConstEvent.SU_KIEN_TRUNG_THU;
            if (showExchange) {
                menu.add("Đổi điểm\nsự kiện\n [" + countOfEventItem1999 + "]");
            }

            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
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
            switch (player.idMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    int talkOptionIndex = 0;
                    int giveTurtleOptionIndex = 1;
                    Item ruacon = InventoryService.gI().findItemBag(player, 874);
                    boolean hasTurtleItem = (ruacon != null && ruacon.quantity >= 1);

                    if (select == talkOptionIndex) {
                        ArrayList<String> menu = new ArrayList<>();
                        menu.add("Nhiệm vụ");
                        menu.add("Học\nKỹ năng");
                        Clan clan = player.clan;
                        if (clan != null) {
                            menu.add("Về khu\nvực bang");
                            if (clan.isLeader(player)) {
                                menu.add("Giải tán\nBang hội");
                            }
                        }
                        menu.add("Kho báu\ndưới biển");
                        this.createOtherMenu(player, ConstNpc.MENU_LEAR,
                                "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menu.toArray(String[]::new));

                    } else if (hasTurtleItem && select == giveTurtleOptionIndex) {
                        this.createOtherMenu(player, 1,
                                "Cảm ơn cậu đã cứu con rùa của ta\nĐể cảm ơn ta sẽ tặng cậu món quà.",
                                "Nhận quà", "Đóng");

                    } else {
                        int exchangeIndex = (hasTurtleItem ? 2 : 1);
                        boolean showExchange = Manager.EVENT_SEVER == ConstEvent.SU_KIEN_HALLOWEEN
                                || Manager.EVENT_SEVER == ConstEvent.SU_KIEN_TRUNG_THU;
                        if (showExchange && select == exchangeIndex) {
                            if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_HALLOWEEN) {
                                ShopService.gI().opendShop(player, "DOI_HALLOWEEN", true);
                            } else if (Manager.EVENT_SEVER == ConstEvent.SU_KIEN_TRUNG_THU) {
                                ShopService.gI().opendShop(player, "QDDN", true);
                            }

                        }
                    }
                }
                case ConstNpc.MENU_LEAR -> {
                    switch (select) {
                        case 0 ->
                            NpcService.gI().createTutorial(player, tempId, avartar,
                                    player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                        case 1 -> {
                            if (player.LearnSkill != null && player.LearnSkill.Time != -1) {
                                var ngoc = 5;
                                var time = player.LearnSkill.Time - System.currentTimeMillis();
                                if (time / 600_000 >= 2) {
                                    ngoc += time / 600_000;
                                }
                                try {
                                    String skillName = ItemService.gI()
                                            .getTemplate(player.LearnSkill.ItemTemplateSkillId).name;
                                    byte level = 0;
                                    for (int i = skillName.length() - 1; i >= 0; i--) {
                                        char c = skillName.charAt(i);
                                        if (Character.isDigit(c)) {
                                            level = Byte.parseByte(String.valueOf(c));
                                            break;
                                        }
                                    }
                                    this.createOtherMenu(player, 12,
                                            "Con đang học kỹ năng\n"
                                                    + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(
                                                            player.LearnSkill.ItemTemplateSkillId)).name
                                                    + " cấp " + level + "\nThời gian còn lại " + TimeUtil.getTime(time),
                                            "Học\nCấp tốc\n" + ngoc + " ngọc", "Huỷ", "Bỏ qua");
                                } catch (Exception e) {
                                    Logger.log(e.toString());
                                    Service.gI().sendThongBao(player, "Có lỗi khi hiển thị thông tin kỹ năng.");
                                }
                            } else {
                                ShopService.gI().opendShop(player, "QUY_LAO", false);
                            }
                        }
                        case 2 -> {
                            Clan clan = player.clan;
                            if (clan != null) {
                                ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
                            } else {
                                Service.gI().sendThongBao(player, "Bạn chưa có bang hội.");
                            }
                        }
                        case 3 -> {
                            Clan clan = player.clan;
                            if (clan != null && clan.isLeader(player)) {
                                createOtherMenu(player, 4, "Con có chắc muốn giải tán bang hội không?", "Đồng ý",
                                        "Từ chối");
                            } else {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                                    + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 4 -> {
                            if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                        "Bang hội con đang ở hang kho báu cấp "
                                                + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                        "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                        "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                        "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                            }
                        }
                    }
                }
                case 12 -> {
                    switch (select) {
                        case 1 ->
                            this.createOtherMenu(player, 13,
                                    "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?",
                                    "Ok", "Đóng");
                        case 0 -> {
                            if (player.LearnSkill == null || player.LearnSkill.Time == -1) {
                                Service.gI().sendThongBao(player, "Bạn không đang học kỹ năng nào.");
                                return;
                            }
                            var time = player.LearnSkill.Time - System.currentTimeMillis();
                            var ngoc = 5;
                            if (time / 600_000 >= 2) {
                                ngoc += time / 600_000;
                            }
                            if (player.inventory.gem < ngoc) {
                                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                                return;
                            }
                            player.inventory.subGem(ngoc);
                            player.LearnSkill.Time = -1;
                            try {
                                String skillName = ItemService.gI()
                                        .getTemplate(player.LearnSkill.ItemTemplateSkillId).name;
                                byte level = 0;
                                for (int i = skillName.length() - 1; i >= 0; i--) {
                                    char c = skillName.charAt(i);
                                    if (Character.isDigit(c)) {
                                        level = Byte.parseByte(String.valueOf(c));
                                        break;
                                    }
                                }

                                int skillTemplateId = SkillUtil
                                        .getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId);
                                if (!SkillUtil.validateSkillBeforeLearn(player, skillTemplateId)) {
                                    Service.gI().sendThongBao(player,
                                            "Kỹ năng này không phù hợp với hành tinh của bạn!");
                                    Logger.warning("Player " + player.name + " (gender: " + player.gender
                                            + ") cố gắng học skill không hợp lệ: " + skillTemplateId);
                                    return;
                                }

                                Skill curSkill = SkillUtil.getSkillByItemID(player,
                                        player.LearnSkill.ItemTemplateSkillId);
                                if (curSkill.point == 0) {
                                    player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                    curSkill = SkillUtil.createSkill(skillTemplateId, level);
                                    SkillUtil.setSkill(player, curSkill);
                                    var msg = Service.gI().messageSubCommand((byte) 23);
                                    msg.writer().writeShort(curSkill.skillId);
                                    player.sendMessage(msg);
                                    msg.cleanup();
                                } else {
                                    curSkill = SkillUtil.createSkill(skillTemplateId, level);
                                    player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                    SkillUtil.setSkill(player, curSkill);
                                    var msg = Service.gI().messageSubCommand((byte) 62);
                                    msg.writer().writeShort(curSkill.skillId);
                                    player.sendMessage(msg);
                                    msg.cleanup();
                                    PlayerService.gI().sendInfoHpMpMoney(player);
                                }
                                Service.gI().sendThongBao(player, "Học kỹ năng thành công!");
                            } catch (Exception e) {
                                Logger.log(e.toString());
                                Service.gI().sendThongBao(player, "Có lỗi khi học kỹ năng.");
                            }
                        }
                    }
                }

                case 13 -> {
                    if (select == 0) {
                        if (player.LearnSkill != null && player.LearnSkill.ItemTemplateSkillId != 0) {
                            try {
                                player.LearnSkill.Time = -1;
                                Service.gI().sendThongBao(player,
                                        "Bạn đã hủy học kỹ năng thành công và nhận lại 50% tiềm năng.");
                            } catch (Exception e) {
                                Logger.log(e.toString());
                                Service.gI().sendThongBao(player, "Có lỗi khi hủy học kỹ năng.");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Bạn không đang học kỹ năng nào.");
                        }
                    }
                }
                case 4 -> {
                    Clan clan = player.clan;
                    if (clan != null && clan.isLeader(player)) {
                        if (select == 0) {
                            Input.gI().createFormGiaiTanBangHoi(player);
                        }
                    }
                }
                case ConstNpc.MENU_OPENED_DBKB, ConstNpc.MENU_OPEN_DBKB -> {
                    switch (select) {
                        case 0 -> {
                            TopBDKBService.gI().showTop(player);
                        }
                        case 1 -> {
                            // TopThanhTichService.gI().showTop(player);
                        }
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPENED_DBKB) {
                                    ChangeMapService.gI().goToDBKB(player);
                                } else {
                                    Input.gI().createFormChooseLevelBDKB(player);
                                }
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }
                    }
                }
                case ConstNpc.MENU_ACCEPT_GO_TO_BDKB -> {
                    switch (select) {
                        case 0 ->
                            TreasureUnderSeaService.gI().openBanDoKhoBau(player,
                                    Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                    }
                }
                case ConstNpc.MENU_XOA_RAC -> {
                    List<Item> itemsHsd = player.inventory.itemsBag.stream()
                            .filter(i -> i != null && i.isNotNullItem()
                                    && i.itemOptions.stream().anyMatch(io -> io.optionTemplate.id == 93))
                            .toList();

                    itemsHsd.stream()
                            .forEach(i -> InventoryService.gI().subQuantityItemsBag(player, i, i.quantity));

                    InventoryService.gI().sendItemBags(player);

                    Service.gI().sendThongBao(player, "Đã xóa các vật phẩm rác!");
                }

            }
        }
    }
}
