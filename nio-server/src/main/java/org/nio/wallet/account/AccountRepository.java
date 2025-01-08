package org.nio.wallet.account;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface AccountRepository extends ReactiveCassandraRepository<Account, String>, AccountCustomRepository<Account, String> {
}
