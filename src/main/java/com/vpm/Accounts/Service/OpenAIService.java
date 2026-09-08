package com.vpm.Accounts.Service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.vpm.Accounts.DTO.ProfitLossDTO;


@Service
public class OpenAIService {


    public BigDecimal getProfitLoss(  ProfitLossDTO profitLoss){
       BigDecimal profit=profitLoss.getNetProfit();
       System.out.println("Profit is "+profit);
       return profit;
    }



    
    


  
}