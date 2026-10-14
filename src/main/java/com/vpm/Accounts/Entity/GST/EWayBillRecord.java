package com.vpm.Accounts.Entity.GST;

import java.time.LocalDate;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vpm.Accounts.security.TenantEntityListener;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "eway_bill_records", uniqueConstraints = @UniqueConstraint(
        name = "uk_ewaybill_tenant_invoice", columnNames = {"tenant_id", "invoice_id"}))
@EntityListeners(TenantEntityListener.class)
public class EWayBillRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long invoiceId;

    private String ewbNumber;

    private String documentNumber;

    private LocalDate documentDate;

    private String transporterId;

    private String vehicleNumber;

    private String fromPincode;

    private String toPincode;

    private String status;

    private String errorMessage;

    @TenantId
    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    public Long getId() { return id; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getEwbNumber() { return ewbNumber; }
    public void setEwbNumber(String ewbNumber) { this.ewbNumber = ewbNumber; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public LocalDate getDocumentDate() { return documentDate; }
    public void setDocumentDate(LocalDate documentDate) { this.documentDate = documentDate; }
    public String getTransporterId() { return transporterId; }
    public void setTransporterId(String transporterId) { this.transporterId = transporterId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getFromPincode() { return fromPincode; }
    public void setFromPincode(String fromPincode) { this.fromPincode = fromPincode; }
    public String getToPincode() { return toPincode; }
    public void setToPincode(String toPincode) { this.toPincode = toPincode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    @JsonIgnore
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
