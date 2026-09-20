package backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Full MySQL backup online bang consistent transaction. */
public final class DatabaseBackupService {

    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public Path createFullBackup(BackupConfig config) throws Exception {
        if (!RUNNING.compareAndSet(false, true)) {
            throw new IllegalStateException("Một tác vụ full backup khác đang chạy");
        }
        Path clientConfig = null;
        Path sql = null;
        try {
            DatabaseSettings db = DatabaseSettings.load();
            Path directory = config.localDirectory().resolve("full");
            Files.createDirectories(directory);
            String base = "nro-full-" + LocalDateTime.now().format(FILE_TIME);
            sql = directory.resolve(base + ".sql.part");
            Path compressedPart = directory.resolve(base + ".sql.gz.part");
            Path completed = directory.resolve(base + ".sql.gz");
            Path processLog = directory.resolve(base + ".mysqldump.log");
            clientConfig = db.createTemporaryClientConfig();

            List<String> command = new ArrayList<>();
            command.add(config.mysqldumpPath());
            command.add("--defaults-extra-file=" + clientConfig.toAbsolutePath());
            command.add("--single-transaction");
            command.add("--quick");
            command.add("--skip-lock-tables");
            command.add("--routines");
            command.add("--events");
            command.add("--triggers");
            command.add("--hex-blob");
            command.add("--default-character-set=utf8mb4");
            command.add("--result-file=" + sql.toAbsolutePath());
            command.add(db.database());

            runProcess(command, processLog, 2, TimeUnit.HOURS, "mysqldump");
            Files.deleteIfExists(processLog);
            gzip(sql, compressedPart);
            moveCompleted(compressedPart, completed);
            Files.deleteIfExists(sql);
            cleanupOldFiles(directory, "nro-full-", config.retentionDays());
            return completed;
        } finally {
            if (clientConfig != null) Files.deleteIfExists(clientConfig);
            if (sql != null) Files.deleteIfExists(sql);
            RUNNING.set(false);
        }
    }

    /** Chi goi khi server game chua chay. Tu dong tao emergency backup truoc restore. */
    public void restoreFullBackup(Path gzipBackup, BackupConfig config) throws Exception {
        if (!Files.isRegularFile(gzipBackup)) throw new IOException("Không tìm thấy backup: " + gzipBackup);
        createFullBackup(config);

        DatabaseSettings db = DatabaseSettings.load();
        Path clientConfig = db.createTemporaryClientConfig();
        Path restoreDir = config.localDirectory().resolve("restore-temp");
        Files.createDirectories(restoreDir);
        Path sql = Files.createTempFile(restoreDir, "full-restore-", ".sql");
        Path log = Files.createTempFile(restoreDir, "mysql-restore-", ".log");
        try {
            try (InputStream in = new GZIPInputStream(Files.newInputStream(gzipBackup));
                 OutputStream out = Files.newOutputStream(sql)) {
                in.transferTo(out);
            }
            List<String> command = List.of(
                    config.mysqlPath(),
                    "--defaults-extra-file=" + clientConfig.toAbsolutePath(),
                    "--default-character-set=utf8mb4",
                    db.database());
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectInput(sql.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(log.toFile());
            Process process = builder.start();
            if (!process.waitFor(2, TimeUnit.HOURS)) {
                process.destroyForcibly();
                throw new IOException("mysql restore quá thời gian 2 giờ");
            }
            if (process.exitValue() != 0) {
                throw new IOException("mysql restore thất bại: " + readLog(log));
            }
        } finally {
            Files.deleteIfExists(clientConfig);
            Files.deleteIfExists(sql);
            Files.deleteIfExists(log);
        }
    }

    static void cleanupOldFiles(Path directory, String prefix, int retentionDays) throws IOException {
        if (!Files.isDirectory(directory)) return;
        long cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays);
        try (var files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .filter(path -> !path.getFileName().toString().endsWith(".part"))
                    .forEach(path -> {
                        try {
                            if (Files.getLastModifiedTime(path).toMillis() < cutoff) Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    private static void runProcess(List<String> command, Path log, long timeout, TimeUnit unit, String name)
            throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        builder.redirectOutput(log.toFile());
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new IOException("Không chạy được " + name + ". Kiểm tra đường dẫn trong Setup: " + e.getMessage(), e);
        }
        if (!process.waitFor(timeout, unit)) {
            process.destroyForcibly();
            throw new IOException(name + " quá thời gian cho phép");
        }
        if (process.exitValue() != 0) {
            throw new IOException(name + " thất bại: " + readLog(log));
        }
    }

    private static String readLog(Path log) {
        try {
            String text = Files.readString(log);
            return text.length() > 2000 ? text.substring(text.length() - 2000) : text;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private static void gzip(Path source, Path target) throws IOException {
        try (InputStream in = Files.newInputStream(source);
             OutputStream out = new GZIPOutputStream(Files.newOutputStream(target))) {
            in.transferTo(out);
        }
    }

    private static void moveCompleted(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
