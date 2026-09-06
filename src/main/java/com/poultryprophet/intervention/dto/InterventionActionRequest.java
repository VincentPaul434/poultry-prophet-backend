package com.poultryprophet.intervention.dto;

import jakarta.validation.constraints.Size;

public record InterventionActionRequest(
        @Size(max = 2000) String note
) {
}
