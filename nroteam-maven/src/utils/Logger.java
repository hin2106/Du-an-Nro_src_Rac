package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class Logger {

    private static Logger instance;

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    // Log level configuration
    private static final AtomicBoolean DEBUG_ENABLED = new AtomicBoolean(false);
    private static final AtomicBoolean INFO_ENABLED = new AtomicBoolean(true);
    private static final AtomicBoolean WARNING_ENABLED = new AtomicBoolean(true);
    private static final AtomicBoolean ERROR_ENABLED = new AtomicBoolean(true);

    // // Reset
    // public static final String RESET = "\033[0m";  // Text Reset

    // // Regular Colors
    // public static final String BLACK = "\033[0;30m";   // BLACK
    // public static final String RED = "\033[0;31m";     // RED
    // public static final String GREEN = "\033[0;32m";   // GREEN
    // public static final String YELLOW = "\033[0;33m";  // YELLOW
    // public static final String BLUE = "\033[0;34m";    // BLUE
    // public static final String PURPLE = "\033[0;35m";
    // public static final String CYAN = "\033[0;36m";    // CYAN
    // public static final String WHITE = "\033[0;37m";   // WHITE
    // public static final String PINK = "\033[95m"; // Mã màu hồng tạo bằng cách kết hợp màu đỏ và màu xanh dương

    // // Bold
    // public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    // public static final String RED_BOLD = "\033[1;31m";    // RED
    // public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    // public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    // public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    // public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    // public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    // public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // // Underline
    // public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    // public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    // public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    // public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    // public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    // public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    // public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    // public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // // Background
    // public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    // public static final String RED_BACKGROUND = "\033[41m";    // RED
    // public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    // public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    // public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    // public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    // public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    // public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // // High Intensity
    // public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    // public static final String RED_BRIGHT = "\033[0;91m";    // RED
    // public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    // public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    // public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    // public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    // public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    // public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // // Bold High Intensity
    // public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    // public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    // public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    // public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    // public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    // public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    // public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    // public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // // High Intensity backgrounds
    // public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    // public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    // public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    // public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    // public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    // public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    // public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    // public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE

    // === EXISTING METHODS - KEPT UNCHANGED ===
    public static void log(String text) {
        System.out.print(ConsoleLogFormatter.normalize(text));
    }

    public static void logln(String text) {
        System.out.println(ConsoleLogFormatter.normalize(text));
    }

    public static void success(String text) {
        System.out.print(ConsoleLogFormatter.normalize(text));
    }

    public static void successln(String text) {
        System.out.println(ConsoleLogFormatter.normalize(text));
    }

    public static void warning(String text) {
        System.out.print(ConsoleLogFormatter.normalize(text));
    }

    public static void warningln(String text) {
        System.out.println(ConsoleLogFormatter.normalize(text));
    }

    public static void error(String text) {
        System.out.print(ConsoleLogFormatter.normalize(text));
    }

    public static void errorln(String text) {
        System.out.println(ConsoleLogFormatter.normalize(text));
    }

    public static void primary(String text) {
        System.out.print(ConsoleLogFormatter.normalize(text));
    }

    public static void primaryln(String text) {
        System.out.println(ConsoleLogFormatter.normalize(text));
    }

    public static void logException(Class<?> clazz, Exception ex, String... log) {
        try {
            if (log != null && log.length > 0) {
                log(log[0] + "\n");
            }

            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionDetails = sw.toString();

            Logger.warning("Loi tai lop: ");
            Logger.error(clazz.getName());
            Logger.warning(" - tai phuong thuc: ");
            Logger.error(methodName + "\n");
            Logger.warning("Chi tiet loi:\n");
            for (String line : exceptionDetails.split("\n")) {
                Logger.error(line + "\n");
            }
            Logger.log("--------------------------------------------------------\n");
        } catch (Exception e) {
            Logger.error("Khong the ghi chi tiet loi: " + e.getMessage());
        }
    }

    public static void fileLog(String playerName, String string) {
        new Thread(() -> {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy + HH:mm:ss");
                String timeNow = formatter.format(new Date());
                String logEntry = timeNow + " + " + string;
                writeFile("log/" + playerName + "_log.txt", logEntry);
            } catch (IOException e) {
            }
        }).start();
    }

    private static void writeFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
            out.println(content);
        }
    }

    public static void logError(String message) {
        System.err.println("LOI: " + ConsoleLogFormatter.normalize(message));
    }

    public static void logError(String message, Throwable e) {
        System.err.println("LOI: " + ConsoleLogFormatter.normalize(message));
        e.printStackTrace(System.err);
    }

    public static void logInfo(String text) {
        if (INFO_ENABLED.get()) {
            System.out.println("[THONG TIN] " + ConsoleLogFormatter.normalize(text));
        }
    }

    public static void logWarning(String text) {
        if (WARNING_ENABLED.get()) {
            System.out.println("[CANH BAO] " + ConsoleLogFormatter.normalize(text));
        }
    }

    public static void rainbow(String text) {
        System.out.println(ConsoleLogFormatter.normalize(text));
    }

    public static void rainbow1(String text) {
        System.out.println(ConsoleLogFormatter.normalize(text));
    }

    public static void rainbowBlock(String block) {
        String[] lines = block.split("\n");
        for (String line : lines) {
            rainbow(line);
        }
    }
}
