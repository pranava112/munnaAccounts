package com.vpm.Accounts.Controller;

import com.vpm.Accounts.DTO.*;
import com.vpm.Accounts.Service.ReportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private ReportService service;
    
    @GetMapping("/tradingaccount")
    public TradingAccountDTO getTradingAccount() {
    	return service.getTradingAccount();
    }

    @GetMapping("/profit-loss")
    public ProfitLossDTO getProfitLoss() {
        return service.getProfitLoss();
    }

    @GetMapping("/balance-sheet")
    public BalanceSheetDTO getBalanceSheet() {
        return service.getBalanceSheet();
    }
    
}