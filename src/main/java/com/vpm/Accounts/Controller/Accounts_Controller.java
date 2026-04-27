package com.vpm.Accounts.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Service.Accounts_Service;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = {"http://localhost:5173"})
public class Accounts_Controller {

    private final Accounts_Service accounts_Service;

    // @Autowired
    public Accounts_Controller(Accounts_Service accounts_Service) {
        this.accounts_Service = accounts_Service;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Account>> createJournal(@RequestBody Account Account) {
        return accounts_Service.saveAccountAsync(Account)
                .thenApply(createdJournal -> new ResponseEntity<>(createdJournal, HttpStatus.CREATED));
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Account>> getJournalById(@PathVariable Long id) {
        return accounts_Service.getAccountByIdAsync(id)
                .thenApply(account -> account.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Account>> updateJournal(@PathVariable Long id, @RequestBody Account account) {
        return accounts_Service.updateAccountAsync(id, account)
                .thenApply(updatedAccount -> {
                    if (updatedAccount != null) {
                        return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
                    }
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                });
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteJournal(@PathVariable Long id) {
        return accounts_Service.getAccountByIdAsync(id)
                .thenCompose(existing -> {
                    if (existing.isPresent()) {
                        return accounts_Service.deleteAccountAsync(id)
                                .thenApply(ignored -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
                    }
                    return CompletableFuture.completedFuture(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
                });
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<Account>>> getAllAccount() {
        return accounts_Service.getAllAccountsAsync()
                .thenApply(accounts -> new ResponseEntity<>(accounts, HttpStatus.OK));
    }
}
