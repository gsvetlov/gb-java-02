package ru.svetlov.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            Thread listener = getListener(serverSocket);
            listener.setDaemon(true);
            listener.start();
            while (listener.isAlive()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                    if ((reader.readLine()).equals("/close")) listener.interrupt();
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("server stopped");
    }

    private static Thread getListener(ServerSocket serverSocket) {
        return new Thread(() -> {
            try {
                System.out.printf("Listening on [localhost:%d]\n", serverSocket.getLocalPort());
                Socket socket = serverSocket.accept();
                System.out.println("connection established...");
                DataInputStream inStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                int msgCount = 0;
                while (!Thread.interrupted()) {
                    String msg = inStream.readUTF();
                    System.out.println(msg);
                    if ("/stat".equals(msg))
                        outStream.writeUTF("Messages sent: " + msgCount);
                    else {
                        outStream.writeUTF("Echo: " + msg);
                        msgCount++;
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }, "listener");
    }
}
