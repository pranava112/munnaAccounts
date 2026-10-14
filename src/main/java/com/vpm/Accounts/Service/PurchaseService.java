// // package com.vpm.Accounts.Service;

// // import java.util.ArrayList;
// // import java.util.List;
// // import java.util.concurrent.CompletableFuture;

// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.lang.NonNull;
// // import org.springframework.scheduling.annotation.Async;
// // import org.springframework.stereotype.Service;

// // import com.vpm.Accounts.Entity.Account;
// // import com.vpm.Accounts.Entity.JournalEntry;
// // import com.vpm.Accounts.Entity.JournalEntryLine;
// // import com.vpm.Accounts.Entity.Product;
// // import com.vpm.Accounts.Entity.Purchase;
// // import com.vpm.Accounts.Entity.PurchaseItem;
// // import com.vpm.Accounts.Repository.AccountRepository;
// // import com.vpm.Accounts.Repository.JournalEntryRepository;
// // import com.vpm.Accounts.Repository.ProductRepository;
// // import com.vpm.Accounts.Repository.PurchaseRepository;

// // @Service
// // public class PurchaseService {

// //     @Autowired
// //     private PurchaseRepository purchaseRepo;

// //     @Autowired
// //     private ProductRepository productRepo;

// //     @Autowired
// //     private AccountRepository accountRepo;

// //     @Autowired
// //     private JournalEntryRepository journalRepo;

// //     // =========================================
// //     // 🔹 COMMON METHODS
// //     // =========================================
// //     private Account findAccountByName(String name) {
// //         if (name == null || name.isBlank()) return null;
// //         return accountRepo.findFirstByNameIgnoreCase(name).orElse(null);
// //     }

// //     private double calculatePurchaseTotal(Purchase purchase) {
// //         return purchase.getItems()
// //                 .stream()
// //                 .mapToDouble(PurchaseItem::getTotal)
// //                 .sum();
// //     }

// //     // =========================================
// //     // 🔹 CREATE JOURNAL ENTRY (ONLY SOURCE)
// //     // =========================================
// //     // private JournalEntry createPurchaseJournal(Purchase purchase, double amount) {

// //     //     Account purchaseAcc = findAccountByName("Purchases");
// //     //     if (purchaseAcc == null) {
// //     //         throw new RuntimeException("Purchases account not found");
// //     //     }

// //     //     Account supplier = findAccountByName(purchase.getSupplierName());
// //     //     Account cash = findAccountByName("Cash");

// //     //     JournalEntry journal = new JournalEntry();
// //     //     journal.setEntryDate(purchase.getDate());
// //     //     journal.setDescription("Purchase Entry - " + purchase.getSupplierName());

// //     //     List<JournalEntryLine> lines = new ArrayList<>();

// //     //     // ✅ Purchases Dr
// //     //     JournalEntryLine dr = new JournalEntryLine();
// //     //     dr.setAccount(purchaseAcc);
// //     //     dr.setDebit(amount);
// //     //     dr.setCredit(0);
// //     //     dr.setJournalEntry(journal);
// //     //     lines.add(dr);

// //     //     // ✅ Cash / Creditor Cr
// //     //     JournalEntryLine cr = new JournalEntryLine();

// //     //     if (supplier != null) {
// //     //         cr.setAccount(supplier);
// //     //     } else {
// //     //         if (cash == null) {
// //     //             throw new RuntimeException("Cash account not found");
// //     //         }
// //     //         cr.setAccount(cash);
// //     //     }

// //     //     cr.setDebit(0);
// //     //     cr.setCredit(amount);
// //     //     cr.setJournalEntry(journal);
// //     //     lines.add(cr);

// //     //     journal.setLines(lines);

// //     //     return journalRepo.save(journal);
// //     // }

// //     private JournalEntry createPurchaseJournal(
// //         Purchase purchase,
// //         double amount
// // ) {

// //     Account purchaseAcc = findAccountByName("Purchases");

// //     if (purchaseAcc == null) {
// //         throw new RuntimeException("Purchases account not found");
// //     }

// //     Account supplier =
// //             findAccountByName(
// //                     purchase.getSupplierName()
// //             );

// //     Account cash =
// //             findAccountByName("Cash");

// //     JournalEntry journal = new JournalEntry();

// //     // ✅ Copy voucher number from Purchase
// //     journal.setVoucherNumber(
// //             purchase.getVoucherNumber()
// //     );

// //     journal.setEntryDate(
// //             purchase.getDate()
// //     );

// //     journal.setDescription(
// //             "Purchase Entry - "
// //                     + purchase.getSupplierName()
// //     );

// //     List<JournalEntryLine> lines =
// //             new ArrayList<>();

// //     // Purchases A/c Dr
// //     JournalEntryLine dr = new JournalEntryLine();
// //     dr.setAccount(purchaseAcc);
// //     dr.setDebit(amount);
// //     dr.setCredit(0);
// //     dr.setJournalEntry(journal);
// //     lines.add(dr);

// //     // Cash / Supplier Cr
// //     JournalEntryLine cr = new JournalEntryLine();

// //     if (supplier != null) {
// //         cr.setAccount(supplier);
// //     } else {
// //         if (cash == null) {
// //             throw new RuntimeException(
// //                     "Cash account not found"
// //             );
// //         }

// //         cr.setAccount(cash);
// //     }

// //     cr.setDebit(0);
// //     cr.setCredit(amount);
// //     cr.setJournalEntry(journal);
// //     lines.add(cr);

// //     journal.setLines(lines);

// //     return journalRepo.save(journal);
// // }

// //     // =========================================
// //     // 🔹 DELETE JOURNAL (FOR UPDATE / DELETE)
// //     // =========================================
// //     private void deleteJournalByDescription(String desc) {
// //         List<JournalEntry> entries = journalRepo.findAll();

// //         for (JournalEntry j : entries) {
// //             if (j.getDescription() != null && j.getDescription().contains(desc)) {
// //                 journalRepo.delete(j);
// //             }
// //         }
// //     }

// //     // =========================================
// //     // 🔹 SAVE PURCHASE
// //     // =========================================
// //     public Purchase savePurchase(Purchase purchase) {

// //         double totalAmount = calculatePurchaseTotal(purchase);

// //         // ✅ STOCK UPDATE
// //         for (PurchaseItem item : purchase.getItems()) {
// //             Long productId = item.getProduct().getId();
// //             if (productId == null) {
// //                 throw new RuntimeException("Product ID is required");
// //             }

// //             Product product = productRepo.findById(productId)
// //                     .orElseThrow(() -> new RuntimeException("Product not found"));

// //             product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
// //             productRepo.save(product);

// //             item.setProduct(product);
// //             item.setPurchase(purchase);
// //         }

// //         // ✅ JOURNAL ENTRY
// //         createPurchaseJournal(purchase, totalAmount);

// //         return purchaseRepo.save(purchase);
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<Purchase> savePurchaseAsync(Purchase purchase) {
// //         return CompletableFuture.completedFuture(savePurchase(purchase));
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<List<Purchase>> getAllAsync() {
// //         return CompletableFuture.completedFuture(getAll());
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<Purchase> getByIdAsync(@NonNull Long id) {
// //         return CompletableFuture.completedFuture(getById(id));
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<Purchase> updatePurchaseAsync(@NonNull Long id, Purchase newPurchase) {
// //         return CompletableFuture.completedFuture(updatePurchase(id, newPurchase));
// //     }

// //     @Async("accountExecutor")
// //     public CompletableFuture<String> deleteAsync(@NonNull Long id) {
// //         return CompletableFuture.completedFuture(delete(id));
// //     }

// //     // =========================================
// //     // 🔹 GET ALL
// //     // =========================================
// //     public List<Purchase> getAll() {
// //         return purchaseRepo.findAll();
// //     }

// //     // =========================================
// //     // 🔹 GET BY ID
// //     // =========================================
// //     public Purchase getById(@NonNull Long id) {
// //         return purchaseRepo.findById(id)
// //                 .orElseThrow(() -> new RuntimeException("Purchase not found"));
// //     }

// //     // =========================================
// //     // 🔹 UPDATE PURCHASE
// //     // =========================================
// //     public Purchase updatePurchase(@NonNull Long id, Purchase newPurchase) {

// //         Purchase existing = getById(id);

// //         // 🔥 REVERSE OLD STOCK
// //         for (PurchaseItem oldItem : existing.getItems()) {
// //             Product product = oldItem.getProduct();
// //             product.setOpeningStock(product.getOpeningStock() - oldItem.getQuantity());
// //             productRepo.save(product);
// //         }

// //         // 🔥 DELETE OLD JOURNAL
// //         deleteJournalByDescription("Purchase Entry - " + existing.getSupplierName());

// //         double newAmount = calculatePurchaseTotal(newPurchase);

// //         // 🔥 APPLY NEW STOCK
// //         for (PurchaseItem item : newPurchase.getItems()) {
// //             Long productId = item.getProduct().getId();
// //             if (productId == null) {
// //                 throw new RuntimeException("Product ID is required");
// //             }

// //             Product product = productRepo.findById(productId)
// //                     .orElseThrow(() -> new RuntimeException("Product not found"));

// //             product.setOpeningStock(product.getOpeningStock() + item.getQuantity());
// //             productRepo.save(product);

// //             item.setProduct(product);
// //             item.setPurchase(existing);
// //         }

// //         // 🔥 CREATE NEW JOURNAL
// //         createPurchaseJournal(newPurchase, newAmount);

// //         // 🔥 UPDATE FIELDS
// //         existing.setItems(newPurchase.getItems());
// //         existing.setDate(newPurchase.getDate());
// //         existing.setSupplierName(newPurchase.getSupplierName());
// //         existing.setPhoneNo(newPurchase.getPhoneNo());
// //         existing.setVoucherNumber(newPurchase.getVoucherNumber());

// //         return purchaseRepo.save(existing);
// //     }

// //     // =========================================
// //     // 🔹 DELETE PURCHASE
// //     // =========================================
// //     public String delete(@NonNull Long id) {

// //         Purchase purchase = getById(id);

// //         // 🔥 REVERSE STOCK
// //         for (PurchaseItem item : purchase.getItems()) {
// //             Product product = item.getProduct();
// //             product.setOpeningStock(product.getOpeningStock() - item.getQuantity());
// //             productRepo.save(product);
// //         }

// //         // 🔥 DELETE JOURNAL
// //         deleteJournalByDescription("Purchase Entry - " + purchase.getSupplierName());

// //         purchaseRepo.deleteById(id);

// //         return "Purchase deleted successfully";
// //     }
// // }


// package com.vpm.Accounts.Service;

// import java.math.BigDecimal;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.concurrent.CompletableFuture;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.lang.NonNull;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.transaction.support.TransactionTemplate;

// import com.vpm.Accounts.Entity.Account;
// import com.vpm.Accounts.Entity.JournalEntry;
// import com.vpm.Accounts.Entity.JournalEntryLine;
// import com.vpm.Accounts.Entity.FinancialYear;
// import com.vpm.Accounts.Entity.AccountingPeriod;
// import com.vpm.Accounts.Entity.Product;
// import com.vpm.Accounts.Entity.Purchase;
// import com.vpm.Accounts.Entity.PurchaseItem;
// import com.vpm.Accounts.Repository.AccountRepository;
// import com.vpm.Accounts.Repository.JournalEntryRepository;
// import com.vpm.Accounts.Repository.ProductRepository;
// import com.vpm.Accounts.Repository.PurchaseRepository;
// import com.vpm.Accounts.Repository.FinancialYearRepository;
// import com.vpm.Accounts.Repository.AccountingPeriodRepository;
// import com.vpm.Accounts.security.TenantContext;
// import com.vpm.Accounts.Entity.AccountType;
// import com.vpm.Accounts.Service.GST.TaxApiService;

// @Service
// public class PurchaseService {

//     @Autowired
//     private PurchaseRepository purchaseRepo;

//     @Autowired
//     private ProductRepository productRepo;

//     @Autowired
//     private AccountRepository accountRepo;

//     @Autowired
//     private JournalEntryRepository journalRepo;

//     @Autowired
//     private TransactionTemplate transactionTemplate;

//     @Autowired
//     private FinancialYearRepository financialYearRepo;

//     @Autowired
//     private AccountingPeriodRepository accountingPeriodRepo;

//     @Autowired
//     private ProductService productService;

//     @Autowired
//     private TaxApiService taxApiService;


//     // =========================================================
//     // COMMON METHODS
//     // =========================================================

//     private Account findAccountByName(String name) {

//         if (name == null || name.isBlank()) {
//             return null;
//         }

//         return accountRepo.findFirstByNameIgnoreCase(name)
//                 .orElse(null);
//     }

//     private void validateOpenAccountingPeriod(java.time.LocalDate date) {
//         if (date == null) {
//             throw new IllegalArgumentException("Purchase date is required");
//         }
//         FinancialYear financialYear = financialYearRepo
//                 .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "No financial year exists for purchase date: " + date));
//         if (financialYear.isClosed()) {
//             throw new IllegalStateException(
//                     "Financial year " + financialYear.getName() + " is closed");
//         }
//         AccountingPeriod accountingPeriod = accountingPeriodRepo
//                 .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//                         financialYear.getId(), date, date)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "No accounting period exists for purchase date: " + date));
//         if (accountingPeriod.isClosed()) {
//             throw new IllegalStateException(
//                     "Accounting period " + accountingPeriod.getName() + " is closed");
//         }
//     }

//     // =========================================================
//     // CALCULATE PURCHASE TOTAL
//     // =========================================================

//     private BigDecimal calculatePurchaseTotal(Purchase purchase) {

//         if (purchase == null
//                 || purchase.getItems() == null
//                 || purchase.getItems().isEmpty()) {

//             return BigDecimal.ZERO;
//         }

//         return purchase.getItems()
//                 .stream()
//                 .filter(item -> item != null)
//                 .map(PurchaseItem::getTotal)
//                 .filter(total -> total != null)
//                 .reduce(BigDecimal.ZERO, BigDecimal::add);
//     }


//     // =========================================================
//     // CREATE JOURNAL ENTRY
//     // =========================================================

//     private JournalEntry createPurchaseJournal(
//             Purchase purchase,
//             BigDecimal amount) {

//         if (purchase == null) {
//             throw new RuntimeException("Purchase cannot be null");
//         }

//         if (amount == null) {
//             amount = BigDecimal.ZERO;
//         }

//         if (amount.compareTo(BigDecimal.ZERO) < 0) {
//             throw new RuntimeException(
//                     "Purchase amount cannot be negative");
//         }


//         // =====================================================
//         // FIND PURCHASE ACCOUNT
//         // =====================================================

//         Account purchaseAcc =
//                 findAccountByName("Purchases");

//         if (purchaseAcc == null) {
//             throw new RuntimeException(
//                     "Purchases account not found");
//         }


//         // =====================================================
//         // FIND SUPPLIER
//         // =====================================================

//         Account supplier =
//                 findAccountByName(
//                         purchase.getSupplierName());


//         // =====================================================
//         // FIND CASH ACCOUNT
//         // =====================================================

//         Account cash =
//                 findAccountByName("Cash");


//         // =====================================================
//         // CREATE JOURNAL
//         // =====================================================

//         JournalEntry journal =
//                 new JournalEntry();


//         // Voucher number
//         journal.setVoucherNumber(
//                 purchase.getVoucherNumber());


//         // Entry date
//         journal.setEntryDate(purchase.getDate());
//         FinancialYear financialYear = financialYearRepo
//                 .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
//                         purchase.getDate(), purchase.getDate())
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "No financial year exists for purchase date: " + purchase.getDate()));
//         journal.setFinancialYear(financialYear);
//         accountingPeriodRepo
//                 .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//                         financialYear.getId(), purchase.getDate(), purchase.getDate())
//                 .ifPresent(journal::setAccountingPeriod);


//         // Description
//         journal.setDescription(
//                 "Purchase Entry - "
//                         + purchase.getSupplierName());


//         // =====================================================
//         // JOURNAL LINES
//         // =====================================================

//         List<JournalEntryLine> lines =
//                 new ArrayList<>();


//         // =====================================================
//         // PURCHASES A/C - DEBIT
//         // =====================================================

//         JournalEntryLine dr =
//                 new JournalEntryLine();

//         dr.setAccount(purchaseAcc);

//         boolean gstEnabledForPurchase = purchase.getGstTaxableAmount() != null
//                 && (purchase.getGstTaxableAmount().signum() > 0
//                         || purchase.getGstTotal() != null && purchase.getGstTotal().signum() > 0);
//         dr.setDebit(gstEnabledForPurchase ? purchase.getGstTaxableAmount() : amount);

//         dr.setCredit(BigDecimal.ZERO);

//         dr.setJournalEntry(journal);

//         lines.add(dr);

//         if (gstEnabledForPurchase) {
//             addInputTaxDebit(lines, journal, "Input CGST", purchase.getGstCgst());
//             addInputTaxDebit(lines, journal, "Input SGST", purchase.getGstSgst());
//             addInputTaxDebit(lines, journal, "Input IGST", purchase.getGstIgst());
//             addInputTaxDebit(lines, journal, "Input GST Cess", purchase.getGstCess());
//             amount = purchase.getGrandTotal();
//         }


//         // =====================================================
//         // SUPPLIER / CASH - CREDIT
//         // =====================================================

//         JournalEntryLine cr =
//                 new JournalEntryLine();


//         if (supplier != null) {

//             // Credit Supplier
//             cr.setAccount(supplier);

//         } else {

//             // If supplier account is not found,
//             // use Cash account.

//             if (cash == null) {
//                 throw new RuntimeException(
//                         "Cash account not found");
//             }

//             cr.setAccount(cash);
//         }


//         cr.setDebit(BigDecimal.ZERO);

//         cr.setCredit(amount);

//         cr.setJournalEntry(journal);

//         lines.add(cr);


//         // =====================================================
//         // SET LINES
//         // =====================================================

//         journal.setLines(lines);


//         // =====================================================
//         // SAVE JOURNAL
//         // =====================================================

//         return journalRepo.save(journal);
//     }

//     private void addInputTaxDebit(List<JournalEntryLine> lines, JournalEntry journal,
//             String accountName, BigDecimal value) {
//         BigDecimal amount = value == null ? BigDecimal.ZERO : value;
//         if (amount.signum() <= 0) return;
//         Account inputTaxAccount = findAccountByName(accountName);
//         if (inputTaxAccount == null) {
//             inputTaxAccount = new Account();
//             inputTaxAccount.setName(accountName);
//             inputTaxAccount.setType(AccountType.ASSET);
//             inputTaxAccount.setAccountGroup("CURRENT_ASSETS");
//             inputTaxAccount.setBalance(BigDecimal.ZERO);
//             inputTaxAccount = accountRepo.save(inputTaxAccount);
//         }
//         JournalEntryLine line = new JournalEntryLine();
//         line.setAccount(inputTaxAccount);
//         line.setDebit(amount);
//         line.setCredit(BigDecimal.ZERO);
//         line.setJournalEntry(journal);
//         lines.add(line);
//     }


//     // =========================================================
//     // DELETE JOURNAL
//     // =========================================================

//     private void deleteJournalByDescription(String desc) {

//         if (desc == null || desc.isBlank()) {
//             return;
//         }

//         List<JournalEntry> entries =
//                 journalRepo.findAll();


//         for (JournalEntry journal : entries) {

//             if (journal.getDescription() != null
//                     && journal.getDescription()
//                             .contains(desc)) {

//                 journalRepo.delete(journal);
//             }
//         }
//     }

//         private void deleteJournalByVoucherNumber(String voucherNumber) {
//                 if (voucherNumber == null || voucherNumber.isBlank()) {
//                         return;
//                 }

//                 journalRepo.deleteByVoucherNumber(voucherNumber);
//                 journalRepo.flush();
//         }


//     // =========================================================
//     // SAVE PURCHASE
//     // =========================================================

//     @Transactional
//     public Purchase savePurchase(Purchase purchase) {

//         if (purchase == null) {
//             throw new RuntimeException(
//                     "Purchase cannot be null");
//         }

//                 if (purchase.getSupplierName() == null || purchase.getSupplierName().isBlank()) {
//                         throw new IllegalArgumentException("Supplier is required");
//                 }

//                 // if (purchase.getDate() == null || purchase.getDate().isBlank()) {
//                 //         throw new IllegalArgumentException("Purchase date is required");
//                 // }

//                 if (purchase.getDate() == null ) {
//                         throw new IllegalArgumentException("Purchase date is required");
//                 }

//                 Long tenantId = TenantContext.require();
//                 int year = purchase.getDate().getYear();
//                 purchase.setVoucherNumber(null);
//                 validateOpenAccountingPeriod(purchase.getDate());


//         // =====================================================
//         // VALIDATE ITEMS
//         // =====================================================

//         if (purchase.getItems() == null
//                 || purchase.getItems().isEmpty()) {

//             throw new RuntimeException(
//                     "Purchase must contain at least one item");
//         }


//         // =====================================================
//         // CALCULATE TOTAL
//         // =====================================================

//         BigDecimal totalAmount =
//                 calculatePurchaseTotal(purchase);


//         // =====================================================
//         // STOCK UPDATE
//         // =====================================================

//         for (PurchaseItem item :
//                 purchase.getItems()) {

//             if (item == null) {
//                 continue;
//             }


//             if (item.getProduct() == null) {
//                 throw new RuntimeException(
//                         "Product is required for purchase item");
//             }


//             Long productId =
//                     item.getProduct().getId();


//             if (productId == null) {
//                 throw new RuntimeException(
//                         "Product ID is required");
//             }


//             if (item.getQuantity() < 0) {
//                 throw new RuntimeException(
//                         "Quantity cannot be negative");
//             }


//             // =================================================
//             // FIND PRODUCT
//             // =================================================

//             Product product =
//                     productRepo.findById(productId)
//                             .orElseThrow(() ->
//                                     new RuntimeException(
//                                             "Product not found with ID: "
//                                                     + productId));


//             // =================================================
//             // GET CURRENT STOCK
//             // =================================================

//             BigDecimal currentStock =
//                     product.getCurrentStockQuantity();

//             if (currentStock == null) {
//                 currentStock =
//                         BigDecimal.ZERO;
//             }


//             // =================================================
//             // ADD PURCHASE QUANTITY
//             // =================================================

//             BigDecimal quantity =
//                     BigDecimal.valueOf(
//                             item.getQuantity());


//                     product.setCurrentStockQuantity(
//                     currentStock.add(quantity)
//             );


//             // =================================================
//             // SAVE PRODUCT
//             // =================================================

//             productRepo.save(product);


//             // =================================================
//             // SET RELATIONSHIPS
//             // =================================================

//             item.setProduct(product);

//             item.setPurchase(purchase);
//         }


//         // =====================================================
//         // SAVE PURCHASE
//         // =====================================================

//         Purchase savedPurchase = purchaseRepo.saveAndFlush(purchase);
//         savedPurchase.setVoucherNumber(String.format(
//                 "PV-%d-%d-%06d", tenantId, year, savedPurchase.getId()));
//         savedPurchase = purchaseRepo.saveAndFlush(savedPurchase);
//         taxApiService.syncPurchaseTax(savedPurchase);
//         savedPurchase = purchaseRepo.saveAndFlush(savedPurchase);
//         createPurchaseJournal(savedPurchase, totalAmount);
//         return savedPurchase;
//     }


//     // =========================================================
//     // ASYNC SAVE
//     // =========================================================

//     @Async("accountExecutor")
//     public CompletableFuture<Purchase> savePurchaseAsync(
//             Purchase purchase) {

//         return CompletableFuture.completedFuture(
//                 transactionTemplate.execute(status -> savePurchase(purchase)));
//     }


//     // =========================================================
//     // ASYNC GET ALL
//     // =========================================================

//     @Async("accountExecutor")
//     public CompletableFuture<List<Purchase>> getAllAsync() {

//         return CompletableFuture.completedFuture(
//                 getAll());
//     }


//     // =========================================================
//     // ASYNC GET BY ID
//     // =========================================================

//     @Async("accountExecutor")
//     public CompletableFuture<Purchase> getByIdAsync(
//             @NonNull Long id) {

//         return CompletableFuture.completedFuture(
//                 getById(id));
//     }


//     // =========================================================
//     // ASYNC UPDATE
//     // =========================================================

//     @Async("accountExecutor")
//     public CompletableFuture<Purchase> updatePurchaseAsync(
//             @NonNull Long id,
//             Purchase newPurchase) {

//         return CompletableFuture.completedFuture(
//                 updatePurchase(id, newPurchase));
//     }


//     // =========================================================
//     // ASYNC DELETE
//     // =========================================================

//     @Async("accountExecutor")
//     @Transactional
//     public CompletableFuture<String> deleteAsync(
//             @NonNull Long id) {

//         return CompletableFuture.completedFuture(
//                 delete(id));
//     }


//     // =========================================================
//     // GET ALL
//     // =========================================================

//     public List<Purchase> getAll() {

//         return purchaseRepo.findAll();
//     }


//     // =========================================================
//     // GET BY ID
//     // =========================================================

//     public Purchase getById(
//             @NonNull Long id) {

//         return purchaseRepo.findById(id)
//                 .orElseThrow(() ->
//                         new RuntimeException(
//                                 "Purchase not found with ID: "
//                                         + id));
//     }


//     // =========================================================
//     // UPDATE PURCHASE
//     // =========================================================

//     @Transactional
//     public Purchase updatePurchase(
//             @NonNull Long id,
//             Purchase newPurchase) {

//         if (newPurchase == null) {
//             throw new RuntimeException(
//                     "Updated purchase cannot be null");
//         }


//         // =====================================================
//         // GET EXISTING PURCHASE
//         // =====================================================

//         Purchase existing =
//                 getById(id);


//         // =====================================================
//         // VALIDATE NEW ITEMS
//         // =====================================================

//         if (newPurchase.getItems() == null
//                 || newPurchase.getItems().isEmpty()) {

//             throw new RuntimeException(
//                     "Purchase must contain at least one item");
//         }
//         validateOpenAccountingPeriod(newPurchase.getDate());


//         // =====================================================
//         // REVERSE OLD STOCK
//         // =====================================================

//         if (existing.getItems() != null) {

//             for (PurchaseItem oldItem :
//                     existing.getItems()) {

//                 if (oldItem == null) {
//                     continue;
//                 }


//                 Product product =
//                         oldItem.getProduct();


//                 if (product == null) {
//                     continue;
//                 }


//                 // =============================================
//                 // CURRENT STOCK
//                 // =============================================

//                 BigDecimal currentStock =
//                         product.getCurrentStockQuantity();

//                 if (currentStock == null) {
//                     currentStock =
//                             BigDecimal.ZERO;
//                 }


//                 // =============================================
//                 // OLD QUANTITY
//                 // =============================================

//                 BigDecimal oldQuantity =
//                         BigDecimal.valueOf(
//                                 oldItem.getQuantity());


//                 // =============================================
//                 // REVERSE OLD PURCHASE
//                 // =============================================

//                 BigDecimal newStock =
//                         currentStock.subtract(
//                                 oldQuantity);


//                 // =============================================
//                 // PREVENT NEGATIVE STOCK
//                 // =============================================

//                 if (newStock.compareTo(
//                         BigDecimal.ZERO) < 0) {

//                     throw new RuntimeException(
//                             "Stock cannot become negative "
//                                     + "while updating purchase. "
//                                     + "Product: "
//                                     + product.getName());
//                 }


//                 product.setCurrentStockQuantity(
//                         newStock);


//                 productRepo.save(product);
//             }
//         }


//         // =====================================================
//         // DELETE OLD JOURNAL
//         // =====================================================

//         deleteJournalByVoucherNumber(existing.getVoucherNumber());


//         // =====================================================
//         // CALCULATE NEW TOTAL
//         // =====================================================

//         BigDecimal newAmount =
//                 calculatePurchaseTotal(
//                         newPurchase);
//         newPurchase.setVoucherNumber(String.format(
//                 "PV-%d-%d-%06d",
//                 TenantContext.require(),
//                 newPurchase.getDate().getYear(),
//                 existing.getId()));


//         // =====================================================
//         // APPLY NEW STOCK
//         // =====================================================

//         for (PurchaseItem item :
//                 newPurchase.getItems()) {

//             if (item == null) {
//                 continue;
//             }


//             if (item.getProduct() == null) {
//                 throw new RuntimeException(
//                         "Product is required for purchase item");
//             }


//             Long productId =
//                     item.getProduct().getId();


//             if (productId == null) {
//                 throw new RuntimeException(
//                         "Product ID is required");
//             }


//             if (item.getQuantity() < 0) {
//                 throw new RuntimeException(
//                         "Quantity cannot be negative");
//             }


//             // =================================================
//             // FIND PRODUCT
//             // =================================================

//             Product product =
//                     productRepo.findById(productId)
//                             .orElseThrow(() ->
//                                     new RuntimeException(
//                                             "Product not found with ID: "
//                                                     + productId));


//             // =================================================
//             // CURRENT STOCK
//             // =================================================

//             BigDecimal currentStock =
//                     product.getCurrentStockQuantity();

//             if (currentStock == null) {
//                 currentStock =
//                         BigDecimal.ZERO;
//             }


//             // =================================================
//             // NEW QUANTITY
//             // =================================================

//             BigDecimal quantity =
//                     BigDecimal.valueOf(
//                             item.getQuantity());


//             // =================================================
//             // ADD NEW PURCHASE QUANTITY
//             // =================================================

//             product.setCurrentStockQuantity(
//                     currentStock.add(quantity)
//             );


//             // =================================================
//             // SAVE PRODUCT
//             // =================================================

//             productRepo.save(product);


//             // =================================================
//             // SET RELATIONSHIPS
//             // =================================================

//             item.setProduct(product);

//             item.setPurchase(existing);
//         }


//         // =====================================================
//         // CREATE NEW JOURNAL
//         // =====================================================

//         // =====================================================
//         // UPDATE PURCHASE FIELDS
//         // =====================================================

//         existing.setItems(
//                 newPurchase.getItems());


//         existing.setDate(
//                 newPurchase.getDate());


//         existing.setSupplierName(
//                 newPurchase.getSupplierName());


//         existing.setPhoneNo(
//                 newPurchase.getPhoneNo());


//         existing.setVoucherNumber(
//                 newPurchase.getVoucherNumber());


//         // =====================================================
//         // SAVE UPDATED PURCHASE
//         // =====================================================

//         Purchase updatedPurchase = purchaseRepo.save(existing);
//         taxApiService.syncPurchaseTax(updatedPurchase);
//         updatedPurchase = purchaseRepo.save(updatedPurchase);
//         createPurchaseJournal(
//                 updatedPurchase,
//                 updatedPurchase.getGrandTotal() == null
//                         ? newAmount : updatedPurchase.getGrandTotal());
//         return updatedPurchase;
//     }


//     // =========================================================
//     // DELETE PURCHASE
//     // =========================================================


    
// @Transactional
// public String delete(@NonNull Long id) {
//     Purchase purchase = purchaseRepo.findById(id)
//             .orElseThrow(() -> new IllegalArgumentException(
//                     "Purchase not found with ID: " + id));

//     // Preserve product IDs before deleting purchase items.
//     List<Long> affectedProductIds = new ArrayList<>();

//     if (purchase.getItems() != null) {
//         for (PurchaseItem item : purchase.getItems()) {
//             if (item == null || item.getProduct() == null) {
//                 continue;
//             }

//             Long productId = item.getProduct().getId();

//             if (productId != null
//                     && !affectedProductIds.contains(productId)) {
//                 affectedProductIds.add(productId);
//             }
//         }
//     }

//     // Delete the linked journal entry using the purchase voucher number.
//     String voucherNumber = purchase.getVoucherNumber();

//     if (voucherNumber != null && !voucherNumber.isBlank()) {
//         journalRepo.deleteByVoucherNumber(voucherNumber);
//         journalRepo.flush();
//     }

//     // Cancel the linked tax invoice, if applicable.
//     taxApiService.cancelSourceInvoice("PURCHASE", purchase.getId());

//     // Delete the purchase and its associated items.
//     purchaseRepo.delete(purchase);
//     purchaseRepo.flush();

//     // Recalculate stock for all affected products.
//     for (Long productId : affectedProductIds) {
//         productService.recalculateCurrentStockQuantity(productId);
//     }

//     return "Purchase and linked journal entry deleted successfully";
// }




// //     @Transactional
// //     public String delete(
// //             @NonNull Long id) {

// //         // =====================================================
// //         // GET PURCHASE
// //         // =====================================================

// //         Purchase purchase =
// //                 getById(id);

// //         List<Long> affectedProductIds = new ArrayList<>();
// //         if (purchase.getItems() != null) {
// //             for (PurchaseItem item : purchase.getItems()) {
// //                 if (item != null && item.getProduct() != null) {
// //                     Long productId = item.getProduct().getId();
// //                     if (productId != null && !affectedProductIds.contains(productId)) {
// //                         affectedProductIds.add(productId);
// //                     }
// //                 }
// //             }
// //         }


// //         // =====================================================
// //         // DELETE JOURNAL
// //         // =====================================================

// //         deleteJournalByVoucherNumber(purchase.getVoucherNumber());
// //         taxApiService.cancelSourceInvoice("PURCHASE", purchase.getId());


// //         // =====================================================
// //         // DELETE PURCHASE
// //         // =====================================================

// //         purchaseRepo.delete(purchase);
// //         purchaseRepo.flush();
// //         affectedProductIds.forEach(productService::recalculateCurrentStockQuantity);


// //         return "Purchase deleted successfully";
// //     }
// }




package com.vpm.Accounts.Service;



import java.math.BigDecimal;

import java.util.ArrayList;

import java.util.List;

import java.util.concurrent.CompletableFuture;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.lang.NonNull;

import org.springframework.scheduling.annotation.Async;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.support.TransactionTemplate;



import com.vpm.Accounts.Entity.Account;

import com.vpm.Accounts.Entity.JournalEntry;

import com.vpm.Accounts.Entity.JournalEntryLine;

import com.vpm.Accounts.Entity.FinancialYear;

import com.vpm.Accounts.Entity.AccountingPeriod;

import com.vpm.Accounts.Entity.Product;

import com.vpm.Accounts.Entity.Purchase;

import com.vpm.Accounts.Entity.PurchaseItem;

import com.vpm.Accounts.Repository.AccountRepository;

import com.vpm.Accounts.Repository.JournalEntryRepository;

import com.vpm.Accounts.Repository.ProductRepository;

import com.vpm.Accounts.Repository.PurchaseRepository;

import com.vpm.Accounts.Repository.FinancialYearRepository;

import com.vpm.Accounts.Repository.AccountingPeriodRepository;

import com.vpm.Accounts.security.TenantContext;

import com.vpm.Accounts.Entity.AccountType;

import com.vpm.Accounts.Service.GST.TaxApiService;



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



    @Autowired

    private TransactionTemplate transactionTemplate;



    @Autowired

    private FinancialYearRepository financialYearRepo;



    @Autowired

    private AccountingPeriodRepository accountingPeriodRepo;



    @Autowired

    private ProductService productService;



    @Autowired

    private TaxApiService taxApiService;





    // =========================================================

    // COMMON METHODS

    // =========================================================



    private Account findAccountByName(String name) {



        if (name == null || name.isBlank()) {

            return null;

        }



        return accountRepo.findFirstByNameIgnoreCase(name)

                .orElse(null);

    }



    private void validateOpenAccountingPeriod(java.time.LocalDate date) {

        if (date == null) {

            throw new IllegalArgumentException("Purchase date is required");

        }

        FinancialYear financialYear = financialYearRepo

                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)

                .orElseThrow(() -> new IllegalArgumentException(

                        "No financial year exists for purchase date: " + date));

        if (financialYear.isClosed()) {

            throw new IllegalStateException(

                    "Financial year " + financialYear.getName() + " is closed");

        }

        AccountingPeriod accountingPeriod = accountingPeriodRepo

                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(

                        financialYear.getId(), date, date)

                .orElseThrow(() -> new IllegalArgumentException(

                        "No accounting period exists for purchase date: " + date));

        if (accountingPeriod.isClosed()) {

            throw new IllegalStateException(

                    "Accounting period " + accountingPeriod.getName() + " is closed");

        }

    }



    // =========================================================

    // CALCULATE PURCHASE TOTAL

    // =========================================================



    private BigDecimal calculatePurchaseTotal(Purchase purchase) {



        if (purchase == null

                || purchase.getItems() == null

                || purchase.getItems().isEmpty()) {



            return BigDecimal.ZERO;

        }



        return purchase.getItems()

                .stream()

                .filter(item -> item != null)

                .map(PurchaseItem::getTotal)

                .filter(total -> total != null)

                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }





    // =========================================================

    // CREATE JOURNAL ENTRY

    // =========================================================



    private JournalEntry createPurchaseJournal(

            Purchase purchase,

            BigDecimal amount) {



        if (purchase == null) {

            throw new RuntimeException("Purchase cannot be null");

        }



        if (amount == null) {

            amount = BigDecimal.ZERO;

        }



        if (amount.compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(

                    "Purchase amount cannot be negative");

        }





        // =====================================================

        // FIND PURCHASE ACCOUNT

        // =====================================================



        Account purchaseAcc =

                findAccountByName("Purchases");



        if (purchaseAcc == null) {

            throw new RuntimeException(

                    "Purchases account not found");

        }





        // =====================================================

        // FIND SUPPLIER

        // =====================================================



        Account supplier =

                findAccountByName(

                        purchase.getSupplierName());





        // =====================================================

        // FIND CASH ACCOUNT

        // =====================================================



        Account cash =

                findAccountByName("Cash");





        // =====================================================

        // CREATE JOURNAL

        // =====================================================



        JournalEntry journal =

                new JournalEntry();





        // Voucher number

        journal.setVoucherNumber(

                purchase.getVoucherNumber());





        // Entry date

        journal.setEntryDate(purchase.getDate());

        FinancialYear financialYear = financialYearRepo

                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(

                        purchase.getDate(), purchase.getDate())

                .orElseThrow(() -> new IllegalArgumentException(

                        "No financial year exists for purchase date: " + purchase.getDate()));

        journal.setFinancialYear(financialYear);

        accountingPeriodRepo

                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(

                        financialYear.getId(), purchase.getDate(), purchase.getDate())

                .ifPresent(journal::setAccountingPeriod);





        // Description

        journal.setDescription(

                "Purchase Entry - "

                        + purchase.getSupplierName());





        // =====================================================

        // JOURNAL LINES

        // =====================================================



        List<JournalEntryLine> lines =

                new ArrayList<>();





        // =====================================================

        // PURCHASES A/C - DEBIT

        // =====================================================



        JournalEntryLine dr =

                new JournalEntryLine();



        dr.setAccount(purchaseAcc);



        boolean gstEnabledForPurchase = purchase.getGstTaxableAmount() != null

                && (purchase.getGstTaxableAmount().signum() > 0

                        || purchase.getGstTotal() != null && purchase.getGstTotal().signum() > 0);

        dr.setDebit(gstEnabledForPurchase ? purchase.getGstTaxableAmount() : amount);



        dr.setCredit(BigDecimal.ZERO);



        dr.setJournalEntry(journal);



        lines.add(dr);



        if (gstEnabledForPurchase) {

            addInputTaxDebit(lines, journal, "Input CGST", purchase.getGstCgst());

            addInputTaxDebit(lines, journal, "Input SGST", purchase.getGstSgst());

            addInputTaxDebit(lines, journal, "Input IGST", purchase.getGstIgst());

            addInputTaxDebit(lines, journal, "Input GST Cess", purchase.getGstCess());

            amount = purchase.getGrandTotal();

        }





        // =====================================================

        // SUPPLIER / CASH - CREDIT

        // =====================================================



        JournalEntryLine cr =

                new JournalEntryLine();





        if (supplier != null) {



            // Credit Supplier

            cr.setAccount(supplier);



        } else {



            // If supplier account is not found,

            // use Cash account.



            if (cash == null) {

                throw new RuntimeException(

                        "Cash account not found");

            }



            cr.setAccount(cash);

        }





        cr.setDebit(BigDecimal.ZERO);



        cr.setCredit(amount);



        cr.setJournalEntry(journal);



        lines.add(cr);





        // =====================================================

        // SET LINES

        // =====================================================



        journal.setLines(lines);





        // =====================================================

        // SAVE JOURNAL

        // =====================================================



        return journalRepo.save(journal);

    }



    private void addInputTaxDebit(List<JournalEntryLine> lines, JournalEntry journal,

            String accountName, BigDecimal value) {

        BigDecimal amount = value == null ? BigDecimal.ZERO : value;

        if (amount.signum() <= 0) return;

        Account inputTaxAccount = findAccountByName(accountName);

        if (inputTaxAccount == null) {

            inputTaxAccount = new Account();

            inputTaxAccount.setName(accountName);

            inputTaxAccount.setType(AccountType.ASSET);

            inputTaxAccount.setAccountGroup("CURRENT_ASSETS");

            inputTaxAccount.setBalance(BigDecimal.ZERO);

            inputTaxAccount = accountRepo.save(inputTaxAccount);

        }

        JournalEntryLine line = new JournalEntryLine();

        line.setAccount(inputTaxAccount);

        line.setDebit(amount);

        line.setCredit(BigDecimal.ZERO);

        line.setJournalEntry(journal);

        lines.add(line);

    }





    // =========================================================

    // DELETE JOURNAL

    // =========================================================



    private void deleteJournalByDescription(String desc) {



        if (desc == null || desc.isBlank()) {

            return;

        }



        List<JournalEntry> entries =

                journalRepo.findAll();





        for (JournalEntry journal : entries) {



            if (journal.getDescription() != null

                    && journal.getDescription()

                            .contains(desc)) {



                journalRepo.delete(journal);

            }

        }

    }



        private void deleteJournalByVoucherNumber(String voucherNumber) {

                if (voucherNumber == null || voucherNumber.isBlank()) {

                        return;

                }



                journalRepo.deleteByVoucherNumber(voucherNumber);

                journalRepo.flush();

        }





    // =========================================================

    // SAVE PURCHASE

    // =========================================================



    @Transactional

    public Purchase savePurchase(Purchase purchase) {



        if (purchase == null) {

            throw new RuntimeException(

                    "Purchase cannot be null");

        }



                if (purchase.getSupplierName() == null || purchase.getSupplierName().isBlank()) {

                        throw new IllegalArgumentException("Supplier is required");

                }



                // if (purchase.getDate() == null || purchase.getDate().isBlank()) {

                //         throw new IllegalArgumentException("Purchase date is required");

                // }



                if (purchase.getDate() == null ) {

                        throw new IllegalArgumentException("Purchase date is required");

                }



                Long tenantId = TenantContext.require();

                int year = purchase.getDate().getYear();

                purchase.setVoucherNumber(null);

                validateOpenAccountingPeriod(purchase.getDate());





        // =====================================================

        // VALIDATE ITEMS

        // =====================================================



        if (purchase.getItems() == null

                || purchase.getItems().isEmpty()) {



            throw new RuntimeException(

                    "Purchase must contain at least one item");

        }





        // =====================================================

        // CALCULATE TOTAL

        // =====================================================



        BigDecimal totalAmount =

                calculatePurchaseTotal(purchase);





        // =====================================================

        // STOCK UPDATE

        // =====================================================



        for (PurchaseItem item :

                purchase.getItems()) {



            if (item == null) {

                continue;

            }





            if (item.getProduct() == null) {

                throw new RuntimeException(

                        "Product is required for purchase item");

            }





            Long productId =

                    item.getProduct().getId();





            if (productId == null) {

                throw new RuntimeException(

                        "Product ID is required");

            }





            if (item.getQuantity() < 0) {

                throw new RuntimeException(

                        "Quantity cannot be negative");

            }





            // =================================================

            // FIND PRODUCT

            // =================================================



            Product product =

                    productRepo.findById(productId)

                            .orElseThrow(() ->

                                    new RuntimeException(

                                            "Product not found with ID: "

                                                    + productId));





            // =================================================

            // GET CURRENT STOCK

            // =================================================



            BigDecimal currentStock =

                    product.getCurrentStockQuantity();



            if (currentStock == null) {

                currentStock =

                        BigDecimal.ZERO;

            }





            // =================================================

            // ADD PURCHASE QUANTITY

            // =================================================



            BigDecimal quantity =

                    BigDecimal.valueOf(

                            item.getQuantity());





                    product.setCurrentStockQuantity(

                    currentStock.add(quantity)

            );





            // =================================================

            // SAVE PRODUCT

            // =================================================



            productRepo.save(product);





            // =================================================

            // SET RELATIONSHIPS

            // =================================================



            item.setProduct(product);



            item.setPurchase(purchase);

        }





        // =====================================================

        // SAVE PURCHASE

        // =====================================================



        Purchase savedPurchase = purchaseRepo.saveAndFlush(purchase);

        savedPurchase.setVoucherNumber(String.format(

                "PURV-%d-%d-%06d", tenantId, year, savedPurchase.getId()));

        savedPurchase = purchaseRepo.saveAndFlush(savedPurchase);

        taxApiService.syncPurchaseTax(savedPurchase);

        savedPurchase = purchaseRepo.saveAndFlush(savedPurchase);

        createPurchaseJournal(savedPurchase, totalAmount);

        return savedPurchase;

    }





    // =========================================================

    // ASYNC SAVE

    // =========================================================



    @Async("accountExecutor")

    public CompletableFuture<Purchase> savePurchaseAsync(

            Purchase purchase) {



        return CompletableFuture.completedFuture(

                transactionTemplate.execute(status -> savePurchase(purchase)));

    }





    // =========================================================

    // ASYNC GET ALL

    // =========================================================



    @Async("accountExecutor")

    public CompletableFuture<List<Purchase>> getAllAsync() {



        return CompletableFuture.completedFuture(

                getAll());

    }





    // =========================================================

    // ASYNC GET BY ID

    // =========================================================



    @Async("accountExecutor")

    public CompletableFuture<Purchase> getByIdAsync(

            @NonNull Long id) {



        return CompletableFuture.completedFuture(

                getById(id));

    }





    // =========================================================

    // ASYNC UPDATE

    // =========================================================



    @Async("accountExecutor")

    public CompletableFuture<Purchase> updatePurchaseAsync(

            @NonNull Long id,

            Purchase newPurchase) {



        return CompletableFuture.completedFuture(

                updatePurchase(id, newPurchase));

    }





    // =========================================================

    // ASYNC DELETE

    // =========================================================



    @Async("accountExecutor")

    @Transactional

    public CompletableFuture<String> deleteAsync(

            @NonNull Long id) {



        return CompletableFuture.completedFuture(

                delete(id));

    }





    // =========================================================

    // GET ALL

    // =========================================================



    public List<Purchase> getAll() {



        return purchaseRepo.findAll();

    }





    // =========================================================

    // GET BY ID

    // =========================================================



    public Purchase getById(

            @NonNull Long id) {



        return purchaseRepo.findById(id)

                .orElseThrow(() ->

                        new RuntimeException(

                                "Purchase not found with ID: "

                                        + id));

    }





    // =========================================================

    // UPDATE PURCHASE

    // =========================================================



    @Transactional

    public Purchase updatePurchase(

            @NonNull Long id,

            Purchase newPurchase) {



        if (newPurchase == null) {

            throw new RuntimeException(

                    "Updated purchase cannot be null");

        }





        // =====================================================

        // GET EXISTING PURCHASE

        // =====================================================



        Purchase existing =

                getById(id);





        // =====================================================

        // VALIDATE NEW ITEMS

        // =====================================================



        if (newPurchase.getItems() == null

                || newPurchase.getItems().isEmpty()) {



            throw new RuntimeException(

                    "Purchase must contain at least one item");

        }

        validateOpenAccountingPeriod(newPurchase.getDate());





        // =====================================================

        // REVERSE OLD STOCK

        // =====================================================



        if (existing.getItems() != null) {



            for (PurchaseItem oldItem :

                    existing.getItems()) {



                if (oldItem == null) {

                    continue;

                }





                Product product =

                        oldItem.getProduct();





                if (product == null) {

                    continue;

                }





                // =============================================

                // CURRENT STOCK

                // =============================================



                BigDecimal currentStock =

                        product.getCurrentStockQuantity();



                if (currentStock == null) {

                    currentStock =

                            BigDecimal.ZERO;

                }





                // =============================================

                // OLD QUANTITY

                // =============================================



                BigDecimal oldQuantity =

                        BigDecimal.valueOf(

                                oldItem.getQuantity());





                // =============================================

                // REVERSE OLD PURCHASE

                // =============================================



                BigDecimal newStock =

                        currentStock.subtract(

                                oldQuantity);





                // =============================================

                // PREVENT NEGATIVE STOCK

                // =============================================



                if (newStock.compareTo(

                        BigDecimal.ZERO) < 0) {



                    throw new RuntimeException(

                            "Stock cannot become negative "

                                    + "while updating purchase. "

                                    + "Product: "

                                    + product.getName());

                }





                product.setCurrentStockQuantity(

                        newStock);





                productRepo.save(product);

            }

        }





        // =====================================================

        // DELETE OLD JOURNAL

        // =====================================================



        deleteJournalByVoucherNumber(existing.getVoucherNumber());





        // =====================================================

        // CALCULATE NEW TOTAL

        // =====================================================



        BigDecimal newAmount =

                calculatePurchaseTotal(

                        newPurchase);

        newPurchase.setVoucherNumber(String.format(

                "PURV-%d-%d-%06d",

                TenantContext.require(),

                newPurchase.getDate().getYear(),

                existing.getId()));





        // =====================================================

        // APPLY NEW STOCK

        // =====================================================



        for (PurchaseItem item :

                newPurchase.getItems()) {



            if (item == null) {

                continue;

            }





            if (item.getProduct() == null) {

                throw new RuntimeException(

                        "Product is required for purchase item");

            }





            Long productId =

                    item.getProduct().getId();





            if (productId == null) {

                throw new RuntimeException(

                        "Product ID is required");

            }





            if (item.getQuantity() < 0) {

                throw new RuntimeException(

                        "Quantity cannot be negative");

            }





            // =================================================

            // FIND PRODUCT

            // =================================================



            Product product =

                    productRepo.findById(productId)

                            .orElseThrow(() ->

                                    new RuntimeException(

                                            "Product not found with ID: "

                                                    + productId));





            // =================================================

            // CURRENT STOCK

            // =================================================



            BigDecimal currentStock =

                    product.getCurrentStockQuantity();



            if (currentStock == null) {

                currentStock =

                        BigDecimal.ZERO;

            }





            // =================================================

            // NEW QUANTITY

            // =================================================



            BigDecimal quantity =

                    BigDecimal.valueOf(

                            item.getQuantity());





            // =================================================

            // ADD NEW PURCHASE QUANTITY

            // =================================================



            product.setCurrentStockQuantity(

                    currentStock.add(quantity)

            );





            // =================================================

            // SAVE PRODUCT

            // =================================================



            productRepo.save(product);





            // =================================================

            // SET RELATIONSHIPS

            // =================================================



            item.setProduct(product);



            item.setPurchase(existing);

        }





        // =====================================================

        // CREATE NEW JOURNAL

        // =====================================================



        // =====================================================

        // UPDATE PURCHASE FIELDS

        // =====================================================



        existing.setItems(

                newPurchase.getItems());





        existing.setDate(

                newPurchase.getDate());





        existing.setSupplierName(

                newPurchase.getSupplierName());





        existing.setPhoneNo(

                newPurchase.getPhoneNo());





        existing.setVoucherNumber(

                newPurchase.getVoucherNumber());





        // =====================================================

        // SAVE UPDATED PURCHASE

        // =====================================================



        Purchase updatedPurchase = purchaseRepo.save(existing);

        taxApiService.syncPurchaseTax(updatedPurchase);

        updatedPurchase = purchaseRepo.save(updatedPurchase);

        createPurchaseJournal(

                updatedPurchase,

                updatedPurchase.getGrandTotal() == null

                        ? newAmount : updatedPurchase.getGrandTotal());

        return updatedPurchase;

    }





    // =========================================================

    // DELETE PURCHASE

    // =========================================================



    @Transactional
    public String delete(@NonNull Long id) {
        Purchase purchase = purchaseRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found with ID: " + id));

        // Preserve product IDs before deleting purchase items.
        List<Long> affectedProductIds = new ArrayList<>();

        if (purchase.getItems() != null) {
            for (PurchaseItem item : purchase.getItems()) {
                if (item == null || item.getProduct() == null) {
                    continue;
                }

                Long productId = item.getProduct().getId();
                if (productId != null && !affectedProductIds.contains(productId)) {
                    affectedProductIds.add(productId);
                }
            }
        }

        // Delete the journal entry linked to this purchase voucher.
        deleteJournalByVoucherNumber(purchase.getVoucherNumber());

        // Cancel the associated tax invoice, when applicable.
        taxApiService.cancelSourceInvoice("PURCHASE", purchase.getId());

        // Delete the purchase (and its items according to the entity mapping).
        purchaseRepo.delete(purchase);
        purchaseRepo.flush();

        // Recalculate stock for affected products after deletion.
        for (Long productId : affectedProductIds) {
            productService.recalculateCurrentStockQuantity(productId);
        }

        return "Purchase and linked journal entry deleted successfully";
    }


@Transactional
public boolean deleteByVoucherNumber(String voucherNumber) {

    if (voucherNumber == null || voucherNumber.isBlank()) {
        return false;
    }

    Purchase purchase = purchaseRepo
            .findFirstByVoucherNumber(voucherNumber)
            .orElse(null);

    if (purchase == null) {
        return false;
    }

    // Reuse the existing deletion logic.
    delete(purchase.getId());

    return true;
}







}
