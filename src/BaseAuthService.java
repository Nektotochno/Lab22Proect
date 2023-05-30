import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseAuthService implements AuthService {
    static final String connectionUrl = "jdbc:sqlite:mydb.db";
    public static Connection connection;
    private static Statement stmt;

    public BaseAuthService() {
    }

    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(connectionUrl);
            stmt = connection.createStatement();
        } catch (SQLException var2) {
            var2.printStackTrace();
        }

        System.out.println("Сервис аутентификации запущен");
    }

    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }
    @Override
    public synchronized boolean createUser(String login, String pass, String nick) {
        try {
            String query = "INSERT into users (login, pass, nick) VALUES (?,?,?)";
            PreparedStatement pr = connection.prepareStatement(query);
            pr.setString(1, login);
            pr.setString(2, pass);
            pr.setString(3, nick);
            pr.executeUpdate();
            return true;
        } catch (SQLException var6) {
            var6.printStackTrace();
            return false;
        }
    }
    public synchronized boolean deleteUserByNick(String login) {
        try {
            String query = "DELETE from users where nick=?";
            PreparedStatement pr = connection.prepareStatement(query);
            pr.setString(1, login);
            pr.executeUpdate();
            return true;
        } catch (SQLException var4) {
            var4.printStackTrace();
            return false;
        }
    }

    public synchronized boolean findUserByNick(String nick) {
        String nickname = "";

        try {
            String query = "select * from users where nick=?";
            PreparedStatement pr = connection.prepareStatement(query);
            pr.setString(1, nick);

            for(ResultSet rs = pr.executeQuery(); rs.next(); nickname = rs.getString("nick")) {
            }
        } catch (SQLException var6) {
            var6.printStackTrace();
            return false;
        }

        return !nickname.equals("");
    }

    public synchronized String getNickByLoginPass(String login, String pass) {
        String nickReturn = null;

        try {
            String query = "select * from users where login='" + login + "'and pass='" + pass + "'";
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);

            while(rs.next()) {
                nickReturn = rs.getString("nick");
                System.out.println(rs.getString("nick"));
            }
        } catch (SQLException var6) {
            var6.printStackTrace();
        }

        return nickReturn.equals(" ") ? null : nickReturn;
    }
}
