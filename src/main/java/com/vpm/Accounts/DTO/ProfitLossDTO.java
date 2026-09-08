package com.vpm.Accounts.DTO;

import java.math.BigDecimal;
import java.util.Map;

public class ProfitLossDTO {

    private Map<String, BigDecimal> incomeDetails;
    private Map<String, BigDecimal> expenseDetails;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;

    private BigDecimal grossProfit;
    
    public BigDecimal getGrossProfit() {return grossProfit;}
    public void setGrossProfit(BigDecimal grossProfit) {this.grossProfit=grossProfit;}
    
    public Map<String, BigDecimal> getIncomeDetails() { return incomeDetails; }
    public void setIncomeDetails(Map<String, BigDecimal> incomeDetails) { this.incomeDetails = incomeDetails; }

    public Map<String, BigDecimal> getExpenseDetails() { return expenseDetails; }
    public void setExpenseDetails(Map<String, BigDecimal> expenseDetails) { this.expenseDetails = expenseDetails; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
}