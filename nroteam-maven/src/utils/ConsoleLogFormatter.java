package utils;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Chuan hoa moi dong console ve tieng Viet khong dau. */
public final class ConsoleLogFormatter {
    private static final Map<Pattern, String> TRANSLATIONS = new LinkedHashMap<>();
    private static final Map<Pattern, String> WORD_TRANSLATIONS = new LinkedHashMap<>();
    private static final List<Map.Entry<Pattern, String>> ORDERED_TRANSLATIONS;

    static {
        add("\\[INFO]", "[THONG TIN]");
        add("\\[WARNING]", "[CANH BAO]");
        add("\\[ERROR]", "[LOI]");
        add("ERROR:", "LOI:");
        add("Error in class", "Loi tai lop");
        add("in method", "tai phuong thuc");
        add("Error details", "Chi tiet loi");
        add("Failed to log exception", "Khong the ghi chi tiet loi");
        add("successfully", "thanh cong");
        add("failed to", "khong the");
        add("failed", "that bai");
        add("failure", "that bai");
        add("starting", "dang khoi dong");
        add("started", "da khoi dong");
        add("stopping", "dang dung");
        add("stopped", "da dung");
        add("shutting down", "dang tat");
        add("initialized", "da khoi tao");
        add("initialize", "khoi tao");
        add("loaded", "da tai");
        add("loading", "dang tai");
        add("saved", "da luu");
        add("saving", "dang luu");
        add("deleted", "da xoa");
        add("connection rejected", "tu choi ket noi");
        add("connection rate limit", "gioi han toc do ket noi");
        add("rate limit", "gioi han toc do");
        add("max connections per IP", "vuot gioi han ket noi moi IP");
        add("server at max capacity", "may chu da dat gioi han tai");
        add("backend unavailable", "backend khong san sang");
        add("backend connect", "ket noi backend");
        add("did not start", "khong khoi dong duoc");
        add("invalid backend port", "cong backend khong hop le");
        add("forwarding to", "chuyen tiep den");
        add("binding server to all interfaces", "gan may chu vao tat ca giao dien mang");
        add("binding server to IP", "gan may chu vao IP");
        add("server initialized and listening on", "may chu da khoi tao va dang lang nghe tai");
        add("network socket started on port", "socket mang da khoi dong tai cong");
        add("DDoS proxy started on port", "proxy chong DDoS da khoi dong tai cong");
        add("DDoS proxy stopped", "proxy chong DDoS da dung");
        add("DDoS proxy", "proxy chong DDoS");
        add("all interfaces", "tat ca giao dien mang");
        add("on port", "tai cong");
        add("database initialized", "co so du lieu da khoi tao");
        add("database configuration", "cau hinh co so du lieu");
        add("server configuration", "cau hinh may chu");
        add("from properties file", "tu tep cau hinh properties");
        add("properties file", "tep cau hinh properties");
        add("shutdown requested", "da yeu cau dung");
        add("closing DataSource", "dong nguon du lieu");
        add("loaded file properties", "tai tep properties");
        add("ConsignShop data", "du lieu cua hang ky gui");
        add("starting to save", "bat dau luu");
        add("transaction rolled back", "da hoan tac giao dich");
        add("rollback transaction", "hoan tac giao dich");
        add("close database connection", "dong ket noi co so du lieu");
        add("notifying seller", "dang thong bao cho nguoi ban");
        add("was sold", "da duoc ban");
        add("sold item", "vat pham da ban");
        add("claimed money", "da nhan tien");
        add("claim money", "nhan tien");
        add("remove item", "xoa vat pham");
        add("opening consign shop", "dang mo cua hang ky gui");
        add("message sent", "da gui goi tin");
        add("exception details", "chi tiet ngoai le");
        add("metrics reset", "da dat lai thong so");
        add("scale up", "tang quy mo");
        add("resource send rejected", "tu choi gui tai nguyen");
        add("server overloaded", "may chu qua tai");
        add("sending resource", "gui tai nguyen");
        add("version resource", "phien ban tai nguyen");
        add("batch inserted", "da them theo lo");
        add("transaction records", "ban ghi giao dich");
        add("old transaction records", "ban ghi giao dich cu");
        add("transaction history statistics", "thong ke lich su giao dich");
        add("total records", "tong ban ghi");
        add("oldest record", "ban ghi cu nhat");
        add("newest record", "ban ghi moi nhat");
        add("unique players", "nguoi choi khong trung lap");
        add("begin kick out session", "bat dau ngat cac phien ket noi");
        add("fetching top players from database", "dang lay bang xep hang tu co so du lieu");
        add("top players cache", "bo nho dem bang xep hang");
        add("connection stats", "thong ke ket noi");
        add("total sessions", "tong phien ket noi");
        add("logged in", "da dang nhap");
        add("not logged in", "chua dang nhap");
        add("online players", "nguoi choi truc tuyen");
        add("available connection slots", "so ket noi con trong");
        add("no record found", "khong tim thay ban ghi");
        add("claim reward", "nhan thuong");
        add("unclaimed VIP tasks", "nhiem vu VIP chua nhan");
        add("double check", "kiem tra trung lap");
        add("kicking old session", "dang ngat phien ket noi cu");
        add("top rewards", "phan thuong xep hang");
        add("all bosses have been reset", "da dat lai tat ca boss");
        add("all bosses revived", "tat ca boss da hoi sinh");
        add("cannot set default zone", "khong the dat khu mac dinh");
        add("data inserted", "da them du lieu");
        add("optimized query", "truy van toi uu");
        add("optimization", "toi uu hoa");
        add("falling back to original method", "chuyen ve phuong thuc goc");
        add("IN clause", "menh de IN");
        add("exceeded for IP", "bi vuot qua tai IP");
        add("closing resources", "dong tai nguyen");
        add("error loading", "loi khi tai");
        add("error closing", "loi khi dong");
        add("error saving", "loi khi luu");
        add("error deleting", "loi khi xoa");
        add("error parsing", "loi khi phan tich");
        add("error sending", "loi khi gui");
        add("error creating", "loi khi tao");
        add("error getting", "loi khi lay");
        add("error increasing", "loi khi tang");
        add("error claiming", "loi khi nhan");
        add("loading", "dang tai");
        add("parsing", "phan tich");
        add("sending", "dang gui");
        add("creating", "dang tao");
        add("getting", "dang lay");
        add("saving", "dang luu");
        add("deleting", "dang xoa");
        add("increasing", "dang tang");
        add("claiming", "dang nhan");
        add("closing", "dang dong");
        add("load success", "tai thanh cong");
        add("cleanup", "don dep");
        add("timeout", "qua thoi gian");
        add("online", "truc tuyen");
        addWord("players", "nguoi choi");
        addWord("player", "nguoi choi");
        addWord("sessions", "phien ket noi");
        addWord("session", "phien ket noi");
        addWord("connections", "ket noi");
        addWord("connection", "ket noi");
        addWord("items", "vat pham");
        addWord("item", "vat pham");
        addWord("resources", "tai nguyen");
        addWord("resource", "tai nguyen");
        addWord("records", "ban ghi");
        addWord("record", "ban ghi");
        addWord("tasks", "nhiem vu");
        addWord("task", "nhiem vu");
        addWord("rewards", "phan thuong");
        addWord("reward", "phan thuong");
        addWord("seller", "nguoi ban");
        addWord("shops", "cua hang");
        addWord("shop", "cua hang");
        addWord("server", "may chu");
        addWord("database", "co so du lieu");
        addWord("data", "du lieu");
        addWord("file", "tep");
        addWord("count", "so luong");
        addWord("total", "tong");
        addWord("available", "con trong");
        addWord("current", "hien tai");
        addWord("default", "mac dinh");
        addWord("original", "goc");
        addWord("message", "goi tin");
        addWord("command", "lenh");
        addWord("method", "phuong thuc");
        addWord("save", "luu");
        addWord("load", "tai");
        addWord("close", "dong");
        addWord("open", "mo");
        addWord("reset", "dat lai");
        addWord("update", "cap nhat");
        addWord("updated", "da cap nhat");
        addWord("insert", "them");
        addWord("delete", "xoa");
        addWord("remove", "xoa");
        addWord("send", "gui");
        addWord("fetch", "lay");
        addWord("parse", "phan tich");
        addWord("claim", "nhan");
        addWord("create", "tao");
        addWord("warning", "canh bao");
        addWord("error", "loi");
        addWord("exceeded", "vuot gioi han");
        addWord("progress", "tien trinh");
        addWord("for", "cho");
        ORDERED_TRANSLATIONS = new ArrayList<>(TRANSLATIONS.entrySet());
        ORDERED_TRANSLATIONS.sort(Comparator.comparingInt(
                (Map.Entry<Pattern, String> entry) -> entry.getKey().pattern().length()).reversed());
    }

    private ConsoleLogFormatter() {
    }

    private static void add(String englishRegex, String vietnamese) {
        TRANSLATIONS.put(Pattern.compile("(?<![A-Za-z])(?:" + englishRegex + ")(?![A-Za-z])",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), vietnamese);
    }

    private static void addWord(String englishWord, String vietnamese) {
        WORD_TRANSLATIONS.put(Pattern.compile("(?<![A-Za-z0-9_.$])" + Pattern.quote(englishWord)
                + "(?![A-Za-z0-9_$])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), vietnamese);
    }

    public static String normalize(String input) {
        if (input == null || input.isEmpty()) return input;
        String result = input;
        for (Map.Entry<Pattern, String> entry : ORDERED_TRANSLATIONS) {
            result = entry.getKey().matcher(result)
                    .replaceAll(Matcher.quoteReplacement(entry.getValue()));
        }
        for (Map.Entry<Pattern, String> entry : WORD_TRANSLATIONS.entrySet()) {
            result = entry.getKey().matcher(result)
                    .replaceAll(Matcher.quoteReplacement(entry.getValue()));
        }
        result = result.replace('Đ', 'D').replace('đ', 'd');
        result = Normalizer.normalize(result, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return result;
    }
}
