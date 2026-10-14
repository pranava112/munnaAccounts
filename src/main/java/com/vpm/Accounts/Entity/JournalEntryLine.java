package com.vpm.Accounts.Entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.security.TenantEntityListener;


@Entity
@EntityListeners(TenantEntityListener.class)
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal debit;
    private BigDecimal credit;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

//    @ManyToOne
//    @JoinColumn(name = "journal_entry_id")
//    private JournalEntry journalEntry;
    
    
    @ManyToOne
    @JoinColumn(name = "journal_entry_id")
    @JsonBackReference   // ✅ FIX LOOP
    private JournalEntry journalEntry;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    // getters & setters
    
    


	public Long getId() {return id;}
	public void setId( Long id) {this.id=id;}
	
	public BigDecimal getDebit() {return debit;}
	public void setDebit(BigDecimal debit) {this.debit=debit;}
	
	public BigDecimal getCredit() {return credit;}
	public void setCredit(BigDecimal credit) {this.credit=credit;}
	
	public JournalEntry getJournalEntry() {return journalEntry;}
	public void setJournalEntry(JournalEntry journalEntry) {this.journalEntry=journalEntry;}
	
	public Account getAccount() {return account;}
	public void setAccount(Account account) {this.account=account;}

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
	
	
	
	

	
	
}
