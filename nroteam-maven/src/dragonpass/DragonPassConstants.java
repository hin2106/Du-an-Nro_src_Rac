package dragonpass;

public final class DragonPassConstants {
    private DragonPassConstants() {}

    public static final byte CMD = -58;

    public static final int MAX_LEVEL = 100;
    public static final int EXP_PER_LEVEL = 100;
    public static final int DEFAULT_NORMAL_PRICE = 360;
    public static final int DEFAULT_PLUS_PRICE = 720;
    public static final int MAIL_EXPIRE_DAYS = 30;
    public static final int MAIL_REWARD_LIMIT = 50;

    public static final byte PASS_NONE = 0;
    public static final byte PASS_NORMAL = 1;
    public static final byte PASS_PLUS = 2;

    public static final byte TRACK_NORMAL = 1;
    public static final byte TRACK_PLUS = 2;

    public static final byte PHASE_ACTIVE = 0;
    public static final byte PHASE_SUMMARY = 1;
    public static final byte PHASE_INACTIVE = 2;

    public static final byte STATUS_DRAFT = 0;
    public static final byte STATUS_ACTIVE = 1;
    public static final byte STATUS_SUMMARY = 2;
    public static final byte STATUS_FINALIZING = 3;
    public static final byte STATUS_FINISHED = 4;

    public static final byte ACTION_GET_STATE = 0;
    public static final byte ACTION_BUY = 1;
    public static final byte ACTION_CLAIM_REWARD = 2;
    public static final byte ACTION_CLAIM_TASK = 3;
    public static final byte ACTION_CLAIM_ALL_TASKS = 4;
    public static final byte ACTION_RESULT = 5;
    public static final byte ACTION_SEASON_CHANGED = 6;
    public static final byte ACTION_REDEEM_GIFT_CODE = 7;
    public static final byte ACTION_GET_TOP = 20;
}
