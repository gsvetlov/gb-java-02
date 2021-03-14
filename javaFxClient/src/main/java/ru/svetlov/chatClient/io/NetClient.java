package ru.svetlov.chatClient.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class NetClient implements Runnable {
    private final String host;
    private final int port;
    private final BlockingQueue<String> processQueue;
    private Socket socket;
    private DataOutputStream outStream;
    private DataInputStream inStream;

    public NetClient(String host, int port, BlockingQueue<String> processQueue) {
        this.host = host;
        this.port = port;
        this.processQueue = processQueue;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            inStream = new DataInputStream(socket.getInputStream());
            outStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        connect();
        try {
            while (socket.isConnected()) {
                String incoming = inStream.readUTF();
                processQueue.add(incoming);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            disconnect();
        }
    }

    public void send(String msg) throws IOException {
        outStream.writeUTF(msg);
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
