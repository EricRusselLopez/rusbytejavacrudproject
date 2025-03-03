
import components.App;
import javax.swing.SwingUtilities;

/**
 * Entry point for the RusByte Net - User Management application. This class
 * initializes the graphical user interface (GUI) using SwingUtilities to ensure
 * thread safety.
 *
 * @author Eric Russel M. Lopez
 * @version 23.0.1 (Java SE 23.0.1)
 * @since 2025
 * @license Educational Project Use Only - © 2025 Eric Russel M. Lopez
 * @see
 * <a href="https://github.com/EricRusselLopez/rusbytejavacrudproject">GitHub
 * Repository</a>
 */
public class Main {

    public static void main(String[] args) {
        // This Initializes the application GUI Components on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(App::new);
    }
}
