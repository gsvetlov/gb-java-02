package ru.svetlov.chatClient;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.svetlov.chatClient.io.NetClient;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Controller implements Initializable {
    Thread io;
    NetClient netClient;
    BlockingQueue<String> incomingMessages;
    Timer timer;
    private final List<String> messages = new ArrayList<>();

    @FXML
    TextField messageField;
    @FXML
    TextArea messagesArea;

    public void btnSendClick() {
        String messageText = messageField.getText();
        if (messageText.isEmpty()) return;
        messageSend(messageText);
    }

    private void messageSend(String message) {
        try {
            netClient.send(message);
            messageField.clear();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to send message\n" + ex.getMessage(),
                    ButtonType.OK).show();
        }
    }

    private void update(TextArea mArea) {
        mArea.clear();
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
        for (String message : messages) {
            sb.append(sdf.format(System.currentTimeMillis()));
            sb.append(" ");
            sb.append(message);
            sb.append("\n");
        }
        mArea.appendText(sb.toString());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        incomingMessages = new LinkedBlockingQueue<>();
        netClient = new NetClient("localhost", 8189, incomingMessages);
        io = new Thread(netClient);
        io.setDaemon(true);
        io.start();
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (incomingMessages.isEmpty()) return;
                while (!incomingMessages.isEmpty()) {
                    try {
                        String in = incomingMessages.take();
                        messages.add(in);
                    } catch (InterruptedException e) {
                        System.out.println("TimerTask interrupted");
                    }
                }
                update(messagesArea);
            }
        }, 500, 1000);
    }
}
