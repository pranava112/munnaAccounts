

package com.vpm.Accounts.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.vpm.Accounts.Entity.*;
import com.vpm.Accounts.Repository.*;

@Service
public class PurchaseService {
//
//    @Autowired
//    private PurchaseRepository purchaseRepo;
//
//    @Autowired
//    private ProductRepository productRepo;
//    
//    @Autowired
//    private AccountRepository accountRepo;
//    
//
//    
// // ✅ SAVE PURCHASE + STOCK + ACCOUNT UPDATE
//    public Purchase savePurchase(Purchase purchase) {
//
//        double totalAmount = 0;
//
//        // =========================
//        // ✅ STOCK UPDATE
//        // =========================
//        for (PurchaseItem item : purchase.getItems()) {
//
//            Product product = productRepo.findById(item.getProduct().getId())
//                    .orElseThrow(() -> new RuntimeException("Product not found"));
//
//            // Increase stock
//            product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
//            productRepo.save(product);
//
//            item.setProduct(product);
//            item.setPurchase(purchase);
//
//            totalAmount += item.getTotal();
//        }
//
//        // =========================
//        // ✅ ACCOUNT UPDATE
//        // =========================
//        Account account = accountRepo.findByName(purchase.getSupplierName())
//                .orElse(null);
//
//        if (account != null) {
//            // ✅ CREDITOR → increase liability
//            account.setBalance(account.getBalance() + totalAmount);
//            accountRepo.save(account);
//
//        } else {
//            // ✅ CASH PURCHASE
//            Account cash = accountRepo.findByName("CASH")
//                    .orElseThrow(() -> new RuntimeException("Cash account not found"));
//
//            cash.setBalance(cash.getBalance() - totalAmount);
//            accountRepo.save(cash);
//        }
//
//        // =========================
//        // ✅ SAVE PURCHASE (LAST STEP)
//        // =========================
//        return purchaseRepo.save(purchase);
//    }


@Autowired
private PurchaseRepository purchaseRepo;

@Autowired
private ProductRepository productRepo;

@Autowired
private AccountRepository accountRepo;

public Purchase savePurchase(Purchase purchase) {

    double totalAmount = 0;

    // =========================
    // ✅ STOCK UPDATE
    // =========================
    for (PurchaseItem item : purchase.getItems()) {

        Product product = productRepo.findById(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
        productRepo.save(product);

        item.setProduct(product);
        item.setPurchase(purchase);

        totalAmount += item.getTotal();
    }

    // =========================
    // ✅ PURCHASE ACCOUNT (Dr)
    // =========================
    Account purchaseAcc = accountRepo.findByName("Purchases")
            .orElseThrow(() -> new RuntimeException("Purchases account not found"));

    purchaseAcc.debit(totalAmount);
    accountRepo.save(purchaseAcc);

    // =========================
    // ✅ CASH / CREDITOR (Cr)
    // =========================
    Account supplier = accountRepo.findByName(purchase.getSupplierName())
            .orElse(null);

    if (supplier != null) {
        supplier.credit(totalAmount); // creditor increases
        accountRepo.save(supplier);
    } else {
        Account cash = accountRepo.findByName("Cash")
                .orElseThrow(() -> new RuntimeException("Cash not found"));

        cash.credit(totalAmount); // cash goes out
        accountRepo.save(cash);
    }

    return purchaseRepo.save(purchase);
}

    // ✅ GET ALL
    public List<Purchase> getAll() {
        return purchaseRepo.findAll();
    }

    // ✅ GET BY ID
    public Purchase getById(Long id) {
        return purchaseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
    }

    // ✅ UPDATE PURCHASE (WITH STOCK FIX)
    public Purchase updatePurchase(Long id, Purchase newPurchase) {

        Purchase existing = getById(id);

        // 🔥 REVERSE OLD STOCK
        for (PurchaseItem oldItem : existing.getItems()) {
            Product product = oldItem.getProduct();
            product.setOpeningStock(product.getOpeningStock() - oldItem.getQuantity());
            productRepo.save(product);
        }

        // 🔥 APPLY NEW STOCK
        for (PurchaseItem newItem : newPurchase.getItems()) {

            Product product = productRepo.findById(newItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setOpeningStock(product.getOpeningStock() + newItem.getQuantity());
            productRepo.save(product);

            newItem.setProduct(product);
            newItem.setPurchase(existing);
        }

        existing.setItems(newPurchase.getItems());
        existing.setDate(newPurchase.getDate());
        existing.setSupplierName(newPurchase.getSupplierName());
        existing.setPhoneNo(newPurchase.getPhoneNo());

        return purchaseRepo.save(existing);
    }

    // ✅ DELETE PURCHASE (WITH STOCK FIX)
    public String delete(Long id) {

        Purchase purchase = getById(id);

        // 🔥 REDUCE STOCK
        for (PurchaseItem item : purchase.getItems()) {
            Product product = item.getProduct();
            product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
            productRepo.save(product);
        }

        purchaseRepo.deleteById(id);

        return "Purchase deleted successfully";
    }
}




