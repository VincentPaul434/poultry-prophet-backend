package com.poultryprophet.analytics.dto;

import com.poultryprophet.analytics.Indicator;

import java.time.Instant;
import java.time.LocalDate;

public record IndicatorResponse(
        Long id,
        Long batchId,
        Long recordId,
        LocalDate recordDate,
        double bhi,
        Double bsi,
        Double wfr,
        double readinessScore,
        Instant computedAt
) {
    /** Must be invoked inside an open persistence context (lazy record/batch access). */
    public static IndicatorResponse from(Indicator i) {
        return new IndicatorResponse(
                i.getId(),
                i.getBatch().getId(),
                i.getRecord().getId(),
                i.getRecord().getRecordDate(),
                i.getBhi(),
                i.getBsi(),
                i.getWfr(),
                i.getReadinessScore(),
                i.getComputedAt());
    }
}
