package com.threatintel.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

@Service
public class IngestionService {

    @Value("${abuseipdb.api.key:demo_key}")
    private String abuseIpDbKey;

    @Value("${alienvault.api.key:demo_key}")
    private String alienVaultKey;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();

    // Scheduled every 10 minutes
    @Scheduled(fixedDelay = 600000, initialDelay = 5000)
    public void scheduledFetch() {
        fetchAndPublish();
    }

    public Map<String, Object> fetchAndPublish() {
        Map<String, Object> result = new HashMap<>();
        List<String> published = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Fetch from AbuseIPDB
        try {
            String abuseData = fetchAbuseIPDB();
            kafkaTemplate.send("raw-threat-data", "abuseipdb", abuseData);
            published.add("abuseipdb");
        } catch (Exception e) {
            // Use mock data for demo
            String mockData = getMockAbuseIPDBData();
            kafkaTemplate.send("raw-threat-data", "abuseipdb", mockData);
            published.add("abuseipdb-mock");
            errors.add("AbuseIPDB: " + e.getMessage() + " (using mock data)");
        }

        // Fetch from AlienVault
        try {
            String alienData = fetchAlienVault();
            kafkaTemplate.send("raw-threat-data", "alienvault", alienData);
            published.add("alienvault");
        } catch (Exception e) {
            String mockData = getMockAlienVaultData();
            kafkaTemplate.send("raw-threat-data", "alienvault", mockData);
            published.add("alienvault-mock");
            errors.add("AlienVault: " + e.getMessage() + " (using mock data)");
        }

        result.put("published", published);
        result.put("errors", errors);
        result.put("timestamp", new Date().toString());
        return result;
    }

    private String fetchAbuseIPDB() throws Exception {
        return webClient.get()
            .uri("https://api.abuseipdb.com/api/v2/blacklist?confidenceMinimum=90&limit=50")
            .header("Key", abuseIpDbKey)
            .header("Accept", "application/json")
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private String fetchAlienVault() throws Exception {
        return webClient.get()
            .uri("https://otx.alienvault.com/api/v1/pulses/subscribed?limit=10")
            .header("X-OTX-API-KEY", alienVaultKey)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private String getMockAbuseIPDBData() {
        return "{\"source\":\"abuseipdb\",\"data\":[" +
            "{\"ipAddress\":\"192.168.1.100\",\"abuseConfidenceScore\":95,\"countryCode\":\"CN\",\"usageType\":\"Data Center\",\"isp\":\"ChinaNet\",\"domain\":\"chinanet.cn\",\"totalReports\":150,\"lastReportedAt\":\"2024-01-15T10:00:00Z\"}," +
            "{\"ipAddress\":\"10.0.0.50\",\"abuseConfidenceScore\":87,\"countryCode\":\"RU\",\"usageType\":\"Fixed Line ISP\",\"isp\":\"Rostelecom\",\"domain\":\"rostelecom.ru\",\"totalReports\":89,\"lastReportedAt\":\"2024-01-15T09:30:00Z\"}," +
            "{\"ipAddress\":\"172.16.0.25\",\"abuseConfidenceScore\":92,\"countryCode\":\"US\",\"usageType\":\"VPN\",\"isp\":\"Unknown VPN\",\"domain\":\"vpnservice.com\",\"totalReports\":45,\"lastReportedAt\":\"2024-01-15T08:00:00Z\"}," +
            "{\"ipAddress\":\"203.0.113.10\",\"abuseConfidenceScore\":78,\"countryCode\":\"KP\",\"usageType\":\"Data Center\",\"isp\":\"Star JV Co\",\"domain\":\"star.com.kp\",\"totalReports\":200,\"lastReportedAt\":\"2024-01-15T07:00:00Z\"}," +
            "{\"ipAddress\":\"198.51.100.5\",\"abuseConfidenceScore\":99,\"countryCode\":\"IR\",\"usageType\":\"Fixed Line ISP\",\"isp\":\"Respina\",\"domain\":\"respina.net\",\"totalReports\":321,\"lastReportedAt\":\"2024-01-15T06:00:00Z\"}" +
            "]}";
    }

    private String getMockAlienVaultData() {
        return "{\"source\":\"alienvault\",\"results\":[" +
            "{\"id\":\"pulse1\",\"name\":\"Malicious Campaign Jan 2024\",\"description\":\"Active C2 infrastructure\",\"indicators\":[" +
            "{\"type\":\"IPv4\",\"indicator\":\"45.33.32.156\",\"description\":\"C2 server\",\"created\":\"2024-01-14T12:00:00Z\"}," +
            "{\"type\":\"domain\",\"indicator\":\"malware-c2.evil.com\",\"description\":\"C2 domain\",\"created\":\"2024-01-14T12:00:00Z\"}," +
            "{\"type\":\"IPv4\",\"indicator\":\"104.21.45.67\",\"description\":\"Phishing host\",\"created\":\"2024-01-14T11:00:00Z\"}," +
            "{\"type\":\"domain\",\"indicator\":\"phishing-site.net\",\"description\":\"Phishing domain\",\"created\":\"2024-01-13T09:00:00Z\"}]}," +
            "{\"id\":\"pulse2\",\"name\":\"Ransomware Infrastructure\",\"description\":\"Known ransomware C2\",\"indicators\":[" +
            "{\"type\":\"IPv4\",\"indicator\":\"185.220.101.50\",\"description\":\"Ransomware C2\",\"created\":\"2024-01-13T08:00:00Z\"}," +
            "{\"type\":\"domain\",\"indicator\":\"ransomware-payment.onion.ws\",\"description\":\"Payment portal\",\"created\":\"2024-01-13T07:00:00Z\"}]}]}";
    }
}
