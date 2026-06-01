package com.poultryprophet.event.dto;

import com.poultryprophet.event.EventType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBatchEventRequest(
        LocalDate eventDate,
        @NotNull EventType eventType,
        @NotBlank String title,
        String severityLabel,
        @Min(0) int affectedCount,
        String details,
        String tags
) {}
