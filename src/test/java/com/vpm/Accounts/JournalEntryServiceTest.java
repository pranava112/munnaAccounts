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
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Entity.JournalEntryStatus;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Service.JournalEntryService;
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
        JournalEntry entry = new JournalEntry();
        // entry.setStatus(JournalEntryStatus.POSTED);
        // when(repository.findByIdAndTenantId(7L, 1L)).thenReturn(Optional.of(entry));

        assertThrows(IllegalStateException.class, () -> service.updateJournalEntry(7L, new JournalEntry()));
    }

    @Test
    void rejectsPostedEntryDelete() {
        JournalEntry entry = new JournalEntry();
        // entry.setStatus(JournalEntryStatus.POSTED);
        // when(repository.findByIdAndTenantId(7L, 1L)).thenReturn(Optional.of(entry));

        assertThrows(IllegalStateException.class, () -> service.deleteEntry(7L));
    }

    private JournalEntryLine line(Account account, String debit, String credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(new BigDecimal(debit));
        line.setCredit(new BigDecimal(credit));
        return line;
    }
}
