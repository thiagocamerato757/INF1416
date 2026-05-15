package db.dao;
import db.DataBaseStarter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class MessageDAO {

    /**
        * Retrieves the message text associated with a given MID from the messages table.
        * @param MID the message ID to search for
        * @return the message text if found,
     */
    public static String find_message_by_MID(int MID) {
        String sql_querry = "SELECT * FROM Mensagens WHERE MID = ?";
        try(Connection connection = DataBaseStarter.getConnection()){
            PreparedStatement statement = connection.prepareStatement(sql_querry);
            statement.setInt(1, MID);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("texto");
            }

        } catch (SQLException e) {
            return null;
        }
        return null;
    }
}
