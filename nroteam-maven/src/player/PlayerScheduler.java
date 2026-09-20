package player;

import utils.Logger;


public class PlayerScheduler {

    private static volatile boolean started = false;
    private static final boolean USE_OPTIMIZED_VERSION = true; // Switch để test

    public static synchronized void startScheduler() {
        if (started) {
            System.err.println("[PlayerScheduler] Already started!");
            return;
        }
        started = true;
        
        if (USE_OPTIMIZED_VERSION) {
            PlayerUpdateOptimizer.start();
        } else {
            System.err.println("[PlayerScheduler] WARNING: Using legacy single-threaded version!");
            System.err.println("[PlayerScheduler] This will cause severe lag with 1000+ players!");
        }
    }


    public static synchronized void stopScheduler() {
        if (!started) {
            return;
        }

        try {
            if (USE_OPTIMIZED_VERSION) {
                PlayerUpdateOptimizer.stop();
            }
            started = false;
            System.out.println("[PlayerScheduler] Stopped successfully.");
        } catch (Exception e) {
            Logger.logException(PlayerScheduler.class, e, "Error stopping PlayerScheduler");
        }
    }


    public static boolean isRunning() {
        return started && (USE_OPTIMIZED_VERSION ? PlayerUpdateOptimizer.isRunning() : false);
    }

    
    public static String getPerformanceInfo() {
        if (!started) {
            return "Not running";
        }
        if (USE_OPTIMIZED_VERSION) {
            return PlayerUpdateOptimizer.getPerformanceInfo();
        }
        return "Legacy mode";
    }
}
