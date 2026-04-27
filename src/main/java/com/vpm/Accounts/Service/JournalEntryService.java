
package com.vpm.Accounts.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.JournalEntryRepository;

@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository repo;

    public JournalEntry saveJournalEntry(JournalEntry journalEntry) {

        // ✅ FIX: Now this works correctly
        if (journalEntry.getLines() == null || journalEntry.getLines().isEmpty()) {
            throw new RuntimeException("Journal Entry must have at least one line");
        }

        double totalDebit = 0;
        double totalCredit = 0;

        List<JournalEntryLine> validLines = new ArrayList<>();

        for (JournalEntryLine line : journalEntry.getLines()) {

            // Skip invalid rows
            if (line.getAccount() == null) continue;

            if (line.getDebit() == 0 && line.getCredit() == 0) continue;

            // 🔥 IMPORTANT: Set parent
            line.setJournalEntry(journalEntry);

            totalDebit += line.getDebit();
            totalCredit += line.getCredit();

            validLines.add(line);
        }

        if (validLines.isEmpty()) {
            throw new RuntimeException("No valid journal lines!");
        }

        if (totalDebit != totalCredit) {
            throw new RuntimeException("Debit and Credit must be equal!");
        }

        journalEntry.setLines(validLines);

        return repo.save(journalEntry);
    }
    
    

    public List<JournalEntry> getAllEntries() {
        return repo.findAll();
    }

    public Optional<JournalEntry> getEntryById(@NonNull Long id) {
        return repo.findById(id);
    }

    public void deleteEntry(@NonNull Long id) {
        repo.deleteById(id);
    }

    @Async("accountExecutor")
    public CompletableFuture<JournalEntry> saveJournalEntryAsync(JournalEntry journalEntry) {
        return CompletableFuture.completedFuture(saveJournalEntry(journalEntry));
    }

    @Async("accountExecutor")
    public CompletableFuture<List<JournalEntry>> getAllEntriesAsync() {
        return CompletableFuture.completedFuture(getAllEntries());
    }

    @Async("accountExecutor")
    public CompletableFuture<Optional<JournalEntry>> getEntryByIdAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(getEntryById(id));
    }

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteEntryAsync(@NonNull Long id) {
        deleteEntry(id);
        return CompletableFuture.completedFuture(null);
    }

    @Async("accountExecutor")
    public CompletableFuture<JournalEntry> updateJournalEntryAsync(@NonNull Long id, JournalEntry updatedEntry) {
        return CompletableFuture.completedFuture(updateJournalEntry(id, updatedEntry));
    }
    
    public JournalEntry updateJournalEntry(@NonNull Long id, JournalEntry updatedEntry) {

        JournalEntry existingEntry = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal Entry not found with id: " + id));

        // ✅ Validate lines
        if (updatedEntry.getLines() == null || updatedEntry.getLines().isEmpty()) {
            throw new RuntimeException("Journal Entry must have at least one line");
        }

        double totalDebit = 0;
        double totalCredit = 0;

        List<JournalEntryLine> validLines = new ArrayList<>();

        for (JournalEntryLine line : updatedEntry.getLines()) {

            if (line.getAccount() == null) continue;
            if (line.getDebit() == 0 && line.getCredit() == 0) continue;

            // 🔥 SET PARENT
            line.setJournalEntry(existingEntry);

            totalDebit += line.getDebit();
            totalCredit += line.getCredit();

            validLines.add(line);
        }

        if (validLines.isEmpty()) {
            throw new RuntimeException("No valid journal lines!");
        }

        if (totalDebit != totalCredit) {
            throw new RuntimeException("Debit and Credit must be equal!");
        }

        // ✅ UPDATE BASIC FIELDS
        existingEntry.setEntryDate(updatedEntry.getEntryDate());
        existingEntry.setDescription(updatedEntry.getDescription());

        // ✅ CLEAR OLD LINES (IMPORTANT)
        existingEntry.getLines().clear();

        // ✅ ADD NEW LINES
        existingEntry.getLines().addAll(validLines);

        return repo.save(existingEntry);
    }
}