package org.nio.wallet.account.impl;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.RequiredArgsConstructor;
import org.nio.wallet.account.BankAccount;
import org.nio.wallet.account.BankAccountRepositoryCustom;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BankAccountRepositoryImpl implements BankAccountRepositoryCustom<UUID> {
    final ReactiveCassandraTemplate template;

    @Override
    public Mono<UUID> insertLite(BankAccount i) {
        return template.execute(SimpleStatement.newInstance("INSERT INTO bankaccount (id, balance) VALUES (?, ?)", i.getId(), i.getBalance()))
                .map(r -> {
                    return UUID.randomUUID();
                });
    }
}
