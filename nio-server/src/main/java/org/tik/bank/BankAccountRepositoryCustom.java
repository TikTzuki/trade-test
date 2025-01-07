package org.tik.bank;

import reactor.core.publisher.Mono;

public interface BankAccountRepositoryCustom<ID> {
    Mono<ID> insertLite(BankAccount i);
}
