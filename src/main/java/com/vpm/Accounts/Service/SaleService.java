package com.vpm.Accounts.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vpm.Accounts.Entity.*;
import com.vpm.Accounts.Repository.*;

@Service
public class SaleService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SaleRepository saleRepo;

    @Autowired
    private AccountRepository accountRepo;

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

        double totalAmount = 0;

        // =========================
        // ✅ STOCK REDUCE
        // =========================
        for (SaleItem item : sale.getItems()) {

            Product product = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
            productRepo.save(product);

            item.setProduct(product);
            item.setSale(sale);

            totalAmount += item.getTotal();
        }

        // =========================
        // ✅ SALES ACCOUNT (Cr)
        // =========================
        Account salesAcc = accountRepo.findByName("Sales")
                .orElseThrow(() -> new RuntimeException("Sales account not found"));

        salesAcc.credit(totalAmount);
        accountRepo.save(salesAcc);

        // =========================
        // ✅ CASH / DEBTOR (Dr)
        // =========================
        Account customer = accountRepo.findByName(sale.getCustomerName())
                .orElse(null);

        if (customer != null) {
            customer.debit(totalAmount);
            accountRepo.save(customer);
        } else {
            Account cash = accountRepo.findByName("Cash")
                    .orElseThrow(() -> new RuntimeException("Cash not found"));

            cash.debit(totalAmount);
            accountRepo.save(cash);
        }

        return saleRepo.save(sale);
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
    public Sale getSaleById(Long id) {
        return saleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    // =========================
    // ✅ UPDATE SALE (WITH STOCK FIX)
    // =========================
    public Sale updateSale(Long id, Sale newSale) {

        Sale existing = getSaleById(id);

        // 🔥 REVERSE OLD STOCK
        for (SaleItem oldItem : existing.getItems()) {
            Product product = oldItem.getProduct();
            product.setOpeningStock(product.getOpeningStock() + oldItem.getQuantity());
            productRepo.save(product);
        }

        double totalAmount = 0;

        // 🔥 APPLY NEW STOCK
        for (SaleItem item : newSale.getItems()) {

            Product product = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
            productRepo.save(product);

            item.setProduct(product);
            item.setSale(existing);

            totalAmount += item.getTotal();
        }

        // 🔥 ACCOUNT UPDATE
        Account account = accountRepo.findByName(newSale.getCustomerName())
                .orElse(null);

        if (account != null) {
            account.debit(totalAmount);
            accountRepo.save(account);
        } else {
            Account cash = accountRepo.findByName("Cash")
                    .orElseThrow(() -> new RuntimeException("Cash not found"));

            cash.debit(totalAmount);
            accountRepo.save(cash);
        }

        // 🔥 UPDATE FIELDS
        existing.setCustomerName(newSale.getCustomerName());
        existing.setDate(newSale.getDate());
        existing.setPhoneNo(newSale.getPhoneNo());
        existing.setItems(newSale.getItems());

        return saleRepo.save(existing);
    }

    // =========================
    // ✅ DELETE SALE (WITH STOCK FIX)
    // =========================
    public String delete(Long id) {

        Sale sale = getSaleById(id);

        // 🔥 RESTORE STOCK
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
            productRepo.save(product);
        }

        saleRepo.deleteById(id);

        return "Sale deleted successfully";
    }
}



