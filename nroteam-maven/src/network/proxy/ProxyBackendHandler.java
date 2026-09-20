package network.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**
 * Backend handler: manages the proxy→backend side of the connection.
 *
 * On channelRead: forwards raw bytes from backend → client (frontend).
 * On channelInactive: closes the frontend channel.
 */
public class ProxyBackendHandler extends ChannelInboundHandlerAdapter {

    private final Channel frontendChannel;

    public ProxyBackendHandler(Channel frontendChannel) {
        this.frontendChannel = frontendChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Forward raw bytes from backend → client
        if (frontendChannel.isActive()) {
            frontendChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // Resume reading from backend when write completes (backpressure)
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        } else {
            if (msg instanceof io.netty.buffer.ByteBuf) {
                ((io.netty.buffer.ByteBuf) msg).release();
            }
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (frontendChannel.isActive()) {
            frontendChannel.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        if (frontendChannel.isActive()) {
            frontendChannel.close();
        }
    }
}
