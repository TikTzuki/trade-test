package org.nio.endpoint;

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import com.nio.wallet.grpc.WalletServiceOuterClass.TransferResponse;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.nio.server.GracefulShutdown;
import org.nio.util.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

@Slf4j
@Component
public class TcpServerEndpoint {
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
                .handle(onMessage());
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

    public BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> onMessage() {
        return (inbound, outbound) -> {
            return inbound.receive()
                    .flatMap(byteBuf -> {
                        List<TransferRequest> requests = IOUtils.parseByteArray(byteBuf, (inputStream) -> {
                            try {
                                return TransferRequest.parseDelimitedFrom(inputStream);
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
                        return Flux.fromIterable(requests);
                    })
                    .flatMap(data -> {
                        log.info("Received data: {}", data.toString());
                        // handle main logic
                        String transactionId = UUID.randomUUID().toString();

                        try {
                            return outbound.sendByteArray(Mono.just(IOUtils.serializeMessages(
                                    TransferResponse.newBuilder()
                                            .setReferenceId(data.getReferenceId())
                                            .setTransactionId(transactionId)
                                            .build())
                            )).then();
                        } catch (IOException e) {
                            return Flux.error(new RuntimeException(e));
                        }
                    });
        };
    }


}
