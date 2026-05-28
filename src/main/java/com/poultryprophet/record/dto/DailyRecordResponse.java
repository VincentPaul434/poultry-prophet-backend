package com.poultryprophet.record.dto;

import com.poultryprophet.record.DailyRecord;
import com.poultryprophet.record.SyncStatus;

import java.time.Instant;
import java.time.LocalDate;

public record DailyRecordResponse(
        Long id,
        Long batchId,
        Long handlerId,
        LocalDate recordDate,
        double temperatureC,
        int mortalityCount,
        double feedIntakeG,
        double waterIntakeMl,
        String behaviorNotes,
        SyncStatus syncStatus,
        Instant createdAt
) {
    public static DailyRecordResponse from(DailyRecord r) {
        return new DailyRecordResponse(
                r.getId(),
                r.getBatch().getId(),
                r.getHandler().getId(),
                r.getRecordDate(),
                r.getTemperatureC(),
                r.getMortalityCount(),
                r.getFeedIntakeG(),
                r.getWaterIntakeMl(),
                r.getBehaviorNotes(),
                r.getSyncStatus(),
                r.getCreatedAt());
    }
}
