package akira.db;

import akira.db.impl.SQLiteDatabase;
import akira.util.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class Core {
    private DatabaseInterface db;
    private static final Logger LOGGER = LoggerFactory.getLogger(Core.class);

    public Core() {
        try {
            this.db = new SQLiteDatabase(new File("cheese.sqlite").getAbsolutePath());
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize the database: ", e);
        }
    }

    public boolean addUser(String discordId, String surname) {
        return db.addUser(discordId, surname);
    }

    public boolean updateAbsences(String surname, int absences) {
        return db.updateAbsences(surname, absences);
    }

    public boolean updateExcusedAbsences(String surname, int excusedAbsences) {
        return db.updateExcusedAbsences(surname, excusedAbsences);
    }

    public boolean updateSurname(String oldSurname, String newSurname) {
        return db.updateSurname(oldSurname, newSurname);
    }

    public boolean updateId(String newId, String surname) {
        return db.updateId(newId, surname);
    }

    public boolean deleteUser(String discordId) {
        return db.deleteUser(discordId);
    }

    public List<User> getUserById(String discordId) {
        return db.getUserById(discordId);
    }

    public List<User> getAllUsers() {
        return db.getAllUsers();
    }
}