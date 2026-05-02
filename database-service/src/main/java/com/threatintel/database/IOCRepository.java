package com.threatintel.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOCRepository extends JpaRepository<IOCEntity, Long> {
    Optional<IOCEntity> findByValue(String value);
    List<IOCEntity> findByType(String type);
    List<IOCEntity> findBySeverityLevel(String severityLevel);
    List<IOCEntity> findBySource(String source);

    @Query("SELECT i FROM IOCEntity i ORDER BY i.severityScore DESC")
    List<IOCEntity> findAllOrderBySeverity();

    @Query("SELECT i.severityLevel, COUNT(i) FROM IOCEntity i GROUP BY i.severityLevel")
    List<Object[]> countBySeverityLevel();

    @Query("SELECT i.source, COUNT(i) FROM IOCEntity i GROUP BY i.source")
    List<Object[]> countBySource();

    @Query("SELECT i.type, COUNT(i) FROM IOCEntity i GROUP BY i.type")
    List<Object[]> countByType();
}
