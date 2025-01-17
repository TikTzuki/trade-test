package org.nio.client.grpc

import com.nio.wallet.grpc.ReactorWalletServiceGrpc.ReactorWalletServiceStub
import com.nio.wallet.grpc.WalletServiceOuterClass.ErrorDetail
import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.ProtoUtils
import mu.KLogging
import net.devh.boot.grpc.client.inject.GrpcClient
import org.nio.client.utils.genReferenceId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


@Service
class GrpcWorkerService @Autowired constructor(
    @GrpcClient("bank-account-service")
    val stub: ReactorWalletServiceStub
) {
    var executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    fun startRun(
        accountIds: List<String>,
        parallel: Int,
        userCount: Int,
        transPerUser: Int,
        enableStream: Boolean
    ) {
        if (!executorService.isShutdown)
            stopRun()
        executorService = Executors.newSingleThreadScheduledExecutor()
        executorService.scheduleAtFixedRate({
            if (enableStream)
                transferStream(accountIds, parallel, userCount, transPerUser)
//            else
//                transfer(accountIds, userCount, transPerUser)

        }, 0, 1, TimeUnit.SECONDS)
    }

//    fun transfer(
//        accountIds: List<String>,
//        userCount: Int,
//        transPerUser: Int
//    ) {
//        val start = System.currentTimeMillis()
//        accountIds.toFlux()
//            .skip(accountIds.size - userCount)
//            .flatMap { accountId ->
//                return@flatMap stub.transfer(
//                    TransferRequest.newBuilder()
//                        .setUserId(accountId)
//                        .setAmount("1")
//                        .setReferenceId(genReferenceId(accountId))
//                        .build()
//                )
//            }
//            .doOnComplete { logger.info { "Transfer ${userCount * transPerUser} transaction, taken: ${System.currentTimeMillis() - start} ms" } }
//            .subscribe()
//    }

    fun transferStream(
        accountIds: List<String>,
        parallel: Int,
        userCount: Int,
        transPerUser: Int
    ) {
        assert(userCount > accountIds.size) { "User count must be greater than account count" }

        val start = System.currentTimeMillis()
        val accountIds = accountIds.subList(0, userCount);
        var counter = 0
        accountIds.toFlux()
            .bufferTimeout(ceil(accountIds.size / parallel.toDouble()).toInt(), Duration.ofMillis(1))
            .parallel(parallel)
            .runOn(Schedulers.parallel())
            .flatMap { userAccountIds ->
                stub.transferStream(accountIdsToTransferRequest(userAccountIds, transPerUser).doOnNext {
                    logger.debug { it }
                    counter++
                })
                    .onErrorComplete { err ->
                        if (err is StatusRuntimeException) {
                            val error = err.trailers?.get(ProtoUtils.keyForProto(ErrorDetail.getDefaultInstance()))
                            logger.error("Map error response: ${error?.code} ${error?.message} ${error}")
                        } else
                            logger.error { "Unhandled error $err" }
                        true
                    }
            }
            .doOnComplete { logger.info { "Transfer $counter transaction, taken: ${System.currentTimeMillis() - start} ms" } }
            .subscribe { }
    }

    fun accountIdsToTransferRequest(accountIds: List<String>, transPerUser: Int): Flux<TransferRequest> {
        return accountIds.toFlux()
            .flatMap { accountId ->
                Mono.just(
                    TransferRequest.newBuilder()
                        .setUserId(accountId)
                        .setAmount("1")
                )
                    .repeat(transPerUser.toLong())
                    .map {
                        it.setReferenceId(genReferenceId(accountId))
                        return@map it.build()
                    }
            }
    }

    fun stopRun() {
        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.MINUTES)
    }

    companion object : KLogging()
}