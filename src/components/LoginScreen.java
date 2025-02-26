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

public class LoginScreen extends JPanel {

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

    @SuppressWarnings({"", "unused"})
    public LoginScreen(App router) {

        setLayout(new GridBagLayout());

        this.router = router;

        panel.setPreferredSize(new Dimension(400, 500));

        errorMessageLabel = new JLabel("", JLabel.CENTER);
        errorMessageLabel.setFont(new Font("", Font.ITALIC, 18));
        errorMessageLabel.setForeground(Color.RED);

        titleIcon = resizeIcon(titleIcon, 30, 30);
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

        showPasswordIcon = resizeIcon(showPasswordIcon, 16, 16);
        hidePasswordIcon = resizeIcon(hidePasswordIcon, 16, 16);

        ToolTipManager.sharedInstance().setInitialDelay(200);

        JButton toggleShowHidePasswordButton = new JButton(hidePasswordIcon);
        toggleShowHidePasswordButton.setPreferredSize(new Dimension(30, 30));
        toggleShowHidePasswordButton.setBorder(null);
        toggleShowHidePasswordButton.setContentAreaFilled(false);
        toggleShowHidePasswordButton.setFocusPainted(false);
        toggleShowHidePasswordButton.setToolTipText("Show password");

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

    private void loginTimerPenaltyStart() {
        timer = new Timer(1000, _ -> {
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

    private void resetTimer() {
        loginCounter = 3;
        timePenalty = (counterToReAttempt * attemptsPenalty);
        saveTimePenalty(0);
        showError("");
        loginButton.setEnabled(true);
        attemptsPenalty++;
    }

    private void saveTimePenalty(int time) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH_TIMER))) {
            writer.write(String.valueOf(time));
        } catch (IOException e) {
        }
    }

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

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private void setInputBorderColor(JPanel panel, String title, Color color) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 1),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(null, Font.PLAIN, 17),
                color
        );
        panel.setBorder(border);
    }

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
