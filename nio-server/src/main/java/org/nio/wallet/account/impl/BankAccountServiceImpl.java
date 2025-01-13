package org.nio.wallet.account.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nio.wallet.account.Account;
import org.nio.wallet.account.AccountRepository;
import org.nio.wallet.account.AccountService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements AccountService {
    final AccountRepository repository;

    @Override
    public Mono<String> createAccount(Account bankAccount) {
        return repository.insertLite(bankAccount);
    }
}
