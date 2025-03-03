package components.userManagementUtils;

import auth.UserAuthenticator;
import components.UserManagement;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import util.customBorder;

/**
 * This class is used in the user management system to handle CRUD (Create,
 * Read, Update, Delete) operations on user table.
 *
 * @author Eric Russel M. Lopez
 * @version 23.0.1 (Java SE 23.0.1)
 * @since 2025
 */
public class UserEditor implements customBorder {

    private final static String userNameBorderTitle = " 👤 Username";
    private final static String emailBorderTitle = " ✉️ Email address";
    private final static String passwordBorderTitle = " 🔒 Password";

    final static Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static final Pattern VALID_PASSWORD_REGEX_NUMBER_CHAR = Pattern.compile(".*[0-9].*");

    private static final Pattern VALID_PASSWORD_REGEX_UPPER_CHAR = Pattern.compile(".*[A-Z].*");

    private static final Pattern VALID_PASSWORD_REGEX_SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    /**
     * Edits the details of a selected user in the user management system.
     * <p>
     * This method opens a dialog allowing the user to modify the username,
     * email, and password of the selected user in a {@code JTable}. Admin users
     * cannot be edited.
     * </p>
     * <p>
     * The password field includes a tooltip explaining password requirements.
     * The save button remains disabled until a change is detected in the
     * fields. If the input is valid, the system requires admin confirmation
     * before applying the changes.
     * </p>
     *
     * @param userTable The {@code JTable} displaying the list of users.
     * @param tableModel The {@code DefaultTableModel} used to manage the user
     * table data.
     * @param userAuth The {@code UserAuthenticator} instance used for
     * authentication and validation.
     */
    public void editUser(JTable userTable, DefaultTableModel tableModel, UserAuthenticator userAuth) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "No user selected!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String role = (String) tableModel.getValueAt(modelRow, 3);
        if (role.equals("admin")) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Admin cannot be edited!", "Illegal action!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get current values
        String currentUsername = (String) tableModel.getValueAt(modelRow, 0);
        String currentEmail = (String) tableModel.getValueAt(modelRow, 1);
        String currentPassword = (String) tableModel.getValueAt(modelRow, 2);

        // Create Dialog
        JDialog dialog = new JDialog((Frame) null, "Change Details", true);
        dialog.setSize(450, 380);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(null);

        JTextField usernameField = new JTextField(currentUsername);
        JTextField emailField = new JTextField(currentEmail);
        JPasswordField passwordField = new JPasswordField(currentPassword);

        passwordField.setToolTipText(
                "<html><h3>The password must contain at least one of the following:</h3>"
                + "<h4>✅ 8 characters long</h4>"
                + "<h4>✅ An uppercase letter (A-Z)</h4>"
                + "<h4>✅ One number (0-9)</h4>"
                + "<h4>✅ A special character (!@#$%^&*)</h4>"
                + "</html>"
        );

        setInputBorderColor(usernameField, userNameBorderTitle, Color.BLACK);
        setInputBorderColor(emailField, emailBorderTitle, Color.BLACK);
        setInputBorderColor(passwordField, passwordBorderTitle, Color.BLACK);

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(Color.CYAN);
        saveButton.setFocusPainted(false);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFocusPainted(false);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(usernameField);
        panel.add(emailField);
        panel.add(passwordField);
        panel.add(saveButton);
        panel.add(cancelButton);
        dialog.add(panel);

        saveButton.setEnabled(false);

        DocumentListener inputListener = new DocumentListener() {
            void checkChanges() {
                boolean changed = !usernameField.getText().equals(currentUsername)
                        || !emailField.getText().equals(currentEmail)
                        || !new String(passwordField.getPassword()).equals(currentPassword);
                saveButton.setEnabled(changed);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkChanges();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkChanges();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkChanges();
            }
        };

        usernameField.getDocument().addDocumentListener(inputListener);
        emailField.getDocument().addDocumentListener(inputListener);
        passwordField.getDocument().addDocumentListener(inputListener);

        saveButton.addActionListener(e -> {
            if (validateInput(usernameField, emailField, passwordField, dialog)) {
                confirmAdminAndSave(userTable, tableModel, userAuth, modelRow, usernameField, emailField, passwordField, dialog);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * Validates user input fields for username, email, and password before
     * saving changes.
     * <p>
     * This method checks for various conditions:
     * <ul>
     * <li>Username must not be empty, must be at least 3 characters long, and
     * cannot contain numbers or special characters.</li>
     * <li>Email must be valid according to a predefined regex pattern.</li>
     * <li>Password must be at least 8 characters long and include:
     * <ul>
     * <li>At least one uppercase letter</li>
     * <li>At least one number</li>
     * <li>At least one special character</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     *
     * @param usernameField The {@code JTextField} containing the username
     * input.
     * @param emailField The {@code JTextField} containing the email input.
     * @param passwordField The {@code JPasswordField} containing the password
     * input.
     * @param dialog The {@code JDialog} used to display error messages if
     * validation fails.
     * @return {@code true} if all fields meet validation requirements;
     * otherwise, {@code false}.
     */
    private boolean validateInput(JTextField usernameField, JTextField emailField, JPasswordField passwordField, JDialog dialog) {
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPassword = new String(passwordField.getPassword()).trim();
        Pattern namePattern = Pattern.compile("[^a-zA-Z ]");

        Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(newEmail);
        Matcher passwordNumberMatcher = VALID_PASSWORD_REGEX_NUMBER_CHAR.matcher(newPassword);
        Matcher passwordUpperMatcher = VALID_PASSWORD_REGEX_UPPER_CHAR.matcher(newPassword);
        Matcher passwordSpecCharMatcher = VALID_PASSWORD_REGEX_SPECIAL_CHAR.matcher(newPassword);

        setInputBorderColor(usernameField, userNameBorderTitle, Color.BLACK);
        setInputBorderColor(emailField, emailBorderTitle, Color.BLACK);
        setInputBorderColor(passwordField, passwordBorderTitle, Color.BLACK);

        if (newUsername.isEmpty()) {
            setInputBorderColor(usernameField, userNameBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "Username cannot be empty!", usernameField);
            usernameField.grabFocus();
            return false;
        }
        if (newUsername.length() < 3) {
            setInputBorderColor(usernameField, userNameBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "Please enter the username correctly!", usernameField);
            usernameField.grabFocus();
            return false;
        }
        if (namePattern.matcher(newUsername).find()) {
            setInputBorderColor(usernameField, userNameBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "Special characters and numbers are not allowed in username", usernameField);
            usernameField.grabFocus();
            return false;
        }
        if (newEmail.isEmpty()) {
            setInputBorderColor(emailField, emailBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "Please enter the email!", emailField);
            emailField.grabFocus();
            return false;
        }
        if (!emailMatcher.matches()) {
            setInputBorderColor(emailField, emailBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "Please enter the email address correctly.", emailField);
            emailField.grabFocus();
            return false;
        }
        if (newPassword.isEmpty()) {
            setInputBorderColor(passwordField, passwordBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "Please enter the password!", passwordField);
            passwordField.grabFocus();
            return false;
        }
        if (newPassword.length() < 8) {
            setInputBorderColor(passwordField, passwordBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "Password must be at least 8 characters long!", passwordField);
            passwordField.grabFocus();
            return false;
        }
        if (!passwordUpperMatcher.matches()) {
            setInputBorderColor(passwordField, passwordBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "The password must at least one uppercase", passwordField);
            passwordField.grabFocus();
            return false;
        }
        if (!passwordNumberMatcher.matches()) {
            setInputBorderColor(passwordField, passwordBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "The password must at least one number", passwordField);
            passwordField.grabFocus();
            return false;
        }
        if (!passwordSpecCharMatcher.matches()) {
            setInputBorderColor(passwordField, passwordBorderTitle, Color.RED);
            Toolkit.getDefaultToolkit().beep();
            showError(dialog, "The password must at least one special character", passwordField);
            passwordField.grabFocus();
            return false;
        }
        return true;
    }

    /**
     * This method opens a dialog to add a new user to the system. The admin
     * must provide a username, email, and password, which are validated before
     * adding.
     *
     * @param userTable The JTable where the user data is displayed.
     * @param tableModel The DefaultTableModel associated with the userTable.
     * @param userAuth An instance of UserAuthenticator to verify admin
     * credentials.
     */
    public void addUser(JTable userTable, DefaultTableModel tableModel, UserAuthenticator userAuth) {

        JDialog dialog = new JDialog((Frame) null, "Add New User", true);
        dialog.setSize(450, 380);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField newUsernameField = new JTextField();
        JTextField newEmailField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();

        newPasswordField.setToolTipText(
                "<html><h3>The password must contain at least one of the following:</h3>"
                + "<h4>✅ 8 characters long</h4>"
                + "<h4>✅ An uppercase letter (A-Z)</h4>"
                + "<h4>✅ One number (0-9)</h4>"
                + "<h4>✅ A special character (!@#$%^&*)</h4>"
                + "</html>"
        );

        setInputBorderColor(newUsernameField, userNameBorderTitle, Color.BLACK);
        setInputBorderColor(newEmailField, emailBorderTitle, Color.BLACK);
        setInputBorderColor(newPasswordField, passwordBorderTitle, Color.BLACK);

        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        saveButton.setBackground(Color.cyan);
        saveButton.setFocusPainted(false);
        cancelButton.setFocusPainted(false);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(newUsernameField);
        panel.add(newEmailField);
        panel.add(newPasswordField);
        panel.add(saveButton);
        panel.add(cancelButton);

        dialog.add(panel);

        saveButton.setEnabled(false);

        DocumentListener inputListener = new DocumentListener() {
            void checkChanges() {
                boolean changed = !newUsernameField.getText().equals("")
                        || !newEmailField.getText().equals("")
                        || !new String(newPasswordField.getPassword()).equals("");
                saveButton.setEnabled(changed);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkChanges();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkChanges();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkChanges();
            }
        };

        newUsernameField.getDocument().addDocumentListener(inputListener);
        newEmailField.getDocument().addDocumentListener(inputListener);
        newPasswordField.getDocument().addDocumentListener(inputListener);

        saveButton.addActionListener(e -> {
            String newUsername = newUsernameField.getText().trim();
            String newEmail = newEmailField.getText().trim().toLowerCase();
            String newPassword = new String(newPasswordField.getPassword()).trim();

            setInputBorderColor(newUsernameField, userNameBorderTitle, Color.BLACK);
            setInputBorderColor(newEmailField, emailBorderTitle, Color.BLACK);
            setInputBorderColor(newPasswordField, passwordBorderTitle, Color.BLACK);

            if (!validateInput(newUsernameField, newEmailField, newPasswordField, dialog)) {
                return;
            }
            if (checkRegisteredUsers(newUsername, newEmail, newPassword)) {
                setInputBorderColor(newEmailField, emailBorderTitle, Color.RED);
                JOptionPane.showMessageDialog(null, "This user is already registered. Please try another one.", "Operation failed", JOptionPane.ERROR_MESSAGE);
                newEmailField.grabFocus();
            } else {
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to add new user?", "Confirm Changes", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    JPasswordField adminPasswordField = new JPasswordField();
                    setInputBorderColor(adminPasswordField, "Enter your password", Color.BLACK);
                    int adminPasswordConfirm = JOptionPane.showConfirmDialog(null, adminPasswordField, "🔑 Confirm Your Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (adminPasswordConfirm == JOptionPane.OK_OPTION) {
                        char[] getAdminPasswordField = adminPasswordField.getPassword();
                        String passwordToStringAdmin = new String(getAdminPasswordField);
                        if (!passwordToStringAdmin.trim().isEmpty() && userAuth.confirmUserAdminChanges(passwordToStringAdmin)) {
                            LocalDate currentDate = LocalDate.now();
                            tableModel.addRow(new Object[]{newUsername, newEmail, newPassword, "member", currentDate});
                            tableModel.fireTableDataChanged();
                            dialog.dispose();
                            saveUsersToFile(tableModel);
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(null, "New User added successfully!", "Action success!", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                            JOptionPane.showMessageDialog(null, "Incorrect admin password!", "Invalid Details", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * This method confirms admin credentials before to finish the CRUD
     * operations. This method prompts the admin for password verification
     * before applying changes.
     *
     * @param userTable The JTable displaying user data.
     * @param tableModel The DefaultTableModel associated with the userTable.
     * @param userAuth An instance of UserAuthenticator to verify admin
     * credentials.
     * @param modelRow The row index of the user in the table.
     * @param usernameField The JTextField containing the updated username.
     * @param emailField The JTextField containing the updated email.
     * @param passwordField The JPasswordField containing the updated password.
     * @param dialog The JDialog window for editing user details.
     */
    private void confirmAdminAndSave(JTable userTable, DefaultTableModel tableModel, UserAuthenticator userAuth, int modelRow,
            JTextField usernameField, JTextField emailField, JPasswordField passwordField, JDialog dialog) {
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to change details of this user?",
                "Confirm Changes", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JPasswordField adminPasswordField = new JPasswordField();
            setInputBorderColor(adminPasswordField, "Enter your password", Color.BLACK);

            int adminPasswordConfirm = JOptionPane.showConfirmDialog(null, adminPasswordField, "🔑 Confirm Your Password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (adminPasswordConfirm == JOptionPane.OK_OPTION) {
                String adminPassword = new String(adminPasswordField.getPassword()).trim();
                if (!adminPassword.isEmpty() && userAuth.confirmUserAdminChanges(adminPassword)) {
                    tableModel.setValueAt(usernameField.getText(), modelRow, 0);
                    tableModel.setValueAt(emailField.getText(), modelRow, 1);
                    tableModel.setValueAt(new String(passwordField.getPassword()), modelRow, 2);
                    tableModel.fireTableDataChanged();
                    dialog.dispose();
                    saveUsersToFile(tableModel);
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null, "User details changed successfully!", "Action success!", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null, "Incorrect admin password!", "Invalid Details", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * This method ensures that admin users cannot be deleted and requires an
     * admin password before removing the user. Upon successful deletion, the
     * updated user list is saved.
     *
     * @param userTable The JTable displaying the list of users.
     * @param tableModel The DefaultTableModel associated with the JTable.
     * @param userAuth An instance of UserAuthenticator to verify admin
     * credentials.
     */
    public void deleteUser(JTable userTable, DefaultTableModel tableModel, UserAuthenticator userAuth) {
        int selectedRow = userTable.getSelectedRow();
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String role = (String) tableModel.getValueAt(modelRow, 3);
        if (role.equals("admin")) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Admin cannot be deleted!", "Illegal action!", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Toolkit.getDefaultToolkit().beep();
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete this user?\nThis action CANNOT be undone!",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            JPasswordField adminPasswordField = new JPasswordField();
            setInputBorderColor(adminPasswordField, "Enter your password", Color.BLACK);
            int adminPasswordConfirm = JOptionPane.showConfirmDialog(null, adminPasswordField, "🔑 Confirm Your Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (adminPasswordConfirm == JOptionPane.OK_OPTION) {
                char[] getAdminPasswordField = adminPasswordField.getPassword();
                String passwordToStringAdmin = new String(getAdminPasswordField);
                if (!passwordToStringAdmin.trim().isEmpty() && userAuth.confirmUserAdminChanges(passwordToStringAdmin)) {
                    tableModel.removeRow(modelRow);
                    saveUsersToFile(tableModel);
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null, "User deleted successfully!", "Action success!", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null, "Incorrect admin password!", "Invalid Details", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private static void showError(JDialog dialog, String message, JComponent field) {
        JOptionPane.showMessageDialog(dialog, message, "Error", JOptionPane.ERROR_MESSAGE);
        field.grabFocus();
    }

    /**
     * Saves the new table data to file
     *
     * @param tableModel
     */
    private static void saveUsersToFile(DefaultTableModel tableModel) {
        String filePath = "./src/data/userManagement/manageableUsers.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String username = (String) tableModel.getValueAt(i, 0);
                String email = (String) tableModel.getValueAt(i, 1);
                String password = (String) tableModel.getValueAt(i, 2);
                String role = (String) tableModel.getValueAt(i, 3);
                Object createdAtObj = tableModel.getValueAt(i, 4);
                String createdAt = (createdAtObj instanceof LocalDate) ? createdAtObj.toString() : String.valueOf(createdAtObj);

                if (role.equalsIgnoreCase("admin")) {
                    continue;
                }

                String line = username + "," + email + "," + password + "," + role + "," + createdAt;
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            UserManagement.loadUsersFromFile();
        } catch (IOException e) {
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
     * This method checks if a user with the given email is already registered
     * in the manageable users file. The method reads the user data from a text
     * file and verifies if the provided email exists.
     *
     * @param username The username of the user (not used in the current
     * implementation).
     * @param email The email to check for existing registration.
     * @param password The password of the user (not used in the current
     * implementation).
     * @return {@code true} if the email is already registered, otherwise
     * {@code false}.
     */
    public boolean checkRegisteredUsers(String username, String email, String password) {
        List<String[]> loadedManageableUsers = readUsersFromFile("./src/data/userManagement/manageableUsers.txt");
        email = email.toLowerCase();

        boolean existedInManageable = false;

        if (loadedManageableUsers != null) {
            for (String[] user : loadedManageableUsers) {
                if (user[1].equalsIgnoreCase(email)) {
                    existedInManageable = true;
                    break;
                }
            }
        }
        return existedInManageable;
    }
}
