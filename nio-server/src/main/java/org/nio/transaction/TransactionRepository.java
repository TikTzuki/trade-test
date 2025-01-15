package org.nio.transaction;

import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

import static com.datastax.oss.driver.api.core.DefaultConsistencyLevel.LOCAL_QUORUM;

public interface TransactionRepository extends ReactiveCassandraRepository<Transaction, String>, TransactionCustomRepository {
    @Override
    @Consistency(LOCAL_QUORUM)
    <S extends Transaction> Mono<S> insert(S entity);
}
