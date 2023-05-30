import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client2 extends JFrame {
    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8189;
    private JTextField msgInputField;
    private JTextField login;
    private JTextField password;
    private JTextArea chatArea;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private JButton authBtn;

    public Client2() {
        try {
            this.openConnection();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

        this.prepareGUI();
        this.createAuthPanel();
    }

    public void openConnection() throws IOException {
        this.socket = new Socket(SERVER_ADDR, SERVER_PORT);
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());
        (new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        String strFromServer = Client2.this.in.readUTF();
                        if (strFromServer.startsWith("/authok")) {
                            while (true) {
                                strFromServer = Client2.this.in.readUTF();
                                if (strFromServer.equalsIgnoreCase("/end")) {
                                    return;
                                }

                                Client2.this.chatArea.append(strFromServer);
                                Client2.this.chatArea.append("\n");
                            }
                        }

                        Client2.this.chatArea.append(strFromServer + "\n");
                    }
                } catch (EOFException var2) {
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        })).start();
    }

    public void closeConnection() {
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

    public void sendMessage() {
        if (!this.msgInputField.getText().trim().isEmpty()) {
            String message = this.msgInputField.getText();
            if (message.startsWith("/auth")) { // проверка на команду авторизации
                String login = this.login.getText().trim();
                String pass = String.valueOf(this.password.getText()).trim();

                try {
                    this.out.writeUTF("/auth " + login + " " + pass);
                    this.out.flush();
                } catch (IOException var4) {
                    var4.printStackTrace();
                    JOptionPane.showMessageDialog((Component) null, "Ошибка авторизации");
                }
            } else {
                try {
                    this.out.writeUTF(message);
                    this.msgInputField.setText("");
                    this.msgInputField.grabFocus();
                } catch (IOException var2) {
                    var2.printStackTrace();
                    JOptionPane.showMessageDialog((Component) null, "Ошибка отправки сообщения");
                }
            }
        }
    }

    private void createAuthPanel() {
        JPanel authPanel = new JPanel(new GridLayout());
        this.login = new JTextField();
        this.password = new JTextField();
        this.authBtn = new JButton("Авторизация");
        authPanel.add(login);
        authPanel.add(password);
        authPanel.add(authBtn);
        add(authPanel, BorderLayout.NORTH);
        authBtn.addActionListener(e -> connect(login.getText(), String.valueOf(password.getText())));
    }

    private void connect(String login, String pass) {
        try {
            this.out.writeUTF("/auth " + login + " " + pass);
            this.out.flush();
        } catch (IOException var4) {
            var4.printStackTrace();
            JOptionPane.showMessageDialog((Component) null, "Ошибка авторизации");
        }
    }

    public void prepareGUI() {
        this.setBounds(600, 300, 500, 500);
        this.setTitle("Клиент 2");
        this.setDefaultCloseOperation(3);

        this.chatArea = new JTextArea();
        this.chatArea.setEditable(false);
        this.chatArea.setLineWrap(true);
        this.add(new JScrollPane(this.chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        this.msgInputField = new JTextField();
        bottomPanel.add(this.msgInputField, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        btnSendMsg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Client2.this.sendMessage();
            }
        });
        this.msgInputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Client2.this.sendMessage();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    Client2.this.out.writeUTF("/end");
                    Client2.this.closeConnection();
                } catch (IOException var3) {
                    var3.printStackTrace();
                }

            }
        });

        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Client();
            }
        });
    }
}
