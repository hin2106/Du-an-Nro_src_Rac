package test;

import utils.ConsoleLogFormatter;

public final class ConsoleLogFormatterTest {
    public static void main(String[] args) {
        assertEquals("Loi khi khoi dong may chu", ConsoleLogFormatter.normalize("Lỗi khi khởi động máy chủ"));
        assertEquals("[THONG TIN] proxy chong DDoS da khoi dong thanh cong",
                ConsoleLogFormatter.normalize("[INFO] DDoS proxy started successfully"));
        assertEquals("PlayerManager", ConsoleLogFormatter.normalize("PlayerManager"));
        assertEquals("Loi tai lop: network.Session - tai phuong thuc: run",
                ConsoleLogFormatter.normalize("Error in class: network.Session - in method: run"));
        assertEquals("ConsignShopManager: bat dau luu du lieu cua hang ky gui (3 vat pham)...",
                ConsoleLogFormatter.normalize("ConsignShopManager: Starting to save ConsignShop data (3 items)..."));
        assertEquals("loi khi tai KOL nhiem vu tien trinh cho nguoi choi 10",
                ConsoleLogFormatter.normalize("Error loading KOL task progress for player 10"));
        System.out.println("ConsoleLogFormatterTest PASSED");
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected [" + expected + "] but got [" + actual + "]");
        }
    }
}
