package network.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import network.AntiDDoSEngine;

import java.net.InetSocketAddress;

/** Cheap connection gate that runs before a backend connection is created. */
public final class DDoSFilterHandler extends ChannelInboundHandlerAdapter {
    private static final int MAX_INBOUND_BYTES_PER_SECOND = 4 * 1024 * 1024;
    private static final AttributeKey<Boolean> LEASE_TRANSFERRED =
            AttributeKey.valueOf("ddosLeaseTransferred");

    private String clientIp;
    private boolean admitted;
    private long byteWindowSecond;
    private int bytesInWindow;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientIp = extractIp(ctx);
        if (!AntiDDoSEngine.gI().checkConnectionAllowed(clientIp)) {
            ctx.close();
            return;
        }
        admitted = true;
        byteWindowSecond = System.currentTimeMillis() / 1000L;
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (admitted && !Boolean.TRUE.equals(ctx.channel().attr(LEASE_TRANSFERRED).get())) {
            admitted = false;
            AntiDDoSEngine.gI().onConnectionClosed(clientIp, false);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf) msg;
            long second = System.currentTimeMillis() / 1000L;
            if (second != byteWindowSecond) {
                byteWindowSecond = second;
                bytesInWindow = 0;
            }
            bytesInWindow += buffer.readableBytes();
            if (bytesInWindow > MAX_INBOUND_BYTES_PER_SECOND) {
                buffer.release();
                AntiDDoSEngine.gI().addReputationPublic(clientIp, 100,
                        "Inbound bandwidth exceeded 4 MiB/s");
                ctx.close();
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.close();
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    private static String extractIp(ChannelHandlerContext ctx) {
        try {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            return address.getAddress().getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }

    static void transferLease(io.netty.channel.Channel channel) {
        channel.attr(LEASE_TRANSFERRED).set(Boolean.TRUE);
    }
}
