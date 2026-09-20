package setup;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path; 
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Quan ly setup nen tang truoc khi server khoi dong. */
public final class SetupManager {

    private static final int SETUP_VERSION = 1;
    private static final Path SERVER_CONFIG = Path.of("data", "config", "sever.properties");
    private static final Path SETUP_STATE = Path.of("data", "config", "setup.properties");

    public boolean isSetupComplete() {
        if (!Files.isRegularFile(SERVER_CONFIG) || !Files.isRegularFile(SETUP_STATE)) {
            return false;
        }

        Properties state = loadProperties(SETUP_STATE);
        return Boolean.parseBoolean(state.getProperty("setup.completed", "false"))
                && parseInt(state.getProperty("setup.version"), 0) >= SETUP_VERSION
                && hasRequiredConfiguration(loadProperties(SERVER_CONFIG));
    }

    public void runInteractiveSetup(BufferedReader input) throws Exception {
        Properties current = getSavedConfiguration();

        System.out.println();
        System.out.println("--- SETUP SERVER ---");
        System.out.println("Nhan Enter de giu gia tri hien tai trong dau [].");
        System.out.println("Mat khau nhap tai day khong duoc ghi vao log.");

        Map<String, String> changes = new LinkedHashMap<>();

        System.out.println();
        System.out.println("[1/2] Cau hinh server");
        ask(input, changes, current, "server.name", "Ten server", "Nro Luna", false);
        ask(input, changes, current, "server.ip", "IP/domain client dung de ket noi", "127.0.0.1", false);
        askPort(input, changes, current, "server.port", "Cong game", "14445");
        ask(input, changes, current, "server.bind.ip", "IP bind (de trong = moi interface)", "", false);
        askBoolean(input, changes, current, "server.usenettyproxy", "Bat Netty proxy", "true");
        askPort(input, changes, current, "server.backendport", "Cong backend", "14446");

        System.out.println();
        System.out.println("[2/2] Cau hinh MySQL/MariaDB (khong phai tai khoan phpMyAdmin)");
        ask(input, changes, current, "database.driver", "JDBC driver", "com.mysql.cj.jdbc.Driver", false);
        ask(input, changes, current, "database.host", "Database host", "localhost", false);
        askPort(input, changes, current, "database.port", "Database port", "3306");
        ask(input, changes, current, "database.name", "Ten database", "nro", false);
        ask(input, changes, current, "database.user", "Database user", "root", false);
        ask(input, changes, current, "database.pass", "Database password", "", true);

        System.out.print("Kiem tra ket noi database... ");
        saveConfiguration(changes);
        System.out.println("THANH CONG");

        System.out.println("Setup da hoan tat.");
        System.out.println("Cau hinh server: " + SERVER_CONFIG.toAbsolutePath());
        System.out.println("Trang thai setup: " + SETUP_STATE.toAbsolutePath());
    }

    public void verifySavedConfiguration() throws Exception {
        Properties properties = getSavedConfiguration();
        validateConfiguration(properties);
        testDatabase(properties);
    }

    public Properties getSavedConfiguration() {
        return loadProperties(SERVER_CONFIG);
    }

    public void testConfiguration(Map<String, String> changes) throws Exception {
        Properties candidate = mergedConfiguration(changes);
        validateConfiguration(candidate);
        testDatabase(candidate);
    }

    public void saveConfiguration(Map<String, String> changes) throws Exception {
        Properties candidate = mergedConfiguration(changes);
        validateConfiguration(candidate);
        testDatabase(candidate);

        Files.createDirectories(SERVER_CONFIG.getParent());
        backupExistingConfig();
        updatePropertiesFileAtomically(SERVER_CONFIG, changes);
        restrictToOwnerWhenSupported(SERVER_CONFIG);
        saveSetupState();
    }

    private Properties mergedConfiguration(Map<String, String> changes) {
        Properties candidate = getSavedConfiguration();
        candidate.putAll(changes);
        return candidate;
    }

    public void printStatus() {
        Properties config = loadProperties(SERVER_CONFIG);
        System.out.println();
        System.out.println("Setup: " + (isSetupComplete() ? "DA HOAN TAT" : "CHUA HOAN TAT"));
        System.out.println("Config: " + SERVER_CONFIG.toAbsolutePath());
        System.out.println("Server: " + config.getProperty("server.name", "<chua co>"));
        System.out.println("Dia chi: " + config.getProperty("server.ip", "<chua co>")
                + ":" + config.getProperty("server.port", "<chua co>"));
        System.out.println("Database: " + config.getProperty("database.user", "<chua co>")
                + "@" + config.getProperty("database.host", "<chua co>")
                + ":" + config.getProperty("database.port", "<chua co>")
                + "/" + config.getProperty("database.name", "<chua co>"));
        System.out.println("Database password: "
                + (config.getProperty("database.pass", "").isEmpty() ? "CHUA DAT" : "DA DAT (duoc an)"));
    }

    private static void ask(
            BufferedReader input,
            Map<String, String> changes,
            Properties current,
            String key,
            String label,
            String fallback,
            boolean password) throws IOException {
        String existing = current.getProperty(key, fallback);
        String shown = password ? (existing.isEmpty() ? "trong" : "da dat") : existing;
        String entered;

        if (password) {
            Console console = System.console();
            if (console != null) {
                char[] chars = console.readPassword("%s [%s]: ", label, shown);
                entered = chars == null ? "" : new String(chars);
                if (chars != null) {
                    java.util.Arrays.fill(chars, '\0');
                }
            } else {
                System.out.print(label + " [" + shown + "]: ");
                entered = input.readLine();
                System.out.println("Canh bao: console nay khong ho tro an ky tu mat khau.");
            }
        } else {
            System.out.print(label + " [" + shown + "]: ");
            entered = input.readLine();
        }

        if (entered == null) {
            throw new IOException("Console da dong trong khi setup");
        }
        if (!password) {
            entered = entered.trim();
        }
        changes.put(key, entered.isEmpty() ? existing : entered);
    }

    private static void askPort(
            BufferedReader input,
            Map<String, String> changes,
            Properties current,
            String key,
            String label,
            String fallback) throws IOException {
        while (true) {
            ask(input, changes, current, key, label, fallback, false);
            int port = parseInt(changes.get(key), -1);
            if (port >= 1 && port <= 65535) {
                return;
            }
            System.out.println("Cong phai nam trong khoang 1-65535.");
            current.setProperty(key, fallback);
        }
    }

    private static void askBoolean(
            BufferedReader input,
            Map<String, String> changes,
            Properties current,
            String key,
            String label,
            String fallback) throws IOException {
        while (true) {
            ask(input, changes, current, key, label + " (true/false)", fallback, false);
            String value = changes.get(key);
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                changes.put(key, value.toLowerCase());
                return;
            }
            System.out.println("Chi nhap true hoac false.");
            current.setProperty(key, fallback);
        }
    }

    private static void validateConfiguration(Properties properties) {
        if (!hasRequiredConfiguration(properties)) {
            throw new IllegalArgumentException("Cau hinh con thieu truong bat buoc");
        }
        validatePort(properties, "server.port");
        validatePort(properties, "server.backendport");
        validatePort(properties, "database.port");
    }

    private static boolean hasRequiredConfiguration(Properties properties) {
        String[] required = {
            "server.name", "server.ip", "server.port",
            "database.driver", "database.host", "database.port",
            "database.name", "database.user"
        };
        for (String key : required) {
            if (properties.getProperty(key, "").trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void validatePort(Properties properties, String key) {
        int port = parseInt(properties.getProperty(key), -1);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(key + " khong hop le: " + properties.getProperty(key));
        }
    }

    private static void testDatabase(Properties properties) throws Exception {
        String driver = properties.getProperty("database.driver");
        Class.forName(driver);
        String url = "jdbc:mysql://" + properties.getProperty("database.host")
                + ":" + properties.getProperty("database.port")
                + "/" + properties.getProperty("database.name")
                + "?useUnicode=true&characterEncoding=UTF-8&connectTimeout=10000&socketTimeout=10000";

        DriverManager.setLoginTimeout(10);
        try (Connection connection = DriverManager.getConnection(
                url,
                properties.getProperty("database.user"),
                properties.getProperty("database.pass", ""))) {
            if (!connection.isValid(5)) {
                throw new SQLException("Database khong tra loi lenh kiem tra");
            }
        }
    }

    private static Properties loadProperties(Path path) {
        Properties properties = new Properties();
        if (!Files.isRegularFile(path)) {
            return properties;
        }
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Khong doc duoc " + path + ": " + e.getMessage(), e);
        }
        return properties;
    }

    private static void updatePropertiesFileAtomically(Path path, Map<String, String> changes) throws IOException {
        List<String> lines = Files.isRegularFile(path)
                ? Files.readAllLines(path, StandardCharsets.UTF_8)
                : new java.util.ArrayList<>();
        List<String> output = new java.util.ArrayList<>(lines.size() + changes.size());
        Map<String, String> remaining = new LinkedHashMap<>(changes);

        for (String line : lines) {
            String trimmed = line.trim();
            int equals = trimmed.indexOf('=');
            if (!trimmed.startsWith("#") && !trimmed.startsWith("!") && equals > 0) {
                String key = trimmed.substring(0, equals).trim();
                if (remaining.containsKey(key)) {
                    output.add(key + "=" + escapePropertyValue(remaining.remove(key)));
                    continue;
                }
            }
            output.add(line);
        }

        if (!remaining.isEmpty()) {
            output.add("");
            output.add("# Added by NRO setup launcher");
            remaining.forEach((key, value) -> output.add(key + "=" + escapePropertyValue(value)));
        }

        Path temp = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
        try {
            Files.write(temp, output, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            moveAtomically(temp, path);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static String escapePropertyValue(String value) {
        String escaped = value.replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return escaped.startsWith(" ") ? "\\" + escaped : escaped;
    }

    private static void backupExistingConfig() throws IOException {
        if (!Files.isRegularFile(SERVER_CONFIG)) {
            return;
        }
        Path backup = SERVER_CONFIG.resolveSibling(SERVER_CONFIG.getFileName() + ".before-setup.bak");
        Files.copy(SERVER_CONFIG, backup, StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES);
        restrictToOwnerWhenSupported(backup);
    }

    private static void saveSetupState() throws IOException {
        Properties state = new Properties();
        state.setProperty("setup.completed", "true");
        state.setProperty("setup.version", String.valueOf(SETUP_VERSION));
        state.setProperty("setup.completedAt", Instant.now().toString());

        Files.createDirectories(SETUP_STATE.getParent());
        Path temp = Files.createTempFile(SETUP_STATE.getParent(), "setup", ".tmp");
        try (var writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            state.store(writer, "NRO setup state - do not edit while server is running");
        }
        try {
            moveAtomically(temp, SETUP_STATE);
            restrictToOwnerWhenSupported(SETUP_STATE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void restrictToOwnerWhenSupported(Path path) {
        try {
            Files.setPosixFilePermissions(path,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows/NTFS khong ho tro POSIX permissions.
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
