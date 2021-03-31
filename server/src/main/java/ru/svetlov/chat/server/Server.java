package ru.svetlov.chat.server;

import ru.svetlov.chat.server.user.UserInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private final AuthenticationProvider provider;
    private final int serverPort;
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients;

    private Map<String, UserInfo> users;

    public Server(int port, AuthenticationProvider provider) throws IOException {
        this.provider = provider;
        serverPort = port;
        serverSocket = new ServerSocket(serverPort);
        clients = new ArrayList<>();
        Thread listener = new Thread(()->{
            try {
                while (!Thread.interrupted()) {
                    System.out.printf("Listening on [localhost:%d]%n", serverPort);
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

    public LoginResponse login(String loginMsg, ClientHandler client) {

        String[] tokens = loginMsg.split("\\s", 2);
        UserInfo user = provider.authenticate(tokens[0], tokens[1]);

        if (user == null) {
            return new LoginResponse(null, ServerResponse.LOGIN_FAIL_INCORRECT);
        }

        if (isOnline(user.getNickname())) {
            return new LoginResponse(null, ServerResponse.LOGIN_FAIL_CONNECTED);
        } else {
            client.setUser(user);
            clients.add(client);
            publish(user.getNickname() + " has joined the chat. Welcome!", null);
            publishClientsList();
            return new LoginResponse(user, ServerResponse.LOGIN_OK + user.getNickname());
        }
    }

    public synchronized void logout(ClientHandler client) {
        clients.remove(client);
        publish(client.getNick() + " has left the chat. See ya!", null);
        publishClientsList();
    }

    public void process(String msg, ClientHandler client) {
        String[] commands = msg.split("\\s", 2);
        switch (commands[0]) {
            case Commands.WHO_AM_I: {
                client.sendMessage("You are " + client.getNick());
                break;
            }
            case Commands.LOGOUT:
            case Commands.EXIT: {
                logout(client);
                break;
            }
            case Commands.PRIVATE_MESSAGE: {
                String[] split = commands[1].split("\\s", 2);
                ClientHandler destination = getClientByNick(split[0]);
                if (destination != null) {
                    String out = getMessageString(split[1], client.getNick());
                    destination.sendMessage(out);
                    client.sendMessage(out);
                }
                else
                    client.sendMessage("User " + split[0] + " offline or unknown ");
                break;
            }
            case Commands.CHANGE_NICK:{
                if (changeUserNick(commands[1], client)) {
                    client.sendMessage(ServerResponse.CHANGE_NICK_OK + commands[1]);
                } else
                    client.sendMessage(ServerResponse.CHANGE_NICK_FAIL);
                break;
            }
            default: {
                client.sendMessage(Commands.UNKNOWN);
            }
        }

    }

    private synchronized boolean changeUserNick(String newNick, ClientHandler client){
        if (isOnline(newNick)) return false;
        UserInfo oldUser = client.getUser();
        UserInfo newUser = new UserInfo(oldUser.getUsername(), newNick, oldUser.getId());
        if (!provider.update(oldUser, newUser)) return false;
        client.setUser(newUser);
        publish(oldUser.getNickname() + " has changed name to " + newNick, null);
        publishClientsList();
        return true;
    }

    private synchronized ClientHandler getClientByNick(String s) {
        for(ClientHandler client : clients){
            if (s.equals(client.getNick()))
                return client;
        }
        return  null;
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

    private synchronized boolean isOnline(String s) {
        for (ClientHandler client : clients) {
            if (s.equals(client.getNick()))
                return true;
        }
        return false;
    }

    private synchronized void publishClientsList(){
        String list = getClientsList();
        for (ClientHandler c : clients){
            c.sendMessage(list);
        }
    }

    private synchronized String getClientsList(){
        StringBuilder sb = new StringBuilder("/clients_list ");
        for (ClientHandler c : clients) {
            sb.append(c.getNick()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
