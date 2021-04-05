package ru.svetlov.chat.server;

import ru.svetlov.chat.server.user.UserInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

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

    public void setUser(UserInfo user){
        this.user = user;
    }

    public ClientHandler(Socket socket, Server server, ExecutorService threadPool) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            threadPool.execute(() -> {
                try {
                    while (Thread.interrupted()) {
                        authenticate();
                        if (hasLogin) communicate();
                    }
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                } finally {
                    disconnect();
                }
            });
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void disconnect() {
        hasLogin = false;
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
                if (response.getMessage().startsWith(ServerResponse.LOGIN_OK)){
                    user = response.getUser();
                    hasLogin = true;
                }
                sendMessage(response.getMessage());
            }
        }
    }

    private void communicate() throws IOException {
        while (!socket.isClosed()) {
            String msg = in.readUTF();
            if (msg.startsWith("/"))
                server.process(msg, this);
            else
                server.publish(msg, this);
            if (msg.equals("/logout")) break;
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
