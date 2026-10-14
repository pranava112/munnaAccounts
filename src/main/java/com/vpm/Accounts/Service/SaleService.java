
package com.vpm.Accounts.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.SaleRepository;
import com.vpm.Accounts.Repository.FinancialYearRepository;
import com.vpm.Accounts.Repository.AccountingPeriodRepository;
import com.vpm.Accounts.security.TenantContext;
import com.vpm.Accounts.Service.GST.TaxApiService;
import com.vpm.Accounts.Entity.AccountType;

@Service
public class SaleService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SaleRepository saleRepo;

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

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Account findAccountByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return accountRepo.findFirstByName(name)
                .or(() -> accountRepo.findFirstByNameIgnoreCase(name))
                .orElse(null);
    }

    private Account findAccountByNames(String... names) {
        for (String name : names) {
            Account account = findAccountByName(name);
            if (account != null) {
                return account;
            }
        }
        return null;
    }

    private Account requireAccount(String name) {
        Account account = findAccountByName(name);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + name);
        }
        return account;
    }

    private BigDecimal calculateSubtotal(Sale sale) {
        if (sale == null || sale.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return sale.getItems().stream()
                .map(SaleItem::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscountAmount(Sale sale, BigDecimal subtotal) {
        if (sale == null || !(sale.isDiscountEnabled() || sale.getDiscountEnabled())) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = zeroIfNull(sale.getDiscountValue());
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        String type = sale.getDiscountType() == null ? "PERCENTAGE" : sale.getDiscountType().trim().toUpperCase();
        if ("FIXED".equals(type) || "FIXED AMOUNT".equals(type)) {
            if (value.compareTo(subtotal) > 0) {
                throw new IllegalArgumentException("Discount amount cannot be greater than subtotal");
            }
            return value;
        }
        if (value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Discount percentage cannot exceed 100");
        }
        return subtotal.multiply(value)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCommissionAmount(Sale sale, BigDecimal baseValue) {
        if (sale == null || !(sale.isCommissionEnabled() || sale.getCommissionEnabled())) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = zeroIfNull(sale.getCommissionValue());
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Commission cannot be negative");
        }
        String type = sale.getCommissionType() == null ? "PERCENTAGE" : sale.getCommissionType().trim().toUpperCase();
        if ("FIXED".equals(type) || "FIXED AMOUNT".equals(type)) {
            return value;
        }
        if (value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Commission percentage cannot exceed 100");
        }
        return baseValue.multiply(value)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private void validateAndPrepareSale(Sale sale) {
        validateAndPrepareSale(sale, false);
    }

    private void validateAndPrepareSale(Sale sale, boolean preserveInvoicePrices) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale is required");
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }
        if (sale.getCustomerName() == null || sale.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Customer is required");
        }
        // if (sale.getDate() == null || sale.getDate().isBlank()) {
        //     throw new IllegalArgumentException("Sale date is required");
        // }

        if (sale.getDate() == null) {
            throw new IllegalArgumentException("Sale date is required");
        }

        FinancialYear financialYear = financialYearRepo
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        sale.getDate(), sale.getDate())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No financial year exists for sale date: " + sale.getDate()));
        if (financialYear.isClosed()) {
            throw new IllegalStateException(
                    "Financial year " + financialYear.getName() + " is closed");
        }
        AccountingPeriod accountingPeriod = accountingPeriodRepo
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        financialYear.getId(), sale.getDate(), sale.getDate())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No accounting period exists for sale date: " + sale.getDate()));
        if (accountingPeriod.isClosed()) {
            throw new IllegalStateException(
                    "Accounting period " + accountingPeriod.getName() + " is closed");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleItem item : sale.getItems()) {
            if (item == null || item.getProduct() == null || item.getProduct().getId() == null) {
                throw new IllegalArgumentException("Please select a valid product");
            }
            Product product = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getId()));
            if (!(product.isActive() || product.getActive())) {
                throw new IllegalArgumentException("Product is inactive: " + product.getName());
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product: " + product.getName());
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price cannot be negative for product: " + product.getName());
            }
            BigDecimal currentStock = zeroIfNull(product.getCurrentStockQuantity());
            BigDecimal requested = BigDecimal.valueOf(item.getQuantity());
            if (currentStock.compareTo(requested) < 0) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock for product: " + product.getName());
            }
            item.setProduct(product);
            if (!preserveInvoicePrices || item.getPrice() == null) {
                item.setPrice(product.getSellingPrice() != null
                        ? product.getSellingPrice()
                        : item.getPrice());
            }
            subtotal = subtotal.add(item.getPrice().multiply(requested));
        }

        sale.setSubtotal(subtotal);

        boolean discountEnabled = sale.isDiscountEnabled() || sale.getDiscountEnabled();
        sale.setDiscountEnabled(discountEnabled);
        if (discountEnabled) {
            sale.setDiscountAmount(calculateDiscountAmount(sale, subtotal));
            sale.setDiscountValue(zeroIfNull(sale.getDiscountValue()));
        } else {
            sale.setDiscountAmount(BigDecimal.ZERO);
            sale.setDiscountValue(BigDecimal.ZERO);
        }

        BigDecimal netAmount = subtotal.subtract(zeroIfNull(sale.getDiscountAmount()));
        sale.setNetAmount(netAmount);

        boolean commissionEnabled = sale.isCommissionEnabled() || sale.getCommissionEnabled();
        sale.setCommissionEnabled(commissionEnabled);
        if (commissionEnabled) {
            sale.setCommissionAmount(calculateCommissionAmount(sale, netAmount));
            sale.setCommissionValue(zeroIfNull(sale.getCommissionValue()));
        } else {
            sale.setCommissionAmount(BigDecimal.ZERO);
            sale.setCommissionValue(BigDecimal.ZERO);
        }

        boolean partialCash = sale.isPartialCashEnabled() || sale.getPartialCashEnabled();
        sale.setPartialCashEnabled(partialCash);
        if (partialCash) {
            BigDecimal cashPaid = zeroIfNull(sale.getCashPaid());
            if (cashPaid.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Cash paid cannot be negative");
            }
            BigDecimal commissionAmount = zeroIfNull(sale.getCommissionAmount());
            BigDecimal maximumCollection = netAmount.subtract(commissionAmount);
            if (cashPaid.compareTo(maximumCollection) > 0) {
                throw new IllegalArgumentException("Cash paid cannot exceed the amount due after commission");
            }
            sale.setCashPaid(cashPaid);
            sale.setCreditAmount(maximumCollection.subtract(cashPaid));
        } else {
            sale.setCashPaid(BigDecimal.ZERO);
            sale.setCreditAmount(netAmount.subtract(zeroIfNull(sale.getCommissionAmount())));
        }
    }

    private void restoreStockForSale(Sale sale) {
        if (sale == null || sale.getItems() == null) {
            return;
        }
        for (SaleItem item : sale.getItems()) {
            if (item == null || item.getProduct() == null) {
                continue;
            }
            Product product = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getId()));
            product.setCurrentStockQuantity(zeroIfNull(product.getCurrentStockQuantity()).add(BigDecimal.valueOf(item.getQuantity())));
            productRepo.save(product);
        }
    }

    private void reduceStockForSale(Sale sale) {
        if (sale == null || sale.getItems() == null) {
            return;
        }
        for (SaleItem item : sale.getItems()) {
            if (item == null || item.getProduct() == null) {
                continue;
            }
            Product product = productRepo.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getId()));
            BigDecimal currentStock = zeroIfNull(product.getCurrentStockQuantity());
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            if (currentStock.compareTo(quantity) < 0) {
                throw new IllegalArgumentException("Requested quantity exceeds available stock for product: " + product.getName());
            }
            product.setCurrentStockQuantity(currentStock.subtract(quantity));
            productRepo.save(product);
        }
    }

    private void buildJournalEntry(Sale sale) {
        Account salesAcc = requireAccount("Sales");
        Account cashAcc = findAccountByNames("Cash", "Cash-in-hand", "Cash In Hand");

        JournalEntry journal = new JournalEntry();
        journal.setVoucherNumber(sale.getVoucherNumber());
        journal.setEntryDate(sale.getDate());
        FinancialYear financialYear = financialYearRepo
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        sale.getDate(), sale.getDate())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No financial year exists for sale date: " + sale.getDate()));
        journal.setFinancialYear(financialYear);
        accountingPeriodRepo
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        financialYear.getId(), sale.getDate(), sale.getDate())
                .ifPresent(journal::setAccountingPeriod);
        journal.setDescription("Sales Entry - " + sale.getCustomerName());

        List<JournalEntryLine> lines = new ArrayList<>();
        boolean gstEnabledForSale = zeroIfNull(sale.getGstTaxableAmount()).signum() > 0
                || zeroIfNull(sale.getGstTotal()).signum() > 0;

        if (sale.isPartialCashEnabled() || sale.getPartialCashEnabled()) {
            BigDecimal cashPaid = zeroIfNull(sale.getCashPaid());
            if (cashPaid.compareTo(BigDecimal.ZERO) > 0) {
                if (cashAcc == null) {
                    throw new IllegalArgumentException("Cash account not found");
                }
                JournalEntryLine cashLine = new JournalEntryLine();
                cashLine.setAccount(cashAcc);
                cashLine.setDebit(cashPaid);
                cashLine.setCredit(BigDecimal.ZERO);
                cashLine.setJournalEntry(journal);
                lines.add(cashLine);
            }

            BigDecimal creditAmount = zeroIfNull(sale.getCreditAmount());
            if (creditAmount.compareTo(BigDecimal.ZERO) > 0) {
                Account debtorAccount = findAccountByName(sale.getCustomerName());
                if (debtorAccount == null) {
                    debtorAccount = cashAcc;
                }
                if (debtorAccount == null) {
                    throw new IllegalArgumentException("Customer or Cash account not found");
                }
                JournalEntryLine customerLine = new JournalEntryLine();
                customerLine.setAccount(debtorAccount);
                customerLine.setDebit(creditAmount);
                customerLine.setCredit(BigDecimal.ZERO);
                customerLine.setJournalEntry(journal);
                lines.add(customerLine);
            }
        } else {
            BigDecimal customerDr = gstEnabledForSale
                    ? zeroIfNull(sale.getGrandTotal()).subtract(zeroIfNull(sale.getCommissionAmount()))
                    : zeroIfNull(sale.getNetAmount());
            if (customerDr.compareTo(BigDecimal.ZERO) > 0) {
                Account debtorAccount = findAccountByName(sale.getCustomerName());
                if (debtorAccount == null) {
                    debtorAccount = cashAcc;
                }
                if (debtorAccount == null) {
                    throw new IllegalArgumentException("Customer or Cash account not found");
                }
                JournalEntryLine customerLine = new JournalEntryLine();
                customerLine.setAccount(debtorAccount);
                customerLine.setDebit(customerDr);
                customerLine.setCredit(BigDecimal.ZERO);
                customerLine.setJournalEntry(journal);
                lines.add(customerLine);
            }
        }

        if (!gstEnabledForSale && (sale.isDiscountEnabled() || sale.getDiscountEnabled())) {
            BigDecimal discount = zeroIfNull(sale.getDiscountAmount());
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                Account discountAcc = findAccountByNames("Discount Allowed", "Discount", "discount");
                if (discountAcc == null) {
                    throw new IllegalArgumentException("Account not found: Discount");
                }
                JournalEntryLine discountLine = new JournalEntryLine();
                discountLine.setAccount(discountAcc);
                discountLine.setDebit(discount);
                discountLine.setCredit(BigDecimal.ZERO);
                discountLine.setJournalEntry(journal);
                lines.add(discountLine);
            }
        }

        if (sale.isCommissionEnabled() || sale.getCommissionEnabled()) {
            BigDecimal commission = zeroIfNull(sale.getCommissionAmount());
            if (commission.compareTo(BigDecimal.ZERO) > 0) {
                Account commissionExpenseAcc = findAccountByNames("Commission Expense", "Commission", "commission");
                if (commissionExpenseAcc == null) {
                    throw new IllegalArgumentException("Account not found: Commission");
                }
                JournalEntryLine commissionLine = new JournalEntryLine();
                commissionLine.setAccount(commissionExpenseAcc);
                commissionLine.setDebit(commission);
                commissionLine.setCredit(BigDecimal.ZERO);
                commissionLine.setJournalEntry(journal);
                lines.add(commissionLine);
            }
        }

        JournalEntryLine salesLine = new JournalEntryLine();
        salesLine.setAccount(salesAcc);
        salesLine.setDebit(BigDecimal.ZERO);
        salesLine.setCredit(gstEnabledForSale
                ? zeroIfNull(sale.getGstTaxableAmount())
                : zeroIfNull(sale.getSubtotal()));
        salesLine.setJournalEntry(journal);
        lines.add(salesLine);

        if (gstEnabledForSale) {
            addTaxCredit(lines, journal, "Output CGST", zeroIfNull(sale.getGstCgst()));
            addTaxCredit(lines, journal, "Output SGST", zeroIfNull(sale.getGstSgst()));
            addTaxCredit(lines, journal, "Output IGST", zeroIfNull(sale.getGstIgst()));
            addTaxCredit(lines, journal, "Output GST Cess", zeroIfNull(sale.getGstCess()));
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (JournalEntryLine line : lines) {
            totalDebit = totalDebit.add(zeroIfNull(line.getDebit()));
            totalCredit = totalCredit.add(zeroIfNull(line.getCredit()));
        }
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException("Unbalanced journal entry for sale: debit=" + totalDebit + ", credit=" + totalCredit);
        }

        journal.setLines(lines);
        journalRepo.save(journal);
    }

    private void addTaxCredit(List<JournalEntryLine> lines, JournalEntry journal,
            String accountName, BigDecimal amount) {
        if (amount.signum() <= 0) return;
        Account taxAccount = accountRepo.findFirstByNameIgnoreCase(accountName).orElseGet(() -> {
            Account created = new Account();
            created.setName(accountName);
            created.setType(AccountType.LIABILITY);
            created.setAccountGroup("DUTIES_TAXES");
            created.setBalance(BigDecimal.ZERO);
            return accountRepo.save(created);
        });
        JournalEntryLine line = new JournalEntryLine();
        line.setAccount(taxAccount);
        line.setDebit(BigDecimal.ZERO);
        line.setCredit(amount);
        line.setJournalEntry(journal);
        lines.add(line);
    }

    @Transactional
    public Sale saveSale(Sale sale) {
        return persistSale(sale, false);
    }

    @Transactional
    public Sale saveSaleFromInvoice(Sale sale) {
        return persistSale(sale, true);
    }

    private Sale persistSale(Sale sale, boolean preserveInvoicePrices) {
        validateAndPrepareSale(sale, preserveInvoicePrices);
        for (SaleItem item : sale.getItems()) {
            item.setSale(sale);
        }
        Long tenantId = TenantContext.require();
        int year = sale.getDate().getYear();
        sale.setVoucherNumber(null);
        reduceStockForSale(sale);
        Sale savedSale = saleRepo.saveAndFlush(sale);
        savedSale.setVoucherNumber(String.format(
                "SV-%d-%d-%06d", tenantId, year, savedSale.getId()));
        savedSale = saleRepo.saveAndFlush(savedSale);
        taxApiService.syncSaleTax(savedSale);
        savedSale = saleRepo.saveAndFlush(savedSale);
        buildJournalEntry(savedSale);
        return savedSale;
    }

    @Async("accountExecutor")
    public CompletableFuture<Sale> saveSaleAsync(Sale sale) {
        return CompletableFuture.completedFuture(
                transactionTemplate.execute(status -> saveSale(sale)));
    }

    public List<Sale> getAll() {
        return saleRepo.findAll();
    }

    @Async("accountExecutor")
    public CompletableFuture<List<Sale>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    public Sale getSaleById(@NonNull Long id) {
        return saleRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Async("accountExecutor")
    public CompletableFuture<Sale> getSaleByIdAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(getSaleById(id));
    }

    @Transactional
    public Sale updateSale(@NonNull Long id, Sale newSale) {
        Sale existing = getSaleById(id);
        if (existing.getVoucherNumber() != null) {
            deleteJournalByVoucherNumber(existing.getVoucherNumber());
        }
        restoreStockForSale(existing);

        validateAndPrepareSale(newSale);
        newSale.setVoucherNumber(String.format(
                "SV-%d-%d-%06d",
                TenantContext.require(),
                newSale.getDate().getYear(),
                existing.getId()));
        reduceStockForSale(newSale);

        existing.setCustomerName(newSale.getCustomerName());
        existing.setDate(newSale.getDate());
        existing.setPhoneNo(newSale.getPhoneNo());
        existing.setVoucherNumber(newSale.getVoucherNumber());
        existing.setSubtotal(newSale.getSubtotal());
        existing.setDiscountEnabled(newSale.isDiscountEnabled() || newSale.getDiscountEnabled());
        existing.setDiscountType(newSale.getDiscountType());
        existing.setDiscountValue(newSale.getDiscountValue());
        existing.setDiscountAmount(newSale.getDiscountAmount());
        existing.setNetAmount(newSale.getNetAmount());
        existing.setCommissionEnabled(newSale.isCommissionEnabled() || newSale.getCommissionEnabled());
        existing.setCommissionType(newSale.getCommissionType());
        existing.setCommissionValue(newSale.getCommissionValue());
        existing.setCommissionAmount(newSale.getCommissionAmount());
        existing.setPartialCashEnabled(newSale.isPartialCashEnabled() || newSale.getPartialCashEnabled());
        existing.setCashPaid(newSale.getCashPaid());
        existing.setCreditAmount(newSale.getCreditAmount());

        List<SaleItem> updatedItems = new ArrayList<>();
        for (SaleItem item : newSale.getItems()) {
            item.setSale(existing);
            updatedItems.add(item);
        }
        existing.setItems(updatedItems);

        Sale updatedSale = saleRepo.save(existing);
        taxApiService.syncSaleTax(updatedSale);
        updatedSale = saleRepo.save(updatedSale);
        buildJournalEntry(updatedSale);
        return updatedSale;
    }

    @Async("accountExecutor")
    public CompletableFuture<Sale> updateSaleAsync(@NonNull Long id, Sale newSale) {
        return CompletableFuture.completedFuture(updateSale(id, newSale));
    }

    // @Transactional
    // public String delete(@NonNull Long id) {
    //     Sale sale = saleRepo.findById(id).orElse(null);
    //     if (sale == null) {
    //         return "Sale already deleted";
    //     }

    //     List<Long> affectedProductIds = new ArrayList<>();
    //     if (sale.getItems() != null) {
    //         for (SaleItem item : sale.getItems()) {
    //             if (item != null && item.getProduct() != null) {
    //                 Long productId = item.getProduct().getId();
    //                 if (productId != null && !affectedProductIds.contains(productId)) {
    //                     affectedProductIds.add(productId);
    //                 }
    //             }
    //         }
    //     }

    //     if (sale.getVoucherNumber() != null) {
    //         deleteJournalByVoucherNumber(sale.getVoucherNumber());
    //     }
    //     taxApiService.cancelSourceInvoice("SALE", sale.getId());
    //     saleRepo.delete(sale);
    //     saleRepo.flush();
    //     affectedProductIds.forEach(productService::recalculateCurrentStockQuantity);
    //     return "Sale deleted successfully";
    // }

    
@Transactional
public String delete(@NonNull Long id) {
    Sale sale = saleRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Sale not found with ID: " + id));

    List<Long> affectedProductIds = new ArrayList<>();

    if (sale.getItems() != null) {
        for (SaleItem item : sale.getItems()) {
            if (item == null || item.getProduct() == null) {
                continue;
            }

            Long productId = item.getProduct().getId();

            if (productId != null && !affectedProductIds.contains(productId)) {
                affectedProductIds.add(productId);
            }
        }
    }

    // Remove the journal entry linked to this sale.
    deleteJournalByVoucherNumber(sale.getVoucherNumber());

    // Cancel the associated tax record.
    taxApiService.cancelSourceInvoice("SALE", sale.getId());

    // Delete the sale and its items.
    saleRepo.delete(sale);
    saleRepo.flush();

    // Recalculate stock based on remaining transactions.
    affectedProductIds.forEach(
            productService::recalculateCurrentStockQuantity
    );

    return "Sale and linked journal entry deleted successfully";
}

/**
 * Called by JournalEntryService when the user deletes
 * a journal entry belonging to a sale.
 */
@Transactional
public boolean deleteByVoucherNumber(String voucherNumber) {
    if (voucherNumber == null || voucherNumber.isBlank()) {
        return false;
    }

    Sale sale = saleRepo.findFirstByVoucherNumber(voucherNumber)
            .orElse(null);

    if (sale == null) {
        return false;
    }

    delete(sale.getId());
    return true;
}


    @Async("accountExecutor")
    @Transactional
    public CompletableFuture<String> deleteAsync(@NonNull Long id) {
        return CompletableFuture.completedFuture(delete(id));
    }

    private void deleteJournalByVoucherNumber(String voucherNumber) {
        if (voucherNumber == null || voucherNumber.isBlank()) {
            return;
        }
        journalRepo.deleteByVoucherNumber(voucherNumber);
        journalRepo.flush();
    }
}