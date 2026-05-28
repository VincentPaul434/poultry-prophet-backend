package com.poultryprophet.alert.dto;

import com.poultryprophet.alert.Alert;
import com.poultryprophet.alert.Severity;

import java.time.Instant;

public record AlertResponse(
        Long id,
        Long batchId,
        Long indicatorId,
        String indicatorType,
        Severity severity,
        String message,
        boolean acknowledged,
        Long acknowledgedByUserId,
        Instant acknowledgedAt,
        String acknowledgmentNote,
        Instant createdAt
) {
    /** Must be invoked inside an open persistence context (lazy associations). */
    public static AlertResponse from(Alert a) {
        return new AlertResponse(
                a.getId(),
                a.getBatch().getId(),
                a.getIndicator() != null ? a.getIndicator().getId() : null,
                a.getIndicatorType(),
                a.getSeverity(),
                a.getMessage(),
                a.getAcknowledgedAt() != null,
                a.getAcknowledgedBy() != null ? a.getAcknowledgedBy().getId() : null,
                a.getAcknowledgedAt(),
                a.getAcknowledgmentNote(),
                a.getCreatedAt());
    }
}
