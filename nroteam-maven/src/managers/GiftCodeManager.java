package managers;

import data.AlyraManager;
import system.GiftCode;
import player.Player;
import services.map.NpcService;
import services.Service;
import java.util.ArrayList;
import item.Item;
import item.Item.ItemOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import utils.Logger;

public class GiftCodeManager {

    public String name;
    public final ArrayList<GiftCode> listGiftCode = new ArrayList<>();

    private static GiftCodeManager instance;
    private long lastReloadTime = 0;
    private static final long RELOAD_COOLDOWN_MS = 5000;
    private final HashMap<Long, Boolean> playerReloadedInSession = new HashMap<>();

    public static GiftCodeManager gI() {
        if (instance == null) {
            instance = new GiftCodeManager();
        }
        return instance;
    }

    public GiftCode checkUseGiftCode(Player player, String code) {
        return checkUseGiftCode(player, code, true);
    }

    public GiftCode checkUseGiftCode(Player player, String code, boolean notifyFailure) {
        synchronized (listGiftCode) {
            for (GiftCode giftCode : listGiftCode) {
                if (giftCode.code.equals(code)) {
                    if (giftCode.timeCode()) {
                        if (notifyFailure) Service.gI().sendThongBaoOK(player, "Giftcode đã hết hạn");
                        return null;
                    }
                    if (giftCode.countLeft <= 0) {
                        if (notifyFailure) Service.gI().sendThongBaoOK(player, "Giftcode đã hết");
                        return null;
                    }
                    if (player.giftCode.isUsedGiftCode(code)) {
                        if (notifyFailure) Service.gI().sendThongBaoOK(player, "Tham lam!");
                        return null;
                    }
                    giftCode.countLeft -= 1;
                    player.giftCode.add(code);
                    updateGiftCode(giftCode);
                    return giftCode;
                }
            }
        }
        return null;
    }

    public void updateGiftCode(GiftCode giftcode) {
        try {
            AlyraManager.executeUpdate("update giftcode set count_left = ? where id = ?",
                    giftcode.countLeft, giftcode.id);
        } catch (Exception e) {
            Logger.error("Lỗi update giftcode: " + e.getMessage());
        }
    }

    public void checkInfomationGiftCode(Player p) {
        reloadGiftCodeForPlayer(p);

        StringBuilder sb = new StringBuilder();
        synchronized (listGiftCode) {
            for (GiftCode giftCode : listGiftCode) {
                sb.append("Code: ").append(giftCode.code)
                        .append(", Số lượng còn lại: ").append(giftCode.countLeft)
                        .append("\b")
                        .append("Ngày tạo: ").append(giftCode.datecreate)
                        .append(", Ngày hết hạn: ").append(giftCode.dateexpired)
                        .append("\n");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        NpcService.gI().createTutorial(p, 5073, sb.toString());
    }

    public void reloadGiftCodeForPlayer(Player player) {
        if (player == null || player.id < 0) {
            return;
        }

        synchronized (playerReloadedInSession) {
            Boolean hasReloaded = playerReloadedInSession.get(player.id);
            if (hasReloaded != null && hasReloaded) {
                return;
            }

            try {
                reloadGiftcodeFromDB();
                playerReloadedInSession.put(player.id, true);
                lastReloadTime = System.currentTimeMillis();
            } catch (Exception e) {
                Logger.error("Lỗi khi reload giftcode cho player " + player.name + ": " + e.getMessage());
            }
        }
    }

    public void reloadGiftCodeIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastReloadTime < RELOAD_COOLDOWN_MS) {
            return;
        }

        synchronized (this) {
            if (currentTime - lastReloadTime < RELOAD_COOLDOWN_MS) {
                return;
            }

            try {
                reloadGiftcodeFromDB();
                lastReloadTime = currentTime;
            } catch (Exception e) {
                Logger.error("Lỗi khi reload giftcode: " + e.getMessage());
            }
        }
    }

    private void reloadGiftcodeFromDB() {
        try {
            List<GiftCode> tempList = new ArrayList<>();
            String sql = "SELECT * FROM giftcode";

            try (Connection con = AlyraManager.getConnection(); java.sql.Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    try {
                        GiftCode giftcode = parseGiftCodeFromResultSet(rs);
                        tempList.add(giftcode);
                    } catch (Exception e) {
                        Logger.error("Lỗi parse giftcode ID: " + rs.getInt("id") + " - " + e.getMessage());
                    }
                }
            }

            synchronized (listGiftCode) {
                int oldSize = listGiftCode.size();
                listGiftCode.clear();
                listGiftCode.addAll(tempList);
                Logger.logln("Đã reload " + tempList.size() + " giftcodes (trước: " + oldSize + ")");
            }
        } catch (Exception e) {
            Logger.error("Lỗi khi reload giftcode từ database: " + e.getMessage());
        }
    }

    private GiftCode parseGiftCodeFromResultSet(ResultSet rs) throws Exception {
        GiftCode giftcode = new GiftCode();
        giftcode.code = rs.getString("code");
        giftcode.id = rs.getInt("id");
        giftcode.countLeft = rs.getInt("count_left");
        if (giftcode.countLeft == -1) {
            giftcode.countLeft = 999999999;
        }
        giftcode.datecreate = rs.getTimestamp("datecreate");
        giftcode.dateexpired = rs.getTimestamp("expired");

        String detailJson = rs.getString("detail");
        if (detailJson != null && !detailJson.isEmpty()) {
            Object parsed = JSONValue.parse(detailJson);
            if (parsed instanceof JSONArray jar) {
                for (Object itemObj : jar) {
                    JSONObject jsonObj = (JSONObject) itemObj;
                    int itemId = Integer.parseInt(jsonObj.get("id").toString());
                    int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                    JSONArray optionArray = (JSONArray) jsonObj.get("options");
                    ArrayList<ItemOption> optionList = new ArrayList<>();
                    if (optionArray != null) {
                        for (Object optionObj : optionArray) {
                            JSONObject optionJson = (JSONObject) optionObj;
                            int optionId = Integer.parseInt(optionJson.get("id").toString());
                            int param = Integer.parseInt(optionJson.get("param").toString());
                            optionList.add(new Item.ItemOption(optionId, param));
                        }
                    }

                    giftcode.option.put(itemId, optionList);
                    giftcode.detail.put(itemId, quantity);
                }
            }
        }
        return giftcode;
    }

    private GiftCode findGiftCodeById(int id) {
        for (GiftCode gc : listGiftCode) {
            if (gc.id == id) {
                return gc;
            }
        }
        return null;
    }

    public int createGiftCode(String code, int countLeft, String detailJson, java.sql.Timestamp expired) {
        try {
            String sql = "INSERT INTO giftcode (code, count_left, detail, datecreate, expired) VALUES (?, ?, ?, NOW(), ?)";
            int newId = AlyraManager.executeInsert(sql, code, countLeft, detailJson, expired);

            if (newId > 0) {
                GiftCode newGiftCode = new GiftCode();
                newGiftCode.id = newId;
                newGiftCode.code = code;
                newGiftCode.countLeft = countLeft;
                newGiftCode.datecreate = new java.sql.Timestamp(System.currentTimeMillis());
                newGiftCode.dateexpired = expired;

                if (detailJson != null && !detailJson.isEmpty()) {
                    Object parsed = JSONValue.parse(detailJson);
                    if (parsed instanceof JSONArray jar) {
                        for (Object itemObj : jar) {
                            JSONObject jsonObj = (JSONObject) itemObj;
                            int itemId = Integer.parseInt(jsonObj.get("id").toString());
                            int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                            JSONArray optionArray = (JSONArray) jsonObj.get("options");
                            ArrayList<ItemOption> optionList = new ArrayList<>();
                            if (optionArray != null) {
                                for (Object optionObj : optionArray) {
                                    JSONObject optionJson = (JSONObject) optionObj;
                                    int optionId = Integer.parseInt(optionJson.get("id").toString());
                                    int param = Integer.parseInt(optionJson.get("param").toString());
                                    optionList.add(new Item.ItemOption(optionId, param));
                                }
                            }

                            newGiftCode.option.put(itemId, optionList);
                            newGiftCode.detail.put(itemId, quantity);
                        }
                    }
                }

                synchronized (listGiftCode) {
                    listGiftCode.add(newGiftCode);
                }

                Logger.success("Đã tạo và thêm giftcode mới: " + code + " (ID: " + newId + ")");
                return newId;
            }
        } catch (Exception e) {
            Logger.error("Lỗi khi tạo giftcode: " + e.getMessage());
        }
        return -1;
    }

    private void parseDetailToGiftCode(GiftCode giftCode, String detailJson) throws Exception {
        if (detailJson == null || detailJson.isEmpty()) {
            return;
        }

        Object parsed = JSONValue.parse(detailJson);
        if (parsed instanceof JSONArray jar) {
            for (Object itemObj : jar) {
                JSONObject jsonObj = (JSONObject) itemObj;
                int itemId = Integer.parseInt(jsonObj.get("id").toString());
                int quantity = Integer.parseInt(jsonObj.get("quantity").toString());

                JSONArray optionArray = (JSONArray) jsonObj.get("options");
                ArrayList<ItemOption> optionList = new ArrayList<>();
                if (optionArray != null) {
                    for (Object optionObj : optionArray) {
                        JSONObject optionJson = (JSONObject) optionObj;
                        int optionId = Integer.parseInt(optionJson.get("id").toString());
                        int param = Integer.parseInt(optionJson.get("param").toString());
                        optionList.add(new Item.ItemOption(optionId, param));
                    }
                }

                giftCode.option.put(itemId, optionList);
                giftCode.detail.put(itemId, quantity);
            }
        }
    }

    public boolean deleteGiftCode(int giftCodeId) {
        try {
            AlyraManager.executeUpdate("DELETE FROM giftcode WHERE id = ?", giftCodeId);

            synchronized (listGiftCode) {
                listGiftCode.removeIf(gc -> gc.id == giftCodeId);
            }

            Logger.success("Đã xóa giftcode ID: " + giftCodeId);
            return true;
        } catch (Exception e) {
            Logger.error("Lỗi khi xóa giftcode: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteGiftCodeByCode(String code) {
        try {
            AlyraManager.executeUpdate("DELETE FROM giftcode WHERE code = ?", code);

            synchronized (listGiftCode) {
                listGiftCode.removeIf(gc -> gc.code.equals(code));
            }

            Logger.success("Đã xóa giftcode: " + code);
            return true;
        } catch (Exception e) {
            Logger.error("Lỗi khi xóa giftcode: " + e.getMessage());
        }
        return false;
    }

    public boolean updateGiftCodeDetails(int giftCodeId, String newDetailJson) {
        try {
            AlyraManager.executeUpdate("UPDATE giftcode SET detail = ? WHERE id = ?", newDetailJson, giftCodeId);

            synchronized (listGiftCode) {
                GiftCode existingGiftCode = findGiftCodeById(giftCodeId);
                if (existingGiftCode != null) {
                    existingGiftCode.detail.clear();
                    existingGiftCode.option.clear();
                    parseDetailToGiftCode(existingGiftCode, newDetailJson);
                }
            }

            Logger.success("Đã cập nhật chi tiết giftcode ID: " + giftCodeId);
            return true;
        } catch (Exception e) {
            Logger.error("Lỗi khi cập nhật giftcode: " + e.getMessage());
        }
        return false;
    }

    public void forceReload() {
        synchronized (this) {
            try {
                reloadGiftcodeFromDB();
                lastReloadTime = System.currentTimeMillis();
                Logger.success("Đã force reload giftcode thành công");
            } catch (Exception e) {
                Logger.error("Lỗi khi force reload giftcode: " + e.getMessage());
            }
        }
    }

    public void clearPlayerReloadFlag(long playerId) {
        synchronized (playerReloadedInSession) {
            playerReloadedInSession.remove(playerId);
        }
    }

    public void clearAllPlayerReloadFlags() {
        synchronized (playerReloadedInSession) {
            playerReloadedInSession.clear();
            Logger.logln("Đã xóa tất cả flag reload của players");
        }
    }
}
