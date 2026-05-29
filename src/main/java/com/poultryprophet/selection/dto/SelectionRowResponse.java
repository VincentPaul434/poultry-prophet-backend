package com.poultryprophet.selection.dto;

import com.poultryprophet.scoring.BirdScore;
import com.poultryprophet.selection.SelectionRecord;

import java.time.Instant;

/**
 * One row of the ranked month-5 selection view (Blueprint Module 3). Sub-scores are exposed
 * alongside the CRS so every ranking is interrogable. {@code decision} is null until the
 * manager records a confirm/override.
 */
public record SelectionRowResponse(
        int rank,
        Long birdId,
        String bandNumber,
        double broodingHealthIndex,
        double growthScore,
        double healthHistoryScore,
        double behaviouralScore,
        double crs,
        boolean recommendedAdvance,
        Decision decision
) {
    public record Decision(
            String outcome,
            boolean overridden,
            String reason,
            Instant decidedAt
    ) {
        static Decision from(SelectionRecord record) {
            return new Decision(
                    record.getOutcome().name(),
                    record.isOverridden(),
                    record.getReason(),
                    record.getDecidedAt());
        }
    }

    public static SelectionRowResponse from(int rank, BirdScore score, boolean recommendedAdvance,
                                            SelectionRecord decision) {
        return new SelectionRowResponse(
                rank,
                score.getBird().getId(),
                score.getBird().getBandNumber(),
                score.getBroodingHealthIndex(),
                score.getGrowthScore(),
                score.getHealthHistoryScore(),
                score.getBehaviouralScore(),
                score.getCrs(),
                recommendedAdvance,
                decision == null ? null : Decision.from(decision));
    }
}
