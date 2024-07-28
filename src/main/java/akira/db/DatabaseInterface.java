package akira.db;

import akira.util.User;

import java.util.List;

public interface DatabaseInterface {
    boolean addUser(String discordId, String surname);

    boolean updateAbsences(String surname, int absences);

    boolean updateExcusedAbsences(String surname, int excusedAbsences);

    boolean updateSurname(String oldSurname, String newSurname);

    boolean updateId(String newId, String surname);

    boolean deleteUser(String discordId);

    List<User> getUserById(String discordId);

    List<User> getAllUsers();
}