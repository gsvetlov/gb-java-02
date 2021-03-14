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
                authenticate();
                if (nick != null) communicate();
                disconnect();
            });
            clientThread.setDaemon(true);
            clientThread.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            disconnect();
        }
    }

    private void disconnect() {
        if (socket == null) return;
        try {
            socket.close();
            System.out.println(nick + " disconnect");
            server.logout(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        try {
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
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void communicate() {
        try {
            while (socket.isConnected()) {
                String msg = in.readUTF();
                if (msg.startsWith("/"))
                    server.process(msg, this);
                else
                    server.publish(msg, this);
            }
        } catch (IOException ex) {
            System.out.println("communication error:");
            System.out.println(ex.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            if (!socket.isClosed())
                out.writeUTF(message);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            disconnect();
        }
    }
}
