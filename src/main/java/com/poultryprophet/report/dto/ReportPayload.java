package com.poultryprophet.report.dto;

import com.poultryprophet.alert.dto.AlertResponse;
import com.poultryprophet.analytics.dto.IndicatorResponse;

import java.time.LocalDate;
import java.util.List;

/** SDD 3.2: structured report payload (KPI grid + trend + significant alerts). */
public record ReportPayload(
        Long batchId,
        String batchName,
        LocalDate periodStart,
        LocalDate periodEnd,
        Double avgBhi,
        Double avgWfr,
        long totalMortality,
        Double readinessScore,
        List<IndicatorResponse> trend,
        List<AlertResponse> significantAlerts
) {
}
