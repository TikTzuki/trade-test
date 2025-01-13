package org.nio.wallet.transaction.impl;

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.sqs.MessageKt;
import org.nio.transaction.InsertTransactionFail;
import org.nio.transaction.InsufficientBalance;
import org.nio.transaction.TranLogger;
import org.nio.transaction.VersionConflict;
import org.nio.wallet.account.AccountRepository;
import org.nio.wallet.transaction.NewTransaction;
import org.nio.wallet.transaction.Transaction;
import org.nio.wallet.transaction.TransactionAction;
import org.nio.wallet.transaction.TransactionRepository;
import org.nio.wallet.transaction.TransactionType;
import org.springframework.stereotype.Service;
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

    public Mono<Void> transfer(TransferRequest request) {
        var id = UUID.randomUUID().toString();
        var accountId = request.getUserId();
        var ticketId = request.getTicketId();
        var amount = new BigDecimal(request.getAmount());

        try {
            MessageKt.publish(sqsClient, request);
        } catch (Exception e) {
            return Mono.error(new InsertTransactionFail(request.getReferenceId()));
        }
        return Mono.empty();
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
