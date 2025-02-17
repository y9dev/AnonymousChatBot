package org.anonimchatbot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    public static final String Database_URL = "jdbc:sqlite:database.db";

    public static void initDatabase() {
        String sql1 = "CREATE TABLE IF NOT EXISTS users (" +
                      "chat_id TEXT NOT NULL UNIQUE, " +
                      "wait TEXT DEFAULT 'STAY' CHECK(wait IN('WAIT', 'STAY')), " +
                      "banned TEXT DEFAULT 'PARDONED' CHECK(banned IN('BANNED', 'PARDONED')), " +
                      "waitMessage TEXT" +
                      ");";
        String sql2 = "CREATE TABLE IF NOT EXISTS groupMessages (" +
                "chat_id TEXT NOT NULL, " +
                "message_id TEXT NOT NULL" +
                ");";
        String sql3 = "CREATE TABLE IF NOT EXISTS admins (" +
                "user_id TEXT NOT NULL UNIQUE, " +
                "perm TEXT DEFAULT 'USER' CHECK(perm IN('USER', 'ADMIN', 'SUPER_ADMIN'))" +
                ");";
        String sql4 = "UPDATE admins SET perm = 'USER' WHERE perm = ?";
        String sql5 = "UPDATE admins SET perm = 'SUPER_ADMIN' WHERE user_id = ?";

        try (
                Connection c = DriverManager.getConnection(Database_URL);
                Statement st = c.createStatement()) {
            st.execute(sql1);
            st.execute(sql2);
            st.execute(sql3);
            System.out.println("Database has initialized");
        } catch (SQLException e) { e.printStackTrace(System.err); }
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql4)) {
            st.setString(1, Constants.getPerm_SUPER_ADMIN());
            st.execute();
        } catch (SQLException e) {
            e.printStackTrace(System.err); }
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql5)) {
            st.setString(1, Constants.superAdmin_USER_ID);
            st.execute();
        } catch (SQLException e) {
            addPermUser(Constants.superAdmin_USER_ID);
            e.printStackTrace(System.err); }
    }
    public static void addUser(String chat_id) {
        String sql = "INSERT OR IGNORE INTO users (chat_id) VALUES (?)";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, chat_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void addPermUser(String user_id) {
        String sql = "INSERT OR IGNORE INTO admins (user_id, perm) VALUES (?, ?)";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, user_id);
            st.setString(2, Constants.getPerm_USER());
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void upgradePermUser(String user_id) {
        String sql = "UPDATE admins SET perm = 'ADMIN' WHERE user_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, user_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void demotePermUser(String user_id) {
        String sql = "UPDATE admins SET perm = 'USER' WHERE user_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, user_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void banUser(String chat_id) {
        String sql = "UPDATE users SET banned = 'BANNED' WHERE chat_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, chat_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void pardonUser(String chat_id) {
        String sql = "UPDATE users SET banned = 'PARDONED' WHERE chat_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, chat_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void updMessageId(String message_id, String chat_id) {
        String sql = "UPDATE users SET waitMessage = ? WHERE chat_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(2, chat_id);
            st.setString(1, message_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void updWaitStatus(String newStatus, String chat_id) {
        String sql = "UPDATE users SET wait = ? WHERE chat_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, newStatus);
            st.setString(2, chat_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static void updGroupMessage(String chat_id, String message_id) {
        String sql = "INSERT INTO groupMessages (chat_id, message_id) VALUES (?, ?)";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, chat_id);
            st.setString(2, message_id);
            st.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(System.err); }
    }
    public static String getMessageId(String chat_id) {
        String sql = "SELECT waitMessage FROM users WHERE chat_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, chat_id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("waitMessage");
            }
        } catch (SQLException e) { e.printStackTrace(System.err); }
        return null;
    }
    public static String getWaitStatus(String chat_id) {
        String sql = "SELECT wait FROM users WHERE chat_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, chat_id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("wait");
            }
        } catch (SQLException e) { e.printStackTrace(System.err); }
        return null;
    }
    public static String getChatIdFromGroupMessages(String message_id) {
        String sql = "SELECT chat_id FROM groupMessages WHERE message_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, message_id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("chat_id");
            }
        } catch (SQLException e) { e.printStackTrace(System.err); }
        return null;
    }
    public static String getBanned(String chat_id) {
        String sql = "SELECT banned FROM users WHERE chat_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, chat_id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("banned");
            }
        } catch (SQLException e) { e.printStackTrace(System.err); }
        return null;
    }
    public static String getPerm(String user_id) {
        String sql = "SELECT perm FROM admins WHERE user_id = ?";
        try (
                Connection c = DriverManager.getConnection(Database_URL);
                PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, user_id);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getString("perm");
            }
        } catch (SQLException e) { e.printStackTrace(System.err); }
        return null;
    }
}
