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
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.SaleRepository;

@Service
public class SaleService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SaleRepository saleRepo;

    @Autowired
    private AccountRepository accountRepo;

    private Account findAccountByName(String name) {
        if (name == null || name.isBlank()) return null;
        return accountRepo.findByName(name)
                .or(() -> accountRepo.findByNameIgnoreCase(name))
                .orElse(null);
    }

    private double calculateSaleTotal(Sale sale) {
        return sale.getItems().stream().mapToDouble(SaleItem::getTotal).sum();
    }

    private void reverseSaleEntries(Sale sale, double amount) {
        Account salesAcc = findAccountByName("Sales");
        if (salesAcc != null) {
            salesAcc.debit(amount);
            accountRepo.save(salesAcc);
        }

        Account customer = findAccountByName(sale.getCustomerName());
        if (customer != null) {
            customer.credit(amount);
            accountRepo.save(customer);
        } else {
            Account cash = findAccountByName("Cash");
            if (cash != null) {
                cash.credit(amount);
                accountRepo.save(cash);
            }
        }
    }

//    private void applySaleEntries(Sale sale, double amount) {
//        Account salesAcc = findAccountByName("Sales");
//        if (salesAcc == null) {
//            throw new RuntimeException("Sales account not found");
//        }
//        salesAcc.credit(amount);
//        accountRepo.save(salesAcc);
//
//        Account customer = findAccountByName(sale.getCustomerName());
//        if (customer != null) {
//            customer.debit(amount);
//            accountRepo.save(customer);
//        } else {
//            Account cash = findAccountByName("Cash");
//            if (cash == null) {
//                throw new RuntimeException("Cash account not found");
//            }
//            cash.debit(amount);
//            accountRepo.save(cash);
//        }
//    }
    
    @Autowired
    private JournalEntryRepository journalRepo;
    
    private void applySaleEntries(Sale sale, double amount) {

        Account salesAcc = findAccountByName("Sales");
        if (salesAcc == null) {
            throw new RuntimeException("Sales account not found");
        }

        Account customer = findAccountByName(sale.getCustomerName());
        Account cash = findAccountByName("Cash");

        // =========================
        // ✅ CREATE JOURNAL ENTRY
        // =========================
        JournalEntry journal = new JournalEntry();
        journal.setEntryDate(sale.getDate());
        journal.setDescription("Sales Entry");

        List<JournalEntryLine> lines = new ArrayList<>();

        // 🔹 Cash / Debtor Dr
        JournalEntryLine drLine = new JournalEntryLine();

        if (customer != null) {
            drLine.setAccount(customer);
        } else {
            if (cash == null) throw new RuntimeException("Cash account not found");
            drLine.setAccount(cash);
        }

        drLine.setDebit(amount);
        drLine.setCredit(0);
        drLine.setJournalEntry(journal);
        lines.add(drLine);

        // 🔹 Sales Cr
        JournalEntryLine crLine = new JournalEntryLine();
        crLine.setAccount(salesAcc);
        crLine.setDebit(0);
        crLine.setCredit(amount);
        crLine.setJournalEntry(journal);
        lines.add(crLine);

        journal.setLines(lines);

        // =========================
        // ✅ SAVE JOURNAL
        // =========================
        journalRepo.save(journal);
    }

//    // =========================
//    // ✅ SAVE SALE + STOCK + ACCOUNT
//    // =========================
//    public Sale saveSale(Sale sale) {
//
//        double totalAmount = 0;
//
//        // 🔥 STEP 1: STOCK UPDATE
//        for (SaleItem item : sale.getItems()) {
//
//            Product product = productRepo.findById(item.getProduct().getId())
//                    .orElseThrow(() -> new RuntimeException("Product not found"));
//
//            // ✅ REDUCE STOCK
//            product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
//            productRepo.save(product);
//
//            item.setProduct(product);
//            item.setSale(sale);
//
//            totalAmount += item.getTotal();
//        }
//
//        // 🔥 STEP 2: ACCOUNT UPDATE
//        Account account = accountRepo.findByName(sale.getCustomerName())
//                .orElse(null);
//
//        if (account != null) {
//            // ✅ DEBTOR ↑ (money receivable)
//            account.debit(totalAmount);
//            accountRepo.save(account);
//        } else {
//            // ✅ CASH SALE
//            Account cash = accountRepo.findByName("Cash")
//                    .orElseThrow(() -> new RuntimeException("Cash not found"));
//
//            cash.debit(totalAmount); // money comes in
//            accountRepo.save(cash);
//        }
//
//        // 🔥 STEP 3: SAVE SALE
//        return saleRepo.save(sale);
//    }

    public Sale saveSale(Sale sale) {

        double totalAmount = calculateSaleTotal(sale);

        // =========================
        // ✅ STOCK REDUCE
        // =========================
        for (SaleItem item : sale.getItems()) {
            Long productId = item.getProduct().getId();
            if (productId == null) {
                throw new RuntimeException("Product ID is required");
            }

            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
            productRepo.save(product);

            item.setProduct(product);
            item.setSale(sale);
        }

        // =========================
        // ✅ ACCOUNT UPDATE
        // =========================
        applySaleEntries(sale, totalAmount);

        return saleRepo.save(sale);
    }

    @Async("accountExecutor")
    public CompletableFuture<Sale> saveSaleAsync(Sale sale) {
        return CompletableFuture.completedFuture(saveSale(sale));
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Sale>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    @Async("accountExecutor")
    public CompletableFuture<Sale> getSaleByIdAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(getSaleById(id));
    }

    @Async("accountExecutor")
    public CompletableFuture<Sale> updateSaleAsync(@NonNull Long id, Sale newSale) {
        return CompletableFuture.completedFuture(updateSale(id, newSale));
    }

    @Async("accountExecutor")
    public CompletableFuture<String> deleteAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(delete(id));
    }
    
    // =========================
    // ✅ GET ALL
    // =========================
    public List<Sale> getAll() {
        return saleRepo.findAll();
    }

    // =========================
    // ✅ GET BY ID
    // =========================
    public Sale getSaleById(@NonNull Long id) {
        return saleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    // =========================
    // ✅ UPDATE SALE (WITH STOCK + ACCOUNT REVERSE)
    // =========================
    public Sale updateSale(@NonNull Long id, Sale newSale) {

        Sale existing = getSaleById(id);

        double oldAmount = calculateSaleTotal(existing);

        // 🔥 REVERSE OLD STOCK
        for (SaleItem oldItem : existing.getItems()) {
            Product product = oldItem.getProduct();
            product.setOpeningStock(product.getOpeningStock() + oldItem.getQuantity());
            productRepo.save(product);
        }

        reverseSaleEntries(existing, oldAmount);

        double newAmount = calculateSaleTotal(newSale);

        // 🔥 APPLY NEW STOCK
        for (SaleItem item : newSale.getItems()) {
            Long productId = item.getProduct().getId();
            if (productId == null) {
                throw new RuntimeException("Product ID is required");
            }
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
            productRepo.save(product);

            item.setProduct(product);
            item.setSale(existing);
        }

        applySaleEntries(newSale, newAmount);

        // 🔥 UPDATE FIELDS
        existing.setCustomerName(newSale.getCustomerName());
        existing.setDate(newSale.getDate());
        existing.setPhoneNo(newSale.getPhoneNo());
        existing.setItems(newSale.getItems());

        return saleRepo.save(existing);
    }

    // =========================
    // ✅ DELETE SALE (WITH STOCK FIX + ACCOUNT REVERSE)
    // =========================
    public String delete(@NonNull Long id) {

        Sale sale = getSaleById(id);

        double amount = calculateSaleTotal(sale);

        // 🔥 RESTORE STOCK
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
            productRepo.save(product);
        }

        reverseSaleEntries(sale, amount);

        saleRepo.deleteById(id);

        return "Sale deleted successfully";
    }
}



