package player;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import utils.Logger;

public class AttendanceRewardLog {

    public int day;   
    public long timestamp; 
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    private static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();
    public AttendanceRewardLog(int day, long timestamp) {
        this.day = day;
        this.timestamp = timestamp;
    }
    public String getFormattedLog() {
        String formattedTime;
        try {
            // Bước 1: Kiểm tra tính hợp lệ cơ bản của timestamp
            if (this.timestamp <= 0) {
                formattedTime = "TG không hợp lệ";
                // Logger.log("Warning: Invalid timestamp (<= 0) for attendance log day " + this.day); // Ghi log nếu cần
            } else {
                // Bước 2: Chuyển đổi và định dạng thời gian
                Instant instant = Instant.ofEpochMilli(this.timestamp);
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, SYSTEM_ZONE_ID);
                formattedTime = dateTime.format(DATE_TIME_FORMATTER);
            }
        } catch (DateTimeParseException | ArithmeticException | NullPointerException e) {
            // Bước 3: Xử lý các lỗi phổ biến khi định dạng/chuyển đổi thời gian
            //Logger.log("ERROR formatting timestamp " + this.timestamp + " for day " + this.day + ": " + e.getMessage());
            formattedTime = "Lỗi giờ"; // Trả về thông báo lỗi ngắn gọn
        } catch (Exception e) {
            // Bước 4: Xử lý các lỗi không mong muốn khác
            //Logger.log("UNEXPECTED ERROR formatting log for day " + this.day + ": " + e.getMessage());
            e.printStackTrace(); // In lỗi chi tiết ra log server
            formattedTime = "Lỗi"; // Thông báo lỗi chung
        }
        return String.format("|7|- Ngày %d (%s)", this.day, formattedTime);
    }
    public static Comparator<AttendanceRewardLog> DayComparatorAsc = Comparator.comparingInt(log -> log.day);
    public static Comparator<AttendanceRewardLog> DayComparatorDesc = DayComparatorAsc.reversed();
    public static Comparator<AttendanceRewardLog> TimeComparatorDesc = Comparator.comparingLong((AttendanceRewardLog log) -> log.timestamp).reversed();
}