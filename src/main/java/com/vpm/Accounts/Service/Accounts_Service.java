package com.vpm.Accounts.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.Journal_Book;
import com.vpm.Accounts.Repository.Accounts_Repository;

import java.util.Optional;

@Service
public class Accounts_Service {

    private final Accounts_Repository accounts_Repository;

    @Autowired
    public Accounts_Service(Accounts_Repository accounts_Repository) {
        this.accounts_Repository = accounts_Repository;
    }

    // Save or Create Journal_Book
    public Journal_Book saveJournal(Journal_Book journal_book) {
        return accounts_Repository.save(journal_book);
    }

    // Read/Get a Journal_Book by id
    public Optional<Journal_Book> getJournalById(Long id) {
        return accounts_Repository.findById(id);
    }

    // Update an existing Journal_Book by id
    public Journal_Book updateJournal(Long id, Journal_Book journal_book) {
        Optional<Journal_Book> existingJournalOpt = accounts_Repository.findById(id);

        if (existingJournalOpt.isPresent()) {
            Journal_Book existingJournal = existingJournalOpt.get();
            // Update fields if needed
            existingJournal.setDate(journal_book.getDate());
            existingJournal.setType(journal_book.getType());
            existingJournal.setAmount(journal_book.getAmount());
            existingJournal.setDescription(journal_book.getDescription());
            existingJournal.setParticulars(journal_book.getParticulars());
            existingJournal.setMode(journal_book.getMode());
            return accounts_Repository.save(existingJournal);
        }

        return null; // If journal doesn't exist, return null or handle as needed
    }

    // Delete a Journal_Book by id
    public void deleteJournal(Long id) {
        accounts_Repository.deleteById(id);
    }

    // Get all Journal_Books
    public Iterable<Journal_Book> getAllJournals() {
        return accounts_Repository.findAll();
    }
}
