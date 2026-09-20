package server;

import daos.PlayerDAO;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoSaveManager {

    private static AutoSaveManager instance = null;

    private ScheduledExecutorService scheduler;

    private AutoSaveManager() {
    }

    public static synchronized AutoSaveManager getInstance() {
        if (instance == null) {
            instance = new AutoSaveManager();
        }
        return instance;
    }

    public void startAutoSave() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                handleAutoSave();
            } catch (Exception e) {
            }
        }, 60, 90, TimeUnit.SECONDS);
    }

    public void stopAutoSave() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    public void handleAutoSave() {
        Client.gI().getPlayers().forEach(player -> {
            PlayerDAO.updatePlayer(player);
        });
    }

    public void dispose() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        scheduler = null;
    }
}
