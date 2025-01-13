package org.nio.transaction;

import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import static com.datastax.oss.driver.api.core.DefaultConsistencyLevel.LOCAL_ONE;

public interface TransactionRepository extends ReactiveCassandraRepository<Transaction, String>, TransactionCustomRepository {
    @Consistency(LOCAL_ONE)
    Flux<Transaction> findTransactionByAccountId(String accountId);
}
