package com.poultryprophet.intervention;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {

    Optional<Intervention> findByAlertId(Long alertId);

    List<Intervention> findByBatch_FarmIdOrderByCreatedAtDesc(Long farmId, Pageable pageable);

    List<Intervention> findByBatch_FarmIdAndStatusOrderByCreatedAtDesc(
            Long farmId, InterventionStatus status, Pageable pageable);

    List<Intervention> findByBatchIdOrderByCreatedAtDesc(Long batchId, Pageable pageable);

    List<Intervention> findByBatchIdAndStatusOrderByCreatedAtDesc(
            Long batchId, InterventionStatus status, Pageable pageable);

    List<Intervention> findByBatchIdAndStatusInOrderByCreatedAtDesc(
            Long batchId, List<InterventionStatus> statuses);

    List<Intervention> findByBatchIdInOrderByCreatedAtDesc(List<Long> batchIds, Pageable pageable);

    List<Intervention> findByBatchIdInAndStatusOrderByCreatedAtDesc(
            List<Long> batchIds, InterventionStatus status, Pageable pageable);
}
