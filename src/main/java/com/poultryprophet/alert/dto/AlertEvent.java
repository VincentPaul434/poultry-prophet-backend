package com.poultryprophet.alert.dto;

import com.poultryprophet.alert.Alert;
import com.poultryprophet.alert.Severity;

import java.time.Instant;

/** SDD 3.3: self-contained payload broadcast over the socket when an alert is created. */
public record AlertEvent(
        Long alertId,
        Long batchId,
        Severity severity,
        String indicatorType,
        String summary,
        Instant occurredAt
) {
    public static AlertEvent from(Alert a) {
        return new AlertEvent(
                a.getId(),
                a.getBatch().getId(),
                a.getSeverity(),
                a.getIndicatorType(),
                a.getMessage(),
                a.getCreatedAt());
    }
}
