package org.tik.bank.impl;


import org.springframework.stereotype.Service;
import org.tik.bank.BankAccount;
import org.tik.bank.BankAccountService;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BankAccountServiceImpl implements BankAccountService {
    @Override
    public Mono<BankAccount> createBankAccount(BankAccount bankAccount) {
        return Mono.empty();
    }

    @Override
    public Mono<BankAccount> getBankAccountById(UUID id) {
        return Mono.empty();
    }

    @Override
    public Mono<BankAccount> depositAmount(UUID id, BigDecimal amount) {
        return Mono.empty();
    }

    @Override
    public Mono<BankAccount> withdrawAmount(UUID id, BigDecimal amount) {
        return Mono.empty();
    }
}
