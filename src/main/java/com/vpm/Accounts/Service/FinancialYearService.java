package com.vpm.Accounts.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class FinancialYearService {

    private final FinancialYearRepository financialYearRepo;
    private final AccountingPeriodRepository periodRepo;
    private final AccountRepository accountRepo;
    private final JournalEntryRepository journalRepo;

    public FinancialYearService(
            FinancialYearRepository financialYearRepo,
            AccountingPeriodRepository periodRepo,
            AccountRepository accountRepo,
            JournalEntryRepository journalRepo) {

        this.financialYearRepo = financialYearRepo;
        this.periodRepo = periodRepo;
        this.accountRepo = accountRepo;
        this.journalRepo = journalRepo;
    }

    // =====================================================
    // CREATE FINANCIAL YEAR
    // =====================================================

    @Transactional
    public FinancialYear createFinancialYear(
            FinancialYear request) {

        validateFinancialYearDates(request);

        String name = request.getName();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Financial year name is required");
        }

        name = name.trim();

        if (financialYearRepo.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Financial year already exists: " + name);
        }

        List<FinancialYear> overlappingYears =
                financialYearRepo
                        .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                request.getEndDate(),
                                request.getStartDate()
                        );

        if (!overlappingYears.isEmpty()) {
            throw new IllegalStateException(
                    "Financial year overlaps with an existing financial year");
        }

        FinancialYear year = new FinancialYear();

        year.setName(name);
        year.setStartDate(request.getStartDate());
        year.setEndDate(request.getEndDate());
        year.setStatus(FinancialYear.Status.OPEN);
        year.setClosedAt(null);

        year = financialYearRepo.save(year);

        createDefaultMonthlyPeriods(year);

        FinancialYear createdYear = year;
        financialYearRepo.findAllByOrderByStartDateDesc().stream()
                .filter(previous -> previous.isClosed()
                        && previous.getEndDate().plusDays(1).equals(createdYear.getStartDate()))
                .findFirst()
                .ifPresent(previous -> createOpeningBalanceEntry(previous, createdYear));

        return financialYearRepo.findById(year.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Failed to reload financial year"));
    }

    // =====================================================
    // CREATE DEFAULT MONTHLY PERIODS
    // =====================================================

    private void createDefaultMonthlyPeriods(
            FinancialYear year) {

        LocalDate cursor =
                year.getStartDate();

        while (!cursor.isAfter(year.getEndDate())) {

            YearMonth month =
                    YearMonth.from(cursor);

            LocalDate periodStart =
                    cursor;

            LocalDate periodEnd =
                    month.atEndOfMonth();

            if (periodEnd.isAfter(year.getEndDate())) {
                periodEnd =
                        year.getEndDate();
            }

            AccountingPeriod period =
                    new AccountingPeriod();

            String monthName =
                    month.getMonth()
                            .getDisplayName(
                                    TextStyle.FULL,
                                    Locale.ENGLISH
                            );

            period.setName(
                    monthName + " " + month.getYear()
            );

            period.setCode(
                    String.format(
                            "%d-%02d",
                            month.getYear(),
                            month.getMonthValue()
                    )
            );

            period.setStartDate(periodStart);
            period.setEndDate(periodEnd);
            period.setStatus(
                    AccountingPeriod.Status.OPEN
            );

            period.setFinancialYear(year);

            periodRepo.save(period);

            cursor =
                    periodEnd.plusDays(1);
        }
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @Transactional(readOnly = true)
    public List<FinancialYear> getAllFinancialYears() {

        return financialYearRepo
                .findAllByOrderByStartDateDesc();
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public FinancialYear getFinancialYearById(
            Long id) {

        return financialYearRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Financial year not found with id: "
                                        + id));
    }

    // =====================================================
    // GET BY DATE
    // =====================================================

    @Transactional(readOnly = true)
    public FinancialYear getFinancialYearByDate(
            LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Date is required");
        }

        return financialYearRepo
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        date,
                        date
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No financial year exists for date: "
                                        + date));
    }

    // =====================================================
    // CLOSE FINANCIAL YEAR
    // =====================================================

    @Transactional
    public FinancialYear closeFinancialYear(
            Long id) {

        FinancialYear year =
                getFinancialYearById(id);

        if (year.isClosed()) {
            throw new IllegalStateException(
                    "Financial year is already closed");
        }

        List<AccountingPeriod> periods =
                periodRepo
                        .findByFinancialYear_IdOrderByStartDateAsc(
                                id
                        );

        if (periods.isEmpty()) {
            throw new IllegalStateException(
                    "Financial year has no accounting periods");
        }

        boolean hasOpenPeriod =
                periods.stream()
                        .anyMatch(
                                AccountingPeriod::isOpen
                        );

        if (hasOpenPeriod) {
            throw new IllegalStateException(
                    "All accounting periods must be closed "
                            + "before closing the financial year");
        }

        createYearEndClosingEntries(year);

        year.setStatus(
                FinancialYear.Status.CLOSED
        );

        year.setClosedAt(
                LocalDateTime.now()
        );

        return financialYearRepo.save(year);
    }

    // =====================================================
    // REOPEN FINANCIAL YEAR
    // =====================================================

    @Transactional
    public FinancialYear reopenFinancialYear(
            Long id) {

        FinancialYear year =
                getFinancialYearById(id);

        if (year.isOpen()) {
            throw new IllegalStateException(
                    "Financial year is already open");
        }

        removeNextYearOpeningEntryIfUnused(year);
        journalRepo.findByVoucherNumber(closingProfitVoucher(year.getId()))
                .ifPresent(journalRepo::delete);
        journalRepo.findByVoucherNumber(closingBalanceVoucher(year.getId()))
                .ifPresent(journalRepo::delete);

        year.setStatus(
                FinancialYear.Status.OPEN
        );

        year.setClosedAt(null);

        return financialYearRepo.save(year);
    }

    // =====================================================
    // CREATE NEXT FINANCIAL YEAR
    // =====================================================

    @Transactional
    public FinancialYear createNextFinancialYear(
            Long previousYearId) {

        FinancialYear previous =
                getFinancialYearById(
                        previousYearId
                );

        if (!previous.isClosed()) {
            throw new IllegalStateException(
                    "Previous financial year must be closed "
                            + "before creating the next financial year"
            );
        }

        LocalDate nextStart =
                previous.getEndDate()
                        .plusDays(1);

        LocalDate nextEnd =
                nextStart
                        .plusYears(1)
                        .minusDays(1);

        String name =
                String.format(
                        "%d-%02d",
                        nextStart.getYear(),
                        nextEnd.getYear() % 100
                );

        FinancialYear request =
                new FinancialYear();

        request.setName(name);
        request.setStartDate(nextStart);
        request.setEndDate(nextEnd);

        return createFinancialYear(request);
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateFinancialYearDates(
            FinancialYear year) {

        if (year.getStartDate() == null) {
            throw new IllegalArgumentException(
                    "Financial year start date is required");
        }

        if (year.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "Financial year end date is required");
        }

        if (year.getStartDate()
                .isAfter(year.getEndDate())) {

            throw new IllegalArgumentException(
                    "Financial year start date cannot be "
                            + "after end date");
        }

        if (year.getStartDate().getMonthValue() != 4
                || year.getStartDate().getDayOfMonth() != 1
                || year.getEndDate().getMonthValue() != 3
                || year.getEndDate().getDayOfMonth() != 31
                || year.getEndDate().getYear() != year.getStartDate().getYear() + 1) {
            throw new IllegalArgumentException(
                    "Financial years must run from April 1 through March 31");
        }
    }

    private void createYearEndClosingEntries(FinancialYear year) {
        if (journalRepo.findByVoucherNumber(closingBalanceVoucher(year.getId())).isPresent()) {
            throw new IllegalStateException(
                    "Year-end closing entries already exist for " + year.getName());
        }

        Map<Account, BigDecimal> balances = balancesForYear(year);
        List<JournalEntryLine> profitClosingLines = new ArrayList<>();
        BigDecimal nominalBalance = BigDecimal.ZERO;

        for (Map.Entry<Account, BigDecimal> entry : balances.entrySet()) {
            if (isPermanentAccount(entry.getKey().getType())) {
                continue;
            }
            BigDecimal balance = entry.getValue();
            if (balance.signum() == 0) {
                continue;
            }
            profitClosingLines.add(line(entry.getKey(), balance.negate()));
            nominalBalance = nominalBalance.add(balance);
        }

        Account capital = null;
        if (nominalBalance.signum() != 0) {
            capital = accountRepo.findAll().stream()
                    .filter(account -> account.getType() == AccountType.CAPITAL)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "A Capital account is required to close the year's profit or loss"));
            profitClosingLines.add(line(capital, nominalBalance));
        }

        if (!profitClosingLines.isEmpty()) {
            saveJournal(
                    closingProfitVoucher(year.getId()),
                    "Financial year profit/loss closing c/d - " + year.getName(),
                    year.getEndDate(),
                    profitClosingLines);
        }

        Map<Account, BigDecimal> carriedBalances = new LinkedHashMap<>();
        for (Map.Entry<Account, BigDecimal> entry : balances.entrySet()) {
            if (isPermanentAccount(entry.getKey().getType())) {
                carriedBalances.put(entry.getKey(), entry.getValue());
            }
        }
        if (capital != null) {
            carriedBalances.merge(capital, nominalBalance, BigDecimal::add);
        }

        List<JournalEntryLine> balanceClosingLines = new ArrayList<>();
        for (Map.Entry<Account, BigDecimal> entry : carriedBalances.entrySet()) {
            if (entry.getValue().signum() != 0) {
                balanceClosingLines.add(line(entry.getKey(), entry.getValue().negate()));
            }
        }
        if (!balanceClosingLines.isEmpty()) {
            saveJournal(
                    closingBalanceVoucher(year.getId()),
                    "Financial year balance c/d - " + year.getName(),
                    year.getEndDate(),
                    balanceClosingLines);
        }
    }

    private Map<Account, BigDecimal> balancesForYear(FinancialYear year) {
        Map<Account, BigDecimal> balances = new LinkedHashMap<>();
        for (JournalEntry journal : journalRepo.findAll()) {
            if (journal.getEntryDate() == null
                    || journal.getEntryDate().isBefore(year.getStartDate())
                    || journal.getEntryDate().isAfter(year.getEndDate())
                    || isClosingEntry(journal)) {
                continue;
            }
            for (JournalEntryLine journalLine : journal.getLines()) {
                if (journalLine.getAccount() == null) {
                    continue;
                }
                BigDecimal debit = journalLine.getDebit() == null
                        ? BigDecimal.ZERO : journalLine.getDebit();
                BigDecimal credit = journalLine.getCredit() == null
                        ? BigDecimal.ZERO : journalLine.getCredit();
                balances.merge(
                        journalLine.getAccount(),
                        debit.subtract(credit),
                        BigDecimal::add);
            }
        }
        return balances;
    }

    private void createOpeningBalanceEntry(
            FinancialYear previous,
            FinancialYear next) {
        String openingVoucher = openingBalanceVoucher(next.getId());
        if (journalRepo.findByVoucherNumber(openingVoucher).isPresent()) {
            return;
        }

        JournalEntry closing = journalRepo.findByVoucherNumber(
                closingBalanceVoucher(previous.getId())).orElse(null);
        if (closing == null) {
            return;
        }

        List<JournalEntryLine> openingLines = new ArrayList<>();
        for (JournalEntryLine closedLine : closing.getLines()) {
            JournalEntryLine openingLine = line(
                    closedLine.getAccount(),
                    amount(closedLine.getCredit()).subtract(amount(closedLine.getDebit())));
            openingLines.add(openingLine);
        }
        if (!openingLines.isEmpty()) {
            saveJournal(
                    openingVoucher,
                    "Financial year opening balance b/d - " + next.getName(),
                    next.getStartDate(),
                    openingLines);
        }
    }

    private void removeNextYearOpeningEntryIfUnused(FinancialYear year) {
        LocalDate nextStart = year.getEndDate().plusDays(1);
        FinancialYear nextYear = financialYearRepo.findAllByOrderByStartDateDesc().stream()
                .filter(candidate -> candidate.getStartDate().equals(nextStart))
                .findFirst()
                .orElse(null);
        if (nextYear == null) {
            return;
        }

        String openingVoucher = openingBalanceVoucher(nextYear.getId());
        boolean hasOtherEntries = journalRepo.findAll().stream()
                .anyMatch(entry -> entry.getEntryDate() != null
                        && !entry.getEntryDate().isBefore(nextYear.getStartDate())
                        && !entry.getEntryDate().isAfter(nextYear.getEndDate())
                        && !openingVoucher.equals(entry.getVoucherNumber()));
        if (hasOtherEntries) {
            throw new IllegalStateException(
                    "Cannot reopen this year because the next financial year already has transactions");
        }
        journalRepo.findByVoucherNumber(openingVoucher)
                .ifPresent(journalRepo::delete);
    }

    private void saveJournal(
            String voucher,
            String description,
            LocalDate entryDate,
            List<JournalEntryLine> lines) {
        JournalEntry journal = new JournalEntry();
        journal.setVoucherNumber(voucher);
        journal.setDescription(description);
        journal.setEntryDate(entryDate);
        FinancialYear year = financialYearRepo
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        entryDate, entryDate)
                .orElseThrow(() -> new IllegalStateException(
                        "No financial year exists for closing entry date: " + entryDate));
        journal.setFinancialYear(year);
        periodRepo
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        year.getId(), entryDate, entryDate)
                .ifPresent(journal::setAccountingPeriod);
        for (JournalEntryLine line : lines) {
            line.setJournalEntry(journal);
        }
        journal.setLines(lines);
        journalRepo.save(journal);
    }

    private JournalEntryLine line(Account account, BigDecimal debitMinusCredit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(debitMinusCredit.signum() > 0
                ? debitMinusCredit : BigDecimal.ZERO);
        line.setCredit(debitMinusCredit.signum() < 0
                ? debitMinusCredit.abs() : BigDecimal.ZERO);
        return line;
    }

    private boolean isPermanentAccount(AccountType type) {
        return type == AccountType.ASSET
                || type == AccountType.LIABILITY
                || type == AccountType.CAPITAL
                || type == AccountType.EQUITY
                || type == AccountType.DEBTORS
                || type == AccountType.CREDITORS;
    }

    private boolean isClosingEntry(JournalEntry entry) {
        String voucher = entry.getVoucherNumber();
        return voucher != null && (voucher.startsWith("FY-PL-CLOSE-")
                || voucher.startsWith("FY-CD-"));
    }

    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String closingProfitVoucher(Long yearId) {
        return "FY-PL-CLOSE-" + yearId;
    }

    private String closingBalanceVoucher(Long yearId) {
        return "FY-CD-" + yearId;
    }

    private String openingBalanceVoucher(Long yearId) {
        return "FY-BF-" + yearId;
    }
}