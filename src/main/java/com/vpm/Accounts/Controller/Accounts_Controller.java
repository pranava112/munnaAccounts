



package com.vpm.Accounts.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull; // Ensure this import is used
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

    private final Accounts_Service accountsService;

    public Accounts_Controller(Accounts_Service accountsService) {
        this.accountsService = accountsService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Account>> createAccount(@RequestBody @NonNull Account account) {
        return accountsService.saveAccountAsync(account)
                .thenApply(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                .exceptionally(this::handleException);
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Account>> updateAccount(
            @PathVariable @NonNull Long id, 
            @RequestBody @NonNull Account account) {
        
        return accountsService.updateAccountAsync(id, account)
                .thenApply(updated -> ResponseEntity.ok().body(updated))
                .exceptionally(this::handleException);
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Account>> getAccountById(@PathVariable @NonNull Long id) {
        return accountsService.getAccountByIdAsync(id)
                .thenApply(opt -> opt
                        .map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build()))
                .exceptionally(this::handleException);
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> deleteAccount(@PathVariable @NonNull Long id) {
        return accountsService.getAccountByIdAsync(id)
                .thenCompose(opt -> {
                    if (opt.isPresent()) {
                        return accountsService.deleteAccountAsync(id)
                                .thenApply(ignored -> (ResponseEntity<?>) ResponseEntity.noContent().build());
                    }
                    return CompletableFuture.completedFuture(
                            (ResponseEntity<?>) ResponseEntity.notFound().build());
                })
                .exceptionally(this::handleDeleteException);
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<Account>>> getAllAccounts() {
        return accountsService.getAllAccountsAsync()
                .thenApply(ResponseEntity::ok)
                .exceptionally(this::handleException);
    }

    /**
     * Centralized Error Handler to satisfy @NonNull requirements.
     * This catches the 'Account not found' RuntimeException from the service.
     */
    private <T> ResponseEntity<T> handleException(Throwable ex) {
        Throwable cause = ex;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        
        String message = cause.getMessage();
        if (message != null && message.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private ResponseEntity<?> handleDeleteException(Throwable ex) {
        Throwable cause = ex;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage();
        if (message != null && message.contains("used by journal entries")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("message", message));
        }

        if (message != null && message.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}