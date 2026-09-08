package com.vpm.Accounts.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Service.SaleService;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin("*")
public class SaleController {
    
    @Autowired
    private SaleService service;
    
    @PostMapping
    public CompletableFuture<ResponseEntity<Object>> create(@RequestBody Sale sale) {
        return service.saveSaleAsync(sale)
                .thenApply(saved -> ResponseEntity.ok((Object) saved))
                .exceptionally(this::handleException);
    }
    
    @GetMapping
    public CompletableFuture<List<Sale>> getAll(){
        return service.getAllAsync();
    }
    
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Sale>> getSaleById(@NonNull @PathVariable Long id) {
        return service.getSaleByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }
    
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Object>> updateSale(@NonNull @PathVariable Long id, @RequestBody Sale sale) {
        return service.updateSaleAsync(id, sale)
                .thenApply(updated -> ResponseEntity.ok((Object) updated))
                .exceptionally(this::handleException);
    }
    
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<String>> delete(@NonNull @PathVariable Long id) {
        return service.deleteAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    private ResponseEntity<Object> handleException(Throwable error) {
        Throwable cause = error;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }

        String message = cause.getMessage() == null
                ? "Unable to save sale" : cause.getMessage();
        HttpStatus status = message.contains("already exists")
                ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(java.util.Map.of("message", message));
    }
}





