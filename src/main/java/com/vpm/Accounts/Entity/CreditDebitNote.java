package com.vpm.Accounts.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpm.Accounts.Entity.GST.CreditDebitNoteLine;

import com.vpm.Accounts.Enum.InvoiceStatus;
import com.vpm.Accounts.Enum.NoteType;
import com.vpm.Accounts.security.TenantEntityListener;
import org.hibernate.annotations.TenantId;

import jakarta.persistence.*;

@Entity
@Table(name = "credit_debit_notes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tax_note_tenant_number",
                columnNames = {"tenant_id", "note_number"}),
        @UniqueConstraint(name = "uk_tax_note_source",
                columnNames = {"tenant_id", "source_type", "source_id"})
})
@EntityListeners(TenantEntityListener.class)
public class CreditDebitNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String noteNumber;

    private LocalDate noteDate;

    @Enumerated(EnumType.STRING)
    private NoteType noteType;

    private Long originalInvoiceId;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType = "SALES";

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    private String reason;

    private BigDecimal taxableAmount;

    private BigDecimal cgst;

    private BigDecimal sgst;

    private BigDecimal igst;

    private BigDecimal cess;

    private BigDecimal total;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CreditDebitNoteLine> lines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @TenantId
    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    // getters/setters


    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id=id;
    }

    public String getNoteNumber(){
        return noteNumber;
    }

    public void setNoteNumber(String noteNumber){
        this.noteNumber=noteNumber;
    }

    public LocalDate getNoteDate(){
        return noteDate;
    }

    public void setNoteDate(LocalDate noteDate){
        this.noteDate = noteDate;
    }

    public NoteType getNoteType(){
        return noteType;
    }

    public void setNoteType(NoteType noteType){
        this.noteType=noteType;
    }

    public Long getOriginalInvoiceId(){
        return originalInvoiceId;
    }

    public void setOriginalInvoiceId(Long originalInvoiceId){
         this.originalInvoiceId=originalInvoiceId;
    }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getReason(){
        return reason;
    }

    public void setReason(String reason){
        this.reason=reason;
    }

    public BigDecimal getTaxableAmount(){
        return taxableAmount;
    }

    public void setTaxableAmount(BigDecimal taxableAmount){
        this.taxableAmount = taxableAmount;
    }

    public BigDecimal getCgst(){
        return cgst;
    }

    public void setCgst(BigDecimal cgst){
        this.cgst=cgst;

    }

     public BigDecimal getSgst(){
        return sgst;
    }

    public void setSgst(BigDecimal sgst){
        this.sgst=sgst;

    }

     public BigDecimal getIgst(){
        return igst;
    }

    public void setIgst(BigDecimal igst){
        this.igst=igst;

    }

     public BigDecimal getCess(){
        return cess;
    }

    public void setCess(BigDecimal cess){
        this.cess=cess;

    }
    
    public BigDecimal getTotal(){
        return total;
    }

    public void setTotal(BigDecimal total){
        this.total=total;

    }

    public List<CreditDebitNoteLine> getLines() {
        return lines;
    }

    public void setLines(List<CreditDebitNoteLine> lines) {
        this.lines = lines == null ? new ArrayList<>() : lines;
        this.lines.forEach(line -> line.setNote(this));
    }

    public InvoiceStatus getStatus(){
        return status;
    }

     public void setStatus(InvoiceStatus status){
        this.status=status;
    }

    @JsonIgnore
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}