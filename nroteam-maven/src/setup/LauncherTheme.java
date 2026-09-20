package setup;

import com.formdev.flatlaf.FlatDarculaLaf;
import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;

final class LauncherTheme {

    static final Color BG_MAIN = new Color(11, 18, 32);
    static final Color BG_CARD = new Color(21, 31, 50);
    static final Color BG_CARD_HEADER = new Color(30, 41, 59);
    static final Color BG_INPUT = new Color(17, 27, 45);
    static final Color BORDER = new Color(51, 65, 85);
    static final Color TEXT = new Color(248, 250, 252);
    static final Color TEXT_MUTED = new Color(148, 163, 184);
    static final Color BLUE = new Color(56, 189, 248);
    static final Color BLUE_DARK = new Color(3, 105, 161);
    static final Color GREEN = new Color(34, 197, 94);
    static final Color YELLOW = new Color(251, 191, 36);
    static final Color RED = new Color(248, 113, 113);

    private LauncherTheme() {
    }

    static void install() {
        FlatDarculaLaf.setup();
        UIManager.put("Panel.background", BG_MAIN);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", BG_CARD_HEADER);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.hoverBackground", new Color(38, 52, 73));
        UIManager.put("Button.pressedBackground", BLUE_DARK);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusColor", BLUE);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("PasswordField.background", BG_INPUT);
        UIManager.put("TabbedPane.background", BG_MAIN);
        UIManager.put("TabbedPane.selectedBackground", BG_CARD);
        UIManager.put("TabbedPane.underlineColor", BLUE);
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
    }
}
