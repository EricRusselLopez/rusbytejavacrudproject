package components;

import auth.UserAuthenticator;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public final class App extends JFrame {

    final CardLayout cardLayout;
    final JPanel mainPanel;

    final ImageIcon appIcon = new ImageIcon("./src/assets/rusbytefaviconmain.png");

    UserAuthenticator userAuth = new UserAuthenticator();

    public App() {
        setTitle("RusByte Net - User Management");
        windowSizeSetDefault();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setIconImage(appIcon.getImage());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginScreen(this), "LOGIN");
        mainPanel.add(new RegisterScreen(this), "REGISTER");
        mainPanel.add(new UserManagement(this), "USER MANAGEMENT");

        if (userAuth.isAuthenticated()) {
            showPage("USER MANAGEMENT", "RusByte Net - User Management");
            windowSizeHomeSetAuthenticated();
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

    public void windowSizeHomeSetAuthenticated() {
        setSize(900, 900);
    }

    public void windowSizeSetDefault() {
        setSize(800, 600);
    }

    public void showPage(String pageName, String windowTitle) {
        cardLayout.show(mainPanel, pageName);
        setTitle(windowTitle);
    }
}
