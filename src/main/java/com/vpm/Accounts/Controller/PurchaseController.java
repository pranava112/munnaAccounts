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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.vpm.Accounts.Entity.Purchase;
import com.vpm.Accounts.Service.PurchaseService;

@RestController
@RequestMapping("/api/purchase")
@CrossOrigin("*")
public class PurchaseController {

    @Autowired
    private PurchaseService service;

    @PostMapping
    public Purchase create(@RequestBody Purchase purchase) {
        return service.savePurchase(purchase);
    }

    @GetMapping
    public List<Purchase> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Purchase getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Purchase updatePurchase(@PathVariable Long id, @RequestBody Purchase purchase) {
        return service.updatePurchase(id, purchase);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        return service.delete(id);
    }
}