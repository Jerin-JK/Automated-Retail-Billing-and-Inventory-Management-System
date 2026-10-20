package shared.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Loads MySQL settings from environment variables, then config/db.properties. */
public final class Database {
    private static final Properties PROPERTIES = load();
    private Database() { }

    private static Properties load() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("config/db.properties")) { p.load(in); }
        catch (IOException ignored) { }
        return p;
    }

    private static String value(String env, String key, String fallback) {
        String v = System.getenv(env);
        if (v == null || v.isBlank()) v = PROPERTIES.getProperty(key, fallback);
        return v;
    }

    public static Connection getConnection() throws SQLException {
        String url = value("RETAIL_DB_URL", "db.url", "jdbc:mysql://localhost:3306/retail_db?useSSL=false&serverTimezone=UTC");
        String user = value("RETAIL_DB_USER", "db.user", "root");
        String password = value("RETAIL_DB_PASSWORD", "db.password", "");
        return DriverManager.getConnection(url, user, password);
    }
}
