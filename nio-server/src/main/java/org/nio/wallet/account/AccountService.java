package org.nio.wallet.account;

import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<String> createAccount(Account bankAccount);
}