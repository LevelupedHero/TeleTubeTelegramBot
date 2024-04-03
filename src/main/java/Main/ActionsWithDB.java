package Main;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.sql.*;

public class ActionsWithDB {

    private final static String URL = "jdbc:mysql://127.0.0.1:3306/teletubetelegrambot";
    private final static String USER = "teletubetelegrambot";
    private final static String PASSWORD = "5FTj7SL.uvZ/0vWb";

    private static Connection connection;

    public static void OpenConnectionWithDB() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int SelectOrInsertChatIntoDB(User usr) {
        int output = -1;
        try {
            Statement statement = connection.createStatement();

            // Выясняю, имеется ли уже запись о пользователе
            String findUserQuery = "SELECT ID FROM Users WHERE Chat_id=" + usr.getId() + " LIMIT 1";
            ResultSet findUserResult = statement.executeQuery(findUserQuery);

            // Если в findUserResult нет следующей строки с ID, то добавить этого пользователя
            if (findUserResult.next()) {
                output = findUserResult.getInt(1);
            } else {
                String updateAndSelectQuery = "INSERT INTO Users(chat_id, First_name, Last_name, Username)\n"
                        + "VALUES ("
                        + usr.getId() + ","
                        + "'" + usr.getFirstName() + "',"
                        + "'" + usr.getLastName() + "',"
                        + "'" + usr.getUserName()
                        + "');\n"
                        + "SELECT ID FROM Users WHERE Chat_id=" + usr.getId() + " LIMIT 1;";
                ResultSet findUserAgainResult = statement.executeQuery(updateAndSelectQuery);

                // Преобразую ResultSet в int
                findUserAgainResult.next();
                output = findUserAgainResult.getInt(1);
            }


            // Выясняю ID только что добавленного пользователя
//            String selectIdQuery = "SELECT ID FROM Users WHERE chat_id=" + usr.getId() + "LIMIT 1";
//            ResultSet results = statement.executeQuery(selectIdQuery);
//
//            // Преобразую ResultSet в int
//            results.next();
//            output = results.getInt("ID");

            // Закрываю потоки
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return output;
        }
    }

    public static void InsertNewVideoInfoIntoDB(Message message, String filePath) {
        try {
            // Устанавливаю соединение
            Statement statement = connection.createStatement();

            int userID = SelectOrInsertChatIntoDB(message.getFrom());

            // Формулирую, оправляю запрос и получаю ответ
            String insertVideoInfoQuery = "INSERT INTO Videos(Name, Link, Width, Height, Duration, User_id) VALUES ("
                    + "'" + message.getCaption() + "',"                  // Название
                    + "'" + filePath.replace('\\', '/') + "',"        // Ссылка в файловой системе
                    + "'" + message.getVideo().getWidth() + "',"        // Ширина
                    + "'" + message.getVideo().getHeight() + "',"        // Длина
                    + "'" + message.getVideo().getDuration() + "',"      // Продолжительность
                    + userID + ")";        // ID пользователя в БД

            statement.executeUpdate(insertVideoInfoQuery);

            // Закрываю потоки
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResultSet SelectVideosFromDB(){
        ResultSet results = null;
        try {
            Statement statemant = connection.createStatement();

            String query = "SELECT Videos.Name AS Name, " +
                    "Videos.Link AS Link, " +
                    "Videos.Width AS Width, " +
                    "Videos.Height AS Height, " +
                    "Videos.Duration AS Duration, " +
                    "Users.First_Name AS FirstName, " +
                    "Users.Last_Name AS LastName, " +
                    "Users.Username AS Username " +
                    "FROM Videos LEFT JOIN Users ON Videos.User_ID=Users.ID";
            results = statemant.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            return results;
        }
    }
}
