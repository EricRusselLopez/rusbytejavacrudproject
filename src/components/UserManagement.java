package components;

import auth.UserAuthenticator;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("FieldMayBeFinal")
public final class UserManagement extends JPanel {

    private final App router;

    private final GridBagConstraints gbc = new GridBagConstraints();
    private JLabel logoLabel, loadingCenter, usernameSessionLabel, emailSessionLabel, roleSessionLabel;
    private JPanel navbar, leftPanel, rightPanel, contentPanel, overlayPanel, leftContentPanel, rightContentPanel, leftContentPanelChild;
    private JButton menuButton, addUserBtn;
    private JPopupMenu dropdownMenu;
    private final JMenuItem logoutItem;
    private final ImageIcon logoIcon, menuIcon;
    private JPanel topTablePanel;
    private JLabel totalUsersLabelDisplay, totalUserAdmins, totalUserMembers;
    private DefaultTableModel tableModel;
    private JTable userTable;
    private JTextField searchField;
    TableRowSorter<DefaultTableModel> sorter;

    UserAuthenticator userAuth = new UserAuthenticator();

    @SuppressWarnings("unused")
    public UserManagement(App router) {

        this.router = router;

        if (!userAuth.isAuthenticated()) {
            userAuth.logout();
        }

        setLayout(new BorderLayout());

        logoIcon = resizeIcon(new ImageIcon("./src/assets/rusbytefaviconmain.png"), 35, 35);
        menuIcon = resizeIcon(new ImageIcon("./src/icons/bars-solid.png"), 20, 20);

        navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Color.LIGHT_GRAY);
        navbar.setPreferredSize(new Dimension(600, 50));
        navbar.setBorder(new MatteBorder(1, 0, 1, 0, Color.DARK_GRAY));

        leftPanel = new JPanel();
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        logoLabel = new JLabel(logoIcon);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        logoLabel.setToolTipText("RusByte Net");
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createVerticalStrut(10));

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        menuButton = new JButton(menuIcon);
        menuButton.setFont(new Font("Arial", Font.BOLD, 18));
        menuButton.setFocusPainted(false);
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);

        dropdownMenu = new JPopupMenu("Menu");
        dropdownMenu.setPreferredSize(new Dimension(105, 40));

        logoutItem = new JMenuItem("↪  Log Out");
        logoutItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "↪  Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                runLoginTask();
            }
        });

        dropdownMenu.add(logoutItem);
        menuButton.addActionListener(e -> dropdownMenu.show(menuButton, -60, menuButton.getHeight()));

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(menuButton);
        rightPanel.add(Box.createVerticalStrut(10));

        navbar.add(leftPanel, BorderLayout.WEST);
        navbar.add(rightPanel, BorderLayout.EAST);

        // -------- Main Contents -------- //
        contentPanel = new JPanel(new BorderLayout());

        leftContentPanel = new JPanel();
        leftContentPanel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, Color.BLACK),
                new EmptyBorder(0, 0, 0, 0)
        ));
        leftContentPanelChild = new JPanel(new GridLayout(3, 1));
        leftContentPanelChild.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(20, 20, 20, 20)
        ));
        leftContentPanelChild.setPreferredSize(new Dimension(240, 150));

        usernameSessionLabel = new JLabel(resizeIcon(new ImageIcon("./src/icons/user-solid.png"), 25, 25), JLabel.LEFT);
        usernameSessionLabel.setFont(new Font("Arial", Font.BOLD, 28));
        emailSessionLabel = new JLabel(resizeIcon(new ImageIcon("./src/icons/envelope-solid.png"), 25, 25), JLabel.LEFT);
        emailSessionLabel.setFont(new Font("Arial", Font.ITALIC, 15));
        roleSessionLabel = new JLabel("Admin");
        roleSessionLabel.setFont(new Font(null, Font.BOLD, 15));

        // totalUsersLabelDisplay = new JLabel();
        // totalUserMembers = new JLabel();
        // totalUserAdmins = new JLabel();
        leftContentPanelChild.add(usernameSessionLabel);
        leftContentPanelChild.add(emailSessionLabel);
        leftContentPanelChild.add(roleSessionLabel);

        // leftContentPanelChild.add(totalUsersLabelDisplay);
        // leftContentPanelChild.add(totalUserMembers);
        // leftContentPanelChild.add(totalUserAdmins);
        leftContentPanel.add(leftContentPanelChild);

        rightContentPanel = new JPanel(new BorderLayout());

        topTablePanel = new JPanel(new BorderLayout());
        topTablePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        String[] columnNames = {"Username", "Email", "Password", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                int modelRow = userTable.convertRowIndexToModel(row);
                return false;
            }
        };

        userTable = new JTable(tableModel);

        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(100, 45));
        tableHeader.setFont(new Font(null, Font.BOLD, 20));
        tableHeader.setEnabled(false);

        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.setRowHeight(40);
        userTable.setFont(new Font(null, Font.ITALIC, 18));
        usersTableSearchFilter();

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setPreferredSize(new Dimension(150, 60));

        addUserBtn.addActionListener(e -> addUser());
        JMenuItem editItem = new JMenuItem("Edit");

        editItem.addActionListener(e -> editUser());

        JMenuItem deleteItem = new JMenuItem("Delete");

        deleteItem.addActionListener(e -> deleteUser());

        popupMenu.add(editItem);
        popupMenu.add(deleteItem);

        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int viewRow = userTable.rowAtPoint(e.getPoint());
                    if (viewRow != -1) {
                        int modelRow = userTable.convertRowIndexToModel(viewRow);
                        userTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

        });

        JScrollPane scrollPane = new JScrollPane(userTable);

        topTablePanel.add(scrollPane, BorderLayout.CENTER);

        rightContentPanel.add(topTablePanel, BorderLayout.CENTER);
        contentPanel.add(leftContentPanel, BorderLayout.WEST);
        contentPanel.add(rightContentPanel, BorderLayout.CENTER);

        // -------- End of Main Contents -------- // 
        add(navbar, BorderLayout.NORTH);
        add(contentPanel, FlowLayout.CENTER);
    }

    @SuppressWarnings("unused")
    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String role = (String) tableModel.getValueAt(modelRow, 3);
        if (selectedRow != -1 && !role.equals("admin")) {

            String currentUsername = (String) tableModel.getValueAt(modelRow, 0);
            String currentEmail = (String) tableModel.getValueAt(modelRow, 1);
            String currentPassword = (String) tableModel.getValueAt(modelRow, 2);

            JDialog dialog = new JDialog((Frame) null, "Change Details", true);
            dialog.setSize(400, 350);
            dialog.setResizable(false);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JTextField newUsernameField = new JTextField(currentUsername);
            JTextField newEmailField = new JTextField(currentEmail);
            JPasswordField newPasswordField = new JPasswordField(currentPassword);

            setInputBorderColor(newUsernameField, "Username", Color.BLACK);
            setInputBorderColor(newEmailField, "Email address", Color.BLACK);
            setInputBorderColor(newPasswordField, "Password", Color.BLACK);

            JButton saveButton = new JButton("Save Changes");
            JButton cancelButton = new JButton("Cancel");
            saveButton.setBackground(Color.cyan);
            saveButton.setFocusPainted(false);
            cancelButton.setFocusPainted(false);

            JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
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
                    boolean changed = !newUsernameField.getText().equals(currentUsername)
                            || !newEmailField.getText().equals(currentEmail)
                            || !new String(newPasswordField.getPassword()).equals(currentPassword);
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
                String newEmail = newEmailField.getText().trim();
                String newPassword = new String(newPasswordField.getPassword()).trim();

                final Pattern VALID_EMAIL_ADDRESS_REGEX
                        = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
                Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(newEmail);
                String regex = "[^a-zA-Z ]";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(newUsername);

                if (newUsername.isEmpty()) {
                    setInputBorderColor(newUsernameField, "Username", Color.RED);
                    JOptionPane.showMessageDialog(dialog, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    newUsernameField.grabFocus();
                    return;
                }
                if (matcher.find()) {
                    setInputBorderColor(newUsernameField, "Username", Color.RED);
                    JOptionPane.showMessageDialog(dialog, "Special characters and numbers are not allowed", "Error", JOptionPane.ERROR_MESSAGE);
                    newUsernameField.grabFocus();
                    return;
                }
                if (newEmail.isEmpty()) {
                    setInputBorderColor(newEmailField, "Email address", Color.RED);
                    JOptionPane.showMessageDialog(dialog, "Email cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    newEmailField.grabFocus();
                    return;
                }
                if (!emailMatcher.matches()) {
                    setInputBorderColor(newEmailField, "Email address", Color.RED);
                    JOptionPane.showMessageDialog(dialog, "Please enter the email address correctly.", "Error", JOptionPane.ERROR_MESSAGE);
                    newEmailField.grabFocus();
                    return;
                }
                if (newPassword.isEmpty()) {
                    setInputBorderColor(newPasswordField, "Password", Color.RED);
                    JOptionPane.showMessageDialog(dialog, "Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    newPasswordField.grabFocus();
                    return;
                }
                if (newPassword.length() < 8) {
                    setInputBorderColor(newPasswordField, "Password", Color.RED);
                    JOptionPane.showMessageDialog(dialog, "The password must at least 8 characters long", "Error", JOptionPane.ERROR_MESSAGE);
                    newPasswordField.grabFocus();
                    return;
                }
                setInputBorderColor(newUsernameField, "Username", Color.BLACK);
                setInputBorderColor(newEmailField, "Email Email address", Color.BLACK);
                setInputBorderColor(newPasswordField, "Password", Color.BLACK);

                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to change details of this user?", "Confirm Changes", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String adminPassword = JOptionPane.showInputDialog(null, "To confirm changes, please enter your password:", "Enter your credentials", JOptionPane.QUESTION_MESSAGE);
                    if (!adminPassword.trim().isEmpty()) {

                        boolean adminConfirmResult = userAuth.confirmUserAdminChanges(adminPassword);
                        if (adminConfirmResult) {
                            tableModel.setValueAt(newUsername, modelRow, 0);
                            tableModel.setValueAt(newEmail, modelRow, 1);
                            tableModel.setValueAt(newPassword, modelRow, 2);

                            tableModel.fireTableDataChanged();
                            dialog.dispose();
                            saveUsersToFile();
                            JOptionPane.showMessageDialog(null, "User changed successfully!", "Action success!", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Incorrect password!", "Invalid Details", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Password input was cancelled or empty.", "Operation Cancelled", JOptionPane.YES_OPTION);
                    }
                }
            });

            cancelButton.addActionListener(e -> {
                dialog.dispose();
            });

            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "Admin cannot be edited!", "Illegal action!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String role = (String) tableModel.getValueAt(modelRow, 3);

        if (role.equals("admin")) {
            JOptionPane.showMessageDialog(null, "Admin cannot be deleted!", "Illegal action!", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete this user?\nThis action CANNOT be undone!",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String adminPassword = JOptionPane.showInputDialog(null, "To confirm deletion, please enter your password:", "Enter your credentials", JOptionPane.QUESTION_MESSAGE);
            if (!adminPassword.trim().isEmpty()) {
                boolean adminConfirmResult = userAuth.confirmUserAdminChanges(adminPassword);
                if (adminConfirmResult) {
                    tableModel.removeRow(modelRow);
                    saveUsersToFile();
                    JOptionPane.showMessageDialog(null, "User deleted successfully!", "Action success!", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect password!", "Invalid Details", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Password input was cancelled or empty.", "Operation Cancelled", JOptionPane.YES_OPTION);
            }
        }

    }

    private void setInputBorderColor(JTextField textField, String title, Color color) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 1),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(null, Font.PLAIN, 17),
                color
        );
        textField.setFont(new Font(null, Font.BOLD, 16));
        textField.setOpaque(false);
        textField.setBorder(border);
    }

    public void usersTableSearchFilter() {
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        addUserBtn = new JButton("Add user", resizeIcon(new ImageIcon("./src/icons/user-plus-solid.png"), 15, 15));
        addUserBtn.setPreferredSize(new Dimension(120, 35));
        addUserBtn.setFocusPainted(false);
        searchField = new JTextField(25);
        searchField.setOpaque(false);
        searchField.setPreferredSize(new Dimension(100, 45));
        searchField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 1), "🔎 Search"));
        searchField.setFont(new Font("Arial", Font.PLAIN, 15));

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText();
                if (searchText.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                }
            }
        });
        searchPanel.add(searchField);
        searchPanel.add(addUserBtn);
        topTablePanel.add(searchPanel, BorderLayout.NORTH);
    }

    public void reloadUserTable() {
        List<String[]> users = readUsersFromFile("./src/auth/loginSession/userSession.txt");
        if (users == null) {
            return;
        }

        for (String[] user : users) {
            usernameSessionLabel.setText(user[0]);
            emailSessionLabel.setText(" " + user[1]);
        }
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private void addUser() {

        JDialog dialog = new JDialog((Frame) null, "Add New User", true);
        dialog.setSize(400, 350);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField newUsernameField = new JTextField();
        JTextField newEmailField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();

        setInputBorderColor(newUsernameField, "Username", Color.BLACK);
        setInputBorderColor(newEmailField, "Email address", Color.BLACK);
        setInputBorderColor(newPasswordField, "Password", Color.BLACK);

        JButton saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");
        saveButton.setBackground(Color.cyan);
        saveButton.setFocusPainted(false);
        cancelButton.setFocusPainted(false);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
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
                        && !newEmailField.getText().equals("")
                        && !new String(newPasswordField.getPassword()).equals("");
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
            String newEmail = newEmailField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();

            final Pattern VALID_EMAIL_ADDRESS_REGEX
                    = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
            Matcher emailMatcher = VALID_EMAIL_ADDRESS_REGEX.matcher(newEmail);
            String regex = "[^a-zA-Z ]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(newUsername);

            if (newUsername.isEmpty()) {
                setInputBorderColor(newUsernameField, "Username", Color.RED);
                JOptionPane.showMessageDialog(dialog, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                newUsernameField.grabFocus();
                return;
            }
            if (matcher.find()) {
                setInputBorderColor(newUsernameField, "Username", Color.RED);
                JOptionPane.showMessageDialog(dialog, "Special characters and numbers are not allowed", "Error", JOptionPane.ERROR_MESSAGE);
                newUsernameField.grabFocus();
                return;
            }
            if (newEmail.isEmpty()) {
                setInputBorderColor(newEmailField, "Email address", Color.RED);
                JOptionPane.showMessageDialog(dialog, "Email cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                newEmailField.grabFocus();
                return;
            }
            if (!emailMatcher.matches()) {
                setInputBorderColor(newEmailField, "Email address", Color.RED);
                JOptionPane.showMessageDialog(dialog, "Please enter the email address correctly.", "Error", JOptionPane.ERROR_MESSAGE);
                newEmailField.grabFocus();
                return;
            }
            if (newPassword.isEmpty()) {
                setInputBorderColor(newPasswordField, "Password", Color.RED);
                JOptionPane.showMessageDialog(dialog, "Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                newPasswordField.grabFocus();
                return;
            }
            if (newPassword.length() < 8) {
                setInputBorderColor(newPasswordField, "Password", Color.RED);
                JOptionPane.showMessageDialog(dialog, "The password must at least 8 characters long", "Error", JOptionPane.ERROR_MESSAGE);
                newPasswordField.grabFocus();
                return;
            }
            if (checkRegisteredUsers(newUsername, newEmail, newPassword)) {
                JOptionPane.showMessageDialog(null, "This user is already registered. Please try another one.", "Illegal action", JOptionPane.ERROR_MESSAGE);
            } else {
                setInputBorderColor(newUsernameField, "Username", Color.BLACK);
                setInputBorderColor(newEmailField, "Email Email address", Color.BLACK);
                setInputBorderColor(newPasswordField, "Password", Color.BLACK);

                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to add new user?", "Confirm Changes", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String adminPassword = JOptionPane.showInputDialog(null, "To add this user, please enter your password:", "Enter your credentials", JOptionPane.QUESTION_MESSAGE);
                    if (!adminPassword.trim().isEmpty()) {

                        boolean adminConfirmResult = userAuth.confirmUserAdminChanges(adminPassword);
                        if (adminConfirmResult) {
                            tableModel.addRow(new Object[]{newUsername, newEmail, newPassword, "member"});
                            tableModel.fireTableDataChanged();
                            dialog.dispose();
                            saveUsersToFile();
                            JOptionPane.showMessageDialog(null, "New User added successfully!", "Action success!", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Incorrect password!", "Invalid Details", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Password input was cancelled or empty.", "Operation Cancelled", JOptionPane.YES_OPTION);
                    }
                }
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public void loadUsersFromFile() {
        tableModel.setRowCount(0);

        List<String[]> usersMembers = readUsersFromFile("./src/data/userManagement/manageableUsers.txt");
        List<String[]> usersAdmin = readUsersFromFile("./src/data/users.txt");

        if (usersMembers != null) {
            for (String[] user : usersMembers) {
                tableModel.addRow(new Object[]{user[0], user[1], user[2], user[3]});
            }
        }

        if (usersAdmin != null) {
            for (String[] user : usersAdmin) {
                tableModel.addRow(new Object[]{user[0], user[1], user[2], user[3]});
            }
        }

        int adminCount = 0;
        int memberCount = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String role = (String) tableModel.getValueAt(i, 3);
            if ("Admin".equalsIgnoreCase(role)) {
                adminCount++;
            } else if ("Member".equalsIgnoreCase(role)) {
                memberCount++;
            }
        }
        // totalUsersLabelDisplay.setText("Total users: " + tableModel.getRowCount());
        // totalUserMembers.setText("Total members: " + memberCount);
        // totalUserAdmins.setText("Total admins: " + adminCount);

        userTable.setDefaultRenderer(Object.class, new RoleBasedRenderer());

    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void saveUsersToFile() {
        String filePath = "./src/data/userManagement/manageableUsers.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String username = (String) tableModel.getValueAt(i, 0);
                String email = (String) tableModel.getValueAt(i, 1);
                String password = (String) tableModel.getValueAt(i, 2);
                String role = (String) tableModel.getValueAt(i, 3);

                if (role.equalsIgnoreCase("admin")) {
                    continue;
                }

                String line = username + "," + email + "," + password + "," + role;
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            loadUsersFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private static class RoleBasedRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));

            Object roleValue = table.getValueAt(row, 3);
            String role = (roleValue != null) ? roleValue.toString() : "";

            if ("Admin".equalsIgnoreCase(role)) {
                label.setBackground(Color.CYAN);
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                label.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
            }

            if (column == 2 && value != null) {
                label.setText("xxxxxxxxxx");
            }

            return label;
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

    private void runLoginTask() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SwingUtilities.invokeLater(() -> {
                    overlayloading();
                    navbar.setVisible(false);
                    contentPanel.setVisible(false);
                    overlayPanel.setVisible(true);
                    revalidate();
                    repaint();
                });

                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    overlayPanel.setVisible(false);
                    navbar.setVisible(true);
                    contentPanel.setVisible(true);
                    revalidate();
                    repaint();
                    router.showPage("LOGIN", "RusByte Net - User Management");
                    userAuth.logout();
                    refreshUI();
                });
            }
        }.execute();
    }

    private void overlayloading() {
        overlayPanel = new JPanel(new GridBagLayout());
        overlayPanel.setOpaque(false);

        loadingCenter = new JLabel("<html><img src='" + getClass().getResource("/icons/loadingicon.gif") + "' width='100' height='100'></html>");
        loadingCenter.setPreferredSize(new Dimension(100, 100));
        loadingCenter.setOpaque(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        overlayPanel.add(loadingCenter, gbc);

        overlayPanel.setBounds(0, 0, getWidth(), getHeight());
        overlayPanel.setVisible(false);

        add(overlayPanel, BorderLayout.CENTER);
    }

    public void refreshUI() {
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + ""));
        searchField.setText("");
        revalidate();
        repaint();
    }
}
