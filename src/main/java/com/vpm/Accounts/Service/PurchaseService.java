package com.vpm.Accounts.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Entity.Purchase;
import com.vpm.Accounts.Entity.PurchaseItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.PurchaseRepository;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private JournalEntryRepository journalRepo;

    // =========================================
    // 🔹 COMMON METHODS
    // =========================================
    private Account findAccountByName(String name) {
        if (name == null || name.isBlank()) return null;
        return accountRepo.findByNameIgnoreCase(name).orElse(null);
    }

    private double calculatePurchaseTotal(Purchase purchase) {
        return purchase.getItems()
                .stream()
                .mapToDouble(PurchaseItem::getTotal)
                .sum();
    }

    // =========================================
    // 🔹 CREATE JOURNAL ENTRY (ONLY SOURCE)
    // =========================================
    private JournalEntry createPurchaseJournal(Purchase purchase, double amount) {

        Account purchaseAcc = findAccountByName("Purchases");
        if (purchaseAcc == null) {
            throw new RuntimeException("Purchases account not found");
        }

        Account supplier = findAccountByName(purchase.getSupplierName());
        Account cash = findAccountByName("Cash");

        JournalEntry journal = new JournalEntry();
        journal.setEntryDate(purchase.getDate());
        journal.setDescription("Purchase Entry - " + purchase.getSupplierName());

        List<JournalEntryLine> lines = new ArrayList<>();

        // ✅ Purchases Dr
        JournalEntryLine dr = new JournalEntryLine();
        dr.setAccount(purchaseAcc);
        dr.setDebit(amount);
        dr.setCredit(0);
        dr.setJournalEntry(journal);
        lines.add(dr);

        // ✅ Cash / Creditor Cr
        JournalEntryLine cr = new JournalEntryLine();

        if (supplier != null) {
            cr.setAccount(supplier);
        } else {
            if (cash == null) {
                throw new RuntimeException("Cash account not found");
            }
            cr.setAccount(cash);
        }

        cr.setDebit(0);
        cr.setCredit(amount);
        cr.setJournalEntry(journal);
        lines.add(cr);

        journal.setLines(lines);

        return journalRepo.save(journal);
    }

    // =========================================
    // 🔹 DELETE JOURNAL (FOR UPDATE / DELETE)
    // =========================================
    private void deleteJournalByDescription(String desc) {
        List<JournalEntry> entries = journalRepo.findAll();

        for (JournalEntry j : entries) {
            if (j.getDescription() != null && j.getDescription().contains(desc)) {
                journalRepo.delete(j);
            }
        }
    }

    // =========================================
    // 🔹 SAVE PURCHASE
    // =========================================
    public Purchase savePurchase(Purchase purchase) {

        double totalAmount = calculatePurchaseTotal(purchase);

        // ✅ STOCK UPDATE
        for (PurchaseItem item : purchase.getItems()) {
            Long productId = item.getProduct().getId();
            if (productId == null) {
                throw new RuntimeException("Product ID is required");
            }

            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
            productRepo.save(product);

            item.setProduct(product);
            item.setPurchase(purchase);
        }

        // ✅ JOURNAL ENTRY
        createPurchaseJournal(purchase, totalAmount);

        return purchaseRepo.save(purchase);
    }

    @Async("accountExecutor")
    public CompletableFuture<Purchase> savePurchaseAsync(Purchase purchase) {
        return CompletableFuture.completedFuture(savePurchase(purchase));
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Purchase>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    @Async("accountExecutor")
    public CompletableFuture<Purchase> getByIdAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(getById(id));
    }

    @Async("accountExecutor")
    public CompletableFuture<Purchase> updatePurchaseAsync(@NonNull Long id, Purchase newPurchase) {
        return CompletableFuture.completedFuture(updatePurchase(id, newPurchase));
    }

    @Async("accountExecutor")
    public CompletableFuture<String> deleteAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(delete(id));
    }

    // =========================================
    // 🔹 GET ALL
    // =========================================
    public List<Purchase> getAll() {
        return purchaseRepo.findAll();
    }

    // =========================================
    // 🔹 GET BY ID
    // =========================================
    public Purchase getById(@NonNull Long id) {
        return purchaseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
    }

    // =========================================
    // 🔹 UPDATE PURCHASE
    // =========================================
    public Purchase updatePurchase(@NonNull Long id, Purchase newPurchase) {

        Purchase existing = getById(id);

        // 🔥 REVERSE OLD STOCK
        for (PurchaseItem oldItem : existing.getItems()) {
            Product product = oldItem.getProduct();
            product.setOpeningStock(product.getOpeningStock() - oldItem.getQuantity());
            productRepo.save(product);
        }

        // 🔥 DELETE OLD JOURNAL
        deleteJournalByDescription("Purchase Entry - " + existing.getSupplierName());

        double newAmount = calculatePurchaseTotal(newPurchase);

        // 🔥 APPLY NEW STOCK
        for (PurchaseItem item : newPurchase.getItems()) {
            Long productId = item.getProduct().getId();
            if (productId == null) {
                throw new RuntimeException("Product ID is required");
            }

            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
            productRepo.save(product);

            item.setProduct(product);
            item.setPurchase(existing);
        }

        // 🔥 CREATE NEW JOURNAL
        createPurchaseJournal(newPurchase, newAmount);

        // 🔥 UPDATE FIELDS
        existing.setItems(newPurchase.getItems());
        existing.setDate(newPurchase.getDate());
        existing.setSupplierName(newPurchase.getSupplierName());
        existing.setPhoneNo(newPurchase.getPhoneNo());

        return purchaseRepo.save(existing);
    }

    // =========================================
    // 🔹 DELETE PURCHASE
    // =========================================
    public String delete(@NonNull Long id) {

        Purchase purchase = getById(id);

        // 🔥 REVERSE STOCK
        for (PurchaseItem item : purchase.getItems()) {
            Product product = item.getProduct();
            product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
            productRepo.save(product);
        }

        // 🔥 DELETE JOURNAL
        deleteJournalByDescription("Purchase Entry - " + purchase.getSupplierName());

        purchaseRepo.deleteById(id);

        return "Purchase deleted successfully";
    }
}

