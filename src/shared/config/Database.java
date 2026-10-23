package shared.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Loads MySQL settings from environment variables, then config/db.properties. */
public final class Database {
    // Fallback settings read once from config/db.properties.
    private static final Properties PROPERTIES = load();

    // Prevent instantiation of this connection utility.
    private Database() {
    }

    /** Loads optional local database properties; missing files leave defaults available. */
    private static Properties load() {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream("config/db.properties")) {
            p.load(in);
        } catch (IOException ignored) {
        }
        return p;
    }

    /** Chooses an environment variable before a property value or fallback. */
    private static String value(String env, String key, String fallback) {
        String v = System.getenv(env);
        if (v == null || v.isBlank())
            v = PROPERTIES.getProperty(key, fallback);
        return v;
    }

    /** Opens a MySQL connection using the resolved URL and credentials. */
    public static Connection getConnection() throws SQLException {
        String url = value("RETAIL_DB_URL", "db.url",
                "jdbc:mysql://localhost:3306/retail_db?useSSL=false&serverTimezone=UTC");
        String user = value("RETAIL_DB_USER", "db.user", "root");
        String password = value("RETAIL_DB_PASSWORD", "db.password", "5821");
        return DriverManager.getConnection(url, user, password);
    }
}
