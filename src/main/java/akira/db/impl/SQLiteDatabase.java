package akira.db.impl;

import akira.db.DatabaseInterface;
import akira.util.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabase implements DatabaseInterface {
    private final String URL;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDatabase.class);

    public SQLiteDatabase(String path) throws SQLException {
        URL = "jdbc:sqlite:" + path;
        try (Connection cn = connect(); Statement st = cn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                    "discord_id TEXT PRIMARY KEY," +
                    "surname TEXT NOT NULL," +
                    "absences INTEGER DEFAULT 0," +
                    "excused_absences INTEGER DEFAULT 0" +
                    ");");
            LOGGER.info("Database initialized at path: {}", path);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    @Override
    public boolean addUser(String discordId, String surname) {
        String sql = "INSERT INTO students (discord_id, surname) values (?, ?)";
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql)) {
            pc.setString(1, discordId);
            pc.setString(2, surname);
            pc.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Failed to add a user: discordId={}, surname={}", discordId, surname, e);
            return false;
        }
    }

    @Override
    public boolean updateAbsences(String surname, int absences) {
        String sql = "UPDATE students SET absences = absences + ? WHERE surname = ?";
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql)) {
            pc.setInt(1, absences);
            pc.setString(2, surname);
            int rowsUpdated = pc.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update absences: surname={}, absences={}", surname, absences, e);
            return false;
        }
    }

    @Override
    public boolean updateExcusedAbsences(String surname, int excusedAbsences) {
        String sql = "UPDATE students SET excused_absences = excused_absences + ? WHERE surname = ?";
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql)) {
            pc.setInt(1, excusedAbsences);
            pc.setString(2, surname);
            int rowsUpdated = pc.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update excused absences: surname={}, excusedAbsences={}", surname, excusedAbsences, e);
            return false;
        }
    }

    @Override
    public boolean updateSurname(String oldSurname, String newSurname) {
        String sql = "UPDATE students SET surname = ? WHERE surname = ?";
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql)) {
            pc.setString(1, newSurname);
            pc.setString(2, oldSurname);
            int rowsUpdated = pc.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update surname: oldSurname={}, newSurname={}", oldSurname, newSurname, e);
            return false;
        }
    }

    @Override
    public boolean updateId(String newId, String surname) {
        String sql = "UPDATE students SET discord_id = ? WHERE surname = ?";
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql)) {
            pc.setString(1, newId);
            pc.setString(2, surname);
            int rowsUpdated = pc.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to update ID: surname={}, newId={}", surname, newId, e);
            return false;
        }
    }

    @Override
    public boolean deleteUser(String discordId) {
        String sql = "DELETE FROM students WHERE discord_id = ?";
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql)) {
            pc.setString(1, discordId);
            int rowsDeleted = pc.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to delete user: discordId={}", discordId, e);
            return false;
        }
    }


    @Override
    public List<User> getUserById(String discordId) {
        String sql = "SELECT surname, absences, excused_absences FROM students WHERE discord_id = ?";
        List<User> userList = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql)) {
            pc.setString(1, discordId);
            try (ResultSet result = pc.executeQuery()) {
                while (result.next()) {
                    User user = new User();
                    user.setSurname(result.getString("surname"));
                    user.setAbsences(result.getInt("absences"));
                    user.setExcusedAbsences(result.getInt("excused_absences"));
                    userList.add(user);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Couldn't find the user by id: discordId={}", discordId, e);
        }
        return userList;
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM students";
        List<User> userList = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement pc = c.prepareStatement(sql); ResultSet result = pc.executeQuery()) {
            while (result.next()) {
                User user = new User();
                user.setDiscordId(result.getString("discord_id"));
                user.setSurname(result.getString("surname"));
                user.setAbsences(result.getInt("absences"));
                user.setExcusedAbsences(result.getInt("excused_absences"));
                userList.add(user);
            }
        } catch (SQLException e) {
            LOGGER.error("Search error: ", e);
        }
        return userList;
    }
}