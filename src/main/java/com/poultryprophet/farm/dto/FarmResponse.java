package com.poultryprophet.farm.dto;

import com.poultryprophet.farm.Farm;

public record FarmResponse(
        Long id,
        String name,
        String location,
        String description
) {
    public static FarmResponse from(Farm farm) {
        return new FarmResponse(farm.getId(), farm.getName(), farm.getLocation(), farm.getDescription());
    }
}
