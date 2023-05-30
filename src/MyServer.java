import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyServer {
    private final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return this.authService;
    }

    public MyServer() {
        try {
            ServerSocket server = new ServerSocket(PORT);

            try {
                this.authService = new BaseAuthService();
                this.authService.start();
                this.clients = new ArrayList();

                while(true) {
                    System.out.println("Сервер ожидает подключения");
                    Socket socket = server.accept();
                    System.out.println("Клиент подключился");
                    new ClientHandler(this, socket);
                }
            } catch (Throwable var10) {
                try {
                    server.close();
                } catch (Throwable var9) {
                    var10.addSuppressed(var9);
                }

                throw var10;
            }
        } catch (IOException var11) {
            System.out.println("Ошибка в работе сервера");
        } finally {
            if (this.authService != null) {
                this.authService.stop();
            }

        }

    }

    public synchronized boolean isNickBusy(String nick) {
        Iterator var2 = this.clients.iterator();

        ClientHandler o;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            o = (ClientHandler)var2.next();
        } while(!o.getName().equals(nick));

        return true;
    }

    public synchronized void broadcastMsg(String msg) {
        Iterator var2 = this.clients.iterator();

        while(var2.hasNext()) {
            ClientHandler o = (ClientHandler)var2.next();
            o.sendMsg(msg);
        }

    }

    public synchronized void unsubscribe(ClientHandler o) {
        this.clients.remove(o);
    }

    public synchronized void subscribe(ClientHandler o) {
        this.clients.add(o);
    }

    public synchronized void broadcastMsgToNick(String nameFrom, String nameTo, String msg) {
        Iterator var4 = this.clients.iterator();

        while(var4.hasNext()) {
            ClientHandler o = (ClientHandler)var4.next();
            if (o.getName().equals(nameTo)) {
                o.sendMsg("ЛС от: " + nameFrom + ": " + msg);
            }

            if (o.getName().equals(nameFrom)) {
                o.sendMsg("ЛС от: " + nameFrom + ": " + msg);
            }
        }

    }
    public synchronized boolean kickUser(String nick) {
        Iterator var2 = this.clients.iterator();

        ClientHandler o;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            o = (ClientHandler)var2.next();
        } while(!o.getName().equals(nick));

        o.closeKick();
        return true;
    }
    public static void main(String[] args) {
        new MyServer();
    }
}