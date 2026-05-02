package com.tip.database.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedIoc {
    private String value;
    private String type;
    private String source;
    private int severityScore;      // 0-100
    private String severityLabel;   // LOW, MEDIUM, HIGH, CRITICAL
    private String rankingResponse; // raw JSON from ranking API
    private String rankingStatus;   // RANKED or FAILED
}
