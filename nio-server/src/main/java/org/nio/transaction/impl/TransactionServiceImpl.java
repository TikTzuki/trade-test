package org.nio.transaction.impl;

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.account.AccountRepository;
import org.nio.sqs.MessageKt;
import org.nio.transaction.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl {
    final TransactionRepository repository;
    final AccountRepository accountRepository;
    final SqsClient sqsClient;

    public Flux<Void> prepareTransfer(Flux<TransferRequest> request) {
        long start = System.currentTimeMillis();
        return request
                .buffer(10)
                .flatMap(batch -> {
                    try {
                        MessageKt.publish(sqsClient, batch);
                    } catch (Exception e) {
                        log.warn("Transfer fail {} {}", batch, e.getMessage());
                        return Flux.concat(Flux.fromIterable(batch)
                                .map(it -> Flux.error(new InsertTransactionFail(it.getReferenceId()))));
                    }
                    return Flux.empty();
                })
                .doOnComplete(() -> log.info("Prepare complete: {}", System.currentTimeMillis() - start))
                .thenMany(Flux.empty());
    }

    public Mono<NewTransaction> persistTransaction(TransferRequest request) {
        var id = UUID.randomUUID().toString();
        var accountId = request.getUserId();
        var ticketId = request.getTicketId();
        var amount = new BigDecimal(request.getAmount());

        return accountRepository
                .getAccountBalance(accountId)
                .doOnNext(balanceAndVersion -> {
                    if (balanceAndVersion.balance().compareTo(amount) >= 0)
                        throw new InsufficientBalance(request.getReferenceId());
                })
                .flatMap(balanceAndVersion -> accountRepository.updateBalance(
                        accountId,
                        balanceAndVersion.balance().subtract(amount),
                        balanceAndVersion.version() + 1,
                        balanceAndVersion.version()
                ))
                .doOnNext(success -> {
                    if (!success) {
                        throw new VersionConflict(request.getReferenceId());
                    }
                })
                .flatMap(success -> {
                    if (!success) {
                        return Mono.error(new InsufficientBalance(request.getReferenceId()));
                    }
                    log.debug("Transfer success: {}", success);
                    return repository.insert(
                                    new Transaction(
                                            id, accountId,
                                            Instant.now(),
                                            ticketId,
                                            TransactionType.WITHDRAW,
                                            TransactionAction.BET,
                                            request.getReferenceId(),
                                            amount,
                                            BigDecimal.ZERO,
                                            1
                                    ))
                            .map(t -> new NewTransaction(t.getId(), t.getRefId()));
                })
                .onErrorMap(origin -> {
                    log.error("Write transaction fail", origin);
                    TranLogger.logger.error("Request\n {} reason: {}", request, origin.getMessage());
                    //TODO: handle on each error type
                    return switch (origin) {
                        case InsufficientBalance e -> e;
                        case VersionConflict e -> e;
                        default -> new InsertTransactionFail(request.getReferenceId());
                    };
                })
                .onErrorContinue(throwable -> switch (throwable) {
                    case InsufficientBalance e -> true;
                    case VersionConflict e -> true;
                    case InsertTransactionFail e -> true;
                    default -> false;
                }, (origin, e) -> {
                });
    }
}
