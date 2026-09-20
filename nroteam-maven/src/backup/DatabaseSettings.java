package backup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public record DatabaseSettings(String host, String port, String database, String user, String password) {

    public static DatabaseSettings load() throws IOException {
        Properties p = new Properties();
        try (var reader = Files.newBufferedReader(
                Path.of("data", "config", "sever.properties"), StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        return new DatabaseSettings(
                required(p, "database.host"), required(p, "database.port"),
                required(p, "database.name"), required(p, "database.user"),
                p.getProperty("database.pass", ""));
    }

    public Connection connect() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=UTF-8&connectTimeout=10000&socketTimeout=30000"
                + "&useCursorFetch=true&defaultFetchSize=100";
        return DriverManager.getConnection(url, user, password);
    }

    public Path createTemporaryClientConfig() throws IOException {
        Path secureDir = Path.of("data", "secure");
        Files.createDirectories(secureDir);
        SecureFiles.ownerDirectory(secureDir);
        Path file = Files.createTempFile(secureDir, "mysql-client-", ".cnf");
        String content = "[client]\n"
                + "host=" + option(host) + "\nport=" + option(port)
                + "\nuser=" + option(user) + "\npassword=" + option(password)
                + "\ndefault-character-set=utf8mb4\n";
        Files.writeString(file, content, StandardCharsets.UTF_8);
        SecureFiles.ownerOnly(file);
        return file;
    }

    private static String required(Properties p, String key) {
        String value = p.getProperty(key, "").trim();
        if (value.isEmpty()) throw new IllegalStateException("Thiếu cấu hình " + key);
        return value;
    }

    private static String option(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
