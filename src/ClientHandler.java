import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    public String getName() {
        return this.name;
    }
    private void setName(String nick) throws SQLException {
        String query = "update users set nick=? where nick='" + this.name + "'";
        PreparedStatement pr = BaseAuthService.connection.prepareStatement(query);
        pr.setString(1, nick);
        try {
            pr.executeUpdate();
            this.name = nick;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            (new Thread(() -> {
                try {
                    this.authentication();
                    this.readMessages();
                } catch (IOException var7) {
                    var7.printStackTrace();
                } catch (SQLException var8) {
                    throw new RuntimeException(var8);
                } catch (ClassNotFoundException var9) {
                    throw new RuntimeException(var9);
                } finally {
                    this.closeConnection();
                }

            })).start();
        } catch (IOException var4) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException, SQLException, ClassNotFoundException {
        while(true) {
            String str = this.in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                String nick = this.myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!this.myServer.isNickBusy(nick)) {
                        this.sendMsg("/authok " + nick);
                        this.name = nick;
                        this.myServer.broadcastMsg(nick + " подключился в чат");
                        this.sendMsg("Авторизация пройдена Hello " + nick + " !");
                        this.myServer.subscribe(this);
                        return;
                    }

                    this.sendMsg("Учетная запись уже используется");
                } else {
                    this.sendMsg("Неверные логин/пароль");
                }
            }
        }
    }

    private void readMessages() throws IOException, SQLException {
        while(true) {
            String strFromClient = this.in.readUTF();
            System.out.println("от " + this.name + ": " + strFromClient);
            String[] arr;
            if (strFromClient.startsWith("/w")) {
                arr = strFromClient.split("\\s");
                if (this.myServer.isNickBusy(arr[1])) {
                    this.myServer.broadcastMsgToNick(this.name, arr[1], arr[2]);
                }
                continue;
            } if (strFromClient.startsWith("/changename ")) {
                arr = strFromClient.split(" ");
                String newName = arr[1];
                myServer.broadcastMsg(name + " cменил ник на: " + newName);
                setName(arr[1]);
                continue;
            } if (this.name.equals("admin")) {
                String login;
                if (strFromClient.startsWith("/adduser ")) {
                    arr = strFromClient.split(" ");
                    login = arr[1];
                    String pass = arr[2];
                    this.myServer.getAuthService().createUser(login, pass, login);
                    this.myServer.broadcastMsgToNick(this.name, "user " + login + " added", "User added");
                    continue;
                }

                if (strFromClient.startsWith("/deluser ")) {
                    arr = strFromClient.split(" ");
                    login = arr[1];
                    boolean isInDB = this.myServer.getAuthService().findUserByNick(login);
                    boolean isOnline = this.myServer.isNickBusy(login);
                    if (!isInDB) {
                        continue;
                    }

                    this.myServer.getAuthService().deleteUserByNick(login);
                    this.myServer.broadcastMsgToNick(this.name, "user " + login + " deleted", "User deleted");
                    if (isOnline) {
                        this.myServer.kickUser(login);
                    }
                    continue;
                }
            }

            if (strFromClient.equals("/end")) {
                this.closeConnection();
            }

            this.myServer.broadcastMsg(this.name + ": " + strFromClient);
        }
    }

    public void sendMsg(String msg) {
        try {
            this.out.writeUTF(msg);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

    }

    public void closeKick() {
        this.myServer.broadcastMsg(this.name + " удален из чата");
        this.myServer.unsubscribe(this);

        try {
            this.in.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        try {
            this.out.close();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        try {
            this.socket.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void closeConnection() {
        this.myServer.unsubscribe(this);
        this.myServer.broadcastMsg(this.name + " вышел из чата");

        try {
            this.in.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        try {
            this.in.close();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        try {
            this.in.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }
}
