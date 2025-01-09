package org.nio.server;

import com.google.protobuf.InvalidProtocolBufferException;
import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.function.BiFunction;

@Slf4j
@Component
public class TcpServerConfig {
    DisposableServer server;

    @PostConstruct
    public void start() {
        int port = 3000;
        TcpServer tcpServer = TcpServer.create()
                .metrics(true)
                .wiretap(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .port(port)
                .handle(simpleHandler());
        // Eager Initialization
        tcpServer.warmup().block();

        server = tcpServer.bindNow();
    }

    @PreDestroy
    public void stop() {
        new GracefulShutdown(() -> server)
                .shutDownGracefully(result -> {
                });
    }

    public BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> simpleHandler() {
        return (inbound, outbound) -> {
            return inbound.receive()
                    .map(byteBuf -> {
                        TransferRequest data = null;
                        try {
                            data = TransferRequest.parseFrom(toByteArrayWithoutConsuming(byteBuf));
                            log.info("Received data: {}", data.toString());
                        } catch (InvalidProtocolBufferException e) {
                            return outbound.sendString(Mono.just("error parse")).then();
                        }
                        return outbound.sendString(Mono.just("success")).then();
                    })
                    .flatMap(o -> o);
//                    .onErrorMap(throwable -> {
//                        log.error("Error", throwable);
//                        return throwable;
//                    });
        };
    }
    public byte[] toByteArrayWithoutConsuming(ByteBuf byteBuf) {
        byte[] array = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(array); // Make a copy of the readable part and read into the array
//        byteBuf.release();
        return array;
    }
}
