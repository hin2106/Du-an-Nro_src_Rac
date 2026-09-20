package services.shenron;
import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstTaskBadges;
import item.Item;
import item.Item.ItemOption;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import map.Zone;
import network.Message;
import player.Inventory;
import player.Player;
import server.Maintenance;
import services.ItemService;
import services.Service;
import services.map.NpcService;
import services.player.InventoryService;
import services.player.PlayerService;
import task.BadgesTaskService;
import utils.Logger;
import utils.Util;

public class SummonDragon {

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;
    public static final byte DRAGON_SHENRON = 0;
    public static final short NGOC_RONG_1_SAO = 14;
    public static final short NGOC_RONG_2_SAO = 15;
    public static final short NGOC_RONG_3_SAO = 16;
    public static final short NGOC_RONG_4_SAO = 17;
    public static final short NGOC_RONG_5_SAO = 18;
    public static final short NGOC_RONG_6_SAO = 19;
    public static final short NGOC_RONG_7_SAO = 20;

    public static final String SUMMON_SHENRON_TUTORIAL = "Có 3 cách gọi rồng thần. Gọi từ ngọc 1 sao, gọi từ ngọc 2 sao, hoặc gọi từ ngọc 3 sao\n"
            + "Các ngọc 4 sao đến 7 sao không thể gọi rồng thần được\n"
            + "Để gọi rồng 1 sao cần ngọc từ 1 sao đến 7 sao\n"
            + "Để gọi rồng 2 sao cần ngọc từ 2 sao đến 7 sao\n"
            + "Để gọi rồng 3 sao cần ngọc từ 3 sao đến 7sao\n"
            + "Điều ước rồng 3 sao: Capsule 3 sao, hoặc 2 triệu sức mạnh, hoặc 200k vàng\n"
            + "Điều ước rồng 2 sao: Capsule 2 sao, hoặc 20 triệu sức mạnh, hoặc 2 triệu vàng\n"
            + "Điều ước rồng 1 sao: Capsule 1 sao, hoặc 200 triệu sức mạnh, hoặc 20 triệu vàng, hoặc đẹp trai, hoặc....\n"
            + "Ngọc rồng sẽ mất ngay khi gọi rồng dù bạn có ước hay không\n"
            + "Quá 5 phút nếu không ước rồng thần sẽ bay mất";
    public static final String SHENRON_SAY = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định";
    public static final String[] SHENRON_1_STAR_WISHES_1 = new String[]{"Giàu có\n+2 Tỏi\nVàng",
        "Găng tay\nđang mang\nlên 1 cấp", "Chí mạng\nGốc +2%", "Thay\nChiêu 2-3\nĐệ tử", "Điều ước\nkhác"};
    public static final String[] SHENRON_1_STAR_WISHES_2 = new String[]{"Đẹp trai\nnhất\nVũ trụ",
        "Giàu có\n+10K\nNgọc", "+200 Tr\nSức mạnh\nvà tiềm\nnăng", "Găng tay đệ\nđang mang\nlên 1 cấp",
        "Điều ước\nkhác"};
    public static final String[] SHENRON_1_STAR_WISHES_3 = new String[]{"Quần\nđang mang\nlên 1 cấp",
        "Quần đệ\nđang mang\nlên 1 cấp", "Điều ước\nkhác"};
    public static final String[] SHENRON_2_STARS_WHISHES = new String[]{"Giàu có\n+2K\nNgọc",
        "+20 Tr\nSức mạnh\nvà tiềm năng", "Giàu có\n+200 Tr\nVàng"};
    public static final String[] SHENRON_3_STARS_WHISHES = new String[]{"Giàu có\n+200\nNgọc",
        "+2 Tr\nSức mạnh\nvà tiềm năng", "Giàu có\n+20 Tr\nVàng"};

    private static SummonDragon instance;
    private final Map<Player, Byte> pl_dragonStar;
    private long lastTimeShenronAppeared;
    private long lastTimeShenronWait;
    private final int timeResummonShenron = 600000;
    public boolean isShenronAppear;
    private final int timeShenronWait = 300000;
    private final Thread update;
    private boolean active;
    public boolean isPlayerDisconnect;
    public Player playerSummonShenron;
    private long playerSummonShenronId;
    private Zone mapShenronAppear;
    private byte shenronStar;
    private int menuShenron;
    private byte select;

    private SummonDragon() {
        this.pl_dragonStar = new HashMap<>();
        this.update = new Thread(() -> {
            while (active && !Maintenance.isRunning()) {
                try {
                    if (isShenronAppear) {
                        if (isPlayerDisconnect) {
                            if (mapShenronAppear != null) {
                                List<Player> players = mapShenronAppear.getPlayers();
                                if (players != null) {
                                    for (Player plMap : players) {
                                        if (plMap != null && plMap.isPl() && plMap.id == playerSummonShenronId) {
                                            playerSummonShenron = plMap;
                                            reSummonShenron();
                                            isPlayerDisconnect = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (playerSummonShenron != null && Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Logger.logException(SummonDragon.class, e);
                }
            }
        });
        this.active();
    }

    private void active() {
        if (!active) {
            active = true;
            this.update.start();
        }
    }

    public static SummonDragon gI() {
        if (instance == null) {
            instance = new SummonDragon();
        }
        return instance;
    }

    public void openMenuSummonShenron(Player pl, byte dragonBallStar) {
        if (pl == null) {
            return;
        }
        this.pl_dragonStar.put(pl, dragonBallStar);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_SHENRON, -1, "Bạn muốn gọi rồng thần ?",
                "Hướng\ndẫn thêm\n(mới)", "Gọi\nRồng Thần\n" + dragonBallStar + " Sao");
    }

    /**
     * Cleanup player from dragon star map when they disconnect
     */
    public void cleanupPlayer(Player pl) {
        if (pl != null) {
            this.pl_dragonStar.remove(pl);
        }
    }

    public void summonShenron(Player pl) {
        if (pl == null || pl.zone == null || pl.zone.map == null) {
            return;
        }
        if (pl.zone.map.mapId == 0 || pl.zone.map.mapId == 7 || pl.zone.map.mapId == 14) {
            if (checkShenronBall(pl)) {
                if (isShenronAppear) {
                    Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    return;
                }
                if (Util.canDoWithTime(lastTimeShenronAppeared, timeResummonShenron)) {
                    playerSummonShenron = pl;
                    playerSummonShenronId = pl.id;
                    mapShenronAppear = pl.zone;
                    Byte dragonStarObj = pl_dragonStar.get(playerSummonShenron);
                    if (dragonStarObj == null) {
                        Service.gI().sendThongBao(pl, "Lỗi hệ thống");
                        return;
                    }
                    byte dragonStar = dragonStarObj;
                    int begin = NGOC_RONG_1_SAO;
                    switch (dragonStar) {
                        case 2 ->
                            begin = NGOC_RONG_2_SAO;
                        case 3 ->
                            begin = NGOC_RONG_3_SAO;
                    }
                    for (int i = begin; i <= NGOC_RONG_7_SAO; i++) {
                        try {
                            InventoryService.gI().subQuantityItemsBag(pl, InventoryService.gI().findItemBag(pl, i), 1);
                        } catch (Exception ex) {
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                    sendNotifyShenronAppear();
                    activeShenron(pl, true, SummonDragon.DRAGON_SHENRON);
                    sendWhishesShenron(pl);
                } else {
                    int timeLeft = (int) ((timeResummonShenron - (System.currentTimeMillis() - lastTimeShenronAppeared))
                            / 1000);
                    Service.gI().sendThongBao(pl, "Vui lòng đợi "
                            + (timeLeft < 7200 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.gI().sendThongBao(pl, "Chỉ được gọi rồng thần ở ngôi làng trước nhà");
        }
    }

    private void reSummonShenron() {
        activeShenron(playerSummonShenron, true, SummonDragon.DRAGON_SHENRON);
        sendWhishesShenron(playerSummonShenron);
    }

    private void sendWhishesShenron(Player pl) {
        if (pl == null) {
            return;
        }
        byte dragonStar;
        Byte dragonStarObj = pl_dragonStar.get(pl);
        if (dragonStarObj != null) {
            dragonStar = dragonStarObj;
            this.shenronStar = dragonStar;
        } else {
            dragonStar = this.shenronStar;
        }
        switch (dragonStar) {
            case 1 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
            case 2 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_2, SHENRON_SAY, SHENRON_2_STARS_WHISHES);
            case 3 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_3, SHENRON_SAY, SHENRON_3_STARS_WHISHES);
        }
    }

    private void activeShenron(Player pl, boolean appear, byte type) {
        if (pl == null || (appear && (pl.zone == null || pl.zone.map == null || pl.location == null))) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("NgocRongWhis");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(type);
                lastTimeShenronWait = System.currentTimeMillis();
                isShenronAppear = true;
                if (pl.idMark != null) {
                    pl.idMark.shenronType = -1;
                }
            }
            Service.gI().sendMessAllPlayer(msg);
        } catch (IOException e) {
            Logger.logException(SummonDragon.class, e);
        } finally {
            if (msg != null && !appear) {
                msg.cleanup();
            }
        }
    }

    private boolean checkShenronBall(Player pl) {
        if (pl == null) {
            return false;
        }
        Byte dragonStarObj = this.pl_dragonStar.get(pl);
        if (dragonStarObj == null) {
            Service.gI().sendThongBao(pl, "Lỗi hệ thống");
            return false;
        }
        byte dragonStar = dragonStarObj;
        if (dragonStar == 1) {
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_1_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 1 sao");
                return false;
            }
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_2_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 2 sao");
                return false;
            }
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_3_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 3 sao");
                return false;
            }
        } else if (dragonStar == 2) {
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_2_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 2 sao");
                return false;
            }
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_3_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 3 sao");
                return false;
            }
        } else if (dragonStar == 3) {
            if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_3_SAO)) {
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 3 sao");
                return false;
            }
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_4_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 4 sao");
            return false;
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_5_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 5 sao");
            return false;
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_6_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 6 sao");
            return false;
        }
        if (!InventoryService.gI().isExistItemBag(pl, NGOC_RONG_7_SAO)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên ngọc rồng 7 sao");
            return false;
        }
        return true;
    }

    private void sendNotifyShenronAppear() {
        if (playerSummonShenron == null || playerSummonShenron.zone == null || playerSummonShenron.zone.map == null) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng thần tại "
                    + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
            Service.gI().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
        } catch (IOException e) {
            Logger.logException(SummonDragon.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void confirmWish() {
        if (playerSummonShenron == null) {
            return;
        }
        switch (this.menuShenron) {
            case ConstNpc.SHENRON_1_1 -> {
                switch (this.select) {
                    case 0 -> {
                        addGoldSafely(this.playerSummonShenron, 2000000000L);
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                    }
                    case 1 -> {
                        if (this.playerSummonShenron.inventory == null
                                || this.playerSummonShenron.inventory.itemsBody == null
                                || this.playerSummonShenron.inventory.itemsBody.size() <= 2) {
                            Service.gI().sendThongBao(playerSummonShenron, "Ngươi hiện tại có đeo găng đâu");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                        Item item = this.playerSummonShenron.inventory.itemsBody.get(2);
                        if (item != null && item.isNotNullItem()) {
                            int level = 0;
                            for (ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id == 72) {
                                    level = io.param;
                                    if (level < 7) {
                                        io.param++;
                                    }
                                    break;
                                }
                            }
                            if (level < 7) {
                                if (level == 0) {
                                    item.itemOptions.add(new ItemOption(72, 1));
                                }
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id == 0) {
                                        io.param += (io.param * 10 / 100);
                                        break;
                                    }
                                }
                                InventoryService.gI().sendItemBody(playerSummonShenron);
                            } else {
                                Service.gI().sendThongBao(playerSummonShenron, "Găng tay của ngươi đã đạt cấp tối đa");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Ngươi hiện tại có đeo găng đâu");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                    }
                    case 2 -> {
                        if (this.playerSummonShenron.nPoint != null && this.playerSummonShenron.nPoint.critdragon < 9) {
                            this.playerSummonShenron.nPoint.critdragon += 2;
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron,
                                    "Điều ước này đã quá sức với ta, ta sẽ cho ngươi chọn lại");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                    }
                    case 3 -> {
                        if (playerSummonShenron.pet != null) {
                            if (playerSummonShenron.pet.playerSkill != null
                                    && playerSummonShenron.pet.playerSkill.skills != null
                                    && playerSummonShenron.pet.playerSkill.skills.size() > 1
                                    && playerSummonShenron.pet.playerSkill.skills.get(1).skillId != -1) {
                                playerSummonShenron.pet.openSkill2();
                                if (playerSummonShenron.pet.playerSkill.skills.size() > 2
                                        && playerSummonShenron.pet.playerSkill.skills.get(2).skillId != -1) {
                                    playerSummonShenron.pet.openSkill3();
                                }
                            } else {
                                Service.gI().sendThongBao(playerSummonShenron,
                                        "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Ngươi làm gì có đệ tử?");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                    }
                }
            }
            case ConstNpc.SHENRON_1_2 -> {
                switch (this.select) {
                    case 0 -> {
                        if (InventoryService.gI().getCountEmptyBag(playerSummonShenron) > 0) {
                            byte gender = this.playerSummonShenron.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 227
                                    : gender == ConstPlayer.NAMEC ? 228 : 229));
                            avtVip.itemOptions.add(new ItemOption(97, Util.nextInt(5, 10)));
                            avtVip.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                            InventoryService.gI().addItemBag(playerSummonShenron, avtVip);
                            InventoryService.gI().sendItemBags(playerSummonShenron);
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Hành trang đã đầy");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                    }
                    case 1 -> {
                        this.playerSummonShenron.inventory.gem += 10000;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                    }
                    case 2 -> {
                        if (this.playerSummonShenron.nPoint != null
                                && this.playerSummonShenron.nPoint.power <= 200000000000L) {
                            Service.gI().addSMTN(this.playerSummonShenron, (byte) 2, 200000000, false);
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron,
                                    "Xin lỗi, điều ước này khó quá, ta không thể thực hiện.");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                    }
                    case 3 -> {
                        if (this.playerSummonShenron.pet != null) {
                            if (this.playerSummonShenron.pet.inventory == null
                                    || this.playerSummonShenron.pet.inventory.itemsBody == null
                                    || this.playerSummonShenron.pet.inventory.itemsBody.size() <= 2) {
                                Service.gI().sendThongBao(playerSummonShenron, "Đệ ngươi hiện tại có đeo găng đâu");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                            Item item = this.playerSummonShenron.pet.inventory.itemsBody.get(2);
                            if (item != null && item.isNotNullItem()) {
                                int level = 0;
                                for (ItemOption io : item.itemOptions) {
                                    if (io.optionTemplate.id == 72) {
                                        level = io.param;
                                        if (level < 7) {
                                            io.param++;
                                        }
                                        break;
                                    }
                                }
                                if (level < 7) {
                                    if (level == 0) {
                                        item.itemOptions.add(new ItemOption(72, 1));
                                    }
                                    for (ItemOption io : item.itemOptions) {
                                        if (io.optionTemplate.id == 0) {
                                            io.param += (io.param * 10 / 100);
                                            break;
                                        }
                                    }
                                    Service.gI().point(playerSummonShenron);
                                } else {
                                    Service.gI().sendThongBao(playerSummonShenron,
                                            "Găng tay của đệ ngươi đã đạt cấp tối đa");
                                    reOpenShenronWishes(playerSummonShenron);
                                    return;
                                }
                            } else {
                                Service.gI().sendThongBao(playerSummonShenron, "Đệ ngươi hiện tại có đeo găng đâu");
                                reOpenShenronWishes(playerSummonShenron);
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(playerSummonShenron, "Ngươi đâu có đệ tử");
                            reOpenShenronWishes(playerSummonShenron);
                            return;
                        }
                    }
                }
            }
            case ConstNpc.SHENRON_2 -> {
                switch (this.select) {
                    case 0 -> {
                        this.playerSummonShenron.inventory.gem += 2000;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                    }
                    case 1 -> {
                        Service.gI().addSMTN(this.playerSummonShenron, (byte) 2, 20000000, false);
                    }
                    case 2 -> {
                        addGoldSafely(this.playerSummonShenron, 200000000L);
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                    }
                }
            }
            case ConstNpc.SHENRON_3 -> {
                switch (this.select) {
                    case 0 -> {
                        this.playerSummonShenron.inventory.gem += 200;
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                    }
                    case 1 ->
                        Service.gI().addSMTN(this.playerSummonShenron, (byte) 2, 2000000, false);
                    case 2 -> {
                        addGoldSafely(this.playerSummonShenron, 20000000L);
                        PlayerService.gI().sendInfoHpMpMoney(this.playerSummonShenron);
                    }
                }
            }
        }
        if (menuShenron == ConstNpc.SHENRON_1_1
                || menuShenron == ConstNpc.SHENRON_1_2
                || menuShenron == ConstNpc.SHENRON_1_3) {
            BadgesTaskService.updateCountBagesTask(playerSummonShenron, ConstTaskBadges.TRUM_UOC_RONG, 1);
        }
        shenronLeave(this.playerSummonShenron, WISHED);
    }

    public void showConfirmShenron(Player pl, int menu, byte select) {
        if (pl == null) {
            return;
        }
        this.menuShenron = menu;
        this.select = select;
        String wish = null;
        try {
            switch (menu) {
                case ConstNpc.SHENRON_1_1 -> {
                    if (select >= 0 && select < SHENRON_1_STAR_WISHES_1.length) {
                        wish = SHENRON_1_STAR_WISHES_1[select];
                    }
                }
                case ConstNpc.SHENRON_1_2 -> {
                    if (select >= 0 && select < SHENRON_1_STAR_WISHES_2.length) {
                        wish = SHENRON_1_STAR_WISHES_2[select];
                    }
                }
                case ConstNpc.SHENRON_1_3 -> {
                    if (select >= 0 && select < SHENRON_1_STAR_WISHES_3.length) {
                        wish = SHENRON_1_STAR_WISHES_3[select];
                    }
                }
                case ConstNpc.SHENRON_2 -> {
                    if (select >= 0 && select < SHENRON_2_STARS_WHISHES.length) {
                        wish = SHENRON_2_STARS_WHISHES[select];
                    }
                }
                case ConstNpc.SHENRON_3 -> {
                    if (select >= 0 && select < SHENRON_3_STARS_WHISHES.length) {
                        wish = SHENRON_3_STARS_WHISHES[select];
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.logException(SummonDragon.class, e,
                    "Array index out of bounds in showConfirmShenron: menu=" + menu + ", select=" + select);
            Service.gI().sendThongBao(pl, "Lỗi hệ thống");
            return;
        }
        if (wish != null) {
            NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_CONFIRM, "Ngươi có chắc muốn ước?", wish,
                    "Từ chối");
        } else {
            Service.gI().sendThongBao(pl, "Lỗi hệ thống");
        }
    }

    public void reOpenShenronWishes(Player pl) {
        switch (menuShenron) {
            case ConstNpc.SHENRON_1_1 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
            case ConstNpc.SHENRON_1_2 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_1_2, SHENRON_SAY, SHENRON_1_STAR_WISHES_2);
            case ConstNpc.SHENRON_1_3 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_1_3, SHENRON_SAY, SHENRON_1_STAR_WISHES_3);
            case ConstNpc.SHENRON_2 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_2, SHENRON_SAY, SHENRON_2_STARS_WHISHES);
            case ConstNpc.SHENRON_3 ->
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_3, SHENRON_SAY, SHENRON_3_STARS_WHISHES);
        }
    }

    private boolean addGoldSafely(Player pl, long amount) {
        if (pl == null || pl.inventory == null) {
            return false;
        }
        long currentGold = pl.inventory.gold;
        if (currentGold + amount > Inventory.LIMIT_GOLD) {
            pl.inventory.gold = Inventory.LIMIT_GOLD;
        } else {
            pl.inventory.gold += amount;
        }
        return true;
    }

    public void shenronLeave(Player pl, byte type) {
        if (pl != null) {
            if (type == WISHED) {
                NpcService.gI().createTutorial(pl, 0,
                        "Điều ước của ngươi đã trở thành sự thật\nHẹn gặp ngươi lần sau, ta đi ngủ đây, bái bai");
            } else {
                NpcService.gI().createMenuRongThieng(pl, ConstNpc.IGNORE_MENU,
                        "Ta buồn ngủ quá rồi\nHẹn gặp ngươi lần sau, ta đi đây, bái bai");
            }
        }
        activeShenron(pl, false, SummonDragon.DRAGON_SHENRON);
        this.isShenronAppear = false;
        this.menuShenron = -1;
        this.select = -1;
        if (this.playerSummonShenron != null) {
            // Cleanup player from map when shenron leaves
            cleanupPlayer(this.playerSummonShenron);
        }
        this.playerSummonShenron = null;
        this.playerSummonShenronId = -1;
        this.shenronStar = -1;
        this.mapShenronAppear = null;
        lastTimeShenronAppeared = System.currentTimeMillis();
    }
}
