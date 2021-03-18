package ru.svetlov.chat.server.user;

public class UserInfo {
    private final String username;
    private String password;
    private String nickname;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserInfo(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    public boolean checkPassword(String password){
        return this.password.equals(password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (!username.equals(userInfo.username)) return false;
        return nickname.equals(userInfo.nickname);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + nickname.hashCode();
        return result;
    }
}
