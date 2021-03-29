package ru.svetlov.chat.server;

import ru.svetlov.chat.server.dbc.JdbcAuthenticationProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerApp {
    static Server server;
    static AuthenticationProvider provider;
    public static void main(String[] args) {
        try {
            provider = new JdbcAuthenticationProvider("jdbc:sqlite:users.db", true);
            server = new Server(8189, provider);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                if (reader.readLine().equals("exit")) {
                    server.shutdown();
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
