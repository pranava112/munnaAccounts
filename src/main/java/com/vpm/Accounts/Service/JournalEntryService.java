package com.vpm.Accounts.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.Entity.AccountingPeriod;
import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.AccountingPeriodRepository;
import com.vpm.Accounts.Repository.FinancialYearRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Service.PurchaseService;
import com.vpm.Accounts.Service.SaleService;

@Service
public class JournalEntryService {

    private final JournalEntryRepository repo;
    private final FinancialYearRepository financialYearRepo;
    private final AccountingPeriodRepository periodRepo;

    private final PurchaseService purchaseService;
    private final SaleService saleService;

//     public JournalEntryService(
//             JournalEntryRepository repo,
//             FinancialYearRepository financialYearRepo,
//             AccountingPeriodRepository periodRepo) {

//         this.repo = repo;
//         this.financialYearRepo = financialYearRepo;
//         this.periodRepo = periodRepo;
//     }

     public JournalEntryService(
        JournalEntryRepository repo,
        FinancialYearRepository financialYearRepo,
        AccountingPeriodRepository periodRepo,
        PurchaseService purchaseService,
        SaleService saleService) {

    this.repo = repo;
    this.financialYearRepo = financialYearRepo;
    this.periodRepo = periodRepo;
    this.purchaseService = purchaseService;
    this.saleService = saleService;
}

    // =====================================================
    // SAVE JOURNAL ENTRY
    // =====================================================

    @Transactional
    public JournalEntry saveJournalEntry(
            JournalEntry journalEntry) {

        if (journalEntry == null) {
            throw new IllegalArgumentException(
                    "Journal entry cannot be null");
        }

        resolveAndValidateAccountingPeriod(
                journalEntry
        );

        validateJournalLines(journalEntry);

        return repo.save(journalEntry);
    }

    // =====================================================
    // RESOLVE FINANCIAL YEAR & ACCOUNTING PERIOD
    // =====================================================

    private void resolveAndValidateAccountingPeriod(
            JournalEntry entry) {

        LocalDate entryDate =
                entry.getEntryDate();

        if (entryDate == null) {
            throw new IllegalArgumentException(
                    "Entry date is required");
        }

        FinancialYear year =
                financialYearRepo
                        .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                entryDate,
                                entryDate
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No financial year exists for date: "
                                                + entryDate
                                ));

        if (year.isClosed()) {
            throw new IllegalStateException(
                    "Financial year "
                            + year.getName()
                            + " is closed. "
                            + "Journal entry cannot be posted."
            );
        }

        AccountingPeriod period =
                periodRepo
                        .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                year.getId(),
                                entryDate,
                                entryDate
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No accounting period exists "
                                                + "for date: "
                                                + entryDate
                                ));

        if (period.isClosed()) {
            throw new IllegalStateException(
                    "Accounting period "
                            + period.getName()
                            + " is closed. "
                            + "Journal entry cannot be posted."
            );
        }

        // -------------------------------------------------
        // Validate client supplied Financial Year
        // -------------------------------------------------

        if (entry.getFinancialYear() != null
                && entry.getFinancialYear().getId() != null
                && !entry.getFinancialYear()
                        .getId()
                        .equals(year.getId())) {

            throw new IllegalArgumentException(
                    "Journal entry financial year does not "
                            + "match entry date"
            );
        }

        // -------------------------------------------------
        // Validate client supplied Period
        // -------------------------------------------------

        if (entry.getAccountingPeriod() != null
                && entry.getAccountingPeriod().getId() != null
                && !entry.getAccountingPeriod()
                        .getId()
                        .equals(period.getId())) {

            throw new IllegalArgumentException(
                    "Journal entry accounting period does "
                            + "not match entry date"
            );
        }

        // -------------------------------------------------
        // Always use managed entities
        // -------------------------------------------------

        entry.setFinancialYear(year);
        entry.setAccountingPeriod(period);
    }

    // =====================================================
    // VALIDATE JOURNAL LINES
    // =====================================================

    private void validateJournalLines(
            JournalEntry journalEntry) {

        if (journalEntry.getLines() == null
                || journalEntry.getLines().isEmpty()) {

            throw new RuntimeException(
                    "Journal Entry must have at least one line");
        }

        BigDecimal totalDebit =
                BigDecimal.ZERO;

        BigDecimal totalCredit =
                BigDecimal.ZERO;

        List<JournalEntryLine> validLines =
                new ArrayList<>();

        for (JournalEntryLine line :
                journalEntry.getLines()) {

            if (line == null) {
                continue;
            }

            if (line.getAccount() == null) {
                throw new IllegalArgumentException(
                        "Account is required for every journal line"
                );
            }

            BigDecimal debit =
                    line.getDebit();

            BigDecimal credit =
                    line.getCredit();

            if (debit == null) {
                debit = BigDecimal.ZERO;
            }

            if (credit == null) {
                credit = BigDecimal.ZERO;
            }

            if (debit.compareTo(
                    BigDecimal.ZERO) < 0
                    ||
                    credit.compareTo(
                            BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Debit and Credit cannot be negative"
                );
            }

            if (debit.compareTo(
                    BigDecimal.ZERO) == 0
                    &&
                    credit.compareTo(
                            BigDecimal.ZERO) == 0) {

                throw new IllegalArgumentException(
                        "Debit and Credit cannot both be zero"
                );
            }

            if (debit.compareTo(
                    BigDecimal.ZERO) > 0
                    &&
                    credit.compareTo(
                            BigDecimal.ZERO) > 0) {

                throw new IllegalArgumentException(
                        "Only one of Debit or Credit "
                                + "can contain a value"
                );
            }

            line.setDebit(debit);
            line.setCredit(credit);

            line.setJournalEntry(
                    journalEntry
            );

            totalDebit =
                    totalDebit.add(debit);

            totalCredit =
                    totalCredit.add(credit);

            validLines.add(line);
        }

        if (validLines.size() < 2) {
            throw new IllegalArgumentException(
                    "A journal entry must contain "
                            + "at least two valid lines"
            );
        }

        if (totalDebit.compareTo(
                totalCredit) != 0) {

            throw new IllegalArgumentException(
                    "Debit and Credit must be equal. "
                            + "Debit: "
                            + totalDebit
                            + ", Credit: "
                            + totalCredit
            );
        }

        journalEntry.setLines(
                validLines
        );
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<JournalEntry> getAllEntries() {

        return repo.findAll();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public Optional<JournalEntry> getEntryById(
            @NonNull Long id) {

        return repo.findById(id);
    }

    // =====================================================
    // DELETE
    // =====================================================

//     @Transactional
//     public void deleteEntry(
//             @NonNull Long id) {

//         JournalEntry entry =
//                 repo.findById(id)
//                         .orElseThrow(() ->
//                                 new RuntimeException(
//                                         "Journal Entry not found"
//                                 ));

//         ensurePeriodIsOpen(entry);

//         repo.delete(entry);
//     }

        
@Transactional
public void deleteEntry(@NonNull Long id) {
    JournalEntry entry = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Journal Entry not found with ID: " + id));

    // Prevent modification of entries in closed periods.
    ensurePeriodIsOpen(entry);

    String voucherNumber = entry.getVoucherNumber();

    if (voucherNumber != null && !voucherNumber.isBlank()) {

        // Purchase voucher: delete purchase, journal, tax record and
        // recalculate affected stock through PurchaseService.
        if (voucherNumber.startsWith("PURV-")) {
            boolean purchaseDeleted =
                    purchaseService.deleteByVoucherNumber(voucherNumber);

            if (purchaseDeleted) {
                return;
            }
        }

        // Sales voucher: delete sale, journal, tax record and
        // recalculate affected stock through SaleService.
        if (voucherNumber.startsWith("SV-")) {
            boolean saleDeleted =
                    saleService.deleteByVoucherNumber(voucherNumber);

            if (saleDeleted) {
                return;
            }
        }
    }

    // Manual journal entry, or no corresponding source transaction.
    repo.delete(entry);
    repo.flush();
}


    // =====================================================
    // CHECK WHETHER ENTRY CAN BE MODIFIED
    // =====================================================

    private void ensurePeriodIsOpen(
            JournalEntry entry) {

        FinancialYear year =
                entry.getFinancialYear();

        AccountingPeriod period =
                entry.getAccountingPeriod();

        if (year == null || period == null) {

            throw new IllegalStateException(
                    "Journal entry is not linked "
                            + "to a financial year and accounting period"
            );
        }

        if (year.isClosed()) {

            throw new IllegalStateException(
                    "Financial year "
                            + year.getName()
                            + " is closed. "
                            + "Journal entry cannot be modified."
            );
        }

        if (period.isClosed()) {

            throw new IllegalStateException(
                    "Accounting period "
                            + period.getName()
                            + " is closed. "
                            + "Journal entry cannot be modified."
            );
        }
    }

    // =====================================================
    // ASYNC SAVE
    // =====================================================

    @Async("accountExecutor")
    @Transactional
    public CompletableFuture<JournalEntry>
    saveJournalEntryAsync(
            JournalEntry journalEntry) {

        return CompletableFuture.completedFuture(
                saveJournalEntry(journalEntry)
        );
    }

    // =====================================================
    // ASYNC GET ALL
    // =====================================================

    @Async("accountExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<List<JournalEntry>>
    getAllEntriesAsync() {

        return CompletableFuture.completedFuture(
                getAllEntries()
        );
    }

    // =====================================================
    // ASYNC GET BY ID
    // =====================================================

    @Async("accountExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Optional<JournalEntry>>
    getEntryByIdAsync(
            @NonNull Long id) {

        return CompletableFuture.completedFuture(
                getEntryById(id)
        );
    }

    // =====================================================
    // ASYNC DELETE
    // =====================================================

    @Async("accountExecutor")
    @Transactional
    public CompletableFuture<Void>
    deleteEntryAsync(
            @NonNull Long id) {

        deleteEntry(id);

        return CompletableFuture.completedFuture(
                null
        );
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Transactional
    public JournalEntry updateJournalEntry(
            @NonNull Long id,
            JournalEntry updatedEntry) {

        JournalEntry existingEntry =
                repo.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Journal Entry not found with id: "
                                                + id));

        // Existing entry must still be editable
        ensurePeriodIsOpen(
                existingEntry
        );

        if (updatedEntry == null) {
            throw new IllegalArgumentException(
                    "Updated journal entry cannot be null"
            );
        }

        if (updatedEntry.getEntryDate()
                == null) {

            throw new IllegalArgumentException(
                    "Entry date is required"
            );
        }

        /*
         * Resolve the NEW accounting period.
         *
         * This is important when somebody edits:
         *
         * 2026-05-10
         *
         * into:
         *
         * 2026-06-10
         */

        resolveAndValidateAccountingPeriod(
                updatedEntry
        );

        validateJournalLines(
                updatedEntry
        );

        existingEntry.setEntryDate(
                updatedEntry.getEntryDate()
        );

        existingEntry.setDescription(
                updatedEntry.getDescription()
        );

        existingEntry.setFinancialYear(
                updatedEntry.getFinancialYear()
        );

        existingEntry.setAccountingPeriod(
                updatedEntry.getAccountingPeriod()
        );

        existingEntry.getLines().clear();

        for (JournalEntryLine line :
                updatedEntry.getLines()) {

            line.setJournalEntry(
                    existingEntry
            );

            existingEntry.getLines()
                    .add(line);
        }

        return repo.save(
                existingEntry
        );
    }

    // =====================================================
    // ASYNC UPDATE
    // =====================================================

    @Async("accountExecutor")
    @Transactional
    public CompletableFuture<JournalEntry> updateJournalEntryAsync(
            @NonNull Long id,
            JournalEntry updatedEntry) {

        return CompletableFuture.completedFuture(
                updateJournalEntry(
                        id,
                        updatedEntry
                )
        );
    }
}