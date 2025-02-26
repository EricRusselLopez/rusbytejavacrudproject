package auth;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserAuthenticator {

    protected String[] loadedUserSession = readUserSessionFromFile("./src/auth/loginSession/userSession.txt");
    List<String[]> loadedUsers = readUsersFromFile("./src/data/users.txt");

    public final boolean isAuthenticated() {
        if (loadedUsers != null && !loadedUsers.isEmpty() && loadedUserSession != null) {
            for (String[] user : loadedUsers) {
                if (user[0].equals(loadedUserSession[0]) && user[1].equals(loadedUserSession[1]) && user[2].equals(loadedUserSession[2])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean login(String username, String email, String password) {

        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            for (String[] user : loadedUsers) {
                username = user[0];
                if (user[1].equals(email) && user[2].equals(password)) {
                    saveUserSessionToFile(username, email, password);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean confirmUserAdminChanges(String password) {

        if (readUserSessionFromFile("./src/auth/loginSession/userSession.txt") != null) {
            if (readUserSessionFromFile("./src/auth/loginSession/userSession.txt")[2].equals(password)) {
                return true;
            }
        }
        return false;
    }

    private static void saveUserSessionToFile(String username, String email, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/auth/loginSession/userSession.txt", true))) {

            writer.write(username + "," + email + "," + password);
            writer.newLine();
        } catch (IOException e) {
        }
    }

    public boolean registerUser(String username, String email, String password) {
        email = email.toLowerCase();

        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            for (String[] user : loadedUsers) {
                if (user[1].equals(email)) {
                    return false;
                }
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/data/users.txt", true))) {
            writer.write(username + "," + email + "," + password + ",admin");
            writer.newLine();
            return true;
        } catch (IOException e) {
        }

        return false;
    }

    private String[] readUserSessionFromFile(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();

            if (line != null) {
                return line.split(",");
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private List<String[]> readUsersFromFile(String file) {
        List<String[]> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(line.split(","));
            }
            return users;
        } catch (IOException e) {
            return null;
        }
    }

    public void logout() {
        try {
            File file = new File("./src/auth/loginSession/userSession.txt");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("");
            }

        } catch (IOException e) {
        }
    }
}
