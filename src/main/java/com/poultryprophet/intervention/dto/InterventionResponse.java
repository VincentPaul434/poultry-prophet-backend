package com.poultryprophet.intervention.dto;

import com.poultryprophet.alert.Severity;
import com.poultryprophet.intervention.Intervention;
import com.poultryprophet.intervention.InterventionStatus;

import java.time.Instant;

public record InterventionResponse(
        Long id,
        Long alertId,
        Long batchId,
        String indicatorType,
        Severity severity,
        String title,
        String instructions,
        InterventionStatus status,
        boolean managerReviewRequired,
        Long assignedHandlerId,
        String assignedHandlerName,
        Long resolvedByUserId,
        String outcomeNote,
        Instant acknowledgedAt,
        Instant startedAt,
        Instant completedAt,
        Instant escalatedAt,
        Instant dismissedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static InterventionResponse from(Intervention intervention) {
        return new InterventionResponse(
                intervention.getId(),
                intervention.getAlert().getId(),
                intervention.getBatch().getId(),
                intervention.getIndicatorType(),
                intervention.getSeverity(),
                intervention.getTitle(),
                intervention.getInstructions(),
                intervention.getStatus(),
                intervention.isManagerReviewRequired(),
                intervention.getAssignedTo() != null ? intervention.getAssignedTo().getId() : null,
                intervention.getAssignedTo() != null ? intervention.getAssignedTo().getFullName() : null,
                intervention.getResolvedBy() != null ? intervention.getResolvedBy().getId() : null,
                intervention.getOutcomeNote(),
                intervention.getAcknowledgedAt(),
                intervention.getStartedAt(),
                intervention.getCompletedAt(),
                intervention.getEscalatedAt(),
                intervention.getDismissedAt(),
                intervention.getCreatedAt(),
                intervention.getUpdatedAt());
    }
}
