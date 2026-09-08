


package com.vpm.Accounts.DTO;

import java.math.BigDecimal;
import java.util.Map;

public class BalanceSheetDTO {

    private Map<String, BigDecimal> assets;
    private Map<String, BigDecimal> liabilities;

    // ✅ NEW FIELD
    private Map<String, BigDecimal> capitalDetails;

    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;

    public Map<String, BigDecimal> getAssets() { return assets; }
    public void setAssets(Map<String, BigDecimal> assets) { this.assets = assets; }

    public Map<String, BigDecimal> getLiabilities() { return liabilities; }
    public void setLiabilities(Map<String, BigDecimal> liabilities) { this.liabilities = liabilities; }

    public Map<String, BigDecimal> getCapitalDetails() { return capitalDetails; }
    public void setCapitalDetails(Map<String, BigDecimal> capitalDetails) { this.capitalDetails = capitalDetails; }

    public BigDecimal getTotalAssets() { return totalAssets; }
    public void setTotalAssets(BigDecimal totalAssets) { this.totalAssets = totalAssets; }

    public BigDecimal getTotalLiabilities() { return totalLiabilities; }
    public void setTotalLiabilities(BigDecimal totalLiabilities) { this.totalLiabilities = totalLiabilities; }
}