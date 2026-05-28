package com.poultryprophet.batch.dto;

import com.poultryprophet.batch.Batch;
import com.poultryprophet.batch.BatchStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record BatchResponse(
        Long id,
        Long farmId,
        String name,
        int initialPopulation,
        int currentPopulation,
        LocalDate startDate,
        Long stageId,
        String stageName,
        BatchStatus status,
        List<Long> handlerUserIds,
        Instant createdAt
) {
    public static BatchResponse from(Batch batch, List<Long> handlerUserIds) {
        return new BatchResponse(
                batch.getId(),
                batch.getFarmId(),
                batch.getName(),
                batch.getInitialPopulation(),
                batch.getCurrentPopulation(),
                batch.getStartDate(),
                batch.getStage().getId(),
                batch.getStage().getName(),
                batch.getStatus(),
                handlerUserIds,
                batch.getCreatedAt());
    }
}
