package com.poultryprophet.analytics;

import com.poultryprophet.alert.AlertService;
import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchRepository;
import com.poultryprophet.config.AnalyticsProperties;
import com.poultryprophet.realtime.RealtimeNotificationService;
import com.poultryprophet.record.DailyRecord;
import com.poultryprophet.record.DailyRecordRepository;
import com.poultryprophet.record.event.RecordCreatedEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.List;

/**
 * SDD 2.1 IndicatorJobWorker. Replaces the Node.js BullMQ worker: consumes
 * {@link RecordCreatedEvent} after the originating transaction commits, recomputes the
 * batch's indicators off the request thread, persists them, pushes a real-time update, and
 * hands the indicator to the alert engine.
 */
@Component
public class IndicatorJobWorker {

    private final DailyRecordRepository recordRepository;
    private final BatchRepository batchRepository;
    private final IndicatorRepository indicatorRepository;
    private final AnalyticsService analyticsService;
    private final AlertService alertService;
    private final RealtimeNotificationService realtime;
    private final int windowDays;

    public IndicatorJobWorker(DailyRecordRepository recordRepository,
                              BatchRepository batchRepository,
                              IndicatorRepository indicatorRepository,
                              AnalyticsService analyticsService,
                              AlertService alertService,
                              RealtimeNotificationService realtime,
                              AnalyticsProperties props) {
        this.recordRepository = recordRepository;
        this.batchRepository = batchRepository;
        this.indicatorRepository = indicatorRepository;
        this.analyticsService = analyticsService;
        this.alertService = alertService;
        this.realtime = realtime;
        this.windowDays = props.getWindowDays();
    }

    @Async("analyticsExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onRecordCreated(RecordCreatedEvent event) {
        Indicator indicator = recompute(event.batchId(), event.recordId());
        if (indicator == null) {
            return;
        }
        realtime.publishIndicatorUpdated(indicator);
        alertService.evaluate(indicator);
    }

    private Indicator recompute(Long batchId, Long recordId) {
        Batch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) {
            return null;
        }
        List<DailyRecord> recent = recordRepository.findByBatchIdOrderByRecordDateDesc(
                batchId, PageRequest.of(0, windowDays));
        IndicatorResult result = analyticsService.compute(recent, batch);
        if (result == null) {
            return null;
        }
        DailyRecord record = recordRepository.findById(recordId).orElse(recent.get(0));
        Indicator indicator = indicatorRepository.findByRecordId(record.getId())
                .orElseGet(Indicator::new);
        indicator.setRecord(record);
        indicator.setBatch(batch);
        indicator.setBhi(result.bhi());
        indicator.setBsi(result.bsi());
        indicator.setWfr(result.wfr());
        indicator.setReadinessScore(result.readinessScore());
        indicator.setComputedAt(Instant.now());
        return indicatorRepository.save(indicator);
    }
}
