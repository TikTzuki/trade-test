package org.nio.wallet.transaction;

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Mono<String> transfer(TransferRequest request) {
        var id = UUID.randomUUID().toString();
        var accountId = request.getUserId();
        var ticketId = request.getTicketId();
        var amount = new BigDecimal(request.getAmount());
        return repository.insert(new Transaction(
                        id, accountId, ticketId,
                        Transaction.Companion.generateShard(id, accountId, ticketId),
                        Instant.now(),
                        TransactionType.WITHDRAW,
                        request.getReferenceId(),
                        amount,
                        BigDecimal.ZERO
                ))
                .map(Transaction::getId);
    }
}
