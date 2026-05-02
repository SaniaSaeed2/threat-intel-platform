package com.tip.ingestion.controller;

import com.tip.ingestion.service.AbuseIPDBService;
import com.tip.ingestion.service.AlienVaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final AbuseIPDBService abuseIPDBService;
    private final AlienVaultService alienVaultService;

    public IngestionController(AbuseIPDBService abuseIPDBService, AlienVaultService alienVaultService) {
        this.abuseIPDBService = abuseIPDBService;
        this.alienVaultService = alienVaultService;
    }

    @PostMapping("/trigger/abuseipdb")
    public ResponseEntity<Map<String, String>> triggerAbuseIPDB() {
        abuseIPDBService.fetchAndPublish();
        return ResponseEntity.ok(Map.of("status", "triggered", "source", "AbuseIPDB"));
    }

    @PostMapping("/trigger/alienvault")
    public ResponseEntity<Map<String, String>> triggerAlienVault() {
        alienVaultService.fetchAndPublish();
        return ResponseEntity.ok(Map.of("status", "triggered", "source", "AlienVault"));
    }

    @PostMapping("/trigger/all")
    public ResponseEntity<Map<String, String>> triggerAll() {
        abuseIPDBService.fetchAndPublish();
        alienVaultService.fetchAndPublish();
        return ResponseEntity.ok(Map.of("status", "triggered", "source", "ALL"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "ingestion-service"));
    }
}
