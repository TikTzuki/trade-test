package org.tik.bank;

public interface BankAccountService {
    Mono<BankAccount> createBankAccount(BankAccount bankAccount);

    Mono<BankAccount> getBankAccountById(UUID id);

    Mono<BankAccount> depositAmount(UUID id, BigDecimal amount);

    Mono<BankAccount> withdrawAmount(UUID id, BigDecimal amount);

    Flux<BankAccount> findBankAccountByBalanceBetween(FindByBalanceRequestDto request);

    Mono<Page<BankAccount>> findAllBankAccountsByBalance(FindByBalanceRequestDto request);
}