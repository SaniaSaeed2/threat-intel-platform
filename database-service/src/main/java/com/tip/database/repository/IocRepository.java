package com.tip.database.repository;

import com.tip.database.entity.IocEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IocRepository extends JpaRepository<IocEntity, Long> {

    Optional<IocEntity> findByValueAndSource(String value, String source);

    boolean existsByValueAndSource(String value, String source);

    List<IocEntity> findByRankingStatus(String status);

    Page<IocEntity> findByType(String type, Pageable pageable);

    Page<IocEntity> findBySeverityLabelOrderBySeverityScoreDesc(String label, Pageable pageable);

    List<IocEntity> findTop10BySeverityScoreIsNotNullOrderBySeverityScoreDesc();

    @Query("SELECT i.source, COUNT(i) FROM IocEntity i GROUP BY i.source")
    List<Object[]> countBySource();

    @Query("SELECT i.severityLabel, COUNT(i) FROM IocEntity i WHERE i.severityLabel IS NOT NULL GROUP BY i.severityLabel")
    List<Object[]> countBySeverityLabel();

    @Query("SELECT i.type, COUNT(i) FROM IocEntity i GROUP BY i.type")
    List<Object[]> countByType();

    @Query("SELECT COUNT(i) FROM IocEntity i WHERE i.rankingStatus = 'PENDING'")
    long countPending();

    Page<IocEntity> findAllByOrderByFirstSeenDesc(Pageable pageable);

    Page<IocEntity> findBySeverityScoreGreaterThanEqualOrderBySeverityScoreDesc(int score, Pageable pageable);
}
