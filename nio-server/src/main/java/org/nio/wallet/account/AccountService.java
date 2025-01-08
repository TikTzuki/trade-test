package org.nio.wallet.account;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountService {
    Mono<String> createBankAccount(Account bankAccount);

    Mono<Account> getBankAccountById(String id);

    Mono<Account> depositAmount(String id, BigDecimal amount);

    Mono<Account> withdrawAmount(String id, BigDecimal amount);

//    Flux<BankAccount> findBankAccountByBalanceBetween(FindByBalanceRequestDto request);
//
//    Mono<Page<BankAccount>> findAllBankAccountsByBalance(FindByBalanceRequestDto request);
}