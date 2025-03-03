package auth;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This class provides methods to authenticate users, register admin users,
 * manage user sessions, and handle authentication-related functionality within
 * the system.
 * </p>
 *
 * <p>
 * <b>Key Features:</b></p>
 * <ul>
 * <li>User login authentication.</li>
 * <li>Register admin users.</li>
 * <li>Session management for authenticated users.</li>
 * <li>Logout functionality to clear user sessions.</li>
 * <li>Support for reading authentication data from a file.</li>
 * </ul>
 *
 * @author Eric Russel M. Lopez
 * @version 23.0.1 (Java SE 23.0.1)
 * @since 2025
 */
public class UserAuthenticator {

    protected String[] loadedUserSession = readUserSessionFromFile("./src/auth/loginSession/userSession.txt");
    List<String[]> loadedUsers = readUsersFromFile("./src/data/users.txt");

    /**
     * <p>
     * This method verifies if the user stored in {@code loadedUserSession}
     * exists within the list of registered users ({@code loadedUsers}). If a
     * matching user is found, the authentication is considered valid.
     * </p>
     *
     * @return {@code true} if the user session matches a registered user,
     * otherwise {@code false}.
     */
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

    /**
     * Authenticates a user based on the provided username, email, and password.
     * <p>
     * This method authenticate admin users to find a match for the given email
     * and password. If a match is found, the user session is saved to a file,
     * and authentication is successful.
     * </p>
     *
     * @param username The username of the user (not used for authentication but
     * assigned from loaded data).
     * @param email The email address used for authentication.
     * @param password The password used for authentication.
     * @return {@code true} if authentication is successful, otherwise
     * {@code false}.
     */
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

    /**
     * Confirms administrative changes by verifying the provided password.
     * <p>
     * This method reads the currently logged-in user's session from the session
     * file and checks if the stored password matches the provided password. If
     * they match, the CRUD operation is allowed.
     * </p>
     *
     * @param password The password of the currently logged-in admin user.
     * @return {@code true} if the password matches and confirmation is
     * successful, otherwise {@code false}.
     * @see #readUserSessionFromFile(String)
     */
    public boolean confirmUserAdminChanges(String password) {
        if (readUserSessionFromFile("./src/auth/loginSession/userSession.txt") != null) {
            if (readUserSessionFromFile("./src/auth/loginSession/userSession.txt")[2].equals(password)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the user session to a file after a successful login.
     * <p>
     * This method appends the user's session details (username, email, and
     * password) to the session file located at
     * <code>./src/auth/loginSession/userSession.txt</code>.
     * </p>
     *
     * @param username The username of the logged-in user.
     * @param email The email of the logged-in user.
     * @param password The password of the logged-in user.
     * @see #readUserSessionFromFile(String)
     */
    private static void saveUserSessionToFile(String username, String email, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/auth/loginSession/userSession.txt", true))) {
            writer.write(username + "," + email + "," + password);
            writer.newLine();
        } catch (IOException e) {
        }
    }

    /**
     * Registers a new user (admin) and saves their details to the users file.
     * <p>
     * This method ensures that duplicate emails cannot be registered. If the
     * email is already present in the existing user list, registration will
     * fail. If registration is successful, the user details are stored in
     * <code>./src/data/users.txt</code> in the format:
     * <pre>
     * username,email,password,admin
     * </pre> The role is set to "admin" by default.
     * </p>
     *
     * @param username The username of the new user.
     * @param email The email of the new user (converted to lowercase for
     * consistency).
     * @param password The password of the new user.
     * @return {@code true} if registration is successful, {@code false} if the
     * email already exists or an error occurs.
     */
    public boolean registerUser(String username, String email, String password) {
        email = email.toLowerCase();

        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            for (String[] user : loadedUsers) {
                if (user[1].equals(email)) {
                    return false;
                }
            }
        }

        File file = new File("./src/data/users.txt");

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            LocalDate currentDate = LocalDate.now();
            if (file.length() > 0) {
                raf.seek(file.length() - 1);
                char lastChar = (char) raf.read();

                if (lastChar != '\n') {
                    writer.newLine();
                }
            }

            writer.write(username + "," + email + "," + password + ",admin," + currentDate);
            writer.newLine();
            return true;

        } catch (IOException e) {
        }

        return false;
    }

    /**
     * Reads the stored user session details from a specified file.
     * <p>
     * This method retrieves the first line of the file and splits it into an
     * array based on commas (<code>,</code>). If the file is empty or an error
     * occurs, it returns {@code null}.
     * </p>
     *
     * @param file The path to the session file.
     * @return A {@code String[]} containing the session details (username,
     * email, password), or {@code null} if the file is empty or an error
     * occurs.
     */
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

    /**
     * This method reads the file line by line, splits each line into an array
     * of strings, and stores the arrays in a list.
     * </p>
     *
     * @param file The path to the file containing user data.
     * @return A {@link List} of string arrays, where each array represents a
     * user. Returns {@code null} if an {@link IOException} occurs while reading
     * the file.
     */
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

    /**
     * <p>
     * This method overwrites the user session file with an empty string,
     * effectively removing any stored session data. It ensures that the user is
     * logged out and must re-authenticate on the next login attempt.
     *
     * It will called automatically if the {@code isAuthenticated() is false}
     * </p>
     */
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
