package ru.ifmo.se.lab.service.payment;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class YookassaApiClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${yookassa.base-url:https://api.yookassa.ru/v3}")
    private String baseUrl;

    @Value("${yookassa.shop-id}")
    private String shopId;

    @Value("${yookassa.api-key}")
    private String apiKey;

    @Value("${yookassa.return-url:https://example.com}")
    private String returnUrl;

    public YookassaCreatePaymentResponse createHoldPayment(YookassaCreatePaymentRequest request) {
        return post("/payments", request, YookassaCreatePaymentResponse.class);
    }

    public YookassaCapturePaymentResponse capturePayment(String paymentId) {
        return post("/payments/" + paymentId + "/capture", null, YookassaCapturePaymentResponse.class);
    }

    public YookassaCancelPaymentResponse cancelPayment(String paymentId) {
        return post("/payments/" + paymentId + "/cancel", null, YookassaCancelPaymentResponse.class);
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        try {
            log.info("CHECK");
            log.info(objectMapper.writeValueAsString(body));
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Authorization", buildBasicAuthHeader())
                    .header("Idempotence-Key", UUID.randomUUID().toString())
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(body == null
                            ? HttpRequest.BodyPublishers.ofString("{}")
                            : HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(response.body());
            }

            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private String buildBasicAuthHeader() {
        String raw = shopId + ":" + apiKey;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public record YookassaCreatePaymentRequest(
            Amount amount,
            boolean capture,
            @JsonProperty("payment_method_data") PaymentMethodData paymentMethodData,
            Confirmation confirmation,
            String description) {
    }

    public record Amount(String value, String currency) {
    }

    public record PaymentMethodData(String type, Card card) {
    }

    public record Card(String csc, String expiry_month, String expiry_year, String number) {
    }

    public record Confirmation(String type, String return_url) {
    }

    public record YookassaCreatePaymentResponse(
            String id,
            String status,
            Amount amount,
            boolean paid,
            boolean refundable) {
    }

    public record YookassaCapturePaymentResponse(
            String id,
            String status,
            Amount amount,
            boolean paid,
            boolean refundable) {
    }

    public record YookassaCancelPaymentResponse(String id, String status, boolean paid) {
    }
}