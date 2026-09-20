package network.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Pipeline for each incoming client connection:
 *   IdleStateHandler → DDoSFilterHandler → ProxyFrontendHandler
 *
 * - IdleStateHandler: kill connections idle for too long (zombie/slowloris defense)
 * - DDoSFilterHandler: per-IP connection limit, rate limit, blacklist
 * - ProxyFrontendHandler: connect to backend and relay bytes bidirectionally
 */
public class ProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String backendHost;
    private final int backendPort;

    public ProxyInitializer(String backendHost, int backendPort) {
        this.backendHost = backendHost;
        this.backendPort = backendPort;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // Pre-auth sessions are closed sooner by SessionTimeoutChecker. This
        // longer transport timeout avoids kicking legitimate idle players.
        p.addLast("idle", new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS));

        // DDoS filter: IP limits, rate limits, blacklist
        p.addLast("ddosFilter", new DDoSFilterHandler());

        // Bi-directional byte relay to backend
        p.addLast("proxy", new ProxyFrontendHandler(backendHost, backendPort));
    }
}
