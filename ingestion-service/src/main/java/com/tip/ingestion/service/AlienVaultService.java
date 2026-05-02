package com.tip.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tip.ingestion.model.RawIoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AlienVaultService {

    private static final Logger log = LoggerFactory.getLogger(AlienVaultService.class);
    private static final String TOPIC = "raw-iocs";

    @Value("${apis.alienvault.key}")
    private String apiKey;

    @Value("${apis.alienvault.url}")
    private String apiUrl;

    private final KafkaTemplate<String, RawIoc> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AlienVaultService(KafkaTemplate<String, RawIoc> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Run every 15 minutes (offset from AbuseIPDB)
    @Scheduled(fixedRateString = "${ingestion.schedule.alienvault:900000}")
    public void fetchAndPublish() {
        log.info("Fetching from AlienVault OTX...");
        try {
            // Fetch subscribed pulses
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/api/v1/pulses/subscribed?limit=20"))
                    .header("X-OTX-API-KEY", apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode results = root.path("results");
                int count = 0;

                if (results.isArray()) {
                    for (JsonNode pulse : results) {
                        JsonNode indicators = pulse.path("indicators");
                        if (indicators.isArray()) {
                            for (JsonNode indicator : indicators) {
                                String type = indicator.path("type").asText("");
                                String value = indicator.path("indicator").asText("");

                                String iocType = null;
                                if ("IPv4".equals(type) || "IPv6".equals(type)) {
                                    iocType = "IP";
                                } else if ("domain".equals(type) || "hostname".equals(type)) {
                                    iocType = "DOMAIN";
                                }

                                if (iocType != null && !value.isBlank()) {
                                    RawIoc ioc = RawIoc.builder()
                                            .value(value)
                                            .type(iocType)
                                            .source("ALIENVAULT")
                                            .rawJson(indicator.toString())
                                            .timestamp(System.currentTimeMillis())
                                            .build();
                                    kafkaTemplate.send(TOPIC, value, ioc);
                                    count++;
                                }
                            }
                        }
                    }
                }
                log.info("AlienVault: published {} IOCs", count);
            } else {
                log.error("AlienVault API returned status: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching from AlienVault: {}", e.getMessage(), e);
        }
    }
}
