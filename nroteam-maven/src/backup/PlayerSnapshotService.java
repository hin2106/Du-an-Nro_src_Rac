package backup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Snapshot rieng phan tai san cua tung player, gom trong mot ZIP de de upload. */
public final class PlayerSnapshotService {

    private static final List<String> COLUMNS = List.of(
            "data_inventory", "items_body", "items_bag", "items_box", "items_box1",
            "items_box_lucky_round", "items_daban", "item_mails_box", "pet");
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static final Gson GSON = new Gson();

    public Path createAllPlayersSnapshot(BackupConfig config) throws Exception {
        if (!RUNNING.compareAndSet(false, true)) {
            throw new IllegalStateException("Một tác vụ snapshot player khác đang chạy");
        }
        try {
            Path directory = config.localDirectory().resolve("players");
            Files.createDirectories(directory);
            String base = "nro-players-" + LocalDateTime.now().format(FILE_TIME);
            Path part = directory.resolve(base + ".zip.part");
            Path completed = directory.resolve(base + ".zip");

            String select = "SELECT id,name," + String.join(",", COLUMNS) + " FROM player ORDER BY id";
            try (Connection connection = DatabaseSettings.load().connect()) {
                connection.setReadOnly(true);
                connection.setAutoCommit(false);
                try (PreparedStatement statement = connection.prepareStatement(select,
                        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                    statement.setFetchSize(100);
                    try (ResultSet rs = statement.executeQuery();
                         ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(part), StandardCharsets.UTF_8)) {
                        while (rs.next()) {
                            PlayerSnapshot snapshot = new PlayerSnapshot();
                            snapshot.schemaVersion = 1;
                            snapshot.createdAt = Instant.now().toString();
                            snapshot.playerId = rs.getLong("id");
                            snapshot.playerName = rs.getString("name");
                            for (String column : COLUMNS) snapshot.data.put(column, rs.getString(column));
                            String safeName = snapshot.playerName.replaceAll("[^a-zA-Z0-9._-]", "_");
                            zip.putNextEntry(new ZipEntry(snapshot.playerId + "_" + safeName + ".json"));
                            OutputStreamWriter writer = new OutputStreamWriter(zip, StandardCharsets.UTF_8);
                            GSON.toJson(snapshot, writer);
                            writer.flush();
                            zip.closeEntry();
                        }
                    }
                }
                connection.rollback();
            }
            move(part, completed);
            DatabaseBackupService.cleanupOldFiles(directory, "nro-players-", config.retentionDays());
            return completed;
        } finally {
            RUNNING.set(false);
        }
    }

    /** Restore chi cac cot tai san. Server game phai dang tat de bo nho khong ghi de nguoc lai. */
    public String restorePlayerInventory(Path zipPath, String playerSelector) throws Exception {
        if (!Files.isRegularFile(zipPath)) throw new IOException("Không tìm thấy snapshot: " + zipPath);
        String selector = playerSelector.trim();
        if (selector.isEmpty()) throw new IllegalArgumentException("Hãy nhập ID hoặc tên player");
        PlayerSnapshot snapshot = findSnapshot(zipPath, selector);
        if (snapshot == null) throw new IllegalArgumentException("Không tìm thấy player " + selector + " trong snapshot");

        String assignments = String.join("=?,", COLUMNS) + "=?";
        String sql = "UPDATE player SET " + assignments + " WHERE id=? AND LOWER(name)=LOWER(?)";
        try (Connection connection = DatabaseSettings.load().connect()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 1;
                for (String column : COLUMNS) statement.setString(index++, snapshot.data.get(column));
                statement.setLong(index++, snapshot.playerId);
                statement.setString(index, snapshot.playerName);
                int updated = statement.executeUpdate();
                if (updated != 1) {
                    connection.rollback();
                    throw new IllegalStateException("Player hiện tại không khớp đồng thời ID và tên; không restore để tránh nhầm tài khoản");
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
        return snapshot.playerId + " - " + snapshot.playerName;
    }

    private PlayerSnapshot findSnapshot(Path path, String selector) throws IOException {
        try (ZipFile zip = new ZipFile(path.toFile(), StandardCharsets.UTF_8)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".json")) continue;
                try (var reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
                    PlayerSnapshot snapshot = GSON.fromJson(reader, PlayerSnapshot.class);
                    if (selector.equals(String.valueOf(snapshot.playerId))
                            || selector.equalsIgnoreCase(snapshot.playerName)) return snapshot;
                }
            }
        }
        return null;
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static final class PlayerSnapshot {
        int schemaVersion;
        String createdAt;
        long playerId;
        String playerName;
        Map<String, String> data = new LinkedHashMap<>();
    }
}
