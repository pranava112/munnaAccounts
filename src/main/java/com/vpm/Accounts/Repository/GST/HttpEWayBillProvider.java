package com.vpm.Accounts.Repository.GST;

import com.vpm.Accounts.Entity.GST.EWayBillResponse;
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
public class HttpEWayBillProvider implements EWayBillProvider {
    private final RestClient client;
    private final String generatePath;
    private final String vehiclePath;
    private final String cancelPath;

    public HttpEWayBillProvider(
            RestClient.Builder builder,
            @Value("${gst.gateway.base-url}") String baseUrl,
            @Value("${gst.gateway.bearer-token:}") String bearerToken,
            @Value("${gst.gateway.ewaybill.generate-path:/ewaybill/generate}") String generatePath,
            @Value("${gst.gateway.ewaybill.vehicle-path:/ewaybill/vehicle}") String vehiclePath,
            @Value("${gst.gateway.ewaybill.cancel-path:/ewaybill/cancel}") String cancelPath) {
        RestClient.Builder configuredBuilder = builder.baseUrl(baseUrl);
        if (bearerToken != null && !bearerToken.isBlank()) {
            configuredBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        }
        this.client = configuredBuilder.build();
        this.generatePath = generatePath;
        this.vehiclePath = vehiclePath;
        this.cancelPath = cancelPath;
    }

    @Override
    public EWayBillResponse generate(TaxInvoice invoice) {
        return client.post().uri(generatePath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(invoice).retrieve().body(EWayBillResponse.class);
    }

    @Override
    public EWayBillResponse updateVehicle(String ewbNumber, String vehicleNumber) {
        return client.put().uri(vehiclePath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("ewbNumber", ewbNumber, "vehicleNumber", vehicleNumber))
                .retrieve().body(EWayBillResponse.class);
    }

    @Override
    public EWayBillResponse cancel(String ewbNumber, String reason) {
        return client.post().uri(cancelPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("ewbNumber", ewbNumber, "reason", reason))
                .retrieve().body(EWayBillResponse.class);
    }
}
