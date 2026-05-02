package com.tip.database.service;

import com.tip.database.entity.IocEntity;
import com.tip.database.model.EnrichedIoc;
import com.tip.database.model.ValidatedIoc;
import com.tip.database.repository.IocRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IocDatabaseService {

    private static final Logger log = LoggerFactory.getLogger(IocDatabaseService.class);

    private final IocRepository iocRepository;

    public IocDatabaseService(IocRepository iocRepository) {
        this.iocRepository = iocRepository;
    }

    @Transactional
    public void saveValidatedIoc(ValidatedIoc ioc) {
        // Deduplicate: same value + source = update, not insert
        Optional<IocEntity> existing = iocRepository.findByValueAndSource(ioc.getValue(), ioc.getSource());

        if (existing.isPresent()) {
            log.debug("IOC already exists, skipping: {} from {}", ioc.getValue(), ioc.getSource());
            return;
        }

        IocEntity entity = IocEntity.builder()
                .value(ioc.getValue())
                .type(ioc.getType())
                .source(ioc.getSource())
                .rawJson(ioc.getRawJson())
                .rankingStatus("PENDING")
                .build();

        iocRepository.save(entity);
        log.debug("Saved new IOC: {}", ioc.getValue());
    }

    @Transactional
    public void updateRanking(EnrichedIoc enriched) {
        Optional<IocEntity> opt = iocRepository.findByValueAndSource(enriched.getValue(), enriched.getSource());

        if (opt.isPresent()) {
            IocEntity entity = opt.get();
            entity.setSeverityScore(enriched.getSeverityScore());
            entity.setSeverityLabel(enriched.getSeverityLabel());
            entity.setRankingResponse(enriched.getRankingResponse());
            entity.setRankingStatus(enriched.getRankingStatus());
            iocRepository.save(entity);
            log.debug("Updated ranking for {}: score={}, label={}",
                    enriched.getValue(), enriched.getSeverityScore(), enriched.getSeverityLabel());
        } else {
            log.warn("Tried to update ranking for unknown IOC: {}", enriched.getValue());
        }
    }
}
