package com.poultryprophet.bird.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBirdRequest(
        @NotBlank String bandNumber,
        String notes
) {
}
