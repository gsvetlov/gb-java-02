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
        for (String message : messages) {
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
                        process(incomingMessages.take());
                    } catch (InterruptedException e) {
                        System.out.println("TimerTask interrupted");
                    }
                }
                update(messagesArea);
            }
        }, 500, 1000);
    }

    private void process(String msg) {
        if (msg == null) return;
        if (msg.startsWith("/")){
            String[] commands = msg.split(" ", 2);
            switch (commands[0]){
                case "/logout":{
                    timer.cancel();
                    netClient.disconnect();
                    //new Alert(Alert.AlertType.ERROR, "Connection closed", ButtonType.OK ).show();
                    break;
                }
                case "/login_ok":
                case "/login_nok": {
                    //new Alert(Alert.AlertType.ERROR, commands[1], ButtonType.OK ).show();
                    messages.add(commands[1]);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + msg);
            }
        } else {
            messages.add(msg);
        }
        update(messagesArea);
    }
}
