package ru.svetlov.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerApp {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Listening on [localhost:8189]");
            Thread listener = getListener(serverSocket);
            listener.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                if ((reader.readLine()).equals("/close")) listener.interrupt();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println("server stopped");

    }

    private static Thread getListener(ServerSocket serverSocket){
        return new Thread(() -> {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("connection established...");
                DataInputStream inStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                while (!Thread.interrupted()) {
                    String msg = inStream.readUTF();
                    System.out.println(msg);
                    outStream.writeUTF("Echo: " + msg);
                }
            } catch (SocketException ex) {
                System.out.println(ex.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }, "listener");
    }
}
