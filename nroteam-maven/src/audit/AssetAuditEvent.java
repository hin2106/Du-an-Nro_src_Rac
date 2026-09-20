package audit;

import java.sql.Timestamp;

public final class AssetAuditEvent {
    public long id;
    public Timestamp createdAt;
    public String traceId;
    public String eventType;
    public String assetType;
    public Integer itemTemplateId;
    public String itemName;
    public long quantity;
    public Long fromPlayerId;
    public String fromPlayerName;
    public Long toPlayerId;
    public String toPlayerName;
    public Integer mapId;
    public Integer zoneId;
    public Integer x;
    public Integer y;
    public String context;
    public String itemData;
}
