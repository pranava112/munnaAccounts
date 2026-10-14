package com.vpm.Accounts.DTO.AI;

import java.math.BigDecimal;

public class AIProductDTO {

    private Long id;
    private String name;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private BigDecimal gstRate;
    private BigDecimal cessRate;
    private String hsnSacCode;
    private BigDecimal currentStockQuantity;

    public AIProductDTO() {
    }

    public AIProductDTO(
            Long id,
            String name,
            BigDecimal purchasePrice,
            BigDecimal sellingPrice,
            BigDecimal gstRate,
            BigDecimal cessRate,
            String hsnSacCode,
            BigDecimal currentStockQuantity) {
        this.id = id;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.gstRate = gstRate;
        this.cessRate = cessRate;
        this.hsnSacCode = hsnSacCode;
        this.currentStockQuantity = currentStockQuantity;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public BigDecimal getGstRate() { return gstRate; }
    public BigDecimal getCessRate() { return cessRate; }
    public String getHsnSacCode() { return hsnSacCode; }
    public BigDecimal getCurrentStockQuantity() { return currentStockQuantity; }
}
