package server;

import audit.AssetAuditEvent;
import audit.AssetAuditService;
import audit.AssetRestoreService;

import bot.Bot;
import bot.BotManager;
import bot.NewBot;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.google.gson.Gson;
import daos.PlayerDAO;
import daos.ShopDAO;
import data.AlyraManager;
import item.Item;
import managers.GiftCodeManager;
import managers.ConsignShopManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;
import network.AntiDDoSEngine;
import network.BehavioralDetector;
import services.Service;
import services.player.InventoryService;
import services.TaskService;
import services.map.ChangeMapService;
import map.Zone;
import system.GiftCode;
import system.Template.NpcTemplate;
import task.TaskMain;
import mail.entity.MailReward;
import mail.enums.RewardType;
import mail.handler.MailHandler;
import mail.service.MailService;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import utils.Logger;
import utils.Util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerManagerUI extends JFrame {

    private static final int CHIEU_DAI_LOG_TOI_DA = 50000;
    private static final int KHOANG_THOI_GIAN_CAP_NHAT_UI_MS = 3000;

    // --- Bảng màu Modern Dark Mode ---
    private static final Color BG_MAIN = new Color(11, 18, 32);
    private static final Color BG_SIDEBAR = new Color(15, 23, 42);
    private static final Color BG_CARD = new Color(21, 31, 50);
    private static final Color BG_CARD_HEADER = new Color(30, 41, 59);
    private static final Color BG_INPUT = new Color(17, 27, 45);
    private static final Color BG_HOVER = new Color(38, 52, 73);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color ACCENT_BLUE = new Color(56, 189, 248);
    private static final Color ACCENT_BLUE_DARK = new Color(3, 105, 161);
    private static final Color MAU_THANH_CONG = new Color(34, 197, 94);
    private static final Color MAU_CANH_BAO = new Color(251, 191, 36);
    private static final Color MAU_LOI = new Color(248, 113, 113);

    private static final Font FONT_TIEU_DE = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_PHU = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_CHU_THUONG = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_THONG_KE = new Font("Consolas", Font.PLAIN, 11);

    // Bảng Điều Khiển Component
    private JLabel lblSoLuongLuong, lblSoLuongNguoiChoi, lblTrangThai, lblDemNguoc, lblThongTin;
    private JLabel lblThoiGianHoatDong, lblSoLuongVatPhamKyGui, lblThongTinGiftcode, lblSoLuongBot;
    private Speedometer chartCpu;
    private Speedometer chartRam;
    private JPanel chartContainer;

    private JComboBox<Integer> cbGio, cbPhut, cbGiay;
    private JCheckBox chkTuDongKhoiDongLai;
    private JToggleButton btnBatTatTuDongLuu;
    private JToggleButton btnBatTatTuDongDonDepCache;
    private final AtomicBoolean isTuDongLuuBat = new AtomicBoolean(false);
    private ScheduledExecutorService boDieuPhoi;
    private CentralProcessor systemProcessor;
    private GlobalMemory systemMemory;
    private long[] previousSystemCpuTicks;
    private javax.swing.Timer boDemUptime;
    private Process windowsCpuCounterProcess;
    private Thread windowsCpuCounterThread;
    private volatile int windowsCpuUtilityPercent = -1;
    private volatile boolean windowsCpuCounterActive;
    private int missingCpuSamples;
    private final AtomicBoolean baoTriDaDuocLenLich = new AtomicBoolean(false);
    private ScheduledFuture<?> tacVuBaoTriDangHoatDong;
    private ScheduledFuture<?> tacVuDemNguocBaoTriNgayLapTuc;
    public static volatile boolean YEU_CAU_TU_DONG_KHOI_DONG_LAI = false;
    private JTable bangNguoiChoi;
    private DefaultTableModel moHinhBangNguoiChoi;
    private JTextField truongTimKiem;
    private TableRowSorter<DefaultTableModel> boSapXep;
    private final Instant thoiGianKhoiDongServer;

    // --- Anti-DDoS panel UI components ---
    private JTable bangIPBiChan;
    private DefaultTableModel moHinhBangIPBiChan;
    private JTable bangIPReputationTable;
    private DefaultTableModel moHinhIPReputation;
    private JTextArea khuVucLogDdos;
    private JLabel lblDdosMode, lblDdosStats;
    private ScheduledExecutorService boDieuPhoiDdos;
    private final DefaultListModel<String> moHinhDanhSachBot;
    private long lanCapNhatUIcuoi = 0;
    private TrayIcon bieuTuongKhay;
    private final SimpleDateFormat dinhDangThoiGian = new SimpleDateFormat("HH:mm:ss");
    private final java.util.List<JButton> nutDieuHuong = new ArrayList<>();
    private CardLayout boCucNoiDung;
    private JPanel khungNoiDung;
    private JLabel lblTieuDeTrang;

    // ================= CLASS ĐỒNG HỐ TỐC ĐỘ =================
    // ================= CLASS ĐỒNG HỐ TỐC ĐỘ (BẢN MỚI - BÁN NGUYỆT & MƯỢT) =================
    private static class Speedometer extends JPanel {
        private int targetValue = 0;         // Giá trị mục tiêu (0-100)
        private double currentValue = 0.0;   // Giá trị hiện tại đang hiển thị (dùng cho animation)
        private final Color arcColor;
        private final String title;
        private final String unit;
        private String detail = "";
        private final javax.swing.Timer animationTimer;

        public Speedometer(String title, Color arcColor, String unit) {
            this.title = title;
            this.arcColor = arcColor;
            this.unit = unit;
            setBackground(BG_CARD);
            setPreferredSize(new Dimension(200, 160));

            // Timer cập nhật hiệu ứng mượt mà (~60fps)
            animationTimer = new javax.swing.Timer(16, e -> {
                double diff = targetValue - currentValue;
                if (Math.abs(diff) < 0.1) {
                    currentValue = targetValue;
                    // Lấy chính Timer đang chạy từ sự kiện để stop, tránh lỗi final variable
                    ((javax.swing.Timer)e.getSource()).stop(); 
                } else {
                    currentValue += diff * 0.1;
                }
                repaint();
            });
        }

        public void setValue(int val) {
            this.targetValue = Math.max(0, Math.min(100, val));
            if (!animationTimer.isRunning()) {
                animationTimer.start();
            }
        }

        public void setValue(int val, String detail) {
            this.detail = detail == null ? "" : detail;
            setValue(val);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            
            // Tính toán tâm và bán kính cho hình bán nguyệt (đẩy tâm xuống dưới cùng)
            int centerX = w / 2;
            int centerY = h - 40; // Chừa chỗ phía dưới để ghi Text
            int radius = Math.min(centerX - 15, centerY - 20);

            int x = centerX - radius;
            int y = centerY - radius;
            int size = radius * 2;

            // 1. Vẽ nền cung tròn (Góc bắt đầu 180 độ, quét -180 độ theo chiều kim đồng hồ)
            g2.setColor(BG_CARD_HEADER);
            g2.fillArc(x, y, size, size, 180, -180);

            // 2. Vẽ cung giá trị hiện tại (chạy theo currentValue)
            int sweepAngle = (int) (-180 * (currentValue / 100.0));
            g2.setColor(arcColor);
            g2.fillArc(x, y, size, size, 180, sweepAngle);

            // 3. Vẽ viền ngoài cho sắc nét
            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(x, y, size, size, 180, -180);
            g2.drawLine(x, centerY, x + size, centerY); // Đường gạch ngang đáy

            // 4. Tính toán và vẽ kim chỉ tốc độ
            // Từ 180 độ (0%) lùi dần về 0 độ (100%)
            double needleAngleRad = Math.toRadians(180 - (180 * (currentValue / 100.0)));
            int needleLength = radius;
            
            // Công thức lượng giác tính tọa độ đầu kim
            int needleX = centerX + (int) (needleLength * Math.cos(needleAngleRad));
            int needleY = centerY - (int) (needleLength * Math.sin(needleAngleRad)); 

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(centerX, centerY, needleX, needleY);

            // 5. Chấm tròn ở tâm kim
            g2.setColor(Color.WHITE);
            g2.fillOval(centerX - 6, centerY - 6, 12, 12);
            g2.setColor(BG_MAIN);
            g2.fillOval(centerX - 2, centerY - 2, 4, 4);

            // 6. Vẽ Text (Giá trị và Tiêu đề)
            String valueStr = targetValue + unit; // Hiển thị số chốt (target), không hiển thị số đang trôi
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(valueStr);
            g2.setColor(TEXT_PRIMARY);
            g2.drawString(valueStr, centerX - textWidth / 2, centerY + 21);

            if (!detail.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                fm = g2.getFontMetrics();
                g2.setColor(TEXT_SECONDARY);
                g2.drawString(detail, centerX - fm.stringWidth(detail) / 2, centerY + 36);
            }

            g2.setFont(FONT_PHU);
            fm = g2.getFontMetrics();
            textWidth = fm.stringWidth(title);
            g2.setColor(TEXT_SECONDARY);
            // Tiêu đề đặt trên vùng bán nguyệt một chút
            g2.drawString(title, centerX - textWidth / 2, centerY - radius / 2);
        }
    }

    private static class ModernCard extends JPanel {
        public ModernCard(String title) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            if (title != null && !title.isEmpty()) {
                JLabel lblTitle = new JLabel(title.toUpperCase());
                lblTitle.setFont(FONT_PHU);
                lblTitle.setForeground(ACCENT_BLUE);
                lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
                add(lblTitle, BorderLayout.NORTH);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ============================================================================
    // CÁC PHƯƠNG THỨC LOGIC (GIỮ NGUYÊN TỪ BẢN GỐC)
    // ============================================================================
    private void capNhatSuKien(JComboBox<String> cboSuKien) {
        try {
            int chiSoDuocChon = cboSuKien.getSelectedIndex();
            if (chiSoDuocChon < 0) {
                hienThiCanhBao("Vui lòng chọn sự kiện hợp lệ.");
                return;
            }

            if (event.Event.isEventActive()) {
                Logger.logln("[AdminTool] Dừng sự kiện cũ...");
                try {
                    event.Event.getInstance().cleanup();
                } catch (Exception ex) {
                    Logger.logException(getClass(), ex);
                }
            }

            Manager.EVENT_SEVER = chiSoDuocChon;

            if (Manager.EVENT_SEVER == 0) {
                event.Event.switchEvent(0);
                lblThongTin.setText("Đã tắt toàn bộ sự kiện");
                guiThongBao("Admin vừa tắt toàn bộ sự kiện server.");
                Logger.successln("[AdminTool] Đã tắt toàn bộ sự kiện.");
                reloadAllPlayers();
                return;
            }

            event.Event.switchEvent(Manager.EVENT_SEVER);

            if (event.Event.isEventActive()) {
                lblThongTin.setText("Đã kích hoạt sự kiện: " + cboSuKien.getSelectedItem());
                guiThongBao("Admin vừa bật sự kiện: " + cboSuKien.getSelectedItem());
                Logger.successln("[AdminTool] Đã kích hoạt sự kiện ID " + Manager.EVENT_SEVER);
                reloadAllPlayers();
            } else {
                lblThongTin.setText("Không thể khởi tạo sự kiện ID: " + Manager.EVENT_SEVER);
                Logger.errorln("[AdminTool] Không thể khởi tạo sự kiện mới.");
            }
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi cập nhật sự kiện: " + ex.getMessage());
            Logger.logException(getClass(), ex);
        }
    }

    private void reloadAllPlayers() {
        try {
            if (Client.gI() == null) return;

            List<Player> allPlayers = Client.gI().getPlayers();
            if (allPlayers == null || allPlayers.isEmpty()) return;

            for (Player player : allPlayers) {
                try {
                    if (player == null || player.getSession() == null) continue;
                    Service.gI().player(player);
                    Service.gI().Send_Caitrang(player);
                    Service.gI().sendFlagBag(player);
                    if (player.zone != null && player.location != null) {
                        Zone zone = player.zone;
                        ChangeMapService.gI().changeMap(player, zone, player.location.x, player.location.y);
                    }
                } catch (Exception ex) {
                    Logger.error("[AdminTool] Lỗi khi reload player " + (player != null ? player.name : "null") + ": " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Logger.error("[AdminTool] Lỗi khi reload tất cả players: " + ex.getMessage());
        }
    }

    private void hienThiLoi(String thongDiep) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, thongDiep, "Lỗi", JOptionPane.ERROR_MESSAGE));
    }

    private void hienThiCanhBao(String thongDiep) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, thongDiep, "Cảnh báo", JOptionPane.WARNING_MESSAGE));
    }

    private void guiThongBao(String thongDiep) {
        if (Client.gI() != null) {
            Client.gI().getPlayers().forEach(p -> Service.gI().sendThongBao(p, thongDiep));
        }
    }

    private static class BoDonDepCacheTuDong {
        private static final BoDonDepCacheTuDong THE_HIEN = new BoDonDepCacheTuDong();
        static BoDonDepCacheTuDong getInstance() { return THE_HIEN; }
        void setEnabled(boolean bat) {}
    }

    public ServerManagerUI() {
        super("Server Control Panel");
        this.moHinhDanhSachBot = new DefaultListModel<>();
        this.thoiGianKhoiDongServer = Instant.now();
        thietLapGiaoDien();
        khoiTaoUI();
        khoiTaoLogic();
        khoiTaoHeThongAntiDdos();
        khoiDongCacTienTrinhServer();
    }

    private void thietLapGiaoDien() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
            UIManager.put("TabbedPane.background", BG_MAIN);
            UIManager.put("TabbedPane.selectedBackground", BG_CARD);
            UIManager.put("TabbedPane.underlineColor", ACCENT_BLUE);
            UIManager.put("TabbedPane.inactiveUnderlineColor", BORDER_COLOR);
            UIManager.put("TabbedPane.tabHeight", 38);
            UIManager.put("Panel.background", BG_MAIN);
            UIManager.put("OptionPane.background", BG_CARD);
            UIManager.put("Label.foreground", TEXT_PRIMARY);
            UIManager.put("Button.background", BG_CARD_HEADER);
            UIManager.put("Button.foreground", TEXT_PRIMARY);
            UIManager.put("Button.hoverBackground", BG_HOVER);
            UIManager.put("Button.pressedBackground", ACCENT_BLUE_DARK);
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("Component.focusColor", ACCENT_BLUE);
            UIManager.put("Component.borderColor", BORDER_COLOR);
            UIManager.put("TextField.background", BG_INPUT);
            UIManager.put("TextField.foreground", TEXT_PRIMARY);
            UIManager.put("TextField.caretForeground", ACCENT_BLUE);
            UIManager.put("ComboBox.background", BG_INPUT);
            UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
            UIManager.put("Spinner.background", BG_INPUT);
            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.foreground", TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", ACCENT_BLUE_DARK);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("TableHeader.background", BG_CARD_HEADER);
            UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
            UIManager.put("Table.rowHeight", 32);
            UIManager.put("ScrollBar.width", 11);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("defaultFont", FONT_CHU_THUONG);
        } catch (UnsupportedLookAndFeelException e) {
            Logger.error("Lỗi cài đặt Look and Feel: " + e.getMessage());
        }
    }

    private void khoiTaoUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);

        boCucNoiDung = new CardLayout();
        khungNoiDung = new JPanel(boCucNoiDung);
        khungNoiDung.setBackground(BG_MAIN);
        khungNoiDung.add(taoBangDieuKhienChinh(), "SYSTEM");
        khungNoiDung.add(taoBangQuanLyNguoiChoi(), "PLAYERS");
        khungNoiDung.add(new RechargeAdminPanel(this), "RECHARGE");
        khungNoiDung.add(taoBangChongDdos(), "DDOS");
        khungNoiDung.add(taoBangBot(), "BOTS");
        khungNoiDung.add(taoBangQuanLyBoss(), "BOSS_DROPS");
        khungNoiDung.add(taoBangDragonPass(), "DRAGON_PASS");
        khungNoiDung.add(taoBangVongQuayMayMan(), "LUCKY_WHEEL");
        khungNoiDung.add(taoBangTop(), "TOP");

        JPanel sidebar = taoThanhDieuHuong();
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG_MAIN);
        center.add(taoThanhTieuDe(), BorderLayout.NORTH);
        center.add(khungNoiDung, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
        chuyenTrang("SYSTEM", "Tổng quan hệ thống", nutDieuHuong.get(0));
        apDungGiaoDienHienDai(root);
        try {
            String iconPath = "/icons/server-icon.png";
            Image icon = Toolkit.getDefaultToolkit().createImage(getClass().getResource(iconPath));
            setIconImage(icon);
        } catch (Exception e) {
            // fallback: nếu không tìm thấy, dùng icon mặc định
            try {
                setIconImage(new ImageIcon(getClass().getResource("/icons/server-icon.png")).getImage());
            } catch (Exception ex) {}
        }
        setTitle("NRO Server Manager");
        setSize(new Dimension(1280, 800));
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                anVaoKhayHeThong();
            }
        });
        thietLapKhayHeThong();
    }

    private JPanel taoThanhDieuHuong() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(24, 20, 20, 16));
        JLabel title = new JLabel("NRO MANAGER");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(TEXT_PRIMARY);
        JLabel subtitle = new JLabel("SERVER CONTROL PANEL");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitle.setForeground(ACCENT_BLUE);
        brand.add(title, BorderLayout.NORTH);
        brand.add(subtitle, BorderLayout.SOUTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(8, 10, 8, 10));
        nav.add(taoNutDieuHuong("Tổng quan", "SYSTEM", "Tổng quan hệ thống"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Người chơi", "PLAYERS", "Quản lý người chơi"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Nạp tiền", "RECHARGE", "Quản lý nạp thủ công"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Chống DDoS", "DDOS", "Giám sát và phòng thủ"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Quản lý Bot", "BOTS", "Quản lý Bot"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Quản Lý Boss", "BOSS_DROPS", "Quản lý đồ rơi Boss"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Dragon Pass", "DRAGON_PASS", "Cấu hình Dragon Pass"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Vòng quay", "LUCKY_WHEEL", "Cấu hình Vòng quay may mắn"));
        nav.add(Box.createVerticalStrut(6));
        nav.add(taoNutDieuHuong("Top", "TOP", "Cấu hình bảng xếp hạng và phần thưởng"));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 18, 20, 18));
        JLabel state = new JLabel("●  SERVER ĐANG CHẠY");
        state.setForeground(MAU_THANH_CONG);
        state.setFont(new Font("Segoe UI", Font.BOLD, 11));
        footer.add(state, BorderLayout.CENTER);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel taoThanhTieuDe() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_MAIN);
        header.setBorder(new EmptyBorder(20, 22, 8, 22));
        lblTieuDeTrang = new JLabel("Tổng quan hệ thống");
        lblTieuDeTrang.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTieuDeTrang.setForeground(TEXT_PRIMARY);
        JLabel hint = new JLabel("Theo dõi và vận hành máy chủ theo thời gian thực");
        hint.setForeground(TEXT_SECONDARY);
        hint.setFont(FONT_CHU_THUONG);
        header.add(lblTieuDeTrang, BorderLayout.NORTH);
        header.add(hint, BorderLayout.SOUTH);
        return header;
    }

    private JButton taoNutDieuHuong(String text, String key, String pageTitle) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(TEXT_SECONDARY);
        button.setBackground(BG_SIDEBAR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(196, 44));
        button.setBorder(new EmptyBorder(0, 16, 0, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> chuyenTrang(key, pageTitle, button));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!button.getBackground().equals(ACCENT_BLUE_DARK)) button.setBackground(BG_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!button.getBackground().equals(ACCENT_BLUE_DARK)) button.setBackground(BG_SIDEBAR);
            }
        });
        nutDieuHuong.add(button);
        return button;
    }

    private void chuyenTrang(String key, String title, JButton activeButton) {
        boCucNoiDung.show(khungNoiDung, key);
        if (lblTieuDeTrang != null) {
            lblTieuDeTrang.setText(title);
        }
        for (JButton button : nutDieuHuong) {
            boolean active = button == activeButton;
            button.setBackground(active ? ACCENT_BLUE_DARK : BG_SIDEBAR);
            button.setForeground(active ? Color.WHITE : TEXT_SECONDARY);
        }
    }

    private void apDungGiaoDienHienDai(Component component) {
        if (component instanceof JTable table) {
            table.setBackground(BG_CARD);
            table.setForeground(TEXT_PRIMARY);
            table.setSelectionBackground(ACCENT_BLUE_DARK);
            table.setSelectionForeground(Color.WHITE);
            table.setGridColor(BORDER_COLOR);
            table.setShowHorizontalLines(true);
            table.setShowVerticalLines(false);
            table.setIntercellSpacing(new Dimension(0, 1));
            table.setRowHeight(Math.max(32, table.getRowHeight()));
            table.setFont(FONT_CHU_THUONG);
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                header.setBackground(BG_CARD_HEADER);
                header.setForeground(TEXT_PRIMARY);
                header.setFont(FONT_PHU);
                header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
            }
        } else if (component instanceof JTextField field) {
            field.setBackground(BG_INPUT);
            field.setForeground(TEXT_PRIMARY);
            field.setCaretColor(ACCENT_BLUE);
            field.setSelectionColor(ACCENT_BLUE_DARK);
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(7, 10, 7, 10)));
        } else if (component instanceof JTextArea area) {
            area.setBackground(BG_INPUT);
            area.setForeground(TEXT_PRIMARY);
            area.setCaretColor(ACCENT_BLUE);
            area.setBorder(new EmptyBorder(10, 12, 10, 12));
        } else if (component instanceof JButton button && !nutDieuHuong.contains(button)) {
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.putClientProperty("JButton.buttonType", "roundRect");
            if (button.getFont() == null || !button.getFont().isBold()) button.setFont(FONT_PHU);
            if (!Boolean.TRUE.equals(button.getClientProperty("compactItemCell"))) {
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(7, 13, 7, 13)));
            }
        } else if (component instanceof JToggleButton toggle) {
            toggle.setFocusPainted(false);
            toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            toggle.putClientProperty("JButton.buttonType", "roundRect");
            toggle.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(7, 13, 7, 13)));
        } else if (component instanceof JComboBox<?> combo) {
            combo.setBackground(BG_INPUT);
            combo.setForeground(TEXT_PRIMARY);
            combo.setMinimumSize(new Dimension(90, 32));
        } else if (component instanceof JSpinner spinner) {
            spinner.setBackground(BG_INPUT);
            spinner.setForeground(TEXT_PRIMARY);
        } else if (component instanceof JList<?> list) {
            list.setBackground(BG_CARD);
            list.setForeground(TEXT_PRIMARY);
            list.setSelectionBackground(ACCENT_BLUE_DARK);
            list.setSelectionForeground(Color.WHITE);
            list.setFixedCellHeight(32);
            list.setBorder(new EmptyBorder(6, 6, 6, 6));
        } else if (component instanceof JScrollPane scroll) {
            scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            scroll.getViewport().setBackground(BG_CARD);
        } else if (component instanceof JTabbedPane tabs) {
            tabs.setBackground(BG_MAIN);
            tabs.setForeground(TEXT_PRIMARY);
            tabs.setFont(FONT_PHU);
            tabs.setBorder(new EmptyBorder(4, 0, 0, 0));
        } else if (component instanceof JSplitPane split) {
            split.setBackground(BG_MAIN);
            split.setDividerSize(8);
            split.setBorder(BorderFactory.createEmptyBorder());
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                apDungGiaoDienHienDai(child);
            }
        }
    }

    private void thietLapKhayHeThong() {
        if (!SystemTray.isSupported()) return;

        SystemTray khayHeThong = SystemTray.getSystemTray();
        BufferedImage hinhAnh = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        Graphics g = hinhAnh.getGraphics();
        g.setColor(ACCENT_BLUE);
        g.fillRect(0, 0, 16, 16);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("S", 4, 13);
        g.dispose();

        PopupMenu menuPopup = new PopupMenu();
        MenuItem mucMo = new MenuItem("Mở Bảng Điều Khiển");
        mucMo.addActionListener(e -> hienThiTuKhayHeThong());
        MenuItem mucThoat = new MenuItem("Thoát Server");
        mucThoat.addActionListener(e -> xacNhanThoatTuKhayHeThong());

        menuPopup.add(mucMo);
        menuPopup.addSeparator();
        menuPopup.add(mucThoat);

        bieuTuongKhay = new TrayIcon(hinhAnh, "Quản Lý Server", menuPopup);
        bieuTuongKhay.setImageAutoSize(true);
        bieuTuongKhay.addActionListener(e -> hienThiTuKhayHeThong());

        try {
            khayHeThong.add(bieuTuongKhay);
        } catch (AWTException e) {
            Logger.error("Không thể thêm biểu tượng vào khay hệ thống.");
        }
    }

    private void anVaoKhayHeThong() {
        if (bieuTuongKhay != null) {
            setVisible(false);
            bieuTuongKhay.displayMessage("Server Manager", "Server đang chạy nền.", TrayIcon.MessageType.INFO);
        }
    }

    private void hienThiTuKhayHeThong() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
        toFront();
        requestFocus();
    }

    private void xacNhanThoatTuKhayHeThong() {
        hienThiTuKhayHeThong();
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn thoát? Server sẽ tắt.", "Xác nhận thoát", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            tatServerVaKhayHeThong();
        } else {
            anVaoKhayHeThong();
        }
    }

    private void tatServerVaKhayHeThong() {
        if (bieuTuongKhay != null) SystemTray.getSystemTray().remove(bieuTuongKhay);
        tatServer();
    }

    // ============================================================================
    // PANEL HỆ THỐNG (GIAO DIỆN MỚI)
    // ============================================================================
    private JPanel taoBangDragonPass() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        List<DragonPassItemChoice> itemChoices = loadDragonPassItems();
        List<DragonPassOptionChoice> optionChoices = loadDragonPassOptions();
        Map<Integer, DragonPassItemChoice> itemsById = new HashMap<>();
        for (DragonPassItemChoice item : itemChoices) itemsById.put(item.id, item);
        JPanel top = new JPanel(); top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel eventBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        JPanel configBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        JComboBox<dragonpass.DragonPassModels.Season> seasons = new JComboBox<>();
        seasons.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof dragonpass.DragonPassModels.Season s) setText(s.key + " - " + dragonPassStatusText(s.status));
                return this;
            }
        });
        JButton reload = new JButton("Tải lại");
        JButton save = new JButton("Lưu cấu hình");
        AtomicBoolean dragonPassSaveOk=new AtomicBoolean(false);
        JButton activate = new JButton("Kích hoạt");
        JButton summary = new JButton("Vào tổng kết");
        JButton finish = new JButton("Kết thúc & trao thưởng");
        JLabel status = new JLabel("●");
        JSpinner days=new JSpinner(new SpinnerNumberModel(30,0,3650,1));
        JSpinner hours=new JSpinner(new SpinnerNumberModel(0,0,23,1));
        JSpinner minutes=new JSpinner(new SpinnerNumberModel(0,0,59,1));
        JSpinner normalPrice=new JSpinner(new SpinnerNumberModel(360,1,30000,1));
        JSpinner plusPrice=new JSpinner(new SpinnerNumberModel(720,2,30000,1));
        eventBar.add(new JLabel("Đợt:"));eventBar.add(seasons);eventBar.add(reload);eventBar.add(status);
        eventBar.add(activate);eventBar.add(summary);eventBar.add(finish);
        configBar.add(new JLabel("Thời lượng:"));configBar.add(days);configBar.add(new JLabel("ngày"));configBar.add(hours);configBar.add(new JLabel("giờ"));configBar.add(minutes);configBar.add(new JLabel("phút"));
        configBar.add(new JLabel("Giá thường:"));configBar.add(normalPrice);configBar.add(new JLabel("Giá Plus:"));configBar.add(plusPrice);
        configBar.add(save);
        top.add(eventBar);
        top.add(configBar);
        panel.add(top, BorderLayout.NORTH);

        String[] columns = {"Cấp", "Quà thường", "SL", "Options", "Quà Plus 1", "SL", "Options",
                "Quà Plus 2", "SL", "Options"};
        AtomicBoolean dragonPassRewardsEditable=new AtomicBoolean(false);
        DefaultTableModel model = new DefaultTableModel(columns, 100) {
            @Override public boolean isCellEditable(int row, int column) { return dragonPassRewardsEditable.get() && column != 0; }
        };
        for (int i = 0; i < 100; i++) model.setValueAt(i + 1, i, 0);
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(42);
        int[] widths = {45, 220, 55, 180, 220, 55, 180, 220, 55, 180};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        DragonPassItemEditor itemEditor = new DragonPassItemEditor(this, itemChoices);
        DragonPassOptionsEditor optionsEditor = new DragonPassOptionsEditor(this, optionChoices);
        for (int column : new int[]{1, 4, 7}) {
            table.getColumnModel().getColumn(column).setCellEditor(itemEditor);
            table.getColumnModel().getColumn(column).setCellRenderer(new DragonPassItemRenderer());
        }
        for (int column : new int[]{3, 6, 9}) table.getColumnModel().getColumn(column).setCellEditor(optionsEditor);
        JPanel rewardActions=new JPanel(new FlowLayout(FlowLayout.LEFT,6,2));
        JButton copyRow=new JButton("Sao chép hàng");JButton pasteRow=new JButton("Dán vào cấp chọn");
        JButton pasteRange=new JButton("Dán vào khoảng...");JButton pasteTargets=new JButton("Dán vào các cấp...");JButton clearRow=new JButton("Xóa cấp chọn");
        rewardActions.add(copyRow);rewardActions.add(pasteRow);rewardActions.add(pasteRange);rewardActions.add(pasteTargets);rewardActions.add(clearRow);
        top.add(rewardActions);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        final Object[][] rowClipboard={null};

        Runnable loadSeasons = () -> {
            try {
                long selectedId = seasons.getSelectedItem() instanceof dragonpass.DragonPassModels.Season s ? s.id : -1;
                seasons.removeAllItems();
                for (dragonpass.DragonPassModels.Season season : dragonpass.DragonPassRepository.listSeasons()) seasons.addItem(season);
                for (int i = 0; i < seasons.getItemCount(); i++) if (seasons.getItemAt(i).id == selectedId) seasons.setSelectedIndex(i);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Dragon Pass", JOptionPane.ERROR_MESSAGE);
            }
        };
        Runnable loadRewards = () -> {
            dragonpass.DragonPassModels.Season season = (dragonpass.DragonPassModels.Season) seasons.getSelectedItem();
            if (season == null) return;
            for (int row = 0; row < 100; row++) {
                for (int col = 1; col < columns.length; col++) {
                    model.setValueAt(col == 3 || col == 6 || col == 9 ? new DragonPassOptionsCell() : null, row, col);
                }
            }
            try {
                for (dragonpass.DragonPassModels.Reward reward : dragonpass.DragonPassRepository.getRewards(season.id)) {
                    int base = reward.track == dragonpass.DragonPassConstants.TRACK_NORMAL ? 1 : (reward.slot == 1 ? 4 : 7);
                    int row = reward.level - 1;
                    if (row >= 0 && row < 100) {
                        DragonPassItemChoice item = reward.itemTemplateId > 0
                                ? itemsById.get(reward.itemTemplateId) : null;
                        if (item == null && reward.itemTemplateId > 0) {
                            item = new DragonPassItemChoice(reward.itemTemplateId, "Vật phẩm không còn trong item_template");
                        }
                        model.setValueAt(item, row, base);
                        model.setValueAt(reward.quantity > 0 ? reward.quantity : null, row, base + 1);
                        model.setValueAt(DragonPassOptionsCell.fromJson(reward.optionsData, optionChoices), row, base + 2);
                    }
                }
                boolean editable=season.editable();
                days.setValue(season.durationDays);hours.setValue(season.durationHours);minutes.setValue(season.durationMinutes);
                normalPrice.setValue(season.normalPrice);plusPrice.setValue(season.plusPrice);status.setText("● "+dragonPassStatusText(season.status));
                dragonPassRewardsEditable.set(editable);
                for(Component c:new Component[]{days,hours,minutes,normalPrice,plusPrice,save,pasteRow,pasteRange,pasteTargets,clearRow})c.setEnabled(editable);
                copyRow.setEnabled(true);table.setEnabled(true);activate.setEnabled(editable);summary.setEnabled(season.status==dragonpass.DragonPassConstants.STATUS_ACTIVE);
                finish.setEnabled(season.status==dragonpass.DragonPassConstants.STATUS_ACTIVE||season.status==dragonpass.DragonPassConstants.STATUS_SUMMARY);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Dragon Pass", JOptionPane.ERROR_MESSAGE);
            }
        };
        reload.addActionListener(e -> { seasons.setSelectedItem(null); loadSeasons.run(); loadRewards.run(); });
        seasons.addActionListener(e -> loadRewards.run());
        save.addActionListener(e -> {
            dragonPassSaveOk.set(false);
            dragonpass.DragonPassModels.Season season = (dragonpass.DragonPassModels.Season) seasons.getSelectedItem();
            if (season == null) return;
            try {
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
                season.durationDays=(Integer)days.getValue();season.durationHours=(Integer)hours.getValue();season.durationMinutes=(Integer)minutes.getValue();
                season.normalPrice=(Integer)normalPrice.getValue();season.plusPrice=(Integer)plusPrice.getValue();
                List<dragonpass.DragonPassModels.Reward> rewards=new ArrayList<>();
                for (int row = 0; row < 100; row++) {
                    dragonpass.DragonPassModels.Reward reward=dragonPassRewardFromRow(model,row,1,dragonpass.DragonPassConstants.TRACK_NORMAL,(byte)1);if(reward!=null)rewards.add(reward);
                    reward=dragonPassRewardFromRow(model,row,4,dragonpass.DragonPassConstants.TRACK_PLUS,(byte)1);if(reward!=null)rewards.add(reward);
                    reward=dragonPassRewardFromRow(model,row,7,dragonpass.DragonPassConstants.TRACK_PLUS,(byte)2);if(reward!=null)rewards.add(reward);
                }
                dragonpass.DragonPassRepository.saveEventConfiguration(season,rewards);
                dragonPassSaveOk.set(true);
                JOptionPane.showMessageDialog(this, "Đã lưu cấu hình " + season.key);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Dragon Pass", JOptionPane.ERROR_MESSAGE);
            }
        });
        copyRow.addActionListener(e->{int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Hãy chọn cấp cần sao chép.");return;}rowClipboard[0]=copyDragonPassRow(model,row);});
        pasteRow.addActionListener(e->{int row=table.getSelectedRow();if(row<0||rowClipboard[0]==null){JOptionPane.showMessageDialog(this,"Chưa chọn cấp hoặc chưa sao chép.");return;}pasteDragonPassRow(model,row,rowClipboard[0]);});
        pasteRange.addActionListener(e->{if(rowClipboard[0]==null){JOptionPane.showMessageDialog(this,"Hãy sao chép một hàng trước.");return;}String value=JOptionPane.showInputDialog(this,"Nhập khoảng cấp, ví dụ 11-20:");if(value==null)return;try{String[] p=value.trim().split("-");int from=Integer.parseInt(p[0].trim()),to=Integer.parseInt(p[1].trim());if(from<1||to>100||from>to)throw new Exception();for(int rank=from;rank<=to;rank++)pasteDragonPassRow(model,rank-1,rowClipboard[0]);}catch(Exception ex){JOptionPane.showMessageDialog(this,"Khoảng cấp không hợp lệ.");}});
        pasteTargets.addActionListener(e->{if(rowClipboard[0]==null){JOptionPane.showMessageDialog(this,"Hãy sao chép một hàng trước.");return;}String value=JOptionPane.showInputDialog(this,"Nhập các cấp, ví dụ 5,10,15,20:");if(value==null)return;try{for(String part:value.split(",")){int rank=Integer.parseInt(part.trim());if(rank<1||rank>100)throw new Exception();pasteDragonPassRow(model,rank-1,rowClipboard[0]);}}catch(Exception ex){JOptionPane.showMessageDialog(this,"Danh sách cấp không hợp lệ.");}});
        clearRow.addActionListener(e->{int row=table.getSelectedRow();if(row>=0)for(int col=1;col<10;col++)model.setValueAt(col==3||col==6||col==9?new DragonPassOptionsCell():null,row,col);});
        activate.addActionListener(e->{dragonpass.DragonPassModels.Season season=(dragonpass.DragonPassModels.Season)seasons.getSelectedItem();if(season==null)return;try{save.doClick();if(!dragonPassSaveOk.get())return;dragonpass.DragonPassService.gI().activate(dragonpass.DragonPassRepository.getEvent(season.id));loadSeasons.run();loadRewards.run();JOptionPane.showMessageDialog(this,"Dragon Pass đã được kích hoạt.");}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Dragon Pass",JOptionPane.ERROR_MESSAGE);}});
        summary.addActionListener(e->{if(JOptionPane.showConfirmDialog(this,"Chuyển Dragon Pass sang tổng kết? Nhiệm vụ sẽ bị khóa.","Dragon Pass",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;try{dragonpass.DragonPassService.gI().enterSummary();loadSeasons.run();loadRewards.run();}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Dragon Pass",JOptionPane.ERROR_MESSAGE);}});
        finish.addActionListener(e->{if(JOptionPane.showConfirmDialog(this,"Kết thúc ngay và gửi toàn bộ quà chưa nhận qua thư 30 ngày?","Dragon Pass",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION)return;finish.setEnabled(false);new SwingWorker<Void,Void>(){protected Void doInBackground()throws Exception{dragonpass.DragonPassService.gI().finishNow();return null;}protected void done(){try{get();seasons.setSelectedItem(null);loadSeasons.run();loadRewards.run();JOptionPane.showMessageDialog(ServerManagerUI.this,"Đã kết thúc và trao thưởng Dragon Pass.");}catch(Exception ex){JOptionPane.showMessageDialog(ServerManagerUI.this,ex.getCause()==null?ex.getMessage():ex.getCause().getMessage(),"Dragon Pass",JOptionPane.ERROR_MESSAGE);}}}.execute();});
        loadSeasons.run();
        loadRewards.run();
        return panel;
    }

    private String dragonPassStatusText(byte status){return switch(status){case 0->"BẢN NHÁP";case 1->"ĐANG HOẠT ĐỘNG";case 2->"TỔNG KẾT";case 3->"ĐANG TRAO THƯỞNG";default->"ĐÃ KẾT THÚC";};}
    private Object[] copyDragonPassRow(DefaultTableModel model,int row){Object[] result=new Object[9];for(int col=1;col<10;col++){Object value=model.getValueAt(row,col);result[col-1]=value instanceof DragonPassOptionsCell cell?cell.copy():value;}return result;}
    private void pasteDragonPassRow(DefaultTableModel model,int row,Object[] values){for(int col=1;col<10;col++){Object value=values[col-1];model.setValueAt(value instanceof DragonPassOptionsCell cell?cell.copy():value,row,col);}}

    private JPanel taoBangVongQuayMayMan() {
        return new LuckyWheelAdminPanel(this);
    }

    private JPanel taoBangQuanLyBoss() {
        return new BossDropAdminPanel(this);
    }

    private JPanel taoBangTop() {
        return new TopAdminPanel(this);
    }

    private dragonpass.DragonPassModels.Reward dragonPassRewardFromRow(DefaultTableModel model,int row,int base,
            byte track,byte slot) throws Exception {
        Object idValue = model.getValueAt(row, base);
        Object quantityValue = model.getValueAt(row, base + 1);
        Object optionValue = model.getValueAt(row, base + 2);
        String quantityText = quantityValue == null ? "" : quantityValue.toString().trim();
        Integer itemId = idValue instanceof DragonPassItemChoice item ? item.id : null;
        Integer quantity = quantityText.isEmpty() ? null : Integer.valueOf(quantityText);
        String options = optionValue instanceof DragonPassOptionsCell cell ? cell.toJson() : "[]";
        if (itemId != null && (quantity == null || quantity <= 0)) {
            throw new IllegalArgumentException("Số lượng quà phải lớn hơn 0 ở cấp " + (row + 1));
        }
        if(itemId==null)return null;
        dragonpass.DragonPassModels.Reward reward=new dragonpass.DragonPassModels.Reward();
        reward.level=row+1;reward.track=track;reward.slot=slot;reward.itemTemplateId=itemId;reward.quantity=quantity;reward.optionsData=options;
        return reward;
    }

    private List<DragonPassItemChoice> loadDragonPassItems() {
        List<DragonPassItemChoice> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT id, name, icon_id FROM item_template ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt(1);
                if (id > 0) result.add(new DragonPassItemChoice(id, rs.getString(2), rs.getInt(3)));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được danh sách item: " + ex.getMessage(),
                    "Dragon Pass", JOptionPane.ERROR_MESSAGE);
        }
        return result;
    }

    private DragonPassItemChoice chonVatPhamCoIcon(Component parent, List<DragonPassItemChoice> choices) {
        DefaultListModel<DragonPassItemChoice> model = new DefaultListModel<>();
        for (DragonPassItemChoice choice : choices) model.addElement(choice);
        JList<DragonPassItemChoice> list = new JList<>(model);
        list.setFixedCellHeight(40);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                JLabel label = (JLabel) super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof DragonPassItemChoice item) label.setIcon(TopAdminPanel.IconCache.get(item.iconId));
                return label;
            }
        });
        JTextField search = new JTextField();
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() { String q=search.getText().trim().toLowerCase(Locale.ROOT);model.clear();for(DragonPassItemChoice c:choices)if(q.isEmpty()||c.toString().toLowerCase(Locale.ROOT).contains(q))model.addElement(c); }
            @Override public void insertUpdate(DocumentEvent e){filter();}@Override public void removeUpdate(DocumentEvent e){filter();}@Override public void changedUpdate(DocumentEvent e){filter();}
        });
        JPanel content=new JPanel(new BorderLayout(5,5));content.setPreferredSize(new Dimension(500,450));content.add(search,BorderLayout.NORTH);content.add(new JScrollPane(list),BorderLayout.CENTER);
        return JOptionPane.showConfirmDialog(parent,content,"Chọn vật phẩm",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION?list.getSelectedValue():null;
    }

    private List<DragonPassOptionChoice> loadDragonPassOptions() {
        List<DragonPassOptionChoice> result = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT id, name FROM item_option_template ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new DragonPassOptionChoice(rs.getInt(1), rs.getString(2)));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tải được danh sách option: " + ex.getMessage(),
                    "Dragon Pass", JOptionPane.ERROR_MESSAGE);
        }
        return result;
    }

    private static final class DragonPassItemChoice {
        final int id;
        final String name;
        final int iconId;

        DragonPassItemChoice(int id, String name) { this(id, name, 0); }

        DragonPassItemChoice(int id, String name, int iconId) {
            this.id = id;
            this.iconId = iconId;
            this.name = name == null || name.isBlank() ? "(không có tên)" : name;
        }

        @Override public String toString() { return id + " - " + name; }
    }

    private static final class DragonPassOptionChoice {
        final int id;
        final String name;

        DragonPassOptionChoice(int id, String name) {
            this.id = id;
            this.name = name == null || name.isBlank() ? "(không có tên)" : name;
        }

        @Override public String toString() { return id + " - " + name; }
    }

    private static final class DragonPassOptionValue {
        DragonPassOptionChoice option;
        int param;

        DragonPassOptionValue(DragonPassOptionChoice option, int param) {
            this.option = option;
            this.param = param;
        }
    }

    private static final class DragonPassOptionsCell {
        final List<DragonPassOptionValue> values = new ArrayList<>();

        DragonPassOptionsCell copy() {
            DragonPassOptionsCell copy = new DragonPassOptionsCell();
            for (DragonPassOptionValue value : values) copy.values.add(new DragonPassOptionValue(value.option, value.param));
            return copy;
        }

        static DragonPassOptionsCell fromJson(String json, List<DragonPassOptionChoice> choices) {
            DragonPassOptionsCell cell = new DragonPassOptionsCell();
            if (json == null || json.isBlank()) return cell;
            Map<Integer, DragonPassOptionChoice> byId = new HashMap<>();
            for (DragonPassOptionChoice choice : choices) byId.put(choice.id, choice);
            Object parsed = JSONValue.parse(json);
            if (!(parsed instanceof JSONArray array)) return cell;
            for (Object value : array) {
                if (!(value instanceof JSONObject object)) continue;
                Object idValue = object.get("id");
                Object paramValue = object.get("param");
                if (!(idValue instanceof Number) || !(paramValue instanceof Number)) continue;
                int id = ((Number) idValue).intValue();
                DragonPassOptionChoice choice = byId.get(id);
                if (choice == null) choice = new DragonPassOptionChoice(id, "Option không còn tồn tại");
                cell.values.add(new DragonPassOptionValue(choice, ((Number) paramValue).intValue()));
            }
            return cell;
        }

        @SuppressWarnings("unchecked")
        String toJson() {
            JSONArray array = new JSONArray();
            for (DragonPassOptionValue value : values) {
                if (value.option == null) continue;
                JSONObject object = new JSONObject();
                object.put("id", value.option.id);
                object.put("param", value.param);
                array.add(object);
            }
            return array.toJSONString();
        }

        @Override public String toString() {
            if (values.isEmpty()) return "";
            StringBuilder text = new StringBuilder();
            for (DragonPassOptionValue value : values) {
                if (text.length() > 0) text.append("; ");
                text.append(value.option == null ? "?" : value.option.id).append(": ").append(value.param);
            }
            return text.toString();
        }
    }

    private static final class DragonPassItemEditor extends AbstractCellEditor implements TableCellEditor {
        private final Component parent;
        private final List<DragonPassItemChoice> choices;
        private final JButton button = new JButton();
        private DragonPassItemChoice value;

        DragonPassItemEditor(Component parent, List<DragonPassItemChoice> choices) {
            this.parent = parent;
            this.choices = choices;
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.addActionListener(e -> {
                DragonPassItemChoice selected = showItemSelector();
                if (selected != value || selected == null) value = selected;
                fireEditingStopped();
            });
        }

        @Override public Object getCellEditorValue() { return value; }

        @Override public Component getTableCellEditorComponent(JTable table, Object cellValue, boolean selected,
                int row, int column) {
            value = cellValue instanceof DragonPassItemChoice item ? item : null;
            button.setText(value == null ? "Chọn vật phẩm..." : value.toString());
            button.setIcon(value == null ? null : TopAdminPanel.IconCache.get(value.iconId));
            return button;
        }

        private DragonPassItemChoice showItemSelector() {
            DefaultListModel<DragonPassItemChoice> model = new DefaultListModel<>();
            JList<DragonPassItemChoice> list = new JList<>(model);
            list.setCellRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i,
                        boolean selected, boolean focus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(l, v, i, selected, focus);
                    if (v instanceof DragonPassItemChoice item) label.setIcon(TopAdminPanel.IconCache.get(item.iconId));
                    return label;
                }
            });
            list.setFixedCellHeight(40);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JTextField search = new JTextField();
            JButton clear = new JButton("Bỏ chọn vật phẩm");
            final boolean[] clearRequested = {false};
            Runnable filter = () -> {
                String keyword = search.getText().trim().toLowerCase(Locale.ROOT);
                model.clear();
                for (DragonPassItemChoice choice : choices) {
                    if (keyword.isEmpty() || choice.toString().toLowerCase(Locale.ROOT).contains(keyword)) model.addElement(choice);
                }
                if (value != null && keyword.isEmpty()) list.setSelectedValue(value, true);
            };
            search.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { filter.run(); }
                @Override public void removeUpdate(DocumentEvent e) { filter.run(); }
                @Override public void changedUpdate(DocumentEvent e) { filter.run(); }
            });
            clear.addActionListener(e -> { clearRequested[0] = true; list.clearSelection(); });
            list.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && list.getSelectedValue() != null) clearRequested[0] = false;
            });
            filter.run();
            JPanel content = new JPanel(new BorderLayout(5, 5));
            content.setPreferredSize(new Dimension(520, 430));
            content.add(new JLabel("Tìm theo ID hoặc tên vật phẩm:"), BorderLayout.NORTH);
            JPanel center = new JPanel(new BorderLayout(5, 5));
            center.add(search, BorderLayout.NORTH);
            center.add(new JScrollPane(list), BorderLayout.CENTER);
            center.add(clear, BorderLayout.SOUTH);
            content.add(center, BorderLayout.CENTER);
            int answer = JOptionPane.showConfirmDialog(parent, content, "Chọn vật phẩm",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return value;
            return clearRequested[0] ? null : (list.getSelectedValue() == null ? value : list.getSelectedValue());
        }
    }

    private static final class DragonPassItemRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            if (value instanceof DragonPassItemChoice item) {
                label.setText(item.name);
                label.setIcon(TopAdminPanel.IconCache.get(item.iconId));
                label.setToolTipText(item.toString());
            } else {
                label.setText("Chưa cấu hình");
                label.setIcon(null);
                label.setToolTipText(null);
            }
            return label;
        }
    }

    private static final class DragonPassOptionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final Component parent;
        private final List<DragonPassOptionChoice> choices;
        private final JButton button = new JButton();
        private DragonPassOptionsCell value = new DragonPassOptionsCell();

        DragonPassOptionsEditor(Component parent, List<DragonPassOptionChoice> choices) {
            this.parent = parent;
            this.choices = choices;
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.addActionListener(e -> {
                editOptions();
                button.setText(value.toString());
                fireEditingStopped();
            });
        }

        @Override public Object getCellEditorValue() { return value; }

        @Override public Component getTableCellEditorComponent(JTable table, Object cellValue, boolean selected,
                int row, int column) {
            value = cellValue instanceof DragonPassOptionsCell cell ? cell : new DragonPassOptionsCell();
            button.setText(value.toString());
            return button;
        }

        private void editOptions() {
            DragonPassOptionsCell editing = value.copy();
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Option", "Chỉ số"}, 0) {
                @Override public Class<?> getColumnClass(int column) { return column == 1 ? Integer.class : Object.class; }
            };
            for (DragonPassOptionValue option : editing.values) model.addRow(new Object[]{option.option, option.param});
            JTable table = new JTable(model);
            table.setRowHeight(25);
            JComboBox<DragonPassOptionChoice> optionBox = new JComboBox<>(choices.toArray(new DragonPassOptionChoice[0]));
            table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(optionBox));
            table.getColumnModel().getColumn(0).setPreferredWidth(390);
            table.getColumnModel().getColumn(1).setPreferredWidth(90);
            JButton add = new JButton("Thêm option");
            JButton remove = new JButton("Xóa option");
            add.addActionListener(e -> {
                if (!choices.isEmpty()) model.addRow(new Object[]{choices.get(0), 0});
            });
            remove.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0) model.removeRow(row);
            });
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttons.add(add);
            buttons.add(remove);
            JPanel content = new JPanel(new BorderLayout(5, 5));
            content.setPreferredSize(new Dimension(560, 360));
            content.add(new JScrollPane(table), BorderLayout.CENTER);
            content.add(buttons, BorderLayout.SOUTH);
            int answer = JOptionPane.showConfirmDialog(parent, content, "Danh sách option",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return;
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            editing.values.clear();
            for (int row = 0; row < model.getRowCount(); row++) {
                Object option = model.getValueAt(row, 0);
                Object param = model.getValueAt(row, 1);
                if (!(option instanceof DragonPassOptionChoice choice)) continue;
                int number;
                try { number = Integer.parseInt(String.valueOf(param)); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parent, "Chỉ số option phải là số nguyên.",
                            "Dragon Pass", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                editing.values.add(new DragonPassOptionValue(choice, number));
            }
            value = editing;
        }
    }

    private JPanel taoBangDieuKhienChinh() {
        JPanel bangChinh = new JPanel(new BorderLayout(15, 15));
        bangChinh.setBackground(BG_MAIN);
        bangChinh.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top: Status Cards
        JPanel pnlStatus = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlStatus.setBackground(BG_MAIN);

        ModernCard cardStatus = new ModernCard("Trạng Thái");
        lblTrangThai = taoNhanTrangThai("ONLINE", MAU_THANH_CONG, new Font("Segoe UI", Font.BOLD, 20));
        cardStatus.add(lblTrangThai, BorderLayout.CENTER);

        ModernCard cardPlayers = new ModernCard("Người Chơi");
        lblSoLuongNguoiChoi = taoNhanTrangThai("0", ACCENT_BLUE, new Font("Segoe UI", Font.BOLD, 24));
        cardPlayers.add(lblSoLuongNguoiChoi, BorderLayout.CENTER);

        ModernCard cardUptime = new ModernCard("Uptime");
        lblThoiGianHoatDong = taoNhanTrangThai("00:00:00", TEXT_PRIMARY, new Font("Segoe UI", Font.BOLD, 16));
        cardUptime.add(lblThoiGianHoatDong, BorderLayout.CENTER);

        pnlStatus.add(cardStatus);
        pnlStatus.add(cardPlayers);
        pnlStatus.add(cardUptime);

        // Center: Controls (Left) and Charts (Right)
        JPanel pnlCenter = new JPanel(new BorderLayout(15, 0));
        pnlCenter.setBackground(BG_MAIN);

        // Controls
        ModernCard cardControls = new ModernCard("Hành Động Nhanh");
        cardControls.setPreferredSize(new Dimension(350, 0));
        JPanel gridControls = new JPanel(new GridLayout(4, 2, 8, 8));
        gridControls.setBackground(BG_CARD);

        JButton btnBaoTri = taoNutCustom("Bảo Trì Hệ Thống", MAU_CANH_BAO, Color.BLACK);
        JButton btnTaiLaiDb = taoNutCustom("Tải lại DB", BG_CARD_HEADER, TEXT_PRIMARY);
        JButton btnDoiKieuDoc = taoNutCustom("Đổi Int/Long", BG_CARD_HEADER, TEXT_PRIMARY);
        JButton btnShopTool = taoNutCustom("Tool Shop", BG_CARD_HEADER, TEXT_PRIMARY);
        JButton btnGuiThu = taoNutCustom("Gửi Thư", BG_CARD_HEADER, TEXT_PRIMARY);

        btnBatTatTuDongLuu = new JToggleButton("Tự Động Lưu: TẮT");
        dinhDangToggleButton(btnBatTatTuDongLuu);
        btnBatTatTuDongDonDepCache = new JToggleButton("Dọn Cache: TẮT");
        dinhDangToggleButton(btnBatTatTuDongDonDepCache);

        btnBaoTri.addActionListener(e -> xacNhanBaoTri());
        btnTaiLaiDb.addActionListener(e -> hienThiTuyChonTaiLai());
        btnDoiKieuDoc.addActionListener(e -> {
            Util.readInt = !Util.readInt;
            lblThongTin.setText("Kiểu đọc: " + (Util.readInt ? "Int" : "Long"));
        });
        btnShopTool.addActionListener(e -> tools.ShopAddTool.showTool());
        btnBatTatTuDongLuu.addActionListener(e -> {
            boolean bat = !isTuDongLuuBat.get();
            if (bat) AutoSaveManager.getInstance().startAutoSave();
            else AutoSaveManager.getInstance().stopAutoSave();
            isTuDongLuuBat.set(bat);
            btnBatTatTuDongLuu.setText(bat ? "Tự Động Lưu: BẬT" : "Tự Động Lưu: TẮT");
        });
        btnBatTatTuDongDonDepCache.addActionListener(e -> {
            boolean bat = btnBatTatTuDongDonDepCache.isSelected();
            BoDonDepCacheTuDong.getInstance().setEnabled(bat);
            btnBatTatTuDongDonDepCache.setText(bat ? "Dọn Cache: BẬT" : "Dọn Cache: TẮT");
        });
        btnGuiThu.addActionListener(e -> moHopThoaiGuiThu(null));

        gridControls.add(btnBaoTri);
        gridControls.add(btnTaiLaiDb);
        gridControls.add(btnBatTatTuDongLuu);
        gridControls.add(btnBatTatTuDongDonDepCache);
        gridControls.add(btnDoiKieuDoc);
        gridControls.add(btnShopTool);
        gridControls.add(btnGuiThu);
        cardControls.add(gridControls, BorderLayout.CENTER);

        // Charts
        ModernCard cardPerf = new ModernCard("Hiệu Năng (Task Manager)");
        chartContainer = new JPanel(new GridLayout(2, 1, 0, 10));
        chartContainer.setBackground(BG_CARD);
        chartCpu = new Speedometer("CPU TOÀN MÁY", ACCENT_BLUE, "%");
        chartRam = new Speedometer("RAM VẬT LÝ", new Color(180, 80, 255), "%");
        chartContainer.add(chartCpu);
        chartContainer.add(chartRam);
        cardPerf.add(chartContainer, BorderLayout.CENTER);

        pnlCenter.add(cardControls, BorderLayout.WEST);
        pnlCenter.add(cardPerf, BorderLayout.CENTER);

        // Bottom: Settings & Maintenance
        JPanel pnlBottom = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlBottom.setBackground(BG_MAIN);

        ModernCard cardSettings = new ModernCard("Cài Đặt Game");
        JPanel setPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        setPanel.setBackground(BG_CARD);

        JPanel pnlExp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlExp.setBackground(BG_CARD);
        pnlExp.add(taoNhanTrangThai("Tỷ lệ EXP:", TEXT_SECONDARY, FONT_CHU_THUONG));
        JTextField txtExp = new JTextField(String.valueOf(Manager.RATE_EXP_SERVER), 5);
        JButton btnExp = taoNutCustom("Lưu", BG_CARD_HEADER, TEXT_PRIMARY);
        btnExp.addActionListener(e -> {
            try { Manager.RATE_EXP_SERVER = Integer.parseInt(txtExp.getText().trim()); lblThongTin.setText("Đã lưu EXP x" + Manager.RATE_EXP_SERVER); } catch(Exception ex){}
        });
        pnlExp.add(txtExp); pnlExp.add(btnExp);

        JPanel pnlEvent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlEvent.setBackground(BG_CARD);
        pnlEvent.add(taoNhanTrangThai("Sự kiện:", TEXT_SECONDARY, FONT_CHU_THUONG));
        String[] events = { "0 - Không", "1 - Halloween", "2 - 20/11", "3 - Noel", "4 - Tết", "5 - Hùng Vương", "6 - Trung Thu", "7 - Hè", "8 - Valentine", "9 - 20/10" };
        JComboBox<String> cbEvent = new JComboBox<>(events);
        cbEvent.setSelectedIndex(Manager.EVENT_SEVER);
        JButton btnEvent = taoNutCustom("Lưu", BG_CARD_HEADER, TEXT_PRIMARY);
        btnEvent.addActionListener(e -> capNhatSuKien(cbEvent));
        pnlEvent.add(cbEvent); pnlEvent.add(btnEvent);

        setPanel.add(pnlExp);
        setPanel.add(pnlEvent);

        lblSoLuongVatPhamKyGui = taoNhanTrangThai("Ký gửi: 0  |  Giftcode: 0", TEXT_SECONDARY, FONT_THONG_KE);
        setPanel.add(lblSoLuongVatPhamKyGui);
        cardSettings.add(setPanel, BorderLayout.CENTER);

        ModernCard cardMaint = new ModernCard("Hẹn Giờ Bảo Trì");
        JPanel maintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maintPanel.setBackground(BG_CARD);
        cbGio = new JComboBox<>(); for (int i = -1; i < 24; i++) cbGio.addItem(i);
        cbPhut = new JComboBox<>(); for (int i = -1; i < 60; i++) cbPhut.addItem(i);
        cbGiay = new JComboBox<>(); for (int i = -1; i < 60; i++) cbGiay.addItem(i);
        chkTuDongKhoiDongLai = new JCheckBox("Tự khởi động lại");
        chkTuDongKhoiDongLai.setBackground(BG_CARD);
        chkTuDongKhoiDongLai.setForeground(TEXT_PRIMARY);
        JButton btnHenGio = taoNutCustom("Đặt Lịch", MAU_CANH_BAO, Color.BLACK);
        btnHenGio.addActionListener(e -> lenLichBaoTri());
        lblDemNguoc = taoNhanTrangThai("Chưa có lịch", TEXT_SECONDARY, FONT_PHU);

        maintPanel.add(cbGio); maintPanel.add(new JLabel("h"));
        maintPanel.add(cbPhut); maintPanel.add(new JLabel("m"));
        maintPanel.add(cbGiay); maintPanel.add(new JLabel("s"));
        maintPanel.add(chkTuDongKhoiDongLai);
        maintPanel.add(btnHenGio);
        maintPanel.add(lblDemNguoc);
        cardMaint.add(maintPanel, BorderLayout.CENTER);

        pnlBottom.add(cardSettings);
        pnlBottom.add(cardMaint);

        // Footer Log
        lblThongTin = taoNhanTrangThai("Hệ thống sẵn sàng.", ACCENT_BLUE, FONT_CHU_THUONG);
        lblThongTin.setBorder(new EmptyBorder(5, 0, 0, 0));

        bangChinh.add(pnlStatus, BorderLayout.NORTH);
        bangChinh.add(pnlCenter, BorderLayout.CENTER);
        JPanel pnlBottomWrapper = new JPanel(new BorderLayout());
        pnlBottomWrapper.setBackground(BG_MAIN);
        pnlBottomWrapper.add(pnlBottom, BorderLayout.CENTER);
        pnlBottomWrapper.add(lblThongTin, BorderLayout.SOUTH);
        bangChinh.add(pnlBottomWrapper, BorderLayout.SOUTH);

        return bangChinh;
    }

    private void dinhDangToggleButton(JToggleButton btn) {
        btn.setFont(FONT_CHU_THUONG);
        btn.setFocusPainted(false);
        btn.setBackground(BG_CARD_HEADER);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }

    private JButton taoNutCustom(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_PHU);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel taoNhanTrangThai(String text, Color fg, Font font) {
        JLabel lbl = new JLabel(text);
        if (fg != null) lbl.setForeground(fg);
        if (font != null) lbl.setFont(font);
        return lbl;
    }

    // ============================================================================
    // PANEL QUẢN LÝ NGƯỜI CHƠI (GIAO DIỆN MỚI)
    // ============================================================================
    private JPanel taoBangQuanLyNguoiChoi() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_MAIN);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_PHU);
        tabs.addTab("Danh sách người chơi", taoTabDanhSachNguoiChoi());
        tabs.addTab("Điều tra & Restore", taoTabDieuTraRestore());
        wrapper.add(tabs, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel taoTabDanhSachNguoiChoi() {
        JPanel khungChua = new JPanel(new BorderLayout(10, 10));
        khungChua.setBackground(BG_MAIN);
        khungChua.setBorder(new EmptyBorder(15, 15, 15, 15));

        ModernCard topCard = new ModernCard(null);
        topCard.setLayout(new BorderLayout());
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.setBackground(BG_CARD);
        truongTimKiem = new JTextField(25);
        truongTimKiem.setFont(FONT_CHU_THUONG);
        truongTimKiem.putClientProperty("JTextField.placeholderText", "Tìm theo tên...");
        pnlSearch.add(new JLabel("Tìm kiếm: "));
        pnlSearch.add(truongTimKiem);

        JButton btnKickAll = taoNutCustom("KICK TOÀN BỘ", MAU_LOI, Color.WHITE);
        btnKickAll.addActionListener(e -> kickTatCaNguoiChoi());

        topCard.add(pnlSearch, BorderLayout.WEST);
        topCard.add(btnKickAll, BorderLayout.EAST);
        khungChua.add(topCard, BorderLayout.NORTH);

        String[] tenCot = { "Hạng", "ID", "Tên Nhân Vật", "Sức Mạnh", "Nhiệm Vụ Hiện Tại" };
        moHinhBangNguoiChoi = new DefaultTableModel(tenCot, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        bangNguoiChoi = new JTable(moHinhBangNguoiChoi);
        boSapXep = new TableRowSorter<>(moHinhBangNguoiChoi);
        bangNguoiChoi.setRowSorter(boSapXep);

        bangNguoiChoi.setBackground(BG_CARD);
        bangNguoiChoi.setForeground(TEXT_PRIMARY);
        bangNguoiChoi.setRowHeight(30);
        bangNguoiChoi.setShowGrid(false);
        bangNguoiChoi.setIntercellSpacing(new Dimension(0, 0));
        bangNguoiChoi.setSelectionBackground(ACCENT_BLUE);
        bangNguoiChoi.setSelectionForeground(Color.WHITE);

        JTableHeader header = bangNguoiChoi.getTableHeader();
        header.setBackground(BG_CARD_HEADER);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(FONT_PHU);
        header.setPreferredSize(new Dimension(100, 35));

        bangNguoiChoi.getColumnModel().getColumn(0).setMaxWidth(50);
        bangNguoiChoi.getColumnModel().getColumn(1).setMaxWidth(60);
        bangNguoiChoi.setDefaultRenderer(Object.class, new BoHienThiOBangNguoiChoiTuyChinh());

        JScrollPane cuonBang = new JScrollPane(bangNguoiChoi);
        cuonBang.setBorder(BorderFactory.createLineBorder(BG_CARD_HEADER));
        cuonBang.getViewport().setBackground(BG_CARD);
        khungChua.add(cuonBang, BorderLayout.CENTER);

        truongTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { loc() ; }
            @Override public void removeUpdate(DocumentEvent e) { loc() ; }
            @Override public void insertUpdate(DocumentEvent e) { loc() ; }
            private void loc() {
                String txt = truongTimKiem.getText();
                boSapXep.setRowFilter(txt.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + txt, 2));
            }
        });
        taoMenuNguoiChoi();
        return khungChua;
    }

    private JPanel taoTabDieuTraRestore() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG_MAIN);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        tools.setBackground(BG_CARD);
        JTextField selector = new JTextField(22);
        selector.putClientProperty("JTextField.placeholderText", "ID, tên player hoặc Trace ID");
        JSpinner days = new JSpinner(new SpinnerNumberModel(3, 1, 3, 1));
        JButton search = taoNutCustom("TRA CỨU", ACCENT_BLUE_DARK, Color.WHITE);
        JButton follow = taoNutCustom("THEO DÕI CHUỖI", BG_CARD_HEADER, Color.WHITE);
        JButton export = taoNutCustom("XUẤT BẰNG CHỨNG", BG_CARD_HEADER, Color.WHITE);
        JButton restore = taoNutCustom("RESTORE TRACE", MAU_LOI, Color.WHITE);
        tools.add(new JLabel("Người chơi/Trace:")); tools.add(selector);
        tools.add(new JLabel("Số ngày:")); tools.add(days); tools.add(search); tools.add(follow); tools.add(export); tools.add(restore);
        root.add(tools, BorderLayout.NORTH);

        String[] columns={"Thời gian","Sự kiện","Trace","Tài sản","SL","Từ","Đến","Map/Khu","X","Y","Chi tiết"};
        DefaultTableModel model=new DefaultTableModel(columns,0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable table=new JTable(model);
        table.setBackground(BG_CARD);table.setForeground(TEXT_PRIMARY);table.setSelectionBackground(ACCENT_BLUE_DARK);
        table.setRowHeight(28);table.getTableHeader().setBackground(BG_CARD_HEADER);table.getTableHeader().setForeground(TEXT_PRIMARY);
        JTextArea detail=new JTextArea(7,80);detail.setEditable(false);detail.setLineWrap(true);detail.setWrapStyleWord(true);
        detail.setBackground(BG_INPUT);detail.setForeground(TEXT_PRIMARY);detail.setBorder(new EmptyBorder(8,8,8,8));
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(table),new JScrollPane(detail));
        split.setResizeWeight(.78);root.add(split,BorderLayout.CENTER);
        List<AssetAuditEvent>[] current=new List[]{new ArrayList<>()};

        Runnable render=()->{
            model.setRowCount(0);
            for(AssetAuditEvent e:current[0]) model.addRow(new Object[]{e.createdAt,e.eventType,e.traceId,
                    e.itemName==null?e.assetType:e.itemName,e.quantity,playerLabel(e.fromPlayerId,e.fromPlayerName),
                    playerLabel(e.toPlayerId,e.toPlayerName),(e.mapId==null?"-":e.mapId)+"/"+(e.zoneId==null?"-":e.zoneId),e.x,e.y,e.context});
            detail.setText("Tìm thấy "+current[0].size()+" dòng trong tối đa 3 ngày gần nhất. Chọn một dòng để xem hoặc restore theo Trace ID.");
        };
        search.addActionListener(ev->new SwingWorker<List<AssetAuditEvent>,Void>(){
            protected List<AssetAuditEvent> doInBackground()throws Exception{return AssetAuditService.gI().search(selector.getText(),(Integer)days.getValue());}
            protected void done(){try{current[0]=get();render.run();}catch(Exception ex){showAuditError(ex);}}
        }.execute());
        follow.addActionListener(ev->{
            String trace=selectedTrace(table,current[0]);
            if(trace==null){JOptionPane.showMessageDialog(root,"Hãy chọn một dòng có Trace ID.");return;}
            new SwingWorker<List<AssetAuditEvent>,Void>(){
                protected List<AssetAuditEvent> doInBackground()throws Exception{return AssetAuditService.gI().trace(trace);}
                protected void done(){try{current[0]=get();render.run();detail.setText(AssetRestoreService.gI().buildEvidence(current[0]));}catch(Exception ex){showAuditError(ex);}}
            }.execute();
        });
        table.getSelectionModel().addListSelectionListener(ev->{if(!ev.getValueIsAdjusting()){
            int row=table.getSelectedRow();if(row>=0&&row<current[0].size())detail.setText(AssetRestoreService.gI().buildEvidence(List.of(current[0].get(row))));
        }});
        export.addActionListener(ev->{
            if(current[0].isEmpty()){JOptionPane.showMessageDialog(root,"Chưa có dữ liệu để xuất.");return;}
            JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new File("bang-chung-truy-vet-"+System.currentTimeMillis()+".txt"));
            if(chooser.showSaveDialog(root)==JFileChooser.APPROVE_OPTION){try{Files.writeString(chooser.getSelectedFile().toPath(),AssetRestoreService.gI().buildEvidence(current[0]));
                JOptionPane.showMessageDialog(root,"Đã xuất bằng chứng: "+chooser.getSelectedFile());}catch(Exception ex){showAuditError(ex);}}
        });
        restore.addActionListener(ev->{
            String trace=selectedTrace(table,current[0]);if(trace==null){JOptionPane.showMessageDialog(root,"Hãy chọn một dòng vật phẩm có Trace ID.");return;}
            String reason=JOptionPane.showInputDialog(root,"Lý do thu hồi:","vật phẩm bất chính cần hoàn về nguyên chủ");
            if(reason==null)return;
            int confirm=JOptionPane.showConfirmDialog(root,"Thu hồi toàn bộ vật phẩm hạ nguồn của trace:\n"+trace+"\n\nHành động sẽ gửi mail cho các bên. Tiếp tục?","Xác nhận restore",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(confirm!=JOptionPane.YES_OPTION)return;
            restore.setEnabled(false);
            new SwingWorker<String,Void>(){
                protected String doInBackground()throws Exception{return AssetRestoreService.gI().restoreTrace(trace,reason);}
                protected void done(){restore.setEnabled(true);try{detail.setText(get());JOptionPane.showMessageDialog(root,"Restore hoàn tất. Hồ sơ bằng chứng đã được lưu.");}catch(Exception ex){showAuditError(ex);}}
            }.execute();
        });
        return root;
    }

    private static String playerLabel(Long id,String name){return id==null?"Mặt đất/Hệ thống":id+" - "+(name==null?"?":name);}
    private static String selectedTrace(JTable table,List<AssetAuditEvent> rows){int r=table.getSelectedRow();return r>=0&&r<rows.size()?rows.get(r).traceId:null;}
    private void showAuditError(Exception ex){Throwable cause=ex.getCause()==null?ex:ex.getCause();JOptionPane.showMessageDialog(this,cause.getMessage(),"Lỗi điều tra/restore",JOptionPane.ERROR_MESSAGE);}

    private static class BoHienThiOBangNguoiChoiTuyChinh extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? BG_CARD : BG_INPUT);
            }
            if (column == 0 || column == 1) setHorizontalAlignment(SwingConstants.CENTER);
            else if (column == 3) setHorizontalAlignment(SwingConstants.RIGHT);
            else setHorizontalAlignment(SwingConstants.LEFT);
            return c;
        }
    }

    private void taoMenuNguoiChoi() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemGift = new JMenuItem("Gửi Vật Phẩm");
        JMenuItem itemBuff = new JMenuItem("Buff VND");
        JMenuItem itemMail = new JMenuItem("Gửi Mail");
        JMenuItem itemTask = new JMenuItem("Đặt Nhiệm Vụ");
        JMenuItem itemKick = new JMenuItem("Kick Người Chơi");
        JMenuItem itemBan = new JMenuItem("Ban Người Chơi");
        itemBan.setForeground(MAU_LOI);

        itemGift.addActionListener(e -> { int r = bangNguoiChoi.getSelectedRow(); if (r != -1) moHopThoaiTangVatPham((int) bangNguoiChoi.getValueAt(r, 1)); });
        itemBuff.addActionListener(e -> { int r = bangNguoiChoi.getSelectedRow(); if (r != -1) moHopThoaiBuffVndChoNguoiChoi((int) bangNguoiChoi.getValueAt(r, 1)); });
        itemMail.addActionListener(e -> { int r = bangNguoiChoi.getSelectedRow(); if (r != -1) moHopThoaiGuiMail((int) bangNguoiChoi.getValueAt(r, 1)); });
        itemTask.addActionListener(e -> { int r = bangNguoiChoi.getSelectedRow(); if (r != -1) moHopThoaiDatNhiemVu((int) bangNguoiChoi.getValueAt(r, 1)); });
        itemKick.addActionListener(e -> { int r = bangNguoiChoi.getSelectedRow(); if (r != -1) kickNguoiChoi((int) bangNguoiChoi.getValueAt(r, 1)); });
        itemBan.addActionListener(e -> { int r = bangNguoiChoi.getSelectedRow(); if (r != -1) banNguoiChoi((int) bangNguoiChoi.getValueAt(r, 1)); });

        menu.add(itemBuff); menu.add(itemGift); menu.add(itemMail); menu.add(itemTask);
        menu.addSeparator(); menu.add(itemKick); menu.add(itemBan);

        bangNguoiChoi.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int r = bangNguoiChoi.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < bangNguoiChoi.getRowCount()) {
                        bangNguoiChoi.setRowSelectionInterval(r, r);
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    // ============================================================================
    // PANEL CHỐNG DDOS (GIAO DIỆN MỚI)
    // ============================================================================
    private JPanel taoBangChongDdos() {
        JPanel bangChinh = new JPanel(new BorderLayout(15, 15));
        bangChinh.setBackground(BG_MAIN);
        bangChinh.setBorder(new EmptyBorder(15, 15, 15, 15));

        ModernCard cardTop = new ModernCard("Bảng Điều Khiển Phòng Thủ");
        JPanel topInner = new JPanel(new GridLayout(2, 1, 5, 5));
        topInner.setBackground(BG_CARD);

        JPanel pnlMode = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlMode.setBackground(BG_CARD);
        lblDdosMode = new JLabel("TRẠNG THÁI: BÌNH THƯỜNG");
        lblDdosMode.setFont(FONT_PHU);
        lblDdosMode.setForeground(MAU_THANH_CONG);

        JButton btnNormal = taoNutCustom("Bình Thường", BG_CARD_HEADER, TEXT_PRIMARY);
        JButton btnAttack = taoNutCustom("Đang Bị Tấn Công", MAU_CANH_BAO, Color.BLACK);
        JButton btnEmerg = taoNutCustom("Khẩn Cấp", MAU_LOI, Color.WHITE);

        btnNormal.addActionListener(e -> { AntiDDoSEngine.gI().setMode(AntiDDoSEngine.DefenseMode.NORMAL); capNhatHienThiDdos(); });
        btnAttack.addActionListener(e -> { AntiDDoSEngine.gI().setMode(AntiDDoSEngine.DefenseMode.UNDER_ATTACK); capNhatHienThiDdos(); });
        btnEmerg.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Kích hoạt KHẨN CẤP?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                AntiDDoSEngine.gI().setMode(AntiDDoSEngine.DefenseMode.EMERGENCY); capNhatHienThiDdos();
            }
        });
        pnlMode.add(lblDdosMode); pnlMode.add(btnNormal); pnlMode.add(btnAttack); pnlMode.add(btnEmerg);

        lblDdosStats = taoNhanTrangThai("Thống kê: Đang tải...", TEXT_SECONDARY, FONT_THONG_KE);
        topInner.add(pnlMode);
        topInner.add(lblDdosStats);
        cardTop.add(topInner, BorderLayout.CENTER);

        JTabbedPane tabDdos = new JTabbedPane();
        tabDdos.setFont(FONT_CHU_THUONG);

        moHinhIPReputation = new DefaultTableModel(new String[]{"IP", "Điểm VP", "Trạng Thái", "Kết Nối", "P/s", "Vi Phạm"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        bangIPReputationTable = new JTable(moHinhIPReputation);
        styleTable(bangIPReputationTable);
        tabDdos.addTab("IP Reputation", new JScrollPane(bangIPReputationTable));

        moHinhBangIPBiChan = new DefaultTableModel(new String[]{"IP", "Lý Do", "Chặn Lúc", "Hết Hạn"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        bangIPBiChan = new JTable(moHinhBangIPBiChan);
        styleTable(bangIPBiChan);
        tabDdos.addTab("IP Bị Chặn", new JScrollPane(bangIPBiChan));

        khuVucLogDdos = new JTextArea();
        khuVucLogDdos.setBackground(BG_INPUT);
        khuVucLogDdos.setForeground(MAU_THANH_CONG);
        khuVucLogDdos.setFont(FONT_THONG_KE);
        khuVucLogDdos.setEditable(false);
        tabDdos.addTab("Nhật Ký", new JScrollPane(khuVucLogDdos));

        ModernCard cardBot = new ModernCard("Hành Động Thủ Công");
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlControls.setBackground(BG_CARD);
        JTextField txtFilterIP = new JTextField(15);
        JButton btnBlock = taoNutCustom("Chặn 5P", BG_CARD_HEADER, TEXT_PRIMARY);
        JButton btnBlockPerm = taoNutCustom("Chặn VV", MAU_LOI, Color.WHITE);
        JButton btnUnblock = taoNutCustom("Bỏ chặn", BG_CARD_HEADER, TEXT_PRIMARY);

        btnBlock.addActionListener(e -> { AntiDDoSEngine.gI().blockIP(txtFilterIP.getText().trim(), "Admin", 300000L); txtFilterIP.setText("");});
        btnBlockPerm.addActionListener(e -> { AntiDDoSEngine.gI().blockIPPermanent(txtFilterIP.getText().trim(), "Admin"); txtFilterIP.setText("");});
        btnUnblock.addActionListener(e -> { AntiDDoSEngine.gI().unblockIP(txtFilterIP.getText().trim()); txtFilterIP.setText("");});

        pnlControls.add(new JLabel("IP: ")); pnlControls.add(txtFilterIP);
        pnlControls.add(btnBlock); pnlControls.add(btnBlockPerm); pnlControls.add(btnUnblock);
        cardBot.add(pnlControls, BorderLayout.CENTER);

        bangChinh.add(cardTop, BorderLayout.NORTH);
        bangChinh.add(tabDdos, BorderLayout.CENTER);
        bangChinh.add(cardBot, BorderLayout.SOUTH);
        return bangChinh;
    }

    private void styleTable(JTable t) {
        t.setBackground(BG_CARD);
        t.setForeground(TEXT_PRIMARY);
        t.setRowHeight(25);
        t.getTableHeader().setBackground(BG_CARD_HEADER);
        t.getTableHeader().setForeground(TEXT_PRIMARY);
        t.getTableHeader().setFont(FONT_PHU);
    }

    private void khoiTaoHeThongAntiDdos() {
        boDieuPhoiDdos = Executors.newScheduledThreadPool(1, r -> { Thread t = new Thread(r, "AntiDDoS-UI"); t.setDaemon(true); return t; });
        AntiDDoSEngine engine = AntiDDoSEngine.gI();
        engine.load();
        engine.setListener(new AntiDDoSEngine.AntiDDoSListener() {
            @Override public void onIPBlocked(String ip, String reason) {}
            @Override public void onIPUnblocked(String ip) {}
            @Override public void onModeChanged(AntiDDoSEngine.DefenseMode m) { capNhatHienThiDdos(); }
            @Override public void onLogEvent(String msg) { ghiLogDdos(msg); }
        });
        boDieuPhoiDdos.scheduleAtFixedRate(this::capNhatDuLieuDdos, 2, 2, TimeUnit.SECONDS);
    }

    private void ghiLogDdos(String msg) {
        if (khuVucLogDdos == null) return;
        String ts = dinhDangThoiGian.format(new Date());
        SwingUtilities.invokeLater(() -> {
            try {
                int len = khuVucLogDdos.getDocument().getLength();
                if (len > CHIEU_DAI_LOG_TOI_DA) khuVucLogDdos.replaceRange("", 0, len / 2);
                khuVucLogDdos.append("[" + ts + "] " + msg + "\n");
                khuVucLogDdos.setCaretPosition(khuVucLogDdos.getDocument().getLength());
            } catch (Exception ignored) {}
        });
    }

    private void capNhatHienThiDdos() {
        if (lblDdosMode == null) return;
        AntiDDoSEngine.DefenseMode m = AntiDDoSEngine.gI().getMode();
        SwingUtilities.invokeLater(() -> {
            switch (m) {
                case NORMAL -> { lblDdosMode.setText("TRẠNG THÁI: BÌNH THƯỜNG"); lblDdosMode.setForeground(MAU_THANH_CONG); }
                case UNDER_ATTACK -> { lblDdosMode.setText("TRẠNG THÁI: ĐANG BỊ TẤN CÔNG"); lblDdosMode.setForeground(MAU_CANH_BAO); }
                case EMERGENCY -> { lblDdosMode.setText("TRẠNG THÁI: KHẨN CẤP"); lblDdosMode.setForeground(MAU_LOI); }
            }
        });
    }

    private void capNhatDuLieuDdos() {
        try {
            AntiDDoSEngine engine = AntiDDoSEngine.gI();
            AntiDDoSEngine.GlobalStats stats = engine.getGlobalStats();
            String statsText = String.format("Kết nối: %d | Theo dõi: %d | Bị chặn: %d | Bỏ qua: %,d", stats.currentConnections, stats.trackedIPs, stats.blockedIPs, stats.totalPacketsDropped);
            java.util.Map<String, AntiDDoSEngine.IpEntry> ipMap = engine.getIpMap();
            java.util.List<java.util.Map.Entry<String, AntiDDoSEngine.IpEntry>> topIps = ipMap.entrySet().stream().sorted((a, b) -> Integer.compare(b.getValue().reputation.get(), a.getValue().reputation.get())).limit(150).collect(java.util.stream.Collectors.toList());
            java.util.Map<String, AntiDDoSEngine.BlockedInfo> blocked = engine.getBlockedIPs();
            java.util.List<String> logs = engine.drainLog(60);

            SwingUtilities.invokeLater(() -> {
                if (lblDdosStats != null) lblDdosStats.setText(statsText);
                capNhatHienThiDdos();
                if (moHinhIPReputation != null) {
                    moHinhIPReputation.setRowCount(0);
                    for (var e : topIps) {
                        AntiDDoSEngine.IpEntry entry = e.getValue();
                        moHinhIPReputation.addRow(new Object[]{e.getKey(), entry.reputation.get(), blocked.containsKey(e.getKey()) ? "BỊ CHẶN" : "OK", entry.activeConnections.get(), entry.getRecentPps(), entry.violationCount.get()});
                    }
                }
                if (moHinhBangIPBiChan != null) {
                    moHinhBangIPBiChan.setRowCount(0);
                    SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
                    for (var e : blocked.entrySet()) {
                        AntiDDoSEngine.BlockedInfo info = e.getValue();
                        String until = info.durationMs < 0 ? "Vĩnh viễn" : sdf2.format(new Date(info.blockedAtMs + info.durationMs));
                        moHinhBangIPBiChan.addRow(new Object[]{info.ip, info.reason, sdf2.format(new Date(info.blockedAtMs)), until});
                    }
                }
                if (khuVucLogDdos != null && !logs.isEmpty()) {
                    logs.forEach(l -> khuVucLogDdos.append(l + "\n"));
                }
            });
        } catch (Exception ignored) {}
    }

    // ============================================================================
    // PANEL BOT (GIAO DIỆN MỚI)
    // ============================================================================
    private JPanel taoBangBot() {
        JPanel khung = new JPanel(new BorderLayout(15, 15));
        khung.setBackground(BG_MAIN);
        khung.setBorder(new EmptyBorder(15, 15, 15, 15));

        ModernCard topCard = new ModernCard("Điều Khiển Bot");
        JPanel pnlCtr = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlCtr.setBackground(BG_CARD);
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Luyện tập", "Boss", "Trò chuyện"});
        JSpinner spinQty = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
        JButton btnCreate = taoNutCustom("Tạo Bot", ACCENT_BLUE, Color.BLACK);
        JButton btnDelAll = taoNutCustom("Xóa Tất Cả", MAU_LOI, Color.WHITE);

        pnlCtr.add(new JLabel("Loại:")); pnlCtr.add(cbType);
        pnlCtr.add(new JLabel("SL:")); pnlCtr.add(spinQty);
        pnlCtr.add(btnCreate); pnlCtr.add(btnDelAll);
        topCard.add(pnlCtr, BorderLayout.CENTER);

        JList<String> listBot = new JList<>(moHinhDanhSachBot);
        listBot.setBackground(BG_CARD);
        listBot.setForeground(TEXT_PRIMARY);
        listBot.setFont(FONT_PHU);
        JScrollPane scrollList = new JScrollPane(listBot);
        scrollList.setBorder(BorderFactory.createEmptyBorder());

        JTextArea txtLogBot = new JTextArea();
        txtLogBot.setBackground(BG_INPUT);
        txtLogBot.setForeground(TEXT_SECONDARY);
        txtLogBot.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLogBot);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollList, scrollLog);
        split.setDividerLocation(350);
        split.setBorder(BorderFactory.createEmptyBorder());

        btnCreate.addActionListener(e -> {
            NewBot.gI().runBot(cbType.getSelectedIndex(), (int)spinQty.getValue());
            txtLogBot.append("> Đã tạo " + spinQty.getValue() + " bot.\n");
            lamMoiDanhSachBot();
        });
        btnDelAll.addActionListener(e -> {
            BotManager.gI().getBotCopy().forEach(b -> { try { ChangeMapService.gI().exitMap(b); } catch (Exception ex){} });
            BotManager.gI().removeAllBots();
            txtLogBot.append("> Đã xóa tất cả bot.\n");
            lamMoiDanhSachBot();
        });

        lblSoLuongBot = new JLabel("Tổng: 0 bots");
        lblSoLuongBot.setForeground(TEXT_SECONDARY);
        khung.add(topCard, BorderLayout.NORTH);
        khung.add(split, BorderLayout.CENTER);
        khung.add(lblSoLuongBot, BorderLayout.SOUTH);
        return khung;
    }

    private void lamMoiDanhSachBot() {
        if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::lamMoiDanhSachBot); return; }
        try {
            moHinhDanhSachBot.clear();
            List<Bot> bots = BotManager.gI().getBotCopy();
            bots.forEach(b -> {
                if (b != null && b.name != null) moHinhDanhSachBot.addElement(b.name + " | " + ((b.zone != null && b.zone.map != null) ? b.zone.map.mapName : "N/A"));
            });
            if (lblSoLuongBot != null) lblSoLuongBot.setText("Tổng: " + bots.size() + " bots");
        } catch (Exception ignored) {}
    }

    // ============================================================================
    // LOGIC & UPDATE THREADS
    // ============================================================================
    private void khoiTaoLogic() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            this.systemProcessor = systemInfo.getHardware().getProcessor();
            this.systemMemory = systemInfo.getHardware().getMemory();
            this.previousSystemCpuTicks = systemProcessor.getSystemCpuLoadTicks();
        } catch (Exception e) {
            this.systemProcessor = null;
            this.systemMemory = null;
            this.previousSystemCpuTicks = null;
            Logger.error("Không thể khởi tạo bộ đo hiệu năng hệ điều hành: " + e.getMessage());
        }
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            khoiDongBoDemCpuWindows();
        }
        boDemUptime = new javax.swing.Timer(250, e -> capNhatThoiGianHoatDong());
        boDemUptime.setCoalesce(true);
        boDemUptime.start();
        boDieuPhoi = Executors.newScheduledThreadPool(2, r -> { Thread t = new Thread(r, "CapNhatUI"); t.setDaemon(true); return t; });
        boDieuPhoi.scheduleAtFixedRate(() -> {
            try { capNhatThongSoHeThong(); capNhatThongSoGame(); } catch (Exception e) {}
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void capNhatThongSoHeThong() {
        if (systemProcessor != null && systemMemory != null) {
            boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
            double systemCpuLoad = -1.0;
            if (!windows && previousSystemCpuTicks != null) {
                long[] currentCpuTicks = systemProcessor.getSystemCpuLoadTicks();
                systemCpuLoad = systemProcessor.getSystemCpuLoadBetweenTicks(previousSystemCpuTicks);
                previousSystemCpuTicks = currentCpuTicks;
            }
            long totalPhysicalMemory = systemMemory.getTotal();
            long availablePhysicalMemory = systemMemory.getAvailable();
            long usedPhysicalMemory = Math.max(0L, totalPhysicalMemory - availablePhysicalMemory);

            int cpuPercent = windows ? (windowsCpuCounterActive ? windowsCpuUtilityPercent : -1) : (systemCpuLoad < 0
                    ? -1 : (int) Math.round(Math.min(1.0, systemCpuLoad) * 100.0));
            int ramPercent = totalPhysicalMemory <= 0
                    ? -1 : (int) Math.round(usedPhysicalMemory * 100.0 / totalPhysicalMemory);
            String cpuDetail = (windows ? "Windows Processor Utility • " : "Toàn hệ thống • ")
                    + systemProcessor.getLogicalProcessorCount() + " luồng";
            String ramDetail = String.format(Locale.US, "%.1f / %.1f GB",
                    usedPhysicalMemory / 1073741824.0, totalPhysicalMemory / 1073741824.0);

            SwingUtilities.invokeLater(() -> {
                if (chartCpu != null && cpuPercent >= 0) {
                    missingCpuSamples = 0;
                    chartCpu.setValue(cpuPercent, cpuDetail);
                } else if (windows && ++missingCpuSamples >= 5) {
                    anDongHoCpuKhongKhaDung();
                }
                if (chartRam != null && ramPercent >= 0) chartRam.setValue(ramPercent, ramDetail);
            });
        }
    }

    private void khoiDongBoDemCpuWindows() {
        try {
            windowsCpuCounterProcess = new ProcessBuilder(
                    "typeperf", "\\Processor Information(_Total)\\% Processor Utility", "-si", "1")
                    .redirectErrorStream(true)
                    .start();
            windowsCpuCounterThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(windowsCpuCounterProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                        int separator = line.lastIndexOf("\",\"");
                        if (separator < 0 || !line.endsWith("\"")) continue;
                        String value = line.substring(separator + 3, line.length() - 1).trim().replace(',', '.');
                        try {
                            double utility = Double.parseDouble(value);
                            windowsCpuUtilityPercent = (int) Math.round(Math.max(0.0, Math.min(100.0, utility)));
                            windowsCpuCounterActive = true;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } catch (IOException e) {
                    Logger.warning("Bộ đếm CPU Windows đã dừng: " + e.getMessage() + "\n");
                } finally {
                    windowsCpuCounterActive = false;
                }
            }, "Windows-Cpu-Utility");
            windowsCpuCounterThread.setDaemon(true);
            windowsCpuCounterThread.start();
        } catch (IOException e) {
            windowsCpuCounterActive = false;
            Logger.warning("Không thể mở Windows Processor Utility: " + e.getMessage() + "\n");
        }
    }

    private void anDongHoCpuKhongKhaDung() {
        if (chartCpu == null || !chartCpu.isVisible()) return;
        chartCpu.setVisible(false);
        if (chartContainer != null) {
            chartContainer.remove(chartCpu);
            chartContainer.setLayout(new GridLayout(1, 1));
            chartContainer.revalidate();
            chartContainer.repaint();
        }
    }

    private void capNhatThongSoGame() {
        long now = System.currentTimeMillis();
        if (now - lanCapNhatUIcuoi < KHOANG_THOI_GIAN_CAP_NHAT_UI_MS) return;
        lanCapNhatUIcuoi = now;

        SwingUtilities.invokeLater(() -> {
            try {
                if (Client.gI() != null) lblSoLuongNguoiChoi.setText(String.valueOf(Client.gI().getPlayers().size()));
                capNhatBangNguoiChoi();
                if (ConsignShopManager.gI() != null && GiftCodeManager.gI() != null) {
                    lblSoLuongVatPhamKyGui.setText("Ký gửi: " + ConsignShopManager.gI().listItem.size() + "  |  Giftcode: " + GiftCodeManager.gI().listGiftCode.size());
                }
                lamMoiDanhSachBot();
            } catch (Exception ignored) {}
        });
    }

    private void capNhatBangNguoiChoi() {
        if (Client.gI() == null) return;
        List<Player> players = Client.gI().getPlayers();
        if (players == null || players.isEmpty()) { moHinhBangNguoiChoi.setRowCount(0); return; }

        List<Player> sorted = new ArrayList<>(players);
        sorted.sort((p1, p2) -> {
            if (p1 == null || p1.nPoint == null) return 1;
            if (p2 == null || p2.nPoint == null) return -1;
            return Long.compare(p2.nPoint.power, p1.nPoint.power);
        });

        int sel = bangNguoiChoi.getSelectedRow();
        int selId = sel != -1 ? (int) moHinhBangNguoiChoi.getValueAt(bangNguoiChoi.convertRowIndexToModel(sel), 1) : -1;

        moHinhBangNguoiChoi.setRowCount(0);
        int rank = 1;
        for (Player p : sorted) {
            if (p != null && p.nPoint != null) {
                moHinhBangNguoiChoi.addRow(new Object[]{ rank++, (int) p.id, p.name, String.format("%,d", p.nPoint.power), layThongTinNhiemVu(p) });
            }
        }
        if (selId != -1) {
            for (int i = 0; i < moHinhBangNguoiChoi.getRowCount(); i++) {
                if ((int) moHinhBangNguoiChoi.getValueAt(i, 1) == selId) {
                    int vRow = bangNguoiChoi.convertRowIndexToView(i);
                    if (vRow != -1) bangNguoiChoi.setRowSelectionInterval(vRow, vRow);
                    break;
                }
            }
        }
    }

    private void capNhatThoiGianHoatDong() {
        if (thoiGianKhoiDongServer != null) {
            Duration d = Duration.between(thoiGianKhoiDongServer, Instant.now());
            long s = d.getSeconds();
            lblThoiGianHoatDong.setText(String.format("%d ngày, %02d:%02d:%02d", s / 86400, (s % 86400) / 3600, (s % 3600) / 60, s % 60));
        }
    }

    // ============================================================================
    // CÁC PHƯƠNG THỨC LOGIC CHI TIẾT (ĐÃ GIỮ NGUYÊN TỪ BẢN GỐC)
    // ============================================================================
    private void hienThiTuyChonTaiLai() {
        String[] tuyChon = { "GiftCode", "Shop", "Npcs" };
        int luaChon = JOptionPane.showOptionDialog(this, "Chọn loại dữ liệu muốn tải lại:", "Tải lại Database",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, tuyChon, tuyChon[0]);
        switch (luaChon) {
            case 0 -> {
                taiGiftcode();
                lblThongTin.setText("Đã tải lại dữ liệu GiftCode.");
            }
            case 1 -> {
                taiShop();
                lblThongTin.setText("Đã tải lại dữ liệu Shop.");
            }
            case 2 -> {
                taiNpcs();
                lblThongTin.setText("Đã tải lại dữ liệu NPCs.");
            }
        }
    }

    public void taiGiftcode() {
        List<GiftCode> danhSachTam = new ArrayList<>();
        String sql = "SELECT * FROM giftcode";
        try (Connection ketNoi = AlyraManager.getConnection();
             java.sql.Statement lenh = ketNoi.createStatement();
             ResultSet ketQua = lenh.executeQuery(sql)) {
            while (ketQua.next()) {
                try {
                    GiftCode giftcode = new GiftCode();
                    giftcode.code = ketQua.getString("code");
                    giftcode.id = ketQua.getInt("id");
                    giftcode.countLeft = ketQua.getInt("count_left");
                    giftcode.datecreate = ketQua.getTimestamp("datecreate");
                    giftcode.dateexpired = ketQua.getTimestamp("expired");
                    String chiTietJson = ketQua.getString("detail");
                    if (chiTietJson != null && !chiTietJson.isEmpty()
                            && JSONValue.parse(chiTietJson) instanceof JSONArray mangJson) {
                        for (Object doiTuongItem : mangJson) {
                            JSONObject doiTuongJson = (JSONObject) doiTuongItem;
                            int idItem = laySoNguyenJson(doiTuongJson, "id");
                            int soLuong = laySoNguyenJson(doiTuongJson, "quantity");
                            JSONArray mangTuyChon = (JSONArray) doiTuongJson.get("options");
                            ArrayList<Item.ItemOption> danhSachTuyChon = new ArrayList<>();
                            if (mangTuyChon != null) {
                                for (Object doiTuongTuyChon : mangTuyChon) {
                                    JSONObject tuyChonJson = (JSONObject) doiTuongTuyChon;
                                    danhSachTuyChon.add(new Item.ItemOption(laySoNguyenJson(tuyChonJson, "id"),
                                            laySoNguyenJson(tuyChonJson, "param")));
                                }
                            }
                            giftcode.option.put(idItem, danhSachTuyChon);
                            giftcode.detail.put(idItem, soLuong);
                        }
                    }
                    danhSachTam.add(giftcode);
                } catch (Exception e) {
                    Logger.error("Lỗi tải giftcode ID: " + ketQua.getInt("id"));
                }
            }
            GiftCodeManager.gI().listGiftCode.clear();
            GiftCodeManager.gI().listGiftCode.addAll(danhSachTam);
            Logger.success("Đã tải lại thành công " + danhSachTam.size() + " giftcodes.\n");
        } catch (Exception e) {
            Logger.error("Lỗi khi tải lại giftcode từ cơ sở dữ liệu: " + e.getMessage() + "\n");
        }
    }

    public void taiNpcs() {
        List<NpcTemplate> danhSachTam = new ArrayList<>();
        String sql = "SELECT * FROM npc_template";
        try (Connection ketNoi = AlyraManager.getConnection();
             PreparedStatement lenhChuanBi = ketNoi.prepareStatement(sql);
             ResultSet ketQua = lenhChuanBi.executeQuery()) {
            while (ketQua.next()) {
                try {
                    NpcTemplate mauNpc = new NpcTemplate();
                    mauNpc.id = ketQua.getByte("id");
                    mauNpc.name = ketQua.getString("name");
                    mauNpc.head = ketQua.getShort("head");
                    mauNpc.body = ketQua.getShort("body");
                    mauNpc.leg = ketQua.getShort("leg");
                    mauNpc.avatar = ketQua.getInt("avatar");
                    danhSachTam.add(mauNpc);
                } catch (Exception e) {
                    Logger.error("Lỗi tải NPC: " + e.getMessage());
                }
            }
            Manager.NPC_TEMPLATES.clear();
            Manager.NPC_TEMPLATES.addAll(danhSachTam);
            Logger.success("Đã tải lại thành công " + danhSachTam.size() + " NPCs.\n");
        } catch (Exception e) {
            Logger.error("Lỗi khi tải lại NPCs từ cơ sở dữ liệu: " + e.getMessage() + "\n");
        }
    }

    public void taiShop() {
        try (Connection ketNoi = AlyraManager.getConnection()) {
            Manager.SHOPS = ShopDAO.getShops(ketNoi);
            Logger.success("Đã tải lại thành công " + Manager.SHOPS.size() + " cửa hàng.\n");
        } catch (Exception e) {
            Logger.error("Lỗi khi tải lại cửa hàng từ cơ sở dữ liệu\n");
        }
    }

    private int laySoNguyenJson(JSONObject doiTuong, String khoa) {
        if (doiTuong != null && doiTuong.containsKey(khoa)) {
            try {
                return Integer.parseInt(doiTuong.get(khoa).toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private void moHopThoaiTangVatPham(int idNguoiChoi) {
        layNguoiChoiTheoId(idNguoiChoi).ifPresent(nguoiChoi -> {
            JTextField truongIdVatPham = new JTextField(5);
            JTextField truongSoLuong = new JTextField(5);
            JTextField truongIdTuyChon = new JTextField(5);
            JTextField truongThamSoTuyChon = new JTextField(5);
            JPanel bang = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            bang.add(new JLabel("ID Vật phẩm:"), gbc);
            gbc.gridx = 1;
            bang.add(truongIdVatPham, gbc);
            gbc.gridy = 1;
            bang.add(truongSoLuong, gbc);
            gbc.gridx = 0;
            bang.add(new JLabel("Số lượng:"), gbc);
            gbc.gridy = 2;
            bang.add(new JLabel("ID Tùy chọn:"), gbc);
            gbc.gridx = 1;
            bang.add(truongIdTuyChon, gbc);
            gbc.gridy = 3;
            bang.add(truongThamSoTuyChon, gbc);
            gbc.gridx = 0;
            bang.add(new JLabel("Tham số:"), gbc);
            int ketQua = JOptionPane.showConfirmDialog(this, bang, "Tặng vật phẩm cho: " + nguoiChoi.name,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ketQua == JOptionPane.OK_OPTION) {
                try {
                    int idVatPham = Integer.parseInt(truongIdVatPham.getText());
                    int soLuong = Integer.parseInt(truongSoLuong.getText());
                    int idTuyChon = truongIdTuyChon.getText().isEmpty() ? 0
                            : Integer.parseInt(truongIdTuyChon.getText());
                    int thamSoTuyChon = truongThamSoTuyChon.getText().isEmpty() ? 0
                            : Integer.parseInt(truongThamSoTuyChon.getText());
                    if (soLuong <= 0) {
                        JOptionPane.showMessageDialog(this, "Số lượng phải là số dương.", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    tangVatPhamChoNguoiChoi(idNguoiChoi, idVatPham, soLuong, idTuyChon, thamSoTuyChon);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số.", "Lỗi Định Dạng",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void tangVatPhamChoNguoiChoi(int idNguoiChoi, int idVatPham, int soLuong, int idTuyChon,
                                         int thamSoTuyChon) {
        layNguoiChoiTheoId(idNguoiChoi).ifPresent(nguoiChoiMucTieu -> {
            try {
                item.Item vatPhamMoi = services.ItemService.gI().createNewItem((short) idVatPham);
                if (vatPhamMoi == null) {
                    JOptionPane.showMessageDialog(this,
                            "Tặng vật phẩm thất bại!\nID vật phẩm không hợp lệ: " + idVatPham,
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                vatPhamMoi.quantity = soLuong;
                if (idTuyChon > 0) {
                    vatPhamMoi.itemOptions.add(new item.Item.ItemOption(idTuyChon, thamSoTuyChon));
                }
                InventoryService.gI().addItemBag(nguoiChoiMucTieu, vatPhamMoi);
                InventoryService.gI().sendItemBags(nguoiChoiMucTieu);
                Service.gI().sendThongBao(nguoiChoiMucTieu,
                        "Bạn nhận được [" + vatPhamMoi.template.name + " (x" + soLuong + ")] từ Admin.");
                lblThongTin.setText("Đã tặng '" + vatPhamMoi.template.name + "' cho " + nguoiChoiMucTieu.name);
            } catch (HeadlessException e) {
                Logger.error("Lỗi khi tặng vật phẩm cho người chơi ID " + idNguoiChoi);
            }
        });
    }

    private void moHopThoaiBuffVndChoNguoiChoi(int idNguoiChoi) {
        layNguoiChoiTheoId(idNguoiChoi).ifPresent(nguoiChoi -> {
            JTextField truongSoTienVnd = new JTextField(10);
            JPanel bang = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            bang.add(new JLabel("Số VND muốn buff:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            bang.add(truongSoTienVnd, gbc);
            int ketQua = JOptionPane.showConfirmDialog(this, bang, "Buff VND cho " + nguoiChoi.name,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ketQua == JOptionPane.OK_OPTION) {
                try {
                    int soTienVnd = Integer.parseInt(truongSoTienVnd.getText().trim());
                    if (soTienVnd <= 0) {
                        JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int idTaiKhoanCapNhat = PlayerDAO.addVnd(nguoiChoi.name, soTienVnd);
                    if (idTaiKhoanCapNhat != -1) {
                        nguoiChoi.getSession().vnd += soTienVnd;
                        Service.gI().sendThongBao(nguoiChoi, "Bạn đã được cộng " + soTienVnd + " VND vào tài khoản.");
                        lblThongTin.setText("Đã buff " + soTienVnd + " VND cho " + nguoiChoi.name + ".");
                        JOptionPane.showMessageDialog(this, "Đã buff " + soTienVnd + " VND thành công!", "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Buff thất bại.", "Thất bại", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void moHopThoaiDatNhiemVu(int idNguoiChoi) {
        Optional<Player> nguoiChoiTuyChon = layNguoiChoiTheoId(idNguoiChoi);
        if (nguoiChoiTuyChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Người chơi không online.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Player nguoiChoiMucTieu = nguoiChoiTuyChon.get();
        JTextField truongIdNhiemVu = new JTextField(5);
        JTextField truongChiSoNhiemVuCon = new JTextField(5);
        JPanel bang = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        bang.add(new JLabel("ID Nhiệm vụ:"), gbc);
        gbc.gridx = 1;
        bang.add(truongIdNhiemVu, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        bang.add(new JLabel("Chỉ số nhiệm vụ con:"), gbc);
        gbc.gridx = 1;
        bang.add(truongChiSoNhiemVuCon, gbc);
        int ketQua = JOptionPane.showConfirmDialog(this, bang,
                "Đặt nhiệm vụ cho người chơi: " + nguoiChoiMucTieu.name,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ketQua == JOptionPane.OK_OPTION) {
            try {
                int idNhiemVu = Integer.parseInt(truongIdNhiemVu.getText());
                int chiSoNhiemVuCon = Integer.parseInt(truongChiSoNhiemVuCon.getText());
                datNhiemVuChoNguoiChoi(nguoiChoiMucTieu, idNhiemVu, chiSoNhiemVuCon);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập ID và Chỉ số là số hợp lệ.", "Lỗi Định Dạng",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void datNhiemVuChoNguoiChoi(Player nguoiChoi, int idNhiemVu, int chiSoNhiemVuCon) {
        try {
            TaskMain nhiemVuMoi = TaskService.gI().getTaskMainById(nguoiChoi, idNhiemVu);
            if (nhiemVuMoi.id != idNhiemVu) {
                JOptionPane.showMessageDialog(this, "ID Nhiệm vụ chính không tồn tại: " + idNhiemVu, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (chiSoNhiemVuCon < 0 || chiSoNhiemVuCon >= nhiemVuMoi.subTasks.size()) {
                JOptionPane.showMessageDialog(this,
                        "Chỉ số nhiệm vụ con không hợp lệ. Phải từ 0 đến " + (nhiemVuMoi.subTasks.size() - 1),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            nhiemVuMoi.index = (byte) chiSoNhiemVuCon;
            nhiemVuMoi.subTasks.get(nhiemVuMoi.index).count = 0;
            nguoiChoi.playerTask.taskMain = nhiemVuMoi;
            TaskService.gI().sendTaskMain(nguoiChoi);
            lblThongTin.setText(
                    "Đã đặt nhiệm vụ ID:" + idNhiemVu + " Chỉ số nhiệm vụ:" + chiSoNhiemVuCon + " cho "
                            + nguoiChoi.name);
            Service.gI().sendThongBao(nguoiChoi, "Nhiệm vụ hiện tại của bạn là: " + idNhiemVu);
            JOptionPane.showMessageDialog(this, "Đặt nhiệm vụ thành công!", "Hoàn tất",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            lblThongTin.setText("Lỗi khi đặt nhiệm vụ cho: " + nguoiChoi.name);
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kickNguoiChoi(int idNguoiChoi) {
        layNguoiChoiTheoId(idNguoiChoi).ifPresent(nguoiChoiBiKick -> {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn kick người chơi '" + nguoiChoiBiKick.name + "'?",
                    "Xác nhận Kick", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (luaChon == JOptionPane.YES_OPTION) {
                Client.gI().kickSession(nguoiChoiBiKick.getSession());
                Service.gI().sendThongBao(nguoiChoiBiKick, "Bạn đã bị kick khỏi server bởi Admin.");
                lblThongTin.setText("Đã kick người chơi: " + nguoiChoiBiKick.name);
            }
        });
    }

    private void banNguoiChoi(int idNguoiChoi) {
        layNguoiChoiTheoId(idNguoiChoi).ifPresent(nguoiChoiBiBan -> {
            int luaChon = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn BAN người chơi '" + nguoiChoiBiBan.name
                            + "'?\nHành động này sẽ kick và cấm họ đăng nhập.",
                    "Xác nhận BAN", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (luaChon == JOptionPane.YES_OPTION) {
                PlayerDAO.banAccount(nguoiChoiBiBan.getSession(), nguoiChoiBiBan);
                Service.gI().sendThongBao(nguoiChoiBiBan,
                        "Tài khoản của bạn đã bị khóa game sẽ mất kết nối sau 5 giây...");
                nguoiChoiBiBan.idMark.setLastTimeBan(System.currentTimeMillis());
                nguoiChoiBiBan.idMark.setBan(true);
                lblThongTin.setText("Đã Ban Người Chơi: " + nguoiChoiBiBan.name);
            }
        });
    }

    private void kickTatCaNguoiChoi() {
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn KICK TẤT CẢ người chơi khỏi server?",
                "Xác nhận Kick Toàn Bộ", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (luaChon == JOptionPane.YES_OPTION) {
            List<Player> cacNguoiChoiDeKick = new ArrayList<>(Client.gI().getPlayers());
            int soLuong = cacNguoiChoiDeKick.size();
            cacNguoiChoiDeKick.forEach(p -> Client.gI().kickSession(p.getSession()));
            JOptionPane.showMessageDialog(this, "Đã kick " + soLuong + " người chơi.", "Hoàn Tất",
                    JOptionPane.INFORMATION_MESSAGE);
            lblThongTin.setText("Đã kick toàn bộ " + soLuong + " người chơi.");
        }
    }

    private Optional<Player> layNguoiChoiTheoId(int id) {
        return (Client.gI() != null) ? Client.gI().getPlayers().stream().filter(p -> p.id == id).findFirst()
                : Optional.empty();
    }

    private void moHopThoaiGuiMail(int idNguoiChoi) {
        moHopThoaiGuiThu(idNguoiChoi);
    }

    @SuppressWarnings("unchecked")
    private void moHopThoaiGuiThu(Integer idNguoiChoiMacDinh) {
        JDialog dialog = new JDialog(this, "Gửi Thư", true);
        dialog.setLayout(new BorderLayout(5, 5));
        dialog.setSize(580, 620);
        dialog.setLocationRelativeTo(this);

        JPanel bangChinh = new JPanel(new GridBagLayout());
        bangChinh.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // === Người nhận ===
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel lblNguoiNhan = new JLabel("Người nhận:");
        lblNguoiNhan.setFont(FONT_TIEU_DE);
        bangChinh.add(lblNguoiNhan, gbc);

        ButtonGroup nhomNguoiNhan = new ButtonGroup();
        JRadioButton rbMotNguoi = new JRadioButton("Một người chơi");
        JRadioButton rbTatCaOnline = new JRadioButton("Tất cả online");
        JRadioButton rbTatCa = new JRadioButton("Tất cả (cả offline)");
        nhomNguoiNhan.add(rbMotNguoi);
        nhomNguoiNhan.add(rbTatCaOnline);
        nhomNguoiNhan.add(rbTatCa);
        rbMotNguoi.setSelected(true);

        JPanel bangRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bangRadio.add(rbMotNguoi);
        bangRadio.add(rbTatCaOnline);
        bangRadio.add(rbTatCa);
        gbc.gridy = 1;
        bangChinh.add(bangRadio, gbc);

        JPanel bangNguoiNhan = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JTextField truongIdNguoiNhan = new JTextField(8);
        JTextField truongTenNguoiNhan = new JTextField(12);
        bangNguoiNhan.add(new JLabel("ID:"));
        bangNguoiNhan.add(truongIdNguoiNhan);
        bangNguoiNhan.add(new JLabel("hoặc Tên:"));
        bangNguoiNhan.add(truongTenNguoiNhan);
        gbc.gridy = 2;
        bangChinh.add(bangNguoiNhan, gbc);

        if (idNguoiChoiMacDinh != null) {
            truongIdNguoiNhan.setText(String.valueOf(idNguoiChoiMacDinh));
            layNguoiChoiTheoId(idNguoiChoiMacDinh).ifPresent(p -> truongTenNguoiNhan.setText(p.name));
        }

        rbTatCaOnline.addActionListener(e -> {
            truongIdNguoiNhan.setEnabled(false);
            truongTenNguoiNhan.setEnabled(false);
        });
        rbTatCa.addActionListener(e -> {
            truongIdNguoiNhan.setEnabled(false);
            truongTenNguoiNhan.setEnabled(false);
        });
        rbMotNguoi.addActionListener(e -> {
            truongIdNguoiNhan.setEnabled(true);
            truongTenNguoiNhan.setEnabled(true);
        });

        // === Separator ===
        gbc.gridy = 3; gbc.gridwidth = 3;
        bangChinh.add(new JSeparator(), gbc);

        // === Tiêu đề ===
        gbc.gridy = 4; gbc.gridwidth = 1;
        bangChinh.add(new JLabel("Tiêu đề:"), gbc);
        JTextField truongTieuDe = new JTextField(30);
        gbc.gridx = 1; gbc.gridwidth = 2;
        bangChinh.add(truongTieuDe, gbc);

        // === Nội dung ===
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        bangChinh.add(new JLabel("Nội dung:"), gbc);
        JTextArea truongNoiDung = new JTextArea(3, 30);
        truongNoiDung.setLineWrap(true);
        truongNoiDung.setWrapStyleWord(true);
        gbc.gridx = 1; gbc.gridwidth = 2;
        bangChinh.add(new JScrollPane(truongNoiDung), gbc);

        // === Hết hạn ===
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        bangChinh.add(new JLabel("Hết hạn (ngày):"), gbc);
        JTextField truongNgayHetHan = new JTextField("30", 5);
        gbc.gridx = 1; gbc.gridwidth = 2;
        bangChinh.add(truongNgayHetHan, gbc);

        // === Separator ===
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        bangChinh.add(new JSeparator(), gbc);

        // === Phần thưởng ===
        gbc.gridy = 8;
        JLabel lblReward = new JLabel("Phần thưởng đính kèm");
        lblReward.setFont(FONT_TIEU_DE);
        bangChinh.add(lblReward, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 9;
        bangChinh.add(new JLabel("Vàng:"), gbc);
        JTextField truongVang = new JTextField("0", 10);
        gbc.gridx = 1;
        bangChinh.add(truongVang, gbc);

        gbc.gridx = 0; gbc.gridy = 10;
        bangChinh.add(new JLabel("Ngọc:"), gbc);
        JTextField truongNgoc = new JTextField("0", 10);
        gbc.gridx = 1;
        bangChinh.add(truongNgoc, gbc);

        // === Separator ===
        gbc.gridx = 0; gbc.gridy = 11; gbc.gridwidth = 3;
        bangChinh.add(new JSeparator(), gbc);

        // === Vật phẩm đính kèm (động) ===
        gbc.gridy = 12;
        JLabel lblVatPham = new JLabel("Vật phẩm đính kèm");
        lblVatPham.setFont(FONT_TIEU_DE);
        bangChinh.add(lblVatPham, gbc);

        JPanel bangDanhSachVatPham = new JPanel();
        bangDanhSachVatPham.setLayout(new BoxLayout(bangDanhSachVatPham, BoxLayout.Y_AXIS));
        List<JPanel> danhSachHangVatPham = new ArrayList<>();
        List<DragonPassItemChoice> mailItemChoices = loadDragonPassItems();
        List<DragonPassOptionChoice> mailOptionChoices = loadDragonPassOptions();

        Runnable themHangVatPham = () -> {
            JPanel hang = new JPanel();
            hang.setLayout(new BoxLayout(hang, BoxLayout.Y_AXIS));
            hang.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(3, 4, 3, 4)));

            // --- Dòng chính: ID, SL, +Option, X ---
            JPanel dongChinh = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 1));
            JTextField fId = new JTextField(5);
            JButton btnChonItem = new JButton("Chọn...");
            JLabel lblItemDaChon = new JLabel();
            JTextField fSoLuong = new JTextField("1", 4);
            List<JPanel> danhSachOption = new ArrayList<>();
            JPanel bangOption = new JPanel();
            bangOption.setLayout(new BoxLayout(bangOption, BoxLayout.Y_AXIS));

            JButton btnThemOpt = new JButton("+Opt");
            btnThemOpt.setMargin(new Insets(0, 4, 0, 4));
            JButton btnXoa = new JButton("X");
            btnXoa.setMargin(new Insets(0, 4, 0, 4));
            btnXoa.setForeground(MAU_LOI);

            dongChinh.add(new JLabel("ID:"));
            dongChinh.add(fId);
            dongChinh.add(btnChonItem);
            dongChinh.add(lblItemDaChon);
            dongChinh.add(new JLabel("SL:"));
            dongChinh.add(fSoLuong);
            dongChinh.add(btnThemOpt);
            dongChinh.add(btnXoa);
            hang.add(dongChinh);
            hang.add(bangOption);

            // Thêm 1 dòng option
            Runnable themOption = () -> {
                JPanel optRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
                JComboBox<DragonPassOptionChoice> optionBox = new JComboBox<>();
                optionBox.addItem(null);
                for (DragonPassOptionChoice choice : mailOptionChoices) optionBox.addItem(choice);
                optionBox.setMaximumRowCount(18);
                optionBox.setPreferredSize(new Dimension(330, 25));
                optionBox.setRenderer(new DefaultListCellRenderer() {
                    @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                            int index, boolean selected, boolean focus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(
                                list, value, index, selected, focus);
                        label.setText(value instanceof DragonPassOptionChoice
                                ? value.toString() : "Chọn option...");
                        return label;
                    }
                });
                JTextField fParam = new JTextField(5);
                optionBox.setToolTipText("Chọn từ danh sách item_option_template");
                fParam.setToolTipText("Param, VD: 100");
                JButton btnXoaOpt = new JButton("x");
                btnXoaOpt.setMargin(new Insets(0, 3, 0, 3));
                btnXoaOpt.setFont(btnXoaOpt.getFont().deriveFont(10f));
                optRow.add(new JLabel("  Opt:"));
                optRow.add(optionBox);
                optRow.add(new JLabel("Param:"));
                optRow.add(fParam);
                optRow.add(btnXoaOpt);
                optRow.putClientProperty("optionChoice", optionBox);
                optRow.putClientProperty("fParam", fParam);
                btnXoaOpt.addActionListener(ev2 -> {
                    bangOption.remove(optRow);
                    danhSachOption.remove(optRow);
                    bangOption.revalidate();
                    bangOption.repaint();
                    bangDanhSachVatPham.revalidate();
                });
                danhSachOption.add(optRow);
                bangOption.add(optRow);
                bangOption.revalidate();
                bangOption.repaint();
                bangDanhSachVatPham.revalidate();
            };

            btnThemOpt.addActionListener(ev -> themOption.run());
            btnChonItem.addActionListener(ev -> {
                DragonPassItemChoice item = chonVatPhamCoIcon(dialog, mailItemChoices);
                if (item != null) { fId.setText(String.valueOf(item.id)); lblItemDaChon.setText(item.name); lblItemDaChon.setIcon(TopAdminPanel.IconCache.get(item.iconId)); }
            });
            btnXoa.addActionListener(ev -> {
                bangDanhSachVatPham.remove(hang);
                danhSachHangVatPham.remove(hang);
                bangDanhSachVatPham.revalidate();
                bangDanhSachVatPham.repaint();
            });

            hang.putClientProperty("fId", fId);
            hang.putClientProperty("fSoLuong", fSoLuong);
            hang.putClientProperty("danhSachOption", danhSachOption);
            danhSachHangVatPham.add(hang);
            bangDanhSachVatPham.add(hang);
            bangDanhSachVatPham.revalidate();
            bangDanhSachVatPham.repaint();
        };

        JButton btnThemVatPham = new JButton("+ Thêm vật phẩm");
        btnThemVatPham.addActionListener(e -> themHangVatPham.run());

        gbc.gridy = 13; gbc.gridwidth = 3;
        bangChinh.add(btnThemVatPham, gbc);

        JScrollPane cuonVatPham = new JScrollPane(bangDanhSachVatPham);
        cuonVatPham.setPreferredSize(new Dimension(540, 180));
        gbc.gridy = 14;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        bangChinh.add(cuonVatPham, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // === Buttons ===
        JPanel bangNut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGui = new JButton("Gửi Thư");
        JButton btnHuy = new JButton("Hủy");
        bangNut.add(btnGui);
        bangNut.add(btnHuy);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnGui.addActionListener(e -> {
            try {
                String tieuDe = truongTieuDe.getText().trim();
                String noiDung = truongNoiDung.getText().trim();
                if (tieuDe.isEmpty() || noiDung.isEmpty()) {
                    hienThiLoi("Tiêu đề và nội dung không được để trống.");
                    return;
                }
                int ngayHetHan = Integer.parseInt(truongNgayHetHan.getText().trim());
                if (ngayHetHan <= 0 || ngayHetHan > 365) {
                    hienThiLoi("Ngày hết hạn phải từ 1 đến 365.");
                    return;
                }
                int soVang = truongVang.getText().trim().isEmpty() ? 0
                        : Integer.parseInt(truongVang.getText().trim());
                int soNgoc = truongNgoc.getText().trim().isEmpty() ? 0
                        : Integer.parseInt(truongNgoc.getText().trim());

                // Parse items
                List<int[]> danhSachItem = new ArrayList<>(); // {id, quantity}
                List<String> danhSachOptionsJson = new ArrayList<>();
                for (JPanel hang : danhSachHangVatPham) {
                    JTextField fId = (JTextField) hang.getClientProperty("fId");
                    JTextField fSoLuong = (JTextField) hang.getClientProperty("fSoLuong");
                    @SuppressWarnings("unchecked")
                    List<JPanel> optRows = (List<JPanel>) hang.getClientProperty("danhSachOption");
                    String sId = fId.getText().trim();
                    if (sId.isEmpty()) continue;
                    int itemId = Integer.parseInt(sId);
                    int sl = fSoLuong.getText().trim().isEmpty() ? 1
                            : Integer.parseInt(fSoLuong.getText().trim());
                    if (itemId <= 0 || sl <= 0) continue;

                    item.Item kiemTra = services.ItemService.gI().createNewItem((short) itemId);
                    if (kiemTra == null) {
                        hienThiLoi("ID vật phẩm không hợp lệ: " + itemId);
                        return;
                    }

                    String optionsJson = taoOptionsJson(optRows);
                    danhSachItem.add(new int[]{itemId, sl});
                    danhSachOptionsJson.add(optionsJson);
                }

                // Determine recipients and send
                if (rbMotNguoi.isSelected()) {
                    String sIdNguoiNhan = truongIdNguoiNhan.getText().trim();
                    String sTenNguoiNhan = truongTenNguoiNhan.getText().trim();
                    Long playerId = null;
                    if (!sIdNguoiNhan.isEmpty()) {
                        playerId = Long.parseLong(sIdNguoiNhan);
                    } else if (!sTenNguoiNhan.isEmpty()) {
                        Player found = Client.gI().getPlayer(sTenNguoiNhan);
                        if (found != null) {
                            playerId = found.id;
                        } else {
                            playerId = timPlayerIdTheoTen(sTenNguoiNhan);
                        }
                    }
                    if (playerId == null || playerId <= 0) {
                        hienThiLoi("Không tìm thấy người chơi.");
                        return;
                    }
                    guiThuChoPlayerId(playerId, tieuDe, noiDung, ngayHetHan,
                            soVang, soNgoc, danhSachItem, danhSachOptionsJson);
                    dialog.dispose();
                } else if (rbTatCaOnline.isSelected()) {
                    List<Player> dsOnline = Client.gI().getPlayers();
                    if (dsOnline == null || dsOnline.isEmpty()) {
                        hienThiLoi("Không có người chơi online.");
                        return;
                    }
                    int xacNhan = JOptionPane.showConfirmDialog(dialog,
                            "Gửi thư cho " + dsOnline.size() + " người chơi online?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (xacNhan != JOptionPane.YES_OPTION) return;
                    int count = 0;
                    for (Player p : dsOnline) {
                        try {
                            guiThuChoPlayerId(p.id, tieuDe, noiDung, ngayHetHan,
                                    soVang, soNgoc, danhSachItem, danhSachOptionsJson);
                            count++;
                        } catch (Exception ex) {
                            Logger.error("Lỗi gửi thư cho " + p.name + ": " + ex.getMessage() + "\n");
                        }
                    }
                    lblThongTin.setText("Đã gửi thư cho " + count + "/" + dsOnline.size() + " người chơi online.");
                    JOptionPane.showMessageDialog(dialog,
                            "Đã gửi thư cho " + count + "/" + dsOnline.size() + " người chơi online.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else if (rbTatCa.isSelected()) {
                    List<Long> dsPlayerId = layTatCaPlayerId();
                    if (dsPlayerId.isEmpty()) {
                        hienThiLoi("Không tìm thấy người chơi nào trong database.");
                        return;
                    }
                    int xacNhan = JOptionPane.showConfirmDialog(dialog,
                            "Gửi thư cho " + dsPlayerId.size() + " người chơi (bao gồm offline)?",
                            "Xác nhận", JOptionPane.YES_NO_OPTION);
                    if (xacNhan != JOptionPane.YES_OPTION) return;
                    btnGui.setEnabled(false);
                    btnGui.setText("Đang gửi...");
                    new Thread(() -> {
                        int count = 0;
                        for (long pid : dsPlayerId) {
                            try {
                                guiThuChoPlayerId(pid, tieuDe, noiDung, ngayHetHan,
                                        soVang, soNgoc, danhSachItem, danhSachOptionsJson);
                                count++;
                            } catch (Exception ex) {
                                Logger.error("Lỗi gửi thư cho player " + pid + ": " + ex.getMessage() + "\n");
                            }
                        }
                        int finalCount = count;
                        int total = dsPlayerId.size();
                        SwingUtilities.invokeLater(() -> {
                            lblThongTin.setText("Đã gửi thư cho " + finalCount + "/" + total + " người chơi.");
                            JOptionPane.showMessageDialog(dialog,
                                    "Đã gửi thư cho " + finalCount + "/" + total + " người chơi.",
                                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            dialog.dispose();
                        });
                    }, "GuiThuThread").start();
                    return;
                }
            } catch (NumberFormatException ex) {
                hienThiLoi("Vui lòng nhập đúng định dạng số.");
            }
        });

        dialog.add(new JScrollPane(bangChinh), BorderLayout.CENTER);
        dialog.add(bangNut, BorderLayout.SOUTH);
        apDungGiaoDienHienDai(dialog.getContentPane());
        dialog.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private String taoOptionsJson(List<JPanel> optionRows) {
        if (optionRows == null || optionRows.isEmpty()) {
            return null;
        }
        JSONArray arr = new JSONArray();
        for (JPanel row : optionRows) {
            @SuppressWarnings("unchecked")
            JComboBox<DragonPassOptionChoice> optionBox =
                    (JComboBox<DragonPassOptionChoice>) row.getClientProperty("optionChoice");
            JTextField fParam = (JTextField) row.getClientProperty("fParam");
            Object selected = optionBox == null ? null : optionBox.getSelectedItem();
            if (!(selected instanceof DragonPassOptionChoice choice)) continue;
            try {
                int param = 0;
                String sParam = fParam.getText().trim();
                if (!sParam.isEmpty()) param = Integer.parseInt(sParam);
                JSONObject obj = new JSONObject();
                obj.put("id", choice.id);
                obj.put("param", param);
                arr.add(obj);
            } catch (NumberFormatException ignored) {
            }
        }
        return arr.isEmpty() ? null : arr.toJSONString();
    }

    private void guiThuChoPlayerId(long playerId, String tieuDe, String noiDung,
                                   int ngayHetHan, int soVang, int soNgoc,
                                   List<int[]> danhSachItem, List<String> danhSachOptionsJson) {
        MailService mailService = MailHandler.gI().getMailService();
        long mailId = mailService.sendMail(playerId, tieuDe, noiDung, ngayHetHan);

        boolean coReward = false;

        if (soVang > 0) {
            mailService.getRewardDAO().saveReward(
                    new MailReward(mailId, RewardType.GOLD.getCode(), soVang));
            coReward = true;
        }
        if (soNgoc > 0) {
            mailService.getRewardDAO().saveReward(
                    new MailReward(mailId, RewardType.GEM.getCode(), soNgoc));
            coReward = true;
        }
        for (int i = 0; i < danhSachItem.size(); i++) {
            int[] it = danhSachItem.get(i);
            String optJson = danhSachOptionsJson.get(i);
            mailService.getRewardDAO().saveReward(
                    new MailReward(mailId, RewardType.ITEM.getCode(), it[0], it[1], optJson));
            coReward = true;
        }

        if (coReward) {
            mailService.getMailDAO().updateHasReward(mailId, true);
        }

        // Thông báo nếu online
        Player online = Client.gI().getPlayerByID((int) playerId);
        if (online != null) {
            MailHandler.gI().notifyNewMail(online, tieuDe);
        }
    }

    private Long timPlayerIdTheoTen(String ten) {
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id FROM player WHERE LOWER(name) = LOWER(?) LIMIT 1")) {
            ps.setString(1, ten);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (Exception e) {
            Logger.error("Lỗi tìm player theo tên: " + e.getMessage() + "\n");
        }
        return null;
    }

    private List<Long> layTatCaPlayerId() {
        List<Long> ds = new ArrayList<>();
        try (Connection con = AlyraManager.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id FROM player");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(rs.getLong("id"));
            }
        } catch (Exception e) {
            Logger.error("Lỗi lấy danh sách player: " + e.getMessage() + "\n");
        }
        return ds;
    }

    private void khoiDongCacTienTrinhServer() {
        taiCauHinhBaoTri();
        ServerManager.gI().init();
        if (AutoMaintenance.AutoMaintenance) {
            AutoMaintenance.gI().start();
        }
        EventQueue.invokeLater(() -> setVisible(true));
    }

    private void taiCauHinhBaoTri() {
        File file = new File("maintenanceConfig.txt");
        if (!file.exists()) {
            cbGio.setSelectedItem(-1);
            cbPhut.setSelectedItem(-1);
            cbGiay.setSelectedItem(-1);
            return;
        }
        try (BufferedReader boDoc = new BufferedReader(new FileReader(file))) {
            cbGio.setSelectedItem(Integer.valueOf(boDoc.readLine()));
            cbPhut.setSelectedItem(Integer.valueOf(boDoc.readLine()));
            cbGiay.setSelectedItem(Integer.valueOf(boDoc.readLine()));
            chkTuDongKhoiDongLai.setSelected(Boolean.parseBoolean(boDoc.readLine()));
        } catch (Exception e) {
            lblThongTin.setText("Lỗi đọc cấu hình bảo trì.");
        }
    }

    private void xacNhanBaoTri() {
        String thongDiepKhoiDongLai = chkTuDongKhoiDongLai.isSelected()
                ? "\nServer sẽ tự động khởi động lại."
                : "\nServer sẽ KHÔNG tự động khởi động lại.";

        Object[] cacLuaChon = {
                "Bảo trì sau 10 giây",
                "Bảo trì sau 60 giây",
                "Hủy"
        };
        int luaChon = JOptionPane.showOptionDialog(
                this,
                "Chọn thời gian đếm ngược trước khi bảo trì." + thongDiepKhoiDongLai
                        + "\nNgười chơi online sẽ nhận được thông báo trong game.",
                "Xác nhận bảo trì",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                cacLuaChon,
                cacLuaChon[0]);

        if (luaChon == 0 || luaChon == 1) {
            YEU_CAU_TU_DONG_KHOI_DONG_LAI = chkTuDongKhoiDongLai.isSelected();
            huyTatCaCacTacVuDaLenLich();
            int thoiGianBaoTri = luaChon == 0 ? 10 : 60;
            Maintenance.gI().startBySecond(thoiGianBaoTri);
            lblTrangThai.setText("Bảo trì sau " + thoiGianBaoTri + " giây...");
            lblTrangThai.setForeground(MAU_CANH_BAO);

            var giayConLai = new AtomicInteger(thoiGianBaoTri);

            if (tacVuDemNguocBaoTriNgayLapTuc != null && !tacVuDemNguocBaoTriNgayLapTuc.isCancelled()) {
                tacVuDemNguocBaoTriNgayLapTuc.cancel(false);
            }
            tacVuDemNguocBaoTriNgayLapTuc = boDieuPhoi.scheduleAtFixedRate(() -> {
                int giay = giayConLai.getAndDecrement();

                if (giay >= 0) {
                    SwingUtilities.invokeLater(() -> {
                        lblDemNguoc.setText(String.format("%02d:%02d", giay / 60, giay % 60));
                    });
                } else {
                    tacVuDemNguocBaoTriNgayLapTuc.cancel(false);
                    SwingUtilities.invokeLater(() -> lblDemNguoc.setText("00:00"));
                }

            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void lenLichBaoTri() {
        Integer gio = (Integer) cbGio.getSelectedItem();
        Integer phut = (Integer) cbPhut.getSelectedItem();
        Integer giay = (Integer) cbGiay.getSelectedItem();
        if (gio == -1 || phut == -1 || giay == -1) {
            hienThiCanhBao("Vui lòng chọn thời gian hẹn giờ hợp lệ (không để '-1').");
            return;
        }
        YEU_CAU_TU_DONG_KHOI_DONG_LAI = chkTuDongKhoiDongLai.isSelected();
        long thoiGianCho = LocalTime.of(gio, phut, giay).toSecondOfDay() - LocalTime.now().toSecondOfDay();
        if (thoiGianCho < 0) {
            thoiGianCho += 86400;
        }
        huyTatCaCacTacVuDaLenLich();
        baoTriDaDuocLenLich.set(true);
        lblTrangThai.setText("Đã hẹn giờ bảo trì");
        lblTrangThai.setForeground(MAU_CANH_BAO);
        long thoiDiemKichHoatEpoch = System.currentTimeMillis() / 1000 + thoiGianCho;
        tacVuBaoTriDangHoatDong = boDieuPhoi.schedule(() -> {
            if (baoTriDaDuocLenLich.get()) {
                Maintenance.gI().start(1);
            }
        }, thoiGianCho, TimeUnit.SECONDS);
        tacVuDemNguocBaoTriNgayLapTuc = boDieuPhoi.scheduleAtFixedRate(() -> {
            if (!baoTriDaDuocLenLich.get()) {
                if (tacVuDemNguocBaoTriNgayLapTuc != null) {
                    tacVuDemNguocBaoTriNgayLapTuc.cancel(false);
                }
                return;
            }
            long conLai = thoiDiemKichHoatEpoch - (System.currentTimeMillis() / 1000);
            long hr = conLai / 3600, mn = (conLai % 3600) / 60, sc = conLai % 60;
            SwingUtilities.invokeLater(() -> lblDemNguoc.setText(String.format("%02d:%02d:%02d", hr, mn, sc)));
        }, 0, 1, TimeUnit.SECONDS);
        luuCauHinhBaoTri(gio, phut, giay, YEU_CAU_TU_DONG_KHOI_DONG_LAI);
    }

    private void luuCauHinhBaoTri(int gio, int phut, int giay, boolean tuDongKhoiDongLai) {
        try (PrintWriter boGhi = new PrintWriter(new FileWriter("maintenanceConfig.txt"))) {
            boGhi.println(gio);
            boGhi.println(phut);
            boGhi.println(giay);
            boGhi.println(tuDongKhoiDongLai);
            lblThongTin.setText(String.format("Lịch bảo trì: %02d:%02d:%02d | Tự động khởi động lại: %s", gio, phut,
                    giay, tuDongKhoiDongLai ? "Bật" : "Tắt"));
        } catch (IOException e) {
            Logger.error("Lỗi lưu cấu hình bảo trì: " + e.getMessage());
        }
    }

    private void huyTatCaCacTacVuDaLenLich() {
        if (tacVuBaoTriDangHoatDong != null) {
            tacVuBaoTriDangHoatDong.cancel(true);
        }
        if (tacVuDemNguocBaoTriNgayLapTuc != null) {
            tacVuDemNguocBaoTriNgayLapTuc.cancel(true);
        }
        baoTriDaDuocLenLich.set(false);
    }

    private void tatServer() {
        AntiDDoSEngine.gI().persist();
        AntiDDoSEngine.gI().shutdown();

        if (boDemUptime != null) {
            boDemUptime.stop();
        }
        if (windowsCpuCounterThread != null) {
            windowsCpuCounterThread.interrupt();
        }
        if (windowsCpuCounterProcess != null) {
            windowsCpuCounterProcess.destroy();
        }

        if (boDieuPhoiDdos != null && !boDieuPhoiDdos.isShutdown()) {
            boDieuPhoiDdos.shutdownNow();
            try {
                boDieuPhoiDdos.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        AutoSaveManager.getInstance().stopAutoSave();

        if (AutoMaintenance.isRunning) {
            AutoMaintenance.gI().interrupt();
        }

        if (boDieuPhoi != null && !boDieuPhoi.isShutdown()) {
            boDieuPhoi.shutdownNow();
            try {
                boDieuPhoi.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.exit(0);
    }

    public static void thuTucTuDongKhoiDongLai() {
        if (!YEU_CAU_TU_DONG_KHOI_DONG_LAI) {
            return;
        }
        String tenHeDieuHanh = System.getProperty("os.name").toLowerCase();
        String thuMucHienTai = System.getProperty("user.dir");
        try {
            if (tenHeDieuHanh.contains("win")) {
                new ProcessBuilder("cmd.exe", "/c", "start", "run.bat").directory(new File(thuMucHienTai)).start();
            } else {
                new ProcessBuilder("xterm", "-e", "./run.sh").directory(new File(thuMucHienTai)).start();
            }
        } catch (IOException ignored) {
        }
    }

    private String layThongTinNhiemVu(Player nguoiChoi) {
        try {
            if (nguoiChoi != null && nguoiChoi.playerTask != null && nguoiChoi.playerTask.taskMain != null
                    && nguoiChoi.playerTask.taskMain.subTasks != null
                    && !nguoiChoi.playerTask.taskMain.subTasks.isEmpty()
                    && nguoiChoi.playerTask.taskMain.index < nguoiChoi.playerTask.taskMain.subTasks.size()) {
                String tenNhiemVu = nguoiChoi.playerTask.taskMain.subTasks
                        .get(nguoiChoi.playerTask.taskMain.index).name;
                short soLuongHoanThanh = nguoiChoi.playerTask.taskMain.subTasks
                        .get(nguoiChoi.playerTask.taskMain.index).count;
                short soLuongCanThiet = nguoiChoi.playerTask.taskMain.subTasks
                        .get(nguoiChoi.playerTask.taskMain.index).maxCount;
                if (soLuongCanThiet > 0) {
                    return String.format("%s (%d/%d)", tenNhiemVu, soLuongHoanThanh, soLuongCanThiet);
                } else {
                    return tenNhiemVu;
                }
            }
        } catch (Exception e) {
            return "Lỗi dữ liệu";
        }
        return "Không có nhiệm vụ";
    }
}
