package com.threatintel.kafkaproducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/")
public class KafkaProducerController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(
            @RequestParam String topic,
            @RequestParam String key,
            @RequestBody String message) {
        kafkaTemplate.send(topic, key, message);
        return ResponseEntity.ok(Map.of(
            "status", "published",
            "topic", topic,
            "key", key
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("service", "kafka-producer-service", "status", "running"));
    }
}
