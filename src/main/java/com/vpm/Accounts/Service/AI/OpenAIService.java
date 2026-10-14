

// package com.vpm.Accounts.Service.AI;

// import java.math.BigDecimal;
// import java.util.Base64;
// import java.util.List;

// import org.springframework.http.MediaType;
// import org.springframework.http.HttpStatus;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestClientResponseException;
// import org.springframework.web.client.RestClient;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.server.ResponseStatusException;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.vpm.Accounts.DTO.ProfitLossDTO;
// import com.vpm.Accounts.DTO.AI.AIAccountDTO;
// import com.vpm.Accounts.DTO.AI.AIProductDTO;
// import com.vpm.Accounts.DTO.AI.AIJournalDraftDTO;
// import com.vpm.Accounts.DTO.AI.AIJournalLineDTO;
// import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;

// @Service
// public class OpenAIService {

//     private final RestClient openAIRestClient;

//     private final ObjectMapper objectMapper;
//     private final String model;

//     public OpenAIService(
//             RestClient openAIRestClient,
//             ObjectMapper objectMapper,
//             @Value("${openai.model:gpt-4.1}") String model) {

//         this.openAIRestClient = openAIRestClient;
//         this.objectMapper = objectMapper;
//         this.model = model;
//     }

//     /*
//      * Your existing Profit/Loss method
//      */
//     public BigDecimal getProfitLoss(ProfitLossDTO profitLoss) {

//         BigDecimal profit = profitLoss.getNetProfit();

//         System.out.println("Profit is " + profit);

//         return profit;
//     }

//     /*
//      * ============================================================
//      * AI DOCUMENT -> PURCHASE/SALE DRAFT
//      * ============================================================
//      */

//     public AITransactionDraftDTO scanDocument(
//             MultipartFile file,
//             List<AIAccountDTO> accounts,
//             List<AIProductDTO> products) throws Exception {

//         if (file == null || file.isEmpty()) {
//             throw new IllegalArgumentException(
//                     "Document file is required");
//         }

//         String contentType = DocumentContentType.resolve(
//                 file.getContentType(),
//                 file.getOriginalFilename());

//         if (!MediaType.APPLICATION_PDF_VALUE.equals(contentType)
//                 && !MediaType.IMAGE_JPEG_VALUE.equals(contentType)
//                 && !MediaType.IMAGE_PNG_VALUE.equals(contentType)
//                 && !"image/webp".equals(contentType)) {
//             throw new IllegalArgumentException(
//                     "Supported document types are PDF, JPG, PNG, and WEBP");
//         }

//         String base64 = Base64.getEncoder()
//                 .encodeToString(file.getBytes());

//         String dataUrl =
//                 "data:" + contentType + ";base64," + base64;
//         String documentContent;
//         if (MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
//             String filename = file.getOriginalFilename() == null
//                     ? "document.pdf"
//                     : file.getOriginalFilename();
//             documentContent = """
//                     {
//                       "type": "input_file",
//                       "filename": %s,
//                       "file_data": %s
//                     }
//                     """.formatted(
//                     objectMapper.writeValueAsString(filename),
//                     objectMapper.writeValueAsString(dataUrl));
//         } else {
//             documentContent = """
//                     {
//                       "type": "input_image",
//                       "image_url": %s
//                     }
//                     """.formatted(objectMapper.writeValueAsString(dataUrl));
//         }

//         String accountList = objectMapper.writeValueAsString(accounts);
//         String productList = objectMapper.writeValueAsString(products);

//         String prompt = """
//                 Extract a source PURCHASE or SALES invoice; do not create journal
//                 lines. We will create the real transaction and its journal only
//                 after a user reviews and confirms the draft.
//                 Use the document to extract its date, supplier/customer name,
//                 grand total, and every item. Quantities must be exact; do not
//                 round fractional quantities. Copy the exact per-unit price
//                 shown; do not remove or add tax or discount. GST/cess rates and HSN/SAC must be
//                 null/empty if they are not explicitly shown.
//                 Match products only to the supplied product list, using the
//                 exact product id. A purchase line without a match may have a
//                 null productId; the application can create that product with
//                 zero selling price. For sales, only match existing active
//                 products; never invent stock or product IDs. Use transaction
//                 type PURCHASE for supplier bills and SALES for customer invoices.
//                 Do not treat a payment status as a cash receipt/payment; this
//                 flow records the source invoice, and settlement is separate.
//                 Do not infer absent details. Add warnings about any unclear
//                 value, discount, or invoice whose total cannot be explained by
//                 its lines/tax. Return only the required JSON structure.

//                 Existing ledger accounts (counterparties must already exist
//                 as named accounts):
//                 %s

//                 Existing active products:
//                 %s
//                 """.formatted(accountList, productList);

//         String request = """
//                 {
//                   "model": %s,
//                   "input": [
//                     {
//                       "role": "developer",
//                       "content": [
//                         {
//                           "type": "input_text",
//                           "text": %s
//                         }
//                       ]
//                     },
//                     {
//                       "role": "user",
//                       "content": [
//                         {
//                           "type": "input_text",
//                           "text": "Extract a purchase or sales invoice draft for human review."
//                         },
//                         %s
//                       ]
//                     }
//                   ],
//                   "text": {
//                     "format": {
//                       "type": "json_schema",
//                       "name": "transaction_draft",
//                       "strict": true,
//                       "schema": {
//                         "type": "object",
//                         "additionalProperties": false,
//                         "properties": {
//                           "transactionType": {
//                             "type": "string"
//                           },
//                           "date": {
//                             "type": ["string", "null"]
//                           },
//                           "counterpartyName": {
//                             "type": "string"
//                           },
//                           "invoiceTotal": {
//                             "type": ["number", "null"]
//                           },
//                           "confidence": {
//                             "type": "string"
//                           },
//                           "warnings": {
//                             "type": "array",
//                             "items": {
//                               "type": "string"
//                             }
//                           },
//                           "items": {
//                             "type": "array",
//                             "items": {
//                               "type": "object",
//                               "additionalProperties": false,
//                               "properties": {
//                                 "productName": {
//                                   "type": "string"
//                                 },
//                                 "productId": {
//                                   "type": ["integer", "null"]
//                                 },
//                                 "quantity": {
//                                   "type": "number"
//                                 },
//                                 "unitPrice": {
//                                   "type": "number"
//                                 },
//                                 "gstRate": {
//                                   "type": ["number", "null"]
//                                 },
//                                 "cessRate": {
//                                   "type": ["number", "null"]
//                                 },
//                                 "hsnSacCode": {
//                                   "type": "string"
//                                 }
//                               },
//                               "required": [
//                                 "productName",
//                                 "productId",
//                                 "quantity",
//                                 "unitPrice",
//                                 "gstRate",
//                                 "cessRate",
//                                 "hsnSacCode"
//                               ]
//                             }
//                           }
//                         },
//                         "required": [
//                           "transactionType",
//                           "date",
//                           "counterpartyName",
//                           "invoiceTotal",
//                           "confidence",
//                           "warnings",
//                           "items"
//                         ]
//                       }
//                     }
//                   }
//                 }
//                 """.formatted(
//                         objectMapper.writeValueAsString(model),
//                         objectMapper.writeValueAsString(prompt),
//                         documentContent);

//         String response;
//         try {
//             response = openAIRestClient
//                     .post()
//                     .uri("/responses")
//                     .contentType(MediaType.APPLICATION_JSON)
//                     .body(request)
//                     .retrieve()
//                     .body(String.class);
//         } catch (RestClientResponseException exception) {
//             JsonNode error;
//             try {
//                 error = objectMapper.readTree(exception.getResponseBodyAsString())
//                         .path("error");
//             } catch (Exception parsingException) {
//                 throw exception;
//             }

//             String code = error.path("code").asText();
//             String type = error.path("type").asText();
//             if ("credit_balance_exhausted".equals(code)
//                     || "insufficient_quota".equals(type)) {
//                 throw new ResponseStatusException(
//                         HttpStatus.PAYMENT_REQUIRED,
//                         "OpenAI API credits are exhausted. Add billing credits or configure an API key with available quota, then retry document scanning.");
//             }
//             throw exception;
//         }

//         return parseTransactionDraft(response);
//     }

//     /*
//      * ============================================================
//      * PARSE OPENAI RESPONSE
//      * ============================================================
//      */

//     private AITransactionDraftDTO parseTransactionDraft(
//             String response) throws Exception {

//         JsonNode root =
//                 objectMapper.readTree(response);

//         String outputText =
//                 root.path("output")
//                         .get(0)
//                         .path("content")
//                         .get(0)
//                         .path("text")
//                         .asText();

//         return objectMapper.readValue(
//                 outputText,
//                 AITransactionDraftDTO.class);
//     }

//     /*
//      * ============================================================
//      * VALIDATE JOURNAL
//      * ============================================================
//      */

//     public void validateJournalDraft(
//             AIJournalDraftDTO draft) {

//         if (draft == null) {
//             throw new IllegalArgumentException(
//                     "Journal draft cannot be null");
//         }

//         if (draft.getLines() == null ||
//                 draft.getLines().size() < 2) {

//             throw new IllegalArgumentException(
//                     "Journal must contain at least two lines");
//         }

//         BigDecimal debit = BigDecimal.ZERO;

//         BigDecimal credit = BigDecimal.ZERO;

//         for (AIJournalLineDTO line : draft.getLines()) {
//             if (line == null) {
//                 throw new IllegalArgumentException(
//                         "Journal cannot contain an empty line");
//             }

//             if (line.getAccountId() == null
//                     || line.getAccountName() == null
//                     || line.getAccountName().isBlank()) {
//                 throw new IllegalArgumentException(
//                         "Account ID and account name are required for every journal line");
//             }

//             if (line.getDebit() == null) {
//                 line.setDebit(BigDecimal.ZERO);
//             }

//             if (line.getCredit() == null) {
//                 line.setCredit(BigDecimal.ZERO);
//             }

//             debit = debit.add(line.getDebit());

//             credit = credit.add(line.getCredit());

//             if (line.getDebit().signum() < 0 ||
//                     line.getCredit().signum() < 0) {

//                 throw new IllegalArgumentException(
//                         "Debit/Credit cannot be negative");
//             }

//             boolean hasDebit = line.getDebit().signum() > 0;
//             boolean hasCredit = line.getCredit().signum() > 0;
//             if (hasDebit == hasCredit) {
//                 throw new IllegalArgumentException(
//                         "Each journal line must have a positive debit or credit, but not both");
//             }
//         }

//         if (debit.compareTo(credit) != 0) {

//             throw new IllegalArgumentException(
//                     "Journal is not balanced. Debit = "
//                     + debit +
//                     ", Credit = "
//                     + credit);
//         }

//         draft.setTotalDebit(debit);
//         draft.setTotalCredit(credit);

//         draft.setBalanced(true);

//         if (draft.getDate() == null) {
//             throw new IllegalArgumentException(
//                     "Journal date is required");
//         }

//         if (draft.getVoucherType() == null ||
//                 draft.getVoucherType().isBlank()) {

//             throw new IllegalArgumentException(
//                     "Voucher type is required");
//         }
//     }
// }






package com.vpm.Accounts.Service.AI;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vpm.Accounts.DTO.ProfitLossDTO;
import com.vpm.Accounts.DTO.AI.AIAccountDTO;
import com.vpm.Accounts.DTO.AI.AIProductDTO;
import com.vpm.Accounts.DTO.AI.AIJournalDraftDTO;
import com.vpm.Accounts.DTO.AI.AIJournalLineDTO;
import com.vpm.Accounts.DTO.AI.AITransactionDraftDTO;

@Service
public class OpenAIService {

    private final RestClient openAIRestClient;

    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAIService(
            RestClient openAIRestClient,
            ObjectMapper objectMapper,
            @Value("${openai.model:gpt-4.1}") String model) {

        this.openAIRestClient = openAIRestClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    /*
     * Your existing Profit/Loss method
     */
    public BigDecimal getProfitLoss(ProfitLossDTO profitLoss) {

        BigDecimal profit = profitLoss.getNetProfit();

        System.out.println("Profit is " + profit);

        return profit;
    }

    /*
     * ============================================================
     * AI DOCUMENT -> PURCHASE/SALE DRAFT
     * ============================================================
     */

    public AITransactionDraftDTO scanDocument(
            MultipartFile file,
            List<AIAccountDTO> accounts,
            List<AIProductDTO> products) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Document file is required");
        }

        String contentType = DocumentContentType.resolve(
                file.getContentType(),
                file.getOriginalFilename());

        if (!MediaType.APPLICATION_PDF_VALUE.equals(contentType)
                && !MediaType.IMAGE_JPEG_VALUE.equals(contentType)
                && !MediaType.IMAGE_PNG_VALUE.equals(contentType)
                && !"image/webp".equals(contentType)) {
            throw new IllegalArgumentException(
                    "Supported document types are PDF, JPG, PNG, and WEBP");
        }

        String base64 = Base64.getEncoder()
                .encodeToString(file.getBytes());

        String dataUrl =
                "data:" + contentType + ";base64," + base64;
        String documentContent;
        if (MediaType.APPLICATION_PDF_VALUE.equals(contentType)) {
            String filename = file.getOriginalFilename() == null
                    ? "document.pdf"
                    : file.getOriginalFilename();
            documentContent = """
                    {
                      "type": "input_file",
                      "filename": %s,
                      "file_data": %s
                    }
                    """.formatted(
                    objectMapper.writeValueAsString(filename),
                    objectMapper.writeValueAsString(dataUrl));
        } else {
            documentContent = """
                    {
                      "type": "input_image",
                      "image_url": %s
                    }
                    """.formatted(objectMapper.writeValueAsString(dataUrl));
        }

        String accountList = objectMapper.writeValueAsString(accounts);
        String productList = objectMapper.writeValueAsString(products);

        String prompt = """
                Extract a source PURCHASE or SALES invoice; do not create journal
                lines. We will create the real transaction and its journal only
                after a user reviews and confirms the draft.
                Use the document to extract its date, supplier/customer name,
                grand total, and every item. Quantities must be exact; do not
                round fractional quantities. Copy the exact per-unit price
                shown; do not remove or add tax or discount. GST/cess rates and HSN/SAC must be
                null/empty if they are not explicitly shown.
                Match products only to the supplied product list, using the
                exact product id. A purchase line without a match may have a
                null productId; the application can create that product with
                zero selling price. For sales, only match existing active
                products; never invent stock or product IDs. Use transaction
                type PURCHASE for supplier bills and SALES for customer invoices.
                Do not treat a payment status as a cash receipt/payment; this
                flow records the source invoice, and settlement is separate.
                Do not infer absent details. Add warnings about any unclear
                value, discount, or invoice whose total cannot be explained by
                its lines/tax. Return only the required JSON structure.

                Existing ledger accounts (counterparties must already exist
                as named accounts):
                %s

                Existing active products:
                %s
                """.formatted(accountList, productList);

        String request = """
                {
                  "model": %s,
                  "input": [
                    {
                      "role": "developer",
                      "content": [
                        {
                          "type": "input_text",
                          "text": %s
                        }
                      ]
                    },
                    {
                      "role": "user",
                      "content": [
                        {
                          "type": "input_text",
                          "text": "Extract a purchase or sales invoice draft for human review."
                        },
                        %s
                      ]
                    }
                  ],
                  "text": {
                    "format": {
                      "type": "json_schema",
                      "name": "transaction_draft",
                      "strict": true,
                      "schema": {
                        "type": "object",
                        "additionalProperties": false,
                        "properties": {
                          "transactionType": {
                            "type": "string"
                          },
                          "date": {
                            "type": ["string", "null"]
                          },
                          "counterpartyName": {
                            "type": "string"
                          },
                          "invoiceTotal": {
                            "type": ["number", "null"]
                          },
                          "confidence": {
                            "type": "string"
                          },
                          "warnings": {
                            "type": "array",
                            "items": {
                              "type": "string"
                            }
                          },
                          "items": {
                            "type": "array",
                            "items": {
                              "type": "object",
                              "additionalProperties": false,
                              "properties": {
                                "productName": {
                                  "type": "string"
                                },
                                "productId": {
                                  "type": ["integer", "null"]
                                },
                                "quantity": {
                                  "type": "number"
                                },
                                "unitPrice": {
                                  "type": "number"
                                },
                                "gstRate": {
                                  "type": ["number", "null"]
                                },
                                "cessRate": {
                                  "type": ["number", "null"]
                                },
                                "hsnSacCode": {
                                  "type": "string"
                                }
                              },
                              "required": [
                                "productName",
                                "productId",
                                "quantity",
                                "unitPrice",
                                "gstRate",
                                "cessRate",
                                "hsnSacCode"
                              ]
                            }
                          }
                        },
                        "required": [
                          "transactionType",
                          "date",
                          "counterpartyName",
                          "invoiceTotal",
                          "confidence",
                          "warnings",
                          "items"
                        ]
                      }
                    }
                  }
                }
                """.formatted(
                        objectMapper.writeValueAsString(model),
                        objectMapper.writeValueAsString(prompt),
                        documentContent);

        String response;
        try {
            response = openAIRestClient
                    .post()
                    .uri("/responses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            JsonNode error;
            try {
                error = objectMapper.readTree(exception.getResponseBodyAsString())
                        .path("error");
            } catch (Exception parsingException) {
                throw exception;
            }

            String code = error.path("code").asText();
            String type = error.path("type").asText();
            if ("credit_balance_exhausted".equals(code)
                    || "insufficient_quota".equals(type)) {
                throw new ResponseStatusException(
                        HttpStatus.PAYMENT_REQUIRED,
                        "OpenAI API credits are exhausted. Add billing credits or configure an API key with available quota, then retry document scanning.");
            }
            throw exception;
        }

        return parseTransactionDraft(response);
    }

    /*
     * ============================================================
     * PARSE OPENAI RESPONSE
     * ============================================================
     */

    private AITransactionDraftDTO parseTransactionDraft(
            String response) throws Exception {

        JsonNode root =
                objectMapper.readTree(response);

        String outputText =
                root.path("output")
                        .get(0)
                        .path("content")
                        .get(0)
                        .path("text")
                        .asText();

        return objectMapper.readValue(
                outputText,
                AITransactionDraftDTO.class);
    }

    /*
     * ============================================================
     * VALIDATE JOURNAL
     * ============================================================
     */

    public void validateJournalDraft(
            AIJournalDraftDTO draft) {

        if (draft == null) {
            throw new IllegalArgumentException(
                    "Journal draft cannot be null");
        }

        if (draft.getLines() == null ||
                draft.getLines().size() < 2) {

            throw new IllegalArgumentException(
                    "Journal must contain at least two lines");
        }

        BigDecimal debit = BigDecimal.ZERO;

        BigDecimal credit = BigDecimal.ZERO;

        for (AIJournalLineDTO line : draft.getLines()) {
            if (line == null) {
                throw new IllegalArgumentException(
                        "Journal cannot contain an empty line");
            }

            if (line.getAccountId() == null
                    || line.getAccountName() == null
                    || line.getAccountName().isBlank()) {
                throw new IllegalArgumentException(
                        "Account ID and account name are required for every journal line");
            }

            if (line.getDebit() == null) {
                line.setDebit(BigDecimal.ZERO);
            }

            if (line.getCredit() == null) {
                line.setCredit(BigDecimal.ZERO);
            }

            debit = debit.add(line.getDebit());

            credit = credit.add(line.getCredit());

            if (line.getDebit().signum() < 0 ||
                    line.getCredit().signum() < 0) {

                throw new IllegalArgumentException(
                        "Debit/Credit cannot be negative");
            }

            boolean hasDebit = line.getDebit().signum() > 0;
            boolean hasCredit = line.getCredit().signum() > 0;
            if (hasDebit == hasCredit) {
                throw new IllegalArgumentException(
                        "Each journal line must have a positive debit or credit, but not both");
            }
        }

        if (debit.compareTo(credit) != 0) {

            throw new IllegalArgumentException(
                    "Journal is not balanced. Debit = "
                    + debit +
                    ", Credit = "
                    + credit);
        }

        draft.setTotalDebit(debit);
        draft.setTotalCredit(credit);

        draft.setBalanced(true);

        if (draft.getDate() == null) {
            throw new IllegalArgumentException(
                    "Journal date is required");
        }

        if (draft.getVoucherType() == null ||
                draft.getVoucherType().isBlank()) {

            throw new IllegalArgumentException(
                    "Voucher type is required");
        }
    }
}