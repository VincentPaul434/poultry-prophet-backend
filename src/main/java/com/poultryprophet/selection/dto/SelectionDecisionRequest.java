package com.poultryprophet.selection.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Manager's month-5 decision. {@code advance=true} advances the bird to pre-conditioning.
 * A {@code reason} is required when the decision overrides the system recommendation.
 */
public record SelectionDecisionRequest(
        @NotNull Boolean advance,
        String reason
) {
}
