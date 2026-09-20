package mail.enums;

/**
 * Loại reward có thể đính kèm trong mail
 */
public enum RewardType {
    ITEM((byte) 1, "Item"),
    GOLD((byte) 2, "Vàng"),
    GEM((byte) 3, "Ngọc"),
    STAMINA((byte) 4, "Sức chịu đựng"),
    EXP((byte) 5, "Kinh nghiệm"),
    VIP_POINT((byte) 6, "VIP Point");

    private final byte code;
    private final String name;

    RewardType(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static RewardType fromCode(byte code) {
        for (RewardType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return ITEM;
    }
}
