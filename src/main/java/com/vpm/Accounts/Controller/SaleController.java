package com.vpm.Accounts.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public CompletableFuture<ResponseEntity<Sale>> create(@RequestBody Sale sale) {
        return service.saveSaleAsync(sale)
                .thenApply(ResponseEntity::ok);
    }
    
    @GetMapping
    public CompletableFuture<List<Sale>> getAll(){
        return service.getAllAsync();
    }
    
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Sale>> getSaleById(@PathVariable Long id) {
        return service.getSaleByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }
    
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Sale>> updateSale(@PathVariable Long id, @RequestBody Sale sale) {
        return service.updateSaleAsync(id, sale)
                .thenApply(ResponseEntity::ok);
    }
    
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<String>> delete(@PathVariable Long id) {
        return service.deleteAsync(id)
                .thenApply(ResponseEntity::ok);
    }
}





