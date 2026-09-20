package test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP Load Test — mô phỏng nhiều client kết nối đồng thời.
 * Dùng để test DDoS filter + server capacity.
 *
 * Cách chạy: java test.LoadTest [host] [port] [connections] [holdSeconds]
 * Mặc định: localhost:14445, 500 connections, giữ 30 giây
 */
public class LoadTest {

    private static final AtomicInteger connected = new AtomicInteger(0);
    private static final AtomicInteger failed = new AtomicInteger(0);
    private static final AtomicInteger rejected = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 14445;
        int totalConns = args.length > 2 ? Integer.parseInt(args[2]) : 500;
        int holdSeconds = args.length > 3 ? Integer.parseInt(args[3]) : 30;

        System.out.println("=== NRO TCP Load Test ===");
        System.out.println("Target: " + host + ":" + port);
        System.out.println("Connections: " + totalConns);
        System.out.println("Hold time: " + holdSeconds + "s");
        System.out.println("=========================\n");

        ExecutorService pool = Executors.newFixedThreadPool(
                Math.min(totalConns, 200)); // max 200 threads
        CopyOnWriteArrayList<Socket> sockets = new CopyOnWriteArrayList<>();

        long startTime = System.currentTimeMillis();

        // Phase 1: Mở connections
        System.out.println("[Phase 1] Opening " + totalConns + " connections...");
        CountDownLatch latch = new CountDownLatch(totalConns);

        for (int i = 0; i < totalConns; i++) {
            final int id = i;
            pool.submit(() -> {
                try {
                    Socket s = new Socket();
                    s.connect(new java.net.InetSocketAddress(host, port), 5000);
                    s.setSoTimeout(10000);
                    sockets.add(s);
                    int c = connected.incrementAndGet();
                    if (c % 50 == 0) {
                        System.out.println("  Connected: " + c + "/" + totalConns
                                + " (failed: " + failed.get() + ", rejected: " + rejected.get() + ")");
                    }
                } catch (java.net.ConnectException e) {
                    rejected.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });

            // Stagger: 2ms giữa mỗi connection để không flood quá nhanh
            if (i % 10 == 0) Thread.sleep(2);
        }

        latch.await(60, TimeUnit.SECONDS);
        long connectTime = System.currentTimeMillis() - startTime;

        System.out.println("\n[Phase 1 Complete]");
        System.out.println("  Connected: " + connected.get());
        System.out.println("  Rejected:  " + rejected.get());
        System.out.println("  Failed:    " + failed.get());
        System.out.println("  Time:      " + connectTime + "ms");

        // Phase 2: Giữ connections, đo stability
        System.out.println("\n[Phase 2] Holding " + sockets.size() + " connections for " + holdSeconds + "s...");
        AtomicInteger droppedDuringHold = new AtomicInteger(0);

        for (int sec = 0; sec < holdSeconds; sec++) {
            Thread.sleep(1000);

            // Kiểm tra bao nhiêu connection đã bị server đóng
            int stillAlive = 0;
            for (Socket s : sockets) {
                if (s.isConnected() && !s.isClosed()) {
                    try {
                        // Gửi 1 byte kiểm tra connection còn sống
                        s.getOutputStream().write(0);
                        stillAlive++;
                    } catch (Exception e) {
                        droppedDuringHold.incrementAndGet();
                    }
                }
            }

            if ((sec + 1) % 5 == 0) {
                System.out.println("  [" + (sec + 1) + "s] Alive: " + stillAlive
                        + " / Dropped: " + droppedDuringHold.get());
            }
        }

        // Phase 3: Cleanup
        System.out.println("\n[Phase 3] Closing connections...");
        for (Socket s : sockets) {
            try { s.close(); } catch (Exception ignored) {}
        }
        pool.shutdownNow();

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("\n=== KẾT QUẢ ===");
        System.out.println("Tổng thời gian:     " + totalTime + "ms");
        System.out.println("Connections thành công: " + connected.get() + "/" + totalConns);
        System.out.println("Bị DDoS filter chặn:   " + rejected.get());
        System.out.println("Lỗi khác:             " + failed.get());
        System.out.println("Bị drop trong hold:    " + droppedDuringHold.get());
        System.out.println(
                connected.get() >= totalConns * 0.8
                        ? ">> SERVER ĐỦ TẢI <<"
                        : ">> SERVER QUÁ TẢI — chỉ chấp nhận " + (connected.get() * 100 / totalConns) + "% <<"
        );
    }
}
