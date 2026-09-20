package event;

public class EventUtils {

    public static String getTimeRemaining(long endTime) {
        long ms = endTime - System.currentTimeMillis();
        if (ms <= 0) {
            return "đã kết thúc";
        }
        long days = ms / 86_400_000L;
        long hours = (ms % 86_400_000L) / 3_600_000L;
        long minutes = (ms % 3_600_000L) / 60_000L;

        if (days > 0) {
            return days + " ngày nữa";
        }
        if (hours > 0) {
            return hours + " giờ nữa";
        }
        return Math.max(1, minutes) + " phút nữa";
    }


    public static String formatTopMenu(String eventName, String topType, long eventEnd, long claimDeadline) {
        return String.format(
                "Sự kiện đua Top %s nhận quà khủng\n" +
                        "Kết thúc và trao giải sau: (%s)\n" +
                        "Hạn chót nhận giải: (%s)\n" +
                        "Đến gặp Chi Chi để nhận giải nhé\n" +
                        "Chi tiết xem tại diễn đàn, fanpage.",
                topType,
                getTimeRemaining(eventEnd),
                getTimeRemaining(claimDeadline));
    }


    public static String[] buildTopMenuOptions(boolean isClaimOpen) {
        if (isClaimOpen) {
            return new String[] { "Top 100", "Xem điểm", "Nhận thưởng", "Đóng" };
        }
        return new String[] { "Top 100", "Xem điểm", "Đóng" };
    }
}
