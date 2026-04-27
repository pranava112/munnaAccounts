package com.vpm.Accounts.Controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public CompletableFuture<TradingAccountDTO> getTradingAccount() {
        return service.getTradingAccountAsync();
    }

    @GetMapping("/profit-loss")
    public CompletableFuture<ProfitLossDTO> getProfitLoss() {
        return service.getProfitLossAsync();
    }

    @GetMapping("/balance-sheet")
    public CompletableFuture<BalanceSheetDTO> getBalanceSheet() {
        return service.getBalanceSheetAsync();
    }
    
}