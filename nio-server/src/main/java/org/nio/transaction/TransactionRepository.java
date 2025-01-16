package org.nio.transaction;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveCassandraRepository<Transaction, String>, TransactionCustomRepository {
}
