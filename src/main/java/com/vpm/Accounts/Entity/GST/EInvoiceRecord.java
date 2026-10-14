package com.vpm.Accounts.Entity.GST;


import java.time.LocalDate;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpm.Accounts.security.TenantEntityListener;
import org.hibernate.annotations.TenantId;
@Entity
@Table(name = "e_invoice_records", uniqueConstraints = @UniqueConstraint(
        name = "uk_einvoice_tenant_invoice", columnNames = {"tenant_id", "invoice_id"}))
@EntityListeners(TenantEntityListener.class)
public class EInvoiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long invoiceId;

    private String irn;

    private String ackNumber;

    private LocalDate ackDate;

    private String qrCode;

    private String signedInvoice;

    private String status;

    private String errorCode;

    private String errorMessage;

    @TenantId
    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    public Long getId() { return id; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getIrn() { return irn; }
    public void setIrn(String irn) { this.irn = irn; }
    public String getAckNumber() { return ackNumber; }
    public void setAckNumber(String ackNumber) { this.ackNumber = ackNumber; }
    public LocalDate getAckDate() { return ackDate; }
    public void setAckDate(LocalDate ackDate) { this.ackDate = ackDate; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getSignedInvoice() { return signedInvoice; }
    public void setSignedInvoice(String signedInvoice) { this.signedInvoice = signedInvoice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    @JsonIgnore
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
