package setup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.GraphicsEnvironment;
import java.nio.charset.StandardCharsets;
import server.ServerManager;

/**
 * Entry point duy nhat cua file JAR.
 *
 * <p>Launcher nay chi dieu huong den trinh setup hoac server that. Vi nam trong
 * cung project nen ban deploy van chi can mot file nro-server-*.jar.</p>
 */
public final class ApplicationLauncher {

    private ApplicationLauncher() {
    }

    public static void main(String[] args) {
        String command = args.length == 0 ? "" : args[0].trim().toLowerCase();

        try {
            switch (command) {
                case "--setup", "setup" -> openSetup();
                case "--server", "server" -> startServerIfReady();
                case "--help", "-h", "help" -> printHelp();
                case "" -> openLauncher();
                default -> {
                    System.err.println("Lenh khong hop le: " + args[0]);
                    printHelp();
                    System.exit(2);
                }
            }
        } catch (Exception e) {
            System.err.println("Khong the khoi dong: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void openLauncher() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            showMenu();
        } else {
            LauncherWindow.showWindow();
        }
    }

    private static void openSetup() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            runSetup();
        } else {
            LauncherTheme.install();
            javax.swing.SwingUtilities.invokeLater(
                    () -> new SetupWindow(null, new SetupManager()).setVisible(true));
        }
    }

    private static void showMenu() throws Exception {
        SetupManager setup = new SetupManager();
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));

        while (true) {
            boolean ready = setup.isSetupComplete();
            String defaultChoice = ready ? "1" : "2";

            System.out.println();
            System.out.println("========================================");
            System.out.println("          NRO SERVER LAUNCHER");
            System.out.println("========================================");
            System.out.println("Trang thai setup: " + (ready ? "DA HOAN TAT" : "CHUA HOAN TAT"));
            System.out.println("1. Chay server");
            System.out.println("2. Setup / sua cau hinh");
            System.out.println("3. Kiem tra cau hinh");
            System.out.println("0. Thoat");
            System.out.print("Lua chon [mac dinh " + defaultChoice + "]: ");

            String choice = input.readLine();
            if (choice == null) {
                System.out.println("Khong co console dau vao. Dung --server de chay tu dong.");
                return;
            }
            choice = choice.trim().isEmpty() ? defaultChoice : choice.trim();

            switch (choice) {
                case "1" -> {
                    if (!setup.isSetupComplete()) {
                        System.out.println("Server chua duoc setup. Hay chon muc 2 truoc.");
                        continue;
                    }
                    setup.verifySavedConfiguration();
                    startServer();
                    return;
                }
                case "2" -> setup.runInteractiveSetup(input);
                case "3" -> setup.printStatus();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Lua chon khong hop le.");
            }
        }
    }

    private static void runSetup() throws Exception {
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        new SetupManager().runInteractiveSetup(input);
    }

    private static void startServerIfReady() throws Exception {
        SetupManager setup = new SetupManager();
        if (!setup.isSetupComplete()) {
            throw new IllegalStateException(
                    "Setup chua hoan tat. Chay: java -jar nro-server-1.0.0.jar --setup");
        }
        setup.verifySavedConfiguration();
        startServer();
    }

    private static void startServer() {
        System.out.println("Dang chuyen sang NRO server...");
        ServerManager.main(new String[0]);
    }

    private static void printHelp() {
        System.out.println("java -jar nro-server-1.0.0.jar            Mo menu");
        System.out.println("java -jar nro-server-1.0.0.jar --setup    Mo trinh setup");
        System.out.println("java -jar nro-server-1.0.0.jar --server   Chay server khong hien menu");
    }
}
