package com.tip.extraction.service;

import com.tip.extraction.model.RawIoc;
import com.tip.extraction.model.ValidatedIoc;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class IocValidatorService {

    // IPv4 regex
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    // Domain regex (basic, covers most real domains)
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?" +
            "\\.)+[a-zA-Z]{2,}$"
    );

    // Private/reserved IP ranges to filter out
    private static final Pattern PRIVATE_IP = Pattern.compile(
            "^(10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.|127\\.|0\\.|255\\.)"
    );

    public ValidatedIoc validate(RawIoc raw) {
        if (raw.getValue() == null || raw.getValue().isBlank()) {
            return buildInvalid(raw, "Empty value");
        }

        String value = raw.getValue().trim().toLowerCase();

        if ("IP".equals(raw.getType())) {
            return validateIp(raw, value);
        } else if ("DOMAIN".equals(raw.getType())) {
            return validateDomain(raw, value);
        }

        return buildInvalid(raw, "Unknown IOC type: " + raw.getType());
    }

    private ValidatedIoc validateIp(RawIoc raw, String ip) {
        if (!IPV4_PATTERN.matcher(ip).matches()) {
            return buildInvalid(raw, "Invalid IPv4 format");
        }
        if (PRIVATE_IP.matcher(ip).find()) {
            return buildInvalid(raw, "Private/reserved IP range");
        }
        return buildValid(raw, ip);
    }

    private ValidatedIoc validateDomain(RawIoc raw, String domain) {
        if (!DOMAIN_PATTERN.matcher(domain).matches()) {
            return buildInvalid(raw, "Invalid domain format");
        }
        // Filter common false positives
        if (domain.endsWith(".local") || domain.endsWith(".internal")) {
            return buildInvalid(raw, "Internal domain");
        }
        return buildValid(raw, domain);
    }

    private ValidatedIoc buildValid(RawIoc raw, String normalizedValue) {
        return ValidatedIoc.builder()
                .value(normalizedValue)
                .type(raw.getType())
                .source(raw.getSource())
                .rawJson(raw.getRawJson())
                .timestamp(raw.getTimestamp())
                .valid(true)
                .validationNote("OK")
                .build();
    }

    private ValidatedIoc buildInvalid(RawIoc raw, String reason) {
        return ValidatedIoc.builder()
                .value(raw.getValue())
                .type(raw.getType())
                .source(raw.getSource())
                .rawJson(raw.getRawJson())
                .timestamp(raw.getTimestamp())
                .valid(false)
                .validationNote(reason)
                .build();
    }
}
