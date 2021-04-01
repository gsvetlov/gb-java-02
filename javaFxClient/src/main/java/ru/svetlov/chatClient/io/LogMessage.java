package ru.svetlov.chatClient.io;

import java.io.Serializable;

public class LogMessage implements Serializable {
    private String nickname;
    private String message;

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    public LogMessage(String nickname, String message) {
        this.nickname = nickname;
        this.message = message;
    }
}
