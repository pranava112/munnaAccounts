package com.vpm.Accounts.Entity;

public enum AccountType {
    ASSET, LIABILITY, INCOME, EXPENSE, CAPITAL, EQUITY, DEBTORS, CREDITORS,DRAWINGS,DIRECTEXPENSES,DIRECTINCOME,PURCHASES,SALES,OPENING_STOCK,CLOSING_STOCK;

    public static AccountType fromString(String value) {
        return AccountType.valueOf(value.toUpperCase());
    }
}