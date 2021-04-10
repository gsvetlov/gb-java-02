package ru.svetlov.chat.server.dbc;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.svetlov.chat.server.AuthenticationProvider;
import ru.svetlov.chat.server.user.UserInfo;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class JdbcAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LogManager.getLogger();
    String connectionString;
    Connection connection;
    Statement stmt;

    public JdbcAuthenticationProvider(String connectionString, boolean ensureCreated) {
        this.connectionString = connectionString;
        log.trace("connection string: {}", connectionString);
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
                log.throwing(Level.ERROR, e);
            }
        } catch (ClassNotFoundException e) {
            log.throwing(Level.ERROR, e);
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
            log.throwing(Level.ERROR, e);
        }
        log.debug("Database connected");
    }

    private void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                log.debug("Database disconnected");
            } catch (SQLException e) {
                log.throwing(Level.ERROR, e);
            }
        }
    }

    @Override
    public UserInfo authenticate(String username, String password) {
        log.debug("Incoming login request {}:{}", username, password);
        UserInfo user = null;
        try {
            connect();
            ResultSet rs = stmt.executeQuery(String.format("select * from users where username='%s';", username));
            if (rs.next() && rs.getString("password").equals(password)) {
                user = new UserInfo(rs.getString("username"), rs.getString("nickname"), rs.getInt("id"));
                log.info("User {} has logged in", user.getUsername());
            } else
                log.warn("Login request {}:{} failed", username, password);
        } catch (SQLException e) {
            log.throwing(Level.ERROR, e);
        } finally {
            disconnect();
        }
        return user;
    }

    @Override
    public boolean update(UserInfo user, UserInfo newUser) {
        log.info("Incoming update request: {} to {}", user, newUser);
        try {
            connect();
            ResultSet record = stmt.executeQuery(
                    String.format("select username, nickname from users where nickname='%s'",
                            newUser.getNickname()));
            if (record.next() && !record.getString("username").equals(newUser.getUsername())){
                log.debug("Update failed");
                return false;
            }
            int result = stmt.executeUpdate(
                    String.format("update users set username='%s', nickname='%s' where id='%d';",
                            newUser.getUsername(),
                            newUser.getNickname(),
                            user.getId()));
            log.trace("Sql command returned {}", result);
            if (result == 0){
                log.debug("Update failed");
                return false;
            }
        } catch (SQLException e) {
            log.throwing(Level.ERROR, e);
        } finally {
            disconnect();
        }
        log.info("Update successful");
        return true;
    }

    @Override
    public UserInfo getUserById(int id) {
        return null;
    }

    private void createDb() throws SQLException {
        log.debug("DB creation started");
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
        log.error("DB created from scratch");
    }
}
