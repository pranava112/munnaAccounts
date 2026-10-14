package com.vpm.Accounts.Controller.Accounts;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vpm.Accounts.DTO.BalanceSheetDTO;
import com.vpm.Accounts.DTO.ProfitLossDTO;
import com.vpm.Accounts.DTO.TradingAccountDTO;
import com.vpm.Accounts.Service.ReportService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private ReportService service;
    
    @GetMapping("/tradingaccount")
    public CompletableFuture<TradingAccountDTO> getTradingAccount(
            @RequestParam(required = false) Long financialYearId) {
        return service.getTradingAccountAsync(financialYearId);
    }

    @GetMapping("/profit-loss")
    public CompletableFuture<ProfitLossDTO> getProfitLoss(
            @RequestParam(required = false) Long financialYearId) {
        return service.getProfitLossAsync(financialYearId);
    }

    @GetMapping("/balance-sheet")
    public CompletableFuture<BalanceSheetDTO> getBalanceSheet(
            @RequestParam(required = false) Long financialYearId) {
        return service.getBalanceSheetAsync(financialYearId);
    }
    
}