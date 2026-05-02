package com.threatintel.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AnalyticsController {

    private final WebClient webClient = WebClient.builder()
        .baseUrl("http://localhost:8085")
        .build();

    @GetMapping("/summary")
    public ResponseEntity<Object> getSummary() {
        Object stats = webClient.get()
            .uri("/iocs/stats")
            .retrieve()
            .bodyToMono(Object.class)
            .block();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-threats")
    public ResponseEntity<Object> getTopThreats() {
        Object iocs = webClient.get()
            .uri("/iocs/severity/CRITICAL")
            .retrieve()
            .bodyToMono(Object.class)
            .block();
        return ResponseEntity.ok(iocs);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of("service", "analytics-service", "status", "running"));
    }
}
