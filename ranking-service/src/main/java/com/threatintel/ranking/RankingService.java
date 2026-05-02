package com.threatintel.ranking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class RankingService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @KafkaListener(topics = "validated-iocs", groupId = "ranking-group")
    public void rankIOC(String iocJson) {
        try {
            ObjectNode ioc = (ObjectNode) objectMapper.readTree(iocJson);
            String type = ioc.path("type").asText();
            String value = ioc.path("value").asText();
            String source = ioc.path("source").asText();

            // Calculate severity score (in real world, call external API like VirusTotal)
            int severityScore = calculateSeverity(type, value, source);
            String severityLevel = getSeverityLevel(severityScore);

            ioc.put("severityScore", severityScore);
            ioc.put("severityLevel", severityLevel);
            ioc.put("rankedAt", java.time.LocalDateTime.now().toString());

            kafkaTemplate.send("ranked-iocs", type, ioc.toString());
            System.out.println("Ranked: " + value + " -> Score: " + severityScore + " (" + severityLevel + ")");
        } catch (Exception e) {
            System.err.println("Ranking error: " + e.getMessage());
        }
    }

    // Simulates external API call - replace with real VirusTotal/similar API
    private int calculateSeverity(String type, String value, String source) {
        int base = 0;
        // Source-based scoring
        if ("abuseipdb".equals(source)) base += 40;
        if ("alienvault".equals(source)) base += 35;
        // Type-based adjustment
        if ("ip".equals(type)) base += 20;
        if ("domain".equals(type)) base += 15;
        // Add variability to simulate real API
        base += random.nextInt(25);
        return Math.min(base, 100);
    }

    private String getSeverityLevel(int score) {
        if (score >= 80) return "CRITICAL";
        if (score >= 60) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }
}
