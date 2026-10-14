package com.vpm.Accounts.Service.AI;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class DocumentContentTypeTest {

    @Test
    void resolvesPdfFromGenericOrMissingContentType() {
        assertEquals(
                MediaType.APPLICATION_PDF_VALUE,
                DocumentContentType.resolve("application/octet-stream", "invoice.PDF"));
        assertEquals(
                MediaType.APPLICATION_PDF_VALUE,
                DocumentContentType.resolve(null, "invoice.pdf"));
    }

    @Test
    void prefersSupportedDeclaredContentType() {
        assertEquals(
                MediaType.APPLICATION_PDF_VALUE,
                DocumentContentType.resolve("application/pdf; charset=binary", "invoice.bin"));
    }

    @Test
    void resolvesSupportedImageExtensions() {
        assertEquals(
                MediaType.IMAGE_JPEG_VALUE,
                DocumentContentType.resolve("application/octet-stream", "receipt.jpeg"));
        assertEquals(
                MediaType.IMAGE_PNG_VALUE,
                DocumentContentType.resolve(null, "receipt.png"));
        assertEquals(
                "image/webp",
                DocumentContentType.resolve(null, "receipt.webp"));
    }
}
