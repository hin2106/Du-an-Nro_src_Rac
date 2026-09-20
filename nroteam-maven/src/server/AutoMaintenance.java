package server;
import utils.Logger;

import java.time.LocalDate;
import java.time.LocalTime;

public class AutoMaintenance extends Thread {

    public static boolean isRunning = false;
    private static AutoMaintenance instance;
    public static boolean AutoMaintenance = Manager.AUTO_MAINTENANCE == 1;
    public static final int hours = Manager.AUTO_MAINTENANCE_HOUR;
    public static final int mins = Manager.AUTO_MAINTENANCE_MINUTE;
    private LocalDate lastMaintenanceDate = null;

    public static AutoMaintenance gI() {
        if (instance == null) {
            instance = new AutoMaintenance();
        }
        return instance;
    }

    @Override
    public void run() {
        while (!isRunning) {
            try {
                if (AutoMaintenance) {
                    LocalTime currentTime = LocalTime.now();
                    LocalDate currentDate = LocalDate.now();
                    if (currentTime.getHour() == hours && currentTime.getMinute() == mins) {
                        if (lastMaintenanceDate == null || !lastMaintenanceDate.equals(currentDate)) {
                            Logger.log("Đang tiến hành quá trình bảo trì tự động\n");
                            lastMaintenanceDate = currentDate;
                            Maintenance.gI().start(60);
                            isRunning = true;
                        }
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
