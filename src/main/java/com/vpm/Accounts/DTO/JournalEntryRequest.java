package com.vpm.Accounts.DTO;

public class JournalEntryRequest {
    private String transaction;

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }
}

class JournalEntryResponse {
    private String debit;
    private String credit;
    private double amount;

    public JournalEntryResponse() {}

    public JournalEntryResponse(String debit, String credit, double amount) {
        this.debit = debit;
        this.credit = credit;
        this.amount = amount;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}