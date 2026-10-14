

// package com.vpm.Accounts.Entity;

// import jakarta.persistence.*;
// import java.util.*;
// import com.fasterxml.jackson.annotation.JsonManagedReference;

// @Entity
// public class Purchase {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String supplierName;//private Long supplierId;
//     private String date;
//     private Long phoneNo;

//      @Column(unique = true)
//     private String voucherNumber;

//     @OneToMany(
//             mappedBy = "purchase",
//             cascade = CascadeType.ALL,
//             orphanRemoval = true,
//             fetch = FetchType.EAGER
//     )
//     @JsonManagedReference
//     private List<PurchaseItem> items = new ArrayList<>();

//     // ✅ HELPER METHODS
//     public void addItem(PurchaseItem item) {
//         items.add(item);
//         item.setPurchase(this);
//     }

//     public void setItems(List<PurchaseItem> items) {
//         this.items = items;
//         if (items != null) {
//             items.forEach(i -> i.setPurchase(this));
//         }
//     }

//     // ✅ GETTERS & SETTERS
//     public Long getId() {
//         return id;
//     }

//      public String getVoucherNumber(){
//         return voucherNumber;
//     }

//     public void setVoucherNumber(String voucherNumber){
//         this.voucherNumber=voucherNumber;
//     }

//     public String getSupplierName() {
//         return supplierName;
//     }

//     public void setSupplierName(String supplierName) {
//         this.supplierName = supplierName;
//     }

//     public String getDate() {
//         return date;
//     }

//     public void setDate(String date) {
//         this.date = date;
//     }

//     public Long getPhoneNo() {
//         return phoneNo;
//     }

//     public void setPhoneNo(Long phoneNo) {
//         this.phoneNo = phoneNo;
//     }

//     public List<PurchaseItem> getItems() {
//         return items;
//     }
// }

package com.vpm.Accounts.Entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.security.TenantEntityListener;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_purchase_tenant_voucher",
        columnNames = {"tenant_id", "voucher_number"}))
@EntityListeners(TenantEntityListener.class)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String supplierName;

    private LocalDate date;

    private Long phoneNo;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal gstTaxableAmount = java.math.BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal gstCgst = java.math.BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal gstSgst = java.math.BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal gstIgst = java.math.BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal gstCess = java.math.BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal gstTotal = java.math.BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal grandTotal = java.math.BigDecimal.ZERO;

    @Column
    private String voucherNumber;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @OneToMany(
            mappedBy = "purchase",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JsonManagedReference
    private List<PurchaseItem> items = new ArrayList<>();

    // =========================================
    // ADD ITEM
    // =========================================

    public void addItem(PurchaseItem item) {

        if (item == null) {
            return;
        }

        items.add(item);
        item.setPurchase(this);
    }

    // =========================================
    // SET ITEMS
    // =========================================

    public void setItems(List<PurchaseItem> items) {

        this.items.clear();

        if (items != null) {

            for (PurchaseItem item : items) {

                if (item != null) {
                    this.items.add(item);
                    item.setPurchase(this);
                }
            }
        }
    }

    // =========================================
    // GETTERS & SETTERS
    // =========================================

    public Long getId() {
        return id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(Long phoneNo) {
        this.phoneNo = phoneNo;
    }

    public java.math.BigDecimal getGstTaxableAmount() { return gstTaxableAmount; }
    public void setGstTaxableAmount(java.math.BigDecimal value) { this.gstTaxableAmount = value; }
    public java.math.BigDecimal getGstCgst() { return gstCgst; }
    public void setGstCgst(java.math.BigDecimal value) { this.gstCgst = value; }
    public java.math.BigDecimal getGstSgst() { return gstSgst; }
    public void setGstSgst(java.math.BigDecimal value) { this.gstSgst = value; }
    public java.math.BigDecimal getGstIgst() { return gstIgst; }
    public void setGstIgst(java.math.BigDecimal value) { this.gstIgst = value; }
    public java.math.BigDecimal getGstCess() { return gstCess; }
    public void setGstCess(java.math.BigDecimal value) { this.gstCess = value; }
    public java.math.BigDecimal getGstTotal() { return gstTotal; }
    public void setGstTotal(java.math.BigDecimal value) { this.gstTotal = value; }
    public java.math.BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(java.math.BigDecimal value) { this.grandTotal = value; }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }
}