package ru.svetlov.chat.server;

import ru.svetlov.chat.server.user.UserInfo;

public interface AuthenticationProvider {
    UserInfo authenticate(String username, String password);
    boolean update(UserInfo user, UserInfo newUser);
    UserInfo getUserById(int id);
}
