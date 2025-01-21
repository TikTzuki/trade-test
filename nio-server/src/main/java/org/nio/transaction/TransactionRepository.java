package org.nio.transaction;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface TransactionRepository extends ReactiveCassandraRepository<Transaction, String>, TransactionCustomRepository {
}
