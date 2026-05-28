package com.poultryprophet.batch.dto;

import com.poultryprophet.batch.LifecycleStage;

public record LifecycleStageResponse(
        Long id,
        String name,
        int orderIndex
) {
    public static LifecycleStageResponse from(LifecycleStage stage) {
        return new LifecycleStageResponse(stage.getId(), stage.getName(), stage.getOrderIndex());
    }
}
