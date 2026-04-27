package com.vpm.Accounts.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Repository.AccountRepository;

@Service
public class Accounts_Service {

    private final AccountRepository accountRepository;

    // @Autowired
    public Accounts_Service(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Async("accountExecutor")
    public CompletableFuture<Account> saveAccountAsync(Account account) {
        return CompletableFuture.completedFuture(accountRepository.save(account));
    }

    @Async("accountExecutor")
    public CompletableFuture<Optional<Account>> getAccountByIdAsync(Long id) {
        return CompletableFuture.completedFuture(accountRepository.findById(id));
    }

    @Async("accountExecutor")
    public CompletableFuture<Account> updateAccountAsync(Long id, Account account) {
        Optional<Account> existingAccountOpt = accountRepository.findById(id);

        if (existingAccountOpt.isPresent()) {
            Account existingJournal = existingAccountOpt.get();
            existingJournal.setName(account.getName());
            existingJournal.setType(account.getType());
            existingJournal.setCode(account.getCode());
            return CompletableFuture.completedFuture(accountRepository.save(existingJournal));
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteAccountAsync(Long id) {
        accountRepository.deleteById(id);
        return CompletableFuture.completedFuture(null);
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Account>> getAllAccountsAsync() {
        List<Account> accounts = StreamSupport.stream(accountRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(accounts);
    }
}

