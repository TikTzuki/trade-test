package org.nio.wallet.account;

import reactor.core.publisher.Mono;

public interface BankAccountRepositoryCustom<ID> {
    Mono<ID> insertLite(BankAccount i);
}
