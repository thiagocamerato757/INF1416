package db.dao;

import db.DataBaseStarter;
import model.RegisterModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegisterDAO {

    public static List<RegisterModel> findAllChronological() {
        List<RegisterModel> registers = new ArrayList<>();
        String sql = "SELECT RID, data_hora, MID, UID, detalhe FROM Registros ORDER BY data_hora ASC, RID ASC";

        try (Connection conn = DataBaseStarter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                RegisterModel reg = new RegisterModel();
                if (rs.getTimestamp("data_hora") != null) {
                    reg.setDate_hour(rs.getTimestamp("data_hora").toLocalDateTime());
                }
                reg.setMID(rs.getInt("MID"));
                int uid = rs.getInt("UID");
                reg.setUID(rs.wasNull() ? 0 : uid);
                reg.setDetail(rs.getString("detalhe"));
                registers.add(reg);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to load audit registers", e);
        }
        return registers;
    }
}
