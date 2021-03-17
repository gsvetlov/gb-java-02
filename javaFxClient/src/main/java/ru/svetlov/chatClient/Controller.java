package ru.svetlov.chatClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import ru.svetlov.chatClient.io.NetClient;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    TextField messageField, loginField;
    @FXML
    TextArea messagesArea;
    @FXML
    HBox messageBox, loginBox;
    @FXML
    Button btnLogin;

    NetClient netClient;
    Timer timer;
    private List<String> messages;
    private String nickName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        netClient = new NetClient("localhost", 8189);
        messages = new ArrayList<>();
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (netClient.getSize() != 0)
                    process(netClient.dequeue());
            }
        }, 1000, 200);
    }


    public void btnSendClick() {
        String messageText = messageField.getText();
        messageField.requestFocus();
        if (messageText.isEmpty()) return;
        sendMessage(messageText);
    }

    private void sendMessage(String message) {
        netClient.send(message);
        messageField.clear();
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



    private void process(String msg) {
        if (msg == null) return;
        if (msg.startsWith("/")) {
            String[] commands = msg.split(" ", 2);
            switch (commands[0]) {
                case "/logout": {
                    Platform.runLater(() ->
                            new Alert(
                                    Alert.AlertType.ERROR,
                                    "Connection closed",
                                    ButtonType.OK
                            ).showAndWait());
                    break;
                }
                case "/login_ok": {
                    messages.add(commands[1]);
                    Platform.runLater(this::processLogin);
                    break;
                }
                case "/login_nok": {
                    Platform.runLater(() ->
                            new Alert(
                                    Alert.AlertType.ERROR,
                                    commands[1],
                                    ButtonType.OK
                            ).showAndWait());
                    messages.add(commands[1]);
                    Platform.runLater(this::processLogout);
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
        loginField.clear();
        loginField.setEditable(true);
        loginField.setPromptText("type your name to login ...");
        loginField.requestFocus();
        btnLogin.setText("Login");
        messageBox.setManaged(false);
        messageBox.setVisible(false);
        netClient.disconnect();
    }

    private void processLogin() {
        loginField.setEditable(false);
        loginField.setFocusTraversable(false);
        loginField.setText(nickName);
        btnLogin.setText("Logout");
        messageBox.setManaged(true);
        messageBox.setVisible(true);
        messageField.requestFocus();
    }

    public void btnLoginClick() {
        if (loginField.getText().isEmpty()) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "Nickname can't be empty",
                    ButtonType.OK
            ).showAndWait();
            return;
        }
        try {
        if (nickName == null){

            nickName = loginField.getText();
            netClient.connect();
            netClient.send("/login " + nickName);

        } else {
            netClient.send("/logout");
            processLogout();
        }
        } catch (IOException ex) {
            new Alert(
                    Alert.AlertType.ERROR,
                    "NetworkError",
                    ButtonType.OK
            ).showAndWait();
            System.out.println(ex.getMessage());
            processLogout();
        }


    }
}
