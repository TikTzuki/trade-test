package org.tik.bank;

import com.tik.grpc.bank.service.Bank.CreateBankAccountRequest;
import com.tik.grpc.bank.service.Bank.CreateBankAccountResponse;
import com.tik.grpc.bank.service.Bank.GetBankAccountByIdRequest;
import com.tik.grpc.bank.service.Bank.GetBankAccountByIdResponse;
import com.tik.grpc.bank.service.ReactorBankAccountServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class BackAccountGrpcService extends ReactorBankAccountServiceGrpc.BankAccountServiceImplBase {
    private static final Long TIMEOUT_MILLIS = 5000L;
    private final BankAccountService bankAccountService;

    @Override
    public Mono<CreateBankAccountResponse> createBankAccount(Mono<CreateBankAccountRequest> request) {
        return request.flatMap(req -> bankAccountService.createBankAccount(MapperKt.of(req))
                        .doOnNext(v -> log.debug("span req {}", req.toString())))
                .map(bankAccount -> CreateBankAccountResponse.newBuilder()
                        .setBankAccount(MapperKt.toGrpc(bankAccount))
                        .build())
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .doOnError(ex -> {
                    log.error(ex.getMessage(), ex);
                })
                .doOnSuccess(result -> log.info("created account: {}", result.getBankAccount()));
    }

    @Override
    public Mono<GetBankAccountByIdResponse> getBankAccountById(Mono<GetBankAccountByIdRequest> request) {
        return Mono.just(GetBankAccountByIdResponse.newBuilder()
                .build());
    }
    //    private <T> T validate(T data) {
//        var errors = validator.validate(data);
//        if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
//        return data;
//    }
//
//    private void spanTag(String key, String value) {
//        var span = tracer.currentSpan();
//        if (span != null) span.tag(key, value);
//    }
//
//    private void spanError(Throwable ex) {
//        var span = tracer.currentSpan();
//        if (span != null) span.error(ex);
//    }
}
