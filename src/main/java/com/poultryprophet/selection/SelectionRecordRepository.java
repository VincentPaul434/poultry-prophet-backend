package com.poultryprophet.selection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SelectionRecordRepository extends JpaRepository<SelectionRecord, Long> {

    Optional<SelectionRecord> findByBirdId(Long birdId);

    List<SelectionRecord> findByBatchId(Long batchId);
}
