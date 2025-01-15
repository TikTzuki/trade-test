package org.nio.transaction;

import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import org.springframework.data.cassandra.repository.Consistency;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveCassandraRepository<Transaction, String>, TransactionCustomRepository {
    @Consistency(DefaultConsistencyLevel.LOCAL_QUORUM)
    default <S extends Transaction> Mono<S> insertLite(S entity) {
        return insert(entity);
    }
}
