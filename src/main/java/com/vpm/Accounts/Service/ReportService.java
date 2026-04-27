package com.vpm.Accounts.Service;

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

    // =========================================
    // 🔹 CLOSING STOCK
    // =========================================
    private double calculateClosingStock(Map<String, Double> stockDetails) {

        double total = 0;

        for (Product p : productRepo.findAll()) {
            double value = p.getOpeningStock() * p.getPurchasePrice();
            stockDetails.put(p.getName(), value);
            total += value;
        }

        return total;
    }

    // =========================================
    // 🔹 TRADING ACCOUNT
    // =========================================
    public TradingAccountDTO getTradingAccount() {

        double purchases = 0;
        double sales = 0;
        double openingStock = 0;

        Map<String, Double> directIncome = new LinkedHashMap<>();
        Map<String, Double> directExpense = new LinkedHashMap<>();

        double totalDirectIncome = 0;
        double totalDirectExpense = 0;

        // ✅ READ FROM JOURNAL ONLY
        for (JournalEntry entry : journalRepo.findAll()) {
            for (JournalEntryLine line : entry.getLines()) {

                Account acc = line.getAccount();
                if (acc == null) continue;

                double debit = line.getDebit();
                double credit = line.getCredit();

                switch (acc.getType()) {

                    case PURCHASES:
                        purchases += debit-credit;
                        break;

                    case SALES:
                        sales += credit-debit;
                        break;

                    case OPENING_STOCK:
                        openingStock += debit-credit;
                        break;

                    case DIRECTINCOME:
                        directIncome.put(
                                acc.getName(),
                                directIncome.getOrDefault(acc.getName(), 0.0) + credit-debit
                        );
                        totalDirectIncome += credit-debit;
                        break;

                    case DIRECTEXPENSES:
                        directExpense.put(
                                acc.getName(),
                                directExpense.getOrDefault(acc.getName(), 0.0) + debit-credit
                        );
                        totalDirectExpense += debit-credit;
                        break;

                    default:
                        break;
                }
            }
        }

        // 🔹 CLOSING STOCK
        Map<String, Double> stockDetails = new LinkedHashMap<>();
        double closingStock = calculateClosingStock(stockDetails);

        // 🔹 GROSS PROFIT
        double grossProfit =
                (sales + closingStock + totalDirectIncome)
                        - (purchases + openingStock + totalDirectExpense);

        TradingAccountDTO dto = new TradingAccountDTO();
        dto.setPurchases(purchases);
        dto.setSales(sales);
        dto.setOpeningStock(openingStock);
        dto.setClosingStock(closingStock);
        dto.setDirectIncome(directIncome);
        dto.setDirectExpenses(directExpense);
        dto.setDirectTotalIncome(totalDirectIncome);
        dto.setDirectTotalExpense(totalDirectExpense);
        dto.setGrossProfit(grossProfit);
        dto.setStockDetails(stockDetails);

        return dto;
    }

    // =========================================
    // 🔹 PROFIT & LOSS
    // =========================================
    public ProfitLossDTO getProfitLoss() {

        double grossProfit = getTradingAccount().getGrossProfit();

        Map<String, Double> incomeMap = new LinkedHashMap<>();
        Map<String, Double> expenseMap = new LinkedHashMap<>();

        double totalIncome = 0;
        double totalExpense = 0;

        for (JournalEntry entry : journalRepo.findAll()) {
            for (JournalEntryLine line : entry.getLines()) {

                Account acc = line.getAccount();
                if (acc == null) continue;

                double debit = line.getDebit();
                double credit = line.getCredit();

                if (acc.getType() == AccountType.INCOME) {
                    incomeMap.put(acc.getName(),
                            incomeMap.getOrDefault(acc.getName(), 0.0) + credit-debit);//changes if return of products or services by -debit
                    totalIncome += credit-debit;
                }

                if (acc.getType() == AccountType.EXPENSE) {
                    expenseMap.put(acc.getName(),
                            expenseMap.getOrDefault(acc.getName(), 0.0) + debit-credit);//changes if return of products or services by -credit
                    totalExpense += debit-credit;
                }
            }
        }

        double netProfit = (grossProfit + totalIncome) - totalExpense;

        ProfitLossDTO dto = new ProfitLossDTO();
        dto.setGrossProfit(grossProfit);
        dto.setIncomeDetails(incomeMap);
        dto.setExpenseDetails(expenseMap);
        dto.setTotalIncome(totalIncome);
        dto.setTotalExpense(totalExpense);
        dto.setNetProfit(netProfit);

        return dto;
    }

    
    
    
    
    
    
    
    // =========================================
    // 🔹 BALANCE SHEET
    // =========================================
    public BalanceSheetDTO getBalanceSheet() {

        Map<String, Double> assets = new LinkedHashMap<>();
        Map<String, Double> liabilities = new LinkedHashMap<>();
        Map<String, Double> capitalDetails = new LinkedHashMap<>();

        double drawings = 0;

        for (JournalEntry entry : journalRepo.findAll()) {
            for (JournalEntryLine line : entry.getLines()) {

                Account acc = line.getAccount();
                if (acc == null) continue;

                double debit = line.getDebit();
                double credit = line.getCredit();

                switch (acc.getType()) {

                    case ASSET:
                    case DEBTORS:
                        assets.put(acc.getName(),
                                assets.getOrDefault(acc.getName(), 0.0) + (debit - credit));
                        break;

                    case LIABILITY:
                    case CREDITORS:
                        liabilities.put(acc.getName(),
                                liabilities.getOrDefault(acc.getName(), 0.0) + (credit - debit));
                        break;

                    case CAPITAL:
                        capitalDetails.put(acc.getName(),
                                capitalDetails.getOrDefault(acc.getName(), 0.0) + (credit - debit));
                        break;

                    case DRAWINGS:
                        drawings += debit;
                        break;

                    default:
                        break;
                }
            }
        }

        // 🔹 ADD CLOSING STOCK
        double closingStock = 0;
        for (Product p : productRepo.findAll()) {
            closingStock += p.getOpeningStock() * p.getPurchasePrice();
        }
        assets.put("Closing Stock", closingStock);

        // 🔹 NET PROFIT
        double netProfit = getProfitLoss().getNetProfit();

        double openingCapital = capitalDetails.values()
                .stream().mapToDouble(Double::doubleValue).sum();

        double closingCapital = openingCapital + netProfit - drawings;

//        capitalDetails.put("Net Profit", netProfit);
        
        if(netProfit>=0) {
        	 capitalDetails.put("Add: Net Profit", netProfit);
        }
        else {
        	 capitalDetails.put("Less: Net Loss", netProfit);
        }
        
        capitalDetails.put("Less: Drawings", drawings);
        capitalDetails.put("Closing Capital", closingCapital);

        liabilities.put("Capital", closingCapital);

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

    @Async("accountExecutor")
    public CompletableFuture<TradingAccountDTO> getTradingAccountAsync() {
        return CompletableFuture.completedFuture(getTradingAccount());
    }

    @Async("accountExecutor")
    public CompletableFuture<ProfitLossDTO> getProfitLossAsync() {
        return CompletableFuture.completedFuture(getProfitLoss());
    }

    @Async("accountExecutor")
    public CompletableFuture<BalanceSheetDTO> getBalanceSheetAsync() {
        return CompletableFuture.completedFuture(getBalanceSheet());
    }
}