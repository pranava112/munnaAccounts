

package com.vpm.Accounts.DTO;

import java.math.BigDecimal;
import java.util.Map;

public class TradingAccountDTO {

    private Map<String, BigDecimal> directIncome;
    private Map<String, BigDecimal> directExpenses;

    private BigDecimal totalDirectIncome;
    private BigDecimal totalDirectExpenses;

    private BigDecimal purchases;
    private BigDecimal sales;
    private BigDecimal openingStock;
    private BigDecimal closingStock;

    private BigDecimal grossProfit;

    // ✅ NEW: product-wise closing stock details
    private Map<String, BigDecimal> stockDetails;

    // ✅ Getters & Setters

    public Map<String, BigDecimal> getDirectIncome() {
        return directIncome;
    }

    public void setDirectIncomeDetails(Map<String, BigDecimal> directIncome) {
        this.directIncome = directIncome;
    }

    public Map<String, BigDecimal> getDirectExpenses() {
        return directExpenses;
    }

    public void setDirectExpensesDetails(Map<String, BigDecimal> directExpenses) {
        this.directExpenses = directExpenses;
    }

    public BigDecimal getTotalDirectIncome() {
        return totalDirectIncome;
    }

    public void setDirectTotalIncome(BigDecimal totalDirectIncome) {
        this.totalDirectIncome = totalDirectIncome;
    }

    public BigDecimal getTotalDirectExpenses() {
        return totalDirectExpenses;
    }

    public void setDirectTotalExpense(BigDecimal totalDirectExpenses) {
        this.totalDirectExpenses = totalDirectExpenses;
    }

    public BigDecimal getPurchases() {
        return purchases;
    }

    public void setPurchases(BigDecimal purchases) {
        this.purchases = purchases;
    }

    public BigDecimal getSales() {
        return sales;
    }

    public void setSales(BigDecimal sales) {
        this.sales = sales;
    }

    public BigDecimal getOpeningStock() {
        return openingStock;
    }

    public void setOpeningStock(BigDecimal openingStock) {
        this.openingStock = openingStock;
    }

    public BigDecimal getClosingStock() {
        return closingStock;
    }

    public void setClosingStock(BigDecimal closingStock) {
        this.closingStock = closingStock;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }

    public Map<String, BigDecimal> getStockDetails() {
        return stockDetails;
    }

    public void setStockDetails(Map<String, BigDecimal> stockDetails) {
        this.stockDetails = stockDetails;
    }
    
    public void setDirectIncome(Map<String, BigDecimal> directIncome) {
        this.directIncome = directIncome;
    }

    public void setDirectExpenses(Map<String, BigDecimal> directExpenses) {
        this.directExpenses = directExpenses;
    }
}
