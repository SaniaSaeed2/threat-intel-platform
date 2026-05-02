package com.tip.database.controller;

import com.tip.database.entity.IocEntity;
import com.tip.database.repository.IocRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/iocs")
public class IocController {

    private final IocRepository iocRepository;

    public IocController(IocRepository iocRepository) {
        this.iocRepository = iocRepository;
    }

    // Get all IOCs with pagination
    @GetMapping
    public ResponseEntity<Page<IocEntity>> getAllIocs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IocEntity> result = iocRepository.findAllByOrderByFirstSeenDesc(
                PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    // Get IOC by ID
    @GetMapping("/{id}")
    public ResponseEntity<IocEntity> getById(@PathVariable Long id) {
        return iocRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Filter by type (IP or DOMAIN)
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<IocEntity>> getByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IocEntity> result = iocRepository.findByType(type.toUpperCase(),
                PageRequest.of(page, size, Sort.by("firstSeen").descending()));
        return ResponseEntity.ok(result);
    }

    // Get high severity IOCs (score >= threshold)
    @GetMapping("/high-severity")
    public ResponseEntity<Page<IocEntity>> getHighSeverity(
            @RequestParam(defaultValue = "70") int minScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<IocEntity> result = iocRepository
                .findBySeverityScoreGreaterThanEqualOrderBySeverityScoreDesc(minScore,
                        PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    // Top 10 most dangerous IOCs
    @GetMapping("/top10")
    public ResponseEntity<List<IocEntity>> getTop10() {
        return ResponseEntity.ok(
                iocRepository.findTop10BySeverityScoreIsNotNullOrderBySeverityScoreDesc());
    }

    // Analytics/stats endpoint
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalIocs", iocRepository.count());
        stats.put("pendingRanking", iocRepository.countPending());

        // Source breakdown
        Map<String, Long> bySource = new LinkedHashMap<>();
        iocRepository.countBySource().forEach(row -> bySource.put((String) row[0], (Long) row[1]));
        stats.put("bySource", bySource);

        // Severity breakdown
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        iocRepository.countBySeverityLabel().forEach(row -> bySeverity.put((String) row[0], (Long) row[1]));
        stats.put("bySeverity", bySeverity);

        // Type breakdown
        Map<String, Long> byType = new LinkedHashMap<>();
        iocRepository.countByType().forEach(row -> byType.put((String) row[0], (Long) row[1]));
        stats.put("byType", byType);

        return ResponseEntity.ok(stats);
    }

    // Search by value
    @GetMapping("/search")
    public ResponseEntity<Optional<IocEntity>> search(
            @RequestParam String value,
            @RequestParam String source) {
        return ResponseEntity.ok(iocRepository.findByValueAndSource(value, source));
    }
}
