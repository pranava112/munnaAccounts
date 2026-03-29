package com.vpm.Accounts.Entity;

import jakarta.persistence.*;


@Entity

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
   
    @Enumerated(EnumType.STRING)
    private AccountType type;
    
    private String code;
    
    @Column(nullable = false)
    private double balance = 0.0;
    
    public Long getId() {return id;}
    public void setId(Long id) {this.id=id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name=name;}
    
    public AccountType getType() {return type;}
    public void setType(AccountType type) {this.type=type;}
    
    public String getCode() {return code;}
    public void setCode(String code) {this.code=code;}
    
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public void debit(double amount) {
        this.balance += amount;
    }

    public void credit(double amount) {
        this.balance -= amount;
    }
    
    
}