package com.saiev.server;

import java.sql.*;

public class DatabaseAuthService implements AuthService {

    private Connection connection;
    private PreparedStatement preparedStatement;

    private void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/chatUsers.db");
    }

    private void disconnect() {
        try {
            preparedStatement.close();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            connect();
            if (!connection.isClosed()) {
                preparedStatement = connection.prepareStatement("SELECT user_nick FROM users WHERE user_name= ? and user_password=?;");
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, password);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    return rs.getString("user_nick");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            disconnect();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            connect();
            if (!connection.isClosed()) {
                preparedStatement = connection.prepareStatement("SELECT count(id) as usrs FROM users WHERE user_name= ? and user_nick=?;");
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, nickname);

                int userFound = 0;
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    userFound = rs.getInt("users");
                }
                if (userFound > 0) {
                    return false;
                } else {
                    preparedStatement = connection.prepareStatement("INSERT INTO users (user_name, user_password, user_nick) values (?, ?, ?);");
                    preparedStatement.setString(1, login);
                    preparedStatement.setString(2, password);
                    preparedStatement.setString(3, nickname);
                    if (preparedStatement.executeUpdate() > 0) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return false;
    }

    public boolean setNewNickname(String newNickName, String oldNickName) {
        try {
            connect();
            if (!connection.isClosed()) {
                //для поля user_nick установлен признак уникальности
                preparedStatement = connection.prepareStatement("UPDATE users SET user_nick = ? WHERE user_nick = ?;");
                preparedStatement.setString(1, newNickName);
                preparedStatement.setString(2, oldNickName);
                if (preparedStatement.executeUpdate() > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return false;
    }
}
