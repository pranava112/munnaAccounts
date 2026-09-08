
// // package com.vpm.Accounts.Entity;

// // import jakarta.persistence.*;

// // import java.math.BigDecimal;

// // import com.fasterxml.jackson.annotation.JsonBackReference;

// // @Entity
// // public class PurchaseItem {

// //     @Id
// //     @GeneratedValue(strategy = GenerationType.IDENTITY)
// //     private Long id;

// //     private int quantity;
// //     private BigDecimal price;

// //     @ManyToOne
// //     @JoinColumn(name = "product_id")
// //     private Product product;

// //     @ManyToOne
// //     @JoinColumn(name = "purchase_id")
// //     @JsonBackReference
// //     private Purchase purchase;

// //     // public BigDecimal getTotal() {
// //     //     return quantity * price;
// //     // }

// // //     public BigDecimal getTotal() {
// // //     return quantity.multiply(price);
// // // }

// // public BigDecimal getTotal() {
// //     return price.multiply(BigDecimal.valueOf(quantity));
// // }

// //     public Long getId() { return id; }

// //     public int getQuantity() { return quantity; }
// //     public void setQuantity(int quantity) { this.quantity = quantity; }

// //     public BigDecimal getPrice() { return price; }
// //     public void setPrice(BigDecimal price) { this.price = price; }

// //     public Product getProduct() { return product; }
// //     public void setProduct(Product product) { this.product = product; }

// //     public Purchase getPurchase() { return purchase; }
// //     public void setPurchase(Purchase purchase) { this.purchase = purchase; }
// // }


// package com.vpm.Accounts.Entity;

// import jakarta.persistence.*;
// import java.math.BigDecimal;

// import com.fasterxml.jackson.annotation.JsonBackReference;

// @Entity
// public class PurchaseItem {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private int quantity;

//     private BigDecimal price;

//     @ManyToOne
//     @JoinColumn(name = "product_id")
//     private Product product;

//     @ManyToOne
//     @JoinColumn(name = "purchase_id")
//     @JsonBackReference
//     private Purchase purchase;

//     public BigDecimal getTotal() {
//         if (price == null) {
//             return BigDecimal.ZERO;
//         }

//         return price.multiply(BigDecimal.valueOf(quantity));
//     }

//     public Long getId() {
//         return id;
//     }

//     public int getQuantity() {
//         return quantity;
//     }

//     public void setQuantity(int quantity) {
//         this.quantity = quantity;
//     }

//     public BigDecimal getPrice() {
//         return price;
//     }

//     public void setPrice(BigDecimal price) {
//         this.price = price;
//     }

//     public Product getProduct() {
//         return product;
//     }

//     public void setProduct(Product product) {
//         this.product = product;
//     }

//     public Purchase getPurchase() {
//         return purchase;
//     }

//     public void setPurchase(Purchase purchase) {
//         this.purchase = purchase;
//     }
// }

package com.vpm.Accounts.Entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    private BigDecimal price = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    @JsonBackReference
    private Purchase purchase;

    // =========================================
    // CALCULATE TOTAL
    // =========================================

    @Transient
    public BigDecimal getTotal() {

        if (price == null) {
            return BigDecimal.ZERO;
        }

        return price.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    // =========================================
    // GETTERS & SETTERS
    // =========================================

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative"
            );
        }

        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }
}