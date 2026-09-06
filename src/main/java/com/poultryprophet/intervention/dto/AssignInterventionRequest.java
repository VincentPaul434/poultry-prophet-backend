package com.poultryprophet.intervention.dto;

import jakarta.validation.constraints.NotNull;

public record AssignInterventionRequest(
        @NotNull Long handlerUserId
) {
}
