package com.threatintel.ingestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/")
public class IngestionController {

    @Autowired
    private IngestionService ingestionService;

    @PostMapping("/fetch")
    public ResponseEntity<Map<String, Object>> triggerFetch() {
        return ResponseEntity.ok(ingestionService.fetchAndPublish());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity.ok(Map.of(
            "service", "ingestion-service",
            "status", "running"
        ));
    }
}
