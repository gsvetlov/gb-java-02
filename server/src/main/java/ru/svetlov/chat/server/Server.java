package ru.svetlov.chat.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.svetlov.chat.server.user.UserInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger log = LogManager.getLogger();
    private final AuthenticationProvider provider;
    private final int serverPort;
    private final ServerSocket serverSocket;
    private final List<ClientHandler> clients;

    public Server(int port, AuthenticationProvider provider) throws IOException {
        this.provider = provider;
        serverPort = port;
        serverSocket = new ServerSocket(serverPort);
        log.info("ServerSocket created on {}:{}",serverSocket.getInetAddress(), serverSocket.getLocalPort());
        clients = new ArrayList<>();
        ExecutorService threadPool = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        threadPool.execute(() -> {
            log.trace("Server thread created");
            try {
                log.info("Listening on port {}", serverSocket.getLocalPort());
                while (!Thread.interrupted()) {
                    Socket socket = serverSocket.accept();
                    log.trace("connection established...");
                    ClientHandler client = new ClientHandler(socket, this, threadPool);
                    log.trace("new ClientHandler: {}", client);
                }
            } catch (SocketException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.throwing(Level.ERROR, e);
            } finally {
                log.debug("Server shutdown initiated");
                shutdown();
                threadPool.shutdown();
                log.warn("Server shutdown successful");
            }
        });
    }

    public synchronized void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            log.throwing(Level.ERROR, e);
        }
        log.debug("Logout of {} clients initiated", clients.size());
        while (clients.size() > 0)
            logout(clients.get(0));
        log.debug("Logout of clients finished");
    }

    public LoginResponse login(String loginMsg, ClientHandler client) {
        log.debug("Client login requested: {}", loginMsg);
        String[] tokens = loginMsg.split("\\s", 2);
        log.info("Login initiated with tokens: [{}] [{}]", tokens[0], tokens[1]);
        UserInfo user = provider.authenticate(tokens[0], tokens[1]);

        if (user == null) {
            log.info("Login failed");
            return new LoginResponse(null, ServerResponse.LOGIN_FAIL_INCORRECT);
        }

        if (isOnline(user.getNickname())) {
            log.info("Login failed. User is already online");
            return new LoginResponse(null, ServerResponse.LOGIN_FAIL_CONNECTED);
        } else {
            log.info("Login successful. User: {}, Nick: {}", user.getUsername(), user.getNickname());
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
        log.debug("Incoming command: {} from {}", msg, client);
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
                } else
                    client.sendMessage("User " + split[0] + " offline or unknown ");
                break;
            }
            case Commands.CHANGE_NICK: {
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

    private synchronized boolean changeUserNick(String newNick, ClientHandler client) {
        log.debug("Change of nick {} to {} requested by {}", client.getNick(), newNick, client.getUser());
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
        for (ClientHandler client : clients) {
            if (s.equals(client.getNick()))
                return client;
        }
        return null;
    }

    public synchronized void publish(String msg, ClientHandler client) {
        String userName = client == null ? "Server" : client.getNick();
        String outMsg = getMessageString(msg, userName);
        log.trace("publishing {}", outMsg);
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

    private synchronized void publishClientsList() {
        String list = getClientsList();
        for (ClientHandler c : clients) {
            c.sendMessage(list);
        }
    }

    private synchronized String getClientsList() {
        StringBuilder sb = new StringBuilder("/clients_list ");
        for (ClientHandler c : clients) {
            sb.append(c.getNick()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
