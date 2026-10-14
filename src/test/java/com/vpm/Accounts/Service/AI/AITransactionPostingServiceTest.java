package com.vpm.Accounts.Service.AI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;
import com.vpm.Accounts.DTO.AI.AITransactionItemDTO;
import com.vpm.Accounts.Entity.Product;

class AITransactionPostingServiceTest {

    private final AITransactionPostingService service =
            new AITransactionPostingService(null, null, null, null, null);

    @Test
    void rejectsUnsupportedTransactionTypeBeforePosting() {
        AITransactionDraftDTO draft = validDraft();
        draft.setTransactionType("JOURNAL");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.post(draft));

        assertEquals("Transaction type must be PURCHASE or SALES", exception.getMessage());
    }

    @Test
    void rejectsFractionalQuantityBeforePosting() {
        AITransactionDraftDTO draft = validDraft();
        draft.getItems().get(0).setQuantity(new BigDecimal("1.5"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.post(draft));

        assertEquals(
                "Fractional quantities are not supported for Test product",
                exception.getMessage());
    }

    @Test
    void rejectsMissingInvoiceTotalBeforePosting() {
        AITransactionDraftDTO draft = validDraft();
        draft.setInvoiceTotal(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.post(draft));

        assertEquals("Invoice total must be greater than zero", exception.getMessage());
    }

    @Test
    void rejectsEmptyTransactionBatch() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.postBatch(List.of()));

        assertEquals("At least one transaction is required", exception.getMessage());
    }

    @Test
    void appliesInvoiceHsnToExistingProductWhenCatalogCodeIsMissing() {
        AITransactionItemDTO item = new AITransactionItemDTO();
        item.setProductName("Test product");
        item.setHsnSacCode("490199");
        Product product = new Product();
        product.setName("Test product");

        boolean updated = service.applyInvoiceHsnIfProvided(item, product);

        assertTrue(updated);
        assertEquals("490199", product.getHsnSacCode());
    }

    @Test
    void rejectsInvalidInvoiceHsnBeforePosting() {
        AITransactionItemDTO item = new AITransactionItemDTO();
        item.setProductName("Test product");
        item.setHsnSacCode("BOOKS");
        Product product = new Product();
        product.setName("Test product");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.applyInvoiceHsnIfProvided(item, product));

        assertEquals("HSN/SAC code for \"Test product\" must contain 4 to 8 digits.", exception.getMessage());
    }

    private AITransactionDraftDTO validDraft() {
        AITransactionDraftDTO draft = new AITransactionDraftDTO();
        draft.setTransactionType("PURCHASE");
        draft.setDate(LocalDate.of(2026, 10, 14));
        draft.setCounterpartyName("Test supplier");
        draft.setInvoiceTotal(new BigDecimal("100.00"));

        AITransactionItemDTO item = new AITransactionItemDTO();
        item.setProductName("Test product");
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPrice(new BigDecimal("100.00"));
        draft.setItems(List.of(item));
        return draft;
    }
}
