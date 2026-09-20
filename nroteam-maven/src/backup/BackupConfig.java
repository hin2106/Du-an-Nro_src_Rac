package backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.Properties;

/** Cau hinh rieng cho backup, khong tron token Google vao sever.properties. */
public final class BackupConfig {

    public static final Path CONFIG_PATH = Path.of("data", "config", "backup.properties");
    private final Properties values = new Properties();

    public BackupConfig() {
        values.setProperty("backup.enabled", "false");
        values.setProperty("backup.runOnStartup", "false");
        values.setProperty("backup.full.time", "03:00");
        values.setProperty("backup.player.time", "02:30");
        values.setProperty("backup.local.directory", "backup");
        values.setProperty("backup.local.retentionDays", "7");
        values.setProperty("backup.mysqldump.path", "mysqldump");
        values.setProperty("backup.mysql.path", "mysql");
        values.setProperty("backup.google.enabled", "false");
        values.setProperty("backup.google.credentials", "data/config/google-drive-credentials.json");
        values.setProperty("backup.google.folderName", "NRO Server Backups");
        values.setProperty("backup.google.folderId", "");
        values.setProperty("backup.google.retentionDays", "30");
        load();
    }

    public boolean enabled() { return bool("backup.enabled"); }
    public boolean runOnStartup() { return bool("backup.runOnStartup"); }
    public boolean googleEnabled() { return bool("backup.google.enabled"); }
    public LocalTime fullTime() { return time("backup.full.time"); }
    public LocalTime playerTime() { return time("backup.player.time"); }
    public Path localDirectory() { return Path.of(get("backup.local.directory")); }
    public int retentionDays() { return Math.max(1, integer("backup.local.retentionDays", 7)); }
    public String mysqldumpPath() { return get("backup.mysqldump.path"); }
    public String mysqlPath() { return get("backup.mysql.path"); }
    public Path googleCredentialsPath() { return Path.of(get("backup.google.credentials")); }
    public String googleFolderName() { return get("backup.google.folderName"); }
    public String googleFolderId() { return get("backup.google.folderId"); }
    public int googleRetentionDays() { return Math.max(1, integer("backup.google.retentionDays", 30)); }
    public String get(String key) { return values.getProperty(key, "").trim(); }
    public void set(String key, String value) { values.setProperty(key, value == null ? "" : value.trim()); }

    public synchronized void save() throws IOException {
        validate();
        Files.createDirectories(CONFIG_PATH.getParent());
        Path temp = Files.createTempFile(CONFIG_PATH.getParent(), "backup", ".tmp");
        try (var writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            values.store(writer, "NRO automated backup configuration");
        }
        try {
            try {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
            SecureFiles.ownerOnly(CONFIG_PATH);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public void validate() {
        fullTime();
        playerTime();
        if (localDirectory().toString().isBlank()) throw new IllegalArgumentException("Thư mục backup đang trống");
        if (mysqldumpPath().isBlank()) throw new IllegalArgumentException("Đường dẫn mysqldump đang trống");
        if (mysqlPath().isBlank()) throw new IllegalArgumentException("Đường dẫn mysql đang trống");
        if (googleEnabled() && !Files.isRegularFile(googleCredentialsPath())) {
            throw new IllegalArgumentException("Không tìm thấy OAuth credentials JSON: " + googleCredentialsPath());
        }
    }

    private void load() {
        if (!Files.isRegularFile(CONFIG_PATH)) return;
        try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            values.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Không đọc được " + CONFIG_PATH + ": " + e.getMessage(), e);
        }
    }

    private boolean bool(String key) { return Boolean.parseBoolean(get(key)); }
    private int integer(String key, int fallback) {
        try { return Integer.parseInt(get(key)); } catch (Exception e) { return fallback; }
    }
    private LocalTime time(String key) {
        try { return LocalTime.parse(get(key)); }
        catch (Exception e) { throw new IllegalArgumentException(key + " phải có dạng HH:mm"); }
    }
}
