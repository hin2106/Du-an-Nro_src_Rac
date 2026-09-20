package test;

import network.proxy.NettyDDoSProxy;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Local-only regression test for real-IP forwarding and byte relay. */
public final class ProxyIntegrationTest {
    public static void main(String[] args) throws Exception {
        try (ServerSocket backend = new ServerSocket(0, 20, java.net.InetAddress.getLoopbackAddress());
             ServerSocket portReservation = new ServerSocket(0)) {
            int proxyPort = portReservation.getLocalPort();
            portReservation.close();
            NettyDDoSProxy proxy = new NettyDDoSProxy(proxyPort, "127.0.0.1", backend.getLocalPort());

            CompletableFuture<Void> backendCheck = CompletableFuture.runAsync(() -> {
                try (Socket accepted = backend.accept()) {
                    accepted.setSoTimeout(3_000);
                    String header = readLine(accepted.getInputStream());
                    if (!header.startsWith("PROXY TCP4 127.0.0.1 ")) {
                        throw new AssertionError("Unexpected proxy header: " + header);
                    }
                    int value = accepted.getInputStream().read();
                    if (value != 0x5A) throw new AssertionError("Client byte was not relayed");
                    accepted.getOutputStream().write(0x6B);
                    accepted.getOutputStream().flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            proxy.startAsync();
            if (!proxy.awaitStarted(3, TimeUnit.SECONDS)) {
                throw new AssertionError("Proxy failed to start");
            }
            try (Socket client = connectWithRetry(proxyPort)) {
                client.setSoTimeout(3_000);
                client.getOutputStream().write(0x5A);
                client.getOutputStream().flush();
                if (client.getInputStream().read() != 0x6B) {
                    throw new AssertionError("Backend byte was not relayed");
                }
            } finally {
                proxy.shutdown();
            }
            backendCheck.get(5, TimeUnit.SECONDS);
            System.out.println("ProxyIntegrationTest PASSED");
        }
    }

    private static Socket connectWithRetry(int port) throws Exception {
        Exception last = null;
        for (int i = 0; i < 30; i++) {
            try {
                return new Socket("127.0.0.1", port);
            } catch (Exception e) {
                last = e;
                Thread.sleep(50);
            }
        }
        throw last;
    }

    private static String readLine(InputStream input) throws Exception {
        byte[] data = new byte[108];
        int length = 0;
        while (length < data.length) {
            int value = input.read();
            if (value < 0) break;
            data[length++] = (byte)value;
            if (length >= 2 && data[length - 2] == '\r' && data[length - 1] == '\n') break;
        }
        return new String(data, 0, length, StandardCharsets.US_ASCII).trim();
    }
}
