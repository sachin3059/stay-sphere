package com.staysphere.booking.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyIntegrationClient {

    @Value("${staysphere.property-service-url:http://localhost:8082}")
    private String propertyServiceUrl;

    private WebClient client() {
        return WebClient.builder().baseUrl(propertyServiceUrl).build();
    }

    public PriceQuote calculatePrice(String propertyId,
                                     LocalDate checkIn,
                                     LocalDate checkOut) {
        Map<String, Object> body = Map.of(
                "propertyId", propertyId,
                "checkIn", checkIn.toString(),
                "checkOut", checkOut.toString());

        JsonNode root = client().post()
                .uri("/api/pricing/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        JsonNode data = root != null ? root.get("data") : null;
        if (data == null) {
            throw new IllegalStateException("Unable to calculate price for property");
        }
        return new PriceQuote(
                new BigDecimal(data.get("finalPricePerNight").asText()),
                new BigDecimal(data.get("totalPrice").asText()));
    }

    public boolean isRangeAvailable(String propertyId,
                                    LocalDate checkIn,
                                    LocalDate checkOut) {
        JsonNode root = client().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/availability/{propertyId}/check")
                        .queryParam("checkIn", checkIn)
                        .queryParam("checkOut", checkOut)
                        .build(propertyId))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        JsonNode data = root != null ? root.get("data") : null;
        return data != null && data.path("available").asBoolean(false);
    }

    public record PriceQuote(BigDecimal pricePerNight, BigDecimal totalPrice) {}
}
