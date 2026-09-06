package com.poultryprophet.event;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchService;
import com.poultryprophet.event.dto.BatchEventResponse;
import com.poultryprophet.event.dto.CreateBatchEventRequest;
import com.poultryprophet.record.DailyRecord;
import com.poultryprophet.record.DailyRecordRepository;
import com.poultryprophet.user.User;
import com.poultryprophet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchEventServiceTest {

    @Mock
    private BatchEventRepository eventRepository;
    @Mock
    private BatchService batchService;
    @Mock
    private DailyRecordRepository recordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @InjectMocks
    private BatchEventService service;

    @Test
    void mortalityEventWithoutDailyRecordDecrementsPopulation() {
        Batch batch = batch(50);
        User handler = new User();
        LocalDate date = LocalDate.of(2026, 9, 6);
        CreateBatchEventRequest request = mortality(date, 7);
        BatchEvent saved = event(date, 7);

        when(batchService.requireBatch(1L, 2L)).thenReturn(batch);
        when(eventRepository.findByBatchIdAndEventDateAndEventType(1L, date, EventType.MORTALITY))
                .thenReturn(List.of(), List.of(saved));
        when(eventRepository.save(any(BatchEvent.class))).thenReturn(saved);
        when(recordRepository.findByBatchIdAndRecordDate(1L, date)).thenReturn(Optional.empty());
        when(userRepository.findById(3L)).thenReturn(Optional.of(handler));

        BatchEventResponse response = service.create(1L, 2L, 3L, request);

        assertThat(batch.getCurrentPopulation()).isEqualTo(43);
        assertThat(response.affectedCount()).isEqualTo(7);
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void mortalityEventUpdatesExistingDailyRecordByOnlyTheNewEventDeaths() {
        Batch batch = batch(43);
        User handler = new User();
        DailyRecord record = new DailyRecord();
        record.setMortalityCount(7);
        LocalDate date = LocalDate.of(2026, 9, 6);
        CreateBatchEventRequest request = mortality(date, 2);
        BatchEvent saved = event(date, 2);
        BatchEvent previous = event(date, 7);

        when(batchService.requireBatch(1L, 2L)).thenReturn(batch);
        when(eventRepository.findByBatchIdAndEventDateAndEventType(1L, date, EventType.MORTALITY))
                .thenReturn(List.of(previous), List.of(previous, saved));
        when(eventRepository.save(any(BatchEvent.class))).thenReturn(saved);
        when(recordRepository.findByBatchIdAndRecordDate(1L, date))
                .thenReturn(Optional.of(record));
        when(recordRepository.save(record)).thenReturn(record);
        when(userRepository.findById(3L)).thenReturn(Optional.of(handler));

        service.create(1L, 2L, 3L, request);

        assertThat(batch.getCurrentPopulation()).isEqualTo(41);
        assertThat(record.getMortalityCount()).isEqualTo(9);
        verify(publisher).publishEvent(any(Object.class));
    }

    private Batch batch(int currentPopulation) {
        Batch batch = new Batch();
        batch.setCurrentPopulation(currentPopulation);
        return batch;
    }

    private CreateBatchEventRequest mortality(LocalDate date, int count) {
        return new CreateBatchEventRequest(date, EventType.MORTALITY, "Unknown", null,
                count, null, null);
    }

    private BatchEvent event(LocalDate date, int count) {
        BatchEvent event = new BatchEvent();
        event.setEventDate(date);
        event.setAffectedCount(count);
        return event;
    }
}
