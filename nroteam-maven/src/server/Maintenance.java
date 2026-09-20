package server;

import services.Service;
import utils.Logger;

public class Maintenance extends Thread {

    private static Maintenance instance;
    private static boolean running = false;
    private int secondsRemaining;

    private Maintenance() {
    }

    public static Maintenance gI() {
        if (instance == null) {
            instance = new Maintenance();
        }
        return instance;
    }


    public void start(int minutes) {
        if (running) return;
        running = true;
        this.secondsRemaining = Math.max(minutes, 1) * 60;
        new Thread(this, "Thread Bảo Trì").start();
    }

    /** Bắt đầu bảo trì theo giây */
    public void startBySecond(int seconds) {
        if (running) return;
        running = true;
        this.secondsRemaining = Math.max(seconds, 1);
        new Thread(this, "Thread Bảo Trì").start();
    }

    public void start(int hours, int minutes, int seconds) {
        if (running) return;
        running = true;
        this.secondsRemaining = (hours * 3600) + (minutes * 60) + seconds;
        new Thread(this, "Thread Bảo Trì").start();
    }


    public void startImmediately() {
        if (running) return;
        running = true;
        Logger.log("[MAINTENANCE] BẮT ĐẦU NGAY LẬP TỨC\n");
        ServerManager.gI().close();
    }

    @Override
    public void run() {
        Logger.logln("[MAINTENANCE] Đếm ngược " + formatTime(secondsRemaining));

        while (secondsRemaining > 0) {
            sendAnnounce();
            sleepMillis(1000);
            secondsRemaining--;
        }

        Logger.log("[MAINTENANCE] BẮT ĐẦU TIẾN TRÌNH BẢO TRÌ\n");
        ServerManager.gI().close();
    }

    private void sendAnnounce() {
        if (secondsRemaining == 60) {
            sendMaintenanceWarning("Hệ thống sẽ bảo trì sau 1 phút nữa!");
        } else if (secondsRemaining <= 15) {
            sendMaintenanceWarning("Hệ thống sẽ bảo trì sau " + secondsRemaining + " giây nữa!");
        } else if (secondsRemaining < 60 && secondsRemaining % 5 == 0) {
            sendMaintenanceWarning("Hệ thống sẽ bảo trì sau " + secondsRemaining + " giây nữa!");
        } else if (secondsRemaining % 3600 == 0) {
            int h = secondsRemaining / 3600;
            sendMaintenanceWarning("Hệ thống sẽ bảo trì sau " + h + " giờ nữa!");
        } else if (secondsRemaining % 300 == 0 && secondsRemaining > 300) {
            int m = secondsRemaining / 60;
            sendMaintenanceWarning("Hệ thống sẽ bảo trì sau " + m + " phút nữa!");
        } else if (secondsRemaining % 60 == 0 && secondsRemaining / 60 <= 5) {
            int m = secondsRemaining / 60;
            sendMaintenanceWarning("Hệ thống sẽ bảo trì sau " + m + " phút nữa!");
        }
    }

    private void sendMaintenanceWarning(String countdownMessage) {
        Service.gI().sendThongBaoAllPlayer(
                countdownMessage + " Vui lòng thoát game để tránh mất vật phẩm."
        );
    }

    public static boolean isRunning() {
        return running;
    }

    private void sleepMillis(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private String formatTime(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
