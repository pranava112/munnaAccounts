// package com.vpm.Accounts.Service;

// import java.util.LinkedHashMap;
// import java.util.Map;
// import java.util.concurrent.CompletableFuture;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import com.vpm.Accounts.DTO.BalanceSheetDTO;
// import com.vpm.Accounts.DTO.ProfitLossDTO;
// import com.vpm.Accounts.DTO.TradingAccountDTO;
// import com.vpm.Accounts.Entity.Account;
// import com.vpm.Accounts.Entity.AccountType;
// import com.vpm.Accounts.Entity.JournalEntry;
// import com.vpm.Accounts.Entity.JournalEntryLine;
// import com.vpm.Accounts.Entity.Product;
// import com.vpm.Accounts.Repository.JournalEntryRepository;
// import com.vpm.Accounts.Repository.ProductRepository;

// @Service
// public class ReportService {

//     @Autowired
//     private ProductRepository productRepo;

//     @Autowired
//     private JournalEntryRepository journalRepo;

//     // =========================================
//     // 🔹 CLOSING STOCK
//     // =========================================
//     private double calculateClosingStock(Map<String, Double> stockDetails) {

//         double total = 0;

//         for (Product p : productRepo.findAll()) {
//             double value = p.getOpeningStock() * p.getPurchasePrice();
//             stockDetails.put(p.getName(), value);
//             total += value;
//         }

//         return total;
//     }

//     // =========================================
//     // 🔹 TRADING ACCOUNT
//     // =========================================
//     public TradingAccountDTO getTradingAccount() {

//         double purchases = 0;
//         double sales = 0;
//         double openingStock = 0;

//         Map<String, Double> directIncome = new LinkedHashMap<>();
//         Map<String, Double> directExpense = new LinkedHashMap<>();

//         double totalDirectIncome = 0;
//         double totalDirectExpense = 0;

//         // ✅ READ FROM JOURNAL ONLY
//         for (JournalEntry entry : journalRepo.findAll()) {
//             for (JournalEntryLine line : entry.getLines()) {

//                 Account acc = line.getAccount();
//                 if (acc == null) continue;

//                 double debit = line.getDebit();
//                 double credit = line.getCredit();

//                 switch (acc.getType()) {

//                     case PURCHASES:
//                         purchases += debit-credit;
//                         break;

//                     case SALES:
//                         sales += credit-debit;
//                         break;

//                     case OPENING_STOCK:
//                         openingStock += debit-credit;
//                         break;

//                     case DIRECTINCOME:
//                         directIncome.put(
//                                 acc.getName(),
//                                 directIncome.getOrDefault(acc.getName(), 0.0) + credit-debit
//                         );
//                         totalDirectIncome += credit-debit;
//                         break;

//                     case DIRECTEXPENSES:
//                         directExpense.put(
//                                 acc.getName(),
//                                 directExpense.getOrDefault(acc.getName(), 0.0) + debit-credit
//                         );
//                         totalDirectExpense += debit-credit;
//                         break;

//                     default:
//                         break;
//                 }
//             }
//         }

//         // 🔹 CLOSING STOCK
//         Map<String, Double> stockDetails = new LinkedHashMap<>();
//         double closingStock = calculateClosingStock(stockDetails);

//         // 🔹 GROSS PROFIT
//         double grossProfit =
//                 (sales + closingStock + totalDirectIncome)
//                         - (purchases + openingStock + totalDirectExpense);

//         TradingAccountDTO dto = new TradingAccountDTO();
//         dto.setPurchases(purchases);
//         dto.setSales(sales);
//         dto.setOpeningStock(openingStock);
//         dto.setClosingStock(closingStock);
//         dto.setDirectIncome(directIncome);
//         dto.setDirectExpenses(directExpense);
//         dto.setDirectTotalIncome(totalDirectIncome);
//         dto.setDirectTotalExpense(totalDirectExpense);
//         dto.setGrossProfit(grossProfit);
//         dto.setStockDetails(stockDetails);

//         return dto;
//     }

//     // =========================================
//     // 🔹 PROFIT & LOSS
//     // =========================================
//     public ProfitLossDTO getProfitLoss() {

//         double grossProfit = getTradingAccount().getGrossProfit();

//         Map<String, Double> incomeMap = new LinkedHashMap<>();
//         Map<String, Double> expenseMap = new LinkedHashMap<>();

//         double totalIncome = 0;
//         double totalExpense = 0;

//         for (JournalEntry entry : journalRepo.findAll()) {
//             for (JournalEntryLine line : entry.getLines()) {

//                 Account acc = line.getAccount();
//                 if (acc == null) continue;

//                 double debit = line.getDebit();
//                 double credit = line.getCredit();

//                 if (acc.getType() == AccountType.INCOME) {
//                     incomeMap.put(acc.getName(),
//                             incomeMap.getOrDefault(acc.getName(), 0.0) + credit-debit);//changes if return of products or services by -debit
//                     totalIncome += credit-debit;
//                 }

//                 if (acc.getType() == AccountType.EXPENSE) {
//                     expenseMap.put(acc.getName(),
//                             expenseMap.getOrDefault(acc.getName(), 0.0) + debit-credit);//changes if return of products or services by -credit
//                     totalExpense += debit-credit;
//                 }
//             }
//         }

//         double netProfit = (grossProfit + totalIncome) - totalExpense;

//         ProfitLossDTO dto = new ProfitLossDTO();
//         dto.setGrossProfit(grossProfit);
//         dto.setIncomeDetails(incomeMap);
//         dto.setExpenseDetails(expenseMap);
//         dto.setTotalIncome(totalIncome);
//         dto.setTotalExpense(totalExpense);
//         dto.setNetProfit(netProfit);

//         return dto;
//     }

    
    
    
    
    
    
    
//     // =========================================
//     // 🔹 BALANCE SHEET
//     // =========================================
//     public BalanceSheetDTO getBalanceSheet() {

//         Map<String, Double> assets = new LinkedHashMap<>();
//         Map<String, Double> liabilities = new LinkedHashMap<>();
//         Map<String, Double> capitalDetails = new LinkedHashMap<>();

//         double drawings = 0;

//         for (JournalEntry entry : journalRepo.findAll()) {
//             for (JournalEntryLine line : entry.getLines()) {

//                 Account acc = line.getAccount();
//                 if (acc == null) continue;

//                 double debit = line.getDebit();
//                 double credit = line.getCredit();

//                 switch (acc.getType()) {

//                     case ASSET:
//                     case DEBTORS:
//                         assets.put(acc.getName(),
//                                 assets.getOrDefault(acc.getName(), 0.0) + (debit - credit));
//                         break;

//                     case LIABILITY:
//                     case CREDITORS:
//                         liabilities.put(acc.getName(),
//                                 liabilities.getOrDefault(acc.getName(), 0.0) + (credit - debit));
//                         break;

//                     case CAPITAL:
//                         capitalDetails.put(acc.getName(),
//                                 capitalDetails.getOrDefault(acc.getName(), 0.0) + (credit - debit));
//                         break;

//                     case DRAWINGS:
//                         drawings += debit;
//                         break;

//                     default:
//                         break;
//                 }
//             }
//         }

//         // 🔹 ADD CLOSING STOCK
//         double closingStock = 0;
//         for (Product p : productRepo.findAll()) {
//             closingStock += p.getOpeningStock() * p.getPurchasePrice();
//         }
//         assets.put("Closing Stock", closingStock);

//         // 🔹 NET PROFIT
//         double netProfit = getProfitLoss().getNetProfit();

//         double openingCapital = capitalDetails.values()
//                 .stream().mapToDouble(Double::doubleValue).sum();

//         double closingCapital = openingCapital + netProfit - drawings;

// //        capitalDetails.put("Net Profit", netProfit);
        
//         if(netProfit>=0) {
//         	 capitalDetails.put("Add: Net Profit", netProfit);
//         }
//         else {
//         	 capitalDetails.put("Less: Net Loss", netProfit);
//         }
        
//         capitalDetails.put("Less: Drawings", drawings);
//         capitalDetails.put("Closing Capital", closingCapital);

//         liabilities.put("Capital", closingCapital);

//         double totalAssets = assets.values().stream().mapToDouble(Double::doubleValue).sum();
//         double totalLiabilities = liabilities.values().stream().mapToDouble(Double::doubleValue).sum();

//         BalanceSheetDTO dto = new BalanceSheetDTO();
//         dto.setAssets(assets);
//         dto.setLiabilities(liabilities);
//         dto.setCapitalDetails(capitalDetails);
//         dto.setTotalAssets(totalAssets);
//         dto.setTotalLiabilities(totalLiabilities);

//         return dto;
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<TradingAccountDTO> getTradingAccountAsync() {
//         return CompletableFuture.completedFuture(getTradingAccount());
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<ProfitLossDTO> getProfitLossAsync() {
//         return CompletableFuture.completedFuture(getProfitLoss());
//     }

//     @Async("accountExecutor")
//     public CompletableFuture<BalanceSheetDTO> getBalanceSheetAsync() {
//         return CompletableFuture.completedFuture(getBalanceSheet());
//     }
// }


package com.vpm.Accounts.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
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
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;

@Service
public class ReportService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private JournalEntryRepository journalRepo;


    // =========================================================
    // CLOSING STOCK
    // =========================================================

    private BigDecimal calculateClosingStock(
            Map<String, BigDecimal> stockDetails) {

        BigDecimal total = BigDecimal.ZERO;

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

            BigDecimal stock = p.getOpeningStock();

            if (stock == null) {
                stock = BigDecimal.ZERO;
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


    // =========================================================
    // TRADING ACCOUNT
    // =========================================================

    public TradingAccountDTO getTradingAccount() {

        // -----------------------------------------------------
        // MAIN VALUES
        // -----------------------------------------------------

        BigDecimal purchases = BigDecimal.ZERO;

        BigDecimal sales = BigDecimal.ZERO;

        BigDecimal openingStock = BigDecimal.ZERO;


        // -----------------------------------------------------
        // DIRECT INCOME / EXPENSE
        // -----------------------------------------------------

        Map<String, BigDecimal> directIncome =
                new LinkedHashMap<>();

        Map<String, BigDecimal> directExpense =
                new LinkedHashMap<>();


        BigDecimal totalDirectIncome =
                BigDecimal.ZERO;

        BigDecimal totalDirectExpense =
                BigDecimal.ZERO;


        // =====================================================
        // READ JOURNAL ENTRIES
        // =====================================================

        for (JournalEntry entry :
                journalRepo.findAll()) {

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


                switch (type) {

                    // =================================================
                    // PURCHASES
                    // =================================================

                    case PURCHASES:

                        purchases =
                                purchases
                                        .add(debit)
                                        .subtract(credit);

                        break;


                    // =================================================
                    // SALES
                    // =================================================

                    case SALES:

                        sales =
                                sales
                                        .add(credit)
                                        .subtract(debit);

                        break;


                    // =================================================
                    // OPENING STOCK
                    // =================================================

                    case OPENING_STOCK:

                        openingStock =
                                openingStock
                                        .add(debit)
                                        .subtract(credit);

                        break;


                    // =================================================
                    // DIRECT INCOME
                    // =================================================

                    case DIRECTINCOME:

                        BigDecimal directIncomeAmount =
                                credit.subtract(debit);


                        String incomeName =
                                acc.getName();


                        if (incomeName == null
                                || incomeName.isBlank()) {

                            incomeName =
                                    "Unnamed Direct Income";
                        }


                        directIncome.put(
                                incomeName,
                                directIncome.getOrDefault(
                                        incomeName,
                                        BigDecimal.ZERO
                                ).add(directIncomeAmount)
                        );


                        totalDirectIncome =
                                totalDirectIncome.add(
                                        directIncomeAmount
                                );

                        break;


                    // =================================================
                    // DIRECT EXPENSE
                    // =================================================

                    case DIRECTEXPENSES:

                        BigDecimal directExpenseAmount =
                                debit.subtract(credit);


                        String expenseName =
                                acc.getName();


                        if (expenseName == null
                                || expenseName.isBlank()) {

                            expenseName =
                                    "Unnamed Direct Expense";
                        }


                        directExpense.put(
                                expenseName,
                                directExpense.getOrDefault(
                                        expenseName,
                                        BigDecimal.ZERO
                                ).add(directExpenseAmount)
                        );


                        totalDirectExpense =
                                totalDirectExpense.add(
                                        directExpenseAmount
                                );

                        break;


                    // =================================================
                    // OTHER ACCOUNT TYPES
                    // =================================================

                    default:

                        break;
                }
            }
        }


        // =====================================================
        // CLOSING STOCK
        // =====================================================

        Map<String, BigDecimal> stockDetails =
                new LinkedHashMap<>();


        BigDecimal closingStock =
                calculateClosingStock(
                        stockDetails
                );


        // =====================================================
        // GROSS PROFIT
        // =====================================================

        /*
         * Gross Profit =
         *
         * (Sales + Closing Stock + Direct Income)
         *
         * -
         *
         * (Purchases + Opening Stock + Direct Expense)
         */

        BigDecimal grossProfit =
                sales
                        .add(closingStock)
                        .add(totalDirectIncome)
                        .subtract(purchases)
                        .subtract(openingStock)
                        .subtract(totalDirectExpense);


        // =====================================================
        // CREATE DTO
        // =====================================================

        TradingAccountDTO dto =
                new TradingAccountDTO();


        dto.setPurchases(purchases);

        dto.setSales(sales);

        dto.setOpeningStock(openingStock);

        dto.setClosingStock(closingStock);

        dto.setDirectIncome(directIncome);

        dto.setDirectExpenses(directExpense);

        dto.setDirectTotalIncome(
                totalDirectIncome
        );

        dto.setDirectTotalExpense(
                totalDirectExpense
        );

        dto.setGrossProfit(
                grossProfit
        );

        dto.setStockDetails(
                stockDetails
        );


        return dto;
    }


    // =========================================================
    // PROFIT & LOSS
    // =========================================================

    public ProfitLossDTO getProfitLoss() {

        // =====================================================
        // GET GROSS PROFIT
        // =====================================================

        BigDecimal grossProfit =
                getTradingAccount()
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

        for (JournalEntry entry :
                journalRepo.findAll()) {

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

        for (JournalEntry entry :
                journalRepo.findAll()) {

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


                switch (type) {

                    // =================================================
                    // ASSETS
                    // =================================================

                    case ASSET:

                    case DEBTORS:

                        BigDecimal assetAmount =
                                debit.subtract(credit);


                        String assetName =
                                acc.getName();


                        if (assetName == null
                                || assetName.isBlank()) {

                            assetName =
                                    "Unnamed Asset";
                        }


                        assets.put(
                                assetName,
                                assets.getOrDefault(
                                        assetName,
                                        BigDecimal.ZERO
                                ).add(assetAmount)
                        );

                        break;


                    // =================================================
                    // LIABILITIES
                    // =================================================

                    case LIABILITY:

                    case CREDITORS:

                        BigDecimal liabilityAmount =
                                credit.subtract(debit);


                        String liabilityName =
                                acc.getName();


                        if (liabilityName == null
                                || liabilityName.isBlank()) {

                            liabilityName =
                                    "Unnamed Liability";
                        }


                        liabilities.put(
                                liabilityName,
                                liabilities.getOrDefault(
                                        liabilityName,
                                        BigDecimal.ZERO
                                ).add(liabilityAmount)
                        );

                        break;


                    // =================================================
                    // CAPITAL
                    // =================================================

                    case CAPITAL:

                        BigDecimal capitalAmount =
                                credit.subtract(debit);


                        String capitalName =
                                acc.getName();


                        if (capitalName == null
                                || capitalName.isBlank()) {

                            capitalName =
                                    "Unnamed Capital";
                        }


                        capitalDetails.put(
                                capitalName,
                                capitalDetails.getOrDefault(
                                        capitalName,
                                        BigDecimal.ZERO
                                ).add(capitalAmount)
                        );

                        break;


                    // =================================================
                    // DRAWINGS
                    // =================================================

                    case DRAWINGS:

                        drawings =
                                drawings.add(
                                        debit
                                );

                        break;


                    // =================================================
                    // OTHER
                    // =================================================

                    default:

                        break;
                }
            }
        }


        // =====================================================
        // CLOSING STOCK
        // =====================================================

        BigDecimal closingStock =
                BigDecimal.ZERO;


        for (Product p :
                productRepo.findAll()) {

            if (p == null) {
                continue;
            }


            BigDecimal stock =
                    p.getOpeningStock();

            if (stock == null) {
                stock =
                        BigDecimal.ZERO;
            }


            BigDecimal purchasePrice =
                    p.getPurchasePrice();

            if (purchasePrice == null) {
                purchasePrice =
                        BigDecimal.ZERO;
            }


            BigDecimal stockValue =
                    stock.multiply(
                            purchasePrice
                    );


            closingStock =
                    closingStock.add(
                            stockValue
                    );
        }


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

        BigDecimal netProfit =
                getProfitLoss()
                        .getNetProfit();


        if (netProfit == null) {
            netProfit =
                    BigDecimal.ZERO;
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

        BigDecimal closingCapital =
                openingCapital
                        .add(netProfit)
                        .subtract(drawings);


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
                                BigDecimal::add
                        );


        // =====================================================
        // CREATE BALANCE SHEET DTO
        // =====================================================

        BalanceSheetDTO dto =
                new BalanceSheetDTO();


        dto.setAssets(
                assets
        );


        dto.setLiabilities(
                liabilities
        );


        dto.setCapitalDetails(
                capitalDetails
        );


        dto.setTotalAssets(
                totalAssets
        );


        dto.setTotalLiabilities(
                totalLiabilities
        );


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
}

