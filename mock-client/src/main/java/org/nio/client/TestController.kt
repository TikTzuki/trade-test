package org.nio.client

import com.google.protobuf.Empty
import com.nio.wallet.grpc.ReactorWalletServiceGrpc.ReactorWalletServiceStub
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountRequest
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountResponse
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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

    @PostMapping("/build-insert-accounts")
    fun buildInsertAccounts(@RequestParam number: Int): String {
        val reqFlux = Flux.generate({ 0 }, { state, sink ->
            if (state < number) {
                sink.next(CreateAccountRequest.newBuilder().build())
                state + 1
            } else {
                sink.complete()
                state
            }
        }).flatMap {
            val resp: Mono<CreateAccountResponse> = stub.createAccount(Mono.just(it))
            return@flatMap resp
        }
        reqFlux.subscribe { println(it) }
        return "Hello, World!"
    }
}