package server;

import recharge.RechargeModels.History;
import recharge.RechargeModels.PackageReward;
import recharge.RechargeModels.PlayerChoice;
import recharge.RechargeModels.RechargePackage;
import recharge.RechargeModels.Settings;
import recharge.RechargeRepository;
import recharge.RechargeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/** Tab nạp thủ công dành cho một admin vận hành server. */
public final class RechargeAdminPanel extends JPanel {
    private final JFrame owner;
    private Settings currentSettings = new Settings();
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<PlayerChoice> playerBox = new JComboBox<>();
    private final JLabel selectedPlayerLabel = new JLabel("Chưa chọn người chơi");

    private final JTextField cashField = new JTextField(16);
    private final JLabel diamondPreview = new JLabel("Kim cương: 0 | Điểm sự kiện: 0");
    private final JButton moneyButton = new JButton("NẠP KIM CƯƠNG");

    private final JComboBox<RechargePackage> packageBox = new JComboBox<>();
    private final JTextArea packagePreview = new JTextArea(8, 38);
    private final JButton packageButton = new JButton("GỬI GÓI NẠP");

    private final JTextField bankName = new JTextField(20);
    private final JTextField accountNumber = new JTextField(20);
    private final JTextField accountHolder = new JTextField(20);
    private final JTextField transferPattern = new JTextField(20);
    private final JTextArea warningText = new JTextArea(3, 30);
    private final JSpinner rateVnd = new JSpinner(new SpinnerNumberModel(1000L, 1L, Long.MAX_VALUE, 1000L));
    private final JSpinner rateGem = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

    private final DefaultTableModel packageModel = readOnlyModel(new String[]{"ID", "Tên gói", "Đang bật"});
    private final JTable packageTable = new JTable(packageModel);
    private final JTextField packageName = new JTextField(22);
    private final JCheckBox packageActive = new JCheckBox("Đang bật", true);
    private final DefaultTableModel rewardModel = new DefaultTableModel(
            new String[]{"Loại", "Item ID", "Số lượng", "Options JSON"}, 0);
    private final JTable rewardTable = new JTable(rewardModel);
    private long editingPackageId;

    private final DefaultTableModel historyModel = readOnlyModel(
            new String[]{"ID", "Thời gian", "Player", "Loại", "Số tiền", "Kim cương", "Điểm", "Gói", "Mail"});

    public RechargeAdminPanel(JFrame owner) {
        super(new BorderLayout(10, 10));
        this.owner = owner;
        setBorder(new EmptyBorder(12, 12, 12, 12));
        try {
            RechargeRepository.ensureSchema();
        } catch (Exception e) {
            showError(e);
        }
        add(createPlayerSearch(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Nạp theo số tiền", createMoneyPanel());
        tabs.addTab("Nạp theo gói", createPackageTopUpPanel());
        tabs.addTab("Cấu hình", createConfigPanel());
        tabs.addTab("Lịch sử", createHistoryPanel());
        add(tabs, BorderLayout.CENTER);
        bindEvents();
        reloadAll();
    }

    private JPanel createPlayerSearch() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Chọn người chơi"));
        JButton search = new JButton("Tìm");
        playerBox.setPreferredSize(new Dimension(270, 32));
        panel.add(new JLabel("Tên hoặc ID:")); panel.add(searchField); panel.add(search);
        panel.add(playerBox); panel.add(selectedPlayerLabel);
        search.addActionListener(this::searchPlayers);
        searchField.addActionListener(this::searchPlayers);
        playerBox.addActionListener(e -> updateSelectedPlayer());
        return panel;
    }

    private JPanel createMoneyPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        GridBagConstraints c = constraints();
        addRow(root, c, 0, "Số tiền đã chuyển (VNĐ):", cashField);
        c.gridy = 1; c.gridx = 1; c.weightx = 1; root.add(diamondPreview, c);
        c.gridy = 2; c.insets = new Insets(22, 6, 6, 6); root.add(moneyButton, c);
        c.gridy = 3; c.insets = new Insets(8, 6, 6, 6);
        root.add(new JLabel("Kim cương nạp theo tiền được tính điểm sự kiện."), c);
        return root;
    }

    private JPanel createPackageTopUpPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reload = new JButton("Tải lại");
        packageBox.setPreferredSize(new Dimension(300, 32));
        top.add(new JLabel("Gói nạp:")); top.add(packageBox); top.add(reload);
        packagePreview.setEditable(false); packagePreview.setLineWrap(true); packagePreview.setWrapStyleWord(true);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new JLabel("Gói nạp không được tính điểm sự kiện.")); bottom.add(packageButton);
        root.add(top, BorderLayout.NORTH); root.add(new JScrollPane(packagePreview), BorderLayout.CENTER); root.add(bottom, BorderLayout.SOUTH);
        reload.addActionListener(e -> loadPackages());
        return root;
    }

    private JComponent createConfigPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ngân hàng và tỷ giá", createBankConfigPanel());
        tabs.addTab("Gói nạp", createPackageConfigPanel());
        return tabs;
    }

    private JPanel createBankConfigPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints c = constraints();
        addRow(form, c, 0, "Ngân hàng:", bankName);
        addRow(form, c, 1, "Số tài khoản:", accountNumber);
        addRow(form, c, 2, "Chủ tài khoản:", accountHolder);
        addRow(form, c, 3, "Nội dung chuyển khoản:", transferPattern);
        addRow(form, c, 4, "Số VNĐ:", rateVnd);
        addRow(form, c, 5, "Số kim cương:", rateGem);
        c.gridx = 0; c.gridy = 6; c.weightx = 0; c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Lưu ý:"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        form.add(new JScrollPane(warningText), c);
        JButton save = new JButton("Lưu cấu hình");
        c.gridy = 7; c.weighty = 0; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.EAST;
        form.add(save, c);
        JLabel hint = new JLabel("Dùng {player_name} để chèn tên nhân vật vào nội dung chuyển khoản.");
        c.gridy = 8; c.anchor = GridBagConstraints.WEST; form.add(hint, c);
        save.addActionListener(e -> saveSettings());
        return form;
    }

    private JPanel createPackageConfigPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(.34);
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(new EmptyBorder(8, 8, 8, 4));
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newButton = new JButton("Gói mới"); JButton reload = new JButton("Tải lại");
        leftButtons.add(newButton); leftButtons.add(reload);
        left.add(leftButtons, BorderLayout.NORTH); left.add(new JScrollPane(packageTable), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setBorder(new EmptyBorder(8, 4, 8, 8));
        JPanel packageFields = new JPanel(new FlowLayout(FlowLayout.LEFT));
        packageFields.add(new JLabel("Tên gói:")); packageFields.add(packageName); packageFields.add(packageActive);
        JButton addReward = new JButton("Thêm phần thưởng"); JButton removeReward = new JButton("Xóa dòng"); JButton save = new JButton("Lưu gói");
        JPanel rewardButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rewardButtons.add(addReward); rewardButtons.add(removeReward); rewardButtons.add(save);
        rewardTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"ITEM", "GEM", "GOLD"})));
        right.add(packageFields, BorderLayout.NORTH); right.add(new JScrollPane(rewardTable), BorderLayout.CENTER); right.add(rewardButtons, BorderLayout.SOUTH);
        split.setLeftComponent(left); split.setRightComponent(right); root.add(split, BorderLayout.CENTER);

        newButton.addActionListener(e -> clearPackageEditor());
        reload.addActionListener(e -> loadPackageConfiguration());
        addReward.addActionListener(e -> rewardModel.addRow(new Object[]{"ITEM", 0, 1, ""}));
        removeReward.addActionListener(e -> { int row = rewardTable.getSelectedRow(); if (row >= 0) rewardModel.removeRow(row); });
        save.addActionListener(e -> savePackage());
        packageTable.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) editSelectedPackage(); });
        return root;
    }

    private JPanel createHistoryPanel() {
        JPanel root = new JPanel(new BorderLayout(5, 5));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        JButton reload = new JButton("Tải lại lịch sử");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.add(reload);
        root.add(top, BorderLayout.NORTH); root.add(new JScrollPane(new JTable(historyModel)), BorderLayout.CENTER);
        reload.addActionListener(e -> loadHistory());
        return root;
    }

    private void bindEvents() {
        cashField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateMoneyPreview(); }
            public void removeUpdate(DocumentEvent e) { updateMoneyPreview(); }
            public void changedUpdate(DocumentEvent e) { updateMoneyPreview(); }
        });
        moneyButton.addActionListener(e -> topUpMoney());
        packageButton.addActionListener(e -> topUpPackage());
        packageBox.addActionListener(e -> updatePackagePreview());
    }

    private void reloadAll() { loadSettings(); loadPackages(); loadPackageConfiguration(); loadHistory(); searchPlayers(null); }

    private void searchPlayers(ActionEvent ignored) {
        runTask(() -> RechargeRepository.searchPlayers(searchField.getText()), players -> {
            playerBox.removeAllItems();
            for (PlayerChoice p : players) playerBox.addItem(p);
            updateSelectedPlayer();
        });
    }

    private void updateSelectedPlayer() {
        PlayerChoice p = selectedPlayer();
        selectedPlayerLabel.setText(p == null ? "Chưa chọn người chơi" : "Đã chọn: " + p.name + " / ID " + p.id);
    }

    private void updateMoneyPreview() {
        try {
            long cash = parseLong(cashField.getText());
            int diamonds = currentSettings.diamondsFor(cash);
            diamondPreview.setText("Kim cương: " + diamonds + " | Điểm sự kiện: " + diamonds);
        } catch (Exception e) {
            diamondPreview.setText("Kim cương: 0 | Điểm sự kiện: 0");
        }
    }

    private void topUpMoney() {
        PlayerChoice player = selectedPlayer();
        if (player == null) { showError(new IllegalArgumentException("Chưa chọn người chơi")); return; }
        long cash;
        try { cash = parseLong(cashField.getText()); }
        catch (Exception e) { showError(new IllegalArgumentException("Số tiền không hợp lệ")); return; }
        int diamonds;
        diamonds = currentSettings.diamondsFor(cash);
        if (diamonds <= 0) { showError(new IllegalArgumentException("Số tiền quá nhỏ so với tỷ giá hiện tại")); return; }
        String text = "Nạp " + RechargeService.formatMoney(cash) + " VNĐ cho " + player
                + "?\nNgười chơi nhận " + diamonds + " kim cương và " + diamonds + " điểm sự kiện.";
        if (confirm(text) != JOptionPane.YES_OPTION) return;
        moneyButton.setEnabled(false);
        runTask(() -> RechargeService.gI().topUpMoney(player, cash), mailId -> {
            moneyButton.setEnabled(true); cashField.setText(""); loadHistory();
            JOptionPane.showMessageDialog(owner, "Nạp thành công. Mail ID: " + mailId);
        }, () -> moneyButton.setEnabled(true));
    }

    private void topUpPackage() {
        PlayerChoice player = selectedPlayer(); RechargePackage pack = (RechargePackage) packageBox.getSelectedItem();
        if (player == null || pack == null) { showError(new IllegalArgumentException("Hãy chọn người chơi và gói nạp")); return; }
        if (confirm("Gửi gói " + pack.name + " cho " + player + "?\nGiao dịch này không cộng điểm sự kiện.") != JOptionPane.YES_OPTION) return;
        packageButton.setEnabled(false);
        runTask(() -> RechargeService.gI().topUpPackage(player, pack), mailId -> {
            packageButton.setEnabled(true); loadHistory();
            JOptionPane.showMessageDialog(owner, "Gửi gói thành công. Mail ID: " + mailId);
        }, () -> packageButton.setEnabled(true));
    }

    private void loadSettings() {
        runTask(RechargeRepository::getSettings, s -> {
            currentSettings = s;
            bankName.setText(s.bankName); accountNumber.setText(s.accountNumber); accountHolder.setText(s.accountHolder);
            transferPattern.setText(s.transferPattern); warningText.setText(s.warning); rateVnd.setValue(s.rateVnd); rateGem.setValue(s.rateGem);
            updateMoneyPreview();
        });
    }

    private void saveSettings() {
        Settings s = new Settings();
        s.bankName = bankName.getText().trim(); s.accountNumber = accountNumber.getText().trim();
        s.accountHolder = accountHolder.getText().trim(); s.transferPattern = transferPattern.getText().trim();
        s.warning = warningText.getText().trim(); s.rateVnd = ((Number) rateVnd.getValue()).longValue(); s.rateGem = ((Number) rateGem.getValue()).intValue();
        runTask(() -> { RechargeRepository.saveSettings(s); return true; }, ok -> {
            currentSettings = s;
            updateMoneyPreview(); JOptionPane.showMessageDialog(owner, "Đã lưu thông tin ngân hàng và tỷ giá.");
        });
    }

    private void loadPackages() {
        runTask(() -> RechargeRepository.listPackages(true), packages -> {
            packageBox.removeAllItems(); for (RechargePackage p : packages) packageBox.addItem(p); updatePackagePreview();
        });
    }

    private void updatePackagePreview() {
        RechargePackage pack = (RechargePackage) packageBox.getSelectedItem();
        if (pack == null) { packagePreview.setText("Chưa có gói nạp đang bật."); return; }
        StringBuilder text = new StringBuilder(pack.name).append("\n\n");
        for (PackageReward r : pack.rewards) text.append("- ").append(r.describe(RechargeRepository.itemName(r.itemId))).append('\n');
        text.append("\nĐiểm sự kiện: 0"); packagePreview.setText(text.toString());
    }

    private void loadPackageConfiguration() {
        runTask(() -> RechargeRepository.listPackages(false), packages -> {
            packageModel.setRowCount(0);
            for (RechargePackage p : packages) packageModel.addRow(new Object[]{p.id, p.name, p.active ? "Có" : "Không"});
            packageTable.putClientProperty("packages", packages);
            clearPackageEditor();
        });
    }

    @SuppressWarnings("unchecked")
    private void editSelectedPackage() {
        int row = packageTable.getSelectedRow(); if (row < 0) return;
        List<RechargePackage> packages = (List<RechargePackage>) packageTable.getClientProperty("packages");
        if (packages == null || row >= packages.size()) return;
        RechargePackage pack = packages.get(row); editingPackageId = pack.id; packageName.setText(pack.name); packageActive.setSelected(pack.active);
        rewardModel.setRowCount(0);
        for (PackageReward r : pack.rewards) rewardModel.addRow(new Object[]{r.type, r.itemId, r.amount, r.optionsData == null ? "" : r.optionsData});
    }

    private void clearPackageEditor() {
        editingPackageId = 0; packageName.setText(""); packageActive.setSelected(true); rewardModel.setRowCount(0);
        packageTable.clearSelection();
    }

    private void savePackage() {
        if (rewardTable.isEditing()) rewardTable.getCellEditor().stopCellEditing();
        try {
            RechargePackage pack = new RechargePackage(); pack.id = editingPackageId; pack.name = packageName.getText().trim(); pack.active = packageActive.isSelected();
            for (int row = 0; row < rewardModel.getRowCount(); row++) {
                PackageReward reward = new PackageReward(); reward.type = stringAt(rewardModel, row, 0).toUpperCase();
                reward.itemId = intAt(rewardModel, row, 1); reward.amount = intAt(rewardModel, row, 2); reward.optionsData = stringAt(rewardModel, row, 3);
                pack.rewards.add(reward);
            }
            runTask(() -> RechargeRepository.savePackage(pack), id -> {
                JOptionPane.showMessageDialog(owner, "Đã lưu gói nạp ID " + id + "."); loadPackageConfiguration(); loadPackages();
            });
        } catch (Exception e) { showError(e); }
    }

    private void loadHistory() {
        runTask(() -> RechargeRepository.listHistory(100), histories -> {
            historyModel.setRowCount(0);
            for (History h : histories) historyModel.addRow(new Object[]{h.id, h.createdAt, h.playerName + " [" + h.playerId + "]",
                    h.type, h.cashAmount == 0 ? "-" : RechargeService.formatMoney(h.cashAmount), h.diamondTotal,
                    h.eventPoints, h.packageName == null ? "-" : h.packageName, h.mailId});
        });
    }

    private PlayerChoice selectedPlayer() { return (PlayerChoice) playerBox.getSelectedItem(); }
    private int confirm(String text) { return JOptionPane.showConfirmDialog(owner, text, "Xác nhận nạp", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); }
    private void showError(Throwable e) { JOptionPane.showMessageDialog(owner, rootMessage(e), "Nạp tiền", JOptionPane.ERROR_MESSAGE); }

    private static GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6, 6, 6, 6); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL; return c;
    }
    private static void addRow(JPanel panel, GridBagConstraints c, int row, String label, Component value) {
        c.gridy = row; c.gridx = 0; c.weightx = 0; panel.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; panel.add(value, c);
    }
    private static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
    }
    private static String stringAt(DefaultTableModel model, int row, int col) {
        Object value = model.getValueAt(row, col); return value == null ? "" : value.toString().trim();
    }
    private static int intAt(DefaultTableModel model, int row, int col) { return Integer.parseInt(stringAt(model, row, col)); }
    private static long parseLong(String text) { return Long.parseLong(text.replace(".", "").replace(",", "").trim()); }
    private static String rootMessage(Throwable error) {
        Throwable current = error; while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.toString() : current.getMessage();
    }

    private <T> void runTask(Task<T> task, Result<T> result) { runTask(task, result, () -> {}); }
    private <T> void runTask(Task<T> task, Result<T> result, Runnable finishedOnError) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return task.run(); }
            @Override protected void done() {
                try { result.accept(get()); }
                catch (Exception e) { finishedOnError.run(); showError(e); }
            }
        }.execute();
    }
    @FunctionalInterface private interface Task<T> { T run() throws Exception; }
    @FunctionalInterface private interface Result<T> { void accept(T value); }
}
