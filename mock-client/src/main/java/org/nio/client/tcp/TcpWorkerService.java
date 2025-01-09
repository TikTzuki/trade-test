package org.nio.client.tcp;

import com.nio.wallet.grpc.WalletServiceOuterClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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
        Connection connection = walletTcpClient.newConnection();
        Connection connection2 = walletTcpClient.newConnection();
        executorService.scheduleAtFixedRate(() -> {
            log.info("Sending data to server");
            long start = System.currentTimeMillis();
            Flux.fromIterable(accountIds)
                    .skip(accountIds.size() - userCount)
                    .flatMap(accountId -> {
                        return connection2.outbound()
                                .sendByteArray(Mono.just(WalletServiceOuterClass.TransferRequest.newBuilder()
                                        .setUserId(accountId)
                                        .setAmount("1")
                                        .build()
                                        .toByteArray()))
                                .then().then(Mono.fromCallable(() -> accountId));
                    })
                    .flatMap(accountId -> {
                        return connection.outbound()
                                .sendByteArray(Mono.just(WalletServiceOuterClass.TransferRequest.newBuilder()
                                        .setUserId(accountId)
                                        .setAmount("1")
                                        .build()
                                        .toByteArray()))
                                ;
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

    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }
}
