package com.vpm.Accounts.Controller;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Service.Accounts_Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins= {"http://localhost:5173"})
public class Accounts_Controller {

    private final Accounts_Service accounts_Service;

    @Autowired
    public Accounts_Controller(Accounts_Service accounts_Service) {
        this.accounts_Service = accounts_Service;
    }

    
    @PostMapping
    public ResponseEntity<Account> createJournal(@RequestBody Account Account) {
    	Account createdJournal = accounts_Service.saveAccount(Account);
        return new ResponseEntity<>(createdJournal, HttpStatus.CREATED);
    }

    // Get a Journal_Book by id
    @GetMapping("/{id}")
    public ResponseEntity<Account> getJournalById(@PathVariable Long id) {
        Optional<Account> account = accounts_Service.getAccountById(id);
        if (account.isPresent()) {
            return new ResponseEntity<>(account.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Update an existing Journal_Book by id
    @PutMapping("/{id}")
    public ResponseEntity<Account> updateJournal(@PathVariable Long id, @RequestBody Account account) {
    	Account updatedAccount = accounts_Service.updateAccount(id, account);
        if (updatedAccount != null) {
            return new ResponseEntity<>(updatedAccount, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Delete a Journal_Book by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJournal(@PathVariable Long id) {
        Optional<Account> journal = accounts_Service.getAccountById(id);
        if (journal.isPresent()) {
            accounts_Service.deleteAccount(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get all Journal_Books
    @GetMapping
    public ResponseEntity<Iterable<Account>> getAllAccount() {
        Iterable<Account> account = accounts_Service.getAllAccounts();
        return new ResponseEntity<>(account, HttpStatus.OK);
    }
}
