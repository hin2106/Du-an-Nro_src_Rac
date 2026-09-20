package player;
import utils.Util;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ItemEvent {

    public Player player;

    public long lastTVGSTime;
    public long lastItemChuongDong;
    public long lastItemBanhQuy;
    public long lastItemCaTuyet;
    public long lastItemKeoDuong;
    public long lastItemKeoNguoiTuyet;
    public long lastItemManhVo;

    public int remainingChuongDongCount;
    public int remainingBanhQuyCount;
    public int remainingCaTuyetCount;
    public int remainingKeoDuongCount;
    public int remainingKeoNguoiTuyetCount;
    public int remainingTVGSCount;
    public int remainingManhVo;

    public long lastHHTime;
    public int remainingHHCount;

    public long lastBNTime;
    public int remainingBNCount;

    // Shenron
    public boolean isShenronPetX3;
    public long lastTimeShenronPetX3;
    public boolean isShenronPorataHp;
    public long lastTimeShenronPorataHp;
    public boolean isShenronPorataKi;
    public long lastTimeShenronPorataKi;
    public boolean isShenronPorataDame;
    public long lastTimeShenronPorataDame;
    public boolean isShenronPlayerX3;
    public long lastTimeShenronPlayerX3;

    public long lastLimitedItemDropTime;
    public Map<Integer, Integer> limitedItemDropCounts = new HashMap<>();

    public ItemEvent(Player player) {
        this.player = player;
    }

    public boolean canDropTatVoGiangSinh(int maxCount) {
        if (Util.isAfterMidnight(lastTVGSTime)) {
            remainingTVGSCount = maxCount;
            lastTVGSTime = System.currentTimeMillis();
            return true;
        } else if (remainingTVGSCount > 0) {
            remainingTVGSCount--;
            return true;
        }
        return false;
    }

    public boolean canDropRemainingChuongDongCount(int maxCount) {
        if (Util.isAfterMidnight(lastItemChuongDong)) {
            remainingChuongDongCount = maxCount;
            lastItemChuongDong = System.currentTimeMillis();
            return true;
        } else if (remainingChuongDongCount > 0) {
            remainingChuongDongCount--;
            return true;
        }
        return false;
    }

    public boolean canDropRemainingBanhQuyCount(int maxCount) {
        if (Util.isAfterMidnight(lastItemBanhQuy)) {
            remainingBanhQuyCount = maxCount;
            lastItemBanhQuy = System.currentTimeMillis();
            return true;
        } else if (remainingBanhQuyCount > 0) {
            remainingBanhQuyCount--;
            return true;
        }
        return false;
    }

    public boolean canDropRemainingCaTuyetCount(int maxCount) {
        if (Util.isAfterMidnight(lastItemCaTuyet)) {
            remainingCaTuyetCount = maxCount;
            lastItemCaTuyet = System.currentTimeMillis();
            return true;
        } else if (remainingCaTuyetCount > 0) {
            remainingCaTuyetCount--;
            return true;
        }
        return false;
    }

    public boolean canDropRemainingKeoDuongCount(int maxCount) {
        if (Util.isAfterMidnight(lastItemKeoDuong)) {
            remainingKeoDuongCount = maxCount;
            lastItemKeoDuong = System.currentTimeMillis();
            return true;
        } else if (remainingKeoDuongCount > 0) {
            remainingKeoDuongCount--;
            return true;
        }
        return false;
    }

    public boolean canDropRemainingKeoNguoiTuyetCount(int maxCount) {
        if (Util.isAfterMidnight(lastItemKeoNguoiTuyet)) {
            remainingKeoNguoiTuyetCount = maxCount;
            lastItemKeoNguoiTuyet = System.currentTimeMillis();
            return true;
        } else if (remainingKeoNguoiTuyetCount > 0) {
            remainingKeoNguoiTuyetCount--;
            return true;
        }
        return false;
    }

    public boolean canDropHoaHong(int maxCount) {
        if (Util.isAfterMidnight(lastHHTime)) {
            remainingHHCount = maxCount;
            lastHHTime = System.currentTimeMillis();
            return true;
        } else if (remainingHHCount > 0) {
            remainingHHCount--;
            return true;
        }
        return false;
    }

    public boolean canDropBinhNuoc(int maxCount) {
        if (Util.isAfterMidnight(lastBNTime)) {
            remainingBNCount = maxCount;
            lastBNTime = System.currentTimeMillis();
            return true;
        } else if (remainingBNCount > 0) {
            remainingBNCount--;
            return true;
        }
        return false;
    }

    public boolean canDropManhVo(int maxCount) {
        if (Util.isAfterMidnight(lastItemManhVo)) {
            remainingManhVo = maxCount;
            lastItemManhVo = System.currentTimeMillis();
            return true;
        } else if (remainingManhVo > 0) {
            remainingManhVo--;
            return true;
        }
        return false;
    }

    public int consumeLimitedItemDrop(int itemId, int quantity, int maxPerDay) {
        if (quantity <= 0 || maxPerDay <= 0) {
            return 0;
        }
        if (Util.isAfterMidnight(lastLimitedItemDropTime)) {
            limitedItemDropCounts.clear();
            lastLimitedItemDropTime = System.currentTimeMillis();
        }
        int current = limitedItemDropCounts.getOrDefault(itemId, 0);
        if (current >= maxPerDay) {
            return 0;
        }
        int allow = Math.min(quantity, maxPerDay - current);
        limitedItemDropCounts.put(itemId, current + allow);
        return allow;
    }

    public String toLimitedItemDropJson() {
        try {
            JSONObject obj = new JSONObject();
            for (Map.Entry<Integer, Integer> entry : limitedItemDropCounts.entrySet()) {
                obj.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return obj.toJSONString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public void loadLimitedItemDropJson(String json) {
        limitedItemDropCounts.clear();
        if (json == null || json.isEmpty()) {
            return;
        }
        try {
            Object parsed = JSONValue.parse(json);
            if (!(parsed instanceof JSONObject)) {
                return;
            }
            JSONObject jsonObject = (JSONObject) parsed;
            for (Object key : jsonObject.keySet()) {
                try {
                    int itemId = Integer.parseInt(String.valueOf(key));
                    int count = Integer.parseInt(String.valueOf(jsonObject.get(key)));
                    if (count > 0) {
                        limitedItemDropCounts.put(itemId, count);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
}
