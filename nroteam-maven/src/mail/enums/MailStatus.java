package mail.enums;

/**
 * Trạng thái của mail
 */
public enum MailStatus {
    ACTIVE((byte) 1, "Hoạt động"),
    READ((byte) 2, "Đã đọc"),
    EXPIRED((byte) 3, "Hết hạn"),
    DELETED((byte) 4, "Đã xóa");

    private final byte code;
    private final String name;

    MailStatus(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MailStatus fromCode(byte code) {
        for (MailStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ACTIVE;
    }
}
