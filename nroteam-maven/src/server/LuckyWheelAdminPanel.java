package server;

import data.AlyraManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import luckywheel.LuckyWheelConstants;
import luckywheel.LuckyWheelHandler;
import luckywheel.LuckyWheelModels.Event;
import luckywheel.LuckyWheelModels.Milestone;
import luckywheel.LuckyWheelModels.RankReward;
import luckywheel.LuckyWheelModels.Reward;
import luckywheel.LuckyWheelRepository;
import luckywheel.LuckyWheelService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

final class LuckyWheelAdminPanel extends JPanel {
    private static final Color BG = new Color(11, 18, 32);
    private static final Color CARD = new Color(21, 31, 50);
    private static final Color INPUT = new Color(17, 27, 45);
    private static final Color BORDER = new Color(51, 65, 85);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color BLUE = new Color(3, 105, 161);
    private static final Color GOLD = new Color(245, 158, 11);
    private final Component owner;
    private final List<ItemChoice> itemChoices = new ArrayList<>();
    private final List<OptionChoice> optionChoices = new ArrayList<>();
    private final SlotValue[] slots = new SlotValue[LuckyWheelConstants.SLOT_COUNT];
    private final JButton[] slotButtons = new JButton[LuckyWheelConstants.SLOT_COUNT];
    private final JSpinner days = new JSpinner(new SpinnerNumberModel(7, 0, 3650, 1));
    private final JSpinner hours = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
    private final JSpinner minutes = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
    private final JSpinner priceOne = new JSpinner(new SpinnerNumberModel(200, 1, Integer.MAX_VALUE, 1));
    private final JSpinner priceTen = new JSpinner(new SpinnerNumberModel(2000, 1, Integer.MAX_VALUE, 1));
    private final JSpinner pity = new JSpinner(new SpinnerNumberModel(1000, 1, Integer.MAX_VALUE, 1));
    private final JLabel status = new JLabel();
    private final DefaultTableModel milestoneModel = new DefaultTableModel(new Object[]{"Mốc lượt", "Danh sách quà"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return column == 0; }
    };
    private final JTable milestoneTable = new JTable(milestoneModel);
    private final DefaultTableModel rankModel = new DefaultTableModel(new Object[]{"Hạng", "Danh sách quà"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable rankTable = new JTable(rankModel);
    private Event event;

    LuckyWheelAdminPanel(Component owner) {
        this.owner = owner;
        setLayout(new BorderLayout(12, 12)); setBackground(BG); setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        loadTemplates();
        add(buildToolbar(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildWheelCard(), buildRewardTabs());
        split.setResizeWeight(0.58); split.setBorder(null); split.setBackground(BG); add(split, BorderLayout.CENTER);
        loadData();
    }

    private JComponent buildToolbar() {
        JPanel root = card(new BorderLayout(10, 8));
        JPanel fields = new JPanel(new java.awt.GridLayout(2, 1, 0, 4)); fields.setOpaque(false);
        JPanel durationFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2)); durationFields.setOpaque(false);
        JPanel priceFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2)); priceFields.setOpaque(false);
        addField(durationFields, "Thời lượng - Ngày", days); addField(durationFields, "Giờ", hours); addField(durationFields, "Phút", minutes);
        addField(priceFields, "Giá quay 1", priceOne); addField(priceFields, "Giá quay 10", priceTen); addField(priceFields, "Bảo hiểm", pity);
        fields.add(durationFields); fields.add(priceFields);
        JButton reload = button("Tải lại", new Color(51, 65, 85));
        JButton save = button("Lưu cấu hình", BLUE);
        JButton activate = button("Kích hoạt", new Color(22, 163, 74));
        JButton finish = button("Kết thúc", new Color(185, 28, 28));
        reload.addActionListener(e -> loadData());
        save.addActionListener(e -> saveWithNotice());
        activate.addActionListener(e -> activate());
        finish.addActionListener(e -> finish());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4)); actions.setOpaque(false);
        actions.add(status); actions.add(reload); actions.add(save); actions.add(activate); actions.add(finish);
        root.add(fields, BorderLayout.CENTER); root.add(actions, BorderLayout.SOUTH); return root;
    }

    private JComponent buildWheelCard() {
        JPanel wrapper = card(new BorderLayout(8, 8));
        JLabel title = new JLabel("19 ô phần thưởng - ô 17 là bảo hiểm", SwingConstants.CENTER);
        title.setForeground(TEXT); title.setFont(new Font("Segoe UI", Font.BOLD, 14)); wrapper.add(title, BorderLayout.NORTH);
        JPanel grid = new JPanel(new GridBagLayout()); grid.setOpaque(false);
        int[][] positions = {
            {0,0},{1,0},{2,0},{3,0},{4,0},{4,1},{4,2},{4,3},
            {4,4},{3,4},{2,4},{1,4},{0,4},{0,3},{0,2},{0,1},
            {2,1},{1,2},{3,2}
        };
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new SlotValue(i + 1);
            JButton cell = new JButton();
            Dimension cellSize = new Dimension(68, 52);
            cell.setPreferredSize(cellSize); cell.setMinimumSize(cellSize); cell.setFocusPainted(false);
            cell.putClientProperty("compactItemCell", Boolean.TRUE);
            cell.setFont(new Font("Segoe UI", Font.BOLD, 11)); cell.setMargin(new Insets(2, 3, 2, 3));
            cell.setHorizontalAlignment(SwingConstants.CENTER); cell.setIconTextGap(2);
            cell.setBackground(i == LuckyWheelConstants.PITY_SLOT - 1 ? new Color(120, 75, 10) : INPUT);
            cell.setForeground(TEXT); cell.setBorder(BorderFactory.createLineBorder(i == 16 ? GOLD : BORDER, i == 16 ? 2 : 1));
            final int index = i; cell.addActionListener(e -> editSlot(index)); slotButtons[i] = cell;
            GridBagConstraints c = new GridBagConstraints(); c.gridx = positions[i][0]; c.gridy = positions[i][1];
            c.insets = new Insets(4, 4, 4, 4); c.fill = GridBagConstraints.BOTH; grid.add(cell, c);
        }
        wrapper.add(grid, BorderLayout.CENTER);
        JLabel hint = new JLabel("Bấm từng ô để chọn item, số lượng, options và tỉ lệ (%)", SwingConstants.CENTER);
        hint.setForeground(MUTED); wrapper.add(hint, BorderLayout.SOUTH); refreshSlotButtons(); return wrapper;
    }

    private JComponent buildRewardTabs() {
        JTabbedPane tabs = new JTabbedPane(); tabs.setBackground(CARD); tabs.setForeground(TEXT);
        tabs.addTab("Quà mốc", buildMilestones()); tabs.addTab("Top 5", buildRanks()); return tabs;
    }

    private JComponent buildMilestones() {
        JPanel panel = card(new BorderLayout(6, 6)); milestoneTable.setRowHeight(46);
        milestoneTable.getColumnModel().getColumn(1).setCellRenderer(new RewardGroupIconRenderer());
        milestoneTable.getColumnModel().getColumn(0).setPreferredWidth(90); milestoneTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        JButton add = button("Thêm mốc", BLUE), edit = button("Sửa quà", new Color(51,65,85)), remove = button("Xóa mốc", new Color(185,28,28));
        add.addActionListener(e -> milestoneModel.addRow(new Object[]{20, new RewardGroup()}));
        edit.addActionListener(e -> { int row = milestoneTable.getSelectedRow(); if (row >= 0) editGroupAt(milestoneModel, row); });
        remove.addActionListener(e -> { int row = milestoneTable.getSelectedRow(); if (row >= 0) milestoneModel.removeRow(row); });
        milestoneTable.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) { int row = milestoneTable.rowAtPoint(e.getPoint()); if (row >= 0) editGroupAt(milestoneModel, row); } }});
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(add); actions.add(edit); actions.add(remove);
        panel.add(new JScrollPane(milestoneTable), BorderLayout.CENTER); panel.add(actions, BorderLayout.SOUTH); return panel;
    }

    private JComponent buildRanks() {
        JPanel panel = card(new BorderLayout(6, 6)); rankTable.setRowHeight(46);
        rankTable.getColumnModel().getColumn(1).setCellRenderer(new RewardGroupIconRenderer());
        for (int i = 1; i <= 5; i++) rankModel.addRow(new Object[]{"Top " + i, new RewardGroup()});
        JButton edit = button("Cấu hình quà hạng đã chọn", BLUE);
        edit.addActionListener(e -> { int row = rankTable.getSelectedRow(); if (row >= 0) editGroupAt(rankModel, row); });
        rankTable.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) { int row = rankTable.rowAtPoint(e.getPoint()); if (row >= 0) editGroupAt(rankModel, row); } }});
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(edit);
        panel.add(new JScrollPane(rankTable), BorderLayout.CENTER); panel.add(actions, BorderLayout.SOUTH); return panel;
    }

    private void editSlot(int index) {
        SlotValue value = slots[index].copy();
        JButton choose = button(value.item == null ? "Chọn vật phẩm..." : value.item.toString(), new Color(51,65,85));
        choose.setIcon(value.item == null ? null : TopAdminPanel.IconCache.get(value.item.iconId));
        choose.addActionListener(e -> { ItemChoice picked = selectItem(value.item); if (picked != value.item) { value.item = picked; choose.setText(picked == null ? "Chọn vật phẩm..." : picked.toString()); choose.setIcon(picked == null ? null : TopAdminPanel.IconCache.get(picked.iconId)); } });
        JSpinner quantity = new JSpinner(new SpinnerNumberModel(Math.max(1, value.quantity), 1, Integer.MAX_VALUE, 1));
        JButton options = button("Options: " + value.options, new Color(51,65,85));
        options.addActionListener(e -> { editOptions(value.options); options.setText("Options: " + value.options); });
        JTextField rate = new JTextField(rateText(value.rateUnits)); rate.setEnabled(index != LuckyWheelConstants.SLOT_COUNT - 1);
        JPanel form = new JPanel(new GridBagLayout());
        addFormRow(form, 0, "Vị trí", new JLabel(index == 16 ? "Ô 17 - bảo hiểm" : "Ô " + (index + 1)));
        addFormRow(form, 1, "Vật phẩm", choose); addFormRow(form, 2, "Số lượng", quantity);
        addFormRow(form, 3, "Options", options); addFormRow(form, 4, "Tỉ lệ (%)", rate);
        int answer = JOptionPane.showConfirmDialog(owner, form, "Cấu hình ô " + (index + 1), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        try {
            value.quantity = (Integer) quantity.getValue();
            if (index != LuckyWheelConstants.SLOT_COUNT - 1) value.rateUnits = parseRate(rate.getText());
            slots[index] = value; recalculateLastRate(); refreshSlotButtons();
        } catch (Exception ex) { showError(ex); }
    }

    private void editGroupAt(DefaultTableModel model, int row) {
        Object current = model.getValueAt(row, 1); RewardGroup group = current instanceof RewardGroup g ? g.copy() : new RewardGroup();
        DefaultTableModel rewards = new DefaultTableModel(new Object[]{"Vật phẩm", "SL", "Options"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 1; }
        };
        for (RewardValue reward : group.rewards) rewards.addRow(new Object[]{reward, reward.quantity, reward.options});
        JTable table = new JTable(rewards); table.setRowHeight(32);
        JButton add = button("Thêm quà", BLUE), remove = button("Xóa quà", new Color(185,28,28)), configure = button("Chọn item/options", new Color(51,65,85));
        add.addActionListener(e -> { RewardValue reward = new RewardValue(); rewards.addRow(new Object[]{reward, 1, reward.options}); table.setRowSelectionInterval(rewards.getRowCount()-1,rewards.getRowCount()-1); });
        remove.addActionListener(e -> { int selected = table.getSelectedRow(); if (selected >= 0) rewards.removeRow(selected); });
        configure.addActionListener(e -> {
            int selected = table.getSelectedRow(); if (selected < 0) return;
            RewardValue reward = rewards.getValueAt(selected,0) instanceof RewardValue rv ? rv : new RewardValue();
            ItemChoice item = selectItem(reward.item); if (item != null || reward.item != null) reward.item = item;
            editOptions(reward.options); rewards.setValueAt(reward, selected, 0); rewards.setValueAt(reward.options, selected, 2);
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.add(add); actions.add(configure); actions.add(remove);
        JPanel content = new JPanel(new BorderLayout(5,5)); content.setPreferredSize(new Dimension(680,420)); content.add(new JScrollPane(table),BorderLayout.CENTER); content.add(actions,BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(owner, content, "Danh sách phần thưởng", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        group.rewards.clear();
        for (int i=0;i<rewards.getRowCount();i++) {
            RewardValue reward = rewards.getValueAt(i,0) instanceof RewardValue rv ? rv : new RewardValue();
            reward.quantity = Integer.parseInt(String.valueOf(rewards.getValueAt(i,1))); if (reward.item != null && reward.quantity > 0) group.rewards.add(reward);
        }
        model.setValueAt(group,row,1);
    }

    private ItemChoice selectItem(ItemChoice current) {
        DefaultListModel<ItemChoice> model = new DefaultListModel<>(); JList<ItemChoice> list = new JList<>(model); list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(40); list.setCellRenderer(new javax.swing.DefaultListCellRenderer(){@Override public Component getListCellRendererComponent(JList<?>l,Object v,int i,boolean s,boolean f){javax.swing.JLabel label=(javax.swing.JLabel)super.getListCellRendererComponent(l,v,i,s,f);if(v instanceof ItemChoice item)label.setIcon(TopAdminPanel.IconCache.get(item.iconId));return label;}});
        JTextField search = new JTextField();
        Runnable filter = () -> { String key = search.getText().trim().toLowerCase(Locale.ROOT); model.clear(); for (ItemChoice item : itemChoices) if (key.isEmpty() || item.toString().toLowerCase(Locale.ROOT).contains(key)) model.addElement(item); if (current != null && key.isEmpty()) list.setSelectedValue(current,true); };
        search.getDocument().addDocumentListener(new DocumentListener(){ public void insertUpdate(DocumentEvent e){filter.run();} public void removeUpdate(DocumentEvent e){filter.run();} public void changedUpdate(DocumentEvent e){filter.run();} }); filter.run();
        JPanel content = new JPanel(new BorderLayout(5,5)); content.setPreferredSize(new Dimension(540,440)); content.add(search,BorderLayout.NORTH); content.add(new JScrollPane(list),BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(owner,content,"Chọn vật phẩm",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION) return current;
        return list.getSelectedValue();
    }

    private void editOptions(OptionsValue value) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Option","Chỉ số"},0);
        for (OptionParam option : value.values) model.addRow(new Object[]{option.option,option.param});
        JTable table = new JTable(model); table.setRowHeight(30); JComboBox<OptionChoice> combo = new JComboBox<>(optionChoices.toArray(new OptionChoice[0])); table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(combo));
        JButton add=button("Thêm option",BLUE),remove=button("Xóa option",new Color(185,28,28)); add.addActionListener(e->{if(!optionChoices.isEmpty())model.addRow(new Object[]{optionChoices.get(0),0});}); remove.addActionListener(e->{int row=table.getSelectedRow();if(row>=0)model.removeRow(row);});
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));actions.add(add);actions.add(remove); JPanel content=new JPanel(new BorderLayout(5,5));content.setPreferredSize(new Dimension(600,380));content.add(new JScrollPane(table),BorderLayout.CENTER);content.add(actions,BorderLayout.SOUTH);
        if(JOptionPane.showConfirmDialog(owner,content,"Danh sách options",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)!=JOptionPane.OK_OPTION)return;
        if(table.isEditing())table.getCellEditor().stopCellEditing(); value.values.clear();
        for(int i=0;i<model.getRowCount();i++){Object o=model.getValueAt(i,0);if(o instanceof OptionChoice option)value.values.add(new OptionParam(option,Integer.parseInt(String.valueOf(model.getValueAt(i,1)))));}
    }

    private void loadTemplates() {
        try (Connection con=AlyraManager.getConnection();PreparedStatement ps=con.prepareStatement("SELECT id,name,icon_id FROM item_template WHERE id>0 ORDER BY id");ResultSet rs=ps.executeQuery()){while(rs.next())itemChoices.add(new ItemChoice(rs.getInt(1),rs.getString(2),rs.getInt(3)));}
        catch(Exception e){showError(e);}
        try (Connection con=AlyraManager.getConnection();PreparedStatement ps=con.prepareStatement("SELECT id,name FROM item_option_template ORDER BY id");ResultSet rs=ps.executeQuery()){while(rs.next())optionChoices.add(new OptionChoice(rs.getInt(1),rs.getString(2)));}
        catch(Exception e){showError(e);}
    }

    private void loadData() {
        try {
            event=LuckyWheelRepository.getEditableEvent(); days.setValue(event.durationDays);hours.setValue(event.durationHours);minutes.setValue(event.durationMinutes);priceOne.setValue(event.priceOne);priceTen.setValue(event.priceTen);pity.setValue(event.pityLimit);
            for(int i=0;i<slots.length;i++)slots[i]=new SlotValue(i+1); Map<Integer,ItemChoice> items=new HashMap<>();for(ItemChoice item:itemChoices)items.put(item.id,item);
            for(Reward reward:LuckyWheelRepository.getRewards(event.id,event.configVersion)){SlotValue slot=slots[reward.position-1];slot.item=items.get(reward.itemTemplateId);if(slot.item==null)slot.item=new ItemChoice(reward.itemTemplateId,"Không còn trong item_template");slot.quantity=reward.quantity;slot.options=OptionsValue.fromJson(reward.optionsData,optionChoices);slot.rateUnits=reward.rateUnits;}
            milestoneModel.setRowCount(0);for(Milestone m:LuckyWheelRepository.getMilestones(event.id)){RewardGroup g=RewardGroup.from(m.rewards,items,optionChoices);milestoneModel.addRow(new Object[]{m.targetSpins,g});}
            for(int i=0;i<5;i++)rankModel.setValueAt(new RewardGroup(),i,1);for(RankReward rank:LuckyWheelRepository.getRankRewards(event.id))rankModel.setValueAt(RewardGroup.from(rank.rewards,items,optionChoices),rank.rank-1,1);
            refreshStatus();refreshSlotButtons();
        } catch(Exception e){showError(e);}
    }

    private void saveWithNotice(){try{saveAll();JOptionPane.showMessageDialog(owner,"Đã lưu cấu hình Vòng quay may mắn.");}catch(Exception e){showError(e);}}

    private void saveAll() throws Exception {
        event.durationDays=(Integer)days.getValue();event.durationHours=(Integer)hours.getValue();event.durationMinutes=(Integer)minutes.getValue();event.priceOne=(Integer)priceOne.getValue();event.priceTen=(Integer)priceTen.getValue();event.pityLimit=(Integer)pity.getValue();recalculateLastRate();
        List<Reward> rewards=new ArrayList<>();for(SlotValue slot:slots){if(slot.item==null)throw new IllegalArgumentException("Chưa chọn vật phẩm cho ô "+slot.position);Reward r=slot.toReward(event);rewards.add(r);} LuckyWheelService.gI().saveConfiguration(event,rewards);
        List<Milestone> milestones=new ArrayList<>();Set<Integer>targets=new HashSet<>();for(int i=0;i<milestoneModel.getRowCount();i++){int target=Integer.parseInt(String.valueOf(milestoneModel.getValueAt(i,0)));if(target<=0||!targets.add(target))throw new IllegalArgumentException("Mốc lượt phải lớn hơn 0 và không trùng nhau");RewardGroup g=(RewardGroup)milestoneModel.getValueAt(i,1);if(g.rewards.isEmpty())throw new IllegalArgumentException("Mốc "+target+" chưa có quà");Milestone m=new Milestone();m.targetSpins=target;m.rewards.addAll(g.toRewards(event));milestones.add(m);}LuckyWheelRepository.replaceMilestones(event.id,milestones);
        List<RankReward> ranks=new ArrayList<>();for(int i=0;i<5;i++){RewardGroup g=(RewardGroup)rankModel.getValueAt(i,1);if(g.rewards.isEmpty())throw new IllegalArgumentException("Top "+(i+1)+" chưa có quà");RankReward rank=new RankReward();rank.rank=i+1;rank.rewards.addAll(g.toRewards(event));ranks.add(rank);}LuckyWheelRepository.replaceRankRewards(event.id,ranks);event=LuckyWheelRepository.getEvent(event.id);if(event.status==LuckyWheelConstants.STATUS_ACTIVE)LuckyWheelHandler.gI().broadcastConfigChanged();refreshStatus();
    }

    private void activate(){try{saveAll();LuckyWheelService.gI().activate(event);loadData();JOptionPane.showMessageDialog(owner,"Sự kiện đã được kích hoạt.");}catch(Exception e){showError(e);}}
    private void finish(){if(event==null||event.status!=LuckyWheelConstants.STATUS_ACTIVE){JOptionPane.showMessageDialog(owner,"Không có sự kiện đang chạy.");return;}String text="Kết thúc sự kiện ngay bây giờ?\nHành động này sẽ khóa vòng quay và gửi quà mốc, quà xếp hạng qua Mail.\nKhông thể tiếp tục đợt hiện tại.";if(JOptionPane.showConfirmDialog(owner,text,"Xác nhận kết thúc",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION)return;new SwingWorker<Void,Void>(){protected Void doInBackground()throws Exception{LuckyWheelService.gI().finishNow();return null;}protected void done(){try{get();loadData();JOptionPane.showMessageDialog(owner,"Đã tổng kết và kết thúc sự kiện.");}catch(Exception e){showError(e);}}}.execute();}

    private void recalculateLastRate(){long total=0;for(int i=0;i<slots.length-1;i++)total+=slots[i].rateUnits;long remain=LuckyWheelConstants.RATE_TOTAL-total;if(remain<LuckyWheelConstants.LAST_SLOT_MIN_RATE)throw new IllegalArgumentException("Phải chừa ít nhất 0.01% cho ô 19");slots[18].rateUnits=remain;}
    private long parseRate(String value){BigDecimal percent=new BigDecimal(value.trim());if(percent.signum()<=0)throw new IllegalArgumentException("Tỉ lệ phải lớn hơn 0");return percent.movePointRight(6).setScale(0,RoundingMode.UNNECESSARY).longValueExact();}
    private String rateText(long units){return BigDecimal.valueOf(units,6).stripTrailingZeros().toPlainString();}
    private void refreshSlotButtons(){
        for(int i=0;i<slots.length;i++){
            SlotValue s=slots[i];
            JButton button=slotButtons[i];
            button.setText(s.item==null?"+":"x"+s.quantity);
            button.setIcon(s.item==null?null:TopAdminPanel.IconCache.get(s.item.iconId));
            String itemName=s.item==null?"Chưa chọn vật phẩm":s.item.name;
            button.setToolTipText("Ô "+(i+1)+(i==LuckyWheelConstants.PITY_SLOT-1?" - bảo hiểm":"")
                    +" | "+itemName+" x"+s.quantity+" | Tỉ lệ "+rateText(s.rateUnits)+"% | Options: "+s.options);
        }
    }
    private void refreshStatus(){String text=event==null?"Không có dữ liệu":switch(event.status){case 0->"● BẢN NHÁP";case 1->"● ĐANG CHẠY";case 2->"● ĐANG TỔNG KẾT";default->"● ĐÃ KẾT THÚC";};status.setText(text);status.setForeground(event!=null&&event.status==1?new Color(34,197,94):GOLD);boolean canEditDuration=event!=null&&event.status==LuckyWheelConstants.STATUS_DRAFT;days.setEnabled(canEditDuration);hours.setEnabled(canEditDuration);minutes.setEnabled(canEditDuration);}

    private JPanel card(java.awt.LayoutManager layout){JPanel panel=new JPanel(layout);panel.setBackground(CARD);panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),BorderFactory.createEmptyBorder(10,10,10,10)));return panel;}
    private JButton button(String text,Color color){JButton b=new JButton(text);b.setBackground(color);b.setForeground(Color.WHITE);b.setFocusPainted(false);return b;}
    private void addField(JPanel panel,String label,JComponent input){
        JLabel l=new JLabel(label);
        l.setForeground(MUTED);
        panel.add(l);
        int width=label.contains("Giá")||label.contains("Bảo")?120:82;
        Dimension fieldSize=new Dimension(width,40);
        input.setPreferredSize(fieldSize);
        input.setMinimumSize(fieldSize);
        if(input instanceof JSpinner spinner&&spinner.getEditor() instanceof JSpinner.DefaultEditor editor){
            JTextField textField=editor.getTextField();
            textField.setFont(new Font("Segoe UI",Font.PLAIN,13));
            textField.setPreferredSize(new Dimension(Math.max(40,width-22),38));
            textField.setMinimumSize(textField.getPreferredSize());
        }
        panel.add(input);
    }
    private void addFormRow(JPanel panel,int row,String label,JComponent value){GridBagConstraints l=new GridBagConstraints();l.gridx=0;l.gridy=row;l.anchor=GridBagConstraints.WEST;l.insets=new Insets(5,5,5,10);panel.add(new JLabel(label),l);GridBagConstraints v=new GridBagConstraints();v.gridx=1;v.gridy=row;v.weightx=1;v.fill=GridBagConstraints.HORIZONTAL;v.insets=new Insets(5,5,5,5);panel.add(value,v);}
    private String shorten(String value,int max){if(value==null)return "?";return value.length()<=max?value:value.substring(0,max-1)+"…";}
    private void showError(Exception e){Throwable t=e instanceof java.util.concurrent.ExecutionException&&e.getCause()!=null?e.getCause():e;JOptionPane.showMessageDialog(owner,t.getMessage()==null?t.toString():t.getMessage(),"Vòng quay may mắn",JOptionPane.ERROR_MESSAGE);}

    private static final class ItemChoice{final int id;final String name;final int iconId;ItemChoice(int id,String name){this(id,name,0);}ItemChoice(int id,String name,int iconId){this.id=id;this.name=name==null?"(không tên)":name;this.iconId=iconId;}public String toString(){return id+" - "+name;}}
    private static final class RewardGroupIconRenderer implements javax.swing.table.TableCellRenderer{
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int column){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,5,3));p.setBackground(selected?table.getSelectionBackground():table.getBackground());if(value instanceof RewardGroup group)for(RewardValue reward:group.rewards){JLabel label=new JLabel("x"+reward.quantity);if(reward.item!=null){label.setIcon(TopAdminPanel.IconCache.get(reward.item.iconId));label.setToolTipText(reward.item.name+" x"+reward.quantity);}p.add(label);}return p;}}
    private static final class OptionChoice{final int id;final String name;OptionChoice(int id,String name){this.id=id;this.name=name==null?"(không tên)":name;}public String toString(){return id+" - "+name;}}
    private static final class OptionParam{final OptionChoice option;final int param;OptionParam(OptionChoice option,int param){this.option=option;this.param=param;}}
    private static final class OptionsValue{final List<OptionParam>values=new ArrayList<>();OptionsValue copy(){OptionsValue c=new OptionsValue();c.values.addAll(values);return c;}@SuppressWarnings("unchecked")String toJson(){JSONArray a=new JSONArray();for(OptionParam p:values){JSONObject o=new JSONObject();o.put("id",p.option.id);o.put("param",p.param);a.add(o);}return a.toJSONString();}static OptionsValue fromJson(String json,List<OptionChoice>choices){OptionsValue v=new OptionsValue();Map<Integer,OptionChoice>map=new HashMap<>();for(OptionChoice c:choices)map.put(c.id,c);Object parsed=JSONValue.parse(json==null?"[]":json);if(parsed instanceof JSONArray a)for(Object raw:a)if(raw instanceof JSONObject o&&o.get("id")instanceof Number id&&o.get("param")instanceof Number param){int key=id.intValue();v.values.add(new OptionParam(map.getOrDefault(key,new OptionChoice(key,"Không còn tồn tại")),param.intValue()));}return v;}public String toString(){if(values.isEmpty())return "Không có";StringBuilder s=new StringBuilder();for(OptionParam p:values){if(s.length()>0)s.append("; ");s.append(p.option.id).append(':').append(p.param);}return s.toString();}}
    private static final class SlotValue{final int position;ItemChoice item;int quantity=1;OptionsValue options=new OptionsValue();long rateUnits;SlotValue(int position){this.position=position;}SlotValue copy(){SlotValue c=new SlotValue(position);c.item=item;c.quantity=quantity;c.options=options.copy();c.rateUnits=rateUnits;return c;}Reward toReward(Event e){Reward r=new Reward();r.eventId=e.id;r.configVersion=e.configVersion;r.position=position;r.itemTemplateId=item.id;r.quantity=quantity;r.optionsData=options.toJson();r.rateUnits=rateUnits;return r;}}
    private static final class RewardValue{ItemChoice item;int quantity=1;OptionsValue options=new OptionsValue();RewardValue copy(){RewardValue c=new RewardValue();c.item=item;c.quantity=quantity;c.options=options.copy();return c;}public String toString(){return item==null?"Chưa chọn":item.toString();}}
    private static final class RewardGroup{final List<RewardValue>rewards=new ArrayList<>();RewardGroup copy(){RewardGroup c=new RewardGroup();for(RewardValue r:rewards)c.rewards.add(r.copy());return c;}List<Reward>toRewards(Event event){List<Reward>list=new ArrayList<>();for(RewardValue v:rewards){Reward r=new Reward();r.eventId=event.id;r.itemTemplateId=v.item.id;r.quantity=v.quantity;r.optionsData=v.options.toJson();list.add(r);}return list;}static RewardGroup from(List<Reward>source,Map<Integer,ItemChoice>items,List<OptionChoice>options){RewardGroup g=new RewardGroup();for(Reward r:source){RewardValue v=new RewardValue();v.item=items.getOrDefault(r.itemTemplateId,new ItemChoice(r.itemTemplateId,"Không còn trong item_template"));v.quantity=r.quantity;v.options=OptionsValue.fromJson(r.optionsData,options);g.rewards.add(v);}return g;}public String toString(){if(rewards.isEmpty())return "Chưa cấu hình";StringBuilder s=new StringBuilder();for(RewardValue r:rewards){if(s.length()>0)s.append(" | ");s.append(r.item==null?"?":r.item.id).append(" x").append(r.quantity);}return s.toString();}}
}
