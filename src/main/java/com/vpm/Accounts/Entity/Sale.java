package com.vpm.Accounts.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.security.TenantEntityListener;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_sale_tenant_voucher",
        columnNames = {"tenant_id", "voucher_number"}))
@EntityListeners(TenantEntityListener.class)
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String voucherNumber;
    private String customerName;
    private LocalDate date;
    private Long phoneNo;

    @Column(precision = 19, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal cashPaid = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal gstTaxableAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal gstCgst = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal gstSgst = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal gstIgst = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal gstCess = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal gstTotal = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    private boolean discountEnabled;
    private boolean commissionEnabled;
    private boolean partialCashEnabled;

    private String discountType = "PERCENTAGE";
    private String commissionType = "PERCENTAGE";

    @Column(precision = 19, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal commissionValue = BigDecimal.ZERO;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @OneToMany(mappedBy = "sale",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<SaleItem> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVoucherNumber() { return voucherNumber; }
    public void setVoucherNumber(String voucherNumber) { this.voucherNumber = voucherNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getPhoneNo() { return phoneNo; }
    public void setPhoneNo(Long phoneNo) { this.phoneNo = phoneNo; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }

    public BigDecimal getCashPaid() { return cashPaid; }
    public void setCashPaid(BigDecimal cashPaid) { this.cashPaid = cashPaid; }

    public BigDecimal getCreditAmount() { return creditAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }
    public BigDecimal getGstTaxableAmount() { return gstTaxableAmount; }
    public void setGstTaxableAmount(BigDecimal value) { this.gstTaxableAmount = value; }
    public BigDecimal getGstCgst() { return gstCgst; }
    public void setGstCgst(BigDecimal value) { this.gstCgst = value; }
    public BigDecimal getGstSgst() { return gstSgst; }
    public void setGstSgst(BigDecimal value) { this.gstSgst = value; }
    public BigDecimal getGstIgst() { return gstIgst; }
    public void setGstIgst(BigDecimal value) { this.gstIgst = value; }
    public BigDecimal getGstCess() { return gstCess; }
    public void setGstCess(BigDecimal value) { this.gstCess = value; }
    public BigDecimal getGstTotal() { return gstTotal; }
    public void setGstTotal(BigDecimal value) { this.gstTotal = value; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal value) { this.grandTotal = value; }

    public boolean isDiscountEnabled() { return discountEnabled; }
    public boolean getDiscountEnabled() { return discountEnabled; }
    public void setDiscountEnabled(boolean discountEnabled) { this.discountEnabled = discountEnabled; }

    public boolean isCommissionEnabled() { return commissionEnabled; }
    public boolean getCommissionEnabled() { return commissionEnabled; }
    public void setCommissionEnabled(boolean commissionEnabled) { this.commissionEnabled = commissionEnabled; }

    public boolean isPartialCashEnabled() { return partialCashEnabled; }
    public boolean getPartialCashEnabled() { return partialCashEnabled; }
    public void setPartialCashEnabled(boolean partialCashEnabled) { this.partialCashEnabled = partialCashEnabled; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public String getCommissionType() { return commissionType; }
    public void setCommissionType(String commissionType) { this.commissionType = commissionType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getCommissionValue() { return commissionValue; }
    public void setCommissionValue(BigDecimal commissionValue) { this.commissionValue = commissionValue; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
