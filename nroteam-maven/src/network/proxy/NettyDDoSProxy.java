package network.proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import utils.Logger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Netty TCP Proxy — DDoS filter layer.
 *
 * Sits in front of the existing NIO Network.java server:
 *   Client ──► [NettyDDoSProxy :publicPort] ──filter──► [Network.java :backendPort on 127.0.0.1]
 *
 * Does NOT understand the NRO protocol — just filters at TCP/connection level
 * and forwards raw bytes transparently. Zero changes to game logic.
 */
public class NettyDDoSProxy {

    private final int listenPort;
    private final String backendHost;
    private final int backendPort;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    private final CountDownLatch startupLatch = new CountDownLatch(1);
    private volatile Throwable startupFailure;

    public NettyDDoSProxy(int listenPort, String backendHost, int backendPort) {
        this.listenPort = listenPort;
        this.backendHost = backendHost;
        this.backendPort = backendPort;
    }

    /**
     * Start proxy in a daemon thread. Non-blocking.
     */
    public void startAsync() {
        Thread t = new Thread(() -> {
            try {
                start();
            } catch (Exception e) {
                startupFailure = e;
                startupLatch.countDown();
                Logger.logError("NettyDDoSProxy start failed", e);
            }
        }, "NettyDDoSProxy");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Start proxy (blocking until server channel closes).
     */
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        int workerThreads = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
        workerGroup = new NioEventLoopGroup(workerThreads);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ProxyInitializer(backendHost, backendPort))
                .option(ChannelOption.SO_BACKLOG, 2048)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                        new WriteBufferWaterMark(64 * 1024, 256 * 1024))
                .childOption(ChannelOption.AUTO_READ, false); // manual read control for backpressure

            ChannelFuture f = b.bind(listenPort).sync();
            serverChannel = f.channel();
            startupLatch.countDown();
            Logger.logInfo("NettyDDoSProxy started on port " + listenPort
                    + " → forwarding to " + backendHost + ":" + backendPort);

            f.channel().closeFuture().sync();
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        if (!stopping.compareAndSet(false, true)) return;
        Logger.logInfo("NettyDDoSProxy shutting down...");
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public boolean awaitStarted(long timeout, TimeUnit unit) throws InterruptedException {
        return startupLatch.await(timeout, unit) && serverChannel != null
                && serverChannel.isActive() && startupFailure == null;
    }
}
