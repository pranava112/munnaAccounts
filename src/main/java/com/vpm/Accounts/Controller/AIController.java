


package com.vpm.Accounts.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.Accounts.DTO.ProfitLossDTO;
import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Service.Accounts_Service;
import com.vpm.Accounts.Service.JournalEntryService;
import com.vpm.Accounts.Service.OpenAIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private Accounts_Service accountsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // private Account resolveAccount(String accountName) {
    //     if (accountName == null || accountName.isBlank()) {
    //         throw new RuntimeException("Account name is empty");
    //     }

    //     String normalized = accountName.trim();
    //     return accountsService.findAllByName(normalized)
    //             .or(() -> accountsService.findAllByNameIgnoreCase(normalized))
    //             .or(() -> accountsService.findAllByName(normalized).stream().findFirst())
    //             .or(() -> accountsService.findAllByNameIgnoreCase(normalized).stream().findFirst())
    //             .orElseThrow(() -> new RuntimeException("Account not found in DB: " + accountName));
    // }

  
    

    
}