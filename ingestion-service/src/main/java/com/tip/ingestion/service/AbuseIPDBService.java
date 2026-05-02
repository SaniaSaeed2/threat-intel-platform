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
public class AbuseIPDBService {

    private static final Logger log = LoggerFactory.getLogger(AbuseIPDBService.class);
    private static final String TOPIC = "raw-iocs";

    @Value("${apis.abuseipdb.key}")
    private String apiKey;

    @Value("${apis.abuseipdb.url}")
    private String apiUrl;

    private final KafkaTemplate<String, RawIoc> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AbuseIPDBService(KafkaTemplate<String, RawIoc> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Run every 10 minutes
    @Scheduled(fixedRateString = "${ingestion.schedule.abuseipdb:600000}")
    public void fetchAndPublish() {
        log.info("Fetching from AbuseIPDB...");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?confidenceMinimum=70&limit=100"))
                    .header("Key", apiKey)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode data = root.path("data");

                if (data.isArray()) {
                    for (JsonNode entry : data) {
                        String ip = entry.path("ipAddress").asText("");
                        if (!ip.isBlank()) {
                            RawIoc ioc = RawIoc.builder()
                                    .value(ip)
                                    .type("IP")
                                    .source("ABUSEIPDB")
                                    .rawJson(entry.toString())
                                    .timestamp(System.currentTimeMillis())
                                    .build();
                            kafkaTemplate.send(TOPIC, ip, ioc);
                            log.debug("Published IP {} to Kafka", ip);
                        }
                    }
                    log.info("AbuseIPDB: published {} IOCs", data.size());
                }
            } else {
                log.error("AbuseIPDB API returned status: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching from AbuseIPDB: {}", e.getMessage(), e);
        }
    }
}
