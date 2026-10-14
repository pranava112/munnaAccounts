// package com.vpm.Accounts.Service.AI;

// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Locale;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;
// import com.vpm.Accounts.DTO.AI.AITransactionItemDTO;
// import com.vpm.Accounts.Entity.Account;
// import com.vpm.Accounts.Entity.Product;
// import com.vpm.Accounts.Entity.Purchase;
// import com.vpm.Accounts.Entity.PurchaseItem;
// import com.vpm.Accounts.Entity.Sale;
// import com.vpm.Accounts.Entity.SaleItem;
// import com.vpm.Accounts.Repository.AccountRepository;
// import com.vpm.Accounts.Repository.ProductRepository;
// import com.vpm.Accounts.Service.ProductService;
// import com.vpm.Accounts.Service.PurchaseService;
// import com.vpm.Accounts.Service.SaleService;

// @Service
// public class AITransactionPostingService {

//     private final AccountRepository accountRepository;
//     private final ProductRepository productRepository;
//     private final ProductService productService;
//     private final PurchaseService purchaseService;
//     private final SaleService saleService;

//     public AITransactionPostingService(
//             AccountRepository accountRepository,
//             ProductRepository productRepository,
//             ProductService productService,
//             PurchaseService purchaseService,
//             SaleService saleService) {
//         this.accountRepository = accountRepository;
//         this.productRepository = productRepository;
//         this.productService = productService;
//         this.purchaseService = purchaseService;
//         this.saleService = saleService;
//     }

//     @Transactional
//     public Object post(AITransactionDraftDTO draft) {
//         validateDraft(draft);
//         requireCounterpartyAccount(draft.getCounterpartyName());

//         String type = draft.getTransactionType().trim().toUpperCase(Locale.ROOT);
//         if ("PURCHASE".equals(type)) {
//             return postPurchase(draft);
//         }
//         if ("SALES".equals(type)) {
//             return postSale(draft);
//         }
//         throw new IllegalArgumentException("Transaction type must be PURCHASE or SALES");
//     }

//     @Transactional
//     public List<Object> postBatch(List<AITransactionDraftDTO> drafts) {
//         if (drafts == null || drafts.isEmpty()) {
//             throw new IllegalArgumentException("At least one transaction is required");
//         }
//         return drafts.stream().map(this::post).collect(Collectors.toList());
//     }

//     private Purchase postPurchase(AITransactionDraftDTO draft) {
//         Purchase purchase = new Purchase();
//         purchase.setSupplierName(draft.getCounterpartyName().trim());
//         purchase.setDate(draft.getDate());
//         List<PurchaseItem> items = new ArrayList<>();

//         for (AITransactionItemDTO item : draft.getItems()) {
//             Product product = item.getProductId() == null
//                     ? resolvePurchaseProduct(item)
//                     : findActiveProduct(item.getProductId(), item.getProductName());
//             int quantity = item.getQuantity().intValueExact();
//             if (applyInvoiceHsnIfProvided(item, product)) {
//                 productRepository.save(product);
//             }
//             validateProductTaxRates(item, product);

//             PurchaseItem purchaseItem = new PurchaseItem();
//             purchaseItem.setProduct(product);
//             purchaseItem.setQuantity(quantity);
//             purchaseItem.setPrice(item.getUnitPrice());
//             purchaseItem.setPurchase(purchase);
//             items.add(purchaseItem);
//         }
//         purchase.setItems(items);

//         Purchase saved = purchaseService.savePurchase(purchase);
//         requireInvoiceTotal(draft.getInvoiceTotal(), saved.getGrandTotal(), "purchase");
//         return saved;
//     }

//     private Sale postSale(AITransactionDraftDTO draft) {
//         Sale sale = new Sale();
//         sale.setCustomerName(draft.getCounterpartyName().trim());
//         sale.setDate(draft.getDate());
//         List<SaleItem> items = new ArrayList<>();

//         for (AITransactionItemDTO item : draft.getItems()) {
//             if (item.getProductId() == null) {
//                 throw new IllegalArgumentException(
//                         "Sales require an existing product selection for: " + item.getProductName());
//             }
//             Product product = findActiveProduct(item.getProductId(), item.getProductName());
//             if (applyInvoiceHsnIfProvided(item, product)) {
//                 productRepository.save(product);
//             }
//             validateProductTaxRates(item, product);

//             SaleItem saleItem = new SaleItem();
//             saleItem.setProduct(product);
//             saleItem.setQuantity(item.getQuantity().intValueExact());
//             saleItem.setPrice(item.getUnitPrice());
//             saleItem.setSale(sale);
//             items.add(saleItem);
//         }
//         sale.setItems(items);

//         Sale saved = saleService.saveSaleFromInvoice(sale);
//         requireInvoiceTotal(draft.getInvoiceTotal(), saved.getGrandTotal(), "sales");
//         return saved;
//     }

//     private void validateDraft(AITransactionDraftDTO draft) {
//         if (draft == null) {
//             throw new IllegalArgumentException("Transaction draft is required");
//         }
//         if (draft.getTransactionType() == null
//                 || !List.of("PURCHASE", "SALES").contains(
//                         draft.getTransactionType().trim().toUpperCase(Locale.ROOT))) {
//             throw new IllegalArgumentException("Transaction type must be PURCHASE or SALES");
//         }
//         if (draft.getDate() == null) {
//             throw new IllegalArgumentException("Invoice date is required");
//         }
//         if (draft.getCounterpartyName() == null || draft.getCounterpartyName().isBlank()) {
//             throw new IllegalArgumentException("Supplier/customer name is required");
//         }
//         if (draft.getInvoiceTotal() == null || draft.getInvoiceTotal().signum() <= 0) {
//             throw new IllegalArgumentException("Invoice total must be greater than zero");
//         }
//         if (draft.getItems() == null || draft.getItems().isEmpty()) {
//             throw new IllegalArgumentException("At least one invoice item is required");
//         }

//         for (AITransactionItemDTO item : draft.getItems()) {
//             if (item == null || item.getProductName() == null || item.getProductName().isBlank()) {
//                 throw new IllegalArgumentException("Every invoice item needs a product name");
//             }
//             if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
//                 throw new IllegalArgumentException(
//                         "Quantity must be greater than zero for " + item.getProductName());
//             }
//             try {
//                 item.getQuantity().intValueExact();
//             } catch (ArithmeticException exception) {
//                 throw new IllegalArgumentException(
//                         "Fractional quantities are not supported for " + item.getProductName());
//             }
//             if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
//                 throw new IllegalArgumentException(
//                         "A valid unit price is required for " + item.getProductName());
//             }
//             validateRate(item.getGstRate(), "GST", item.getProductName());
//             validateRate(item.getCessRate(), "cess", item.getProductName());
//         }
//     }

//     private void validateRate(BigDecimal rate, String label, String productName) {
//         if (rate != null && (rate.signum() < 0 || rate.compareTo(new BigDecimal("100")) > 0)) {
//             throw new IllegalArgumentException(label + " rate is invalid for " + productName);
//         }
//     }

//     private void requireCounterpartyAccount(String name) {
//         if (accountRepository.findFirstByNameIgnoreCase(name.trim()).isEmpty()) {
//             throw new IllegalArgumentException(
//                     "Create a ledger account named \"" + name.trim()
//                     + "\" before confirming this transaction.");
//         }
//     }

//     private Product findActiveProduct(Long productId, String invoiceName) {
//         Product product = productRepository.findById(productId)
//                 .orElseThrow(() -> new IllegalArgumentException(
//                         "Selected product was not found: " + invoiceName));
//         if (!(product.isActive() || product.getActive())) {
//             throw new IllegalArgumentException("Selected product is inactive: " + product.getName());
//         }
//         return product;
//     }

//     private java.util.Optional<Product> findProductByName(String name) {
//         return productRepository.findAll().stream()
//                 .filter(product -> product.getName() != null
//                         && product.getName().trim().equalsIgnoreCase(name.trim()))
//                 .findFirst();
//     }

//     private Product createPurchaseProduct(AITransactionItemDTO item) {
//         Product product = new Product();
//         product.setName(item.getProductName().trim());
//         product.setPurchasePrice(item.getUnitPrice());
//         product.setSellingPrice(BigDecimal.ZERO);
//         product.setGstRate(item.getGstRate() == null ? BigDecimal.ZERO : item.getGstRate());
//         product.setCessRate(item.getCessRate() == null ? BigDecimal.ZERO : item.getCessRate());
//         product.setHsnSacCode(blankToNull(item.getHsnSacCode()));
//         product.setOpeningStockQuantity(BigDecimal.ZERO);
//         product.setOpeningStockValue(BigDecimal.ZERO);
//         product.setCurrentStockQuantity(BigDecimal.ZERO);
//         product.setActive(true);
//         return productService.saveProduct(product);
//     }

//     private Product resolvePurchaseProduct(AITransactionItemDTO item) {
//         return findProductByName(item.getProductName())
//                 .map(product -> {
//                     if (!(product.isActive() || product.getActive())) {
//                         throw new IllegalArgumentException(
//                                 "Product already exists but is inactive: " + product.getName());
//                     }
//                     return product;
//                 })
//                 .orElseGet(() -> createPurchaseProduct(item));
//     }

//     private void validateProductTaxRates(AITransactionItemDTO item, Product product) {
//         if (item.getGstRate() != null
//                 && item.getGstRate().compareTo(zeroIfNull(product.getGstRate())) != 0) {
//             throw new IllegalArgumentException(
//                     "GST rate on the invoice does not match product \"" + product.getName()
//                     + "\". Update the product or correct the invoice draft first.");
//         }
//         if (item.getCessRate() != null && item.getCessRate().compareTo(zeroIfNull(product.getCessRate())) != 0) {
//             throw new IllegalArgumentException(
//                     "Cess rate on the invoice does not match product \"" + product.getName()
//                     + "\". Update the product or correct the invoice draft first.");
//         }
//     }

//     boolean applyInvoiceHsnIfProvided(AITransactionItemDTO item, Product product) {
//         String invoiceHsn = blankToNull(item.getHsnSacCode());
//         if (invoiceHsn == null) {
//             return false;
//         }
//         if (!invoiceHsn.matches("\\d{4,8}")) {
//             throw new IllegalArgumentException(
//                     "HSN/SAC code for \"" + item.getProductName() + "\" must contain 4 to 8 digits.");
//         }
//         String productHsn = blankToNull(product.getHsnSacCode());
//         if (productHsn != null && !productHsn.equals(invoiceHsn)) {
//             throw new IllegalArgumentException(
//                     "HSN/SAC code on the invoice does not match product \"" + product.getName()
//                     + "\". Update the product or correct the invoice code before posting.");
//         }
//         if (productHsn == null) {
//             product.setHsnSacCode(invoiceHsn);
//             return true;
//         }
//         return false;
//     }

//     private void requireInvoiceTotal(BigDecimal expected, BigDecimal actual, String type) {
//         BigDecimal calculated = zeroIfNull(actual).setScale(2, RoundingMode.HALF_UP);
//         BigDecimal invoice = expected.setScale(2, RoundingMode.HALF_UP);
//         if (calculated.compareTo(invoice) != 0) {
//             throw new IllegalArgumentException(
//                     "Calculated " + type + " total " + calculated.toPlainString()
//                     + " does not match invoice total " + invoice.toPlainString()
//                     + ". Check quantities, unit prices, tax rates, and discounts. Nothing was posted.");
//         }
//     }

//     private BigDecimal zeroIfNull(BigDecimal value) {
//         return value == null ? BigDecimal.ZERO : value;
//     }

//     private String blankToNull(String value) {
//         return value == null || value.isBlank() ? null : value.trim();
//     }
// }



package com.vpm.Accounts.Service.AI;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;
import com.vpm.Accounts.DTO.AI.AITransactionItemDTO;
import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Entity.Purchase;
import com.vpm.Accounts.Entity.PurchaseItem;
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Service.ProductService;
import com.vpm.Accounts.Service.PurchaseService;
import com.vpm.Accounts.Service.SaleService;

@Service
public class AITransactionPostingService {

    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final SaleService saleService;

    public AITransactionPostingService(
            AccountRepository accountRepository,
            ProductRepository productRepository,
            ProductService productService,
            PurchaseService purchaseService,
            SaleService saleService) {
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.purchaseService = purchaseService;
        this.saleService = saleService;
    }

    @Transactional
    public Object post(AITransactionDraftDTO draft) {
        validateDraft(draft);
        requireCounterpartyAccount(draft.getCounterpartyName());

        String type = draft.getTransactionType().trim().toUpperCase(Locale.ROOT);
        if ("PURCHASE".equals(type)) {
            return postPurchase(draft);
        }
        if ("SALES".equals(type)) {
            return postSale(draft);
        }
        throw new IllegalArgumentException("Transaction type must be PURCHASE or SALES");
    }

    @Transactional
    public List<Object> postBatch(List<AITransactionDraftDTO> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            throw new IllegalArgumentException("At least one transaction is required");
        }
        return drafts.stream().map(this::post).collect(Collectors.toList());
    }

    private Purchase postPurchase(AITransactionDraftDTO draft) {
        Purchase purchase = new Purchase();
        purchase.setSupplierName(draft.getCounterpartyName().trim());
        purchase.setDate(draft.getDate());
        List<PurchaseItem> items = new ArrayList<>();

        for (AITransactionItemDTO item : draft.getItems()) {
            Product product = item.getProductId() == null
                    ? resolvePurchaseProduct(item)
                    : findActiveProduct(item.getProductId(), item.getProductName());
            int quantity = item.getQuantity().intValueExact();
            if (applyInvoiceHsnIfProvided(item, product)) {
                productRepository.save(product);
            }
            validateProductTaxRates(item, product);

            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setProduct(product);
            purchaseItem.setQuantity(quantity);
            purchaseItem.setPrice(item.getUnitPrice());
            purchaseItem.setPurchase(purchase);
            items.add(purchaseItem);
        }
        purchase.setItems(items);

        Purchase saved = purchaseService.savePurchase(purchase);
        requireInvoiceTotal(draft.getInvoiceTotal(), saved.getGrandTotal(), "purchase");
        return saved;
    }

    private Sale postSale(AITransactionDraftDTO draft) {
        Sale sale = new Sale();
        sale.setCustomerName(draft.getCounterpartyName().trim());
        sale.setDate(draft.getDate());
        List<SaleItem> items = new ArrayList<>();

        for (AITransactionItemDTO item : draft.getItems()) {
            if (item.getProductId() == null) {
                throw new IllegalArgumentException(
                        "Sales require an existing product selection for: " + item.getProductName());
            }
            Product product = findActiveProduct(item.getProductId(), item.getProductName());
            if (applyInvoiceHsnIfProvided(item, product)) {
                productRepository.save(product);
            }
            validateProductTaxRates(item, product);

            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setQuantity(item.getQuantity().intValueExact());
            saleItem.setPrice(item.getUnitPrice());
            saleItem.setSale(sale);
            items.add(saleItem);
        }
        sale.setItems(items);

        Sale saved = saleService.saveSaleFromInvoice(sale);
        requireInvoiceTotal(draft.getInvoiceTotal(), saved.getGrandTotal(), "sales");
        return saved;
    }

    private void validateDraft(AITransactionDraftDTO draft) {
        if (draft == null) {
            throw new IllegalArgumentException("Transaction draft is required");
        }
        if (draft.getTransactionType() == null
                || !List.of("PURCHASE", "SALES").contains(
                        draft.getTransactionType().trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Transaction type must be PURCHASE or SALES");
        }
        if (draft.getDate() == null) {
            throw new IllegalArgumentException("Invoice date is required");
        }
        if (draft.getCounterpartyName() == null || draft.getCounterpartyName().isBlank()) {
            throw new IllegalArgumentException("Supplier/customer name is required");
        }
        if (draft.getInvoiceTotal() == null || draft.getInvoiceTotal().signum() <= 0) {
            throw new IllegalArgumentException("Invoice total must be greater than zero");
        }
        if (draft.getItems() == null || draft.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one invoice item is required");
        }

        for (AITransactionItemDTO item : draft.getItems()) {
            if (item == null || item.getProductName() == null || item.getProductName().isBlank()) {
                throw new IllegalArgumentException("Every invoice item needs a product name");
            }
            if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be greater than zero for " + item.getProductName());
            }
            try {
                item.getQuantity().intValueExact();
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException(
                        "Fractional quantities are not supported for " + item.getProductName());
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().signum() < 0) {
                throw new IllegalArgumentException(
                        "A valid unit price is required for " + item.getProductName());
            }
            validateRate(item.getGstRate(), "GST", item.getProductName());
            validateRate(item.getCessRate(), "cess", item.getProductName());
        }
    }

    private void validateRate(BigDecimal rate, String label, String productName) {
        if (rate != null && (rate.signum() < 0 || rate.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException(label + " rate is invalid for " + productName);
        }
    }

    private void requireCounterpartyAccount(String name) {
        if (accountRepository.findFirstByNameIgnoreCase(name.trim()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Create a ledger account named \"" + name.trim()
                    + "\" before confirming this transaction.");
        }
    }

    private Product findActiveProduct(Long productId, String invoiceName) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Selected product was not found: " + invoiceName));
        if (!(product.isActive() || product.getActive())) {
            throw new IllegalArgumentException("Selected product is inactive: " + product.getName());
        }
        return product;
    }

    private java.util.Optional<Product> findProductByName(String name) {
        return productRepository.findAll().stream()
                .filter(product -> product.getName() != null
                        && product.getName().trim().equalsIgnoreCase(name.trim()))
                .findFirst();
    }

    private Product createPurchaseProduct(AITransactionItemDTO item) {
        Product product = new Product();
        product.setName(item.getProductName().trim());
        product.setPurchasePrice(item.getUnitPrice());
        product.setSellingPrice(BigDecimal.ZERO);
        product.setGstRate(item.getGstRate() == null ? BigDecimal.ZERO : item.getGstRate());
        product.setCessRate(item.getCessRate() == null ? BigDecimal.ZERO : item.getCessRate());
        product.setHsnSacCode(blankToNull(item.getHsnSacCode()));
        product.setOpeningStockQuantity(BigDecimal.ZERO);
        product.setOpeningStockValue(BigDecimal.ZERO);
        product.setCurrentStockQuantity(BigDecimal.ZERO);
        product.setActive(true);
        return productService.saveProduct(product);
    }

    private Product resolvePurchaseProduct(AITransactionItemDTO item) {
        return findProductByName(item.getProductName())
                .map(product -> {
                    if (!(product.isActive() || product.getActive())) {
                        throw new IllegalArgumentException(
                                "Product already exists but is inactive: " + product.getName());
                    }
                    return product;
                })
                .orElseGet(() -> createPurchaseProduct(item));
    }

    private void validateProductTaxRates(AITransactionItemDTO item, Product product) {
        if (item.getGstRate() != null
                && item.getGstRate().compareTo(zeroIfNull(product.getGstRate())) != 0) {
            throw new IllegalArgumentException(
                    "GST rate on the invoice does not match product \"" + product.getName()
                    + "\". Update the product or correct the invoice draft first.");
        }
        if (item.getCessRate() != null && item.getCessRate().compareTo(zeroIfNull(product.getCessRate())) != 0) {
            throw new IllegalArgumentException(
                    "Cess rate on the invoice does not match product \"" + product.getName()
                    + "\". Update the product or correct the invoice draft first.");
        }
    }

    boolean applyInvoiceHsnIfProvided(AITransactionItemDTO item, Product product) {
        String invoiceHsn = blankToNull(item.getHsnSacCode());
        if (invoiceHsn == null) {
            return false;
        }
        if (!invoiceHsn.matches("\\d{4,8}")) {
            throw new IllegalArgumentException(
                    "HSN/SAC code for \"" + item.getProductName() + "\" must contain 4 to 8 digits.");
        }
        String productHsn = blankToNull(product.getHsnSacCode());
        if (productHsn != null && !productHsn.equals(invoiceHsn)) {
            throw new IllegalArgumentException(
                    "HSN/SAC code on the invoice does not match product \"" + product.getName()
                    + "\". Update the product or correct the invoice code before posting.");
        }
        if (productHsn == null) {
            product.setHsnSacCode(invoiceHsn);
            return true;
        }
        return false;
    }

    private void requireInvoiceTotal(BigDecimal expected, BigDecimal actual, String type) {
        BigDecimal calculated = zeroIfNull(actual).setScale(2, RoundingMode.HALF_UP);
        BigDecimal invoice = expected.setScale(2, RoundingMode.HALF_UP);
        if (calculated.compareTo(invoice) != 0) {
            throw new IllegalArgumentException(
                    "Calculated " + type + " total " + calculated.toPlainString()
                    + " does not match invoice total " + invoice.toPlainString()
                    + ". Check quantities, unit prices, tax rates, and discounts. Nothing was posted.");
        }
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
