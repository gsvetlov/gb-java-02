package ru.svetlov.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    public String getNick() {
        return nick;
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
                    if (nick != null) communicate();
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
        while (true) {
            String[] msg = in.readUTF().split(" ", 2);
            if (msg.length < 2) continue;
            if ("/login".equals(msg[0])) {
                msg = server.login(msg[1], this);
                sendMessage(msg[1]);
                if (msg[0] != null) {
                    nick = msg[0];
                    break;
                }
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
