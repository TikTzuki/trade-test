package org.nio.wallet.transaction;

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.transaction.InsertTransactionFail;
import org.nio.transaction.InsufficientBalance;
import org.nio.transaction.TranLogger;
import org.nio.wallet.account.AccountRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    final TransactionRepository repository;
    final AccountRepository accountRepository;

    public Mono<NewTransaction> transfer(TransferRequest request) {
        var id = UUID.randomUUID().toString();
        var accountId = request.getUserId();
        var ticketId = request.getTicketId();
        var amount = new BigDecimal(request.getAmount());

        return accountRepository.getAccountBalance(accountId)
                .doOnNext(balanceAndVersion -> {
                    if (balanceAndVersion.getBalance().compareTo(amount) >= 0)
                        throw new InsufficientBalance(request.getReferenceId());
                })
                .flatMap(balanceAndVersion -> accountRepository.updateBalance(
                        accountId,
                        balanceAndVersion.getBalance().subtract(amount),
                        balanceAndVersion.getVersion() + 1,
                        balanceAndVersion.getVersion()))
                .onErrorMap(_ -> new InsufficientBalance(request.getReferenceId()))
                .flatMap(success -> {
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
                    return new InsertTransactionFail(request.getReferenceId());
                });
    }
}
