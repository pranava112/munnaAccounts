package com.vpm.Accounts.Controller.AI;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.vpm.Accounts.DTO.AI.AIAccountDTO;
import com.vpm.Accounts.DTO.AI.AIJournalDraftDTO;
import com.vpm.Accounts.DTO.AI.AIProductDTO;
import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;
// import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Service.Accounts_Service;
import com.vpm.Accounts.Service.AI.AIJournalPostingService;
import com.vpm.Accounts.Service.AI.AITransactionPostingService;
import com.vpm.Accounts.Service.AI.OpenAIService;
import com.vpm.Accounts.security.TenantContext;

import com.vpm.Accounts.Service.AI.AIProviderRouter;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final OpenAIService openAIService;
    private final AIJournalPostingService aiJournalPostingService;
    private final Accounts_Service accountsService;
    private final AIProviderRouter aiProviderRouter;
    private final ProductRepository productRepository;
    private final AITransactionPostingService aiTransactionPostingService;

    public AIController(
            OpenAIService openAIService,
            AIJournalPostingService aiJournalPostingService,
            Accounts_Service accountsService,
            AIProviderRouter aiProviderRouter,
            ProductRepository productRepository,
            AITransactionPostingService aiTransactionPostingService) {

        this.openAIService = openAIService;
        this.aiJournalPostingService = aiJournalPostingService;
        this.accountsService = accountsService;
        this.aiProviderRouter = aiProviderRouter;
        this.productRepository = productRepository;
        this.aiTransactionPostingService = aiTransactionPostingService;
    }

    @PostMapping(
            value = "/scan-document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> scanDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            AITransactionDraftDTO draft = aiProviderRouter.scanDocument(
                    file, getAIAccounts(), getAIProducts());
            return ResponseEntity.ok(draft);
        } catch (ResponseStatusException exception) {
            return ResponseEntity.status(exception.getStatusCode()).body(
                    Map.of("message", exception.getReason()));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(
                    "AI document processing failed: " + exception.getMessage());
        }
    }

    @PostMapping("/confirm-transaction")
    public ResponseEntity<?> confirmTransaction(
            @RequestBody AITransactionDraftDTO draft) {
        try {
            return ResponseEntity.ok(aiTransactionPostingService.post(draft));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(
                    "Transaction confirmation failed: " + exception.getMessage());
        }
    }

    @PostMapping("/confirm-transaction/batch")
    public ResponseEntity<?> confirmTransactionBatch(
            @RequestBody List<AITransactionDraftDTO> drafts) {
        try {
            return ResponseEntity.ok(aiTransactionPostingService.postBatch(drafts));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(
                    "Transaction batch confirmation failed: " + exception.getMessage());
        }
    }

    @PostMapping("/validate-journal")
    public ResponseEntity<?> validateJournal(
            @RequestBody AIJournalDraftDTO draft) {
        try {
            openAIService.validateJournalDraft(draft);
            return ResponseEntity.ok(draft);
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/confirm-journal")
    public ResponseEntity<?> confirmJournal(
            @RequestBody AIJournalDraftDTO draft) {
        try {
            JournalEntry savedJournal = aiJournalPostingService.post(draft);
            return ResponseEntity.ok(savedJournal);
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(
                    "Journal confirmation failed: " + exception.getMessage());
        }
    }

    @PostMapping("/confirm-journal/batch")
    public ResponseEntity<?> confirmJournalBatch(
            @RequestBody List<AIJournalDraftDTO> drafts) {
        try {
            return ResponseEntity.ok(aiJournalPostingService.postBatch(drafts));
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body(
                    "Journal batch confirmation failed: " + exception.getMessage());
        }
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AIAccountDTO>> getAccounts() {
        return ResponseEntity.ok(getAIAccounts());
    }

    @GetMapping("/products")
    public ResponseEntity<List<AIProductDTO>> getProducts() {
        return ResponseEntity.ok(getAIProducts());
    }

    private List<AIAccountDTO> getAIAccounts() {
        return accountsService.getAllAccountsAsync(TenantContext.require()).join()
                .stream()
                .map(account -> new AIAccountDTO(
                        account.getId(),
                        account.getName(),
                        account.getCode(),
                        account.getType() != null ? account.getType().name() : null,
                        account.getAccountGroup()))
                .toList();
    }

    private List<AIProductDTO> getAIProducts() {
        return productRepository.findAll().stream()
                .filter(product -> product.isActive() || product.getActive())
                .map(this::toAIProductDTO)
                .toList();
    }

    private AIProductDTO toAIProductDTO(Product product) {
        return new AIProductDTO(
                product.getId(),
                product.getName(),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getGstRate(),
                product.getCessRate(),
                product.getHsnSacCode(),
                product.getCurrentStockQuantity());
    }
}
