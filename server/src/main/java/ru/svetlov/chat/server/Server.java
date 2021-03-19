package ru.svetlov.chat.server;

import ru.svetlov.chat.server.user.UserInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private final int serverPort;
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients;

    private Map<String, UserInfo> users;

    public Server(int port) throws IOException {
        initUserInfo();
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

    public LoginResponse login(String loginMsg, ClientHandler client) {

        String[] userInfoMsg = loginMsg.split("\\s", 2);
        UserInfo user = users.get(userInfoMsg[0]);

        if (user == null || !user.checkPassword(userInfoMsg[1])) {
            return new LoginResponse(null, ServerResponse.LOGIN_FAIL_INCORRECT);
        }

        if (isOnline(user.getNickname())) {
            return new LoginResponse(null, ServerResponse.LOGIN_FAIL_CONNECTED);
        } else {
            clients.add(client);
            publish(user.getNickname() + " joined the chat. Welcome!", null);
            publishClientsList();
            return new LoginResponse(user, ServerResponse.LOGIN_OK);
        }
    }

    public synchronized void logout(ClientHandler client) {
        clients.remove(client);
        publish(client.getNick() + " is leaving the chat. See ya later!", null);
        publishClientsList();

    }

    public void process(String msg, ClientHandler client) {
        String[] commands = msg.split(" ", 2);
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
                String[] split = commands[1].split(" ", 2);
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
                if (changeUserNick(commands[1], client))
                    client.sendMessage(ServerResponse.CHANGE_NICK_OK);
                else
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
        String oldNick = client.getNick();
        client.getUser().setNickname(newNick);
        publish(oldNick + " has changed name to " + newNick, null);
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

    public void initUserInfo(){
        users = new HashMap<>();
        List<UserInfo> list = Arrays.asList(
                new UserInfo("bob", "bob1234","GreatBob"),
                new UserInfo("jacky", "jacky1234","Jacky"),
                new UserInfo("mike", "mike1234","Michael"),
                new UserInfo("pat", "pat1234","Pat"),
                new UserInfo("sue", "sue1234","Sue"),
                new UserInfo("mary", "mary1234","Mary Jane")
        );
        for(UserInfo user : list){
            users.put(user.getUsername(),user);
        }
    }

    public void publishClientsList(){
        publish(getClientsList(), null);
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
