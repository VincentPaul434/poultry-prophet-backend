package com.poultryprophet.intervention.dto;

import com.poultryprophet.alert.Severity;
import com.poultryprophet.intervention.Intervention;
import com.poultryprophet.intervention.InterventionStatus;

import java.time.Instant;

/** Farm-scoped STOMP event for newly created and updated interventions. */
public record InterventionEvent(
        Long interventionId,
        Long alertId,
        Long batchId,
        String indicatorType,
        Severity severity,
        String title,
        InterventionStatus status,
        Long assignedHandlerId,
        Instant occurredAt
) {
    public static InterventionEvent from(Intervention intervention) {
        return new InterventionEvent(
                intervention.getId(),
                intervention.getAlert().getId(),
                intervention.getBatch().getId(),
                intervention.getIndicatorType(),
                intervention.getSeverity(),
                intervention.getTitle(),
                intervention.getStatus(),
                intervention.getAssignedTo() != null ? intervention.getAssignedTo().getId() : null,
                intervention.getUpdatedAt());
    }
}
