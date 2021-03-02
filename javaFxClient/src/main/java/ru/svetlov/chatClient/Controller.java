package ru.svetlov.chatClient;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.svetlov.chatClient.message.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    TextField messageField;
    @FXML
    TextArea messagesArea;

    private final List<ChatMessage> messages = new ArrayList<>();

    public void btnSendClick() {
        String messageText = messageField.getText();
        if (messageText.isEmpty()) return;
        ChatMessage chatMessage = new ChatMessage(
                "@me",
                messageText,
                System.currentTimeMillis());
        messageSend(chatMessage);
        messageField.clear();
    }

    private void messageSend(ChatMessage message) {
        messages.add(message);
        update(messagesArea);
    }

    private void update(TextArea mArea) {
        mArea.clear();
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
        for (ChatMessage message : messages) {
            sb.append(sdf.format(message.getTimestamp()));
            sb.append(" [");
            sb.append(message.getUser());
            sb.append("] : ");
            sb.append(message.getMessage());
            sb.append("\n");
        }
        mArea.appendText(sb.toString());
    }

    public void btnMessageTargetClick() {
        // заготовка под выбор адресата сообщения
    }
}
