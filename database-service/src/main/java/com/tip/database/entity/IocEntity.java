package com.tip.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "iocs", indexes = {
        @Index(name = "idx_value", columnList = "value"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_severity", columnList = "severity_score"),
        @Index(name = "idx_source", columnList = "source")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IocEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(nullable = false, length = 10)
    private String type;  // IP or DOMAIN

    @Column(nullable = false, length = 50)
    private String source; // ABUSEIPDB, ALIENVAULT

    @Column(name = "severity_score")
    private Integer severityScore;  // 0-100, null = not yet ranked

    @Column(name = "severity_label", length = 20)
    private String severityLabel;   // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    @Column(name = "ranking_response", columnDefinition = "TEXT")
    private String rankingResponse;

    @Column(name = "first_seen")
    @CreationTimestamp
    private LocalDateTime firstSeen;

    @Column(name = "last_updated")
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @Column(name = "ranking_status", length = 20)
    @Builder.Default
    private String rankingStatus = "PENDING"; // PENDING, RANKED, FAILED
}
