package com.vpm.Accounts.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryLineRepository;

@Service
@Transactional // Ensures database integrity across async threads
public class Accounts_Service {

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public Accounts_Service(AccountRepository accountRepository,
            JournalEntryLineRepository journalEntryLineRepository) {
        this.accountRepository = accountRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
    }

    @Async("accountExecutor")
    public CompletableFuture<Account> saveAccountAsync(@NonNull Account account) {
        // use supplyAsync to offload the actual DB blocking call to the executor
        return CompletableFuture.supplyAsync(() -> accountRepository.save(account));
    }

    @Async("accountExecutor")
    public CompletableFuture<Optional<Account>> getAccountByIdAsync(@NonNull Long id) {
        return CompletableFuture.supplyAsync(() -> accountRepository.findById(id));
    }

    @Async("accountExecutor")
    public CompletableFuture<Account> updateAccountAsync(@NonNull Long id, @NonNull Account account) {
        return CompletableFuture.supplyAsync(() -> {
            return accountRepository.findById(id)
                .map(existing -> {
                    existing.setName(account.getName());
                    existing.setType(account.getType());
                    existing.setCode(account.getCode());
                    return accountRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + id));
        });
    }

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteAccountAsync(@NonNull Long id) {
        return CompletableFuture.runAsync(() -> {
            if (journalEntryLineRepository.existsByAccount_Id(id)) {
                throw new IllegalStateException(
                        "Account cannot be deleted because it is used by journal entries");
            }

            accountRepository.deleteById(id);
        });
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Account>> getAllAccountsAsync() {
        // findAll() usually returns a List in modern Spring Data; if not, cast is faster than stream
        return CompletableFuture.supplyAsync(() -> (List<Account>) accountRepository.findAll());
    }

    @Async("accountExecutor")
    public CompletableFuture<Optional<Account>> findByNameAsync(@NonNull String name) {
        return CompletableFuture.supplyAsync(() -> accountRepository.findFirstByName(name));
    }

    // Read-only hint for optimization
    @Transactional(readOnly = true)
    public Optional<Account> findAllByName(String name) {
        return accountRepository.findFirstByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<Account> findAllByNameIgnoreCase(String name) {
        return accountRepository.findFirstByNameIgnoreCase(name);
    }
}