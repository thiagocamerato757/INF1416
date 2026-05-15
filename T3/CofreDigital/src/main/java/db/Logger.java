package db;

import db.DataBaseStarter;

import java.sql.*;
import java.time.LocalDateTime;

public class Logger {

    public static void log(int mid, Integer uid, String detail) {
        String sql = "INSERT INTO Registros (data_hora, MID, UID, detalhe) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataBaseStarter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, mid);
            if (uid != null) stmt.setInt(3, uid);
            else stmt.setNull(3, Types.INTEGER);
            if (detail != null) stmt.setString(4, detail);
            else stmt.setNull(4, Types.VARCHAR);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void log(int mid, Integer uid) {
        log(mid, uid, null);
    }

    public static void log(int mid) {
        log(mid, null, null);
    }
}
