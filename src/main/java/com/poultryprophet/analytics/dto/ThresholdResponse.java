package com.poultryprophet.analytics.dto;

import com.poultryprophet.analytics.ThresholdConfig;

public record ThresholdResponse(
        Long id,
        Long farmId,
        String indicator,
        double minValue,
        double maxValue
) {
    public static ThresholdResponse from(ThresholdConfig t) {
        return new ThresholdResponse(t.getId(), t.getFarmId(), t.getIndicator(), t.getMinValue(), t.getMaxValue());
    }
}
