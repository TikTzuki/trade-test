package org.nio.client.grpc

import com.nio.wallet.grpc.ReactorWalletServiceGrpc.ReactorWalletServiceStub
import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest
import mu.KLogging
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toFlux
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


@Service
class GrpcWorkerService @Autowired constructor(
    @GrpcClient("bank-account-service")
    val stub: ReactorWalletServiceStub
) {
    var executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun startRun(
        accountIds: List<String>,
        userCount: Long,
        transPerUser: Int
    ) {
        if (!executorService.isShutdown)
            stopRun()
        executorService = Executors.newSingleThreadScheduledExecutor()
        executorService.scheduleAtFixedRate({
            val start = System.currentTimeMillis()
            accountIds.toFlux()
                .skip(accountIds.size - userCount)
                .flatMap { accountId ->
                    return@flatMap stub.transfer(
                        TransferRequest.newBuilder()
                            .setUserId(accountId)
                            .setAmount("1")
                            .build()
                    )
                }.subscribe({}, {}, {
                    val now = System.currentTimeMillis()
                    logger.info { "Transfer $userCount transaction, $start - $now , taken: ${now - start} ms" }
                })
        }, 0, 1, TimeUnit.SECONDS)
    }

    fun stopRun() {
        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.MINUTES)
    }

    companion object : KLogging()
}