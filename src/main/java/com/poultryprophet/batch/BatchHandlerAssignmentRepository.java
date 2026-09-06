package com.poultryprophet.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BatchHandlerAssignmentRepository extends JpaRepository<BatchHandlerAssignment, Long> {

    List<BatchHandlerAssignment> findByBatchId(Long batchId);

    List<BatchHandlerAssignment> findByUserId(Long userId);

    @Query("select a.user.id from BatchHandlerAssignment a where a.batch.id = :batchId")
    List<Long> findHandlerUserIdsByBatchId(Long batchId);

    boolean existsByBatchIdAndUserId(Long batchId, Long userId);
}
