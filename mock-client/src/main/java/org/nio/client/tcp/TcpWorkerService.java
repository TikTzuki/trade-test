package org.nio.client.tcp;

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import com.nio.wallet.grpc.WalletServiceOuterClass.TransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.util.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;


@Slf4j
@Service
@RequiredArgsConstructor
public class TcpWorkerService {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    final WalletTcpClient walletTcpClient;

    public void start(
            List<String> accountIds,
            Long userCount,
            int transPerUser
    ) throws InterruptedException {
        log.info("Starting TCP Transfer");
        if (!executorService.isShutdown())
            shutdown();
        executorService = Executors.newSingleThreadScheduledExecutor();
        Connection connection = walletTcpClient.newConnection(onMessage(userCount.intValue()));
        executorService.scheduleAtFixedRate(() -> {
            log.info("Sending data to server");
            long start = System.currentTimeMillis();
            Flux.fromIterable(accountIds)
                    .skip(accountIds.size() - userCount)
                    .flatMap(accountId -> {
                        String ref = UUID.randomUUID().toString();
                        log.debug("Send {} transaction, ref {}", accountId, ref);
                        try {
                            var b = IOUtils.serializeMessages(
                                    TransferRequest.newBuilder()
                                            .setUserId(accountId)
                                            .setAmount("8".repeat(256))
                                            .setReferenceId(ref)
                                            .build());
//                            var b =
//                                    TransferRequest.newBuilder()
//                                            .setUserId(accountId)
//                                            .setAmount("8".repeat(256))
//                                            .setReferenceId(ref)
//                                            .build().toByteArray();
                            log.debug("Send byte length: {}", b.length);
                            return connection.outbound()
                                    .sendByteArray(Mono.just(b))
                                    .then();
                        } catch (Exception e) {
                            return Flux.error(new RuntimeException(e));
                        }
                    })
                    .doOnComplete(() -> {
                        long now = System.currentTimeMillis();
                        log.info("Send {} transaction, {} - {}, taken: {} ms",
                                userCount * transPerUser,
                                start,
                                now,
                                now - start);
                    })
                    .subscribe();
        }, 0, 1, TimeUnit.SECONDS);
    }

    public BiFunction<? super NettyInbound, ? super NettyOutbound, ? extends Publisher<Void>> onMessage(int userCount) {
        return (inbound, outbound) -> {
            AtomicInteger counter = new AtomicInteger();
            AtomicLong start = new AtomicLong(System.currentTimeMillis());
            return inbound.receive()
                    .flatMap(byteBuf -> {
                        List<TransferResponse> responses = IOUtils.parseByteArray(byteBuf, input -> {
                            try {
                                return TransferResponse.parseDelimitedFrom(input);
                            } catch (IOException e) {
                                log.error("Error parsing response", e);
                            }
                            return null;
                        });

                        log.debug("Received response: {}", responses);
                        counter.getAndAdd(responses.size());

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

    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }
}
