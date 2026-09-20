package services.func;

import audit.AssetAuditService;

import boss.Boss;import boss.miniboss.SoiHecQuyn;
import boss.miniboss.XinBaTo;
import services.shenron.SummonDragon;
import combine.CombineService;
import consts.ConstItem;
import radar.Card;
import services.RadarService;
import radar.RadarCard;
import consts.ConstMap;
import item.Item;
import consts.ConstNpc;
import consts.ConstPlayer;
import consts.ConstTaskBadges;
import data.AlyraManager;
import item.Item.ItemOption;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import map.ItemMap;
import map.Zone;
import player.Inventory;
import services.map.NpcService;
import player.Player;
import skill.Skill;
import network.Message;
import services.map.ChangeMapService;
import utils.SkillUtil;
import services.Service;
import utils.Util;
import network.MySession;
import server.ServerNotify;
import services.ItemService;
import services.ItemTimeService;
import services.PetService;
import services.RewardService;
import services.player.PlayerService;
import services.TaskService;
import services.player.InventoryService;
import services.map.MapService;
import services.dungeon.NgocRongNamecService;
import services.map.ItemMapService;
import services.shenron.SummonDragonBonney;
import task.BadgesTaskService;
import utils.Logger;

public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;
    
    // Danh sách các TYPE vật phẩm CHO PHÉP vứt ra đất (Ngoài danh sách này sẽ KHÔNG ĐƯỢC vỨT)
    private static final Set<Integer> ALLOWED_THROWABLE_ITEM_TYPES = new HashSet<>();
    // Danh sách các OPTION ID CẤM vứt ra đất (Ví dụ: Đồ khóa giao dịch, đồ sự kiện,...)
    private static final Set<Integer> NON_THROWABLE_OPTION_IDS = new HashSet<>();

    static {
        // Thêm các Type được phép vứt ra đất vào đây (Ví dụ minh họa, bạn sửa lại theo ý mình)
        ALLOWED_THROWABLE_ITEM_TYPES.add(0); // ao
        ALLOWED_THROWABLE_ITEM_TYPES.add(1); // quan
        ALLOWED_THROWABLE_ITEM_TYPES.add(2);  // gang
        ALLOWED_THROWABLE_ITEM_TYPES.add(3); // giay
        ALLOWED_THROWABLE_ITEM_TYPES.add(4); // rd
        ALLOWED_THROWABLE_ITEM_TYPES.add(14); // da nang cap
        ALLOWED_THROWABLE_ITEM_TYPES.add(30); // rd

        
        // Thêm các Option ID CẤM vứt ra đất vào đây
        NON_THROWABLE_OPTION_IDS.add(30);  // options khong giao dich
        NON_THROWABLE_OPTION_IDS.add(86); //
        NON_THROWABLE_OPTION_IDS.add(87); //
        NON_THROWABLE_OPTION_IDS.add(72); //
    }

    private static UseItem instance;

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(MySession session, Message msg) {
        Player player = session.player;
        if (player == null) {
            return;
        }
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG -> {
                    if (player.zone.map.mapId == 102) {
                        InventoryService.gI().itemBoxToBodyOrBag1(player, index);
                    } else {
                        InventoryService.gI().itemBoxToBodyOrBag(player, index);
                        TaskService.gI().checkDoneTaskGetItemBox(player);
                    }
                }
                case ITEM_BAG_TO_BOX -> {
                    if (player.zone.map.mapId == 102) {
                        InventoryService.gI().itemBagToBox1(player, index);
                    } else {
                        InventoryService.gI().itemBagToBox(player, index);
                    }
                }
                case ITEM_BODY_TO_BOX ->
                    InventoryService.gI().itemBodyToBox(player, index);
                case ITEM_BAG_TO_BODY -> {
                    InventoryService.gI().itemBagToBody(player, index);
                }
                case ITEM_BODY_TO_BAG -> {
                    InventoryService.gI().itemBodyToBag(player, index);
                }
                case ITEM_BAG_TO_PET_BODY ->
                    InventoryService.gI().itemBagToPetBody(player, index);
                case ITEM_BODY_PET_TO_BAG ->
                    InventoryService.gI().itemPetBodyToBag(player, index);
            }
            if (player.setClothes != null) {
                player.setClothes.setup();
            }
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            Service.gI().sendFlagBag(player);
            Service.gI().point(player);
            Service.gI().sendSpeedPlayer(player, -1);
        } catch (IOException e) {
            Logger.logException(UseItem.class, e);

        }
    }

    public Item finditem(Player player, int iditem) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == iditem) {
                return item;
            }
        }
        return null;
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg = null;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM -> {
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            if (index < 0) {
                                return;
                            }
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học "
                                            + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else if (item.template.id == 570) {
                                    if (!Util.isAfterMidnight(player.lastTimeRewardWoodChest)) {
                                        Service.gI().sendThongBao(player, "Hãy chờ đến ngày mai");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn mở\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else if (item.template.type == 22) {
                                    if (player.zone.items.stream()
                                            .filter(it -> it != null && it.itemTemplate.type == 22).count() > 2) {
                                        Service.gI().sendThongBaoOK(player, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                        return;
                                    }
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc muốn dùng\n"
                                            + player.inventory.itemsBag.get(index).template.name + " ?");
                                    player.sendMessage(msg);
                                } else {
                                    UseItem.gI().useItem(player, item, index);
                                }
                            }
                        } else {
                            int iditem = _msg.reader().readShort();
                            Item item = finditem(player, iditem);
                            UseItem.gI().useItem(player, item, index);
                        }
                    }
                }
                case DO_THROW_ITEM -> {
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (index < 0) {
                            return;
                        }
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }

                        if (item == null || !item.isNotNullItem()) {
                            return;
                        }
                        
                        if (item.template.id == 570) {
                            Service.gI().sendThongBao(player, "Không thể bỏ vật phẩm này.");
                            return;
                        }

                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        
                        if (canThrowItemToGround(item)) {
                            msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        } else {
                            msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "(mất luôn)?");
                        }
                        
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                
                case ACCEPT_THROW_ITEM -> {
                    Item item = null;
                    try {
                        if (where == 0) {
                            if (index >= 0 && index < player.inventory.itemsBody.size()) {
                                item = player.inventory.itemsBody.get(index);
                            }
                        } else {
                            if (index >= 0 && index < player.inventory.itemsBag.size()) {
                                item = player.inventory.itemsBag.get(index);
                            }
                        }
                        
                        if (item != null && item.isNotNullItem()) {
                            if (canThrowItemToGround(item)) {
                                dropItemToGround(player, item);
                            } else {

                            }
                            
                            InventoryService.gI().throwItem(player, where, index);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Service.gI().point(player);
                        InventoryService.gI().sendItemBags(player);
                    }
                }
                case ACCEPT_USE_ITEM ->
                    UseItem.gI().useItem(player, player.inventory.itemsBag.get(index), index);
            }
        } catch (IOException e) {
            Logger.logException(UseItem.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (item != null && item.isNotNullItem()) {

            if (item.template.id == 570) {
                if (!Util.isAfterMidnight(pl.lastTimeRewardWoodChest)) {
                    Service.gI().sendThongBao(pl, "Hãy chờ đến ngày mai");
                } else {
                    openRuongGo(pl);
                }
                return;
            }
            if (item.template.strRequire <= pl.nPoint.power) {
                switch (item.template.type) {
                    case 33 ->
                        UseCard(pl, item);
                    case 7 ->
                        learnSkill(pl, item);
                    case 6 ->
                        this.eatPea(pl, item);
                    case 12 ->
                        controllerCallRongThan(pl, item);
                    case 23, 24 -> {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                    }
                    case 11 -> {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                        Service.gI().sendFlagBag(pl);
                        if (pl.isPet) {
                            Service.gI().sendFlagBag(pl.pet);
                        }
                    }
                    case 27 -> {
                        // InventoryService.gI().itemBagToBody(pl, indexBag);
                        // pl.sendNewPet();
                    }
                    case 30 -> {
                        InventoryService.gI().itemBagToBody(pl, indexBag);
                    }
                    default -> {
                        switch (item.template.id) {
                            case 992:
                                pl.type = 2;
                                pl.maxTime = 5;
                                Service.gI().Transport(pl);
                                break;
                            case 361:
                                pl.idGo = (short) Util.nextInt(0, 6);
                                NgocRongNamecService.gI().menuCheckTeleNamekBall(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                InventoryService.gI().sendItemBags(pl);
                                break;
                            case 1666:
                                NpcService.gI().createMenuConMeo(pl, ConstNpc.MENU_TELE_ITEM_1666, 0,
                                        "Đã phát hiện nơi có nhiều vỏ Xên bọ hung, bạn có muốn đến đó ngay?",
                                        "Đồng ý", "Từ chối");
                                break;
                            case 211:
                            case 212:
                                eatGrapes(pl, item);
                                break;
                            case 460:
                                giveBoneToSoi(pl, item);
                                break;
                            case 456:
                                givewaterToXinBaTo(pl);
                                break;
                            case 962:
                                Gwen_C5(pl, item);
                                break;
                            case 963:
                                Gwen_C7(pl, item);
                                break;
                            case 1310:
                                if (server.Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TRUNG_THU) {
                                    NpcService.gI().createMenuConMeo(pl, consts.ConstNpc.MENU_CONFIRM_SUMMON_THO_DAI_CA,
                                            351,
                                            "Dùng Thỏ Ngọc để dụ Thỏ Đại Ca xuất hiện?",
                                            new String[] { "Đồng ý", "Từ chối" },
                                            item);
                                } else {
                                    Service.gI().sendThongBao(pl, "Sự kiện Trung thu đã kết thúc");
                                }
                                break;
                            case 1305:
                                if (server.Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TRUNG_THU) {
                                    NpcService.gI().createMenuConMeo(pl, ConstNpc.MENU_CONFIRM_SUMMON_KHIDOT, 351,
                                            "Bạn có chắc muốn dùng Ánh trăng tròn để dụ Khỉ Đột khổng lồ không ?",
                                            new String[] { "Đồng ý", "Từ chối" }, item);
                                } else {
                                    Service.gI().sendThongBao(pl, "Sự kiện Trung thu đã kết thúc");
                                }
                                break;
                            case 342:
                            case 343:
                            case 344:
                            case 345:
                                if (pl.zone.items.stream().filter(it -> it != null && it.itemTemplate.type == 22)
                                        .count() < 3) {
                                    Service.gI().dropSatellite(pl, item, pl.zone, pl.location.x, pl.location.y);
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                } else {
                                    Service.gI().sendThongBaoOK(pl, "Mỗi map chỉ đặt được 3 Vệ Tinh");
                                }
                                break;
                            case 380:
                                openCSKB(pl, item);
                                break;
                            case 1968:
                                openGoldenDragonPupEgg(pl, item);
                                break;
                            case 1773:
                                openBabyBox(pl, item);
                                break;
                            case 1440:
                                openSplChest(pl, item);
                                break;
                            case 1453:
                                openSplChestVip(pl, item);
                                break;
                            case 1910:
                                openTrungThuVipBox(pl, item);
                                break;
                            case 381:
                            case 752:
                            case 753:
                            case 382:
                            case 383:
                            case 384:
                            case 385:
                            case 379:
                            case 638:
                            case 579:
                            case 1045:
                            case 663:
                            case 664:
                            case 665:
                            case 666:
                            case 667:
                            case 764:
                            case 1150:
                            case 1151:
                            case 1152:
                            case 1153:
                            case 1154:
                            case 1233:
                            case 1532:
                            case 1628:
                            case 1635:
                            case 473:
                            case 1701:
                            case 466:
                            case 465:
                            case 1306:
                            case 1307:
                            case 1308:
                                useItemTime(pl, item);
                                break;
                            case 880:
                            case 881:
                            case 882:
                                if (pl.itemTime.isEatMeal2) {
                                    Service.gI().sendThongBao(pl, "Chỉ được sử dụng 1 cái");
                                    break;
                                }
                                useItemTime(pl, item);
                                break;
                            case 521:
                                useTDLT(pl, item);
                                break;
                            case 454:
                                UseItem.gI().usePorata(pl);
                                break;
                            case 921:
                                UseItem.gI().usePorata2(pl);
                                break;
                            case 1884:
                                UseItem.gI().usePorata3(pl);
                                break;
                            case 193:
                                openCapsuleUI(pl);
                                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            case 194:
                                openCapsuleUI(pl);
                                break;
                            case 401:
                                changePet(pl, item);
                                break;
                            case 1116:
                                openHalloweenBox2024(pl, item);
                                break;
                            case 1728:
                                UseItem.gI().openHalloweenBox(pl, item);
                                break;
                            case 402:
                            case 403:
                            case 404:
                            case 759:
                                upSkillPet(pl, item);
                                break;
                            case 726:
                                UseItem.gI().ItemManhGiay(pl, item);
                                break;
                            case 727:
                            case 728:
                                UseItem.gI().ItemSieuThanThuy(pl, item);
                                break;
                            case 1770:
                                UseItem.gI().OpenHopThanlinh(pl, item.template.id);
                                break;
                            case 2001:
                                UseItem.gI().TuiVang(pl, item);
                                break;
                            case 1999:
                                int emtyBag = 1;
                                if (InventoryService.gI().getCountEmptyBag(pl) >= emtyBag) {
                                    InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                                    short itemId = ConstItem.TrangBiKichHoat[Util.nextInt(0, 4)][pl.gender][0];
                                    Item skh = ItemService.gI().createNewItem(itemId);
                                    RewardService.gI().initChiSoItem(skh);
                                    ItemService.gI().addSetOptionsByGender(skh, pl.gender);
                                    InventoryService.gI().addItemBag(pl, skh);
                                    InventoryService.gI().sendItemBags(pl);
                                    Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + skh.template.name);
                                } else {
                                    Service.gI().sendThongBao(pl, "Hành trang cần ít nhất " + emtyBag + " ô trống");
                                }
                                break;
                            case 2002:
                                Input.gI().createFormTangRuby(pl);
                                break;
                            case 648:
                                UseItem.gI().NoelItemBox(pl, item);
                                break;
                            case 1171:
                                UseItem.gI().ChuLunBox(pl, item);
                                break;
                            case 1560:
                                if (InventoryService.gI().findItem(pl.inventory.itemsBag, 1561) != null) {
                                    UseItem.gI().RuongNgocRong(pl, item);
                                } else {
                                    Service.gI().sendThongBao(pl, "Bạn không có chía khoá vàng!");
                                }
                                break;
                            case 1170:
                                UseItem.gI().WhisItemBoxEventNoel(pl, item);
                                break;
                            case 736:
                                ItemService.gI().OpenItem736(pl, item);
                                break;
                            case 987:
                                Service.gI().sendThongBao(pl, "Bảo vệ trang bị không bị rớt cấp");
                                break;
                            case 2006:
                                Input.gI().createFormChangeNameByItem(pl);
                                break;
                            case 457:
                                Input.gI().createFormBanSLL(pl, indexBag);
                                break;
                            case 1786:
                                TayakiTrade(pl);
                                break;
                            case 1787:
                                KeoTaoTrade(pl);
                                break;
                            case 1788:
                                QueKemDoiTrade(pl);
                                break;
                            case 1860:
                                MochiTrade(pl);
                                break;
                            case 1790:
                                RauBachTuocTrade(pl);
                                break;
                            case 1791:
                                MiRaMenTrade(pl);
                                break;
                            case 1852:
                                useBinhHutNangLuong(pl);
                                break;
                            case 1890:
                                useBinhHutNangLuong2(pl);
                                break;
                            case 1900:
                                openRuongRongThan(pl, item);
                                break;
                            case 1901:
                                openRuongRongThanVip(pl, item);
                                break;
                            case 1655:
                                NpcService.gI().createMenuConMeo(pl, item.template.id,
                                        -1, "Hãy chọn 1 món quà", "Áo", "Quần", "Găng", "Giầy", "Rađa", "Đóng");
                                break;
                            case 1559:
                                ItemService.gI().OpenSKH(pl);
                                break;
                            case 1757:
                                UseItem.gI().openCadicvip(pl, item);
                                break;
                            case 1592:
                                UseItem.gI().openGokuvip(pl, item);
                                break;
                            case 1821:
                                UseItem.gI().openGoldenDragonPupEgg(pl, item);
                                break;
                            case 1809:
                                UseItem.gI().openRadanr(pl, item);
                                break;
                            case 718:
                                Input.gI().createFormTangNgocxanh(pl);
                                break;
                            case 1940:
                                useItemNearCauVangBoss(pl, item);
                                break;
                        }
                    }
                }
                TaskService.gI().checkDoneTaskUseItem(pl, item);
                InventoryService.gI().sendItemBags(pl);
            } else {
                Service.gI().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
            }
        }
    }

    // ===== KIỂM TRA ĐIỀU KIỆN VỨT VẬT PHẨM =====
    private static boolean canThrowItemToGround(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        
        // 1. KIỂM TRA TYPE: Nếu type của item KHÔNG nằm trong danh sách CHO PHÉP -> Từ chối vứt
        if (!ALLOWED_THROWABLE_ITEM_TYPES.contains((int) item.template.type)) {
            return false;
        }
        
        // Ngoại lệ đặc biệt (Ví dụ cấu hình cứng như Rương gỗ id 570 không cho vứt)
        if (item.template.id == 570) {
            return false;
        }
        
        // 2. KIỂM TRA OPTIONS: Nếu vật phẩm chứa bất kỳ option nào nằm trong danh sách CẤM -> Từ chối vứt
        if (item.itemOptions != null && !item.itemOptions.isEmpty()) {
            for (ItemOption option : item.itemOptions) {
                if (option.optionTemplate != null && NON_THROWABLE_OPTION_IDS.contains(option.optionTemplate.id)) {
                    return false; // Phát hiện option cấm vứt
                }
            }
        }
        
        return true; // Thỏa mãn các điều kiện -> Cho phép vứt
    }

    // ===== THỰC HIỆN VỨT ITEM XUỐNG BẢN ĐỒ =====
    private static void dropItemToGround(Player player, Item item) {
        try {
            // Tính toán tọa độ ngẫu nhiên xung quanh người chơi phạm vi lệch ±15 dòng X
            int dropX = player.location.x + Util.nextInt(-15, 15);
            int dropY = player.location.y;
            
            // Khởi tạo ItemMap mới trên Khu vực (Zone) hiện tại của người chơi
            ItemMap itemMap = new ItemMap(
                player.zone,
                item.template.id,
                item.quantity,
                dropX,
                dropY,
                player.id // Gắn ID người vứt làm chủ sở hữu tạm thời của item dưới đất
            );
            itemMap.playerOriginDrop = true;
            itemMap.originalDropperId = player.id;
            itemMap.auditTraceId = item.ensureAuditTraceId();
            itemMap.auditItemCreateTime = item.createTime;
            
            // Sao chép toàn bộ thuộc tính chỉ số (Options) sang item rơi dưới đất
            if (item.itemOptions != null && !item.itemOptions.isEmpty()) {
                for (ItemOption opt : item.itemOptions) {
                    if (opt.optionTemplate != null) {
                        itemMap.options.add(new ItemOption(opt.optionTemplate.id, opt.param));
                    }
                }
            }
            
            // Gọi hàm từ hệ thống để hiển thị item rơi ra bản đồ
            Service.gI().dropItemMap(player.zone, itemMap);
            AssetAuditService.gI().recordItemMap("PLAYER_DROP", itemMap, player, null,
                    "Người chơi chủ động vứt vật phẩm xuống đất");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openRuongGo(Player player) {
        Item ruongGo = InventoryService.gI().findItemBag(player, 570);
        if (ruongGo != null) {
            int level = InventoryService.gI().getParam(player, 72, 570);
            int requiredSlots = calculateRequiredEmptySlots(level);
            if (InventoryService.gI().getCountEmptyBag(player) < requiredSlots) {
                Service.gI().sendThongBao(player,
                        "Cần ít nhất " + (requiredSlots - InventoryService.gI().getCountEmptyBag(player))
                                + " ô trống trong hành trang");
            } else {
                player.itemsWoodChest.clear();
                if (level == 0) {
                    InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
                    InventoryService.gI().sendItemBags(player);
                    Item item = ItemService.gI().createNewItem((short) 190);
                    item.quantity = 1;
                    InventoryService.gI().addItemBag(player, item);
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendThongBao(player, "reward");
                    return;
                }
                int baseGoldAmount = 100 * level;
                int randomFactor = Util.nextInt(-15, 15);
                int goldAmount = baseGoldAmount + (baseGoldAmount * randomFactor / 100);
                Item itemGold = ItemService.gI().createNewItem((short) 190);
                itemGold.quantity = goldAmount * 1000;
                player.itemsWoodChest.add(itemGold);
                if (level >= 9) {
                    int quantity = 100 + (level - 9) * 20;
                    Item item77 = ItemService.gI().createNewItem((short) 77);
                    item77.quantity = quantity;
                    player.itemsWoodChest.add(item77);
                }
                int clothesCount = 1;
                if (level >= 5 && level <= 8) {
                    clothesCount = 2;
                } else if (level >= 10 && level <= 12) {
                    clothesCount = 3;
                }
                for (int i = 0; i < clothesCount; i++) {
                    int randItemId = randClothes(level);
                    Item rewardItem = ItemService.gI().createNewItem((short) randItemId);
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) randItemId);
                    if (ops != null && !ops.isEmpty()) {
                        rewardItem.itemOptions.addAll(ops);
                    }
                    rewardItem.quantity = 1;
                    player.itemsWoodChest.add(rewardItem);
                }
                int[] rewardItems = { 17, 18, 19, 20, 380, 381, 382, 383, 384, 385, 1229 };
                int rewardCount = 2;
                if (level >= 5 && level <= 8) {
                    rewardCount = 3;
                } else if (level >= 10 && level <= 12) {
                    rewardCount = 4;
                }
                Set<Integer> selectedItems = new HashSet<>();
                while (selectedItems.size() < rewardCount) {
                    int randItemId = rewardItems[Util.nextInt(0, rewardItems.length - 1)];
                    if (!selectedItems.contains(randItemId)) {
                        selectedItems.add(randItemId);
                        Item rewardItem = ItemService.gI().createNewItem((short) randItemId);
                        rewardItem.quantity = Util.nextInt(1, level);
                        player.itemsWoodChest.add(rewardItem);
                    }
                }
                int saoPhaLeCount = (level > 9) ? 2 : 1;
                for (int i = 0; i < saoPhaLeCount; i++) {
                    int rand = Util.nextInt(0, 6);
                    Item level1 = ItemService.gI().createNewItem((short) (441 + rand));
                    level1.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
                    level1.quantity = Util.nextInt(1, 3);
                    player.itemsWoodChest.add(level1);
                }
                int dncCount = (level > 9) ? 2 : 1;
                for (int i = 0; i < dncCount; i++) {
                    int rand = Util.nextInt(0, 4);
                    Item dnc = ItemService.gI().createNewItem((short) (220 + rand));
                    dnc.itemOptions.add(new Item.ItemOption(71 - rand, 0));
                    dnc.quantity = Util.nextInt(1, level * 2);
                    player.itemsWoodChest.add(dnc);
                }
                InventoryService.gI().subQuantityItemsBag(player, ruongGo, 1);
                InventoryService.gI().sendItemBags(player);
                for (Item it : player.itemsWoodChest) {
                    InventoryService.gI().addItemBag(player, it);
                }
                InventoryService.gI().sendItemBags(player);
                player.indexWoodChest = player.itemsWoodChest.size() - 1;
                int i = player.indexWoodChest;
                if (i < 0) {
                    return;
                }
                Item itemWoodChest = player.itemsWoodChest.get(i);
                player.indexWoodChest--;
                String info = "|1|" + itemWoodChest.template.name;
                if (itemWoodChest.quantity > 1) {
                    info += " (x" + itemWoodChest.quantity + ")";
                }
                String info2 = "\n|2|";
                if (!itemWoodChest.itemOptions.isEmpty()) {
                    for (Item.ItemOption io : itemWoodChest.itemOptions) {
                        if (io.optionTemplate.id != 102 && io.optionTemplate.id != 73) {
                            info2 += io.getOptionString() + "\n";
                        }
                    }
                }
                info = (info2.length() > "\n|2|".length() ? (info + info2).trim() : info.trim()) + "\n|0|"
                        + itemWoodChest.template.description;
                NpcService.gI().createMenuConMeo(player, ConstNpc.RUONG_GO, -1, "Bạn nhận được\n"
                        + info.trim(), "OK" + (i > 0 ? " [" + i + "]" : ""));
            }
        }
    }

    public int calculateRequiredEmptySlots(int level) {
        int requiredSlots = 0;
        int baseGoldAmount = 100 * level;
        int randomFactor = Util.nextInt(-15, 15);
        int goldAmount = baseGoldAmount + (baseGoldAmount * randomFactor / 100);
        if (goldAmount > 0) {
            requiredSlots++;
        }
        int clothesCount = 1;
        if (level >= 5 && level <= 8) {
            clothesCount = 2;
        } else if (level >= 10 && level <= 12) {
            clothesCount = 3;
        }
        requiredSlots += clothesCount;
        int rewardCount = 2;

        if (level >= 5 && level <= 8) {
            rewardCount = 3;
        } else if (level >= 10 && level <= 12) {
            rewardCount = 4;
        }
        requiredSlots += rewardCount;
        int saoPhaLeCount = (level > 9) ? 2 : 1;
        requiredSlots += saoPhaLeCount;
        int dncCount = (level > 9) ? 2 : 1;
        requiredSlots += dncCount;
        return requiredSlots;
    }

    private int randClothes(int level) {
        int result = level - Util.nextInt(2, 4);
        if (result < 1) {
            result = 1;
        }
        return ConstItem.LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][result];
    }

    private void changePet(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender + 1;
            if (gender > 2) {
                gender = 0;
            }
            PetService.gI().changeNormalPet(player, gender);
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.gI().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = { 76, 188, 189, 190, 381, 382, 383, 384, 385 };
            int[][] gold = { { 5000, 20000 } };
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryService.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);

            CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useItemTime(Player pl, Item item) {
        switch (item.template.id) {
            case 379 -> {
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                break;
            }

            case 381 -> {
                if (pl.itemTime.isUseCuongNo2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                Service.gI().point(pl);
                break;
            }
            case 382 -> {
                if (pl.itemTime.isUseBoHuyet2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                Service.gI().point(pl);
                break;
            }
            case 383 -> {
                if (pl.itemTime.isUseBoKhi2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                Service.gI().point(pl);
                break;
            }
            case 384 -> {
                if (pl.itemTime.isUseGiapXen2) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                Service.gI().point(pl);
                break;
            }
            case 385 -> {
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            }
            case 764 -> {
                pl.itemTime.lastTimeKhauTrang = System.currentTimeMillis();
                pl.itemTime.isUseKhauTrang = true;
                break;
            }
            case 1150 -> {
                if (pl.itemTime.isUseCuongNo) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                Service.gI().point(pl);
                break;
            }

            case 752 -> {
                pl.itemTime.lastTimeBanhChung = System.currentTimeMillis();
                pl.itemTime.isUseBanhChung = true;
                Service.gI().point(pl);
                break;
            }

            case 753 -> {
                pl.itemTime.lastTimeBanhTet = System.currentTimeMillis();
                pl.itemTime.isUseBanhTet = true;
                Service.gI().point(pl);
                break;
            }

            case 1151 -> {
                if (pl.itemTime.isUseBoKhi) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                Service.gI().point(pl);
                break;
            }

            case 1152 -> {
                if (pl.itemTime.isUseBoHuyet) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                Service.gI().point(pl);
                break;
            }

            case 1153 -> {
                if (pl.itemTime.isUseGiapXen) {
                    Service.gI().sendThongBao(pl, "Chỉ có thể sự dụng cùng lúc 1 vật phẩm bổ trợ cùng loại");
                    return;
                }
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                Service.gI().point(pl);
                break;
            }

            case 1154 -> {
                pl.itemTime.lastTimeAnDanh2 = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh2 = true;
                break;
            }

            case 638 -> {
                pl.itemTime.lastTimeUseCMS = System.currentTimeMillis();
                pl.itemTime.isUseCMS = true;
                break;
            }
            case 1233 -> {
                pl.itemTime.lastTimeUseNCD = System.currentTimeMillis();
                pl.itemTime.isUseNCD = true;
                break;
            }
            case 579, 1045 -> {
                pl.itemTime.lastTimeUseDK = System.currentTimeMillis();
                pl.itemTime.isUseDK = true;
                break;
            }
            case 663, 664, 665, 666, 667 -> {
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                break;
            }
            case 880, 881, 882 -> {
                if (pl.itemTime.isEatMeal2) {
                    Service.gI().sendThongBao(pl, "Chỉ được sử dụng 1 cái");
                    break;
                }
                pl.itemTime.lastTimeEatMeal2 = System.currentTimeMillis();
                pl.itemTime.isEatMeal2 = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal2);
                pl.itemTime.iconMeal2 = item.template.iconID;
                break;
            }
            case 1532 -> {
                pl.itemTime.lastTimeUseKhoBauX2 = System.currentTimeMillis();
                pl.itemTime.isUseKhoBauX2 = true;
                break;
            }
            case 1628 -> {
                long currentTime = System.currentTimeMillis();
                if (pl.itemTime.isUseBuaSanta) {
                    pl.itemTime.lastTimeBuaSanta += 1_800_000;
                } else {
                    pl.itemTime.lastTimeBuaSanta = currentTime + 1;
                    pl.itemTime.isUseBuaSanta = true;
                    break;
                }
            }
            case 1635 -> {
                long currentTime = System.currentTimeMillis();
                if (pl.itemTime.isUseCoBonLa) {
                    pl.itemTime.lastTimeCoBonLa += 1_800_000;
                } else {
                    pl.itemTime.lastTimeCoBonLa = currentTime + 1;
                    pl.itemTime.isUseCoBonLa = true;
                    Service.gI().point(pl);
                    break;
                }
            }
            case 473 -> {
                pl.itemTime.lastTimeBanhTrungThu = System.currentTimeMillis();
                pl.itemTime.isUseBanhTrungThu = true;
                Service.gI().point(pl);
                break;
            }
            case 1701 -> {
                pl.itemTime.lastTimeBanhTrungThuDacBiet = System.currentTimeMillis();
                pl.itemTime.isUseBanhTrungThuDacBiet = true;
                Service.gI().point(pl);
                break;
            }
            case 466 -> {
                pl.itemTime.lastTimeBanhTrungThu2Trung = System.currentTimeMillis();
                pl.itemTime.isUseBanhTrungThu2Trung = true;
                Service.gI().point(pl);
                break;
            }
            case 465 -> {
                pl.itemTime.lastTimeBanhTrungThu1Trung = System.currentTimeMillis();
                pl.itemTime.isUseBanhTrungThu1Trung = true;
                Service.gI().point(pl);
                break;
            }
            case 1306 -> {
                pl.itemTime.lastTimeBanhDeoThoTrang = System.currentTimeMillis();
                pl.itemTime.isUseBanhDeoThoTrang = true;
                Service.gI().point(pl);
                break;
            }
            case 1307 -> {
                pl.itemTime.lastTimeBanhDeoThoXanh = System.currentTimeMillis();
                pl.itemTime.isUseBanhDeoThoXanh = true;
                Service.gI().point(pl);
                break;
            }
            case 1308 -> {
                pl.itemTime.lastTimeBanhDeoThoHong = System.currentTimeMillis();
                pl.itemTime.isUseBanhDeoThoHong = true;
                Service.gI().point(pl);
                break;
            }
        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO, SummonDragon.NGOC_RONG_2_SAO, SummonDragon.NGOC_RONG_3_SAO ->
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13));
                default ->
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON,
                            -1, "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
            }
        } else if (tempId >= SummonDragonBonney.NGOC_RONG_1_SAO) {
            SummonDragonBonney.gI().openMenuSummonShenron(pl, 0);
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                // Validate skill trước khi học
                int skillTemplateId = SkillUtil.getTempSkillSkillByItemID(item.template.id);
                if (skillTemplateId == -1) {
                    Service.gI().sendThongBao(pl, "Skill không hợp lệ!");
                    return;
                }

                // Kiểm tra skill có thuộc gender của player không
                if (!SkillUtil.validateSkillBeforeLearn(pl, skillTemplateId)) {
                    Service.gI().sendThongBao(pl, "Skill này không phù hợp với hành tinh của bạn!");
                    Logger.warning("Player " + pl.name + " (gender: " + pl.gender
                            + ") cố gắng học skill không hợp lệ: " + skillTemplateId);
                    return;
                }

                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Skill skillNeed = SkillUtil
                                    .createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl,
                                    "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id),
                                    level);
                            pl.BoughtSkill.add((int) item.template.id);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp "
                                    + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryService.gI().sendItemBags(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (IOException | NumberFormatException e) {
            Logger.logException(UseItem.class, e);
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata3(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion3(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void openCapsuleUI(Player pl) {
        pl.idMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {

        if (pl.idNRNM != -1) {
            Service.gI().sendThongBao(pl, "Không thể mang ngọc rồng này lên Phi thuyền");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        int zoneId = -1;
        if (index > pl.mapCapsule.size() - 1 || index < 0) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            Service.gI().hideWaitDialog(pl);
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        if (zoneChose.getNumOfPlayers() > 25
                || MapService.gI().isMapDoanhTrai(zoneChose.map.mapId)
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        pl.changeMapVIP = true;
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    public void eatPea(Player player) {
        if (!Util.canDoWithTime(player.lastTimeEatPea, 10000)) {
            return;
        }
        player.lastTimeEatPea = System.currentTimeMillis();
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            long hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (Item.ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc);
            player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc);
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += statima;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc);
                player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc);
                Service.gI().sendInfoPlayerEatPea(player.pet);
                Service.gI().chatJustForMe(player, player.pet, "Cám ơn sư phụ");
            }

            InventoryService.gI().subQuantityItemsBag(player, pea, 1);
            InventoryService.gI().sendItemBags(player);
        }
    }

    /**
     * Uses the exact bean stack selected by the player instead of searching for
     * the first type-6 item in the bag.
     */
    public void eatPea(Player player, Item pea) {
        if (!Util.canDoWithTime(player.lastTimeEatPea, 10000)) {
            return;
        }
        if (pea == null || !pea.isNotNullItem() || pea.template.type != 6 || pea.quantity <= 0) {
            return;
        }

        player.lastTimeEatPea = System.currentTimeMillis();
        long hpKiHoiPhuc = 0;
        int lvPea = getPeaLevel(pea.template.id);
        for (Item.ItemOption io : pea.itemOptions) {
            if (io.optionTemplate.id == 2) {
                hpKiHoiPhuc = io.param * 1000L;
                break;
            }
            if (io.optionTemplate.id == 48) {
                hpKiHoiPhuc = io.param;
                break;
            }
        }

        player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc);
        player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc);
        PlayerService.gI().sendInfoHpMp(player);
        Service.gI().sendInfoPlayerEatPea(player);
        if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
            int stamina = 100 * lvPea;
            player.pet.nPoint.stamina += stamina;
            if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
            }
            player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc);
            player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc);
            Service.gI().sendInfoPlayerEatPea(player.pet);
            Service.gI().chatJustForMe(player, player.pet, "Cảm ơn sư phụ");
        }

        InventoryService.gI().subQuantityItemsBag(player, pea, 1);
        InventoryService.gI().sendItemBags(player);
    }

    private int getPeaLevel(int itemTemplateId) {
        return switch (itemTemplateId) {
            case ConstItem.DAU_THAN_CAP_1 -> 1;
            case ConstItem.DAU_THAN_CAP_2 -> 2;
            case ConstItem.DAU_THAN_CAP_3 -> 3;
            case ConstItem.DAU_THAN_CAP_4 -> 4;
            case ConstItem.DAU_THAN_CAP_5 -> 5;
            case ConstItem.DAU_THAN_CAP_6 -> 6;
            case ConstItem.DAU_THAN_CAP_7 -> 7;
            case ConstItem.DAU_THAN_CAP_8 -> 8;
            case ConstItem.DAU_THAN_CAP_9 -> 9;
            case ConstItem.DAU_THAN_CAP_10 -> 10;
            case ConstItem.DAU_THAN_CAP_11 -> 11;
            default -> 1;
        };
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }
                case 403 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }
                case 404 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }
                case 759 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cám ơn sư phụ");
                        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }

            }

        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    public void OpenHopThanlinh(Player player, int itemUseiD) {
        if (InventoryService.gI().getCountEmptyBag(player) > 4) {
            Item itemused = InventoryService.gI().findItemBag(player, itemUseiD);
            int[][] items = { { 555, 556, 562, 563, 561 }, { 557, 558, 564, 565, 561 }, { 559, 560, 566, 567, 561 } };
            Item aotl = ItemService.gI().createNewItem((short) items[player.gender][0]);
            Item wTl = ItemService.gI().createNewItem((short) items[player.gender][1]);
            Item gTl = ItemService.gI().createNewItem((short) items[player.gender][2]);
            Item jayTl = ItemService.gI().createNewItem((short) items[player.gender][3]);
            Item RdTl = ItemService.gI().createNewItem((short) items[player.gender][4]);
            RewardService.gI().initChiSoItem(aotl);
            RewardService.gI().initChiSoItem(wTl);
            RewardService.gI().initChiSoItem(gTl);
            RewardService.gI().initChiSoItem(jayTl);
            RewardService.gI().initChiSoItem(RdTl);
            aotl.itemOptions.add(new ItemOption(30, 1));
            wTl.itemOptions.add(new ItemOption(30, 1));
            gTl.itemOptions.add(new ItemOption(30, 1));
            jayTl.itemOptions.add(new ItemOption(30, 1));
            RdTl.itemOptions.add(new ItemOption(30, 1));
            InventoryService.gI().addItemBag(player, aotl);
            InventoryService.gI().addItemBag(player, wTl);
            InventoryService.gI().addItemBag(player, gTl);
            InventoryService.gI().addItemBag(player, jayTl);
            InventoryService.gI().addItemBag(player, RdTl);
            InventoryService.gI().subQuantityItemsBag(player, itemused, 1);
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Bạn vừa nhận được Set thần linh");
        } else {
            Service.gI().sendThongBao(player, "Yêu cầu có 5 ô trống hành trang");
        }
    }

    private void ItemManhGiay(Player pl, Item item) {
        if (pl.winSTT && !Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            Service.gI().sendThongBao(pl, "Hãy gặp thần mèo Karin để sử dụng");
            return;
        } else if (pl.winSTT && Util.isAfterMidnight(pl.lastTimeWinSTT)) {
            pl.winSTT = false;
            pl.callBossPocolo = false;
            pl.zoneSieuThanhThuy = null;
        }
        NpcService.gI().createMenuConMeo(pl, item.template.id, 564,
                "Đây chính là dấu hiệu riêng của...\nĐại Ma Vương Pôcôlô\n"
                        + "Đó là một tên quỷ dữ đội lốt người, một kẻ đại gian ác\n"
                        + "có sức mạnh vô địch và lòng tham không đáy...\n"
                        + "Đối phó với hắn không phải dễ\n"
                        + "Con có chắc chắn muốn tìm hắn không?",
                "Đồng ý", "Từ chối");
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
    }

    private void giveBoneToSoi(Player pl, Item item) {
        List<Player> bossPlayers = pl.zone.getBosses();
        synchronized (bossPlayers) {
            for (Player bossPlayer : bossPlayers) {
                if (!(bossPlayer instanceof SoiHecQuyn)) {
                    continue;
                }
                SoiHecQuyn boss = (SoiHecQuyn) bossPlayer;
                if (boss.hasPickedBone()) {
                    Service.gI().sendThongBao(pl, "Sói đã no rồi");
                    continue;
                }
                boss.pickBone();
                int x = pl.location.x;
                if (x < 0 || x >= pl.zone.map.mapWidth) {
                    return;
                }
                int y = pl.zone.map.yPhysicInTop(x, pl.location.y - 24);
                ItemMap boneMap = new ItemMap(pl.zone, 460, 1, x, y, pl.id);
                boneMap.isPickedUp = true;
                boneMap.createTime -= 23000;
                Service.gI().dropItemMap(pl.zone, boneMap);
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().sendItemBags(pl);

                if (Util.nextInt(4) < 3) {
                    int rand = Util.nextInt(0, 6);
                    short idItem = (short) (441 + rand);
                    Item reward = ItemService.gI().createNewItem(idItem);
                    reward.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
                    if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                        InventoryService.gI().addItemBag(pl, reward);
                        Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + reward.template.name);
                    } else {
                        Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống.");
                    }
                } else {
                    short idItem = 459;
                    Item reward = ItemService.gI().createNewItem(idItem);
                    reward.itemOptions.add(new Item.ItemOption(112, 80));
                    reward.itemOptions.add(new Item.ItemOption(93, 90));
                    reward.itemOptions.add(new Item.ItemOption(20, Util.nextInt(10000)));
                    if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
                        InventoryService.gI().addItemBag(pl, reward);
                        Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + reward.template.name);
                    } else {
                        Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống.");
                    }
                }
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        ItemMapService.gI().removeItemMapAndSendClient(boneMap);
                        if (!boss.isDie()) {
                            boss.leaveMapNew();
                        }
                    } catch (InterruptedException e) {
                        Logger.error("Error delaying cleanup bone: " + e.getMessage());
                    }
                }).start();

                break;
            }
            InventoryService.gI().sendItemBags(pl);
            BadgesTaskService.updateCountBagesTask(pl, ConstTaskBadges.KE_THAO_TUNG_SOI, 1);
        }
    }

    private void givewaterToXinBaTo(Player pl) {
        XinBaTo xinBaTo = (XinBaTo) pl.zone.getBosses().stream()
                .filter(boss -> boss instanceof XinBaTo)
                .findFirst()
                .orElse(null);
        if (xinBaTo == null) {
            Service.gI().sendThongBao(pl, "Xin Ba To hiện không xuất hiện ở khu vực này.");
            return;
        }
        if (xinBaTo.hasDrunkWater()) {
            Service.gI().sendThongBao(pl, "Xin Ba To đã uống nước rồi.");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đầy! Vui lòng dọn chỗ trống trước khi cho Xin Ba To uống nước.");
            return;
        }
        int totalWater = pl.inventory.itemsBag.stream()
                .filter(item -> item != null && item.template != null && item.template.id == 456)
                .mapToInt(item -> item.quantity)
                .sum();

        if (totalWater < 99) {
            Service.gI().sendThongBao(pl, "Bạn cần ít nhất " + 99 + " bình nước để cho Xin Ba To uống. (Hiện có: "
                    + totalWater + "/" + 99 + ")");
            return;
        }
        int needToRemove = 99;
        for (Item item : pl.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id == 456) {
                int quantityToRemove = Math.min(needToRemove, item.quantity);
                InventoryService.gI().subQuantityItemsBag(pl, item, quantityToRemove);
                needToRemove -= quantityToRemove;

                if (needToRemove <= 0) {
                    break;
                }
            }
        }
        InventoryService.gI().sendItemBags(pl);
        xinBaTo.drinkWater();
        Item reward;
        if (Util.nextInt(4) < 3) {
            int rand = Util.nextInt(0, 6);
            short idItem = (short) (441 + rand);
            reward = ItemService.gI().createNewItem(idItem);
            reward.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
        } else {
            reward = ItemService.gI().createNewItem((short) 456);
            reward.itemOptions.add(new Item.ItemOption(112, 80));
            reward.itemOptions.add(new Item.ItemOption(93, 90));
            reward.itemOptions.add(new Item.ItemOption(20, Util.nextInt(10000)));
        }

        InventoryService.gI().addItemBag(pl, reward);
        Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + reward.template.name);
        BadgesTaskService.updateCountBagesTask(pl, ConstTaskBadges.NUOC_ANH_BAO, 1);
    }

    private void useItemNearCauVangBoss(Player pl, Item item) {
        List<Player> bossPlayers = pl.zone.getBosses();
        synchronized (bossPlayers) {
            for (Player bossPlayer : bossPlayers) {
                Boss cauVangBoss = (Boss) bossPlayer;
                if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
                    Service.gI().sendThongBao(pl, "Hành trang của bạn đã đầy.");
                    return;
                }
                short[] icon = new short[2];
                icon[0] = item.template.iconID;
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                if (Util.nextInt(100) < 20) {
                    Item rewardItem = ItemService.gI().createNewItem((short) 1935);

                    rewardItem.itemOptions.add(new Item.ItemOption(50, 11));
                    rewardItem.itemOptions.add(new Item.ItemOption(77, 11));
                    rewardItem.itemOptions.add(new Item.ItemOption(103, 25));
                    rewardItem.itemOptions.add(new Item.ItemOption(148, 75));

                    if (Util.nextInt(0, 9) != 0) {
                        int[] expiryChoices = { 3, 5, 7, 15, 30 };
                        int expiry = expiryChoices[Util.nextInt(0, expiryChoices.length - 1)];
                        rewardItem.itemOptions.add(new Item.ItemOption(93, expiry));
                    }
                    InventoryService.gI().addItemBag(pl, rewardItem);

                    int quantity1736 = Util.nextInt(2, 4);
                    Item additionalReward = ItemService.gI().createNewItem((short) 1736);
                    additionalReward.quantity = quantity1736;
                    InventoryService.gI().addItemBag(pl, additionalReward);

                    Service.gI().sendThongBao(pl, "Bạn nhận được Pet cậu vàng");
                    Service.gI().sendThongBao(pl,
                            "Bạn nhận được " + quantity1736 + "x " + additionalReward.template.name);

                    icon[1] = rewardItem.template.iconID;
                    cauVangBoss.chat("Éc Éc");
                    if (!cauVangBoss.isDie()) {
                        cauVangBoss.leaveMap();
                    }
                } else {
                    icon[1] = -1;
                    cauVangBoss.chat("Xí hụt");
                }
                CombineService.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
                InventoryService.gI().sendItemBags(pl);

                break;
            }
        }
    }

    public void NoelItemBox(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int spl = Util.nextInt(441, 445);
            int dnc = Util.nextInt(381, 384);
            int nr = Util.nextInt(17, 20);
            int nrBang = Util.nextInt(925, 931);
            int mts = Util.nextInt(1066, 1070);

            if (Util.isTrue(5, 90)) {
                int ruby = Util.nextInt(10, 20);
                pl.inventory.gem += ruby;
                PlayerService.gI().sendInfoHpMpMoney(pl);
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Bạn nhận được " + ruby + "  Ngọc");
            } else {
                int[] temp = { spl, dnc, nr, nrBang, mts, 533, 380 };
                byte index = (byte) Util.nextInt(0, temp.length - 1);
                short[] icon = new short[2];
                icon[0] = item.template.iconID;
                Item it = ItemService.gI().createNewItem((short) temp[index]);
                if (temp[index] >= 441 && temp[index] <= 443) {// sao pha le
                    it.itemOptions.add(new ItemOption(temp[index] - 346, 5));
                    it.quantity = Util.nextInt(1, 5);
                } else if (temp[index] >= 444 && temp[index] <= 445) {
                    it.itemOptions.add(new ItemOption(temp[index] - 346, 3));
                    it.quantity = Util.nextInt(1, 5);
                } else if (temp[index] >= 381 && temp[index] <= 384) {
                    it.quantity = Util.nextInt(1, 5);
                } else if (temp[index] >= 1066 && temp[index] <= 1070) {
                    it.quantity = Util.nextInt(1, 5);
                } else if (temp[index] >= 387 && temp[index] <= 393) {
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(30, 40)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(30, 40)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(30, 40)));
                    it.itemOptions.add(new ItemOption(80, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                    it.itemOptions.add(new ItemOption(199, 0));
                } else if (temp[index] == 936) { // tuan loc
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(5, 10)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(5, 10)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(5, 10)));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                } else if (temp[index] == 822) {
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(3, 30)));
                    it.itemOptions.add(new ItemOption(30, 0));
                    it.itemOptions.add(new ItemOption(74, 0));
                } else if (temp[index] == 746) {
                    it.itemOptions.add(new ItemOption(74, 0));
                    it.itemOptions.add(new ItemOption(30, 0));
                    if (Util.isTrue(99, 100)) {
                        it.itemOptions.add(new ItemOption(93, Util.nextInt(30, 360)));
                    }
                } else if (temp[index] == 821) {
                    it.itemOptions.add(new ItemOption(30, 0));
                } else {
                    it.itemOptions.add(new ItemOption(73, 0));
                }
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                icon[1] = it.template.iconID;
                InventoryService.gI().addItemBag(pl, it);
                InventoryService.gI().sendItemBags(pl);
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void HopQuaChinhChu(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int tst = Util.nextInt(637, 642);
            int itc1 = Util.nextInt(1150, 1154);
            {
                int[] temp = { tst, itc1 };
                byte index = (byte) Util.nextInt(0, temp.length - 1);
                short[] icon = new short[2];
                icon[0] = item.template.iconID;
                Item it = ItemService.gI().createNewItem((short) temp[index]);
                if (temp[index] == 1503 && temp[index] == 1504) {
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                } else if (temp[index] == 681) {
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                icon[1] = it.template.iconID;
                InventoryService.gI().addItemBag(pl, it);
                InventoryService.gI().sendItemBags(pl);
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void HopQuaNheNhang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int tst = Util.nextInt(381, 385);
            int itc1 = Util.nextInt(628, 636);
            {
                int[] temp = { tst, itc1 };
                byte index = (byte) Util.nextInt(0, temp.length - 1);
                short[] icon = new short[2];
                icon[0] = item.template.iconID;
                Item it = ItemService.gI().createNewItem((short) temp[index]);
                if (temp[index] == 1503 && temp[index] == 1504) {
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                } else if (temp[index] == 681) { // tuan loc
                    it.itemOptions.add(new ItemOption(50, Util.nextInt(19, 25)));
                    it.itemOptions.add(new ItemOption(77, Util.nextInt(18, 25)));
                    it.itemOptions.add(new ItemOption(103, Util.nextInt(17, 25)));
                    it.itemOptions.add(new ItemOption(47, Util.nextInt(13, 12)));
                    it.itemOptions.add(new ItemOption(106, 0));
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
                }
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                icon[1] = it.template.iconID;
                InventoryService.gI().addItemBag(pl, it);
                InventoryService.gI().sendItemBags(pl);
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void WhisItemBoxEventNoel(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int hanhtinh = pl.gender;
            int[] caitrangdietquy = { 1087, 1088, 1089, 1090, 1091 };

            int itemwhis = Util.nextInt(0, 3) == 0 ? 746
                    : // 1/3%
                    Util.nextInt(0, 3) == 1 ? (hanhtinh == 0 ? 1155 : hanhtinh == 1 ? 1157 : 1156)
                            : // Cải trang Noel
                            Util.nextInt(0, 3) == 2 ? (hanhtinh == 0 ? 1018 : hanhtinh == 1 ? 1019 : 1020)
                                    : // Cải trang Broly
                                    caitrangdietquy[Util.nextInt(0, caitrangdietquy.length - 1)]; // Cải Trang Diệt Wỹ

            Item it = ItemService.gI().createNewItem((short) itemwhis);
            if (itemwhis == 746) { // Xe trượt tuyết
                if (Util.isTrue(1, 100)) {
                    it.itemOptions.add(new ItemOption(73, 0));
                } else {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(7, 30)));
                }
            } else if (itemwhis == 1155 || itemwhis == 1156 || itemwhis == 1157
                    || itemwhis == 1018 || itemwhis == 1019 || itemwhis == 1020) {
                it.itemOptions.add(new ItemOption(50, 23));
                it.itemOptions.add(new ItemOption(77, 23));
                it.itemOptions.add(new ItemOption(103, 23));
                if (Util.isTrue(95, 100)) {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                } else {
                    it.itemOptions.add(new ItemOption(73, 0));
                }
            } else if (itemwhis >= 1087 && itemwhis <= 1191) {
                it.itemOptions.add(new ItemOption(50, 22));
                it.itemOptions.add(new ItemOption(77, 21));
                it.itemOptions.add(new ItemOption(103, 21));
                if (Util.isTrue(95, 100)) {
                    it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
                } else {
                    it.itemOptions.add(new ItemOption(73, 0));
                }
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void Gwen_C5(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int[] itemList = { 1087, 1088, 1089, 1090, 1091 };
            int itemwhis = itemList[Util.nextInt(0, itemList.length - 1)];
            Item it = ItemService.gI().createNewItem((short) itemwhis);
            it.itemOptions.add(new ItemOption(50, Util.nextInt(1, 16)));
            it.itemOptions.add(new ItemOption(77, Util.nextInt(1, 17)));
            it.itemOptions.add(new ItemOption(103, Util.nextInt(1, 15)));
            it.itemOptions.add(new ItemOption(95, Util.nextInt(1, 5)));
            it.itemOptions.add(new ItemOption(96, Util.nextInt(1, 5)));

            int[] options = { 94, 97, 108 };
            int randomOption = options[Util.nextInt(0, options.length - 1)];
            it.itemOptions.add(new ItemOption(randomOption, Util.nextInt(3, 5)));
            if (Util.isTrue(98, 100)) {
                it.itemOptions.add(new ItemOption(93, 5));
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void Gwen_C7(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int[] itemList = { 1087, 1088, 1089, 1090, 1091 };
            int itemwhis = itemList[Util.nextInt(0, itemList.length - 1)];
            Item it = ItemService.gI().createNewItem((short) itemwhis);
            it.itemOptions.add(new ItemOption(50, Util.nextInt(1, 16)));
            it.itemOptions.add(new ItemOption(77, Util.nextInt(1, 17)));
            it.itemOptions.add(new ItemOption(103, Util.nextInt(1, 15)));
            it.itemOptions.add(new ItemOption(95, Util.nextInt(1, 5)));
            it.itemOptions.add(new ItemOption(96, Util.nextInt(1, 5)));
            int[] options = { 94, 97, 108 };
            int randomOption = options[Util.nextInt(0, options.length - 1)];
            it.itemOptions.add(new ItemOption(randomOption, Util.nextInt(3, 5)));
            if (Util.isTrue(100, 100)) {
                it.itemOptions.add(new ItemOption(93, 7));
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void ChuLunBox(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int[] itemList = { 1158, 1159, 1160, 1161, 1162, 1163, 1164 };
            int itemwhis = itemList[Util.nextInt(0, itemList.length - 1)];
            Item it = ItemService.gI().createNewItem((short) itemwhis);
            it.itemOptions.add(new ItemOption(50, 11));
            it.itemOptions.add(new ItemOption(77, 13));
            it.itemOptions.add(new ItemOption(103, 13));
            int[] options = { 94, 97, 108 };
            int randomOption = options[Util.nextInt(0, options.length - 1)];
            it.itemOptions.add(new ItemOption(randomOption, Util.nextInt(3, 5)));
            if (Util.isTrue(98, 100)) {
                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
            } else {
                it.itemOptions.add(new ItemOption(73, 0));
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void NonNoelDo(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int hanhtinh = pl.gender;
            int itemwhis = (hanhtinh == 0 ? 387 : hanhtinh == 1 ? 390 : 393);
            Item it = ItemService.gI().createNewItem((short) itemwhis);
            it.itemOptions.add(new ItemOption(50, Util.nextInt(10, 20)));
            it.itemOptions.add(new ItemOption(80, Util.nextInt(30, 50)));
            it.itemOptions.add(new ItemOption(103, Util.nextInt(10, 20)));
            it.itemOptions.add(new ItemOption(106, 1));
            if (Util.isTrue(95, 100)) {
                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 3)));
            } else {
                it.itemOptions.add(new ItemOption(73, 0));
            }
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void CapsuleKichHoat(Player player) {
        Item item = InventoryService.gI().findItem(player.inventory.itemsBag, 1559);
        if (item == null || item.quantity < 1) {
            return;
        }

        int emtyBag = 1;
        if (InventoryService.gI().getCountEmptyBag(player) >= emtyBag) {
            InventoryService.gI().subQuantityItemsBag(player, item, 1);
            int[] ID_Option;
            ID_Option = switch (player.gender) {
                case 0 ->
                    new int[] { 127, 128, 129 };
                case 1 ->
                    new int[] { 130, 131, 132 };
                default ->
                    new int[] { 133, 134, 135 };
            };
            int ID_TrangBi = ID_Option[Util.nextInt(0, ID_Option.length - 1)];
            short itemId = ConstItem.TrangBiKichHoat[Util.nextInt(0, 4)][player.gender][0];
            Item ID_SetKichHoat = ItemService.gI().createNewItem(itemId);
            RewardService.gI().initChiSoItem(ID_SetKichHoat);
            ItemService.gI().Gwen_Option(ID_SetKichHoat, ID_TrangBi);

            if (ID_SetKichHoat != null) {
                InventoryService.gI().addItemBag(player, ID_SetKichHoat);
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Bạn vừa nhận được " + ID_SetKichHoat.template.name);
            }
        } else {
            Service.gI().sendThongBao(player, "Hành trang cần ít nhất " + emtyBag + " ô trống");
        }
    }

    public void TuiVang(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            pl.inventory.gold += Util.nextInt(100000, 10000000);
            int itemwhis = 190;
            Item it = ItemService.gI().createNewItem((short) itemwhis);
            int randomValue = Util.nextInt(3, 333);
            it.itemOptions.add(new ItemOption(1, randomValue));
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().addItemBag(pl, it);
            InventoryService.gI().sendItemBags(pl);
            Service.gI().sendThongBao(pl, "Happy");
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void ItemSieuThanThuy(Player pl, Item item) {
        long tnsm = 5_000_000;
        int n = 0;
        switch (item.template.id) {
            case 727 ->
                n = 2;
            case 728 ->
                n = 10;
        }
        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().sendItemBags(pl);
        if (Util.isTrue(90, 100)) {
            Service.gI().sendThongBao(pl, "Bạn đã bị chết vì độc của thuốc tăng lực siêu thần thủy.");
            pl.setDie();
        } else {
            for (int i = 0; i < n; i++) {
                Service.gI().addSMTN(pl, (byte) 2, tnsm, true);
            }
        }
    }

    public void UseCard(Player pl, Item item) {
        RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(c -> c.Id == item.template.id)
                .findFirst().orElse(null);
        if (radarTemplate == null) {
            return;
        }
        if (radarTemplate.Require != -1) {
            RadarCard radarRequireTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                    .filter(r -> r.Id == radarTemplate.Require).findFirst().orElse(null);
            if (radarRequireTemplate == null) {
                return;
            }
            Card cardRequire = pl.Cards.stream().filter(r -> r.Id == radarRequireTemplate.Id).findFirst().orElse(null);
            if (cardRequire == null || cardRequire.Level < radarTemplate.RequireLevel) {
                Service.gI().sendThongBao(pl, "Bạn cần sưu tầm " + radarRequireTemplate.Name + " ở cấp độ "
                        + radarTemplate.RequireLevel + " mới có thể sử dụng thẻ này");
                return;
            }
        }
        Card card = pl.Cards.stream().filter(r -> r.Id == item.template.id).findFirst().orElse(null);
        if (card == null) {
            Card newCard = new Card(item.template.id, (byte) 1, radarTemplate.Max, (byte) -1, radarTemplate.Options);
            pl.Cards.add(newCard);
            RadarService.gI().RadarSetAmount(pl, newCard.Id, newCard.Amount, newCard.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, newCard.Id, newCard.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        } else {
            if (card.Level >= 3) {
                Service.gI().sendThongBao(pl, "Thẻ này đã đạt cấp tối đa");
                return;
            }
            card.Amount++;
            if (card.Amount >= card.MaxAmount) {
                card.Amount = 0;
                if (card.Level == -1) {
                    card.Level = 1;
                } else {
                    card.Level++;
                }
                Service.gI().point(pl);
                pl.updateAura();
            }
            RadarService.gI().RadarSetAmount(pl, card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
            InventoryService.gI().subQuantityItemsBag(pl, item, 1);
            InventoryService.gI().sendItemBags(pl);
        }
    }

    private void RuongNgocRong(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) > 0) {
            int random = Util.nextInt(0, 100);
            int itemwhis;
            if (random < 85) {
                int[] itemList = { 20, 19, 18, 17 };
                itemwhis = itemList[Util.nextInt(0, itemList.length - 1)];
            } else if (random < 95) {
                itemwhis = 16;
            } else {
                itemwhis = Util.nextInt(14, 15);
            }
            Item it = ItemService.gI().createNewItem((short) itemwhis);
            Item item1561 = InventoryService.gI().findItem(pl.inventory.itemsBag, 1561);
            if (item1561 != null) {
                InventoryService.gI().subQuantityItemsBag(pl, item, 1);
                InventoryService.gI().subQuantityItemsBag(pl, item1561, 1);
                InventoryService.gI().addItemBag(pl, it);
                InventoryService.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Bạn vừa nhận được " + it.template.name);
            } else {
                Service.gI().sendThongBao(pl, "Bạn không có chìa khoá vàng");
            }
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void openGoldenDragonPupEgg(Player pl, Item eggItem) {
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] dragonPupIds = {
                (short) 1813, (short) 1814, (short) 1815, (short) 1816, (short) 1817, (short) 1818, (short) 1819
        };
        int[] rates = { 2, 5, 8, 12, 18, 25, 30 };
        int totalRate = 0;
        for (int rate : rates) {
            totalRate += rate;
        }
        int randomPick = Util.nextInt(1, totalRate);
        short selectedPupId = (short) 1961;
        int cumulativeRate = 0;
        for (int i = 0; i < rates.length; i++) {
            cumulativeRate += rates[i];
            if (randomPick <= cumulativeRate) {
                selectedPupId = dragonPupIds[i];
                break;
            }
        }
        Item pet = ItemService.gI().createNewItem(selectedPupId);
        pet.itemOptions.add(new ItemOption(30, 0));
        int randDurationType = Util.nextInt(1, 100);
        int durationInDays = 0;
        boolean isPermanent = false;
        if (randDurationType <= 3) {
            isPermanent = true;
            ServerNotify.gI().notify(pl.name + " đã may mắn mở Trứng Vàng Rồng Nhí nhận được "
                    + pet.template.name + " hạn dùng vĩnh viễn!");
        } else if (randDurationType <= 37) {
            durationInDays = 30;
        } else {
            durationInDays = 15;
        }
        if (!isPermanent) {
            pet.itemOptions.add(new ItemOption(93, durationInDays)); // ID 93: HSD # ngày
        }
        switch (selectedPupId) {
            case 1813 -> {
                pet.itemOptions.add(new Item.ItemOption(77, 22)); // hp
                pet.itemOptions.add(new Item.ItemOption(50, 22)); // sd
                pet.itemOptions.add(new Item.ItemOption(94, 8)); // giảm st
                pet.itemOptions.add(new Item.ItemOption(5, 11)); // sdcm
                pet.itemOptions.add(new Item.ItemOption(14, 8)); // cm
                pet.itemOptions.add(new Item.ItemOption(106, 0)); // chống lạnh
            }
            case 1814 -> {
                pet.itemOptions.add(new Item.ItemOption(77, 22)); // hp
                pet.itemOptions.add(new Item.ItemOption(50, 22)); // sd
                pet.itemOptions.add(new Item.ItemOption(94, 8)); // giảm st
                pet.itemOptions.add(new Item.ItemOption(5, 11)); // sdcm
                pet.itemOptions.add(new Item.ItemOption(14, 8)); // cm
            }
            case 1815 -> {
                pet.itemOptions.add(new Item.ItemOption(77, 18)); // hp
                pet.itemOptions.add(new Item.ItemOption(94, 5)); // gst
                pet.itemOptions.add(new Item.ItemOption(108, 7)); // né đòn
            }
            case 1816 -> {
                pet.itemOptions.add(new Item.ItemOption(77, 18)); // hp
                pet.itemOptions.add(new Item.ItemOption(5, 7)); // sdcm
                pet.itemOptions.add(new Item.ItemOption(14, 5)); // cm
            }
            case 1817 -> {
                pet.itemOptions.add(new Item.ItemOption(50, 18)); // Sức đánh
                pet.itemOptions.add(new Item.ItemOption(94, 5)); // gst
                pet.itemOptions.add(new Item.ItemOption(108, 7)); // né đòn
            }
            case 1818 -> {
                pet.itemOptions.add(new Item.ItemOption(50, 18)); // Sức đánh
                pet.itemOptions.add(new Item.ItemOption(5, 7)); // sdcm
                pet.itemOptions.add(new Item.ItemOption(14, 5)); // cm Chống lạnh
            }
            case 1819 -> {
                pet.itemOptions.add(new Item.ItemOption(77, 16)); // hp
                pet.itemOptions.add(new Item.ItemOption(50, 16)); // sd
                pet.itemOptions.add(new Item.ItemOption(103, 16)); // KI
                pet.itemOptions.add(new Item.ItemOption(236, 20)); // may mắn
            }
        }
        InventoryService.gI().subQuantityItemsBag(pl, eggItem, 1);
        InventoryService.gI().addItemBag(pl, pet);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);

        String hsdString = isPermanent ? "(Vĩnh viễn)" : "(HSD: " + durationInDays + " ngày)";
        Service.gI().sendThongBao(pl, "Bạn nhận được " + pet.template.name + " " + hsdString);
        CombineService.gI().sendEffectOpenItem(pl, eggItem.template.iconID, pet.template.iconID);
    }

    public void openBabyBox(Player pl, Item boxItem) {
        if (boxItem == null || boxItem.template.id != 1773) {
            Service.gI().sendThongBao(pl, "Vật phẩm không hợp lệ.");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] babyPetIds = { 1775, 1778, 1777, 1776 };
        int[] petRates = { 5, 15, 30, 50 };

        int random = Util.nextInt(1, 100);
        int cumulative = 0;
        short selectedPetId = babyPetIds[3];

        for (int i = 0; i < petRates.length; i++) {
            cumulative += petRates[i];
            if (random <= cumulative) {
                selectedPetId = babyPetIds[i];
                break;
            }
        }
        Item pet = ItemService.gI().createNewItem(selectedPetId);
        pet.itemOptions.add(new ItemOption(30, 0));

        boolean isPermanent = false;
        int durationDays = 7;

        int randDuration = Util.nextInt(1, 100);
        if (randDuration <= 5) {
            isPermanent = true;
            Service.gI().sendThongBaoAllPlayer(
                    pl.name + " đã may mắn mở Hộp mù Bé Ba nhận được " + pet.template.name + " hạn dùng vĩnh viễn!");
        } else if (randDuration <= 20) {
            durationDays = 45;
        } else if (randDuration <= 50) {
            durationDays = 15;
        }

        if (!isPermanent) {
            pet.itemOptions.add(new ItemOption(93, durationDays));
        }
        switch (selectedPetId) {
            case 1775 -> {
                pet.itemOptions.add(new ItemOption(77, Util.nextInt(20, 25)));
                pet.itemOptions.add(new ItemOption(50, Util.nextInt(20, 25)));
                pet.itemOptions.add(new ItemOption(94, Util.nextInt(7, 10)));
                pet.itemOptions.add(new ItemOption(5, Util.nextInt(10, 13)));
                pet.itemOptions.add(new ItemOption(14, Util.nextInt(7, 10)));
            }
            case 1776 -> {
                pet.itemOptions.add(new ItemOption(77, Util.nextInt(15, 20)));
                pet.itemOptions.add(new ItemOption(94, Util.nextInt(4, 6)));
                pet.itemOptions.add(new ItemOption(108, Util.nextInt(6, 8)));
            }
            case 1777 -> {
                pet.itemOptions.add(new ItemOption(77, Util.nextInt(15, 20)));
                pet.itemOptions.add(new ItemOption(5, Util.nextInt(6, 8)));
                pet.itemOptions.add(new ItemOption(14, Util.nextInt(4, 6)));
            }
            case 1778 -> {
                pet.itemOptions.add(new ItemOption(50, Util.nextInt(14, 18)));
                pet.itemOptions.add(new ItemOption(77, Util.nextInt(14, 18)));
                pet.itemOptions.add(new ItemOption(103, Util.nextInt(14, 18)));
                pet.itemOptions.add(new ItemOption(236, Util.nextInt(18, 22)));
            }
        }
        InventoryService.gI().subQuantityItemsBag(pl, boxItem, 1);
        InventoryService.gI().addItemBag(pl, pet);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);

        String hsdStr = isPermanent ? "(Vĩnh viễn)" : "(HSD: " + durationDays + " ngày)";
        Service.gI().sendThongBao(pl, "Bạn nhận được " + pet.template.name + " " + hsdStr);
        CombineService.gI().sendEffectOpenItem(pl, boxItem.template.iconID, pet.template.iconID);
    }

    private void TayakiTrade(Player pl) {
        Item tayaki = InventoryService.gI().findItemBag(pl, 1786);
        if (tayaki != null && tayaki.quantity > 0) {
            NpcService.gI().createMenuConMeo(pl, ConstNpc.TRADE_TAYAKI, 5067,
                    // "Ngươi có muốn đổi " + tayaki.quantity + " Tayaki lấy " + tayaki.quantity + "
                    // phiếu ăn không?",
                    "Ngươi có muốn đổi " + tayaki.quantity + " Tayaki lấy " + tayaki.quantity + " phiếu ăn không?",
                    "Đồng ý", "Từ chối");
        }
    }

    private boolean contains(short[] array, short value) {
        for (short v : array) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }

    private void openTrungThuVipBox(Player pl, Item boxItem) {
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] daNangCap = new short[] { 1074, 1075, 1076, 1077, 1078 };
        short[] daMayMan = new short[] { 1079, 1080, 1081, 1082, 1083 };
        short[] congThucVip = new short[] { 1084, 1085, 1086 };
        short[] ruongSPL = new short[] { 1453 };
        short[] manhKhi = new short[] { 1771 };
        short[] manhRong = new short[] { 1204 };
        short[] ctPocThoDen = new short[] { 1886 };
        short[] ctChuCuoi = new short[] { 1889 };
        short[] ctHangNga = new short[] { 1700 };
        short[] phieuCapsule = new short[] { 1690 };
        short[] phieuCapsuleVo = new short[] { 1909 };
        List<Short> rewardPool = new ArrayList<>();
        for (short id : daNangCap) {
            for (int i = 0; i < 6; i++) {
                rewardPool.add(id);
            }
        }
        for (short id : daMayMan) {
            for (int i = 0; i < 1; i++) {
                rewardPool.add(id);
            }
        }
        for (short id : congThucVip) {
            for (int i = 0; i < 1; i++) {
                rewardPool.add(id);
            }
        }
        for (int i = 0; i < 1; i++) {
            rewardPool.add(ruongSPL[0]);
        }
        for (int i = 0; i < 1; i++) {
            rewardPool.add(manhKhi[0]);
        }
        for (int i = 0; i < 1; i++) {
            rewardPool.add(manhRong[0]);
        }
        rewardPool.add(ctPocThoDen[0]);
        rewardPool.add(ctChuCuoi[0]);
        rewardPool.add(ctHangNga[0]);
        for (int i = 0; i < 1; i++) {
            rewardPool.add(phieuCapsule[0]);
        }
        for (int i = 0; i < 1; i++) {
            rewardPool.add(phieuCapsuleVo[0]);
        }
        short chosen = rewardPool.get(Util.nextInt(0, rewardPool.size() - 1));
        Item reward = ItemService.gI().createNewItem(chosen);
        if (reward.itemOptions == null) {
            reward.itemOptions = new ArrayList<>();
        }
        int quantity = 1;
        if (contains(daNangCap, chosen)) {
            quantity = Util.nextInt(1, 3);
        } else if (contains(daMayMan, chosen)) {
            quantity = Util.nextInt(1, 3);
        } else if (chosen == manhKhi[0]) {
            quantity = Util.nextInt(1, 2);
        } else if (chosen == manhRong[0]) {
            quantity = Util.nextInt(1, 2);
        } else if (chosen == phieuCapsule[0]) {
            quantity = Util.nextInt(1, 3);
        } else if (chosen == phieuCapsuleVo[0]) {
            quantity = Util.nextInt(1, 3);
        }
        reward.quantity = quantity;
        boolean isCostume = (chosen == ctPocThoDen[0] || chosen == ctChuCuoi[0] || chosen == ctHangNga[0]);
        if (isCostume) {
            reward.itemOptions.add(new ItemOption(30, 0));
            if (chosen == ctHangNga[0]) {
                reward.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30))); // SĐ +23%
                reward.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30))); // HP +21%
                reward.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30))); // KI +21%
                reward.itemOptions.add(new ItemOption(117, Util.nextInt(10, 15))); // Đẹp +13%
                reward.itemOptions.add(new ItemOption(14, Util.nextInt(10, 15))); // Chí mạng +15%
            } else if (chosen == ctPocThoDen[0]) {
                reward.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30))); // SĐ +23%
                reward.itemOptions.add(new ItemOption(77, Util.nextInt(20, 30))); // HP +27%
                reward.itemOptions.add(new ItemOption(103, Util.nextInt(20, 30))); // KI +21%
                reward.itemOptions.add(new ItemOption(94, Util.nextInt(10, 15))); // Giảm 15% sát thương
                reward.itemOptions.add(new ItemOption(97, Util.nextInt(10, 15))); // Phản 10% sát thương
            } else if (chosen == ctChuCuoi[0]) {
                reward.itemOptions.add(new ItemOption(50, Util.nextInt(20, 30))); // SĐ +23%
                reward.itemOptions.add(new ItemOption(95, Util.nextInt(10, 15))); // Biến 10% tấn công thành HP
                reward.itemOptions.add(new ItemOption(236, Util.nextInt(30, 40))); // +33% May mắn
                reward.itemOptions.add(new ItemOption(100, Util.nextInt(10, 20))); // +10% vàng rơi
            }
            boolean isPermanent = false;
            if (Util.isTrue(20, 100)) {
                isPermanent = true;
                Service.gI().sendThongBaoAllPlayer(pl.name + " đã may mắn mở Hộp quà Trung Thu VIP nhận được "
                        + reward.template.name + " hạn dùng vĩnh viễn!");
            }
            if (!isPermanent) {
                int durationDays = (Util.nextInt(0, 1) == 0) ? 15 : 30;
                reward.itemOptions.add(new ItemOption(93, durationDays));
            }
        }
        if (chosen == phieuCapsule[0] || chosen == phieuCapsuleVo[0]) {
            reward.itemOptions.add(new ItemOption(30, 0));
        }
        if (chosen == manhKhi[0]) {
            reward.itemOptions.add(new ItemOption(30, 0));
            reward.itemOptions.add(new ItemOption(87, 0));
        }
        short boxIconSaved = (boxItem != null && boxItem.template != null) ? boxItem.template.iconID : 0;
        InventoryService.gI().subQuantityItemsBag(pl, boxItem, 1);
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);
        CombineService.gI().sendEffectOpenItem(pl, boxIconSaved, reward.template.iconID);
        Service.gI().sendThongBao(pl,
                "Bạn nhận được " + reward.template.name + (reward.quantity > 1 ? (" x" + reward.quantity) : ""));
    }

    private void KeoTaoTrade(Player player) {
        Item keotao = InventoryService.gI().findItemBag(player, 1787);
        if (keotao != null && keotao.quantity > 0) {
            int sl = keotao.quantity;
            NpcService.gI().createMenuConMeo(player, ConstNpc.TRADE_KEO_TAO, 5067,
                    "Ngươi có muốn đổi " + sl + " Kẹo táo lấy " + (sl * 2) + " Phiếu Ăn không?",
                    "Đồng ý", "Từ chối");
        } else {
            Service.gI().sendThongBao(player, "Bạn không có Kem que đôi.");
        }
    }

    private void QueKemDoiTrade(Player player) {
        Item kem = InventoryService.gI().findItemBag(player, 1788);
        if (kem != null && kem.quantity > 0) {
            int sl = kem.quantity;
            NpcService.gI().createMenuConMeo(player, ConstNpc.TRADE_QUE_KEM_DOI, 5067,
                    "Ngươi có muốn đổi " + sl + " Kem que đôi lấy " + (sl * 3) + " Phiếu Ăn không?",
                    "Đồng ý", "Từ chối");
        } else {
            Service.gI().sendThongBao(player, "Bạn không có Kem que đôi.");
        }
    }

    private void MochiTrade(Player player) {
        Item mochi = InventoryService.gI().findItemBag(player, 1789);
        if (mochi != null && mochi.quantity > 0) {
            int sl = mochi.quantity;
            NpcService.gI().createMenuConMeo(player, ConstNpc.TRADE_MOCHI, 5067,
                    "Ngươi có muốn đổi " + sl + " Mochi lấy " + (sl * 5) + " Phiếu Ăn không?",
                    "Đồng ý", "Từ chối");
        } else {
            Service.gI().sendThongBao(player, "Bạn không có Kem que đôi.");
        }
    }

    private void RauBachTuocTrade(Player player) {
        Item rau = InventoryService.gI().findItemBag(player, 1790);
        if (rau != null && rau.quantity > 0) {
            int sl = rau.quantity;
            NpcService.gI().createMenuConMeo(player, ConstNpc.TRADE_RAU_BACH_TUOC, 5067,
                    "Ngươi có muốn đổi " + sl + " Râu bạch tuộc lấy " + (sl * 10) + " Phiếu Ăn không?",
                    "Đồng ý", "Từ chối");
        } else {
            Service.gI().sendThongBao(player, "Bạn không có Kem que đôi.");
        }
    }

    private void MiRaMenTrade(Player player) {
        Item ramen = InventoryService.gI().findItemBag(player, 1791);
        if (ramen != null && ramen.quantity > 0) {
            int sl = ramen.quantity;
            NpcService.gI().createMenuConMeo(player, ConstNpc.TRADE_MI_RA_MEN, 5067,
                    "Ngươi có muốn đổi " + sl + " Mì Ramen lấy " + (sl * 20) + " Phiếu Ăn không?",
                    "Đồng ý", "Từ chối");
        }
    }

    private void openSplChest(Player pl, Item boxItem) {
        if (boxItem == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        int rand = Util.nextInt(0, 6);
        short splId = (short) (441 + rand);
        Item spl = ItemService.gI().createNewItem(splId);
        spl.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
        short boxIcon = (boxItem.template != null) ? boxItem.template.iconID : 0;
        InventoryService.gI().subQuantityItemsBag(pl, boxItem, 1);
        InventoryService.gI().addItemBag(pl, spl);
        InventoryService.gI().sendItemBags(pl);
        CombineService.gI().sendEffectOpenItem(pl, boxIcon, spl.template.iconID);
        Service.gI().sendThongBao(pl, "Bạn nhận được " + spl.template.name);
    }

    private void openSplChestVip(Player pl, Item boxItem) {
        if (boxItem == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        int rand = Util.nextInt(0, 6);
        short splId = (short) (441 + rand);
        Item spl = ItemService.gI().createNewItem(splId);
        spl.itemOptions.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
        short boxIconSaved = (boxItem.template != null) ? boxItem.template.iconID : 0;
        InventoryService.gI().subQuantityItemsBag(pl, boxItem, 1);
        InventoryService.gI().addItemBag(pl, spl);
        InventoryService.gI().sendItemBags(pl);
        CombineService.gI().sendEffectOpenItem(pl, boxIconSaved, spl.template.iconID);
        Service.gI().sendThongBao(pl, "Bạn nhận được " + spl.template.name);
    }

    private void useBinhHutNangLuong(Player pl) {
        boolean mabuEgg = pl.mabuEgg != null || (pl.pet != null);
        if (!mabuEgg) {
            Service.gI().sendThongBao(pl, "Cần có đệ tử Mabư hoặc trứng Bư đã nở.");
            return;
        }
        if (pl.pet.nPoint == null || pl.pet.nPoint.power < 40_000_000_000L) {
            Service.gI().sendThongBao(pl, "Đệ tử hiện tại phải đạt từ 40 tỷ sức mạnh trở lên.");
            return;
        }
        Item bottle = InventoryService.gI().findItemBag(pl, 1852);
        if (bottle == null || bottle.itemOptions == null) {
            return;
        }
        Item.ItemOption kilisOption = null;
        for (Item.ItemOption io : bottle.itemOptions) {
            if (io.optionTemplate.id == 253) {
                kilisOption = io;
                break;
            }
        }
        if (kilisOption == null) {
            return;
        }
        // Kiểm tra đủ param mới trừ và xóa
        if (kilisOption.param < 3000) {
            Service.gI().sendThongBao(pl, "Chai năng lượng không đủ 3000 điểm để sử dụng.");
            return;
        }
        // Trừ param
        kilisOption.param -= 3000;

        InventoryService.gI().subQuantityItemsBag(pl, bottle, 1);

        int newPetType = Util.nextInt(1, 3);
        PetService.gI().setChildPet(pl, newPetType, pl.gender);
    }

    private void useBinhHutNangLuong2(Player pl) {
        if (pl.pet.typePet != 5 && pl.pet.typePet != 6 && pl.pet.typePet != 4) {
            Service.gI().sendThongBao(pl, "Cần có đệ tử 2 để đổi đệ tử");
            return;
        }
        Item bottle = InventoryService.gI().findItemBag(pl, 1890);
        if (bottle == null) {
            return;
        }
        InventoryService.gI().subQuantityItemsBag(pl, bottle, 1);
        List<Integer> petTypes = new ArrayList<>(List.of(1, 2, 3));
        Collections.shuffle(petTypes);
        for (int i = 0; i < 3; i++) {
            int newPetType = petTypes.get(i);
            PetService.gI().setChildPet(pl, newPetType, pl.gender);
        }
    }

    private void openRuongRongThan(Player pl, Item boxItem) {
        if (boxItem == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] mountIds = { 1896, 1897, 1898 };
        short selectedId = mountIds[Util.nextInt(0, mountIds.length - 1)];
        Item mount = ItemService.gI().createNewItem(selectedId);
        int core = Util.nextInt(5, 15);
        switch (selectedId) {
            case 1896 -> {
                mount.itemOptions.add(new Item.ItemOption(77, core));
                mount.itemOptions.add(new Item.ItemOption(80, 10));
            }
            case 1897 -> {
                mount.itemOptions.add(new Item.ItemOption(103, core));
                mount.itemOptions.add(new Item.ItemOption(81, 10));
                mount.itemOptions.add(new Item.ItemOption(106, 0));
            }
            case 1898 -> {
                mount.itemOptions.add(new Item.ItemOption(147, core));
                mount.itemOptions.add(new Item.ItemOption(95, 15));
            }
        }
        mount.itemOptions.add(new Item.ItemOption(85, 0));
        boolean isPermanent = Util.isTrue(5, 100);
        int durationDays = Util.nextInt(5, 30);
        if (!isPermanent) {
            mount.itemOptions.add(new Item.ItemOption(93, durationDays));
        }
        mount.itemOptions.add(new Item.ItemOption(30, 0));
        InventoryService.gI().subQuantityItemsBag(pl, boxItem, 1);
        InventoryService.gI().addItemBag(pl, mount);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);
        String hsdStr = isPermanent ? "(Vĩnh viễn)" : "(HSD: " + durationDays + " ngày)";
        Service.gI().sendThongBao(pl, "Bạn nhận được " + mount.template.name + " " + hsdStr);
        short boxIcon = (boxItem.template != null) ? boxItem.template.iconID : 0;
        CombineService.gI().sendEffectOpenItem(pl, boxIcon, mount.template.iconID);
    }

    private void openRuongRongThanVip(Player pl, Item mountItem) {
        if (pl == null || mountItem == null || mountItem.template == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] mountPupIds = {
                (short) 1896, (short) 1897, (short) 1898, (short) 1899
        };
        int[] rates = { 50, 30, 15, 5 };
        int totalRate = 0;
        for (int rate : rates) {
            totalRate += rate;
        }
        int randomPick = Util.nextInt(1, totalRate);
        short selectedPupId = (short) 1961;
        int cumulativeRate = 0;
        for (int i = 0; i < rates.length; i++) {
            cumulativeRate += rates[i];
            if (randomPick <= cumulativeRate) {
                selectedPupId = mountPupIds[i];
                break;
            }
        }
        Item mount = ItemService.gI().createNewItem(selectedPupId);
        if (mount == null || mount.template == null) {
            Service.gI().sendThongBao(pl, "Không thể tạo vật phẩm.");
            return;
        }
        mount.itemOptions.add(new ItemOption(30, 0));
        int randDurationType = Util.nextInt(1, 100);
        int durationInDays = 0;
        boolean isPermanent = false;
        if (randDurationType <= 3) {
            isPermanent = true;
            ServerNotify.gI().notify(pl.name + " đã may mắn mở Rương Rồng Thân Vip nhận được "
                    + mount.template.name + " hạn dùng vĩnh viễn!");
        } else if (randDurationType <= 37) {
            durationInDays = 30;
        } else {
            durationInDays = 15;
        }
        if (!isPermanent) {
            mount.itemOptions.add(new ItemOption(93, durationInDays)); // ID 93: HSD # ngày
        }
        switch (selectedPupId) {
            case 1896 -> {
                mount.itemOptions.add(new Item.ItemOption(77, 13)); // KP
                mount.itemOptions.add(new Item.ItemOption(80, 10)); // %HP/S
                mount.itemOptions.add(new Item.ItemOption(85, 0)); // BAY
            }
            case 1897 -> {
                mount.itemOptions.add(new Item.ItemOption(103, 17)); // KI
                mount.itemOptions.add(new Item.ItemOption(81, 10)); // %KI/s
                mount.itemOptions.add(new Item.ItemOption(106, 0)); // KI
                mount.itemOptions.add(new Item.ItemOption(85, 0)); // BAY
            }
            case 1898 -> {
                mount.itemOptions.add(new Item.ItemOption(50, 13)); // sd
                mount.itemOptions.add(new Item.ItemOption(95, 15)); // tan cong thanh hp
                mount.itemOptions.add(new Item.ItemOption(85, 0)); // BAY
            }
            case 1899 -> {
                mount.itemOptions.add(new Item.ItemOption(50, 13)); // Sức đánh
                mount.itemOptions.add(new Item.ItemOption(77, 13)); // HP
                mount.itemOptions.add(new Item.ItemOption(103, 17)); // KI
                mount.itemOptions.add(new Item.ItemOption(5, 7)); // sdcm
                mount.itemOptions.add(new Item.ItemOption(94, 7)); // gst
                mount.itemOptions.add(new Item.ItemOption(10, 10)); // stc
                mount.itemOptions.add(new Item.ItemOption(85, 0)); // BAY
            }
        }
        InventoryService.gI().subQuantityItemsBag(pl, mountItem, 1);
        InventoryService.gI().addItemBag(pl, mount);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);

        String hsdString = isPermanent ? "(Vĩnh viễn)" : "(HSD: " + durationInDays + " ngày)";
        if (mount.template != null) {
            Service.gI().sendThongBao(pl, "Bạn nhận được " + mount.template.name + " " + hsdString);
        }
        if (mountItem.template != null && mount.template != null) {
            CombineService.gI().sendEffectOpenItem(pl, mountItem.template.iconID, mount.template.iconID);
        }
    }

    public void openGokuvip(Player pl, Item gokuItem) {
        if (pl == null || gokuItem == null || gokuItem.template == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] gokuPupIds = {
                (short) 1588, (short) 1589, (short) 1595, (short) 1587, (short) 1593, (short) 1590
        };
        int[] rates = { 31, 24, 18, 13, 9, 5 };
        int totalRate = 0;
        for (int rate : rates) {
            totalRate += rate;
        }
        int randomPick = Util.nextInt(1, totalRate);
        short selectedPupId = (short) 1961;
        int cumulativeRate = 0;
        for (int i = 0; i < rates.length; i++) {
            cumulativeRate += rates[i];
            if (randomPick <= cumulativeRate) {
                selectedPupId = gokuPupIds[i];
                break;
            }
        }
        Item goku = ItemService.gI().createNewItem(selectedPupId);
        if (goku == null || goku.template == null) {
            Service.gI().sendThongBao(pl, "Không thể tạo vật phẩm.");
            return;
        }
        goku.itemOptions.add(new ItemOption(30, 0));
        int randDurationType = Util.nextInt(1, 100);
        int durationInDays = 0;
        boolean isPermanent = false;
        if (randDurationType <= 3) {
            isPermanent = true;
            ServerNotify.gI().notify(pl.name + " đã may mắn mở Hộp quà Goku Vip nhận được "
                    + goku.template.name + " hạn dùng vĩnh viễn!");
        } else if (randDurationType <= 37) {
            durationInDays = 30;
        } else {
            durationInDays = 15;
        }
        if (!isPermanent) {
            goku.itemOptions.add(new ItemOption(93, durationInDays)); // ID 93: HSD # ngày
        }
        switch (selectedPupId) {
            case 1588 -> {
                goku.itemOptions.add(new Item.ItemOption(50, 18)); // Sức đánh
                goku.itemOptions.add(new Item.ItemOption(77, 18)); // HP
                goku.itemOptions.add(new Item.ItemOption(103, 18)); // KI
                goku.itemOptions.add(new Item.ItemOption(210, 1)); // Tùy chọn khác
            }
            case 1589 -> {
                goku.itemOptions.add(new Item.ItemOption(50, 21)); // Sức đánh
                goku.itemOptions.add(new Item.ItemOption(77, 21)); // HP
                goku.itemOptions.add(new Item.ItemOption(103, 21)); // KI
                goku.itemOptions.add(new Item.ItemOption(210, 1)); // Tùy chọn khác
            }
            case 1595 -> {
                goku.itemOptions.add(new Item.ItemOption(50, 22)); // Sức đánh
                goku.itemOptions.add(new Item.ItemOption(77, 22)); // HP
                goku.itemOptions.add(new Item.ItemOption(103, 22)); // KI
                goku.itemOptions.add(new Item.ItemOption(210, 2)); // Tùy chọn khác
            }
            case 1587 -> {
                goku.itemOptions.add(new Item.ItemOption(50, 23)); // Sức đánh
                goku.itemOptions.add(new Item.ItemOption(77, 23)); // HP
                goku.itemOptions.add(new Item.ItemOption(103, 23)); // KI
                goku.itemOptions.add(new Item.ItemOption(210, 3)); // Tùy chọn khác
            }
            case 1593 -> {
                goku.itemOptions.add(new Item.ItemOption(50, 25)); // Sức đánh
                goku.itemOptions.add(new Item.ItemOption(77, 25)); // HP
                goku.itemOptions.add(new Item.ItemOption(103, 25)); // KI
                goku.itemOptions.add(new Item.ItemOption(210, 4));
                goku.itemOptions.add(new Item.ItemOption(106, 0));
            }
            case 1590 -> {
                goku.itemOptions.add(new Item.ItemOption(50, 27)); // Sức đánh
                goku.itemOptions.add(new Item.ItemOption(77, 27)); // HP
                goku.itemOptions.add(new Item.ItemOption(103, 27)); // KI
                goku.itemOptions.add(new Item.ItemOption(210, 4)); // Tùy chọn khác
                goku.itemOptions.add(new Item.ItemOption(106, 0));
            }
        }
        InventoryService.gI().subQuantityItemsBag(pl, gokuItem, 1);
        InventoryService.gI().addItemBag(pl, goku);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);

        String hsdString = isPermanent ? "(Vĩnh viễn)" : "(HSD: " + durationInDays + " ngày)";
        if (goku.template != null) {
            Service.gI().sendThongBao(pl, "Bạn nhận được " + goku.template.name + " " + hsdString);
        }
        if (gokuItem.template != null && goku.template != null) {
            CombineService.gI().sendEffectOpenItem(pl, gokuItem.template.iconID, goku.template.iconID);
        }
    }

    public void openCadicvip(Player pl, Item cadicItem) {
        if (pl == null || cadicItem == null || cadicItem.template == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] cadicPupIds = {
                (short) 1741, (short) 1742, (short) 1743, (short) 1744, (short) 1745, (short) 1746
        };
        int[] rates = { 31, 24, 18, 13, 9, 5 };
        int totalRate = 0;
        for (int rate : rates) {
            totalRate += rate;
        }
        int randomPick = Util.nextInt(1, totalRate);
        short selectedPupId = (short) 1961;
        int cumulativeRate = 0;
        for (int i = 0; i < rates.length; i++) {
            cumulativeRate += rates[i];
            if (randomPick <= cumulativeRate) {
                selectedPupId = cadicPupIds[i];
                break;
            }
        }
        Item cadic = ItemService.gI().createNewItem(selectedPupId);
        if (cadic == null || cadic.template == null) {
            Service.gI().sendThongBao(pl, "Không thể tạo vật phẩm.");
            return;
        }
        cadic.itemOptions.add(new ItemOption(30, 0));
        int randDurationType = Util.nextInt(1, 100);
        int durationInDays = 0;
        boolean isPermanent = false;
        if (randDurationType <= 3) {
            isPermanent = true;
            ServerNotify.gI().notify(pl.name + " đã may mắn mở Hộp quà Cađíc Vip nhận được "
                    + cadic.template.name + " hạn dùng vĩnh viễn!");
        } else if (randDurationType <= 37) {
            durationInDays = 30;
        } else {
            durationInDays = 15;
        }
        if (!isPermanent) {
            cadic.itemOptions.add(new ItemOption(93, durationInDays)); // ID 93: HSD # ngày
        }
        switch (selectedPupId) {
            case 1741 -> {
                cadic.itemOptions.add(new Item.ItemOption(50, 18)); // Sức đánh
                cadic.itemOptions.add(new Item.ItemOption(77, 18)); // HP
                cadic.itemOptions.add(new Item.ItemOption(103, 18)); // KI
                cadic.itemOptions.add(new Item.ItemOption(210, 1)); // Tùy chọn khác
            }
            case 1742 -> {
                cadic.itemOptions.add(new Item.ItemOption(50, 21)); // Sức đánh
                cadic.itemOptions.add(new Item.ItemOption(77, 21)); // HP
                cadic.itemOptions.add(new Item.ItemOption(103, 21)); // KI
                cadic.itemOptions.add(new Item.ItemOption(210, 1)); // Tùy chọn khác
            }
            case 1743 -> {
                cadic.itemOptions.add(new Item.ItemOption(50, 22)); // Sức đánh
                cadic.itemOptions.add(new Item.ItemOption(77, 22)); // HP
                cadic.itemOptions.add(new Item.ItemOption(103, 22)); // KI
                cadic.itemOptions.add(new Item.ItemOption(210, 2)); // Tùy chọn khác
            }
            case 1744 -> {
                cadic.itemOptions.add(new Item.ItemOption(50, 23)); // Sức đánh
                cadic.itemOptions.add(new Item.ItemOption(77, 23)); // HP
                cadic.itemOptions.add(new Item.ItemOption(103, 23)); // KI
                cadic.itemOptions.add(new Item.ItemOption(210, 3)); // Tùy chọn khác
            }
            case 1745 -> {
                cadic.itemOptions.add(new Item.ItemOption(50, 25)); // Sức đánh
                cadic.itemOptions.add(new Item.ItemOption(77, 25)); // HP
                cadic.itemOptions.add(new Item.ItemOption(103, 25)); // KI
                cadic.itemOptions.add(new Item.ItemOption(210, 4));
            }
            case 1746 -> {
                cadic.itemOptions.add(new Item.ItemOption(50, 27)); // Sức đánh
                cadic.itemOptions.add(new Item.ItemOption(77, 27)); // HP
                cadic.itemOptions.add(new Item.ItemOption(103, 27)); // KI
                cadic.itemOptions.add(new Item.ItemOption(210, 5)); // Tùy chọn khác
                cadic.itemOptions.add(new Item.ItemOption(106, 0));
            }
        }
        InventoryService.gI().subQuantityItemsBag(pl, cadicItem, 1);
        InventoryService.gI().addItemBag(pl, cadic);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);

        String hsdString = isPermanent ? "(Vĩnh viễn)" : "(HSD: " + durationInDays + " ngày)";
        if (cadic.template != null) {
            Service.gI().sendThongBao(pl, "Bạn nhận được " + cadic.template.name + " " + hsdString);
        }
        if (cadicItem.template != null && cadic.template != null) {
            CombineService.gI().sendEffectOpenItem(pl, cadicItem.template.iconID, cadic.template.iconID);
        }
    }

    public void openRadanr(Player pl, Item rdnrItem) {
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }
        short[] rdnrPupIds = {
                (short) 1579, (short) 1580, (short) 1581, (short) 1582, (short) 1583, (short) 1584, (short) 1585,
                (short) 1586
        };
        int[] rates = { 7, 9, 11, 13, 15, 18, 22, 5 };
        int totalRate = 0;
        for (int rate : rates) {
            totalRate += rate;
        }
        int randomPick = Util.nextInt(1, totalRate);
        short selectedPupId = (short) 1961;
        int cumulativeRate = 0;
        for (int i = 0; i < rates.length; i++) {
            cumulativeRate += rates[i];
            if (randomPick <= cumulativeRate) {
                selectedPupId = rdnrPupIds[i];
                break;
            }
        }
        Item rdnr = ItemService.gI().createNewItem(selectedPupId);
        rdnr.itemOptions.add(new ItemOption(30, 0));
        int randDurationType = Util.nextInt(1, 100);
        int durationInDays = 0;
        boolean isPermanent = false;
        if (randDurationType <= 3) {
            isPermanent = true;
            ServerNotify.gI().notify(pl.name + " đã may mắn mở Rađa ngọc rồng Vip nhận được "
                    + rdnr.template.name + " hạn dùng vĩnh viễn!");
        } else if (randDurationType <= 37) {
            durationInDays = 30;
        } else {
            durationInDays = 15;
        }
        if (!isPermanent) {
            rdnr.itemOptions.add(new ItemOption(93, durationInDays)); // ID 93: HSD # ngày
        }
        switch (selectedPupId) {
            case 1579 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 19)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 19)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 19)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(14, 10)); // cm
                rdnr.itemOptions.add(new Item.ItemOption(5, 10)); // sdcm
            }
            case 1580 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 18)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 18)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 18)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(108, 10)); // né đòn
                rdnr.itemOptions.add(new Item.ItemOption(94, 10)); // giảm st
            }
            case 1581 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 17)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 17)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 17)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(108, 5)); // né đòn
                rdnr.itemOptions.add(new Item.ItemOption(94, 5)); // gst
            }
            case 1582 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 16)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 16)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 16)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(14, 5)); // cm
                rdnr.itemOptions.add(new Item.ItemOption(5, 5)); // sdcm
            }
            case 1583 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 15)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 15)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 15)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(14, 5)); // cm
                rdnr.itemOptions.add(new Item.ItemOption(108, 5)); // né đòn
            }
            case 1584 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 13)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 13)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 13)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(108, 5)); // né đòn
            }
            case 1585 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 12)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 12)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 12)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(14, 5)); // cm
            }
            case 1586 -> {
                rdnr.itemOptions.add(new Item.ItemOption(50, 22)); // sd
                rdnr.itemOptions.add(new Item.ItemOption(77, 22)); // hp
                rdnr.itemOptions.add(new Item.ItemOption(103, 22)); // KI
                rdnr.itemOptions.add(new Item.ItemOption(108, 10)); // né đòn
                rdnr.itemOptions.add(new Item.ItemOption(94, 12)); // gst
                rdnr.itemOptions.add(new Item.ItemOption(14, 10)); // cm
                rdnr.itemOptions.add(new Item.ItemOption(5, 15)); // sdcm
            }
        }
        InventoryService.gI().subQuantityItemsBag(pl, rdnrItem, 1);
        InventoryService.gI().addItemBag(pl, rdnr);
        InventoryService.gI().sendItemBags(pl);
        PlayerService.gI().sendInfoHpMpMoney(pl);

        String hsdString = isPermanent ? "(Vĩnh viễn)" : "(HSD: " + durationInDays + " ngày)";
        Service.gI().sendThongBao(pl, "Bạn nhận được " + rdnr.template.name + " " + hsdString);
        CombineService.gI().sendEffectOpenItem(pl, rdnrItem.template.iconID, rdnr.template.iconID);
    }

    public void giaHanKichHoat(Player pl, int addDays) {
        try {
            final int MAX_DAYS = 60;
            long now = System.currentTimeMillis();
            long addMs = addDays * 24L * 60 * 60 * 1000;

            if (pl.timeUpSKH < now) {
                pl.timeUpSKH = now + addMs;
            } else {
                pl.timeUpSKH += addMs;
            }

            long maxExpire = now + (MAX_DAYS * 24L * 60 * 60 * 1000);
            if (pl.timeUpSKH > maxExpire) {
                pl.timeUpSKH = maxExpire;
            }

            String query = "UPDATE player SET time_up_skh = '"
                    + new java.sql.Timestamp(pl.timeUpSKH)
                    + "' WHERE id = " + pl.id;
            AlyraManager.executeUpdate(query);

            String expireDateStr = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(pl.timeUpSKH));
            Service.gI().sendThongBao(pl,
                    "Gia hạn thành công thêm " + addDays + " ngày.\nHiệu lực đến: " + expireDateStr);

        } catch (Exception e) {
            Logger.logException(UseItem.class, e, "Lỗi khi gia hạn tim set kích hoạt");
            Service.gI().sendThongBao(pl, "Có lỗi khi gia hạn tim set kích hoạt!");
        }
    }

    private void openHalloweenBox(Player pl, Item boxItem) {
        if (boxItem == null || boxItem.template.id != 1728) {
            Service.gI().sendThongBao(pl, "Vật phẩm không hợp lệ.");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }

        // Định nghĩa các item ID và tỷ lệ rơi
        short[] daMayMan = { 1079, 1080, 1081, 1082 }; // Đá may mắn cấp 1-4
        short[] biNgo = { 702, 703, 704, 705, 706, 707, 708 }; // Bí ngô 1-7 sao
        short cuonSachCu = 1283;
        short thungRac = 1891;
        short petCerberus = 1654;
        short coHonMabu = 1358;
        short coHonXenBoHung = 1359;
        short coMaDenNhayMua = 1360;
        short ctOmega = 1685;
        short canhThienThan = 1927;

        // Tỷ lệ rơi (tổng 100%)
        int rand = Util.nextInt(1, 100);
        short selectedItemId;

        if (rand <= 25) { // 25% - Đá may mắn
            selectedItemId = daMayMan[Util.nextInt(0, daMayMan.length - 1)];
        } else if (rand <= 50) { // 25% - Bí ngô
            selectedItemId = biNgo[Util.nextInt(0, biNgo.length - 1)];
        } else if (rand <= 65) { // 15% - Cuốn sách cũ
            selectedItemId = cuonSachCu;
        } else if (rand <= 80) { // 15% - Thùng rác
            selectedItemId = thungRac;
        } else if (rand <= 85) { // 5% - Pet Cerberus
            selectedItemId = petCerberus;
        } else if (rand <= 90) { // 5% - Cờ hồn Mabư
            selectedItemId = coHonMabu;
        } else if (rand <= 93) { // 3% - Cờ hồn Xên Bọ Hung
            selectedItemId = coHonXenBoHung;
        } else if (rand <= 96) { // 3% - Cờ Ma đèn nhảy múa
            selectedItemId = coMaDenNhayMua;
        } else if (rand <= 98) { // 2% - CT Omega
            selectedItemId = ctOmega;
        } else { // 2% - Cánh thiên thần sa ngã
            selectedItemId = canhThienThan;
        }
        Item reward = ItemService.gI().createNewItem(selectedItemId);
        addHalloweenItemOptions(reward, selectedItemId);
        short boxIcon = (boxItem.template != null) ? boxItem.template.iconID : 0;
        InventoryService.gI().subQuantityItemsBag(pl, boxItem, 1);
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().sendItemBags(pl);

        // Hiệu ứng mở hộp
        CombineService.gI().sendEffectOpenItem(pl, boxIcon, reward.template.iconID);

        // Thông báo
        String message = "Bạn nhận được " + reward.template.name;
        if (reward.quantity > 1) {
            message += " x" + reward.quantity;
        }
        Service.gI().sendThongBao(pl, message);

        // Thông báo toàn server nếu trúng item hiếm
        if (selectedItemId == petCerberus || selectedItemId == ctOmega || selectedItemId == canhThienThan) {
            Service.gI().sendThongBaoAllPlayer(
                    pl.name + " đã may mắn mở Túi mù Halloween nhận được " + reward.template.name + "!");
        }
    }

    private void addHalloweenItemOptions(Item item, short itemId) {
        // Đá may mắn cấp 1-4
        if (itemId >= 1079 && itemId <= 1082) {
        } // Bí ngô 1-7 sao
        else if (itemId >= 702 && itemId <= 708) {
            item.itemOptions.add(new ItemOption(87, 1));
            item.itemOptions.add(new ItemOption(174, 2025));
            item.itemOptions.add(new ItemOption(30, 1));
        } // Cuốn sách cũ
        else if (itemId == 1283) {
            item.itemOptions.add(new ItemOption(30, 1));
        } // Thùng rác
        else if (itemId == 1891) {
            item.itemOptions.add(new ItemOption(30, 1));
        } // Pet Cerberus
        else if (itemId == 1654) {
            item.itemOptions.add(new ItemOption(50, 16));
            item.itemOptions.add(new ItemOption(77, 15));
            item.itemOptions.add(new ItemOption(103, 15));
            item.itemOptions.add(new ItemOption(106, 1));
            // Random 1 trong 4 option
            int[] randomOpts = { 3, 5, 7, 15 };
            int selectedOpt = randomOpts[Util.nextInt(0, randomOpts.length - 1)];
            item.itemOptions.add(new ItemOption(93, selectedOpt));
            item.itemOptions.add(new ItemOption(30, 1));
        } // Cờ hồn Mabư
        else if (itemId == 1358) {
            item.itemOptions.add(new ItemOption(50, 10));
            item.itemOptions.add(new ItemOption(77, 10));
            item.itemOptions.add(new ItemOption(103, 10));
            item.itemOptions.add(new ItemOption(191, 3));
            item.itemOptions.add(new ItemOption(126, 5));
            // Random 1 trong 4 option
            int[] randomOpts = { 3, 5, 7, 15 };
            int selectedOpt = randomOpts[Util.nextInt(0, randomOpts.length - 1)];
            item.itemOptions.add(new ItemOption(93, selectedOpt));
            item.itemOptions.add(new ItemOption(30, 1));
        } // Cờ hồn Xên Bọ Hung
        else if (itemId == 1359) {
            item.itemOptions.add(new ItemOption(50, 10));
            item.itemOptions.add(new ItemOption(77, 10));
            item.itemOptions.add(new ItemOption(103, 10));
            item.itemOptions.add(new ItemOption(194, 3));
            item.itemOptions.add(new ItemOption(8, 2));
            // Random 1 trong 4 option
            int[] randomOpts = { 3, 5, 7, 15 };
            int selectedOpt = randomOpts[Util.nextInt(0, randomOpts.length - 1)];
            item.itemOptions.add(new ItemOption(93, selectedOpt));
            item.itemOptions.add(new ItemOption(30, 1));
        } // Cờ Ma đèn nhảy múa
        else if (itemId == 1360) {
            item.itemOptions.add(new ItemOption(50, 13));
            item.itemOptions.add(new ItemOption(77, 13));
            item.itemOptions.add(new ItemOption(103, 13));
            item.itemOptions.add(new ItemOption(101, 20));
            // Random 1 trong 4 option
            int[] randomOpts = { 3, 5, 7, 15 };
            int selectedOpt = randomOpts[Util.nextInt(0, randomOpts.length - 1)];
            item.itemOptions.add(new ItemOption(93, selectedOpt));
            item.itemOptions.add(new ItemOption(30, 1));
        } // CT Omega
        else if (itemId == 1685) {
            item.itemOptions.add(new ItemOption(50, 28));
            item.itemOptions.add(new ItemOption(77, 28));
            item.itemOptions.add(new ItemOption(103, 28));
            item.itemOptions.add(new ItemOption(5, 25));
            item.itemOptions.add(new ItemOption(192, 10));
            // Random 1 trong 4 option
            int[] randomOpts = { 3, 5, 7, 15 };
            int selectedOpt = randomOpts[Util.nextInt(0, randomOpts.length - 1)];
            item.itemOptions.add(new ItemOption(93, selectedOpt));
            item.itemOptions.add(new ItemOption(30, 1));
        } // Cánh thiên thần sa ngã
        else if (itemId == 1927) {
            item.itemOptions.add(new ItemOption(204, 30));
            item.itemOptions.add(new ItemOption(95, 15));
            item.itemOptions.add(new ItemOption(94, 10));
            // Random 1 trong 4 option
            int[] randomOpts = { 3, 5, 7, 15 };
            int selectedOpt = randomOpts[Util.nextInt(0, randomOpts.length - 1)];
            item.itemOptions.add(new ItemOption(93, selectedOpt));
            item.itemOptions.add(new ItemOption(30, 1));
        }
    }

    public void openCommonTrashBox(Player pl, Item boxItem, int tierLevel) {
        if (boxItem == null) {
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }

        Item reward = createCommonTrashReward(pl, tierLevel);
        short boxIcon = (boxItem.template != null) ? boxItem.template.iconID : 0;

        InventoryService.gI().subQuantityItemsBag(pl, boxItem, 1);

        if (reward != null) {
            InventoryService.gI().addItemBag(pl, reward);
            String quantityStr = reward.quantity > 1 ? " x" + reward.quantity : "";
            Service.gI().sendThongBao(pl, "Bạn nhận được " + reward.template.name + quantityStr);
            CombineService.gI().sendEffectOpenItem(pl, boxIcon, reward.template.iconID);
        }
        InventoryService.gI().sendItemBags(pl);
    }

    private void openHalloweenBox2024(Player pl, Item item) {
        if (InventoryService.gI().getCountEmptyBag(pl) <= 0) {
            Service.gI().sendThongBao(pl, "Hành trang đã đầy, cần ít nhất 1 ô trống.");
            return;
        }

        int rand = Util.nextInt(1, 100);
        if (rand <= 70) { // 80% ra đồ rác
            openCommonTrashBox(pl, item, 2);
            return;
        }

        // 20% ra đồ xịn
        short rewardId = 0;
        int[] hsdOptions = { 3, 5, 7, 15 };
        int randomHsd = hsdOptions[Util.nextInt(0, hsdOptions.length - 1)];

        rand = Util.nextInt(1, 100);
        if (rand <= 25) { // Ác ma piccolo
            rewardId = 1926;
        } else if (rand <= 50) { // Mighty mask
            rewardId = 906;
        } else if (rand <= 75) { // Biden dracula
            rewardId = 1106;
        } else { // Pet mông quỷ
            rewardId = 1318;
        }

        Item reward = ItemService.gI().createNewItem(rewardId);
        reward.itemOptions.add(new ItemOption(30, 1)); // Khóa

        switch (rewardId) {
            case 1926: // Ác ma piccolo
                reward.itemOptions.add(new ItemOption(50, 24));
                reward.itemOptions.add(new ItemOption(77, 24));
                reward.itemOptions.add(new ItemOption(103, 24));
                reward.itemOptions.add(new ItemOption(42, 30));
                reward.itemOptions.add(new ItemOption(43, 30));
                reward.itemOptions.add(new ItemOption(44, 30));
                if (Util.isTrue(0, 100)) { // 50% tỉ lệ
                    reward.itemOptions.add(new ItemOption(231, 1));
                }
                break;
            case 906: // Mighty mask
                reward.itemOptions.add(new ItemOption(50, 24));
                reward.itemOptions.add(new ItemOption(14, 3));
                reward.itemOptions.add(new ItemOption(77, 19));
                reward.itemOptions.add(new ItemOption(103, 19));
                reward.itemOptions.add(new ItemOption(94, 19));
                reward.itemOptions.add(new ItemOption(5, 14));
                reward.itemOptions.add(new ItemOption(154, 1));
                break;
            case 1106: // Biden dracula
                reward.itemOptions.add(new ItemOption(50, 21));
                reward.itemOptions.add(new ItemOption(77, 21));
                reward.itemOptions.add(new ItemOption(94, 20));
                reward.itemOptions.add(new ItemOption(16, 25));
                reward.itemOptions.add(new ItemOption(17, 15));
                reward.itemOptions.add(new ItemOption(154, 1));
                break;
            case 1318: // Pet mông quỷ
                reward.itemOptions.add(new ItemOption(77, 15));
                reward.itemOptions.add(new ItemOption(103, 15));
                reward.itemOptions.add(new ItemOption(50, 15));
                reward.itemOptions.add(new ItemOption(8, 3));
                break;
        }

        reward.itemOptions.add(new ItemOption(231, 1)); // HSD ngẫu nhiên

        InventoryService.gI().subQuantityItemsBag(pl, item, 1);
        InventoryService.gI().addItemBag(pl, reward);
        InventoryService.gI().sendItemBags(pl);

        Service.gI().sendThongBao(pl, "Bạn đã nhận được " + reward.template.name);
        CombineService.gI().sendEffectOpenItem(pl, item.template.iconID, reward.template.iconID);
    }

    private Item createCommonTrashReward(Player pl, int tierLevel) {
        int rand = Util.nextInt(1, 100);
        Item reward = null;

        // Tier 1: Hộp rác nhất (CSKB, Hộp thường)
        if (tierLevel == 1) {
            if (rand <= 40) { // 40% - Vàng ít
                int goldAmount = Util.nextInt(5000, 20000);
                pl.inventory.gold += goldAmount;
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                Service.gI().sendThongBao(pl, "Bạn nhận được " + Util.numberToMoney(goldAmount) + " vàng");
                return null;
            } else if (rand <= 60) { // 20% - Đá nâng cấp rác
                short[] daNangCap = { 76, 188, 189, 190 };
                reward = ItemService.gI().createNewItem(daNangCap[Util.nextInt(0, daNangCap.length - 1)]);
            } else if (rand <= 75) { // 15% - Sao pha lê thấp
                int splId = Util.nextInt(441, 443);
                reward = ItemService.gI().createNewItem((short) splId);
                reward.quantity = Util.nextInt(1, 3);
            } else if (rand <= 90) { // 15% - Thùng rác
                reward = ItemService.gI().createNewItem((short) 1891);
            } else { // 10% - Cuốn sách cũ
                reward = ItemService.gI().createNewItem((short) 1283);
            }
        } // Tier 2: Hộp trung bình (Noel, Halloween)
        else if (tierLevel == 2) {
            if (rand <= 25) { // 25% - Đá may mắn
                short[] daMayMan = { 1079, 1080, 1081, 1082 };
                reward = ItemService.gI().createNewItem(daMayMan[Util.nextInt(0, daMayMan.length - 1)]);
            } else if (rand <= 45) { // 20% - Sao pha lê
                int splId = Util.nextInt(441, 445);
                reward = ItemService.gI().createNewItem((short) splId);
                reward.quantity = Util.nextInt(1, 5);
            } else if (rand <= 60) { // 15% - Mảnh thiên sứ
                int mtsId = Util.nextInt(1066, 1070);
                reward = ItemService.gI().createNewItem((short) mtsId);
                reward.quantity = Util.nextInt(1, 5);
            } else if (rand <= 75) { // 15% - Đá nâng cấp
                short[] daNangCap = { 220, 221, 222, 223, 224 };
                reward = ItemService.gI().createNewItem(daNangCap[Util.nextInt(0, daNangCap.length - 1)]);
            } else if (rand <= 85) { // 10% - Thùng rác
                reward = ItemService.gI().createNewItem((short) 1891);
            } else { // 15% - Cuốn sách cũ
                reward = ItemService.gI().createNewItem((short) 1283);
            }
        } // Tier 3: Hộp cao cấp (Trung Thu VIP, SPL)
        else if (tierLevel == 3) {
            if (rand <= 30) { // 30% - Đá nâng cấp cao cấp
                short[] daNangCap = { 1074, 1075, 1076, 1077, 1078 };
                reward = ItemService.gI().createNewItem(daNangCap[Util.nextInt(0, daNangCap.length - 1)]);
                reward.quantity = Util.nextInt(1, 3);
            } else if (rand <= 50) { // 20% - Sao pha lê cao
                int splId = Util.nextInt(441, 447);
                reward = ItemService.gI().createNewItem((short) splId);
                reward.quantity = Util.nextInt(2, 5);
            } else if (rand <= 65) { // 15% - Mảnh thiên sứ
                int mtsId = Util.nextInt(1066, 1070);
                reward = ItemService.gI().createNewItem((short) mtsId);
                reward.quantity = Util.nextInt(3, 10);
            } else if (rand <= 75) { // 10% - Rương SPL
                reward = ItemService.gI().createNewItem((short) 1440);
            } else if (rand <= 85) { // 10% - Mảnh Khỉ
                reward = ItemService.gI().createNewItem((short) 1771);
                reward.quantity = Util.nextInt(1, 5);
            } else if (rand <= 95) { // 10% - Mảnh Rồng
                reward = ItemService.gI().createNewItem((short) 1204);
                reward.quantity = Util.nextInt(1, 3);
            } else { // 5% - Đá may mắn cao cấp
                short[] daMayMan = { 1079, 1080, 1081, 1082, 1083 };
                reward = ItemService.gI().createNewItem(daMayMan[Util.nextInt(0, daMayMan.length - 1)]);
            }
        }
        if (reward != null) {
            reward.itemOptions.add(new ItemOption(73, 0));
        }
        return reward;
    }
}
