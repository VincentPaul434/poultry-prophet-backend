package com.poultryprophet.event;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchService;
import com.poultryprophet.event.dto.BatchEventResponse;
import com.poultryprophet.event.dto.CreateBatchEventRequest;
import com.poultryprophet.record.DailyRecord;
import com.poultryprophet.record.DailyRecordRepository;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BatchEventService {

    private final BatchEventRepository eventRepository;
    private final BatchService batchService;
    private final DailyRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

    public BatchEventService(BatchEventRepository eventRepository,
                             BatchService batchService,
                             DailyRecordRepository recordRepository,
                             UserRepository userRepository,
                             ApplicationEventPublisher publisher) {
        this.eventRepository = eventRepository;
        this.batchService = batchService;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
        this.publisher = publisher;
    }

    @Transactional
    public BatchEventResponse create(Long batchId, Long farmId, Long handlerId,
                                     CreateBatchEventRequest req) {
        Batch batch = batchService.requireBatch(batchId, farmId);
        LocalDate date = req.eventDate() != null ? req.eventDate() : LocalDate.now();

        if (req.eventType() == EventType.MORTALITY && req.affectedCount() > 0) {
            // Sum mortality events already logged for this date (before this one is saved).
            int todayEventMortality = eventRepository
                    .findByBatchIdAndEventDateAndEventType(batchId, date, EventType.MORTALITY)
                    .stream().mapToInt(BatchEvent::getAffectedCount).sum();
            // Mortality already reflected in currentPopulation via DailyRecord (if any).
            int todayRecordMortality = recordRepository
                    .findByBatchIdAndRecordDate(batchId, date)
                    .map(DailyRecord::getMortalityCount).orElse(0);
            // Deaths from events that haven't been written into a DailyRecord yet —
            // they are not yet reflected in currentPopulation, so we must account for them.
            int unaccounted = Math.max(0, todayEventMortality - todayRecordMortality);
            int availableForNewDeaths = batch.getCurrentPopulation() - unaccounted;
            if (req.affectedCount() > availableForNewDeaths) {
                throw new com.poultryprophet.common.BadRequestException(
                        "Cannot record " + req.affectedCount() + " deaths — only " +
                        availableForNewDeaths + " bird" + (availableForNewDeaths == 1 ? "" : "s") +
                        " available (current alive: " + batch.getCurrentPopulation() +
                        ", already logged today: " + unaccounted + ")");
            }
        }

        BatchEvent event = new BatchEvent();
        event.setBatchId(batchId);
        event.setHandlerId(handlerId);
        event.setEventDate(date);
        event.setEventType(req.eventType());
        event.setSeverityLabel(req.severityLabel());
        event.setAffectedCount(req.affectedCount());
        event.setTitle(req.title());
        event.setDetails(req.details());
        event.setTags(req.tags());
        BatchEvent saved = eventRepository.save(event);

        // Propagate to DailyRecord so BHI scoring stays accurate.
        // Only updates an existing record — does not fabricate one with zero vitals.
        if (req.eventType() == EventType.MORTALITY && req.affectedCount() > 0) {
            recordRepository.findByBatchIdAndRecordDate(batchId, date).ifPresent(record -> {
                int total = eventRepository
                        .findByBatchIdAndEventDateAndEventType(batchId, date, EventType.MORTALITY)
                        .stream().mapToInt(BatchEvent::getAffectedCount).sum();
                int delta = total - record.getMortalityCount();
                record.setMortalityCount(total);
                record.setUpdatedAt(Instant.now());
                DailyRecord persisted = recordRepository.save(record);
                batch.setCurrentPopulation(batch.getCurrentPopulation() - delta);
                publisher.publishEvent(new RecordCreatedEvent(persisted.getId(), batchId));
            });
        }

        String handlerName = userRepository.findById(handlerId)
                .map(User::getFullName).orElse("Unknown");
        return BatchEventResponse.from(saved, handlerName);
    }

    @Transactional(readOnly = true)
    public List<BatchEventResponse> recent(Long batchId, Long farmId, int limit) {
        batchService.requireBatch(batchId, farmId);
        List<BatchEvent> events = eventRepository
                .findByBatchIdOrderByEventDateDescCreatedAtDesc(batchId, PageRequest.of(0, limit))
                .stream().toList();
        Set<Long> hids = events.stream().map(BatchEvent::getHandlerId).collect(Collectors.toSet());
        Map<Long, String> names = userRepository.findAllById(hids).stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));
        return events.stream()
                .map(e -> BatchEventResponse.from(e, names.getOrDefault(e.getHandlerId(), "Unknown")))
                .toList();
    }
}
