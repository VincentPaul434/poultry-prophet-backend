package com.poultryprophet.record.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRecordRequest(
        LocalDate recordDate,
        @NotNull @DecimalMin("0.0") @DecimalMax("60.0") Double temperatureC,
        @NotNull @Min(0) Integer mortalityCount,
        @NotNull @DecimalMin("0.0") Double feedIntakeG,
        @NotNull @DecimalMin("0.0") Double waterIntakeMl,
        String behaviorNotes
) {
}
