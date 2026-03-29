


package com.vpm.Accounts.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Service.JournalEntryService;

@RestController
@RequestMapping("/api/journal")
@CrossOrigin(origins = "*")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @PostMapping
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry) {
        return new ResponseEntity<>(journalEntryService.saveJournalEntry(entry), HttpStatus.CREATED);
    }

    @GetMapping
    public Iterable<JournalEntry> getAllEntries() {
        return journalEntryService.getAllEntries();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntry> getEntry(@PathVariable Long id) {
        return journalEntryService.getEntryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntry(@PathVariable Long id, @RequestBody JournalEntry entry) {
        try {
            return ResponseEntity.ok(journalEntryService.updateJournalEntry(id, entry));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteEntry(@PathVariable Long id) {
        journalEntryService.deleteEntry(id);
    }
}