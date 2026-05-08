package db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for database connection management.
 * Loads configuration from application.properties and provides JDBC connections.
*/
public class DataBaseStarter {

    private static final Logger LOGGER = Logger.getLogger(DataBaseStarter.class.getName());

    private static final String url;
    private static final String user;
    private static final String password;

    // Static initializer: loads database settings from properties file
    static {
        try (InputStream input = DataBaseStarter.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            Properties props = new Properties();
            props.load(input);

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            // Load JDBC driver class (ensures compatibility)
            Class.forName(props.getProperty("db.driver"));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load database configuration", e);
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    /**
     * Obtains a new database connection using the configured credentials.
     *
     * @return a {@link Connection} to the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests the database connection by attempting to open and close a connection.
     * Logs success or failure using java.util.logging.
     *
     * @return boolean indicating whether the connection test was successful
     */
    public static boolean testConnection() {
        try (Connection ignored = getConnection()) {
            LOGGER.info("Successfully connected to the database");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Connection failed", e);
            return false;
        }
        return true;
    }
}