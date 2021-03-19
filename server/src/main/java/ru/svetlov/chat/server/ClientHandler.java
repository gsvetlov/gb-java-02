package ru.svetlov.chat.server;

import ru.svetlov.chat.server.user.UserInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private UserInfo user;
    private boolean hasLogin;

    public UserInfo getUser() {
        return user;
    }

    public String getNick() {
        return user.getNickname();
    }

    public ClientHandler(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread clientThread = new Thread(() -> {
                try {
                    authenticate();
                    if (hasLogin) communicate();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                } finally {
                    disconnect();
                }

            });
            clientThread.setDaemon(true);
            clientThread.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void disconnect() {
        if (socket == null) return;
        try {
            if (socket.isConnected()) {
                server.logout(this);
                socket.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void authenticate() throws IOException {
        while (!hasLogin) {
            String[] msg = in.readUTF().split(" ", 2);
            if (msg.length < 2) continue;
            if (msg[0].equals(Commands.LOGIN)) {
                LoginResponse response = server.login(msg[1], this);
                if (response.getUser() == null)
                    continue;
                sendMessage(response.getMessage());
                user = response.getUser();
                hasLogin = true;
                break;
            }
        }

    }

    private void communicate() throws IOException {
        while (socket.isConnected()) {
            String msg = in.readUTF();
            if (msg.startsWith("/"))
                server.process(msg, this);
            else
                server.publish(msg, this);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            disconnect();
        }
    }
}
