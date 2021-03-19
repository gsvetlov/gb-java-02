package ru.svetlov.chat.server;

import ru.svetlov.chat.server.user.UserInfo;

import java.io.*;
import java.util.*;

public class ServerApp {
    public static Server server;
    public static void main(String[] args) {

        try {
            server = new Server(8189);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                if ((reader.readLine()).equals("exit")) {
                    server.shutdown();
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("ServerApp shutdown gracefully");
    }


}
