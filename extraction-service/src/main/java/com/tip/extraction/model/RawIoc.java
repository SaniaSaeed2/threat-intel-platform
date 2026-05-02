package com.tip.extraction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawIoc {
    private String value;
    private String type;
    private String source;
    private String rawJson;
    private long timestamp;
}
