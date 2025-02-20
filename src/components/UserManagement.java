package components;

import auth.UserAuthenticator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class UserManagement extends JPanel {

    private final App router;

    private GridBagConstraints gbc = new GridBagConstraints();

    //List<String[]> loadedUsers = readUsersFromFile("./src/data/users.txt");
    private JLabel logoLabel, logoTextLabel, loadingCenter;
    private final JPanel navbar, leftPanel, rightPanel, contentPanel, overlayPanel;
    private JButton menuButton;
    private JPopupMenu dropdownMenu;
    private final JMenuItem logoutItem;
    private final ImageIcon logoIcon, menuIcon, loadImageIcon;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagement(App router) {

        this.router = router;

        if (!new UserAuthenticator().isAuthenticated()) {
            new UserAuthenticator().logout();
        }

        setLayout(new BorderLayout());

        logoIcon = resizeIcon(new ImageIcon("./src/assets/rusbytefaviconmain.png"), 35, 35);
        menuIcon = resizeIcon(new ImageIcon("./src/icons/bars-solid.png"), 20, 20);
        loadImageIcon = new ImageIcon("./src/icons/loadingicon.gif");

        navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Color.LIGHT_GRAY);
        navbar.setPreferredSize(new Dimension(600, 50));
        navbar.setBorder(new MatteBorder(1, 0, 1, 0, Color.gray));

        leftPanel = new JPanel();
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        logoLabel = new JLabel(logoIcon);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(logoLabel);
        logoTextLabel = new JLabel("User Management");
        logoTextLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        logoTextLabel.setFont(new Font(null, Font.BOLD, 17));
        leftPanel.add(logoTextLabel);
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
        logoutItem.addActionListener(_ -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "↪  Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                runLoginTask();
            }
        });

        dropdownMenu.add(logoutItem);
        menuButton.addActionListener(_ -> dropdownMenu.show(menuButton, -60, menuButton.getHeight()));

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(menuButton);
        rightPanel.add(Box.createVerticalStrut(10));

        navbar.add(leftPanel, BorderLayout.WEST);
        navbar.add(rightPanel, BorderLayout.EAST);

        contentPanel = new JPanel();

        Set<Integer> editableRows = new HashSet<>();
        String[] columnNames = {" 👤 Username", "✉️ Email address", "✏️ Edit"};
        userTable = new JTable(tableModel);

        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setFont(new Font(null, Font.PLAIN, 19));
        userTable.getTableHeader().setReorderingAllowed(false);

        userTable.setRowHeight(40);
        userTable.setFont(new Font(null, Font.ITALIC, 15));
        JScrollPane scrollPane = new JScrollPane(userTable);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editableRows.contains(row) && column != 2;
            }
        };

        userTable.setModel(tableModel);

        userTable.getColumnModel().getColumn(2).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton editableButton = new JButton("Edit");
            editableButton.setContentAreaFilled(false);
            editableButton.setBorderPainted(false);
            editableButton.setFocusPainted(false);
            editableButton.setForeground(Color.BLUE);
            return editableButton;
        });

        userTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JButton editButton = new JButton("Edit");
                editButton.setContentAreaFilled(false);
                editButton.setBorderPainted(false);
                editButton.setFocusPainted(false);
                editButton.setForeground(Color.BLUE);

                editButton.addActionListener((ActionEvent e) -> {
                    editableRows.add(row);
                    tableModel.fireTableDataChanged();
                    System.out.println("Row " + row + " is now editable.");
                });

                return editButton;
            }
        });

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        loadUsersFromFile("./src\\data\\userManagement\\manageableUsers.txt");

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

        add(overlayPanel);
        add(navbar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.EAST);
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private void loadUsersFromFile(String filePath) {
        List<String[]> users = readUsersFromFile(filePath);
        for (String[] user : users) {
            tableModel.addRow(new Object[]{user[0], user[1]});
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
                    navbar.setVisible(false);
                    contentPanel.setVisible(false);
                    overlayPanel.setVisible(true);
                    revalidate();
                    repaint();
                });

                Thread.sleep(3000);
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    navbar.setVisible(true);
                    contentPanel.setVisible(true);
                    overlayPanel.setVisible(false);
                    revalidate();
                    repaint();
                    router.showPage("LOGIN", "RusByte Net - User Management");
                    new UserAuthenticator().logout();
                });
            }
        }.execute();
    }
}
