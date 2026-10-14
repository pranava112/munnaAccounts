package com.vpm.Accounts.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.DTO.BalanceSheetDTO;
import com.vpm.Accounts.DTO.ProfitLossDTO;
import com.vpm.Accounts.DTO.TradingAccountDTO;
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

@Service
public class ReportService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private JournalEntryRepository journalRepo;

    @Autowired
    private FinancialYearRepository financialYearRepo;

    @Autowired
    private SaleRepository saleRepo;

    @Autowired
    private PurchaseRepository purchaseRepo;

    // =========================================================
    // CLOSING STOCK
    // =========================================================

    private BigDecimal calculateStockValueAsOf(
            LocalDate date,
            Map<String, BigDecimal> stockDetails) {

        BigDecimal total = BigDecimal.ZERO;
        List<com.vpm.Accounts.Entity.Sale> sales = saleRepo.findAll();
        List<com.vpm.Accounts.Entity.Purchase> purchases = purchaseRepo.findAll();

        for (Product p : productRepo.findAll()) {

            if (p == null) {
                continue;
            }

            String productName = p.getName();

            if (productName == null || productName.isBlank()) {
                productName = "Unnamed Product";
            }

            // -------------------------------------------------
            // GET STOCK
            // -------------------------------------------------

            BigDecimal stock = p.getCurrentStockQuantity();

            if (stock == null) {
                stock = BigDecimal.ZERO;
            }
            for (com.vpm.Accounts.Entity.Sale sale : sales) {
                if (sale.getDate() == null || !sale.getDate().isAfter(date)
                        || sale.getItems() == null) {
                    continue;
                }
                for (com.vpm.Accounts.Entity.SaleItem item : sale.getItems()) {
                    if (item.getProduct() != null
                            && p.getId().equals(item.getProduct().getId())) {
                        stock = stock.add(BigDecimal.valueOf(item.getQuantity()));
                    }
                }
            }
            for (com.vpm.Accounts.Entity.Purchase purchase : purchases) {
                if (purchase.getDate() == null || !purchase.getDate().isAfter(date)
                        || purchase.getItems() == null) {
                    continue;
                }
                for (com.vpm.Accounts.Entity.PurchaseItem item : purchase.getItems()) {
                    if (item.getProduct() != null
                            && p.getId().equals(item.getProduct().getId())) {
                        stock = stock.subtract(BigDecimal.valueOf(item.getQuantity()));
                    }
                }
            }
            if (p.getOpeningStockDate() != null
                    && p.getOpeningStockDate().isAfter(date)) {
                BigDecimal openingQuantity = p.getOpeningStockQuantity() == null
                        ? BigDecimal.ZERO
                        : p.getOpeningStockQuantity();
                stock = stock.subtract(openingQuantity);
            }

            // -------------------------------------------------
            // GET PURCHASE PRICE
            // -------------------------------------------------

            BigDecimal purchasePrice =
                    p.getPurchasePrice();

            if (purchasePrice == null) {
                purchasePrice = BigDecimal.ZERO;
            }

            // -------------------------------------------------
            // STOCK VALUE
            // -------------------------------------------------

            BigDecimal value =
                    stock.multiply(purchasePrice);

            stockDetails.put(
                    productName,
                    value
            );

            // -------------------------------------------------
            // TOTAL CLOSING STOCK
            // -------------------------------------------------

            total = total.add(value);
        }

        return total;
    }

    private FinancialYear resolveFinancialYear(Long financialYearId) {
        if (financialYearId != null) {
            return financialYearRepo.findById(financialYearId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Financial year not found: " + financialYearId));
        }
        LocalDate today = LocalDate.now();
        return financialYearRepo
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today)
                .orElseThrow(() -> new IllegalStateException(
                        "No financial year is configured for " + today));
    }

    private List<JournalEntry> entriesForYear(FinancialYear year) {
        return journalRepo.findAll().stream()
                .filter(entry -> entry.getEntryDate() != null
                        && !entry.getEntryDate().isBefore(year.getStartDate())
                        && !entry.getEntryDate().isAfter(year.getEndDate())
                        && !isClosingEntry(entry))
                .toList();
    }

    private boolean isClosingEntry(JournalEntry entry) {
        String voucher = entry.getVoucherNumber();
        return voucher != null
                && (voucher.startsWith("FY-PL-CLOSE-")
                        || voucher.startsWith("FY-CD-"));
    }

    private String normalizedAccountGroup(Account account) {
        if (account == null || account.getAccountGroup() == null) {
            return "";
        }
        return account.getAccountGroup().trim().toUpperCase();
    }

    private boolean isSalesAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return type == AccountType.SALES
                || type == AccountType.INCOME && "SALES".equals(group)
                || "SALES".equals(group);
    }

    private boolean isPurchaseAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return type == AccountType.PURCHASES
                || type == AccountType.EXPENSE && "PURCHASES".equals(group)
                || "PURCHASES".equals(group);
    }

    private boolean isDirectIncomeAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return type == AccountType.DIRECTINCOME
                || type == AccountType.INCOME && "DIRECT_INCOME".equals(group)
                || "DIRECT_INCOME".equals(group);
    }

    private boolean isDirectExpenseAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return type == AccountType.DIRECTEXPENSES
                || type == AccountType.EXPENSE && "DIRECT_EXPENSES".equals(group)
                || "DIRECT_EXPENSES".equals(group);
    }

    private boolean isAssetAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return type == AccountType.ASSET
                || type == AccountType.DEBTORS
                || "ASSET".equals(group)
                || "DEBTORS".equals(group)
                || "FIXED_ASSETS".equals(group)
                || "CURRENT_ASSETS".equals(group);
    }

    private boolean isLiabilityAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return type == AccountType.LIABILITY
                || type == AccountType.CREDITORS
                || "LIABILITY".equals(group)
                || "CREDITORS".equals(group)
                || "BANK_LOANS".equals(group)
                || "CURRENT_LIABILITIES".equals(group)
                || "LONG_TERM_LIABILITIES".equals(group);
    }

    private boolean isCapitalAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return !isDrawingAccount(account)
                && (type == AccountType.CAPITAL
                        || "CAPITAL".equals(group)
                        || "RETAINED_EARNINGS".equals(group)
                        || "OWNER_CAPITAL".equals(group));
    }

    private boolean isDrawingAccount(Account account) {
        String group = normalizedAccountGroup(account);
        AccountType type = account == null ? null : account.getType();
        return type == AccountType.DRAWINGS
                || "DRAWINGS".equals(group)
                || "OWNER_DRAWINGS".equals(group);
    }

    private BigDecimal openingStockValueBeforeYear(FinancialYear year) {
        BigDecimal total = BigDecimal.ZERO;
        for (Product product : productRepo.findAll()) {
            if (product == null || product.getOpeningStockDate() == null
                    || product.getOpeningStockDate().isAfter(year.getStartDate().minusDays(1))) {
                continue;
            }
            BigDecimal quantity = product.getOpeningStockQuantity() == null
                    ? BigDecimal.ZERO
                    : product.getOpeningStockQuantity();
            BigDecimal price = product.getPurchasePrice() == null
                    ? BigDecimal.ZERO
                    : product.getPurchasePrice();
            total = total.add(quantity.multiply(price));
        }
        return total;
    }

    private BigDecimal stockIntroducedDuringYear(FinancialYear year) {
        BigDecimal total = BigDecimal.ZERO;

        for (Product product : productRepo.findAll()) {
            if (product == null || product.getTenantId() == null || product.getId() == null) {
                continue;
            }

            String voucherNumber = "OB-STOCK-" + product.getTenantId() + "-" + product.getId();
            JournalEntry entry = journalRepo.findByVoucherNumber(voucherNumber).orElse(null);
            if (entry == null || entry.getEntryDate() == null) {
                continue;
            }
            if (!entry.getEntryDate().isBefore(year.getStartDate())
                    && !entry.getEntryDate().isAfter(year.getEndDate())) {
                total = total.add(openingStockValueFromJournal(entry));
            }
        }

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            total = journalRepo.findAll().stream()
                    .filter(entry -> entry != null
                            && entry.getVoucherNumber() != null
                            && entry.getVoucherNumber().startsWith("OB-STOCK-"))
                    .filter(entry -> entry.getEntryDate() != null
                            && !entry.getEntryDate().isBefore(year.getStartDate())
                            && !entry.getEntryDate().isAfter(year.getEndDate()))
                    .map(this::openingStockValueFromJournal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return total;
    }

    private BigDecimal openingStockValueFromJournal(JournalEntry entry) {
        if (entry == null || entry.getLines() == null) {
            return BigDecimal.ZERO;
        }
        return entry.getLines().stream()
                .filter(line -> line != null
                        && line.getAccount() != null
                        && line.getAccount().getType() == AccountType.OPENING_STOCK)
                .map(JournalEntryLine::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal closingStockValue(FinancialYear year) {
        BigDecimal total = openingStockValueBeforeYear(year)
                .add(stockIntroducedDuringYear(year));

        for (Product product : productRepo.findAll()) {
            if (product == null || product.getOpeningStockDate() == null) {
                continue;
            }
            if (product.getOpeningStockDate().isAfter(year.getEndDate())) {
                continue;
            }

            BigDecimal quantity = product.getCurrentStockQuantity() == null
                    ? BigDecimal.ZERO
                    : product.getCurrentStockQuantity();
            BigDecimal price = product.getPurchasePrice() == null
                    ? BigDecimal.ZERO
                    : product.getPurchasePrice();
            total = total.add(quantity.multiply(price));
        }
        return total;
    }

    // =========================================================
    // TRADING ACCOUNT
    // =========================================================

    public TradingAccountDTO getTradingAccount() {
        return getTradingAccount(null);
    }

    public TradingAccountDTO getTradingAccount(Long financialYearId) {
        FinancialYear year = resolveFinancialYear(financialYearId);

        saleRepo.findAll();
        purchaseRepo.findAll();

        BigDecimal purchases = BigDecimal.ZERO;
        BigDecimal sales = BigDecimal.ZERO;
        BigDecimal openingStock = BigDecimal.ZERO;
        BigDecimal stockIntroduced = BigDecimal.ZERO;

        Map<String, BigDecimal> directIncome = new LinkedHashMap<>();
        Map<String, BigDecimal> directExpense = new LinkedHashMap<>();

        BigDecimal totalDirectIncome = BigDecimal.ZERO;
        BigDecimal totalDirectExpense = BigDecimal.ZERO;

        for (JournalEntry entry : entriesForYear(year)) {
            if (entry == null || entry.getLines() == null) {
                continue;
            }

            for (JournalEntryLine line : entry.getLines()) {
                if (line == null) {
                    continue;
                }

                Account acc = line.getAccount();
                if (acc == null) {
                    continue;
                }

                BigDecimal debit = line.getDebit() == null ? BigDecimal.ZERO : line.getDebit();
                BigDecimal credit = line.getCredit() == null ? BigDecimal.ZERO : line.getCredit();
                AccountType type = acc.getType();
                if (type == null) {
                    continue;
                }

                if (isPurchaseAccount(acc)) {
                    purchases = purchases.add(debit).subtract(credit);
                    continue;
                }
                if (isSalesAccount(acc)) {
                    sales = sales.add(credit).subtract(debit);
                    continue;
                }
                if (isDirectIncomeAccount(acc)) {
                    BigDecimal directIncomeAmount = credit.subtract(debit);
                    String incomeName = acc.getName() == null || acc.getName().isBlank()
                            ? "Unnamed Direct Income"
                            : acc.getName();
                    directIncome.put(incomeName,
                            directIncome.getOrDefault(incomeName, BigDecimal.ZERO).add(directIncomeAmount));
                    totalDirectIncome = totalDirectIncome.add(directIncomeAmount);
                    continue;
                }
                if (isDirectExpenseAccount(acc)) {
                    BigDecimal directExpenseAmount = debit.subtract(credit);
                    String expenseName = acc.getName() == null || acc.getName().isBlank()
                            ? "Unnamed Direct Expense"
                            : acc.getName();
                    directExpense.put(expenseName,
                            directExpense.getOrDefault(expenseName, BigDecimal.ZERO).add(directExpenseAmount));
                    totalDirectExpense = totalDirectExpense.add(directExpenseAmount);
                }
            }
        }

        Map<String, BigDecimal> stockDetails = new LinkedHashMap<>();
        openingStock = openingStockValueBeforeYear(year);
        stockIntroduced = stockIntroducedDuringYear(year);
        BigDecimal closingStock = closingStockValue(year);
        BigDecimal grossProfit = sales
                .add(closingStock)
                .add(totalDirectIncome)
                .subtract(purchases)
                .subtract(openingStock)
                .subtract(stockIntroduced)
                .subtract(totalDirectExpense);

        TradingAccountDTO dto = new TradingAccountDTO();
        dto.setPurchases(purchases);
        dto.setSales(sales);
        dto.setOpeningStock(openingStock);
        dto.setStockIntroduced(stockIntroduced);
        dto.setClosingStock(closingStock);
        dto.setDirectIncome(directIncome);
        dto.setDirectExpenses(directExpense);
        dto.setDirectTotalIncome(totalDirectIncome);
        dto.setDirectTotalExpense(totalDirectExpense);
        dto.setGrossProfit(grossProfit);
        dto.setStockDetails(stockDetails);
        return dto;
    }

    // =========================================================
    // PROFIT & LOSS
    // =========================================================

    public ProfitLossDTO getProfitLoss() {
        return getProfitLoss(null);
    }

    public ProfitLossDTO getProfitLoss(Long financialYearId) {
        FinancialYear year = resolveFinancialYear(financialYearId);

        // =====================================================
        // GET GROSS PROFIT
        // =====================================================

        BigDecimal grossProfit =
                getTradingAccount(year.getId())
                        .getGrossProfit();

        if (grossProfit == null) {
            grossProfit =
                    BigDecimal.ZERO;
        }

        // =====================================================
        // INCOME / EXPENSE MAPS
        // =====================================================

        Map<String, BigDecimal> incomeMap =
                new LinkedHashMap<>();

        Map<String, BigDecimal> expenseMap =
                new LinkedHashMap<>();

        // =====================================================
        // TOTALS
        // =====================================================

        BigDecimal totalIncome =
                BigDecimal.ZERO;

        BigDecimal totalExpense =
                BigDecimal.ZERO;

        // =====================================================
        // READ JOURNALS
        // =====================================================

        for (JournalEntry entry : entriesForYear(year)) {

            if (entry == null
                    || entry.getLines() == null) {

                continue;
            }

            for (JournalEntryLine line :
                    entry.getLines()) {

                if (line == null) {
                    continue;
                }

                Account acc =
                        line.getAccount();

                if (acc == null) {
                    continue;
                }

                // -------------------------------------------------
                // DEBIT
                // -------------------------------------------------

                BigDecimal debit =
                        line.getDebit();

                if (debit == null) {
                    debit = BigDecimal.ZERO;
                }

                // -------------------------------------------------
                // CREDIT
                // -------------------------------------------------

                BigDecimal credit =
                        line.getCredit();

                if (credit == null) {
                    credit = BigDecimal.ZERO;
                }

                if (isSalesAccount(acc)
                        || isPurchaseAccount(acc)
                        || isDirectIncomeAccount(acc)
                        || isDirectExpenseAccount(acc)) {
                    continue;
                }

                // =================================================
                // INCOME
                // =================================================

                if (acc.getType()
                        == AccountType.INCOME) {

                    /*
                     * Income:
                     *
                     * Credit - Debit
                     *
                     * If income is reversed/returned,
                     * debit reduces the income.
                     */

                    BigDecimal amount =
                            credit.subtract(debit);

                    String accountName =
                            acc.getName();

                    if (accountName == null
                            || accountName.isBlank()) {

                        accountName =
                                "Unnamed Income";
                    }

                    incomeMap.put(
                            accountName,
                            incomeMap.getOrDefault(
                                    accountName,
                                    BigDecimal.ZERO
                            ).add(amount)
                    );

                    totalIncome =
                            totalIncome.add(amount);
                }

                // =================================================
                // EXPENSE
                // =================================================

                if (acc.getType()
                        == AccountType.EXPENSE) {

                    /*
                     * Expense:
                     *
                     * Debit - Credit
                     *
                     * If expense is reversed,
                     * credit reduces the expense.
                     */

                    BigDecimal amount =
                            debit.subtract(credit);

                    String accountName =
                            acc.getName();

                    if (accountName == null
                            || accountName.isBlank()) {

                        accountName =
                                "Unnamed Expense";
                    }

                    expenseMap.put(
                            accountName,
                            expenseMap.getOrDefault(
                                    accountName,
                                    BigDecimal.ZERO
                            ).add(amount)
                    );

                    totalExpense =
                            totalExpense.add(amount);
                }
            }
        }

        // =====================================================
        // NET PROFIT
        // =====================================================

        /*
         * Net Profit =
         *
         * Gross Profit
         * + Other Income
         * - Other Expenses
         */

        BigDecimal netProfit =
                grossProfit
                        .add(totalIncome)
                        .subtract(totalExpense);

        // =====================================================
        // CREATE DTO
        // =====================================================

        ProfitLossDTO dto =
                new ProfitLossDTO();

        dto.setGrossProfit(
                grossProfit
        );

        dto.setIncomeDetails(
                incomeMap
        );

        dto.setExpenseDetails(
                expenseMap
        );

        dto.setTotalIncome(
                totalIncome
        );

        dto.setTotalExpense(
                totalExpense
        );

        dto.setNetProfit(
                netProfit
        );

        return dto;
    }

    // =========================================================
    // BALANCE SHEET
    // =========================================================

    public BalanceSheetDTO getBalanceSheet() {
        return getBalanceSheet(null);
    }

    public BalanceSheetDTO getBalanceSheet(Long financialYearId) {
        FinancialYear year = resolveFinancialYear(financialYearId);

        // =====================================================
        // MAPS
        // =====================================================

        Map<String, BigDecimal> assets =
                new LinkedHashMap<>();

        Map<String, BigDecimal> liabilities =
                new LinkedHashMap<>();

        Map<String, BigDecimal> capitalDetails =
                new LinkedHashMap<>();

        // =====================================================
        // DRAWINGS
        // =====================================================

        BigDecimal drawings =
                BigDecimal.ZERO;

        // =====================================================
        // READ JOURNALS
        // =====================================================

        for (JournalEntry entry : entriesForYear(year)) {

            if (entry == null
                    || entry.getLines() == null) {

                continue;
            }

            for (JournalEntryLine line :
                    entry.getLines()) {

                if (line == null) {
                    continue;
                }

                Account acc =
                        line.getAccount();

                if (acc == null) {
                    continue;
                }

                // -------------------------------------------------
                // DEBIT
                // -------------------------------------------------

                BigDecimal debit =
                        line.getDebit();

                if (debit == null) {
                    debit = BigDecimal.ZERO;
                }

                // -------------------------------------------------
                // CREDIT
                // -------------------------------------------------

                BigDecimal credit =
                        line.getCredit();

                if (credit == null) {
                    credit = BigDecimal.ZERO;
                }

                // =================================================
                // ACCOUNT TYPE
                // =================================================

                AccountType type =
                        acc.getType();

                if (type == null) {
                    continue;
                }

                if (isAssetAccount(acc)) {
                    BigDecimal assetAmount = debit.subtract(credit);
                    String assetName = acc.getName();
                    if (assetName == null || assetName.isBlank()) {
                        assetName = "Unnamed Asset";
                    }
                    assets.put(assetName,
                            assets.getOrDefault(assetName, BigDecimal.ZERO).add(assetAmount));
                } else if (isLiabilityAccount(acc)) {
                    BigDecimal liabilityAmount = credit.subtract(debit);
                    String liabilityName = acc.getName();
                    if (liabilityName == null || liabilityName.isBlank()) {
                        liabilityName = "Unnamed Liability";
                    }
                    liabilities.put(liabilityName,
                            liabilities.getOrDefault(liabilityName, BigDecimal.ZERO).add(liabilityAmount));
                } else if (isDrawingAccount(acc)) {
                    drawings = drawings.add(debit);
                } else if (isCapitalAccount(acc)) {
                    BigDecimal capitalAmount = credit.subtract(debit);
                    String capitalName = acc.getName();
                    if (capitalName == null || capitalName.isBlank()) {
                        capitalName = "Unnamed Capital";
                    }
                    capitalDetails.put(capitalName,
                            capitalDetails.getOrDefault(capitalName, BigDecimal.ZERO).add(capitalAmount));
                }
            }
        }

        // =====================================================
        // CLOSING STOCK
        // =====================================================

        BigDecimal closingStock =
                BigDecimal.ZERO;

        closingStock = calculateStockValueAsOf(
                year.getEndDate(), new LinkedHashMap<>());

        // =====================================================
        // ADD CLOSING STOCK TO ASSETS
        // =====================================================

        assets.put(
                "Closing Stock",
                closingStock
        );

        // =====================================================
        // NET PROFIT
        // =====================================================

        BigDecimal netProfit = getProfitLoss(year.getId()).getNetProfit();

        if (netProfit == null) {
            netProfit = BigDecimal.ZERO;
        }

        // =====================================================
        // OPENING CAPITAL
        // =====================================================

        BigDecimal openingCapital =
                capitalDetails.values()
                        .stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // CLOSING CAPITAL
        // =====================================================

        /*
         * Closing Capital =
         *
         * Opening Capital
         * + Net Profit
         * - Drawings
         */

        BigDecimal closingCapital =openingCapital .add(netProfit).subtract(drawings);

        // =====================================================
        // ADD PROFIT / LOSS TO CAPITAL DETAILS
        // =====================================================

        if (netProfit.compareTo(
                BigDecimal.ZERO) >= 0) {

            capitalDetails.put(
                    "Add: Net Profit",
                    netProfit
            );

        } else {

            /*
             * Store loss as a positive display value.
             *
             * Example:
             * Net Loss = -5000
             *
             * Balance Sheet displays:
             * Less: Net Loss = 5000
             */

            capitalDetails.put(
                    "Less: Net Loss",
                    netProfit.abs()
            );
        }

        // =====================================================
        // DRAWINGS
        // =====================================================

        capitalDetails.put(
                "Less: Drawings",
                drawings
        );

        // =====================================================
        // CLOSING CAPITAL
        // =====================================================

        capitalDetails.put(
                "Closing Capital",
                closingCapital
        );

        // =====================================================
        // ADD CAPITAL TO LIABILITIES
        // =====================================================

        liabilities.put(
                "Capital",
                closingCapital
        );

        // =====================================================
        // TOTAL ASSETS
        // =====================================================

        BigDecimal totalAssets =
                assets.values()
                        .stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // TOTAL LIABILITIES
        // =====================================================

        BigDecimal totalLiabilities =
                liabilities.values()
                        .stream()
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add);

        // =====================================================
        // CREATE BALANCE SHEET DTO
        // =====================================================

        BalanceSheetDTO dto = new BalanceSheetDTO();

        dto.setAssets(assets);

        dto.setLiabilities(liabilities);

        dto.setCapitalDetails(capitalDetails);

        dto.setTotalAssets(totalAssets);

        dto.setTotalLiabilities(totalLiabilities);

        return dto;
    }

    // =========================================================
    // ASYNC - TRADING ACCOUNT
    // =========================================================

    @Async("accountExecutor")
    public CompletableFuture<TradingAccountDTO>
            getTradingAccountAsync() {

        return CompletableFuture.completedFuture(
                getTradingAccount()
        );
    }

    @Async("accountExecutor")
    public CompletableFuture<TradingAccountDTO> getTradingAccountAsync(
            Long financialYearId) {
        return CompletableFuture.completedFuture(
                getTradingAccount(financialYearId));
    }

    // =========================================================
    // ASYNC - PROFIT & LOSS
    // =========================================================

    @Async("accountExecutor")
    public CompletableFuture<ProfitLossDTO>
            getProfitLossAsync() {

        return CompletableFuture.completedFuture(
                getProfitLoss()
        );
    }

    @Async("accountExecutor")
    public CompletableFuture<ProfitLossDTO> getProfitLossAsync(
            Long financialYearId) {
        return CompletableFuture.completedFuture(
                getProfitLoss(financialYearId));
    }

    // =========================================================
    // ASYNC - BALANCE SHEET
    // =========================================================

    @Async("accountExecutor")
    public CompletableFuture<BalanceSheetDTO>
            getBalanceSheetAsync() {

        return CompletableFuture.completedFuture(
                getBalanceSheet()
        );
    }

    @Async("accountExecutor")
    public CompletableFuture<BalanceSheetDTO> getBalanceSheetAsync(
            Long financialYearId) {
        return CompletableFuture.completedFuture(
                getBalanceSheet(financialYearId));
    }
}

