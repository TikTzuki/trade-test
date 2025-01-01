package org.tik

import io.grpc.netty.NettyServerBuilder

fun main(args: Array<String>) {
    val port = if (args.isNotEmpty()) Integer.parseInt(args[0]) else 8080

    val server = NettyServerBuilder
        .forPort(port)
        .build();

    server.start();

    server.awaitTermination();
}
