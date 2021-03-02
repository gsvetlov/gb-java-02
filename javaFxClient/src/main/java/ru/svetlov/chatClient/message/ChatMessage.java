package ru.svetlov.chatClient.message;

import java.util.Date;

public class ChatMessage {
    private final String message;
    private final String user;
    private final long timestamp;

    public ChatMessage(String user, String message, long timestamp) {
        this.message = message;
        this.user = user;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getUser() {
        return user;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
