package org.nio.wallet.account;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankAccountService {
    Mono<UUID> createBankAccount(BankAccount bankAccount);

    Mono<BankAccount> getBankAccountById(UUID id);

    Mono<BankAccount> depositAmount(UUID id, BigDecimal amount);

    Mono<BankAccount> withdrawAmount(UUID id, BigDecimal amount);

//    Flux<BankAccount> findBankAccountByBalanceBetween(FindByBalanceRequestDto request);
//
//    Mono<Page<BankAccount>> findAllBankAccountsByBalance(FindByBalanceRequestDto request);
}