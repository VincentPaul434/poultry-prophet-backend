package com.poultryprophet.batch.dto;

import com.poultryprophet.batch.DevelopmentStage;

public record StageTrackerItem(
        DevelopmentStage stage,
        String displayName,
        int startDay,
        int endDay,
        String status
) {}
