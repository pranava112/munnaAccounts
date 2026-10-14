package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vpm.Accounts.DTO.TradingAccountDTO;
import com.vpm.Accounts.DTO.BalanceSheetDTO;
import com.vpm.Accounts.DTO.ProfitLossDTO;
import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Repository.FinancialYearRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.PurchaseRepository;
import com.vpm.Accounts.Repository.SaleRepository;
import com.vpm.Accounts.Service.ReportService;

@ExtendWith(MockitoExtension.class)
class ReportServiceFinancialYearTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private FinancialYearRepository financialYearRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void tradingAccountOnlyIncludesJournalsWithinSelectedFinancialYear() {
        FinancialYear financialYear = new FinancialYear();
        financialYear.setId(44L);
        financialYear.setName("2025-26");
        financialYear.setStartDate(LocalDate.of(2025, 4, 1));
        financialYear.setEndDate(LocalDate.of(2026, 3, 31));

        Account purchases = account("Purchases", AccountType.PURCHASES);
        Account sales = account("Sales", AccountType.SALES);
        Account cash = account("Cash", AccountType.ASSET);

        when(financialYearRepository.findById(44L))
                .thenReturn(Optional.of(financialYear));
        when(journalEntryRepository.findAll()).thenReturn(List.of(
                entry(LocalDate.of(2025, 2, 2),
                        line(purchases, "700", "0"), line(cash, "0", "700")),
                entry(LocalDate.of(2025, 5, 2),
                        line(purchases, "60", "0"), line(cash, "0", "60")),
                entry(LocalDate.of(2026, 2, 2),
                        line(cash, "100", "0"), line(sales, "0", "100"))));
        when(productRepository.findAll()).thenReturn(List.of());
        when(saleRepository.findAll()).thenReturn(List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of());

        TradingAccountDTO trading = reportService.getTradingAccount(44L);

        assertEquals(0, new BigDecimal("60").compareTo(trading.getPurchases()));
        assertEquals(0, new BigDecimal("100").compareTo(trading.getSales()));
        assertEquals(0, new BigDecimal("40").compareTo(trading.getGrossProfit()));
    }

    @Test
    void stockAddedAfterPriorYearDoesNotAppearInThatYearsTradingAccount() {
        FinancialYear priorYear = new FinancialYear();
        priorYear.setId(43L);
        priorYear.setName("2024-25");
        priorYear.setStartDate(LocalDate.of(2024, 4, 1));
        priorYear.setEndDate(LocalDate.of(2025, 3, 31));

        Product product = new Product();
        product.setId(2L);
        product.setName("Telugu book");
        product.setPurchasePrice(new BigDecimal("55"));
        product.setOpeningStockQuantity(new BigDecimal("5"));
        product.setCurrentStockQuantity(new BigDecimal("5"));
        product.setTenantId(1L);

        when(financialYearRepository.findById(43L))
                .thenReturn(Optional.of(priorYear));
        when(journalEntryRepository.findAll()).thenReturn(List.of());
        when(journalEntryRepository.findByVoucherNumber("OB-STOCK-1-2"))
                .thenReturn(Optional.of(openingStockEntry(
                        LocalDate.of(2026, 10, 2), "275")));
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(saleRepository.findAll()).thenReturn(List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of());

        TradingAccountDTO trading = reportService.getTradingAccount(43L);

        assertEquals(BigDecimal.ZERO, trading.getOpeningStock());
        assertEquals(BigDecimal.ZERO, trading.getClosingStock());
    }

    @Test
    void stockIntroducedDuringYearIsExcludedFromOpeningAndNotCountedAsProfit() {
        FinancialYear year = new FinancialYear();
        year.setId(45L);
        year.setName("2026-27");
        year.setStartDate(LocalDate.of(2026, 4, 1));
        year.setEndDate(LocalDate.of(2027, 3, 31));

        Product product = new Product();
        product.setId(2L);
        product.setName("Telugu book");
        product.setPurchasePrice(new BigDecimal("55"));
        product.setOpeningStockQuantity(new BigDecimal("10"));
        product.setCurrentStockQuantity(new BigDecimal("10"));
        product.setTenantId(1L);

        JournalEntry openingStock = openingStockEntry(
                LocalDate.of(2026, 10, 2), "550");

        when(financialYearRepository.findById(45L))
                .thenReturn(Optional.of(year));
        when(journalEntryRepository.findAll()).thenReturn(List.of(openingStock));
        when(journalEntryRepository.findByVoucherNumber("OB-STOCK-1-2"))
                .thenReturn(Optional.of(openingStock));
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(saleRepository.findAll()).thenReturn(List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of());

        TradingAccountDTO trading = reportService.getTradingAccount(45L);

        assertEquals(0, new BigDecimal("0").compareTo(trading.getOpeningStock()));
        assertEquals(0, new BigDecimal("550").compareTo(trading.getStockIntroduced()));
        assertEquals(0, new BigDecimal("550").compareTo(trading.getClosingStock()));
        assertEquals(0, new BigDecimal("0").compareTo(trading.getGrossProfit()));
    }

    @Test
    void financialStatementsClassifyAccountsByNewAccountGroups() {
        FinancialYear year = new FinancialYear();
        year.setId(46L);
        year.setName("2026-27");
        year.setStartDate(LocalDate.of(2026, 4, 1));
        year.setEndDate(LocalDate.of(2027, 3, 31));

        Account purchases = groupedAccount("Purchases", AccountType.EXPENSE, "PURCHASES");
        Account sales = groupedAccount("Sales", AccountType.INCOME, "SALES");
        Account directIncome = groupedAccount("Service income", AccountType.INCOME, "DIRECT_INCOME");
        Account directExpense = groupedAccount("Freight", AccountType.EXPENSE, "DIRECT_EXPENSES");
        Account indirectIncome = groupedAccount("Interest received", AccountType.INCOME, "INDIRECT_INCOME");
        Account indirectExpense = groupedAccount("Rent", AccountType.EXPENSE, "INDIRECT_EXPENSES");

        when(financialYearRepository.findById(46L)).thenReturn(Optional.of(year));
        when(journalEntryRepository.findAll()).thenReturn(List.of(
                entry(LocalDate.of(2026, 6, 1),
                        line(purchases, "100", "0"),
                        line(sales, "0", "300"),
                        line(directIncome, "0", "20"),
                        line(directExpense, "10", "0"),
                        line(indirectIncome, "0", "30"),
                        line(indirectExpense, "5", "0"))));
        when(productRepository.findAll()).thenReturn(List.of());
        when(saleRepository.findAll()).thenReturn(List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of());

        TradingAccountDTO trading = reportService.getTradingAccount(46L);
        ProfitLossDTO profitLoss = reportService.getProfitLoss(46L);

        assertEquals(0, new BigDecimal("100").compareTo(trading.getPurchases()));
        assertEquals(0, new BigDecimal("300").compareTo(trading.getSales()));
        assertEquals(0, new BigDecimal("20").compareTo(trading.getTotalDirectIncome()));
        assertEquals(0, new BigDecimal("10").compareTo(trading.getTotalDirectExpenses()));
        assertEquals(0, new BigDecimal("210").compareTo(trading.getGrossProfit()));
        assertEquals(0, new BigDecimal("30").compareTo(profitLoss.getTotalIncome()));
        assertEquals(0, new BigDecimal("5").compareTo(profitLoss.getTotalExpense()));
        assertEquals(0, new BigDecimal("235").compareTo(profitLoss.getNetProfit()));
    }

    @Test
    void balanceSheetClassifiesNewAssetLiabilityAndCapitalGroups() {
        FinancialYear year = new FinancialYear();
        year.setId(47L);
        year.setName("2026-27");
        year.setStartDate(LocalDate.of(2026, 4, 1));
        year.setEndDate(LocalDate.of(2027, 3, 31));

        Account fixedAssets = groupedAccount("Equipment", AccountType.ASSET, "FIXED_ASSETS");
        Account loan = groupedAccount("Business loan", AccountType.LIABILITY, "BANK_LOANS");
        Account retainedEarnings = groupedAccount("Retained earnings", AccountType.CAPITAL, "RETAINED_EARNINGS");
        Account drawings = groupedAccount("Owner drawings", AccountType.CAPITAL, "DRAWINGS");
        Account indirectIncome = groupedAccount("Interest received", AccountType.INCOME, "INDIRECT_INCOME");
        Account indirectExpense = groupedAccount("Rent", AccountType.EXPENSE, "INDIRECT_EXPENSES");

        when(financialYearRepository.findById(47L)).thenReturn(Optional.of(year));
        when(journalEntryRepository.findAll()).thenReturn(List.of(
                entry(LocalDate.of(2026, 6, 1),
                        line(fixedAssets, "100", "0"),
                        line(loan, "0", "30"),
                        line(retainedEarnings, "0", "60"),
                        line(indirectIncome, "0", "20"),
                        line(indirectExpense, "10", "0"),
                        line(drawings, "5", "0"),
                        line(fixedAssets, "0", "5"))));
        when(productRepository.findAll()).thenReturn(List.of());
        when(saleRepository.findAll()).thenReturn(List.of());
        when(purchaseRepository.findAll()).thenReturn(List.of());

        BalanceSheetDTO balanceSheet = reportService.getBalanceSheet(47L);

        assertEquals(0, new BigDecimal("95").compareTo(balanceSheet.getAssets().get("Equipment")));
        assertEquals(0, new BigDecimal("30").compareTo(balanceSheet.getLiabilities().get("Business loan")));
        assertEquals(0, new BigDecimal("60").compareTo(balanceSheet.getCapitalDetails().get("Retained earnings")));
        assertEquals(0, new BigDecimal("5").compareTo(balanceSheet.getCapitalDetails().get("Less: Drawings")));
        assertEquals(0, new BigDecimal("65").compareTo(balanceSheet.getLiabilities().get("Capital")));
    }

    private JournalEntry openingStockEntry(LocalDate date, String value) {
        Account openingStock = account("Opening Stock", AccountType.OPENING_STOCK);
        Account capital = account("Capital", AccountType.CAPITAL);
        JournalEntry entry = entry(
                date,
                line(openingStock, value, "0"),
                line(capital, "0", value));
        entry.setVoucherNumber("OB-STOCK-1-2");
        return entry;
    }

    private Account account(String name, AccountType type) {
        Account account = new Account();
        account.setName(name);
        account.setType(type);
        return account;
    }

    private Account groupedAccount(String name, AccountType type, String group) {
        Account account = account(name, type);
        account.setAccountGroup(group);
        return account;
    }

    private JournalEntry entry(LocalDate date, JournalEntryLine... lines) {
        JournalEntry entry = new JournalEntry();
        entry.setEntryDate(date);
        entry.setLines(List.of(lines));
        for (JournalEntryLine line : lines) {
            line.setJournalEntry(entry);
        }
        return entry;
    }

    private JournalEntryLine line(
            Account account,
            String debit,
            String credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(account);
        line.setDebit(new BigDecimal(debit));
        line.setCredit(new BigDecimal(credit));
        return line;
    }
}
