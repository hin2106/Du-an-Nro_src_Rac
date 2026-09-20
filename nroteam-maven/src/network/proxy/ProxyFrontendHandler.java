package network.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import utils.Logger;

import java.net.InetSocketAddress;

/** Relays client bytes and sends a trusted PROXY-v1 header to the backend. */
public final class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {
    private final String backendHost;
    private final int backendPort;
    private volatile Channel backendChannel;

    public ProxyFrontendHandler(String backendHost, int backendPort) {
        this.backendHost = backendHost;
        this.backendPort = backendPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel frontendChannel = ctx.channel();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(frontendChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ProxyBackendHandler(frontendChannel));

        bootstrap.connect(backendHost, backendPort).addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                Logger.logWarning("[Proxy] Backend unavailable for " + frontendChannel.remoteAddress());
                frontendChannel.close();
                return;
            }
            backendChannel = future.channel();
            ByteBuf header = Unpooled.copiedBuffer(createProxyHeader(frontendChannel), CharsetUtil.US_ASCII);
            backendChannel.writeAndFlush(header).addListener(headerFuture -> {
                if (headerFuture.isSuccess()) {
                    DDoSFilterHandler.transferLease(frontendChannel);
                    backendChannel.read();
                    frontendChannel.read();
                } else {
                    frontendChannel.close();
                    backendChannel.close();
                }
            });
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (backendChannel != null && backendChannel.isActive()) {
            backendChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        } else {
            if (msg instanceof ByteBuf) {
                ((ByteBuf) msg).release();
            }
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (backendChannel != null && backendChannel.isActive()) {
            backendChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        if (backendChannel != null && backendChannel.isActive()) {
            backendChannel.close();
        }
    }

    private static String createProxyHeader(Channel channel) {
        InetSocketAddress source = (InetSocketAddress) channel.remoteAddress();
        InetSocketAddress destination = (InetSocketAddress) channel.localAddress();
        String family = source.getAddress().getAddress().length == 16 ? "TCP6" : "TCP4";
        return "PROXY " + family + " " + source.getAddress().getHostAddress() + " "
                + destination.getAddress().getHostAddress() + " " + source.getPort() + " "
                + destination.getPort() + "\r\n";
    }
}
