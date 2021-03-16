package ru.svetlov.chatClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import ru.svetlov.chatClient.io.NetClient;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

public class Controller implements Initializable {
    @FXML
    TextField messageField;
    @FXML
    TextArea messagesArea;
    @FXML
    HBox loginBox;
    @FXML
    TextField tfLogin;
    @FXML
    HBox messageBox;

    NetClient netClient;
    BlockingQueue<String> incomingMessages;
    Timer timer;
    private final List<String> messages = new ArrayList<>();
    private String nickName;


    public void btnSendClick() {
        String messageText = messageField.getText();
        if (messageText.isEmpty()) return;
        sendMessage(messageText);
    }

    private void sendMessage(String message) {
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
        processLogout();
        incomingMessages = new LinkedBlockingQueue<>();
        netClient = new NetClient("localhost", 8189, incomingMessages);
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
                case "/login_ok": {
                    messages.add(commands[1]);
                    processLogin();
                }
                case "/login_nok": {
                    Platform.runLater(()-> new Alert(Alert.AlertType.ERROR, commands[1], ButtonType.OK ).showAndWait());
                    messages.add(commands[1]);
                    processLogout();
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

    private void processLogout() {
        nickName = null;
        loginBox.setVisible(true);
        messageBox.setVisible(false);
    }

    private void processLogin() {
        loginBox.setVisible(false);
        messageBox.setVisible(true);
    }

    public void btnLoginClick() {
        if (tfLogin.getText().isEmpty()) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Nickname can't be empty",
                    ButtonType.OK
            ).showAndWait();
            return;
        }
        try {
            nickName = tfLogin.getText();
            netClient.connect();
            netClient.send("/login " + nickName);
        }catch (IOException ex){
            new Alert(
                    Alert.AlertType.ERROR,
                    "NetworkError",
                    ButtonType.OK
            ).showAndWait();
            System.out.println(ex.getMessage());
            netClient.disconnect();
            processLogout();
        }

    }
}
