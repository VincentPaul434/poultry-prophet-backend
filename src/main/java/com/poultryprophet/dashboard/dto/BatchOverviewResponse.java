package com.poultryprophet.dashboard.dto;

import com.poultryprophet.alert.dto.AlertResponse;
import com.poultryprophet.analytics.dto.IndicatorResponse;
import com.poultryprophet.batch.dto.BatchResponse;
import com.poultryprophet.record.dto.DailyRecordResponse;

import java.util.List;

/** SDD 3.1: composite payload aggregating recent indicators, records and active alerts. */
public record BatchOverviewResponse(
        BatchResponse batch,
        IndicatorResponse latestIndicator,
        List<DailyRecordResponse> recentRecords,
        List<AlertResponse> activeAlerts
) {
}
