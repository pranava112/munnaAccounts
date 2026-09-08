

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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String supplierName;

    private String date;

    private Long phoneNo;

    @Column(unique = true)
    private String voucherNumber;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(Long phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }
}