package backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import utils.Logger;

/** Scheduler nhe, tu khoi dong sau khi server va database da san sang. */
public final class BackupScheduler {

    private static final BackupScheduler INSTANCE = new BackupScheduler();
    private static final Path STATE = Path.of("data", "backup", "scheduler-state.properties");
    private final AtomicBoolean started = new AtomicBoolean(false);
    private ScheduledExecutorService executor;

    private BackupScheduler() {}
    public static BackupScheduler gI() { return INSTANCE; }

    public void start() {
        if (!started.compareAndSet(false, true)) return;
        BackupConfig config;
        try {
            config = new BackupConfig();
            if (!config.enabled()) {
                Logger.log("[Backup] Scheduler đang tắt trong backup.properties\n");
                started.set(false);
                return;
            }
            config.validate();
        } catch (Exception e) {
            Logger.error("[Backup] Không thể bật scheduler: " + e.getMessage() + "\n");
            started.set(false);
            return;
        }

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "nro-backup-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        long initialDelay = secondsUntilNextMinute();
        executor.scheduleAtFixedRate(this::tickSafely, initialDelay, 60, TimeUnit.SECONDS);
        if (config.runOnStartup()) {
            executor.schedule(this::runStartupBackup, 2, TimeUnit.MINUTES);
        }
        Logger.log("[Backup] Scheduler đã bật. Full=" + config.fullTime()
                + ", players=" + config.playerTime() + "\n");
    }

    public void shutdown() {
        ScheduledExecutorService current = executor;
        if (current != null) current.shutdownNow();
        started.set(false);
    }

    private void tickSafely() {
        try {
            BackupConfig config = new BackupConfig();
            if (!config.enabled()) return;
            BackupCoordinator.gI().retryPendingUploads();
            LocalDate today = LocalDate.now();
            Properties state = loadState();
            if (due(config.playerTime()) && !today.toString().equals(state.getProperty("last.player"))) {
                BackupCoordinator.gI().backupPlayersNow();
                state.setProperty("last.player", today.toString());
                saveState(state);
            }
            if (due(config.fullTime()) && !today.toString().equals(state.getProperty("last.full"))) {
                BackupCoordinator.gI().backupDatabaseNow();
                state.setProperty("last.full", today.toString());
                saveState(state);
            }
        } catch (Exception e) {
            Logger.error("[Backup] Tác vụ tự động thất bại: " + rootMessage(e) + "\n");
        }
    }

    private void runStartupBackup() {
        Properties state = loadState();
        LocalDate today = LocalDate.now();
        try {
            BackupCoordinator.gI().backupPlayersNow();
            state.setProperty("last.player", today.toString());
            saveState(state);
        } catch (Exception e) {
            Logger.error("[Backup] Startup player snapshot thất bại: " + rootMessage(e) + "\n");
        }
        try {
            BackupCoordinator.gI().backupDatabaseNow();
            state.setProperty("last.full", today.toString());
            saveState(state);
        } catch (Exception e) {
            Logger.error("[Backup] Startup full backup thất bại: " + rootMessage(e) + "\n");
        }
    }

    private boolean due(LocalTime scheduled) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(scheduled);
    }

    private long secondsUntilNextMinute() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.plusMinutes(1).withSecond(0).withNano(0);
        return Math.max(1, Duration.between(now, next).toSeconds());
    }

    private Properties loadState() {
        Properties state = new Properties();
        if (!Files.isRegularFile(STATE)) return state;
        try (var reader = Files.newBufferedReader(STATE, StandardCharsets.UTF_8)) {
            state.load(reader);
        } catch (IOException ignored) {
        }
        return state;
    }

    private void saveState(Properties state) throws IOException {
        Files.createDirectories(STATE.getParent());
        Path temp = Files.createTempFile(STATE.getParent(), "scheduler-state", ".tmp");
        try (var writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            state.store(writer, "NRO backup scheduler state");
        }
        try {
            Files.move(temp, STATE, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temp, STATE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.toString() : current.getMessage();
    }
}
