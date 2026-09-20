package setup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Form setup truc quan, ghi vao data/config/sever.properties. */
public final class SetupWindow extends JDialog {

    private final SetupManager setupManager;
    private final Map<String, JTextField> fields = new LinkedHashMap<>();
    private final JCheckBox nettyProxy = new JCheckBox("Bật Netty DDoS proxy");
    private final JProgressBar progress = new JProgressBar();
    private final JLabel status = new JLabel("Các trường có dấu * là bắt buộc");
    private final JButton testButton = new JButton("Kiểm tra kết nối");
    private final JButton saveButton = new JButton("Lưu thiết lập");
    private final JButton cancelButton = new JButton("Đóng");
    private final BackupSetupPanel backupPanel = new BackupSetupPanel();

    public SetupWindow(java.awt.Window owner, SetupManager setupManager) {
        super(owner, "Thiết lập NRO Server", ModalityType.APPLICATION_MODAL);
        this.setupManager = setupManager;
        LauncherTheme.install();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(700, 570));
        setSize(740, 610);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(LauncherTheme.BG_MAIN);

        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
        loadCurrentValues();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 26, 4, 26));
        JLabel title = new JLabel("THIẾT LẬP HỆ THỐNG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Cấu hình được dùng chung bởi launcher và server.jar");
        subtitle.setForeground(LauncherTheme.TEXT_MUTED);
        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);
        return panel;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        tabs.addTab("Server", createServerForm());
        tabs.addTab("Database", createDatabaseForm());
        tabs.addTab("Backup & Google Drive", backupPanel);
        return tabs;
    }

    private JPanel createServerForm() {
        JPanel form = formPanel();
        int row = 0;
        row = addField(form, row, "server.name", "Tên server *", false);
        row = addField(form, row, "server.ip", "IP/domain public *", false);
        row = addField(form, row, "server.port", "Cổng game *", false);
        row = addField(form, row, "server.bind.ip", "IP bind", false);
        row = addField(form, row, "server.backendport", "Cổng backend *", false);
        GridBagConstraints check = constraints(1, row);
        check.anchor = GridBagConstraints.WEST;
        nettyProxy.setOpaque(false);
        form.add(nettyProxy, check);
        return form;
    }

    private JPanel createDatabaseForm() {
        JPanel form = formPanel();
        int row = 0;
        row = addField(form, row, "database.driver", "JDBC driver *", false);
        row = addField(form, row, "database.host", "Database host *", false);
        row = addField(form, row, "database.port", "Database port *", false);
        row = addField(form, row, "database.name", "Tên database *", false);
        row = addField(form, row, "database.user", "Database user *", false);
        addField(form, row, "database.pass", "Database password", true);
        return form;
    }

    private JPanel formPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(LauncherTheme.BG_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LauncherTheme.BORDER),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        return form;
    }

    private int addField(JPanel form, int row, String key, String label, boolean password) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setForeground(LauncherTheme.TEXT_MUTED);
        JTextField field = password ? new JPasswordField() : new JTextField();
        field.setPreferredSize(new Dimension(390, 34));
        fields.put(key, field);

        GridBagConstraints left = constraints(0, row);
        left.anchor = GridBagConstraints.WEST;
        left.weightx = 0;
        form.add(fieldLabel, left);

        GridBagConstraints right = constraints(1, row);
        right.fill = GridBagConstraints.HORIZONTAL;
        right.weightx = 1;
        form.add(field, right);
        return row + 1;
    }

    private GridBagConstraints constraints(int x, int y) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.insets = new Insets(7, x == 0 ? 0 : 18, 7, 0);
        return constraints;
    }

    private JPanel createFooter() {
        JPanel outer = new JPanel(new BorderLayout(10, 8));
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(0, 26, 22, 26));
        progress.setIndeterminate(true);
        progress.setVisible(false);
        outer.add(progress, BorderLayout.NORTH);
        status.setForeground(LauncherTheme.TEXT_MUTED);
        outer.add(status, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        saveButton.setBackground(LauncherTheme.BLUE_DARK);
        buttons.add(cancelButton);
        buttons.add(testButton);
        buttons.add(saveButton);
        outer.add(buttons, BorderLayout.EAST);

        cancelButton.addActionListener(e -> dispose());
        testButton.addActionListener(e -> execute(false));
        saveButton.addActionListener(e -> execute(true));
        return outer;
    }

    private void loadCurrentValues() {
        Properties values = setupManager.getSavedConfiguration();
        set("server.name", values, "Nro Luna");
        set("server.ip", values, "127.0.0.1");
        set("server.port", values, "14445");
        set("server.bind.ip", values, "");
        set("server.backendport", values, "14446");
        set("database.driver", values, "com.mysql.cj.jdbc.Driver");
        set("database.host", values, "localhost");
        set("database.port", values, "3306");
        set("database.name", values, "nro");
        set("database.user", values, "root");
        set("database.pass", values, "");
        nettyProxy.setSelected(Boolean.parseBoolean(
                values.getProperty("server.usenettyproxy", "true")));
    }

    private void set(String key, Properties values, String fallback) {
        fields.get(key).setText(values.getProperty(key, fallback));
    }

    private Map<String, String> collectValues() {
        Map<String, String> values = new LinkedHashMap<>();
        fields.forEach((key, field) -> values.put(key,
                key.equals("database.pass") ? field.getText() : field.getText().trim()));
        values.put("server.usenettyproxy", Boolean.toString(nettyProxy.isSelected()));
        return values;
    }

    private void execute(boolean save) {
        Map<String, String> values = collectValues();
        setBusy(true, save ? "Đang kiểm tra và lưu cấu hình..." : "Đang kiểm tra kết nối...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (save) {
                    backupPanel.saveConfiguration();
                    setupManager.saveConfiguration(values);
                } else {
                    setupManager.testConfiguration(values);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    setBusy(false, save ? "Đã lưu thiết lập thành công" : "Kết nối database thành công");
                    JOptionPane.showMessageDialog(SetupWindow.this,
                            save ? "Thiết lập đã được kiểm tra và lưu thành công."
                                    : "Kết nối database thành công.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    if (save) {
                        dispose();
                    }
                } catch (Exception e) {
                    setBusy(false, "Không thể hoàn tất");
                    JOptionPane.showMessageDialog(SetupWindow.this,
                            LauncherWindow.rootMessage(e), "Lỗi thiết lập",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy, String message) {
        progress.setVisible(busy);
        testButton.setEnabled(!busy);
        saveButton.setEnabled(!busy);
        cancelButton.setEnabled(!busy);
        fields.values().forEach(field -> field.setEnabled(!busy));
        nettyProxy.setEnabled(!busy);
        status.setText(message);
        status.setForeground(busy ? LauncherTheme.YELLOW : LauncherTheme.TEXT_MUTED);
    }
}
