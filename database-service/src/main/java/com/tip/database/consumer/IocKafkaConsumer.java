package com.tip.database.consumer;

import com.tip.database.model.EnrichedIoc;
import com.tip.database.model.ValidatedIoc;
import com.tip.database.service.IocDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IocKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(IocKafkaConsumer.class);

    private final IocDatabaseService databaseService;

    public IocKafkaConsumer(IocDatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @KafkaListener(topics = "validated-iocs", groupId = "database-group",
                   containerFactory = "validatedIocListenerFactory")
    public void consumeValidated(ValidatedIoc ioc) {
        log.debug("Received validated IOC for storage: {}", ioc.getValue());
        try {
            databaseService.saveValidatedIoc(ioc);
        } catch (Exception e) {
            log.error("Failed to save IOC {}: {}", ioc.getValue(), e.getMessage());
        }
    }

    @KafkaListener(topics = "enriched-iocs", groupId = "database-group",
                   containerFactory = "enrichedIocListenerFactory")
    public void consumeEnriched(EnrichedIoc ioc) {
        log.debug("Received enriched IOC for update: {}", ioc.getValue());
        try {
            databaseService.updateRanking(ioc);
        } catch (Exception e) {
            log.error("Failed to update ranking for {}: {}", ioc.getValue(), e.getMessage());
        }
    }
}
