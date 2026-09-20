package luckywheel;

public final class LuckyWheelConstants {
    private LuckyWheelConstants() {}

    public static final byte CMD = 67;

    public static final byte STATUS_DRAFT = 0;
    public static final byte STATUS_ACTIVE = 1;
    public static final byte STATUS_FINALIZING = 2;
    public static final byte STATUS_FINISHED = 3;

    public static final int OUTER_SLOT_COUNT = 16;
    public static final int SLOT_COUNT = 19;
    public static final int PITY_SLOT = 17;
    public static final int MIN_RANK_SPINS = 50;
    public static final int RANK_LIMIT = 5;
    public static final long RATE_TOTAL = 100_000_000L;
    public static final long LAST_SLOT_MIN_RATE = 10_000L; // 0.01%
    public static final int MAIL_EXPIRE_DAYS = 30;

    public static final byte ACTION_GET_STATE = 0;
    public static final byte ACTION_SPIN = 1;
    public static final byte ACTION_CLAIM_MILESTONE = 2;
    public static final byte ACTION_EVENT_ENDED = 3;
    public static final byte ACTION_CONFIG_CHANGED = 4;
}
