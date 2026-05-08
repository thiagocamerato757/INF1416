package db.dao;
import db.DataBaseStarter;
import java.sql.*;
import model.UserModel;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
*  Data Access Object for the Usuarios table.
 * Handles all database operations related to users.
*/
public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    /**
     * Creates User in SQL table Usuarios
     * @param user - an instance of User
     * @return the User ID number
     */
    public static int createUser(UserModel user) {
        String sql = "INSERT INTO Usuarios (login, nome, senha_bcrypt, totp_secret_encrypted, grupo_id, KID ) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseStarter.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getNome());
            stmt.setString(3, user.getSenhaBcrypt());
            stmt.setBytes (4, user.getTotpSecretEncrypted());
            stmt.setInt   (5, user.getGrupoId());

            // KID can be NULL before creating a Keypair
            if (user.getKid() != null) stmt.setInt(6, user.getKid());
            else stmt.setNull(6, Types.INTEGER);

            int rows = stmt.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error ocurred in new user creation: ", e.getMessage());
        }
        return -1;
    }

    /**
     * Gets a User based on login.
     * @param login login
     * @return User if it exists
     */
    public static UserModel getUserByLogin(String login) {
        String sql = "SELECT * FROM Usuarios WHERE login = ?";
        UserModel user = null;

        try (Connection conn = DataBaseStarter.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {

            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet != null && resultSet.next()) {
                user = new UserModel();
                user.setLogin(resultSet.getString("login"));
                user.setNome(resultSet.getString("nome"));
                user.setSenhaBcrypt(resultSet.getString("senha_bcrypt"));
                user.setTotpSecretEncrypted(resultSet.getBytes("totp_secret_encrypted"));
                user.setGrupoId(resultSet.getInt("grupo_id"));
            }
        }
        catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return user;
    }

    /**
    * Checks if there are any users in the Usuarios table.
    *
    * @return true if at least one user exists, false otherwise
   */
    public static boolean checkAnyUser(){
        String sql_query = "SELECT COUNT(*) FROM Usuarios";
        try (Connection connection = DataBaseStarter.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql_query)) {

            if(resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }

        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
