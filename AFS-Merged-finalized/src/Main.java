import javax.swing.SwingUtilities;
import ui.LoginFrame;
import ui.Theme;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.applyDarkTheme();
            new LoginFrame().setVisible(true);
        });
    }
}
