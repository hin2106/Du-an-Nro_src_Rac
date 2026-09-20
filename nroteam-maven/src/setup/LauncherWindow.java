package setup;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import server.ServerManager;

/** Cua so dieu huong hien thi truoc giao dien quan ly server. */
public final class LauncherWindow extends JFrame {

    private final SetupManager setupManager = new SetupManager();
    private final JLabel statusLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();
    private final JButton startButton;
    private final JButton setupButton;
    private final JButton checkButton;
    private final JProgressBar progress = new JProgressBar();

    public LauncherWindow() {
        super("NRO Server Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 460));
        setSize(820, 500);
        setLocationRelativeTo(null);
        getContentPane().setBackground(LauncherTheme.BG_MAIN);
        setLayout(new BorderLayout(0, 20));
        try {
            Image icon = Toolkit.getDefaultToolkit().createImage(
                    getClass().getResource("/icons/server-icon.png"));
            setIconImage(icon);
        } catch (Exception ignored) {
        }

        JPanel header = createHeader();
        header.setBorder(BorderFactory.createEmptyBorder(28, 32, 0, 32));
        add(header, BorderLayout.NORTH);

        startButton = createActionButton("▶  CHẠY SERVER",
                "Kiểm tra database và mở bảng điều khiển server", LauncherTheme.BLUE_DARK);
        setupButton = createActionButton("⚙  THIẾT LẬP",
                "Cấu hình server, MySQL/MariaDB và mật khẩu", LauncherTheme.BG_CARD_HEADER);
        checkButton = createActionButton("✓  KIỂM TRA",
                "Kiểm tra lại cấu hình và kết nối database", LauncherTheme.BG_CARD_HEADER);

        JPanel actions = new JPanel(new GridLayout(1, 3, 16, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));
        actions.add(wrapButton(startButton));
        actions.add(wrapButton(setupButton));
        actions.add(wrapButton(checkButton));
        add(actions, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 32, 24, 32));
        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.setPreferredSize(new Dimension(180, 5));
        footer.add(progress, BorderLayout.NORTH);

        JLabel hint = new JLabel("Tự động chạy trên VPS: run.bat --server");
        hint.setForeground(LauncherTheme.TEXT_MUTED);
        JButton exit = new JButton("Thoát");
        exit.addActionListener(e -> dispose());
        footer.add(hint, BorderLayout.WEST);
        footer.add(exit, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startServer());
        setupButton.addActionListener(e -> openSetup());
        checkButton.addActionListener(e -> checkConnection());
        refreshStatus();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("NRO SERVER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(LauncherTheme.TEXT);
        JLabel subtitle = new JLabel("Khởi động và thiết lập hệ thống");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(LauncherTheme.TEXT_MUTED);
        titles.add(title);
        titles.add(subtitle);

        JPanel status = new JPanel(new GridLayout(2, 1, 0, 3));
        status.setOpaque(false);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailLabel.setForeground(LauncherTheme.TEXT_MUTED);
        detailLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        status.add(statusLabel);
        status.add(detailLabel);

        panel.add(titles, BorderLayout.WEST);
        panel.add(status, BorderLayout.EAST);
        return panel;
    }

    private JButton createActionButton(String text, String tooltip, java.awt.Color background) {
        JButton button = new JButton("<html><div style='text-align:center'>" + text
                + "<br><span style='font-size:9px;color:#94a3b8'>" + tooltip + "</span></div></html>");
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(background);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    private JPanel wrapButton(JButton button) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(LauncherTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LauncherTheme.BORDER),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        card.add(button, BorderLayout.CENTER);
        return card;
    }

    private void refreshStatus() {
        boolean ready = setupManager.isSetupComplete();
        statusLabel.setText(ready ? "●  ĐÃ THIẾT LẬP" : "●  CHƯA THIẾT LẬP");
        statusLabel.setForeground(ready ? LauncherTheme.GREEN : LauncherTheme.YELLOW);
        detailLabel.setText(ready ? "Sẵn sàng kiểm tra và chạy" : "Hãy mở Thiết lập trước khi chạy");
        startButton.setEnabled(ready);
    }

    private void openSetup() {
        SetupWindow window = new SetupWindow(this, setupManager);
        window.setVisible(true);
        refreshStatus();
    }

    private void checkConnection() {
        runDatabaseTask("Đang kiểm tra kết nối database...", false);
    }

    private void startServer() {
        runDatabaseTask("Đang xác minh trước khi chạy server...", true);
    }

    private void runDatabaseTask(String message, boolean startAfterSuccess) {
        setBusy(true, message);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                setupManager.verifySavedConfiguration();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (startAfterSuccess) {
                        dispose();
                        Thread serverThread = new Thread(
                                () -> ServerManager.main(new String[0]), "nro-server-starter");
                        serverThread.setDaemon(false);
                        serverThread.start();
                    } else {
                        setBusy(false, "Kết nối database thành công");
                        JOptionPane.showMessageDialog(LauncherWindow.this,
                                "Kết nối database thành công.", "Kiểm tra",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    setBusy(false, "Kết nối database thất bại");
                    JOptionPane.showMessageDialog(LauncherWindow.this,
                            rootMessage(e), "Không thể kết nối", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy, String message) {
        progress.setVisible(busy);
        startButton.setEnabled(!busy && setupManager.isSetupComplete());
        setupButton.setEnabled(!busy);
        checkButton.setEnabled(!busy);
        detailLabel.setText(message);
    }

    static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.toString() : current.getMessage();
    }

    public static void showWindow() {
        SwingUtilities.invokeLater(() -> {
            LauncherTheme.install();
            new LauncherWindow().setVisible(true);
        });
    }
}
