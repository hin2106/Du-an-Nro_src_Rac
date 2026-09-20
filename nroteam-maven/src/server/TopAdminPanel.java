package server;

import data.AlyraManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import top.TopBoardModels;
import top.TopBoardModels.Entry;
import top.TopBoardModels.Reward;
import top.TopBoardRepository;
import top.TopBoardService;

final class TopAdminPanel extends JPanel {
    private final Component owner;
    private final List<ItemChoice> items = new ArrayList<>();
    private RewardGroup clipboard;

    TopAdminPanel(Component owner) {
        super(new BorderLayout());
        this.owner = owner;
        loadItems();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Top Siêu Hạng", boardPanel(TopBoardModels.SUPER_RANK));
        tabs.addTab("Top Whis", boardPanel(TopBoardModels.WHIS));
        add(tabs);
    }

    private JPanel boardPanel(String board) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        DefaultTableModel rankingModel = new DefaultTableModel(
                new Object[]{"Hạng", "Tên nhân vật", "Bang hội", "Thành tích"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable rankingTable = new JTable(rankingModel);
        rankingTable.setRowHeight(30);

        DefaultTableModel rewardModel = new DefaultTableModel(new Object[]{"Hạng", "Quà đã cấu hình"}, 100) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (int i = 0; i < 100; i++) {
            rewardModel.setValueAt(i + 1, i, 0);
            rewardModel.setValueAt(new RewardGroup(), i, 1);
        }
        JTable rewardTable = new JTable(rewardModel);
        rewardTable.setRowHeight(48);
        rewardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rewardTable.getColumnModel().getColumn(0).setMaxWidth(70);
        rewardTable.getColumnModel().getColumn(1).setCellRenderer(new RewardGroupRenderer());
        rewardTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && rewardTable.getSelectedRow() >= 0) editSelected(rewardTable, rewardModel);
            }
        });

        Runnable loadRanking = () -> {
            rankingModel.setRowCount(0);
            List<Entry> rows = board.equals(TopBoardModels.WHIS)
                    ? TopBoardService.gI().queryWhis() : TopBoardService.gI().querySuperRank();
            for (Entry e : rows) {
                if (e.rank > 100) break;
                String achievement = board.equals(TopBoardModels.WHIS)
                        ? "LV " + e.primaryValue + " • " + formatTime(e.timeMs)
                        : e.primaryValue + " trận thắng";
                rankingModel.addRow(new Object[]{e.rank, e.name, e.guild == null ? "" : e.guild, achievement});
            }
        };
        Runnable loadRewards = () -> {
            for (int i = 0; i < 100; i++) rewardModel.setValueAt(new RewardGroup(), i, 1);
            try {
                for (Reward reward : TopBoardRepository.loadBoard(board)) {
                    int from = Math.max(1, reward.rankFrom), to = Math.min(100, reward.rankTo);
                    for (int rank = from; rank <= to; rank++) {
                        RewardGroup group = (RewardGroup) rewardModel.getValueAt(rank - 1, 1);
                        group.values.add(RewardValue.from(reward, findItem(reward.itemId)));
                    }
                }
                rewardTable.repaint();
            } catch (Exception e) { error(e.getMessage()); }
        };

        JButton refreshTop = new JButton("Làm mới danh sách Top");
        JButton edit = new JButton("Sửa quà hạng đã chọn");
        JButton copy = new JButton("Sao chép");
        JButton paste = new JButton("Dán vào hạng chọn");
        JButton pasteRange = new JButton("Dán vào khoảng...");
        JButton pasteTargets = new JButton("Dán vào các hạng...");
        JButton clear = new JButton("Xóa quà hạng chọn");
        JButton reload = new JButton("Tải lại cấu hình");
        JButton save = new JButton("Lưu toàn bộ 1–100");
        refreshTop.addActionListener(e -> loadRanking.run());
        edit.addActionListener(e -> editSelected(rewardTable, rewardModel));
        copy.addActionListener(e -> {
            int row = rewardTable.getSelectedRow();
            if (row < 0) { error("Hãy chọn một hạng để sao chép."); return; }
            clipboard = ((RewardGroup) rewardModel.getValueAt(row, 1)).copy();
        });
        paste.addActionListener(e -> {
            int row = rewardTable.getSelectedRow();
            if (row < 0 || clipboard == null) { error("Chưa chọn hạng hoặc chưa sao chép quà."); return; }
            rewardModel.setValueAt(clipboard.copy(), row, 1);
        });
        pasteRange.addActionListener(e -> pasteRange(rewardModel));
        pasteTargets.addActionListener(e -> pasteTargets(rewardModel));
        clear.addActionListener(e -> {
            int row = rewardTable.getSelectedRow();
            if (row >= 0) rewardModel.setValueAt(new RewardGroup(), row, 1);
        });
        reload.addActionListener(e -> loadRewards.run());
        save.addActionListener(e -> saveBoard(board, rewardModel, loadRewards));

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topActions.add(refreshTop);
        JPanel rankingPanel = new JPanel(new BorderLayout(5, 5));
        rankingPanel.add(topActions, BorderLayout.NORTH);
        rankingPanel.add(new JScrollPane(rankingTable), BorderLayout.CENTER);

        JPanel rewardActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        rewardActions.add(edit); rewardActions.add(copy); rewardActions.add(paste); rewardActions.add(pasteRange); rewardActions.add(pasteTargets);
        rewardActions.add(clear); rewardActions.add(reload); rewardActions.add(save);
        JPanel rewardsPanel = new JPanel(new BorderLayout(5, 5));
        rewardsPanel.add(rewardActions, BorderLayout.NORTH);
        rewardsPanel.add(new JScrollPane(rewardTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rankingPanel, rewardsPanel);
        split.setResizeWeight(0.35); split.setDividerLocation(230);
        root.add(split, BorderLayout.CENTER);
        loadRanking.run(); loadRewards.run();
        return root;
    }

    private void editSelected(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) { error("Hãy chọn một hạng."); return; }
        RewardGroup edited = editGroup(((RewardGroup) model.getValueAt(row, 1)).copy(), row + 1);
        if (edited != null) model.setValueAt(edited, row, 1);
    }

    private RewardGroup editGroup(RewardGroup group, int rank) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Loại", "Vật phẩm", "Số lượng", "Options"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2 || c == 3; }
        };
        for (RewardValue value : group.values) addRewardRow(model, value);
        JTable table = new JTable(model); table.setRowHeight(42); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(1).setCellRenderer(new ItemRenderer());
        JButton addItem = new JButton("+ Vật phẩm"); JButton addGold = new JButton("+ Vàng");
        JButton addGem = new JButton("+ Ngọc"); JButton changeItem = new JButton("Đổi vật phẩm"); JButton remove = new JButton("Xóa dòng");
        addItem.addActionListener(e -> { ItemChoice item = pickItem(); if (item != null) addRewardRow(model, new RewardValue((byte)1, item, 1, "[]")); });
        addGold.addActionListener(e -> addRewardRow(model, new RewardValue((byte)2, null, 1, "[]")));
        addGem.addActionListener(e -> addRewardRow(model, new RewardValue((byte)3, null, 1, "[]")));
        changeItem.addActionListener(e -> { int r=table.getSelectedRow();if(r>=0&&"Vật phẩm".equals(model.getValueAt(r,0))){ItemChoice item=pickItem();if(item!=null)model.setValueAt(item,r,1);}});
        remove.addActionListener(e -> { int r=table.getSelectedRow();if(r>=0)model.removeRow(r); });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(addItem); actions.add(addGold); actions.add(addGem); actions.add(changeItem); actions.add(remove);
        JPanel content = new JPanel(new BorderLayout(5, 5)); content.setPreferredSize(new Dimension(760, 470));
        content.add(new JScrollPane(table), BorderLayout.CENTER); content.add(actions, BorderLayout.SOUTH);
        if (JOptionPane.showConfirmDialog(owner, content, "Quà hạng " + rank,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return null;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        RewardGroup result = new RewardGroup();
        for (int i = 0; i < model.getRowCount(); i++) {
            String type = String.valueOf(model.getValueAt(i, 0));
            byte code = (byte)(type.equals("Vật phẩm") ? 1 : type.equals("Vàng") ? 2 : 3);
            ItemChoice item = model.getValueAt(i, 1) instanceof ItemChoice value ? value : null;
            int amount;
            try { amount = Integer.parseInt(String.valueOf(model.getValueAt(i, 2))); }
            catch (Exception e) { error("Số lượng quà không hợp lệ ở dòng " + (i + 1)); return null; }
            if (amount <= 0 || (code == 1 && item == null)) { error("Quà ở dòng " + (i + 1) + " chưa hợp lệ."); return null; }
            result.values.add(new RewardValue(code, item, amount, String.valueOf(model.getValueAt(i, 3))));
        }
        return result;
    }

    private void addRewardRow(DefaultTableModel model, RewardValue value) {
        model.addRow(new Object[]{value.type == 1 ? "Vật phẩm" : value.type == 2 ? "Vàng" : "Ngọc",
                value.item, value.amount, value.type == 1 ? value.options : "[]"});
    }

    private void pasteRange(DefaultTableModel model) {
        if (clipboard == null) { error("Hãy sao chép quà từ một hạng trước."); return; }
        JSpinner from = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JSpinner to = new JSpinner(new SpinnerNumberModel(100, 1, 100, 1));
        JPanel p = new JPanel(new FlowLayout()); p.add(new JLabel("Từ hạng:")); p.add(from); p.add(new JLabel("Đến hạng:")); p.add(to);
        if (JOptionPane.showConfirmDialog(owner, p, "Dán nhóm quà vào khoảng hạng",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        int first=(Integer)from.getValue(),last=(Integer)to.getValue();
        if(first>last){error("Hạng bắt đầu phải nhỏ hơn hoặc bằng hạng kết thúc.");return;}
        for(int rank=first;rank<=last;rank++)model.setValueAt(clipboard.copy(),rank-1,1);
    }

    private void pasteTargets(DefaultTableModel model) {
        if (clipboard == null) { error("Hãy sao chép quà từ một hạng trước."); return; }
        String text = JOptionPane.showInputDialog(owner,
                "Nhập các hạng hoặc khoảng, ví dụ: 1,2,5-10,20", "Dán vào các hạng", JOptionPane.PLAIN_MESSAGE);
        if (text == null || text.isBlank()) return;
        try {
            boolean[] targets = new boolean[101];
            for (String token : text.split(",")) {
                String part = token.trim(); if (part.isEmpty()) continue;
                if (part.contains("-")) {
                    String[] range = part.split("-", 2); int from=Integer.parseInt(range[0].trim()),to=Integer.parseInt(range[1].trim());
                    if(from<1||to>100||from>to)throw new IllegalArgumentException();
                    for(int rank=from;rank<=to;rank++)targets[rank]=true;
                } else { int rank=Integer.parseInt(part);if(rank<1||rank>100)throw new IllegalArgumentException();targets[rank]=true; }
            }
            for(int rank=1;rank<=100;rank++)if(targets[rank])model.setValueAt(clipboard.copy(),rank-1,1);
        } catch (Exception e) { error("Danh sách hạng không hợp lệ. Ví dụ đúng: 1,2,5-10,20"); }
    }

    private void saveBoard(String board, DefaultTableModel model, Runnable afterSave) {
        try {
            List<Reward> rows = new ArrayList<>();
            for (int rank = 1; rank <= 100; rank++) {
                RewardGroup group = (RewardGroup) model.getValueAt(rank - 1, 1);
                for (int slot = 0; slot < group.values.size(); slot++) {
                    RewardValue value = group.values.get(slot); Reward reward = new Reward();
                    reward.board=board;reward.rankFrom=rank;reward.rankTo=rank;reward.type=value.type;
                    reward.itemId=value.item==null?0:value.item.id;reward.amount=value.amount;
                    reward.options=value.options==null?"[]":value.options;reward.slot=slot;rows.add(reward);
                }
            }
            TopBoardRepository.replaceBoard(board, rows);
            JOptionPane.showMessageDialog(owner, "Đã lưu quà riêng cho 100 hạng."); afterSave.run();
        } catch (Exception e) { error(e.getMessage()); }
    }

    private void loadItems() {
        try(Connection con=AlyraManager.getConnection();PreparedStatement ps=con.prepareStatement(
                "SELECT id,name,icon_id FROM item_template WHERE id>0 ORDER BY id");ResultSet rs=ps.executeQuery()) {
            while(rs.next())items.add(new ItemChoice(rs.getInt(1),rs.getString(2),rs.getInt(3)));
        } catch(Exception e){error("Không tải được item_template: "+e.getMessage());}
    }

    private ItemChoice findItem(int id){if(id<=0)return null;for(ItemChoice i:items)if(i.id==id)return i;return new ItemChoice(id,"Không còn trong item_template",0);}
    private ItemChoice pickItem(){DefaultListModel<ItemChoice>model=new DefaultListModel<>();for(ItemChoice i:items)model.addElement(i);JList<ItemChoice>list=new JList<>(model);list.setCellRenderer(new ItemRenderer());list.setFixedCellHeight(46);JTextField search=new JTextField();search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){filter();}public void removeUpdate(javax.swing.event.DocumentEvent e){filter();}public void changedUpdate(javax.swing.event.DocumentEvent e){filter();}private void filter(){String q=search.getText().trim().toLowerCase(Locale.ROOT);model.clear();for(ItemChoice i:items)if(q.isEmpty()||i.name.toLowerCase(Locale.ROOT).contains(q)||String.valueOf(i.id).contains(q))model.addElement(i);}});JPanel p=new JPanel(new BorderLayout(4,4));p.add(search,BorderLayout.NORTH);p.add(new JScrollPane(list),BorderLayout.CENTER);p.setPreferredSize(new Dimension(520,540));return JOptionPane.showConfirmDialog(owner,p,"Chọn vật phẩm x4",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION?list.getSelectedValue():null;}
    private String formatTime(long ms){return String.format(Locale.US,"%.1f giây",ms/1000d);}
    private void error(String text){JOptionPane.showMessageDialog(owner,text,"Top",JOptionPane.ERROR_MESSAGE);}

    private static final class RewardValue {
        byte type; ItemChoice item; int amount; String options;
        RewardValue(byte type,ItemChoice item,int amount,String options){this.type=type;this.item=item;this.amount=amount;this.options=options==null?"[]":options;}
        RewardValue copy(){return new RewardValue(type,item,amount,options);}
        static RewardValue from(Reward r,ItemChoice item){return new RewardValue(r.type,item,r.amount,r.options);}
    }
    private static final class RewardGroup {
        final List<RewardValue> values=new ArrayList<>();
        RewardGroup copy(){RewardGroup c=new RewardGroup();for(RewardValue v:values)c.values.add(v.copy());return c;}
    }
    private static final class ItemChoice {
        final int id;final String name;final int iconId;
        ItemChoice(int id,String name,int iconId){this.id=id;this.name=name==null?"(không tên)":name;this.iconId=iconId;}
        ImageIcon icon(){return IconCache.get(iconId);}
        @Override public String toString(){return id+" - "+name;}
    }
    private static final class ItemRenderer extends DefaultListCellRenderer implements javax.swing.table.TableCellRenderer {
        private final DefaultTableCellRenderer tableRenderer=new DefaultTableCellRenderer();
        @Override public Component getListCellRendererComponent(JList<?>l,Object v,int i,boolean s,boolean f){JLabel c=(JLabel)super.getListCellRendererComponent(l,v,i,s,f);if(v instanceof ItemChoice item){c.setText(item.toString());c.setIcon(item.icon());}return c;}
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){JLabel label=(JLabel)tableRenderer.getTableCellRendererComponent(t,v,s,f,r,c);if(v instanceof ItemChoice item){label.setText(item.name);label.setIcon(item.icon());}else{label.setText("");label.setIcon(null);}return label;}
    }
    private static final class RewardGroupRenderer implements javax.swing.table.TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int column){
            JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT,5,3));
            if(selected)panel.setBackground(table.getSelectionBackground());else panel.setBackground(table.getBackground());
            if(value instanceof RewardGroup group){for(RewardValue reward:group.values){JLabel label=new JLabel(formatAmount(reward.amount));label.setHorizontalTextPosition(SwingConstants.RIGHT);if(reward.type==1&&reward.item!=null){label.setIcon(reward.item.icon());label.setToolTipText(reward.item.name+" x"+reward.amount);}else{label.setText((reward.type==2?"Vàng ":"Ngọc ")+formatAmount(reward.amount));}panel.add(label);}}
            return panel;
        }
        private static String formatAmount(int amount){if(amount>=1_000_000_000)return String.format(Locale.US,"%.1fB",amount/1_000_000_000d);if(amount>=1_000_000)return String.format(Locale.US,"%.1fM",amount/1_000_000d);if(amount>=1_000)return String.format(Locale.US,"%.1fK",amount/1_000d);return "x"+amount;}
    }
    static final class IconCache {
        private static final Map<Integer,ImageIcon>CACHE=new HashMap<>();
        static ImageIcon get(int id){if(id<=0)return null;if(CACHE.containsKey(id))return CACHE.get(id);ImageIcon icon=null;for(String base:new String[]{"data/icon/x4/","nroteam-maven/data/icon/x4/"}){try{File f=new File(base+id+".png");if(f.isFile()){BufferedImage img=ImageIO.read(f);Image scaled=img.getScaledInstance(36,36,Image.SCALE_SMOOTH);icon=new ImageIcon(scaled);break;}}catch(Exception ignored){}}CACHE.put(id,icon);return icon;}
    }
}
