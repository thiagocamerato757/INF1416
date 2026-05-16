package logger;

import db.DataBaseStarter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Logger {

    public static boolean log(int MID, Integer UID, String fname) {
        String sql_query = "INSERT INTO Registros (MID, UID, data_hora, fname) VALUES (?, ?, ?, ?)";

        try (Connection connection = DataBaseStarter.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql_query);
            statement.setInt(1, MID);
            statement.setObject(2, UID);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(4, fname);

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error logging MID " + MID + ": " + e.getMessage());
            return false;
        }
    }

    public static boolean log(int MID, Integer UID) {
        return log(MID, UID, null);
    }

    public static boolean log(int MID) {
        return log(MID, (Integer) null, null);
    }

}
