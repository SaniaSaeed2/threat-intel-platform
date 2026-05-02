package com.threatintel.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class IOCService {

    @Autowired
    private IOCRepository iocRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "ranked-iocs", groupId = "database-group")
    public void saveIOC(String iocJson) {
        try {
            JsonNode node = objectMapper.readTree(iocJson);
            String value = node.path("value").asText();

            // Upsert logic - avoid duplicates
            Optional<IOCEntity> existing = iocRepository.findByValue(value);
            IOCEntity ioc = existing.orElse(new IOCEntity());

            ioc.setType(node.path("type").asText());
            ioc.setValue(value);
            ioc.setSource(node.path("source").asText());
            ioc.setDescription(node.path("description").asText());

            if (node.has("severityScore")) {
                ioc.setSeverityScore(node.path("severityScore").asInt());
                ioc.setSeverityLevel(node.path("severityLevel").asText());
                ioc.setRankedAt(LocalDateTime.now());
            }

            iocRepository.save(ioc);
            System.out.println("Saved IOC: " + value + " [" + ioc.getSeverityLevel() + "]");
        } catch (Exception e) {
            System.err.println("DB save error: " + e.getMessage());
        }
    }
}
