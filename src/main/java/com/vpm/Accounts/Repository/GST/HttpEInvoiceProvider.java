package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.EInvoiceResponse;
import com.vpm.Accounts.Entity.GST.TaxInvoice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "gst.gateway", name = "base-url")
public class HttpEInvoiceProvider implements EInvoiceProvider {
    private final RestClient client;
    private final String generatePath;
    private final String cancelPath;

    public HttpEInvoiceProvider(
            RestClient.Builder builder,
            @Value("${gst.gateway.base-url}") String baseUrl,
            @Value("${gst.gateway.bearer-token:}") String bearerToken,
            @Value("${gst.gateway.einvoice.generate-path:/einvoice/generate}") String generatePath,
            @Value("${gst.gateway.einvoice.cancel-path:/einvoice/cancel}") String cancelPath) {
        RestClient.Builder configuredBuilder = builder.baseUrl(baseUrl);
        if (bearerToken != null && !bearerToken.isBlank()) {
            configuredBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        }
        this.client = configuredBuilder.build();
        this.generatePath = generatePath;
        this.cancelPath = cancelPath;
    }

    @Override
    public EInvoiceResponse generate(TaxInvoice invoice) {
        return client.post().uri(generatePath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(invoice).retrieve().body(EInvoiceResponse.class);
    }

    @Override
    public EInvoiceResponse cancel(String irn, String reason) {
        return client.post().uri(cancelPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("irn", irn, "reason", reason))
                .retrieve().body(EInvoiceResponse.class);
    }
}
