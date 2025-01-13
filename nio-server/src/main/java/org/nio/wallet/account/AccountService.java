package org.nio.wallet.account;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService {
    Mono<String> createAccount(Account bankAccount);
}