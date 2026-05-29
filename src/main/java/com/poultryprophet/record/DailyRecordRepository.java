package com.poultryprophet.record;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {

    Optional<DailyRecord> findByBatchIdAndRecordDate(Long batchId, LocalDate recordDate);

    List<DailyRecord> findByBatchIdOrderByRecordDateDesc(Long batchId, Pageable pageable);

    List<DailyRecord> findByBatchIdOrderByRecordDateAsc(Long batchId);

    List<DailyRecord> findByBatchIdAndRecordDateBetweenOrderByRecordDateAsc(Long batchId,
                                                                            LocalDate start,
                                                                            LocalDate end);

    @Query("select coalesce(sum(r.mortalityCount), 0) from DailyRecord r "
            + "where r.batch.id = :batchId and r.recordDate between :start and :end")
    long sumMortalityBetween(@Param("batchId") Long batchId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);
}
