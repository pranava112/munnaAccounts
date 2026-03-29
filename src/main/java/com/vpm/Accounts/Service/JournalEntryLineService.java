package com.vpm.Accounts.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.JournalEntryLineRepository;


@Service
public class JournalEntryLineService {

    @Autowired
    private JournalEntryLineRepository repo;

    public JournalEntryLine saveJournalEntryLine(JournalEntryLine line) {

        // ❌ BLOCK INVALID DATA
        if (line.getAccount() == null) {
            throw new RuntimeException("Account is required!");
        }

        if (line.getDebit() == 0 && line.getCredit() == 0) {
            throw new RuntimeException("Debit or Credit must be non-zero!");
        }

        return repo.save(line);
    }

    public List<JournalEntryLine> getAllJournalEntryLine() {
        return repo.findAll();
    }

    public Optional<JournalEntryLine> getJournalEntryLineById(Long id) {
        return repo.findById(id);
    }

    public void deleteJournalEntryLine(Long id) {
        repo.deleteById(id);
    }

    public List<JournalEntryLine> getLedger(Long accountId) {
        return repo.findByAccountId(accountId);
    }

//	public JournalEntryLine updateJournalEntryLine(Long id, JournalEntryLine line) {
//		// TODO Auto-generated method stub
//		return null;
//	}
    
    public JournalEntryLine updateJournalEntryLine(Long id, JournalEntryLine updatedLine) {

        JournalEntryLine existingLine = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Journal Entry Line not found with id: " + id));

        // ✅ VALIDATION
        if (updatedLine.getAccount() == null) {
            throw new RuntimeException("Account is required!");
        }

        if (updatedLine.getDebit() == 0 && updatedLine.getCredit() == 0) {
            throw new RuntimeException("Either Debit or Credit must be non-zero!");
        }

        // ❌ Prevent both debit & credit filled
        if (updatedLine.getDebit() > 0 && updatedLine.getCredit() > 0) {
            throw new RuntimeException("Only one of Debit or Credit should be filled!");
        }

        // ✅ UPDATE FIELDS
        existingLine.setAccount(updatedLine.getAccount());
        existingLine.setDebit(updatedLine.getDebit());
        existingLine.setCredit(updatedLine.getCredit());

        // ⚠️ IMPORTANT: Don't change parent reference blindly
        if (updatedLine.getJournalEntry() != null) {
            existingLine.setJournalEntry(updatedLine.getJournalEntry());
        }

        return repo.save(existingLine);
    }
	
	
}