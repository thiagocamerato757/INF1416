package db.dao;

import db.DataBaseStarter;

import java.sql.*;

public class KeyDAO {

    public static int createKey(String certPem, byte[] encryptedPrivateKey) {
        String sql = "INSERT INTO Chaveiro (certificado_pem, chave_privada_encrypted) VALUES (?, ?)";
        try (Connection conn = DataBaseStarter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, certPem);
            stmt.setBytes(2, encryptedPrivateKey);
            int rows = stmt.executeUpdate();
            if (rows == 0) return -1;
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Object[] getCertAndKey(int kid) {
        String sql = "SELECT certificado_pem, chave_privada_encrypted FROM Chaveiro WHERE KID = ?";
        try (Connection conn = DataBaseStarter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, kid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{rs.getString("certificado_pem"), rs.getBytes("chave_privada_encrypted")};
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
