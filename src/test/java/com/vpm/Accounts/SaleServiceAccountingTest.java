package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.AccountingPeriod;
import com.vpm.Accounts.Entity.FinancialYear;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.AccountingPeriodRepository;
import com.vpm.Accounts.Repository.FinancialYearRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.SaleRepository;
import com.vpm.Accounts.Service.SaleService;
import com.vpm.Accounts.security.TenantContext;

@ExtendWith(MockitoExtension.class)
class SaleServiceAccountingTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private FinancialYearRepository financialYearRepository;

    @Mock
    private AccountingPeriodRepository accountingPeriodRepository;

    @InjectMocks
    private SaleService saleService;

    private Sale validSale;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Mouse");
        product.setBarcode("MOUSE-001");
        product.setActive(true);
        product.setSellingPrice(new BigDecimal("250.00"));
        product.setOpeningStock(new BigDecimal("10"));

        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(product.getSellingPrice());

        validSale = new Sale();
        validSale.setVoucherNumber("SV-2026-001");
        validSale.setCustomerName("Cash Customer");
        validSale.setDate(LocalDate.of(2026, 9, 5));
        validSale.setItems(List.of(item));
        validSale.setDiscountEnabled(false);
        validSale.setCommissionEnabled(false);
        validSale.setPartialCashEnabled(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        TenantContext.set(42L);

        FinancialYear financialYear = new FinancialYear();
        financialYear.setId(6L);
        financialYear.setName("2026-27");
        financialYear.setStartDate(LocalDate.of(2026, 4, 1));
        financialYear.setEndDate(LocalDate.of(2027, 3, 31));
        financialYear.setStatus(FinancialYear.Status.OPEN);

        AccountingPeriod accountingPeriod = new AccountingPeriod();
        accountingPeriod.setId(11L);
        accountingPeriod.setName("September 2026");
        accountingPeriod.setStartDate(LocalDate.of(2026, 9, 1));
        accountingPeriod.setEndDate(LocalDate.of(2026, 9, 30));
        accountingPeriod.setStatus(AccountingPeriod.Status.OPEN);
        accountingPeriod.setFinancialYear(financialYear);

        when(financialYearRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        validSale.getDate(), validSale.getDate()))
                .thenReturn(Optional.of(financialYear));
        when(accountingPeriodRepository
                .findFirstByFinancialYear_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        6L, validSale.getDate(), validSale.getDate()))
                .thenReturn(Optional.of(accountingPeriod));
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void shouldRejectDiscountGreaterThanSubtotal() {
        validSale.setDiscountEnabled(true);
        validSale.setDiscountType("PERCENTAGE");
        validSale.setDiscountValue(new BigDecimal("120"));

        assertThrows(IllegalArgumentException.class, () -> saleService.saveSale(validSale));
    }

    @Test
    void shouldPostEnabledAdjustmentsAndPartialCashInBalancedJournal() {
        Account cash = account(2L, "Cash-in-hand", AccountType.ASSET);
        Account sales = account(4L, "sales", AccountType.SALES);
        Account discount = account(7L, "discount", AccountType.EXPENSE);
        Account commission = account(8L, "commission", AccountType.EXPENSE);
        Account customer = account(5L, "sai", AccountType.DEBTORS);

        when(accountRepository.findFirstByName(any())).thenReturn(Optional.empty());
        when(accountRepository.findFirstByNameIgnoreCase(eq("Sales"))).thenReturn(Optional.of(sales));
        when(accountRepository.findFirstByNameIgnoreCase(eq("Cash"))).thenReturn(Optional.empty());
        when(accountRepository.findFirstByNameIgnoreCase(eq("Cash-in-hand"))).thenReturn(Optional.of(cash));
        when(accountRepository.findFirstByNameIgnoreCase(eq("Discount Allowed"))).thenReturn(Optional.empty());
        when(accountRepository.findFirstByNameIgnoreCase(eq("Discount"))).thenReturn(Optional.of(discount));
        when(accountRepository.findFirstByNameIgnoreCase(eq("Commission Expense"))).thenReturn(Optional.empty());
        when(accountRepository.findFirstByNameIgnoreCase(eq("Commission"))).thenReturn(Optional.of(commission));
        when(accountRepository.findFirstByNameIgnoreCase(eq("sai"))).thenReturn(Optional.of(customer));
        when(saleRepository.saveAndFlush(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            if (sale.getId() == null) {
                sale.setId(1L);
            }
            return sale;
        });

        validSale.setCustomerName("sai");
        validSale.setDiscountEnabled(true);
        validSale.setDiscountType("FIXED");
        validSale.setDiscountValue(new BigDecimal("100"));
        validSale.setCommissionEnabled(true);
        validSale.setCommissionType("FIXED");
        validSale.setCommissionValue(new BigDecimal("50"));
        validSale.setPartialCashEnabled(true);
        validSale.setCashPaid(new BigDecimal("25"));

        saleService.saveSale(validSale);

        assertEquals(validSale, validSale.getItems().get(0).getSale());
        assertMoneyEquals("10", validSale.getItems().get(0).getProduct().getOpeningStockQuantity());
        assertMoneyEquals("9", validSale.getItems().get(0).getProduct().getCurrentStockQuantity());

        var journalCaptor = org.mockito.ArgumentCaptor.forClass(JournalEntry.class);
        org.mockito.Mockito.verify(journalEntryRepository).save(journalCaptor.capture());
        JournalEntry journal = journalCaptor.getValue();

        assertMoneyEquals("250.00", journal.getLines().stream()
            .map(line -> line.getCredit()).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertMoneyEquals("250.00", journal.getLines().stream()
            .map(line -> line.getDebit()).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertMoneyEquals("100.00", validSale.getDiscountAmount());
        assertMoneyEquals("50.00", validSale.getCommissionAmount());
        assertMoneyEquals("25.00", validSale.getCashPaid());
        assertMoneyEquals("75.00", validSale.getCreditAmount());
    }

        private void assertMoneyEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
        }

    private Account account(Long id, String name, AccountType type) {
        Account account = new Account();
        account.setId(id);
        account.setName(name);
        account.setType(type);
        return account;
    }
}
