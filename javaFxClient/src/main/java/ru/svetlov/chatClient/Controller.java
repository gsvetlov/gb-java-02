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
    @FXML
    ListView<String> clientsList;

    NetClient netClient;
    Thread listener;
    private List<String> messages;
    private String nickName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        netClient = new NetClient("localhost", 8189);
        messages = new ArrayList<>();
        listener = new Thread(()->{
            while(true) {
                String message = netClient.nextMessage();
                System.out.println(message);
                process(message);
            }
        }); // не успел реализовать через callback
        listener.setDaemon(true);
        listener.start();
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
                    Platform.runLater(() ->
                            showAlert("Connection closed"));
                    break;
                }
                case "/login_ok":
                case "/change_nick_ok": {
                    nickName = commands[1];
                    Platform.runLater(this::processLogin);
                    break;
                }
                case "/login_nok": {
                    Platform.runLater(() ->
                            showAlert(commands[1]));
                    messages.add(commands[1]);
                    Platform.runLater(this::processLogout);
                    break;
                }

                case "/clients_list":{
                    String[] clients = commands[1].split("\\s");
                    System.out.println(commands[1]);
                    Platform.runLater(()->{
                        clientsList.getItems().clear();
                        for (String c : clients){
                            clientsList.getItems().add(c);
                        }
                    });
                    break;
                }

                case "/change_nick_nok":{
                    Platform.runLater(() ->
                            showAlert(commands[1]));
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

    // переписать на отдельную панель
    private void processLogout() {
        loginField.clear();
        loginField.setEditable(true);
        loginField.setPromptText("type your name and password to login ...");
        loginField.requestFocus();
        btnLogin.setText("Login");
        messageBox.setManaged(false);
        messageBox.setVisible(false);
        netClient.disconnect();
    }

    // переписать на отдельную панель
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
        if (loginField.getText().isEmpty()) { // для простоты вводим имя пользователя и пароль через пробел
            new Alert(
                    Alert.AlertType.ERROR,
                    "Username can't be empty",
                    ButtonType.OK
            ).showAndWait();
            return;
        }
        try {
            if (nickName == null) {
                netClient.connect();
                netClient.send("/login " + loginField.getText());

            } else {
                netClient.send("/logout");
                nickName = null;
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
    private void showAlert(String message){
        new Alert(
                Alert.AlertType.ERROR,
                message,
                ButtonType.OK
        ).showAndWait();
    }
}
