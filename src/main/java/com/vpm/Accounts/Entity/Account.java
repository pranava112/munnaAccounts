package com.vpm.Accounts.Entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


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
    private BigDecimal balance = BigDecimal.ZERO;
    
    public Long getId() {return id;}
    public void setId(Long id) {this.id=id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name=name;}
    
    public AccountType getType() {return type;}
    public void setType(AccountType type) {this.type=type;}
    
    public String getCode() {return code;}
    public void setCode(String code) {this.code=code;}
    
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    // public void debit(BigDecimal amount) {
    //     this.balance += amount;
    // }

    // public void credit(BigDecimal amount) {
    //     this.balance -= amount;
    // }

    public void debit(BigDecimal amount) {
    this.balance = this.balance.add(amount);
}

public void credit(BigDecimal amount) {
    this.balance = this.balance.subtract(amount);
}
    
    
}

// class Account {
//     Long id;
//     String name;
//     Account parent;
//     List<Account> children;
// }