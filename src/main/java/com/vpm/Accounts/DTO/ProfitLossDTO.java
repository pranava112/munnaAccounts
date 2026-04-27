package com.vpm.Accounts.DTO;

import java.util.Map;

public class ProfitLossDTO {

    private Map<String, Double> incomeDetails;
    private Map<String, Double> expenseDetails;

    private double totalIncome;
    private double totalExpense;
    private double netProfit;

    private double grossProfit;
    
    public double getGrossProfit() {return grossProfit;}
    public void setGrossProfit(double grossProfit) {this.grossProfit=grossProfit;}
    
    public Map<String, Double> getIncomeDetails() { return incomeDetails; }
    public void setIncomeDetails(Map<String, Double> incomeDetails) { this.incomeDetails = incomeDetails; }

    public Map<String, Double> getExpenseDetails() { return expenseDetails; }
    public void setExpenseDetails(Map<String, Double> expenseDetails) { this.expenseDetails = expenseDetails; }

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public double getNetProfit() { return netProfit; }
    public void setNetProfit(double netProfit) { this.netProfit = netProfit; }
}