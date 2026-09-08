package com.vpm.Accounts.Entity;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String voucherNumber;
    private String customerName;
    private String date;
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

    private boolean discountEnabled;
    private boolean commissionEnabled;
    private boolean partialCashEnabled;

    private String discountType = "PERCENTAGE";
    private String commissionType = "PERCENTAGE";

    @Column(precision = 19, scale = 2)
    private BigDecimal discountValue = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal commissionValue = BigDecimal.ZERO;

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

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

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
}
