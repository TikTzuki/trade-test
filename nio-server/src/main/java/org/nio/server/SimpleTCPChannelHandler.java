package org.nio.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("{} {}", ctx.channel().remoteAddress(), "Channel Active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        log.info("{} {}", ctx.channel().remoteAddress(), s);
        ctx.channel().writeAndFlush("Thanks\n");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("{} {}", ctx.channel().remoteAddress(), "Channel Inactive");
    }
}