package org.nio.client.grpc

import com.google.protobuf.Empty
import com.nio.wallet.grpc.ReactorWalletServiceGrpc.ReactorWalletServiceStub
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountRequest
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountResponse
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.core.task.VirtualThreadTaskExecutor
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

val TRANSFER_RANGE = listOf("1", "5", "10")

@RestController
@RequestMapping("/api/v1/wallet")
class TestController @Autowired constructor(
    @GrpcClient("bank-account-service")
    val stub: ReactorWalletServiceStub,
    val workerService: GrpcWorkerService
) {
    @PostMapping("/ping")
    fun test(): String {
//        val stub = ReactorBankAccountServiceGrpc.newReactorStub(null)
        val f = Flux.generate({ 0 }, { state, sink ->
            if (state < 10) {
                sink.next(Empty.getDefaultInstance())
                state + 1
            } else {
                sink.complete()
                state
            }
        })
        stub.ping(f).subscribe { println(it) }
        return "Hello, World!"
    }

    @PostMapping("/bulk-insert-accounts")
    fun bulkInsertAccounts(@RequestParam number: Int): ResponseEntity<Flux<DataBuffer>> {
        val reqFlux = Flux.generate({ 0 }, { state, sink ->
            if (state < number) {
                sink.next(
                    CreateAccountRequest.newBuilder()
                        .setBalance("100")
                        .build()
                )
                state + 1
            } else {
                sink.complete()
                state
            }
        })
//            .parallel(3)
//            .runOn(Schedulers.fromExecutor(VirtualThreadTaskExecutor()))
            .flatMap {
                val resp: Mono<CreateAccountResponse> = stub.createAccount(Mono.just(it))
                return@flatMap resp
            }
//            .sequential()

        val bufferFlux: Flux<DataBuffer> = reqFlux.map {
            val bytes = (it.accountId + "\n").toByteArray(Charsets.UTF_8)
            return@map DefaultDataBufferFactory.sharedInstance.wrap(bytes)
        }
        return ResponseEntity.ok()
            .headers {
                it.contentType = MediaType.parseMediaType("text/csv")
                it.contentDisposition = ContentDisposition.parse("attachment; filename=\"data.csv\"")
            }
            .body(bufferFlux)
    }

    @PostMapping("/bulk-transfers", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun bulkTransfers(
        @RequestParam parallel: Int,
        @RequestParam userCount: Long,
        @RequestParam transactionPerUser: Int,
        @RequestPart("file")
        fileFlux: FilePart
    ): String {
        val accountIds: List<String> = DataBufferUtils.join(fileFlux.content())
            .map { buffer ->
                buffer.asInputStream(true)
                    .bufferedReader().use { reader ->
                        reader.readLines()
                    }
            }.block()!!
//        var i = 0
//        accountIds.toFlux()
//            .skip(accountIds.size - userCount)
//            .parallel(parallel)
//            .runOn(Schedulers.fromExecutor(VirtualThreadTaskExecutor("bulk-transfer")))
//            .flatMap { accountId ->
//                i++
//                if (i % 50_000 == 0)
//                    println(i)
//                return@flatMap stub.transfer(
//                    Mono.just(
//                        TransferRequest.newBuilder()
//                            .setUserId(accountId)
//                            .setAmount(TRANSFER_RANGE.random())
//                            .build()
//                    )
//                )
//            }.subscribe { }
        workerService.startRun(accountIds, userCount, transactionPerUser)
        return "Hello, World!"
    }

    @GetMapping("/stop-transfers")
    fun stopScheduler(): String {
        workerService.stopRun()
        return "Stopped"
    }

    companion object
}
