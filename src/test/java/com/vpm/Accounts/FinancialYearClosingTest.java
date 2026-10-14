package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.AccountingPeriod;
import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.AccountingPeriodRepository;
import com.vpm.Accounts.Repository.FinancialYearRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Service.FinancialYearService;

@ExtendWith(MockitoExtension.class)
class FinancialYearClosingTest {

    @Mock
    private FinancialYearRepository financialYearRepository;

    @Mock
    private AccountingPeriodRepository accountingPeriodRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @InjectMocks
    private FinancialYearService financialYearService;

    @Test
    void closingYearCreatesProfitTransferAndBalanceCarryForwardJournals() {
        FinancialYear year = new FinancialYear();
        year.setId(17L);
        year.setName("2025-26");
        year.setStartDate(LocalDate.of(2025, 4, 1));
        year.setEndDate(LocalDate.of(2026, 3, 31));
        year.setStatus(FinancialYear.Status.OPEN);

        AccountingPeriod closedPeriod = new AccountingPeriod();
        closedPeriod.setId(8L);
        closedPeriod.setStatus(AccountingPeriod.Status.CLOSED);
        closedPeriod.setStartDate(year.getStartDate());
        closedPeriod.setEndDate(year.getEndDate());

        Account capital = account("Capital", AccountType.CAPITAL);
        Account sales = account("Sales", AccountType.SALES);
        Account purchases = account("Purchases", AccountType.PURCHASES);
        Account cash = account("Cash", AccountType.ASSET);

        JournalEntry operatingEntry = new JournalEntry();
        operatingEntry.setEntryDate(LocalDate.of(2025, 5, 1));
        operatingEntry.setLines(List.of(
                line(sales, "0", "100", operatingEntry),
                line(purchases, "60", "0", operatingEntry),
                line(cash, "40", "0", operatingEntry)));

        when(financialYearRepository.findById(17L)).thenReturn(Optional.of(year));
        when(accountingPeriodRepository.findByFinancialYear_IdOrderByStartDateAsc(17L))
                .thenReturn(List.of(closedPeriod));
        when(accountingPeriodRepository
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        17L, year.getEndDate(), year.getEndDate()))
                .thenReturn(Optional.of(closedPeriod));
        when(financialYearRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        year.getEndDate(), year.getEndDate()))
                .thenReturn(Optional.of(year));
        when(accountRepository.findAll()).thenReturn(List.of(capital));
        when(journalEntryRepository.findAll()).thenReturn(List.of(operatingEntry));
        when(journalEntryRepository.findByVoucherNumber("FY-CD-17"))
                .thenReturn(Optional.empty());
        when(journalEntryRepository.save(any(JournalEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(financialYearRepository.save(any(FinancialYear.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FinancialYear closed = financialYearService.closeFinancialYear(17L);

        assertEquals(FinancialYear.Status.CLOSED, closed.getStatus());
        ArgumentCaptor<JournalEntry> savedJournals =
                ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository, org.mockito.Mockito.times(2))
                .save(savedJournals.capture());
        assertEquals("FY-PL-CLOSE-17", savedJournals.getAllValues().get(0).getVoucherNumber());
        assertEquals("FY-CD-17", savedJournals.getAllValues().get(1).getVoucherNumber());
        assertEquals(year.getEndDate(), savedJournals.getAllValues().get(1).getEntryDate());
    }

    private Account account(String name, AccountType type) {
        Account account = new Account();
        account.setName(name);
        account.setType(type);
        return account;
    }

    private JournalEntryLine line(
            Account account,
            String debit,
            String credit,
            JournalEntry journal) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(new BigDecimal(debit));
        line.setCredit(new BigDecimal(credit));
        line.setJournalEntry(journal);
        return line;
    }
}
