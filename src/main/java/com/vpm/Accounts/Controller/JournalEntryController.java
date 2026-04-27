


package com.vpm.Accounts.Controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Service.JournalEntryService;

@RestController
@RequestMapping("/api/journal")
@CrossOrigin(origins = "*")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @PostMapping
    public CompletableFuture<ResponseEntity<JournalEntry>> createEntry(@RequestBody JournalEntry entry) {
        return journalEntryService.saveJournalEntryAsync(entry)
                .thenApply(saved -> new ResponseEntity<>(saved, HttpStatus.CREATED));
    }

    @GetMapping
    public CompletableFuture<Iterable<JournalEntry>> getAllEntries() {
        return journalEntryService.getAllEntriesAsync().thenApply(entries -> entries);
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<JournalEntry>> getEntry(@PathVariable Long id) {
        return journalEntryService.getEntryByIdAsync(id)
                .thenApply(entryOpt -> entryOpt.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build()));
    }
    
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> updateEntry(@PathVariable Long id, @RequestBody JournalEntry entry) {
        return journalEntryService.updateJournalEntryAsync(id, entry)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body(ex.getMessage()));
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteEntry(@PathVariable Long id) {
        return journalEntryService.getEntryByIdAsync(id)
                .thenCompose(existing -> existing.map(journal -> journalEntryService.deleteEntryAsync(id)
                        .thenApply(ignored -> ResponseEntity.noContent().<Void>build()))
                        .orElseGet(() -> CompletableFuture.completedFuture(ResponseEntity.notFound().build())));
    }
}