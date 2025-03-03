package components;

import auth.UserAuthenticator;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/**
 * The main application window for the RusByte Net - User Management system.
 * <p>
 * This class serves as a router for different UI components, including:
 * <ul>
 * <li>LoginScreen - Handles user authentication</li>
 * <li>RegisterScreen - Allows new admin users to create an account</li>
 * <li>UserManagement - Provides administrative functions</li>
 * </ul>
 *
 * @author Eric Russel M. Lopez
 * @version 23.0.1 (Java SE 23.0.1)
 * @since 2025
 */
public final class App extends JFrame {

    final CardLayout cardLayout;
    final JPanel mainPanel;

    final ImageIcon appIcon = new ImageIcon(getClass().getResource("/assets/rusbytefaviconmain.png"));

    UserAuthenticator userAuth = new UserAuthenticator();

    /**
     * Constructs the main application window and initializes UI components
     * dynamically.
     * <p>
     * The UI includes login, registration, and user management screens. Based
     * on the authentication status, the appropriate page is displayed upon
     * startup.
     */
    public App() {
        setTitle("RusByte Net - User Management");
        windowSizeSetDefault();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(appIcon.getImage());
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginScreen(this), "LOGIN");
        mainPanel.add(new RegisterScreen(this), "REGISTER");
        mainPanel.add(new UserManagement(this), "USER MANAGEMENT");

        if (userAuth.isAuthenticated()) {
            showPage("USER MANAGEMENT", "RusByte Net - User Management");
        } else {
            userAuth.logout();
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int userExitChoice = JOptionPane.showConfirmDialog(App.this, "Are you sure you want to exit RusByte Net?", "Exit application", JOptionPane.YES_NO_OPTION);
                if (userExitChoice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        add(mainPanel);
        setVisible(true);
    }

    /**
     * This method sets the default window size and center it on the screen.
     */
    public void windowSizeSetDefault() {
        setSize(750, 650);
        setLocationRelativeTo(null);
    }

    /**
     * This method routes the specified page within the application and updates
     * the window title accordingly.
     * <p>
     * If the "USER MANAGEMENT" page is displayed, the window is maximized.
     * Otherwise, the window size is set to its default state.</p>
     *
     * @param pageName The name of the page to be displayed.
     * @param windowTitle The title to be set for the window.
     */
    public void showPage(String pageName, String windowTitle) {
        cardLayout.show(mainPanel, pageName);
        setTitle(windowTitle);

        if ("USER MANAGEMENT".equals(pageName)) {
            Component c = mainPanel.getComponent(2); // Gets the component to check if it is current on render to applies the live data
            if (c instanceof UserManagement userManagement) {
                userManagement.reloadUserProfile();
                UserManagement.loadUsersFromFile();
                setExtendedState(JFrame.MAXIMIZED_BOTH); //Full screen view
            }
        } else {
            windowSizeSetDefault();
        }
    }
}
