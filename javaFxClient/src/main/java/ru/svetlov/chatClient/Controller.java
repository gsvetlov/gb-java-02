package ru.svetlov.chatClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import ru.svetlov.chatClient.io.FileLogger;
import ru.svetlov.chatClient.io.LogMessage;
import ru.svetlov.chatClient.io.Logger;
import ru.svetlov.chatClient.io.NetClient;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    TextField messageField, loginField, passwordField, headerText;
    @FXML
    TextArea messagesArea;
    @FXML
    HBox messageBox, loginBox, headerBox;
    @FXML
    ListView<String> clientsList;

    private NetClient netClient;
    private Thread listener;
    private List<String> messages;
    private String nickName;
    private Logger logger;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        netClient = new NetClient("localhost", 8189);
        messages = new ArrayList<>();
        listener = new Thread(() -> {
            while (true) {
                String message = netClient.nextMessage();
                System.out.println(message);
                process(message);
            }
        }); // не успел реализовать через callback
        listener.setDaemon(true);
        listener.start();
        logger = new FileLogger("chatLog.log", true);
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
            String[] commands = msg.split("\\s", 2);
            switch (commands[0]) {
                case "/logout": {
                    Platform.runLater(() -> {
                        nickName = null;
                        changeView(false);
                    });
                    break;
                }
                case "/login_ok":
                case "/change_nick_ok": {
                    Platform.runLater(() -> {
                        nickName = commands[1];
                        changeView(true);
                        messages.addAll(logger.getEntries(nickName));
                        update(messagesArea);
                    });
                    break;
                }
                case "/login_nok": {
                    Platform.runLater(() -> showAlert(commands[1]));
                    messages.add(commands[1]);
                    Platform.runLater(() -> changeView(false));
                    break;
                }
                case "/clients_list": {
                    String[] clients = commands[1].split("\\s");
                    System.out.println(commands[1]);
                    Platform.runLater(() -> {
                        clientsList.getItems().clear();
                        for (String c : clients) {
                            clientsList.getItems().add(c);
                        }
                    });
                    break;
                }
                case "/change_nick_nok": {
                    Platform.runLater(() ->
                            showAlert(commands[1]));
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + msg);
            }
        } else {
            messages.add(msg);
            if (nickName != null)
                logger.log(new LogMessage(nickName, msg));
        }
        update(messagesArea);
    }

    private void changeView(boolean isLogin) {
        loginBox.setVisible(!isLogin);
        loginBox.setManaged((!isLogin));
        headerText.setText(nickName);
        headerBox.setVisible(isLogin);
        headerBox.setManaged(isLogin);
        messageBox.setVisible(isLogin);
        messageBox.setManaged(isLogin);
        messageField.requestFocus();
    }

    public void btnLoginClick() {
        if (loginField.getText().isEmpty()) {
            showAlert("Username can't be empty");
            return;
        }
        try {
            if (nickName == null) {
                netClient.connect();
                netClient.send("/login " + loginField.getText() + " " + passwordField.getText());
            }
        } catch (IOException ex) {
            showAlert("NetworkError");
            System.out.println(ex.getMessage());
        }
    }

    public void btnLogoutClick() {
        netClient.send("/logout");
        nickName = null;
        messagesArea.clear();
        clientsList.getItems().clear();
        changeView(false);

    }

    private void showAlert(String message) {
        new Alert(
                Alert.AlertType.ERROR,
                message,
                ButtonType.OK
        ).showAndWait();
    }


}
