package components;

import auth.UserAuthenticator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class RegisterScreen extends JPanel {

    private final App router;

    private JPanel panel = new JPanel(new GridLayout(8, 1, 10, 10));

    private ImageIcon titleIcon = new ImageIcon("./src/icons/user-solid.png");

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private JLabel errorMessageLabel;

    private final String errMessageUserNameBorderTitle = " 👤 Username";
    private final String errMessageEmailBorderTitle = " ✉️ Email address";
    private final String errMessagePasswordBorderTitle = " 🔒 Password";
    private final String errMessageConfirmPasswordBorderTitle = " 🔒 Please confirm your password";

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

    public RegisterScreen(App router) {

        this.router = router;

        setLayout(new GridBagLayout());

        panel.setPreferredSize(new Dimension(400, 470));
        titleIcon = resizeIcon(titleIcon, 30, 30);
        JLabel title = new JLabel(" Register as admin", titleIcon, JLabel.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 20));

        errorMessageLabel = new JLabel("", JLabel.CENTER);
        errorMessageLabel.setFont(new Font("", Font.ITALIC, 16));
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
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        confirmPasswordPanel.add(confirmJPasswordField, BorderLayout.CENTER);

        registerButton.setBackground(Color.cyan);
        registerButton.setFocusPainted(false);
        backToLogin.setFocusPainted(false);

        registerButton.addActionListener(_ -> {
            registerKeyPressed();

            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmJPasswordField.getPassword());

            if (!registerValidator(username, email.toLowerCase(), password, confirmPassword)) {
                return;
            }

            boolean isRegisterAuthenticated = new UserAuthenticator().registerUser(username, email, password);

            runLoginTask(isRegisterAuthenticated, registerButton, passwordField);
        });

        backToLogin.addActionListener(_ -> resetRegisterForm());

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

    private boolean registerValidator(String username, String email, String password, String confirmpassword) {

        Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        String regex = "[^a-zA-Z ]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);

        if (username.isEmpty()) {
            showError("What's your name?");
            setInputBorderColor(usernamePanel, errMessageUserNameBorderTitle, Color.RED);
            usernameField.grabFocus();
            return false;
        }
        if (matcher.find()) {
            showError("Special characters and numbers are not allowed");
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
        if (password.length() < 8) {
            showError("Your password must at least 8 characters long");
            setInputBorderColor(passwordPanel, errMessagePasswordBorderTitle, Color.RED);
            setInputBorderColor(emailPanel, errMessageEmailBorderTitle, Color.BLACK);
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
            showError("Your passwords do not match. Please try again");
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

    private void runLoginTask(boolean registerIsAuthenticated, JButton registerButton, JPasswordField passwordField) {
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

                Thread.sleep(3000);
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
                        showError("The email address you entered is already exist.\n Please try other");

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

    @SuppressWarnings("deprecation")
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

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}
