package org.nio.client

import com.google.protobuf.Empty
import com.tik.grpc.bank.service.ReactorBankAccountServiceGrpc.ReactorBankAccountServiceStub
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/test")
class TestController @Autowired constructor(
    @GrpcClient("bank-account-service")
    val stub: ReactorBankAccountServiceStub
) {
    @PostMapping
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
}