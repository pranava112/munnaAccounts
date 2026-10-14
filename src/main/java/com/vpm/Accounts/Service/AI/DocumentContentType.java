package com.vpm.Accounts.Service.AI;

import java.util.Locale;

import org.springframework.http.MediaType;

final class DocumentContentType {

    private DocumentContentType() {
    }

    static String resolve(String contentType, String filename) {
        String normalizedType = contentType == null
                ? ""
                : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);

        if (MediaType.APPLICATION_PDF_VALUE.equals(normalizedType)
                || MediaType.IMAGE_JPEG_VALUE.equals(normalizedType)
                || MediaType.IMAGE_PNG_VALUE.equals(normalizedType)
                || "image/webp".equals(normalizedType)) {
            return normalizedType;
        }

        if (!normalizedType.isEmpty()
                && !"application/octet-stream".equals(normalizedType)) {
            return normalizedType;
        }

        if (filename == null) {
            return normalizedType;
        }

        String normalizedFilename = filename.toLowerCase(Locale.ROOT);
        if (normalizedFilename.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        }
        if (normalizedFilename.endsWith(".jpg")
                || normalizedFilename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        if (normalizedFilename.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (normalizedFilename.endsWith(".webp")) {
            return "image/webp";
        }

        return normalizedType;
    }
}
