package services.func;

import clan.Clan;
import clan.ClanMember;
import consts.ConstMiniGame;
import data.AlyraManager;
import consts.ConstNpc;
import static consts.ConstNpc.CON_SO_MAY_MAN_VANG;
import item.Item;
import map.Zone;
import npc.Npc;
import services.map.NpcManager;
import player.Player;
import network.Message;
import interfaces.ISession;
import server.Client;
import services.Service;
import services.GiftCodeService;
import services.player.InventoryService;
import services.ItemService;
import services.map.NpcService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import managers.GiftCodeManager;
import minigame.LuckyNumberService;
import player.Inventory;
import server.Manager;
import services.player.ClanService;
import services.map.ChangeMapService;
import services.player.PlayerService;
import utils.Logger;
import utils.Util;

public class Input {

    private static final Map<Integer, Object> PLAYER_ID_OBJECT = new HashMap<>();
    private static final Map<Integer, Integer> PLAYER_ITEM_457_INDEX = new HashMap<>();

    public static final int CHANGE_PASSWORD = 500;
    public static final int GIFT_CODE = 501;
    public static final int FIND_PLAYER = 502;
    public static final int CHANGE_NAME = 503;
    public static final int CHOOSE_LEVEL_BDKB = 504;
    public static final int NAP_THE = 505;
    public static final int CHANGE_NAME_BY_ITEM = 506;
    public static final int GIVE_IT = 507;
    public static final int GET_IT = 508;
    public static final int DANGKY = 509;
    public static final int CHOOSE_LEVEL_KGHD = 510;
    public static final int CHOOSE_LEVEL_CDRD = 511;
    public static final int DISSOLUTION_CLAN = 513;
    public static final int TANG_NGOC_HONG = 514;
    public static final int SELECT_LUCKYNUMBER = 515;
    public static final int MUA_VAT_PHAM = 516;
    public static final int TANG_NGOC_XANH = 517; // form tặng item 77 qua item 718

    public static final byte NUMERIC = 0;
    public static final byte ANY = 1;
    public static final byte PASSWORD = 2;
    public static final byte MBV = 23;
    public static final byte BANSLL = 24;
    public static final byte BANGHOI = 25;

    private static Input intance;

    private Input() {

    }

    public static Input gI() {
        if (intance == null) {
            intance = new Input();
        }
        return intance;
    }

    public void doInput(Player player, Message msg) {
        try {
            String[] text = new String[msg.reader().readByte()];
            for (int i = 0; i < text.length; i++) {
                text[i] = msg.reader().readUTF();
            }
            switch (player.idMark.getTypeInput()) {
                case GIVE_IT -> {
                    String name = text[0];
                    int id = Integer.parseInt(text[1]);
                    String opStr = text[2].trim();
                    String prStr = text[3].trim();
                    int q = Integer.parseInt(text[4]);

                    Player targetPlayer = Client.gI().getPlayer(name);
                    if (targetPlayer != null && targetPlayer.inventory != null) {
                        try {
                            // Parse options và params
                            int[] options = parseNumbers(opStr);
                            int[] params = parseNumbers(prStr);

                            // Kiểm tra số lượng option và param phải khớp
                            if (options.length != params.length) {
                                Service.gI().sendThongBao(player,
                                        "Số lượng option (" + options.length + ") và param (" + params.length
                                                + ") không khớp!");
                                return;
                            }

                            // Kiểm tra phải có ít nhất 1 option
                            if (options.length == 0) {
                                Service.gI().sendThongBao(player, "Phải nhập ít nhất 1 option!");
                                return;
                            }

                            Item item = ItemService.gI().createNewItem(((short) id));
                            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                            if (ops != null && !ops.isEmpty()) {
                                item.itemOptions = ops;
                            }
                            item.quantity = q;

                            // Thêm từng option với param tương ứng
                            for (int i = 0; i < options.length; i++) {
                                item.itemOptions.add(new Item.ItemOption(options[i], params[i]));
                            }

                            InventoryService.gI().addItemBag(targetPlayer, item);
                            InventoryService.gI().sendItemBags(targetPlayer);
                            if (item.template != null) {
                                Service.gI().sendThongBao(targetPlayer,
                                        "Nhận " + item.template.name + " từ " + player.name);
                            }
                        } catch (NumberFormatException e) {
                            Service.gI().sendThongBao(player, "Lỗi: Option hoặc Param không hợp lệ!");
                        } catch (Exception e) {
                            Service.gI().sendThongBao(player, "Lỗi khi tạo item: " + e.getMessage());
                        }

                    } else {
                        Service.gI().sendThongBao(player, "Không online");
                    }
                }
                case GET_IT -> {
                    int id = Integer.parseInt(text[0]);
                    String opStr = text[1].trim();
                    String prStr = text[2].trim();
                    int q = Integer.parseInt(text[3]);
                    if (player.isAdmin()) {
                        try {
                            int[] options = parseNumbers(opStr);
                            int[] params = parseNumbers(prStr);

                            if (options.length != params.length) {
                                Service.gI().sendThongBao(player,
                                        "Số lượng option (" + options.length + ") và param (" + params.length
                                                + ") không khớp!");
                                return;
                            }

                            if (options.length == 0) {
                                Service.gI().sendThongBao(player, "Phải nhập ít nhất 1 option!");
                                return;
                            }

                            Item item = ItemService.gI().createNewItem(((short) id));
                            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) id);
                            if (ops != null && !ops.isEmpty()) {
                                item.itemOptions = ops;
                            }
                            item.quantity = q;

                            // Thêm từng option với param tương ứng
                            for (int i = 0; i < options.length; i++) {
                                item.itemOptions.add(new Item.ItemOption(options[i], params[i]));
                            }

                            InventoryService.gI().addItemBag(player, item);
                            InventoryService.gI().sendItemBags(player);
                            Service.gI().sendThongBao(player, "Nhận " + item.template.name + " !");
                        } catch (NumberFormatException e) {
                            Service.gI().sendThongBao(player, "Lỗi: Option hoặc Param không hợp lệ!");
                        } catch (Exception e) {
                            Service.gI().sendThongBao(player, "Lỗi khi tạo item: " + e.getMessage());
                        }

                    } else {
                        Service.gI().sendThongBao(player, "Không đủ quyền hạn!");
                    }
                }
                case SELECT_LUCKYNUMBER -> {
                    int number = Integer.parseInt(text[0]);
                    if (number >= 0 && number <= 99) {
                        LuckyNumberService.addNumber(player, number);
                    } else {
                        Service.gI().sendThongBao(player, "Số phải từ 0 đến 99!");
                    }
                }
                case CHANGE_PASSWORD ->
                    Service.gI().changePassword(player, text[0], text[1], text[2]);
                case GIFT_CODE ->
                    GiftCodeService.gI().giftCode(player, text[0]);
                case FIND_PLAYER -> {
                    Player pl = Client.gI().getPlayer(text[0]);
                    if (pl != null) {
                        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_FIND_PLAYER, -1, "Ngài muốn..?",
                                new String[] { "Đi tới\n" + pl.name, "Gọi " + pl.name + "\ntới đây", "Đổi tên", "Ban",
                                        "Kick" },
                                pl);
                    } else {
                        Service.gI().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                }
                case CHANGE_NAME -> {
                    Player plChanged = (Player) PLAYER_ID_OBJECT.get((int) player.id);
                    if (plChanged != null) {
                        if (AlyraManager.executeQuery("select * from player where name = ?", text[0]).next()) {
                            Service.gI().sendThongBao(player, "Tên nhân vật đã tồn tại");
                        } else {
                            plChanged.name = text[0];
                            AlyraManager.executeUpdate("update player set name = ? where id = ?", plChanged.name,
                                    plChanged.id);
                            Service.gI().player(plChanged);
                            Service.gI().Send_Caitrang(plChanged);
                            Service.gI().sendFlagBag(plChanged);
                            if (plChanged.zone != null && plChanged.location != null) {
                                Zone zone = plChanged.zone;
                                ChangeMapService.gI().changeMap(plChanged, zone, plChanged.location.x,
                                        plChanged.location.y);
                                Service.gI().sendThongBao(plChanged,
                                        "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            } else {
                                Logger.error(
                                        "[Input] zone hoặc location là null khi đổi tên cho player: " + plChanged.name);
                            }
                            Service.gI().sendThongBao(player, "Đổi tên người chơi thành công");
                        }
                    }
                }
                case CHANGE_NAME_BY_ITEM -> {
                    if (AlyraManager.executeQuery("select * from player where name = ?", text[0]).next()) {
                        Service.gI().sendThongBao(player, "Tên nhân vật đã tồn tại");
                        createFormChangeNameByItem(player);
                    } else if (Util.haveSpecialCharacter(text[0])) {
                        Service.gI().sendThongBaoOK(player, "Tên nhân vật không được chứa ký tự đặc biệt");
                    } else if (text[0].length() < 5) {
                        Service.gI().sendThongBaoOK(player, "Tên nhân vật quá ngắn");
                    } else if (text[0].length() > 10) {
                        Service.gI().sendThongBaoOK(player,
                                "Tên nhân vật chỉ đồng ý các ký tự a-z, 0-9 và chiều dài từ 5 đến 10 ký tự");
                    } else {
                        Item theDoiTen = InventoryService.gI().findItemBag(player, (short) 2006);
                        if (theDoiTen == null) {
                            Service.gI().sendThongBao(player, "Không tìm thấy thẻ đổi tên");
                        } else {
                            InventoryService.gI().subQuantityItemsBag(player, theDoiTen, 1);
                            player.name = text[0].toLowerCase();
                            AlyraManager.executeUpdate("update player set name = ? where id = ?", player.name,
                                    player.id);
                            Service.gI().player(player);
                            Service.gI().Send_Caitrang(player);
                            Service.gI().sendFlagBag(player);
                            if (player.zone != null && player.location != null) {
                                Zone zone = player.zone;
                                ChangeMapService.gI().changeMap(player, zone, player.location.x, player.location.y);
                                Service.gI().sendThongBao(player,
                                        "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            } else {
                                Logger.error("[Input] zone hoặc location là null khi đổi tên bằng item cho player: "
                                        + player.name);
                            }
                        }
                    }
                }
                case CHOOSE_LEVEL_BDKB -> {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.QUY_LAO_KAME, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_BDKB,
                                    "Con có chắc muốn đến\nhang kho báu cấp độ " + level + " ?",
                                    new String[] { "Đồng ý", "Từ chối" }, level);
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                case CHOOSE_LEVEL_KGHD -> {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.MR_POPO, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, 2,
                                    "Cậu có chắc muốn đến\nDestron Gas cấp độ " + level + " ?",
                                    new String[] { "Đồng ý", "Từ chối" }, level);
                        }
                    }
                }
                case CHOOSE_LEVEL_CDRD -> {
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.THAN_VU_TRU, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, 3,
                                    "Con có chắc muốn đến\ncon đường rắn độc cấp độ " + level + " ?",
                                    new String[] { "Đồng ý", "Từ chối" }, level);
                        }
                    }
                }
                case MBV -> {
                    int mbv = Integer.parseInt(text[0]);
                    int nmbv = Integer.parseInt(text[1]);
                    int rembv = Integer.parseInt(text[2]);
                    if ((mbv + "").length() != 6 || (nmbv + "").length() != 6 || (rembv + "").length() != 6) {
                        Service.gI().sendThongBao(player, "Trêu bố mày à?");
                    } else if (player.mbv == 0) {
                        Service.gI().sendThongBao(player, "Bạn chưa cài mã bảo vệ!");
                    } else if (player.mbv != mbv) {
                        Service.gI().sendThongBao(player, "Mã bảo vệ không đúng");
                    } else if (nmbv != rembv) {
                        Service.gI().sendThongBao(player, "Mã bảo vệ không trùng khớp");
                    } else {
                        player.mbv = nmbv;
                        Service.gI().sendThongBao(player, "Đổi mã bảo vệ thành công!");
                    }
                }
                case BANSLL -> {
                    int sltv;
                    try {
                        sltv = Integer.parseInt(text[0]);
                    } catch (NumberFormatException e) {
                        Service.gI().sendThongBao(player, "Số lượng không hợp lệ.");
                        return;
                    }

                    if (sltv <= 0) {
                        Service.gI().sendThongBao(player, "Số lượng bán phải lớn hơn 0.");
                        return;
                    }
                    Integer savedIndex = PLAYER_ITEM_457_INDEX.get((int) player.id);
                    int itemIndex = (savedIndex != null) ? savedIndex : -1;
                    if (itemIndex < 0 || itemIndex >= player.inventory.itemsBag.size()) {
                        Service.gI().sendThongBao(player, "Không tìm thấy Thỏi vàng để bán.");
                        PLAYER_ITEM_457_INDEX.remove((int) player.id);
                        return;
                    }
                    Item thoiVang = player.inventory.itemsBag.get(itemIndex);

                    if (thoiVang == null || !thoiVang.isNotNullItem() || thoiVang.template == null
                            || thoiVang.template.id != 457) {
                        Service.gI().sendThongBao(player, "Không tìm thấy Thỏi vàng để bán.");
                        PLAYER_ITEM_457_INDEX.remove((int) player.id);
                        return;
                    }
                    if (thoiVang.quantity <= 0) {
                        Service.gI().sendThongBao(player, "Bạn không có Thỏi vàng để bán.");
                        PLAYER_ITEM_457_INDEX.remove((int) player.id);
                        return;
                    }

                    if (thoiVang.quantity < sltv) {
                        Service.gI().sendThongBao(player,
                                "Bạn chỉ có " + thoiVang.quantity + " Thỏi vàng. Không đủ để bán " + sltv + " thỏi.");
                        return;
                    }

                    long costPerItem = 500_000_000L;
                    long totalCost = (long) sltv * costPerItem;

                    if (player.inventory.gold >= Inventory.LIMIT_GOLD) {
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player, "Bạn đã đạt giới hạn vàng, không thể bán thêm.");
                        return;
                    }
                    long currentGold = player.inventory.gold;
                    long newGoldAmount = currentGold + totalCost;
                    if (newGoldAmount > Inventory.LIMIT_GOLD) {
                        long remainingCapacity = Inventory.LIMIT_GOLD - currentGold;
                        int maxSellableQuantity = (int) (remainingCapacity / costPerItem);

                        if (maxSellableQuantity < 1) {
                            Service.gI().sendThongBao(player,
                                    "Vàng sau khi bán sẽ vượt quá giới hạn. Bạn không thể bán thêm Thỏi vàng nào vào lúc này.");
                        } else {
                            maxSellableQuantity = Math.min(maxSellableQuantity, thoiVang.quantity);
                            Service.gI().sendThongBao(player,
                                    "Vàng sau khi bán sẽ vượt giới hạn. Bạn chỉ có thể bán tối đa "
                                            + maxSellableQuantity + " Thỏi vàng.");
                        }
                        return;
                    }
                    Item itemCheck = player.inventory.itemsBag.get(itemIndex);
                    if (itemCheck == null || !itemCheck.isNotNullItem() || itemCheck.template == null
                            || itemCheck.template.id != 457 || itemCheck.quantity < sltv) {
                        Service.gI().sendThongBao(player, "Thỏi vàng đã bị thay đổi, vui lòng thử lại.");
                        PLAYER_ITEM_457_INDEX.remove((int) player.id);
                        return;
                    }
                    InventoryService.gI().subQuantityItemsBagByIndex(player, itemIndex, sltv);
                    InventoryService.gI().sendItemBags(player);

                    currentGold = player.inventory.gold;
                    newGoldAmount = currentGold + totalCost;
                    if (newGoldAmount > Inventory.LIMIT_GOLD) {
                        player.inventory.gold = Inventory.LIMIT_GOLD;
                    } else {
                        player.inventory.gold = newGoldAmount;
                    }
                    Service.gI().sendMoney(player);
                    Service.gI().sendThongBao(player,
                            "Đã bán " + sltv + " Thỏi vàng, thu được " + Util.numberToMoney(totalCost) + " vàng.");

                    PLAYER_ITEM_457_INDEX.remove((int) player.id);
                }
                case TANG_NGOC_HONG -> {
                    Player pl = Client.gI().getPlayer(text[0]);
                    int numruby = Integer.parseInt((text[1]));
                    if (pl != null) {
                        if (numruby > 0 && player.inventory.ruby >= numruby) {
                            Item item = InventoryService.gI().findItemBag(player, (short) 2002);
                            player.inventory.subGem(numruby);
                            PlayerService.gI().sendInfoHpMpMoney(player);
                            pl.inventory.ruby += numruby;
                            PlayerService.gI().sendInfoHpMpMoney(pl);
                            Service.gI().sendThongBao(player, "Tặng ngọc thành công");
                            Service.gI().sendThongBao(pl,
                                    "Bạn được " + player.name + " tặng " + numruby + " ngọc xanh");
                            if (item != null) {
                                InventoryService.gI().subQuantityItemsBag(player, item, 1);
                                InventoryService.gI().sendItemBags(player);
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Không đủ ngọc xanh để tặng");
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                }
                case TANG_NGOC_XANH -> {
                    String targetName = text[0];
                    int quantity;
                    try {
                        quantity = Integer.parseInt(text[1]);
                    } catch (NumberFormatException e) {
                        Service.gI().sendThongBao(player, "Số lượng không hợp lệ");
                        return;
                    }
                    if (quantity <= 0 || quantity % 10 != 0) {
                        Service.gI().sendThongBao(player, "Số ngọc phải > 0 và là bội số của 10");
                        return;
                    }
                    Player target = Client.gI().getPlayer(targetName);
                    if (target == null) {
                        Service.gI().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                        return;
                    }
                    if (player.inventory.gem < quantity) {
                        Service.gI().sendThongBao(player, "Không đủ ngọc xanh để tặng");
                        return;
                    }
                    int ticketsNeeded = (quantity + 9) / 10;
                    Item ticket = InventoryService.gI().findItemBag(player, (short) 718);
                    if (ticket == null || ticket.quantity < ticketsNeeded) {
                        Service.gI().sendThongBao(player,
                                "Cần " + ticketsNeeded + " vé để tặng " + quantity + " ngọc xanh");
                        return;
                    }
                    player.inventory.subGem(quantity);
                    services.player.PlayerService.gI().sendInfoHpMpMoney(player);
                    target.inventory.gem += quantity;
                    services.player.PlayerService.gI().sendInfoHpMpMoney(target);
                    InventoryService.gI().subQuantityItemsBag(player, ticket, ticketsNeeded);
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendThongBao(player, "Đã tặng " + quantity + " ngọc xanh cho " + target.name
                            + " (đã dùng " + ticketsNeeded + " vé)");
                    Service.gI().sendThongBao(target, "Bạn được " + player.name + " tặng " + quantity + " ngọc xanh");
                }
                case BANGHOI -> {
                    Clan clan = player.clan;
                    if (clan != null) {
                        ClanMember cm = clan.getClanMember((int) player.id);
                        if (clan.isLeader(player)) {
                            if (clan.canUpdateClan(player)) {
                                String tenvt = text[0];
                                if (!Util.haveSpecialCharacter(tenvt) && tenvt.length() > 1 && tenvt.length() < 5) {
                                    clan.name2 = tenvt;
                                    clan.update();
                                    Service.gI().sendThongBao(player, "[" + tenvt + "] OK");
                                } else {
                                    Service.gI().sendThongBaoOK(player,
                                            "Chỉ chấp nhận các ký tự a-z, 0-9 và chiều dài từ 2 đến 4 ký tự");
                                }
                            }
                        }
                    }
                }
                case DISSOLUTION_CLAN -> {
                    String xacNhan = text[0];
                    Clan clan;
                    if (xacNhan.equalsIgnoreCase("OK")) {
                        clan = player.clan;
                        if (clan != null && clan.isLeader(player)) {
                            clan.deleteDB(clan.id);
                            Manager.CLANS.remove(clan);
                            player.clan = null;
                            player.clanMember = null;
                            ClanService.gI().sendMyClan(player);
                            ClanService.gI().sendClanId(player);
                            Service.gI().sendThongBao(player, "Bang hội đã giải tán thành công.");
                        } else {
                            Service.gI().sendThongBao(player, "Không thể thực hiện.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createForm(Player pl, int typeInput, String title, SubInput... subInputs) {
        pl.idMark.setTypeInput(typeInput);
        Message msg = null;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void createForm(ISession session, int typeInput, String title, SubInput... subInputs) {
        if (session instanceof Player player) {
            player.idMark.setTypeInput(typeInput);
        }
        Message msg = null;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            session.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void createFormChangePassword(Player pl) {
        createForm(pl, CHANGE_PASSWORD, "Đổi mật khẩu", new SubInput("Mật khẩu cũ", PASSWORD),
                new SubInput("Mật khẩu mới", PASSWORD),
                new SubInput("Nhập lại mật khẩu mới", PASSWORD));
    }

    public void createFormGiveItem(Player pl) {
        createForm(pl, GIVE_IT, "Tặng vật phẩm", new SubInput("Tên", ANY), new SubInput("Id Item", ANY),
                new SubInput("ID OPTION (cách nhau bởi khoảng trắng, VD: 30 31)", ANY),
                new SubInput("PARAM (tương ứng với option, VD: 100 200)", ANY),
                new SubInput("Số lượng", ANY));
    }

    public void createFormGetItem(Player pl) {
        createForm(pl, GET_IT, "Get vật phẩm", new SubInput("Id Item", ANY),
                new SubInput("ID OPTION (cách nhau bởi khoảng trắng, VD: 30 31)", ANY),
                new SubInput("PARAM (tương ứng với option, VD: 100 200)", ANY),
                new SubInput("Số lượng", ANY));
    }

    public void createFormGiftCode(Player pl) {
        GiftCodeManager.gI().reloadGiftCodeForPlayer(pl);
        switch (pl.zone.map.mapId) {
            case 5, 20, 13 ->
                createForm(pl, GIFT_CODE, "Mã quà tặng", new SubInput("Gift Code", ANY));
            case 47 ->
                createForm(pl, GIFT_CODE, "Nhập mã Gift Code gồm 16 chữ số",
                        new SubInput("Nhập Gift Code gồm 16 chữ số", ANY));
            default ->
                createForm(pl, GIFT_CODE, "Nhập Giftcode", new SubInput("Gift-code", ANY));
        }
    }

    public void createFormMBV(Player pl) {
        createForm(pl, MBV, "Đồ ngu! Đồ ăn hại! Cút mẹ mày đi!", new SubInput("Nhập Mã Bảo Vệ Đã Quên", NUMERIC),
                new SubInput("Nhập Mã Bảo Vệ Mới", NUMERIC), new SubInput("Nhập Lại Mã Bảo Vệ Mới", NUMERIC));
    }

    public void createFormBangHoi(Player pl) {
        createForm(pl, BANGHOI, "Nhập tên viết tắt bang hội", new SubInput("Tên viết tắt từ 2 đến 4 kí tự", ANY));
    }

    public void createFormFindPlayer(Player pl) {
        createForm(pl, FIND_PLAYER, "Tìm kiếm người chơi", new SubInput("Tên người chơi", ANY));
    }

    public void createFormNapThe(Player pl, byte loaiThe) {
        pl.idMark.setLoaiThe(loaiThe);
        createForm(pl, NAP_THE, "Nạp thẻ", new SubInput("Mã thẻ", ANY), new SubInput("Seri", ANY));
    }

    public void createFormTangRuby(Player pl) {
        createForm(pl, TANG_NGOC_HONG, "Tặng ngọc", new SubInput("Tên nhân vật", ANY),
                new SubInput("Số Hồng Ngọc Muốn Tặng", NUMERIC));
    }

    public void createFormTangNgocxanh(Player pl) {
        createForm(pl, TANG_NGOC_XANH, "Vé tặng ngọc (mỗi 10 ngọc tốn 1 vé)", new SubInput("Tên nhân vật", ANY),
                new SubInput("Số ngọc muốn tặng", NUMERIC));
    }

    public void createFormChangeName(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChangeNameByItem(Player pl) {
        createForm(pl, CHANGE_NAME_BY_ITEM, "Đổi tên " + pl.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChooseLevelBDKB(Player pl) {
        createForm(pl, CHOOSE_LEVEL_BDKB, "Hãy chọn cấp độ hang kho báu từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelCDRD(Player pl) {
        createForm(pl, CHOOSE_LEVEL_CDRD, "Hãy chọn cấp độ từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormChooseLevelKGHD(Player pl) {
        createForm(pl, CHOOSE_LEVEL_KGHD, "Hãy chọn cấp độ từ 1-110", new SubInput("Cấp độ", NUMERIC));
    }

    public void createFormBanSLL(Player pl) {
        createFormBanSLL(pl, -1);
    }

    /**
     * Tạo form bán thỏi vàng (item 457)
     *
     * @param pl        Player
     * @param itemIndex Index của item trong itemsBag, -1 nếu không có (tìm tự
     *                  động)
     */
    public void createFormBanSLL(Player pl, int itemIndex) {
        short itemIdThoiVang = 457;
        long costPerThoiVang = 500_000_000L;
        int maxSellableQuantity = 0;
        int soLuongDangCo = 0;
        int actualIndex = -1;

        // Kiểm tra và lấy item theo index nếu có, hoặc tìm item đầu tiên
        if (itemIndex >= 0 && itemIndex < pl.inventory.itemsBag.size()) {
            Item item = pl.inventory.itemsBag.get(itemIndex);
            if (item != null && item.isNotNullItem() && item.template.id == itemIdThoiVang) {
                soLuongDangCo = item.quantity;
                actualIndex = itemIndex;
            }
        }

        // Nếu không tìm thấy theo index, tìm item đầu tiên (backward compatibility)
        if (actualIndex == -1) {
            Item thoiVang = InventoryService.gI().findItemBag(pl, itemIdThoiVang);
            if (thoiVang != null) {
                soLuongDangCo = thoiVang.quantity;
                // Tìm index của item này
                for (int i = 0; i < pl.inventory.itemsBag.size(); i++) {
                    if (pl.inventory.itemsBag.get(i) == thoiVang) {
                        actualIndex = i;
                        break;
                    }
                }
            }
        }
        if (actualIndex >= 0) {
            PLAYER_ITEM_457_INDEX.put((int) pl.id, actualIndex);
        } else {
            PLAYER_ITEM_457_INDEX.remove((int) pl.id);
        }
        if (soLuongDangCo > 0 && pl.inventory.gold < Inventory.LIMIT_GOLD) {
            long remainingGoldCapacity = Inventory.LIMIT_GOLD - pl.inventory.gold;

            if (remainingGoldCapacity >= costPerThoiVang) {
                int sellableByGoldLimit = (int) (remainingGoldCapacity / costPerThoiVang);
                maxSellableQuantity = Math.min(sellableByGoldLimit, soLuongDangCo);
            }
        }

        StringBuilder prompt = new StringBuilder("Bạn muốn bán bao nhiêu [Thỏi vàng]?");
        if (soLuongDangCo == 0) {
            prompt.append("\n(Bạn không có Thỏi vàng nào để bán)");
        } else if (maxSellableQuantity == 0) {
            prompt.append("\n(Vàng đã gần hoặc đạt giới hạn, không thể bán thêm)");
        } else {
            prompt.append("\n(Hiện có: ").append(soLuongDangCo)
                    .append(", Tối đa bán được: ").append(maxSellableQuantity).append(")");
        }

        createForm(pl, BANSLL, prompt.toString(), new SubInput("Số lượng", NUMERIC));
    }

    public void createFormGiaiTanBangHoi(Player pl) {
        createForm(pl, DISSOLUTION_CLAN, "Nhập OK để xác nhận giải tán bang hội.", new SubInput("", ANY));
    }

    public void creatFormLumberLucky(Player pl, boolean isGem) {
        createForm(pl, CON_SO_MAY_MAN_VANG,
                isGem ? "Hãy chọn 1 số từ 0 đến 99 giá 450 ngọc" : "Hãy chọn 1 số từ 0 đến 99 giá 1.000.000 vàng",
                new SubInput("Số bạn chọn", NUMERIC));
    }

    public void createFormSelectOneNumberLuckyNumber(Player pl, boolean isGem) {
        createForm(pl, SELECT_LUCKYNUMBER, isGem
                ? "Hãy chọn 1 số từ 0 đến 99 giá " + Util.number(ConstMiniGame.COST_PLAY_GEM) + " ngọc"
                : "Hãy chọn 1 số từ 0 đến 99 giá " + Util.number(ConstMiniGame.COST_PLAY_GOLD) + " vàng",
                new SubInput("Số bạn chọn", NUMERIC));
    }

    private int[] parseNumbers(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new int[0];
        }

        String[] parts = input.trim().split("\\s+");
        int[] numbers = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            numbers[i] = Integer.parseInt(parts[i].trim());
        }

        return numbers;
    }

    public static class SubInput {

        private String name;
        private byte typeInput;

        public SubInput(String name, byte typeInput) {
            this.name = name;
            this.typeInput = typeInput;
        }
    }
}
