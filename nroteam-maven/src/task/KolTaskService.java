package task;

import player.Player;
import services.ItemService;
import services.Service;
import services.player.InventoryService;
import item.Item;
import server.Manager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import utils.Util;
import utils.Logger;
import daos.KolTaskDAO;

public class KolTaskService {

    public static int pickMaxCount(KolTaskTemplate tpl) {
        if (tpl == null) {
            return 1;
        }
        if (tpl.maxCountMin > 0 && tpl.maxCountMax >= tpl.maxCountMin) {
            return Util.nextInt(tpl.maxCountMin, tpl.maxCountMax);
        }
        return tpl.maxCount > 0 ? tpl.maxCount : 1;
    }

    public static boolean hasVipReward(KolTaskTemplate tpl) {
        if (tpl == null) {
            return false;
        }
        if (tpl.isVip) {
            return true;
        }
        return tpl.rewardDataVip != null && !tpl.rewardDataVip.trim().isEmpty();
    }

    private static boolean isAutoTraining(Player player) {
        if (player == null) {
            return false;
        }
        if (player.itemTime == null) {
            return false;
        }
        return player.itemTime.isUseTDLT;
    }

    public static synchronized void loadTaskFromDB(Player player) {
        if (player == null || player.id <= 0) {
            return;
        }

        int highestCompleted = KolTaskDAO.gI().getHighestCompletedTask(player.id);

        KolTaskTemplate nextTpl = null;
        if (Manager.KOL_TASKS_TEMPLATE != null && !Manager.KOL_TASKS_TEMPLATE.isEmpty()) {
            for (KolTaskTemplate tpl : Manager.KOL_TASKS_TEMPLATE.values()) {
                if (tpl == null || !tpl.active) {
                    continue;
                }
                if (tpl.id > highestCompleted) {
                    if (nextTpl == null || tpl.id < nextTpl.id) {
                        nextTpl = tpl;
                    }
                }
            }

            if (nextTpl == null) {
                for (KolTaskTemplate tpl : Manager.KOL_TASKS_TEMPLATE.values()) {
                    if (tpl != null && tpl.active) {
                        if (nextTpl == null || tpl.id < nextTpl.id) {
                            nextTpl = tpl;
                        }
                    }
                }
            }
        }

        if (nextTpl != null) {
            KolTask existing = KolTaskDAO.gI().loadProgress(player.id, nextTpl.id);
            if (existing != null) {
                existing.name = nextTpl.name != null ? nextTpl.name : "Nhiệm vụ KOL";
                existing.description = nextTpl.description != null ? nextTpl.description : "";
                existing.maxCount = pickMaxCount(nextTpl);
                player.kolTask = existing;
            } else {
                KolTask t = new KolTask();
                t.missionId = nextTpl.id;
                t.name = nextTpl.name != null ? nextTpl.name : "Nhiệm vụ KOL";
                t.description = nextTpl.description != null ? nextTpl.description : "";
                t.maxCount = pickMaxCount(nextTpl);
                t.count = 0;
                t.claimed = false;
                t.claimedVip = false;
                player.kolTask = t;
            }
        } else {
            createFirstTask(player);
        }
    }

    public static synchronized void saveTaskToDB(Player player) {
        if (player == null || player.id <= 0 || player.kolTask == null) {
            return;
        }
        KolTaskDAO.gI().saveProgress(player.id, player.kolTask);
    }

    private static void createFirstTask(Player player) {
        if (player == null) {
            return;
        }

        KolTaskTemplate chosen = null;
        if (Manager.KOL_TASKS_TEMPLATE != null && !Manager.KOL_TASKS_TEMPLATE.isEmpty()) {
            for (KolTaskTemplate tpl : Manager.KOL_TASKS_TEMPLATE.values()) {
                if (tpl == null || !tpl.active) {
                    continue;
                }
                if (chosen == null || tpl.id < chosen.id) {
                    chosen = tpl;
                }
            }
        }

        KolTask t = new KolTask();
        if (chosen != null) {
            t.missionId = chosen.id;
            t.name = chosen.name != null ? chosen.name : "Nhiệm vụ KOL";
            t.description = chosen.description != null ? chosen.description : "";
            t.maxCount = pickMaxCount(chosen);
        } else {
            t.missionId = -1;
            t.name = "Nhiệm vụ KOL";
            t.description = "Chưa có nhiệm vụ KOL mới";
            t.maxCount = 1;
        }
        t.count = 0;
        t.claimed = false;
        t.claimedVip = false;
        player.kolTask = t;
        saveTaskToDB(player);
    }

    public static synchronized void ensureTaskLoaded(Player player) {
        if (player == null) {
            return;
        }
        if (player.kolTask == null) {
            loadTaskFromDB(player);
        }
    }

    private static synchronized void increaseCount(Player player) {
        if (player == null || player.kolTask == null) {
            return;
        }
        if (player.kolTask.claimed && player.kolTask.claimedVip) {
            return;
        }
        if (player.kolTask.maxCount <= 0) {
            return;
        }

        int oldCount = player.kolTask.count;
        player.kolTask.count++;
        if (player.kolTask.count > player.kolTask.maxCount) {
            player.kolTask.count = player.kolTask.maxCount;
        }

        if (player.kolTask.count != oldCount && player.kolTask.missionId > 0) {
            KolTaskDAO.gI().increaseCount(player.id, player.kolTask.missionId);
        }
    }

    public static synchronized void doneTaskKolBoMong(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask != null && player.kolTask.missionId == 1
                && player.playerTask != null && player.playerTask.sideTask != null
                && player.playerTask.sideTask.template != null
                && player.playerTask.sideTask.level == consts.ConstTask.VERY_HARD
                && player.playerTask.sideTask.isDone()) {
            increaseCount(player);
        }
    }

    public static synchronized void doneTaskKolKillMob(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask == null) {
            return;
        }
        if (player.kolTask.missionId == 2 && isAutoTraining(player)) {
            int oldCount = player.kolTask.count;
            increaseCount(player);
        }
    }

    public static synchronized void doneTaskKolTreasureUnderSea(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask != null && player.kolTask.missionId == 3) {
            increaseCount(player);
        }
    }

    public static synchronized void doneTaskKolMartialArtsTournament(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask != null && player.kolTask.missionId == 4) {
            increaseCount(player);
        }
    }

    public static synchronized void doneTaskKolGauTuongCuop(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask != null && player.kolTask.missionId == 5) {
            increaseCount(player);
        }
    }

    public static synchronized void doneTaskKolKillAnyMob(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask == null) {
            return;
        }
        if (player.kolTask.missionId == 6 && isAutoTraining(player)) {
            increaseCount(player);
        }
    }

    private static void nextTask(Player player) {
        if (player == null || player.kolTask == null) {
            return;
        }

        int currentTaskId = player.kolTask.missionId > 0 ? player.kolTask.missionId : -1;
        KolTaskTemplate nextTemplate = null;

        if (Manager.KOL_TASKS_TEMPLATE != null && !Manager.KOL_TASKS_TEMPLATE.isEmpty()) {
            for (KolTaskTemplate tpl : Manager.KOL_TASKS_TEMPLATE.values()) {
                if (tpl != null && tpl.active && tpl.id > currentTaskId) {
                    if (nextTemplate == null || tpl.id < nextTemplate.id) {
                        nextTemplate = tpl;
                    }
                }
            }

            if (nextTemplate == null) {
                Service.gI().sendThongBao(player, "Bạn đã hoàn thành tất cả nhiệm vụ KOL!");
                return;
            }
        } else {
            return;
        }

        KolTask existing = KolTaskDAO.gI().loadProgress(player.id, nextTemplate.id);
        if (existing != null) {
            existing.name = nextTemplate.name != null ? nextTemplate.name : "Nhiệm vụ KOL";
            existing.description = nextTemplate.description != null ? nextTemplate.description : "";
            if (existing.maxCount <= 0 || existing.maxCount != pickMaxCount(nextTemplate)) {
                existing.maxCount = pickMaxCount(nextTemplate);
            }
            player.kolTask = existing;
        } else {
            KolTask t = new KolTask();
            t.missionId = nextTemplate.id;
            t.name = nextTemplate.name != null ? nextTemplate.name : "Nhiệm vụ KOL";
            t.description = nextTemplate.description != null ? nextTemplate.description : "";
            t.maxCount = pickMaxCount(nextTemplate);
            t.count = 0;
            t.claimed = false;
            t.claimedVip = false;
            player.kolTask = t;
            saveTaskToDB(player);
        }
    }

    public static void claimReward(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy nhiệm vụ KOL");
            return;
        }
        if (player.kolTask.missionId <= 0) {
            Service.gI().sendThongBao(player, "Bạn chưa có nhiệm vụ KOL nào");
            return;
        }
        if (!player.kolTask.isDone()) {
            Service.gI().sendThongBao(player,
                    "Chưa hoàn thành nhiệm vụ KOL (" + player.kolTask.count + "/" + player.kolTask.maxCount + ")");
            return;
        }
        if (player.kolTask.claimed) {
            Service.gI().sendThongBao(player, "Bạn đã nhận thưởng Free nhiệm vụ này rồi");
            return;
        }
        KolTaskTemplate temp = Manager.KOL_TASKS_TEMPLATE != null
                ? Manager.KOL_TASKS_TEMPLATE.get(player.kolTask.missionId)
                : null;
        if (temp == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy template nhiệm vụ KOL");
            player.kolTask.claimed = true;
            saveTaskToDB(player);
            return;
        }
        // Give reward using helper method
        if (giveRewardToPlayer(player, temp.rewardData, "Bạn đã nhận được phần thưởng Free KOL")) {
            player.kolTask.claimed = true;
            KolTaskDAO.gI().claimReward(player.id, player.kolTask.missionId, false);
            nextTask(player);
        }
    }

    public static void claimRewardVipByTaskId(Player player, int taskId) {
        if (player == null || taskId <= 0) {
            return;
        }

        KolTask task = KolTaskDAO.gI().loadProgress(player.id, taskId);
        KolTaskTemplate temp = Manager.KOL_TASKS_TEMPLATE != null
                ? Manager.KOL_TASKS_TEMPLATE.get(taskId)
                : null;

        if (temp == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy template nhiệm vụ");
            return;
        }

        if (task == null) {
            Service.gI().sendThongBao(player, "Bạn chưa bắt đầu nhiệm vụ này");
            return;
        }

        int maxCount = pickMaxCount(temp);
        if (task.count < maxCount) {
            Service.gI().sendThongBao(player, "Nhiệm vụ chưa hoàn thành (" + task.count + "/" + maxCount + ")");
            return;
        }

        if (task.claimedVip) {
            Service.gI().sendThongBao(player, "Bạn đã nhận thưởng VIP nhiệm vụ này rồi");
            return;
        }

        Item vipPass = InventoryService.gI().findItemBag(player, 1835);
        if (vipPass == null || vipPass.quantity <= 0) {
            Service.gI().sendThongBao(player, "Bạn cần có Vé VIP để nhận thưởng VIP");
            return;
        }

        if (!hasVipReward(temp)) {
            Service.gI().sendThongBao(player, "Nhiệm vụ này không hỗ trợ thưởng VIP");
            return;
        }

        // Use VIP reward data if available, fallback to normal reward
        String rewardData = temp.rewardDataVip != null && !temp.rewardDataVip.trim().isEmpty()
                ? temp.rewardDataVip
                : temp.rewardData;

        // Give reward using helper method
        if (giveRewardToPlayer(player, rewardData, "Bạn đã nhận được phần thưởng KOL VIP cấp " + taskId)) {
            KolTaskDAO.gI().claimReward(player.id, taskId, true);
        }
    }

    public static void claimRewardVip(Player player) {
        if (player == null) {
            return;
        }
        ensureTaskLoaded(player);
        if (player.kolTask == null || player.kolTask.missionId <= 0) {
            Service.gI().sendThongBao(player, "Không tìm thấy nhiệm vụ KOL hiện tại");
            return;
        }
        claimRewardVipByTaskId(player, player.kolTask.missionId);
    }

    /**
     * Helper method to give reward to player
     * 
     * @param player         The player to receive reward
     * @param rewardData     JSON reward data
     * @param successMessage Message to show on success
     * @return true if reward given successfully, false otherwise
     */
    private static boolean giveRewardToPlayer(Player player, String rewardData, String successMessage) {
        if (rewardData == null || rewardData.trim().isEmpty()) {
            Service.gI().sendThongBao(player, "Hoàn thành nhiệm vụ KOL!");
            return true;
        }

        try {
            // Parse and create item
            Item item = dataReward(rewardData);
            if (item == null || item.template == null) {
                Service.gI().sendThongBao(player, "Không thể tạo vật phẩm thưởng");
                return false;
            }

            int quantity = item.quantity;
            boolean needSeparateItems = !item.template.isUpToUp;

            // Check bag space
            int requiredSlots = (needSeparateItems && quantity > 1) ? quantity : 1;
            int emptySlots = InventoryService.gI().getCountEmptyBag(player);
            if (emptySlots < requiredSlots) {
                Service.gI().sendThongBao(player,
                        "Hành trang không đủ chỗ trống! Cần " + requiredSlots + " ô, có " + emptySlots + " ô");
                return false;
            }

            // Give items
            boolean success;
            if (needSeparateItems && quantity > 1) {
                // Create separate items
                int successCount = 0;
                for (int i = 0; i < quantity; i++) {
                    Item newItem = dataReward(rewardData);
                    if (newItem != null) {
                        newItem.quantity = 1;
                        if (InventoryService.gI().addItemBag(player, newItem)) {
                            successCount++;
                        }
                    }
                }
                success = successCount > 0;
            } else {
                // Stack items
                success = InventoryService.gI().addItemBag(player, item);
            }

            if (success) {
                InventoryService.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, successMessage);
                return true;
            } else {
                Service.gI().sendThongBao(player, "Không thể thêm vật phẩm thưởng vào túi đồ");
                return false;
            }
        } catch (Exception e) {
            Service.gI().sendThongBao(player, "Không thể trao thưởng, vui lòng thử lại");
            Logger.log("Error giving reward: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static Item dataReward(String rewardData) {
        if (rewardData == null || rewardData.trim().isEmpty()) {
            return null;
        }
        try {
            JSONArray rewardArray = (JSONArray) JSONValue.parseWithException(rewardData.trim());
            if (rewardArray == null || rewardArray.isEmpty()) {
                return null;
            }
            JSONObject rewardObject = (JSONObject) rewardArray.get(0);
            if (rewardObject == null) {
                return null;
            }
            Object idObj = rewardObject.get("id");
            Object qtyObj = rewardObject.get("quantity");
            if (idObj == null || qtyObj == null) {
                return null;
            }
            int itemId = Integer.parseInt(idObj.toString());
            int quantity = Integer.parseInt(qtyObj.toString());
            if (quantity <= 0)
                quantity = 1;
            Item item = ItemService.gI().createNewItem((short) itemId);
            if (item == null || item.template == null) {
                return null;
            }
            item.quantity = quantity;
            JSONArray optionsArray = (JSONArray) rewardObject.get("options");
            if (optionsArray != null) {
                for (Object opt : optionsArray) {
                    if (opt == null)
                        continue;
                    JSONObject optionObject = (JSONObject) opt;
                    Object optIdObj = optionObject.get("id");
                    Object paramObj = optionObject.get("param");
                    if (optIdObj == null || paramObj == null)
                        continue;
                    int optionId = Integer.parseInt(optIdObj.toString());
                    int param = Integer.parseInt(paramObj.toString());
                    item.itemOptions.add(new Item.ItemOption(optionId, param));
                }
            }
            return item;
        } catch (NumberFormatException | ParseException | ClassCastException e) {
            Logger.log("Error parsing reward data: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Logger.log("Unexpected error in dataReward: " + e.getMessage());
            return null;
        }
    }

    public static String getItemNameById(int itemId) {
        try {
            if (Manager.ITEM_TEMPLATES != null && !Manager.ITEM_TEMPLATES.isEmpty()) {
                for (int i = 0; i < Manager.ITEM_TEMPLATES.size(); i++) {
                    if (Manager.ITEM_TEMPLATES.get(i).id == itemId) {
                        return Manager.ITEM_TEMPLATES.get(i).name;
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static String createRewarText(String rewardData) {
        if (rewardData == null || rewardData.trim().isEmpty()) {
            return "";
        }

        try {
            Item item = dataReward(rewardData);
            if (item != null && item.template != null) {
                StringBuilder descriptionBuilder = new StringBuilder();
                descriptionBuilder.append("\nThưởng ").append(item.quantity).append(" ").append(item.template.name);
                if (item.itemOptions != null) {
                    for (Item.ItemOption option : item.itemOptions) {
                        if (option.optionTemplate != null && option.optionTemplate.id == 93) {
                            descriptionBuilder.append(" ").append(option.param).append(" ngày");
                            break;
                        }
                    }
                }
                return descriptionBuilder.toString();
            }
        } catch (Exception e) {
        }
        return "";
    }

    public static String createRewarTextVip(String rewardDataVip) {
        return createRewarText(rewardDataVip);
    }
}
