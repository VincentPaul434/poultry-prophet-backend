package com.poultryprophet.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IndicatorRepository extends JpaRepository<Indicator, Long> {

    Optional<Indicator> findByRecordId(Long recordId);

    Optional<Indicator> findFirstByBatchIdOrderByComputedAtDesc(Long batchId);

    List<Indicator> findByBatchIdOrderByComputedAtDesc(Long batchId, Pageable pageable);

    List<Indicator> findByBatchIdAndComputedAtBetweenOrderByComputedAtAsc(Long batchId,
                                                                          Instant start,
                                                                          Instant end);

    @Query("select avg(i.bhi) from Indicator i where i.batch.id = :batchId "
            + "and i.computedAt between :start and :end")
    Double avgBhiBetween(@Param("batchId") Long batchId,
                         @Param("start") Instant start,
                         @Param("end") Instant end);

    @Query("select avg(i.wfr) from Indicator i where i.batch.id = :batchId "
            + "and i.computedAt between :start and :end")
    Double avgWfrBetween(@Param("batchId") Long batchId,
                         @Param("start") Instant start,
                         @Param("end") Instant end);
}
