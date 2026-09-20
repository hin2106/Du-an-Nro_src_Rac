package audit;

import daos.NDVSqlFetcher;
import daos.PlayerDAO;
import data.AlyraManager;
import item.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mail.entity.MailReward;
import mail.enums.RewardType;
import mail.service.MailService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;
import server.Client;
import services.player.InventoryService;

/** Thu hoi mot lo vat pham theo trace va hoan ve chu goc bang mail. */
public final class AssetRestoreService {
    private static final AssetRestoreService INSTANCE = new AssetRestoreService();
    public static AssetRestoreService gI() { return INSTANCE; }

    public synchronized String restoreTrace(String traceId, String reason) throws Exception {
        traceId = traceId == null ? null : traceId.trim();
        ensureNotRestored(traceId);
        if (traceId == null || traceId.isBlank()) throw new IllegalArgumentException("Trace ID trống");
        List<AssetAuditEvent> events = AssetAuditService.gI().trace(traceId);
        if (events.isEmpty()) throw new IllegalArgumentException("Không có lịch sử trace trong 3 ngày gần nhất");

        AssetAuditEvent first = events.get(0);
        if (!"ITEM".equals(first.assetType) || first.itemTemplateId == null)
            throw new IllegalArgumentException("Restore theo chuỗi hiện chỉ áp dụng cho vật phẩm/thỏi vàng dạng item");
        AssetAuditEvent terminal = events.get(events.size() - 1);
        if ("GROUND_EXPIRED".equals(terminal.eventType)) {
            return restoreExpiredGroundItem(traceId, events, terminal, reason);
        }
        Long ownerId = first.fromPlayerId;
        String ownerName = first.fromPlayerName;
        if (ownerId == null) throw new IllegalStateException("Không xác định được nguyên chủ từ sự kiện đầu tiên");

        Map<Long, Long> balances = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();
        for (AssetAuditEvent e : events) {
            if (e.fromPlayerId != null) {
                balances.merge(e.fromPlayerId, -Math.abs(e.quantity), Long::sum);
                names.put(e.fromPlayerId, e.fromPlayerName);
            }
            if (e.toPlayerId != null) {
                balances.merge(e.toPlayerId, Math.abs(e.quantity), Long::sum);
                names.put(e.toPlayerId, e.toPlayerName);
            }
        }
        balances.remove(ownerId);

        long totalRemoved = 0;
        Item sample = null;
        StringBuilder evidence = new StringBuilder(buildEvidence(events));
        evidence.append("\nKẾT QUẢ THU HỒI:\n");
        MailService mail = new MailService();
        for (Map.Entry<Long, Long> holder : balances.entrySet()) {
            long expected = Math.max(0, holder.getValue());
            if (expected == 0) continue;
            Player player = Client.gI().getPlayer(holder.getKey());
            boolean offline = player == null;
            if (offline) player = NDVSqlFetcher.loadById(holder.getKey());
            if (player == null) {
                evidence.append("- Không tải được player ").append(holder.getKey()).append('\n');
                continue;
            }
            Removal removal = removeTrace(player, traceId, expected);
            if (removal.quantity > 0) {
                if (sample == null) sample = removal.sample;
                PlayerDAO.updatePlayer(player);
                totalRemoved += removal.quantity;
                String asset = removal.sample == null ? first.itemName : removal.sample.template.name;
                String content = "Bạn bị thu hồi \"" + asset + "\" số lượng " + removal.quantity
                        + " vì lý do \"" + normalizeReason(reason) + "\", vật phẩm bất chính cần hoàn về nguyên chủ \""
                        + ownerName + "\". Nếu có bất kỳ thắc mắc hay khiếu nại xin vui lòng liên hệ admin hoặc bộ phận hỗ trợ. Xin cảm ơn.";
                mail.sendMail(player.id, "Thông báo thu hồi vật phẩm", content, 7);
                AssetAuditService.gI().recordItem("RESTORE_REVOKE", traceId, removal.sample, removal.quantity,
                        player, null, player, "Thu hồi theo quyết định admin; nguyên chủ: " + ownerName);
                evidence.append("- Thu hồi ").append(removal.quantity).append(" từ ")
                        .append(player.id).append(" - ").append(player.name).append('\n');
                if (!offline) {
                    InventoryService.gI().sendItemBags(player);
                    InventoryService.gI().sendItemBody(player);
                    InventoryService.gI().sendItemBox(player);
                }
            }
        }
        if (totalRemoved <= 0 || sample == null) throw new IllegalStateException("Không tìm thấy vật phẩm mang trace trong tài sản hiện tại của các tài khoản nhận");

        long mailId = mail.sendMail(ownerId, "Hoàn trả vật phẩm đã thu hồi",
                "Admin đã truy vết và thu hồi " + totalRemoved + " " + sample.template.name
                        + " trong chuỗi " + traceId + ". Vật phẩm được đính kèm mail này.", 7);
        mail.getRewardDAO().saveReward(new MailReward(mailId, RewardType.ITEM.getCode(),
                sample.template.id, (int) Math.min(Integer.MAX_VALUE, totalRemoved), optionsJson(sample)));
        mail.getMailDAO().updateHasReward(mailId, true);

        evidence.append("- Hoàn về nguyên chủ qua mail: ").append(totalRemoved).append('\n');
        saveCase(traceId, ownerId, ownerName, first.itemTemplateId, first.itemName, totalRemoved,
                normalizeReason(reason), evidence.toString());
        return evidence.toString();
    }

    private String restoreExpiredGroundItem(String traceId, List<AssetAuditEvent> events,
            AssetAuditEvent expired, String reason) throws Exception {
        AssetAuditEvent lastDrop = null;
        for (int i = events.size() - 1; i >= 0; i--) {
            if ("PLAYER_DROP".equals(events.get(i).eventType)) { lastDrop = events.get(i); break; }
        }
        if (lastDrop == null || lastDrop.fromPlayerId == null)
            throw new IllegalStateException("Không xác định được người vứt cuối cùng của vật phẩm");
        String itemData = expired.itemData != null ? expired.itemData : lastDrop.itemData;
        Item item = deserializeItem(itemData);
        if (item == null || item.template == null)
            throw new IllegalStateException("Sự kiện cũ chưa có bản chụp options; không thể hoàn chính xác vật phẩm này");
        int quantity = (int)Math.min(Integer.MAX_VALUE, Math.max(1, expired.quantity));
        item.quantity = quantity;
        String restoreReason = reason == null || reason.isBlank()
                ? "khôi phục vật phẩm tự vứt đã mất do hết thời gian trên mặt đất" : reason.trim();

        MailService mail = new MailService();
        long mailId = mail.sendMail(lastDrop.fromPlayerId, "Khôi phục vật phẩm đã mất trên mặt đất",
                "Hệ thống đã xác minh \"" + item.template.name + "\" số lượng " + quantity
                        + " do bạn vứt tại map " + lastDrop.mapId + ", khu " + lastDrop.zoneId
                        + ", tọa độ X=" + lastDrop.x + ", Y=" + lastDrop.y + " và đã biến mất do hết thời gian. "
                        + "Vật phẩm được hoàn lại theo Trace ID " + traceId + ".", 7);
        mail.getRewardDAO().saveReward(new MailReward(mailId, RewardType.ITEM.getCode(),
                item.template.id, quantity, optionsJson(item)));
        mail.getMailDAO().updateHasReward(mailId, true);

        Player online = Client.gI().getPlayer(lastDrop.fromPlayerId);
        AssetAuditService.gI().recordItem("RESTORE_RETURN_EXPIRED", traceId, item, quantity,
                null, online, online, "Hoàn item tự vứt đã mất do hết hạn; " + restoreReason);
        StringBuilder evidence = new StringBuilder(buildEvidence(events));
        evidence.append("\nKẾT QUẢ RESTORE:\n- Không thu hồi từ tài khoản khác.\n- Hoàn qua mail cho người vứt cuối cùng: ")
                .append(lastDrop.fromPlayerId).append(" - ").append(lastDrop.fromPlayerName)
                .append("\n- Vật phẩm: ").append(item.template.name).append(" x").append(quantity).append('\n');
        saveCase(traceId, lastDrop.fromPlayerId, lastDrop.fromPlayerName, item.template.id,
                item.template.name, quantity, restoreReason, evidence.toString());
        return evidence.toString();
    }

    public String buildEvidence(List<AssetAuditEvent> events) {
        StringBuilder out = new StringBuilder("BẰNG CHỨNG TRUY VẾT (giờ máy chủ)\n");
        for (AssetAuditEvent e : events) {
            out.append(e.createdAt).append(" | ").append(e.eventType).append(" | ")
                    .append(e.itemName).append(" x").append(e.quantity).append(" | ")
                    .append(label(e.fromPlayerId,e.fromPlayerName)).append(" -> ")
                    .append(label(e.toPlayerId,e.toPlayerName)).append(" | map ")
                    .append(e.mapId).append(" khu ").append(e.zoneId).append(" x=").append(e.x).append(" y=").append(e.y)
                    .append(" | ").append(e.context == null ? "" : e.context).append('\n');
        }
        return out.toString();
    }

    private Removal removeTrace(Player player, String traceId, long maximum) {
        Removal result = new Removal();
        removeFromList(player.inventory.itemsBag, traceId, maximum, result);
        removeFromList(player.inventory.itemsBody, traceId, maximum, result);
        removeFromList(player.inventory.itemsBox, traceId, maximum, result);
        removeFromList(player.inventory.itemsBox1, traceId, maximum, result);
        removeFromList(player.inventory.itemsBoxCrackBall, traceId, maximum, result);
        removeFromList(player.inventory.itemsDaBan, traceId, maximum, result);
        removeFromList(player.inventory.itemsMailBox, traceId, maximum, result);
        if (player.pet != null && player.pet.inventory != null)
            removeFromList(player.pet.inventory.itemsBody, traceId, maximum, result);
        return result;
    }

    private void removeFromList(List<Item> items, String traceId, long maximum, Removal result) {
        if (items == null || result.quantity >= maximum) return;
        for (int i=0;i<items.size() && result.quantity<maximum;i++) {
            Item item=items.get(i);
            if(item==null || item.template==null || !traceId.equals(item.auditTraceId)) continue;
            long take=Math.min(item.quantity,maximum-result.quantity);
            if(result.sample==null) result.sample=services.ItemService.gI().copyItem(item);
            result.quantity+=take;
            item.quantity-=take;
            if(item.quantity<=0) items.set(i,services.ItemService.gI().createItemNull());
        }
    }

    @SuppressWarnings("unchecked")
    private String optionsJson(Item item) {
        JSONArray all=new JSONArray();
        for(Item.ItemOption option:item.itemOptions){org.json.simple.JSONObject one=new org.json.simple.JSONObject();one.put("id",option.optionTemplate.id);one.put("param",option.param);all.add(one);}
        return all.toJSONString();
    }

    private void saveCase(String trace,long ownerId,String ownerName,int templateId,String itemName,long quantity,String reason,String evidence)throws Exception{
        try(Connection c=AlyraManager.getConnection();PreparedStatement ps=c.prepareStatement(
                "INSERT INTO asset_restore_case(trace_id,original_player_id,original_player_name,item_template_id,item_name,quantity,reason,evidence) VALUES(?,?,?,?,?,?,?,?)")){
            ps.setString(1,trace);ps.setLong(2,ownerId);ps.setString(3,ownerName);ps.setInt(4,templateId);ps.setString(5,itemName);
            ps.setLong(6,quantity);ps.setString(7,reason);ps.setString(8,evidence);ps.executeUpdate();
        }
    }
    private void ensureNotRestored(String trace) throws Exception {
        if (trace == null || trace.isBlank()) return;
        try(Connection c=AlyraManager.getConnection();PreparedStatement ps=c.prepareStatement(
                "SELECT id FROM asset_restore_case WHERE trace_id=? LIMIT 1")){
            ps.setString(1,trace);
            try(java.sql.ResultSet rs=ps.executeQuery()){
                if(rs.next())throw new IllegalStateException("Trace ID này đã được restore trước đó, mã hồ sơ: "+rs.getLong(1));
            }
        }
    }

    private Item deserializeItem(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            JSONObject root=(JSONObject)JSONValue.parse(json);
            int templateId=((Number)root.get("templateId")).intValue();
            int quantity=((Number)root.get("quantity")).intValue();
            Item item=services.ItemService.gI().createNewItem((short)templateId,Math.max(1,quantity));
            item.createTime=root.get("createTime") instanceof Number n?n.longValue():System.currentTimeMillis();
            JSONArray options=(JSONArray)root.get("options");
            if(options!=null)for(Object value:options){JSONObject option=(JSONObject)value;
                item.itemOptions.add(new Item.ItemOption(((Number)option.get("id")).intValue(),((Number)option.get("param")).intValue()));}
            return item;
        } catch(Exception ignored) { return null; }
    }
    private static String normalizeReason(String reason){return reason==null||reason.isBlank()?"vật phẩm bất chính cần hoàn về nguyên chủ":reason.trim();}
    private static String label(Long id,String name){return id==null?"MẶT ĐẤT/HỆ THỐNG":id+" - "+name;}
    private static final class Removal{long quantity;Item sample;}
}
