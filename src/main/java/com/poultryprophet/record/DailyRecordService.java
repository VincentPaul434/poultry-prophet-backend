package com.poultryprophet.record;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchService;
import com.poultryprophet.record.dto.CreateRecordRequest;
import com.poultryprophet.record.dto.DailyRecordResponse;
import com.poultryprophet.record.event.RecordCreatedEvent;
import com.poultryprophet.user.User;
import com.poultryprophet.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * SDD 1.1: record creation with duplicate-day detection and idempotent upsert. Emits a
 * {@link RecordCreatedEvent} so analytics can recompute indicators.
 */
@Service
public class DailyRecordService {

    private final DailyRecordRepository recordRepository;
    private final BatchService batchService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;

    public DailyRecordService(DailyRecordRepository recordRepository,
                              BatchService batchService,
                              UserRepository userRepository,
                              ApplicationEventPublisher events) {
        this.recordRepository = recordRepository;
        this.batchService = batchService;
        this.userRepository = userRepository;
        this.events = events;
    }

    @Transactional
    public DailyRecordResponse record(Long batchId, Long farmId, CreateRecordRequest req, Long handlerId) {
        LocalDate date = req.recordDate() != null ? req.recordDate() : LocalDate.now();
        DailyRecord saved = upsert(batchId, farmId, handlerId, date,
                req.temperatureC(), req.mortalityCount(), req.feedIntakeG(), req.waterIntakeMl(),
                req.behaviorNotes(), Instant.now(), SyncStatus.SYNCED);
        return DailyRecordResponse.from(saved);
    }

    /**
     * Idempotent upsert keyed on (batch, recordDate). Loads the batch inside this
     * transaction so the mortality-driven population adjustment is persisted, then
     * publishes a record-created event. Used by the online endpoint and offline sync.
     */
    @Transactional
    public DailyRecord upsert(Long batchId, Long farmId, Long handlerId, LocalDate date,
                              double temperatureC, int mortalityCount, double feedIntakeG,
                              double waterIntakeMl, String behaviorNotes,
                              Instant updatedAt, SyncStatus syncStatus) {
        Batch batch = batchService.requireBatch(batchId, farmId);
        DailyRecord record = recordRepository.findByBatchIdAndRecordDate(batch.getId(), date)
                .orElseGet(DailyRecord::new);

        int previousMortality = record.getId() != null ? record.getMortalityCount() : 0;

        User handler = userRepository.getReferenceById(handlerId);
        record.setBatch(batch);
        record.setHandler(handler);
        record.setRecordDate(date);
        record.setTemperatureC(temperatureC);
        record.setMortalityCount(mortalityCount);
        record.setFeedIntakeG(feedIntakeG);
        record.setWaterIntakeMl(waterIntakeMl);
        record.setBehaviorNotes(behaviorNotes);
        record.setSyncStatus(syncStatus);
        record.setUpdatedAt(updatedAt);

        int delta = mortalityCount - previousMortality;
        batch.setCurrentPopulation(Math.max(0, batch.getCurrentPopulation() - delta));

        DailyRecord persisted = recordRepository.save(record);
        events.publishEvent(new RecordCreatedEvent(persisted.getId(), batch.getId()));
        return persisted;
    }

    @Transactional(readOnly = true)
    public List<DailyRecordResponse> recent(Long batchId, Long farmId, int limit) {
        batchService.requireBatch(batchId, farmId);
        return recordRepository
                .findByBatchIdOrderByRecordDateDesc(batchId, PageRequest.of(0, limit))
                .stream()
                .map(DailyRecordResponse::from)
                .toList();
    }
}
