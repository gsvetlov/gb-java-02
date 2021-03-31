package ru.svetlov.chat.server.user;

public class UserInfo {
    private int id = -1;
    private String username;
    private String nickname;

    public int getId(){
        return id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public UserInfo(String username, String nickname) {
        this.username = username;
        this.nickname = nickname;
    }
    public UserInfo(String username, String nickname, int id) {
        this.username = username;
        this.nickname = nickname;
        this.id = id;
    }
}
