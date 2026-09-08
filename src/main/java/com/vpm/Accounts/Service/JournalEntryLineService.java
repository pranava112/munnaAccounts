// package com.vpm.Accounts.Service;

// import java.util.List;
// import java.util.Optional;
// import java.util.concurrent.CompletableFuture;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.lang.NonNull;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import com.vpm.Accounts.Entity.JournalEntryLine;
// import com.vpm.Accounts.Repository.JournalEntryLineRepository;


// @Service
// public class JournalEntryLineService {

//     @Autowired
//     private JournalEntryLineRepository repo;

//     @Async("accountExecutor")
//     public CompletableFuture<JournalEntryLine> saveJournalEntryLineAsync(JournalEntryLine line) {
//         if (line.getAccount() == null) {
//             throw new RuntimeException("Account is required!");
//         }

//         if (line.getDebit() == 0 && line.getCredit() == 0) {
//             throw new RuntimeException("Debit or Credit must be non-zero!");
//         }

//         return CompletableFuture.completedFuture(repo.save(line));
//     }
    
//     @Async("accountExecutor")
//     public CompletableFuture<List<JournalEntryLine>> getAllJournalEntryLineAsync() {
//         return CompletableFuture.completedFuture(repo.findAll());
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Optional<JournalEntryLine>> getJournalEntryLineByIdAsync(@NonNull Long id) {
//         return CompletableFuture.completedFuture(repo.findById(id));
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<Void> deleteJournalEntryLineAsync(@NonNull Long id) {
//         repo.deleteById(id);
//         return CompletableFuture.completedFuture(null);
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<List<JournalEntryLine>> getLedgerAsync(@NonNull Long accountId) {
//         return CompletableFuture.completedFuture(repo.findByAccountId(accountId));
//     }

// //	public JournalEntryLine updateJournalEntryLine(Long id, JournalEntryLine line) {
// //		// TODO Auto-generated method stub
// //		return null;
// //	}
    
//     @Async("accountExecutor")
//     public CompletableFuture<JournalEntryLine> updateJournalEntryLineAsync(@NonNull Long id, JournalEntryLine updatedLine) {

//         JournalEntryLine existingLine = repo.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Journal Entry Line not found with id: " + id));

//         if (updatedLine.getAccount() == null) {
//             throw new RuntimeException("Account is required!");
//         }

//         if (updatedLine.getDebit() == 0 && updatedLine.getCredit() == 0) {
//             throw new RuntimeException("Either Debit or Credit must be non-zero!");
//         }

//         if (updatedLine.getDebit() > 0 && updatedLine.getCredit() > 0) {
//             throw new RuntimeException("Only one of Debit or Credit should be filled!");
//         }

//         existingLine.setAccount(updatedLine.getAccount());
//         existingLine.setDebit(updatedLine.getDebit());
//         existingLine.setCredit(updatedLine.getCredit());

//         if (updatedLine.getJournalEntry() != null) {
//             existingLine.setJournalEntry(updatedLine.getJournalEntry());
//         }

//         return CompletableFuture.completedFuture(repo.save(existingLine));
//     }
	
	
// }


package com.vpm.Accounts.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.JournalEntryLineRepository;

@Service
public class JournalEntryLineService {

    @Autowired
    private JournalEntryLineRepository repo;

    // =========================
    // SAVE
    // =========================

    @Async("accountExecutor")
    public CompletableFuture<JournalEntryLine> saveJournalEntryLineAsync(
            JournalEntryLine line) {

        if (line.getAccount() == null) {
            throw new RuntimeException("Account is required!");
        }

        BigDecimal debit = line.getDebit();
        BigDecimal credit = line.getCredit();

        // Prevent null values
        if (debit == null) {
            debit = BigDecimal.ZERO;
        }

        if (credit == null) {
            credit = BigDecimal.ZERO;
        }

        // Both cannot be zero
        if (debit.compareTo(BigDecimal.ZERO) == 0
                && credit.compareTo(BigDecimal.ZERO) == 0) {

            throw new RuntimeException(
                    "Debit or Credit must be non-zero!");
        }

        // Both cannot have values
        if (debit.compareTo(BigDecimal.ZERO) > 0
                && credit.compareTo(BigDecimal.ZERO) > 0) {

            throw new RuntimeException(
                    "Only one of Debit or Credit should be filled!");
        }

        line.setDebit(debit);
        line.setCredit(credit);

        return CompletableFuture.completedFuture(repo.save(line));
    }

    // =========================
    // GET ALL
    // =========================

    @Async("accountExecutor")
    public CompletableFuture<List<JournalEntryLine>> getAllJournalEntryLineAsync() {

        return CompletableFuture.completedFuture(
                repo.findAll()
        );
    }

    // =========================
    // GET BY ID
    // =========================

    @Async("accountExecutor")
    public CompletableFuture<Optional<JournalEntryLine>> getJournalEntryLineByIdAsync(
            @NonNull Long id) {

        return CompletableFuture.completedFuture(
                repo.findById(id)
        );
    }

    // =========================
    // DELETE
    // =========================

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteJournalEntryLineAsync(
            @NonNull Long id) {

        repo.deleteById(id);

        return CompletableFuture.completedFuture(null);
    }

    // =========================
    // GET LEDGER
    // =========================

    @Async("accountExecutor")
    public CompletableFuture<List<JournalEntryLine>> getLedgerAsync(
            @NonNull Long accountId) {

        return CompletableFuture.completedFuture(
                repo.findByAccountId(accountId)
        );
    }

    // =========================
    // UPDATE
    // =========================

    @Async("accountExecutor")
    public CompletableFuture<JournalEntryLine> updateJournalEntryLineAsync(
            @NonNull Long id,
            JournalEntryLine updatedLine) {

        JournalEntryLine existingLine = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Journal Entry Line not found with id: " + id));

        // Account validation
        if (updatedLine.getAccount() == null) {
            throw new RuntimeException("Account is required!");
        }

        BigDecimal debit = updatedLine.getDebit();
        BigDecimal credit = updatedLine.getCredit();

        // Prevent null values
        if (debit == null) {
            debit = BigDecimal.ZERO;
        }

        if (credit == null) {
            credit = BigDecimal.ZERO;
        }

        // Both cannot be zero
        if (debit.compareTo(BigDecimal.ZERO) == 0
                && credit.compareTo(BigDecimal.ZERO) == 0) {

            throw new RuntimeException(
                    "Either Debit or Credit must be non-zero!");
        }

        // Both cannot have values
        if (debit.compareTo(BigDecimal.ZERO) > 0
                && credit.compareTo(BigDecimal.ZERO) > 0) {

            throw new RuntimeException(
                    "Only one of Debit or Credit should be filled!");
        }

        // Update account
        existingLine.setAccount(updatedLine.getAccount());

        // Update debit
        existingLine.setDebit(debit);

        // Update credit
        existingLine.setCredit(credit);

        // Update journal entry if provided
        if (updatedLine.getJournalEntry() != null) {
            existingLine.setJournalEntry(
                    updatedLine.getJournalEntry()
            );
        }

        return CompletableFuture.completedFuture(
                repo.save(existingLine)
        );
    }
}