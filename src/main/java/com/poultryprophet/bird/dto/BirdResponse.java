package com.poultryprophet.bird.dto;

import com.poultryprophet.bird.Bird;

import java.time.Instant;

public record BirdResponse(
        Long id,
        Long batchId,
        String bandNumber,
        String notes,
        Instant createdAt
) {
    public static BirdResponse from(Bird bird) {
        return new BirdResponse(
                bird.getId(),
                bird.getBatch().getId(),
                bird.getBandNumber(),
                bird.getNotes(),
                bird.getCreatedAt());
    }
}
