package logger;

import db.DataBaseStarter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Logger class for audit trail management in the Digital Vault system.
 * Provides comprehensive logging capabilities for recording all system events and user actions
 * to the Registros database table. Supports automatic timestamp generation, optional user context,
 * and detailed event information. All methods are static and require no instantiation.
 * This class integrates with INF1416 security requirements for comprehensive audit trails.
 *
 * @author Digital Vault Team
 * @version 1.0
 */
public class Logger {

    /**
     * Logs a complete event with all parameters to the audit trail.
     * Inserts a record into the Registros table with a message ID, optional user ID,
     * optional detail string, and automatic current timestamp. The method handles edge cases
     * including automatic truncation of details exceeding 255 characters, proper NULL handling,
     * and secure prepared statements to prevent SQL injection.
     *
     * @param MID the Message ID corresponding to an entry in the Mensagens table.
     *            Valid range: 1001-8004. Examples: 1001 (System started),
     *            1002 (System shutdown), 2001 (Authentication stage 1 initiated),
     *            3003 (Password verified successfully), 8001 (Exit screen displayed)
     *
     * @param UID the User ID that identifies the user associated with this event.
     *            Can be null for system-level events. Must match a valid entry in
     *            the Usuarios table if provided
     *
     * @param detail optional descriptive string providing additional context about the event.
     *               Maximum length is 255 characters; longer strings are automatically truncated.
     *               Can be null if no additional details are needed. Common details include
     *               filename, email address, or validation failure reason
     *
     * @return true if the log record was successfully inserted into the database,
     *         false if a database error occurred. Errors are logged to stderr
     */
    public static boolean log(int MID, Integer UID, String detail) {
        String sql_query = "INSERT INTO Registros (MID, UID, detalhe, data_hora) VALUES (?, ?, ?, ?)";

        try (Connection connection = DataBaseStarter.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql_query);
            statement.setInt(1, MID);
            statement.setObject(2, UID);

            if (detail != null && detail.length() > 255) {
                statement.setString(3, detail.substring(0, 255));
            } else {
                statement.setString(3, detail);
            }

            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error logging MID " + MID + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Logs an event with message ID and detail information, without user context.
     * Useful for system-level events that don't require user identification,
     * such as system startup or shutdown. This is a convenience overload that
     * delegates to log(int, Integer, String) with UID set to null.
     *
     * @param MID the Message ID corresponding to the Mensagens table
     *
     * @param detail optional detail string (max 255 characters). Common usage includes
     *               logging system status, error messages, or configuration changes
     *
     * @return true if the log was successfully inserted, false otherwise
     */
    public static boolean log(int MID, String detail) {
        return log(MID, null, detail);
    }

    /**
     * Logs an event with message ID and user context, without additional details.
     * Suitable for logging user-specific events where the action itself is
     * self-explanatory and no additional context is needed. This is a convenience
     * overload that delegates to log(int, Integer, String) with detail set to null.
     *
     * @param MID the Message ID corresponding to the Mensagens table
     *
     * @param UID the User ID that uniquely identifies the user performing the action.
     *            Must be a valid UID from the Usuarios table. Can be null if user
     *            context is not applicable
     *
     * @return true if the log was successfully inserted, false otherwise
     */
    public static boolean log(int MID, Integer UID) {
        return log(MID, UID, null);
    }

}
