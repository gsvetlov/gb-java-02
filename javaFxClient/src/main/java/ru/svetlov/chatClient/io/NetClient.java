package ru.svetlov.chatClient.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetClient {
    private final String host;
    private final int port;
    private final BlockingQueue<String> processQueue;
    private final Object queueLock = new Object();
    private Socket socket;
    private DataOutputStream outStream;
    private DataInputStream inStream;

    public NetClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.processQueue = new LinkedBlockingQueue<>();
    }

    public void connect() throws IOException {
        if (socket != null && !socket.isClosed()) return;
        socket = new Socket(host, port);
        inStream = new DataInputStream(socket.getInputStream());
        outStream = new DataOutputStream(socket.getOutputStream());
        Thread listener = new Thread(() -> {
            try {
                while (true) {
                    String in = inStream.readUTF();
                    processQueue.put(in);
                }
            } catch (IOException | InterruptedException ex) {
                System.out.println(ex.getMessage());
            } finally {
                disconnect();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    public String nextMessage() {
        try {
            return processQueue.take();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void send(String msg) {
        try {
            System.out.println(msg);
            outStream.writeUTF(msg);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            disconnect();
        }
    }

    public void disconnect() {
        if (socket == null) return;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
