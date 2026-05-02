package com.tip.extraction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedIoc {
    private String value;
    private String type;      // "IP" or "DOMAIN"
    private String source;
    private String rawJson;
    private long timestamp;
    private boolean valid;
    private String validationNote;
}
