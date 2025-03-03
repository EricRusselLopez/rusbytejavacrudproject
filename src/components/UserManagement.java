package components;

import auth.UserAuthenticator;
import components.userManagementUtils.UserEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import util.CustomHeaderRenderer;
import util.customBorder;
import util.customImageResizer;

/**
 * Copyright (c) 2025 Eric Russel M. Lopez
 *
 * This class represents the UserManagement Screen Component UI.
 *
 * @component UserManagement
 */
public final class UserManagement extends JPanel implements customBorder {

    private final App router;

    private final GridBagConstraints gbc = new GridBagConstraints();
    private JLabel logoLabel, loadingCenter, usernameSessionLabel, emailSessionLabel, roleSessionLabel;
    private JPanel navbar, leftPanel, rightPanel, contentPanel, overlayPanel, leftContentPanel, rightContentPanel, leftContentPanelChild1, leftContentPanelChild2;
    private JButton menuButton, addUserBtn;
    private JPopupMenu dropdownMenu;
    private final JMenuItem logoutItem;
    private final ImageIcon logoIcon, menuIcon;
    private JPanel topTablePanel;
    private static JLabel totalUsersLabelDisplay, totalUserAdmins, totalUserMembers;
    private static DefaultTableModel tableModel;
    private static JTable userTable;
    private JTextField searchField;
    static TableRowSorter<DefaultTableModel> sorter;

    UserAuthenticator userAuth = new UserAuthenticator();
    UserEditor userManagementUserEditor = new UserEditor();
    customImageResizer customImageSize = new customImageResizer();

    /**
     * This is the constructor of the UserManagement component. Initializes the
     * user interface.
     *
     * @param router The application router used for navigating between screens.
     */
    public UserManagement(App router) {

        this.router = router;

        if (!userAuth.isAuthenticated()) {
            userAuth.logout();
        }

        setLayout(new BorderLayout());

        logoIcon = customImageSize.resizeIcon(new ImageIcon("./src/assets/rusbytefaviconmain.png"), 35, 35);
        menuIcon = customImageSize.resizeIcon(new ImageIcon("./src/icons/bars-solid.png"), 20, 20);

        /* Navigation bar */
        navbar = new JPanel(new BorderLayout());
        navbar.setBackground(new Color(230, 230, 230));
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
        dropdownMenu.setPreferredSize(new Dimension(115, 42));

        logoutItem = new JMenuItem(" Log Out", customImageSize.resizeIcon(new ImageIcon("./src/icons/right-from-bracket-solid.png"), 20, 20));
        logoutItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "↪  Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                runLogoutTask();
            }
        });

        dropdownMenu.add(logoutItem);
        menuButton.addActionListener(e -> dropdownMenu.show(menuButton, -75, menuButton.getHeight()));

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(menuButton);
        rightPanel.add(Box.createVerticalStrut(10));

        navbar.add(leftPanel, BorderLayout.WEST);
        navbar.add(rightPanel, BorderLayout.EAST);

        // -------- Main Contents -------- //
        contentPanel = new JPanel(new BorderLayout());

        // Main Left container panel
        leftContentPanel = new JPanel();
        leftContentPanel.setLayout(new BoxLayout(leftContentPanel, BoxLayout.Y_AXIS)); // Vertical layout
        leftContentPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK)); // Border follows the panel size
        leftContentPanel.setPreferredSize(new Dimension(260, 600)); // Fixed size

        // First child Left panel (User Info)
        leftContentPanelChild1 = new JPanel();
        leftContentPanelChild1.setLayout(new BoxLayout(leftContentPanelChild1, BoxLayout.Y_AXIS));
        leftContentPanelChild1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        leftContentPanelChild1.setPreferredSize(new Dimension(260, 150));
        leftContentPanelChild1.setMaximumSize(new Dimension(260, 150)); // Force full width

        usernameSessionLabel = new JLabel("Username", customImageSize.resizeIcon(new ImageIcon("./src/icons/user-solid.png"), 25, 25), JLabel.LEFT);
        usernameSessionLabel.setFont(new Font("Arial", Font.BOLD, 20));
        usernameSessionLabel.setBorder(new EmptyBorder(25, 30, 5, 10));

        emailSessionLabel = new JLabel("Email", customImageSize.resizeIcon(new ImageIcon("./src/icons/envelope-solid.png"), 15, 15), JLabel.LEFT);
        emailSessionLabel.setFont(new Font("Arial", Font.ITALIC, 15));
        emailSessionLabel.setBorder(new EmptyBorder(10, 30, 5, 10));

        roleSessionLabel = new JLabel(" Admin", customImageSize.resizeIcon(new ImageIcon("./src/icons/user-tie-solid.png"), 15, 15), JLabel.LEFT);
        roleSessionLabel.setFont(new Font("Arial", Font.BOLD, 15));
        roleSessionLabel.setForeground(new Color(0, 145, 145));
        roleSessionLabel.setBorder(new EmptyBorder(10, 30, 5, 10));

        usernameSessionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailSessionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleSessionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftContentPanelChild1.add(usernameSessionLabel);
        leftContentPanelChild1.add(emailSessionLabel);
        leftContentPanelChild1.add(roleSessionLabel);

        // Second child Left panel (User Info)
        leftContentPanelChild2 = new JPanel();
        leftContentPanelChild2.setLayout(new BoxLayout(leftContentPanelChild2, BoxLayout.Y_AXIS));
        leftContentPanelChild2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        leftContentPanelChild2.setPreferredSize(new Dimension(260, 150));
        leftContentPanelChild2.setMaximumSize(new Dimension(260, 150));

        totalUsersLabelDisplay = new JLabel("Total Users", customImageSize.resizeIcon(new ImageIcon("./src/icons/user-solid.png"), 20, 20), JLabel.LEFT);
        totalUsersLabelDisplay.setFont(new Font("Arial", Font.BOLD, 20));
        totalUsersLabelDisplay.setBorder(new EmptyBorder(15, 20, 5, 10));
        totalUserMembers = new JLabel("Members", customImageSize.resizeIcon(new ImageIcon("./src/icons/user-solid.png"), 20, 20), JLabel.LEFT);
        totalUserMembers.setFont(new Font("Arial", Font.ITALIC, 16));
        totalUserMembers.setBorder(new EmptyBorder(15, 20, 5, 10));
        totalUserAdmins = new JLabel("Admins", customImageSize.resizeIcon(new ImageIcon("./src/icons/user-tie-solid.png"), 20, 20), JLabel.LEFT);
        totalUserAdmins.setFont(new Font("Arial", Font.BOLD, 15));
        totalUserAdmins.setForeground(new Color(0, 145, 145));
        totalUserAdmins.setBorder(new EmptyBorder(15, 20, 5, 10));

        totalUsersLabelDisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalUserMembers.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalUserAdmins.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftContentPanelChild2.add(totalUsersLabelDisplay);
        leftContentPanelChild2.add(totalUserMembers);
        leftContentPanelChild2.add(totalUserAdmins);

        leftContentPanel.add(leftContentPanelChild1);
        leftContentPanel.add(leftContentPanelChild2);

        rightContentPanel = new JPanel(new BorderLayout());

        topTablePanel = new JPanel(new BorderLayout());
        topTablePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));

        /* Table Area */
        String[] columnNames = {"Username", "Email", "Password", "Role", "CreatedAt"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                Point point = e.getPoint();
                int row = rowAtPoint(point);
                if (row >= 0) {
                    return "<html><div style='background-color: white; padding: 8px; border-radius: 5px;'>"
                            + "<font size='4'><strong>Name:</strong> " + getValueAt(row, 0)
                            + "<br><strong>Email:</strong> " + getValueAt(row, 1)
                            + "<br><strong>Role:</strong> " + getValueAt(row, 3)
                            + "<br><strong>Since:</strong> " + getValueAt(row, 4) + "</font></div></html>";

                }
                return null;
            }
        };

        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(100, 45));
        tableHeader.setEnabled(false);
        tableHeader.getColumnModel().getColumn(0).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/user-solid.png"));
        tableHeader.getColumnModel().getColumn(1).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/envelope-solid.png"));
        tableHeader.getColumnModel().getColumn(2).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/key-solid.png"));
        tableHeader.getColumnModel().getColumn(3).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/user-tie-solid.png"));
        tableHeader.getColumnModel().getColumn(4).setHeaderRenderer(new CustomHeaderRenderer("./src/icons/calendar-solid.png"));

        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.setRowHeight(40);
        userTable.setFont(new Font(null, Font.ITALIC, 17));
        usersTableSearchFilter();

        /* End of table Area */
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setPreferredSize(new Dimension(155, 75));

        addUserBtn.addActionListener(e -> userManagementUserEditor.addUser(userTable, tableModel, userAuth));
        JMenuItem editItem = new JMenuItem(" Edit this user", customImageSize.resizeIcon(new ImageIcon("./src/icons/user-pen-solid.png"), 15, 15));

        editItem.addActionListener(e -> userManagementUserEditor.editUser(userTable, tableModel, userAuth));

        JMenuItem deleteItem = new JMenuItem(" Delete this user", customImageSize.resizeIcon(new ImageIcon("./src/icons/trash-solid.png"), 15, 15));

        deleteItem.addActionListener(e -> userManagementUserEditor.deleteUser(userTable, tableModel, userAuth));
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

    /**
     * This method filters the user search entries dynamically. The filtering
     * applies to specific columns (Username, Email, Role, and createdAt) but
     * excludes sensitive data like passwords.
     */
    public void usersTableSearchFilter() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        addUserBtn = new JButton("Add user", customImageSize.resizeIcon(new ImageIcon("./src/icons/user-plus-solid.png"), 15, 15));
        addUserBtn.setBackground(new Color(0, 235, 235));
        addUserBtn.setForeground(Color.BLACK);
        addUserBtn.setToolTipText("Add new user");
        addUserBtn.setPreferredSize(new Dimension(120, 35));
        addUserBtn.setFocusPainted(false);
        addUserBtn.setFont(new Font("Arial", Font.BOLD, 13));
        searchField = new JTextField(25);
        searchField.setToolTipText("Search for user");
        searchField.setOpaque(false);
        searchField.setPreferredSize(new Dimension(100, 45));
        setInputBorderColor(searchField, "🔎 Search", Color.BLACK);
        searchField.setFont(new Font("Arial", Font.PLAIN, 15));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText().trim().toLowerCase();
                if (searchText.isEmpty()) {
                    setInputBorderColor(searchField, "🔎 Search", Color.BLACK);
                    sorter.setRowFilter(null);
                } else {
                    RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
                        @Override
                        public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                            // The columns to search (Username, Email, Role, CreatedAt) - not Password
                            for (int col : new int[]{0, 1, 3, 4}) {
                                String value = entry.getStringValue(col).trim().toLowerCase();
                                if (value.contains(searchText)) {
                                    return true; // Keep the row
                                }
                            }
                            return false;
                        }
                    };
                    sorter.setRowFilter(filter);
                    if (userTable.getRowCount() > 0) {
                        setInputBorderColor(searchField, "🔎 Search", Color.BLACK);
                    } else {
                        setInputBorderColor(searchField, "❌ No user found", Color.RED);
                    }
                }
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(addUserBtn);
        topTablePanel.add(searchPanel, BorderLayout.NORTH);
    }

    /**
     * This method reloads the user Profile by reading user session data from a
     * file. It updates the session labels (username and email).
     *
     * The user data is fetched from `./src/auth/loginSession/userSession.txt`,
     * and only one user's session details are displayed in the labels.
     */
    public void reloadUserProfile() {
        List<String[]> users = readUsersFromFile("./src/auth/loginSession/userSession.txt");
        if (users == null) {
            return;
        }

        for (String[] user : users) {
            usernameSessionLabel.setText(user[0]);
            usernameSessionLabel.setToolTipText("Name: " + usernameSessionLabel.getText());
            emailSessionLabel.setText(" " + user[1]);
            emailSessionLabel.setToolTipText("Email: " + emailSessionLabel.getText());
            roleSessionLabel.setToolTipText("Role: " + roleSessionLabel.getText());

        }
        userTableSorter();
    }

    /**
     * Loads user data from specified files and populates the table model.
     * <p>
     * This method reads user information from two files:
     * <ul>
     * <li><code>./src/data/userManagement/manageableUsers.txt</code> - Contains
     * member users.</li>
     * <li><code>./src/data/users.txt</code> - Contains admin users.</li>
     * </ul>
     * The retrieved data is added to the table model, and the total count of
     * users, members, and admins is displayed. The table is then updated with a
     * custom role-based renderer and sorted.
     * </p>
     *
     * @see #readUsersFromFile(String) Reads user data from a file.
     * @see #RoleBasedRenderer Custom renderer for displaying roles.
     */
    public static void loadUsersFromFile() {
        tableModel.setRowCount(0);

        List<String[]> usersMembers = readUsersFromFile("./src/data/userManagement/manageableUsers.txt");
        List<String[]> usersAdmin = readUsersFromFile("./src/data/users.txt");

        if (usersMembers != null) {
            for (String[] user : usersMembers) {
                tableModel.addRow(new Object[]{user[0], user[1], user[2], user[3], user[4]});
            }
        }

        if (usersAdmin != null) {
            for (String[] user : usersAdmin) {
                tableModel.addRow(new Object[]{user[0], user[1], user[2], user[3], user[4]});
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
        totalUsersLabelDisplay.setText("Total users: " + tableModel.getRowCount());
        totalUserMembers.setText("Total members: " + memberCount);
        totalUserAdmins.setText("Total admins: " + adminCount);

        userTable.setDefaultRenderer(Object.class, new RoleBasedRenderer());
        userTableSorter();
    }

    /**
     * This this method renderer modifies the appearance of table cells based on
     * the user's role:
     * <ul>
     * <li><b>Admin</b>: The text is bold, with a cyan background and black
     * foreground.</li>
     * <li><b>Other users</b>: Uses default styling, adjusting for
     * selection.</li>
     * </ul>
     * Additionally, passwords in the third column (index 2) are masked with
     * asterisks.
     * </p>
     */
    private static class RoleBasedRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));

            Object roleValue = table.getValueAt(row, 3);
            String role = (roleValue != null) ? roleValue.toString() : "";

            if ("Admin".equalsIgnoreCase(role)) {
                label.setFont(new Font("Ärial", Font.BOLD, 16));
                label.setBackground(Color.CYAN);
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                label.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
            }

            if (column == 2 && value != null) {
                label.setText("**************");
            }

            return label;
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
    private static List<String[]> readUsersFromFile(String file) {
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
     * This method Runs an asynchronous logout proccess task using a
     * {@link SwingWorker}.
     * <p>
     * This method performs UI updates to display a loading overlay while
     * delaying the logout proccess for 2 seconds to simulate a processing
     * effect.
     * </p>
     *
     * The method ensures UI modifications are performed on the Event Dispatch
     * Thread (EDT) using {@link SwingUtilities#invokeLater(Runnable)}.
     */
    private void runLogoutTask() {
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

    /**
     * This method creates a transparent overlay panel centered within the UI,
     * displaying a loading GIF to indicate processing. It uses
     * {@link GridBagLayout} to ensure proper alignment of the loading icon.
     * </p>
     *
     * @see GridBagLayout
     * @see GridBagConstraints
     */
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

    /**
     * This method reinitializes the {@link TableRowSorter} for the user table,
     * clears any existing filters, and resets the search field to ensure the
     * table displays all available data.
     * </p>
     *
     * <p>
     * <b>Key Functionalities:</b></p>
     * <ul>
     * <li>Creates a new {@link TableRowSorter} and assigns it to the
     * table.</li>
     * <li>Applies an empty regex filter to show all rows.</li>
     * <li>Clears the search field for a fresh search input.</li>
     * <li>Triggers UI updates using {@code revalidate()} and
     * {@code repaint()}.</li>
     * </ul>
     *
     * @see TableRowSorter
     * @see RowFilter
     */
    public void refreshUI() {
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + ""));
        searchField.setText("");
        revalidate();
        repaint();
    }

    /**
     * This method initializes a {@link TableRowSorter} for the user table
     * model, sets the default sorting order to ascending for the first column,
     * and applies the sorting to update the table view.
     * </p>
     *
     * @see TableRowSorter
     * @see RowSorter.SortKey
     * @see SortOrder
     */
    public static void userTableSorter() {
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();
    }
}
