package ru.svetlov.chat.server;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.svetlov.chat.server.dbc.JdbcAuthenticationProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerApp implements Loggable {
    private static final Logger log = LogManager.getLogger();
    static Server server;
    static AuthenticationProvider provider;

    public static void main(String[] args) {
        try {
            provider = new JdbcAuthenticationProvider("jdbc:sqlite:users.db", true);
            log.debug("Database provider created successfully {}", provider);
            server = new Server(8189, provider);
            log.debug("Server created successfully {}", server);
        } catch (IOException e) {
            log.throwing(Level.ERROR, e);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            log.trace("Processing console input...");
            while (true) {
                if (reader.readLine().equals("exit")) {
                    log.trace("Server shutdown requested");
                    server.shutdown();
                    log.warn("Server shutdown successful");
                    break;
                }
            }
        } catch (IOException e) {
            log.throwing(Level.ERROR, e);
        }
        log.debug("ServerApp finished");
    }
}
