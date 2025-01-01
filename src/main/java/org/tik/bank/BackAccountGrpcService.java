package org.tik.bank;

import com.example.grpc.bank.service.Bank;
import com.example.grpc.bank.service.ReactorBankAccountServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class BackAccountGrpcService extends ReactorBankAccountServiceGrpc.BankAccountServiceImplBase {
    private final BankAccountService bankAccountService;
    private final Tracer tracer;
    private static final Long TIMEOUT_MILLIS = 5000L;
    private final Validator validator;

    @Override
    public Mono<Bank.CreateBankAccountResponse> createBankAccount(Mono<Bank.CreateBankAccountRequest> request) {
        return request.flatMap(req -> bankAccountService.createBankAccount(validate(BankAccountMapper.of(req)))
                        .doOnNext(v -> spanTag("req", req.toString())))
                .map(bankAccount -> Bank.CreateBankAccountResponse.newBuilder()
                        .setBankAccount(BankAccountMapper.toGrpc(bankAccount))
                        .build())
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .doOnError(this::spanError)
                .doOnSuccess(result -> log.info("created account: {}", result.getBankAccount()));
    }

    private <T> T validate(T data) {
        var errors = validator.validate(data);
        if (!errors.isEmpty()) throw new ConstraintViolationException(errors);
        return data;
    }

    private void spanTag(String key, String value) {
        var span = tracer.currentSpan();
        if (span != null) span.tag(key, value);
    }

    private void spanError(Throwable ex) {
        var span = tracer.currentSpan();
        if (span != null) span.error(ex);
    }
}
