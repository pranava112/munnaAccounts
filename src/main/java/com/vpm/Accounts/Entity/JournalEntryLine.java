package com.vpm.Accounts.Entity;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
public class JournalEntryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double debit;
    private double credit;

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

    // getters & setters
    
    


	public Long getId() {return id;}
	public void setId( Long id) {this.id=id;}
	
	public double getDebit() {return debit;}
	public void setDebit(double debit) {this.debit=debit;}
	
	public double getCredit() {return credit;}
	public void setCredit(double credit) {this.credit=credit;}
	
	public JournalEntry getJournalEntry() {return journalEntry;}
	public void setJournalEntry(JournalEntry journalEntry) {this.journalEntry=journalEntry;}
	
	public Account getAccount() {return account;}
	public void setAccount(Account account) {this.account=account;}
	
	
	
	

	
	
}
