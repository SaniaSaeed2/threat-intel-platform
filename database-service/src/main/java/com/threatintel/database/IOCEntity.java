package com.threatintel.database;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "iocs")
@Data
public class IOCEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 512)
    private String value;

    @Column(nullable = false)
    private String source;

    @Column(length = 1000)
    private String description;

    private Integer severityScore;
    private String severityLevel;

    private LocalDateTime discoveredAt;
    private LocalDateTime rankedAt;

    @PrePersist
    public void prePersist() {
        if (discoveredAt == null) discoveredAt = LocalDateTime.now();
    }
}
