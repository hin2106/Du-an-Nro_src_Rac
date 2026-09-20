package server;

import boss.Boss;
import boss.drop.BossDropModels.Config;
import boss.drop.BossDropModels.LegacyMode;
import boss.drop.BossDropModels.OptionMode;
import boss.drop.BossDropModels.OptionRule;
import boss.drop.BossDropModels.Rule;
import boss.drop.BossDropModels.SourceType;
import boss.drop.BossDropModels.TlBossGenerator;
import boss.drop.BossDropModels.WeightedItems;
import boss.drop.BossDropModels.PoolGenerator;
import boss.drop.BossDropModels.ActivationOptions;
import boss.drop.BossDropRepository;
import boss.drop.BossDropService;
import boss.drop.BossDropDefaults;
import data.AlyraManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import managers.boss.BossManager;

/** Card based editor for boss drop rules. */
public final class BossDropAdminPanel extends JPanel {

    private final JFrame owner;
    private final DefaultListModel<BossChoice> bossModel = new DefaultListModel<>();
    private final JList<BossChoice> bossList = new JList<>(bossModel);
    private final JPanel cards = new WidthTrackingPanel();
    private final JComboBox<LegacyMode> legacyMode = new JComboBox<>(LegacyMode.values());
    private final JLabel state = new JLabel("Chọn một boss để cấu hình");
    private final List<RuleCard> editors = new ArrayList<>();
    private final Map<Integer, String> itemNames = new HashMap<>();
    private Config current;

    public BossDropAdminPanel(JFrame owner) {
        super(new BorderLayout(10, 10));
        this.owner = owner;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loadItemNames();
        buildUi();
        refreshBosses();
    }

    private void buildUi() {
        JPanel left = new JPanel(new BorderLayout(5, 5));
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Tìm tên hoặc ID boss...");
        JButton refresh = new JButton("Tải lại danh sách");
        JPanel leftTop = new JPanel(new BorderLayout(4, 4));
        leftTop.add(search, BorderLayout.CENTER);
        leftTop.add(refresh, BorderLayout.SOUTH);
        bossList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        left.add(leftTop, BorderLayout.NORTH);
        left.add(new JScrollPane(bossList), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(260, 0));

        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        JPanel right = new JPanel(new BorderLayout(6, 6));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addItem = new JButton("+ Ô vật phẩm");
        JButton addTl = new JButton("+ Bộ sinh đồ");
        JButton save = new JButton("Lưu cấu hình");
        JButton restore = new JButton("Khôi phục setup code gốc");
        toolbar.add(new JLabel("Xử lý setup cũ:"));
        toolbar.add(legacyMode);
        toolbar.add(addItem);
        toolbar.add(addTl);
        toolbar.add(save);
        toolbar.add(restore);
        right.add(toolbar, BorderLayout.NORTH);
        JScrollPane cardScroll = new JScrollPane(cards);
        cardScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cardScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        right.add(cardScroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(new JLabel("Nhóm loại trừ: các ô cùng tên nhóm phải có cùng tỷ lệ/lượt; hệ thống chọn 1 ô theo trọng số."), BorderLayout.NORTH);
        footer.add(state, BorderLayout.SOUTH);
        right.add(footer, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.22);
        add(split, BorderLayout.CENTER);

        refresh.addActionListener(e -> refreshBosses());
        bossList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadSelected(); });
        search.getDocument().addDocumentListener(SimpleDocumentListener.of(() -> filterBosses(search.getText())));
        addItem.addActionListener(e -> { selectOverrideMode(); addCard(newRule(SourceType.FIXED_ITEM)); });
        addTl.addActionListener(e -> addGeneratorCard());
        save.addActionListener(e -> save());
        restore.addActionListener(e -> restore());
    }

    private void refreshBosses() {
        String selected = bossList.getSelectedValue() == null ? null : bossList.getSelectedValue().toString();
        bossModel.clear();
        for (Boss boss : BossManager.gI().getDashboardBosses()) {
            bossModel.addElement(new BossChoice((int) boss.id, boss.data[0].getName()));
        }
        if (selected != null) {
            for (int i = 0; i < bossModel.size(); i++) if (selected.equals(bossModel.get(i).toString())) bossList.setSelectedIndex(i);
        }
        state.setText(bossModel.isEmpty() ? "Danh sách boss chưa được nạp. Hãy tải lại sau khi server khởi động." : "Có " + bossModel.size() + " loại boss từ lệnh boss");
    }

    private void filterBosses(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        bossModel.clear();
        for (Boss boss : BossManager.gI().getDashboardBosses()) {
            BossChoice choice = new BossChoice((int) boss.id, boss.data[0].getName());
            if (q.isEmpty() || choice.toString().toLowerCase().contains(q)) bossModel.addElement(choice);
        }
    }

    private void loadSelected() {
        BossChoice boss = bossList.getSelectedValue();
        clearCards();
        if (boss == null) return;
        Config loaded = BossDropRepository.load(boss.id);
        Config builtIn = loaded == null ? BossDropDefaults.create(boss.id) : null;
        current = loaded != null ? loaded : (builtIn == null ? new Config() : builtIn);
        current.bossId = boss.id;
        legacyMode.setSelectedItem(current.legacyMode == null ? LegacyMode.PRESERVE : current.legacyMode);
        if (current.rules != null) for (Rule rule : current.rules) addCard(rule);
        state.setText(loaded == null
                ? (builtIn == null ? "Đang giữ nguyên toàn bộ setup trong code. Thêm ô để ghi đè hoặc bổ sung."
                        : "Đã nạp setup code gốc thành ô có thể chỉnh sửa; hãy Lưu để ghi thay đổi.")
                : "Đã tải " + editors.size() + " ô cấu hình cho boss #" + boss.id);
    }

    private Rule newRule(SourceType type) {
        Rule rule = new Rule();
        rule.id = UUID.randomUUID().toString();
        rule.sourceType = type;
        if (isTlSource(type)) rule.tlBoss = BossDropDefaults.createTlGenerator(type);
        if (isPoolSource(type)) rule.pool = BossDropDefaults.createPoolGenerator(type, 0);
        return rule;
    }

    private void addGeneratorCard() {
        SourceType[] generators = {SourceType.RAND_DO_TL, SourceType.RAND_DO_TL_COLOR,
            SourceType.RAND_DO_TL_BOSS, SourceType.RAND_DO_SAO,
            SourceType.RAND_TEMP_ITEM_DO_SAO, SourceType.RAND_TEMP_ITEM_KICH_HOAT};
        SourceType selected = (SourceType) JOptionPane.showInputDialog(this, "Chọn setup sinh đồ:",
                "Thêm bộ sinh đồ", JOptionPane.PLAIN_MESSAGE, null, generators, generators[0]);
        if (selected != null) {
            selectOverrideMode();
            addCard(newRule(selected));
        }
    }

    private void selectOverrideMode() {
        if (legacyMode.getSelectedItem() == LegacyMode.PRESERVE) {
            legacyMode.setSelectedItem(LegacyMode.REPLACE_CONFIGURED);
        }
    }

    private void addCard(Rule rule) {
        RuleCard card = new RuleCard(rule);
        editors.add(card);
        cards.add(card);
        cards.add(Box.createVerticalStrut(8));
        cards.revalidate();
        cards.repaint();
    }

    private void removeCard(RuleCard card) {
        editors.remove(card);
        rebuildCards();
    }

    private void rebuildCards() {
        cards.removeAll();
        for (RuleCard editor : editors) {
            cards.add(editor);
            cards.add(Box.createVerticalStrut(8));
        }
        cards.revalidate();
        cards.repaint();
    }

    private void clearCards() {
        editors.clear();
        cards.removeAll();
        cards.revalidate();
        cards.repaint();
    }

    private void save() {
        BossChoice boss = bossList.getSelectedValue();
        if (boss == null) return;
        try {
            Config config = new Config();
            config.bossId = boss.id;
            config.legacyMode = (LegacyMode) legacyMode.getSelectedItem();
            for (RuleCard editor : editors) config.rules.add(editor.read());
            BossDropRepository.save(config);
            BossDropService.gI().reload(boss.id);
            current = config;
            state.setText("Đã lưu và áp dụng ngay " + config.rules.size() + " ô vật phẩm.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Quản Lý Boss", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restore() {
        BossChoice boss = bossList.getSelectedValue();
        if (boss == null || JOptionPane.showConfirmDialog(this,
                "Xóa cấu hình giao diện và dùng lại nguyên setup đồ rơi trong code?",
                "Khôi phục", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            BossDropRepository.delete(boss.id);
            BossDropService.gI().reload(boss.id);
            loadSelected();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Quản Lý Boss", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItemNames() {
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT id,name FROM item_template ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) itemNames.put(rs.getInt(1), rs.getString(2));
        } catch (Exception ignored) {
        }
    }

    private final class RuleCard extends JPanel {
        final Rule original;
        final JCheckBox enabled = new JCheckBox("Bật");
        final JComboBox<SourceType> source = new JComboBox<>(SourceType.values());
        final JSpinner itemId = new JSpinner(new SpinnerNumberModel(0, 0, 32767, 1));
        final JLabel itemName = new JLabel();
        final JSpinner qtyMin = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        final JSpinner qtyMax = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        final JTextField rate = new JTextField(6);
        final JSpinner rolls = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        final JTextField group = new JTextField(12);
        final JSpinner weight = new JSpinner(new SpinnerNumberModel(1, 0, 1_000_000, 1));
        final JCheckBox killerOnly = new JCheckBox("Chỉ người kết liễu");
        final JTextArea options = new JTextArea(3, 20);
        final JButton generatorButton = new JButton("Cấu hình bộ sinh");
        TlBossGenerator tlBoss;
        PoolGenerator pool;

        RuleCard(Rule rule) {
            super(new GridBagLayout());
            this.original = rule;
            this.tlBoss = rule.tlBoss;
            this.pool = rule.pool;
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
            enabled.setSelected(rule.enabled);
            source.setSelectedItem(rule.sourceType == null ? SourceType.FIXED_ITEM : rule.sourceType);
            itemId.setValue(Math.max(0, rule.itemTemplateId));
            qtyMin.setValue(Math.max(1, rule.quantityMin));
            qtyMax.setValue(Math.max(1, rule.quantityMax));
            rate.setText(formatRate(rule.rateUnits));
            rolls.setValue(Math.max(1, rule.rolls));
            group.setText(rule.exclusiveGroup == null ? "" : rule.exclusiveGroup);
            weight.setValue(Math.max(0, rule.weight));
            killerOnly.setSelected(rule.killerOnly);
            options.setText(formatOptions(rule.options));
            itemId.addChangeListener(e -> updateItemName());
            source.addActionListener(e -> updateSource());
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 255));
            generatorButton.addActionListener(e -> editGenerator());
            JButton duplicate = new JButton("Sao chép ô");
            duplicate.addActionListener(e -> addCard(copy(read())));
            JButton delete = new JButton("Xóa ô");
            delete.addActionListener(e -> removeCard(this));

            source.setPreferredSize(new Dimension(210, source.getPreferredSize().height));
            compact(itemId, 85);
            compact(qtyMin, 90);
            compact(qtyMax, 90);
            compact(rolls, 60);
            compact(weight, 90);
            itemName.setPreferredSize(new Dimension(190, itemName.getPreferredSize().height));
            itemName.setMinimumSize(new Dimension(80, itemName.getPreferredSize().height));

            int row = 0;
            GridBagConstraints c = gbc(0, row);
            add(enabled, c);
            c = gbc(1, row); c.gridwidth = 3; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            JPanel header = flowPanel(new JLabel("Loại:"), source);
            add(header, c);
            c = gbc(4, row++); c.anchor = GridBagConstraints.EAST;
            JPanel buttons = flowPanel(generatorButton, duplicate, delete);
            add(buttons, c);

            addFieldRow(row++, "Item ID:", itemPanel(), "Số lượng:", quantityPanel());
            addFieldRow(row++, "Tỷ lệ rơi:", ratePanel(), "Nhóm loại trừ:", group);
            addFieldRow(row++, "Trọng số:", weight, "Quyền nhặt:", killerOnly);

            c = gbc(0, row++); c.gridwidth = 5; c.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel("Options, mỗi dòng: optionId|min|max|tỷ lệ%"), c);
            c = gbc(0, row++); c.gridwidth = 5; c.weightx = 1; c.fill = GridBagConstraints.BOTH;
            add(new JScrollPane(options), c);
            updateItemName();
            updateSource();
        }

        private JPanel itemPanel() {
            JPanel panel = flowPanel(itemId, itemName);
            panel.setMinimumSize(new Dimension(0, panel.getPreferredSize().height));
            return panel;
        }

        private JPanel quantityPanel() {
            return flowPanel(qtyMin, new JLabel("đến"), qtyMax);
        }

        private JPanel ratePanel() {
            return flowPanel(rate, new JLabel("%"), new JLabel("Lượt:"), rolls);
        }

        private JPanel flowPanel(Component... components) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            for (Component component : components) panel.add(component);
            return panel;
        }

        private void compact(javax.swing.JComponent component, int width) {
            component.setPreferredSize(new Dimension(width, component.getPreferredSize().height));
        }

        private void addFieldRow(int row, String leftLabel, Component leftField,
                String rightLabel, Component rightField) {
            GridBagConstraints c = gbc(0, row);
            add(new JLabel(leftLabel), c);
            c = gbc(1, row); c.weightx = .5; c.fill = GridBagConstraints.HORIZONTAL;
            add(leftField, c);
            c = gbc(2, row);
            add(new JLabel(rightLabel), c);
            c = gbc(3, row); c.gridwidth = 2; c.weightx = .5; c.fill = GridBagConstraints.HORIZONTAL;
            add(rightField, c);
        }

        private GridBagConstraints gbc(int x, int y) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = x; c.gridy = y; c.insets = new Insets(3, 4, 3, 4); c.anchor = GridBagConstraints.WEST;
            return c;
        }

        private void updateItemName() {
            int id = (Integer) itemId.getValue();
            String name = itemNames.getOrDefault(id, "Không tìm thấy item");
            itemName.setText(name);
            itemName.setToolTipText(name);
        }

        private void updateSource() {
            boolean fixed = source.getSelectedItem() == SourceType.FIXED_ITEM;
            itemId.setEnabled(fixed);
            itemName.setVisible(fixed);
            generatorButton.setVisible(!fixed);
            revalidate();
        }

        Rule read() {
            Rule rule = new Rule();
            rule.id = original.id == null ? UUID.randomUUID().toString() : original.id;
            rule.enabled = enabled.isSelected();
            rule.sourceType = (SourceType) source.getSelectedItem();
            rule.itemTemplateId = (Integer) itemId.getValue();
            rule.quantityMin = (Integer) qtyMin.getValue();
            rule.quantityMax = (Integer) qtyMax.getValue();
            rule.rateUnits = parseRate(rate.getText());
            rule.rolls = (Integer) rolls.getValue();
            rule.exclusiveGroup = group.getText().trim();
            rule.weight = (Integer) weight.getValue();
            rule.killerOnly = killerOnly.isSelected();
            rule.options = parseOptions(options.getText());
            rule.tlBoss = isTlSource(rule.sourceType)
                    ? (tlBoss == null ? BossDropDefaults.createTlGenerator(rule.sourceType) : tlBoss) : null;
            rule.pool = isPoolSource(rule.sourceType)
                    ? (pool == null ? BossDropDefaults.createPoolGenerator(rule.sourceType, 0) : pool) : null;
            return rule;
        }

        private void editGenerator() {
            SourceType type = (SourceType) source.getSelectedItem();
            if (isTlSource(type)) {
                if (tlBoss == null) tlBoss = BossDropDefaults.createTlGenerator(type);
                GeneratorDialog dialog = new GeneratorDialog(owner, type, tlBoss);
                dialog.setVisible(true);
                if (dialog.result != null) tlBoss = dialog.result;
            } else if (isPoolSource(type)) {
                if (pool == null) pool = BossDropDefaults.createPoolGenerator(type, 0);
                PoolGeneratorDialog dialog = new PoolGeneratorDialog(owner, type, pool);
                dialog.setVisible(true);
                if (dialog.result != null) pool = dialog.result;
            }
        }
    }

    private static final class GeneratorDialog extends JDialog {
        TlBossGenerator result;
        final List<JTextField> itemFields = new ArrayList<>();
        final List<JTextField> weightFields = new ArrayList<>();
        final JSpinner scaleMin;
        final JSpinner scaleMax;
        final JTextField specialRate;
        final JTextField specialIds;
        final JSpinner op21Min, op21Max, op218, trailingId, trailingMin, trailingMax;
        final JCheckBox add218;

        GeneratorDialog(JFrame owner, SourceType type, TlBossGenerator source) {
            super(owner, "Cấu hình " + type, true);
            JPanel form = new JPanel(new GridBagLayout());
            int row = 0;
            for (WeightedItems category : source.categories) {
                JTextField ids = new JTextField(join(category.itemIds), 22);
                JTextField weight = new JTextField(Integer.toString(category.weightUnits), 9);
                itemFields.add(ids); weightFields.add(weight);
                addDialogRow(form, row++, new JLabel(category.name + " - Item IDs:"), ids, new JLabel("Trọng số:"), weight);
            }
            scaleMin = spinner(source.statScaleMin); scaleMax = spinner(source.statScaleMax);
            specialRate = new JTextField(formatRate(source.specialOptionRateUnits), 7);
            specialIds = new JTextField(join(source.specialOptionIds), 16);
            op21Min = spinner(source.option21Min); op21Max = spinner(source.option21Max);
            add218 = new JCheckBox("Thêm option 218", source.addOption218);
            op218 = spinner(source.option218Value);
            trailingId = new JSpinner(new SpinnerNumberModel(source.trailingOptionId, -1, Integer.MAX_VALUE, 1));
            trailingMin = spinner(source.trailingOptionMin); trailingMax = spinner(source.trailingOptionMax);
            addDialogRow(form, row++, new JLabel("Nhân chỉ số %:"), scaleMin, new JLabel("đến"), scaleMax);
            addDialogRow(form, row++, new JLabel("Option đặc biệt IDs:"), specialIds, new JLabel("Tỷ lệ %:"), specialRate);
            addDialogRow(form, row++, new JLabel("Option 21:"), op21Min, new JLabel("đến"), op21Max);
            addDialogRow(form, row++, add218, new JLabel("Giá trị:"), op218);
            addDialogRow(form, row++, new JLabel("Option cuối (-1 để tắt):"), trailingId,
                    new JLabel("Giá trị:"), trailingMin, new JLabel("đến"), trailingMax);
            JButton ok = new JButton("Áp dụng"); JButton cancel = new JButton("Hủy");
            ok.addActionListener(e -> { result = read(source); dispose(); }); cancel.addActionListener(e -> dispose());
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT)); buttons.add(ok); buttons.add(cancel);
            add(new JScrollPane(form), BorderLayout.CENTER); add(buttons, BorderLayout.SOUTH);
            setSize(720, 480); setLocationRelativeTo(owner);
        }

        private TlBossGenerator read(TlBossGenerator old) {
            TlBossGenerator value = new TlBossGenerator();
            value.categories = new ArrayList<>();
            for (int i = 0; i < old.categories.size(); i++) value.categories.add(new WeightedItems(
                    old.categories.get(i).name, Integer.parseInt(weightFields.get(i).getText().trim()),
                    parseIds(itemFields.get(i).getText())));
            value.statScaleMin = (Integer) scaleMin.getValue(); value.statScaleMax = (Integer) scaleMax.getValue();
            value.specialOptionRateUnits = parseRate(specialRate.getText()); value.specialOptionIds = parseIds(specialIds.getText());
            value.option21Min = (Integer) op21Min.getValue(); value.option21Max = (Integer) op21Max.getValue();
            value.addOption218 = add218.isSelected(); value.option218Value = (Integer) op218.getValue();
            value.trailingOptionId = (Integer) trailingId.getValue(); value.trailingOptionMin = (Integer) trailingMin.getValue();
            value.trailingOptionMax = (Integer) trailingMax.getValue(); return value;
        }

        private static JSpinner spinner(int value) { return new JSpinner(new SpinnerNumberModel(value, 0, Integer.MAX_VALUE, 1)); }
        private static void addDialogRow(JPanel p, int row, Component... cs) {
            for (int i = 0; i < cs.length; i++) { GridBagConstraints c = new GridBagConstraints(); c.gridx=i;c.gridy=row;c.insets=new Insets(4,4,4,4);c.anchor=GridBagConstraints.WEST;p.add(cs[i],c); }
        }
    }

    private static final class PoolGeneratorDialog extends JDialog {
        PoolGenerator result;
        final SourceType type;
        final JComboBox<Integer> gender = new JComboBox<>(new Integer[]{0, 1, 2});
        final List<JTextField> itemFields = new ArrayList<>();
        final List<JTextField> weightFields = new ArrayList<>();
        final JCheckBox activationEnabled = new JCheckBox("Thêm randOptionItemKichHoat");
        final JTextField earthOptions = new JTextField(16), namekOptions = new JTextField(16), xaydaOptions = new JTextField(16);
        final JSpinner activationParam, option30Value;
        final JCheckBox addOption30 = new JCheckBox("Thêm option 30");

        PoolGeneratorDialog(JFrame owner, SourceType type, PoolGenerator source) {
            super(owner, "Cấu hình " + type, true);
            this.type = type;
            JPanel form = new JPanel(new GridBagLayout());
            int row = 0;
            gender.setSelectedItem(source.gender);
            GeneratorDialog.addDialogRow(form, row++, new JLabel("Hành tinh (0/1/2):"), gender);
            for (WeightedItems category : source.categories) {
                JTextField ids = new JTextField(join(category.itemIds), 24);
                JTextField weight = new JTextField(Integer.toString(category.weightUnits), 10);
                itemFields.add(ids); weightFields.add(weight);
                GeneratorDialog.addDialogRow(form, row++, new JLabel(category.name + " - Item IDs:"), ids,
                        new JLabel("Trọng số:"), weight);
            }
            ActivationOptions activation = source.activation == null ? new ActivationOptions() : source.activation;
            activationEnabled.setSelected(activation.enabled);
            earthOptions.setText(join(activation.optionIdsByGender[0]));
            namekOptions.setText(join(activation.optionIdsByGender[1]));
            xaydaOptions.setText(join(activation.optionIdsByGender[2]));
            activationParam = GeneratorDialog.spinner(activation.param);
            addOption30.setSelected(activation.addOption30);
            option30Value = GeneratorDialog.spinner(activation.option30Value);
            GeneratorDialog.addDialogRow(form, row++, activationEnabled, new JLabel("Param:"), activationParam);
            GeneratorDialog.addDialogRow(form, row++, new JLabel("Option Trái Đất:"), earthOptions);
            GeneratorDialog.addDialogRow(form, row++, new JLabel("Option Namek:"), namekOptions);
            GeneratorDialog.addDialogRow(form, row++, new JLabel("Option Xayda:"), xaydaOptions);
            GeneratorDialog.addDialogRow(form, row++, addOption30, new JLabel("Giá trị:"), option30Value);
            JButton reload = new JButton("Nạp danh sách mặc định theo hành tinh");
            reload.addActionListener(e -> loadDefaultCategories());
            JButton oldOptions = new JButton("Option kích hoạt cũ");
            oldOptions.addActionListener(e -> loadActivationOptions(false));
            JButton newOptions = new JButton("Option kích hoạt mới/Unified");
            newOptions.addActionListener(e -> loadActivationOptions(true));
            JButton ok = new JButton("Áp dụng"); JButton cancel = new JButton("Hủy");
            ok.addActionListener(e -> { result = read(source); dispose(); }); cancel.addActionListener(e -> dispose());
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(reload); buttons.add(oldOptions); buttons.add(newOptions); buttons.add(ok); buttons.add(cancel);
            add(new JScrollPane(form), BorderLayout.CENTER); add(buttons, BorderLayout.SOUTH);
            setSize(760, 520); setLocationRelativeTo(owner);
        }

        private void loadDefaultCategories() {
            PoolGenerator defaults = BossDropDefaults.createPoolGenerator(type, (Integer) gender.getSelectedItem());
            for (int i = 0; i < Math.min(defaults.categories.size(), itemFields.size()); i++) {
                itemFields.get(i).setText(join(defaults.categories.get(i).itemIds));
                weightFields.get(i).setText(Integer.toString(defaults.categories.get(i).weightUnits));
            }
        }

        private void loadActivationOptions(boolean useNew) {
            int[][] values = useNew
                    ? new int[][]{{245,246,247,248},{237,238,239,240},{241,242,243,244}}
                    : new int[][]{{128,140},{130,142},{134,137}};
            earthOptions.setText(join(values[0])); namekOptions.setText(join(values[1]));
            xaydaOptions.setText(join(values[2])); activationEnabled.setSelected(true);
        }

        private PoolGenerator read(PoolGenerator old) {
            PoolGenerator value = new PoolGenerator(); value.gender = (Integer) gender.getSelectedItem();
            value.categories = new ArrayList<>();
            for (int i = 0; i < old.categories.size(); i++) value.categories.add(new WeightedItems(
                    old.categories.get(i).name, Integer.parseInt(weightFields.get(i).getText().trim()),
                    parseIds(itemFields.get(i).getText())));
            value.activation.enabled = activationEnabled.isSelected(); value.activation.param = (Integer) activationParam.getValue();
            value.activation.optionIdsByGender = new int[][]{parseIds(earthOptions.getText()), parseIds(namekOptions.getText()), parseIds(xaydaOptions.getText())};
            value.activation.addOption30 = addOption30.isSelected(); value.activation.option30Value = (Integer) option30Value.getValue();
            return value;
        }
    }

    private static Rule copy(Rule source) {
        Rule result = new Rule();
        result.id = UUID.randomUUID().toString(); result.enabled = source.enabled; result.sourceType = source.sourceType;
        result.itemTemplateId = source.itemTemplateId; result.quantityMin = source.quantityMin; result.quantityMax = source.quantityMax;
        result.rateUnits = source.rateUnits; result.rolls = source.rolls; result.exclusiveGroup = source.exclusiveGroup;
        result.weight = source.weight; result.killerOnly = source.killerOnly; result.options = new ArrayList<>(source.options);
        result.tlBoss = source.tlBoss; result.pool = source.pool;
        return result;
    }

    private static boolean isTlSource(SourceType type) {
        return type == SourceType.RAND_DO_TL || type == SourceType.RAND_DO_TL_COLOR || type == SourceType.RAND_DO_TL_BOSS;
    }

    private static boolean isPoolSource(SourceType type) {
        return type == SourceType.RAND_DO_SAO || type == SourceType.RAND_TEMP_ITEM_DO_SAO
                || type == SourceType.RAND_TEMP_ITEM_KICH_HOAT;
    }

    private static String formatOptions(List<OptionRule> options) {
        if (options == null) return "";
        StringBuilder out = new StringBuilder();
        for (OptionRule option : options) out.append(option.optionId).append('|').append(option.valueMin).append('|')
                .append(option.valueMax).append('|').append(formatRate(option.rateUnits)).append('\n');
        return out.toString();
    }

    private static List<OptionRule> parseOptions(String text) {
        List<OptionRule> result = new ArrayList<>();
        if (text == null || text.isBlank()) return result;
        for (String line : text.split("\\R")) {
            if (line.isBlank()) continue;
            String[] parts = line.trim().split("\\|");
            if (parts.length != 4) throw new IllegalArgumentException("Option sai định dạng: " + line);
            OptionRule option = new OptionRule(); option.optionId = Integer.parseInt(parts[0].trim());
            option.valueMin = Integer.parseInt(parts[1].trim()); option.valueMax = Integer.parseInt(parts[2].trim());
            option.mode = option.valueMin == option.valueMax ? OptionMode.FIXED : OptionMode.RANDOM_RANGE;
            option.rateUnits = parseRate(parts[3]); result.add(option);
        }
        return result;
    }

    private static int parseRate(String text) {
        double percent = Double.parseDouble(text.trim().replace(',', '.'));
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Tỷ lệ phải từ 0 đến 100%");
        return (int) Math.round(percent * 10_000d);
    }

    private static String formatRate(int units) {
        String value = String.format(java.util.Locale.US, "%.4f", units / 10_000d)
                .replaceAll("0+$", "").replaceAll("\\.$", "");
        return value.isEmpty() ? "0" : value;
    }
    private static int[] parseIds(String text) { String[] p=text.trim().split("[,;\\s]+");int[] ids=new int[p.length];for(int i=0;i<p.length;i++)ids[i]=Integer.parseInt(p[i]);return ids; }
    private static String join(int[] ids) { if(ids==null)return "";StringBuilder s=new StringBuilder();for(int id:ids){if(s.length()>0)s.append(',');s.append(id);}return s.toString(); }

    private record BossChoice(int id, String name) { @Override public String toString() { return "#" + id + " - " + name; } }

    /** Makes BoxLayout cards follow the viewport width instead of creating a horizontal scrollbar. */
    private static final class WidthTrackingPanel extends JPanel implements javax.swing.Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) { return 24; }
        @Override public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();
        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        static SimpleDocumentListener of(Runnable action) { return action::run; }
    }
}
