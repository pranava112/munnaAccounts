package com.vpm.Accounts.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Repository.AccountRepository;

import java.util.Optional;

@Service
public class Accounts_Service {

    private final AccountRepository accountRepository;

    @Autowired
    public Accounts_Service(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }


    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public Account updateAccount(Long id, Account account) {
        Optional<Account> existingAccountOpt = accountRepository.findById(id);

        if (existingAccountOpt.isPresent()) {
        	Account existingJournal = existingAccountOpt.get();
            
            existingJournal.setName(account.getName());
            existingJournal.setType(account.getType());
            existingJournal.setCode(account.getCode());

            return accountRepository.save(existingJournal);
        }

        return null; 
    }

    
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    public Iterable<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
  }

