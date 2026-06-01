package com.poultryprophet.batch.dto;

import com.poultryprophet.batch.DevelopmentStage;

import java.time.LocalDate;
import java.util.List;

public record BatchTrackingResponse(
        Long batchId,
        String batchName,
        LocalDate hatchingDate,
        long daysElapsed,
        DevelopmentStage currentStage,
        String currentStageDisplayName,
        List<StageTrackerItem> stageTracker
) {}
