package com.threatintel.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
public class ProcessingService {

    private static final Pattern VALID_IP =
        Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern VALID_DOMAIN =
        Pattern.compile("^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$");

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "extracted-iocs", groupId = "processing-group")
    public void processIOC(String iocJson) {
        try {
            var root = objectMapper.readTree(iocJson);
            String type = root.path("type").asText();
            String value = root.path("value").asText();

            if (!isValid(type, value)) {
                System.out.println("Invalid IOC filtered out: " + value);
                return;
            }

            // Forward to ranking topic
            kafkaTemplate.send("validated-iocs", type, iocJson);
            System.out.println("Validated and forwarded: " + type + " -> " + value);
        } catch (Exception e) {
            System.err.println("Processing error: " + e.getMessage());
        }
    }

    private boolean isValid(String type, String value) {
        if (value == null || value.isBlank()) return false;
        if ("ip".equals(type)) return VALID_IP.matcher(value).matches();
        if ("domain".equals(type)) return VALID_DOMAIN.matcher(value).matches();
        return false;
    }
}
