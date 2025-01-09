package org.nio.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import mu.KLogging
import org.springframework.stereotype.Component
import reactor.netty.tcp.TcpServer

class TcpServerBootstrap {
    val bossGroup: EventLoopGroup = NioEventLoopGroup()
    val workerGroup: EventLoopGroup = NioEventLoopGroup()

    @PostConstruct
    @Throws(InterruptedException::class)
    fun start() {
        val port = 3000
        logger.info("Starting server at: $port")

        val b = ServerBootstrap()
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(SimpleTCPChannelInitializer())
            .childOption(ChannelOption.SO_KEEPALIVE, true)

        // Bind and start to accept incoming connections.
        val f: ChannelFuture = b.bind(port).sync()
        if (f.isSuccess) logger.info("Server started successfully")
//        f.channel().closeFuture().sync()
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Stopping server")
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
        logger.info("Server stopped gracefully")
    }



    companion object : KLogging()
}