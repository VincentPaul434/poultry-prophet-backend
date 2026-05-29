package com.poultryprophet.ranging.dto;

import com.poultryprophet.ranging.HealthEventSeverity;
import com.poultryprophet.ranging.QualityRating;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateRangingRecordRequest(
        LocalDate recordDate,
        @NotNull @DecimalMin("0.0") Double weightG,
        HealthEventSeverity healthEvent,
        String temperamentNotes,
        @NotNull QualityRating qualityRating
) {
}
