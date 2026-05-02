package com.threatintel.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/iocs")
public class IOCController {

    @Autowired
    private IOCRepository iocRepository;

    @GetMapping
    public ResponseEntity<List<IOCEntity>> getAllIOCs() {
        return ResponseEntity.ok(iocRepository.findAllOrderBySeverity());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IOCEntity> getById(@PathVariable Long id) {
        return iocRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<IOCEntity>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(iocRepository.findByType(type));
    }

    @GetMapping("/severity/{level}")
    public ResponseEntity<List<IOCEntity>> getBySeverity(@PathVariable String level) {
        return ResponseEntity.ok(iocRepository.findBySeverityLevel(level.toUpperCase()));
    }

    @GetMapping("/search")
    public ResponseEntity<IOCEntity> search(@RequestParam String value) {
        return iocRepository.findByValue(value)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", iocRepository.count());

        Map<String, Long> bySeverity = new HashMap<>();
        for (Object[] row : iocRepository.countBySeverityLevel()) {
            bySeverity.put((String) row[0], (Long) row[1]);
        }
        stats.put("bySeverity", bySeverity);

        Map<String, Long> bySource = new HashMap<>();
        for (Object[] row : iocRepository.countBySource()) {
            bySource.put((String) row[0], (Long) row[1]);
        }
        stats.put("bySource", bySource);

        Map<String, Long> byType = new HashMap<>();
        for (Object[] row : iocRepository.countByType()) {
            byType.put((String) row[0], (Long) row[1]);
        }
        stats.put("byType", byType);

        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        iocRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
