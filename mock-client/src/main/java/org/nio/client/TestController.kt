package org.nio.client

import com.google.protobuf.Empty
import com.nio.wallet.grpc.ReactorWalletServiceGrpc.ReactorWalletServiceStub
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountRequest
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountResponse
import net.devh.boot.grpc.client.inject.GrpcClient
import org.nio.client.utils.readCsvFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

val TRANSFER_RANGE = listOf("1", "5", "10")

@RestController
@RequestMapping("/api/v1/wallet")
class TestController @Autowired constructor(
    @GrpcClient("bank-account-service")
    val stub: ReactorWalletServiceStub
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
        val reqFlux: Flux<CreateAccountResponse> = Flux.generate({ 0 }, { state, sink ->
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
        }).flatMap {
            val resp: Mono<CreateAccountResponse> = stub.createAccount(Mono.just(it))
            return@flatMap resp
        }
        // Create csv file
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

    @PostMapping("/bulk-transfers")
    fun bulkTransfers(
        @RequestParam number: Int,
        @RequestPart("file") fileFlux: Flux<MultipartFile>
    ): String {
        fileFlux.flatMap {
            readCsvFile(it)
//                .flatMap { it ->
//                    return@flatMap stub.transfer(
//                        Mono.just(
//                            TransferRequest.newBuilder()
//                                .setUserId(it[0])
//                                .setAmount(TRANSFER_RANGE.random())
//                                .build()
//                        )
//                    )
//                }
        }.subscribe()
//        val reqFlux = Flux.generate({ 0 }, { state, sink ->
//            if (state < number) {
//                sink.next(CreateAccountRequest.newBuilder().build())
//                state + 1
//            } else {
//                sink.complete()
//                state
//            }
//        }).flatMap {
//            val resp: Mono<CreateAccountResponse> = stub.createAccount(Mono.just(it))
//            return@flatMap resp
//        }
//        reqFlux.subscribe { println(it) }
        return "Hello, World!"
    }

    companion object
}
