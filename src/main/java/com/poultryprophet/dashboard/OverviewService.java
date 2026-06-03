package com.poultryprophet.dashboard;

import com.poultryprophet.alert.AlertRepository;
import com.poultryprophet.alert.dto.AlertResponse;
import com.poultryprophet.analytics.IndicatorRepository;
import com.poultryprophet.analytics.dto.IndicatorResponse;
import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchHandlerAssignmentRepository;
import com.poultryprophet.batch.BatchService;
import com.poultryprophet.batch.dto.BatchResponse;
import com.poultryprophet.dashboard.dto.BatchOverviewResponse;
import com.poultryprophet.record.DailyRecordRepository;
import com.poultryprophet.record.dto.DailyRecordResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** SDD 3.1: aggregation logic for the batch dashboard. */
@Service
public class OverviewService {

    private static final int RECENT_RECORD_LIMIT = 30;

    private final BatchService batchService;
    private final BatchHandlerAssignmentRepository assignmentRepository;
    private final IndicatorRepository indicatorRepository;
    private final DailyRecordRepository recordRepository;
    private final AlertRepository alertRepository;

    public OverviewService(BatchService batchService,
                           BatchHandlerAssignmentRepository assignmentRepository,
                           IndicatorRepository indicatorRepository,
                           DailyRecordRepository recordRepository,
                           AlertRepository alertRepository) {
        this.batchService = batchService;
        this.assignmentRepository = assignmentRepository;
        this.indicatorRepository = indicatorRepository;
        this.recordRepository = recordRepository;
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public BatchOverviewResponse getOverview(Long batchId, Long farmId) {
        Batch batch = batchService.requireBatch(batchId, farmId);

        BatchService.StageView stageView = batchService.resolveStage(batch);
        BatchResponse batchResponse = BatchResponse.from(
                batch, assignmentRepository.findHandlerUserIdsByBatchId(batchId),
                stageView.stage(), stageView.auto());

        IndicatorResponse latestIndicator = indicatorRepository
                .findFirstByBatchIdOrderByComputedAtDesc(batchId)
                .map(IndicatorResponse::from)
                .orElse(null);

        List<DailyRecordResponse> recentRecords = recordRepository
                .findByBatchIdOrderByRecordDateDesc(batchId, PageRequest.of(0, RECENT_RECORD_LIMIT))
                .stream()
                .map(DailyRecordResponse::from)
                .toList();

        List<AlertResponse> activeAlerts = alertRepository
                .findByBatchIdAndAcknowledgedAtIsNullOrderByCreatedAtDesc(batchId)
                .stream()
                .map(AlertResponse::from)
                .toList();

        return new BatchOverviewResponse(batchResponse, latestIndicator, recentRecords, activeAlerts);
    }
}
