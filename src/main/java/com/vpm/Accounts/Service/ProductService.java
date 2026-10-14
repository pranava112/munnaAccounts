package com.vpm.Accounts.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.JournalEntryLine;
import com.vpm.Accounts.Entity.Purchase;
import com.vpm.Accounts.Entity.PurchaseItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.PurchaseRepository;
import com.vpm.Accounts.Repository.SaleRepository;
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.security.TenantContext;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private JournalEntryRepository journalRepo;

    @Autowired
    private SaleRepository saleRepo;

    @Autowired
    private PurchaseRepository purchaseRepo;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Value("${app.barcode.directory:}")
    private String barcodeDirectory;

    /**
     * Save Product and Auto Generate Barcode
     */
    // public Product saveProduct(@NonNull Product p) {

    //     // First save to generate ID
    //     Product saved = repo.save(p);

    //     // Barcode based on Product ID
    //     String barcode = String.format("%06d", saved.getId());

    //     saved.setBarcode(barcode);

    //     saved = repo.save(saved);

    //     generateBarcode(barcode);

    //     return saved;
    // }

    @Transactional
    public Product saveProduct(Product p) {

        validateTaxDetails(p);
        if (!p.getActive()) {
            p.setActive(true);
        }

        String barcode =
                String.format("%06d",
                        new Random().nextInt(999999));

        while (repo.findByBarcode(barcode).isPresent()) {

            barcode =
                    String.format("%06d",
                            new Random().nextInt(999999));
        }

        p.setBarcode(barcode);
        p.setOpeningStockDate(LocalDate.now());

        Product saved = repo.save(p);

        createOpeningStockJournal(saved);

        generateBarcode(barcode);

        return saved;
    }

    private void createOpeningStockJournal(Product product) {
        BigDecimal quantity = product.getOpeningStockQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String voucherNumber = "OB-STOCK-" + TenantContext.require()
                + "-" + product.getId();
        if (journalRepo.findByVoucherNumber(voucherNumber).isPresent()) {
            return;
        }

        BigDecimal purchasePrice = product.getPurchasePrice() == null
                ? BigDecimal.ZERO
                : product.getPurchasePrice();
        BigDecimal openingValue = quantity.multiply(purchasePrice);
        if (openingValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Opening stock value must be greater than zero");
        }

        Account openingStockAccount = accountRepo.findFirstByType(AccountType.OPENING_STOCK)
                .orElse(null);
        if (openingStockAccount == null) {
            openingStockAccount = accountRepo.findAll().stream()
                    .filter(account -> "OPENING_STOCK".equalsIgnoreCase(account.getAccountGroup()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Opening Stock account not found"));
        }
        Account capitalAccount = accountRepo.findFirstByType(AccountType.CAPITAL)
                .orElseThrow(() -> new IllegalStateException(
                        "Capital account not found for opening stock"));

        product.setOpeningStockValue(openingValue);
        repo.save(product);

        JournalEntry journal = new JournalEntry();
        journal.setVoucherNumber(voucherNumber);
        // journal.setEntryDate(LocalDate.now().toString());
        journal.setEntryDate(product.getOpeningStockDate());
        journal.setDescription("Opening stock - " + product.getName());

        JournalEntryLine debit = new JournalEntryLine();
        debit.setAccount(openingStockAccount);
        debit.setDebit(openingValue);
        debit.setCredit(BigDecimal.ZERO);
        debit.setJournalEntry(journal);

        JournalEntryLine credit = new JournalEntryLine();
        credit.setAccount(capitalAccount);
        credit.setDebit(BigDecimal.ZERO);
        credit.setCredit(openingValue);
        credit.setJournalEntry(journal);

        List<JournalEntryLine> lines = new ArrayList<>();
        lines.add(debit);
        lines.add(credit);
        journal.setLines(lines);
        journalRepo.save(journal);
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> saveProductAsync(
            @NonNull Long tenantId,
            @NonNull Product p) {
        return CompletableFuture.completedFuture(
                executeForTenant(tenantId, () ->
                        transactionTemplate.execute(
                                status -> saveProduct(p)
                        )
                )
        );
    }

    /**
     * Get All Products
     */
    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Product>> getAllProductsAsync(
            @NonNull Long tenantId) {
        return CompletableFuture.completedFuture(
                executeForTenant(tenantId, this::getAllProducts)
        );
    }

    /**
     * Get Product By ID
     */
    public Product getProductById(@NonNull Long id) {

        return repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> getProductByIdAsync(
            @NonNull Long tenantId,
            @NonNull Long id) {
        return CompletableFuture.completedFuture(
                executeForTenant(tenantId, () -> getProductById(id))
        );
    }

    /**
     * Get Product By Barcode
     */
    public Product getProductByBarcode(String barcode) {

        return repo.findByBarcode(barcode)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));
    }

    public byte[] getBarcodeImage(String barcode) {
        getProductByBarcode(barcode);
        Path imagePath = findBarcodeImagePath(barcode);
        if (imagePath == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Barcode image not found for barcode: " + barcode);
        }
        try {
            return Files.readAllBytes(imagePath);
        } catch (java.io.IOException error) {
            throw new IllegalStateException(
                    "Unable to read barcode image for product barcode: " + barcode,
                    error);
        }
    }

    private Path findBarcodeImagePath(String barcode) {
        for (Path directory : barcodeDirectories()) {
            Path imagePath = directory.resolve(barcode + ".png").normalize();
            if (imagePath.startsWith(directory) && Files.isRegularFile(imagePath)) {
                return imagePath;
            }
        }
        return null;
    }

    private Path resolveBarcodeDirectory() {
        List<Path> directories = barcodeDirectories();
        return directories.stream()
                .filter(Files::isDirectory)
                .findFirst()
                .orElse(directories.get(0));
    }

    private List<Path> barcodeDirectories() {
        if (barcodeDirectory != null && !barcodeDirectory.isBlank()) {
            return List.of(Path.of(barcodeDirectory).toAbsolutePath().normalize());
        }

        Path workingDirectory = Path.of(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize();
        Set<Path> candidates = new LinkedHashSet<>();
        candidates.add(workingDirectory.resolve("uploads").resolve("barcodes").normalize());

        Path ancestor = workingDirectory;
        for (int depth = 0; ancestor != null && depth < 6; depth++) {
            candidates.add(ancestor.resolve("Backend")
                    .resolve("munnaAccounts")
                    .resolve("uploads")
                    .resolve("barcodes")
                    .normalize());
            candidates.add(ancestor.resolve("uploads")
                    .resolve("barcodes")
                    .normalize());
            ancestor = ancestor.getParent();
        }
        return new ArrayList<>(candidates);
    }

    @Transactional
    public void recalculateCurrentStockQuantity(@NonNull Long productId) {
        Product product = getProductById(productId);
        BigDecimal purchasedQuantity = BigDecimal.ZERO;
        for (Purchase purchase : purchaseRepo.findDistinctByItems_Product_Id(productId)) {
            if (purchase.getItems() == null) {
                continue;
            }
            for (PurchaseItem item : purchase.getItems()) {
                if (item != null && item.getProduct() != null
                        && productId.equals(item.getProduct().getId())) {
                    purchasedQuantity = purchasedQuantity.add(
                            BigDecimal.valueOf(item.getQuantity()));
                }
            }
        }

        BigDecimal soldQuantity = BigDecimal.ZERO;
        for (Sale sale : saleRepo.findDistinctByItems_Product_Id(productId)) {
            if (sale.getItems() == null) {
                continue;
            }
            for (SaleItem item : sale.getItems()) {
                if (item != null && item.getProduct() != null
                        && productId.equals(item.getProduct().getId())) {
                    soldQuantity = soldQuantity.add(
                            BigDecimal.valueOf(item.getQuantity()));
                }
            }
        }

        BigDecimal openingQuantity = product.getOpeningStockQuantity() == null
                ? BigDecimal.ZERO
                : product.getOpeningStockQuantity();
        BigDecimal currentQuantity = openingQuantity
                .add(purchasedQuantity)
                .subtract(soldQuantity);
        if (currentQuantity.signum() < 0) {
            throw new IllegalStateException(
                    "Recorded sales exceed opening stock and purchases for product: "
                            + product.getName());
        }

        product.setCurrentStockQuantity(currentQuantity);
        repo.save(product);
    }

    /**
     * Update Product
     */
    @Transactional
    public Product updateProduct(
            @NonNull Long id,
            @NonNull Product p) {

        Product existing = getProductById(id);

        existing.setName(p.getName());
        existing.setCode(p.getCode());
        existing.setPurchasePrice(p.getPurchasePrice());
        existing.setSellingPrice(p.getSellingPrice());
        validateTaxDetails(p);
        existing.setHsnSacCode(p.getHsnSacCode());
        existing.setGstRate(p.getGstRate());
        existing.setCessRate(p.getCessRate());
        existing.setActive(p.getActive());

        // Keep Existing Barcode
        Product updated = repo.save(existing);

        createOpeningStockJournal(updated);

        // Regenerate barcode image if needed
        if (updated.getBarcode() != null &&
                !updated.getBarcode().isBlank()) {

            generateBarcode(updated.getBarcode());
        }

        return updated;
    }

    private void validateTaxDetails(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required");
        }
        BigDecimal gstRate = product.getGstRate() == null ? BigDecimal.ZERO : product.getGstRate();
        BigDecimal cessRate = product.getCessRate() == null ? BigDecimal.ZERO : product.getCessRate();
        if (gstRate.signum() < 0 || gstRate.compareTo(new BigDecimal("100")) > 0
                || cessRate.signum() < 0 || cessRate.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("GST and cess rates must be between 0 and 100");
        }
        product.setGstRate(gstRate);
        product.setCessRate(cessRate);
        if (product.getHsnSacCode() != null && !product.getHsnSacCode().isBlank()
                && !product.getHsnSacCode().trim().matches("\\d{4,8}")) {
            throw new IllegalArgumentException("HSN/SAC code must contain 4 to 8 digits");
        }
        if (product.getHsnSacCode() != null) {
            product.setHsnSacCode(product.getHsnSacCode().trim());
        }
    }

    @Async("accountExecutor")
    public CompletableFuture<Product> updateProductAsync(
            @NonNull Long tenantId,
            @NonNull Long id,
            @NonNull Product p) {

        return CompletableFuture.completedFuture(
                executeForTenant(tenantId, () ->
                        transactionTemplate.execute(
                                status -> updateProduct(id, p)
                        )
                ));
    }

    /**
     * Delete Product
     */
    public void deleteById(@NonNull Long id) {
        Product product = getProductById(id);
        if (saleRepo.existsByItems_Product_Id(id)
                || purchaseRepo.existsByItems_Product_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product cannot be deleted because it is used in sales or purchases");
        }

        String openingStockVoucher =
                "OB-STOCK-" + TenantContext.require() + "-" + product.getId();
        journalRepo.findByVoucherNumber(openingStockVoucher)
                .ifPresentOrElse(
                        journalRepo::delete,
                        () -> journalRepo.findByVoucherNumber(
                                "OB-STOCK-" + product.getId())
                                .ifPresent(journalRepo::delete));
        repo.delete(product);
    }

    @Async("accountExecutor")
    public CompletableFuture<Void> deleteByIdAsync(
            @NonNull Long tenantId,
            @NonNull Long id) {

        return CompletableFuture.completedFuture(
                executeForTenant(tenantId, () ->
                        transactionTemplate.execute(status -> {
                            deleteById(id);
                            return null;
                        })
                )
        );
    }

    /**
     * Generate Barcode Image
     */
    private void generateBarcode(String barcode) {

        try {

            Path folder = resolveBarcodeDirectory();

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            BitMatrix matrix =
                    new MultiFormatWriter().encode(
                            barcode,
                            BarcodeFormat.CODE_128,
                            600,
                            200
                    );

            Path path =
                    folder.resolve(barcode + ".png");

            MatrixToImageWriter.writeToPath(
                    matrix,
                    "PNG",
                    path
            );

            System.out.println(
                    "Barcode Generated : " +
                            path.toAbsolutePath());

        } catch (Exception e) {

            System.err.println(
                    "Barcode Generation Failed");

            e.printStackTrace();
        }
    }

    private <T> T executeForTenant(
            Long tenantId,
            Supplier<T> operation) {
        TenantContext.set(tenantId);
        try {
            return operation.get();
        } finally {
            TenantContext.clear();
        }
    }
}