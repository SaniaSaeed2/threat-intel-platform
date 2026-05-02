package com.tip.extraction.consumer;

import com.tip.extraction.model.RawIoc;
import com.tip.extraction.model.ValidatedIoc;
import com.tip.extraction.service.IocValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RawIocConsumer {

    private static final Logger log = LoggerFactory.getLogger(RawIocConsumer.class);
    private static final String OUTPUT_TOPIC = "validated-iocs";

    private final IocValidatorService validatorService;
    private final KafkaTemplate<String, ValidatedIoc> kafkaTemplate;

    // Simple stats counter
    private long totalReceived = 0;
    private long totalValid = 0;
    private long totalInvalid = 0;

    public RawIocConsumer(IocValidatorService validatorService,
                          KafkaTemplate<String, ValidatedIoc> kafkaTemplate) {
        this.validatorService = validatorService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "raw-iocs", groupId = "extraction-group")
    public void consume(RawIoc rawIoc) {
        totalReceived++;
        log.debug("Received raw IOC: {} ({})", rawIoc.getValue(), rawIoc.getType());

        try {
            ValidatedIoc validated = validatorService.validate(rawIoc);

            if (validated.isValid()) {
                kafkaTemplate.send(OUTPUT_TOPIC, validated.getValue(), validated);
                totalValid++;
                log.debug("Valid IOC forwarded: {}", validated.getValue());
            } else {
                totalInvalid++;
                log.debug("Invalid IOC filtered: {} - Reason: {}",
                        rawIoc.getValue(), validated.getValidationNote());
            }

            if (totalReceived % 100 == 0) {
                log.info("Stats - Received: {}, Valid: {}, Invalid: {}",
                        totalReceived, totalValid, totalInvalid);
            }
        } catch (Exception e) {
            log.error("Error processing IOC {}: {}", rawIoc.getValue(), e.getMessage());
        }
    }

    public long[] getStats() {
        return new long[]{totalReceived, totalValid, totalInvalid};
    }
}
