package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountingPeriod;
import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Service.JournalEntryService;
import com.vpm.Accounts.security.TenantContext;
import com.vpm.Accounts.security.TenantContext;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {
    @Mock
    private JournalEntryRepository repository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private JournalEntryService service;

    @BeforeEach
    void setUp() {
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void rejectsUnbalancedEntry() {
        Account account = new Account();
        account.setId(10L);
        JournalEntryLine debit = line(account, "100.00", "0.00");
        JournalEntryLine credit = line(account, "0.00", "90.00");
        JournalEntry entry = new JournalEntry();
        entry.setLines(List.of(debit, credit));
        // when(accountRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> service.saveJournalEntry(entry));
    }

    @Test
    void rejectsPostedEntryUpdate() {
        JournalEntry entry = entryInClosedPeriod();
        when(repository.findById(7L)).thenReturn(Optional.of(entry));

        assertThrows(IllegalStateException.class, () -> service.updateJournalEntry(7L, new JournalEntry()));
    }

    @Test
    void rejectsPostedEntryDelete() {
        JournalEntry entry = entryInClosedPeriod();
        when(repository.findById(7L)).thenReturn(Optional.of(entry));

        assertThrows(IllegalStateException.class, () -> service.deleteEntry(7L));
    }

    private JournalEntry entryInClosedPeriod() {
        FinancialYear year = new FinancialYear();
        year.setName("2025-26");
        year.setStatus(FinancialYear.Status.OPEN);

        AccountingPeriod period = new AccountingPeriod();
        period.setName("February 2026");
        period.setStatus(AccountingPeriod.Status.CLOSED);
        period.setFinancialYear(year);

        JournalEntry entry = new JournalEntry();
        entry.setFinancialYear(year);
        entry.setAccountingPeriod(period);
        return entry;
    }

    private JournalEntryLine line(Account account, String debit, String credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(new BigDecimal(debit));
        line.setCredit(new BigDecimal(credit));
        return line;
    }
}
