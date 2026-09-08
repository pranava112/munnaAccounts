


// package com.vpm.Accounts.Entity;

// import java.math.BigDecimal;

// import jakarta.persistence.*;
// // import lombok.Data;
// // import lombok.Getter;
// // import lombok.Setter;

// @Entity
// // @Data
// // @Getter
// // @Setter
// @Table(name = "products")
// public class Product {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String name;
//     // private String code;

//     private BigDecimal purchasePrice;
//     private BigDecimal sellingPrice;
//     private BigDecimal openingStock;

//     // @Column(unique = true)
//     @Column( unique = true)
//     private String barcode;


//         public String getBarcode(){return barcode;}
//     public void setBarcode(String barcode){this.barcode=barcode;}

//     // Getters & Setters
//     public Long getId() { return id; }
//     public void setId(Long id) {this.id=id;}

//     public String getName() { return name; }
//     public void setName(String name) { this.name = name; }

//     // public String getCode() { return code; }
//     // public void setCode(String code) { this.code = code; }

//     public BigDecimal getPurchasePrice() { return purchasePrice; }
//     public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

//     public BigDecimal getSellingPrice() { return sellingPrice; }
//     public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

//     public BigDecimal getOpeningStock() { return openingStock; }
//     public void setOpeningStock(BigDecimal openingStock) { this.openingStock = openingStock; }

// }

package com.vpm.Accounts.Entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;

    @Column(precision = 19, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal openingStock = BigDecimal.ZERO;

    @Column(unique = true)
    private String barcode;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getOpeningStock() {
        return openingStock;
    }

    public void setOpeningStock(BigDecimal openingStock) {
        this.openingStock = openingStock;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isActive() {
        return active;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}