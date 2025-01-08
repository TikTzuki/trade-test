package org.nio.wallet.account;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import java.util.UUID;

public interface BankAccountRepository extends ReactiveCassandraRepository<BankAccount, UUID>, BankAccountRepositoryCustom<UUID> {
    String TABLE_NAME = "bank_account";
}
