package com.threatintel.extraction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ExtractionService {

    private static final Pattern IP_PATTERN =
        Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern DOMAIN_PATTERN =
        Pattern.compile("\\b([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}\\b");

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "raw-threat-data", groupId = "extraction-group")
    public void consumeAndExtract(String rawData) {
        try {
            JsonNode root = objectMapper.readTree(rawData);
            String source = root.has("source") ? root.get("source").asText() : "unknown";
            List<IOC> iocs = new ArrayList<>();

            if (source.contains("abuseipdb")) {
                extractFromAbuseIPDB(root, iocs);
            } else if (source.contains("alienvault")) {
                extractFromAlienVault(root, iocs);
            }

            for (IOC ioc : iocs) {
                String iocJson = objectMapper.writeValueAsString(ioc);
                kafkaTemplate.send("extracted-iocs", ioc.getType(), iocJson);
            }

            System.out.println("Extracted " + iocs.size() + " IOCs from " + source);
        } catch (Exception e) {
            System.err.println("Extraction error: " + e.getMessage());
        }
    }

    private void extractFromAbuseIPDB(JsonNode root, List<IOC> iocs) {
        JsonNode data = root.path("data");
        if (data.isArray()) {
            for (JsonNode item : data) {
                String ip = item.path("ipAddress").asText();
                if (!ip.isEmpty()) {
                    IOC ioc = new IOC();
                    ioc.setType("ip");
                    ioc.setValue(ip);
                    ioc.setSource("abuseipdb");
                    ioc.setDescription("Abuse confidence: " + item.path("abuseConfidenceScore").asText()
                        + "%, Country: " + item.path("countryCode").asText());
                    ioc.setTimestamp(LocalDateTime.now().toString());
                    iocs.add(ioc);
                }
                String domain = item.path("domain").asText();
                if (!domain.isEmpty() && !domain.equals("null")) {
                    IOC ioc = new IOC();
                    ioc.setType("domain");
                    ioc.setValue(domain);
                    ioc.setSource("abuseipdb");
                    ioc.setDescription("Associated domain for IP: " + ip);
                    ioc.setTimestamp(LocalDateTime.now().toString());
                    iocs.add(ioc);
                }
            }
        }
    }

    private void extractFromAlienVault(JsonNode root, List<IOC> iocs) {
        JsonNode results = root.path("results");
        if (results.isArray()) {
            for (JsonNode pulse : results) {
                String pulseName = pulse.path("name").asText();
                JsonNode indicators = pulse.path("indicators");
                if (indicators.isArray()) {
                    for (JsonNode ind : indicators) {
                        String type = ind.path("type").asText();
                        String value = ind.path("indicator").asText();
                        IOC ioc = new IOC();
                        ioc.setValue(value);
                        ioc.setSource("alienvault");
                        ioc.setDescription("Pulse: " + pulseName + " - " + ind.path("description").asText());
                        ioc.setTimestamp(LocalDateTime.now().toString());
                        if (type.equalsIgnoreCase("IPv4")) {
                            ioc.setType("ip");
                        } else if (type.equalsIgnoreCase("domain") || type.equalsIgnoreCase("hostname")) {
                            ioc.setType("domain");
                        } else {
                            continue;
                        }
                        iocs.add(ioc);
                    }
                }
            }
        }
    }
}
