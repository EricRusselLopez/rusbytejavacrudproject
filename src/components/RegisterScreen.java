package components;

import auth.UserAuthenticator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import util.customBorder;
import util.customImageResizer;

/**
 * Copyright (c) 2025 Eric Russel M. Lopez
 *
 * This class represents the Register Screen Component UI.
 *
 * @component RegisterScreen
 */
public class RegisterScreen extends JPanel implements customBorder {

    customImageResizer customImageSize = new customImageResizer();

    private final App router;

    private JPanel panel = new JPanel(new GridLayout(8, 1, 10, 10));

    private ImageIcon titleIcon = new ImageIcon("./src/icons/user-solid.png");

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static final Pattern VALID_PASSWORD_REGEX_NUMBER_CHAR = Pattern.compile(".*[0-9].*");

    private static final Pattern VALID_PASSWORD_REGEX_UPPER_CHAR = Pattern.compile(".*[A-Z].*");

    private static final Pattern VALID_PASSWORD_REGEX_SPECIAL_CHAR = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    private JLabel errorMessageLabel;

    private final String errMessageUserNameBorderTitle = " 👤 Username";
    private final String errMessageEmailBorderTitle = " ✉️ Email address";
    private final String errMessagePasswordBorderTitle = " 🔒 Password";
    private final String errMessageConfirmPasswordBorderTitle = " 🔒 Confirm your password";

    private JButton registerButton = new JButton("Register");
    private JButton backToLogin = new JButton("Login");

    private JTextField usernameField = new JTextField();
    private JTextField emailField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JPasswordField confirmJPasswordField = new JPasswordField();

    private JPanel usernamePanel = new JPanel(new BorderLayout());
    private JPanel emailPanel = new JPanel(new BorderLayout());
    private JPanel passwordPanel = new JPanel(new BorderLayout());
    private JPanel confirmPasswordPanel = new JPanel(new BorderLayout());

    /**
     * This is the constructor of the RegisterScreen component. Initializes the
     * user interface.
     *
     * @param router The application router used for navigating between screens.
     */
    public RegisterScreen(App router) {

        this.router = router;

        setLayout(new GridBagLayout());

        panel.setPreferredSize(new Dimension(400, 470));
        titleIcon = customImageSize.resizeIcon(titleIcon, 30, 30);
        JLabel title = new JLabel(" Register as admin", titleIcon, JLabel.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 20));

        errorMessageLabel = new JLabel("", JLabel.CENTER);
        errorMessageLabel.setFont(new Font("", Font.ITALIC, 18));
        errorMessageLabel.setForeground(Color.RED);

        setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.black);
        setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.black);
        setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.black);
        setInputBorderColor(confirmPasswordPanel, errMessageConfirmPasswordBorderTitle, Color.black);

        usernameField.setOpaque(false);
        usernameField.setBorder(BorderFactory.createEmptyBorder());
        usernameField.setFont(new Font(null, Font.PLAIN, 15));

        emailField.setOpaque(false);
        emailField.setBorder(BorderFactory.createEmptyBorder());
        emailField.setFont(new Font(null, Font.PLAIN, 15));

        passwordField.setOpaque(false);
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setFont(new Font(null, Font.PLAIN, 15));

        confirmJPasswordField.setOpaque(false);
        confirmJPasswordField.setBorder(BorderFactory.createEmptyBorder());
        confirmJPasswordField.setFont(new Font(null, Font.PLAIN, 15));

        usernamePanel.add(usernameField, BorderLayout.CENTER);
        emailPanel.add(emailField, BorderLayout.CENTER);

        JButton toolTipPasswordReq = new JButton(customImageSize.resizeIcon(new ImageIcon("./src/icons/circle-question-solid.png"), 15, 15));
        toolTipPasswordReq.setBorder(null);
        toolTipPasswordReq.setContentAreaFilled(false);
        toolTipPasswordReq.setFocusPainted(false);
        toolTipPasswordReq.setToolTipText(
                "<html><h3>Your password must contain at least one of the following:</h3>"
                + "<h4>✅ 8 characters long</h4>"
                + "<h4>✅ An uppercase letter (A-Z)</h4>"
                + "<h4>✅ One number (0-9)</h4>"
                + "<h4>✅ A special character (!@#$%^&*)</h4>"
                + "</html>"
        );
        toolTipPasswordReq.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 5));

        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toolTipPasswordReq, BorderLayout.EAST);
        confirmPasswordPanel.add(confirmJPasswordField, BorderLayout.CENTER);

        registerButton.setBackground(Color.cyan);
        registerButton.setFocusPainted(false);
        backToLogin.setFocusPainted(false);

        registerButton.addActionListener(e -> {
            registerKeyPressed();

            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmJPasswordField.getPassword());

            if (!registerValidator(username, email.toLowerCase(), password, confirmPassword)) {
                return;
            }

            boolean isRegisterAuthenticated = new UserAuthenticator().registerUser(username, email, password);

            runRegisterTask(isRegisterAuthenticated, registerButton, passwordField);
        });

        backToLogin.addActionListener(e -> resetRegisterForm());

        panel.add(title);
        panel.add(errorMessageLabel);
        panel.add(usernamePanel);
        panel.add(emailPanel);
        panel.add(passwordPanel);
        panel.add(confirmPasswordPanel);
        panel.add(registerButton);
        panel.add(backToLogin);

        add(panel);
    }

    /**
     * Assigns the Enter key as a trigger for the register button. When the
     * Enter key is pressed while focusing on any input field, it simulates a
     * click on the register button.
     */
    private void registerKeyPressed() {
        Object[] loginFieldsKeyFocused = {usernameField, emailField, passwordField, confirmJPasswordField};

        for (Object field : loginFieldsKeyFocused) {
            ((JComponent) field).getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");

            ((JComponent) field).getActionMap().put("enterPressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    registerButton.doClick();
                }
            });
        }
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
    }

    /**
     * <p>
     * This method displays an error message and highlights the corresponding
     * field if validation fails.</p>
     *
     * @param username The username entered by the user. It must not be empty
     * and cannot contain special characters or numbers.
     * @param email The email address entered by the user. It must be in a valid
     * email format.
     * @param password The password entered by the user. It must be at least 8
     * characters long, contain an uppercase letter, a number, and a special
     * character.
     * @param confirmpassword The password confirmation entered by the user. It
     * must match the original password.
     * @return {@code true} if all input fields are valid, otherwise
     * {@code false}.
     */
    private boolean registerValidator(String username, String email, String password, String confirmpassword) {

        Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        Matcher passwordNumberMatcher = VALID_PASSWORD_REGEX_NUMBER_CHAR.matcher(password);
        Matcher passwordUpperMatcher = VALID_PASSWORD_REGEX_UPPER_CHAR.matcher(password);
        Matcher passwordSpecCharMatcher = VALID_PASSWORD_REGEX_SPECIAL_CHAR.matcher(password);
        String regex = "[^a-zA-Z ]";
        Pattern pattern = Pattern.compile(regex);
        Matcher usernameMatcher = pattern.matcher(username);

        if (username.isEmpty()) {
            showError("What's your name?");
            setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.RED);
            usernameField.grabFocus();
            return false;
        }
        if (username.length() < 3) {
            showError("Invalid username");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Please enter your username correctly.", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.RED);
            usernameField.grabFocus();
            return false;
        }
        if (usernameMatcher.find()) {
            showError("Invalid username");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Special characters and numbers are not allowed", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.RED);
            usernameField.grabFocus();
            return false;
        }
        if (email.isEmpty()) {
            showError("Please enter your email address");
            setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.RED);
            setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.BLACK);
            emailField.grabFocus();
            return false;
        }
        if (!emailMatcher.matches()) {
            showError("Invalid email");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Please enter your email address correctly.", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.RED);
            emailField.grabFocus();
            return false;
        }
        if (password.isEmpty()) {
            showError("Please enter your password");
            setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.RED);
            setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.BLACK);
            passwordField.grabFocus();
            return false;
        }
        if (password.length() < 8) {
            showError("Invalid password");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Your password must at least 8 characters long", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.RED);
            setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.BLACK);
            passwordField.grabFocus();
            return false;
        }
        if (!passwordUpperMatcher.matches()) {
            showError("Invalid password");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Your password must at least one uppercase", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.RED);
            passwordField.grabFocus();
            return false;
        }
        if (!passwordNumberMatcher.matches()) {
            showError("Invalid password");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Your password must at least one number", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.RED);
            passwordField.grabFocus();
            return false;
        }
        if (!passwordSpecCharMatcher.matches()) {
            showError("Invalid password");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Your password must at least one special character", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.RED);
            passwordField.grabFocus();
            return false;
        }
        if (confirmpassword.isEmpty()) {
            showError("Please confirm your password");
            setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.BLACK);
            setInputBorderColor(confirmPasswordPanel, errMessageConfirmPasswordBorderTitle, Color.RED);
            confirmJPasswordField.grabFocus();
            return false;
        }
        if (!confirmpassword.equals(password)) {
            showError("Invalid password");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "Your passwords do not match. Please try again", "Invalid details", JOptionPane.ERROR_MESSAGE);
            setInputBorderColor(confirmPasswordPanel, errMessageConfirmPasswordBorderTitle, Color.RED);
            confirmJPasswordField.grabFocus();
            return false;
        }
        setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.BLACK);
        setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.BLACK);
        setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.BLACK);
        setInputBorderColor(confirmPasswordPanel, errMessageConfirmPasswordBorderTitle, Color.BLACK);
        return true;
    }

    /**
     * This method Runs an asynchronous register proccess task using a
     * {@link SwingWorker}.
     * <p>
     * This method performs UI updates to display a loading overlay while
     * delaying the register proccess for 2 seconds to simulate a processing
     * effect.
     * </p>
     *
     * The method ensures UI modifications are performed on the Event Dispatch
     * Thread (EDT) using {@link SwingUtilities#invokeLater(Runnable)}.
     */
    private void runRegisterTask(boolean registerIsAuthenticated, JButton registerButton, JPasswordField passwordField) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    showError("");
                    setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.black);
                    usernameField.setEnabled(false);
                    emailField.setEnabled(false);
                    passwordField.setEnabled(false);
                    confirmJPasswordField.setEnabled(false);
                    registerButton.setEnabled(false);
                    backToLogin.setEnabled(false);
                    registerButton.setText("Creating your account...");
                });

                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    if (registerIsAuthenticated) {
                        showError("");
                        usernameField.setText("");
                        emailField.setText("");
                        passwordField.setText("");
                        confirmJPasswordField.setText("");

                        registerButton.setText("Register");
                        usernameField.setEnabled(true);
                        emailField.setEnabled(true);
                        passwordField.setEnabled(true);
                        confirmJPasswordField.setEnabled(true);
                        registerButton.setEnabled(true);
                        backToLogin.setEnabled(true);
                        passwordField.setText("");
                        passwordField.grabFocus();

                        Object[] options = {"Login now", "OK"};
                        int choice = JOptionPane.showOptionDialog(
                                null,
                                "Account created successfully!",
                                "Register success",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                options,
                                null
                        );

                        if (choice == 0) {
                            router.showPage("LOGIN", "RusByte Net - User Management");
                        }
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(null, "This email is already in use. Please try another one.", "Registration failure", JOptionPane.ERROR_MESSAGE);

                        usernameField.setEnabled(true);
                        emailField.setEnabled(true);
                        setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.RED);
                        emailField.grabFocus();
                        passwordField.setEnabled(true);
                        confirmJPasswordField.setEnabled(true);
                        registerButton.setEnabled(true);
                        backToLogin.setEnabled(true);
                        registerButton.setText("Register");
                    }

                });
            }
        }.execute();
    }

    /**
     * Resets the registration form
     */
    private void resetRegisterForm() {
        if (!usernameField.getText().isEmpty() || !emailField.getText().isEmpty() || !passwordField.getText().isEmpty()) {
            int userRouteChoice = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to leave now? Your form data will be lost.",
                    "Going to Login",
                    JOptionPane.YES_NO_OPTION
            );

            if (userRouteChoice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        showError("");
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmJPasswordField.setText("");

        setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.black);
        setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.black);
        setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.black);
        setInputBorderColor(confirmPasswordPanel, errMessageConfirmPasswordBorderTitle, Color.black);

        router.showPage("LOGIN", "RusByte Net - User Management");
    }
}
