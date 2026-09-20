package data;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;
import utils.Logger;

public class AlyraManager {

    private static String DRIVER;
    private static String URL;
    private static String DB_HOST;
    private static String DB_PORT;
    private static String DB_NAME;
    private static String DB_NAME_DATA;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static int MIN_CONN;
    private static int MAX_CONN;
    private static long MAX_LIFE_TIME;
    public static boolean LOG_QUERY;
    private static HikariConfig config;
    private static HikariConfig config_data;
    private static volatile HikariDataSource ds;
    private static volatile HikariDataSource ds_data;
    private static final Connection[] connections = new Connection[10];
    private static AlyraManager i;
    private static final Object initLock = new Object();
    
    public static AlyraManager gI() {
        if (i == null) {
            synchronized (AlyraManager.class) {
                if (i == null) {
                    i = new AlyraManager();
                }
            }
        }
        return i;
    }

    static {
        try {
            // Tắt log DEBUG của HikariCP bằng cách set system property
            System.setProperty("org.slf4j.simpleLogger.log.com.zaxxer.hikari", "error");
            System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        } catch (Exception ignored) {}
    }
    

    private static void ensureInitialized() {
        if (ds != null) {
            return; // Đã được initialize
        }
        
        synchronized (initLock) {
            if (ds != null) {
                return; // Double check
            }
            loadProperties();
            config = createConfig("User Management", DB_NAME);
            config_data = createConfig("Game Assets", DB_NAME_DATA);
            
            ds = new HikariDataSource(config);
            ds_data = new HikariDataSource(config_data);
            Logger.log("AlyraManager initialized from properties file\n");
        }
    }
    

    public void initialize() {
        if (ds == null) {
            loadProperties();
            config = createConfig("User Management", DB_NAME);
            config_data = createConfig("Game Assets", DB_NAME_DATA);
            
            ds = new HikariDataSource(config);
            ds_data = new HikariDataSource(config_data);
            Logger.log("AlyraManager initialized successfully\n");
        }
    }
    

    public void shutdown() {
        Logger.log("AlyraManager shutdown requested\n");
        close();
        close_data();
    }

    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        return AlyraManager.ds.getConnection();
    }

    public static Connection getConnection_Data() throws SQLException {
        ensureInitialized();
        return AlyraManager.ds_data.getConnection();
    }
    

    public static PreparedStatement prepareStatement(Connection con, String sql) throws SQLException {
        return con.prepareStatement(sql, 
                java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, 
                java.sql.ResultSet.CONCUR_READ_ONLY);
    }

    public static void close() {
        if (ds != null && !ds.isClosed()) {
            try {
                ds.close();
            } catch (Exception e) {
                Logger.warning("Error closing DataSource: " + e.getMessage() + "\n");
            }
        }
    }

    public static void close_data() {
        if (ds_data != null && !ds_data.isClosed()) {
            try {
                ds_data.close();
            } catch (Exception e) {
                Logger.warning("Error closing DataSource: " + e.getMessage() + "\n");
            }
        }
    }

    private static void loadProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("data/config/sever.properties"));
            Object value;
            if ((value = properties.get("database.driver")) != null) {
                AlyraManager.DRIVER = String.valueOf(value);
            }
            if ((value = properties.get("database.host")) != null) {
                AlyraManager.DB_HOST = String.valueOf(value);
            }
            if ((value = properties.get("database.port")) != null) {
                AlyraManager.DB_PORT = String.valueOf(value);
            }
            if ((value = properties.get("database.name")) != null) {
                AlyraManager.DB_NAME = String.valueOf(value);
                AlyraManager.DB_NAME_DATA = String.valueOf(value);
            }
            if ((value = properties.get("database.user")) != null) {
                AlyraManager.DB_USER = String.valueOf(value);
            }
            if ((value = properties.get("database.pass")) != null) {
                AlyraManager.DB_PASSWORD = String.valueOf(value);
            }
            if ((value = properties.get("database.min")) != null) {
                AlyraManager.MIN_CONN = Integer.parseInt(String.valueOf(value));
            }
            if ((value = properties.get("database.max")) != null) {
                AlyraManager.MAX_CONN = Integer.parseInt(String.valueOf(value));
            }
            if ((value = properties.get("database.lifetime")) != null) {
                AlyraManager.MAX_LIFE_TIME = Integer.parseInt(String.valueOf(value));
            }
            if ((value = properties.get("database.log")) != null) {
                AlyraManager.LOG_QUERY = Boolean.parseBoolean(String.valueOf(value));
            }
            if (AlyraManager.MIN_CONN <= 0) {
                AlyraManager.MIN_CONN = 1;
            }
            if (AlyraManager.MAX_CONN < 8) {
                AlyraManager.MAX_CONN = 8;
            }
            if (AlyraManager.MAX_LIFE_TIME < 1_800_000L) {
                AlyraManager.MAX_LIFE_TIME = 1_800_000L;
            }
            Logger.rainbow( "Successfully loaded file properites!");
        } catch (final IOException | NumberFormatException ex) {
            Logger.log("Không thể load file properites!\n");
        } finally {
            properties.clear();
        }
    }

    public static AlyraResultSet executeQuery(final String query) throws Exception {
        try {
            Connection con = getConnection();
            try (PreparedStatement ps = con.prepareStatement(query, 
                    java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, 
                    java.sql.ResultSet.CONCUR_READ_ONLY)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (AlyraManager.LOG_QUERY) {
                        Logger.log("Thực thi thành công câu lệnh: " + ps.toString() + "\n");
                    }
                    return new ResultSetImpl(rs);
                }
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        } catch (Exception ex) {
            Logger.log("Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }

    public static AlyraResultSet executeQuery(final String query, final Object... objs) throws Exception {
        try (final Connection con = getConnection(); 
             final PreparedStatement ps = con.prepareStatement(query, 
                     java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, 
                     java.sql.ResultSet.CONCUR_READ_ONLY)) {
            for (int i = 0; i < objs.length; ++i) {
                ps.setObject(i + 1, objs[i]);
            }
            if (AlyraManager.LOG_QUERY) {
                Logger.log("Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            return new ResultSetImpl(ps.executeQuery());
        } catch (final Exception ex) {
            Logger.log("Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }

    public static int executeUpdate(final String query) throws Exception {
        int rowUpdated = -1;
        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            if (AlyraManager.LOG_QUERY) {
                Logger.log("Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            rowUpdated = ps.executeUpdate();
        } catch (final Exception e) {
            Logger.log("Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw e;
        }
        return rowUpdated;
    }

    public static int executeUpdate(String query, final Object... objs) throws Exception {
        if (query.indexOf("insert") == 0 && query.lastIndexOf("()") == query.length() - 2) {
            final StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < objs.length; ++i) {
                sb.append("?");
                if (i < objs.length - 1) {
                    sb.append(",");
                } else {
                    sb.append(")");
                }
            }
            query = query.replace("()", sb.toString());
        }
        try (final Connection con = getConnection(); final PreparedStatement ps = con.prepareStatement(query)) {
            for (int j = 0; j < objs.length; ++j) {
                ps.setObject(j + 1, objs[j]);
            }
            if (AlyraManager.LOG_QUERY) {
                Logger.log("Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            return ps.executeUpdate();
        } catch (final Exception ex) {
            Logger.log("Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }

    /**
     * Execute INSERT và trả về ID được tạo
     */
    public static int executeInsert(String query, final Object... objs) throws Exception {
        if (query.indexOf("insert") == 0 && query.lastIndexOf("()") == query.length() - 2) {
            final StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < objs.length; ++i) {
                sb.append("?");
                if (i < objs.length - 1) {
                    sb.append(",");
                } else {
                    sb.append(")");
                }
            }
            query = query.replace("()", sb.toString());
        }
        try (final Connection con = getConnection(); 
             final PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            for (int j = 0; j < objs.length; ++j) {
                ps.setObject(j + 1, objs[j]);
            }
            if (AlyraManager.LOG_QUERY) {
                Logger.log("Thực thi thành công câu lệnh: " + ps.toString() + "\n");
            }
            ps.executeUpdate();
            
            // Lấy ID được generate
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        } catch (final Exception ex) {
            Logger.log("Có lỗi xảy ra khi thực thi câu lệnh: " + query + "\n");
            throw ex;
        }
    }
    
    public static Connection getConnectionServer() throws SQLException {
        ensureInitialized();
        return ds.getConnection();
    }
    
    public synchronized Connection getConnectionForSaveData() throws SQLException {
        if (connections[2] != null) {
            if (!connections[2].isValid(10)) {
                connections[2].close();
            }
        }
        if (connections[2] == null || connections[2].isClosed()) {
            try {
                connections[2] = getConnectionServer();
                return getConnectionForSaveData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return connections[2];
    }

    private static HikariConfig createConfig(String poolName, String databaseName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER);
        
        // OPTIMIZED: Added rewriteBatchedStatements for 20x faster batch inserts
        // Added cachePrepStmts for prepared statement caching
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8&tcpKeepAlive=true&rewriteBatchedStatements=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048&useServerPrepStmts=true&defaultResultSetType=SCROLL_INSENSITIVE",
                DB_HOST, DB_PORT, databaseName));
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        
        config.addDataSourceProperty("defaultResultSetType", "SCROLL_INSENSITIVE");
        
        config.setMinimumIdle(Math.max(10, MIN_CONN)); 
        config.setMaximumPoolSize(Math.max(30, MAX_CONN));
        config.setMaxLifetime(Math.max(1_800_000L, MAX_LIFE_TIME));  
        config.setConnectionTimeout(30_000L);
        config.setIdleTimeout(600_000L);
        config.setKeepaliveTime(300_000L);            // Keepalive 5 phút, < idleTimeout và < maxLifetime
        config.setConnectionTestQuery("SELECT 1");   // Validation query cho MySQL 5.x
        config.setLeakDetectionThreshold(20_000L);  // Detect connection leaks
        config.setPoolName(poolName);
        
        // Performance optimization properties
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");  // CRITICAL for batch performance
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");  // Reduced overhead
        
        return config;
    }
}
