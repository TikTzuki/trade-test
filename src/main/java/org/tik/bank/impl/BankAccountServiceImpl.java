package org.tik.bank.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tik.bank.BankAccount;
import org.tik.bank.BankAccountRepository;
import org.tik.bank.BankAccountService;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {
    final BankAccountRepository repository;

    @Override
    public Mono<UUID> createBankAccount(BankAccount bankAccount) {
        return repository.insertLite(bankAccount);
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
