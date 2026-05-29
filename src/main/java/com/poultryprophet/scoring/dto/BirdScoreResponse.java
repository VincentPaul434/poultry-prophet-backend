package com.poultryprophet.scoring.dto;

import com.poultryprophet.scoring.BirdScore;

import java.time.Instant;

/** Transparent breakdown of a bird's Conditioning Readiness Score (Blueprint 6). */
public record BirdScoreResponse(
        Long birdId,
        String bandNumber,
        double broodingHealthIndex,
        double growthScore,
        double healthHistoryScore,
        double behaviouralScore,
        double crs,
        Instant computedAt
) {
    public static BirdScoreResponse from(BirdScore score) {
        return new BirdScoreResponse(
                score.getBird().getId(),
                score.getBird().getBandNumber(),
                score.getBroodingHealthIndex(),
                score.getGrowthScore(),
                score.getHealthHistoryScore(),
                score.getBehaviouralScore(),
                score.getCrs(),
                score.getComputedAt());
    }
}
