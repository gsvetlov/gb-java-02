package ru.svetlov.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerApp {
    static Server server;
    public static void main(String[] args) {
        try {
            server = new Server(8189);
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
