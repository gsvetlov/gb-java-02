package ru.svetlov.chat.server;

import ru.svetlov.chat.server.user.UserInfo;

public class LoginResponse {
    private final String message;
    private final UserInfo user;

    public LoginResponse(UserInfo user, String message) {
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public UserInfo getUser() {
        return user;
    }

}
