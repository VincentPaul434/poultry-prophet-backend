package com.poultryprophet.farm.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateFarmRequest(
        @NotBlank String name,
        String location,
        String description
) {
}
