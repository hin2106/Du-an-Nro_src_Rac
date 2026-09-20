package npc;

import boss.Boss;
import boss.BossID;
import npc.list.Whis;
import npc.list.LinhCanh;
import npc.list.Bulma;
import npc.list.Cargo;
import npc.list.ThanVuTru;
import npc.list.Tapion;
import npc.list.DocNhan;
import npc.list.TruongLaoGuru;
import npc.list.Rong5Sao;
import npc.list.Santa;
import npc.list.Cui;
import npc.list.ToSuKaio;
import npc.list.Kibit;
import npc.list.Appule;
import npc.list.QuaTrung;
import npc.list.KyGui;
import npc.list.Rong7Sao;
import npc.list.Potage;
import npc.list.Babiday;
import npc.list.OngGohan;
import npc.list.DrDrief;
import npc.list.Karin;
import npc.list.LyTieuNuong;
import npc.list.Rong1Sao;
import npc.list.MrPoPo;
import npc.list.Bill;
import npc.list.Rong6Sao;
import npc.list.Bardock;
import npc.list.Uron;
import npc.list.VuaVegeta;
import npc.list.GokuSSJ;
import npc.list.RongOmega;
import npc.list.BulmaTuongLai;
import npc.list.GhiDanh;
import npc.list.QuyLaoKame;
import npc.list.Calick;
import npc.list.Rong2Sao;
import npc.list.BaHatMit;
import npc.list.RuongDo;
import npc.list.ThuongDe;
import npc.list.OngParagus;
import npc.list.GiuMaDauBo;
import npc.list.Vados;
import npc.list.TrongTai;
import npc.list.Rong4Sao;
import npc.list.Rong3Sao;
import npc.list.BoMong;
import npc.list.Dende;
import npc.list.Jaco;
import npc.list.QuocVuong;
import npc.list.Osin;
import npc.list.DauThan;
import npc.list.OngMoori;
import npc.list.DaiThienSu;
import npc.list.GokuSSJ2;
import services.ConsignShopService;
import services.player.ClanService;
import services.Service;
import services.ItemService;
import services.dungeon.NgocRongNamecService;
import services.player.IntrinsicService;
import services.player.InventoryService;
import services.map.NpcService;
import services.PetService;
import services.player.PlayerService;
import services.player.FriendAndEnemyService;
import consts.ConstNpc;
import managers.boss.BossManager;
import clan.Clan;

import java.util.HashMap;

import services.map.ChangeMapService;

import player.Player;
import item.Item;
import matches.PVPService;
import npc.list.ChiChi;
import npc.list.DrMyuu;
import server.Client;
import server.Maintenance;
import server.Manager;
import services.func.Input;
import utils.Logger;
import utils.Util;
import services.dungeon.SuperDivineWaterService;
import npc.list.RuongSuuTam;
import npc.list.ToriBot;
import services.DisguisePurchaseService;
import services.DisguisePurchaseService.DisguisePurchaseInfo;
import services.shenron.SummonDragon;
import static services.shenron.SummonDragon.SHENRON_1_STAR_WISHES_1;
import static services.shenron.SummonDragon.SHENRON_1_STAR_WISHES_2;
import static services.shenron.SummonDragon.SHENRON_1_STAR_WISHES_3;
import static services.shenron.SummonDragon.SHENRON_SAY;
import services.shenron.SummonDragonBonney;
import services.shenron.SummonDragonNamek;

public class NpcFactory {

    public static final java.util.Map<Long, Object> PLAYERID_OBJECT = new HashMap<>();

    public static Npc createNPC(int mapId, int status, int cx, int cy, int tempId) {
        int avatar = Manager.NPC_TEMPLATES.get(tempId).avatar;
        try {
            return switch (tempId) {
                case ConstNpc.GHI_DANH ->
                    new GhiDanh(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TRONG_TAI ->
                    new TrongTai(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.POTAGE ->
                    new Potage(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.MR_POPO ->
                    new MrPoPo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.QUY_LAO_KAME ->
                    new QuyLaoKame(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TRUONG_LAO_GURU ->
                    new TruongLaoGuru(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.VUA_VEGETA ->
                    new VuaVegeta(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CUA_HANG_KY_GUI ->
                    new KyGui(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.ONG_GOHAN ->
                    new OngGohan(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.ONG_MOORI ->
                    new OngMoori(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.ONG_PARAGUS ->
                    new OngParagus(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BUNMA ->
                    new Bulma(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DENDE ->
                    new Dende(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.APPULE ->
                    new Appule(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DR_DRIEF ->
                    new DrDrief(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CARGO ->
                    new Cargo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CUI ->
                    new Cui(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.SANTA ->
                    new Santa(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.URON ->
                    new Uron(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BA_HAT_MIT ->
                    new BaHatMit(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RUONG_DO ->
                    new RuongDo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DAU_THAN ->
                    new DauThan(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CALICK ->
                    new Calick(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.JACO ->
                    new Jaco(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.THUONG_DE ->
                    new ThuongDe(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.VADOS ->
                    new Vados(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.THAN_VU_TRU ->
                    new ThanVuTru(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.KIBIT ->
                    new Kibit(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.OSIN ->
                    new Osin(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TORIBOT ->
                    new ToriBot(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BABIDAY ->
                    new Babiday(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.LY_TIEU_NUONG ->
                    new LyTieuNuong(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.LINH_CANH ->
                    new LinhCanh(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.QUA_TRUNG ->
                    new QuaTrung(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.QUOC_VUONG ->
                    new QuocVuong(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BUNMA_TL ->
                    new BulmaTuongLai(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_OMEGA ->
                    new RongOmega(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_1S ->
                    new Rong1Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_2S ->
                    new Rong2Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_3S ->
                    new Rong3Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_4S ->
                    new Rong4Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_5S ->
                    new Rong5Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_6S ->
                    new Rong6Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RONG_7S ->
                    new Rong7Sao(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DAI_THIEN_SU ->
                    new DaiThienSu(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.WHIS ->
                    new Whis(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BILL ->
                    new Bill(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BO_MONG ->
                    new BoMong(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.THAN_MEO_KARIN ->
                    new Karin(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.GOKU_SSJ ->
                    new GokuSSJ(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.GOKU_SSJ_2 ->
                    new GokuSSJ2(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TAPION ->
                    new Tapion(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DOC_NHAN ->
                    new DocNhan(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.GIUMA_DAU_BO ->
                    new GiuMaDauBo(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.TO_SU_KAIO ->
                    new ToSuKaio(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.BARDOCK ->
                    new Bardock(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.RUONG_SUU_TAM ->
                    new RuongSuuTam(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.CHI_CHI ->
                    new ChiChi(mapId, status, cx, cy, tempId, avatar);
                case ConstNpc.DR_MYUU ->
                    new DrMyuu(mapId, status, cx, cy, tempId, avatar);
                default ->
                    new Npc(mapId, status, cx, cy, tempId, avatar) {
                        @Override
                        public void openBaseMenu(Player player) {
                            if (canOpenNpc(player)) {
                                super.openBaseMenu(player);
                            }
                        }

                        @Override
                        public void confirmMenu(Player player, int select) {
                            if (canOpenNpc(player)) {
                            }
                        }
                    };
            };
        } catch (Exception e) {
            Logger.logException(NpcFactory.class,
                    e, "Lỗi load npc");
            return null;
        }
    }

    public static void createNpcRongThieng() {
        new Npc(-1, -1, -1, -1, ConstNpc.RONG_THIENG, -1) {
            @Override
            public void confirmMenu(Player player, int select) {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.IGNORE_MENU:
                        break;
                    case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM:
                        SummonDragonNamek.gI().showConfirmShenron(player, player.idMark.getIndexMenu(), (byte) select);
                        break;
                    case ConstNpc.SHENRON_NAMEK_CONFIRM:
                        if (select == 0) {
                            SummonDragonNamek.gI().confirmWish();
                        } else if (select == 1) {
                            SummonDragonNamek.gI().sendWhishesNamec(player);
                        }
                        break;
                    case ConstNpc.SHOW_SHENRON_EVENT_CONFIRM:
                        if (player.shenronEvent != null) {
                            player.shenronEvent.showConfirmShenron((byte) select);
                        }
                        break;
                    case ConstNpc.SHOW_SHENRON_EVENT_CONFIRM_2:
                        if (player.shenronEvent != null) {
                            player.shenronEvent.showConfirmShenron2((byte) select);
                        }
                        break;
                    case ConstNpc.SHENRON_EVENT_CONFIRM:
                        if (select == 0) {
                            if (player.shenronEvent != null) {
                                player.shenronEvent.confirmWish();
                            }
                        } else if (select == 1) {
                            if (player.shenronEvent != null) {
                                player.shenronEvent.sendBlackGokuhesShenron();
                            }
                        }
                        break;
                    case ConstNpc.SHENRON_CONFIRM:
                        if (select == 0) {
                            SummonDragon.gI().confirmWish();
                        } else if (select == 1) {
                            SummonDragon.gI().reOpenShenronWishes(player);
                        }
                        break;
                    case ConstNpc.SHENRON_1_1:
                        if (player.idMark.getIndexMenu() == ConstNpc.SHENRON_1_1 && select == SHENRON_1_STAR_WISHES_1.length - 1) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_1_2, SHENRON_SAY, SHENRON_1_STAR_WISHES_2);
                            break;
                        }
                    case ConstNpc.SHENRON_1_2:
                        if (player.idMark.getIndexMenu() == ConstNpc.SHENRON_1_2 && select == SHENRON_1_STAR_WISHES_2.length - 1) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
                            break;
                        }
                    case ConstNpc.SHENRON_1_3:
                        if (player.idMark.getIndexMenu() == ConstNpc.SHENRON_1_3 && select == SHENRON_1_STAR_WISHES_3.length - 1) {
                            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_1_1, SHENRON_SAY, SHENRON_1_STAR_WISHES_1);
                            break;
                        }
                    default:
                        SummonDragon.gI().showConfirmShenron(player, player.idMark.getIndexMenu(), (byte) select);
                        break;
                }
            }
        };
    }

    public static void createNpcConMeo() {
        new Npc(-1, -1, -1, -1, ConstNpc.CON_MEO, 351) {
            @Override
            public void confirmMenu(Player player, int select) {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.IGNORE_MENU -> {
                    }
                    case ConstNpc.MAKE_MATCH_PVP -> {
                        if (Maintenance.isRunning()) {
                        }
                        PVPService.gI().sendInvitePVP(player, (byte) select);
                    }
                    case ConstNpc.MAKE_FRIEND -> {
                        if (select == 0) {
                            Object playerId = PLAYERID_OBJECT.get(player.id);
                            if (playerId != null) {
                                try {
                                    FriendAndEnemyService.gI().acceptMakeFriend(player,
                                            Integer.parseInt(String.valueOf(playerId)));
                                } catch (NumberFormatException e) {
                                }
                            }
                        }
                    }
                    case ConstNpc.MENTOR_REQUEST -> {
                        if (select == 0) {
                            Object o = PLAYERID_OBJECT.get(player.id);
                            if (o != null) {
                                int masterId = Integer.parseInt(String.valueOf(o));
                                Player master = Client.gI().getPlayer(masterId);
                                if (master != null) {
                                    master.discipleId = (int) player.id;
                                    player.masterId = masterId;
                                    Service.gI().sendThongBao(master, "Đã nhận " + player.name + " làm đệ tử");
                                    Service.gI().sendThongBao(player, "Đã trở thành đệ tử của " + master.name);
                                }
                            }
                        }
                    }
                    case ConstNpc.REVENGE -> {
                        if (select == 0) {
                            PVPService.gI().acceptRevenge(player);
                        }
                    }

                    case ConstNpc.TUTORIAL_SUMMON_DRAGON -> {
                        if (select == 0) {
                            NpcService.gI().createTutorial(player, -1, SummonDragon.SUMMON_SHENRON_TUTORIAL);
                        }
                    }
                    case ConstNpc.SUMMON_SHENRON -> {
                        if (select == 0) {
                            NpcService.gI().createTutorial(player, -1, SummonDragon.SUMMON_SHENRON_TUTORIAL);
                        } else if (select == 1) {
                            SummonDragon.gI().summonShenron(player);
                        }
                    }
                    case ConstNpc.SUMMON_SHENRON_EVENT -> {
                        if (select == 0) {
                            SummonDragonBonney.summonShenron(player);
                        }
                    }
                    case ConstNpc.MENU_OPTION_USE_ITEM726 -> {
                        if (select == 0) {
                            SuperDivineWaterService.gI().joinMapThanhThuy(player);
                        }
                    }
                    case ConstNpc.MENU_TELE_ITEM_1666 -> {
                        if (select == 0) {
                            ChangeMapService.gI().changeMap(player, 176, 12, Util.nextInt(100, 300), 336);
                        }
                    }
                    case ConstNpc.MENU_CONFIRM_SUMMON_KHIDOT -> {
                        if (select == 0) {
                            Object obj = PLAYERID_OBJECT.get(player.id);
                            if (obj instanceof Item it && it.isNotNullItem() && it.template.id == 1305) {
                                if (Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TRUNG_THU) {
                                    int mapId = player.zone.map.mapId;
                                    if (mapId == 19 || mapId == 20 || mapId == 37) {
                                        InventoryService.gI().subQuantityItemsBag(player, it, 1);
                                        InventoryService.gI().sendItemBags(player);
                                        try {
                                            Boss spawnedBoss = BossManager.gI().createBoss(BossID.KHIDOT);
                                            if (spawnedBoss != null) {
                                                spawnedBoss.zoneFinal = player.zone;
                                                spawnedBoss.changeStatus(consts.BossStatus.RESPAWN);
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    } else {
                                        NpcService.gI().createMenuConMeo(player, -1, 351, "Hãy đổi sang map khác",
                                                "Đóng");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Sự kiện Trung thu đã kết thúc");
                                }
                            }
                        }
                    }
                    case ConstNpc.MENU_CONFIRM_SUMMON_THO_DAI_CA -> {
                        if (select == 0) {
                            Object obj = PLAYERID_OBJECT.get(player.id);
                            if (obj instanceof Item it && it.isNotNullItem() && it.template.id == 1310) {
                                if (Manager.EVENT_SEVER == consts.ConstEvent.SU_KIEN_TRUNG_THU) {
                                    int mapId = player.zone.map.mapId;
                                    if (mapId == 19 || mapId == 20 || mapId == 37) {
                                        InventoryService.gI().subQuantityItemsBag(player, it, 1);
                                        InventoryService.gI().sendItemBags(player);
                                        try {
                                            Boss spawnedBoss = managers.boss.BossManager.gI().createBoss(BossID.THO_DAI_CA);
                                            if (spawnedBoss != null) {
                                                spawnedBoss.zoneFinal = player.zone;
                                                spawnedBoss.changeStatus(consts.BossStatus.RESPAWN);
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    } else {
                                        NpcService.gI().createMenuConMeo(player, -1, 351, "Hãy đổi sang map khác",
                                                "Đóng");
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Sự kiện Trung thu đã kết thúc");
                                }
                            }
                        }
                    }
                    case ConstNpc.CAPSULE_KICH_HOAT -> {
                        try {
                            switch (select) {
                                case 0 ->
                                    ItemService.gI().OpenSKHTuChon(player, 1655, 0);
                                case 1 ->
                                    ItemService.gI().OpenSKHTuChon(player, 1655, 1);
                                case 2 ->
                                    ItemService.gI().OpenSKHTuChon(player, 1655, 2);
                                case 3 ->
                                    ItemService.gI().OpenSKHTuChon(player, 1655, 3);
                                case 4 ->
                                    ItemService.gI().OpenSKHTuChon(player, 1655, 4);
                                default ->
                                    Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    case ConstNpc.MENU_SIEU_THAN_THUY -> {
                        if (select == 0) {
                            ChangeMapService.gI().changeMap(player, 46, -1, Util.nextInt(300, 400), 408);
                        }
                    }
                    case ConstNpc.TAP_TU_DONG_CONFIRM -> {
                        if (select == 0) {
                            ChangeMapService.gI().changeMapBySpaceShip(player, player.lastMapOffline,
                                    player.lastZoneOffline, player.lastXOffline);
                        }
                    }
                    case ConstNpc.INTRINSIC -> {
                        switch (select) {
                            case 0 ->
                                IntrinsicService.gI().showAllIntrinsic(player);
                            case 1 ->
                                IntrinsicService.gI().showConfirmOpen(player);
                            case 2 ->
                                IntrinsicService.gI().showConfirmOpenVip(player);
                            default -> {
                            }
                        }
                    }
                    case ConstNpc.CONFIRM_OPEN_INTRINSIC -> {
                        if (select == 0) {
                            IntrinsicService.gI().open(player);
                        }
                    }
                    case ConstNpc.CONFIRM_OPEN_INTRINSIC_VIP -> {
                        if (select == 0) {
                            IntrinsicService.gI().openVip(player);
                        }
                    }
                    case ConstNpc.CONFIRM_LEAVE_CLAN -> {
                        if (select == 0) {
                            ClanService.gI().leaveClan(player);
                        }
                    }
                    case ConstNpc.CONFIRM_NHUONG_PC -> {
                        if (select == 0) {
                            ClanService.gI().phongPc(player, (int) PLAYERID_OBJECT.get(player.id));
                        }
                    }

                    case ConstNpc.BAN_PLAYER -> {
                        if (select == 0) {
                            PlayerService.gI().banPlayer((Player) PLAYERID_OBJECT.get(player.id));
                            Service.gI().sendThongBao(player,
                                    "Ban người chơi " + ((Player) PLAYERID_OBJECT.get(player.id)).name + " thành công");
                        }
                    }
                    case ConstNpc.BUFF_PET -> {
                        if (select == 0) {
                            Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                            if (pl.pet == null) {
                                PetService.gI().createNormalPet(pl);
                                Service.gI().sendThongBao(player, "Phát đệ tử cho "
                                        + ((Player) PLAYERID_OBJECT.get(player.id)).name + " thành công");
                            }
                        }
                    }
                    case ConstNpc.BUY_DISGUISE -> {
                        if (select == 0) {
                            try {
                                Object obj = PLAYERID_OBJECT.get(player.id);
                                if (obj == null) {
                                    Service.gI().sendThongBao(player, "Không tìm thấy thông tin giao dịch.");
                                    break;
                                }
                                DisguisePurchaseInfo info;
                                if (obj instanceof DisguisePurchaseInfo) {
                                    info = (DisguisePurchaseInfo) obj;
                                } else if (obj instanceof Player) {
                                    Player seller = (Player) obj;
                                    if (seller.inventory == null || seller.inventory.itemsBody == null
                                            || seller.inventory.itemsBody.size() <= 5) {
                                        Service.gI().sendThongBao(player, "Không thể truy cập trang bị của người bán.");
                                        break;
                                    }
                                    Item disguise = seller.inventory.itemsBody.get(5);
                                    String validationError = DisguisePurchaseService.gI().validateDisguise(disguise, seller);
                                    if (validationError != null) {
                                        Service.gI().sendThongBao(player, validationError);
                                        break;
                                    }
                                    info = DisguisePurchaseService.gI().createPurchaseInfo(seller, disguise);
                                    if (info == null) {
                                        Service.gI().sendThongBao(player, "Không thể xác định giá ngọc cho cải trang này.");
                                        break;
                                    }
                                } else {
                                    Service.gI().sendThongBao(player, "Dữ liệu giao dịch không hợp lệ.");
                                    break;
                                }

                                DisguisePurchaseService.gI().processPurchase(player, info);

                            } catch (Exception e) {
                                utils.Logger.logException(NpcFactory.class, e);
                                Service.gI().sendThongBao(player, "Đã xảy ra lỗi trong quá trình giao dịch.");
                            }
                        }
                    }
                    case ConstNpc.OTT -> {
                        if (select < 3) {
                            Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                            player.idMark.setOtt(select);
                            String[] selects = new String[]{"Kéo", "Búa", "Bao", "Hủy"};
                            NpcService.gI().createMenuConMeo(pl, ConstNpc.OTT_ACCEPT, -1,
                                    player.name + " muốn chơi oẳn tù tì với bạn mức cược 5tr.", selects, player);
                        }
                    }
                    case ConstNpc.OTT_ACCEPT -> {
                        if (select < 3) {
                            Player pl = (Player) PLAYERID_OBJECT.get(player.id);
                            int slp1 = pl.idMark.getOtt();
                            int slp2 = select;
                            if (slp1 == -1 || slp2 == -1) {
                                return;
                            }
                            pl.idMark.setOtt(-1);
                            String[] selects = new String[]{"Kéo", "Búa", "Bao"};
                            Service.gI().chat(pl, selects[slp1]);
                            Service.gI().chat(player, selects[slp2]);
                            Service.gI().sendEffAllPlayer(pl, 1000 + slp1, 1, 2, 1);
                            Service.gI().sendEffAllPlayer(player, 1000 + slp2, 1, 2, 1);
                            if (slp1 == slp2) {
                                Service.gI().sendThongBao(pl, "Hòa!");
                                Service.gI().sendThongBao(player, "Hòa!");
                            } else if (slp1 == 0 && slp2 == 2 || slp1 == 1 && slp2 == 0 || slp1 == 2 && slp2 == 1) {
                                Service.gI().sendThongBao(pl, "Thắng!");
                                Service.gI().sendThongBao(player, "Thua!");
                                pl.inventory.gold += 4800000;
                                player.inventory.gold -= 5000000;
                                Service.gI().sendMoney(pl);
                                Service.gI().sendMoney(player);
                            } else {
                                Service.gI().sendThongBao(pl, "Thua!");
                                Service.gI().sendThongBao(player, "Thắng!");
                                pl.inventory.gold -= 5000000;
                                player.inventory.gold += 4800000;
                                Service.gI().sendMoney(pl);
                                Service.gI().sendMoney(player);
                            }
                        }
                    }
                    case ConstNpc.MENU_ADMIN -> {
                        switch (select) {
                            case 0 -> {
                                for (int i = 14; i <= 20; i++) {
                                    Item item = ItemService.gI().createNewItem((short) i);
                                    InventoryService.gI().addItemBag(player, item);
                                }
                                InventoryService.gI().sendItemBags(player);
                            }
                            case 1 -> {
                                PetService.gI().createNormalPet(player, null);
                            }
                            case 2 -> {
                                if (player.isAdmin()) {
                                    System.out.println(player.name + " Đang bảo trì game!");
                                    Maintenance.gI().start(30);
                                }
                            }
                            case 3 ->
                                Input.gI().createFormFindPlayer(player);
                            case 4 ->
                                BossManager.gI().showListBoss(player);
                        }
                    }
                    case ConstNpc.CONFIRM_DISSOLUTION_CLAN -> {
                        switch (select) {
                            case 0 -> {
                                Clan clan = player.clan;
                                clan.deleteDB(clan.id);
                                Manager.CLANS.remove(clan);
                                player.clan = null;
                                player.clanMember = null;
                                ClanService.gI().sendMyClan(player);
                                ClanService.gI().sendClanId(player);
                                Service.gI().sendThongBao(player, "Đã giải tán bang hội.");
                            }
                        }
                    }
                    case 671 -> {
                        switch (select) {
                            case 0 -> {
                                long[] time = new long[]{
                                    900_000L, 1_800_000L, 3_600_000L,
                                    86_400_000L, 259_200_000L, 604_800_000L, 1_296_000_000L
                                };

                                var itemTemplate = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId);
                                String[] subName = itemTemplate.name.split("");
                                byte level = Byte.parseByte(subName[subName.length - 1]);
                                int requiredPotential = player.LearnSkill.Potential;
                                if (player.nPoint.tiemNang < requiredPotential) {
                                    Service.gI().sendThongBao(player, "Bạn cần " + Util.numberToMoney(requiredPotential)
                                            + " tiềm năng để học kỹ năng này");
                                    return;
                                }
                                player.nPoint.tiemNang -= requiredPotential;
                                player.LearnSkill.Time = time[level - 1] + System.currentTimeMillis();

                                Service.gI().point(player);
                                Service.gI().ClosePanel(player);
                                NpcService.gI().createTutorial(player, NpcService.gI().getAvatar(13 + player.gender),
                                        "Con đã học thành công, hãy cố gắng chờ đợi nha");
                                break;
                            }
                            case 1 -> {

                                break;
                            }
                        }
                    }

                    case ConstNpc.CONFIRM_REMOVE_ALL_ITEM_LUCKY_ROUND -> {
                        if (select == 0) {
                            for (int i = 0; i < player.inventory.itemsBoxCrackBall.size(); i++) {
                                player.inventory.itemsBoxCrackBall.set(i, ItemService.gI().createItemNull());
                            }
                            player.inventory.itemsBoxCrackBall.clear();
                            Service.gI().sendThongBao(player, "Đã xóa hết vật phẩm trong rương");
                        }
                    }

                    case ConstNpc.MENU_FIND_PLAYER -> {
                        Player p = (Player) PLAYERID_OBJECT.get(player.id);
                        if (p != null) {
                            switch (select) {
                                case 0 -> {
                                    if (p.zone != null) {
                                        ChangeMapService.gI().changeMapYardrat(player, p.zone, p.location.x,
                                                p.location.y);
                                    }
                                }
                                case 1 -> {
                                    if (p.zone != null) {
                                        ChangeMapService.gI().changeMap(p, player.zone, player.location.x,
                                                player.location.y);
                                    }
                                }
                                case 2 ->
                                    Input.gI().createFormChangeName(player, p);
                                case 3 -> {
                                    String[] selects = new String[]{"Đồng ý", "Hủy"};
                                    NpcService.gI().createMenuConMeo(player, ConstNpc.BAN_PLAYER, -1,
                                            "Bạn có chắc chắn muốn ban " + p.name, selects, p);
                                }
                                case 4 -> {
                                    Service.gI().sendThongBao(player, "Kik người chơi " + p.name + " thành công");
                                    Client.gI().getPlayers().remove(p);
                                    Client.gI().kickSession(p.session);
                                }
                            }
                        }
                    }
                    case ConstNpc.CONFIRM_TELE_NAMEC -> {
                        if (select == 0) {
                            NgocRongNamecService.gI().teleportToNrNamec(player);
                            player.inventory.subGem(10);
                            Service.gI().sendMoney(player);
                        }
                    }
                    case ConstNpc.MA_BAO_VE -> {
                        if (select == 0) {
                            if (player.mbv == 0) {
                                if (player.inventory.gold >= 500_000) {
                                    player.inventory.gold -= 500_000;
                                    Service.gI().sendMoney(player);
                                    player.mbv = player.idMark.getMbv();
                                    player.baovetaikhoan = true;
                                    Service.gI().sendThongBao(player,
                                            "Kích hoạt thành công, tài khoản đang được bảo vệ");
                                } else {
                                    Service.gI().sendThongBao(player,
                                            "Bạn không đủ tiền để kích hoạt bảo vệ tài khoản");
                                }
                            } else {
                                if (player.baovetaikhoan) {
                                    player.baovetaikhoan = false;
                                    Service.gI().sendThongBao(player, "Chức năng bảo vệ tài khoản đang tắt");
                                } else {
                                    player.baovetaikhoan = true;
                                    Service.gI().sendThongBao(player, "Tài khoản đang được bảo vệ");
                                }
                            }
                        }
                    }
                    case ConstNpc.UP_TOP_ITEM -> {
                        if (select == 0) {
                            // Delegate to ConsignShopService (thread-safe, validated)
                            int iditem = player.idMark.getIdItemUpTop();
                            ConsignShopService.gI().confirmUpTop(player, iditem);
                        }
                    }
                    case ConstNpc.RUONG_GO -> {
                        int i = player.indexWoodChest;
                        if (i < 0) {
                            return;
                        }
                        Item itemWoodChest = player.itemsWoodChest.get(i);
                        player.indexWoodChest--;
                        String info = "|1|" + itemWoodChest.template.name;
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
                    case ConstNpc.MENU_XUONG_TANG_DUOI -> {
                        if (player.fightMabu.pointMabu >= player.fightMabu.POINT_MAX && player.zone.map.mapId != 120) {
                            ChangeMapService.gI().changeMap(player,
                                    player.zone.map.mapIdNextMabu((short) player.zone.map.mapId), -1, -1, 100);
                        }
                    }
                    case ConstNpc.TRADE_TAYAKI -> {
                        if (select == 0) {
                            if (player.zone.map.mapId != 48) {
                                Service.gI().sendThongBao(player,
                                        "Hãy lên gặp Thần Bill để đổi vật phẩm! Tại hành tinh Kaio!");
                                break;
                            }
                            Item item = InventoryService.gI().findItemBag(player, 1786);
                            if (item != null && item.quantity > 0) {
                                int quantity = item.quantity;
                                InventoryService.gI().removeItemBag(player, item);
                                Item phieu = ItemService.gI().createNewItem((short) 1792);
                                phieu.quantity = quantity;
                                InventoryService.gI().addItemBag(player, phieu);
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendThongBao(player, "Đổi thành công Tayaki.");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không có Tayaki để đổi.");
                            }
                        }
                        break;
                    }
                    case ConstNpc.TRADE_KEO_TAO -> {
                        if (select == 0) {
                            if (player.zone.map.mapId != 48) {
                                Service.gI().sendThongBao(player,
                                        "Hãy lên gặp Thần Bill để đổi vật phẩm! Tại hành tinh Kaio!");
                                break;
                            }
                            Item item = InventoryService.gI().findItemBag(player, 1787);
                            if (item != null && item.quantity > 0) {
                                int quantity = item.quantity;
                                InventoryService.gI().removeItemBag(player, item);
                                Item phieu = ItemService.gI().createNewItem((short) 1792);
                                phieu.quantity = quantity * 2;
                                InventoryService.gI().addItemBag(player, phieu);
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendThongBao(player, "Đổi thành công Kẹo Táo.");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không có Kẹo Táo để đổi.");
                            }
                        }
                        break;
                    }
                    case ConstNpc.TRADE_QUE_KEM_DOI -> {
                        if (select == 0) {
                            if (player.zone.map.mapId != 48) {
                                Service.gI().sendThongBao(player,
                                        "Hãy lên gặp Thần Bill để đổi vật phẩm! Tại hành tinh Kaio!");
                                break;
                            }
                            Item item = InventoryService.gI().findItemBag(player, 1788);
                            if (item != null && item.quantity > 0) {
                                int quantity = item.quantity;
                                InventoryService.gI().removeItemBag(player, item);
                                Item phieu = ItemService.gI().createNewItem((short) 1792);
                                phieu.quantity = quantity * 3;
                                InventoryService.gI().addItemBag(player, phieu);
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendThongBao(player, "Đổi thành công Kem Que Đôi.");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không có Kem Que Đôi để đổi.");
                            }
                        }
                        break;
                    }
                    case ConstNpc.TRADE_MOCHI -> {
                        if (select == 0) {
                            if (player.zone.map.mapId != 48) {
                                Service.gI().sendThongBao(player,
                                        "Hãy lên gặp Thần Bill để đổi vật phẩm! Tại hành tinh Kaio!");
                                break;
                            }
                            Item item = InventoryService.gI().findItemBag(player, 1789);
                            if (item != null && item.quantity > 0) {
                                int quantity = item.quantity;
                                InventoryService.gI().removeItemBag(player, item);
                                Item phieu = ItemService.gI().createNewItem((short) 1792);
                                phieu.quantity = quantity * 5;
                                InventoryService.gI().addItemBag(player, phieu);
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendThongBao(player, "Đổi thành công Mochi.");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không có Mochi để đổi.");
                            }
                        }
                        break;
                    }
                    case ConstNpc.TRADE_RAU_BACH_TUOC -> {
                        if (select == 0) {
                            if (player.zone.map.mapId != 48) {
                                Service.gI().sendThongBao(player,
                                        "Hãy lên gặp Thần Bill để đổi vật phẩm! Tại hành tinh Kaio!");
                                break;
                            }
                            Item item = InventoryService.gI().findItemBag(player, 1790);
                            if (item != null && item.quantity > 0) {
                                int quantity = item.quantity;
                                InventoryService.gI().removeItemBag(player, item);
                                Item phieu = ItemService.gI().createNewItem((short) 1792);
                                phieu.quantity = quantity * 10;
                                InventoryService.gI().addItemBag(player, phieu);
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendThongBao(player, "Đổi thành công Râu Bạch Tuộc.");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không có Râu Bạch Tuộc để đổi.");
                            }
                        }
                        break;
                    }
                    case ConstNpc.MENU_TRADE_TUAN_LOC -> {
                        if (select == 0) { // Giao Tuần Lộc
                            var zone = player.zone;
                            if (zone != null) {
                                Boss tuanLoc = null;
                                for (Player b : zone.getBosses()) {
                                    if (b instanceof boss.event.noel.TuanLoc tl && tl.playerId == Math.abs(player.id)) {
                                        tuanLoc = tl;
                                        break;
                                    }
                                }
                                if (tuanLoc != null) {
                                    try {
                                        ((boss.event.noel.TuanLoc) tuanLoc).leaveMapNew();
                                    } catch (Exception ignored) {
                                        tuanLoc.leaveMap();
                                    }
                                    Item gift = ItemService.gI().createNewItem((short) 648);
                                    gift.quantity = 1;
                                    InventoryService.gI().addItemBag(player, gift);
                                    InventoryService.gI().sendItemBags(player);
                                    Service.gI().sendThongBao(player, "Bạn đã giao Tuần Lộc và nhận được một hộp quà Noel!");
                                } else {
                                    Service.gI().sendThongBao(player, "Không tìm thấy Tuần lộc đang theo bạn.");
                                }
                            }
                        } else { // Từ chối
                            Service.gI().sendThongBao(player, "Bạn đã từ chối giao Tuần lộc.");
                        }
                        break;
                    }
                    case ConstNpc.TRADE_MI_RA_MEN -> {
                        if (select == 0) {
                            if (player.zone.map.mapId != 48) {
                                Service.gI().sendThongBao(player,
                                        "Hãy lên gặp Thần Bill để đổi vật phẩm! Tại hành tinh Kaio!");
                                break;
                            }
                            Item ramen = InventoryService.gI().findItemBag(player, 1791);
                            if (ramen != null && ramen.quantity > 0) {
                                int qty = ramen.quantity;
                                Item phieu = ItemService.gI().createNewItem((short) 1792);
                                phieu.quantity = qty * 20;
                                ramen.quantity = 0;
                                InventoryService.gI().removeItemBag(player, ramen);
                                InventoryService.gI().addItemBag(player, phieu);
                                InventoryService.gI().sendItemBags(player);
                                Service.gI().sendThongBao(player, "Đổi thành công Mì Ramen.");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không có Mì Ramen để đổi.");
                            }
                        }
                        break;
                    }
                }
            }

        };
    }
}
