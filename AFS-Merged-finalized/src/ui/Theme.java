package ui;

import java.awt.*;
import javax.swing.*;

public class Theme {
    // Base
    public static final Color BG = new Color(16, 18, 23);       // app background
    public static final Color SIDEBAR = new Color(20, 22, 28);  // sidebar
    public static final Color CARD = new Color(24, 27, 34);     // cards/panels

    // Text
    public static final Color TEXT = new Color(236, 239, 244);
    public static final Color MUTED = new Color(160, 166, 179);

    // Accents
    public static final Color PRIMARY = new Color(73, 118, 255);
    public static final Color DANGER = new Color(220, 76, 76);

    // Inputs / borders
    public static final Color INPUT_BG = new Color(18, 20, 26);
    public static final Color BORDER = new Color(46, 50, 62);

    public static void applyDarkTheme() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        UIManager.put("control", CARD);
        UIManager.put("info", CARD);
        UIManager.put("nimbusLightBackground", BG);
        UIManager.put("text", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Panel.background", BG);
    }
}
