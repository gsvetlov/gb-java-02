package ru.svetlov.chat.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.svetlov.chat.server.user.UserInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private static final Logger log = LogManager.getLogger();
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
            log.debug("InputDataStream created");
            out = new DataOutputStream(socket.getOutputStream());
            log.debug("OutDataStream created");
            threadPool.execute(() -> {
                try {
                    log.trace("{} in communication cycle", this);
                    while (!Thread.interrupted()) {
                        authenticate();
                        if (hasLogin) communicate();
                    }
                } catch (IOException e) {
                    log.throwing(Level.ERROR, e);
                } finally {
                    disconnect();
                }
            });
        } catch (IOException e) {
            log.throwing(Level.ERROR, e);
        }
    }

    private void disconnect() {
        log.debug("{} disconnecting", this);
        hasLogin = false;
        if (socket == null) return;
        try {
            if (socket.isConnected()) {
                server.logout(this);
                socket.close();
            }
        } catch (IOException e) {
            log.throwing(Level.ERROR, e);
        }
    }

    private void authenticate() throws IOException {
        log.debug("{} authenticating", this);
        while (!hasLogin) {
            log.trace("{} waiting for input", this);
            String[] msg = in.readUTF().split(" ", 2);
            log.trace("{} get: {}", this, msg);
            if (msg.length < 2) continue;
            if (msg[0].equals(Commands.LOGIN)) {
                LoginResponse response = server.login(msg[1], this);
                if (response.getMessage().startsWith(ServerResponse.LOGIN_OK)){
                    user = response.getUser();
                    hasLogin = true;
                }
                log.debug("{} sending response: {}", this, response.getMessage());
                sendMessage(response.getMessage());
            }
        }
    }

    private void communicate() throws IOException {
        log.debug("{} communicating", this);
        while (!socket.isClosed()) {
            String msg = in.readUTF();
            log.trace("{} get: {}", this, msg);
            if (msg.startsWith("/"))
                server.process(msg, this);
            else
                server.publish(msg, this);
            if (msg.equals("/logout")) break;
        }
    }

    public void sendMessage(String message) {
        log.trace("{} sending: {}", this, message);
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            log.throwing(Level.ERROR, e);
            disconnect();
        }
    }
}
