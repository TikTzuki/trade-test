package org.nio.wallet.account;

import reactor.core.publisher.Mono;

public interface AccountCustomRepository<T, ID> {
    Mono<ID> insertLite(T i);

//    Mono<Void> updateBalance(String id, BigDecimal balance);
}
