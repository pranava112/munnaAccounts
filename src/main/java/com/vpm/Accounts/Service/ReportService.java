////package com.vpm.Accounts.Service;
////
////import com.vpm.Accounts.DTO.*;
////import com.vpm.Accounts.Entity.*;
////import com.vpm.Accounts.Repository.JournalEntryRepository;
////
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import java.util.*;
////
////@Service
////public class ReportService {
////
////	 @Autowired
////    private JournalEntryRepository journalRepo;
////
////    
////    
////    
////    public TradingAccountDTO getTradingAccount() {
////
////        List<JournalEntry> entries = journalRepo.findAll();
////
////        Map<String, Double> directIncome = new HashMap<>();
////        Map<String, Double> directExpenses = new HashMap<>();
////
////        double totalDirectIncome = 0;
////        double totalDirectExpenses = 0;
////
////        double purchases = 0;
////        double sales = 0;
////        double openingStock = 0;
////        double closingStock = 0;
////
////        for (JournalEntry entry : entries) {
////
////            for (JournalEntryLine line : entry.getLines()) {
////
////                Account account = line.getAccount();
////                String name = account.getName();
////
////                // ✅ DIRECT INCOME
////                if (account.getType() == AccountType.DIRECTINCOME) {
////                    double value = line.getCredit() - line.getDebit();
////
////                    directIncome.put(name,
////                            directIncome.getOrDefault(name, 0.0) + value);
////
////                    totalDirectIncome += value;
////                }
////
////                // ✅ DIRECT EXPENSE
////                if (account.getType() == AccountType.DIRECTEXPENSES) {
////                    double value = line.getDebit() - line.getCredit();
////
////                    directExpenses.put(name,
////                            directExpenses.getOrDefault(name, 0.0) + value);
////
////                    totalDirectExpenses += value;
////                }
////
////                // ✅ PURCHASES
////                if (account.getType() == AccountType.PURCHASES) {
////                    purchases += (line.getDebit() - line.getCredit());
////                }
////
////                // ✅ SALES
////                if (account.getType() == AccountType.SALES) {
////                    sales += (line.getCredit() - line.getDebit());
////                }
////
////                // ✅ OPENING STOCK
////                if (account.getType() == AccountType.OPENING_STOCK) {
////                    openingStock += (line.getDebit() - line.getCredit());
////                }
////
////                // ✅ CLOSING STOCK
////                if (account.getType() == AccountType.CLOSING_STOCK) {
////                    closingStock += (line.getDebit() - line.getCredit());
////                }
////            }
////        }
////
////        // ✅ GROSS PROFIT FORMULA (REAL ACCOUNTING)
////        double grossProfit =
////                (sales + closingStock + totalDirectIncome)
////                - (purchases + openingStock + totalDirectExpenses);
////
////        // ✅ SET DTO
////        TradingAccountDTO dto = new TradingAccountDTO();
////
////        dto.setDirectIncomeDetails(directIncome);
////        dto.setDirectExpensesDetails(directExpenses);
////
////        dto.setDirectTotalIncome(totalDirectIncome);
////        dto.setDirectTotalExpense(totalDirectExpenses);
////
////        dto.setPurchases(purchases);
////        dto.setSales(sales);
////        dto.setOpeningStock(openingStock);
////        dto.setClosingStock(closingStock);
////
////        dto.setGrossProfit(grossProfit);
////
////        return dto;
////    }
////    
////
////package com.vpm.Accounts.Service;
////
////import com.vpm.Accounts.DTO.BalanceSheetDTO;
////import com.vpm.Accounts.DTO.ProfitLossDTO;
////import com.vpm.Accounts.DTO.TradingAccountDTO;
////import com.vpm.Accounts.Entity.*;
////import com.vpm.Accounts.Repository.*;
////
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import java.util.*;
////
////@Service
////public class ReportService {
////	
////	 @Autowired
////	    private JournalEntryRepository journalRepo;
////
////    @Autowired
////    private PurchaseRepository purchaseRepo;
////
////    @Autowired
////    private SaleRepository saleRepo;
////
////    @Autowired
////    private ProductRepository productRepo;
////
////    public TradingAccountDTO getTradingAccount() {
////
////        double purchases = 0;
////        double sales = 0;
////        double openingStock = 0;
////        double closingStock = 0;
////
////        Map<String, Double> stockDetails = new HashMap<>();
////
////        // =========================
////        // ✅ CALCULATE PURCHASES
////        // =========================
////        List<Purchase> purchaseList = purchaseRepo.findAll();
////
////        for (Purchase p : purchaseList) {
////            for (PurchaseItem item : p.getItems()) {
////                purchases += item.getTotal();
////            }
////        }
////
////        // =========================
////        // ✅ CALCULATE SALES
////        // =========================
////        List<Sale> saleList = saleRepo.findAll();
////
////        for (Sale s : saleList) {
////            for (SaleItem item : s.getItems()) {
////                sales += item.getTotal();
////            }
////        }
////
////        // =========================
////        // ✅ STOCK CALCULATION
////        // =========================
////        List<Product> products = productRepo.findAll();
////
////        for (Product product : products) {
////
////            double stockQty = product.getOpeningStock();
////            double value = stockQty * product.getPurchasePrice();
////
////            // Opening stock (initial value)
////            openingStock += value;
////
////            // Closing stock (same since updated dynamically)
////            closingStock += value;
////
////            stockDetails.put(product.getName(), value);
////        }
////
////        // =========================
////        // ✅ GROSS PROFIT
////        // =========================
////        double grossProfit =
////                (sales + closingStock)
////                - (purchases + openingStock);
////
////        TradingAccountDTO dto = new TradingAccountDTO();
////
////        dto.setPurchases(purchases);
////        dto.setSales(sales);
////        dto.setOpeningStock(openingStock);
////        dto.setClosingStock(closingStock);
////        dto.setGrossProfit(grossProfit);
////        dto.setStockDetails(stockDetails);
////
////        return dto;
////    }
//
//    
////    
////    package com.vpm.Accounts.Service;
////
////import com.vpm.Accounts.DTO.BalanceSheetDTO;
////import com.vpm.Accounts.DTO.ProfitLossDTO;
////import com.vpm.Accounts.DTO.TradingAccountDTO;
////import com.vpm.Accounts.Entity.*;
////import com.vpm.Accounts.Repository.*;
////
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import java.util.*;
////
////@Service
////public class ReportService {
////
////    @Autowired
////    private PurchaseRepository purchaseRepo;
////
////    @Autowired
////    private SaleRepository saleRepo;
////
////    @Autowired
////    private ProductRepository productRepo;
////
////    @Autowired
////    private AccountRepository accountRepo;
////    
////    @Autowired
////    private JournalEntryRepository journalRepo;
////
////    public TradingAccountDTO getTradingAccount() {
////
////        double purchases = 0;
////        double sales = 0;
////        double openingStock = 0;
////        double closingStock = 0;
////
////        Map<String, Double> directExpenses = new HashMap<>();
////        double totalDirectExpenses = 0;
////
////        // =========================
////        // ✅ PURCHASES
////        // =========================
////        for (Purchase p : purchaseRepo.findAll()) {
////            for (PurchaseItem item : p.getItems()) {
////                purchases += item.getTotal();
////            }
////        }
////
////        // =========================
////        // ✅ SALES
////        // =========================
////        for (Sale s : saleRepo.findAll()) {
////            for (SaleItem item : s.getItems()) {
////                sales += item.getTotal();
////            }
////        }
////
////        // =========================
////        // ✅ OPENING STOCK (FROM ACCOUNT)
////        // =========================
////        Account opening = accountRepo.findByName("Opening Stock").orElse(null);
////        if (opening != null) {
////            openingStock = opening.getBalance();
////        }
////
////        // =========================
////        // ✅ DIRECT EXPENSES
////        // =========================
////        for (Account acc : accountRepo.findAll()) {
////            if (acc.getType() == AccountType.DIRECTEXPENSES) {
////                directExpenses.put(acc.getName(), acc.getBalance());
////                totalDirectExpenses += acc.getBalance();
////            }
////        }
////
////        // =========================
////        // ✅ CLOSING STOCK (FROM PRODUCTS)
////        // =========================
////        for (Product product : productRepo.findAll()) {
////            double value = product.getOpeningStock() * product.getPurchasePrice();
////            closingStock += value;
////        }
////
////        // =========================
////        // ✅ GROSS PROFIT
////        // =========================
////        double grossProfit =
////                (sales + closingStock)
////                - (purchases + openingStock + totalDirectExpenses);
////
////        TradingAccountDTO dto = new TradingAccountDTO();
////
////        dto.setPurchases(purchases);
////        dto.setSales(sales);
////        dto.setOpeningStock(openingStock);
////        dto.setClosingStock(closingStock);
////        dto.setGrossProfit(grossProfit);
////
////        dto.setDirectExpensesDetails(directExpenses);
////        dto.setDirectTotalExpense(totalDirectExpenses);
////
////        return dto;
////    }
////
////   
////    
////    public ProfitLossDTO getProfitLoss() {
////
////        List<JournalEntry> entries = journalRepo.findAll();
////
////        Map<String, Double> incomeMap = new LinkedHashMap<>();
////        Map<String, Double> expenseMap = new LinkedHashMap<>();
////
////        double totalIncome = 0;
////        double totalExpense = 0;
////
////        // ✅ STEP 1: NORMAL INCOME & EXPENSE
////        for (JournalEntry entry : entries) {
////            for (JournalEntryLine line : entry.getLines()) {
////
////                Account acc = line.getAccount();
////                String name = acc.getName();
////
////                if (acc.getType() == AccountType.INCOME) {
////                    double value = line.getCredit() - line.getDebit();
////
////                    incomeMap.put(name,
////                            incomeMap.getOrDefault(name, 0.0) + value);
////
////                    totalIncome += value;
////                }
////
////                if (acc.getType() == AccountType.EXPENSE) {
////                    double value = line.getDebit() - line.getCredit();
////
////                    expenseMap.put(name,
////                            expenseMap.getOrDefault(name, 0.0) + value);
////
////                    totalExpense += value;
////                }
////            }
////        }
////
////        // ✅ STEP 2: GET GROSS PROFIT / LOSS
////        TradingAccountDTO trading = getTradingAccount();
////        double grossProfit = trading.getGrossProfit();
////
////        // ✅ STEP 3: ADD GROSS PROFIT / LOSS CORRECTLY
////        if (grossProfit >= 0) {
////            // 👉 Add to income
////            incomeMap.put("Gross Profit", grossProfit);
////            totalIncome += grossProfit;
////        } else {
////            // 👉 Add to expense
////            expenseMap.put("Gross Loss", Math.abs(grossProfit));
////            totalExpense += Math.abs(grossProfit);
////        }
////
////        // ✅ STEP 4: FINAL NET PROFIT
////        double netProfit = totalIncome - totalExpense;
////
////        // ✅ DTO
////        ProfitLossDTO dto = new ProfitLossDTO();
////        dto.setIncomeDetails(incomeMap);
////        dto.setExpenseDetails(expenseMap);
////        dto.setTotalIncome(totalIncome);
////        dto.setTotalExpense(totalExpense);
////        dto.setNetProfit(netProfit);
////
////        return dto;
////    }
////    
////  	
////
////    
////    public BalanceSheetDTO getBalanceSheet() {
////
////        List<JournalEntry> entries = journalRepo.findAll();
////
////        Map<String, Double> assets = new HashMap<>();
////        Map<String, Double> liabilities = new HashMap<>();
////        Map<String, Double> capitalAccounts = new HashMap<>();
////
////        double drawings = 0;
////
////        // ✅ STEP 1: PROCESS ENTRIES
////        for (JournalEntry entry : entries) {
////            for (JournalEntryLine line : entry.getLines()) {
////
////                Account acc = line.getAccount();
////                double balance = line.getDebit() - line.getCredit();
////
////                // ✅ ASSETS
////                if (acc.getType() == AccountType.ASSET || acc.getType() == AccountType.DEBTORS) {
////                    assets.put(acc.getName(),
////                            assets.getOrDefault(acc.getName(), 0.0) + balance);
////                }
////                
////                
////
////                // ✅ LIABILITIES
////                else if (acc.getType() == AccountType.LIABILITY || acc.getType() == AccountType.CREDITORS) {
////                    liabilities.put(acc.getName(),
////                            liabilities.getOrDefault(acc.getName(), 0.0) + (-balance));
////                }
////
////                // ✅ CAPITAL
////                else if (acc.getType() == AccountType.CAPITAL) {
////                    capitalAccounts.put(acc.getName(),
////                            capitalAccounts.getOrDefault(acc.getName(), 0.0) + (-balance));
////                }
////
////                // ✅ DRAWINGS
////                if (acc.getName().equalsIgnoreCase("Drawings")) {
////                    drawings += balance;
////                }
////            }
////        }
////        
////     // =========================
////     // ✅ ADD CLOSING STOCK (VERY IMPORTANT)
////     // =========================
////     double closingStock = 0;
////
////     for (Product product : productRepo.findAll()) {
////         closingStock += product.getOpeningStock() * product.getPurchasePrice();
////     }
////
////     // Add to assets
////     assets.put("Closing Stock", closingStock);
////
////        // ✅ STEP 2: GET PROFIT / LOSS
////        ProfitLossDTO profitLoss = getProfitLoss();
////        double netProfit = profitLoss.getNetProfit();
////
////        // ✅ STEP 3: TOTAL CAPITAL
////        double totalCapital = capitalAccounts.values()
////                .stream()
////                .mapToDouble(Double::doubleValue)
////                .sum();
////
////        // ✅ STEP 4: CLOSING CAPITAL
////        double closingCapital = totalCapital + netProfit - drawings;
////
////        // ✅ STEP 5: CAPITAL DETAILS (FOR UI)
////        Map<String, Double> capitalDetails = new LinkedHashMap<>();
////
////        // Opening capital accounts
////        for (Map.Entry<String, Double> entry : capitalAccounts.entrySet()) {
////            capitalDetails.put(entry.getKey(), entry.getValue());
////        }
////
////        // ✅ HANDLE PROFIT / LOSS PROPERLY
////        if (netProfit >= 0) {
////            capitalDetails.put("Net Profit", netProfit);
////        } else {
////            capitalDetails.put("Net Loss", Math.abs(netProfit)); // 🔥 FIX
////        }
////
////        // ✅ DRAWINGS
////        if (drawings > 0) {
////            capitalDetails.put("drawings", drawings);
////        }
////
////        capitalDetails.put("closingCapital", closingCapital);
////
////        // ✅ STEP 6: ADD ONLY FINAL CAPITAL TO LIABILITIES
////        liabilities.put("Capital", closingCapital);
////
////        // ✅ STEP 7: TOTALS (NOW CORRECT)
////        double totalAssets = assets.values()
////                .stream()
////                .mapToDouble(Double::doubleValue)
////                .sum();
////
////        double totalLiabilities = liabilities.values()
////                .stream()
////                .mapToDouble(Double::doubleValue)
////                .sum();
////
////        // ✅ FINAL DTO
////        BalanceSheetDTO dto = new BalanceSheetDTO();
////        dto.setAssets(assets);
////        dto.setLiabilities(liabilities);
////        dto.setCapitalDetails(capitalDetails);
////        dto.setTotalAssets(totalAssets);
////        dto.setTotalLiabilities(totalLiabilities);
////
////        return dto;
////    }
////
////	
////    
////    
////}
//
//
//
//
//
//package com.vpm.Accounts.Service;
//
//import com.vpm.Accounts.DTO.*;
//import com.vpm.Accounts.Entity.*;
//import com.vpm.Accounts.Repository.*;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class ReportService {
//
//    @Autowired
//    private PurchaseRepository purchaseRepo;
//
//    @Autowired
//    private SaleRepository saleRepo;
//
//    @Autowired
//    private ProductRepository productRepo;
//
//    @Autowired
//    private AccountRepository accountRepo;
//
//    @Autowired
//    private JournalEntryRepository journalRepo;
//
////    // =========================================================
////    // ✅ TRADING ACCOUNT
////    // =========================================================
////    public TradingAccountDTO getTradingAccount() {
////
////        double purchases = 0;
////        double sales = 0;
////        double openingStock = 0;
////        double closingStock = 0;
////
////        Map<String, Double> directExpenses = new LinkedHashMap<>();
////        double totalDirectExpenses = 0;
////
////        // =========================
////        // ✅ PURCHASES
////        // =========================
////        for (Purchase p : purchaseRepo.findAll()) {
////            for (PurchaseItem item : p.getItems()) {
////                purchases += item.getTotal();
////            }
////        }
////
////        // =========================
////        // ✅ SALES
////        // =========================
////        for (Sale s : saleRepo.findAll()) {
////            for (SaleItem item : s.getItems()) {
////                sales += item.getTotal();
////            }
////        }
////
////        // =========================
////        // ✅ OPENING STOCK (FROM ACCOUNT)
////        // =========================
////        Account opening = accountRepo.findByName("Opening Stock").orElse(null);
////        if (opening != null) {
////            openingStock = opening.getBalance();
////        }
////
////        // =========================
////        // ✅ DIRECT EXPENSES
////        // =========================
////        for (Account acc : accountRepo.findAll()) {
////            if (acc.getType() == AccountType.DIRECTEXPENSES) {
////                directExpenses.put(acc.getName(), acc.getBalance());
////                totalDirectExpenses += acc.getBalance();
////            }
////        }
////
////        // =========================
////        // ✅ CLOSING STOCK (FROM PRODUCTS)
////        // =========================
////        for (Product product : productRepo.findAll()) {
////            double value = product.getOpeningStock() * product.getPurchasePrice();
////            closingStock += value;
////        }
////
////        // =========================
////        // ✅ GROSS PROFIT
////        // =========================
////        double grossProfit =
////                (sales + closingStock)
////                - (purchases + openingStock + totalDirectExpenses);
////
////        TradingAccountDTO dto = new TradingAccountDTO();
////
////        dto.setPurchases(purchases);
////        dto.setSales(sales);
////        dto.setOpeningStock(openingStock);
////        dto.setClosingStock(closingStock);
////        dto.setGrossProfit(grossProfit);
////
////        dto.setDirectExpensesDetails(directExpenses);
////        dto.setDirectTotalExpense(totalDirectExpenses);
////
////        return dto;
////    }
//    
////    public TradingAccountDTO getTradingAccount() {
////
////        double purchases = 0;
////        double sales = 0;
////        double openingStock = 0;
////        double closingStock = 0;
////
////        // ✅ PURCHASES (from account)
////        Account purchaseAcc = accountRepo.findByName("Purchases").orElse(null);
////        if (purchaseAcc != null) {
////            purchases = purchaseAcc.getBalance();
////        }
////
////        // ✅ SALES
////        Account salesAcc = accountRepo.findByName("Sales").orElse(null);
////        if (salesAcc != null) {
////            sales = -salesAcc.getBalance(); // credit balance
////        }
////
////        // ✅ OPENING STOCK
////        Account opening = accountRepo.findByName("Opening Stock").orElse(null);
////        if (opening != null) {
////            openingStock = opening.getBalance();
////        }
////
////        // ✅ CLOSING STOCK (from products)
////        for (Product product : productRepo.findAll()) {
////            closingStock += product.getOpeningStock() * product.getPurchasePrice();
////        }
////
////        double grossProfit =
////                (sales + closingStock)
////                - (purchases + openingStock);
////
////        TradingAccountDTO dto = new TradingAccountDTO();
////        dto.setPurchases(purchases);
////        dto.setSales(sales);
////        dto.setOpeningStock(openingStock);
////        dto.setClosingStock(closingStock);
////        dto.setGrossProfit(grossProfit);
////
////        return dto;
////    }
//
//    
//    public TradingAccountDTO getTradingAccount() {
//
//        double purchases = 0;
//        double sales = 0;
//        double openingStock = 0;
//        double closingStock = 0;
//
//        // ✅ PURCHASES (from account)
//        Account purchaseAcc = accountRepo.findByName("Purchases").orElse(null);
//        if (purchaseAcc != null) {
//            purchases = purchaseAcc.getBalance();
//        }
//
//        // ✅ SALES
//        Account salesAcc = accountRepo.findByName("Sales").orElse(null);
//        if (salesAcc != null) {
//            sales = -salesAcc.getBalance(); // credit balance
//        }
//
//        // ✅ OPENING STOCK
//        Account opening = accountRepo.findByName("Opening Stock").orElse(null);
//        if (opening != null) {
//            openingStock = opening.getBalance();
//        }
//
//        // ✅ CLOSING STOCK (from products)
//        for (Product product : productRepo.findAll()) {
//            closingStock += product.getOpeningStock() * product.getPurchasePrice();
//        }
//
//        double grossProfit =
//                (sales + closingStock)
//                - (purchases + openingStock);
//
//        TradingAccountDTO dto = new TradingAccountDTO();
//        dto.setPurchases(purchases);
//        dto.setSales(sales);
//        dto.setOpeningStock(openingStock);
//        dto.setClosingStock(closingStock);
//        dto.setGrossProfit(grossProfit);
//
//        return dto;
//    }
//    
//    
//    // =========================================================
//    // ✅ PROFIT & LOSS ACCOUNT
//    // =========================================================
//    public ProfitLossDTO getProfitLoss() {
//
//        List<JournalEntry> entries = journalRepo.findAll();
//
//        Map<String, Double> incomeMap = new LinkedHashMap<>();
//        Map<String, Double> expenseMap = new LinkedHashMap<>();
//
//        double totalIncome = 0;
//        double totalExpense = 0;
//
//        // =========================
//        // ✅ NORMAL INCOME & EXPENSE
//        // =========================
//        for (JournalEntry entry : entries) {
//            for (JournalEntryLine line : entry.getLines()) {
//
//                Account acc = line.getAccount();
//
//                if (acc.getType() == AccountType.INCOME) {
//                    double value = line.getCredit() - line.getDebit();
//
//                    incomeMap.put(acc.getName(),
//                            incomeMap.getOrDefault(acc.getName(), 0.0) + value);
//
//                    totalIncome += value;
//                }
//
//                if (acc.getType() == AccountType.EXPENSE) {
//                    double value = line.getDebit() - line.getCredit();
//
//                    expenseMap.put(acc.getName(),
//                            expenseMap.getOrDefault(acc.getName(), 0.0) + value);
//
//                    totalExpense += value;
//                }
//            }
//        }
//
//        // =========================
//        // ✅ ADD GROSS PROFIT / LOSS
//        // =========================
//        double grossProfit = getTradingAccount().getGrossProfit();
//
//        if (grossProfit >= 0) {
//            incomeMap.put("Gross Profit", grossProfit);
//            totalIncome += grossProfit;
//        } else {
//            expenseMap.put("Gross Loss", Math.abs(grossProfit));
//            totalExpense += Math.abs(grossProfit);
//        }
//
//        // =========================
//        // ✅ NET PROFIT
//        // =========================
//        double netProfit = totalIncome - totalExpense;
//
//        ProfitLossDTO dto = new ProfitLossDTO();
//        dto.setIncomeDetails(incomeMap);
//        dto.setExpenseDetails(expenseMap);
//        dto.setTotalIncome(totalIncome);
//        dto.setTotalExpense(totalExpense);
//        dto.setNetProfit(netProfit);
//
//        return dto;
//    }
//
//    // =========================================================
//    // ✅ BALANCE SHEET
//    // =========================================================
//    public BalanceSheetDTO getBalanceSheet() {
//
//        List<JournalEntry> entries = journalRepo.findAll();
//
//        Map<String, Double> assets = new LinkedHashMap<>();
//        Map<String, Double> liabilities = new LinkedHashMap<>();
//        Map<String, Double> capitalAccounts = new LinkedHashMap<>();
//
//        double drawings = 0;
//
//        // =========================
//        // ✅ PROCESS JOURNAL
//        // =========================
//        for (JournalEntry entry : entries) {
//            for (JournalEntryLine line : entry.getLines()) {
//
//                Account acc = line.getAccount();
//                double balance = line.getDebit() - line.getCredit();
//
//                // ASSETS
//                if (acc.getType() == AccountType.ASSET || acc.getType() == AccountType.DEBTORS) {
//                    assets.put(acc.getName(),
//                            assets.getOrDefault(acc.getName(), 0.0) + balance);
//                }
//                
//////                CLOSING_STOCK
////                else if (acc.getType() == AccountType.ASSET || acc.getType() == AccountType.CLOSING_STOCK) {
////                    assets.put(acc.getName(),
////                            assets.getOrDefault(acc.getName(), 0.0) + balance);
////                }
//
//                // LIABILITIES
//                else if (acc.getType() == AccountType.LIABILITY || acc.getType() == AccountType.CREDITORS) {
//                    liabilities.put(acc.getName(),
//                            liabilities.getOrDefault(acc.getName(), 0.0) + (-balance));
//                }
//
//                // CAPITAL
//                else if (acc.getType() == AccountType.CAPITAL) {
//                    capitalAccounts.put(acc.getName(),
//                            capitalAccounts.getOrDefault(acc.getName(), 0.0) + (-balance));
//                }
//
//                // DRAWINGS
//                if (acc.getName().equalsIgnoreCase("Drawings")) {
//                    drawings += balance;
//                }
//            }
//        }
//
//        // =========================
//        // ✅ ADD CLOSING STOCK (CRITICAL 🔥)
//        // =========================
//        double closingStock = 0;
//
//        for (Product product : productRepo.findAll()) {
//            closingStock += product.getOpeningStock() * product.getPurchasePrice();
//        }
//
//        assets.put("Closing Stock", closingStock);
//
//        // =========================
//        // ✅ PROFIT
//        // =========================
//        double netProfit = getProfitLoss().getNetProfit();
//
//        // =========================
//        // ✅ CAPITAL CALCULATION
//        // =========================
//        double totalCapital = capitalAccounts.values()
//                .stream()
//                .mapToDouble(Double::doubleValue)
//                .sum();
//
//        double closingCapital = totalCapital + netProfit - drawings;
//
//        Map<String, Double> capitalDetails = new LinkedHashMap<>();
//
//        capitalAccounts.forEach(capitalDetails::put);
//
//        if (netProfit >= 0) {
//            capitalDetails.put("Net Profit", netProfit);
//        } else {
//            capitalDetails.put("Net Loss", Math.abs(netProfit));
//        }
//
//        if (drawings > 0) {
//            capitalDetails.put("Drawings", drawings);
//        }
//
//        capitalDetails.put("Closing Capital", closingCapital);
//
//        // =========================
//        // ✅ FINAL LIABILITY
//        // =========================
//        liabilities.put("Capital", closingCapital);
//
//        // =========================
//        // ✅ TOTALS
//        // =========================
//        double totalAssets = assets.values().stream().mapToDouble(Double::doubleValue).sum();
//        double totalLiabilities = liabilities.values().stream().mapToDouble(Double::doubleValue).sum();
//
//        BalanceSheetDTO dto = new BalanceSheetDTO();
//        dto.setAssets(assets);
//        dto.setLiabilities(liabilities);
//        dto.setCapitalDetails(capitalDetails);
//        dto.setTotalAssets(totalAssets);
//        dto.setTotalLiabilities(totalLiabilities);
//
//        return dto;
//    }
//}




package com.vpm.Accounts.Service;

import com.vpm.Accounts.DTO.*;
import com.vpm.Accounts.Entity.*;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportService {
	
	@Autowired
	private AccountRepository accountRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
	public TradingAccountDTO getTradingAccount() {

	    double purchases = 0;
	    double sales = 0;
	    double openingStock = 0;
	    double closingStock = 0;

	    // ✅ PURCHASES
	    Account purchaseAcc = accountRepo.findByName("Purchases").orElse(null);
	    if (purchaseAcc != null) {
	        purchases = purchaseAcc.getBalance(); // already debit balance
	    }

	    // ✅ SALES
	    Account salesAcc = accountRepo.findByName("Sales").orElse(null);
	    if (salesAcc != null) {
	        sales = Math.abs(salesAcc.getBalance()); // credit balance
	    }

	    // ✅ OPENING STOCK
	    Account opening = accountRepo.findByName("Opening Stock").orElse(null);
	    if (opening != null) {
	        openingStock = opening.getBalance();
	    }

	    // ✅ CLOSING STOCK (FROM PRODUCTS)
	    for (Product product : productRepo.findAll()) {
	        closingStock += product.getOpeningStock() * product.getPurchasePrice();
	    }

	    double grossProfit =
	            (sales + closingStock)
	            - (purchases + openingStock);

	    TradingAccountDTO dto = new TradingAccountDTO();
	    dto.setPurchases(purchases);
	    dto.setSales(sales);
	    dto.setOpeningStock(openingStock);
	    dto.setClosingStock(closingStock);
	    dto.setGrossProfit(grossProfit);

	    return dto;
	}
	
	public ProfitLossDTO getProfitLoss() {

	    Map<String, Double> incomeMap = new LinkedHashMap<>();
	    Map<String, Double> expenseMap = new LinkedHashMap<>();

	    double totalIncome = 0;
	    double totalExpense = 0;

	    for (Account acc : accountRepo.findAll()) {

	        // ✅ INCOME
	        if (acc.getType() == AccountType.INCOME) {
	            double value = Math.abs(acc.getBalance());
	            incomeMap.put(acc.getName(), value);
	            totalIncome += value;
	        }

	        // ✅ EXPENSE
	        if (acc.getType() == AccountType.EXPENSE) {
	            double value = acc.getBalance();
	            expenseMap.put(acc.getName(), value);
	            totalExpense += value;
	        }
	    }

	    // ✅ ADD GROSS PROFIT
	    double grossProfit = getTradingAccount().getGrossProfit();

	    if (grossProfit >= 0) {
	        incomeMap.put("Gross Profit", grossProfit);
	        totalIncome += grossProfit;
	    } else {
	        expenseMap.put("Gross Loss", Math.abs(grossProfit));
	        totalExpense += Math.abs(grossProfit);
	    }

	    double netProfit = totalIncome - totalExpense;

	    ProfitLossDTO dto = new ProfitLossDTO();
	    dto.setIncomeDetails(incomeMap);
	    dto.setExpenseDetails(expenseMap);
	    dto.setTotalIncome(totalIncome);
	    dto.setTotalExpense(totalExpense);
	    dto.setNetProfit(netProfit);

	    return dto;
	}
	
	public BalanceSheetDTO getBalanceSheet() {

	    Map<String, Double> assets = new LinkedHashMap<>();
	    Map<String, Double> liabilities = new LinkedHashMap<>();
	    Map<String, Double> capitalDetails = new LinkedHashMap<>();

	    double drawings = 0;

	    for (Account acc : accountRepo.findAll()) {

	        double balance = acc.getBalance();

	        // ✅ ASSETS
	        if (acc.getType() == AccountType.ASSET
	                || acc.getType() == AccountType.DEBTORS) {

	            assets.put(acc.getName(), balance);
	        }

	        // ✅ LIABILITIES (INCLUDING CREDITORS)
	        else if (acc.getType() == AccountType.LIABILITY
	                || acc.getType() == AccountType.CREDITORS) {

	            liabilities.put(acc.getName(), Math.abs(balance));
	        }

	        // ✅ CAPITAL
	        else if (acc.getType() == AccountType.CAPITAL) {

	            capitalDetails.put(acc.getName(), Math.abs(balance));
	        }

	        // ✅ DRAWINGS
	        if (acc.getType() == AccountType.DRAWINGS) {
	            drawings += balance;
	        }
	    }

	    // ✅ ADD CLOSING STOCK
	    double closingStock = 0;
	    for (Product product : productRepo.findAll()) {
	        closingStock += product.getOpeningStock() * product.getPurchasePrice();
	    }
	    assets.put("Closing Stock", closingStock);

	    // ✅ NET PROFIT
	    double netProfit = getProfitLoss().getNetProfit();

	    double totalCapital = capitalDetails.values()
	            .stream().mapToDouble(Double::doubleValue).sum();

	    double closingCapital = totalCapital + netProfit - drawings;

	    // ✅ CAPITAL DETAILS
	    if (netProfit >= 0) {
	        capitalDetails.put("Net Profit", netProfit);
	    } else {
	        capitalDetails.put("Net Loss", Math.abs(netProfit));
	    }

	    if (drawings > 0) {
	        capitalDetails.put("Drawings", drawings);
	    }

	    capitalDetails.put("Closing Capital", closingCapital);

	    // ✅ FINAL LIABILITY
	    liabilities.put("Capital", closingCapital);

	    // ✅ TOTALS
	    double totalAssets = assets.values().stream().mapToDouble(Double::doubleValue).sum();
	    double totalLiabilities = liabilities.values().stream().mapToDouble(Double::doubleValue).sum();

	    BalanceSheetDTO dto = new BalanceSheetDTO();
	    dto.setAssets(assets);
	    dto.setLiabilities(liabilities);
	    dto.setCapitalDetails(capitalDetails);
	    dto.setTotalAssets(totalAssets);
	    dto.setTotalLiabilities(totalLiabilities);

	    return dto;
	}
	
	
	

	
}