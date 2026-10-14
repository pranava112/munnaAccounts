package com.vpm.Accounts.Entity;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.security.TenantEntityListener;


@Entity
@EntityListeners(TenantEntityListener.class)
@jakarta.persistence.Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
   
    @Enumerated(EnumType.STRING)
    private AccountType type;
    
    private String code;

    @Column(name = "account_group", length = 80)
    private String accountGroup;

    @Column(name = "contact_person", length = 120)
    private String contactPerson;

    @Column(name = "phone_number", length = 40)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Column(length = 500)
    private String address;

    @Column(name = "gstin", length = 15)
    private String gstin;

    @Column(name = "state", length = 80)
    private String state;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "account_custom_fields",
            joinColumns = @JoinColumn(name = "account_id"))
    @MapKeyColumn(name = "field_name", length = 80)
    @Column(name = "field_value", length = 500)
    private Map<String, String> customFields = new LinkedHashMap<>();
    
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;
    
    public Long getId() {return id;}
    public void setId(Long id) {this.id=id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name=name;}
    
    public AccountType getType() {return type;}
    public void setType(AccountType type) {this.type=type;}
    
    public String getCode() {return code;}
    public void setCode(String code) {this.code=code;}

    public String getAccountGroup() { return accountGroup; }
    public void setAccountGroup(String accountGroup) { this.accountGroup = accountGroup; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getStateCode() { return stateCode; }
    public void setStateCode(String stateCode) { this.stateCode = stateCode; }

    public Map<String, String> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(customFields);
    }
    
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    
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