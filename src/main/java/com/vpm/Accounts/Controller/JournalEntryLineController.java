package com.vpm.Accounts.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Service.JournalEntryLineService;

@RestController
@RequestMapping("/api/lines")
@CrossOrigin(origins = "*")
public class JournalEntryLineController {

    @Autowired
    private JournalEntryLineService service;
    
    @PostMapping
    public CompletableFuture<ResponseEntity<?>> save(@RequestBody JournalEntryLine line) {
        return service.saveJournalEntryLineAsync(line)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body(ex.getMessage()));
    }

    @GetMapping
    public CompletableFuture<List<JournalEntryLine>> getAll() {
        return service.getAllJournalEntryLineAsync();
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<JournalEntryLine>> getById(@PathVariable Long id) {
        return service.getJournalEntryLineByIdAsync(id)
                .thenApply(account -> account.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build()));
    }
    
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> update(@PathVariable Long id, @RequestBody JournalEntryLine line) {
        return service.updateJournalEntryLineAsync(id, line)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body(ex.getMessage()));
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> delete(@PathVariable Long id) {
        return service.getJournalEntryLineByIdAsync(id)
                .thenCompose(existing -> {
                    if (existing.isPresent()) {
                        return service.deleteJournalEntryLineAsync(id)
                                .thenApply(ignored -> ResponseEntity.noContent().<Void>build());
                    }
                    return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
                });
    }
    
    @GetMapping("/ledger/{accountId}")
    public CompletableFuture<List<JournalEntryLine>> getLedger(@PathVariable Long accountId) {
        return service.getLedgerAsync(accountId);
    }
}
