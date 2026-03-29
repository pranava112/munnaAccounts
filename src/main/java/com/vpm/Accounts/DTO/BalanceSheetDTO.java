//package com.vpm.Accounts.DTO;
//
//import java.util.Map;
//
//public class BalanceSheetDTO {
//
//    private Map<String, Double> assets;
//    private Map<String, Double> liabilities;
//    private double totalAssets;
//    private double totalLiabilities;
//
//    public Map<String, Double> getAssets() { return assets; }
//    public void setAssets(Map<String, Double> assets) { this.assets = assets; }
//
//    public Map<String, Double> getLiabilities() { return liabilities; }
//    public void setLiabilities(Map<String, Double> liabilities) { this.liabilities = liabilities; }
//
//    public double getTotalAssets() { return totalAssets; }
//    public void setTotalAssets(double totalAssets) { this.totalAssets = totalAssets; }
//
//    public double getTotalLiabilities() { return totalLiabilities; }
//    public void setTotalLiabilities(double totalLiabilities) { this.totalLiabilities = totalLiabilities; }
//}


package com.vpm.Accounts.DTO;

import java.util.Map;

public class BalanceSheetDTO {

    private Map<String, Double> assets;
    private Map<String, Double> liabilities;

    // ✅ NEW FIELD
    private Map<String, Double> capitalDetails;

    private double totalAssets;
    private double totalLiabilities;

    public Map<String, Double> getAssets() { return assets; }
    public void setAssets(Map<String, Double> assets) { this.assets = assets; }

    public Map<String, Double> getLiabilities() { return liabilities; }
    public void setLiabilities(Map<String, Double> liabilities) { this.liabilities = liabilities; }

    public Map<String, Double> getCapitalDetails() { return capitalDetails; }
    public void setCapitalDetails(Map<String, Double> capitalDetails) { this.capitalDetails = capitalDetails; }

    public double getTotalAssets() { return totalAssets; }
    public void setTotalAssets(double totalAssets) { this.totalAssets = totalAssets; }

    public double getTotalLiabilities() { return totalLiabilities; }
    public void setTotalLiabilities(double totalLiabilities) { this.totalLiabilities = totalLiabilities; }
}