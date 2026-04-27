

package com.vpm.Accounts.DTO;

import java.util.Map;

public class TradingAccountDTO {

    private Map<String, Double> directIncome;
    private Map<String, Double> directExpenses;

    private double totalDirectIncome;
    private double totalDirectExpenses;

    private double purchases;
    private double sales;
    private double openingStock;
    private double closingStock;

    private double grossProfit;

    // ✅ NEW: product-wise closing stock details
    private Map<String, Double> stockDetails;

    // ✅ Getters & Setters

    public Map<String, Double> getDirectIncome() {
        return directIncome;
    }

    public void setDirectIncomeDetails(Map<String, Double> directIncome) {
        this.directIncome = directIncome;
    }

    public Map<String, Double> getDirectExpenses() {
        return directExpenses;
    }

    public void setDirectExpensesDetails(Map<String, Double> directExpenses) {
        this.directExpenses = directExpenses;
    }

    public double getTotalDirectIncome() {
        return totalDirectIncome;
    }

    public void setDirectTotalIncome(double totalDirectIncome) {
        this.totalDirectIncome = totalDirectIncome;
    }

    public double getTotalDirectExpenses() {
        return totalDirectExpenses;
    }

    public void setDirectTotalExpense(double totalDirectExpenses) {
        this.totalDirectExpenses = totalDirectExpenses;
    }

    public double getPurchases() {
        return purchases;
    }

    public void setPurchases(double purchases) {
        this.purchases = purchases;
    }

    public double getSales() {
        return sales;
    }

    public void setSales(double sales) {
        this.sales = sales;
    }

    public double getOpeningStock() {
        return openingStock;
    }

    public void setOpeningStock(double openingStock) {
        this.openingStock = openingStock;
    }

    public double getClosingStock() {
        return closingStock;
    }

    public void setClosingStock(double closingStock) {
        this.closingStock = closingStock;
    }

    public double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Map<String, Double> getStockDetails() {
        return stockDetails;
    }

    public void setStockDetails(Map<String, Double> stockDetails) {
        this.stockDetails = stockDetails;
    }
    
    public void setDirectIncome(Map<String, Double> directIncome) {
        this.directIncome = directIncome;
    }

    public void setDirectExpenses(Map<String, Double> directExpenses) {
        this.directExpenses = directExpenses;
    }
}

//
//package com.vpm.Accounts.DTO;
//
//import java.util.Map;
//
//public class TradingAccountDTO {
//
//    private double purchases;
//    private double sales;
//    private double openingStock;
//    private double closingStock;
//
//    private double grossProfit;
//
//    // ✅ NEW: Stock Details
//    private Map<String, Double> stockDetails;
//
//    // Getters & Setters
//
//    public double getPurchases() { return purchases; }
//    public void setPurchases(double purchases) { this.purchases = purchases; }
//
//    public double getSales() { return sales; }
//    public void setSales(double sales) { this.sales = sales; }
//
//    public double getOpeningStock() { return openingStock; }
//    public void setOpeningStock(double openingStock) { this.openingStock = openingStock; }
//
//    public double getClosingStock() { return closingStock; }
//    public void setClosingStock(double closingStock) { this.closingStock = closingStock; }
//
//    public double getGrossProfit() { return grossProfit; }
//    public void setGrossProfit(double grossProfit) { this.grossProfit = grossProfit; }
//
//    public Map<String, Double> getStockDetails() { return stockDetails; }
//    public void setStockDetails(Map<String, Double> stockDetails) { this.stockDetails = stockDetails; }
//}