
// package com.vpm.Accounts.Service.AI;

// import java.io.IOException;
// import java.math.BigDecimal;
// import java.util.Base64;
// import java.util.List;
// import java.util.Map;

// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.text.PDFTextStripper;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestClient;
// import org.springframework.web.multipart.MultipartFile;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.vpm.Accounts.DTO.AI.AIAccountDTO;
// import com.vpm.Accounts.DTO.AI.AIProductDTO;
// import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;

// @Service
// public class OllamaAIService {

//     private final RestClient ollamaClient;
//     private final ObjectMapper objectMapper;
//     private final String model;

//     public OllamaAIService(
//             RestClient.Builder builder,
//             ObjectMapper objectMapper,
//             @Value("${ollama.base-url:http://localhost:11434}")
//             String baseUrl,
//             @Value("${ollama.model:qwen2.5vl:3b}")
//             String model) {

//         this.ollamaClient = builder.baseUrl(baseUrl).build();
//         this.objectMapper = objectMapper;
//         this.model = model;
//     }

//     public AITransactionDraftDTO scanDocument(
//             MultipartFile file,
//             List<AIAccountDTO> accounts,
//             List<AIProductDTO> products) throws IOException {

//         if (file == null || file.isEmpty()) {
//             throw new IllegalArgumentException(
//                     "Please upload a document.");
//         }

//         String type = DocumentContentType.resolve(
//                 file.getContentType(),
//                 file.getOriginalFilename());

//         String documentText = "";
//         String imageBase64 = null;

//         if (MediaType.APPLICATION_PDF_VALUE.equals(type)) {
//             try (PDDocument pdf =
//                     PDDocument.load(file.getInputStream())) {

//                 documentText = new PDFTextStripper().getText(pdf);
//             }

//             if (documentText == null || documentText.isBlank()) {
//                 throw new IllegalArgumentException(
//                     "This PDF has no extractable text. " +
//                     "Use OCR for scanned PDFs before sending to Ollama.");
//             }

//         } else if (type.equals("image/jpeg")
//                 || type.equals("image/png")
//                 || type.equals("image/webp")) {

//             imageBase64 = Base64.getEncoder()
//                     .encodeToString(file.getBytes());

//         } else {
//             throw new IllegalArgumentException(
//                     "Supported formats: PDF, JPG, PNG and WEBP.");
//         }

//         String accountJson = objectMapper.writeValueAsString(accounts);
//         String productJson = objectMapper.writeValueAsString(products);

//         String prompt = """
//             Extract a source PURCHASE or SALES invoice; do not create journal
//             lines. The application will create the real transaction and its
//             linked journal only after the user reviews and confirms.
//             Extract the document date, supplier/customer name, invoice grand
//             total, and every invoice item. Quantities must be exact; never
//             round fractional quantities. Copy the exact per-unit price shown;
//             do not remove or add tax or discount. Return null
//             GST/cess rates and an empty HSN/SAC code when these are not
//             explicitly stated on the invoice.
//             Match a product only to the exact id from this existing active
//             product list. For a purchase, an unmatched product may use null
//             productId and will be created with zero selling price. For a sale,
//             only existing active products can be selected; do not invent stock
//             or product IDs. The invoice supplier/customer must match an
//             existing account by name. Do not infer missing values. Add warnings
//             for unclear values, discounts, or totals that do not reconcile.
//             A paid marker does not record cash settlement; payments/receipts
//             are entered separately.

//             Existing accounts:
//             %s

//             Existing active products:
//             %s

//             Document text, if provided:
//             %s

//             Return only this JSON structure:
//             {
//               "transactionType": "PURCHASE",
//               "date": "2026-10-05",
//               "counterpartyName": "Supplier name from invoice",
//               "invoiceTotal": 9440.00,
//               "confidence": "MEDIUM",
//               "warnings": [],
//               "items": [
//                 {
//                   "productName": "A4 paper",
//                   "productId": null,
//                   "quantity": 10,
//                   "unitPrice": 800.00,
//                   "gstRate": 18,
//                   "cessRate": 0,
//                   "hsnSacCode": "480256"
//                 }
//               ]
//             }
//             """.formatted(
//                 accountJson,
//                 productJson,
//                 documentText == null ? "" : documentText);

//         Map<String, Object> userMessage;

//         if (imageBase64 != null) {
//             userMessage = Map.of(
//                     "role", "user",
//                     "content", prompt,
//                     "images", List.of(imageBase64));
//         } else {
//             userMessage = Map.of(
//                     "role", "user",
//                     "content", prompt);
//         }

//         Map<String, Object> request = Map.of(
//                 "model", model,
//                 "stream", false,
//                 "format", "json",
//                 "messages", List.of(userMessage),
//                 "options", Map.of("temperature", 0));

//         String response = ollamaClient.post()
//                 .uri("/api/chat")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .body(request)
//                 .retrieve()
//                 .body(String.class);

//         JsonNode root = objectMapper.readTree(response);
//         String output = root.path("message")
//                 .path("content")
//                 .asText();

//         if (output == null || output.isBlank()) {
//             throw new IllegalStateException(
//                     "Ollama returned an empty response.");
//         }

//         return objectMapper.readValue(
//                 output, AITransactionDraftDTO.class);
//     }
// }




package com.vpm.Accounts.Service.AI;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.Accounts.DTO.AI.AIAccountDTO;
import com.vpm.Accounts.DTO.AI.AIProductDTO;
import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;

@Service
public class OllamaAIService {

    private final RestClient ollamaClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OllamaAIService(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${ollama.base-url:http://localhost:11434}")
            String baseUrl,
            @Value("${ollama.model:qwen2.5vl:3b}")
            String model) {

        this.ollamaClient = builder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public AITransactionDraftDTO scanDocument(
            MultipartFile file,
            List<AIAccountDTO> accounts,
            List<AIProductDTO> products) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a document.");
        }

        String type = DocumentContentType.resolve(
                file.getContentType(),
                file.getOriginalFilename());

        String documentText = "";
        String imageBase64 = null;

        if (MediaType.APPLICATION_PDF_VALUE.equals(type)) {
            try (PDDocument pdf =
                    PDDocument.load(file.getInputStream())) {

                documentText = new PDFTextStripper().getText(pdf);
            }

            if (documentText == null || documentText.isBlank()) {
                throw new IllegalArgumentException(
                    "This PDF has no extractable text. " +
                    "Use OCR for scanned PDFs before sending to Ollama.");
            }

        } else if (type.equals("image/jpeg")
                || type.equals("image/png")
                || type.equals("image/webp")) {

            imageBase64 = Base64.getEncoder()
                    .encodeToString(file.getBytes());

        } else {
            throw new IllegalArgumentException(
                    "Supported formats: PDF, JPG, PNG and WEBP.");
        }

        String accountJson = objectMapper.writeValueAsString(accounts);
        String productJson = objectMapper.writeValueAsString(products);

        String prompt = """
            Extract a source PURCHASE or SALES invoice; do not create journal
            lines. The application will create the real transaction and its
            linked journal only after the user reviews and confirms.
            Extract the document date, supplier/customer name, invoice grand
            total, and every invoice item. Quantities must be exact; never
            round fractional quantities. Copy the exact per-unit price shown;
            do not remove or add tax or discount. Return null
            GST/cess rates and an empty HSN/SAC code when these are not
            explicitly stated on the invoice.
            Match a product only to the exact id from this existing active
            product list. For a purchase, an unmatched product may use null
            productId and will be created with zero selling price. For a sale,
            only existing active products can be selected; do not invent stock
            or product IDs. The invoice supplier/customer must match an
            existing account by name. Do not infer missing values. Add warnings
            for unclear values, discounts, or totals that do not reconcile.
            A paid marker does not record cash settlement; payments/receipts
            are entered separately.

            Existing accounts:
            %s

            Existing active products:
            %s

            Document text, if provided:
            %s

            Return only this JSON structure:
            {
              "transactionType": "PURCHASE",
              "date": "2026-10-05",
              "counterpartyName": "Supplier name from invoice",
              "invoiceTotal": 9440.00,
              "confidence": "MEDIUM",
              "warnings": [],
              "items": [
                {
                  "productName": "A4 paper",
                  "productId": null,
                  "quantity": 10,
                  "unitPrice": 800.00,
                  "gstRate": 18,
                  "cessRate": 0,
                  "hsnSacCode": "480256"
                }
              ]
            }
            """.formatted(
                accountJson,
                productJson,
                documentText == null ? "" : documentText);

        Map<String, Object> userMessage;

        if (imageBase64 != null) {
            userMessage = Map.of(
                    "role", "user",
                    "content", prompt,
                    "images", List.of(imageBase64));
        } else {
            userMessage = Map.of(
                    "role", "user",
                    "content", prompt);
        }

        Map<String, Object> request = Map.of(
                "model", model,
                "stream", false,
                "format", "json",
                "messages", List.of(userMessage),
                "options", Map.of("temperature", 0));

        String response = ollamaClient.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        String output = root.path("message")
                .path("content")
                .asText();

        if (output == null || output.isBlank()) {
            throw new IllegalStateException(
                    "Ollama returned an empty response.");
        }

        return objectMapper.readValue(
                output, AITransactionDraftDTO.class);
    }
}
