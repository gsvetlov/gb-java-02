package ru.svetlov.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int port;
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients;

    public Server(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();
        Thread listener = new Thread(this::run);
        listener.setDaemon(true);
        listener.start();
    }


    public void run() {
        try {
            while (!Thread.interrupted()) {
                System.out.printf("Listening on [localhost:%d]\n", port);
                Socket socket = serverSocket.accept();
                System.out.println("connection established...");
                new ClientHandler(socket, this);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("performing clients logoff");
        while (clients.size() > 0) {
            System.out.println(clients.get(0).getNick() + " logout");
            logout(clients.get(0));
        }

    }

    public String[] login(String s, ClientHandler client) {
        String[] result;
        if (isUnique(s)) {
            clients.add(client);
            result = new String[]{s, "/login_ok Login successful"};
            System.out.println(s + " login");
            publish(s + " joined the chat. Welcome!", null);

        } else {
            result = new String[]{null, "/login_nok User already connected"};
        }

        return result;
    }

    public void logout(ClientHandler client) {
        clients.remove(client);
        publish(client.getNick() + " is leaving the chat. See ya later!", null);
        client.sendMessage("/logout");
    }

    public void process(String msg, ClientHandler client) {
        String[] commands = msg.split(" ", 2);
        switch (commands[0]) {
            case "/who_am_i": {
                client.sendMessage("You are " + client.getNick());
                break;
            }
            case "/logout":
            case "/exit": {
                logout(client);
                break;
            }
            case "/w": {
                String[] split = commands[1].split(" ", 2);
                ClientHandler destination = getClientByNick(split[0]);
                if (destination != null) {
                    String out = getMessageString(split[1], client.getNick());
                    destination.sendMessage(out);
                    client.sendMessage(out);
                }
                else
                    client.sendMessage("Unknown user " + split[0]);
                break;
            }
            default: {
                client.sendMessage("unknown command");
            }
        }

    }

    private ClientHandler getClientByNick(String s) {
        for(ClientHandler client : clients){
            if (s.equals(client.getNick()))
                return client;
        }
        return  null;
    }

    public void publish(String msg, ClientHandler client) {
        String userName = client == null ? "Server" : client.getNick();
        String outMsg = getMessageString(msg, userName);
        for (ClientHandler c : clients) {
            c.sendMessage(outMsg);
        }
    }

    private String getMessageString(String msg, String user) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
        return sdf.format(System.currentTimeMillis()) +
                " @" +
                user +
                " " +
                msg;
    }

    private boolean isUnique(String s) {
        for (ClientHandler client : clients) {
            if (s.equals(client.getNick()))
                return false;
        }
        return true;
    }


}
