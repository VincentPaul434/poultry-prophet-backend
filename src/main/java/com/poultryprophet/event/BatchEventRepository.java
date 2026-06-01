package com.poultryprophet.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BatchEventRepository extends JpaRepository<BatchEvent, Long> {

    List<BatchEvent> findByBatchIdOrderByEventDateDescCreatedAtDesc(Long batchId, Pageable pageable);

    List<BatchEvent> findByBatchIdAndEventDateAndEventType(Long batchId, LocalDate date, EventType type);
}
