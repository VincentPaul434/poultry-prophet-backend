package com.poultryprophet.batch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateBatchRequest(
        @NotBlank String name,
        @NotNull @Min(1) Integer initialPopulation,
        @NotNull LocalDate startDate,
        @NotNull Long stageId,
        List<Long> handlerUserIds
) {
}
