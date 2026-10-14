package com.vpm.Accounts.DTO;

import java.math.BigDecimal;

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
    private BigDecimal amount;

    public JournalEntryResponse() {}

    public JournalEntryResponse(String debit, String credit, BigDecimal amount) {
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}