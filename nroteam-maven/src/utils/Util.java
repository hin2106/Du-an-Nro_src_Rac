package utils;

import managers.boss.BossManager;
import java.text.NumberFormat;
import java.util.*;
import mob.Mob;
import npc.Npc;
import player.Player;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;
import java.time.*;

public class Util {

    private static final Random rand;
    public static boolean readInt = false;

    static {
        rand = new Random();
    }
    private static final Locale locale = new Locale("vi", "VN");
    private static final NumberFormat num = NumberFormat.getInstance(locale);

    public static int createIdBossClone(int idPlayer) {
        return -idPlayer - 1_000_000_000;
    }

    public static boolean contains(String[] arr, String key) {
        return Arrays.toString(arr).contains(key);
    }

    public static String numberToMoney(double power) {
        Locale locale = new Locale("vi", "VN");
        NumberFormat num = NumberFormat.getInstance(locale);
        num.setMaximumFractionDigits(1);
        if (power >= 1000000000) {
            return num.format((double) power / 1000000000) + " Tỷ";
        } else if (power >= 1000000) {
            return num.format((double) power / 1000000) + " Tr";
        } else if (power >= 1000) {
            return num.format((double) power / 1000) + " k";
        } else {
            return num.format(power);
        }
    }

    public static String powerToString(long power) {
        Locale locale = new Locale("vi", "VN");
        NumberFormat num = NumberFormat.getInstance(locale);
        num.setMaximumFractionDigits(1);
        if (power >= 1000000000) {
            return num.format((double) power / 1000000000) + " Tỷ";
        } else if (power >= 1000000) {
            return num.format((double) power / 1000000) + " Tr";
        } else if (power >= 1000) {
            return num.format((double) power / 1000) + " k";
        } else {
            return num.format(power);
        }
    }

    public static String remain(long end) {
        long ms = end - System.currentTimeMillis();
        if (ms <= 0) {
            return "đã kết thúc";
        }
        long days = ms / 86_400_000L;
        long hours = (ms % 86_400_000L) / 3_600_000L;
        if (days > 0) {
            return days + " ngày nữa";
        }
        if (hours > 0) {
            return hours + " giờ nữa";
        }
        long minutes = (ms % 3_600_000L) / 60_000L;
        return Math.max(1, minutes) + " phút nữa";
    }

    public static int getDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static int getDistance(Player pl1, Player pl2) {
        return getDistance(pl1.location.x, pl1.location.y, pl2.location.x, pl2.location.y);
    }

    public static int getDistance(Player pl, Npc npc) {
        return getDistance(pl.location.x, pl.location.y, npc.cx, npc.cy);
    }

    public static int getDistance(Player pl, Mob mob) {
        return getDistance(pl.location.x, pl.location.y, mob.location.x, mob.location.y);
    }

    public static long parseTime(String s) {
        try {
            return TimeUtil.getTime(s, "dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            return 0L;
        }
    }

    public static String relativeTimeShort(long last) {
        if (last <= 0) {
            return "vừa xong";
        }
        long ms = System.currentTimeMillis() - last;
        if (ms < 0) {
            ms = 0;
        }
        long months = ms / (30L * 24 * 60 * 60 * 1000);
        long days = (ms / (24L * 60 * 60 * 1000));
        long hours = (ms / (60L * 60 * 1000));
        long minutes = (ms / (60L * 1000));
        if (months > 0) {
            return months + " tháng trước";
        } else if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "vừa xong";
        }
    }

    public static int getDistance(Mob mob1, Mob mob2) {
        return getDistance(mob1.location.x, mob1.location.y, mob2.location.x, mob2.location.y);
    }

    public static int nextInt(int from, int to) {
        return from + rand.nextInt(to - from + 1);
    }

    public static int nextInt(int max) {
        if (max <= 0) {
            return 0;
        }
        return rand.nextInt(max);
    }

    public static long nextLong(long from, long to) {
        if (from > to) {
            long temp = from;
            from = to;
            to = temp;
        }
        if (from == to) {
            return from;
        }
        long range = to - from + 1;
        long value = Math.abs(rand.nextLong()) % range;
        return from + value;
    }

    public static long nextLong(long max) {
        if (max <= 0) {
            return 0;
        }
        // Trả về giá trị trong khoảng [0, max)
        return Math.abs(rand.nextLong()) % max;
    }

    public static double nextDouble(double max) {
        return rand.nextDouble() * max;
    }

    public static int nextInt(int[] percen) {
        int next = nextInt(1000), i;
        for (i = 0; i < percen.length; i++) {
            if (next < percen[i]) {
                return i;
            }
            next -= percen[i];
        }
        return i;
    }

    public static int getOne(int n1, int n2) {
        return rand.nextInt() % 2 == 0 ? n1 : n2;
    }

    public static String replace(String text, String regex, String replacement) {
        return text.replace(regex, replacement);
    }

    /**
     * Kiểm tra xác suất với tỉ lệ chính xác
     *
     * @param ratioPercentage Số phần muốn đạt được (ví dụ: 1)
     * @param totalPercentage Tổng số phần (ví dụ: 100 = 1%)
     * @return true nếu random số trong [0, totalPercentage) < ratioPercentage
     *
     * Ví dụ: isTrue(1, 100) = 1% cơ hội isTrue(50, 100) = 50% cơ hội
     */
    public static boolean isTrue(long ratioPercentage, long totalPercentage) {
        // Validation: xử lý các trường hợp edge cases
        if (totalPercentage <= 0) {
            return false; // Không có xác suất nếu total <= 0
        }
        if (ratioPercentage <= 0) {
            return false; // Không có xác suất nếu ratio <= 0
        }
        if (ratioPercentage >= totalPercentage) {
            return true; // 100% hoặc hơn thì luôn true
        }

        // Tạo số ngẫu nhiên trong khoảng [0, totalPercentage)
        long randomValue = Util.nextLong(totalPercentage);

        // So sánh chính xác: randomValue < ratioPercentage
        return randomValue < ratioPercentage;
    }

    /**
     * Overload với float: tự động nhân 100 nếu ratioPercentage < 1 Ví dụ:
     * isTrue(0.01f, 100) hoặc isTrue(1.0f, 100) đều = 1%
     */
    public static boolean isTrue(float ratioPercentage, long totalPercentage) {
        if (ratioPercentage < 0) {
            return false;
        }
        if (ratioPercentage < 1) {
            // Nếu < 1, coi như decimal (ví dụ: 0.01 = 1%)
            // Nhân cả hai lên để giữ nguyên tỉ lệ
            ratioPercentage *= 100;
            totalPercentage *= 100;
        }
        return isTrue((long) ratioPercentage, totalPercentage);
    }

    /**
     * Kiểm tra xác suất với độ chính xác cao hơn (accuracy multiplier) Tăng độ
     * chính xác bằng cách nhân cả ratio và total với accuracy
     *
     * @param accuracy Hệ số độ chính xác (ví dụ: 10 = 10x chính xác hơn)
     */
    public static boolean isTrue(long ratioPercentage, long totalPercentage, int accuracy) {
        if (accuracy <= 0) {
            return isTrue(ratioPercentage, totalPercentage);
        }

        // Validation
        if (totalPercentage <= 0 || ratioPercentage <= 0) {
            return false;
        }
        if (ratioPercentage >= totalPercentage) {
            return true;
        }

        // Tăng độ chính xác: nhân cả hai với accuracy
        long scaledRatio = ratioPercentage * accuracy;
        long scaledTotal = totalPercentage * accuracy;

        // Kiểm tra điều kiện chính xác hơn: random trong [0, scaledTotal) < scaledRatio
        // Và thêm một lớp kiểm tra ngẫu nhiên khác với accuracy
        boolean mainCheck = Util.nextLong(scaledTotal) < scaledRatio;
        boolean accuracyCheck = Util.nextInt(accuracy) == 0;

        // Cả hai điều kiện phải thỏa mãn
        return mainCheck && accuracyCheck;
    }

    public static int highlightsItem(boolean highlights, int value) {
        double highlightsNumber = 1.1;
        return highlights ? (int) (value * highlightsNumber) : value;
    }

    /**
     * Overload với float và accuracy
     */
    public static boolean isTrue(float ratioPercentage, long totalPercentage, int accuracy) {
        if (ratioPercentage < 0) {
            return false;
        }
        if (ratioPercentage < 1) {
            // Nếu < 1, coi như decimal (ví dụ: 0.01 = 1%)
            ratioPercentage *= 100;
            totalPercentage *= 100;
        }
        return isTrue((long) ratioPercentage, totalPercentage, accuracy);
    }

    public static boolean haveSpecialCharacter(String text) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        boolean b = m.find();
        return b || text.contains(" ");
    }

    public static boolean canDoWithTime(long lastTime, long miniTimeTarget) {
        long elapsed = System.currentTimeMillis() - lastTime;
        return elapsed > miniTimeTarget;
    }

    public static boolean isTimeExpired(long lastTime, long duration) {
        long elapsed = System.currentTimeMillis() - lastTime;
        return elapsed >= duration;
    }

    private static final char[] SOURCE_CHARACTERS = {'À', 'Á', 'Â', 'Ã', 'È', 'É',
        'Ê', 'Ì', 'Í', 'Ò', 'Ó', 'Ô', 'Õ', 'Ù', 'Ú', 'Ý', 'à', 'á', 'â',
        'ã', 'è', 'é', 'ê', 'ì', 'í', 'ò', 'ó', 'ô', 'õ', 'ù', 'ú', 'ý',
        'Ă', 'ă', 'Đ', 'đ', 'Ĩ', 'ĩ', 'Ũ', 'ũ', 'Ơ', 'ơ', 'Ư', 'ư', 'Ạ',
        'ạ', 'Ả', 'ả', 'Ấ', 'ấ', 'Ầ', 'ầ', 'Ẩ', 'ẩ', 'Ẫ', 'ẫ', 'Ậ', 'ậ',
        'Ắ', 'ắ', 'Ằ', 'ằ', 'Ẳ', 'ẳ', 'Ẵ', 'ẵ', 'Ặ', 'ặ', 'Ẹ', 'ẹ', 'Ẻ',
        'ẻ', 'Ẽ', 'ẽ', 'Ế', 'ế', 'Ề', 'ề', 'Ể', 'ể', 'Ễ', 'ễ', 'Ệ', 'ệ',
        'Ỉ', 'ỉ', 'Ị', 'ị', 'Ọ', 'ọ', 'Ỏ', 'ỏ', 'Ố', 'ố', 'Ồ', 'ồ', 'Ổ',
        'ổ', 'Ỗ', 'ỗ', 'Ộ', 'ộ', 'Ớ', 'ớ', 'Ờ', 'ờ', 'Ở', 'ở', 'Ỡ', 'ỡ',
        'Ợ', 'ợ', 'Ụ', 'ụ', 'Ủ', 'ủ', 'Ứ', 'ứ', 'Ừ', 'ừ', 'Ử', 'ử', 'Ữ',
        'ữ', 'Ự', 'ự',};

    private static final char[] DESTINATION_CHARACTERS = {'A', 'A', 'A', 'A', 'E',
        'E', 'E', 'I', 'I', 'O', 'O', 'O', 'O', 'U', 'U', 'Y', 'a', 'a',
        'a', 'a', 'e', 'e', 'e', 'i', 'i', 'o', 'o', 'o', 'o', 'u', 'u',
        'y', 'A', 'a', 'D', 'd', 'I', 'i', 'U', 'u', 'O', 'o', 'U', 'u',
        'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A',
        'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'A', 'a', 'E', 'e',
        'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E', 'e', 'E',
        'e', 'I', 'i', 'I', 'i', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o',
        'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O', 'o', 'O',
        'o', 'O', 'o', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u', 'U', 'u',
        'U', 'u', 'U', 'u',};

    public static char removeAccent(char ch) {
        int index = Arrays.binarySearch(SOURCE_CHARACTERS, ch);
        if (index >= 0) {
            ch = DESTINATION_CHARACTERS[index];
        }
        return ch;
    }

    public static String removeAccent(String str) {
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < sb.length(); i++) {
            sb.setCharAt(i, removeAccent(sb.charAt(i)));
        }
        return sb.toString();
    }

    public static Object[] addArray(Object[]... arrays) {
        if (arrays == null || arrays.length == 0) {
            return null;
        }
        if (arrays.length == 1) {
            return arrays[0];
        }
        Object[] arr0 = arrays[0];
        for (int i = 1; i < arrays.length; i++) {
            arr0 = ArrayUtils.addAll(arr0, arrays[i]);
        }
        return arr0;
    }

    public static int randomBossId() {
        int bossId = Util.nextInt(-1000000, -100000);
        while (BossManager.gI().getBossById(bossId) != null) {
            bossId = Util.nextInt(-1000000, 100000);
        }
        return bossId;
    }

    public static boolean isAfterMidnight(long currenttimemillis) {
        Instant instant = Instant.ofEpochMilli(currenttimemillis);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        LocalDate otherDate = zonedDateTime.toLocalDate();
        LocalDate currentDate = LocalDate.now();
        return currentDate.isAfter(otherDate);
    }

    public static boolean isTimeDifferenceGreaterThanNDays(long setTime, int nDays) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - setTime;
        long daysDifference = timeDifference / 86400000;
        return daysDifference >= nDays;
    }

    public static String formatNumber(long j) {
        long j2 = (j / 1000) + 1;
        String str = "";
        int i = 0;
        while (((long) i) < j2) {
            if (j >= 1000) {
                long j3 = j % 1000;
                if (j3 == 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(".000");
                    stringBuilder.append(str);
                    str = stringBuilder.toString();
                } else {
                    StringBuilder stringBuilder2;
                    String str2;
                    if (j3 < 10) {
                        stringBuilder2 = new StringBuilder();
                        str2 = ".00";
                    } else if (j3 < 100) {
                        stringBuilder2 = new StringBuilder();
                        str2 = ".0";
                    } else {
                        stringBuilder2 = new StringBuilder();
                        str2 = ".";
                    }
                    stringBuilder2.append(str2);
                    stringBuilder2.append(j3);
                    stringBuilder2.append(str);
                    str = stringBuilder2.toString();
                }
                j /= 1000;
                i++;
            } else {
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append(j);
                stringBuilder3.append(str);
                return stringBuilder3.toString();
            }
        }
        return str;
    }

    public static String format(double power) {
        return num.format(power);
    }

    public static long maxIntValue(double a) {
        if (readInt) {
            if (a > 2_123_456_789D) {
                a = 2_123_456_789D;
            }
            return (long) (int) a;
        } else {
            if (a > Long.MAX_VALUE) {
                a = Long.MAX_VALUE;
            } else if (a < Long.MIN_VALUE) {
                a = Long.MIN_VALUE;
            }
            return (long) a;
        }
    }

    public static int maxIntValue(long current, long max) {
        if (max <= 0) {
            return 0;
        }
        if (current >= max) {
            return Integer.MAX_VALUE;
        }
        double ratio = (double) current / (double) max;
        return (int) (ratio * Integer.MAX_VALUE);
    }

    public static String number(long number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number).replace(',', '.');
    }


    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "").replace("\"", "").replace("\\", "").replace("[", "");
    }
}
