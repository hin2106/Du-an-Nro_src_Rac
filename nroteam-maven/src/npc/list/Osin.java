package npc.list;

import consts.ConstNpc;
import item.Item;
import item.ItemTime;
import npc.Npc;
import player.Player;
import services.dungeon.MajinBuu14HService;
import services.dungeon.MajinBuuService;
import services.map.ChangeMapService;
import services.map.NpcService;
import services.player.InventoryService;
import utils.TimeUtil;
import utils.Util;

import java.util.ArrayList;
import java.util.List;
import services.ItemTimeService;
import services.Service;
import services.ShopService;
import services.TaskService;

public class Osin extends Npc {
    
    private static final int[] MAP_IDS_MABU_DUNGEON = {114, 115, 117, 118, 119, 120};
    
    public Osin(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) return;
        TaskService.gI().checkDoneTaskTalkNpc(player, this);
        switch (this.mapId) {
            case 50 -> createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                        "Đến\nKaio", "Đến\nhành tinh\nBill", "Từ chối");
            case 154 -> createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                        "Đến\nhành tinh\nngục tù", "Từ chối");
            case 155 -> createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                        "Quay về", "Từ chối");
            case 52 -> {
                player.fightMabu.clear();
                if (TimeUtil.isMabu14HOpen()) {
                    createOtherMenu(player, ConstNpc.MENU_OPEN_MMB, "Mabư đã thoát khỏi vỏ bọc\nmau đi cùng ta ngăn chặn hắn lại\ntrước khi hắn tàn phá trái đất này",
                            "OK", "Bình hút\nNăng lượng", "Từ chối");
                } else if (TimeUtil.isMabuOpen()) {
                    createOtherMenu(player, ConstNpc.MENU_OPEN_MMB, "Bây giờ tôi sẽ bí mật...\nđuổi theo 2 tên đồ tể...\nQuý vị nào muốn đi theo thì xin mời !",
                            "OK", "Bình hút\nNăng lượng", "Từ chối");
                } else {
                    createOtherMenu(player, ConstNpc.MENU_NOT_OPEN_MMB,
                            "Vào lúc " + MajinBuuService.HOUR_OPEN_MAP_MABU + "h tôi sẽ bí mật...\nđuổi theo 2 tên đồ tể...\nQuý vị nào muốn đi theo thì xin mời !",
                            "OK", "Bình hút\nNăng lượng", "Từ chối");
                }
            }
            case 114, 115, 117, 118, 119, 120 -> {
                if (player.cFlag != 9) {
                    NpcService.gI().createTutorial(player, this.tempId, this.avartar, "Ngươi hãy về phe của mình mà thể hiện");
                    return;
                }
                String npcSayMabu = "Đừng vội xem thường Babiđây, ngay đến cha hắn là thần ma đạo sĩ Bibiđây khi còn sống cũng phải sợ hắn đấy!";
                List<String> menuMabu = new ArrayList<>();
                menuMabu.add("Hướng\ndẫn\nthêm");
                if (!player.itemTime.isUseGTPT) {
                    menuMabu.add("Giải trừ\nphép thuật\n1 ngọc");
                }
                if (player.fightMabu.pointMabu >= player.fightMabu.POINT_MAX && this.mapId != 120) {
                    menuMabu.add("Xuống\nTầng dưới");
                }
                createOtherMenu(player, ConstNpc.GO_UPSTAIRS_MENU, npcSayMabu, menuMabu.toArray(String[]::new));
            }
            case 127 -> {
                String npcSayKaio = player.isPhuHoMapMabu ?
                        "Ta có thể giúp gì cho ngươi ?" :
                        "Ta sẽ phù hộ ngươi bằng\nnguồn sức mạnh của Thần Kaiô\n+1 triệu HP, +1 triệu KI, +10k Sức đánh\nLưu ý: sức mạnh này sẽ biến mất khi ngươi rời khỏi đây";
                List<String> menuKaio = new ArrayList<>();
                if (!player.isPhuHoMapMabu) {
                    menuKaio.add("Phù hộ\n10 ngọc");
                }
                menuKaio.add("Từ chối");
                menuKaio.add("Về\nĐại Hội\nVõ Thuật");
                createOtherMenu(player, ConstNpc.BASE_MENU, npcSayKaio, menuKaio.toArray(String[]::new));
            }
            case 165 -> createOtherMenu(player, ConstNpc.BASE_MENU, "Mau hút năng lượng tà ác trong người cậu ấy (Dùng tự động luyện tập)","Về nhà","Bùa hỗ trợ", "Từ chối");
            default -> super.openBaseMenu(player);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) return;

        int currentMenuId = player.idMark.getIndexMenu();

        switch (this.mapId) {
            case 50 -> {
                if (player.idMark.isBaseMenu()) {
                    if (select == 0) {
                        ChangeMapService.gI().changeMap(player, 48, -1, 367, 36);
                    } else if (select == 1) {
                        ChangeMapService.gI().changeMap(player, 154, -1, 833, 156);
                    }
                }
            }
            case 154 -> {
                if (player.idMark.isBaseMenu() && select == 0) {
                        ChangeMapService.gI().changeMap(player, 155, -1, 111, 792);
                    }
            }
            case 155 -> {
                if (player.idMark.isBaseMenu() && select == 0) {
                    ChangeMapService.gI().changeMap(player, 154, -1, 200, 312);
                }
            }
            case 52 -> {
                if (currentMenuId == ConstNpc.MENU_OPEN_MMB || currentMenuId == ConstNpc.MENU_NOT_OPEN_MMB) {
                    if (select == 0) {
                        if (TimeUtil.isMabu14HOpen()) {
                            MajinBuu14HService.gI().joinMaBu2H(player);
                        } else if (TimeUtil.isMabuOpen()) {
                            ChangeMapService.gI().changeMap(player, MAP_IDS_MABU_DUNGEON[0], -1, Util.nextInt(100, 500), 336);
                        } else {
                            Service.gI().sendThongBao(player, "Chưa đến giờ sự kiện diễn ra.");
                        }
                    } else if (select == 1) {
                        createOtherMenu(player, ConstNpc.MENU_BINH_HUT_NANG_LUONG,
                                "Cađíc đã bị phù thủy Babidi thôi miên\nhãy mang Bình Hút Năng Lượng đến\nhút cạn năng lượng tà ác trong cậu ấy",
                                "OK", "Từ chối");
                    }
                } else if (currentMenuId == ConstNpc.MENU_BINH_HUT_NANG_LUONG) {
                    if (select == 0) {
                        Item item = InventoryService.gI().findItemBag(player, 1852);
                        if (item != null) {
                            ChangeMapService.gI().changeMap(player, 165, -1, 111, 792);
                            Service.gI().sendThongBao(player, "Hãy tìm Cađíc và sử dụng Bình Hút Năng Lượng!");
                        } else {
                            Service.gI().sendThongBao(player, "Bạn cần có Bình Hút Năng Lượng để đến khu vực này!");
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Cađíc sẽ phải tự mình chiến đấu với năng lượng tà ác vậy...");
                    }
                }
            }

            case 114, 115, 117, 118, 119, 120 -> {
                if (player.cFlag != 9) return;
                if (currentMenuId == ConstNpc.GO_UPSTAIRS_MENU) {
                    String actionGuide = "GUIDE";
                    String actionDispel = "DISPEL";
                    String actionNextFloor = "NEXT_FLOOR";

                    List<String> possibleActions = new ArrayList<>();
                    possibleActions.add(actionGuide);
                    if (!player.itemTime.isUseGTPT) {
                        possibleActions.add(actionDispel);
                    }
                    if (player.fightMabu.pointMabu >= player.fightMabu.POINT_MAX && this.mapId != 120) {
                        possibleActions.add(actionNextFloor);
                    }

                    if (select < 0 || select >= possibleActions.size()) {
                        return;
                    }
                    String selectedAction = possibleActions.get(select);

                    if (actionGuide.equals(selectedAction)) {
                        NpcService.gI().createTutorial(player, this.tempId, 4388, ConstNpc.HUONG_DAN_MAP_MA_BU);
                    } else if (actionDispel.equals(selectedAction)) {
                        if (player.inventory.getGem() >= 1) {
                            player.inventory.subGem(1);
                            player.itemTime.lastTimeUseGTPT = System.currentTimeMillis();
                            player.itemTime.isUseGTPT = true;
                            ItemTimeService.gI().sendAllItemTime(player);
                            Service.gI().sendThongBao(player, "Phép thuật đã được giải trừ, sức đánh của bạn đã tăng theo điểm tích lũy");
                        } else {
                            Service.gI().sendThongBao(player, "Bạn không đủ ngọc để giải trừ phép thuật.");
                        }
                    } else if (actionNextFloor.equals(selectedAction)) {
                        ChangeMapService.gI().changeMap(player, map.mapIdNextMabu((short) this.mapId), -1, this.cx, this.cy);
                    }
                }
            }

            case 127 -> {
                if (currentMenuId == ConstNpc.BASE_MENU) {
                    List<String> possibleActionsKaio = new ArrayList<>();
                    String actionPhuHo = "PHU_HO";
                    String actionTuChoi = "TU_CHOI";
                    String actionVeDHVT = "VE_DHVT";

                    if (!player.isPhuHoMapMabu) {
                        possibleActionsKaio.add(actionPhuHo);
                    }
                    possibleActionsKaio.add(actionTuChoi);
                    possibleActionsKaio.add(actionVeDHVT);

                    if (select < 0 || select >= possibleActionsKaio.size()) {
                        return;
                    }
                    String selectedActionKaio = possibleActionsKaio.get(select);

                    if (actionPhuHo.equals(selectedActionKaio)) {
                        if (player.inventory.getGem() < 10) {
                            Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                        } else {
                            player.inventory.subGem(10);
                            player.isPhuHoMapMabu = true;
                            player.nPoint.calPoint();
                            Service.gI().sendThongBao(player, "Bạn đã được phù hộ!");

                            player.nPoint.setHp(player.nPoint.hpMax);
                            player.nPoint.setMp(player.nPoint.mpMax);
                            Service.gI().point(player);
                            Service.gI().Send_Info_NV(player);
                        }
                    } else if (actionVeDHVT.equals(selectedActionKaio)) {
                        ChangeMapService.gI().changeMap(player, 52, -1, Util.nextInt(100, 300), 336);
                    }
                }
            }
            case 165 -> {
                    if (currentMenuId == ConstNpc.BASE_MENU) {
                        if (select == 0) {
                            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, 0, -1);
                        } else if (select == 1) {
                            createOtherMenu(player, ConstNpc.MENU_CONFIRM_BUY_CHAMR,
                                "Ta có thể làm phép giúp ngươi\nhút nhanh hơn gấp đôi năng lượng tà ác\ntrong 10p",
                                "Đồng ý\n5 ngọc\n 10 phút","Đồng ý\n28 ngọc\n 60 phút","Đồng ý\n250 ngọc\n 530 phút", "Từ chối");
                        }
                    } else if (currentMenuId == ConstNpc.MENU_CONFIRM_BUY_CHAMR) {
                        if (select == 0 || select == 1 || select == 2) {
                            int cost;
                            int addMinutes;
                            switch (select) {
                                case 0 -> {
                                    cost = 5;
                                    addMinutes = 10;
                                }
                                case 1 -> {
                                    cost = 28;
                                    addMinutes = 60;
                                }
                                default -> {
                                    cost = 250;
                                    addMinutes = 530;
                                }
                            }
                            if (player.inventory.getGem() < cost) {
                                Service.gI().sendThongBao(player, "Bạn không đủ ngọc");
                                return;
                            }
                            long now = System.currentTimeMillis();
                            long currentRemain = player.itemTime.isUseNangLuong
                                    ? (ItemTime.TIME_ITEM - (now - player.itemTime.lastTimeNangLuong))
                                    : 0;
                            long addMillis = addMinutes * 60L * 1000;
                            long capMillis = 530L * 60 * 1000;
                            if (currentRemain >= capMillis) {
                                Service.gI().sendThongBao(player, "Bạn đã đạt tối đa 530 phút bùa hút nhanh.");
                                return;
                            }
                            if (currentRemain + addMillis > capMillis) {
                                long canBuyMore = (capMillis - currentRemain) / 60000;
                                Service.gI().sendThongBao(player, "Bạn chỉ có thể mua thêm " + canBuyMore + " phút (tối đa 530 phút).");
                                return;
                            }
                            long newRemain = currentRemain + addMillis;
                            player.inventory.subGem(cost);
                            Service.gI().sendMoney(player);
                            player.itemTime.isUseNangLuong = true;
                            player.itemTime.lastTimeNangLuong = now - (ItemTime.TIME_ITEM - newRemain);
                            ItemTimeService.gI().sendAllItemTime(player);
                        }
                    }
            }
            default -> {
            }
        }
    }
}