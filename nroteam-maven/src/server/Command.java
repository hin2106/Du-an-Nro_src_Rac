package server;

import boss.event.noel.TuanLoc;
import consts.BossStatus;
import consts.ConstNpc;
import consts.ConstTaskBadges;
import managers.GiftCodeManager;
import managers.boss.*;
import item.Item;
import player.Player;
import services.ItemService;
import services.Service;
import services.func.Input;
import services.map.NpcService;
import services.player.InventoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import player.Pet;
import services.EffectSkillService;
import services.PetService;
import services.TaskService;
import services.dungeon.TreasureUnderSeaService;
import services.dungeon.DestronGasService;
import services.dungeon.SnakeWayService;
import services.map.ChangeMapService;
import services.shenron.SummonDragonBonney;
import services.shenron.SummonDragonNamek;
import task.BadgesTaskService;
import task.KolTaskService;
import utils.Util;
import recharge.RechargeModels.Settings;
import recharge.RechargeRepository;

public class Command {

    private static Command instance;

    private final Map<String, Consumer<Player>> adminCommands = new HashMap<>();
    private final Map<String, BiConsumer<Player, String>> parameterizedCommands = new HashMap<>();

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    private Command() {
        initAdminCommands();
        initParameterizedCommands();
    }

    @SuppressWarnings("empty-statement")
    private void initAdminCommands() {
        adminCommands.put("buoi", player -> {
            for (int i = 702; i <= 708; i++) {
                Item item = ItemService.gI().createNewItem((short) i);
                InventoryService.gI().addItemBag(player, item);
            }
            InventoryService.gI().sendItemBags(player);
        });
        adminCommands.put("gift", player -> GiftCodeManager.gI().checkInfomationGiftCode(player));
        adminCommands.put("boss", player -> BossManager.gI().showBossDashboard(player));
        adminCommands.put("broly", player -> BrolyManager.gI().showListBoss(player));
        adminCommands.put("antrom", player -> AnTromManager.gI().showListBoss(player));
        adminCommands.put("12h", player -> Boss12hManager.gI().showListBoss12h(player));
        adminCommands.put("14h", player -> Boss14hManager.gI().showListBoss14h(player));
        adminCommands.put("trungthu", player -> TrungThuEventManager.gI().showListBoss(player));
        adminCommands.put("namec", player -> SummonDragonNamek.gI().summonNamec(player));
        adminCommands.put("hlw", player -> HalloweenEventManager.gI().showListBoss(player));
        adminCommands.put("noel", player -> ChristmasEventManager.gI().showListBoss(player));
        adminCommands.put("rongx", player -> SummonDragonBonney.gI().summonShenron(player));
        adminCommands.put("menu", player -> NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1,
                "|0|Time start: " + ServerManager.timeStart + "\nClients: " + Client.gI().getPlayers().size()
                        + "\n" + SystemMetrics.getMetrics(),
                "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "Đóng"));

        adminCommands.put("item", player -> Input.gI().createFormGiveItem(player));
        adminCommands.put("getitem", player -> Input.gI().createFormGetItem(player));
        adminCommands.put("d", player -> Service.gI().setPos(player, player.location.x, player.location.y + 10));
        adminCommands.put("hs", (var player) -> {
            if (player == null || player.nPoint == null) {
                return;
            }
            player.nPoint.hp = player.nPoint.hpMax;
            player.nPoint.mp = player.nPoint.mpMax;
            Service.gI().releaseCooldownSkill(player);
            if (player != null) {
                EffectSkillService.gI().removeStun(player);
                if (player.effectSkill.plAnTroi != null) {
                    EffectSkillService.gI().removeAnTroi(player.effectSkill.plAnTroi);
                }
                EffectSkillService.gI().removeThoiMien(player);
            }
            Service.gI().Send_Info_NV(player);
        });

        adminCommands.put("donebd", player -> {
            boolean ok = TreasureUnderSeaService.gI().finishNow(player);
            Service.gI().sendThongBao(player,
                    ok ? "Đã kết thúc BĐKB ngay lập tức." : "Không có phiên BĐKB đang mở cho bang của bạn.");
        });
        adminCommands.put("donekg", player -> {
            boolean ok = DestronGasService.gI().finishNow(player);
            Service.gI().sendThongBao(player,
                    ok ? "Đã kết thúc KGHD ngay lập tức." : "Không có phiên KGHD đang mở cho bang của bạn.");
        });
        adminCommands.put("donerd", player -> {
            boolean ok = SnakeWayService.gI().finishNow(player);
            Service.gI().sendThongBao(player,
                    ok ? "Đã kết thúc CDRD ngay lập tức." : "Không có phiên CDRD đang mở cho bang của bạn.");
        });
        // adminCommands.put("donedh", player -> {
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.DAI_GIA_MOI_NHU, 1000000);
        // BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.TRUM_UOC_RONG,
        // 100);
        // BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.TRUM_SAN_BOSS,
        // 300);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.THANH_DAP_DO_7, 5);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.CAO_THU_SIEU_HANG, 1);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.NONG_DAN_CHAM_CHI, 10);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.KE_THAO_TUNG_SOI, 20);
        // BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.NUOC_ANH_BAO,
        // 5);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.ONG_THAN_VE_CHAI, 500);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.BI_MOC_SACH_TUI, 30);
        // BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.O_DO, 30);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.CHIEN_BINH_SAO_DEN, 1);
        // BadgesTaskService.updateCountBagesTask(player,
        // ConstTaskBadges.TRUNG_THU_PHA_CO, 1);
        // });

        adminCommands.put(
                "donekol", player -> {
                    KolTaskService.ensureTaskLoaded(player);
                    if (player.kolTask == null) {
                        Service.gI().sendThongBao(player, "Không có nhiệm vụ KOL!");
                        return;
                    }
                    player.kolTask.count = player.kolTask.maxCount;
                    player.kolTask.claimed = false;
                    player.kolTask.claimedVip = false;
                    KolTaskService.saveTaskToDB(player);
                    Service.gI().sendThongBao(player,
                            "Đã hoàn thành nhiệm vụ KOL hiện tại! Hãy đến gặp Quy Lão Kame để nhận thưởng.");
                });

        adminCommands.put(
                "donebm", player -> {
                    try {
                        if (player != null && player.playerTask != null && player.playerTask.sideTask != null
                                && player.playerTask.sideTask.template != null) {
                            player.playerTask.sideTask.count = player.playerTask.sideTask.maxCount;
                            Service.gI().sendThongBao(player,
                                    "Đã đánh dấu hoàn thành nhiệm vụ Bò Mộng hiện tại. Hãy nhận thưởng tại Bò Mộng.");
                        } else {
                            Service.gI().sendThongBao(player, "Bạn chưa có nhiệm vụ Bò Mộng nào đang làm.");
                        }
                    } catch (Exception e) {
                        Service.gI().sendThongBao(player, "Lỗi khi hoàn thành nhiệm vụ Bò Mộng: " + e.getMessage());
                    }
                });
    }

    private void initParameterizedCommands() {
        parameterizedCommands.put("m ", (player, text) -> {
            try {
                String mapIdStr = text.replace("m ", "").trim();
                if (mapIdStr.isEmpty()) {
                    throw new RuntimeException();
                }
                int mapId = Integer.parseInt(mapIdStr);
                ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });

        parameterizedCommands.put("toado", (player, text) -> {
            Service.gI().sendThongBaoOK(player, "x: " + player.location.x + " - y: " + player.location.y);
        });
        parameterizedCommands.put("n", (player, text) -> {
            try {
                String taskStr = text.replaceAll("n", "").trim();
                if (taskStr.isEmpty()) {
                    throw new RuntimeException();
                }
                int idTask = Integer.parseInt(taskStr);
                player.playerTask.taskMain.id = idTask - 1;
                player.playerTask.taskMain.index = 0;
                TaskService.gI().sendNextTaskMain(player);
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
        parameterizedCommands.put("i ", (player, text) -> {
            try {
                String[] parts = text.trim().split(" ");
                if (parts.length < 2) {
                    throw new RuntimeException();
                }

                int itemId = Integer.parseInt(parts[1]);
                int quantity = (parts.length >= 3) ? Integer.parseInt(parts[2]) : 1;

                if (quantity <= 0) {
                    throw new RuntimeException();
                }

                for (int i = 0; i < quantity; i++) {
                    Item item = ItemService.gI().createNewItem((short) itemId);
                    List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) itemId);
                    if (!ops.isEmpty()) {
                        item.itemOptions = ops;
                    }
                    InventoryService.gI().addItemBag(player, item);
                }

                InventoryService.gI().sendItemBags(player);
                String itemName = ItemService.gI().getTemplate((short) itemId).name;
                Service.gI().sendThongBao(player, "GET " + quantity + "x " + itemName + " [" + itemId + "] SUCCESS !");
            } catch (Exception e) {
                throw new RuntimeException();
            }
        });
    }

    public void chat(Player player, String text) {
        if (!check(player, text)) {
            Service.gI().chat(player, text);
        }
    }

    public boolean check(Player player, String text) {
        if (player != null && text != null && text.trim().equalsIgnoreCase("bank")) {
            try {
                Settings settings = RechargeRepository.getSettings();
                String content = "|1|HƯỚNG DẪN CHUYỂN KHOẢN\n"
                        + "|0|Ngân hàng: " + settings.bankName + "\n"
                        + "Số tài khoản: " + settings.accountNumber + "\n"
                        + "Chủ tài khoản: " + settings.accountHolder + "\n"
                        + "Nội dung: " + settings.transferContent(player.name) + "\n\n"
                        + "|7|Lưu ý: " + settings.warning;
                NpcService.gI().createMenuConMeo(player, ConstNpc.IGNORE_MENU, -1, content, "Đóng");
            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Hệ thống nạp đang tạm thời không khả dụng.");
            }
            return true;
        }
        if (player.isAdmin()) {
            try {
                if (adminCommands.containsKey(text)) {
                    adminCommands.get(text).accept(player);
                    return true;
                }

                for (Map.Entry<String, BiConsumer<Player, String>> entry : parameterizedCommands.entrySet()) {
                    if (text.startsWith(entry.getKey())) {
                        entry.getValue().accept(player, text);
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        }

        if (player.pet != null) {
            switch (text) {
                case "di theo", "follow" ->
                    player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" ->
                    player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" ->
                    player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" ->
                    player.pet.changeStatus(Pet.GOHOME);
                case "bien hinh" ->
                    player.pet.transform();
            }
        }
        if (player != null && player.zone != null) {
            String normalized = text.trim().toLowerCase();
            if (normalized.equals("éc éc") || normalized.equals("ec ec")) {
                List<Player> bossesInZone = player.zone.getBosses();
                boolean alreadyHas = false;
                if (bossesInZone != null) {
                    for (Player b : bossesInZone) {
                        if (b instanceof TuanLoc tl && tl.playerId == Math.abs(player.id)) {
                            alreadyHas = true;
                            break;
                        }
                    }
                }
                if (alreadyHas) {
                    Service.gI().sendThongBao(player, "Bạn đang dắt một Tuần lộc rồi, không thể gọi thêm!");
                    return true;
                }
                final int INTERACTION_DISTANCE = 100;
                TuanLoc target = null;
                double minDis = INTERACTION_DISTANCE + 1;
                if (bossesInZone != null) {
                    for (Player b : bossesInZone) {
                        if (b instanceof TuanLoc tl && !tl.isDie() && tl.playerId == 0) {
                            double d = Util.getDistance(player, b);
                            if (d <= INTERACTION_DISTANCE && d < minDis) {
                                minDis = d;
                                target = tl;
                            }
                        }
                    }
                }
                if (target != null) {
                    target.changeToTypeNonPK();
                    target.playerId = Math.abs(player.id);
                    Service.gI().chat(player, "Đi thôi tuần lộc!");
                    target.nPoint.hp = target.nPoint.hpMax;
                    target.changeStatus(BossStatus.AFK);
                    Service.gI().sendThongBao(player, "Tuần lộc nghe lời và đi theo bạn!");
                    return true;
                } else {
                    Service.gI().sendThongBao(player, "Không có Tuần lộc nào gần bạn để thuần hóa (<=100m)");
                    return true;
                }
            }
        }
        return false;
    }
}
