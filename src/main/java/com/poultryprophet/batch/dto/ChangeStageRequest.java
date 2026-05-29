package com.poultryprophet.batch.dto;

import jakarta.validation.constraints.NotNull;

public record ChangeStageRequest(
        @NotNull Long stageId
) {
}
