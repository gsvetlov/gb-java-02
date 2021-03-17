package ru.svetlov.chatClient.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class NetClient {
    private final String host;
    private final int port;
    private final Queue<String> processQueue;
    private final Object queueLock = new Object();
    private Socket socket;
    private DataOutputStream outStream;
    private DataInputStream inStream;

    public NetClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.processQueue = new LinkedList<>();
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
                    System.out.println(in);
                    enqueue(in);
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            } finally {
                disconnect();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    private void enqueue(String in) {
        synchronized (queueLock) {
            processQueue.add(in);
        }
    }

    public String dequeue() {
        synchronized (queueLock) {
            return processQueue.poll();
        }
    }

    public int getSize() {
        synchronized (queueLock) {
            return processQueue.size();
        }
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
