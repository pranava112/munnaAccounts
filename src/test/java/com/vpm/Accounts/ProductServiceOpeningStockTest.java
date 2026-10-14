package com.vpm.Accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.vpm.Accounts.Entity.Account;
import com.vpm.Accounts.Entity.AccountType;
import com.vpm.Accounts.Entity.JournalEntry;
import com.vpm.Accounts.Entity.Product;
import com.vpm.Accounts.Entity.Purchase;
import com.vpm.Accounts.Entity.PurchaseItem;
import com.vpm.Accounts.Entity.Sale;
import com.vpm.Accounts.Entity.SaleItem;
import com.vpm.Accounts.Repository.AccountRepository;
import com.vpm.Accounts.Repository.JournalEntryRepository;
import com.vpm.Accounts.Repository.ProductRepository;
import com.vpm.Accounts.Repository.PurchaseRepository;
import com.vpm.Accounts.Repository.SaleRepository;
import com.vpm.Accounts.Service.ProductService;
import com.vpm.Accounts.security.TenantContext;

@ExtendWith(MockitoExtension.class)
class ProductServiceOpeningStockTest {

    @TempDir
    Path barcodeDirectory;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setTenant() {
        TenantContext.set(42L);
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void postsOpeningStockAgainstCapitalWhenProductStartsWithStock() {
        Product product = new Product();
        product.setId(12L);
        product.setName("Telugu book");
        product.setPurchasePrice(new BigDecimal("65"));
        product.setSellingPrice(new BigDecimal("90"));
        product.setOpeningStock(new BigDecimal("200"));

        Account openingStock = account("Opening Stock", AccountType.OPENING_STOCK);
        Account capital = account("Capital", AccountType.CAPITAL);

        when(productRepository.findByBarcode(any())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.findFirstByType(AccountType.OPENING_STOCK)).thenReturn(Optional.of(openingStock));
        when(accountRepository.findFirstByType(AccountType.CAPITAL)).thenReturn(Optional.of(capital));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product saved = productService.saveProduct(product);

        assertEquals(0, new BigDecimal("13000.00").compareTo(saved.getOpeningStockValue()));
        assertEquals(LocalDate.now(), saved.getOpeningStockDate());

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        JournalEntry journal = captor.getValue();

        assertEquals("OB-STOCK-42-12", journal.getVoucherNumber());
        assertEquals(saved.getOpeningStockDate(), journal.getEntryDate());
        assertEquals(2, journal.getLines().size());
        assertEquals(openingStock, journal.getLines().get(0).getAccount());
        assertEquals(new BigDecimal("13000"), journal.getLines().get(0).getDebit());
        assertEquals(capital, journal.getLines().get(1).getAccount());
        assertEquals(new BigDecimal("13000"), journal.getLines().get(1).getCredit());
    }

    @Test
    void deletingUnusedProductAlsoDeletesItsOpeningStockJournal() {
        Product product = new Product();
        product.setId(12L);
        JournalEntry openingStockJournal = new JournalEntry();

        when(productRepository.findById(12L)).thenReturn(Optional.of(product));
        when(journalEntryRepository.findByVoucherNumber("OB-STOCK-42-12"))
                .thenReturn(Optional.of(openingStockJournal));

        productService.deleteById(12L);

        verify(journalEntryRepository).delete(openingStockJournal);
        verify(productRepository).delete(product);
    }

    @Test
    void recalculatingStockUsesOpeningStockAndRemainingTransactions() {
        Product product = new Product();
        product.setId(12L);
        product.setName("Hindi book");
        product.setOpeningStockQuantity(new BigDecimal("10"));
        product.setCurrentStockQuantity(new BigDecimal("100"));

        PurchaseItem purchaseItem = new PurchaseItem();
        purchaseItem.setProduct(product);
        purchaseItem.setQuantity(90);
        Purchase purchase = new Purchase();
        purchase.setItems(java.util.List.of(purchaseItem));

        SaleItem saleItem = new SaleItem();
        saleItem.setProduct(product);
        saleItem.setQuantity(10);
        Sale sale = new Sale();
        sale.setItems(java.util.List.of(saleItem));

        when(productRepository.findById(12L)).thenReturn(Optional.of(product));
        when(purchaseRepository.findDistinctByItems_Product_Id(12L))
                .thenReturn(java.util.List.of(purchase));
        when(saleRepository.findDistinctByItems_Product_Id(12L))
                .thenReturn(java.util.List.of(sale));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.recalculateCurrentStockQuantity(12L);

        assertEquals(0, new BigDecimal("90").compareTo(product.getCurrentStockQuantity()));
        verify(productRepository).save(product);
    }

    @Test
    void recalculatingStockAfterAllTransactionsAreDeletedRestoresOpeningQuantity() {
        Product product = new Product();
        product.setId(12L);
        product.setName("Hindi book");
        product.setOpeningStockQuantity(new BigDecimal("10"));
        product.setCurrentStockQuantity(new BigDecimal("100"));
        product.setPurchasePrice(new BigDecimal("50"));

        when(productRepository.findById(12L)).thenReturn(Optional.of(product));
        when(purchaseRepository.findDistinctByItems_Product_Id(12L))
                .thenReturn(java.util.List.of());
        when(saleRepository.findDistinctByItems_Product_Id(12L))
                .thenReturn(java.util.List.of());
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.recalculateCurrentStockQuantity(12L);

        assertEquals(0, new BigDecimal("10").compareTo(product.getCurrentStockQuantity()));
        assertEquals(
                0,
                new BigDecimal("500").compareTo(
                        product.getCurrentStockQuantity().multiply(product.getPurchasePrice())));
    }

    @Test
    void readsBarcodeImageFromConfiguredDirectory() throws Exception {
        Product product = new Product();
        product.setBarcode("716071");
        byte[] imageBytes = new byte[] { 1, 2, 3 };
        Files.write(barcodeDirectory.resolve("716071.png"), imageBytes);
        ReflectionTestUtils.setField(
                productService,
                "barcodeDirectory",
                barcodeDirectory.toString());
        when(productRepository.findByBarcode("716071"))
                .thenReturn(Optional.of(product));

        assertArrayEquals(imageBytes, productService.getBarcodeImage("716071"));
    }

    @Test
    void deletingProductUsedBySalesOrPurchasesIsRejected() {
        Product product = new Product();
        product.setId(12L);

        when(productRepository.findById(12L)).thenReturn(Optional.of(product));
        when(saleRepository.existsByItems_Product_Id(12L)).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productService.deleteById(12L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(journalEntryRepository, never()).delete(any(JournalEntry.class));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deletingProductUsedByPurchasesIsRejected() {
        Product product = new Product();
        product.setId(12L);

        when(productRepository.findById(12L)).thenReturn(Optional.of(product));
        when(purchaseRepository.existsByItems_Product_Id(12L)).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productService.deleteById(12L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(journalEntryRepository, never()).delete(any(JournalEntry.class));
        verify(productRepository, never()).delete(any(Product.class));
    }

    private Account account(String name, AccountType type) {
        Account account = new Account();
        account.setName(name);
        account.setType(type);
        return account;
    }
}
