package com.tip.ingestion.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawIoc {
    private String value;         // IP or domain string
    private String type;          // "IP" or "DOMAIN"
    private String source;        // "ABUSEIPDB" or "ALIENVAULT"
    private String rawJson;       // original response snippet
    private long timestamp;
}
