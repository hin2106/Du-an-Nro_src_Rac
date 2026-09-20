package services.shenron;
import consts.ConstNpc;
import item.Item;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import map.Zone;
import network.Message;
import player.Player;
import server.Client;
import services.ItemService;
import services.ItemTimeService;
import services.Service;
import services.map.NpcService;
import services.player.InventoryService;
import utils.Util;

public class SummonDragonBonney {

    private static final List<SummonDragonBonney> list = new CopyOnWriteArrayList<>();
    private static SummonDragonBonney instance;
    private static long lastTimeShenronAppeared;
    private static long lastUpdate;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;
    public static final byte DRAGON_EVENT = 1;

    public static int timeResummonShenron = 60000;
    public static int timeShenronWait = 60000;

    public static final short NGOC_RONG_1_SAO = 702;
    public static final short NGOC_RONG_2_SAO = 703;
    public static final short NGOC_RONG_3_SAO = 704;
    public static final short NGOC_RONG_4_SAO = 705;
    public static final short NGOC_RONG_5_SAO = 706;
    public static final short NGOC_RONG_6_SAO = 707;
    public static final short NGOC_RONG_7_SAO = 708;

    public static final String SHENRONEVENT_SAY = "Ta sẽ ban cho người một điều ước, ngươi có 5 phút, hãy chọn đi:\n1) Đổi cả 3 kỹ năng đầu của đệ tử\n(Lưu ý: Kỹ năng mới có cấp 1 và vẫn có thể trùng lại với kỹ năng vốn có).\n2) X3 tiềm năng sức mạnh đệ tử 30 phút.\n3) Tăng thêm 15% HP cho sư phụ khi sử dụng bông tai Porata 30 phút.\n4) Tăng thêm 15% KI cho sư phụ khi sử dụng bông tai Porata 30 phút.\n5) Tăng thêm 15% sức đánh cho sư phụ khi sử dụng bông tai Porata 30 phút.";
    public static final String SHENRONEVENT_SAY_2 = "Ta sẽ ban cho người một điều ước, ngươi có 5 phút, hãy chọn đi:\n6) Tiềm năng sức mạnh X3 cho bản thân 30 phút.\n7) Giảm 100 triệu sức mạnh bản thân (yêu cầu trên 40 tỷ sức mạnh).\n8) Pet chỉ Địa ngục hạn sử dụng từ 15 tới 90 ngày.\n9) 2% cơ hội ra trứng Majin Bưu (nếu hụt sẽ ra hộp sao pha lê VIP).";
    public static final String[] SHENRON_WISHES = new String[]{"Điều ước 1", "Điều ước 2", "Điều ước 3", "Điều ước 4", "Điều ước 5", "->"};
    public static final String[] SHENRON_WISHES_2 = new String[]{"<-", "Điều ước 6", "Điều ước 7", "Điều ước 8", "Điều ước 9"};

    private Player player;
    private Zone zone;
    public long playerId;
    public boolean isPlayerDisconnect;
    public byte select;
    public int shenronType;
    public boolean leaveMap;
    public long lastTimeShenronWait;
    public boolean shenronLeave;

    public static SummonDragonBonney gI() {
        if (instance == null) {
            instance = new SummonDragonBonney();
        }
        return instance;
    }

    public static void update() {
        if (Util.canDoWithTime(lastUpdate, 1000)) {
            lastUpdate = System.currentTimeMillis();
            for (SummonDragonBonney se : list) {
                try {
                    se.updateInstance();
                } catch (Exception e) {
                    System.err.println("Error updating SummonDragonBonney for player " + se.playerId);
                }
            }
        }
    }

    public static void openMenuSummonShenron(Player pl, int type) {
        pl.idMark.setShenronType(type);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_SHENRON_EVENT, -1, "Bạn có muốn gọi Rồng Xương không ?", "Đồng ý", "Từ chối");
    }

    public static void summonShenron(Player player) {
        if (player == null || player.zone == null) {
            return;
        }
        if (player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14) {
            if (checkShenronBall(player)) {
                if (player.isShenronAppear || player.shenronEvent != null) {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                    return;
                }
                if (Util.canDoWithTime(lastTimeShenronAppeared, timeResummonShenron)) {
                    for (int i = NGOC_RONG_1_SAO; i <= NGOC_RONG_7_SAO; i++) {
                        try {
                            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, i), 1);
                        } catch (Exception ex) {
                        }
                    }
                    InventoryService.gI().sendItemBags(player);
                    SummonDragonBonney shenron = new SummonDragonBonney();
                    shenron.setPlayer(player);
                    shenron.setZone(player.zone);
                    player.shenronEvent = shenron;
                    add(shenron);
                    shenron.activeShenron(true, DRAGON_EVENT);
                    shenron.sendBlackGokuhesShenron();
                } else {
                    int timeLeft = (int) ((timeResummonShenron - (System.currentTimeMillis() - lastTimeShenronAppeared)) / 1000);
                    Service.gI().sendThongBao(player, "Vui lòng đợi " + (timeLeft < 7200 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.gI().sendThongBao(player, "Không thể gọi rồng ở đây");
        }
    }

    private static void add(SummonDragonBonney se) {
        list.add(se);
    }

    private static void remove(SummonDragonBonney se) {
        list.remove(se);
    }

    private static boolean checkShenronBall(Player pl) {
        for (int i = NGOC_RONG_1_SAO; i <= NGOC_RONG_7_SAO; i++) {
            if (!InventoryService.gI().isExistItemBag(pl, i)) {
                Item it = ItemService.gI().createNewItem((short) i);
                if (it != null) {
                    Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên " + it.template.name);
                }
                return false;
            }
        }
        return true;
    }

    public void updateInstance() {
        if (shenronLeave) {
            return;
        }
        if (isPlayerDisconnect) {
            Player pl = Client.gI().getPlayer(playerId);
            if (pl != null) {
                this.player = pl;
                if (this.player.zone != null && this.player.zone.map.mapId != 0 && this.player.zone.map.mapId != 7
                        && this.player.zone.map.mapId != 14 && this.player.zone.map.mapId != 21
                        && this.player.zone.map.mapId != 22 && this.player.zone.map.mapId != 23) {
                    this.player.shenronEvent = this;
                    this.zone = this.player.zone;
                    this.player.idMark.setShenronType(this.shenronType);
                    this.isPlayerDisconnect = false;
                    reSummonShenron();
                }
            }
        }
        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
            leaveMap = true;
            if (this.player != null && this.player.idMark != null) {
                NpcService.gI().createMenuRongThieng(this.player, ConstNpc.IGNORE_MENU,
                        "Còn cái nịt =))\nCó không ước mất đừng tìm.", "Xin vĩnh biệt cụ........");
            }
            shenronLeave();
        }
    }

    public void reSummonShenron() {
        activeShenron(true, DRAGON_EVENT);
        sendBlackGokuhesShenron();
    }

    public void sendBlackGokuhesShenron() {
        NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_EVENT_CONFIRM, SHENRONEVENT_SAY, SHENRON_WISHES);
    }

    public void sendBlackGokuhesShenron2() {
        NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_EVENT_CONFIRM_2, SHENRONEVENT_SAY_2, SHENRON_WISHES_2);
    }

    public void showConfirmShenron(byte select) {
        if (select == 5) {
            sendBlackGokuhesShenron2();
            return;
        }
        this.select = select;
        String wish = SHENRON_WISHES[select];
        NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_EVENT_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void showConfirmShenron2(byte select) {
        if (select == 0) {
            sendBlackGokuhesShenron();
            return;
        }
        this.select = (byte) (select + 4);
        String wish = SHENRON_WISHES_2[select];
        NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_EVENT_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void activeShenron(boolean appear, byte type) {
        if (player == null || player.zone == null) {
            return;
        }
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : 1);
            if (appear) {
                msg.writer().writeShort(player.zone.map.mapId);
                msg.writer().writeShort(player.zone.map.bgId);
                msg.writer().writeByte(player.zone.zoneId);
                msg.writer().writeInt((int) player.id);
                msg.writer().writeUTF("null");
                msg.writer().writeShort(player.location.x);
                msg.writer().writeShort(player.location.y);
                msg.writer().writeByte(type);
                this.playerId = player.id;
                this.shenronType = player.idMark.getShenronType();
                this.zone.shenronType = this.shenronType;
                this.lastTimeShenronWait = System.currentTimeMillis();
                player.isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (IOException e) {
        }
    }

    public void confirmWish() {
        if (player == null || player.idMark == null) {
            shenronLeave();
            return;
        }
        switch (player.idMark.getShenronType()) {
            case 0 -> {
                switch (this.select) {
                    case 0 -> {
                        if (player.pet != null) {
                            if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                player.pet.openSkill2();
                                player.pet.openSkill3();
                                Service.gI().sendThongBao(player, "Đã đổi kỹ năng đệ tử thành công!");
                            } else {
                                Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                    }
                    case 1 -> {
                        if (player.pet == null) {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        if (player.itemEvent.isShenronPorataHp || player.itemEvent.isShenronPorataKi
                                || player.itemEvent.isShenronPorataDame || player.itemEvent.isShenronPlayerX3) {
                            Service.gI().sendThongBao(player, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemEvent.isShenronPetX3 = true;
                        player.itemEvent.lastTimeShenronPetX3 = System.currentTimeMillis();
                        Service.gI().sendThongBao(player, "Đệ tử nhận x3 tiềm năng trong 30 phút!");
                    }
                    case 2 -> {
                        if (player.itemEvent.isShenronPetX3 || player.itemEvent.isShenronPorataKi
                                || player.itemEvent.isShenronPorataDame || player.itemEvent.isShenronPlayerX3) {
                            Service.gI().sendThongBao(player, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemEvent.isShenronPorataHp = true;
                        player.itemEvent.lastTimeShenronPorataHp = System.currentTimeMillis();
                        Service.gI().sendThongBao(player, "Khi hợp thể bông tai Porata, HP tăng thêm 15% trong 30 phút!");
                    }
                    case 3 -> {
                        if (player.itemEvent.isShenronPetX3 || player.itemEvent.isShenronPorataHp
                                || player.itemEvent.isShenronPorataDame || player.itemEvent.isShenronPlayerX3) {
                            Service.gI().sendThongBao(player, "Bố hiếu đz");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemEvent.isShenronPorataKi = true;
                        player.itemEvent.lastTimeShenronPorataKi = System.currentTimeMillis();
                        Service.gI().sendThongBao(player, "Khi hợp thể bông tai Porata, KI tăng thêm 15% trong 30 phút!");
                    }
                    case 4 -> {
                        if (player.itemEvent.isShenronPetX3 || player.itemEvent.isShenronPorataHp
                                || player.itemEvent.isShenronPorataKi || player.itemEvent.isShenronPlayerX3) {
                            Service.gI().sendThongBao(player, "Bố hiếu đz");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemEvent.isShenronPorataDame = true;
                        player.itemEvent.lastTimeShenronPorataDame = System.currentTimeMillis();
                        Service.gI().sendThongBao(player, "Khi hợp thể bông tai Porata, sức đánh tăng thêm 15% trong 30 phút!");
                    }
                    case 5 -> {
                        if (player.itemEvent.isShenronPetX3 || player.itemEvent.isShenronPorataHp
                                || player.itemEvent.isShenronPorataKi || player.itemEvent.isShenronPorataDame) {
                            Service.gI().sendThongBao(player, "Bố hiếu đz");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemEvent.isShenronPlayerX3 = true;
                        player.itemEvent.lastTimeShenronPlayerX3 = System.currentTimeMillis();
                        Service.gI().sendThongBao(player, "Bản thân nhận x3 tiềm năng trong 30 phút!");
                    }
                    case 6 -> {
                        if (player.nPoint.power < 40_000_000_000L) {
                            Service.gI().sendThongBao(player, "Yêu cầu tối thiểu 40 tỷ sức mạnh.");
                            sendBlackGokuhesShenron2();
                            return;
                        }
                        player.nPoint.power = Math.max(0L, player.nPoint.power - 100_000_000L);
                        Service.gI().sendThongBao(player, "Đã giảm 100 triệu sức mạnh của ngươi.");
                    }
//                    case 7:
//                        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
//                            Service.gI().sendThongBao(player, "Hành trang đã đầy, hãy chừa ít nhất 1 ô trống.");
//                            sendBlackGokuhesShenron2();
//                            return;
//                        }
//                        short petTemplateId = HELL_EVENT_PET_IDS[Util.nextInt(0, HELL_EVENT_PET_IDS.length - 1)];
//                        Item hellPet = ItemService.gI().createNewItem(petTemplateId);
//                        int durationDays = Util.nextInt(15, 90);
//                        hellPet.itemOptions.add(new ItemOption(93, durationDays));
//                        InventoryService.gI().addItemBag(player, hellPet);
//                        InventoryService.gI().sendItemBags(player);
//                        Service.gI().sendThongBao(player, "Bạn nhận được " + hellPet.template.name + " (" + durationDays + " ngày).");
//                        break;
//                    case 8:
//                        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
//                            Service.gI().sendThongBao(player, "Hành trang đãầy, hãy chừa ít nhất 1 ô trống.");
//                            sendBlackGokuhesShenron2();
//                            return;
//                        }
//                        boolean receiveEgg = Util.isTrue(2, 100);
//                        Item rewardItem = createItemByName(receiveEgg ? ITEM_NAME_MAJIN_EGG : ITEM_NAME_CRYSTAL_BOX_VIP);
//                        if (rewardItem == null) {
//                            Service.gI().sendThongBao(player, "Phần thưởng đang được cập nhật, vui lòng chọn lại sau.");
//                            sendBlackGokuhesShenron2();
//                            return;
//                        }
//                        InventoryService.gI().addItemBag(player, rewardItem);
//                        InventoryService.gI().sendItemBags(player);
//                        Service.gI().sendThongBao(player, receiveEgg ? "Bạn nhận được Trứng Majin Bưu!" : "Bạn nhận được Hộp sao pha lê VIP.");
//                        break;
                }
                Service.gI().point(player);
                ItemTimeService.gI().sendAllItemTime(player);
            }
        }
        shenronLeave();
    }

    public void shenronLeave() {
        if (!shenronLeave) {
            shenronLeave = true;
            if (player != null && player.zone != null) {
                player.shenronEvent = null;
                if (!leaveMap) {
                    NpcService.gI().createTutorial(player, 0, "Điều ước của ngươi đã được thực hiện...tạm biệt");
                }
                activeShenron(false, DRAGON_EVENT);
                player.isShenronAppear = false;
            }
            if (zone != null) {
                zone.shenronType = -1;
            }
            lastTimeShenronAppeared = System.currentTimeMillis();
            remove(this);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }
}
