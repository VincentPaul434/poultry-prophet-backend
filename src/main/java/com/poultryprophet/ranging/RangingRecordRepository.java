package com.poultryprophet.ranging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RangingRecordRepository extends JpaRepository<RangingRecord, Long> {

    Optional<RangingRecord> findByBirdIdAndRecordDate(Long birdId, LocalDate recordDate);

    List<RangingRecord> findByBirdIdOrderByRecordDateAsc(Long birdId);
}
