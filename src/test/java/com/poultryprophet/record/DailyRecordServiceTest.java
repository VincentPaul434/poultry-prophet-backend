package com.poultryprophet.record;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchService;
import com.poultryprophet.event.BatchEvent;
import com.poultryprophet.event.BatchEventRepository;
import com.poultryprophet.event.EventType;
import com.poultryprophet.user.User;
import com.poultryprophet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRecordServiceTest {

    @Mock
    private DailyRecordRepository recordRepository;
    @Mock
    private BatchEventRepository eventRepository;
    @Mock
    private BatchService batchService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher events;
    @InjectMocks
    private DailyRecordService service;

    @Test
    void laterDailyRecordDoesNotDoubleCountStandaloneMortalityEvents() {
        Batch batch = new Batch();
        batch.setId(1L);
        batch.setCurrentPopulation(43);
        User handler = new User();
        LocalDate date = LocalDate.of(2026, 9, 6);
        BatchEvent mortality = new BatchEvent();
        mortality.setEventType(EventType.MORTALITY);
        mortality.setAffectedCount(7);

        when(batchService.requireBatch(1L, 2L)).thenReturn(batch);
        when(recordRepository.findByBatchIdAndRecordDate(1L, date)).thenReturn(Optional.empty());
        when(eventRepository.findByBatchIdAndEventDateAndEventType(1L, date, EventType.MORTALITY))
                .thenReturn(List.of(mortality));
        when(userRepository.getReferenceById(3L)).thenReturn(handler);
        when(recordRepository.save(any(DailyRecord.class))).thenAnswer(invocation -> {
            DailyRecord saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        DailyRecord saved = service.upsert(1L, 2L, 3L, date,
                33.0, 7, 100.0, 200.0, null, Instant.now(), SyncStatus.SYNCED);

        assertThat(saved.getMortalityCount()).isEqualTo(7);
        assertThat(batch.getCurrentPopulation()).isEqualTo(43);
    }
}
