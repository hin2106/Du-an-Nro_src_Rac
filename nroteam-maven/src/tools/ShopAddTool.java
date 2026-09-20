package tools;
import daos.ShopDAO;
import data.AlyraManager;
import server.Manager;
import system.Template.ItemTemplate;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ShopAddTool extends JFrame {

    private JTextField txtNpcId, txtTagName, txtTypeShop;
    private JTextField txtTabName;
    private JTextField txtTempId, txtCost, txtIconSpec, txtTypeSell;
    private JLabel lblItemName;
    private JCheckBox chkIsNew;
    private JComboBox<String> cbOptions;
    private JTextField txtParam;
    private JLabel lblOptionName;

    private JComboBox<String> cbShops, cbTabs, cbItemShops;
    private JTable tblItems, tblOptions, tblItemShopOptions, tblShops, tblTabs, tblItemShops;
    private DefaultTableModel modelItems, modelOptions, modelItemShopOptions, modelShops, modelTabs, modelItemShops;
    
    private static final Font FONT_TIEU_DE = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_CHU_THUONG = new Font("Segoe UI", Font.PLAIN, 10);
    private static final Font FONT_THONG_KE = new Font("Segoe UI", Font.PLAIN, 9);


    private JTextArea txtLog;

    private int currentShopId = -1;
    private int currentTabId = -1;
    private int currentItemShopId = -1;
    private static final Map<Integer, ImageIcon> ICON_CACHE = new HashMap<>();

    public ShopAddTool() {
        initComponents();
        loadShops();
        loadItemTemplates();
        loadItemOptions();
    }

    private void initComponents() {
        setTitle("Shop Management Tool - Quản Lý Shop");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.5);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createLeftPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        JPanel shopAndTabPanel = createShopAndTabPanel();
        JScrollPane shopAndTabScroll = new JScrollPane(shopAndTabPanel);
        shopAndTabScroll.setBorder(null);
        shopAndTabScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        shopAndTabScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("1. Shop & Tab", shopAndTabScroll);
        JPanel itemAndOptionPanel = createItemAndOptionPanel();
        JScrollPane itemAndOptionScroll = new JScrollPane(itemAndOptionPanel);
        itemAndOptionScroll.setBorder(null);
        itemAndOptionScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        itemAndOptionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane.addTab("2. Item Shop & Option", itemAndOptionScroll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        return panel;
    }

    private JPanel createShopAndTabPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel form bên trái
        JPanel leftForm = new JPanel();
        leftForm.setLayout(new BoxLayout(leftForm, BoxLayout.Y_AXIS));

        // Panel thêm Shop
        JPanel shopForm = new JPanel(new GridBagLayout());
        shopForm.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 75)),
            "Thêm Shop Mới",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TIEU_DE,
            Color.LIGHT_GRAY
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        shopForm.add(new JLabel("NPC ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNpcId = new JTextField(15);
        shopForm.add(txtNpcId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        shopForm.add(new JLabel("Tag Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTagName = new JTextField(15);
        shopForm.add(txtTagName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        shopForm.add(new JLabel("Type Shop:"), gbc);
        gbc.gridx = 1;
        txtTypeShop = new JTextField(15);
        shopForm.add(txtTypeShop, gbc);

        JButton btnAddShop = new JButton("Thêm Shop Mới");
        btnAddShop.addActionListener(e -> addShop());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        shopForm.add(btnAddShop, gbc);

        leftForm.add(shopForm);
        leftForm.add(Box.createVerticalStrut(10));

        // Panel chọn Shop & Tab
        JPanel selectPanel = new JPanel(new GridBagLayout());
        selectPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 75)),
            "Chọn Shop / Tab",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TIEU_DE,
            Color.LIGHT_GRAY
        ));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        selectPanel.add(new JLabel("Chọn Shop:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbShops = new JComboBox<>();
        cbShops.addActionListener(e -> onShopSelected());
        selectPanel.add(cbShops, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        selectPanel.add(new JLabel("Chọn Tab:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbTabs = new JComboBox<>();
        cbTabs.addActionListener(e -> onTabSelected());
        selectPanel.add(cbTabs, gbc);

        leftForm.add(selectPanel);
        leftForm.add(Box.createVerticalStrut(10));

        // Panel thêm Tab
        JPanel tabForm = new JPanel(new GridBagLayout());
        tabForm.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 75)),
            "Thêm Tab Mới",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_TIEU_DE,
            Color.LIGHT_GRAY
        ));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        tabForm.add(new JLabel("Tên Tab:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTabName = new JTextField(15);
        tabForm.add(txtTabName, gbc);

        JButton btnAddTab = new JButton("Thêm Tab Mới");
        btnAddTab.addActionListener(e -> addTab());
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        tabForm.add(btnAddTab, gbc);

        leftForm.add(tabForm);

        // Panel bên phải: bảng Shops và Tabs (sử dụng JTabbedPane)
        JTabbedPane tableTabbedPane = new JTabbedPane();
        tableTabbedPane.setFont(FONT_CHU_THUONG);

        // Bảng Shops
        String[] shopColumns = { "ID", "NPC ID", "Tag Name", "Type Shop" };
        modelShops = new DefaultTableModel(shopColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblShops = new JTable(modelShops);
        tblShops.setFont(FONT_THONG_KE);
        tblShops.setRowHeight(22);
        tblShops.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblShops.getSelectedRow();
                if (row >= 0) {
                    String id = String.valueOf(modelShops.getValueAt(row, 0));
                    for (int i = 0; i < cbShops.getItemCount(); i++) {
                        if (cbShops.getItemAt(i).startsWith("ID: " + id + " |")) {
                            cbShops.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });
        JScrollPane shopScroll = new JScrollPane(tblShops);
        tableTabbedPane.addTab("Danh Sách Shops", shopScroll);

        // Bảng Tabs
        String[] tabColumns = { "ID", "Shop ID", "Tên Tab" };
        modelTabs = new DefaultTableModel(tabColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTabs = new JTable(modelTabs);
        tblTabs.setFont(FONT_THONG_KE);
        tblTabs.setRowHeight(22);
        tblTabs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblTabs.getSelectedRow();
                if (row >= 0) {
                    String id = String.valueOf(modelTabs.getValueAt(row, 0));
                    for (int i = 0; i < cbTabs.getItemCount(); i++) {
                        if (cbTabs.getItemAt(i).startsWith("ID: " + id + " |")) {
                            cbTabs.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        });
        JScrollPane tabScroll = new JScrollPane(tblTabs);
        tableTabbedPane.addTab("Danh Sách Tabs", tabScroll);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftForm, tableTabbedPane);
        splitPane.setDividerLocation(380);
        splitPane.setResizeWeight(0.4);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createItemAndOptionPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel leftFormPanel = new JPanel();
        leftFormPanel.setLayout(new BoxLayout(leftFormPanel, BoxLayout.Y_AXIS));
        JPanel itemPanel = new JPanel();
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Thêm Item Shop Mới"),
                new EmptyBorder(10, 10, 10, 10)));
        itemPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        itemPanel.add(new JLabel("Item ID (Temp ID):"), gbc);
        gbc.gridx = 1;
        JPanel itemIdPanel = new JPanel(new BorderLayout(5, 0));
        txtTempId = new JTextField(12);
        txtTempId.addActionListener(e -> updateItemName());
        itemIdPanel.add(txtTempId, BorderLayout.CENTER);
        JButton btnFindItem = new JButton("Tìm");
        btnFindItem.addActionListener(e -> updateItemName());
        itemIdPanel.add(btnFindItem, BorderLayout.EAST);
        itemPanel.add(itemIdPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        lblItemName = new JLabel("Tên item: (chưa chọn)");
        lblItemName.setForeground(new Color(100, 100, 100));
        itemPanel.add(lblItemName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        itemPanel.add(new JLabel("Giá (Cost):"), gbc);
        gbc.gridx = 1;
        txtCost = new JTextField(15);
        itemPanel.add(txtCost, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        itemPanel.add(new JLabel("Icon Spec:"), gbc);
        gbc.gridx = 1;
        txtIconSpec = new JTextField(15);
        txtIconSpec.setText("0");
        itemPanel.add(txtIconSpec, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        itemPanel.add(new JLabel("Type Sell (0=Vàng, 1=Gem):"), gbc);
        gbc.gridx = 1;
        txtTypeSell = new JTextField(15);
        txtTypeSell.setText("0");
        itemPanel.add(txtTypeSell, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        chkIsNew = new JCheckBox("Item Mới");
        itemPanel.add(chkIsNew, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btnAddItem = new JButton("Thêm Item Vào Shop");
        btnAddItem.addActionListener(e -> addItem());
        itemPanel.add(btnAddItem, gbc);

        leftFormPanel.add(itemPanel);
        leftFormPanel.add(Box.createVerticalStrut(10));

        // Panel Option
        JPanel optionPanel = new JPanel();
        optionPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Thêm Option Mới"),
                new EmptyBorder(10, 10, 10, 10)));
        optionPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        optionPanel.add(new JLabel("Chọn Option:"), gbc);
        gbc.gridx = 1;
        cbOptions = new JComboBox<>();
        cbOptions.addActionListener(e -> updateOptionName());
        optionPanel.add(cbOptions, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        lblOptionName = new JLabel("Tên option: (chưa chọn)");
        lblOptionName.setForeground(new Color(100, 100, 100));
        optionPanel.add(lblOptionName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        optionPanel.add(new JLabel("Param:"), gbc);
        gbc.gridx = 1;
        txtParam = new JTextField(15);
        optionPanel.add(txtParam, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btnAddOption = new JButton("Thêm Option");
        btnAddOption.addActionListener(e -> addOption());
        optionPanel.add(btnAddOption, gbc);

        leftFormPanel.add(optionPanel);

        // Panel bên phải: Bảng hiển thị Item Shops và Options
        JPanel rightTablePanel = new JPanel(new BorderLayout(5, 5));

        // Tabbed pane cho bảng Item Shops và Options
        JTabbedPane tableTabbedPane = new JTabbedPane();

        // Bảng Item Shops
        String[] itemShopColumns = { "ID", "Item ID", "Tên Item", "Giá", "Icon Spec", "Type Sell", "Item Mới" };
        modelItemShops = new DefaultTableModel(itemShopColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho phép edit: Giá (3), Icon Spec (4), Type Sell (5), Item Mới (6)
                return column >= 3 && column <= 6;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Cột "Item Mới" là Boolean
                if (columnIndex == 6) {
                    return Boolean.class;
                }
                return String.class;
            }
        };
        tblItemShops = new JTable(modelItemShops);
        tblItemShops.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Thêm listener để detect khi edit xong
        modelItemShops.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row >= 0 && column >= 3 && column <= 6) {
                    handleItemShopCellEdit(row, column);
                }
            }
        });

        tblItemShops.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblItemShops.getSelectedRow();
                if (row >= 0) {
                    String id = String.valueOf(modelItemShops.getValueAt(row, 0));
                    currentItemShopId = Integer.parseInt(id);
                    loadItemShopOptions(currentItemShopId);
                    log("Đã chọn item shop ID: " + currentItemShopId);
                }
            }
        });
        tblItemShops.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblItemShops.getColumnModel().getColumn(1).setPreferredWidth(70);
        tblItemShops.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblItemShops.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblItemShops.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblItemShops.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblItemShops.getColumnModel().getColumn(6).setPreferredWidth(80);

        JPanel itemShopTablePanel = new JPanel(new BorderLayout());
        JScrollPane itemShopTableScroll = new JScrollPane(tblItemShops);
        itemShopTablePanel.add(itemShopTableScroll, BorderLayout.CENTER);

        // Buttons panel cho Item Shop
        JPanel itemShopButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton btnDeleteItemShop = new JButton("Xóa Item Shop");
        btnDeleteItemShop.addActionListener(e -> {
            int row = tblItemShops.getSelectedRow();
            if (row >= 0) {
                deleteItemShop(row);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn item shop để xóa!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        itemShopButtonsPanel.add(btnDeleteItemShop);
        itemShopTablePanel.add(itemShopButtonsPanel, BorderLayout.SOUTH);

        tableTabbedPane.addTab("Danh Sách Item Shops", itemShopTablePanel);

        // Bảng Options của Item Shop đã chọn
        String[] itemShopOptionColumns = { "Option ID", "Tên Option", "Param" };
        modelItemShopOptions = new DefaultTableModel(itemShopOptionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho phép edit cột Param (2)
                return column == 2;
            }
        };
        tblItemShopOptions = new JTable(modelItemShopOptions);
        tblItemShopOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Thêm listener để detect khi edit Param xong
        modelItemShopOptions.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row >= 0 && column == 2 && currentItemShopId > 0) {
                    handleOptionParamEdit(row);
                }
            }
        });

        tblItemShopOptions.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblItemShopOptions.getColumnModel().getColumn(1).setPreferredWidth(300);
        tblItemShopOptions.getColumnModel().getColumn(2).setPreferredWidth(100);

        JPanel optionsTablePanel = new JPanel(new BorderLayout());
        JScrollPane optionsScroll = new JScrollPane(tblItemShopOptions);
        optionsTablePanel.add(optionsScroll, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton btnEditOption = new JButton("Sửa Option");
        JButton btnDeleteOption = new JButton("Xóa Option");
        btnEditOption.addActionListener(e -> {
            int row = tblItemShopOptions.getSelectedRow();
            if (row >= 0) {
                editOption(row);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn option để sửa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnDeleteOption.addActionListener(e -> {
            int row = tblItemShopOptions.getSelectedRow();
            if (row >= 0) {
                deleteOption(row);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn option để xóa!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonsPanel.add(btnEditOption);
        buttonsPanel.add(btnDeleteOption);
        optionsTablePanel.add(buttonsPanel, BorderLayout.SOUTH);

        tableTabbedPane.addTab("Options của Item Shop", optionsTablePanel);

        rightTablePanel.add(tableTabbedPane, BorderLayout.CENTER);

        // Split pane để chia form và bảng
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftFormPanel, rightTablePanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.4);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        // Tabbed pane cho items và options
        JTabbedPane tabbedPane = new JTabbedPane();
        // Item templates table
        JPanel itemTablePanel = new JPanel(new BorderLayout());
        itemTablePanel.setBorder(new TitledBorder("Danh Sách Item Templates"));

        String[] columns = { "Icon", "ID", "Tên Item", "Type", "Level", "Gender" };
        modelItems = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) { return column == 0 ? ImageIcon.class : Object.class; }
        };
        tblItems = new JTable(modelItems);
        tblItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblItems.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblItems.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tblItems.convertRowIndexToModel(row);
                    String id = String.valueOf(modelItems.getValueAt(modelRow, 1));
                    String name = String.valueOf(modelItems.getValueAt(modelRow, 2));
                    txtTempId.setText(id);
                    lblItemName.setText("Tên item: " + name);
                    lblItemName.setForeground(Color.BLACK);
                }
            }
        });
        tblItems.setRowHeight(38);
        tblItems.getColumnModel().getColumn(0).setPreferredWidth(42);
        tblItems.getColumnModel().getColumn(1).setPreferredWidth(70);
        tblItems.getColumnModel().getColumn(2).setPreferredWidth(250);
        tblItems.getColumnModel().getColumn(2).setPreferredWidth(60);
        tblItems.getColumnModel().getColumn(3).setPreferredWidth(60);
        tblItems.getColumnModel().getColumn(4).setPreferredWidth(60);
        tblItems.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(modelItems);
        tblItems.setRowSorter(sorter);
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("Tìm kiếm:"), BorderLayout.WEST);
        JTextField txtSearchItem = new JTextField();
        txtSearchItem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterItems();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterItems();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterItems();
            }

            private void filterItems() {
                String search = txtSearchItem.getText().trim();
                if (search.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + search, 1, 2, 3, 5));
                }
            }
        });
        searchPanel.add(txtSearchItem, BorderLayout.CENTER);
        itemTablePanel.add(searchPanel, BorderLayout.NORTH);

        JScrollPane itemScrollPane = new JScrollPane(tblItems);
        itemTablePanel.add(itemScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Items", itemTablePanel);

        // Options table
        JPanel optionTablePanel = new JPanel(new BorderLayout());
        optionTablePanel.setBorder(new TitledBorder("Danh Sách Item Options"));

        String[] optionColumns = { "ID", "Tên Option" };
        modelOptions = new DefaultTableModel(optionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblOptions = new JTable(modelOptions);
        tblOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Thêm TableRowSorter để hỗ trợ search
        javax.swing.table.TableRowSorter<DefaultTableModel> optionSorter = new javax.swing.table.TableRowSorter<>(
                modelOptions);
        tblOptions.setRowSorter(optionSorter);

        tblOptions.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblOptions.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tblOptions.convertRowIndexToModel(row);
                    String id = String.valueOf(modelOptions.getValueAt(modelRow, 0));
                    for (int i = 0; i < cbOptions.getItemCount(); i++) {
                        String item = cbOptions.getItemAt(i);
                        if (item.startsWith("ID: " + id + " |")) {
                            cbOptions.setSelectedIndex(i);
                            break;
                        }
                    }
                    updateOptionName();
                }
            }
        });
        // Set column widths
        tblOptions.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblOptions.getColumnModel().getColumn(1).setPreferredWidth(400);

        JPanel optionSearchPanel = new JPanel(new BorderLayout(5, 0));
        optionSearchPanel.add(new JLabel("Tìm kiếm:"), BorderLayout.WEST);
        JTextField txtSearchOption = new JTextField();
        txtSearchOption.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterOptions();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterOptions();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterOptions();
            }

            private void filterOptions() {
                String search = txtSearchOption.getText().trim();
                if (search.isEmpty()) {
                    optionSorter.setRowFilter(null);
                } else {
                    // Tìm kiếm theo ID và tên (cột 0, 1)
                    optionSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + search, 0, 1));
                }
            }
        });
        optionSearchPanel.add(txtSearchOption, BorderLayout.CENTER);
        optionTablePanel.add(optionSearchPanel, BorderLayout.NORTH);

        JScrollPane optionScrollPane = new JScrollPane(tblOptions);
        optionTablePanel.add(optionScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Options", optionTablePanel);

        // Log area
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("Log"));
        txtLog = new JTextArea(10, 30);
        txtLog.setEditable(false);
        txtLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(txtLog);
        logPanel.add(logScroll, BorderLayout.CENTER);

        // Clear log button
        JButton btnClearLog = new JButton("Xóa Log");
        btnClearLog.addActionListener(e -> txtLog.setText(""));
        logPanel.add(btnClearLog, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, logPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.6);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadShops() {
        try (Connection con = AlyraManager.getConnection()) {
            List<String[]> shops = ShopDAO.getAllShops(con);
            cbShops.removeAllItems();
            cbShops.addItem("-- Chọn Shop --");

            // Load vào combo box
            for (String[] shop : shops) {
                cbShops.addItem("ID: " + shop[0] + " | NPC: " + shop[1] + " | " + shop[2]);
            }

            // Load vào bảng
            if (modelShops != null) {
                modelShops.setRowCount(0);
                for (String[] shop : shops) {
                    // shop[0] = id, shop[1] = npc_id, shop[2] = tag_name, shop[3] = type_shop
                    modelShops.addRow(new Object[] { shop[0], shop[1], shop[2], shop.length > 3 ? shop[3] : "0" });
                }
            }

            log("Đã tải " + shops.size() + " shops");
        } catch (Exception e) {
            logError("Lỗi khi tải shops: " + e.getMessage());
        }
    }

    private void loadItemTemplates() {
        modelItems.setRowCount(0);
        for (ItemTemplate item : Manager.ITEM_TEMPLATES) {
            modelItems.addRow(new Object[] {
                    iconFor(item.iconID),
                    item.id,
                    item.name != null ? item.name : "(Không có tên)",
                    item.type,
                    item.level,
                    item.gender
            });
        }
        log("Đã tải " + Manager.ITEM_TEMPLATES.size() + " item templates");
    }

    private static ImageIcon iconFor(int iconId) {
        if (iconId <= 0) return null;
        if (ICON_CACHE.containsKey(iconId)) return ICON_CACHE.get(iconId);
        ImageIcon icon = null;
        for (String base : new String[]{"data/icon/x4/", "nroteam-maven/data/icon/x4/"}) {
            File file = new File(base + iconId + ".png");
            if (file.isFile()) {
                icon = new ImageIcon(new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
                break;
            }
        }
        ICON_CACHE.put(iconId, icon);
        return icon;
    }

    private void loadItemOptions() {
        try (Connection con = AlyraManager.getConnection()) {
            List<String[]> options = ShopDAO.getAllItemOptions(con);
            cbOptions.removeAllItems();
            cbOptions.addItem("-- Chọn Option --");
            modelOptions.setRowCount(0);
            for (String[] option : options) {
                String displayText = "ID: " + option[0] + " | " + option[1];
                cbOptions.addItem(displayText);
                modelOptions.addRow(new Object[] {
                        Integer.parseInt(option[0]),
                        option[1]
                });
            }
            log("Đã tải " + options.size() + " item options");
        } catch (Exception e) {
            logError("Lỗi khi tải options: " + e.getMessage());
        }
    }

    private void loadItemShops() {
        try (Connection con = AlyraManager.getConnection()) {
            java.sql.PreparedStatement ps = con.prepareStatement(
                    "SELECT its.id, its.temp_id, it.name, its.cost " +
                            "FROM item_shop its " +
                            "LEFT JOIN item_template it ON it.id = its.temp_id " +
                            "WHERE its.is_sell = 1 " +
                            "ORDER BY its.id DESC LIMIT 100");
            java.sql.ResultSet rs = ps.executeQuery();
            cbItemShops.removeAllItems();
            cbItemShops.addItem("-- Chọn Item Shop --");

            while (rs.next()) {
                int id = rs.getInt("id");
                int tempId = rs.getInt("temp_id");
                String itemName = rs.getString("name");
                int cost = rs.getInt("cost");
                String display = "ID: " + id + " | Item: " + (itemName != null ? itemName : "ID:" + tempId) + " | Giá: "
                        + cost;
                cbItemShops.addItem(display);
            }
            rs.close();
            ps.close();
            log("Đã tải danh sách item shops");
        } catch (Exception e) {
            logError("Lỗi khi tải item shops: " + e.getMessage());
        }
    }

    private void onItemShopSelected() {
        int index = cbItemShops.getSelectedIndex();
        if (index <= 0) {
            currentItemShopId = -1;
            modelItemShopOptions.setRowCount(0);
            return;
        }

        String selected = (String) cbItemShops.getSelectedItem();
        if (selected != null && selected.startsWith("ID: ")) {
            int indexOfPipe = selected.indexOf(" |");
            if (indexOfPipe > 4) {
                String idStr = selected.substring(4, indexOfPipe);
                currentItemShopId = Integer.parseInt(idStr);
                loadItemShopOptions(currentItemShopId);
            }
        }
    }

    private void loadItemShopOptions(int itemShopId) {
        try (Connection con = AlyraManager.getConnection()) {
            List<String[]> options = ShopDAO.getItemShopOptions(con, itemShopId);
            modelItemShopOptions.setRowCount(0);

            for (String[] option : options) {
                modelItemShopOptions.addRow(new Object[] {
                        Integer.parseInt(option[0]),
                        option[1],
                        Integer.parseInt(option[2])
                });
            }
            log("Đã tải " + options.size() + " options cho item shop ID: " + itemShopId);
        } catch (Exception e) {
            logError("Lỗi khi tải options: " + e.getMessage());
        }
    }

    private void editOption(int row) {
        if (currentItemShopId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn item shop trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int optionId = (Integer) modelItemShopOptions.getValueAt(row, 0);
        int currentParam = (Integer) modelItemShopOptions.getValueAt(row, 2);
        String optionName = (String) modelItemShopOptions.getValueAt(row, 1);

        String newParamStr = JOptionPane.showInputDialog(this,
                "Sửa Param cho option:\n" + optionName + "\n\nParam hiện tại: " + currentParam,
                "Sửa Option",
                JOptionPane.QUESTION_MESSAGE);

        if (newParamStr != null && !newParamStr.trim().isEmpty()) {
            try {
                int newParam = Integer.parseInt(newParamStr.trim());
                try (Connection con = AlyraManager.getConnection()) {
                    boolean success = ShopDAO.updateItemShopOption(con, currentItemShopId, optionId, newParam);
                    if (success) {
                        // Refresh bảng options
                        loadItemShopOptions(currentItemShopId);
                        log("✓ Đã cập nhật option - Option ID: " + optionId + ", Param: " + currentParam + " -> "
                                + newParam);
                        JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        logError("✗ Không thể cập nhật option");
                        JOptionPane.showMessageDialog(this, "Không thể cập nhật option!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Param phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                logError("✗ Lỗi: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteOption(int row) {
        if (currentItemShopId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn item shop trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int optionId = (Integer) modelItemShopOptions.getValueAt(row, 0);
        String optionName = (String) modelItemShopOptions.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa option:\n" + optionName + " (ID: " + optionId + ")",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = AlyraManager.getConnection()) {
                boolean success = ShopDAO.deleteItemShopOption(con, currentItemShopId, optionId);
                if (success) {
                    loadItemShopOptions(currentItemShopId);
                    log("✓ Đã xóa option - Option ID: " + optionId + ", Tên: " + optionName);
                    JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    logError("✗ Không thể xóa option");
                    JOptionPane.showMessageDialog(this, "Không thể xóa option!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                logError("✗ Lỗi: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteItemShop(int row) {
        if (currentTabId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tab trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idStr = String.valueOf(modelItemShops.getValueAt(row, 0));
        int itemShopId = Integer.parseInt(idStr);
        String itemName = String.valueOf(modelItemShops.getValueAt(row, 2));
        String itemId = String.valueOf(modelItemShops.getValueAt(row, 1));

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa item shop:\n" +
                        "ID: " + itemShopId + "\n" +
                        "Item: " + itemName + " (ID: " + itemId + ")\n\n" +
                        "Lưu ý: Tất cả options của item shop này cũng sẽ bị xóa!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = AlyraManager.getConnection()) {
                boolean success = ShopDAO.deleteItemShop(con, itemShopId);
                if (success) {
                    // Reset currentItemShopId nếu đang chọn item shop bị xóa
                    if (currentItemShopId == itemShopId) {
                        currentItemShopId = -1;
                        modelItemShopOptions.setRowCount(0);
                    }
                    // Refresh bảng item shops
                    loadItemShopsByTabId(currentTabId);
                    log("✓ Đã xóa item shop - ID: " + itemShopId + ", Item: " + itemName + " (ID: " + itemId + ")");
                    JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    logError("✗ Không thể xóa item shop");
                    JOptionPane.showMessageDialog(this, "Không thể xóa item shop!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                logError("✗ Lỗi: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateItemName() {
        String tempIdStr = txtTempId.getText().trim();
        if (tempIdStr.isEmpty()) {
            lblItemName.setText("Tên item: (chưa chọn)");
            lblItemName.setForeground(new Color(100, 100, 100));
            return;
        }

        try {
            short tempId = Short.parseShort(tempIdStr);
            ItemTemplate template = null;
            for (ItemTemplate item : Manager.ITEM_TEMPLATES) {
                if (item.id == tempId) {
                    template = item;
                    break;
                }
            }

            if (template != null) {
                lblItemName.setText("Tên item: " + (template.name != null ? template.name : "(Không có tên)"));
                lblItemName.setForeground(Color.BLACK);
            } else {
                lblItemName.setText("Tên item: (Không tìm thấy ID: " + tempId + ")");
                lblItemName.setForeground(Color.RED);
            }
        } catch (NumberFormatException e) {
            lblItemName.setText("Tên item: (ID không hợp lệ)");
            lblItemName.setForeground(Color.RED);
        }
    }

    private void updateOptionName() {
        int index = cbOptions.getSelectedIndex();
        if (index <= 0) {
            lblOptionName.setText("Tên option: (chưa chọn)");
            lblOptionName.setForeground(new Color(100, 100, 100));
            return;
        }

        String selected = (String) cbOptions.getSelectedItem();
        if (selected != null && selected.contains(" | ")) {
            String name = selected.substring(selected.indexOf(" | ") + 3);
            lblOptionName.setText("Tên option: " + name);
            lblOptionName.setForeground(Color.BLACK);
        }
    }

    private void onShopSelected() {
        int index = cbShops.getSelectedIndex();
        if (index <= 0) {
            currentShopId = -1;
            cbTabs.removeAllItems();
            return;
        }

        String selected = (String) cbShops.getSelectedItem();
        if (selected != null && selected.startsWith("ID: ")) {
            String idStr = selected.substring(4, selected.indexOf(" |"));
            currentShopId = Integer.parseInt(idStr);
            loadTabs(currentShopId);
        }
    }

    private void loadTabs(int shopId) {
        try (Connection con = AlyraManager.getConnection()) {
            List<String[]> tabs = ShopDAO.getTabsByShopId(con, shopId);
            cbTabs.removeAllItems();
            cbTabs.addItem("-- Chọn Tab --");
            for (String[] tab : tabs) {
                cbTabs.addItem("ID: " + tab[0] + " | " + tab[1]);
            }
            if (modelTabs != null) {
                modelTabs.setRowCount(0);
                for (String[] tab : tabs) {
                    modelTabs.addRow(new Object[] { tab[0], String.valueOf(shopId), tab[1] });
                }
            }

            log("Đã tải " + tabs.size() + " tabs cho shop ID: " + shopId);
        } catch (Exception e) {
            logError("Lỗi khi tải tabs: " + e.getMessage());
        }
    }

    private void onTabSelected() {
        int index = cbTabs.getSelectedIndex();
        if (index <= 0) {
            currentTabId = -1;
            if (modelItemShops != null) {
                modelItemShops.setRowCount(0);
            }
            if (modelItemShopOptions != null) {
                modelItemShopOptions.setRowCount(0);
            }
            return;
        }

        String selected = (String) cbTabs.getSelectedItem();
        if (selected != null && selected.startsWith("ID: ")) {
            String idStr = selected.substring(4, selected.indexOf(" |"));
            currentTabId = Integer.parseInt(idStr);
            log("Đã chọn tab ID: " + currentTabId);
            loadItemShopsByTabId(currentTabId);
        }
    }

    private void loadItemShopsByTabId(int tabId) {
        try (Connection con = AlyraManager.getConnection()) {
            List<String[]> itemShops = ShopDAO.getItemShopsByTabId(con, tabId);
            if (modelItemShops != null) {
                modelItemShops.setRowCount(0);
                for (String[] itemShop : itemShops) {
                    boolean isNew = "Có".equals(itemShop[6]);
                    modelItemShops.addRow(new Object[] {
                            itemShop[0], itemShop[1], itemShop[2], itemShop[3],
                            itemShop[4], itemShop[5], isNew
                    });
                }
            }

            log("Đã tải " + itemShops.size() + " item shops cho tab ID: " + tabId);
        } catch (Exception e) {
            logError("Lỗi khi tải item shops: " + e.getMessage());
        }
    }

    private void addShop() {
        try {
            String npcIdStr = txtNpcId.getText().trim();
            String tagName = txtTagName.getText().trim();
            String typeShopStr = txtTypeShop.getText().trim();

            if (npcIdStr.isEmpty() || tagName.isEmpty() || typeShopStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte npcId = Byte.parseByte(npcIdStr);
            byte typeShop = Byte.parseByte(typeShopStr);

            try (Connection con = AlyraManager.getConnection()) {
                int newId = ShopDAO.insertShop(con, npcId, tagName, typeShop);
                if (newId > 0) {
                    log("✓ Đã thêm shop mới - ID: " + newId + ", NPC: " + npcId + ", Tag: " + tagName);
                    JOptionPane.showMessageDialog(this, "Thêm shop thành công! ID: " + newId, "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    txtNpcId.setText("");
                    txtTagName.setText("");
                    txtTypeShop.setText("");
                    loadShops();
                } else {
                    logError("✗ Không thể thêm shop");
                    JOptionPane.showMessageDialog(this, "Không thể thêm shop!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            logError("✗ Lỗi định dạng số: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi định dạng số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logError("✗ Lỗi: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTab() {
        if (currentShopId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn shop trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tabName = txtTabName.getText().trim();
        if (tabName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên tab!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = AlyraManager.getConnection()) {
            int newId = ShopDAO.insertTabShop(con, currentShopId, tabName);
            if (newId > 0) {
                log("✓ Đã thêm tab mới - ID: " + newId + ", Tên: " + tabName);
                JOptionPane.showMessageDialog(this, "Thêm tab thành công! ID: " + newId, "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                txtTabName.setText("");
                loadTabs(currentShopId);
            } else {
                logError(" Không thể thêm tab");
                JOptionPane.showMessageDialog(this, "Không thể thêm tab!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            logError("✗ Lỗi: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addItem() {
        if (currentTabId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tab trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String tempIdStr = txtTempId.getText().trim();
            String costStr = txtCost.getText().trim();
            String iconSpecStr = txtIconSpec.getText().trim();
            String typeSellStr = txtTypeSell.getText().trim();

            if (tempIdStr.isEmpty() || costStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            short tempId = Short.parseShort(tempIdStr);
            int cost = Integer.parseInt(costStr);
            int iconSpec = iconSpecStr.isEmpty() ? 0 : Integer.parseInt(iconSpecStr);
            byte typeSell = typeSellStr.isEmpty() ? 0 : Byte.parseByte(typeSellStr);
            boolean isNew = chkIsNew.isSelected();
            ItemTemplate template = null;
            for (ItemTemplate item : Manager.ITEM_TEMPLATES) {
                if (item.id == tempId) {
                    template = item;
                    break;
                }
            }

            if (template == null) {
                JOptionPane.showMessageDialog(this, "Item template không tồn tại! ID: " + tempId, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection con = AlyraManager.getConnection()) {
                int newId = ShopDAO.insertItemShop(con, currentTabId, tempId, cost, isNew, iconSpec, typeSell);
                if (newId > 0) {
                    currentItemShopId = newId; // Lưu ID để thêm option sau
                    log("✓ Đã thêm item vào shop - ID: " + newId + ", Item: " + template.name + " (ID: " + tempId
                            + "), Giá: " + cost);

                    // Cập nhật combo box item shops và chọn item vừa tạo
                    // Refresh bảng item shops
                    if (currentTabId > 0) {
                        loadItemShopsByTabId(currentTabId);
                    }

                    JOptionPane.showMessageDialog(this,
                            "Thêm item thành công!\nID: " + newId + "\nItem: " + template.name,
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    txtTempId.setText("");
                    txtCost.setText("");
                    txtIconSpec.setText("0");
                    txtTypeSell.setText("0");
                    chkIsNew.setSelected(false);
                    lblItemName.setText("Tên item: (chưa chọn)");
                    lblItemName.setForeground(new Color(100, 100, 100));
                } else {
                    logError("✗ Không thể thêm item");
                    JOptionPane.showMessageDialog(this, "Không thể thêm item!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            logError("✗ Lỗi định dạng số: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi định dạng số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logError("✗ Lỗi: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addOption() {
        try {
            int index = cbOptions.getSelectedIndex();
            if (index <= 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn option!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String paramStr = txtParam.getText().trim();
            if (paramStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập param!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Lấy option ID từ combo box
            String selected = (String) cbOptions.getSelectedItem();
            if (selected == null || !selected.startsWith("ID: ")) {
                JOptionPane.showMessageDialog(this, "Option không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int indexOfPipe = selected.indexOf(" |");
            if (indexOfPipe <= 4) {
                JOptionPane.showMessageDialog(this, "Option không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String optionIdStr = selected.substring(4, indexOfPipe);
            int optionId = Integer.parseInt(optionIdStr);
            int param = Integer.parseInt(paramStr);
            int itemShopId = currentItemShopId;
            if (itemShopId <= 0) {
                try (Connection con = AlyraManager.getConnection()) {
                    java.sql.PreparedStatement ps = con
                            .prepareStatement("SELECT id FROM item_shop ORDER BY id DESC LIMIT 1");
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        itemShopId = rs.getInt("id");
                    }
                    rs.close();
                    ps.close();
                }
            }

            if (itemShopId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn Item Shop từ dropdown hoặc thêm item trước!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Lấy tên option
            int nameStart = selected.indexOf(" | ") + 3;
            String optionName = nameStart > 2 ? selected.substring(nameStart) : "Unknown";

            try (Connection con = AlyraManager.getConnection()) {
                boolean success = ShopDAO.insertItemShopOption(con, itemShopId, optionId, param);
                if (success) {
                    currentItemShopId = itemShopId; // Cập nhật currentItemShopId
                    log("✓ Đã thêm option - Item Shop ID: " + itemShopId + ", Option: " + optionName
                            + " (ID: " + optionId + "), Param: " + param);
                    // Refresh bảng options
                    loadItemShopOptions(itemShopId);
                    JOptionPane.showMessageDialog(this,
                            "Thêm option thành công!\nOption: " + optionName + "\nParam: " + param,
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    txtParam.setText("");
                    cbOptions.setSelectedIndex(0);
                    updateOptionName();
                } else {
                    logError("✗ Không thể thêm option");
                    JOptionPane.showMessageDialog(this, "Không thể thêm option!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            logError("✗ Lỗi định dạng số: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi định dạng số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            logError("✗ Lỗi: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void log(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        txtLog.append("[" + timestamp + "] " + message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void logError(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        txtLog.append("[" + timestamp + "] " + message + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    /**
     * Xử lý khi user edit cell trong bảng Item Shops
     */
    private void handleItemShopCellEdit(int row, int column) {
        try {
            // Lấy item shop ID
            String idStr = String.valueOf(modelItemShops.getValueAt(row, 0));
            int itemShopId = Integer.parseInt(idStr);

            // Parse giá trị mới từ bảng
            int cost = 0, iconSpec = 0;
            byte typeSell = 0;
            boolean isNew = false;

            // Lấy các giá trị hiện tại từ bảng
            try {
                cost = Integer.parseInt(String.valueOf(modelItemShops.getValueAt(row, 3)));
                iconSpec = Integer.parseInt(String.valueOf(modelItemShops.getValueAt(row, 4)));
                typeSell = Byte.parseByte(String.valueOf(modelItemShops.getValueAt(row, 5)));
                Object isNewObj = modelItemShops.getValueAt(row, 6);
                if (isNewObj instanceof Boolean) {
                    isNew = (Boolean) isNewObj;
                } else {
                    isNew = "Có".equals(String.valueOf(isNewObj));
                }
            } catch (NumberFormatException e) {
                logError("✗ Giá trị không hợp lệ, đang tải lại từ database...");
                if (currentTabId > 0) {
                    loadItemShopsByTabId(currentTabId);
                }
                return;
            }

            try (Connection con = AlyraManager.getConnection()) {
                boolean success = ShopDAO.updateItemShop(con, itemShopId, cost, iconSpec, typeSell, isNew);
                if (success) {
                    log("✓ Đã cập nhật item shop ID: " + itemShopId +
                            " - Giá: " + cost + ", Icon Spec: " + iconSpec +
                            ", Type Sell: " + typeSell + ", Item Mới: " + (isNew ? "Có" : "Không"));
                } else {
                    logError("✗ Không thể cập nhật item shop ID: " + itemShopId);
                    if (currentTabId > 0) {
                        loadItemShopsByTabId(currentTabId);
                    }
                }
            }
        } catch (Exception e) {
            logError("✗ Lỗi khi cập nhật: " + e.getMessage());
            if (currentTabId > 0) {
                loadItemShopsByTabId(currentTabId);
            }
        }
    }

    /**
     * Xử lý khi user edit Param trong bảng Options
     */
    private void handleOptionParamEdit(int row) {
        try {
            if (currentItemShopId <= 0) {
                logError("✗ Chưa chọn item shop");
                return;
            }

            // Lấy option ID và param mới
            int optionId = Integer.parseInt(String.valueOf(modelItemShopOptions.getValueAt(row, 0)));
            String paramStr = String.valueOf(modelItemShopOptions.getValueAt(row, 2));
            int newParam;

            try {
                newParam = Integer.parseInt(paramStr);
            } catch (NumberFormatException e) {
                logError("✗ Param phải là số!");
                // Revert về giá trị cũ
                loadItemShopOptions(currentItemShopId);
                return;
            }

            // Update vào database
            try (Connection con = AlyraManager.getConnection()) {
                boolean success = ShopDAO.updateItemShopOption(con, currentItemShopId, optionId, newParam);
                if (success) {
                    log("✓ Đã cập nhật option ID: " + optionId + ", Param: " + newParam);
                } else {
                    logError("✗ Không thể cập nhật option");
                    loadItemShopOptions(currentItemShopId);
                }
            }
        } catch (Exception e) {
            logError("✗ Lỗi khi cập nhật option: " + e.getMessage());
            if (currentItemShopId > 0) {
                loadItemShopOptions(currentItemShopId);
            }
        }
    }

    public static void showTool() {
        try {
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
            } catch (Exception e1) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
        }
        SwingUtilities.invokeLater(() -> {
            try {
                data.AlyraManager.gI();
                // Load item templates từ database (load tất cả)
                try (Connection con = data.AlyraManager.getConnection()) {
                    java.sql.PreparedStatement ps = con.prepareStatement("SELECT * FROM item_template");
                    java.sql.ResultSet rs = ps.executeQuery();
                    Manager.ITEM_TEMPLATES.clear();
                    while (rs.next()) {
                        ItemTemplate itemTemp = new ItemTemplate();
                        itemTemp.id = rs.getShort("id");
                        itemTemp.type = rs.getByte("type");
                        itemTemp.gender = rs.getByte("gender");
                        itemTemp.name = rs.getString("name");
                        itemTemp.description = rs.getString("description");
                        itemTemp.level = rs.getByte("level");
                        itemTemp.iconID = rs.getShort("icon_id");
                        itemTemp.part = rs.getShort("part");
                        itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                        itemTemp.strRequire = rs.getInt("power_require");
                        itemTemp.gold = rs.getInt("gold");
                        itemTemp.gem = rs.getInt("gem");
                        itemTemp.head = rs.getInt("head");
                        itemTemp.body = rs.getInt("body");
                        itemTemp.leg = rs.getInt("leg");
                        Manager.ITEM_TEMPLATES.add(itemTemp);
                    }
                    rs.close();
                    ps.close();
                }

                ShopAddTool tool = new ShopAddTool();
                tool.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi khởi tạo: " + e.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
