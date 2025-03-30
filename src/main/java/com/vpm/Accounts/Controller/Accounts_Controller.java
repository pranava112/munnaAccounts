package com.vpm.Accounts.Controller;

import com.vpm.Accounts.Entity.Journal_Book;
import com.vpm.Accounts.Service.Accounts_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/journal")
public class Accounts_Controller {

    private final Accounts_Service accounts_Service;

    @Autowired
    public Accounts_Controller(Accounts_Service accounts_Service) {
        this.accounts_Service = accounts_Service;
    }

    // Create a new Journal_Book
    @PostMapping
    public ResponseEntity<Journal_Book> createJournal(@RequestBody Journal_Book journal_book) {
        Journal_Book createdJournal = accounts_Service.saveJournal(journal_book);
        return new ResponseEntity<>(createdJournal, HttpStatus.CREATED);
    }

    // Get a Journal_Book by id
    @GetMapping("/{id}")
    public ResponseEntity<Journal_Book> getJournalById(@PathVariable Long id) {
        Optional<Journal_Book> journal = accounts_Service.getJournalById(id);
        if (journal.isPresent()) {
            return new ResponseEntity<>(journal.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Update an existing Journal_Book by id
    @PutMapping("/{id}")
    public ResponseEntity<Journal_Book> updateJournal(@PathVariable Long id, @RequestBody Journal_Book journal_book) {
        Journal_Book updatedJournal = accounts_Service.updateJournal(id, journal_book);
        if (updatedJournal != null) {
            return new ResponseEntity<>(updatedJournal, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a Journal_Book by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJournal(@PathVariable Long id) {
        Optional<Journal_Book> journal = accounts_Service.getJournalById(id);
        if (journal.isPresent()) {
            accounts_Service.deleteJournal(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get all Journal_Books
    @GetMapping
    public ResponseEntity<Iterable<Journal_Book>> getAllJournals() {
        Iterable<Journal_Book> journals = accounts_Service.getAllJournals();
        return new ResponseEntity<>(journals, HttpStatus.OK);
    }
}
