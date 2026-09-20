package backup;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import utils.Logger;

/** Mot diem dieu phoi chung cho scheduler va cac nut backup thu cong. */
public final class BackupCoordinator {

    private static final BackupCoordinator INSTANCE = new BackupCoordinator();
    private final DatabaseBackupService database = new DatabaseBackupService();
    private final PlayerSnapshotService players = new PlayerSnapshotService();
    private final GoogleDriveBackupService drive = new GoogleDriveBackupService();

    private BackupCoordinator() {}
    public static BackupCoordinator gI() { return INSTANCE; }

    public Path backupDatabaseNow() throws Exception {
        BackupConfig config = new BackupConfig();
        Path result = database.createFullBackup(config);
        uploadIfEnabled(result, "Full Database", config);
        Logger.log("[Backup] Full database hoàn tất: " + result + "\n");
        return result;
    }

    public Path backupPlayersNow() throws Exception {
        BackupConfig config = new BackupConfig();
        Path result = players.createAllPlayersSnapshot(config);
        uploadIfEnabled(result, "Player Snapshots", config);
        Logger.log("[Backup] Player snapshot hoàn tất: " + result + "\n");
        return result;
    }

    public String restorePlayer(Path archive, String selector) throws Exception {
        Path emergency = players.createAllPlayersSnapshot(new BackupConfig());
        Logger.log("[Backup] Emergency player snapshot trước restore: " + emergency + "\n");
        String restored = players.restorePlayerInventory(archive, selector);
        Logger.log("[Backup] Đã restore tài sản player: " + restored + "\n");
        return restored;
    }

    public void restoreDatabase(Path archive) throws Exception {
        database.restoreFullBackup(archive, new BackupConfig());
        Logger.log("[Backup] Đã restore full database từ: " + archive + "\n");
    }

    private void uploadIfEnabled(Path file, String category, BackupConfig config) throws Exception {
        if (!config.googleEnabled()) return;
        try {
            String id = drive.upload(file, category, config);
            Files.writeString(uploadMarker(file), id, StandardCharsets.UTF_8);
            Logger.log("[Backup] Google Drive upload hoàn tất, fileId=" + id + "\n");
        } catch (Exception e) {
            Logger.error("[Backup] File local an toàn nhưng upload Drive chưa thành công; scheduler sẽ thử lại: "
                    + rootMessage(e) + "\n");
        }
    }

    public void retryPendingUploads() {
        BackupConfig config = new BackupConfig();
        if (!config.googleEnabled() || !drive.hasStoredAuthorization()) return;
        retryDirectory(config.localDirectory().resolve("full"), ".sql.gz", "Full Database", config);
        retryDirectory(config.localDirectory().resolve("players"), ".zip", "Player Snapshots", config);
    }

    private void retryDirectory(Path directory, String suffix, String category, BackupConfig config) {
        if (!Files.isDirectory(directory)) return;
        try (var paths = Files.list(directory)) {
            for (Path file : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .filter(path -> !Files.exists(uploadMarker(path)))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .limit(3).toList()) {
                uploadIfEnabled(file, category, config);
            }
        } catch (Exception e) {
            Logger.error("[Backup] Retry upload Drive thất bại: " + rootMessage(e) + "\n");
        }
    }

    private Path uploadMarker(Path file) {
        return file.resolveSibling(file.getFileName() + ".drive-uploaded");
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.toString() : current.getMessage();
    }
}
