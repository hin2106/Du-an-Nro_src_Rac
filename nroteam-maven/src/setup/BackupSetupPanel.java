package setup;

import backup.BackupConfig;
import backup.BackupCoordinator;
import backup.GoogleDriveBackupService;
import backup.SecureFiles;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Tab setup va van hanh backup truoc khi server game khoi dong. */
final class BackupSetupPanel extends JPanel {

    private final JCheckBox enabled = new JCheckBox("Bật backup tự động");
    private final JCheckBox runOnStartup = new JCheckBox("Backup sau khi server chạy thành công");
    private final JCheckBox googleEnabled = new JCheckBox("Tự động upload Google Drive");
    private final JTextField fullTime = new JTextField();
    private final JTextField playerTime = new JTextField();
    private final JTextField directory = new JTextField();
    private final JTextField retention = new JTextField();
    private final JTextField mysqldump = new JTextField();
    private final JTextField mysql = new JTextField();
    private final JTextField credentials = new JTextField();
    private final JTextField driveFolder = new JTextField();
    private final JTextField driveRetention = new JTextField();
    private final JLabel driveStatus = new JLabel();
    private final JLabel operationStatus = new JLabel("Sẵn sàng");
    private final GoogleDriveBackupService drive = new GoogleDriveBackupService();
    private final java.util.List<JButton> actionButtons = new java.util.ArrayList<>();

    BackupSetupPanel() {
        super(new BorderLayout(0, 10));
        setBackground(LauncherTheme.BG_MAIN);
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(LauncherTheme.BG_CARD);
        content.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        int row = 0;
        row = addChecks(content, row);
        row = addField(content, row, "Giờ full backup (HH:mm)", fullTime);
        row = addField(content, row, "Giờ snapshot player (HH:mm)", playerTime);
        row = addField(content, row, "Thư mục backup", directory);
        row = addField(content, row, "Số ngày giữ local", retention);
        row = addField(content, row, "Đường dẫn mysqldump", mysqldump);
        row = addField(content, row, "Đường dẫn mysql", mysql);
        row = addCredentialField(content, row);
        row = addField(content, row, "Tên thư mục trên Drive", driveFolder);
        row = addField(content, row, "Số ngày giữ trên Drive", driveRetention);
        addDriveActions(content, row);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createLineBorder(LauncherTheme.BORDER));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        add(createOperationBar(), BorderLayout.SOUTH);
        load();
    }

    void saveConfiguration() throws Exception {
        BackupConfig config = new BackupConfig();
        config.set("backup.enabled", Boolean.toString(enabled.isSelected()));
        config.set("backup.runOnStartup", Boolean.toString(runOnStartup.isSelected()));
        config.set("backup.full.time", fullTime.getText());
        config.set("backup.player.time", playerTime.getText());
        config.set("backup.local.directory", directory.getText());
        config.set("backup.local.retentionDays", retention.getText());
        config.set("backup.mysqldump.path", mysqldump.getText());
        config.set("backup.mysql.path", mysql.getText());
        config.set("backup.google.enabled", Boolean.toString(googleEnabled.isSelected()));
        config.set("backup.google.credentials", credentials.getText());
        config.set("backup.google.folderName", driveFolder.getText());
        config.set("backup.google.retentionDays", driveRetention.getText());
        config.save();
    }

    private void load() {
        BackupConfig config = new BackupConfig();
        enabled.setSelected(config.enabled());
        runOnStartup.setSelected(config.runOnStartup());
        googleEnabled.setSelected(config.googleEnabled());
        fullTime.setText(config.get("backup.full.time"));
        playerTime.setText(config.get("backup.player.time"));
        directory.setText(config.get("backup.local.directory"));
        retention.setText(config.get("backup.local.retentionDays"));
        mysqldump.setText(config.get("backup.mysqldump.path"));
        mysql.setText(config.get("backup.mysql.path"));
        credentials.setText(config.get("backup.google.credentials"));
        driveFolder.setText(config.get("backup.google.folderName"));
        driveRetention.setText(config.get("backup.google.retentionDays"));
        refreshDriveStatus();
    }

    private int addChecks(JPanel panel, int row) {
        JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        checks.setOpaque(false);
        enabled.setOpaque(false);
        runOnStartup.setOpaque(false);
        googleEnabled.setOpaque(false);
        checks.add(enabled);
        checks.add(runOnStartup);
        checks.add(googleEnabled);
        GridBagConstraints c = constraints(0, row);
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(checks, c);
        return row + 1;
    }

    private int addField(JPanel panel, int row, String label, JTextField field) {
        field.setColumns(28);
        JLabel title = new JLabel(label);
        title.setForeground(LauncherTheme.TEXT_MUTED);
        GridBagConstraints left = constraints(0, row);
        left.anchor = GridBagConstraints.WEST;
        panel.add(title, left);
        GridBagConstraints right = constraints(1, row);
        right.gridwidth = 2;
        right.weightx = 1;
        right.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, right);
        return row + 1;
    }

    private int addCredentialField(JPanel panel, int row) {
        JLabel title = new JLabel("OAuth credentials JSON");
        title.setForeground(LauncherTheme.TEXT_MUTED);
        panel.add(title, constraints(0, row));
        GridBagConstraints fieldC = constraints(1, row);
        fieldC.weightx = 1;
        fieldC.fill = GridBagConstraints.HORIZONTAL;
        panel.add(credentials, fieldC);
        JButton browse = button("Chọn file");
        browse.addActionListener(e -> chooseCredentials());
        panel.add(browse, constraints(2, row));
        return row + 1;
    }

    private void addDriveActions(JPanel panel, int row) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton connect = button("Kết nối Google Drive");
        JButton test = button("Kiểm tra Drive");
        JButton reconnect = button("Cấp quyền lại");
        connect.addActionListener(e -> connectDrive());
        test.addActionListener(e -> testDrive());
        reconnect.addActionListener(e -> reconnectDrive());
        actions.add(connect);
        actions.add(test);
        actions.add(reconnect);
        actions.add(driveStatus);
        GridBagConstraints c = constraints(0, row);
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(actions, c);
    }

    private JPanel createOperationBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        operationStatus.setForeground(LauncherTheme.TEXT_MUTED);
        panel.add(operationStatus, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        actions.setOpaque(false);
        JButton full = button("Full backup ngay");
        JButton players = button("Snapshot player ngay");
        JButton restorePlayer = button("Restore player");
        JButton restoreFull = button("Restore full DB");
        full.addActionListener(e -> runBackup(true));
        players.addActionListener(e -> runBackup(false));
        restorePlayer.addActionListener(e -> restorePlayer());
        restoreFull.addActionListener(e -> restoreFull());
        actions.add(full);
        actions.add(players);
        actions.add(restorePlayer);
        actions.add(restoreFull);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JButton button(String text) {
        JButton button = new JButton(text);
        actionButtons.add(button);
        return button;
    }

    private void chooseCredentials() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Google OAuth JSON", "json"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Path destination = Path.of("data", "config", "google-drive-credentials.json");
                Files.createDirectories(destination.getParent());
                Files.copy(chooser.getSelectedFile().toPath(), destination,
                        StandardCopyOption.REPLACE_EXISTING);
                SecureFiles.ownerOnly(destination);
                credentials.setText(destination.toString().replace('\\', '/'));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, LauncherWindow.rootMessage(e),
                        "Không thể lưu credentials", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void connectDrive() {
        runWorker("Đang mở trình duyệt để cấp quyền Google...", () -> {
            saveConfiguration();
            return drive.authorizeInteractive(new BackupConfig());
        }, this::refreshDriveStatus);
    }

    private void testDrive() {
        runWorker("Đang kiểm tra Google Drive...", () -> {
            saveConfiguration();
            return drive.testConnection(new BackupConfig());
        }, this::refreshDriveStatus);
    }

    private void reconnectDrive() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Refresh token hiện tại sẽ bị xóa trên máy. Trình duyệt sẽ yêu cầu cấp quyền lại.",
                "Cấp quyền Google lại", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        runWorker("Đang xóa token cũ và mở Google...", () -> {
            saveConfiguration();
            drive.clearStoredAuthorization();
            return drive.authorizeInteractive(new BackupConfig());
        }, this::refreshDriveStatus);
    }

    private void runBackup(boolean full) {
        runWorker(full ? "Đang tạo full database backup..." : "Đang snapshot tất cả player...", () -> {
            saveConfiguration();
            Path result = full ? BackupCoordinator.gI().backupDatabaseNow()
                    : BackupCoordinator.gI().backupPlayersNow();
            return "Hoàn tất: " + result.toAbsolutePath();
        }, null);
    }

    private void restorePlayer() {
        JFileChooser chooser = new JFileChooser(new BackupConfig().localDirectory().resolve("players").toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Player snapshot ZIP", "zip"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String selector = JOptionPane.showInputDialog(this, "Nhập chính xác ID hoặc tên player cần restore:");
        if (selector == null || selector.isBlank()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Server game phải đang TẮT. Chỉ hành trang/rương/trang bị/pet sẽ được khôi phục.\nTiếp tục?",
                "Xác nhận restore player", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        Path archive = chooser.getSelectedFile().toPath();
        runWorker("Đang restore player...", () ->
                "Đã restore: " + BackupCoordinator.gI().restorePlayer(archive, selector), null);
    }

    private void restoreFull() {
        JFileChooser chooser = new JFileChooser(new BackupConfig().localDirectory().resolve("full").toFile());
        chooser.setFileFilter(new FileNameExtensionFilter("Full database GZIP", "gz"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String confirm = JOptionPane.showInputDialog(this,
                "CẢNH BÁO: server phải TẮT và toàn database sẽ bị thay thế.\nNhập RESTORE để xác nhận:");
        if (!"RESTORE".equals(confirm)) return;
        Path archive = chooser.getSelectedFile().toPath();
        runWorker("Đang tạo emergency backup rồi restore database...", () -> {
            saveConfiguration();
            BackupCoordinator.gI().restoreDatabase(archive);
            return "Restore full database hoàn tất";
        }, null);
    }

    private void runWorker(String message, Work work, Runnable afterSuccess) {
        setBusy(true, message);
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return work.run(); }
            @Override protected void done() {
                try {
                    String result = get();
                    setBusy(false, result);
                    if (afterSuccess != null) afterSuccess.run();
                    JOptionPane.showMessageDialog(BackupSetupPanel.this, result,
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    setBusy(false, "Thất bại");
                    JOptionPane.showMessageDialog(BackupSetupPanel.this,
                            LauncherWindow.rootMessage(e), "Lỗi backup", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy, String message) {
        actionButtons.forEach(button -> button.setEnabled(!busy));
        operationStatus.setText(message);
        operationStatus.setForeground(busy ? LauncherTheme.YELLOW : LauncherTheme.TEXT_MUTED);
    }

    private void refreshDriveStatus() {
        boolean connected = drive.hasStoredAuthorization();
        driveStatus.setText(connected ? "● Đã có refresh token" : "● Chưa cấp quyền");
        driveStatus.setForeground(connected ? LauncherTheme.GREEN : LauncherTheme.YELLOW);
    }

    private GridBagConstraints constraints(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(5, x == 0 ? 0 : 12, 5, 0);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }

    @FunctionalInterface
    private interface Work { String run() throws Exception; }
}
