package editor;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.*;

public final class EventMapEditor extends JFrame {
    private static final int TILE = 24;
    private static final int ASSET_SCALE = 4;
    private static final DataFlavor ASSET_FLAVOR = new DataFlavor(Integer.class, "NRO effect id");
    private static final Asset[] ASSETS = {
            new Asset(47, "Effect 47"), new Asset(48, "Effect 48"),
            new Asset(53, "Effect 53"), new Asset(54, "Effect 54"),
            new Asset(62, "Effect 62"), new Asset(63, "Effect 63"),
            new Asset(64, "Effect 64"), new Asset(65, "Effect 65"),
            new Asset(66, "Effect 66"), new Asset(67, "Effect 67"),
            new Asset(68, "Bí ngô Halloween"), new Asset(69, "Effect 69"),
            new Asset(70, "Effect 70"), new Asset(71, "Effect 71"),
            new Asset(72, "Cây thông lớn có nơ"), new Asset(73, "Cây thông Noel"),
            new Asset(74, "Effect 74"), new Asset(75, "Effect 75"),
            new Asset(76, "Cây mai vàng"), new Asset(77, "Cây đào hồng"),
            new Asset(244, "Effect 244"), new Asset(245, "Effect 245"),
            new Asset(248, "Effect 248"), new Asset(249, "Effect 249"),
            new Asset(250, "Effect 250"), new Asset(251, "Effect 251")
    };
    private static final EventChoice[] EVENTS = {
            new EventChoice(1, "Halloween"), new EventChoice(2, "20/11"),
            new EventChoice(3, "Noel"), new EventChoice(4, "Tết"),
            new EventChoice(5, "Hùng Vương"), new EventChoice(6, "Trung Thu"),
            new EventChoice(7, "Hè"), new EventChoice(8, "Valentine"),
            new EventChoice(9, "20/10")
    };

    private final Path serverDir;
    private final Path dataDir;
    private final Path clientRes;
    private final Db db;
    private final JComboBox<MapMeta> maps = new JComboBox<>();
    private final JComboBox<EventChoice> eventFilter = new JComboBox<>(EVENTS);
    private final JSlider zoom = new JSlider(1, 4, 4);
    private final JCheckBox showBgItems = new JCheckBox("BG item", true);
    private final JLabel status = new JLabel(" ");
    private final MapCanvas canvas = new MapCanvas();
    private final JList<Asset> palette = new JList<>(ASSETS);
    private final Inspector inspector = new Inspector();
    private final Map<Integer, BgTemplate> bgTemplates = new HashMap<>();
    private final Map<Integer, BufferedImage> bgImages = new HashMap<>();
    private final Map<Integer, BufferedImage> tileImages = new HashMap<>();
    private final Map<Integer, EffectAsset> effectAssets = new HashMap<>();
    private Scene scene;
    private PlacedEffect selected;
    private boolean dirty;
    private boolean loading;

    public EventMapEditor() throws Exception {
        super("NRO Event Map Editor - kéo thả effect theo sự kiện");
        serverDir = findServerDir();
        dataDir = serverDir.resolve("data");
        clientRes = serverDir.getParent().resolve("prj247/Assets/Resources/res/x4").normalize();
        db = new Db(dataDir.resolve("config/sever.properties"));
        buildUi();
        loadBgTemplates();
        loadMapList();
    }

    private void buildUi() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(1500, 900);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { if (confirmDiscard()) dispose(); }
        });

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        toolbar.add(new JLabel("Map:"));
        maps.setPreferredSize(new Dimension(280, 28));
        toolbar.add(maps);
        toolbar.add(new JLabel("Event đang xem:"));
        eventFilter.setPreferredSize(new Dimension(170, 28));
        toolbar.add(eventFilter);
        toolbar.add(new JLabel("Zoom:"));
        zoom.setPreferredSize(new Dimension(120, 28));
        zoom.setMajorTickSpacing(1);
        zoom.setPaintTicks(true);
        toolbar.add(zoom);
        toolbar.add(showBgItems);
        JButton reload = new JButton("Nạp lại DB");
        JButton save = new JButton("Lưu Ctrl+S");
        reload.addActionListener(e -> reloadScene());
        save.addActionListener(e -> saveScene());
        toolbar.add(reload);
        toolbar.add(save);
        root.add(toolbar, BorderLayout.NORTH);

        JScrollPane mapScroll = new JScrollPane(canvas);
        mapScroll.getVerticalScrollBar().setUnitIncrement(24);
        mapScroll.getHorizontalScrollBar().setUnitIncrement(24);

        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setPreferredSize(new Dimension(320, 0));
        palette.setCellRenderer(new AssetRenderer());
        palette.setDragEnabled(true);
        palette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        palette.setTransferHandler(new AssetTransferHandler());
        JScrollPane paletteScroll = new JScrollPane(palette);
        paletteScroll.setBorder(BorderFactory.createTitledBorder("Kéo asset thả vào map"));
        paletteScroll.setPreferredSize(new Dimension(310, 300));
        right.add(paletteScroll, BorderLayout.NORTH);
        right.add(inspector, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapScroll, right);
        split.setResizeWeight(1);
        split.setDividerLocation(1130);
        root.add(split, BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(3, 5, 3, 5));
        root.add(status, BorderLayout.SOUTH);
        setContentPane(root);

        maps.addActionListener(e -> { if (!loading) changeMap(); });
        eventFilter.addActionListener(e -> { clearSelection(); canvas.repaint(); });
        zoom.addChangeListener(e -> canvas.updateSize());
        showBgItems.addActionListener(e -> canvas.repaint());

        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save", this::saveScene);
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "duplicate", this::duplicateSelected);
        bindKey(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete", this::deleteSelected);
    }

    private void bindKey(KeyStroke key, String name, Runnable action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getRootPane().getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private void loadMapList() {
        loading = true;
        maps.removeAllItems();
        try (Connection con = db.open();
             PreparedStatement ps = con.prepareStatement("SELECT id, NAME, tile_id, bg_id, bg_type FROM map_template ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                maps.addItem(new MapMeta(rs.getInt("id"), rs.getString("NAME"), rs.getInt("tile_id"),
                        rs.getInt("bg_id"), rs.getInt("bg_type")));
            }
        } catch (Exception e) {
            error("Không tải được map", e);
        } finally {
            loading = false;
        }
        if (maps.getItemCount() > 0) maps.setSelectedIndex(0);
        loadSelectedMap();
    }

    private void loadBgTemplates() throws SQLException {
        try (Connection con = db.open();
             PreparedStatement ps = con.prepareStatement("SELECT id, image_id, layer, dx, dy FROM bg_item_template");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BgTemplate t = new BgTemplate(rs.getInt("id"), rs.getInt("image_id"), rs.getInt("layer"),
                        rs.getInt("dx"), rs.getInt("dy"));
                bgTemplates.put(t.id, t);
            }
        }
    }

    private void changeMap() {
        if (!confirmDiscard()) return;
        loadSelectedMap();
    }

    private void reloadScene() {
        if (!confirmDiscard()) return;
        loadSelectedMap();
    }

    private void loadSelectedMap() {
        MapMeta meta = (MapMeta) maps.getSelectedItem();
        if (meta == null) return;
        try {
            Scene next = new Scene(meta);
            readTiles(next);
            readBgItems(next);
            try (Connection con = db.open();
                 PreparedStatement ps = con.prepareStatement("SELECT eff_event FROM map_template WHERE id=?")) {
                ps.setInt(1, meta.id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String json = rs.getString(1);
                        JSONArray array = json == null || json.isBlank() ? new JSONArray() : new JSONArray(json);
                        for (int i = 0; i < array.length(); i++) next.effects.add(PlacedEffect.from(array.getJSONObject(i)));
                    }
                }
            }
            scene = next;
            selected = null;
            inspector.setEffect(null);
            dirty = false;
            canvas.updateSize();
            setStatus("Đã dựng map " + meta.id + " - " + meta.name + ", " + next.effects.size() + " effect.", false);
        } catch (Exception e) {
            error("Không dựng được map " + meta.id, e);
        }
    }

    private void readTiles(Scene s) throws IOException {
        Path file = dataDir.resolve("map/tile_map_data/" + s.meta.id);
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            s.width = in.readUnsignedByte();
            s.height = in.readUnsignedByte();
            s.tiles = new int[s.width * s.height];
            for (int i = 0; i < s.tiles.length; i++) s.tiles[i] = in.readUnsignedByte();
        }
    }

    private void readBgItems(Scene s) throws IOException {
        Path file = dataDir.resolve("map/item_bg_map_data/" + s.meta.id);
        if (!Files.exists(file)) return;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            int count = in.readUnsignedShort();
            for (int i = 0; i < count; i++) {
                int templateId = in.readUnsignedShort();
                int tileX = in.readUnsignedShort();
                int tileY = in.readUnsignedShort();
                BgTemplate t = bgTemplates.get(templateId);
                if (t != null) s.bgItems.add(new BgPlaced(t, tileX * TILE, tileY * TILE));
            }
        }
    }

    private void saveScene() {
        if (scene == null) return;
        JSONArray array = new JSONArray();
        for (PlacedEffect e : scene.effects) array.put(e.toJson());
        try (Connection con = db.open();
             PreparedStatement ps = con.prepareStatement("UPDATE map_template SET eff_event=? WHERE id=?")) {
            ps.setString(1, array.toString(2));
            ps.setInt(2, scene.meta.id);
            if (ps.executeUpdate() != 1) throw new SQLException("Map không tồn tại");
            dirty = false;
            setStatus("Đã lưu map " + scene.meta.id + ". Preview trong editor đã cập nhật tức thì.", false);
        } catch (Exception e) {
            error("Không lưu được map", e);
        }
    }

    private void addEffect(int id, int mapX, int mapY) {
        if (scene == null) return;
        EventChoice ev = currentEvent();
        PlacedEffect p = new PlacedEffect(ev.id, id, 2, mapX, mapY, 0, 0);
        scene.effects.add(p);
        setSelected(p);
        dirty = true;
        canvas.repaint();
    }

    private void duplicateSelected() {
        if (selected == null || scene == null) return;
        PlacedEffect p = selected.copy();
        p.x += 50;
        scene.effects.add(p);
        setSelected(p);
        dirty = true;
        canvas.repaint();
    }

    private void deleteSelected() {
        if (selected == null || scene == null) return;
        scene.effects.remove(selected);
        clearSelection();
        dirty = true;
        canvas.repaint();
    }

    private void setSelected(PlacedEffect effect) {
        selected = effect;
        inspector.setEffect(effect);
        canvas.repaint();
    }

    private void clearSelection() {
        selected = null;
        inspector.setEffect(null);
    }

    private boolean confirmDiscard() {
        if (!dirty) return true;
        int answer = JOptionPane.showConfirmDialog(this, "Map chưa lưu. Bỏ các thay đổi?", "Chưa lưu",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    private EventChoice currentEvent() {
        EventChoice e = (EventChoice) eventFilter.getSelectedItem();
        return e == null ? EVENTS[0] : e;
    }

    private EffectAsset effectAsset(int id) {
        return effectAssets.computeIfAbsent(id, key -> {
            try { return EffectAsset.load(key, dataDir); }
            catch (Exception e) {
                setStatus("Không đọc được DataEffect_" + key + ": " + e.getMessage(), true);
                return EffectAsset.missing(key);
            }
        });
    }

    private BufferedImage tileImage(int tileSet, int index) {
        int key = tileSet * 1000 + index;
        return tileImages.computeIfAbsent(key,
                unused -> readImage(dataDir.resolve("res/x4/" + tileSet + "$" + index)));
    }

    private BufferedImage bgImage(int imageId) {
        return bgImages.computeIfAbsent(imageId,
                key -> readImage(dataDir.resolve("item_bg_temp/x4/" + key + ".png")));
    }

    private static BufferedImage readImage(Path path) {
        try { return Files.exists(path) ? ImageIO.read(path.toFile()) : null; }
        catch (IOException ignored) { return null; }
    }

    private void setStatus(String message, boolean bad) {
        status.setForeground(bad ? new Color(255, 100, 100) : new Color(100, 220, 130));
        status.setText(message);
    }

    private void error(String message, Exception e) {
        setStatus(message + ": " + e.getMessage(), true);
        JOptionPane.showMessageDialog(this, message + "\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private static Path findServerDir() throws IOException {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        List<Path> candidates = List.of(cwd.resolve("../nroteam-maven").normalize(), cwd.resolve("nroteam-maven"), cwd);
        for (Path p : candidates) if (Files.exists(p.resolve("data/config/sever.properties"))) return p;
        throw new IOException("Không tìm thấy nroteam-maven/data/config/sever.properties");
    }

    private final class MapCanvas extends JComponent {
        private PlacedEffect dragging;
        private int dragDx, dragDy;

        MapCanvas() {
            setOpaque(true);
            setBackground(new Color(70, 145, 210));
            setTransferHandler(new CanvasTransferHandler());
            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    Point p = toMap(e.getPoint());
                    dragging = findEffect(p.x, p.y);
                    setSelected(dragging);
                    if (dragging != null) { dragDx = p.x - dragging.x; dragDy = p.y - dragging.y; }
                }
                @Override public void mouseDragged(MouseEvent e) {
                    if (dragging == null) return;
                    Point p = toMap(e.getPoint());
                    dragging.x = p.x - dragDx;
                    dragging.y = p.y - dragDy;
                    dirty = true;
                    inspector.refreshCoordinates();
                    repaint();
                }
                @Override public void mouseReleased(MouseEvent e) { dragging = null; }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            new javax.swing.Timer(100, e -> repaint()).start();
        }

        void updateSize() {
            int z = zoom.getValue();
            int w = scene == null ? 800 : scene.width * TILE * z;
            int h = scene == null ? 500 : scene.height * TILE * z;
            setPreferredSize(new Dimension(w, h));
            revalidate();
            repaint();
        }

        Point toMap(Point p) { int z = zoom.getValue(); return new Point(p.x / z, p.y / z); }

        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            if (scene == null) return;
            Graphics2D g = (Graphics2D) raw.create();
            g.scale(zoom.getValue(), zoom.getValue());
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            paintBackdrop(g);
            paintBgLayer(g, 1);
            paintTiles(g);
            paintBgLayer(g, 2);
            paintEffects(g, 1);
            paintEffects(g, 2);
            paintBgLayer(g, 3);
            paintEffects(g, 3);
            paintBgLayer(g, 4);
            paintEffects(g, 4);
            g.dispose();
        }

        private void paintBackdrop(Graphics2D g) {
            int w = scene.width * TILE, h = scene.height * TILE;
            Color top = switch (scene.meta.bgId % 6) {
                case 1 -> new Color(129, 190, 235); case 2 -> new Color(94, 174, 214);
                case 3 -> new Color(36, 54, 92); case 4 -> new Color(210, 132, 75);
                case 5 -> new Color(90, 74, 136); default -> new Color(112, 190, 232);
            };
            g.setPaint(new GradientPaint(0, 0, top, 0, h, top.darker()));
            g.fillRect(0, 0, w, h);
            BufferedImage bg = readImage(clientRes.resolve("bg/b" + scene.meta.bgId + "0.png"));
            if (bg != null) {
                int bw = Math.max(1, bg.getWidth() / ASSET_SCALE);
                int bh = Math.max(1, bg.getHeight() / ASSET_SCALE);
                for (int xx = 0; xx < w; xx += bw) g.drawImage(bg, xx, Math.max(0, h - bh), bw, bh, null);
            }
        }

        private void paintTiles(Graphics2D g) {
            for (int yy = 0; yy < scene.height; yy++) for (int xx = 0; xx < scene.width; xx++) {
                int index = scene.tiles[yy * scene.width + xx];
                if (index <= 0) continue;
                BufferedImage image = tileImage(scene.meta.tileId, index);
                if (image != null) g.drawImage(image, xx * TILE, yy * TILE, TILE, TILE, null);
            }
        }

        private void paintBgLayer(Graphics2D g, int layer) {
            if (!showBgItems.isSelected()) return;
            for (BgPlaced p : scene.bgItems) {
                if (p.template.layer != layer) continue;
                BufferedImage image = bgImage(p.template.imageId);
                if (image != null) {
                    int width = Math.max(1, image.getWidth() / ASSET_SCALE);
                    int height = Math.max(1, image.getHeight() / ASSET_SCALE);
                    g.drawImage(image, p.x + p.template.dx, p.y + p.template.dy, width, height, null);
                }
            }
        }

        private void paintEffects(Graphics2D g, int layer) {
            int eventId = currentEvent().id;
            for (PlacedEffect p : scene.effects) {
                if (p.eventId != eventId || p.layer != layer) continue;
                EffectAsset asset = effectAsset(p.effectId);
                asset.paint(g, p.x, p.y, System.currentTimeMillis());
                if (p == selected) {
                    Rectangle b = asset.boundsAt(p.x, p.y);
                    g.setColor(new Color(0, 255, 240, 210));
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawRect(b.x, b.y, Math.max(1, b.width), Math.max(1, b.height));
                    g.drawLine(p.x - 5, p.y, p.x + 5, p.y);
                    g.drawLine(p.x, p.y - 5, p.x, p.y + 5);
                    g.setColor(Color.WHITE);
                    g.drawString("(" + p.x + ", " + p.y + ")", p.x + 7, p.y - 7);
                }
            }
        }

        private PlacedEffect findEffect(int mx, int my) {
            if (scene == null) return null;
            int eventId = currentEvent().id;
            ListIterator<PlacedEffect> it = scene.effects.listIterator(scene.effects.size());
            while (it.hasPrevious()) {
                PlacedEffect p = it.previous();
                if (p.eventId == eventId && effectAsset(p.effectId).boundsAt(p.x, p.y).contains(mx, my)) return p;
            }
            return null;
        }
    }

    private final class Inspector extends JPanel {
        private final JComboBox<EventChoice> event = new JComboBox<>(EVENTS);
        private final JSpinner effect = spin(68, 0, Short.MAX_VALUE);
        private final JSpinner layer = spin(2, 1, 4);
        private final JSpinner x = spin(0, Short.MIN_VALUE, Short.MAX_VALUE);
        private final JSpinner y = spin(0, Short.MIN_VALUE, Short.MAX_VALUE);
        private final JSpinner loop = spin(0, -1, Short.MAX_VALUE);
        private final JSpinner delay = spin(0, -1, Short.MAX_VALUE);
        private boolean filling;

        Inspector() {
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createTitledBorder("Effect đang chọn"));
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(5, 6, 5, 6); c.fill = GridBagConstraints.HORIZONTAL;
            addRow(c, 0, "Event", event); addRow(c, 1, "Effect ID", effect); addRow(c, 2, "Layer", layer);
            addRow(c, 3, "X", x); addRow(c, 4, "Y", y); addRow(c, 5, "Loop", loop); addRow(c, 6, "Delay", delay);
            JPanel actions = new JPanel(new GridLayout(1, 2, 5, 0));
            JButton dup = new JButton("Nhân bản"); JButton del = new JButton("Xóa");
            dup.addActionListener(e -> duplicateSelected()); del.addActionListener(e -> deleteSelected());
            actions.add(dup); actions.add(del);
            c.gridx = 0; c.gridy = 7; c.gridwidth = 2; c.weighty = 1; c.anchor = GridBagConstraints.NORTH;
            add(actions, c);
            ChangeListener change = e -> apply();
            effect.addChangeListener(change); layer.addChangeListener(change); x.addChangeListener(change);
            y.addChangeListener(change); loop.addChangeListener(change); delay.addChangeListener(change);
            event.addActionListener(e -> apply());
            setEnabledFields(false);
        }

        private JSpinner spin(int value, int min, int max) { return new JSpinner(new SpinnerNumberModel(value, min, max, 1)); }
        private void addRow(GridBagConstraints c, int row, String name, JComponent input) {
            c.gridwidth = 1; c.weighty = 0; c.gridy = row; c.gridx = 0; c.weightx = 0; add(new JLabel(name), c);
            c.gridx = 1; c.weightx = 1; add(input, c);
        }
        void setEffect(PlacedEffect p) {
            filling = true;
            setEnabledFields(p != null);
            if (p != null) {
                selectEvent(event, p.eventId); effect.setValue(p.effectId); layer.setValue(Math.max(1, Math.min(4, p.layer)));
                x.setValue(p.x); y.setValue(p.y); loop.setValue(p.loop); delay.setValue(p.delay);
            }
            filling = false;
        }
        void refreshCoordinates() {
            if (selected == null) return;
            filling = true; x.setValue(selected.x); y.setValue(selected.y); filling = false;
        }
        private void apply() {
            if (filling || selected == null) return;
            EventChoice ev = (EventChoice) event.getSelectedItem();
            selected.eventId = ev == null ? 1 : ev.id; selected.effectId = val(effect); selected.layer = val(layer);
            selected.x = val(x); selected.y = val(y); selected.loop = val(loop); selected.delay = val(delay);
            dirty = true; canvas.repaint();
        }
        private void setEnabledFields(boolean enabled) {
            event.setEnabled(enabled); effect.setEnabled(enabled); layer.setEnabled(enabled); x.setEnabled(enabled);
            y.setEnabled(enabled); loop.setEnabled(enabled); delay.setEnabled(enabled);
        }
    }

    private static int val(JSpinner s) { return ((Number) s.getValue()).intValue(); }
    private static void selectEvent(JComboBox<EventChoice> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) if (combo.getItemAt(i).id == id) combo.setSelectedIndex(i);
    }

    private final class AssetRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            Asset asset = (Asset) value;
            BufferedImage image = effectAsset(asset.id).preview();
            if (image != null) label.setIcon(new ImageIcon(image.getScaledInstance(90, 70, Image.SCALE_SMOOTH)));
            label.setText("<html><b>" + asset.id + "</b> - " + asset.name + "</html>");
            label.setBorder(new EmptyBorder(5, 5, 5, 5));
            return label;
        }
    }

    private final class AssetTransferHandler extends TransferHandler {
        @Override protected Transferable createTransferable(JComponent c) {
            Asset a = palette.getSelectedValue();
            return a == null ? null : new IdTransferable(a.id);
        }
        @Override public int getSourceActions(JComponent c) { return DnDConstants.ACTION_COPY; }
    }

    private final class CanvasTransferHandler extends TransferHandler {
        @Override public boolean canImport(TransferSupport support) { return support.isDataFlavorSupported(ASSET_FLAVOR); }
        @Override public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            try {
                int id = (Integer) support.getTransferable().getTransferData(ASSET_FLAVOR);
                Point p = canvas.toMap(support.getDropLocation().getDropPoint());
                addEffect(id, p.x, p.y);
                return true;
            } catch (Exception e) { error("Không thả được asset", e); return false; }
        }
    }

    private record IdTransferable(int id) implements Transferable {
        @Override public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{ASSET_FLAVOR}; }
        @Override public boolean isDataFlavorSupported(DataFlavor flavor) { return ASSET_FLAVOR.equals(flavor); }
        @Override public Object getTransferData(DataFlavor flavor) { return id; }
    }

    private static final class EffectAsset {
        final int id; final BufferedImage sheet; final List<ImgPart> images; final List<FrameData> frames; final int[] animation;
        EffectAsset(int id, BufferedImage sheet, List<ImgPart> images, List<FrameData> frames, int[] animation) {
            this.id=id; this.sheet=sheet; this.images=images; this.frames=frames; this.animation=animation;
        }
        static EffectAsset load(int id, Path data) throws IOException {
            BufferedImage sheet = ImageIO.read(data.resolve("effect/x4/ImgEffect_" + id + ".png").toFile());
            byte[] raw = Files.readAllBytes(data.resolve("effdata/DataEffect_" + id));
            try { return parse(id, sheet, raw, false); }
            catch (IOException oldFormatFailed) { return parse(id, sheet, raw, true); }
        }
        private static EffectAsset parse(int id, BufferedImage sheet, byte[] raw, boolean wideCoordinates) throws IOException {
            List<ImgPart> parts = new ArrayList<>(); List<FrameData> frames = new ArrayList<>(); int[] anim;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw))) {
                int count = in.readUnsignedByte();
                if (count == 0) throw new IOException("DataEffect không có image part");
                int logicalWidth = sheet.getWidth() / ASSET_SCALE;
                int logicalHeight = sheet.getHeight() / ASSET_SCALE;
                for (int i=0;i<count;i++) {
                    int imageId = in.readUnsignedByte();
                    int imageX = wideCoordinates ? in.readUnsignedShort() : in.readUnsignedByte();
                    int imageY = wideCoordinates ? in.readUnsignedShort() : in.readUnsignedByte();
                    int width = in.readUnsignedByte(), height = in.readUnsignedByte();
                    if (width <= 0 || height <= 0 || imageX + width > logicalWidth || imageY + height > logicalHeight) {
                        throw new IOException("Image part vượt atlas (wide=" + wideCoordinates + ")");
                    }
                    parts.add(new ImgPart(imageId,imageX,imageY,width,height));
                }
                int frameCount = in.readUnsignedShort();
                if (frameCount <= 0 || frameCount > 4096) throw new IOException("Số frame không hợp lệ: " + frameCount);
                for (int i=0;i<frameCount;i++) {
                    int pieces=in.readUnsignedByte(); List<Piece> list=new ArrayList<>();
                    for (int j=0;j<pieces;j++) {
                        Piece piece = new Piece(in.readShort(),in.readShort(),in.readUnsignedByte());
                        boolean found = false;
                        for (ImgPart part : parts) if (part.id == piece.imageId) { found = true; break; }
                        if (!found) throw new IOException("Frame tham chiếu image part không tồn tại: " + piece.imageId);
                        list.add(piece);
                    }
                    frames.add(new FrameData(list));
                }
                int animCount=in.readUnsignedShort(); anim=new int[animCount];
                for(int i=0;i<animCount;i++) anim[i]=in.readShort();
            }
            return new EffectAsset(id,sheet,parts,frames,anim);
        }
        static EffectAsset missing(int id) { return new EffectAsset(id,null,List.of(),List.of(),new int[0]); }
        void paint(Graphics2D g,int x,int y,long time) {
            if(sheet==null||frames.isEmpty()) return;
            for(Piece piece:currentFrame(time).pieces) {
                ImgPart p=part(piece.imageId); if(p==null) continue;
                int sx = p.x * ASSET_SCALE, sy = p.y * ASSET_SCALE;
                int sw = p.w * ASSET_SCALE, sh = p.h * ASSET_SCALE;
                g.drawImage(sheet,x+piece.dx,y+piece.dy,x+piece.dx+p.w,y+piece.dy+p.h,sx,sy,sx+sw,sy+sh,null);
            }
        }
        Rectangle boundsAt(int x,int y) {
            if(frames.isEmpty()) return new Rectangle(x-12,y-24,24,24);
            int minX=0,minY=0,maxX=1,maxY=1;
            for(FrameData f:frames) for(Piece piece:f.pieces) { ImgPart p=part(piece.imageId); if(p==null) continue;
                minX=Math.min(minX,piece.dx); minY=Math.min(minY,piece.dy); maxX=Math.max(maxX,piece.dx+p.w); maxY=Math.max(maxY,piece.dy+p.h); }
            return new Rectangle(x+minX,y+minY,maxX-minX,maxY-minY);
        }
        BufferedImage preview() {
            if(sheet==null||frames.isEmpty()) return sheet;
            Rectangle b=boundsAt(0,0); BufferedImage out=new BufferedImage(Math.max(1,b.width),Math.max(1,b.height),BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=out.createGraphics(); paint(g,-b.x,-b.y,0); g.dispose(); return out;
        }
        private FrameData currentFrame(long time) {
            int index=animation.length==0?0:animation[(int)((time/120)%animation.length)];
            return frames.get(Math.max(0,Math.min(index,frames.size()-1)));
        }
        private ImgPart part(int id) { for(ImgPart p:images) if(p.id==id) return p; return null; }
    }

    private static final class Db {
        final String url,user,pass;
        Db(Path propertiesFile) throws IOException {
            Properties p=new Properties(); try(InputStream in=Files.newInputStream(propertiesFile)){p.load(in);}
            String host=p.getProperty("database.host","localhost"),port=p.getProperty("database.port","3306"),name=p.getProperty("database.name","nro");
            user=p.getProperty("database.user","root"); pass=p.getProperty("database.pass","");
            url="jdbc:mysql://"+host+":"+port+"/"+name+"?useUnicode=true&characterEncoding=UTF-8";
        }
        Connection open() throws SQLException { return DriverManager.getConnection(url,user,pass); }
    }

    private static final class Scene {
        final MapMeta meta; int width,height; int[] tiles; final List<BgPlaced> bgItems=new ArrayList<>(); final List<PlacedEffect> effects=new ArrayList<>();
        Scene(MapMeta meta){this.meta=meta;}
    }
    private static final class PlacedEffect {
        int eventId,effectId,layer,x,y,loop,delay;
        PlacedEffect(int eventId,int effectId,int layer,int x,int y,int loop,int delay){this.eventId=eventId;this.effectId=effectId;this.layer=layer;this.x=x;this.y=y;this.loop=loop;this.delay=delay;}
        static PlacedEffect from(JSONObject o){return new PlacedEffect(o.optInt("event_id"),o.optInt("eff_id"),o.optInt("layer",2),o.optInt("x"),o.optInt("y"),o.optInt("loop"),o.optInt("delay"));}
        JSONObject toJson(){return new JSONObject().put("event_id",eventId).put("eff_id",effectId).put("layer",layer).put("x",x).put("y",y).put("loop",loop).put("delay",delay);}
        PlacedEffect copy(){return new PlacedEffect(eventId,effectId,layer,x,y,loop,delay);}
    }
    private record MapMeta(int id,String name,int tileId,int bgId,int bgType){@Override public String toString(){return id+" - "+name;}}
    private record EventChoice(int id,String name){@Override public String toString(){return id+" - "+name;}}
    private record Asset(int id,String name){@Override public String toString(){return id+" - "+name;}}
    private record BgTemplate(int id,int imageId,int layer,int dx,int dy){}
    private record BgPlaced(BgTemplate template,int x,int y){}
    private record ImgPart(int id,int x,int y,int w,int h){}
    private record Piece(int dx,int dy,int imageId){}
    private record FrameData(List<Piece> pieces){}

    private static void selfCheck() throws Exception {
        Path server = findServerDir();
        Path data = server.resolve("data");
        Db db = new Db(data.resolve("config/sever.properties"));
        int mapCount;
        int bgTemplateCount;
        try (Connection con = db.open();
             PreparedStatement ps = con.prepareStatement("SELECT (SELECT COUNT(*) FROM map_template), (SELECT COUNT(*) FROM bg_item_template)");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            mapCount = rs.getInt(1);
            bgTemplateCount = rs.getInt(2);
        }
        Path map0 = data.resolve("map/tile_map_data/0");
        try (DataInputStream in = new DataInputStream(Files.newInputStream(map0))) {
            int width = in.readUnsignedByte(), height = in.readUnsignedByte();
            if (width <= 0 || height <= 0) throw new IOException("Kích thước map 0 không hợp lệ");
        }
        for (Asset asset : ASSETS) {
            try { EffectAsset.load(asset.id, data); }
            catch (Exception e) { throw new IOException("Không giải mã được effect " + asset.id, e); }
        }
        BufferedImage tile = ImageIO.read(data.resolve("res/x4/1$1").toFile());
        if (tile == null) throw new IOException("Không đọc được tile client");
        BufferedImage bgItem = ImageIO.read(data.resolve("item_bg_temp/x4/0.png").toFile());
        if (bgItem == null) throw new IOException("Không đọc được BG item x4");
        System.out.println("SELF-CHECK OK: " + mapCount + " maps; " + bgTemplateCount
                + " bg templates; x4 tile/effect/bg assets readable; database connected.");
    }

    private static void cloneEventDecorations(boolean apply) throws Exception {
        Path server = findServerDir();
        Db db = new Db(server.resolve("data/config/sever.properties"));
        JSONArray backupRows = new JSONArray();
        List<CloneUpdate> updates = new ArrayList<>();
        int sourceCount = 0;

        try (Connection con = db.open();
             PreparedStatement ps = con.prepareStatement("SELECT id, NAME, eff_event FROM map_template ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int mapId = rs.getInt("id");
                String mapName = rs.getString("NAME");
                String original = rs.getString("eff_event");
                JSONArray effects = original == null || original.isBlank() ? new JSONArray() : new JSONArray(original);
                List<JSONObject> sources = new ArrayList<>();
                for (int i = 0; i < effects.length(); i++) {
                    JSONObject effect = effects.getJSONObject(i);
                    if (effect.optInt("event_id") == 1 && effect.optInt("eff_id") == 68) {
                        sources.add(effect);
                    }
                }
                if (sources.isEmpty()) continue;

                JSONArray result = new JSONArray();
                for (int i = 0; i < effects.length(); i++) {
                    JSONObject effect = effects.getJSONObject(i);
                    int eventId = effect.optInt("event_id");
                    int effectId = effect.optInt("eff_id");
                    boolean generatedTarget = (eventId == 3 && (effectId == 72 || effectId == 73))
                            || (eventId == 4 && (effectId == 76 || effectId == 77));
                    if (!generatedTarget) result.put(new JSONObject(effect.toString()));
                }

                Random random = new Random(System.nanoTime() ^ ((long) mapId << 32));
                for (int i = 0; i < sources.size(); i++) {
                    int noelId = sources.size() == 1 ? (random.nextBoolean() ? 72 : 73) : (i % 2 == 0 ? 72 : 73);
                    JSONObject clone = new JSONObject(sources.get(i).toString());
                    clone.put("event_id", 3).put("eff_id", noelId);
                    result.put(clone);
                }
                for (int i = 0; i < sources.size(); i++) {
                    int tetId = sources.size() == 1 ? (random.nextBoolean() ? 76 : 77) : (i % 2 == 0 ? 76 : 77);
                    JSONObject clone = new JSONObject(sources.get(i).toString());
                    clone.put("event_id", 4).put("eff_id", tetId);
                    result.put(clone);
                }

                sourceCount += sources.size();
                updates.add(new CloneUpdate(mapId, mapName, result.toString(2), sources.size()));
                backupRows.put(new JSONObject().put("map_id", mapId).put("map_name", mapName)
                        .put("eff_event", original == null ? JSONObject.NULL : original));
                System.out.println("Map " + mapId + " - " + mapName + ": " + sources.size() + " mốc eff 68");
            }
        }

        System.out.println("Tổng: " + updates.size() + " map, " + sourceCount + " mốc; tạo "
                + sourceCount + " effect Noel và " + sourceCount + " effect Tết.");
        if (!apply) {
            System.out.println("DRY-RUN: chưa ghi database.");
            return;
        }

        Path backupDir = Paths.get("").toAbsolutePath().resolve("backups");
        Files.createDirectories(backupDir);
        String stamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
        Path backupFile = backupDir.resolve("eff_event-before-clone-" + stamp + ".json");
        JSONObject backup = new JSONObject().put("created_at", stamp).put("rows", backupRows);
        Files.writeString(backupFile, backup.toString(2), java.nio.charset.StandardCharsets.UTF_8);

        try (Connection con = db.open()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("UPDATE map_template SET eff_event=? WHERE id=?")) {
                for (CloneUpdate update : updates) {
                    ps.setString(1, update.json);
                    ps.setInt(2, update.mapId);
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
        System.out.println("APPLY OK. Backup: " + backupFile);
    }

    private static void verifyEventDecorations() throws Exception {
        Path server = findServerDir();
        Db db = new Db(server.resolve("data/config/sever.properties"));
        int mapsChecked = 0, markersChecked = 0;
        try (Connection con = db.open();
             PreparedStatement ps = con.prepareStatement("SELECT id, eff_event FROM map_template ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String json = rs.getString("eff_event");
                JSONArray effects = json == null || json.isBlank() ? new JSONArray() : new JSONArray(json);
                List<JSONObject> source = new ArrayList<>(), noel = new ArrayList<>(), tet = new ArrayList<>();
                for (int i = 0; i < effects.length(); i++) {
                    JSONObject e = effects.getJSONObject(i);
                    if (e.optInt("event_id") == 1 && e.optInt("eff_id") == 68) source.add(e);
                    if (e.optInt("event_id") == 3 && (e.optInt("eff_id") == 72 || e.optInt("eff_id") == 73)) noel.add(e);
                    if (e.optInt("event_id") == 4 && (e.optInt("eff_id") == 76 || e.optInt("eff_id") == 77)) tet.add(e);
                }
                if (source.isEmpty()) continue;
                if (noel.size() != source.size() || tet.size() != source.size()) {
                    throw new IllegalStateException("Map " + rs.getInt("id") + " sai số lượng clone");
                }
                for (int i = 0; i < source.size(); i++) {
                    JSONObject src = source.get(i), n = noel.get(i), t = tet.get(i);
                    for (String key : List.of("layer", "x", "y", "loop", "delay")) {
                        if (src.optInt(key) != n.optInt(key) || src.optInt(key) != t.optInt(key)) {
                            throw new IllegalStateException("Map " + rs.getInt("id") + " lệch " + key + " tại mốc " + i);
                        }
                    }
                    if (source.size() > 1) {
                        if (n.optInt("eff_id") != (i % 2 == 0 ? 72 : 73)
                                || t.optInt("eff_id") != (i % 2 == 0 ? 76 : 77)) {
                            throw new IllegalStateException("Map " + rs.getInt("id") + " không so le tại mốc " + i);
                        }
                    }
                }
                mapsChecked++;
                markersChecked += source.size();
            }
        }
        System.out.println("VERIFY OK: " + mapsChecked + " maps, " + markersChecked
                + " markers; counts, coordinates, layers and alternating IDs match.");
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--check".equals(args[0])) {
            try { selfCheck(); }
            catch (Exception e) { e.printStackTrace(); System.exit(1); }
            return;
        }
        if (args.length > 0 && ("--clone-events".equals(args[0]) || "--clone-events-dry-run".equals(args[0]))) {
            try { cloneEventDecorations("--clone-events".equals(args[0])); }
            catch (Exception e) { e.printStackTrace(); System.exit(1); }
            return;
        }
        if (args.length > 0 && "--verify-events".equals(args[0])) {
            try { verifyEventDecorations(); }
            catch (Exception e) { e.printStackTrace(); System.exit(1); }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try { FlatDarculaLaf.setup(); new EventMapEditor().setVisible(true); }
            catch(Exception e){e.printStackTrace();JOptionPane.showMessageDialog(null,"Không mở được editor:\n"+e.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);}
        });
    }

    private record CloneUpdate(int mapId, String mapName, String json, int sourceCount) {}
}
