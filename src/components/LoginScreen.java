package components;

import auth.UserAuthenticator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;
import util.customBorder;
import util.customImageResizer;

/**
 * Copyright (c) 2025 Eric Russel M. Lopez
 *
 * This class represents the Login Screen Component UI.
 *
 * @component LoginScreen
 */
public class LoginScreen extends JPanel implements customBorder {

    customImageResizer customImageSize = new customImageResizer();

    private final App router;
    private JPanel panel = new JPanel(new GridLayout(8, 1, 10, 10));

    private Timer timer;
    private static JLabel errorMessageLabel;
    private static int attemptsPenalty = 1;
    private int counterToReAttempt = 30;
    private int timePenalty = (counterToReAttempt * attemptsPenalty);
    private static int loginCounter = 3;
    private static final String FILE_PATH_TIMER = "./src/components/LoginUtils/storedTimePenalty.txt";

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private ImageIcon titleIcon = new ImageIcon("./src/icons/user-solid.png");

    private JTextField emailField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    final String errMessageEmailBorderTitle = " ✉️ Email address";
    final String errMessagePasswordBorderTitle = " 🔒 Password";

    private JPanel emailPanel = new JPanel(new BorderLayout());
    private JPanel passwordPanel = new JPanel(new BorderLayout());

    private JButton loginButton = new JButton("Login");
    private JButton goToRegister = new JButton("Register");

    private ImageIcon showPasswordIcon = new ImageIcon("./src/icons/eye-regular.png");
    private ImageIcon hidePasswordIcon = new ImageIcon("./src/icons/eye-slash-regular.png");

    private Border loginButtonsRounded = new LineBorder(Color.gray, 1, true);

    /**
     * This is the constructor of the LoginScreen component. Initializes the
     * user interface.
     *
     * @param router The application router used for navigating between screens.
     */
    public LoginScreen(App router) {
        setLayout(new GridBagLayout());

        this.router = router;

        panel.setPreferredSize(new Dimension(400, 500));

        errorMessageLabel = new JLabel("", JLabel.CENTER);
        errorMessageLabel.setFont(new Font("", Font.ITALIC, 18));
        errorMessageLabel.setForeground(Color.RED);

        titleIcon = customImageSize.resizeIcon(titleIcon, 30, 30);
        JLabel title = new JLabel(" Admin login", titleIcon, JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));

        setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.black);
        setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.black);

        emailField.setOpaque(false);
        passwordField.setOpaque(false);
        emailField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        emailField.setFont(new Font(null, Font.PLAIN, 15));
        passwordField.setFont(new Font(null, Font.PLAIN, 15));

        loginButton.setBorder(loginButtonsRounded);
        loginButton.setBackground(Color.cyan);
        loginButton.setFocusPainted(false);
        goToRegister.setBorder(loginButtonsRounded);
        goToRegister.setFocusPainted(false);
        loginButton.setFont(new Font(null, Font.BOLD, 13));
        goToRegister.setFont(new Font(null, Font.BOLD, 13));
        goToRegister.setToolTipText("Create an account");

        showPasswordIcon = customImageSize.resizeIcon(showPasswordIcon, 16, 16);
        hidePasswordIcon = customImageSize.resizeIcon(hidePasswordIcon, 16, 16);

        ToolTipManager.sharedInstance().setInitialDelay(200);

        JButton toggleShowHidePasswordButton = new JButton(hidePasswordIcon);
        toggleShowHidePasswordButton.setPreferredSize(new Dimension(30, 30));
        toggleShowHidePasswordButton.setBorder(null);
        toggleShowHidePasswordButton.setContentAreaFilled(false);
        toggleShowHidePasswordButton.setFocusPainted(false);
        toggleShowHidePasswordButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        toggleShowHidePasswordButton.setToolTipText("Show password");

        // Show / Hide password toggle function
        toggleShowHidePasswordButton.addActionListener(new ActionListener() {
            private boolean isPasswordVisible = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPasswordVisible) {
                    passwordField.setEchoChar('•');
                    toggleShowHidePasswordButton.setIcon(hidePasswordIcon);
                    toggleShowHidePasswordButton.setToolTipText("Show password");
                } else {
                    passwordField.setEchoChar((char) 0);
                    toggleShowHidePasswordButton.setIcon(showPasswordIcon);
                    toggleShowHidePasswordButton.setToolTipText("Hide password");
                }
                isPasswordVisible = !isPasswordVisible;
            }
        });

        emailPanel.add(emailField, BorderLayout.CENTER);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toggleShowHidePasswordButton, BorderLayout.EAST);

        loginButton.addActionListener(e -> {
            loginKeyPressed();

            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            if (!loginValidator(email.toLowerCase(), password)) {
                return;
            }

            boolean isAuthenticated = new UserAuthenticator().login("", email.toLowerCase(), password);

            runLoginTask(isAuthenticated, loginButton, passwordField);
        });

        goToRegister.addActionListener(e -> {
            resetLoginForm();
        });

        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(title);
        panel.add(errorMessageLabel);
        panel.add(emailPanel);
        panel.add(passwordPanel);
        panel.add(loginButton);
        panel.add(goToRegister);
        add(panel);

        restoreTimePenalty();
        loginTimerPenaltyStart();
    }

    /**
     * This method detects if the input fields has focused. If so, it focuses on
     * the login button and allows the user to press the Enter key to submit the
     * login form instead of clicking the button.
     */
    private void loginKeyPressed() {
        Object[] loginFieldsKeyFocused = {emailField, passwordField};

        for (Object field : loginFieldsKeyFocused) {
            ((JComponent) field).getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");

            ((JComponent) field).getActionMap().put("enterPressed", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginButton.doClick();
                }
            });
        }
    }

    /**
     * This timer method applies a penalty by disabling the login button after 3
     * times of failed login attempts
     *
     * @TimePenaltyCounter
     */
    private void loginTimerPenaltyStart() {
        timer = new Timer(1000, e -> {
            if (timePenalty > 0) {
                updateTime();
                timePenalty--;
                saveTimePenalty(timePenalty);
                loginButton.setEnabled(false);
            } else {
                timer.stop();
                resetTimer();
            }
        });
        timer.start();
    }

    /**
     * This reset timer method resets the timer to default, but it increase the
     * (attemptsPenalty * to counter -> default to 30s)
     *
     * @TimePenaltyCounter
     */
    private void resetTimer() {
        loginCounter = 3;
        timePenalty = (counterToReAttempt * attemptsPenalty);
        saveTimePenalty(0);
        showError("");
        loginButton.setEnabled(true);
        attemptsPenalty++;
    }

    /**
     * @TimePenaltyCounter This saveTimePenalty saves current penalty time to a
     * text file to ensure the timer continues counting even if the user closes
     * the window before the timer ends. The time is restored when reopening the
     * application.
     */
    private void saveTimePenalty(int time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH_TIMER))) {
            writer.write(String.valueOf(time));
        } catch (IOException e) {
        }
    }

    /**
     * @restoreTimePenalty This restoreTimePenalty restores time from a text
     * file to ensure the timer continues counting when user reoponed the
     * window.
     */
    private void restoreTimePenalty() {
        if (Files.exists(Paths.get(FILE_PATH_TIMER))) {
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH_TIMER))) {
                String line = reader.readLine();
                if (line != null) {
                    timePenalty = Integer.parseInt(line);
                }
            } catch (IOException | NumberFormatException e) {
            }
        }
    }

    private void updateTime() {
        showError("Too many attempts! Try again later (" + timePenalty + "s)");
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
    }

    /**
     * This method validates the user's email and password input for login.
     * Ensures that the email field is not empty, follows a valid format, and
     * that the password field is not empty.
     *
     * @param email The email address entered by the user.
     * @param password The password entered by the user.
     * @return {@code true} if both inputs are valid, {@code false} otherwise.
     */
    private boolean loginValidator(String email, String password) {

        Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);

        if (email.isEmpty()) {
            showError("Please enter your email address");
            setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.RED);
            emailField.grabFocus();
            return false;
        }
        if (!emailMatcher.matches()) {
            showError("Please enter your email address correctly.");
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
        setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.RED);
        setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.BLACK);
        return true;
    }

    /**
     * Executes the login process asynchronously using SwingWorker. It disables
     * input fields during the login attempt, simulates a delay, and handles
     * authentication success or failure.
     *
     * @param isAuthenticated {@code true} if the user is successfully
     * authenticated, {@code false} otherwise.
     * @param loginButton The button that triggers the login process.
     * @param passwordField The password input field.
     *
     * This method ensures UI modifications are performed on the Event Dispatch
     * Thread (EDT) using {@link SwingUtilities#invokeLater(Runnable)}.
     */
    private void runLoginTask(boolean isAuthenticated, JButton loginButton, JPasswordField passwordField) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    showError("");
                    setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.black);
                    emailField.setEnabled(false);
                    passwordField.setEnabled(false);
                    loginButton.setEnabled(false);
                    goToRegister.setEnabled(false);
                    loginButton.setText("Logging in...");
                });

                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    loginButton.setText("Login");
                    emailField.setEnabled(true);
                    passwordField.setEnabled(true);
                    loginButton.setEnabled(true);
                    goToRegister.setEnabled(true);
                    passwordField.setText("");
                    passwordField.grabFocus();

                    if (!isAuthenticated) {
                        loginCounter--;
                        if (loginCounter <= 0) {
                            updateTime();
                            loginButton.setEnabled(false);
                            timer.start();
                        }
                        setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.red);
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(null, "Incorrect email or password", "Invalid details", JOptionPane.ERROR_MESSAGE);
                    } else {
                        attemptsPenalty = 1;
                        loginCounter = 3;
                        timePenalty = (counterToReAttempt * attemptsPenalty);
                        router.showPage("USER MANAGEMENT", "RusByte Net - User Management");
                    }
                });
            }
        }.execute();
    }

    /**
     * Resets the login form
     */
    private void resetLoginForm() {
        if (!emailField.getText().isEmpty()) {
            int userRouteChoice = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to leave now? Your form data will be lost.",
                    "Going to Register",
                    JOptionPane.YES_NO_OPTION
            );

            if (userRouteChoice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        emailField.setText("");
        passwordField.setText("");

        setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.BLACK);
        setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.BLACK);

        if (loginCounter != 0) {
            showError("");
        }
        router.showPage("REGISTER", "RusByte Net - Register admin");
    }
}
