

package com.vpm.Accounts.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import org.hibernate.annotations.TenantId;
import com.vpm.Accounts.security.TenantEntityListener;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(
        name = "uk_product_tenant_barcode",
        columnNames = {"tenant_id", "barcode"}))
@EntityListeners(TenantEntityListener.class)
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

    @Column(name = "hsn_sac_code", length = 8)
    private String hsnSacCode;

    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate = BigDecimal.ZERO;

    @Column(name = "cess_rate", precision = 5, scale = 2)
    private BigDecimal cessRate = BigDecimal.ZERO;

    @Column(name = "opening_stock", precision = 19, scale = 2)
    private BigDecimal openingStockQuantity = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal openingStockValue = BigDecimal.ZERO;

    @Column(name = "opening_stock_date")
    private LocalDate openingStockDate;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentStockQuantity = BigDecimal.ZERO;

    @Column
    private String barcode;

    @Column(nullable = false)
    private boolean active = true;

    @TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

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

    public String getHsnSacCode() {
        return hsnSacCode;
    }

    public void setHsnSacCode(String hsnSacCode) {
        this.hsnSacCode = hsnSacCode;
    }

    public BigDecimal getGstRate() {
        return gstRate;
    }

    public void setGstRate(BigDecimal gstRate) {
        this.gstRate = gstRate;
    }

    public BigDecimal getCessRate() {
        return cessRate;
    }

    public void setCessRate(BigDecimal cessRate) {
        this.cessRate = cessRate;
    }

    public BigDecimal getOpeningStockQuantity() {
        return openingStockQuantity;
    }

    public void setOpeningStockQuantity(BigDecimal openingStockQuantity) {
        this.openingStockQuantity = openingStockQuantity;
    }

    public BigDecimal getOpeningStockValue() {
        return openingStockValue;
    }

    public void setOpeningStockValue(BigDecimal openingStockValue) {
        this.openingStockValue = openingStockValue;
    }

    public LocalDate getOpeningStockDate() {
        return openingStockDate;
    }

    public void setOpeningStockDate(LocalDate openingStockDate) {
        this.openingStockDate = openingStockDate;
    }

    public BigDecimal getCurrentStockQuantity() {
        return currentStockQuantity == null
                ? openingStockQuantity
                : currentStockQuantity;
    }

    public void setCurrentStockQuantity(BigDecimal currentStockQuantity) {
        this.currentStockQuantity = currentStockQuantity;
    }

    /**
     * Compatibility accessors for older API clients that used openingStock
     * as the available stock quantity.
     */
    public BigDecimal getOpeningStock() {
        return getCurrentStockQuantity();
    }

    public void setOpeningStock(BigDecimal openingStock) {
        this.openingStockQuantity = openingStock;
        this.currentStockQuantity = openingStock;
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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}