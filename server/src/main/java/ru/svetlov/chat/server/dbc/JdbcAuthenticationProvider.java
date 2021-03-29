package ru.svetlov.chat.server.dbc;

import ru.svetlov.chat.server.AuthenticationProvider;
import ru.svetlov.chat.server.user.UserInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JdbcAuthenticationProvider implements AuthenticationProvider {
    String connectionString;
    Connection connection;
    Statement stmt;
    //PreparedStatement ps;

    public JdbcAuthenticationProvider(String connectionString, boolean ensureCreated) {
        this.connectionString = connectionString;
        try {
            Class.forName("org.sqlite.JDBC");
            connect();
            try (ResultSet rs = stmt.executeQuery("select name from sqlite_master where type='table' and name='users';")) {
                if (!rs.next())
                    if (ensureCreated)
                        createDb();
                    else
                        throw new RuntimeException("Missing database table");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Missing JDBC driver");
        } finally {
            disconnect();
        }
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(connectionString);
            stmt = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public UserInfo authenticate(String username, String password) {
        UserInfo user = null;
        try {
            connect();
            ResultSet rs = stmt.executeQuery(String.format("select * from users where username='%s';", username));
            if (rs.next() && rs.getString("password").equals(password))
                user = new UserInfo(rs.getString("username"), rs.getString("nickname"), rs.getInt("id"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return user;
    }

    @Override
    public boolean update(UserInfo user, UserInfo newUser) {
        try {
            connect();
            ResultSet record = stmt.executeQuery(
                    String.format("select username, nickname from users where nickname='%s'",
                            newUser.getNickname()));
            if (record.next() && !record.getString("username").equals(newUser.getUsername()))
                return false;
            int result = stmt.executeUpdate(
                    String.format("update users set username='%s', nickname='%s' where id='%d';",
                            newUser.getUsername(),
                            newUser.getNickname(),
                            user.getId()));
            if (result == 0) return false;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return true;
    }

    @Override
    public UserInfo getUserById(int id) {
        return null;
    }

    private void createDb() throws SQLException {
        stmt.executeUpdate("drop table if exists users;");
        stmt.executeUpdate("CREATE TABLE if not exists users (id INTEGER PRIMARY KEY AUTOINCREMENT, username STRING UNIQUE, nickname STRING UNIQUE, password STRING);");
        List<String[]> list = Arrays.asList(
                new String[]{"bob", "GreatBob", "bob1234"},
                new String[]{"jacky", "Jacky", "jacky1234"},
                new String[]{"mike", "Michael", "mike1234"},
                new String[]{"pat", "Pat", "pat1234"},
                new String[]{"sue", "Sue", "sue1234",},
                new String[]{"mary", "Mary", "mary1234"}
        );
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement("insert into users (username, nickname, password) values (?, ?, ?);");
        for (String[] entry : list) {
            for (int i = 0; i < entry.length; i++)
                ps.setString(i + 1, entry[i]);
            ps.executeUpdate();
        }
        connection.commit();
    }

}
/*
public void initUserInfo(){
        users = new HashMap<>();

        for(UserInfo user : list){
            users.put(user.getUsername(),user);
        }
    }
*/
/*CREATE TABLE users (
    id       INTEGER PRIMARY KEY,
    username STRING  UNIQUE,
    nickname STRING  UNIQUE,
    password STRING
);
*/