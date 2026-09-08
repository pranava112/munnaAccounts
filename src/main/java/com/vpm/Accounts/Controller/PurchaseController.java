//
//
//package com.vpm.Accounts.Controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//import com.vpm.Accounts.Entity.Purchase;
//import com.vpm.Accounts.Service.PurchaseService;
//


//@RestController
//@RequestMapping("/api/purchase")
//@CrossOrigin("*")
//public class PurchaseController {
//
//    @Autowired
//    private PurchaseService service;
//
//    @PostMapping
//    public Purchase create(@RequestBody Purchase purchase) {
//        return service.savePurchase(purchase);
//    }
//
//    @GetMapping
//    public List<Purchase> getAll() {
//        return service.getAll();
//    }
//
//    @GetMapping("/{id}")
//    public Purchase getById(@PathVariable Long id) {
//        return service.getById(id);
//    }
//    
////    @PutMapping("/{id}")
////    public Purchase updatePurchase(@PathVariable Long id) {
////    	return service.getById(id);
////    }
//    
//    @PutMapping("/{id}")
//    public Purchase updatePurchase(@PathVariable Long id, @RequestBody Purchase purchase) {
//        return service.updatePurchase(id, purchase);
//    }
//
//    @DeleteMapping("/{id}")
//    public String delete(@PathVariable Long id) {
//        service.delete(id);
//        return "Deleted successfully";
//    }
//}


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

import com.vpm.Accounts.Entity.Purchase;
import com.vpm.Accounts.Service.PurchaseService;

@RestController
@RequestMapping("/api/purchase")
@CrossOrigin("*")
public class PurchaseController {

    @Autowired
    private PurchaseService service;

    @PostMapping
    public CompletableFuture<ResponseEntity<Object>> create(@RequestBody Purchase purchase) {
        return service.savePurchaseAsync(purchase)
                .thenApply(saved -> ResponseEntity.ok((Object) saved))
                .exceptionally(this::handleException);
    }

    @GetMapping
    public CompletableFuture<List<Purchase>> getAll() {
        return service.getAllAsync();
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Purchase>> getById(@NonNull @PathVariable Long id) {
        return service.getByIdAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Object>> updatePurchase(@NonNull @PathVariable Long id, @RequestBody Purchase purchase) {
        return service.updatePurchaseAsync(id, purchase)
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
                ? "Unable to save purchase" : cause.getMessage();
        HttpStatus status = message.contains("already exists")
                ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(java.util.Map.of("message", message));
    }
}