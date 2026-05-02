package com.threatintel.extraction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IOC {
    private String type;       // "ip" or "domain"
    private String value;
    private String source;
    private String description;
    private String timestamp;
}
