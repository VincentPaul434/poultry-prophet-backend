package com.poultryprophet.intervention.dto;

import com.poultryprophet.intervention.InterventionHistory;
import com.poultryprophet.intervention.InterventionStatus;

import java.time.Instant;

public record InterventionHistoryResponse(
        Long id,
        InterventionStatus status,
        String action,
        Long actorUserId,
        String actorName,
        String note,
        Instant createdAt
) {
    public static InterventionHistoryResponse from(InterventionHistory history) {
        return new InterventionHistoryResponse(
                history.getId(),
                history.getStatus(),
                history.getAction(),
                history.getActor() != null ? history.getActor().getId() : null,
                history.getActor() != null ? history.getActor().getFullName() : null,
                history.getNote(),
                history.getCreatedAt());
    }
}
