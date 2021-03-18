package ru.svetlov.chat.server;

public class ServerResponse {
    public static final String LOGIN_OK = "/login_ok Login successful";
    public static final String LOGIN_FAIL_CONNECTED = "/login_nok User already connected";
    public static final String LOGIN_FAIL_INCORRECT = "/login_nok Username/password incorrect";
    public static final String CHANGE_NICK_FAIL = "/change_nick_nok Nickname is already occupied";
    public static final String CHANGE_NICK_OK = "/change_nick_ok";

}
