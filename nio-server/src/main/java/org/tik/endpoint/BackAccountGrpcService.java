package org.tik.endpoint;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.tik.grpc.bank.service.Bank.CreateBankAccountRequest;
import com.tik.grpc.bank.service.Bank.CreateBankAccountResponse;
import com.tik.grpc.bank.service.Bank.GetBankAccountByIdRequest;
import com.tik.grpc.bank.service.Bank.GetBankAccountByIdResponse;
import com.tik.grpc.bank.service.ReactorBankAccountServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.tik.bank.BankAccountService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class BackAccountGrpcService extends ReactorBankAccountServiceGrpc.BankAccountServiceImplBase {
    private static final Long TIMEOUT_MILLIS = 5000L;
    private final BankAccountService bankAccountService;

    @Override
    public Flux<StringValue> ping(Flux<Empty> request) {
        return request.map(req -> StringValue.newBuilder()
                        .setValue("pong")
                        .build())
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS));
    }

    @Override
    public Mono<CreateBankAccountResponse> createBankAccount(Mono<CreateBankAccountRequest> request) {
        return request.flatMap(req -> bankAccountService.createBankAccount(MapperKt.of(req))
                        .doOnNext(v -> log.debug("span req {}", req)))
                .map(bankAccount -> CreateBankAccountResponse.newBuilder()
                        .setBankAccount(bankAccount.toString())
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