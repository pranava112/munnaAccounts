package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.SaleRepository;
import com.vpm.Accounts.Service.SaleService;

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
        validSale.setDate("2026-09-05");
        validSale.setItems(List.of(item));
        validSale.setDiscountEnabled(false);
        validSale.setCommissionEnabled(false);
        validSale.setPartialCashEnabled(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
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
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
