package org.nio.wallet.account.impl;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import lombok.RequiredArgsConstructor;
import org.nio.wallet.account.Account;
import org.nio.wallet.account.AccountCustomRepository;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static org.nio.wallet.account.AccountKt.ACCOUNT_TABLE;

@Component
@RequiredArgsConstructor
public class AccountCustomRepositoryImpl implements AccountCustomRepository<Account, String> {
    final ReactiveCassandraTemplate template;

    @Override
    public Mono<String> insertLite(Account i) {
        return template.execute(SimpleStatement.newInstance("INSERT INTO " + ACCOUNT_TABLE + "(id, balance) VALUES (?, ?)", i.getId(), i.getBalance()))
                .map(r -> i.getId());
    }

//    @Override
//    public Mono<Void> updateBalance(String id, BigDecimal balance, Long version) {
//        template.execute(SimpleStatement.newInstance("UPDATE " + ACCOUNT_TABLE + " SET balance = ? WHERE id = ? IF VERSION = ?", balance, id, version));
//        return null;
//    }
}
