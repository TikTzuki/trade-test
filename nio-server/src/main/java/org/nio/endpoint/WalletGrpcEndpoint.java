package org.nio.endpoint;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.nio.wallet.grpc.ReactorWalletServiceGrpc;
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountRequest;
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountResponse;
import com.nio.wallet.grpc.WalletServiceOuterClass.GetAccountByIdRequest;
import com.nio.wallet.grpc.WalletServiceOuterClass.GetAccountByIdResponse;
import com.nio.wallet.grpc.WalletServiceOuterClass.WithdrawRequest;
import com.nio.wallet.grpc.WalletServiceOuterClass.WithdrawResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.nio.wallet.account.AccountService;
import org.nio.wallet.transaction.TransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

import static com.nio.wallet.grpc.WalletServiceOuterClass.CloseTicketRequest;
import static com.nio.wallet.grpc.WalletServiceOuterClass.CloseTicketResponse;
import static com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import static com.nio.wallet.grpc.WalletServiceOuterClass.TransferResponse;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class WalletGrpcEndpoint extends ReactorWalletServiceGrpc.WalletServiceImplBase {
    private static final Long TIMEOUT_MILLIS = 5_000L;
    final AccountService bankAccountService;
    final TransactionService transactionService;

    @Override
    public Flux<StringValue> ping(Flux<Empty> request) {
        return request.map(req -> StringValue.newBuilder()
                        .setValue("pong")
                        .build())
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS));
    }

    @Override
    public Mono<CreateAccountResponse> createAccount(Mono<CreateAccountRequest> request) {
        return request.flatMap(req -> bankAccountService.createBankAccount(MapperKt.of(req))
                        .doOnNext(v -> log.debug("span req {}", req)))
                .map(bankAccount -> CreateAccountResponse.newBuilder()
                        .setAccountId(bankAccount)
                        .build())
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .doOnError(ex -> {
                    log.error(ex.getMessage(), ex);
                })
                .doOnSuccess(result -> log.debug("created account: {}", result.getAccountId()));
    }

    @Override
    public Flux<TransferResponse> transferStream(Flux<TransferRequest> request) {
        return request
                .flatMap(transactionService::transfer)
                .map(newTran -> TransferResponse.newBuilder()
                        .setTransactionId(newTran.getId())
                        .setReferenceId(newTran.getRefId())
                        .build())
                .doOnNext(result -> log.debug("Write success tran: {}", result))
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS));
    }

    @Override
    public Mono<TransferResponse> transfer(Mono<TransferRequest> request) {
        return request
//                .flatMap(transactionService::transfer)
                .map(newTran -> TransferResponse.newBuilder()
                        .setTransactionId(UUID.randomUUID().toString())
                        .setReferenceId(newTran.getReferenceId())
                        .build())
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .doOnError(ex -> {
                    log.error(ex.getMessage(), ex);
                })
                .doOnSuccess(result -> log.debug("Write success tran: {}", result));
    }

    @Override
    public Mono<CloseTicketResponse> closeTicket(Mono<CloseTicketRequest> request) {
        return Mono.empty();
    }

    @Override
    public Mono<GetAccountByIdResponse> getAccountById(Mono<GetAccountByIdRequest> request) {
        return Mono.just(GetAccountByIdResponse.newBuilder()
                .build());
    }

    @Override
    public Mono<WithdrawResponse> withdraw(Mono<WithdrawRequest> request) {
        return request.map(req -> {
            return req;
        }).map(req -> WithdrawResponse.newBuilder()
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
