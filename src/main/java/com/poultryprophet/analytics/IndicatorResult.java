package com.poultryprophet.analytics;

/** Immutable output of an analytics computation pass (SDD 2.1/2.2). */
public record IndicatorResult(
        double bhi,
        Double bsi,
        Double wfr,
        double readinessScore
) {
}
