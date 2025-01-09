package org.nio.client.tcp;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

@Slf4j
@Component
public class WalletTcpClient {
    Connection connection;

    public Connection newConnection() {
//        if (connection != null)
//            connection.onDispose().block();
        connection = TcpClient.create()
                .host("localhost")
                .port(3000)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .doOnConnected(conn -> conn.addHandlerLast(new IdleStateHandler(60, 30, 0)))
                .resolver(spec -> spec.queryTimeout(Duration.ofMillis(500)))
                .wiretap("reactor.netty.tcp.client", LogLevel.DEBUG)
                .metrics(true)
                .doOnResolveError((conn, error) -> System.err.println("Connection error: " + error.getMessage()))
                .doOnDisconnected(conn -> System.out.println("Disconnected from server."))
                .handle(simpleHandler())
                .connect()
                .doOnSuccess(conn -> System.out.println("Connection established!"))
                .doOnError(error -> System.err.println("Connection failed: " + error.getMessage()))
                .block();
        return connection;
    }

    public BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> simpleHandler() {
        return (inbound, outbound) -> {
            AtomicInteger counter = new AtomicInteger();
            AtomicLong start = new AtomicLong();
            int userCount = 35_000;
            return inbound.receive()
                    .flatMap(byteBuf -> {
                        counter.getAndIncrement();
                        if (counter.get() == userCount) {
                            long now = System.currentTimeMillis();
                            log.info("Receive {} resp, {} - {}, taken: {} ms",
                                    userCount,
                                    start.get(),
                                    now,
                                    now - start.get());
                            counter.set(0);
                            start.set(System.currentTimeMillis());
                        }
                        return Mono.empty();
                    });
        };
    }
}
