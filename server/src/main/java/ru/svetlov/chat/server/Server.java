package ru.svetlov.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int serverPort;
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients;

    public Server(int port) throws IOException {
        serverPort = port;
        serverSocket = new ServerSocket(serverPort);
        clients = new ArrayList<>();
        Thread listener = new Thread(()->{
            try {
                while (!Thread.interrupted()) {
                    System.out.printf("Listening on [localhost:%d]\n", serverPort);
                    Socket socket = serverSocket.accept();
                    System.out.println("connection established...");
                    new ClientHandler(socket, this);
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            } finally {
                shutdown();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public synchronized void shutdown() {
        try {
            serverSocket.close();
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (clients.size() > 0)
            logout(clients.get(0));
    }

    public String[] login(String s, ClientHandler client) {
        String[] result;
        if (isUnique(s)) {
            clients.add(client);
            result = new String[]{s, "/login_ok Login successful"};
            publish(s + " joined the chat. Welcome!", null);
            publishClientsList();

        } else {
            result = new String[]{null, "/login_nok User already connected"};
        }

        return result;
    }

    public synchronized void logout(ClientHandler client) {
        clients.remove(client);
        publish(client.getNick() + " is leaving the chat. See ya later!", null);
        publishClientsList();
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

    private synchronized ClientHandler getClientByNick(String s) {
        for(ClientHandler client : clients){
            if (s.equals(client.getNick()))
                return client;
        }
        return  null;
    }

    public synchronized void publishClientsList(){
        publish(getClientsList(), null);
    }

    private synchronized String getClientsList(){
        StringBuilder sb = new StringBuilder("/clients_list ");
        for (ClientHandler c : clients){
            sb.append(c.getNick()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public synchronized void publish(String msg, ClientHandler client) {
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

    private synchronized boolean isUnique(String s) {
        for (ClientHandler client : clients) {
            if (s.equals(client.getNick()))
                return false;
        }
        return true;
    }


}
