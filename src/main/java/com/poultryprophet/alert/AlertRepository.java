package com.poultryprophet.alert;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    boolean existsByBatchIdAndIndicatorTypeAndAcknowledgedAtIsNull(Long batchId, String indicatorType);

    List<Alert> findByBatchIdAndAcknowledgedAtIsNullOrderByCreatedAtDesc(Long batchId);

    List<Alert> findByBatchIdOrderByCreatedAtDesc(Long batchId, Pageable pageable);

    List<Alert> findByBatchIdAndCreatedAtBetweenOrderByCreatedAtAsc(Long batchId, Instant start, Instant end);

    // Farm-wide feeds for the notifications centre (traverse alert.batch.farmId).
    List<Alert> findByBatch_FarmIdAndAcknowledgedAtIsNullOrderByCreatedAtDesc(Long farmId, Pageable pageable);

    List<Alert> findByBatch_FarmIdOrderByCreatedAtDesc(Long farmId, Pageable pageable);
}
