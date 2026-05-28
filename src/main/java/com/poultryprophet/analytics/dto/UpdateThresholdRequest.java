package com.poultryprophet.analytics.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateThresholdRequest(
        @NotNull Double minValue,
        @NotNull Double maxValue
) {
}
